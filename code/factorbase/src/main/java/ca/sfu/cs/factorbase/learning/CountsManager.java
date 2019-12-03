package ca.sfu.cs.factorbase.learning;

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
import java.util.List;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.lattice.LatticeGenerator;
import ca.sfu.cs.factorbase.lattice.RelationshipLattice;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;
import ca.sfu.cs.factorbase.util.Sort_merge3;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class CountsManager {

    private static Connection con_BN;
    private static Connection con_CT;
    private static String databaseName_std;
    private static String databaseName_BN;
    private static String databaseName_CT;
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
     * @Overload
     * buildCT
     *
     * @throws SQLException if there are issues executing the SQL queries.
     */
    public static void buildCT() throws SQLException {
        //connect to db using jdbc
        con_BN = connectDB(databaseName_BN);
        con_CT = connectDB(databaseName_CT);

        try (Statement statement = con_BN.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + databaseName_CT + ";");
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + databaseName_CT + ";");
        }

        // Propagate metadata based on the FunctorSet.
        RelationshipLattice relationshipLattice = propagateFunctorSetInfo(con_BN);

        // Build the counts tables for the RChains.
        buildRChainCounts(con_CT, relationshipLattice);

        // building CT tables for Rchain
        CTGenerator(relationshipLattice);
        disconnectDB();
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
        MySQLScriptRunner.callSP(
            dbConnection,
            "cascadeFS"
        );

        // Generate the relationship lattice based on the FunctorSet.
        RelationshipLattice relationshipLattice = LatticeGenerator.generate(
            dbConnection,
            databaseName_std
        );

        // TODO: Add support for Continuous = 1.
        if (cont.equals("1")) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
        } else {
            MySQLScriptRunner.callSP(
                dbConnection,
                "populateMQ"
            );
        }

        MySQLScriptRunner.callSP(
            dbConnection,
            "populateMQRChain"
        );

        return relationshipLattice;
    }


    /*** this part we do need O.s. May 16, 2018 ***/
    /**
     *  Building the _CT tables for length >=2
     *  For each length
     *  1. find rchain, find list of members of rc-hain. Suppose first member is rnid1.
     *  2. initialize current_ct = rchain_counts after summing out the relational attributes of rnid1.
     *  3. Current list = all members of rchain minus rndi1. find ct(table) for current list = . Select rows where all members of current list are true. Add 1nodes of rnid1.
     *  4. Compute false table using the results of 2 and 3 (basically 2 - 3).
     *  5. Union false table with current_ct to get new ct where all members of current list are true.
     *  6. Repeat with current list as initial list until list is empty.
     *  Example:
     *  1. Rchain = R3,R2,R1. first rnid1 = R3.
     *  2. Find `R3,R2,R1_counts`. Sum out fields from R3 to get `R2,R1-R3_flat1`.
     *  3. Current list = R2,R1. Find `R2,R1_ct` where R1 = T, R2 = T. Add 1nodes of R3 (multiplying) to get `R2,R1-R3_star`.
     *  4. Compute `R2,R1-R3_false` = `R2,R1-R3_star - `R2,R1-R3_flat1` union (as before)
     *  5. Compute `R2,R1-R3_ct` = `R2,R1-R3_false` cross product `R3_join` union `R3,R2,R1_counts`.
     *  6. Current list = R1. Current rnid = R2. Current ct_table = `R2,R1-R3_ct`.
     *
     *  BuildCT_Rnodes_flat(len);
     *
     *  BuildCT_Rnodes_star(len);
     *
     *  BuildCT_Rnodes_CT(len);
     *
     * @param relationshipLattice - the relationship lattice used to determine which contingency tables to generate.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void CTGenerator(RelationshipLattice relationshipLattice) throws SQLException {
        int latticeHeight = relationshipLattice.getHeight();

        long l = System.currentTimeMillis(); //@zqian : CT table generating time
           // handling Pvars, generating pvars_counts       
        BuildCT_Pvars();
        
        // preparing the _join part for _CT tables
        BuildCT_Rnodes_join();

        if (linkCorrelation.equals("1") && relationshipLattice.getHeight() != 0) {
            // handling Rnodes with Lattice Moebius Transform
            // Retrieve the first level of the lattice.
            List<FunctorNodesInfo> rchainInfos = relationshipLattice.getRChainsInfo(1);

            // Building the _flat tables.
            BuildCT_Rnodes_flat(rchainInfos);

            // Building the _star tables.
            BuildCT_Rnodes_star(rchainInfos);

            // Building the _false tables first and then the _CT tables.
            BuildCT_Rnodes_CT(rchainInfos);
            
            //building the _CT tables. Going up the Rchain lattice
            for(int len = 2; len <= latticeHeight; len++)
            { 
                rchainInfos = relationshipLattice.getRChainsInfo(len);
                logger.fine("now we're here for Rchain!");
                logger.fine("Building Time(ms) for Rchain >=2 \n");
                BuildCT_RChain_flat(rchainInfos, len);
                logger.fine(" Rchain! are done");
            }
        }
        

        //delete the tuples with MULT=0 in the biggest CT table
        String BiggestRchain = relationshipLattice.getLongestRChainShortID();
        logger.fine("\n BiggestRchain: " + BiggestRchain);

        if (BiggestRchain != null)
        {
            try (Statement st_CT = con_CT.createStatement()) {
                String deleteQuery = "DELETE FROM `" + BiggestRchain + "_CT` WHERE MULT = '0';";
                logger.fine(deleteQuery);
                st_CT.execute(deleteQuery);
            }
        }
        
        long l2 = System.currentTimeMillis();  //@zqian
        logger.fine("Building Time(ms) for ALL CT tables:  "+(l2-l)+" ms.\n");
    }


    /**
     * Generate the global counts tables.
     */
    public static void buildRChainsGlobalCounts() throws SQLException {
        con_BN = connectDB(databaseName_BN);
        try(
            Connection conGlobalCounts = connectDB(databaseName_global_counts)
        ) {

            // Propagate metadata based on the FunctorSet.
            RelationshipLattice relationshipLattice = propagateFunctorSetInfo(con_BN);

            // Generate the global counts in the "_global_counts" database.
            buildRChainCounts(conGlobalCounts, relationshipLattice);
        }

        con_BN.close();
    }


    /**
     * Build the "_counts" tables for the RChains in the given relationship lattice.
     *
     * @param dbConnection - connection to the database to create the "_counts" tables in.
     * @param relationshipLattice - the relationship lattice containing the RChains to build the "_counts" tables for.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void buildRChainCounts(
        Connection dbConnection,
        RelationshipLattice relationshipLattice
    ) throws SQLException {
        int latticeHeight = relationshipLattice.getHeight();

        // Building the <RChain>_counts tables.
        if(linkCorrelation.equals("1")) {
            // Generate the counts tables.
            for(int len = 1; len <= latticeHeight; len++){
                generateCountsTables(
                    dbConnection,
                    relationshipLattice.getRChainsInfo(len),
                    false
                );
            }
        } else {
            // Generate the counts tables and copy their values to the CT tables.
            for(int len = 1; len <= latticeHeight; len++) {
                generateCountsTables(
                    dbConnection,
                    relationshipLattice.getRChainsInfo(len),
                    true
                );
            }
        }
    }


    /**
     * setVarsFromConfig
     * ToDo : Remove Duplicate definitions across java files
     */
    private static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName_std = conf.getProperty("dbname");
        databaseName_BN = databaseName_std + "_BN";
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
        String CONN_STR = "jdbc:" + dbaddress + "/" + databaseName;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        return (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
    }


    /**
     * Building the _CT tables. Going up the Rchain lattice ( When rchain.length >=2)
     * @param rchainInfos: FunctorNodesInfos for the RChains to build the "_CT" tables for.
     * @param int : length of the RChain
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void BuildCT_RChain_flat(List<FunctorNodesInfo> rchainInfos, int len) throws SQLException {
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
            Statement st1 = con_BN.createStatement();
            ResultSet rs1 = st1.executeQuery(
                "SELECT DISTINCT parent, removed, short_rnid " +
                "FROM lattice_rel " +
                "JOIN lattice_mapping " +
                "ON lattice_rel.removed = lattice_mapping.orig_rnid " +
                "WHERE child = '" + rchain + "' " +
                "ORDER BY removed ASC;"
            ); // members of rchain

            while(rs1.next())
            {       
                long l2 = System.currentTimeMillis(); 
                String parent = rs1.getString("parent");
                logger.fine("\n parent : " + parent);
                String removed = rs1.getString("removed");
                logger.fine("\n removed : " + removed);  
                String removedShort = rs1.getString("short_rnid");
                logger.fine("\n removed short : " + removedShort);
                String BaseName = shortRchain + "_" + removedShort;
                logger.fine(" BaseName : " + BaseName );

                Statement st2 = con_BN.createStatement();
                Statement st3 = con_CT.createStatement();

                //  create select query string  


                ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'SELECT' and TableType = 'Star';");
                String selectString = makeCommaSepQuery(rs2, "Entries", ", ");
                logger.fine("Select String : " + selectString);
                rs2.close();
                //  create mult query string
                ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'FROM' and TableType = 'Star';");
                String MultString = makeStarSepQuery(rs3, "Entries", " * ");
                logger.fine("Mult String : " + MultString+ " as `MULT`");
                rs3.close();
                //  create from query string
                ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'FROM' and TableType = 'Star';");
                String fromString = makeCommaSepQuery(rs4, "Entries", ", ");
                logger.fine("From String : " + fromString);          
                rs4.close();
                //  create where query string
                ResultSet rs5 = st2.executeQuery("SELECT DISTINCT Entries FROM MetaQueries WHERE Lattice_Point = '" + rchain + "' and '"+removed+"' = EntryType and ClauseType = 'WHERE' and TableType = 'Star';");
                String whereString = makeCommaSepQuery(rs5, "Entries", " AND ");
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

                //make the rnid shorter 
                String rnid_or=removedShort;
            
                String cur_star_Table = removedShort + len + "_" + fc + "_star";
                String createStarString = "create table "+cur_star_Table +" as "+queryString;

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

                String createStringflat = "create table "+cur_flat_Table+" as "+queryStringflat;
                logger.fine("\n create flat String : " + createStringflat );         
                st3.execute(createStringflat);      //create flat table

                // Add covering index.
                addCoveringIndex(
                    con_CT,
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
                    cur_star_Table,
                    cur_flat_Table,
                    cur_false_Table,
                    con_CT
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
                String CTJoinString = makeUnionSepQuery(rs_45, "Entries", ", ");
                logger.fine("CT Join String : " + CTJoinString);

                //join false table with join table to add in rnid (= F) and 2nid (= n/a). then can union with CT table
                String QueryStringCT =
                    "SELECT " + CTJoinString + " " +
                    "FROM `" + cur_CT_Table + "` " +

                    "UNION ALL " +

                    "SELECT " + CTJoinString + " " +
                    "FROM `" + cur_false_Table + "`, `" + rnid_or + "_join`";

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
                st3.execute("CREATE TABLE `" + Next_CT_Table + "` AS " + QueryStringCT);
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

    /* building pvars_counts*/
    private static void BuildCT_Pvars() throws SQLException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select * from PVariables;");

        while(rs.next()){
            //  get pvid for further use
            String pvid = rs.getString("pvid");
            logger.fine("pvid : " + pvid);
            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();
            //  create select query string
            ResultSet rs2 = st2.executeQuery(
                "SELECT DISTINCT " +
                    "Entries " +
                "FROM " +
                    "MetaQueries " +
                "WHERE " +
                    "Lattice_Point = '" + pvid + "' " +
                "AND " +
                    "ClauseType = 'SELECT' " +
                "AND " +
                    "TableType = 'Counts';"
            );

            String selectString = makeCommaSepQuery(rs2, "Entries", ", ");
            logger.fine("Select String : " + selectString);
            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'FROM' and TableType = 'Counts' ;");
            String fromString = makeCommaSepQuery(rs3, "Entries", ", ");

            ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'GROUPBY' and TableType = 'Counts' ;");
            String GroupByString = makeCommaSepQuery(rs_6, "Entries", ", ");

            /*
             *  Check for groundings on pvid
             *  If exist, add as where clause
             */
            logger.fine( "con_BN:SELECT id FROM Groundings WHERE pvid = '"+pvid+"';" );

            ResultSet rsGrounding = null;
            String whereString = "";

            try
            {
                rsGrounding = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'WHERE' and TableType = 'Counts' ;");
            }
            catch( MySQLSyntaxErrorException e )
            {
                logger.severe( "No WHERE clause for groundings" );
            }

            if ( null != rsGrounding )
            {

                whereString = makeCommaSepQuery(rsGrounding, "Entries", " AND ");

            }

            logger.fine( "whereString:" + whereString );

            //  create the final query
            String queryString = "Select " + selectString + " from " +
                                 fromString + whereString;
                                 
//this seems unnecessarily complicated even to deal with continuos variables. OS August 22, 2017

            if (!cont.equals("1")) {
                if (!GroupByString.isEmpty()) {
                    queryString = queryString + " GROUP BY " + GroupByString;
                }
            }

            String countsTableName = pvid + "_counts";
            String createString = "CREATE TABLE " + countsTableName + " AS " + queryString;
            logger.fine("Create String: " + createString);
            st3.execute(createString);

            //  close statements
            st2.close();
            st3.close();
        }

        rs.close();
        st.close();
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.fine("Building Time(ms) for Pvariables counts: "+(l2-l)+" ms.\n");
        logger.fine("\n Pvariables are DONE \n" );
    }


    /**
     * Create the "_counts" tables for the given RChains and copy the counts to the associated CT table if specified
     * to.
     *
     * @param dbConnection - connection to the database to create the "_counts" tables in.
     * @param rchainInfos - FunctorNodesInfos for the RChains to build the "_counts" tables for.
     * @param copyToCT - True if the values in the generated "_counts" table should be copied to the associated "_CT"
     *                   table; otherwise false.
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void generateCountsTables(
        Connection dbConnection,
        List<FunctorNodesInfo> rchainInfos,
        boolean copyToCT
    ) throws SQLException {
        for (FunctorNodesInfo rchainInfo : rchainInfos) {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain: " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain: " + shortRchain);

            String countsTableName = generateCountsTable(
                dbConnection,
                rchain,
                shortRchain
            );

            if (copyToCT) {
                try (Statement statement = con_CT.createStatement()) {
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
     * @param dbConnection - connection to the database to create the "_counts" table in.
     * @param rchain - the full form name of the RChain.
     * @param shortRchain - the short form name of the RChain.
     * @return the name of the "_counts" table generated.
     * @throws SQLException if an error occurs when executing the queries.
     */
    private static String generateCountsTable(
        Connection dbConnection,
        String rchain,
        String shortRchain
    ) throws SQLException {
        // Create new statements.
        Statement st2 = con_BN.createStatement();
        Statement st3 = dbConnection.createStatement();

        // Create SELECT query string.
        ResultSet rs2 = st2.executeQuery(
            "SELECT DISTINCT Entries " +
            "FROM MetaQueries " +
            "WHERE Lattice_Point = '" + rchain + "' " +
            "AND ClauseType = 'SELECT' " +
            "AND TableType = 'Counts';"
        );

        String selectString = makeCommaSepQuery(rs2, "Entries", ", ");
        logger.fine("SELECT String: " + selectString);

        // Create FROM query string.
        ResultSet rs3 = st2.executeQuery(
            "SELECT DISTINCT Entries " +
            "FROM MetaQueries " +
            "WHERE Lattice_Point = '" + rchain + "' " +
            "AND ClauseType = 'FROM' " +
            "AND TableType = 'Counts';"
        );

        String fromString = makeCommaSepQuery(rs3, "Entries", ", ");
        logger.fine("FROM String: " + fromString);

        // Create WHERE query string.
        ResultSet rs4 = st2.executeQuery(
            "SELECT DISTINCT Entries " +
            "FROM MetaQueries " +
            "WHERE Lattice_Point = '" + rchain + "' " +
            "AND ClauseType = 'WHERE' " +
            "AND TableType = 'Counts';"
        );

        String whereString = makeCommaSepQuery(rs4, "Entries", " AND ");

        // Create the final query.
        String queryString =
            "SELECT " + selectString + " " +
            "FROM " + fromString + " " +
            "WHERE " + whereString;

        // Create GROUP BY query string.
        // This seems unnecessarily complicated - isn't there always a GROUP BY clause?
        // Okay, not with continuous data, but still.
        // Continuous probably requires a different approach.  OS August 22.
        if (!cont.equals("1")) {
            ResultSet rs_6 = st2.executeQuery(
                "SELECT DISTINCT Entries " +
                "FROM MetaQueries " +
                "WHERE Lattice_Point = '" + rchain + "' " +
                "AND ClauseType = 'GROUPBY' " +
                "AND TableType = 'Counts';"
            );

            String GroupByString = makeCommaSepQuery(rs_6, "Entries", ", ");

            if (!GroupByString.isEmpty()) {
                queryString = queryString + " GROUP BY "  + GroupByString;
            }
        }

        String countsTableName = shortRchain + "_counts";
        String createString = "CREATE TABLE `" + countsTableName + "`" + " AS " + queryString;
        logger.fine("CREATE string: " + createString);

        st3.execute("SET tmp_table_size = " + dbTemporaryTableSize + ";");
        st3.executeQuery("SET max_heap_table_size = " + dbTemporaryTableSize + ";");
        st3.execute(createString);

        // Close statements.
        st2.close();
        st3.close();

        return countsTableName;
    }

    /**
     * building the _flat tables
     */
    private static void BuildCT_Rnodes_flat(List<FunctorNodesInfo> rchainInfos) throws SQLException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        for (FunctorNodesInfo rchainInfo : rchainInfos) {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();


            //  create select query string
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'SELECT';");
            String selectString = makeCommaSepQuery(rs2, "Entries", ", ");

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'FROM';" );
            String fromString = makeCommaSepQuery(rs3, "Entries", ", ");

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString ;

            //  create group by query string
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Flat' and ClauseType = 'GROUPBY';");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", ", ");

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                logger.fine("Query String : " + queryString );
            }

            String flatTableName = shortRchain + "_flat";
            String createString = "CREATE TABLE `" + flatTableName + "` AS " + queryString;
            logger.fine("\n create String : " + createString );
            st3.execute(createString);

            // Add covering index.
            addCoveringIndex(
                con_CT,
                databaseName_CT,
                flatTableName
            );

            //  close statements
            st2.close();
            st3.close();
        }

        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.fine("Building Time(ms) for Rnodes_flat: "+(l2-l)+" ms.\n");
        logger.fine("\n Rnodes_flat are DONE \n" );
    }

    /**
     * building the _star tables
     */
    private static void BuildCT_Rnodes_star(List<FunctorNodesInfo> rchainInfos) throws SQLException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        for (FunctorNodesInfo rchainInfo : rchainInfos) {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query string
            
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'SELECT';");
            String selectString = makeCommaSepQuery(rs2, "Entries", ", ");


            //  create from MULT string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'FROM';");
            String MultString = makeStarSepQuery(rs3, "Entries", " * ");
            //makes the aggregate function to be used in the select clause //
            // looks like rs3 and rs4 contain the same data. Ugly! OS August 24, 2017

            //  create from query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and TableType = 'Star' and ClauseType = 'FROM';");
            String fromString = makeCommaSepQuery(rs4, "Entries", ", ");

            //  create the final query
            String queryString = "";
            if (!selectString.isEmpty()) {
                queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString ;
            } else {
                queryString = "Select " +  MultString+ " as `MULT`  from " + fromString ;
            }

            String starTableName = shortRchain + "_star";
            String createString = "CREATE TABLE `" + starTableName + "` AS " + queryString;
            logger.fine("\n create String : " + createString );
            st3.execute(createString);

            //  close statements
            st2.close();
            st3.close();
        }

        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.fine("Building Time(ms) for Rnodes_star: "+(l2-l)+" ms.\n");
        logger.fine("\n Rnodes_star are DONE \n" );
    }

    /**
     * building the _false tables first and then the _CT tables
     */
    private static void BuildCT_Rnodes_CT(List<FunctorNodesInfo> rchainInfos) throws SQLException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        for (FunctorNodesInfo rchainInfo : rchainInfos) {
            // Get the short and full form rnids for further use.
            String rchain = rchainInfo.getID();
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rchainInfo.getShortID();
            logger.fine(" Short RChain : " + shortRchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();
            /**********starting to create _flase table***using sort_merge*******************************/
            String falseTableName = shortRchain + "_false";

            // Computing the false table as the MULT difference between the matching rows of the star and flat tables.
            // This is a big join!
            Sort_merge3.sort_merge(
                shortRchain + "_star",
                shortRchain + "_flat",
                falseTableName,
                con_CT
            );

            String countsTableName = shortRchain + "_counts";

            //building the _CT table        //expanding the columns // May 16
            // must specify the columns, or there's will a mistake in the table that mismatch the columns
            ResultSet rs5 = st3.executeQuery(
                "SELECT column_name AS Entries " +
                "FROM information_schema.columns " +
                "WHERE table_schema = '" + databaseName_CT + "' " +
                "AND table_name = '" + countsTableName + "';"
            );
            // reading the column names from information_schema.columns, and the output will remove the "`" automatically,
            // however some columns contain "()" and MySQL does not support "()" well, so we have to add the "`" back.
            String UnionColumnString = makeUnionSepQuery(rs5, "Entries", ", ");

            String ctTableName = shortRchain + "_CT";

            //join false table with join table to introduce rnid (=F) and 2nids (= n/a). Then union result with counts table.
            String createCTString =
                "CREATE TABLE `" + ctTableName + "` AS " +
                    "SELECT " + UnionColumnString + " " +
                    "FROM `" + countsTableName + "` " +

                    "UNION ALL " +

                    "SELECT " + UnionColumnString + " " +
                    "FROM `" + falseTableName + "`, `" + shortRchain + "_join`;";

            logger.fine("\n create CT table String : " + createCTString );
            st3.execute(createCTString);

            //  close statements
            st2.close();
            st3.close();
        }

        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        logger.fine("Building Time(ms) for Rnodes_false and Rnodes_CT: "+(l2-l)+" ms.\n");
        logger.fine("\n Rnodes_false and Rnodes_CT  are DONE \n" );
    }

    /**
     * preparing the _join part for _CT tables
     *
     * @throws SQLException if there are issues executing the SQL queries.
     */
    private static void BuildCT_Rnodes_join() throws SQLException {
        //set up the join tables that represent the case where a relationship is false and its attributes are undefined //

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select orig_rnid, short_rnid from LatticeRNodes ;");

        while(rs.next()){
        //  get rnid
            String short_rnid = rs.getString("short_rnid");
            logger.fine("\n short_rnid : " + short_rnid);
            String orig_rnid = rs.getString("orig_rnid");
            logger.fine("\n orig_rnid : " + orig_rnid);

            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create ColumnString
            ResultSet rs2 = st2.executeQuery(
                "SELECT DISTINCT Entries " +
                "FROM MetaQueries " +
                "WHERE Lattice_Point = '" + orig_rnid + "' " +
                "AND TableType = 'Join';"
            );

            String additionalColumns = makeCommaSepQuery(rs2, "Entries", ", ");
            String ColumnString = "`" + orig_rnid + "` VARCHAR(5)";
            if (!additionalColumns.isEmpty()) {
                ColumnString += ", " + additionalColumns;
            }
            //if there's no relational attribute, then should remove the "," in the ColumnString
            String createString = "CREATE TABLE `" + short_rnid + "_join` (" + ColumnString + ");";
                logger.fine("create String : " + createString);

            st3.execute(createString);
            st3.execute("INSERT INTO `" + short_rnid + "_join` (`" + orig_rnid + "`) VALUES ('F');");
// should probably make this an insertion in the script: add a column for rnid with default value 'F' OS August 24, 2017
            st2.close();
            st3.close();

        }
        rs.close();
        st.close();
        logger.fine("\n Rnodes_joins are DONE \n" );
    }

    /**
     * for _star  adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    private static String makeStarSepQuery(ResultSet rs, String colName, String del) throws SQLException {
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            String part = rs.getString(colName) + ".MULT";
            parts.add(part);
        }

        return String.join(del, parts);
    }


    /**
     * for _CT part, adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    private static String makeUnionSepQuery(ResultSet rs, String colName, String del) throws SQLException {
    
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            parts.add("`"+rs.getString(colName)+"`");
        }
        return String.join(del, parts);
    }

    /**
     * separate the entries by ","
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    private static String makeCommaSepQuery(ResultSet rs, String colName, String del) throws SQLException {
        ArrayList<String> parts = new ArrayList<String>();
        while(rs.next()){
            parts.add(rs.getString(colName));
        }

        return String.join(del, parts);
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
     * Disconnect all the databases
     * @throws SQLException
     */
    private static void disconnectDB() throws SQLException {
        con_BN.close();
        con_CT.close();
    }
}