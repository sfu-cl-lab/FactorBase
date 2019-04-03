package ca.sfu.cs.factorbase.tables;

/**
 * Aug 18, 2014. zqian
 * Handle the extreme case when there's only `mult` column.
 * Fixed the bug found on July 6th.
 */

/**
 * July 6th, 2014. zqian
 * Bug for processing the following case:
 * star: mult1
 * flat: mult2
 * false: mult1-mult2 ?
 * try: Financial_std_Training1_db.`operation(trans0)_a_star`
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.logging.Logger;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * Sort merge version3
 * Here the compare function is also good without concatenating the order by columns.
 */
public class Sort_merge3 {
    private static Logger logger = Logger.getLogger(Sort_merge3.class.getName());


    public static void sort_merge(String table1, String table2, String table3, Connection conn) throws SQLException, IOException {
        logger.info("\nGenerating false table by Subtraction using Sort_merge, cur_false_Table is: " + table3);

        StringBuilder builder = new StringBuilder();

        Statement st1 = (Statement) conn.createStatement();
        Statement st2 = (Statement) conn.createStatement();

        ArrayList<String> orderList = new ArrayList<String>();
        String order;

        /**
         * Code for getting the ORDER BY sequence using TABLE1, IT DOES NOT MATTER WHICH TABLE WE USE AS BOTH TABLES HAVE SAME COLUMNS.
         */
        ResultSet rst = st1.executeQuery(
            "SHOW COLUMNS FROM " + table1 +
            "WHERE field <> \"MULT\";"
        );

        while(rst.next()) {
            orderList.add("`" + rst.getString(1) + "`");
        }
        rst.close();

        if (orderList.size() > 0) {
            order = " " + orderList.get(0) + " ";

            for(int i = 1; i < orderList.size(); i++) {
                order = order + ", " + orderList.get(i);
            }

            // Code for merging the two tables.
            // BottleNeck, MOST expensive query for large table, more than 16 columns, zqian.
            // SELECT * INTO OUTFILE '/tmp/result.txt'; // Here the files are stored on the Server Side
            // and then load these files into memory?
            long time1 = System.currentTimeMillis();
            logger.info("\n rst1: " + "SELECT DISTINCT MULT, " + order + " FROM " + table1 + " ORDER BY " + order + ";");
            ResultSet rst1 = st1.executeQuery("SELECT DISTINCT MULT, " + order + " FROM " + table1 + ";");
            ResultSet rst2 = st2.executeQuery("SELECT DISTINCT MULT, " + order + " FROM " + table2 + ";");
            long time2 = System.currentTimeMillis();
//            System.out.print("order by time: " + (time2 - time1));

            // Finding the no of columns in a table.
            ResultSetMetaData rsmd = (ResultSetMetaData) rst1.getMetaData(); // DO NOT need to run another query, it should be orderList.size() + 1, zqian.
            int no_of_colmns = rsmd.getColumnCount();

            long time3 = System.currentTimeMillis();

            // Merging starting here.
            HashMap<String, Long> counts = new HashMap<String, Long>();
            while (rst2.next()) {
                StringJoiner rowData = new StringJoiner("$");
                for(int c = 2; c <= no_of_colmns; c++) {
                    rowData.add(rst2.getString(c));
                }

                counts.put(rowData.toString(), rst2.getLong(1));
            }

            while (rst1.next()) {
                StringJoiner rowData = new StringJoiner("$");
                for(int c = 2; c <= no_of_colmns; c++) {
                    rowData.add(rst1.getString(c));
                }

                Long count = counts.get(rowData.toString());
                if (count == null) {
                    builder.append(String.valueOf(rst1.getLong(1)) + "$" + rowData.toString() + "\n");
                } else {
                    builder.append(String.valueOf(rst1.getLong(1) - count) + "$" + rowData.toString() + "\n");
                }
            }

            long time4 = System.currentTimeMillis();
//            System.out.print("\t insert time: " + (time4 - time3));
            st2.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st2.execute("CREATE TABLE " + table3 + " SELECT * FROM " + table1 + " LIMIT 0;");
            st2.setLocalInfileInputStream(new ByteArrayInputStream(builder.toString().getBytes()));
            st2.execute("LOAD DATA LOCAL INFILE 'sort_merge.csv' INTO TABLE " + table3 + " FIELDS TERMINATED BY '$' LINES TERMINATED BY '\\n';");

            rst1.close();
            rst2.close();
            st1.close();
            st2.close();

            long time5 = System.currentTimeMillis();
//            System.out.print("\t export csv file to sql: " + (time5 - time4));
            logger.info("\ntotal time: " + (time5 - time1) + "\n");
        } else { // Aug 18, 2014 zqian: Handle the extreme case when there's only `mult` column.
            logger.fine("\n \t Handle the extreme case when there's only `mult` column \n");
            st2.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st2.execute("CREATE TABLE " + table3 + " SELECT * FROM " + table1 + " LIMIT 0;");
            logger.fine("INSERT INTO " + table3 + " SELECT (" + table1 + ".mult - " + table2 + ".mult) AS mult FROM " + table1 + ", " + table2 + ";");
            st2.execute("INSERT INTO " + table3 + " SELECT (" + table1 + ".mult - " + table2 + ".mult) AS mult FROM " + table1 + ", " + table2 + ";");
        }
    }
}