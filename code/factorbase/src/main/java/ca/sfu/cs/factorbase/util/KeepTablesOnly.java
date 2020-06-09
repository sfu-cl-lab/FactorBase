package ca.sfu.cs.factorbase.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

public class KeepTablesOnly {
    private static Logger logger = Logger.getLogger(KeepTablesOnly.class.getName());


    /**
     * Keeping only the longest RChain table in the CT database and deleting the remaining intermediaries.
     * Subject to change as per need for tables in the CT database.
     *
     * @param dbConnection - a database connection to the tables containing information to
     *            determine the longest RChain.
     * @param latticeDatabase - the name of the database found in the given {@code dbConnection}
     *            that contains the lattice_mapping and lattice_set tables.
     *
     * @throws SQLException if an error occurs when accessing the database.
     */
    private static ArrayList<String> findLongestRChain(Connection dbConnection, String latticeDatabase) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rst = st.executeQuery(
            MessageFormat.format(
                "SELECT short_rnid AS name " +
                "FROM {0}.lattice_set " +
                "JOIN {0}.lattice_mapping " +
                "ON {0}.lattice_set.name = {0}.lattice_mapping.orig_rnid " +
                "WHERE {0}.lattice_set.length = (" +
                    "SELECT MAX(length) " +
                    "FROM {0}.lattice_set" +
                ");",
                latticeDatabase
            )
        );

        ArrayList<String> sets = new ArrayList<String>();
        while(rst.next()) {
            logger.fine(rst.getString("name"));
            String tables = rst.getString("name");
            sets.add(tables + "_CT");
        }

        return sets;
    }


    /**
     * Keeps the tables in the given ArrayList and drops the others.
     *
     * @param con - a connection to the FactorBase database to remove the temporary tables from.
     * @param dbname - the name of the database to remove the temporary tables from.
     * @param tablenames - the names of the tables to save; i.e. these tables won't be dropped.
     *
     * @throws SQLException if an error occurs when removing the temporary tables.
     */
    private static void Drop_tmpTables(Connection con, String dbname, ArrayList<String> tablenames) throws SQLException {
        try (Statement statement = con.createStatement()) {
            ArrayList<String> dropQueries = new ArrayList<String>();

            String dropTableGeneratorQuery = generateDropQuery(dbname, tablenames, false);
            logger.fine(dropTableGeneratorQuery);
            try (ResultSet queries = statement.executeQuery(dropTableGeneratorQuery)) {
                while(queries.next()) {
                    dropQueries.add(queries.getString("dropTableQuery"));
                }
            }

            String dropViewGeneratorQuery = generateDropQuery(dbname, tablenames, true);
            logger.fine(dropViewGeneratorQuery);
            try (ResultSet queries = statement.executeQuery(dropViewGeneratorQuery)) {
                while(queries.next()) {
                    dropQueries.add(queries.getString("dropTableQuery"));
                }
            }

            for(String query : dropQueries) {
                statement.executeUpdate(query);
                logger.fine(query + " OK!");
            }
        }
    }


    /**
     * Generate an SQL String for generating drop table/view queries.
     *
     * @param dbname - the database containing the tables/views to drop.
     * @param tablenames - the tables that shouldn't be dropped.
     * @param droppingViews - true if views are being dropped; otherwise false.
     * @return an SQL String for generating drop table/view queries.
     */
    private static String generateDropQuery(String dbname, ArrayList<String> tablenames, boolean droppingViews) {
        String operator = "<>";
        String tableType = "TABLE";
        if (droppingViews) {
            operator = "=";
            tableType = "VIEW";
        }

        String NewSQL = MessageFormat.format(
            "SELECT CONCAT(''DROP {1} {0}.`'', table_name, ''`;'') AS dropTableQuery " +
            "FROM information_schema.tables " +
            "WHERE TABLE_TYPE {2} ''VIEW'' " +
            "AND table_schema = ''{0}",
            dbname,
            tableType,
            operator
        );

        for(int i = 0; i < tablenames.size(); i++) {
            NewSQL = NewSQL + "' AND table_name != '" + (String) tablenames.get(i);
        }

        NewSQL += "';";

        return NewSQL;
    }


    /**
     * Drop all the CT tables from the given database except for the one for the longest RChain.
     *
     * @param dbConnection - a connection to the database that will have its CT tables dropped.
     * @param databaseName - the name of the database to remove the temporary tables from.
     * @throws SQLException if an error occurs when removing the temporary tables.
     */
    public static void Drop_tmpTables(Connection dbConnection, String databaseName, String latticeDatabase) throws SQLException {
        // TODO: Refactor the findLongestRChain method into a Util Class and pass in an ArrayList of
        // tables to keep instead of the database containing the tables we need to execute the query.
        ArrayList<String> tablenames = findLongestRChain(dbConnection, latticeDatabase);
        Drop_tmpTables(dbConnection, databaseName, tablenames);
    }
}