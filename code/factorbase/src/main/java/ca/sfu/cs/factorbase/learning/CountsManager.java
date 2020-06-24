package ca.sfu.cs.factorbase.learning;

import java.sql.Connection;

/*zqian, April 1st, 2014 fixed the bug of too many connections by adding con4.close()*/


/*Jun 25,2013 @zqian
 * 
 * Trying to conquer the bottleneck of creating false tables (the join issue) by implementing our sort_merge algorithm.
 * Great stuff!
 * 
 * Here we have some different versions.
 * for version 3, naive implementing of sort merge with "load into" command in terms of efficiency issue of mysql insertion.
 * for version 4, concatenating the order by columns into one column when version 3 can not finish the order by .
 * 
 * for version 5, it's a kind of more complicated approach by pre-compressing all the attribute columns into one column, and then employing concatenating trick again on order by part.
 *   this version still has some bugs that need to be investagiated.
 *   
 * Preconditions: database_BN  has been created with lattice information and functor information.
 *  
 * */

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;
import ca.sfu.cs.factorbase.lattice.LatticeGenerator;
import ca.sfu.cs.factorbase.lattice.RelationshipLattice;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;
import ca.sfu.cs.factorbase.util.QueryGenerator;
import ca.sfu.cs.factorbase.util.RuntimeLogger;
import ca.sfu.cs.factorbase.util.Sort_merge3;

public class CountsManager {

    private static Connection dbConnection;
    private static int tableID;
    private static Map<String, String> ctTablesCache = new HashMap<String, String>();
    private static String databaseName_std;
    private static String databaseName_BN;
    private static String databaseName_CT;
    private static String databaseName_CT_cache;
    private static String databaseName_global_counts;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbaddress;
    private static String linkCorrelation;
    private static long dbTemporaryTableSize;
    /*
     * cont is Continuous
     * ToDo: Refactor
     */
    private static String cont;
    private static Logger logger = Logger.getLogger(CountsManager.class.getName());

    static {
        setVarsFromConfig();
    }


    /**
     * Build the CT tables based on the FunctorSet.
     *
     * @param countingStrategy - {@link CountingStrategy} to indicate how counts related tables should be generated.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    public static void buildCT(
        CountingStrategy countingStrategy
    ) throws SQLException {
        RuntimeLogger.addLogEntry(dbConnection);
        try (Statement statement = dbConnection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + databaseName_CT + ";");
            statement.execute("CREATE SCHEMA " + databaseName_CT + " /*M!100316 COLLATE utf8_general_ci*/;");
        }

        // Propagate metadata based on the FunctorSet.
        dbConnection.setCatalog(databaseName_BN);
        RelationshipLattice relationshipLattice = propagateFunctorSetInfo(dbConnection);

        // building CT tables for Rchain
        CTGenerator(relationshipLattice, countingStrategy);
    }


    /**
     * Use the FunctorSet to generate the necessary metadata for constructing CT tables.
     *
     * @param dbConnection - connection to the "_BN" database.
     * @return the relationship lattice created based on the FunctorSet.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static RelationshipLattice propagateFunctorSetInfo(Connection dbConnection) throws SQLException {
        // Transfer metadata from the "_setup" database to the "_BN" database based on the FunctorSet.
        long start = System.currentTimeMillis();
        MySQLScriptRunner.callSP(
            dbConnection,
            "cascadeFS"
        );
        RuntimeLogger.updateLogEntry(dbConnection, "cascadeFS", System.currentTimeMillis() - start);

        // Generate the relationship lattice based on the FunctorSet.
        start = System.currentTimeMillis();
        RelationshipLattice relationshipLattice = LatticeGenerator.generate(
            dbConnection,
            databaseName_std
        );
        RuntimeLogger.updateLogEntry(dbConnection, "lattice", System.currentTimeMillis() - start);

        // TODO: Add support for Continuous = 1.
        if (cont.equals("1")) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
        } else {
            start = System.currentTimeMillis();
            MySQLScriptRunner.callSP(
                dbConnection,
                "populateMQ"
            );
            RuntimeLogger.updateLogEntry(dbConnection, "populateMQ", System.currentTimeMillis() - start);
        }

        start = System.currentTimeMillis();
        MySQLScriptRunner.callSP(
            dbConnection,
            "populateMQRChain"
        );
        RuntimeLogger.updateLogEntry(dbConnection, "populateMQRChain", System.currentTimeMillis() - start);

        return relationshipLattice;
    }


    /**
     * Generate the contingency tables for the given relationship lattice.
     *
     * @param relationshipLattice - the relationship lattice used to determine which contingency tables to generate.
     * @param countingStrategy - {@link CountingStrategy} to indicate how counts related tables should be generated.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void CTGenerator(
        RelationshipLattice relationshipLattice,
        CountingStrategy countingStrategy
    ) throws SQLException {
        int latticeHeight = relationshipLattice.getHeight();

        // Build the counts tables for the RChains.
        buildRChainCounts(
            databaseName_CT,
            relationshipLattice,
            countingStrategy.useProjection(),
            countingStrategy.getStorageEngine()
        );

        long l = System.currentTimeMillis(); //@zqian : CT table generating time
           // handling Pvars, generating pvars_counts       
        buildPVarsCounts(countingStrategy);
        RuntimeLogger.updateLogEntry(dbConnection, "buildPVarsCounts", System.currentTimeMillis() - l);

        // preparing the _join part for _CT tables
        long start = System.currentTimeMillis();
        Map<String, String> joinTableQueries = createJoinTableQueries();
        RuntimeLogger.updateLogEntry(dbConnection, "createJoinTableQueries", System.currentTimeMillis() - start);

        if (linkCorrelation.equals("1") && relationshipLattice.getHeight() != 0) {
            start = System.currentTimeMillis();

            // Retrieve the first level of the lattice.
            List<FunctorNodesInfo> rchainInfos = relationshipLattice.getRChainsInfo(1);

            BuildRNodesCTMethod<
                String,
                String,
                String,
                String,
                String,
                String,
                SQLException
            > buildCTMethod = CountsManager::buildRNodesCT;

            // Use the buildRNodesCTFromCache method if we have been specified to read the counts from the cache.
            if (countingStrategy.useCTCache()) {
                buildCTMethod = CountsManager::buildRNodesCTFromCache;
            }

            // Build the CT tables for the first level of the relationship lattice.
            for (FunctorNodesInfo rnodeInfo : rchainInfos) {
                String rnode = rnodeInfo.getID();
                String shortRNode = rnodeInfo.getShortID();
                logger.fine("RNode: " + rnode);
                logger.fine("Short RNode: " + shortRNode);
                String ctTableName = shortRNode + "_CT";
                String ctCreationQuery = buildRNodeCTCreationQuery(rnode, shortRNode, joinTableQueries);

                buildCTMethod.apply(
                    databaseName_CT,
                    ctTableName,
                    countingStrategy.getStorageEngine(),
                    ctCreationQuery,
                    rnode,
                    shortRNode
                );
            }

            //building the _CT tables. Going up the Rchain lattice
            for(int len = 2; len <= latticeHeight; len++) {
                rchainInfos = relationshipLattice.getRChainsInfo(len);
                buildRChainsCT(rchainInfos, len, joinTableQueries, countingStrategy.getStorageEngine());
            }
            RuntimeLogger.adjustLogEntryValue(dbConnection, "buildFlatStarCT", System.currentTimeMillis() - start);
        }

        long l2 = System.currentTimeMillis();  //@zqian
        logger.fine("Building Time(ms) for ALL CT tables:  "+(l2-l)+" ms.\n");
    }


    /**
     * Build the CT table for the given RNode (relationship lattice length = 1).
     *
     * @param targetDatabaseName - the name of the database to generate the table in.
     * @param ctTableName - the name of the  table to generate.
     * @param storageEngine - the storage engine to use for the tables generated in general.
     * @param ctCreationQuery - SELECT query used to create the CT table.
     * @param rnode - the name of the RNode to generate the CT table for.
     * @param shortRNode - the short name of the RNode to generate the CT table for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRNodesCT(
        String targetDatabaseName,
        String ctTableName,
        String storageEngine,
        String ctCreationQuery,
        String rnode,
        String shortRNode
    ) throws SQLException {
        long start = System.currentTimeMillis();
        generateCountsTable(
            databaseName_CT,
            rnode,
            shortRNode,
            true,
            storageEngine
        );
        long rcountsRuntime = System.currentTimeMillis() - start;

        // Add the runtime to the buildRChainCounts portion since that is where the runtime belongs to.
        RuntimeLogger.adjustLogEntryValue(dbConnection, "buildRChainCounts", rcountsRuntime);
        // Subtract this time from the buildFlatStarCT to make sure the times are correct.
        RuntimeLogger.adjustLogEntryValue(dbConnection, "buildFlatStarCT", -1 * rcountsRuntime);

        // Build the _star table.
        buildRNodeStar(rnode, shortRNode);

        // Build the _flat table.
        buildRNodeFlat(rnode, shortRNode, storageEngine);

        // Build the _false table.
        buildRNodeFalse(shortRNode);

        // Build the _CT table.
        String createCTQuery = QueryGenerator.createSimpleCreateTableQuery(
            ctTableName,
            storageEngine,
            ctCreationQuery
        );
        start = System.currentTimeMillis();
        dbConnection.setCatalog(targetDatabaseName);
        logger.fine("\nCREATE CT table String: " + createCTQuery);
        try (Statement statement = dbConnection.createStatement()) {
            statement.executeUpdate(createCTQuery);
        }

        logger.fine("Build Time for RNode CT: " + (System.currentTimeMillis() - start) + "ms.\n");
    }


    /**
     * Build the CT table for the given RNode (relationship lattice length = 1) using the CT cache database.
     *
     * @param targetDatabaseName - the name of the database to generate the table in.
     * @param ctTableName - the name of the  table to generate.
     * @param storageEngine - the storage engine to use for the tables generated in general.
     * @param ctCreationQuery - SELECT query used to create the CT table.
     * @param rnode - the name of the RNode to generate the CT table for.
     * @param shortRNode - the short name of the RNode to generate the CT table for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRNodesCTFromCache(
        String targetDatabaseName,
        String ctTableName,
        String storageEngine,
        String ctCreationQuery,
        String rnode,
        String shortRNode
    ) throws SQLException {
        String ctTablesCacheKey = ctTableName + ctCreationQuery;
        String cacheTableName = ctTablesCache.get(ctTablesCacheKey);
        long start = System.currentTimeMillis();
        if (cacheTableName == null) {
            // Create the table in the CT cache database.
            cacheTableName = ctTableName + "_" + tableID;
            tableID++;
            buildRNodesCT(
                databaseName_CT_cache,
                cacheTableName,
                storageEngine,
                ctCreationQuery,
                rnode,
                shortRNode
            );

            ctTablesCache.put(ctTablesCacheKey, cacheTableName);
        }

        dbConnection.setCatalog(databaseName_CT);
        try (Statement createViewStatement = dbConnection.createStatement()) {
            String viewQuery =
                "CREATE VIEW " + ctTableName + " AS " +
                "SELECT * " +
                "FROM " + databaseName_CT_cache + "." + cacheTableName;
            createViewStatement.executeUpdate(viewQuery);
        }

        logger.fine("Build Time for RNode CT: " + (System.currentTimeMillis() - start) + "ms.\n");
    }


    /**
     * Generate the global counts tables.
     */
    public static void buildRChainsGlobalCounts() throws SQLException {
        RuntimeLogger.addLogEntry(dbConnection);

        // Propagate metadata based on the FunctorSet.
        dbConnection.setCatalog(databaseName_BN);
        RelationshipLattice relationshipLattice = propagateFunctorSetInfo(dbConnection);

        // Generate the global counts in the "_global_counts" database.
        buildRChainCounts(databaseName_global_counts, relationshipLattice, false, "MEMORY");
    }


    /**
     * Build the "_counts" tables for the RChains in the given relationship lattice.
     *
     * @param dbTargetName - name of the database to create the "_counts" tables in.
     * @param relationshipLattice - the relationship lattice containing the RChains to build the "_counts" tables for.
     * @param buildByProjection - True if the counts table should be built by projecting the necessary information from
                                  the global counts table; otherwise false.
     * @param storageEngine - the storage engine to use for the tables created when executing this method.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRChainCounts(
        String dbTargetName,
        RelationshipLattice relationshipLattice,
        boolean buildByProjection,
        String storageEngine
    ) throws SQLException {
        long start = System.currentTimeMillis();
        int latticeHeight = relationshipLattice.getHeight();

        // Building the <RChain>_counts tables.
        boolean copyToCT = true;
        if(linkCorrelation.equals("1")) {
            copyToCT = false;
        }

        /**
         *  Skip building the first level (RNodes) if we aren't building the global counts tables since the CT table
         *  might already exist in the "_CT_cache" database, in which case the "_counts" tables for a given RNode does
         *  not need to be created.
         */
        int startingHeight = 2;
        if (dbTargetName.equals(databaseName_global_counts)) {
            startingHeight = 1;
        }

        // Generate the counts tables and copy their values to the CT tables if specified to.
        for(int len = startingHeight; len <= latticeHeight; len++) {
            generateCountsTables(
                dbTargetName,
                relationshipLattice.getRChainsInfo(len),
                copyToCT,
                buildByProjection,
                storageEngine
            );
        }

        RuntimeLogger.updateLogEntry(dbConnection, "buildRChainCounts", System.currentTimeMillis() - start);
    }


    /**
     * setVarsFromConfig
     * ToDo : Remove Duplicate definitions across java files
     */
    private static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName_std = conf.getProperty("dbname");
        databaseName_BN = databaseName_std + "_BN";
        databaseName_CT_cache = databaseName_std + "_CT_cache";
        databaseName_global_counts = databaseName_std + "_global_counts";
        databaseName_CT = databaseName_std + "_CT";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        dbTemporaryTableSize = Math.round(1024 * 1024 * 1024 * Double.valueOf(conf.getProperty("dbtemporarytablesize")));
        linkCorrelation = conf.getProperty("LinkCorrelations");
        cont = conf.getProperty("Continuous");
    }

    /**
     * Connect to database via MySQL JDBC driver
     */
    private static Connection connectDB(String databaseName) throws SQLException {
        Properties connectionProperties = MySQLFactorBaseDataBase.getConnectionStringProperties(
            dbUsername,
            dbPassword
        );

        String CONN_STR = "jdbc:" + dbaddress + "/" + databaseName;
        try {
            java.lang.Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        return DriverManager.getConnection(CONN_STR, connectionProperties);
    }


    /**
     * Building the _CT tables. Going up the relationship lattice (When RChain.length >=2).
     * <p>
     * For each RChain of the specified length:<br>
     * 1. Find the list of members for the RChain.  Suppose first member is rnid1.<br>
     * 2. Initialize current_ct = rchain_counts after summing out the relational attributes of rnid1.<br>
     * 3. Current list = all members of RChain minus rnid1.  Find CT table for current list =
     *    Select rows where all members of current list are true.  Add 1nodes of rnid1.<br>
     * 4. Compute false table using the results of 2 and 3 (basically 2 - 3).<br>
     * 5. Union false table with current_ct to get new ct where all members of current list are true.<br>
     * 6. Repeat with current list as initial list until list is empty.<br><br>
     * Example:<br>
     * 1. RChain = R3,R2,R1.  rnid1 = R3.<br>
     * 2. Find `R3,R2,R1_counts`.  Sum out fields from R3 to get `R2,R1-R3_flat1`.<br>
     * 3. Current list = R2,R1.  Find `R2,R1_ct` where R1 = T, R2 = T.  Add 1nodes of R3 (multiplying) to get
     *    `R2,R1-R3_star`.<br>
     * 4. Compute `R2,R1-R3_false` = `R2,R1-R3_star - `R2,R1-R3_flat1`.<br>
     * 5. Compute `R2,R1-R3_ct` = `R2,R1-R3_false` Cartesian Product `R3_join` union `R3,R2,R1_counts`.<br>
     * 6. Current list = R1.  Current rnid1 = R2.  Current ct_table = `R2,R1-R3_ct`.
     * </p>
     * @param rchainInfos - FunctorNodesInfos for the RChains to build the "_CT" tables for.
     * @param len - length of the RChains to consider.
     * @param joinTableQueries - {@code Map} to retrieve the associated query to create a derived JOIN table.
     * @param storageEngine - the storage engine to use for the tables created when executing this method.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRChainsCT(
        List<FunctorNodesInfo> rchainInfos,
        int len,
        Map<String, String> joinTableQueries,
        String storageEngine
    ) throws SQLException {
        logger.fine("\n ****************** \n" +
                "Building the _CT tables for Length = "+len +"\n" );
        int fc=0;
        for (FunctorNodesInfo rchainInfo : rchainInfos)
        {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain : " + shortRchain);
            // Oct 16 2013
            // initialize the cur_CT_Table, at very beginning we will use _counts table to create the _flat table
            String cur_CT_Table = shortRchain + "_counts";
            logger.fine(" cur_CT_Table : " + cur_CT_Table);
            // counts represents the ct tables where all relationships in Rchain are true

            //  create new statement
            dbConnection.setCatalog(databaseName_BN);
            Statement st1 = dbConnection.createStatement();
            ResultSet rs1 = st1.executeQuery(
                "SELECT removed, short_rnid " +
                "FROM lattice_rel " +
                "JOIN lattice_mapping " +
                "ON lattice_rel.removed = lattice_mapping.orig_rnid " +
                "WHERE child = '" + rchain + "' " +
                "ORDER BY removed ASC;"
            ); // members of rchain

            while(rs1.next())
            {       
                long l2 = System.currentTimeMillis(); 
                String removed = rs1.getString("removed");
                logger.fine("\n removed : " + removed);  
                String removedShort = rs1.getString("short_rnid");
                logger.fine("\n removed short : " + removedShort);
                String BaseName = shortRchain + "_" + removedShort;
                logger.fine(" BaseName : " + BaseName );

                dbConnection.setCatalog(databaseName_BN);
                Statement st2 = dbConnection.createStatement();

                //  create select query string  
                ResultSet rs2 = st2.executeQuery(
                    QueryGenerator.createMetaQueriesExtractionQuery(
                        rchain,
                        "Star",
                        "SELECT",
                        removed,
                        false
                    )
                );
                List<String> columns = extractEntries(rs2, "Entries");
                String selectString = String.join(", ", columns);
                logger.fine("Select String : " + selectString);
                rs2.close();
                //  create mult query string
                ResultSet rs3 = st2.executeQuery(
                    QueryGenerator.createMetaQueriesExtractionQuery(
                        rchain,
                        "Star",
                        "FROM",
                        removed,
                        false
                    )
                );
                columns = extractEntries(rs3, "Entries");
                String MultString = makeStarSepQuery(columns);
                logger.fine("Mult String : " + MultString+ " as `MULT`");
                rs3.close();
                //  create from query string
                String fromString = String.join(", ", columns);
                logger.fine("From String : " + fromString);          
                //  create where query string
                ResultSet rs5 = st2.executeQuery(
                    QueryGenerator.createMetaQueriesExtractionQuery(
                        rchain,
                        "Star",
                        "WHERE",
                        removed,
                        false
                    )
                );
                columns = extractEntries(rs5, "Entries");
                String whereString = String.join(" AND ", columns);
               logger.fine("Where String : " + whereString);
                rs5.close();
                //  create the final query
                String queryString ="";
                if (!selectString.isEmpty() && !whereString.isEmpty()) {
                    queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString  + " where " + whereString;
                } else if (!selectString.isEmpty()) {
                    queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString;
                } else if (!whereString.isEmpty()) {
                    queryString =
                        "SELECT " + MultString + " AS `MULT` " +
                        "FROM " + fromString  + " " +
                        "WHERE " + whereString;
                } else {
                    queryString =
                        "SELECT " + MultString + " AS `MULT` " +
                        "FROM " + fromString;
                }
                logger.fine("Query String : " + queryString );   

                dbConnection.setCatalog(databaseName_CT);
                Statement st3 = dbConnection.createStatement();

                //make the rnid shorter 
                String rnid_or=removedShort;
            
                String cur_star_Table = removedShort + len + "_" + fc + "_star";
                String createStarString = QueryGenerator.createSimpleCreateViewQuery(
                    cur_star_Table,
                    queryString
                );

                logger.fine("\n create star String : " + createStarString );
                st3.execute(createStarString);      //create star table     

                long l3 = System.currentTimeMillis(); 
                logger.fine("Building Time(ms) for "+cur_star_Table+ " : "+(l3-l2)+" ms.\n");
                //staring to create the _flat table
                // Oct 16 2013
                // cur_CT_Table should be the one generated in the previous iteration
                // for the very first iteration, it's _counts table
                logger.fine("cur_CT_Table is : " + cur_CT_Table);

                String cur_flat_Table = removedShort + len + "_" + fc + "_flat";
                String queryStringflat = "SELECT SUM(`" + cur_CT_Table + "`.MULT) AS 'MULT' ";

                if (!selectString.isEmpty()) {
                    queryStringflat +=
                        ", " + selectString + " " +
                        "FROM `" + cur_CT_Table + "` " +
                        "GROUP BY " + selectString + ";";
                } else {
                    queryStringflat +=
                        "FROM `" + cur_CT_Table + "`;";
                }

                String createStringflat = QueryGenerator.createSimpleCreateTableQuery(
                    cur_flat_Table,
                    storageEngine,
                    queryStringflat
                );
                logger.fine("\n create flat String : " + createStringflat );         
                st3.execute(createStringflat);      //create flat table

                // Add covering index.
                addCoveringIndex(
                    dbConnection,
                    databaseName_CT,
                    cur_flat_Table
                );

                long l4 = System.currentTimeMillis(); 
                logger.fine("Building Time(ms) for "+cur_flat_Table+ " : "+(l4-l3)+" ms.\n");
                /**********starting to create _flase table***using sort_merge*******************************/
                // starting to create _flase table : part1
                String cur_false_Table = removedShort + len + "_" + fc + "_false";

                // Computing the false table as the MULT difference between the matching rows of the star and flat tables.
                // This is a big join!
                Sort_merge3.sort_merge(
                    dbConnection,
                    cur_star_Table,
                    cur_flat_Table,
                    cur_false_Table
                );

                long l5 = System.currentTimeMillis(); 
                logger.fine("Building Time(ms) for "+cur_false_Table+ " : "+(l5-l4)+" ms.\n");

                // staring to create the CT table
                ResultSet rs_45 = st2.executeQuery(
                    "SELECT column_name AS Entries " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = '" + databaseName_CT + "' " +
                    "AND table_name = '" + cur_CT_Table + "';"
                );
                columns = extractEntries(rs_45, "Entries");
                String CTJoinString = makeEscapedCommaSepQuery(columns);
                logger.fine("CT Join String : " + CTJoinString);

                //join false table with join table to add in rnid (= F) and 2nid (= n/a). then can union with CT table
                String QueryStringCT =
                    "SELECT " + CTJoinString + " " +
                    "FROM `" + cur_CT_Table + "` " +
                    "WHERE MULT > 0 " +

                    "UNION ALL " +

                    "SELECT " + CTJoinString + " " +
                    "FROM " +
                        "`" + cur_false_Table + "`, " +
                        "(" + joinTableQueries.get(rnid_or) + ") AS JOIN_TABLE " +
                    "WHERE MULT > 0;";

                String Next_CT_Table = "";

                if (rs1.next()) {
                    Next_CT_Table = BaseName + "_CT";
                } else {
                    Next_CT_Table = shortRchain + "_CT";
                }

                // Oct 16 2013
                // preparing the CT table for next iteration
                cur_CT_Table = Next_CT_Table;

                // Create CT table.
                st3.execute(
                    QueryGenerator.createSimpleCreateTableQuery(
                        Next_CT_Table,
                        storageEngine,
                        QueryStringCT
                    )
                );
                rs1.previous();

                fc++;   

                //  close statements
                st2.close();            
                st3.close();
                long l6 = System.currentTimeMillis(); 
                logger.fine("Building Time(ms) for "+cur_CT_Table+ " : "+(l6-l5)+" ms.\n");
            }
            st1.close();
            rs1.close();
        }
        logger.fine("\n Build CT_RChain_TABLES for length = "+len+" are DONE \n" );
    }


    /**
     * Build the "_counts" tables for the population variables.
     *
     * @param countingStrategy - {@link CountingStrategy} to indicate how counts related tables should be generated.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildPVarsCounts(CountingStrategy countingStrategy) throws SQLException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        dbConnection.setCatalog(databaseName_BN);
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT " +
                "pvid " +
            "FROM " +
                "PVariables;"
        );

        GeneratePVarsCountsMethod<
            String,
            String,
            List<String>,
            String,
            String,
            SQLException
        > generateCountsMethod = CountsManager::generatePVarsCountsTable;

        // Use the generatePVarsCountsTableFromCache method if we have been specified to read the counts from the cache.
        if (countingStrategy.useCTCache()) {
            generateCountsMethod = CountsManager::generatePVarsCountsTableFromCache;
        }

        while(rs.next()){
            String pvid = rs.getString("pvid");
            logger.fine("pvid : " + pvid);
            String countsTableName = pvid + "_counts";
            String selectQuery = QueryGenerator.createMetaQueriesExtractionQuery(
                pvid,
                "Counts",
                "SELECT",
                null,
                false
            );

            // Extract column aliases.
            List<String> columnAliases;
            dbConnection.setCatalog(databaseName_BN);
            try (
                Statement selectStatement = dbConnection.createStatement();
                ResultSet selectResultSet = selectStatement.executeQuery(selectQuery)
            ) {
                columnAliases = extractEntries(selectResultSet, "Entries");
            }

            generateCountsMethod.apply(
                databaseName_CT,
                countsTableName,
                columnAliases,
                countingStrategy.getStorageEngine(),
                pvid
            );
        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.fine("Building Time(ms) for Pvariables counts: "+(l2-l)+" ms.\n");
        logger.fine("\n Pvariables are DONE \n" );
    }


    /**
     * Generate the "_counts" table for the given population variable.
     *
     * @param targetDatabaseName - the name of the database to generate the "_counts" table in.
     * @param countsTableName - the name of the "_counts" table to generate.
     * @param columnAliases - the alias statements used for the SELECT clause for the "_counts" table to generate.
     * @param storageEngine - the storage engine to use for the "_counts" table to generate.
     * @param pvid - the ID of the population variable that the "_counts" table is for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void generatePVarsCountsTable(
        String targetDatabaseName,
        String countsTableName,
        List<String> columnAliases,
        String storageEngine,
        String pvid
    ) throws SQLException {
        dbConnection.setCatalog(databaseName_BN);
        Statement st = dbConnection.createStatement();
        String selectString = String.join(", ", columnAliases);
        logger.fine("SELECT String: " + selectString);

        // Create FROM query.
        String fromQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            pvid,
            "Counts",
            "FROM",
            null,
            false
        );

        String fromString;
        try(ResultSet rs = st.executeQuery(fromQuery)) {
            List<String> tables = extractEntries(rs, "Entries");
            fromString = String.join(", ", tables);
        }

        // Create GROUP BY query.
        String groupbyQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            pvid,
            "Counts",
            "GROUPBY",
            null,
            false
        );

        String groupbyString;
        try(ResultSet rs = st.executeQuery(groupbyQuery)) {
            List<String> columns = extractEntries(rs, "Entries");
            groupbyString = String.join(", ", columns);
        }

        // Create WHERE query (groundings) if applicable.
        String whereQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            pvid,
            "Counts",
            "WHERE",
            null,
            false
        );

        String whereString = "";
        try (ResultSet rsGrounding = st.executeQuery(whereQuery)) {
            List<String> predicates = extractEntries(rsGrounding, "Entries");
            if (!predicates.isEmpty()) {
                whereString = " WHERE " + String.join(" AND ", predicates);
            }
        }

        st.close();

        logger.fine("WHERE String:" + whereString);

        // Create the final query.
        String queryString =
            "SELECT " + selectString + " " +
            "FROM " + fromString +
            whereString;

// This seems unnecessarily complicated even to deal with continuous variables. OS August 22, 2017

        if (!cont.equals("1") && !groupbyString.isEmpty()) {
            queryString = queryString + " GROUP BY " + groupbyString;
        }

        queryString += " HAVING MULT > 0";

        String createString = QueryGenerator.createSimpleCreateTableQuery(
            countsTableName,
            storageEngine,
            queryString
        );
        logger.fine("CREATE String: " + createString);

        dbConnection.setCatalog(targetDatabaseName);
        try (Statement st2 = dbConnection.createStatement()) {
            st2.execute(createString);
        }
    }


    /**
     * Generate the "_counts" table for the given population variable using the CT cache database.
     * <p>
     * Note: If the "_counts" table can't be found in the CT cache database it will be generated there.
     * </p>
     *
     * @param targetDatabaseName - the name of the database to generate the "_counts" table in.
     * @param countsTableName - the name of the "_counts" table to generate.
     * @param columnAliases - the alias statements used for the SELECT clause for the "_counts" table to generate.
     * @param storageEngine - the storage engine to use for the "_counts" table to generate.
     * @param pvid - the ID of the population variable that the "_counts" table is for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void generatePVarsCountsTableFromCache(
        String targetDatabaseName,
        String countsTableName,
        List<String> columnAliases,
        String storageEngine,
        String pvid
    ) throws SQLException {
        StringJoiner csvJoiner = new StringJoiner(",");
        csvJoiner.add(countsTableName);
        String columnNames = makeDelimitedString(columnAliases, ",", " AS ");
        csvJoiner.add(String.join(",", columnNames));

        String ctTablesCacheKey = csvJoiner.toString();

        String cacheTableName = ctTablesCache.get(ctTablesCacheKey);
        if (cacheTableName == null) {
            // Create the table in the CT cache database.
            cacheTableName = countsTableName + "_" + tableID;
            tableID++;
            generatePVarsCountsTable(
                databaseName_CT_cache,
                cacheTableName,
                columnAliases,
                storageEngine,
                pvid
            );

            ctTablesCache.put(ctTablesCacheKey, cacheTableName);
        }

        dbConnection.setCatalog(targetDatabaseName);
        try (Statement createViewStatement = dbConnection.createStatement()) {
            String viewQuery =
                "CREATE VIEW " + countsTableName + " AS " +
                "SELECT * " +
                "FROM " + databaseName_CT_cache + "." + cacheTableName;
            createViewStatement.executeUpdate(viewQuery);
        }
    }


    /**
     * Create the "_counts" tables for the given RChains and copy the counts to the associated CT table if specified
     * to.
     *
     * @param dbTargetName - name of the database to create the "_counts" tables in.
     * @param rchainInfos - FunctorNodesInfos for the RChains to build the "_counts" tables for.
     * @param copyToCT - True if the values in the generated "_counts" table should be copied to the associated "_CT"
     *                   table; otherwise false.
     * @param buildByProjection - True if the counts table should be built by projecting the necessary information from
                                  the global counts table; otherwise false.
     * @param storageEngine - the storage engine to use for the tables created when executing this method.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void generateCountsTables(
        String dbTargetName,
        List<FunctorNodesInfo> rchainInfos,
        boolean copyToCT,
        boolean buildByProjection,
        String storageEngine
    ) throws SQLException {
        for (FunctorNodesInfo rchainInfo : rchainInfos) {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain: " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain: " + shortRchain);

            String countsTableName = generateCountsTable(
                dbTargetName,
                rchain,
                shortRchain,
                buildByProjection,
                storageEngine
            );

            if (copyToCT) {
                dbConnection.setCatalog(dbTargetName);
                try (Statement statement = dbConnection.createStatement()) {
                    String createString_CT =
                        "CREATE TABLE `" + shortRchain + "_CT`" + " AS " +
                            "SELECT * " +
                            "FROM `" + countsTableName + "`";
                    logger.fine("CREATE String: " + createString_CT);
                    statement.execute(createString_CT);
                }
            }
        }

        logger.fine("\n RChain_counts are DONE \n");
    }


    /**
     * Generate the "_counts" table for the given RChain.
     *
     * @param dbTargetName - name of the database to create the "_counts" table in.
     * @param rchain - the full form name of the RChain.
     * @param shortRchain - the short form name of the RChain.
     * @param buildByProjection - True if the counts table should be built by projecting the necessary information from
                                  the global counts table; otherwise false.
     * @param storageEngine - the storage engine to use for the tables created when executing this method.
     * @return the name of the "_counts" table generated.
     * @throws SQLException if an error occurs when executing the queries.
     */
    private static String generateCountsTable(
        String dbTargetName,
        String rchain,
        String shortRchain,
        boolean buildByProjection,
        String storageEngine
    ) throws SQLException {
        // Name of the counts table to generate.
        String countsTableName = shortRchain + "_counts";

        // Create new statements.
        dbConnection.setCatalog(databaseName_BN);
        Statement st2 = dbConnection.createStatement();

        // Create SELECT query string.
        String selectQuery =
            "SELECT Entries " +
            "FROM MetaQueries " +
            "WHERE Lattice_Point = '" + rchain + "' " +
            "AND ClauseType = 'SELECT' ";

        // If we're trying to project from a "_counts" table we want to SUM(MULT) instead of COUNT(*) so ignore the
        // 'aggregate" EntryType when generating the SELECT string.
        if (buildByProjection) {
            selectQuery += "AND EntryType <> 'aggregate' ";
        }

        selectQuery += "AND TableType = 'Counts';";

        ResultSet rs2 = st2.executeQuery(selectQuery);

        List<String> selectAliases = extractEntries(rs2, "Entries");
        String selectString;
        if (buildByProjection) {
            selectString = makeDelimitedString(selectAliases, ", ", "AS ");
            selectString = "SUM(MULT) AS MULT, " + selectString;
        } else {
            selectString = String.join(", ", selectAliases);
        }

        logger.fine("SELECT String: " + selectString);

        // Create FROM query string.
        String fromString = databaseName_global_counts + ".`" + countsTableName + "`";

        // If we aren't projecting from the global counts table, we need to retrieve the tables that need to be joined
        // in order to generate the counts table.
        if (!buildByProjection) {
            ResultSet rs3 = st2.executeQuery(
                QueryGenerator.createMetaQueriesExtractionQuery(
                    rchain,
                    "Counts",
                    "FROM",
                    null,
                    false
                )
            );

            List<String> fromAliases = extractEntries(rs3, "Entries");
            fromString = String.join(", ", fromAliases);
        }

        logger.fine("FROM String: " + fromString);

        // Create WHERE query string.
        String whereString = null;

        // The WHERE clause is for joining tables to generate the counts from and isn't necessary if we are projecting
        // from the global counts table.
        if (!buildByProjection) {
            ResultSet rs4 = st2.executeQuery(
                QueryGenerator.createMetaQueriesExtractionQuery(
                    rchain,
                    "Counts",
                    "WHERE",
                    null,
                    false
                )
            );

            List<String> columns = extractEntries(rs4, "Entries");
            whereString = String.join(" AND ", columns);
        }

        // Create the final query.
        String queryString =
            "SELECT " + selectString + " " +
            "FROM " + fromString;

        if (!buildByProjection) {
            queryString += " WHERE " + whereString;
        }

        // Create GROUP BY query string.
        // This seems unnecessarily complicated - isn't there always a GROUP BY clause?
        // Okay, not with continuous data, but still.
        // Continuous probably requires a different approach.  OS August 22.
        if (!cont.equals("1")) {
            ResultSet rs_6 = st2.executeQuery(
                QueryGenerator.createMetaQueriesExtractionQuery(
                    rchain,
                    "Counts",
                    "GROUPBY",
                    null,
                    false
                )
            );

            List<String> columns = extractEntries(rs_6, "Entries");
            String GroupByString = String.join(", ", columns);

            if (!GroupByString.isEmpty()) {
                queryString = queryString + " GROUP BY "  + GroupByString;
            }
        }

        dbConnection.setCatalog(dbTargetName);
        Statement st3 = dbConnection.createStatement();
        String createString = QueryGenerator.createSimpleCreateTableQuery(
            countsTableName,
            storageEngine,
            queryString
        );
        st3.execute("SET tmp_table_size = " + dbTemporaryTableSize + ";");
        st3.executeQuery("SET max_heap_table_size = " + dbTemporaryTableSize + ";");
        st3.execute(createString);

        // Close statements.
        st2.close();
        st3.close();

        return countsTableName;
    }


    /**
     * Create the star table for the given RNode.
     *
     * @param rnode - the name of the RNode to build the "_star" table for.
     * @param shortRNode - the short name of the RNode to build the "_star" table for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRNodeStar(
        String rnode,
        String shortRNode
    ) throws SQLException {
        long start = System.currentTimeMillis(); // @zqian: measure structure learning time.
        dbConnection.setCatalog(databaseName_BN);
        Statement statement = dbConnection.createStatement();

        // Create SELECT query string.
        String selectQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            rnode,
            "Star",
            "SELECT",
            null,
            true
        );

        List<String> columns;
        try (ResultSet result = statement.executeQuery(selectQuery)) {
            columns = extractEntries(result, "Entries");
        }
        String selectString = String.join(", ", columns);

        // Create * query string, which will be used for the "SELECT AS MULT".
        String multiplicationQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            rnode,
            "Star",
            "FROM",
            null,
            false
        );

        try (ResultSet result = statement.executeQuery(multiplicationQuery)) {
            columns = extractEntries(result, "Entries");
        }
        statement.close();
        String multiplicationString = makeStarSepQuery(columns);

        // Create FROM query string.
        String fromString = String.join(", ", columns);

        // Create the final query string.
        String queryString = "SELECT " + multiplicationString + " AS MULT";
        if (!selectString.isEmpty()) {
            queryString += ", " + selectString;
        }
        queryString += " FROM " + fromString;

        dbConnection.setCatalog(databaseName_CT);
        try(Statement viewStatement = dbConnection.createStatement()) {
            String starTableName = shortRNode + "_star";
            String createString = QueryGenerator.createSimpleCreateViewQuery(
                starTableName,
                queryString
            );
            logger.fine("\nCREATE String: " + createString);
            viewStatement.executeUpdate(createString);
        }

        long end = System.currentTimeMillis(); // @zqian: measure structure learning time.
        logger.fine("Build Time(ms) for RNode Star: " + (end - start) + " ms.\n");
    }


    /**
     * Create the flat table for the given RNode.
     *
     * @param rnode - the name of the RNode to build the "_flat" table for.
     * @param shortRNode - the short name of the RNode to build the "_flat" table for.
     * @param storageEngine - the storage engine to use for the table created when executing this method.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRNodeFlat(
        String rnode,
        String shortRNode,
        String storageEngine
    ) throws SQLException {
        long start = System.currentTimeMillis(); // @zqian: measure structure learning time.

        dbConnection.setCatalog(databaseName_BN);
        Statement statement = dbConnection.createStatement();

        // Create SELECT query string.
        String selectQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            rnode,
            "Flat",
            "SELECT",
            null,
            false
        );

        List<String> columns;
        try (ResultSet rs2 = statement.executeQuery(selectQuery)) {
            columns = extractEntries(rs2, "Entries");
        }
        String selectString = String.join(", ", columns);

        // Create FROM query string.
        String fromQuery = QueryGenerator.createMetaQueriesExtractionQuery(
            rnode,
            "Flat",
            "FROM",
            null,
            false
        );
        try (ResultSet result = statement.executeQuery(fromQuery)) {
            columns = extractEntries(result, "Entries");
        }
        String fromString = String.join(", ", columns);

        // Create the final query string.
        String queryString = "SELECT " + selectString + " FROM " + fromString;

        // Create GROUP BY query string.
        if (!cont.equals("1")) {
            String groupByQuery = QueryGenerator.createMetaQueriesExtractionQuery(
                rnode,
                "Flat",
                "GROUPBY",
                null,
                false
            );
            try (ResultSet result = statement.executeQuery(groupByQuery)) {
                columns = extractEntries(result, "Entries");
            }
            String groupByString = String.join(", ", columns);
            if (!groupByString.isEmpty()) {
                queryString += " GROUP BY "  + groupByString;
            }
        }
        statement.close();

        String flatTableName = shortRNode + "_flat";
        String createString = QueryGenerator.createSimpleCreateTableQuery(
            flatTableName,
            storageEngine,
            queryString
        );
        logger.fine("\nCREATE String: " + createString);
        dbConnection.setCatalog(databaseName_CT);
        try (Statement createStatement = dbConnection.createStatement()) {
            createStatement.executeUpdate(createString);
        }

        // Add covering index.
        addCoveringIndex(
            dbConnection,
            databaseName_CT,
            flatTableName
        );

        long end = System.currentTimeMillis(); // @zqian: measure structure learning time.
        logger.fine("Build Time(ms) for RNode Flat: " + (end - start) + " ms.\n");
    }


    /**
     * Create the false table for the given RNode using the sort merge algorithm.
     *
     * @param shortRNode - the short name of the RNode to build the "_false" table for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRNodeFalse(String shortRNode) throws SQLException {
        String falseTableName = shortRNode + "_false";

        // Computing the false table as the MULT difference between the matching rows of the star and flat tables.
        Sort_merge3.sort_merge(
            dbConnection,
            shortRNode + "_star",
            shortRNode + "_flat",
            falseTableName
        );
    }


    /**
     * Create the query for generating the CT table of the given RNode.  This is done by cross joining the "_false"
     * table with the associated JOIN (derived) table, and then having the result UNIONed with the proper "_counts"
     * table.
     *
     * @param shortRNode - the short name of the RNode to build the "_CT" table for.
     * @param joinTableQueries - {@code Map} to retrieve the associated query to create a derived JOIN table.
     * @return query that can be used to generate the CT table for the given RNode.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static String buildRNodeCTCreationQuery(
        String rnode,
        String shortRNode,
        Map<String, String> joinTableQueries
    ) throws SQLException {
        String falseTableName = shortRNode + "_false";
        String countsTableName = shortRNode + "_counts";

        // Must specify the columns or there will be column mismatches when taking the UNION of the counts and false
        // tables.
        String columnQuery =
            "SELECT Entries " +
            "FROM MetaQueries " +
            "WHERE Lattice_Point = '" + rnode + "' " +
            "AND ClauseType = 'SELECT' " +
            "AND EntryType <> 'aggregate' " +
            "AND TableType = 'Counts';";

        // Extract and escape the column names since they look like function calls to MySQL.
        List<String> columns;
        dbConnection.setCatalog(databaseName_BN);
        try (
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(columnQuery)
        ) {
            columns = extractEntries(result, "Entries");
        }
        String UnionColumnString;
        if (columns.size() > 0) {
            UnionColumnString = "MULT, " + makeDelimitedString(columns, ", ", " AS ");
        } else {
            UnionColumnString = "MULT";
        }

        // Join the false table with the join table to introduce rnid (= F) and 2nids (= N/A).  Then union the result
        // with the counts table.
        String createCTString =
            "SELECT " + UnionColumnString + " " +
            "FROM " + databaseName_CT + ".`" + countsTableName + "` " +
            "WHERE MULT > 0 " +

            "UNION ALL " +

            "SELECT " + UnionColumnString + " " +
            "FROM " +
                databaseName_CT + ".`" + falseTableName + "`, " +
                "(" + joinTableQueries.get(shortRNode) + ") AS JOIN_TABLE " +
            "WHERE MULT > 0";
        return createCTString;
    }


    /**
     * Create queries that can be used to create JOIN tables as derived tables in "FROM" clauses.
     * <p>
     * JOIN tables are used to help represent the case where a relationship is false and its attributes are undefined.
     * JOIN tables are cross joined with "_false" tables to help generate the "_CT" tables.
     * </p>
     * @return a {@code Map} object containing queries that can be used to create JOIN tables as derived tables.  The
     *         entries in the {@code Map} object have the form of &lt;short_rnid&gt;:&lt;joinTableQuery&gt;.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static Map<String, String> createJoinTableQueries() throws SQLException {
        Map<String, String> joinTableQueries = new HashMap<String, String>();

        dbConnection.setCatalog(databaseName_BN);
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("select orig_rnid, short_rnid from LatticeRNodes ;");

        while(rs.next()){
        //  get rnid
            String short_rnid = rs.getString("short_rnid");
            logger.fine("\n short_rnid : " + short_rnid);
            String orig_rnid = rs.getString("orig_rnid");
            logger.fine("\n orig_rnid : " + orig_rnid);

            Statement st2 = dbConnection.createStatement();

            //  create ColumnString
            ResultSet rs2 = st2.executeQuery(
                QueryGenerator.createMetaQueriesExtractionQuery(
                    orig_rnid,
                    "Join",
                    "COLUMN",
                    null,
                    false
                )
            );

            List<String> columns = extractEntries(rs2, "Entries");
            String additionalColumns = String.join(", ", columns);
            StringBuilder joinTableQuerybuilder = new StringBuilder("SELECT \"F\" AS `" + orig_rnid + "`");
            if (!additionalColumns.isEmpty()) {
                joinTableQuerybuilder.append(", " + additionalColumns);
            }

            joinTableQueries.put(short_rnid, joinTableQuerybuilder.toString());
            st2.close();
        }

        rs.close();
        st.close();
        logger.fine("\n Rnodes_joins are DONE \n" );

        return joinTableQueries;
    }


    /**
     * Extract the values from the specified column of the given {@code ResultSet}.
     *
     * @param results - the ResultSet to extract the values from the specified column.
     * @param column - the column to extract the values from.
     * @return the list of values in the specified column of the given {@code ResultSet}.
     * @throws SQLException if an error occurs when trying to extract the column values.
     */
    private static List<String> extractEntries(ResultSet results, String column) throws SQLException {
        ArrayList<String> values = new ArrayList<String>();
        while (results.next()) {
            values.add(results.getString(column));
        }

        return values;
    }


    /**
     * Generate the multiplication string between the columns necessary to generate an _star table.
     *
     * @param columns - the columns to multiply together.
     * @return the multiplication string between the columns necessary to generate an _star table.
     */
    private static String makeStarSepQuery(List<String> columns) {
        String[] parts = new String[columns.size()];
        int index = 0;
        for (String column : columns) {
            parts[index] = column + ".MULT";
            index++;
        }

        return String.join(" * ", parts);
    }


    /**
     * Generate an escaped, comma delimited list of columns to generate an _CT table.
     *
     * @param columns - the columns to escape using backticks "`" and make a CSV with.
     * @return an escaped, comma delimited list of columns to generate an _CT table.
     */
    private static String makeEscapedCommaSepQuery(List<String> columns) {
        StringJoiner escapedCSV = new StringJoiner("`, `", "`", "`");

        for (String column : columns) {
            escapedCSV.add(column);
        }

        return escapedCSV.toString();
    }


    /**
     * Method to help retrieve the alias names from the given SELECT aliases and concatenate them together using the
     * specified delimiter.
     * <p>
     * Example of what information is retrieved:
     * Professor AS Prof --> Prof
     * </p>
     * @param selectAliases - list of SELECT aliases to retrieve the alias names from.
     * @param delimiter - the delimiter to use when concatenating the alias names together.
     * @param aliasKeyword - the keyword (e.g. AS) used when generating SELECT alias statements.
     * @return the alias names from the given SELECT aliases separated by the specified delimiter.
     */
    private static String makeDelimitedString(List<String> selectAliases, String delimiter, String aliasKeyword) {
        String[] parts = new String[selectAliases.size()];
        int index = 0;
        for (String selectAlias : selectAliases) {
            int prefixDelimiterStartIndex = selectAlias.indexOf(aliasKeyword);
            int prefixDelimiterEndIndex = prefixDelimiterStartIndex + aliasKeyword.length();
            parts[index] = selectAlias.substring(prefixDelimiterEndIndex);
            index++;
        }

        return String.join(delimiter, parts);
    }


    /**
     * Add a covering index to the specified table.
     * <p>
     * Note: All columns will be part of the index except for the "MULT" column.  If there is only a "MULT" column in
     *       the table, no index will be created.
     * </p>
     *
     * @param dbConnection - connection to the database containing the table to create the covering index for.
     * @param databaseName - the name of the database that the specified table is located in.
     * @param tableName - the name of the table to add a covering index to.
     * @throws SQLException if an error occurs when executing the queries.
     */
    private static void addCoveringIndex(Connection dbConnection, String databaseName, String tableName) throws SQLException {
        String allColumnsQuery =
            "SELECT column_name " +
            "FROM information_schema.columns " +
            "WHERE table_schema = '" + databaseName + "' " +
            "AND table_name = '" + tableName + "';";

        try (
            Statement dbStatement = dbConnection.createStatement();
            ResultSet columnsResults = dbStatement.executeQuery(allColumnsQuery)
        ) {
            String columnsCSV = makeIndexQuery(columnsResults, "column_name", ", ");
            if (!columnsCSV.isEmpty()) {
                dbStatement.execute(
                    "ALTER TABLE `" + tableName + "` " +
                    "ADD INDEX CoveringIndex (" + columnsCSV + ");"
                );
            }
        }
    }


    /**
     * Making Index Query by adding "`" and appending ASC
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    private static String makeIndexQuery(ResultSet rs, String colName, String del) throws SQLException {

        ArrayList<String> parts = new ArrayList<String>();
        int count=0;
        while(rs.next()&count<16){

                String temp =rs.getString(colName);
                if (temp.equals("MULT")) {
                    continue;
                }
                temp= "`"+temp+"`";
            parts.add(temp+ " ASC");
            count ++;
        }

        return String.join(del, parts);
    }


    /**
     * Interface to enable the ability to switch between using the
     * {@link CountsManager#generatePVarsCountsTable(String, String, List, String, String)}
     * method and the
     * {@link CountsManager#generatePVarsCountsTableFromCache(String, String, List, String, String)}
     * method.
     *
     * @param <A> (String) the name of the output database.
     * @param <B> (String) the name of the "_counts" table to generate.
     * @param <C> (List&lt;String&gt;) the alias statements for the columns.
     * @param <D> (String) the storage engine to use for the "_counts" table generated.
     * @param <E> (String) the population variable of the "_counts" table generated.
     * @param <F> (SQLException) the SQLException that should be thrown if there are issues executing the SQL queries.
     */
    @FunctionalInterface
    private interface GeneratePVarsCountsMethod<A, B, C extends List<String>, D, E, F extends SQLException> {
        public void apply(
            A outputDatabaseName,
            B outputTableName,
            C columnAliases,
            D storageEngine,
            E pvariable
        ) throws F;
    }


    /**
     * Interface to enable the ability to switch between using the
     * {@link CountsManager#buildRNodesCT(String, String, String, String, String, String)
     * method and the
     * {@link CountsManager#buildRNodesCTFromCache(String, String, String, String, String, String)
     * method.
     *
     * @param <A> (String) the name of the output database.
     * @param <B> (String) the name of the output table.
     * @param <C> (String) the storage engine to use for the tables generated in general.
     * @param <D> (String) the SELECT query used to create the CT table.
     * @param <E> (String) the name of the RNode to generate the CT table for.
     * @param <F> (String) the short name of the RNode to generate the CT table for.
     * @param <G> (SQLException) the SQLException that should be thrown if there are issues executing the SQL queries.
     */
    @FunctionalInterface
    private interface BuildRNodesCTMethod<A, B, C, D, E, F, G extends SQLException> {
        public void apply(
            A targetDatabaseName,
            B ctTableName,
            C storageEngine,
            D ctCreationQuery,
            E rnode,
            F shortRNode
        ) throws G;
    }


    /**
     * Connect to all the relevant databases.
     *
     * @throws SQLException if there are issues connecting to the databases.
     */
    public static void connectDB() throws SQLException {
        dbConnection = connectDB(databaseName_BN);
    }


    /**
     * Disconnect from all the relevant databases.
     *
     * @throws SQLException if there are issues disconnecting from the databases.
     */
    public static void disconnectDB() throws SQLException {
        dbConnection.close();
    }
}