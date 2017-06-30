//package relationalClassification;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

public class RelationalClassification {

    private static Connection con_std;
    private static Connection con_BN;
    private static Connection con_CT;
    private static Connection con_setup;
    private static String databaseName_std;
    private static String databaseName_BN;
    private static String databaseName_CT;
    private static String databaseName_setup;
    private static String dbbase;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbaddress;
    private static String linkCorrelation;
    private static String continuous;
    private static String functorid;

    public static void main(String args[]) throws Exception {

        //computeRelationalClassification();
        computeGroundedCT();
    }

    public static void computeGroundedCT() throws Exception{
        setVarsFromConfig();
        System.out.println("Set variables");

        //take the target instance
        connectDB();
        connectDB1();
        System.out.println("DBs connected");

        //create a new setup copy corresponding to Markov Chain of the targetNode

		System.out.println("@RelaClas: con_std: " + con_std);
				
        BayesBaseCT_SortMerge.buildCT(con_std, con_BN, con_CT, con_setup, databaseName_std, linkCorrelation, continuous);
    }

     /**
     * Connects to database via MySQL JDBC driver
     * @throws SQLException
     */
    public static void setVarsFromConfig(){
        Config conf = new Config();
        databaseName_std = conf.getProperty("dbname");
        //dbbase = conf.getProperty("dbbase");
        databaseName_BN = databaseName_std + "_BN";
        databaseName_CT = databaseName_std + "_CT";
        databaseName_setup = databaseName_std + "_setup";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        linkCorrelation = conf.getProperty("LinkCorrelations");
        continuous = conf.getProperty("Continuous");
        //ToDo: Move to separate config??
        //functorid = conf.getProperty("functorid");
    }

    public static boolean connectDB(Connection con, String database) throws Exception{

        String CONN_STR= "jdbc:" + dbaddress + "/" + database;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        try{
        	
            con = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
            System.out.println("@RelaClas: ConnectDB: "+ database + con_std);
        }
        catch(Exception e){
            System.out.println("Could not conenct to the Database " + database);
        }
        return true;
    }
    /**
     * Connects to database via MySQL JDBC driver
     * @throws SQLException
     */
	 /**
     * Connects to database via MySQL JDBC driver
     * @throws SQLException
     */
	public static void connectDB() throws SQLException {
		String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName_std;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con_std = (Connection) DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
		
		String CONN_STR4 = "jdbc:" + dbaddress + "/" + databaseName_setup;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con_setup = (Connection) DriverManager.getConnection(CONN_STR4, dbUsername, dbPassword);
	}

    /**
     * Connects to <databaseName>_BN (Bayesian Network)
     * @throws SQLException
     */
	public static void connectDB1() throws SQLException {
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName_BN;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con_BN = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
		
		String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName_CT;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con_CT = (Connection) DriverManager.getConnection(CONN_STR3, dbUsername, dbPassword);
	}


}
