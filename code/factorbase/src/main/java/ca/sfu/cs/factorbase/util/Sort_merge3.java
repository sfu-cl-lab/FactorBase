package ca.sfu.cs.factorbase.util;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Sort merge version3
 * Here the compare function is also good without concatenating the order by columns.
 */
public class Sort_merge3 {
    private static Logger logger = Logger.getLogger(Sort_merge3.class.getName());


    /**
     * Subtract the MULT column of the rows in {@code table1} by the MULT column of the matching row in
     * {@code table2}.
     *
     * @param conn - connection to the database containing {@code table1} and {@code table2}.
     * @param table1 - the table to have its values in the MULT column subtracted by the MULT column in
     *                 {@code table2}.
     * @param table2 - the table to match rows with in {@code table1} and subtract by the values found in the
     *                 MULT column.
     * @param outputTableName - the table to generate containing the results of the sort merge.
     * @throws SQLException if there are issues executing the queries.
     */
    public static void sort_merge(
        Connection conn,
        String table1,
        String table2,
        String outputTableName
    ) throws SQLException {
        logger.fine("\nGenerating false table by Subtraction using Sort_merge, cur_false_Table is: " + outputTableName);

        // Ensure that all the table names are escaped before attempting to execute any queries with them.
        table1 = "`" + table1 + "`";
        table2 = "`" + table2 + "`";
        outputTableName = "`" + outputTableName + "`";

        Statement st1 = conn.createStatement();

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
        st1.close();

        // Code for merging the two tables.
        String createQuery;
        if (orderList.size() > 0) {
            createQuery =
                "CREATE VIEW " + outputTableName + " AS " +
                QueryGenerator.createSubtractionQuery(table1, table2, "MULT", orderList);
        } else {
            // Aug 18, 2014 zqian: Handle the extreme case when there's only the `MULT` column.
            logger.fine("\n\tHandle the extreme case when there's only the `MULT` column.\n");
            createQuery =
                "CREATE VIEW " + outputTableName + " AS " +
                "SELECT (" + table1 + ".MULT - " + table2 + ".MULT) AS MULT " +
                "FROM " + table1 + ", " + table2 + ";";
        }

        logger.fine(createQuery);

        long time1 = System.currentTimeMillis();
        try (Statement st2 = conn.createStatement()) {
            st2.execute("DROP VIEW IF EXISTS " + outputTableName + ";");
            st2.execute(createQuery);
        }

        logger.fine("\ntotal time: " + (System.currentTimeMillis() - time1) + "\n");
    }
}