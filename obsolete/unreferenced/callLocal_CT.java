import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
public class callLocal_CT {
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String CT_databaseName = "unielwin_copy_CT";
	static String PB_databaseName = "unielwin_copy_BN";
	static String output_databaseName = "unielwin_db";
	static Connection con_CT;
	static Connection con_PB;
	static Connection con_output;
	
	static int maxNumberOfMembers = 0;
	public static void main(String[] args) throws Exception {
		setVarsFromConfig();
		connectDB();
		//Statement initst = con4.createStatement();
		Local_CT test = new Local_CT(CT_databaseName,PB_databaseName,output_databaseName ,con_CT,con_PB,con_output);
		test.big_CT_table="`a,b_CT`";
		//test.Rchain="`a`";
		Statement st_PB = con_PB.createStatement();
		ResultSet rs_chain = st_PB.executeQuery("select distinct rchain from RChain_pvars;");
		ArrayList<String> chainlist = new ArrayList<String>();
		while(rs_chain.next()){
	 	 	 System.out.println("hasparent node: " + rs_chain.getString(1));
	 	 	chainlist.add(rs_chain.getString(1));
	 	 }
		int size_chain = chainlist.size();
		for(int i = 0; i<chainlist.size(); i++) {
			test.Rchain=chainlist.get(i);
		
		ResultSet rs_par = st_PB.executeQuery("select distinct Fid from FNodes;");
		ArrayList<String> parlist = new ArrayList<String>();
		
		while(rs_par.next()){
	 	 	 System.out.println("hasparent node: " + rs_par.getString(1));
	 	 	 parlist.add(rs_par.getString(1));
	 	 }
		int size_par = parlist.size();
		for(int j = 0; j<parlist.size(); j++) {
			test.FID=parlist.get(j);
			System.out.println(test.FID);
			test.FID.substring(0,test.FID.length()-1);
			//+"_" +  Rchain.substring(1, Rchain.length()-1) + "_CT`";
			test.ComputeCTs();
		}
		
		}
		
	}
	
	
	public static void connectDB() throws SQLException {
		String CONN_STR = "jdbc:" + dbaddress + "/" + CT_databaseName;
		String CONN_STR1 = "jdbc:" + dbaddress + "/" + PB_databaseName;
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + output_databaseName;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con_CT = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
		con_PB = (Connection) DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
		con_output = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		//1: run Setup; 0: not run
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}
}
