package ca.sfu.cs.componentsrunner.app;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;

import com.mysql.jdbc.Connection;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.util.Sort_merge3;


public class RunComponent {
    private static final String CONNECTION_STRING = "jdbc:{0}/{1}";

    /**
     * Helper method to wrap the given string with backticks so that the SQL queries are valid.
     * @return table name wrapped with backticks.
     */
    private static String escapeName(String tableName) {
        return "`" + tableName + "`";
    }


    public static void main(String[] args) throws SQLException, IOException {
        Config config = new Config();
        String connectionString = MessageFormat.format(
            CONNECTION_STRING,
            config.getProperty("dbaddress"),
            config.getProperty("dbname")
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
            Sort_merge3.sort_merge(
                escapeName(config.getProperty("SortMergeCartesianTable")),
                escapeName(config.getProperty("SortMergeSubsetTable")),
                escapeName(config.getProperty("SortMergeOutputTable")),
                dbConnection
            );
        } else {
            System.out.println("Unsupported component specified, given: " + component);
            System.exit(1);
        }

        System.out.println("The run took: " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
