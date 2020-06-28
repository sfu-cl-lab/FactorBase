package ca.sfu.cs.componentsrunner.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.MySQLDataExtractor;
import ca.sfu.cs.factorbase.data.TSVDataExtractor;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.util.Sort_merge3;


public class RunComponent {
    private static final String CONNECTION_STRING = "jdbc:{0}/{1}?{2}";

    /**
     * Helper method to wrap the given string with backticks so that the SQL queries are valid.
     * @return table name wrapped with backticks.
     */
    private static String escapeName(String tableName) {
        return "`" + tableName + "`";
    }


    public static void main(String[] args) throws SQLException, IOException, DataExtractionException {
        Config config = new Config();
        String connectionString = MessageFormat.format(
            CONNECTION_STRING,
            config.getProperty("dbaddress"),
            config.getProperty("dbname"),
            "serverTimezone=PST"
        );

        Connection dbConnection = (Connection) DriverManager.getConnection(
            connectionString,
            config.getProperty("dbusername"),
            config.getProperty("dbpassword")
        );

        String component = config.getProperty("component");

        long startTime = System.currentTimeMillis();
        if (component.equals("SortMerge")) {
            System.out.println("Starting Sort Merge!");
            String falseTableSubQuery = Sort_merge3.sort_merge(
                dbConnection,
                config.getProperty("dbname"),
                config.getProperty("SortMergeCartesianTableSubQuery"),
                config.getProperty("SortMergeSubsetTable")
            );
            try (Statement statement = dbConnection.createStatement()) {
                statement.executeUpdate(
                    "CREATE TABLE " + config.getProperty("SortMergeOutputTable") + " AS " +
                    falseTableSubQuery
                );
            }
        } else if (component.equals("DataExtraction")) {
            System.out.println("Starting Data Extraction");
            DataExtractor dataextractor = null;

            String extractorType = config.getProperty("ExtractorType");
            if (extractorType.equals("MYSQL")) {
                PreparedStatement dbQuery = dbConnection.prepareStatement(
                    "SELECT * FROM " + escapeName(config.getProperty("CountsTable")) + " " +
                    "WHERE MULT > 0;"
                );

                dataextractor = new MySQLDataExtractor(
                    dbQuery,
                    config.getProperty("CountsColumn"),
                    Boolean.valueOf(config.getProperty("IsDiscrete"))
                );
            } else if (extractorType.equals("TSV")) {
                dataextractor = new TSVDataExtractor(
                    config.getProperty("TSVFile"),
                    config.getProperty("CountsColumn"),
                    Boolean.valueOf(config.getProperty("IsDiscrete"))
                );
            } else {
                System.out.println("Unsupported extractor type specified, given: " + extractorType);
                System.exit(1);
            }

            dataextractor.extractData();
        } else {
            System.out.println("Unsupported component specified, given: " + component);
            System.exit(1);
        }

        System.out.println("The run took: " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
