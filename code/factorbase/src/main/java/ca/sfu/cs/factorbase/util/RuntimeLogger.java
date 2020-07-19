package ca.sfu.cs.factorbase.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;

/**
 * Class to help log information for a FactorBase run.
 */
public final class RuntimeLogger {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private RuntimeLogger() {
    }


    private static final String CALL_LOGS = "CallLogs";
    private static int callCount = 0;
    private static String dbName;


    /**
     * Helper method to write out the run times for high level components in a consistent format.
     *
     * @param logger - the logger to write the runtime to.
     * @param stage - the part of the FactorBase program that was run.
     * @param start - the start time for the given stage (ms).
     * @param end - the end time for the given stage (ms).
     */
    public static void logRunTime(Logger logger, String stage, long start, long end) {
        logger.info("Runtime[" + stage + "]: " + String.valueOf(end - start) + "ms.");
    }


    /**
     * Helper method to write out the run times for components of high level components in a consistent format.
     *
     * @param logger - the logger to write the runtime to.
     * @param subStage - the subcomponent of the part of the FactorBase program that was run.
     * @param start - the start time for the given stage (ms).
     * @param end - the end time for the given stage (ms).
     */
    public static void logRunTimeDetails(Logger logger, String stage, long start, long end) {
        logger.fine("  Runtime[" + stage + "]: " + String.valueOf(end - start) + "ms.");
    }


    /**
     * Create the "CallLogs" table within the specified database.
     *
     * @param dbConnection - connection to the database server to create the "CallLogs" table in.
     * @param baseDatabaseName - the name of the input database to FactorBase.
     * @param loggingTableDatabaseName - the name of the database to create the "CallLogs" table in.
     * @throws SQLException if there is an issue creating the "CallLogs" table in the specified database.
     * @throws IOException if there is an issue reading the logging script.
     */
    public static void setupLoggingTable(
        Connection dbConnection,
        String baseDatabaseName,
        String loggingTableDatabaseName
    ) throws SQLException, IOException {
        dbName = loggingTableDatabaseName;
        MySQLScriptRunner.runScript(
            dbConnection,
            Config.SCRIPTS_DIRECTORY + "logging.sql",
            baseDatabaseName
        );
    }


    /**
     * Add a new log entry to the "CallLogs" table.
     *
     * @param dbConnection - connection to the database server containing the "CallLogs" table.
     * @throws SQLException if there is an issue adding a new log entry.
     */
    public static void addLogEntry(Connection dbConnection) throws SQLException {
        callCount++;
        try (Statement st = dbConnection.createStatement()) {
            st.executeUpdate(
                "INSERT INTO " + dbName + "." + CALL_LOGS + " " +
                    "(CallNumber) " +
                "VALUES " +
                    "('" + callCount + "')"
            );
        }
    }


    /**
     * Update the log entry that was created from the last call to {@link RuntimeLogger#addLogEntry(Connection)}.
     *
     * @param dbConnection - connection to the database server containing the "CallLogs" table.
     * @param columnName - the name of the column to update in the "CallLogs" table.
     * @param runtime - the runtime to write in the specified column.
     * @throws SQLException if there is an issue updating the log entry.
     */
    public static void updateLogEntry(Connection dbConnection, String columnName, long runtime) throws SQLException {
        try (Statement st = dbConnection.createStatement()) {
            st.executeUpdate(
                "UPDATE " + dbName + "." + CALL_LOGS + " " +
                "SET " +
                    columnName + " = " + runtime + " " +
                "WHERE " +
                    "CallNumber = " + callCount
            );
        }
    }


    /**
     * Add the given runtime to the column specified for the log entry that was created from the last call to
     * {@link RuntimeLogger#addLogEntry(Connection)}.
     *
     * @param dbConnection - connection to the database server containing the "CallLogs" table.
     * @param columnname - the name of the column to adjust in the "CallLogs" table.
     * @param runtime - the runtime to add (positive value) in the specified column or to subtract (negative value) in
     *                  the specified column.
     * @throws SQLException if there is an issue adjusting the log entry.
     */
    public static void adjustLogEntryValue(
        Connection dbConnection,
        String columnName,
        long runtime
    ) throws SQLException {
        try (Statement st = dbConnection.createStatement()) {
            st.executeUpdate(
                "UPDATE " + dbName + "." + CALL_LOGS + " " +
                "SET " +
                    columnName + " = IFNULL(" + columnName + ", 0) + " + runtime + " " +
                "WHERE " +
                    "CallNumber = " + callCount
            );
        }
    }
}