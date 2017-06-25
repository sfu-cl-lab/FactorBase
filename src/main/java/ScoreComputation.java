/*
 * Compute_FID_Scores.java
 * 
 * Author: Kurt Routley
 * Date created: Wednesday, February 12, 2014
 */

import com.mysql.jdbc.Connection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class ScoreComputation {
    private static String pathBayesNet;
    private static String rchain;
    private static String dbaddress;
    private static String dbschema;
    private static String dbusername;
    private static String dbpassword;
    private static String dbcounts;
    private static String dbcondprob;
    private static boolean linkAnalysis;
    private static Connection con;

    private static boolean debug = false;

    public static int Compute_FID_Scores() {
        /*
		 * Load configuration
		 */
        pathBayesNet = "Path_BayesNets";

        if (loadConfig() != 0) {
            System.out.println("Failed to load configuration.");
            return -1;
        }
		
		/*
		 * Connect to database
		 */
        if (connectDB() != 0) {
            System.out.println("Failed to connect to database.");
            return -2;
        }
		
		/*
		 * Get rchains in list
		 */
        ArrayList <String> rchains = getRchains();

        if (null == rchains) {
            System.out.println("Failed to get rchains.");
            return -3;
        }
		
		/*
		 * Disconnect database
		 */
        if (disconnectDB() != 0) {
            System.out.println("Failed to disconnect database.");
            return -4;
        }
		
		/*
		 * Iterate over rchains
		 */
        int len = rchains.size();

        for (int i = 0; i < len; i++) {
            String currentRchain = rchains.get(i);
            if (Compute_FID_Scores(pathBayesNet, currentRchain) != 0) {
                System.out.println("Failed to compute FID Scores for rchain " + currentRchain);
                return -5;
            }
        }

        return 0;
    }

    public static int Compute_FID_Scores(String argPathBayesNet,
                                         String argRchain) {
        pathBayesNet = argPathBayesNet.replace("`", "");
        rchain = argRchain.replace("`", "");
		/*
		 * Load configuration
		 */
        if (loadConfig() != 0) {
            System.out.println("Failed to load configuration.");
            return -1;
        }
		
		/*
		 * Compute scores
		 */
        if (computeScores() != 0) {
            System.out.println("Failed to compute scores.");
            return -2;
        }

        return 0;
    }

    public static int Compute_FID_Scores(String argPathBayesNet,
                                         String argRchain,
                                         String argDbaddress,
                                         String argDbschema,
                                         String argDbusername,
                                         String argDbpassword,
                                         String argDbcounts,
                                         String argDbcondprob) {
		/*
		 * Copy args
		 */
        pathBayesNet = argPathBayesNet.replace("`", "");
        rchain = argRchain.replace("`", "");
        dbaddress = argDbaddress;
        dbschema = argDbschema;
        dbusername = argDbusername;
        dbpassword = argDbpassword;
        dbcounts = argDbcounts;
        dbcondprob = argDbcondprob;
		
		/*
		 * Compute scores
		 */
        if (computeScores() != 0) {
            System.out.println("Failed to compute scores.");
            return -1;
        }

        return 0;
    }

    private static ArrayList <String> getRchains() {
        ArrayList <String> rchains = new ArrayList <String>();

        try {
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT DISTINCT Rchain FROM " +
                    pathBayesNet + " WHERE LENGTH( " +
                    "Rchain ) = ( SELECT MAX( LENGTH( " +
                    "Rchain ) ) FROM " + pathBayesNet +
                    " );");

            while (rs.next()) {
                String newRchain = rs.getString(1);

                rchains.add(newRchain);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("SQL failure.");
            e.printStackTrace();
            return null;
        }

        return rchains;
    }

    private static int computeScores() {
		/*
		 * Connect to database
		 */
        if (connectDB() != 0) {
            System.out.println("Failed to connect to " + dbschema);
            return -1;
        }
		
		/*
		 * Get FIDs
		 */
        ArrayList <String> fidList = getFIDs();
        if (null == fidList) {
            System.out.println("Failed to get FIDs.");
            return -2;
        }
		
		/*
		 * Compute scores for each FID
		 */
        int len = fidList.size();
        for (int i = 0; i < len; i++) {
			/*
			 * Calculate Log Likelihood
			 */
            String fid = fidList.get(i);
            if (calculateLogLikelihood(fid) != 0) {
                System.out.println("Failed to calculate log likelihood for " + fid);
                return -3;
            }
        }

        if (createScoresTable() != 0) {
            System.out.println("Failed to create table for scores.");
            return -4;
        }

        if (calculateScores(fidList) != 0) {
            System.out.println("Failed to calculate scores.");
            return -5;
        }

        if (setParameters() != 0) {
            System.out.println("Failed to set parameters.");
            return -6;
        }

        if (updateSampleSize(fidList) != 0) {
            System.out.println("Failed to update Sample Size.");
            return -7;
        }

        if (setInformationCriterions() != 0) {
            System.out.println("Failed to set Scores.");
            return -8;
        }
		
		/*
		 * Disconnect
		 */
        if (disconnectDB() != 0) {
            System.out.println("Failed to close database connection.");
            return -9;
        }

        return 0;
    }

    private static int loadConfig() {
        Properties configFile = new java.util.Properties();
        FileReader fr = null;

        try {
            //fr = new FileReader( "cfg/scorecomputation.cfg" );
            fr = new FileReader("cfg/subsetctcomputation.cfg");  // May 22, 2014 zqian, computing the score for link analysis off.
        } catch (FileNotFoundException e) {
            System.out.println("Failed to find configuration file!");
            return -1;
        }

        BufferedReader br = new BufferedReader(fr);

        try {
            configFile.load(br);

            dbaddress = configFile.getProperty("dbaddress");
            dbusername = configFile.getProperty("dbusername");
            dbpassword = configFile.getProperty("dbpassword");
            dbschema = configFile.getProperty("dbschema");
            dbcounts = configFile.getProperty("dbcounts");
            //dbcounts +="_linkon";
            dbcondprob = configFile.getProperty("dbcondprob");
            //dbcondprob +="_linkon";
            String linkAnalysisStr = configFile.getProperty("LinkAnalysis");
            if (linkAnalysisStr.equals("1")) {
                linkAnalysis = true;
            } else {
                linkAnalysis = false;
            }


            br.close();
            fr.close();
        } catch (IOException e) {
            System.out.println("Failed to load configuration file.");
            return -2;
        }

        return 0;
    }

    private static int connectDB() {
        String CONN_STR = "jdbc:" + dbaddress + "/" + dbschema;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
            ex.printStackTrace();
            return -1;
        }

        try {
            con = (Connection) DriverManager.getConnection(CONN_STR,
                    dbusername,
                    dbpassword);
        } catch (SQLException e) {
            System.out.println("Failed to connect to database.");
            e.printStackTrace();
            return -2;
        }

        return 0;
    }

    private static int disconnectDB() {
        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Failed to close database connection.");
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    private static ArrayList <String> getFIDs() {
        ArrayList <String> fidList = new ArrayList <String>();

        try {
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT DISTINCT child FROM " +
                    pathBayesNet + " WHERE Rchain = '`" +
                    rchain + "`';");

            while (rs.next()) {
                String fid = rs.getString(1).replace("`", "");
                fidList.add(fid);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to get FIDs.");
            e.printStackTrace();
            return null;
        }

        return fidList;
    }

    private static int calculateLogLikelihood(String fid) {
		/*
		 * Get columns that aren't MULT
		 */
        try {
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SHOW COLUMNS FROM " + dbcounts +
                    ".`" + fid + "_" + rchain +
                    "_local_CT` WHERE Field <> 'MULT';");

            ArrayList <String> columns = new ArrayList <String>();

            while (rs.next()) {
                String columnName = rs.getString(1).replace("`", "");
                columns.add(columnName);
            }

            rs.close();
			
			/*
			 * Compute Log Likelihood
			 */
            String selectStatement = "";

            int len = columns.size();

            for (int i = 0; i < len; i++) {
                selectStatement += dbcounts + ".`" + fid + "_" + rchain + "_local_CT`.`" +
                        columns.get(i) + "`, ";
                continue;
            }

            st.execute("DROP TABLE IF EXISTS `" + fid + "_" + rchain + "_LL`;");

            String createStatement = "CREATE TABLE `" + fid + "_" + rchain + "_LL` AS " +
                    "SELECT " + selectStatement + dbcounts + ".`" + fid + "_" + rchain +   // mult * log(cp) as loglikelihood, zqian May 23, 2014
                    "_local_CT`.MULT, " + dbcondprob + ".`" + fid + "_local_CP`.CP, " +
                    "LOG(" + dbcondprob + ".`" + fid + "_local_CP`.CP) AS LogCP, " +
                    dbcounts + ".`" + fid + "_" + rchain + "_local_CT`.MULT * " +
                    "LOG(" + dbcondprob + ".`" + fid + "_local_CP`.CP) " +
                    "AS LogLikelihood FROM " + dbcounts + ".`" + fid + "_" + rchain +
                    "_local_CT` NATURAL JOIN " + dbcondprob + ".`" + fid + "_local_CP`;";
            if (debug) {
                System.out.println(createStatement);
            }

            st.execute(createStatement);

            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    private static int createScoresTable() {
        try {
            Statement st = con.createStatement();

            st.execute("DROP TABLE IF EXISTS `FID_" + rchain + "_Scores`");
            st.execute("CREATE TABLE `FID_" + rchain + "_Scores` (`FID` VARCHAR(256) NOT NULL, " +
                    "`LogLikelihood` FLOAT DEFAULT NULL, " +
                    "`Parameters` BIGINT(20) DEFAULT NULL, " +
                    "`SampleSize` BIGINT(20) DEFAULT NULL, " +
//		 	 	 	 	"StandardizedLogLikelihood FLOAT DEFAULT NULL, " +
                    "Normal_LogLikelihood  FLOAT DEFAULT NULL, " +

                    "`BIC` FLOAT DEFAULT NULL, " +
                    "`AIC` FLOAT DEFAULT NULL, " +
                    "`BICNormal` FLOAT DEFAULT NULL, " +
                    "`AICNormal` FLOAT DEFAULT NULL, " +
                    "`Pseudo_AIC` FLOAT DEFAULT NULL, " +  // May 23 2014, zqian
                    "`Pseudo_BIC` FLOAT DEFAULT NULL, " +
                    " PRIMARY KEY (`FID`))");

            st.execute("INSERT INTO `FID_" + rchain + "_Scores`(FID) SELECT " +
                    "DISTINCT child FROM " + pathBayesNet + " WHERE Rchain = '`" + rchain + "`';");

            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    private static int calculateScores(ArrayList <String> fidList) {
        try {
            Statement st = con.createStatement();

            int len = fidList.size();

            for (int i = 0; i < len; i++) {
                String fid = fidList.get(i);
				/*
				 * LogLikelihood
				 */
                System.out.println("UPDATE `FID_" + rchain + "_Scores`, `" + fid + "_" +
                        rchain + "_LL` SET `FID_" + rchain + "_Scores`.LogLikelihood = ( SELECT SUM( " +
                        "`" + fid + "_" + rchain + "_LL`.LogLikelihood ) FROM `" + fid + "_" + rchain +
                        "_LL` ) WHERE FID = '`" + fid + "`';");
                st.execute("UPDATE `FID_" + rchain + "_Scores`, `" + fid + "_" +
                        rchain + "_LL` SET `FID_" + rchain + "_Scores`.LogLikelihood = ( SELECT SUM( " +
                        "`" + fid + "_" + rchain + "_LL`.LogLikelihood ) FROM `" + fid + "_" + rchain +
                        "_LL` ) WHERE FID = '`" + fid + "`';");
            }
            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int setParameters() {
        try {
            Statement st = con.createStatement();

            st.execute("update FNodes,RNodes set FunctorName = (select distinct rnid from RNodes where FNodes.FunctorName " +
                    "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid) where FNodes.FunctorName " +
                    "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid;");

            ResultSet rs = st.executeQuery("SELECT DISTINCT rnid FROM RNodes;");

            ArrayList <String> rnids = new ArrayList <String>();

            while (rs.next()) {
                rnids.add(rs.getString(1));
            }

            rs.close();

            int len = rnids.size();

            for (int i = 0; i < len; i++) {
                String rnid = rnids.get(i);
                st.execute("DELETE FROM Attribute_Value WHERE COLUMN_NAME = '" + rnid + "';");
                st.execute("INSERT INTO Attribute_Value VALUES ( '" + rnid + "', 'True' );");
                st.execute("INSERT INTO Attribute_Value VALUES ( '" + rnid + "', 'False' );");
            }

            st.execute("drop table if exists NumAttributes;");
            st.execute("create table NumAttributes as SELECT count(VALUE) as NumAtts, COLUMN_NAME FROM Attribute_Value group by COLUMN_NAME;");

            st.execute("drop table if exists RNodes_inFamily;");
            st.execute("create table RNodes_inFamily as select FamilyRNodes.child as ChildNode, FamilyRNodes.parent as Rnode " +
                    " FROM Path_BayesNets as FamilyRNodes, FNodes as RNode_check " +
                    " where FamilyRNodes.Rchain = '`" + rchain + "`' and RNode_check.Fid = FamilyRNodes.parent and RNode_check.Type = 'RNode';");

            st.execute("drop table if exists 2Nodes_inFamily;");
            st.execute("create table 2Nodes_inFamily as select Family2Nodes.child as ChildNode, Family2Nodes.parent as 2node, NumAttributes.NumAtts " +
                    " FROM Path_BayesNets as Family2Nodes, FNodes as 2Node_check, NumAttributes where Family2Nodes.Rchain = '`" + rchain + "`' and 2Node_check.Fid = Family2Nodes.parent " +
                    " and 2Node_check.Type = '2Node' and 2Node_check.FunctorName = NumAttributes.COLUMN_NAME;");

            st.execute("drop table if exists 1Nodes_inFamily;");
            st.execute("create table 1Nodes_inFamily as	 select Family1Nodes.child as ChildNode, Family1Nodes.parent as 1node, NumAttributes.NumAtts " +
                    " FROM Path_BayesNets as Family1Nodes, FNodes as 1Node_check, NumAttributes where Family1Nodes.Rchain = '`" + rchain + "`' and 1Node_check.Fid = Family1Nodes.parent" +
                    " and 1Node_check.Type = '1Node' and 1Node_check.FunctorName = NumAttributes.COLUMN_NAME ;");

            st.execute("drop table if exists RNodes_2Nodes_Family;");
            st.execute("create table RNodes_2Nodes_Family as select RNodes_inFamily.ChildNode, RNodes_inFamily.Rnode, 2Nodes_inFamily.2Node, 2Nodes_inFamily.NumAtts " +
                    " from RNodes_inFamily, 2Nodes_inFamily where RNodes_inFamily.ChildNode = 2Nodes_inFamily.ChildNode and " +
                    " (RNodes_inFamily.Rnode, 2Nodes_inFamily.2Node) in (select * from RNodes_2Nodes);");

            st.execute("drop table if exists ChildPars;");
            st.execute("create table ChildPars as SELECT distinct (NumAtts-1) as NumPars, FNodes.Fid as ChildNode FROM " +
                    " FNodes join NumAttributes where FNodes.FunctorName=NumAttributes.COLUMN_NAME;");
            //find number of parameters for child//

            st.execute("drop table if exists 1NodePars;");
            st.execute("create table 1NodePars as select ChildNode, exp(sum(log(NumAtts))) as NumPars from 1Nodes_inFamily group by ChildNode union " +
                    " select distinct child as ChildNode, 1 as NumPars from Path_BayesNets where Path_BayesNets.Rchain = '`" + rchain + "`' and child not in (select ChildNode from 1Nodes_inFamily);");
            //find number of parameters for 1 Node parents//

            st.execute("drop table if exists RelationsParents;");
            st.execute("create table RelationsParents as select ChildNode, rnid, 2node, NumAtts from 2Nodes_inFamily, RNodes_2Nodes where 2node = 2nid union " +
                    " select ChildNode, RNode as rnid, RNode, 1 as NumVals from RNodes_inFamily;");
            //for every 2node parent, find the number of possible attributes union 1 possible value for the Rnode//
            st.execute("drop table if exists RelationsPars;");

            if (linkAnalysis) {
                //the inner query assumes that rnid is true: multiply together the 2node parameter numbers.
                //Add 1 in case that rnid is false. this computes the number of possible parent states for rnodes and 2nodes.//
                st.execute("create table RelationsPars as select ChildNode, exp(sum(log(NumPars))) as NumPars from" +
                        " (select ChildNode, rnid, " +
                        " exp(sum(log(NumAtts)))+1 as NumPars from RelationsParents group by ChildNode, rnid) as " +
                        "ParPerRelation group by ChildNode;");
            } else {
                //the inner query assumes that rnid is true: multiply together the 2node parameter numbers.
                //No need to add 1 in the outer query because rnid is true. Link analysis is off. this computes the number of possible parent states for rnodes and 2nodes.//
                st.execute("create table RelationsPars as select ChildNode, exp(sum(log(NumPars))) as NumPars from" +
                        " (select ChildNode, rnid, " +
                        " exp(sum(log(NumAtts))) as NumPars from RelationsParents group by ChildNode, rnid) as " +
                        "ParPerRelation group by ChildNode;");
            }


            st.execute("update `FID_" + rchain + "_Scores`, ChildPars, 1NodePars, RelationsPars set Parameters= " +
                    " (select ChildPars.NumPars * 1NodePars.NumPars * RelationsPars.NumPars from ChildPars, 1NodePars, RelationsPars " +
                    " where ChildPars.ChildNode = 1NodePars.ChildNode and 1NodePars.ChildNode = RelationsPars.ChildNode and `FID_" + rchain + "_Scores`.Fid = RelationsPars.ChildNode ) " +
                    " where RelationsPars.ChildNode= `FID_" + rchain + "_Scores`.Fid;");

            st.execute("update `FID_" + rchain + "_Scores`, ChildPars, 1NodePars set Parameters= " +
                    " (select ChildPars.NumPars * 1NodePars.NumPars from ChildPars, 1NodePars" +
                    " where ChildPars.ChildNode = 1NodePars.ChildNode and 1NodePars.ChildNode = `FID_" + rchain + "_Scores`.Fid ) where 1NodePars.ChildNode = `FID_" + rchain + "_Scores`.Fid and Parameters is NULL;");

//			st.execute( "UPDATE FNodes, RNodes SET FunctorName = ( SELECT " + 
//						"DISTINCT rnid FROM RNodes WHERE FNodes.FunctorName = " + 
//						"RNodes.TABLE_NAME AND FNodes.Fid = RNodes.rnid ) WHERE " + 
//						"FNodes.FunctorName = RNodes.TABLE_NAME AND FNodes.Fid " + 
//						"= RNodes.rnid;" );
//		 	 
//		 	 
//		 	st.execute( "DROP TABLE IF EXISTS NumAttributes;" );
//		 	st.execute( "CREATE TABLE NumAttributes AS SELECT COUNT( VALUE ) " + 
//		 			 	"AS NumAtts, COLUMN_NAME FROM Attribute_Value GROUP " + 
//		 			 	"BY COLUMN_NAME;" );
//		 	 
//		 	st.execute( "DROP TABLE IF EXISTS RNodes_inFamily;");
//		 	st.execute( "CREATE TABLE RNodes_inFamily AS SELECT " + 
//		 			 	"FamilyRNodes.child AS ChildNode, " + 
//		 			 	"FamilyRNodes.parent AS Rnode FROM " + pathBayesNet + " " + 
//		 			 	"AS FamilyRNodes, FNodes AS RNode_check WHERE " + 
//		 			 	"FamilyRNodes.Rchain = '`" + rchain + "`' AND " + 
//		 			 	"RNode_check.Fid = FamilyRNodes.parent AND " + 
//		 			 	"RNode_check.Type = 'RNode';" );
//		 	 
//		 	st.execute( "DROP TABLE IF EXISTS 2Nodes_inFamily;" );
//		 	st.execute( "CREATE TABLE 2Nodes_inFamily AS SELECT " + 
//		 				"Family2Nodes.child AS ChildNode, Family2Nodes.parent " + 
//		 				"AS 2node, NumAttributes.NumAtts FROM " + pathBayesNet + " " + 
//		 				"AS Family2Nodes, FNodes AS 2Node_check, NumAttributes " + 
//		 				"WHERE Family2Nodes.Rchain = '`" + rchain + "`' AND " + 
//		 				"2Node_check.Fid = Family2Nodes.parent AND " + 
//		 				"2Node_check.Type = '2Node' AND " + 
//		 				"2Node_check.FunctorName = NumAttributes.COLUMN_NAME;" );
//			
//			st.execute( "DROP TABLE IF EXISTS 1Nodes_inFamily;" );
//		 	st.execute( "CREATE TABLE 1Nodes_inFamily AS SELECT " + 
//		 				"Family1Nodes.child AS ChildNode, Family1Nodes.parent " + 
//		 				"AS 1node, NumAttributes.NumAtts FROM " + pathBayesNet + " " + 
//		 				"AS Family1Nodes, FNodes AS 1Node_check, NumAttributes " + 
//		 				"WHERE Family1Nodes.Rchain = '`" + rchain + "`' AND " + 
//		 				"1Node_check.Fid = Family1Nodes.parent AND " + 
//		 				"1Node_check.Type = '1Node' AND 1Node_check.FunctorName" + 
//		 				" = NumAttributes.COLUMN_NAME;" );
//			
//			st.execute( "DROP TABLE IF EXISTS ChildPars;" );
//		 	st.execute( "CREATE TABLE ChildPars AS SELECT DISTINCT ( " + 
//		 				"NumAtts - 1 ) AS NumPars, FNodes.Fid AS ChildNode " + 
//		 				"FROM FNodes JOIN NumAttributes WHERE " + 
//		 				"FNodes.FunctorName = NumAttributes.COLUMN_NAME;" );
//		 	 
//		 	st.execute( "DROP TABLE IF EXISTS 1NodePars;" );
//		 	st.execute( "CREATE TABLE 1NodePars AS SELECT ChildNode, EXP( " + 
//		 				"SUM( LOG( NumAtts ) ) ) AS NumPars FROM " + 
//		 				"1Nodes_inFamily GROUP BY ChildNode UNION SELECT " + 
//		 				"DISTINCT child AS ChildNode, 1 AS NumPars FROM " + 
//		 				"" + pathBayesNet + " WHERE " + pathBayesNet + ".Rchain = '`" + 
//		 				rchain + "`' AND child NOT IN ( SELECT ChildNode " + 
//		 				"FROM 1Nodes_inFamily );" );
//			
//			st.execute( "DROP TABLE IF EXISTS RelationsParents;" );
//		 	st.execute( "CREATE TABLE RelationsParents AS SELECT ChildNode, " + 
//		 				"rnid, 2node, NumAtts FROM 2Nodes_inFamily, " + 
//		 				"RNodes_2Nodes WHERE 2node = 2nid UNION SELECT " + 
//		 				"ChildNode, RNode AS rnid, RNode, 1 AS NumVals FROM " + 
//		 				"RNodes_inFamily;" );
//			
//			st.execute( "DROP TABLE IF EXISTS RelationsPars;" );
//		 	st.execute( "CREATE TABLE RelationsPars AS SELECT ChildNode, " + 
//		 				"EXP( SUM( LOG( NumPars ) ) ) AS NumPars FROM ( " + 
//		 				"SELECT ChildNode, rnid, EXP( SUM( LOG( NumAtts ) ) )" + 
//		 				" + 1 AS NumPars FROM RelationsParents GROUP BY " + 
//		 				"ChildNode, rnid ) AS ParPerRelation GROUP BY ChildNode;" );
//			
//			st.execute( "UPDATE `FID_" + rchain + "_Scores`, ChildPars, " + 
//						"1NodePars, RelationsPars SET Parameters = ( SELECT " + 
//						"ChildPars.NumPars * 1NodePars.NumPars * " + 
//						"RelationsPars.NumPars FROM ChildPars, 1NodePars, " + 
//						"RelationsPars WHERE ChildPars.ChildNode = " + 
//						"1NodePars.ChildNode AND 1NodePars.ChildNode = " + 
//						"RelationsPars.ChildNode AND `FID_" + rchain + 
//						"_Scores`.FID = RelationsPars.ChildNode ) WHERE " + 
//						"RelationsPars.ChildNode = `FID_" + rchain + 
//						"_Scores`.FID;" );
//			
//			st.execute( "UPDATE `FID_" + rchain + "_Scores`, ChildPars, " + 
//						"1NodePars SET Parameters = ( SELECT " + 
//						"ChildPars.NumPars * 1NodePars.NumPars FROM " + 
//						"ChildPars, 1NodePars WHERE ChildPars.ChildNode = " + 
//						"1NodePars.ChildNode AND 1NodePars.ChildNode = `FID_" + 
//						rchain + "_Scores`.FID ) WHERE 1NodePars.ChildNode = " + 
//						"`FID_" + rchain + "_Scores`.FID AND ISNULL(Parameters);" );

            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int updateSampleSize(ArrayList <String> fidList) {
        try {
            Statement st = con.createStatement();

            int len = fidList.size();

            for (int i = 0; i < len; i++) {
                String fid = fidList.get(i);

                st.execute("UPDATE `FID_" + rchain + "_Scores`, `" + fid + "_" +
                        rchain + "_LL` SET `FID_" + rchain + "_Scores`.SampleSize = " +
                        "( SELECT SUM( MULT ) FROM `" + fid + "_" +
                        rchain + "_LL` ) WHERE `FID_" + rchain + "_Scores`.FID = '`" + fid + "`';");
            }

            //May 23 2014, zqian
            //st.execute( "UPDATE `FID_" + rchain + "_Scores` SET StandardizedLogLikelihood = LogLikelihood / SampleSize WHERE SampleSize <> 0;" );
            st.execute("UPDATE `FID_" + rchain + "_Scores` SET Normal_LogLikelihood  = LogLikelihood / SampleSize WHERE SampleSize <> 0; ");


            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private static int setInformationCriterions() {
        try {
            Statement st = con.createStatement();  // formula need to be updated May 23, 2014, zqian

            st.execute("UPDATE `FID_" + rchain + "_Scores` SET AIC = LogLikelihood - Parameters;");
            st.execute("UPDATE `FID_" + rchain + "_Scores` SET BIC = 2 * LogLikelihood - LOG( SampleSize ) * Parameters;");
            st.execute("UPDATE `FID_" + rchain + "_Scores` SET AICNormal = AIC/SampleSize;");
            st.execute("UPDATE `FID_" + rchain + "_Scores` SET BICNormal = BIC/SampleSize;");
            //may 23 2014 zqian
            st.execute("UPDATE `FID_" + rchain + "_Scores` set Pseudo_AIC = Normal_LogLikelihood - Parameters ;");
            st.execute("UPDATE `FID_" + rchain + "_Scores` set Pseudo_BIC = 2*Normal_LogLikelihood - Log(SampleSize) * Parameters ;");
            st.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute queries.");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
