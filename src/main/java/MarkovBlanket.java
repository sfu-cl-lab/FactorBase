/*
 * Author: Kurt Routley
 * Date: September 23, 2013
 * 
 * MarkovBlanket.java
 *  	- Adds TargetChildren, TargetParents, TargetChildrensParents, and 
 *  	  TargetMB to @database@_BN
 */

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

;

public class MarkovBlanket {

	static Connection con1;

	//  to be read from config.cfg.
	// The config.cfg file should  be the working directory.
	static String databaseName, databaseName0;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	
	public static void main(String[] args) throws Exception
	{
		/*
		 *  Create Markov Blankets
		 */
		runMakeMarkovBlanket();
	}
	
	public static void runMakeMarkovBlanket() throws Exception
	{
		long t1 = System.currentTimeMillis(); 
		System.out.println( "Constructing Markov Blanket..." );
		
		setVarsFromConfig();
		connectDB();
		
		/*
		 * Get the largest Rchain
		 */
		String rchain = getLargestRchain();
		
		if ( null == rchain )
		{
			System.out.println( "Failed to get largest rchain." );
			disconnectDB();
			return;
		}
		
		BZScriptRunner bzsr = new BZScriptRunner( databaseName, con1, rchain );  // unielwin_BN
		bzsr.runScript("src/scripts/markov_blanket.sql");  
        
		disconnectDB();

		long t2 = System.currentTimeMillis();
		System.out.println( "Markov Blanket construction run time is: " + 
							( t2 - t1 ) + "ms. \n ******************************** \n\n" );
	}
	
	public static void runMakeMarkovBlanket( String rchain ) throws Exception
	{
		long t1 = System.currentTimeMillis(); 
		System.out.println( "Constructing Markov Blanket..." );
		
		if ( null == rchain )
		{
			System.out.println( "No rchain supplied." );
			return;
		}
		
		setVarsFromConfig();
		connectDB();
		
		BZScriptRunner bzsr = new BZScriptRunner( databaseName, con1, rchain );
		bzsr.runScript("src/scripts/markov_blanket.sql");  
        
		disconnectDB();

		long t2 = System.currentTimeMillis();
		System.out.println( "Markov Blanket construction runtime is: " + 
							( t2 - t1 ) + "ms." );
	}
	
	public static void setVarsFromConfig()
	{
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		System.out.println("databasename :"+ databaseName);
		databaseName0 = databaseName + "_BN";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	public static void connectDB() throws SQLException {
		//open database connections to the original database, the setup 
		//database, the bayes net database, and the contingency table database

		String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName;
		//System.out.println("dbaddress :"+ CONN_STR1 + "\ndbUsernam: " + dbUsername);
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}		

		con1 = (Connection) DriverManager.getConnection( CONN_STR1, dbUsername,	 dbPassword );
		
		
		//System.out.println("dbaddress :"+ CONN_STR1 + "\ndbUsernam: " + dbUsername);
	}
	
	public static void disconnectDB() throws SQLException 
	{
		con1.close();
	}
	
	/*
	 * Check @database@_BN.lattice_set to get the largest rchain
	 * This rchain will be used for creating the Markov Blanket
	 */
	private static String getLargestRchain()
	{
		String largestRchain = "";
		try
		{
			Statement st = con1.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name FROM " + databaseName0 + 
											".lattice_set ORDER BY length " + 
											"DESC LIMIT 1;" );
			
			if ( !rs.first() )
			{
				System.out.println( "No largest rchain" );
				return null;
			}
			
			largestRchain = rs.getString( 1 );
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL failure." );
			e.printStackTrace();
			return null;
		}
		
		return largestRchain;
	}
}
