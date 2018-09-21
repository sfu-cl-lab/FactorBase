package ca.sfu.cs.factorbase.tables;

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

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.exporter.bifexporter.BIF_Generator;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIFExport;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIFImport;
import ca.sfu.cs.factorbase.exporter.bifexporter.bif.BIF_IO;
import ca.sfu.cs.factorbase.jbn.BayesNet_Learning_main;
import ca.sfu.cs.factorbase.util.BZScriptRunner;

import com.mysql.jdbc.Connection;

import nu.xom.ParsingException;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class BayesBaseH {

    static Connection con1, con2, con3;

    // To be read from config.
    static String databaseName, databaseName2, databaseName3;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;

    static String opt3, cont;
    static boolean linkAnalysis;
    static boolean Flag_UseLocal_CT; //zqian June 18, 2014

    static int maxNumberOfMembers = 0;

    static ArrayList<String> rnode_ids;
    static ArrayList<String> rnode_ids_1;
    static ArrayList<String> pvar_ids;

    /**
     * iff Running Time == 1, then generate the csv files.
     * else, just reuse the old csv files. May 1st @zqian.
     */
    static int FirstRunning = 1;


    public static void main(String[] args) throws Exception {
        runBBH();
    }


    public static void runBBH() throws Exception {
        initProgram(FirstRunning);
        connectDB();

        // Build tables for structure learning.
        BZScriptRunner bzsr = new BZScriptRunner(databaseName, con2);

        // Set up the bayes net models O.S. Sep 12, 2017.
        bzsr.runScript(Config.SCRIPTS_DIRECTORY + "model_manager.sql");

        // Get maxNumberOfMembers (max length of rchain).
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT MAX(length) FROM lattice_set;");
        rst.absolute(1);
        maxNumberOfMembers = rst.getInt(1);

        // Get the longest rchain.
        String rchain = null;
        ResultSet rst1 = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + maxNumberOfMembers + ";");
        rst1.absolute(1);
        rchain = rst1.getString(1);
        System.out.println(" ##### lattice is ready for use* "); // @zqian
        
        if(linkAnalysis) {
        	st.execute(
        			"INSERT IGNORE "
        			+ "INTO Path_Required_Edges "
        			+ "SELECT  DISTINCT  * "
        			+ "FROM SchemaEdges;" );
        }
        // Structure learning.
        StructureLearning(con2);

        /**
         * OS: Nov 17, 2016. It can happen that Tetrad learns a forbidden edge. Argh. To catch this, we delete forbidden edges from any insertion. But then
         * it can happen that a node has no edge at all, not even with an empty parent. In that case the Bif generator gets messed up. So we catch such
         * orphaned nodes in the next statement.
         */
        System.out.println("Inserting the Missing Fid as Child into Path_Bayes_Nets \n");
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
            System.out.println("\n Structure Learning is DONE.  ready for parameter learning."); //@zqian

            // Export the final result to xml.  We assume that there is a single largest relationship chain and write the Bayes net for that relationship chain to xml.
            // Only export the structure, prepare for the pruning phase, Oct 23, 2013.
            exportResults();

            //      @zqian  for TestScoreComputation, use local ct to compute local CP.
            if (Flag_UseLocal_CT) {
                System.out.println("\n For BN_ScoreComputation.  use local_CT to compute the local_CP.");
            } else {
                // For FunctorWrapper, do NOT have to use the local_CT, or HAVE TO change the weight learning part. June 18 2014.
                CPGenerator.Generator(databaseName, con2); // May 22, 2014 zqian, computing the score for link analysis off.
                CP mycp = new CP(databaseName2, databaseName3);
                mycp.cp();
                System.out.println("\n Parameter learning is done.");
                //  For FunctorWrapper
            }

            // Score Bayes net: compute KL divergence, and log-likelihood (average probability of node value given its Markov blanket, compared to database frequencies)
            // May 7th, zqian, For RDN do not need to do the smoothing
            // COMPUTE KLD
            long l = System.currentTimeMillis(); // @zqian: measure structure learning time

            if (opt3.equals("1")) {
                System.out.println("\n KLD_generator.KLDGenerator.");
                KLD_generator.KLDGenerator(databaseName, con2);
            } else {
                System.out.println("\n KLD_generator.smoothed_CP.");
                KLD_generator.smoothed_CP(rchain, con2);
            }

            // Generating the bif file, in order to feed into UBC tool (bayes.jar). Based on the largest relationship chain.
            // Need CP tables.
            BIF_Generator.generate_bif(databaseName, "Bif_" + databaseName + ".xml", con2);

            long l2 = System.currentTimeMillis(); // @zqian: measure structure learning time.
            System.out.print("smoothed_CP Time(ms): " + (l2 - l) + " ms.\n");
        } else {
            System.out.println("\n Structure Learning is DONE. \n NO parameter learning for Continuous data."); // @zqian
        }

        // Disconnect from db.
        disconnectDB();
    }


    public static void StructureLearning(Connection conn) throws Exception {
        long l = System.currentTimeMillis(); // @zqian: measure structure learning time.

        // Handle pvars.
        handlePVars(); // import @zqian

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
        handleRNodes_zqian(); // import
        // Population lattice.
        PropagateContextEdges();

        /**
         * OS May 23. 2014 This looks like a much too complicated way to find the context edges. How about this:
         * 1. Use a view Contextedges to find the context edges for each Rchain.
         * 2. Union these edges over the rchain, insert them into the largest rchain.
         * 3. Make sure you insert "<rnid> null" into PathBN as well.
         * zqian, when link is off, check the local_ct for rnode?
         */

        long l2 = System.currentTimeMillis(); // @zqian: Measure structure learning time.
        System.out.print("\n*****************\nStructure Learning Time(ms): " + (l2 - l) + " ms.\n");
    }


    public static void initProgram(int FirstRunning) throws IOException, SQLException {
        // Read config file.
        setVarsFromConfig();

        // Init ids.
        pvar_ids = new ArrayList<String>();
        rnode_ids = new ArrayList<String>();
        rnode_ids_1 = new ArrayList<String>();

        if (FirstRunning==1) {
            new File(databaseName + "/" + File.separator).mkdirs();
            new File(databaseName + "/" + File.separator + "kno" + File.separator).mkdirs();
            new File(databaseName + "/" + File.separator + "res" + File.separator).mkdirs();
            new File(databaseName + "/" + File.separator + "xml" + File.separator).mkdirs();
        }

    }


    public static void setVarsFromConfig() {
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


    public static void connectDB() throws SQLException {
        String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }

        con1 = (Connection) DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);

        String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }

        con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);

        String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName3;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }

        con3 = (Connection) DriverManager.getConnection(CONN_STR3, dbUsername, dbPassword);
    }

    /** Jun 14
     * if the tuples greater than 1, then employ tetradlearner.
     * else just insert the 1nid as child into entity_bayesnet.
     */
    public static void handlePVars() throws Exception {
        // read pvar -> create csv files
        readPvarFromBN();

        String NoTuples = "";
        for(String id : pvar_ids) {
            System.out.println("\nStarting Learning the BN Structure of pvar_ids: " + id + "\n");
            Statement st = con3.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM `" + id.replace("`","") + "_counts`;"); // Optimize this query, too slow, Nov 13, zqian.
            while(rs.next()) {
                NoTuples = rs.getString(1);
                System.out.println("NoTuples : " + NoTuples);
            }

            if (Integer.parseInt(NoTuples) > 1) {
                BayesNet_Learning_main.tetradLearner(
                    databaseName + "/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
                    databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
                );

                bif1(id);
            } else {
                Statement st2 = con2.createStatement();
                // Insert the BN nodes into Entity_BayesNet.
                System.out.println("SELECT 1nid FROM 1Nodes, EntityTables WHERE 1Nodes.pvid = CONCAT(EntityTables.Table_name,'0') AND 1Nodes.pvid = '" + id + "';");
                ResultSet rs2 = st2.executeQuery("SELECT 1nid FROM 1Nodes, EntityTables WHERE 1Nodes.pvid = CONCAT(EntityTables.Table_name,'0') AND 1Nodes.pvid = '" + id + "';");
                String child = "";

                while(rs2.next()) {
                    Statement st3 = con2.createStatement();
                    child = rs2.getString("1nid");
                    System.out.println("INSERT IGNORE INTO Entity_BayesNets VALUES ('" + id + "', '" + child + "', '');");
                    st3.execute("INSERT IGNORE INTO Entity_BayesNets VALUES ('" + id + "', '" + child + "', '');");
                    st3.close();
                }

                rs2.close();
                st2.close();
            }

            System.out.println("\nEnd for " + id + "\n");
        }

        pvar_ids.clear();
    }


    public static void handleRNodes_zqian() throws Exception {
        for(int len = 1; len <= maxNumberOfMembers; len++) {
            readRNodesFromLattice(len); // Create csv files for all rnodes.

            // Required edges.
            for(String id : rnode_ids) { // rchain
                System.out.println("\n !!!!Starting to Export the Required Edges to " + id.replace("`","") +  "_req.xml \n");
                BIFExport.Export(databaseName + "/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
            }

            // Nov25
            // Forbidden edges.
            for(String id : rnode_ids) {
                System.out.println("\n !!!!Starting to Export the Forbidden Edges to " + id.replace("`","") + "_for.xml \n");
                BIFExport.Export(databaseName + "/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
            }

            String NoTuples = "";
            for(String id : rnode_ids) {
                System.out.println("\nStarting Learning the BN Structure of rnode_ids: " + id + "\n");
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
                    System.out.println("NoTuples : " + NoTuples);
                }

                if(Integer.parseInt(NoTuples) > 1) {
                    BayesNet_Learning_main.tetradLearner(
                        databaseName + "/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
                        databaseName + "/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
                        databaseName + "/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
                        databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
                    );

                    System.out.println("The BN Structure Learning for rnode_id::" + id + "is done."); //@zqian Test
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

            System.out.println(" Import is done for length = " + len + "."); // @zqian Test
        }
    }


    public static void PropagateContextEdges() throws Exception {
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
            "on lattice_set.name = lattice_mapping.short_rnid " +
            "where lattice_set.length = 1;"
        );

        while(rs.next()) {
            // Get rvid for further use.
            String rchain = rs.getString("RChain");
            rnode_ids_1.add(rchain);
        }
        st1.close();
        Statement st_temp = con2.createStatement();
        for(String id : rnode_ids_1) { // Feb 7th 2014, zqian; updated May 26, 2014 zqian.
            st_temp.execute("INSERT IGNORE INTO Path_BayesNets (SELECT '" + largest_rchain + "' AS Rchain, '" + id + "' AS child, '' AS parent);");
        }

        st_temp.close();
        // End for adding rnode as child, May 26th, 2014 zqian.
        st.close();
    }


    public static void readPvarFromBN() throws SQLException, IOException {
        Statement st = con2.createStatement();

        // From main db.
        ResultSet rs = st.executeQuery("SELECT * FROM PVariables WHERE index_number = 0;"); //O.S. March 21 ignore variables that aren't main.
        while(rs.next()) {
            // Get pvid for further use.
            String pvid = rs.getString("pvid");
            System.out.println("pvid : " + pvid);

            // Add to ids for further use.
            pvar_ids.add(pvid);
        }

        // Close statements.
        rs.close();
        st.close();
    }


    public static void readRNodesFromLattice(int len) throws SQLException, IOException {
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery("SELECT name AS RChain FROM lattice_set WHERE lattice_set.length = " + len + ";");
        while(rs.next()) {
            // Get pvid for further use.
            String rchain = rs.getString("RChain");
            System.out.println("\n RChain: " + rchain);
            rnode_ids.add(rchain);
        }

        st.close();
    }


    // Import to Entity_BayesNets.
    public static void bif1(String id) throws SQLException, IOException, ParsingException {
        // Import @zqian.
        System.out.println("Starting to Import the learned path into MySQL::**Entity_BayesNets**"); // @zqian Test
        Statement st = con2.createStatement();
        int i = 0;
        BIFImport.Import(databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Entity_BayesNets", con2);
        System.out.println("*** imported Entity_BayesNets " + pvar_ids.get(i++) + " into database");
        System.out.println(" \n !!!!!!!!!Import is done for **Entity_BayesNets** \n"); // @zqian Test
        st.close();
    }


    // Import to Path_BayesNets // zqian@Oct 2nd 2013.
    public static void bif2(String id) throws SQLException, IOException, ParsingException {
        System.out.println(" Starting to Import the learned path into MySQL::**Path_BayesNets**"); // @zqian

        Statement st = con2.createStatement();
        int j = 0;

        BIFImport.Import(databaseName + "/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Path_BayesNets", con2);

        // zqian@Oct 2nd 2013.
        // Delete the edges which is already forbidden in a lower level before inserting into the database.
        // Nov 25
        st.execute("DELETE FROM Path_BayesNets WHERE Rchain = '" + id + "' AND (child, parent) IN (SELECT child, parent FROM Path_Forbidden_Edges WHERE Rchain = '" + id + "');"); // Oct 2nd
        System.out.println("*** imported Path_BayesNets " + rnode_ids.get(j++) + "into database");

        System.out.println(" Import is done for **Path_BayesNets**"); // @zqian Test
        st.close();
    }


    public static void exportResults() throws SQLException, IOException {
        Statement st = con2.createStatement();

        ResultSet rs = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + maxNumberOfMembers + ";");
        while (rs.next()) {
            String setName = rs.getString("name");
            BIFExport.Export(databaseName + "/" + File.separator + "res" + File.separator + setName.replace("`","") + ".xml", "Rchain", "Path_BayesNets", setName, con2);
        }

        st.close();
    }


    public static void disconnectDB() throws SQLException {
        con1.close();
        con2.close();
        con3.close();
    }
}
