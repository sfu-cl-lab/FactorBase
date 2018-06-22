package ca.sfu.cs.factorbase.tables;

import ca.sfu.cs.factorbase.app.Config;

import com.mysql.jdbc.Connection;

import java.io.IOException;
import java.sql.*;
/*@Jun 6, zqian
 * generating CP tables by calling a store procedure in _BN database
 * And also computing BIC,AIC,loglikelihood
 * 
 * 
 *August 9. This just adds Boolean values for the relationship values to the Attribute_Values tables. Todo: move this to Find_Values part.
 * 
 * */

public class CPGenerator {

	static Connection  con2;

	static String databaseName, databaseName2 ,databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

	
	public static void main(String[] args) throws Exception {
		
		//read config file
		setVarsFromConfig();
		
		//connect to db using jdbc
		connectDB();
        System.out.println(" Parameter learning  for :"+ databaseName2);

		CPGenerator.Generator(databaseName,con2);
		CP mycp = new CP(databaseName2,databaseName3);
		mycp.cp();
		System.out.println("\n Parameter learning is done.");
		
		
		con2.close();
	}

	


	public static void  Generator( String databaseName2,Connection con2) throws SQLException, IOException{
		long l = System.currentTimeMillis(); 
		Statement st1 = con2.createStatement();
		// adding possible values of Rnodes into Attribute_Value //Jun 6
		ResultSet rs1 = st1.executeQuery("select rnid from RNodes;;");    
		while(rs1.next()){
			String rnid = rs1.getString("rnid");
			//System.out.println("rnid : " + rnid);
			Statement st2 = con2.createStatement();
			st2.execute("SET SQL_SAFE_UPDATES=0;");
			//adding boolean values for rnodes
			st2.execute("delete from  Attribute_Value  where column_name='"+rnid+"';");
			//System.out.println("delete from  Attribute_Value  where column_name='"+rnid+"';");
			//st2.execute("insert  into Attribute_Value values('"+rnid+"','True');");
			//st2.execute("insert  into Attribute_Value values('"+rnid+"','False');");
			st2.execute("insert  into Attribute_Value values('"+rnid+"','T');"); // April 28, 2014, zqian
			st2.execute("insert  into Attribute_Value values('"+rnid+"','F');"); // keep consistency with ct table
		}
	// we used to make stored procedure. Now we do CP estimation in Java.
		
		/* st1.execute("drop procedure if exists `CP_Generator`;"); */
		
		
	/*	//build stored procedure in _BN        
        BZScriptRunner bzsr = new BZScriptRunner(databaseName2,con2);
        bzsr.CP_createSP("scripts/CPGenerator.sql");
        //System.out.println("creating the stored procedure is done "+databaseName);
        bzsr.callSP("CP_Generator");
        //System.out.println("CP_Generator is done for "+databaseName);
        long l2 = System.currentTimeMillis();
		System.out.print("Parameter Learning Time(ms): "+(l2-l)+" ms.\n");

        ResultSet rs2 = st1.executeQuery("SELECT    sum(LogLikelihood) as FinalLogLikelihood,    sum(Parameters) as FreeParameters,    sum(BIC) as FinalBIC,    sum(AIC) as FinalAIC FROM   Scores;");    
		while(rs2.next()){
			String FinalLogLikelihood = rs2.getString("FinalLogLikelihood");
			System.out.println("FinalLogLikelihood : " + FinalLogLikelihood);
			String FreeParameters = rs2.getString("FreeParameters");
			System.out.println("FreeParameters : " + FreeParameters);
			String  FinalBIC = rs2.getString("FinalBIC");
			System.out.println("FinalBIC  : " + FinalBIC);
			String  FinalAIC = rs2.getString("FinalAIC");
			System.out.println("FinalAIC  : " + FinalAIC);
			
		}
		*/
        
        
        
	}
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		databaseName2 = databaseName + "_BN";
		databaseName3 = databaseName + "_CT";

		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	public static void connectDB() throws SQLException {

		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
		
		
	}


	
}