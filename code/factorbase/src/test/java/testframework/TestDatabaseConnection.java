package testframework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        try {
            con = (Connection) DriverManager.getConnection(MYSQL_URL, userName, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
