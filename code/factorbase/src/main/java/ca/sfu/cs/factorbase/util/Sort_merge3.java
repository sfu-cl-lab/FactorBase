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
     * Subtract the MULT column of the rows in {@code table1Subquery} by the MULT column of the matching row in
     * {@code table2}.
     *
     * @param conn - connection to the database containing {@code table1} and {@code table2}.
     * @param sourceDatabaseName - the name of the database containing {@code table1} and {@code table2}.
     * @param table1Subquery - a subquery that generates a table to have its values in the MULT column subtracted by
     *                         the MULT column in {@code table2}.
     * @param table2 - the table to match rows with in {@code table1} and subtract by the values found in the
     *                 MULT column.
     * @return an SQL query that applies the sort merge algorithm to the two specified tables.
     * @throws SQLException if there are issues executing the queries.
     */
    public static String sort_merge(
        Connection conn,
        String sourceDatabaseName,
        String table1Subquery,
        String table2
    ) throws SQLException {
        // Ensure that all the table names are escaped before attempting to execute any queries with them.
        table2 = sourceDatabaseName + ".`" + table2 + "`";

        Statement st1 = conn.createStatement();

        ArrayList<String> joinOnList = new ArrayList<String>();

        /**
         * Get the JOIN ON columns using TABLE2.
         */
        ResultSet rst = st1.executeQuery(
            "SHOW COLUMNS FROM " + table2 +
            "WHERE field <> \"MULT\";"
        );

        while(rst.next()) {
            joinOnList.add(rst.getString(1));
        }
        rst.close();
        st1.close();

        String selectQuery;
        if (joinOnList.size() > 0) {
            selectQuery = QueryGenerator.createSubtractionQuery(table1Subquery, table2, "MULT", joinOnList);
        } else {
            // Aug 18, 2014 zqian: Handle the extreme case when there's only the `MULT` column.
            selectQuery =
                "SELECT (SUBQUERY.MULT - " + table2 + ".MULT) AS MULT " +
                "FROM (" + table1Subquery + ") AS SUBQUERY, " + table2;
        }

        return selectQuery;
    }
}