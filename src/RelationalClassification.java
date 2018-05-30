//package relationalClassification;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);
        con_BN = connectDB(databaseName_BN);
        con_CT = connectDB(databaseName_CT);
        System.out.println("DBs connected");

        //create a new setup copy corresponding to Markov Chain of the targetNode
				
        BayesBaseCT_SortMerge.buildCT(con_std, con_setup, con_BN, con_CT, databaseName_std, linkCorrelation, continuous);
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

    public static Connection connectDB(String database) throws Exception{

        String CONN_STR= "jdbc:" + dbaddress + "/" + database;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        try{
        	
            return ((Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword));
            
        }
        catch(Exception e){
            System.out.println("Could not conenct to the Database " + database);
        }
        return null;   
    }
}
