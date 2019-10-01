package ca.sfu.cs.factorbase.learning;

/**
 * updated on Feb 6, 2014, fix the bug sum not equals to 1
 * Feb 6 Yan
 * Change ChildValue to FID
 * Fix bug: CP sum up to 1 in IMDB
 */
/**
 * Merge three programs together: pair, join_CP, markov_Blanket.
 * See workflow document, workflow.txt. For parameter estimation workflow, see flow_diagram_for_CP.tif.
 * Goal: generate smoothed CP table,
 *       use smoothed conditional probability to calculate KLD for the biggest rchain,
 *       calculate CLL for each node
 *      And also generate a plot for cll_db and cll_jp. Part not working.
 */
/**
 * Jun 25 zqian
 * This program computing some parameters will be used in the evaluation section.
 * And it's usefull for BIF_Generator.java
 * could output a simple plot of cll_db and cll_jp.
 */
/**
 * Feb 6 Yan
 * Change ChildValue to FID
 * Fix bug: CP sum up to 1 in IMDB
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import ca.sfu.cs.factorbase.util.QueryGenerator;

public class KLD_generator {
    // List of columns in original CP table (node_CP) w/o score columns, e.g.: ChildeValue, b, grade, sat...
    private static ArrayList<String> list;
    // List of node columns in the biggest rchain table, also in KLD table.
    private static ArrayList<String> column_names = new ArrayList<String>();
    // List of conditional probability columns for each node in the rchian, used in KLD table.
    private static ArrayList<String> column_names_CP = new ArrayList<String>();

    private static Logger logger = Logger.getLogger(KLD_generator.class.getName());


    public static void KLDGenerator(String database, Connection con2) throws SQLException, IOException {
        logger.info("KLD Generator starts");
        String Rchain = "";
        String shortRChain = "";
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT name AS RChain, short_rnid AS ShortRChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = (" +
                "SELECT MAX(length) " +
                "FROM lattice_set" +
            ");"
        );

        while(rs.next()) {
            Rchain = rs.getString("RChain");
            shortRChain = rs.getString("ShortRChain");
        }
        rs.close();
        st.close();

        smoothed_CP(Rchain, con2); // Updated the pairs generator Jun 19.
        logger.info("smoothed CP tables are already to use.");

        create_join_CP(database, Rchain, shortRChain, con2);  // Input the biggest rchain.
        logger.info("KLD table is already to use.");

        generate_CLL(Rchain, shortRChain, con2);
        logger.info("CLL tables are already to use.");

        plot_CLL(database, Rchain, shortRChain, con2);

        logger.info("\nKLD Generator Ends");
    }


    /**
     * Main function to generate all smoothed CP table.
     *
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws UnsupportedEncodingException if there are issues generating the CSV file for the pairs tables.
     * @throws FileNotFoundException if there are issues generating the CSV file for the pairs tables.
     */
    public static void smoothed_CP(
        String rchain,
        Connection con2
    ) throws SQLException, FileNotFoundException, UnsupportedEncodingException {
        java.sql.Statement st = con2.createStatement();

        // Find all the nodes that have parents and store them in list final_tables1.
        ArrayList<String> final_tables1 = new ArrayList<String>();

        // Need to specify Rchain.
        ResultSet rst = st.executeQuery(
            "SELECT DISTINCT child " +
            "FROM Path_BayesNets " +
            "WHERE parent <> '' " +
            "AND Rchain='" + rchain + "';"
        );

        while(rst.next()) {
            final_tables1.add(rst.getString(1).substring(0, rst.getString(1).length() - 1) + "_CP`");
        }

        // Generate full pairs table and smooth CP table for each node in final_tables1.
        for(int i = 0; i < final_tables1.size(); i++) {
            new_table_smoothed(final_tables1.get(i), con2); // Updating the full pairs generator.
        }

        // Another situation for some nodes that do NOT have any parent, OCT 22, zqian.
        logger.fine("for some nodes that do NOT have any parent.");
        // Find all the nodes that do NOT have parents and store them in list final_table_smoothed.
        rst = st.executeQuery(
            "SELECT child " +
            "FROM Path_BayesNets " +
            "WHERE rchain='" + rchain + "'" +
            "AND child NOT IN (" +
                "SELECT DISTINCT child " +
                "FROM Path_BayesNets " +
                "WHERE parent <> '' " +
                "AND Rchain = '" + rchain + "'" +
            ");"
        );

        ArrayList<String> final_tables_smoothed = new ArrayList<String>();
        while(rst.next()) {
            final_tables_smoothed.add(rst.getString(1));
        }

        // Generate smooth CP table for each node in final_tables_smoothed.
        for(int i = 0; i < final_tables_smoothed.size(); i++) {
            // Create smoothed CP table for each node.
            String table_name = final_tables_smoothed.get(i).substring(0, final_tables_smoothed.get(i).length() - 1) + "_CP_smoothed`";
            String orig_table = final_tables_smoothed.get(i).substring(0, final_tables_smoothed.get(i).length() - 1) + "_CP`"; // Fixed Oct 22, zqian, missing on '`'.
            logger.fine("CREATE TABLE " + table_name + " AS SELECT * FROM " + orig_table + ";"); // zqian Oct 22, testing.

            String nodeName = final_tables_smoothed.get(i);

            st.execute("DROP TABLE IF EXISTS " + table_name + ";");
            st.execute("CREATE TABLE " + table_name + " AS SELECT * FROM " + orig_table + ";");
            logger.fine("CREATE TABLE " + table_name + " AS SELECT * FROM " + orig_table + ";"); // zqian Oct 22, testing.

            // Update values of mult an CP. We add a virtual count of 1 to each pair (family state). This is where the smoothing happens.
            st.execute("UPDATE " + table_name + " SET mult = mult + 1");
            ResultSet rst1 = st.executeQuery("SELECT SUM(mult) FROM " + table_name + ";");
            rst1.absolute(1);
            long sum_mult = rst1.getLong(1);
            st.execute("UPDATE " + table_name + " SET cp = mult / " + sum_mult + ";");

            // Make CP sum up=1.
            logger.fine("SELECT " + nodeName + " FROM " + table_name + ";");
            ResultSet rst2 = st.executeQuery("SELECT " + nodeName + " FROM " + table_name + ";");
            rst2.absolute(1);
            String CV = rst2.getString(1);
            ResultSet rst3 = st.executeQuery("SELECT SUM(CP) FROM " + table_name + " WHERE " + nodeName + " <> '" + CV + "';");
            rst3.absolute(1);
            float SubTotal = rst3.getFloat(1);
            if (SubTotal >= 1.0) {
                SubTotal = 1;
            }

            // Nov 12 zqian, this may cause minus cp value which is not acceptable of _bif file.
            // Round off issue: compare with a tiny enough number instead of using absolute value.
            String query_temp2= ("UPDATE " + table_name + "SET CP = (1 - " + SubTotal + ") WHERE " + nodeName + " = '" + CV + "';");
            st.execute(query_temp2);
        }
        st.close();
    }

    /**
     * Generate full pairs table and smooth CP table for one node.
     *
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws UnsupportedEncodingException if there are issues generating the CSV file for the pairs tables.
     * @throws FileNotFoundException if there are issues generating the CSV file for the pairs tables.
     */
    private static void new_table_smoothed(
        String table_name,
        Connection con2
    ) throws SQLException, FileNotFoundException, UnsupportedEncodingException {
        java.sql.Statement st = con2.createStatement();
        String name = table_name.substring(0, table_name.length() - 1) + "_smoothed`";

        // Create smooth CP table.
        st.execute("DROP TABLE IF EXISTS " + name + ";");
        st.execute("CREATE TABLE " + name + " LIKE " + table_name + ";");

        // Add index to 1. (MULT, ChildValue, parents....) and 2. (parent nodes...)
        ResultSet rst = st.executeQuery("SHOW COLUMNS FROM " + name);
        ArrayList<String> indexlist1 = new ArrayList<String>();

        while(rst.next()) {
            indexlist1.add(rst.getString(1));
        }

        // Delete parentsum, CP and likelihood, local_mult.
        indexlist1.remove(indexlist1.size() - 1);
        indexlist1.remove(indexlist1.size() - 1);
        indexlist1.remove(indexlist1.size() - 1);
        indexlist1.remove(indexlist1.size() - 1);
        if (((String)indexlist1.get(indexlist1.size() - 1)).equals("ParentSum")) {
            indexlist1.remove(indexlist1.size() - 1);
        }

        ArrayList<String> indexlist2 = new ArrayList<String>(indexlist1);
        // Delete MULT, FID.
        indexlist2.remove(0);
        if (indexlist2.get(0).equalsIgnoreCase("FID")) {
            indexlist2.remove(0);
        }

        // Create clauses to add those indexes.
        // index1: MULT, current node and its parents.
        String index1 = "ALTER TABLE " + name + " ADD INDEX " + name.substring(0, name.length() - 1) + "1`(";
        for (int i = 0; i < indexlist1.size(); ++i) {
            index1 = index1 + "`" + indexlist1.get(i) + "` ASC";
            if ((i + 1) < indexlist1.size()) {
                index1 = index1 + ", ";
            }
        }

        index1 = index1 + ");";
        st.execute(index1);

        // index2: only parents
        String index2 = "ALTER TABLE " + name + " ADD INDEX " + name.substring(0, name.length() - 1) + "2`(";
        for (int i = 0; i < indexlist2.size(); ++i) {
            index2 = index2 + "`" + indexlist2.get(i) + "` ASC";
            if ((i + 1) < indexlist2.size()) {
                index2 = index2 + ", ";
            }
        }

        index2 = index2 + ");";
        st.execute(index2);

        // Generate full pairs table.
        pairs(table_name, con2); // Updated on Jun 19, much faster than sql joins.


        // Get all the columns, generate them as a clause.
        String columns = "`" + String.join("`,`", list) + "`";

        // query1: insert the exist rows into smoothed CP table
        String query1 = "INSERT " + name + " ( MULT ";
        for(int i = 0; i < list.size(); i++) {
            query1 = query1 + ", `" + list.get(i) + "` ";
        }
        query1 = query1 + ") SELECT MULT ";
        for(int i = 0; i < list.size(); i++) {
            query1 = query1 + ", `" + list.get(i) + "` ";
        }
        query1 = query1 + " FROM " + table_name + ";";
        st.execute(query1);

        // zqian@ Oct 21, 2013, Bottleneck??

        //query2: insert not exists pairs into smoothed CP table
        String query2 =
            "INSERT " + name +
            "(MULT," + columns + ") (" +
                QueryGenerator.createDifferenceQuery(
                    "MULT," + columns,
                    list,
                    table_name.subSequence(0, table_name.length() - 1) + "_pairs`",
                    table_name
                ) +
            ");";
        logger.fine("bottleneck? query2:" + query2);
        st.execute(query2);

        // Add all MULT by 1, Laplace.
        st.execute("update "+name+"set MULT=MULT+1;");

        // Update parent sum, and conditional probability.
        // Updated on Feb 6, 2014, fix the bug sum not equals to 1.
        update_ps(name, con2);
        st.close();

    }

    /**
     * Generate full pairs table for one node
     * fast pairs generator, created by YanSun @ Jun 19
     * using recursive loops in java instead of simple inefficient sql joins
     *
     * @throws SQLException if there are issues executing the SQL queries.
     * @throws UnsupportedEncodingException if there are issues generating the CSV file for the pairs tables.
     * @throws FileNotFoundException if there are issues generating the CSV file for the pairs tables.
     */
    private static void pairs(
        String table_name,
        Connection con2
    ) throws SQLException, FileNotFoundException, UnsupportedEncodingException {
        String name = table_name.subSequence(0, table_name.length() - 1) + "_pairs`";

        java.sql.Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SHOW COLUMNS FROM " + table_name);

        // Get all value columns in original CP table.
        list = new ArrayList<String>();
        rst.absolute(1);
        while(rst.next()) {
            list.add(rst.getString(1));
        }

        // Delete parentsum, CP and likelihood, local_mult.
        list.remove(list.size()-1);
        list.remove(list.size()-1);
        list.remove(list.size()-1);
        list.remove(list.size()-1);
        if (((String)list.get(list.size() - 1)).equals("ParentSum")) {
            list.remove(list.size() - 1);
        }

        // Don't know why there is an mult=0 column in pair table, doesm't matter.
        st.execute("DROP TABLE IF EXISTS " + name + ";");
        String createclause = "CREATE TABLE " + name + "( MULT int ";
        for (int i = 0; i < list.size(); ++i) {
            createclause = createclause + ", `" + list.get(i) + "` VARCHAR(20) NOT NULL";
        }

        createclause = createclause + ");";
        logger.fine("createclause: " + createclause);
        st.execute(createclause);

        // Add index to all columns in pair table.
        String index_p = "ALTER TABLE " + name + " ADD INDEX " + name + "( MULT ASC";
        for (int i = 0; i < list.size(); ++i) {
            index_p = index_p + ", `" + list.get(i) + "` ASC";
        }
        index_p = index_p + ");";
        st.execute(index_p);

        ResultSet rst1 = st.executeQuery("SELECT DISTINCT `" + list.get(0) + "` FROM " + table_name);
        ArrayList<String> join = new ArrayList<String>();
        ArrayList<String> join_pre = new ArrayList<String>();

        while(rst1.next()) {
            join.add(rst1.getString(1));
            join_pre.add(rst1.getString(1));
        }

        for (int k = 1; k < list.size(); ++k) {
            ResultSet rst_value = st.executeQuery("SELECT DISTINCT `" + list.get(k) + "` FROM " + table_name);
            ArrayList<String> values = new ArrayList<String>();
            while (rst_value.next()) {
                values.add(rst_value.getString(1));
            }

            join.clear();

            for (int i = 0; i < join_pre.size(); ++i) {
                for (int j = 0; j < values.size(); ++j) {
                    join.add(join_pre.get(i) + "," + values.get(j));
                }
            }

            join_pre.clear();

            for (int i = 0; i < join.size(); ++i) {
                join_pre.add(join.get(i));
            }
        }

        // Write in csv file.
        String filename = table_name.subSequence(1, table_name.length() - 1) + ".csv";
        File ftemp=new File(filename);
        if(ftemp.exists()) {
            ftemp.delete();
        }

        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for (int i = 0; i < join.size(); ++i) {
            writer.print("0," +  join.get(i) + "\n");
        }

        writer.close();

        // Load csv file into database.
        st.execute("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE " + name + "  FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n';");

        if(ftemp.exists()) {
            ftemp.delete();
        }
    }


    /**
     * Updates parent sum (counts), using the extra virtual observation. We use the Laplace correction.
     * The computation is similar to the previous computation of conditional probabilities.
     * The smoothed probabilities are normalized and have at most 6 significant digits.
     */
    private static void update_ps(String table_final_smoothed, Connection con2) throws SQLException {
        java.sql.Statement st = con2.createStatement();
        java.sql.Statement st1 = con2.createStatement();

        String nodeName = "`" + list.get(0) + "`";
        String parents="";
        for(int i = 1; i < list.size(); i++) {
            parents = parents + " , `" + list.get(i) + "` ";
        }

        st.execute("DROP TABLE IF EXISTS temp1;");
        String query1 = "CREATE TABLE temp1 AS SELECT SUM(mult) AS parsum " + parents + " FROM " + table_final_smoothed + " GROUP BY " + parents.substring(2);
        logger.fine("query1: "+ query1);

        st.execute(query1);

        // Add index to temp1 (all parents).
        String index_t = "ALTER TABLE temp1 ADD INDEX temp1( `" + list.get(1) + "` ASC";
        for (int i = 2; i < list.size(); ++i) {
            index_t = index_t + ", `" + list.get(i) + "` ASC";
        }
        index_t = index_t + ");";
        st.execute(index_t);

        // zqian@ Oct 21, 2013, Bottleneck??
        String query2 = "UPDATE " + table_final_smoothed + " SET ParentSum = (SELECT temp1.parsum FROM temp1 WHERE ";

        // Compute parent count as before. Later we apply smoothing.
        String compare = " temp1.`" + list.get(1) + "` = " + table_final_smoothed + ".`" + list.get(1) + "` ";
        for(int i = 2; i < list.size(); i++) {
            compare = compare + "AND temp1.`" + list.get(i) + "` = " + table_final_smoothed + ".`" + list.get(i) + "` ";
        }

        query2 = query2 + " " + compare + ");";
        logger.fine("query2: "+ query2);

        st.execute(query2);
        st.execute("UPDATE " + table_final_smoothed + "SET CP = MULT / ParentSum;");

        ResultSet rst_temp1 = st.executeQuery("SELECT DISTINCT " + parents.substring(2) + " FROM temp1;");
        String whereclause="";
        logger.fine("parents.substring(2): " + parents.substring(2));

        while(rst_temp1.next()) {
            for (int i = 1; i < list.size(); ++i) {
                whereclause = whereclause + "`" + list.get(i) + "` = '" + rst_temp1.getString(i) + "' AND ";
            }
            logger.fine("whereclause: " + whereclause);

            // Let CV1 be the childvalue with the largest CP  Feb 6 Yan.
            logger.fine("SELECT DISTINCT " + nodeName + " FROM " + table_final_smoothed + " WHERE " + whereclause.substring(0, whereclause.length() - 4) + " ORDER BY CP DESC;");
            ResultSet rst_temp = st1.executeQuery(
                "SELECT DISTINCT " + nodeName + " " +
                "FROM " + table_final_smoothed + " " +
                "WHERE " + whereclause.substring(0, whereclause.length() - 4) + " " +
                "ORDER BY CP DESC;"
            );

            String CV1 = null;
            if (rst_temp.absolute(1)) {
                CV1 = rst_temp.getString(1);
                logger.fine(nodeName + " CV1: " + CV1);

                ResultSet rst_temp2 = st1.executeQuery(
                    "SELECT SUM(CP) " +
                    "FROM " + table_final_smoothed + " " +
                    "WHERE " + whereclause + " " + nodeName + " <> '" + CV1 + "';"
                );
                rst_temp2.absolute(1);
                float SubTot = rst_temp2.getFloat(1);

                String query_temp1 = "UPDATE " + table_final_smoothed + " SET CP = 1 - " + SubTot + " WHERE " + whereclause + " " + nodeName + " = '" + CV1 + "';";
                st1.execute(query_temp1);
            }

            whereclause = "";
        }
    }


    /******************************************************************************************************************/
    /******************************************************************************************************************/

    /**
     * Main function to generate KLD table for given Rchain.
     */
    private static void create_join_CP(
        String databaseCT,
        String rchain,
        String shortRChain,
        Connection con2
    ) throws SQLException {
        String table_name = databaseCT + "_CT." + shortRChain.substring(0, shortRChain.length() - 1) + "_CT`";
        String newTable_name = shortRChain.substring(0, shortRChain.length() - 1) + "_CT_KLD`";

        // table_name is input contingency table, newTable_name will be  output KLD table.
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SHOW COLUMNS FROM " + table_name + ";");
        while(rst.next()) {
            column_names.add("`" + rst.getString(1) + "`");
            // Add conditional probability column for each attribute column.
            column_names_CP.add("`" + rst.getString(1) + "_CP`");
        }

        st.execute("DROP TABLE IF EXISTS " + newTable_name);
        String query1 = "CREATE TABLE " + newTable_name + " (id INT(11) NOT NULL AUTO_INCREMENT, MULT BIGINT(21), ";
        // Make query string for creating KLD table.
        // Auto index each row in table.
        for(int i = 1; i < column_names.size(); i++) {
            // Add column headers.
            query1 = query1 + column_names.get(i) + " VARCHAR(45)," + column_names_CP.get(i) + " FLOAT(7,6),";
        }
        query1 = query1 + " JP FLOAT,  JP_DB  FLOAT, KLD  FLOAT DEFAULT 0, PRIMARY KEY (id)) ENGINE=INNODB;";
        st.execute(query1);

        // Copy the values from the mult table. CP values are null at this point.
        String query2 = "INSERT INTO " + newTable_name + " (MULT";
        for(int i = 1; i < column_names.size(); i++) {
            query2 = query2 + ", " + column_names.get(i);
        }
        query2 = query2 + ") SELECT * FROM " + table_name + ";"; // Nov 28 @ zqian, do not use "*" in terms of query optimization.
        // Nov 28 @ zqian, adding covering index to CT table for query string.
        logger.fine(query2);
        st.execute(query2);

        logger.fine("\n insert into KLD table conditional probability for each node"); // zqian
        // Insert CP value to attributes.
        insert_CP_Values(rchain, newTable_name, con2);
        logger.fine("\n compute Bayes net joint probabilities"); //zqian
        // Compute Bayes net joint probabilities.
        cal_KLD(newTable_name, con2);

        st.close();
    }


    /**
     * Insert into KLD table conditional probability for each node.
     * Add index to speed up the update statement.
     */
    private static void insert_CP_Values(String rchain, String newTable_name, Connection con2) throws SQLException {
        Statement st = con2.createStatement();

        // Handle functor nodes with no parents from Path_BayesNet where rchain is given.
        ResultSet rst1 = st.executeQuery(
            "SELECT child " +
            "FROM Path_BayesNets " +
            "WHERE rchain = '" + rchain + "' " +
            "AND child NOT IN (" +
                "SELECT DISTINCT child " +
                "FROM Path_BayesNets " +
                "WHERE parent <> '' " +
                "AND rchain = '" + rchain + "'" +
            ");");

        ArrayList<String> no_parents = new ArrayList<String>();
        while(rst1.next()) {
            // Remove final apostrophe.
            no_parents.add(rst1.getString(1).substring(0, rst1.getString(1).length() - 1));
        }

        String temp = "";
        String query1 = "";
        // For each functor node with no parents, copy conditional probability from _CP table.
        // Change ChildeValue to FID -- Jan 24 Yan.
        for(int i = 0; i < no_parents.size(); i++) {
            query1 = "UPDATE " + newTable_name;
            temp = no_parents.get(i) + "_CP_smoothed`";
            query1 = query1 + " ," + temp + " SET " + no_parents.get(i) + "_CP` = " + temp + ".CP WHERE " + temp + "." + no_parents.get(i) + "` = " + newTable_name + "." + no_parents.get(i) + "`";
            logger.fine(no_parents.get(i) + "`: " + query1);
            st.execute(query1);
        }

        // Handle nodes with parents from Path_BayesNet where rchain is given.
        ResultSet rst2 = st.executeQuery("SELECT DISTINCT child FROM Path_BayesNets WHERE parent <> '' AND Rchain = '" + rchain + "';");

        ArrayList<String> with_parents=new ArrayList<String>();
        ArrayList<String> parents;
        // Stores list of parents for current child/attribute/functor node.
        while(rst2.next()) {
            with_parents.add(rst2.getString(1).substring(0, rst2.getString(1).length() - 1));
        }

        temp = "";
        String query2 = "";

        for(int i = 0; i < with_parents.size(); i++) {
            parents = new ArrayList<String>();
            temp = with_parents.get(i) + "_CP_smoothed`"; // temp contains name of CP table for current child.
            ResultSet rs_temp = st.executeQuery("SHOW COLUMNS FROM " + temp);
            // SQL query to find list of parents for current child.
            rs_temp.absolute(2);
            // Start with 3 row in result set, which is the 3rd column in CP table. The first two columns are MULT and FID.
            while(rs_temp.next()) {
                String s = rs_temp.getString(1);
                if(s.charAt(0) != '`') {
                    parents.add("`" + rs_temp.getString(1) + "`");
                }
                else {
                    parents.add(rs_temp.getString(1));
                }
            }


            // Remove the last 4 rows, which are the last 4 columns in CP table.
            // These are CP, parent_sum, local_mult, log-likelihood. // Remove local_mult column Dec 4th, zqian.
            parents.remove(parents.size()-1);
            parents.remove(parents.size()-1);
            parents.remove(parents.size()-1);
            parents.remove(parents.size()-1);
            parents.remove(parents.size()-1);

            // Nov 28 @ zqian, only add index to KLD table?  helpful for update?
            // Add index to CP_smoothed and big KLD table, current node and its parents.
            String index2 = "ALTER TABLE " + newTable_name + " ADD INDEX " + with_parents.get(i) + "` (" + with_parents.get(i) + "` ASC ";
            for (int j = 0; j < parents.size(); ++j) {
                index2 = index2 + ", " + parents.get(j) + " ASC ";
            }

            index2 = index2 + ");";
            logger.fine("index2: "+ index2);
            st.execute(index2);

            query2 = "UPDATE " + newTable_name;
            query2 = query2 + " ," + temp + " SET " + with_parents.get(i) + "_CP` = " + temp + ".CP WHERE " + temp + "." + with_parents.get(i) + "` = " + newTable_name + "." + with_parents.get(i) + "`";

            // Where parents agree on values.
            for (int j = 0; j < parents.size(); ++j) {
                query2 = query2 + "AND " + newTable_name + "." + parents.get(j) + " = " + temp + "." + parents.get(j) + " ";
            }

            logger.fine(with_parents.get(i) + "`: " + query2);
            st.execute(query2);
        }

        st.close();
    }


    /**
     * Calculate joined probability and KLD.
     */
    private static void cal_KLD(String newTable_name, Connection con2) throws SQLException {
        Statement st = con2.createStatement();
        String query_jp = "UPDATE " + newTable_name + " set JP = " + column_names_CP.get(1);
        for(int l = 2; l < column_names_CP.size(); l++) {
            query_jp = query_jp + " * " + column_names_CP.get(l);
        }
        logger.fine("KLD1: " + query_jp);
        st.execute(query_jp);

        ResultSet rst = st.executeQuery("SELECT SUM(MULT) FROM " + newTable_name + ";");
        rst.absolute(1);
        Long mult_sum = rst.getLong(1);
        String query1 = "UPDATE " + newTable_name + " SET JP_DB = MULT / " + mult_sum + ";";
        logger.fine("query 1: " + query1);
        st.execute(query1);

        String query2 = "UPDATE " + newTable_name + " SET KLD = (JP_DB * (log(JP_DB) - log(JP))) WHERE MULT <> 0;";
        logger.fine("KLD2: " + query2);
        st.execute(query2);
        st.close();
    }

    /******************************************************************************************************************/
    /******************************************************************************************************************/

    /**
     * Main function for generate method for all nodes in table.
     * Rchain needs to be given.
     */
    private static void generate_CLL(
        String rchain,
        String shortRChain,
        Connection con2
    ) throws SQLException {
        Statement st = con2.createStatement();

        // Select all nodes in given Rchain.
        // Should be Path_BayesNets.
        ResultSet rst = st.executeQuery(
            "SELECT DISTINCT child " +
            "FROM Path_BayesNets " +
            "WHERE Rchain = '" + rchain + "';"
        );
        ArrayList<String> node_list=new ArrayList<String>();

        while(rst.next()) {
            node_list.add(rst.getString(1));
        }

        // Generate CLL table for every node.
        for(int i = 0; i < node_list.size(); i++) {
            generate_CLL_node(node_list.get(i), rchain, shortRChain, con2);
        }

        st.close();
    }


    /**
     * Generate CLL table for one node.
     */
    private static void generate_CLL_node(
        String Node_name,
        String rchain,
        String shortRChain,
        Connection con2
    ) throws SQLException {
        Statement st = con2.createStatement();
        String table_name = Node_name.substring(0, Node_name.length() - 1) + "_CLL`";

        // Get markov blanket for given node in rchain.
        ArrayList<String> node_blanket = markov_blank(Node_name, rchain, con2);

        // mrk: made markov blanket as a string for query.
        String mrk="";
        for(int i = 0; i < node_blanket.size(); i++) {
            mrk = mrk + ", " + node_blanket.get(i);
        }
        st.execute("DROP TABLE IF EXISTS " + table_name + ";");

        if (mrk!="") {
            // Create statement for CLL table.
            String query1 = "CREATE TABLE " + table_name + " (" + Node_name + " VARCHAR (45)";
            for(int i = 0; i < node_blanket.size(); i++) {
                query1 = query1 + " , " + node_blanket.get(i) + " VARCHAR (45) ";
            }
            query1 = query1 + ", JP_DB FLOAT, JP_DB_blanket FLOAT, CLL_DB FLOAT,JP FLOAT,JP_blanket FLOAT, CLL_JP FLOAT, AbsDif FLOAT) ENGINE=INNODB;";
            st.execute(query1);

            // Insert node values, JP and JP_DB into CLL table.
            String query2 = "INSERT INTO " + table_name + "(" + Node_name;
            query2 = query2 + mrk;
            query2 = query2 + " , JP_DB, JP) SELECT " + Node_name;
            query2 = query2 + mrk;
            query2 = query2 + " , SUM(JP_DB), SUM(JP) FROM " + shortRChain.substring(0, shortRChain.length() - 1) + "_CT_KLD` " + "GROUP BY " + Node_name;
            // To optimize.
            // Nov 28 @ zqian, adding covering index for group by list.
            query2 = query2 + mrk;
            logger.fine("CLL query2: " + query2);
            st.execute(query2);

            //create a temp table to sum up JP and JP_DB, group by markov blanket
            st.execute("DROP TABLE IF EXISTS temp;");
            logger.fine("Node_name " + Node_name + "\n mrk " + mrk);

            logger.fine("create temp: " + "CREATE TABLE temp SELECT SUM(JP_DB) AS JP_DB_sum, SUM(JP) AS JP_sum " + mrk + " FROM " + table_name + " GROUP BY " + mrk.substring(1) + "\n");
            // To optimize.
            // Nov 28 @ zqian, adding covering index for group by list.
            st.execute("CREATE TABLE temp SELECT SUM(JP_DB) AS JP_DB_sum, SUM(JP) AS JP_sum " + mrk + " FROM " + table_name + " GROUP BY " + mrk.substring(1));

            // Insert the sum of JP and JP_DB into CLL table.
            String query3 = "UPDATE " + table_name + ", temp SET JP_DB_blanket = temp.JP_DB_sum, JP_blanket = temp.JP_sum WHERE ";
            query3 = query3 + "temp." + node_blanket.get(0) + " = " + table_name + "." + node_blanket.get(0);
            for(int i = 1; i < node_blanket.size(); i++) {
                query3 = query3 + " AND temp." + node_blanket.get(i) + " = " + table_name + "." + node_blanket.get(i);
            }
            logger.fine("CLL query2: " + query3);
            st.execute(query3);
        } else {
            String query1 = "CREATE TABLE " + table_name + " (" + Node_name + " VARCHAR (45)";
            query1 = query1 + ", JP_DB FLOAT, JP_DB_blanket FLOAT, CLL_DB FLOAT, JP FLOAT, JP_blanket FLOAT, CLL_JP FLOAT, AbsDif FLOAT) ENGINE=INNODB;";
            st.execute(query1);
            String query2 = "INSERT INTO " + table_name + "(" + Node_name + " , JP_DB, JP) SELECT " + Node_name;
            query2 = query2 + " , SUM(JP_DB), SUM(JP) FROM " + shortRChain.substring(0, shortRChain.length() - 1) + "_CT_KLD` " + "GROUP BY " + Node_name;
            logger.fine("CLL query2: " + query2);
            st.execute(query2);

            // Because the blanket is empty, set the joint probability of the blanket equal to the joint probability of the point.
            st.execute("UPDATE " + table_name + " SET JP_DB_blanket = JP_DB, JP_blanket = JP;");
        }

        // Calculate CLL.
        st.execute("UPDATE " + table_name + "SET CLL_DB = log(JP_DB / JP_DB_blanket), CLL_JP = log(JP / JP_blanket)"); // Updated on Jun 28.
        st.execute("UPDATE " + table_name + " SET AbsDif = ABS(CLL_DB - CLL_JP)");
        st.close();
    }


    /**
     * Generate markov blanket for one node given its rchain, and return the result in a list.
     */
    private static ArrayList<String> markov_blank(String node_name, String Rchain_value, Connection con2) throws SQLException {
         // List of the children of the node.
         ArrayList<String> children = new ArrayList<String>();
         // List of markov blanket.
         ArrayList<String> blanket = new ArrayList<String>();
         // First store markov blanket in a set to reduce the duplicate.
         Set<String> markov_blanket = new HashSet<String>();

         Statement st = con2.createStatement();
         // Select the parents of the node.
         String query1 = "SELECT DISTINCT parent FROM `Path_BayesNets` WHERE child = '" + node_name + "' AND Rchain = '" + Rchain_value + "' AND parent <> '';";
         ResultSet rst1 = st.executeQuery(query1);
         while(rst1.next()) {
             // Add parents to markov blanket.
             markov_blanket.add(rst1.getString(1));
         }

         //select the children of the node
         String query2 = "SELECT DISTINCT child FROM `Path_BayesNets` WHERE parent = '" + node_name + "' AND Rchain = '" + Rchain_value + "';";
         ResultSet rst2 = st.executeQuery(query2);

         while(rst2.next()) {
             // Add children to markov blanket and children list.
             children.add(rst2.getString(1));
             markov_blanket.add(rst2.getString(1));
         }

         // Select the spouses of the node.
         for(int i = 0; i < children.size(); i++) {
             ResultSet rst_temp;
             String temp = children.get(i);
             String query_temp = "SELECT DISTINCT parent FROM `Path_BayesNets` WHERE child = '" + temp + "' AND Rchain = '" + Rchain_value + "' AND parent <> '' AND parent <> '" + node_name + "';";
             rst_temp = st.executeQuery(query_temp);
             while(rst_temp.next()) {
                 markov_blanket.add(rst_temp.getString(1));
             }
         }

         Object[] list_blanket = markov_blanket.toArray();
         for(int i = 0; i < list_blanket.length; i++) {
             blanket.add(list_blanket[i].toString());
         }

         st.close();
         return blanket;
     }

    /******************************************************************************************************************/


    private static void plot_CLL(
        String database,
        String rchain,
        String shortRChain,
        Connection con2
    ) throws SQLException, IOException {
        Statement st = con2.createStatement();
        ArrayList<Double> cll_db = new ArrayList<Double>();
        ArrayList<Double> cll_jp = new ArrayList<Double>();
        ResultSet rst_p = st.executeQuery("SELECT DISTINCT child FROM Path_BayesNets WHERE Rchain = '" + rchain + "';");
        ArrayList<String> node_list = new ArrayList<String>();

        while(rst_p.next()) {
            node_list.add(rst_p.getString(1));
        }

        for(int i = 0; i < node_list.size(); ++i) {
            String table_name = node_list.get(i).substring(0, node_list.get(i).length() - 1) + "_CLL`";
            ResultSet rst_cll = st.executeQuery("SELECT (CLL_DB) FROM " + table_name);
            while(rst_cll.next()) {
                cll_db.add(rst_cll.getDouble(1));
            }
            rst_cll = st.executeQuery("SELECT (CLL_JP) FROM " + table_name);
            while(rst_cll.next()) {
                cll_jp.add(rst_cll.getDouble(1));
            }
        }

        double [] x = new double[cll_db.size()];
        double [] y = new double[cll_jp.size()];

        for (int i = 0; i < cll_db.size(); ++i) {
            x[i] = cll_db.get(i);
            y[i] = cll_jp.get(i);
        }

        // Calculate the average of cll_db & cll_jp.
        double cll_db_sum = 0;
        double cll_jp_sum = 0;
        double MeanError= 0;
        for (int i = 0; i < cll_db.size(); ++i) {
            cll_db_sum = cll_db_sum + x[i];
            cll_jp_sum = cll_jp_sum + y[i];
            MeanError += (x[i] - y[i]);
        }

        int cll_db_size = cll_db.size();
        int cll_jp_size = cll_jp.size();
        double ave_all_db = cll_db_sum / cll_db_size;
        double ave_all_jp = cll_jp_sum / cll_jp_size;
        logger.fine("The average for cll_db is " + ave_all_db + "\n");
        logger.fine("The average for cll_jp is " + ave_all_jp + "\n");
        MeanError = MeanError / cll_db_size;
        logger.fine("\n The Mean Difference of CLL is " + MeanError);
        logger.fine("\n The Relative Mean Difference of CLL is " + Math.abs(MeanError / ave_all_db)); // zqian Dec 5th

        // Calculate the variance.
        double var_sum = 0;
        double var = 0;
        for (int i = 0; i < cll_db.size(); ++i) {
            var_sum = var_sum + Math.pow(((x[i] - y[i]) - MeanError), 2);
        }
        var = Math.sqrt(var_sum / cll_db_size);
        logger.fine("The Standard Deviation is " + var + "\n");

        String KLD="";
        Statement st1 = con2.createStatement();
        ResultSet rs1 = st1.executeQuery("SELECT SUM(kld) AS KLD FROM " + database + "_BN" + ".`" + shortRChain.replace("`", "") + "_CT_KLD`;");
        rs1.next();
        KLD = rs1.getString("KLD");
        logger.fine(" KLD is: " + KLD);
        logger.fine("database: " + database);
        rs1.close();
        st1.close();
        st.close();
    }
}