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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nu.xom.ParsingException;
import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.exception.ScoringException;
import ca.sfu.cs.factorbase.exporter.bifexporter.BIF_Generator;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIFExport;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIFImport;
import ca.sfu.cs.factorbase.graph.Edge;
import ca.sfu.cs.factorbase.jbn.BayesNet_Learning_main;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;

import com.mysql.jdbc.Connection;

public class BayesBaseH {

    static Connection con2, con3;

    // To be read from config.
    static String databaseName, databaseName2, databaseName3;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;

    static String opt3, cont;
    static boolean linkAnalysis;
    static boolean Flag_UseLocal_CT; //zqian June 18, 2014


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
     * @param ctTablesGenerated - True if CT tables have already been generated via precounting; otherwise false.
     * @throws IOException if there are issues reading and writing various files.
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws ParsingException if there are issues reading the BIF file.
     * @throws DataExtractionException if a non database error occurs when retrieving the DataExtractor.
     * @throws DataBaseException if a database error occurs when retrieving the DataExtractor.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    public static void runBBH(
        FactorBaseDataBase database,
        boolean ctTablesGenerated
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        initProgram(FirstRunning);
        connectDB();

        // Build tables for structure learning.
        // Set up the bayes net models O.S. Sep 12, 2017.
        if (ctTablesGenerated) {
            MySQLScriptRunner.runScript(
                con2,
                Config.SCRIPTS_DIRECTORY + "modelmanager_initialize.sql",
                databaseName
            );
        }

        // Get maxNumberOfMembers (max length of rchain).
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT MAX(length) FROM lattice_set;");
        rst.absolute(1);
        int maxNumberOfMembers = rst.getInt(1);

        // Get the longest rchain.
        String rchain = null;
        ResultSet rst1 = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + maxNumberOfMembers + ";");
        rst1.absolute(1);
        rchain = rst1.getString(1);

        // Structure learning.
        StructureLearning(database, con2, ctTablesGenerated, maxNumberOfMembers);

        /**
         * OS: Nov 17, 2016. It can happen that Tetrad learns a forbidden edge. Argh. To catch this, we delete forbidden edges from any insertion. But then
         * it can happen that a node has no edge at all, not even with an empty parent. In that case the Bif generator gets messed up. So we catch such
         * orphaned nodes in the next statement.
         */
        logger.fine("Inserting the Missing Fid as Child into Path_Bayes_Nets \n");
        st.execute(
            "INSERT IGNORE INTO Path_BayesNets " +
            "SELECT '" + rchain + "' AS Rchain, Fid AS child, '' AS parent " +
            "FROM FNodes " +
            "WHERE Fid NOT IN (" +
                "SELECT DISTINCT child " +
                "FROM Path_BayesNets " +
                "WHERE Rchain = '" + rchain + "'" +
            ")");

        // Mapping the orig_rnid back and create a new table: Final_Path_BayesNets. //Sep 19, zqian
        BIF_Generator.Final_Path_BayesNets(con2, rchain);

        // Parameter learning.
        // Add setup options Yan Sept. 10th
        // Continuous
        if (!cont.equals("1")) {
            // Now compute conditional probability estimates and write them to @database@_BN.
            logger.fine("\n Structure Learning is DONE.  ready for parameter learning."); //@zqian

            // Export the final result to xml.  We assume that there is a single largest relationship chain and write the Bayes net for that relationship chain to xml.
            // Only export the structure, prepare for the pruning phase, Oct 23, 2013.
            exportResults(maxNumberOfMembers);

            //      @zqian  for TestScoreComputation, use local ct to compute local CP.
            if (Flag_UseLocal_CT) {
                logger.fine("\n For BN_ScoreComputation.  use local_CT to compute the local_CP.");
            } else {
                // For FunctorWrapper, do NOT have to use the local_CT, or HAVE TO change the weight learning part. June 18 2014.
                CPGenerator.Generator(databaseName, con2); // May 22, 2014 zqian, computing the score for link analysis off.
                CP mycp = new CP(databaseName2, databaseName3);
                mycp.cp();
                logger.fine("\n Parameter learning is done.");
                //  For FunctorWrapper
            }

            // Score Bayes net: compute KL divergence, and log-likelihood (average probability of node value given its Markov blanket, compared to database frequencies)
            // May 7th, zqian, For RDN do not need to do the smoothing
            // COMPUTE KLD
            long l = System.currentTimeMillis(); // @zqian: measure structure learning time

            if (opt3.equals("1")) {
                logger.fine("\n KLD_generator.KLDGenerator.");
                KLD_generator.KLDGenerator(databaseName, con2);
            } else {
                logger.fine("\n KLD_generator.smoothed_CP.");
                KLD_generator.smoothed_CP(rchain, con2);
            }

            // Generating the bif file, in order to feed into UBC tool (bayes.jar). Based on the largest relationship chain.
            // Need CP tables.
            BIF_Generator.generate_bif(databaseName, "Bif_" + databaseName + ".xml", con2);

            long l2 = System.currentTimeMillis(); // @zqian: measure structure learning time.
            logger.fine("smoothed_CP Time(ms): " + (l2 - l) + " ms.\n");
        } else {
            logger.fine("\n Structure Learning is DONE. \n NO parameter learning for Continuous data."); // @zqian
        }

        // Disconnect from db.
        disconnectDB();
    }


    private static void StructureLearning(
        FactorBaseDataBase database,
        Connection conn,
        boolean ctTablesGenerated,
        int maxNumberOfMembers
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        long l = System.currentTimeMillis(); // @zqian: measure structure learning time.

        // Handle pvars.
        if (ctTablesGenerated) {
            learnStructurePVars(database); // import @zqian
        } else {
            learnStructurePVarsOnDemand(database);
        }

        Statement st = conn.createStatement();
        st.execute(
            "INSERT IGNORE INTO Path_Required_Edges " +
            "SELECT DISTINCT RNodes_pvars.rnid AS Rchain, Entity_BayesNets.child AS child, Entity_BayesNets.parent AS parent " +
            "FROM (RNodes_pvars, Entity_BayesNets) " +
            "WHERE (RNodes_pvars.pvid = Entity_BayesNets.pvid " +
            "AND Entity_BayesNets.parent <> '');"
        );
        st.execute("DROP TABLE IF EXISTS Entity_BN_Nodes;");
        st.execute(
            "CREATE TABLE Entity_BN_Nodes AS " +
            "SELECT Entity_BayesNets.pvid AS pvid, Entity_BayesNets.child AS node " +
            "FROM Entity_BayesNets " +
            "ORDER BY pvid;"
        );
        st.execute(
            "INSERT IGNORE INTO Entity_Complement_Edges " +
            "SELECT DISTINCT BN_nodes1.pvid AS pvid, BN_nodes1.node AS child, BN_nodes2.node AS parent " +
            "FROM Entity_BN_Nodes AS BN_nodes1, Entity_BN_Nodes AS BN_nodes2 " +
            "WHERE BN_nodes1.pvid = BN_nodes2.pvid " +
            "AND (NOT (EXISTS(" +
                "SELECT * FROM Entity_BayesNets " +
                "WHERE (Entity_BayesNets.pvid = BN_nodes1.pvid) " +
                "AND (Entity_BayesNets.child = BN_nodes1.node) " +
                "AND (Entity_BayesNets.parent = BN_nodes2.node))));"
        );
        st.execute(
            "INSERT IGNORE INTO Path_Forbidden_Edges " +
            "SELECT DISTINCT RNodes_pvars.rnid AS Rchain, Entity_Complement_Edges.child AS child, Entity_Complement_Edges.parent AS parent " +
            "FROM (RNodes_pvars, Entity_Complement_Edges) " +
            "WHERE (RNodes_pvars.pvid = Entity_Complement_Edges.pvid);"
        );
        st.close();

        // Handle rnodes in a bottom-up way following the lattice.
        // Generating .CSV files by reading _CT tables directly (including TRUE relationship and FALSE relationship).
        handleRNodes_zqian(database, maxNumberOfMembers); // import
        // Population lattice.
        PropagateContextEdges(maxNumberOfMembers);

        /**
         * OS May 23. 2014 This looks like a much too complicated way to find the context edges. How about this:
         * 1. Use a view Contextedges to find the context edges for each Rchain.
         * 2. Union these edges over the rchain, insert them into the largest rchain.
         * 3. Make sure you insert "<rnid> null" into PathBN as well.
         * zqian, when link is off, check the local_ct for rnode?
         */

        long l2 = System.currentTimeMillis(); // @zqian: Measure structure learning time.
        logger.fine("\n*****************\nStructure Learning Time(ms): " + (l2 - l) + " ms.\n");
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
            new File(databaseName + "/" + File.separator + "xml" + File.separator).mkdirs();
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
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        opt3 = conf.getProperty("ComputeKLD");
        cont = conf.getProperty("Continuous");
        String strLinkAnalysis = conf.getProperty("LinkCorrelations");

        if (strLinkAnalysis.equalsIgnoreCase("1")) {
            linkAnalysis = true;
        } else {
            linkAnalysis = false;
        }

        //zqian June 18, 2014
        String UseLocal_CT = conf.getProperty( "UseLocal_CT" );
        if (UseLocal_CT.equalsIgnoreCase("1")) {
            Flag_UseLocal_CT = true;
        } else {
            Flag_UseLocal_CT = false;
        }
    }


    private static void connectDB() throws SQLException {
        String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }

        con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);

        String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName3;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }

        con3 = (Connection) DriverManager.getConnection(CONN_STR3, dbUsername, dbPassword);
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
        String[] pvar_ids = database.getPVariables();

        String NoTuples = "";
        for(String id : pvar_ids) {
            logger.fine("\nStarting Learning the BN Structure of pvar_ids: " + id + "\n");
            Statement st = con3.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM `" + id.replace("`","") + "_counts`;"); // Optimize this query, too slow, Nov 13, zqian.
            while(rs.next()) {
                NoTuples = rs.getString(1);
                logger.fine("NoTuples : " + NoTuples);
            }

            if (Integer.parseInt(NoTuples) > 1) {
                BayesNet_Learning_main.tetradLearner(
                    database.getAndRemoveCTDataExtractor(id.replace("`","")),
                    databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml",
                    !cont.equals("1")
                );

                bif1(id);
            } else {
                Statement st2 = con2.createStatement();
                // Insert the BN nodes into Entity_BayesNet.
                logger.fine("SELECT 1nid FROM 1Nodes, EntityTables WHERE 1Nodes.pvid = CONCAT(EntityTables.Table_name,'0') AND 1Nodes.pvid = '" + id + "';");
                ResultSet rs2 = st2.executeQuery("SELECT 1nid FROM 1Nodes, EntityTables WHERE 1Nodes.pvid = CONCAT(EntityTables.Table_name,'0') AND 1Nodes.pvid = '" + id + "';");
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

            BayesNet_Learning_main.tetradLearner(
                database,
                pvarFunctorNodeInfo,
                databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml",
                !cont.equals("1")
            );

            bif1(id);

            logger.fine("\nEnd for " + id + "\n");
        }
    }


    private static void handleRNodes_zqian(
        FactorBaseDataBase database,
        int maxNumberOfMembers
    ) throws SQLException, IOException, DataBaseException, DataExtractionException, ParsingException, ScoringException {
        for(int len = 1; len <= maxNumberOfMembers; len++) {
            ArrayList<String> rnode_ids = readRNodesFromLattice(len); // Create csv files for all rnodes.

            // Retrieve the required edge information.
            List<Edge> requiredEdges = database.getRequiredEdges(rnode_ids);

            // Retrieve the forbidden edge information.
            List<Edge> forbiddenEdges = database.getForbiddenEdges(rnode_ids);

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
                    "FROM `" + short_rnid.replace("`","") + "_CT`;"
                ); // Oct 2nd, Why not check the csv file directly? faster for larger CT? Oct 23, Comment from: Unknown since lacking Git history.

                while(rs.next()) {
                    NoTuples = rs.getString(1);
                    logger.fine("NoTuples : " + NoTuples);
                }

                if(Integer.parseInt(NoTuples) > 1) {
                    BayesNet_Learning_main.tetradLearner(
                        database.getAndRemoveCTDataExtractor(id.replace("`","")),
                        requiredEdges,
                        forbiddenEdges,
                        databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml",
                        !cont.equals("1")
                    );

                    logger.fine("The BN Structure Learning for rnode_id::" + id + "is done."); //@zqian Test
                    bif2(id); // import to db   @zqian
                }
            }

            // import to db @zqian
            Statement st = con2.createStatement();

            // propagate all edges to next level
            st.execute(
                "INSERT IGNORE INTO InheritedEdges " +
                "SELECT DISTINCT lattice_rel.child AS Rchain, Path_BayesNets.child AS child, Path_BayesNets.parent AS parent " +
                "FROM Path_BayesNets, lattice_rel, lattice_set " +
                "WHERE lattice_rel.parent = Path_BayesNets.Rchain " +
                "AND Path_BayesNets.parent <> '' " +
                "AND lattice_set.name = lattice_rel.parent " +
                "AND lattice_set.length = " + (len) + " " +
                "ORDER BY Rchain;"
            );

            if (!linkAnalysis) {
                // Find new edges learned for this rchain, that were not already required before learning.
                st.execute(
                    "INSERT IGNORE INTO LearnedEdges " +
                    "SELECT DISTINCT Path_BayesNets.Rchain, Path_BayesNets.child, Path_BayesNets.parent " +
                    "FROM Path_BayesNets, lattice_set, lattice_rel " +
                    "WHERE Path_BayesNets.parent <> '' " +
                    "AND lattice_set.name = lattice_rel.parent " +
                    "AND lattice_set.length = " + len + " " +
                    "AND (Path_BayesNets.Rchain, Path_BayesNets.child, Path_BayesNets.parent) NOT IN (" +
                        "SELECT * FROM Path_Required_Edges" +
                    ");"
                );

                // Propagate all edges to next level.
                st.execute(
                    "INSERT IGNORE INTO InheritedEdges " +
                    "SELECT DISTINCT lattice_rel.child AS Rchain, Path_BayesNets.child AS child, Path_BayesNets.parent AS parent " +
                    "FROM Path_BayesNets, lattice_rel, lattice_set " +
                    "WHERE lattice_rel.parent = Path_BayesNets.Rchain " +
                    "AND Path_BayesNets.parent <> '' " +
                    "AND lattice_set.name = lattice_rel.parent " +
                    "AND lattice_set.length = " + (len) + " " +
                    "ORDER BY Rchain;"
                );

                // KURT: Alternate LearnedEdges.
                st.execute(
                    "INSERT IGNORE INTO NewLearnedEdges " +
                    "SELECT Path_BayesNets.Rchain, Path_BayesNets.child, Path_BayesNets.parent " +
                    "FROM Path_BayesNets, lattice_set " +
                    "WHERE Path_BayesNets.parent <> '' " +
                    "AND Path_BayesNets.Rchain = lattice_set.name " +
                    "AND lattice_set.length = " + len + " " +
                    "AND (Path_BayesNets.Rchain, Path_BayesNets.child, Path_BayesNets.parent) NOT IN (" +
                        "SELECT * " +
                        "FROM Path_Required_Edges" +
                    ");"
                );

                st.execute(
                    "INSERT IGNORE INTO InheritedEdges " +
                    "SELECT DISTINCT NewLearnedEdges.Rchain AS Rchain, NewLearnedEdges.child AS child, lattice_membership.member AS parent " +
                    "FROM NewLearnedEdges, lattice_membership " +
                    "WHERE NewLearnedEdges.Rchain = lattice_membership.name;"
                );

                st.execute(
                    "INSERT IGNORE INTO Path_BayesNets " +
                    "SELECT * " +
                    "FROM InheritedEdges;"
                );
            }

            // Make inherited edges as required edges, while avoiding conflict edges
            //#### Design Three Required Edges: propagate edges from/to 1Nodes/2Nodes + SchemaEdges + RNodes to 1Nodes/2Nodes (same as Design Two).
            st.execute(
                "INSERT IGNORE INTO Path_Required_Edges " +
                "SELECT DISTINCT Rchain, child, parent " +
                "FROM InheritedEdges, lattice_set " +
                "WHERE Rchain = lattice_set.name " +
                "AND lattice_set.length = " + (len + 1) + " " +
                "AND (Rchain, parent, child) NOT IN (" +
                    "SELECT * " +
                    "FROM InheritedEdges" +
                ") AND child NOT IN (" +
                    "SELECT rnid " +
                    "FROM RNodes" +
                ")"
            );

            // For path_complemtment edges, rchain should be at current level (len).
            // Nov25
            st.execute(
                "INSERT IGNORE INTO Path_Complement_Edges " +
                "SELECT DISTINCT BN_nodes1.Rchain AS Rchain, BN_nodes1.node AS child, BN_nodes2.node AS parent " +
                "FROM Path_BN_nodes AS BN_nodes1, Path_BN_nodes AS BN_nodes2, lattice_set " +
                "WHERE lattice_set.name = BN_nodes1.Rchain AND lattice_set.length = " + len + " " +
                "AND (" +
                    "(BN_nodes1.Rchain = BN_nodes2.Rchain)" +
                    "AND (NOT (EXISTS(" +
                        "SELECT * " +
                        "FROM Path_BayesNets " +
                        "WHERE (" +
                            "(Path_BayesNets.Rchain = BN_nodes1.Rchain) " +
                            "AND (Path_BayesNets.child = BN_nodes1.node) " +
                            "AND (Path_BayesNets.parent = BN_nodes2.node)" +
                       ")" +
                    ")))" +
                ");"
            );


            // For path forbidden edges, rchain should be at higher level (len+1), so its parent should be at current level (len).
            // Make absent edges as forbidden edges, and give higher priority of required edges in case of conflict edges.
            //#### Design Three Forbidden Edges: propagate edges from/to 1Nodes/2Nodes + 1Nodes/2Nodes to RNodes.
            // Nov 25
            st.execute(
                "INSERT IGNORE INTO Path_Forbidden_Edges " +
                "SELECT DISTINCT lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent " +
                "FROM Path_Complement_Edges, lattice_rel, lattice_set " +
                "WHERE lattice_set.name = lattice_rel.parent " +
                "AND lattice_set.length = " + len + " " +
                "AND lattice_rel.parent = Path_Complement_Edges.Rchain " +
                "AND Path_Complement_Edges.parent <> '' " +
                "AND (lattice_rel.child, Path_Complement_Edges.child, Path_Complement_Edges.parent) NOT IN (" +
                    "SELECT  Rchain, child, parent " +
                    "FROM Path_Required_Edges" +
                ") AND Path_Complement_Edges.parent NOT IN (" +
                    "SELECT rnid FROM RNodes" +
                ");"
            );

            st.close();

            rnode_ids.clear(); // Prepare for next loop.

            logger.fine(" Import is done for length = " + len + "."); // @zqian Test
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

        // TODO: Revisit how the orig_rnids are retrieved.
        ResultSet rs = st1.executeQuery(
            "select orig_rnid as RChain " +
            "from lattice_set " +
            "join lattice_mapping " +
            "on lattice_set.name = lattice_mapping.orig_rnid " +
            "where lattice_set.length = 1;"
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


    private static ArrayList<String> readRNodesFromLattice(int len) throws SQLException, IOException {
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery("SELECT name AS RChain FROM lattice_set WHERE lattice_set.length = " + len + ";");
        ArrayList<String> rnode_ids = new ArrayList<String>();

        while(rs.next()) {
            // Get pvid for further use.
            String rchain = rs.getString("RChain");
            logger.fine("\n RChain: " + rchain);
            rnode_ids.add(rchain);
        }

        st.close();

        return rnode_ids;
    }


    // Import to Entity_BayesNets.
    private static void bif1(String id) throws SQLException, IOException, ParsingException {
        // Import @zqian.
        logger.fine("Starting to Import the learned path into MySQL::**Entity_BayesNets**"); // @zqian Test
        Statement st = con2.createStatement();

        BIFImport.Import(databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Entity_BayesNets", con2);
        logger.fine("*** imported Entity_BayesNets " + id + " into database");
        logger.fine(" \n !!!!!!!!!Import is done for **Entity_BayesNets** \n"); // @zqian Test
        st.close();
    }


    // Import to Path_BayesNets // zqian@Oct 2nd 2013.
    private static void bif2(String id) throws SQLException, IOException, ParsingException {
        logger.fine(" Starting to Import the learned path into MySQL::**Path_BayesNets**"); // @zqian

        Statement st = con2.createStatement();

        BIFImport.Import(databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Path_BayesNets", con2);

        // zqian@Oct 2nd 2013.
        // Delete the edges which is already forbidden in a lower level before inserting into the database.
        // Nov 25
        st.execute("DELETE FROM Path_BayesNets WHERE Rchain = '" + id + "' AND (child, parent) IN (SELECT child, parent FROM Path_Forbidden_Edges WHERE Rchain = '" + id + "');"); // Oct 2nd
        logger.fine("*** imported Path_BayesNets " + id + "into database");

        logger.fine(" Import is done for **Path_BayesNets**"); // @zqian Test
        st.close();
    }


    private static void exportResults(int maxNumberOfMembers) throws SQLException, IOException {
        Statement st = con2.createStatement();

        ResultSet rs = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + maxNumberOfMembers + ";");
        while (rs.next()) {
            String setName = rs.getString("name");
            BIFExport.Export(databaseName + "/" + File.separator + "res" + File.separator + setName.replace("`","") + ".xml", "Rchain", "Path_BayesNets", setName, con2);
        }

        st.close();
    }


    private static void disconnectDB() throws SQLException {
        con2.close();
        con3.close();
    }
}
