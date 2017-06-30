import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Compute_Local_CT {
	static String databaseName;
	static Connection con4;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String databaseName1="PPLRuleDetectionJan23Movies";
	static String databaseName2="PPLRuleDetectionJan23Movies";
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	public static void connectDB1() throws SQLException {

		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName1;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con4 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}
	
	public static void connectDB2() throws SQLException {

		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con4 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}
}
 

