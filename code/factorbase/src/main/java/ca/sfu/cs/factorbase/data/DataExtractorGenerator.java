package ca.sfu.cs.factorbase.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import ca.sfu.cs.factorbase.database.FactorBaseDataBaseInfo;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.exporter.csvexporter.CSVPrecomputor;

/**
 * Class to generate CT table DataExtractor implementations.
 */
public class DataExtractorGenerator {
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private DataExtractorGenerator() {
    }


    /**
     * Generate TSVDataExtractors.
     *
     * @return TSV based implementations of the DataExtractor interface.
     * @throws DataExtractionException if there is an issue when generating the TSVDataExtractors.
     */
    public static Map<String, DataExtractor> generateTSVExtractors() throws DataExtractionException {
        try {
            return CSVPrecomputor.runCSV();
        } catch (SQLException e) {
            throw new DataExtractionException("Ran into a database issue when generating the TSVDataExtractors", e);
        } catch (IOException e) {
            throw new DataExtractionException("Ran into a file issue when generating the TSVDataExtractors", e);
        }
    }


    /**
     * Generate MySQL based implementations of the DataExtractor interface.
     *
     * @param dbConnection - connection to the MySQL database to generate the MySQLDataExtractors for.
     * @param dbInfo - database information related to FactorBase.
     * @return MySQL based implementations of the DataExtractor interface.
     * @throws DataExtractionException if there is an issue when generating the MySQLDataExtractors.
     */
    public static Map<String, DataExtractor> generateMySQLExtractors(Connection dbConnection, FactorBaseDataBaseInfo dbInfo) throws DataExtractionException {
        int maxLength;

        // Get max length for the RChains.
        try(Statement statement = dbConnection.createStatement()) {
            ResultSet results = statement.executeQuery("SELECT MAX(length) FROM " + dbInfo.getBNDatabaseName() + ".lattice_set;");
            results.first();
            maxLength = results.getInt(1);
        } catch (SQLException e) {
            throw new DataExtractionException("Ran into a database issue when generating the MySQLDataExtractors", e);
        }

        return createAndMapExtractors(dbConnection, dbInfo, maxLength);
    }


    /**
     * Generate MySQLDataExtractors and map pvar/rnode to the associated MySQLDataExtractor.
     *
     * @param dbConnection - connection to the MySQL database to generate the MySQLDataExtractors for.
     * @param dbInfo - database information related to FactorBase.
     * @param maxLength - the longest length to consider for RChains.
     * @return Map containing key:value pairs of pvar/rnode:MySQLDataExtractor.
     * @throws DataExtractionException if there is an issue when generating the MySQLDataExtractors.
     */
    private static Map<String, DataExtractor> createAndMapExtractors(
        Connection dbConnection,
        FactorBaseDataBaseInfo dbInfo,
        int maxLength
    ) throws DataExtractionException {
        Map<String, DataExtractor> dataExtractors = generatePVarDataExtractors(
            dbConnection,
            dbInfo
        );

        for (int length = 1; length <= maxLength; length++) {
            addRNodeDataExtractors(dbConnection, dbInfo, dataExtractors, length);
        }

        return dataExtractors;
    }


    /**
     * Generate MySQLDataExtractors for the pvars.
     *
     * @param dbConnection - connection to the MySQL database to generate the MySQLDataExtractors for.
     * @param dbInfo - database information related to FactorBase.
     * @return Map containing key:value pairs of pvar:MySQLDataExtractor.
     * @throws DataExtractionException if there is an issue when generating the MySQLDataExtractors.
     */
    private static Map<String, DataExtractor> generatePVarDataExtractors(
        Connection dbConnection,
        FactorBaseDataBaseInfo dbInfo
    ) throws DataExtractionException {
        Map<String, DataExtractor> dataExtractors = new HashMap<String, DataExtractor>();
        String pvid;
        String extractionQuery;
        PreparedStatement dbQuery;

        try(Statement statement = dbConnection.createStatement()) {
            // Retrieve main variables.
            ResultSet results = statement.executeQuery(
                "SELECT * " +
                "FROM " + dbInfo.getBNDatabaseName() + ".PVariables " +
                "WHERE index_number = 0;"
            );

            while(results.next()) {
                pvid = results.getString("pvid");
                extractionQuery = "SELECT * FROM " + dbInfo.getCTDatabaseName() + ".`" + pvid + "_counts` WHERE MULT > 0;";
                dbQuery = dbConnection.prepareStatement(extractionQuery);
                dataExtractors.put(pvid, new MySQLDataExtractor(dbQuery, dbInfo.getCountColumnName(), dbInfo.isDiscrete()));
            }
        } catch (SQLException e) {
            throw new DataExtractionException("Ran into a database issue when generating the MySQLDataExtractors", e);
        }

        return dataExtractors;
    }


    /**
     * Generate MySQLDataExtractors for the rnodes and add them to the given Map object.
     *
     * @param dbConnection - connection to the MySQL database to generate the MySQLDataExtractors for.
     * @param dbInfo - database information related to FactorBase.
     * @param dataExtractors - Map containing key:value pairs of pvar:MySQLDataExtractor and will have
     *                         MySQLDataExtractors added to it for rnodes.
     * @param length - the length to consider for the RChain.
     * @throws DataExtractionException if there is an issue when generating the MySQLDataExtractors.
     */
    private static void addRNodeDataExtractors(
        Connection dbConnection,
        FactorBaseDataBaseInfo dbInfo,
        Map<String, DataExtractor> dataExtractors,
        int length
    ) throws DataExtractionException {
        String rchain;
        String shortRchain;
        String extractionQuery;
        PreparedStatement dbQuery;
        try(Statement statement = dbConnection.createStatement()) {
            ResultSet results = statement.executeQuery(
                "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
                "FROM " + dbInfo.getBNDatabaseName() + ".lattice_set " +
                "JOIN " + dbInfo.getBNDatabaseName() + ".lattice_mapping " +
                "ON lattice_set.name = lattice_mapping.orig_rnid " +
                "WHERE lattice_set.length = " + length + ";"
            );

            while(results.next()) {
                rchain = results.getString("RChain");
                shortRchain = results.getString("short_RChain");
                extractionQuery = "SELECT * FROM " + dbInfo.getCTDatabaseName() + ".`" + shortRchain + "_CT` WHERE MULT > 0;";
                dbQuery = dbConnection.prepareStatement(extractionQuery);
                dataExtractors.put(rchain, new MySQLDataExtractor(dbQuery, dbInfo.getCountColumnName(), dbInfo.isDiscrete()));
            }
        } catch(SQLException e) {
            throw new DataExtractionException("Ran into a database issue when generating the MySQLDataExtractors", e);
        }
    }
}