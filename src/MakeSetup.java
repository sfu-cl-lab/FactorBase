/*analyze schema data to create setup database. This can be edited by the user before learning.
  If setup = 0, we skip this step and use the existing setup database
  Yan Sept 10th*/


//Assumption: 
  // No suffix or *_std refers to the original database provided to FactorBase
  // *_setup is the setup database



import java.sql.DriverManager;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;


public class MakeSetup {

	static Connection con;

	//  to be read from config.cfg.
	// The config.cfg file should  be the working directory.
	static String databaseName, databaseName_setup;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

	public static void main(String args[]) throws Exception {
		try{
			runMS();
		}
		catch(Exception e){
			System.out.println("ERROR in setup");
		}

	}
	
	public static void runMS() throws Exception {
		setVarsFromConfig();
		connectDB();
		//analyze schema data to create setup database. This can be edited by the user before learning.
		//If setup = 0, we skip this step and use the existing setup database
		


		try{
			BZScriptRunner bzsr = new BZScriptRunner(databaseName,con);

		  bzsr.runScript("scripts/setup.sql");  
		  bzsr.createSP("scripts/storedprocs.sql");
        bzsr.callSP("find_values");
	    }
	    catch(Exception e){
	    	System.out.println("\n Delete the setup and restart!" + e);
	    }

        
		disconnectDB();
	}
	
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		databaseName_setup = databaseName + "_setup";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	public static void connectDB() throws SQLException {
		//open database connections to the original database
		String CONN_STR = "jdbc:" + dbaddress + "/" + databaseName;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
	}
	
	public static void disconnectDB() throws SQLException {
		con.close();
	}
}
