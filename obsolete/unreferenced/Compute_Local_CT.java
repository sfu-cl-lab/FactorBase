import java.sql.Connection;
import java.sql.DriverManager;
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

	/*
	public static void KeepTablesOnly(Connection con,String dbname,String tablename) throws SQLException {
        //drop temporary CT tables
		//1.0 keep one table 
        String CONN_tmp = "jdbc:" + dbaddress + "/" + dbname;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }

        con = (Connection) DriverManager.getConnection(CONN_tmp, dbUsername, dbPassword);
        Statement st = con.createStatement();

		//select the delete tablenames,return a strinng
        st.execute("select concat('drop table ',table_name,';') as result FROM information_schema.tables where table_schema = " +dbname+ " and table_name != " +tablename+ ";" );  

		Statement tmp = con.createStatement();
		for(int i = 0; i< result.length();i++){
			system.out.print(result[i]+"\n");
			//tmp.execute(result[i]);
		}


	}*/
 
}
 

