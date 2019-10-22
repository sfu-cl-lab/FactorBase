package ca.sfu.cs.factorbase.learning;

/** update the prior: zqian, June 23 2014
 * for FID who has parents the prior should be [sum(mult) group by FID ]/total_mult,
 * For FID do not have parents, prior == cp.
 *
 * NOTE the difference with local_CP:
 * 1. local_cp and local_ct are stored in _db database,
 *  v.s. _cp tables are stored in _BN database and _ct tables are stored in _CT database
 * 2. local_cp tables are computed based on local_ct
 * v.s _cp tables are computed based on the biggest ct tables
 * (their values are SAME)
 */

/** Feb 7, 2014 @ zqian, bug: miss some nodes which ONLY act as Parent
 * fixed: for each lattice point,  add one extra step after the structure learning is done
 *Feb 6 Yan
 * Change ChildValue to FID
 * Fix bug: CP sum up to 1 in IMDB
 */

/** Try to using the formula in Oliver's SDM11 paper
 * AIC =  mult * loglikelihood  - #parameters
 * BIC = 2 * local_mult * loglikelihood - parameters * log(BigSampleSize)
 *
 * Zqian@Dec 10th, 2013
 */

/**
 * add only the small mults where log(CP) < 0, i.e. CP < 1
 * this would help a little bit,
 * However, we need to think about the following situtation:
 * suppose for all of the configuration of one node, the CP = 1.0, then how to decide the sample size???
 *
 * So basically, we aborted this idea. Zqian@Dec 10th, 2013
 */

/** editing the float type [float(20,2)],  Zqian@Dec 6th, 2013
 *
 */

/** zqian @ Dec 5th 2013
 * computing BIC and AIC based on local counts
 *
 * ___________________________________ computation based on *Biggest CT*
 * AIC =  mult * loglikelihood  - #parameters
 * BIC = 2 * mult * loglikelihood - #parameters * log (SampleSize)
 *
 * normalized version
 * AICNormal = (mult * loglikelihood) / [sum(mult) in bigCT table] - #parameters / [sum(mult) in bigCT table]
 * BICNormal = (2 * mult * loglikelihood ) / [sum(mult) in bigCT table]
 *                                  -   [#parameters * log (SampleSize)] / [sum(mult) in bigCT table]
 *
 * however this is not quite correct, so we need to compute it *locally*
 * __________________________________________________________
 * local_AIC =  local_mult * loglikelihood  - #parameters
 * local_BIC = 2 * local_mult * loglikelihood - #parameters * log (local_SampleSize)
 *
 * normalized version
 * local_AICNormal = (local_mult * loglikelihood) / [sum(mult) in local CT table] - #parameters / [sum(mult) in local CT table]
 * local_BICNormal = (2 * local_mult * loglikelihood ) / [sum(mult) in local CT table]
 *                                  -   [#parameters * log (local_SampleSize)] / / [sum(mult) in local CT table]
 */

/*Written by Qing & Sara*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;

/**
 * Finds conditional probabilities for the Bayes net parameters. Also computes log-likelihood and other scores for each node.
 */
public class CP {
    static Connection con1;
    // to be read from config
    static String databaseName;
    static String real_database;
    static String databaseName2;
    static String dbUsername="";
    static String dbPassword="";
    static String dbaddress="";
    static String rchain;
    static String shortRchain;
    
    private static Logger logger = Logger.getLogger(CP.class.getName());

    public CP(String databaseName, String databaseName2) {
        CP.databaseName = databaseName;
        CP.databaseName2 = databaseName2;
    }

    public void cp() throws SQLException {
        long l = System.currentTimeMillis(); // @zqian : measure parameter learning time
        setVarsFromConfig();
        connectDB();
        generateCPTable(rchain, con1);

        long l2 = System.currentTimeMillis(); // @zqian : measure parameter learning time
        logger.fine("Parameter Learning Time(ms): " + (l2 - l) + " ms.\n");
    }

    /**
     * Builds CP table for a given rchain. Right now, it's the longest one for the final result.
     */
    public static void generateCPTable(String rchain, Connection con1) throws SQLException {
        prepare(rchain, con1);
        nopar(rchain, con1);
        haspar(rchain,con1);
        // Feb 7, 2014 @ zqian, bug: miss some nodes which ONLY act as Parent
        // fixed by add such node as a child without parent in Path_BayesNet

        Statement st2 = con1.createStatement();

        st2.execute("update Scores set Normal_LogLikelihood = LogLikelihood / SampleSize;");
        st2.execute("update Scores set AIC = LogLikelihood - Parameters;");
        st2.execute("update Scores set BIC = 2 * LogLikelihood - Log(SampleSize) * Parameters;");

        st2.execute("update Scores set Pseudo_AIC = Normal_LogLikelihood - Parameters;");
        st2.execute("update Scores set Pseudo_BIC = 2 * Normal_LogLikelihood - Log(SampleSize) * Parameters;");
//        st2.execute("update Scores set Pseudo_BIC = 2 * Normal_LogLikelihood - Log(Big_SampleSize) * Parameters;"); ??
//        st2.execute("update Scores set AICNormal = AIC / SampleSize;");
//        st2.execute("update Scores set BICNormal = BIC / SampleSize;");
        st2.close();
    }

    /**
     * Prepares a table to record scores for each node. These are computed later. For now we just compute the number of
     * parameters for each node given the BN structure.  The number of parameters is basically:
     *     (number of parent states) * (number of child values - 1).
     * The main complication is that if a relationship is false, then its descriptive attributes must be N/A.  So the
     * number of effective parameters is less than just the standard number of CP-table rows.
     */
    public static void prepare(String rchain, Connection con1) throws SQLException {
        lcoal_mult_update(rchain,con1); //zqian @ Dec 4th

        Statement st = con1.createStatement();
        st.execute("drop table if exists Scores;");
        st.execute(
            "create table Scores (`Fid` varchar(255) NOT NULL, " + // zqian Oct 17
            "`LogLikelihood` float(20,2) default NULL, " + // editing the float type
            "`Normal_LogLikelihood` float(20,2) default NULL, " + // editing the float type
            "`Parameters` bigint(20) default NULL, " +
            "`SampleSize` bigint(20) default NULL, " +
            "`BIC` float(20,2) default NULL, " +
            "`AIC` float(20,2) default NULL, " +
            "`Pseudo_BIC` float(20,2) default NULL, " +
            "`Pseudo_AIC` float(20,2) default NULL, " +
            "`Big_SampleSize` bigint(20) default NULL, " +
//            "`BICNormal` float(20,2) default NULL, " +
//            "`AICNormal` float(20,2) default NULL, " +
            " PRIMARY KEY (`Fid`))"
        );
        // Feb 7, 2014 @ zqian, bug: miss some nodes which ONLY act as Parent
        st.execute("insert into Scores(Fid) SELECT distinct child from Path_BayesNets where Rchain = '" + rchain +"';");
        st.execute(
            "update FNodes,RNodes set FunctorName = (select distinct rnid from RNodes where FNodes.FunctorName " +
            "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid) where FNodes.FunctorName " +
            "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid;"
        );

        st.execute("drop table if exists NumAttributes;");
        st.execute(
            "CREATE TABLE NumAttributes AS " +
                "SELECT " +
                    "COUNT(VALUE) AS NumAtts, " +
                    "COLUMN_NAME " +
                "FROM " +
                    real_database + "_setup.Attribute_Value " +
                "GROUP BY " +
                    "COLUMN_NAME;"
        );

        st.execute("drop table if exists RNodes_inFamily;");
        st.execute(
            "create table RNodes_inFamily as select FamilyRNodes.child as ChildNode, FamilyRNodes.parent as Rnode " +
            " FROM Path_BayesNets as FamilyRNodes, FNodes as RNode_check " +
            " where FamilyRNodes.Rchain = '" + rchain + "' and RNode_check.Fid = FamilyRNodes.parent and RNode_check.Type = 'RNode';"
        );

        st.execute("drop table if exists 2Nodes_inFamily;");
        st.execute(
            "create table 2Nodes_inFamily as select Family2Nodes.child as ChildNode, Family2Nodes.parent as 2node, NumAttributes.NumAtts " +
            " FROM Path_BayesNets as Family2Nodes, FNodes as 2Node_check, NumAttributes where Family2Nodes.Rchain = '" + rchain + "' and 2Node_check.Fid = Family2Nodes.parent " +
            " and 2Node_check.Type = '2Node' and 2Node_check.FunctorName = NumAttributes.COLUMN_NAME;"
        );

        st.execute("drop table if exists 1Nodes_inFamily;");
        st.execute(
            "create table 1Nodes_inFamily as select Family1Nodes.child as ChildNode, Family1Nodes.parent as 1node, NumAttributes.NumAtts " +
            " FROM Path_BayesNets as Family1Nodes, FNodes as 1Node_check, NumAttributes where Family1Nodes.Rchain = '" + rchain + "' and 1Node_check.Fid = Family1Nodes.parent" +
            " and 1Node_check.Type = '1Node' and 1Node_check.FunctorName = NumAttributes.COLUMN_NAME;"
        );

        st.execute("drop table if exists RNodes_2Nodes_Family;");
        st.execute(
            "create table RNodes_2Nodes_Family as select RNodes_inFamily.ChildNode, RNodes_inFamily.Rnode, 2Nodes_inFamily.2Node, 2Nodes_inFamily.NumAtts " +
            " from RNodes_inFamily, 2Nodes_inFamily where RNodes_inFamily.ChildNode = 2Nodes_inFamily.ChildNode and " +
 	 	 	 	 " (RNodes_inFamily.Rnode, 2Nodes_inFamily.2Node) in (select rnid, 2nid from RNodes_2Nodes);");

        st.execute("drop table if exists ChildPars;");
        st.execute(
            "create table ChildPars as SELECT distinct (NumAtts-1) as NumPars, FNodes.Fid as ChildNode FROM " +
            " FNodes join NumAttributes where FNodes.FunctorName=NumAttributes.COLUMN_NAME;"
        );

        st.execute("drop table if exists 1NodePars;");
        st.execute(
            "create table 1NodePars as select ChildNode, exp(sum(log(NumAtts))) as NumPars from 1Nodes_inFamily group by ChildNode union " +
            " select distinct child as ChildNode, 1 as NumPars from Path_BayesNets where Path_BayesNets.Rchain = '" + rchain + "' and child not in (select ChildNode from 1Nodes_inFamily);"
        );

        st.execute("drop table if exists RelationsParents;");
        st.execute(
            "create table RelationsParents as select ChildNode, rnid, 2node, NumAtts from 2Nodes_inFamily, RNodes_2Nodes where 2node = 2nid union " +
            " select ChildNode, RNode as rnid, RNode, 1 as NumVals from RNodes_inFamily;"
        );

        st.execute("drop table if exists RelationsPars;");
        st.execute(
            "create table RelationsPars as select ChildNode, exp(sum(log(NumPars))) as NumPars from (select ChildNode, rnid, " +
            " exp(sum(log(NumAtts)))+1 as NumPars from RelationsParents group by ChildNode, rnid) as ParPerRelation group by ChildNode;"
        );
        // why are we doing this again? line 267, Nov 12 Zqian
/*
        st.execute(
            "update FNodes,RNodes set FunctorName = (select distinct rnid from RNodes where FNodes.FunctorName " +
            "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid) where FNodes.FunctorName " +
            "= RNodes.TABLE_NAME and FNodes.Fid = RNodes.rnid;"
        );
*/
        st.execute(
            "update Scores, ChildPars, 1NodePars, RelationsPars set Parameters= " +
            " (select ChildPars.NumPars * 1NodePars.NumPars * RelationsPars.NumPars from ChildPars, 1NodePars, RelationsPars " +
            " where ChildPars.ChildNode = 1NodePars.ChildNode and 1NodePars.ChildNode = RelationsPars.ChildNode and Scores.Fid = RelationsPars.ChildNode) " +
            " where RelationsPars.ChildNode = Scores.Fid;"
        );

        st.execute(
            "update Scores, ChildPars, 1NodePars set Parameters= " +
            " (select ChildPars.NumPars * 1NodePars.NumPars from ChildPars, 1NodePars" +
            " where ChildPars.ChildNode = 1NodePars.ChildNode and 1NodePars.ChildNode = Scores.Fid) where 1NodePars.ChildNode = Scores.Fid and Parameters is NULL;"
        );

        // why are we doing this again? line 272, Nov 12 Zqian
/*
        st.execute("drop table if exists NumAttributes;");
        st.execute("CREATE TABLE NumAttributes as SELECT COLUMN_NAME, count(VALUE) as NumAtts FROM Attribute_Value group by COLUMN_NAME;");
*/
        st.close();
    }

    /**
     * Deal with nodes without parents.
     */
    public static void nopar(String rchain, Connection con1) throws SQLException {
        java.sql.Statement st = con1.createStatement();
        ResultSet rst=st.executeQuery(
            "SELECT child FROM Path_BayesNets WHERE Rchain='" + rchain +
            "' AND parent = '' and child not in (SELECT distinct child FROM Path_BayesNets WHERE parent<>'' and Rchain= '" + rchain + "');"
        );
        logger.fine(
            "SELECT child FROM Path_BayesNets WHERE Rchain='" + rchain +
            "' AND parent = '' and child not in (SELECT distinct child FROM Path_BayesNets WHERE parent<>'' and Rchain= '" + rchain + "');"
        );
        ArrayList<String> noparent_tables = new ArrayList<String>();

        String bigTable = shortRchain + "_CT";
        logger.fine("bigTable Name: " + bigTable + "\n");
        while(rst.next()) {
            logger.fine("noparent node: " + rst.getString(1));
            noparent_tables.add(rst.getString(1));
        }
        // zqian Nov 13, computing sum(mult) from biggest CT table
        // and close the connections
        java.sql.Statement st2 = con1.createStatement();
        String sql2 =
            "SELECT SUM(MULT) " +
            "FROM " + databaseName2 + ".`" + bigTable + "`;"; // Only need to do this query once, Nov 12 zqian.
        logger.fine(sql2 + "\n");
        ResultSet deno = st2.executeQuery(sql2);
        deno.absolute(1);
        long mydeno = Long.parseLong(deno.getString(1)); // convert string to integer
        logger.fine("SUM(mult) in bigCTTable : "+mydeno + "\n");
        for(int i = 0; i < noparent_tables.size(); i++) {
            nopar_update(rchain, bigTable, noparent_tables.get(i), con1, mydeno);
        }

        st2.close();
        st.close();
    }

    /**
     * Similar simpler computation for nodes without parents.
     */
    public static void nopar_update(String rchain, String bigTable, String nodeName, Connection con1, long mydeno) throws SQLException {
        java.sql.Statement st = con1.createStatement();
        java.sql.Statement st2 = con1.createStatement();
        String tableName = nodeName + "_CP";
        st.execute("DROP TABLE IF EXISTS `" + tableName + "`;");
        logger.fine(tableName + "\n");
        // change the ChildValue to FID -- Jan 23 Yan
        st.execute(
            "CREATE TABLE `" + tableName + "` (" +
                "`" + nodeName + "` VARCHAR(200) NOT NULL, " +
                "CP FLOAT(7,6), " +
                "MULT BIGINT(20), " +
                "local_mult BIGINT(20)" +
            ");"
        );
        st.execute(
            "INSERT INTO `" + tableName + "` (`" + nodeName + "`) " +
            "SELECT DISTINCT `" + nodeName + "` " +
            "FROM " + databaseName2 + ".`" + bigTable + "`;"
        );

        ResultSet rst = st.executeQuery(
            "SELECT `" + nodeName + "` " +
            "FROM `" + tableName + "`;"
        );

        ArrayList<String> column_value = new ArrayList<String>();
        while(rst.next()) {
            logger.fine("column value: " + rst.getString(1) + "\n");
            column_value.add(rst.getString(1));
        }

        for(int i = 0; i < column_value.size(); i++) {
            String sql =
                "SELECT SUM(MULT) " +
                "FROM " + databaseName2 + ".`" + bigTable + "` " +
                "WHERE `" + nodeName + "` = '" + column_value.get(i) + "';";
            logger.fine(sql+"\n");
            ResultSet nume = st2.executeQuery(sql);
            // nume is the sum over all contingency table rows for a specific value.
            nume.absolute(1);
            long mynume = Long.parseLong(nume.getString(1));
            // converts string to integer.
            logger.fine(mynume + "\n");

            // change the ChildValue to FID -- Jan 23 Yan
            String sql3 =
                "UPDATE `" + tableName + "` " +
                "SET MULT = " + mynume + " " +
                "WHERE `" + nodeName + "` = '" + column_value.get(i) + "';";
            logger.fine(sql3 + "\n");
            st2.execute(sql3);
            st2.execute(
                "UPDATE `" + tableName + "` " +
                "SET CP = MULT / " + mydeno + " " +
                "WHERE `" + nodeName + "` = '" + column_value.get(i) + "';"
            );

            ResultSet rs = st.executeQuery("select Tuples from  Pvars_Not_In_Family where child = '" + nodeName + "' ;");
            long local = 1;
            while(rs.next()) {
                local = Long.parseLong (rs.getString("Tuples"));
                logger.fine("local is " + local);
                mynume = mynume / local;
//                logger.fine("set local_mult = mult, May 21, 2014 zqian ");
                // set local_mult = mult, May 21, 2014 zqian
            }
            if (!rs.first()) {
                logger.fine("local is 1, ******");
                mynume = mynume / local;
            }
            // updating the local_mult = mult / local , Dec 3rd
            String sql4 =
                "UPDATE `" + tableName + "` " +
                "SET local_mult = " + mynume + " " +
                "WHERE `" + nodeName + "` = '" + column_value.get(i) + "';";
            logger.fine(sql4 + "\n");
            st2.execute(sql4);
        }

        st2.execute(
            "ALTER TABLE `" + tableName + "` " +
            "ADD likelihood FLOAT(20,2);"
        );
        st2.execute(
            "UPDATE `" + tableName + "` " +
            "SET likelihood = LOG(CP) * local_mult;" // Dec 2nd, likelihood = log(cp) * mult.
        );
        // LOG10() Return the base-10 logarithm of the argument LOG2() Return the base-2 logarithm of the argument //
        // LOG() Return the natural logarithm of the first argument

        ResultSet logsum = st2.executeQuery(
            "SELECT SUM(likelihood) AS loglike " +
            "FROM `" + tableName + "`;");
        logsum.absolute(1);
        double mylogsum = logsum.getDouble(1);
        st2.execute(
            "UPDATE Scores " +
            "SET LogLikelihood = " + mylogsum + " " +
            "WHERE Scores.Fid = '" + nodeName + "';"
        );
        ResultSet samplesize = st2.executeQuery(
            "SELECT SUM(local_mult) " +
            "FROM `" + tableName + "`;"
        );
//        ResultSet samplesize = st2.executeQuery("select sum(local_mult) from " + table_name + " where cp < 1.0;");

        samplesize.absolute(1);
        long mysize = Long.parseLong(samplesize.getString(1));
        st2.execute(
            "UPDATE Scores " +
            "SET SampleSize = " + mysize + " " +
            "WHERE Scores.Fid = '" + nodeName + "';"
        );
        ResultSet big_samplesize = st2.executeQuery(
            "SELECT SUM(MULT) " +
            "FROM `" + tableName + "`;"
        );

        big_samplesize.absolute(1);
        long big_mysize = Long.parseLong(big_samplesize.getString(1));
        st2.execute("update Scores set Big_SampleSize = " + big_mysize + " where Scores.Fid = '" + nodeName + "';");

        // compute the prior June 23, 2014, zqian
        st.execute(
            "ALTER TABLE `" + tableName + "` " +
            "ADD prior FLOAT(7,6);"
        );
        st.execute(
            "UPDATE `" + tableName + "` " +
            "SET prior = CP;"
        );

        st2.close();
        st.close();
    }

    /**
     * Deal with child nodes that have parents.
     */
    public static void haspar(String rchain, Connection con1) throws SQLException {
        java.sql.Statement st = con1.createStatement();
        st.execute("drop table if exists childrenT;");
        ResultSet rst = st.executeQuery("select distinct child FROM Path_BayesNets where Rchain = '" + rchain + "' and parent <> ''");
        ArrayList<String> hasparent_tables = new ArrayList<String>();

        String bigTable = shortRchain + "_CT";
        // find name of contingency table that has the data we need for the chain

        while(rst.next()) {
            logger.fine("hasparent node: " + rst.getString(1));
            hasparent_tables.add(rst.getString(1));
        }
        for(int i = 0; i < hasparent_tables.size(); i++) {
            haspar_update(rchain, bigTable, hasparent_tables.get(i), con1, databaseName2);
        }
        st.close();
    }

    /**
     * Actually compute conditional probabilities.
     */
    public static void haspar_update(String rchain, String bigTable, String nodeName, Connection con1, String CT_Scheme) throws SQLException {
        java.sql.Statement st = con1.createStatement();
        ResultSet parents = st.executeQuery("select distinct parent from Path_BayesNets where Rchain = '" + rchain + "' and child = '" + nodeName + "' and parent != '';");
        ArrayList<String> parent_name = new ArrayList<String>();
        while(parents.next()) {
            logger.fine("parent value: " + parents.getString(1) + "\n");
            parent_name.add(parents.getString(1));
        }

        StringJoiner csvJoiner = new StringJoiner("`, `", "`", "`");
        for(String parent : parent_name) {
            csvJoiner.add(parent);
        }

        String tableColumns = csvJoiner.toString();
        logger.fine("Table columns: " + tableColumns + "\n");

        // general strategy: apply group by parent values to CT table, to find sum of parent counts. Then use that to divide the joint child-family count, which we get from the CT table.
        String escapedTableName = "`" + nodeName + "_CP`";
        st.execute("DROP TABLE IF EXISTS " + escapedTableName + ";");
        st.execute("drop table if exists temp;");

        String createTableString =
            "CREATE TABLE " + escapedTableName + " AS " +
            "SELECT SUM(MULT) AS MULT, `" + nodeName + "`, " + tableColumns + ", 0 as ParentSum " +
            "FROM " + databaseName2 + ".`" + bigTable + "` " +
            "GROUP BY `" + nodeName + "`, " + tableColumns + ";";

        // Change the ChildValue to FID -- Jan 23 Yan
        logger.fine(createTableString);
        st.execute(createTableString);
        st.execute(
            "ALTER TABLE " + escapedTableName + " " +
            "CHANGE COLUMN ParentSum ParentSum bigint(20);"
        );
        st.execute(
            "ALTER TABLE " + escapedTableName + " " +
            "ADD `local_mult` bigint(20);"
        );

        // Add index to CP table
        // Change the ChildValue to FID -- Jan 23 Yan
        String indexQuery =
            "ALTER TABLE " +  escapedTableName + " " +
            "ADD INDEX " + escapedTableName + " (`" + nodeName + "` ASC, ";

        StringJoiner csvAscJoiner = new StringJoiner("` ASC, `", "`", "` ASC");
        for(String parent : parent_name) {
            csvAscJoiner.add(parent);
        }

        String parentIndexColumns = csvAscJoiner.toString();
        indexQuery += parentIndexColumns;
        indexQuery += ");";
//        logger.fine(index);
        // Dec 12
        st.execute(indexQuery);

        String createTableQuery =
            "CREATE TABLE temp AS " +
            "SELECT MULT, " + tableColumns + ", SUM(MULT) AS ParentSum " +
            "FROM " + escapedTableName + " " +
            "GROUP BY " + tableColumns + ";";
        logger.fine(createTableQuery);
        st.execute(createTableQuery);

        // Add index to temp table
        String index_temp = "ALTER TABLE temp ADD INDEX  temp_ (";
        index_temp += parentIndexColumns;
        index_temp = index_temp + ");";
//        logger.fine(index_temp);
        // Dec 12
        st.execute(index_temp);

        String updateclause =
            "UPDATE " + escapedTableName + ", temp SET " + escapedTableName + ".ParentSum = temp.ParentSum " +
            "WHERE " + escapedTableName + ".`" + parent_name.get(0) + "` = temp.`" + parent_name.get(0) + "`";
        for (int i = 1; i < parent_name.size(); ++i) {
            updateclause += " AND " + escapedTableName + ".`" + parent_name.get(i) + "` = temp.`" + parent_name.get(i) + "`";
        }
        logger.fine(updateclause + ";");
        st.execute(updateclause + ";");

        st.execute(
            "ALTER TABLE " + escapedTableName + " " +
            "ADD CP FLOAT(7,6);"
        );
        // Our resolution is only up to 6 digits. This is mainly to help with exporting to BIF format later.
        st.execute(
            "ALTER TABLE " + escapedTableName + " " +
            "ADD likelihood FLOAT(20,2);"
        );

        st.execute("UPDATE " + escapedTableName + " SET CP = MULT / ParentSum;");

//        st.execute("update " + table_name + " set likelihood = log(CP) * mult;"); // Nov 29, likelihood = log(cp) * mult

        ResultSet rs = st.executeQuery("select Tuples from  Pvars_Not_In_Family where child = '" + nodeName + "';");
        java.sql.Statement st1 = con1.createStatement();
        long local = 1;

        while(rs.next()) {
            local = Long.parseLong (rs.getString("Tuples"));
            logger.fine("local is " + local);
            String sql = "UPDATE " + escapedTableName + " SET local_mult = MULT / " + local + ";";
//            String sql = "update " + table_name + " set local_mult = mult;";
            logger.fine(sql);
            // set local_mult = mult, May 21, 2014 zqian
            st1.execute(sql);
        }
        if (!rs.first()) {
            logger.fine("local is 1, ******" );
            String sql = "UPDATE " + escapedTableName + " SET local_mult = MULT / " + local + ";";
            st1.execute(sql);
        }

        st.execute("UPDATE " + escapedTableName + " SET likelihood = LOG(CP) * local_mult;");
        st.execute("drop table if exists temp;");

        // Next, compute scores for each node.

        ResultSet mylog = st.executeQuery(
            "SELECT SUM(likelihood) " +
            "FROM " + escapedTableName + ";"
        );
        mylog.absolute(1);
        double mylogsum = mylog.getDouble(1);
        st.execute("update Scores set LogLikelihood= " + mylogsum + " where Scores.Fid = '" + nodeName + "';");

        ResultSet mysample = st.executeQuery(
            "SELECT SUM(local_mult) " +
            "FROM " + escapedTableName + ";"
        );

//        ResultSet mysample = st.executeQuery("select sum(local_mult) from " + table_name + " where cp < 1.0 ;");
        long size = 0;
        mysample.absolute(1);
        size = Long.parseLong(mysample.getString(1));
        ResultSet big_mysample = st.executeQuery(
            "SELECT SUM(MULT) " +
            "FROM " + escapedTableName + ";"
        );
        long big_size = 0;
        big_mysample.absolute(1);
        big_size = Long.parseLong(big_mysample.getString(1));
        st.execute("update Scores set Big_SampleSize = " + big_size + " where Scores.Fid = '" + nodeName + "';");

        st.execute("update Scores set SampleSize = " + size + " where Scores.Fid = '" + nodeName + "';");
//        st.execute("update Scores set AIC = LogLikelihood - Parameters where Scores.Fid = '" + nodeName + "';");
//        st.execute("update Scores set BIC = 2 * LogLikelihood - Log(SampleSize) * Parameters where Scores.Fid = '" + nodeName + "';");
//        st.execute("update Scores set AICNormal = AIC / SampleSize where Scores.Fid = '" + nodeName + "';");
//        st.execute("update Scores set BICNormal = BIC / SampleSize where Scores.Fid = '" + nodeName + "';");

        // Compute the prior June 23, 2014, zqian
        st.execute(
            "ALTER TABLE " + escapedTableName + " " +
            "ADD prior FLOAT(7,6);"
        );
        ResultSet rst1 = st.executeQuery(
            "SELECT SUM(local_mult) " +
            "FROM " + escapedTableName
        );
        rst1.absolute(1);
        long total_sum = rst1.getLong(1);

        st.execute("DROP TABLE IF EXISTS temp;");
        String createtemp =
            "CREATE TABLE IF NOT EXISTS temp " +
            "SELECT SUM(local_mult) AS prior_parsum, `"+ nodeName + "` " +
            "FROM " + escapedTableName + " " +
            "GROUP BY `" + nodeName + "`;";
        logger.fine("temp: " + createtemp);
        st.execute(createtemp);

        String updateprior =
            "UPDATE " + escapedTableName + ", temp" + " SET " + escapedTableName + ".prior = temp.prior_parsum / " + total_sum + " " +
            "WHERE " + escapedTableName + ".`" + nodeName + "` = temp.`" + nodeName + "`;";

        logger.fine("updateprior: " + updateprior);
        st.execute(updateprior);
        st.execute("DROP TABLE IF EXISTS temp;");

        st.close();
    }

    /**
     * Prepare for the computing of local_mult, Dec 3rd, zqian.
     */
    public static void lcoal_mult_update(String rchain, Connection con1) throws SQLException {
        java.sql.Statement st = con1.createStatement();
        // For each node find its associated population variables
        // (e.g:RA(prof0,student0) as Fid, then it should have two pvid : prof0 and student0)
        st.execute("drop table if exists FNodes_pvars_UNION_RNodes_pvars;");
        st.execute(
            "create table FNodes_pvars_UNION_RNodes_pvars as " +
            " SELECT rnid as Fid , pvid FROM RNodes_pvars union distinct SELECT * FROM FNodes_pvars;"
        );
        /**
         * for each configuration(i.e. each node with all its parents ), find its associated population variables
         * (configuration 1:
         *      intel(student0) <--- RA(prof0,student0),
         *      then return prof0, and student0 )
         * ( or configuration 2:
         *      intel(student0) <--- RA(prof0,student0)
         *      intel(student0) <--- registration(course0,student0)
         *      then return prof0, student0 and course0 )
         * ( or configuration 3:
         *      intel(student0) <-- ranking(student0)
         *     then only return  student0 )
         */
        st.execute("drop table if exists Pvars_Family;");
        st.execute(
            "create table Pvars_Family as " +
            " select child, pvid FROM Path_BayesNets, FNodes_pvars_UNION_RNodes_pvars where Rchain = '" + rchain + "' and Path_BayesNets.parent = Fid " +
            "union select child, pvid FROM Path_BayesNets, FNodes_pvars_UNION_RNodes_pvars where Rchain = '"+rchain + "' and Path_BayesNets.child = Fid;"
        );
        /**
         * for each configuration(i.e. each node with all its parents), find some population variables that *NOT* related
         * (configuration 1: should return course0
         * ( or configuration 2: all population variables are joined, so there should be *NO* entry of this node
         * ( or configuration 3: course0 and prof0
         */
        st.execute("drop table if exists Pvars_Not_In_Family;");
        st.execute(
            "create table Pvars_Not_In_Family as " +
            " select Fid as child, pvid from FNodes, PVariables where (Fid,pvid) not in (select * from Pvars_Family);"
        );
        // Dec 4th, zqian
        // adding one column into table PVariables, for storing the number of tuples in the corresponding real data table
        // student0  ---- student --- count(*)
        ResultSet rs1 = st.executeQuery("show columns from `PVariables` like 'Tuples';");
        if (!rs1.next()) {
            st.execute("ALTER IGNORE TABLE `PVariables` ADD COLUMN `Tuples` BIGINT(20) NULL AFTER `index_number`;");
        }
        // Updating PVariables
        ResultSet rs = st.executeQuery("SELECT table_name FROM EntityTables;");
        java.sql.Statement st1 = con1.createStatement();
        while(rs.next()) {
            String entity_table = rs.getString("table_name");
            String sql = "update PVariables set Tuples = (select count(*) from " + real_database + "." + entity_table + ") where PVariables.table_name = '" + entity_table + "';";
            logger.fine("\n**********\n adding tuple to PVariables :"+ sql);
            st1.execute(sql);
        }
        // Updating Pvars_Not_In_Family
        st.execute("ALTER TABLE `Pvars_Not_In_Family` ADD COLUMN `Tuples` BIGINT(20) NULL AFTER `pvid`;");
        st.execute("update Pvars_Not_In_Family set Tuples = (select Tuples from PVariables where PVariables.pvid = Pvars_Not_In_Family.pvid);");

        st.close();
        st1.close();
    }

    public static void setVarsFromConfig() {
        Config conf = new Config();
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        real_database=conf.getProperty("dbname");
    }

    public static void connectDB() throws SQLException {
        String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName;

        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        con1 = DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
        java.sql.Statement st = con1.createStatement();
        // TODO: Check to see if other queries are more efficient.
        ResultSet myrchain = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = (SELECT max(length) FROM lattice_set);"
        );
        myrchain.absolute(1);
        rchain = myrchain.getString("RChain");
        shortRchain = myrchain.getString("short_RChain");
        logger.fine("rchain: " + rchain + "\n");
        logger.fine("short rchain: " + shortRchain + "\n");
        st.close();
    }
}
