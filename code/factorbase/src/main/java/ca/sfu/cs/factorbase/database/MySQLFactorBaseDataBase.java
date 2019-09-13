package ca.sfu.cs.factorbase.database;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.ContingencyTable;
import ca.sfu.cs.factorbase.data.ContingencyTableGenerator;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.DataExtractorGenerator;
import ca.sfu.cs.factorbase.data.FunctorNode;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.data.MySQLDataExtractor;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.graph.Edge;
import ca.sfu.cs.factorbase.learning.BayesBaseCT_SortMerge;
import ca.sfu.cs.factorbase.util.KeepTablesOnly;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;
import ca.sfu.cs.factorbase.util.QueryGenerator;

import com.mysql.jdbc.Connection;

public class MySQLFactorBaseDataBase implements FactorBaseDataBase {

    private static final String CONNECTION_STRING = "jdbc:{0}/{1}";
    private String baseDatabaseName;
    private Connection dbConnection;
    private FactorBaseDataBaseInfo dbInfo;
    private Map<String, DataExtractor> dataExtractors;


    /**
     * Create a connection to the database server required by FactorBase to learn a Bayesian Network.
     *
     * @param dbInfo - database information related to FactorBase.
     * @param dbaddress - the address of the MySQL database server to connect to. e.g. mysql://127.0.0.1
     * @param dbname - the name of the database with the original data. e.g. unielwin
     * @param username - the username to use when accessing the database.
     * @param password - the password to use when accessing the database.
     * @throws SQLException if there is a problem connecting to the required database.
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
            this.dbConnection = (Connection) DriverManager.getConnection(baseConnectionString, username, password);
        } catch (SQLException e) {
            throw new DataBaseException("Unable to connect to the provided database.", e);
        }
    }


    @Override
    public void setupDatabase() throws DataBaseException {
        try {
            MySQLScriptRunner.runScript(
                this.dbConnection,
                Config.SCRIPTS_DIRECTORY + "initialize_databases.sql",
                this.baseDatabaseName
            );
            this.dbConnection.setCatalog(this.dbInfo.getSetupDatabaseName());
            MySQLScriptRunner.runScript(
                this.dbConnection,
                Config.SCRIPTS_DIRECTORY + "metadata.sql",
                this.baseDatabaseName
            );
            MySQLScriptRunner.runScript(
                this.dbConnection,
                Config.SCRIPTS_DIRECTORY + "metadata_storedprocedures.sql",
                this.baseDatabaseName,
                "//"
            );
            MySQLScriptRunner.callSP(this.dbConnection, "find_values");
        } catch (SQLException | IOException e) {
            throw new DataBaseException("An error occurred when attempting to setup the database for FactorBase.", e);
        }
    }


    @Override
    public void cleanupDatabase() throws DataBaseException {
        try {
            KeepTablesOnly.Drop_tmpTables(
                this.dbConnection,
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
            PreparedStatement statement = this.dbConnection.clientPrepareStatement(query);
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

        try (PreparedStatement st = this.dbConnection.prepareStatement(query)) {
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

        try (PreparedStatement st = this.dbConnection.prepareStatement(query)) {
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
            this.dbConnection,
            this.dbInfo
        );
    }


    @Override
    public List<FunctorNodesInfo> getPVariablesFunctorNodeInfo() throws DataBaseException {
        String query =
            "SELECT P.pvid, N.1nid, A.Value " +
            "FROM " +
                this.baseDatabaseName + "_setup.PVariables P," +
                this.baseDatabaseName + "_setup.1Nodes N, " +
                this.baseDatabaseName + "_setup.Attribute_Value A " +
            "WHERE P.pvid = N.pvid " +
            "AND N.COLUMN_NAME = A.COLUMN_NAME";

        List<FunctorNodesInfo> functorInfos = new ArrayList<FunctorNodesInfo>();
        String previousID = null;
        String previousFunctorID = null;
        try (
            Statement statement = this.dbConnection.createStatement();
            ResultSet results = statement.executeQuery(query)
        ) {
            FunctorNodesInfo info = null;
            FunctorNode functor = null;
            while (results.next()) {
                String currentID = results.getString("pvid");
                String currentFunctorID = results.getString("1nid");
                if (!currentID.equals(previousID)) {
                    info = new FunctorNodesInfo(currentID, this.dbInfo.isDiscrete());
                    functorInfos.add(info);
                    previousID = currentID;
                }

                if (!currentFunctorID.equals(previousFunctorID)) {
                    functor = new FunctorNode(currentFunctorID);
                    info.addFunctorNode(functor);
                    previousFunctorID = currentFunctorID;
                }

                functor.addState(results.getString("Value"));
            }
        } catch (SQLException e) {
            throw new DataBaseException("Failed to retrieve the functors for the PVariables.", e);
        }

        return functorInfos;
    }


    @Override
    public ContingencyTable getContingencyTable(
        FunctorNodesInfo functorInfos,
        String child,
        Set<String> parents,
        int totalNumberOfStates
    ) throws DataBaseException {
        try {
            this.dbConnection.setCatalog(this.dbInfo.getSetupDatabaseName());
            try (Statement statement = this.dbConnection.createStatement()) {
                // Initialize FunctorSet table.
                statement.executeUpdate(QueryGenerator.createTruncateQuery("FunctorSet"));
                statement.executeUpdate(QueryGenerator.createSimpleExtendedInsertQuery("FunctorSet", child, parents));
            }

            // Generate CT tables.
            // TODO: Figure out best way to reuse substituted file instead of recreating a new one each time.
            BayesBaseCT_SortMerge.buildCT();

            PreparedStatement query = this.dbConnection.prepareStatement(
                "SELECT * FROM " + dbInfo.getCTDatabaseName() + ".`" + functorInfos.getID() + "_counts` WHERE MULT > 0;"
            );

            DataExtractor dataextractor = new MySQLDataExtractor(query, dbInfo.getCountColumnName(), dbInfo.isDiscrete());
            ContingencyTableGenerator ctGenerator = new ContingencyTableGenerator(dataextractor);
            int childColumnIndex = ctGenerator.getColumnIndex(child);
            int[] parentColumnIndices = ctGenerator.getColumnIndices(parents);
            return ctGenerator.generateCT(childColumnIndex, parentColumnIndices, totalNumberOfStates);
        } catch (DataExtractionException | IOException | SQLException e) {
            throw new DataBaseException("Failed to generate the CT table.", e);
        }
    }
}