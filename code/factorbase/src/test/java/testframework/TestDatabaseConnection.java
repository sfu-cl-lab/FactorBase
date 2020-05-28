package testframework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;

/**
 * Class to connect to a MySQL database so that queries can be tested.
 */
public class TestDatabaseConnection {
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String MYSQL_URL = "jdbc:mysql://127.0.0.1/tests-database";

    public Connection con;

    public TestDatabaseConnection() {
        String userName = DEFAULT_USERNAME;
        String password = DEFAULT_PASSWORD;

        String testUserName = System.getProperty("testDBUserName");
        if (testUserName != null) {
            userName = testUserName;
        }

        String testPassword = System.getProperty("testDBPassword");
        if (testPassword != null) {
            password = testPassword;
        }

        Properties connectionProperties = MySQLFactorBaseDataBase.getConnectionStringProperties(userName, password);
        try {
            con = DriverManager.getConnection(MYSQL_URL, connectionProperties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
