package ca.sfu.cs.factorbase.learning;

/** zqian June 18, 2014
 * add the flag variable to tell if we use local ct or not.
 * Flag_UseLocal_CT = true, For TestScoreComputation to score the BN structure;
 * Flag_UseLocal_CT = false,  For FunctorWrapper to learn the weight.
 */
/** May 26, 2014 zqian, when link analysis is off, adding rnode as child in Path_BayesNet for the largest rchain,
 * after propagate the rnode edges in  PropagateContextEdges()
 * `a,b` as rchain, `a` as child, '' as parent
 * `a,b` as rchain, `b` as child, '' as parent
 */
/** Feb 7th 2014, zqian; updated on May 26, 2014 zqian, not suitable for link off
 * Make sure each node appear as a child in Path_BayesNet
 * <child,''>, <child,parent>
 * fixed : for each lattice point,  add one extra step after the structure learning is done
 */
/** Nov 25@ zqian, commont out all the codes related to Path_Forbidden_Edges,
 * @ Jun 5, Zqian
 * This is the Extended version1 based on LAJ algorithm which could discover
 * @1. the Attributes Correlations given the Existence of Links
 * @2. the Correlations between different Link Types
 * @3. the Attributes Correlations given the Absence of Links
 * in a Hierarchical Way.
 *
 * The difference with BayesBase.java is as follows:
 * @1. pre-compute the CT tables using ADTree tricky with which trick there's no need to access the False Relationship.
 * @2. learn the Bayes Nets using CT tables which contain both True and False relationship.
 *
 * Oliver and I believe this method will do the Best.
 */
/** Jun 25, zqian
 *
 * For the BayesBase learning program, the csv generator part could be removed
 * since all the files could be prepared in advance by run the program CSVPrecomputor.java
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import nu.xom.ParsingException;
import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.exception.ScoringException;
import ca.sfu.cs.factorbase.exporter.bifexporter.BIF_Generator;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIFExport;
import ca.sfu.cs.factorbase.graph.Edge;
import ca.sfu.cs.factorbase.jbn.BayesNet_Learning_main;
import ca.sfu.cs.factorbase.lattice.RelationshipLattice;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;
import ca.sfu.cs.factorbase.util.RuntimeLogger;


public class BayesBaseH {

    static Connection con2, con3;

    // To be read from config.
    static String databaseName, databaseName2, databaseName3;
    static String setupDatabaseName;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;

    static String opt3, cont;
    static boolean linkAnalysis;
    static boolean Flag_UseLocal_CT; //zqian June 18, 2014
    static boolean SkipParameterLearning;


    /**
     * iff Running Time == 1, then generate the csv files.
     * else, just reuse the old csv files. May 1st @zqian.
     */
    static int FirstRunning = 1;

    private static Logger logger = Logger.getLogger(BayesBaseH.class.getName());


    /**
     * Carry out the structure and parameter learning of the Bayesian network for the input database.
     *
     * @param database - {@code FactorBaseDataBase} to help extract the necessary information required to learn a
     *                   Bayesian network for the input database.
     * @param countingStrategy - the counting strategy to use during structure learning.
     * @throws IOException if there are issues reading and writing various files.
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws ParsingException if there are issues reading the BIF file.
     * @throws DataExtractionException if a non database error occurs when retrieving the DataExtractor.
     * @throws DataBaseException if a database error occurs when retrieving the DataExtractor.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    public static void runBBH(
        FactorBaseDataBase database,
        RelationshipLattice globalLattice,
        String databaseCollation,
        CountingStrategy countingStrategy
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        initProgram(FirstRunning);
        connectDB();

        // Get the height of the lattice (max length of RChain).
        int latticeHeight = globalLattice.getHeight();

        // Get the longest RChain.
        String rchain = globalLattice.getLongestRChain();

        // Structure learning.
        long start = System.currentTimeMillis();
        StructureLearning(
            database,
            con2,
            databaseCollation,
            countingStrategy,
            globalLattice
        );

        RuntimeLogger.logRunTime(logger, "Structure Learning", start, System.currentTimeMillis());

        // Change the CallLogs table to use the InnoDB storage engine so that we retain the data even if the server is
        // turned off.
        try (Statement st = con2.createStatement()) {
            st.execute("ALTER TABLE CallLogs ENGINE = InnoDB;");
        }

        /**
         * OS: Nov 17, 2016. It can happen that Tetrad learns a forbidden edge. Argh. To catch this, we delete forbidden edges from any insertion. But then
         * it can happen that a node has no edge at all, not even with an empty parent. In that case the Bif generator gets messed up. So we catch such
         * orphaned nodes in the next statement.
         */
        logger.fine("Inserting the Missing Fid as Child into Path_Bayes_Nets \n");
        Statement st = con2.createStatement();
        st.execute(
            "INSERT IGNORE INTO Path_BayesNets " +
            "SELECT '" + rchain + "' AS Rchain, Fid AS child, '' AS parent " +
            "FROM FNodes " +
            "WHERE Fid NOT IN (" +
                "SELECT DISTINCT child " +
                "FROM Path_BayesNets " +
                "WHERE Rchain = '" + rchain + "'" +
            ")");

        // Create final version of the BayesNet table without entries where the parent is ''.
        BIF_Generator.Final_Path_BayesNets(con2, rchain);

        // Export the final result to xml.  We assume that there is a single largest relationship chain and write the Bayes net for that relationship chain to xml.
        // Only export the structure, prepare for the pruning phase, Oct 23, 2013.
        exportResults(latticeHeight);

        // Parameter learning.
        // Add setup options Yan Sept. 10th
        // Continuous
        if (!cont.equals("1") && !SkipParameterLearning) {
            // Now compute conditional probability estimates and write them to @database@_BN.
            logger.fine("\n Structure Learning is DONE.  ready for parameter learning."); //@zqian

            //      @zqian  for TestScoreComputation, use local ct to compute local CP.
            if (Flag_UseLocal_CT) {
                logger.fine("\n For BN_ScoreComputation.  use local_CT to compute the local_CP.");
            } else {
                start = System.currentTimeMillis();
                // For FunctorWrapper, do NOT have to use the local_CT, or HAVE TO change the weight learning part. June 18 2014.
                CPGenerator.Generator(databaseName, con2); // May 22, 2014 zqian, computing the score for link analysis off.
                CP mycp = new CP(databaseName2, databaseName3);
                mycp.cp();
                RuntimeLogger.logRunTime(logger, "Parameter Learning", start, System.currentTimeMillis());
            }

            // Score Bayes net: compute KL divergence, and log-likelihood (average probability of node value given its Markov blanket, compared to database frequencies)
            // May 7th, zqian, For RDN do not need to do the smoothing
            // COMPUTE KLD
            start = System.currentTimeMillis();
            if (opt3.equals("1")) {
                KLD_generator.KLDGenerator(databaseName, con2);
                RuntimeLogger.logRunTime(logger, "KLD Generator (KLDGenerator)", start, System.currentTimeMillis());
            } else {
                KLD_generator.smoothed_CP(rchain, con2);
                RuntimeLogger.logRunTime(logger, "KLD Generator (Smoothed CP)", start, System.currentTimeMillis());
            }

            // Generating the bif file, in order to feed into UBC tool (bayes.jar). Based on the largest relationship chain.
            // Need CP tables.
            BIF_Generator.generate_bif(databaseName, "Bif_" + databaseName + ".xml", con2);
        } else {
            logger.fine("\n Structure Learning is DONE. \n No parameter learning done."); // @zqian
        }

        // Disconnect from db.
        disconnectDB();
    }


    private static void StructureLearning(
        FactorBaseDataBase database,
        Connection conn,
        String databaseCollation,
        CountingStrategy countingStrategy,
        RelationshipLattice lattice
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        // Handle pvars.
        if (countingStrategy.isPrecount()) {
            learnStructurePVars(database); // import @zqian
        } else {
            learnStructurePVarsOnDemand(database);
        }

        // Set up Bayesian Network structure information, which comes from the database schema and entity Bayesian
        // Networks learned up to now, to propagate to the structure learning for RChains.
        MySQLScriptRunner.runScript(
            conn,
            Config.SCRIPTS_DIRECTORY + "modelmanager_populate.sql",
            databaseName,
            databaseCollation
        );

        // Handle rnodes in a bottom-up way following the lattice.
        if (countingStrategy.isPrecount()) {
            learnStructureRChains(database, lattice);
        } else {
            learnStructureRChainsOnDemand(database, lattice);
        }

        // Population lattice.
        PropagateContextEdges(lattice.getHeight());

        /**
         * OS May 23. 2014 This looks like a much too complicated way to find the context edges. How about this:
         * 1. Use a view Contextedges to find the context edges for each Rchain.
         * 2. Union these edges over the rchain, insert them into the largest rchain.
         * 3. Make sure you insert "<rnid> null" into PathBN as well.
         * zqian, when link is off, check the local_ct for rnode?
         */
    }


    private static void initProgram(int FirstRunning) throws IOException, SQLException {
        // Read config file.
        setVarsFromConfig();

        String databaseDirectory = databaseName + "/" + File.separator;

        if (FirstRunning==1) {
            try {
                delete(new File(databaseDirectory));
            } catch (Exception e) {
            }

            new File(databaseDirectory).mkdirs();
            new File(databaseName + "/" + File.separator + "res" + File.separator).mkdirs();
        }
    }


    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }


    private static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName = conf.getProperty("dbname");
        databaseName2 = databaseName + "_BN";
        databaseName3 = databaseName + "_CT";
        setupDatabaseName = databaseName + "_setup";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        opt3 = conf.getProperty("ComputeKLD");
        cont = conf.getProperty("Continuous");
        String strLinkAnalysis = conf.getProperty("LinkCorrelations");
        linkAnalysis = strLinkAnalysis.equalsIgnoreCase("1");
        SkipParameterLearning = conf.getProperty("SkipParameterLearning").equalsIgnoreCase("1");

        //zqian June 18, 2014
        String UseLocal_CT = conf.getProperty( "UseLocal_CT" );
        Flag_UseLocal_CT = UseLocal_CT.equalsIgnoreCase("1");
    }


    private static void connectDB() throws SQLException {
        Properties connectionProperties = MySQLFactorBaseDataBase.getConnectionStringProperties(
            dbUsername,
            dbPassword
        );

        String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
        try {
            java.lang.Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }

        con2 = DriverManager.getConnection(CONN_STR2, connectionProperties);

        String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName3;
        try {
            java.lang.Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }

        con3 = DriverManager.getConnection(CONN_STR3, connectionProperties);
    }

    /** Jun 14
     * if the tuples greater than 1, then employ tetradlearner.
     * else just insert the 1nid as child into entity_bayesnet.
     *
     * @throws DataBaseException if a database error occurs when retrieving the DataExtractor.
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws IOException if there are issues generating the BIF file.
     * @throws DataExtractionException if a non database error occurs when retrieving the DataExtractor.
     * @throws ParsingException if there are issues reading the BIF file.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    private static void learnStructurePVars(
        FactorBaseDataBase database
    ) throws DataBaseException, SQLException, DataExtractionException, IOException, ParsingException, ScoringException {
        // Retrieve all the PVariables.
        List<String> pvar_ids = database.getPVariablesFunctorNodeInfo().stream().map(
            functorNodeInfo -> functorNodeInfo.getID()
        ).collect(Collectors.toList());

        String NoTuples = "";
        for(String id : pvar_ids) {
            logger.fine("\nStarting Learning the BN Structure of pvar_ids: " + id + "\n");
            Statement st = con3.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM `" + id + "_counts`;"); // Optimize this query, too slow, Nov 13, zqian.
            while(rs.next()) {
                NoTuples = rs.getString(1);
                logger.fine("NoTuples : " + NoTuples);
            }

            if (Integer.parseInt(NoTuples) > 1) {
                List<Edge> graphEdges = BayesNet_Learning_main.tetradLearner(
                    database.getAndRemoveCTDataExtractor(id),
                    !cont.equals("1")
                );
//O.S. Oct 16, 2019. should allow background knowledge specified by user. Cf. with tetradLearner in handleRchains
                
                database.insertLearnedEdges(id, graphEdges, "Entity_BayesNets", false);
            } else {
                String selectQuery =
                    "SELECT 1nid " +
                    "FROM 1Nodes, " + setupDatabaseName + ".EntityTables " +
                    "WHERE 1Nodes.pvid = CONCAT(" + setupDatabaseName + ".EntityTables.Table_name,'0') " +
                    "AND 1Nodes.pvid = '" + id + "';";

                Statement st2 = con2.createStatement();
                // Insert the BN nodes into Entity_BayesNet.
                RuntimeLogger.logExecutedQuery(logger, selectQuery);
                ResultSet rs2 = st2.executeQuery(selectQuery);
                String child = "";

                while(rs2.next()) {
                    Statement st3 = con2.createStatement();
                    child = rs2.getString("1nid");
                    logger.fine("INSERT IGNORE INTO Entity_BayesNets VALUES ('" + id + "', '" + child + "', '');");
                    st3.execute("INSERT IGNORE INTO Entity_BayesNets VALUES ('" + id + "', '" + child + "', '');");
                    st3.close();
                }

                rs2.close();
                st2.close();
            }

            logger.fine("\nEnd for " + id + "\n");
        }
    }


    /**
     * Learn the Bayesian network structure for the PVariables.
     *
     * @param database - {@code FactorBaseDataBase} to help extract the necessary information required to learn a
     *                   Bayesian network for PVariables.
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws IOException if there are issues generating the BIF file.
     * @throws ParsingException if there are issues reading the BIF file.
     * @throws DataBaseException if a database error occurs when retrieving the information from the database.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    private static void learnStructurePVarsOnDemand(
        FactorBaseDataBase database
    ) throws SQLException, IOException, ParsingException, DataBaseException, ScoringException {
        // Retrieve all the PVariables.
        List<FunctorNodesInfo> pvarFunctorNodeInfos = database.getPVariablesFunctorNodeInfo();

        for(FunctorNodesInfo pvarFunctorNodeInfo : pvarFunctorNodeInfos) {
            String id = pvarFunctorNodeInfo.getID();
            logger.fine("\nStarting Learning the BN Structure of pvar_ids: " + id + "\n");

            List<Edge> graphEdges = BayesNet_Learning_main.tetradLearner(
                database,
                pvarFunctorNodeInfo,
                !cont.equals("1")
            );

            database.insertLearnedEdges(id, graphEdges, "Entity_BayesNets", false);

            logger.fine("\nEnd for " + id + "\n");
        }
    }


    private static void learnStructureRChains(
        FactorBaseDataBase database,
        RelationshipLattice lattice
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        // for loop to build the Bayesian network by following the relationship lattice in a bottom-up manner.
        for(int len = 1; len <= lattice.getHeight(); len++) {
            List<String> rnode_ids = lattice.getRChainsInfo(len).stream().map(
                functorNodeInfo -> functorNodeInfo.getID()
            ).collect(Collectors.toList());

            // Retrieve the required edge information.
            List<Edge> requiredEdges = database.getRequiredEdges(rnode_ids);

            // Retrieve the forbidden edge information.
            List<Edge> forbiddenEdges = database.getForbiddenEdges(rnode_ids);

            // for loop to build the Bayesian network for each RChain at the currently specified length.
            String NoTuples = "";
            for(String id : rnode_ids) {
                logger.fine("\nStarting Learning the BN Structure of rnode_ids: " + id + "\n");
                Statement mapping_st = con2.createStatement();
                Statement st = con3.createStatement();
                ResultSet rnidMappingResult = mapping_st.executeQuery(
                    "SELECT short_rnid " +
                    "FROM lattice_mapping " +
                    "WHERE lattice_mapping.orig_rnid = '" + id + "';"
                );
                rnidMappingResult.absolute(1);
                String short_rnid = rnidMappingResult.getString("short_rnid");
                ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) " +
                    "FROM `" + short_rnid + "_CT`;"
                ); // Oct 2nd, Why not check the csv file directly? faster for larger CT? Oct 23, Comment from: Unknown since lacking Git history.

                while(rs.next()) {
                    NoTuples = rs.getString(1);
                    logger.fine("NoTuples : " + NoTuples);
                }

                if(Integer.parseInt(NoTuples) > 1) {
                    List<Edge> graphEdges = BayesNet_Learning_main.tetradLearner(
                        database.getAndRemoveCTDataExtractor(id),
                        requiredEdges,
                        forbiddenEdges,
                        !cont.equals("1")
                    );

                    logger.fine("The BN Structure Learning for rnode_id:" + id + "is done."); //@zqian Test
                    database.insertLearnedEdges(id, graphEdges, "Path_BayesNets", true);
                }
            }

            database.propagateEdgeInformation(len, linkAnalysis);

            logger.fine(" Import is done for length = " + len + "."); // @zqian Test
        }
    }


    /**
     * Learn the Bayesian network structure for the chains of RNodes (RChains).
     *
     * @param database - {@code FactorBaseDataBase} to help extract the necessary information required to learn a
     *                   Bayesian network for chains of RNodes.
     * @param lattice - global relationship lattice for the input database.
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws IOException if there are issues generating the BIF file.
     * @throws DataBaseException if a database error occurs when retrieving the information from the database.
     * @throws ParsingException if there are issues reading the BIF file.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    private static void learnStructureRChainsOnDemand(
        FactorBaseDataBase database,
        RelationshipLattice lattice
    ) throws SQLException, IOException, DataBaseException, ParsingException, ScoringException {
        // for loop to build the Bayesian network by following the relationship lattice in a bottom-up manner.
        for(int height = 1; height <= lattice.getHeight(); height++) {
            List<FunctorNodesInfo> rchainFunctorNodeInfos = lattice.getRChainsInfo(height);
            List<String> rchainIDs = rchainFunctorNodeInfos.stream().map(
                functorNodeInfo -> functorNodeInfo.getID()
            ).collect(Collectors.toList());

            // Retrieve the required edge information.
            List<Edge> requiredEdges = database.getRequiredEdges(rchainIDs);

            // Retrieve the forbidden edge information.
            List<Edge> forbiddenEdges = database.getForbiddenEdges(rchainIDs);

            // for loop to build the Bayesian network for each RChain at the currently specified length.
            for(FunctorNodesInfo rchainFunctorNodeInfo : rchainFunctorNodeInfos) {
                String rchainID = rchainFunctorNodeInfo.getID();
                logger.fine("\nStart Learning the BN Structure of the RChain: " + rchainID + "\n");
                List<Edge> graphEdges = BayesNet_Learning_main.tetradLearner(
                    database,
                    rchainFunctorNodeInfo,
                    requiredEdges,
                    forbiddenEdges,
                    !cont.equals("1")
                );

                logger.fine("The BN Structure Learning for RChain:" + rchainID + "is done.");
                database.insertLearnedEdges(rchainID, graphEdges, "Path_BayesNets", true);
            }

            database.propagateEdgeInformation(height, linkAnalysis);

            logger.fine(" Import is done for length = " + height + ".");
        }
    }


    private static ArrayList<String> PropagateContextEdges(int maxNumberOfMembers) throws SQLException {
        Statement st = con2.createStatement();
        st.execute("DROP TABLE IF EXISTS RNodeEdges;");
        st.execute("CREATE TABLE RNodeEdges LIKE Path_BayesNets;");
        st.execute("DROP TABLE IF EXISTS ContextEdges;");
        st.execute(
            "CREATE TABLE ContextEdges AS SELECT DISTINCT NewLearnedEdges.Rchain AS Rchain, NewLearnedEdges.child AS child, lattice_membership.member AS parent " +
            "FROM NewLearnedEdges, lattice_membership " +
            "WHERE NewLearnedEdges.Rchain = lattice_membership.name;"
        );
        st.execute(
            "INSERT IGNORE INTO RNodeEdges " +
            "SELECT Rchain, child, parent " +
            "FROM ContextEdges, lattice_set " +
            "WHERE lattice_set.name = ContextEdges.Rchain " +
            "AND lattice_set.length = 1;"
        );

        for(int len = 2; len <= maxNumberOfMembers; len++) {
            st.execute(
                "INSERT IGNORE INTO RNodeEdges " +
                "SELECT Rchain, child, parent " +
                "FROM ContextEdges, lattice_set " +
                "WHERE lattice_set.name = ContextEdges.Rchain " +
                "AND lattice_set.length = " + len + " " +
                "UNION " +
                "SELECT DISTINCT lattice_rel.child, RNodeEdges.child, RNodeEdges.parent " +
                "FROM lattice_set, lattice_rel, RNodeEdges " +
                "WHERE lattice_set.length = " + len + " " +
                "AND lattice_rel.child = lattice_set.name " +
                "AND RNodeEdges.Rchain = lattice_rel.parent;"
            );
        }

        st.execute("INSERT IGNORE INTO Path_BayesNets SELECT * FROM RNodeEdges;");

        /**
         * Start.
         * Adding rnode as child in Path_BayesNet for the largest rchain, May 26, 2014 zqian.
         * `a,b` as rchain, `a` as child, '' as parent
         * `a,b` as rchain, `b` as child, '' as parent
         */
        String largest_rchain = "";
        Statement st_largest = con2.createStatement();
        ResultSet rs_largest = st_largest.executeQuery("SELECT name AS Rchain FROM lattice_set WHERE length = (SELECT MAX(length) FROM lattice_set);");
        rs_largest.absolute(1);
        largest_rchain = rs_largest.getString(1);
        st_largest.close();

        Statement st1 = con2.createStatement();

        ResultSet rs = st1.executeQuery(
            "SELECT " +
                "name AS RChain " +
            "FROM " +
                "lattice_set " +
            "WHERE " +
                "lattice_set.length = 1;"
        );

        ArrayList<String> rnode_ids = new ArrayList<String>();

        while(rs.next()) {
            // Get rvid for further use.
            String rchain = rs.getString("RChain");
            rnode_ids.add(rchain);
        }
        st1.close();
        Statement st_temp = con2.createStatement();
        for(String id : rnode_ids) { // Feb 7th 2014, zqian; updated May 26, 2014 zqian.
            st_temp.execute("INSERT IGNORE INTO Path_BayesNets (SELECT '" + largest_rchain + "' AS Rchain, '" + id + "' AS child, '' AS parent);");
        }

        st_temp.close();
        // End for adding rnode as child, May 26th, 2014 zqian.
        st.close();

        return rnode_ids;
    }


    private static void exportResults(int maxNumberOfMembers) throws SQLException, IOException {
        Statement st = con2.createStatement();

        ResultSet rs = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + maxNumberOfMembers + ";");
        while (rs.next()) {
            String setName = rs.getString("name");
            BIFExport.Export(databaseName + "/" + File.separator + "res" + File.separator + setName + ".xml", "Rchain", "Path_BayesNets", setName, con2);
        }

        st.close();
    }


    private static void disconnectDB() throws SQLException {
        con2.close();
        con3.close();
    }
}
