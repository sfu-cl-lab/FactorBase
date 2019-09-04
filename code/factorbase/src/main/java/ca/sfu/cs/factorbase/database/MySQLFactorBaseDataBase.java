package ca.sfu.cs.factorbase.database;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.DataExtractorGenerator;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.graph.Edge;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;
import ca.sfu.cs.factorbase.util.KeepTablesOnly;
import ca.sfu.cs.factorbase.util.QueryGenerator;

import com.mysql.jdbc.Connection;

public class MySQLFactorBaseDataBase implements FactorBaseDataBase {

    private static final String CONNECTION_STRING = "jdbc:{0}/{1}";
    private String baseDatabaseName;
    private Connection baseConnection;
    private FactorBaseDataBaseInfo dbInfo;
    private Map<String, DataExtractor> dataExtractors;


    /**
     * Create connections to the databases required by FactorBase to learn a Bayesian Network.
     *
     * @param dbInfo - database information related to FactorBase.
     * @param dbaddress - the address of the MySQL database to connect to. e.g. mysql://127.0.0.1
     * @param dbname - the name of the database with the original data. e.g. unielwin
     * @param username - the username to use when accessing the database.
     * @param password - the password to use when accessing the database.
     * @throws SQLException if there is a problem connecting to the required databases.
     */
    public MySQLFactorBaseDataBase(
        FactorBaseDataBaseInfo dbInfo,
        String dbaddress,
        String dbname,
        String username,
        String password
    ) throws DataBaseException {
        this.dbInfo = dbInfo;
        this.baseDatabaseName = dbname;
        String baseConnectionString = MessageFormat.format(CONNECTION_STRING, dbaddress, dbname);

        try {
            this.baseConnection = (Connection) DriverManager.getConnection(baseConnectionString, username, password);
        } catch (SQLException e) {
            throw new DataBaseException("Unable to connect to the provided database.", e);
        }
    }


    @Override
    public void setupDatabase() throws DataBaseException {
        try {
            MySQLScriptRunner.runScript(
                this.baseConnection,
                Config.SCRIPTS_DIRECTORY + "metadata.sql",
                this.baseDatabaseName
            );
            MySQLScriptRunner.runScript(
                this.baseConnection,
                Config.SCRIPTS_DIRECTORY + "metadata_storedprocedures.sql",
                this.baseDatabaseName,
                "//"
            );
            MySQLScriptRunner.callSP(this.baseConnection, "find_values");
        } catch (SQLException | IOException e) {
            throw new DataBaseException("An error occurred when attempting to setup the database for FactorBase.", e);
        }
    }


    @Override
    public void cleanupDatabase() throws DataBaseException {
        try {
            KeepTablesOnly.Drop_tmpTables(
                this.baseConnection,
                this.dbInfo.getCTDatabaseName(),
                this.dbInfo.getBNDatabaseName()
            );
        } catch (SQLException e) {
            throw new DataBaseException("An error occurred when attempting to cleanup the database for FactorBase.", e);
        }
    }


    @Override
    public String[] getPVariables() throws DataBaseException {
        String query =
            "SELECT pvid " +
            "FROM " + this.baseDatabaseName + "_setup.PVariables " +
            "WHERE index_number = 0;"; // O.S. March 21 ignore variables that aren't main.

        try (
            PreparedStatement statement = this.baseConnection.clientPrepareStatement(query);
            ResultSet results = statement.executeQuery()
        ) {
            int size = 0;
            if (results.last()) {
                size = results.getRow();
                results.beforeFirst();
            }

            String[] pvariables = new String[size];

            int insertIndex = 0;
            while (results.next()) {
                pvariables[insertIndex] = results.getString("pvid");
                insertIndex++;
            }

            return pvariables;
        } catch (SQLException e) {
            throw new DataBaseException(
                "An error occurred when attempting to retrieve the PVariables from the database for FactorBase.",
                e
            );
        }
    }


    /**
     * Helper method to extract the edges from the given PreparedStatement.
     * @param statement - the PreparedStatement to extract the edge information from.
     * @return a List of the extracted edges.
     * @throws SQLException if an error occurs when attempting to retrieve the information.
     */
    private List<Edge> extractEdges(PreparedStatement statement) throws SQLException {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        ResultSet results = statement.executeQuery();

        while (results.next()) {
            // Remove the backticks when creating edges so that they match the names in the CSV
            // file that gets generated.
            edges.add(
                new Edge(
                    results.getString("parent").replace("`", ""),
                    results.getString("child").replace("`", "")
                )
            );
        }

        return edges;
    }


    @Override
    public List<Edge> getForbiddenEdges(List<String> rnodeIDs) throws DataBaseException {
        String query = QueryGenerator.createSimpleInQuery(
            this.baseDatabaseName + "_BN.Path_Forbidden_Edges",
            "RChain",
            rnodeIDs
        );

        try (PreparedStatement st = this.baseConnection.prepareStatement(query)) {
            return extractEdges(st);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to retrieve the forbidden edges.", e);
        }
    }


    @Override
    public List<Edge> getRequiredEdges(List<String> rnodeIDs) throws DataBaseException {
        String query = QueryGenerator.createSimpleInQuery(
            this.baseDatabaseName + "_BN.Path_Required_Edges",
            "RChain",
            rnodeIDs
        );

        try (PreparedStatement st = this.baseConnection.prepareStatement(query)) {
            return extractEdges(st);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to retrieve the required edges.", e);
        }
    }


    @Override
    public DataExtractor getAndRemoveCTDataExtractor(String dataExtractorID) throws DataExtractionException {
        if (this.dataExtractors == null) {
            this.dataExtractors = this.generateDataExtractors();
        }

        return this.dataExtractors.remove(dataExtractorID);
    }


    /**
     * Generate all the CT table {@code DataExtractor}s of the FactorBase database.
     *
     * @return a Map containing key:value pairs of dataExtractorID:DataExtractor.
     * @throws DataExtractionException if an error occurs when retrieving the DataExtractors.
     */
    private Map<String, DataExtractor> generateDataExtractors() throws DataExtractionException {
        return DataExtractorGenerator.generateMySQLExtractors(
            this.baseConnection,
            this.dbInfo
        );
    }
}