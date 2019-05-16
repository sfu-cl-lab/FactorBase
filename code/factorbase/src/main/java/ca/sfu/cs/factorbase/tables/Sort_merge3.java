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
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import ca.sfu.cs.factorbase.util.QueryGenerator;

import com.mysql.jdbc.Connection;
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

        /**
         * Code for getting the ORDER BY sequence using TABLE1, IT DOES NOT MATTER WHICH TABLE WE USE AS BOTH TABLES HAVE SAME COLUMNS.
         */
        ResultSet rst = st1.executeQuery(
            "SHOW COLUMNS FROM " + table1 +
            "WHERE field <> \"MULT\";"
        );

        while(rst.next()) {
            orderList.add(rst.getString(1));
        }
        rst.close();

        if (orderList.size() > 0) {
            // Code for merging the two tables.
            // BottleNeck, MOST expensive query for large table, more than 16 columns, zqian.
            // SELECT * INTO OUTFILE '/tmp/result.txt'; // Here the files are stored on the Server Side
            // and then load these files into memory?
            long time1 = System.currentTimeMillis();

            st2.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st2.execute("CREATE TABLE " + table3 + " SELECT * FROM " + table1 + " LIMIT 0;");
            String query = "INSERT INTO " + table3 + " " + QueryGenerator.createSubtractionQuery(table1, table2, "MULT", orderList);
            st2.execute(query);

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