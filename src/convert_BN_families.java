import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.*;


public class convert_BN_families{

	static String databaseName;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String dbname;
	private static Connection con_setup;
	private static Connection con_BN;
	private static String dbname_CT;
	private static String dbname_BN;
	private static String dbname_setup;
	private static String target;
	private static String maxRchain;



	public static void convert_BN() throws Exception{

		setVarsFromConfig();
		System.out.println("Set variables from config...");
		con_BN = connectDB(dbname_BN);
		con_setup = connectDB(dbname_setup);

		//MakeSetup.runMS();
		//System.out.println("The MakeSetup complete.");

		//STEP 1 
		String maxRchain = findLongestRChain(con_BN);
		System.out.println("Longest Rchain is : " + maxRchain );
		//STEP 2
		//Firstly, read fid from setup.target table
		target = get_target(con_setup);
		System.out.println("The Target is : " + target);
		target_parent_set(con_BN,target,maxRchain);
		System.out.println("Create view target_parent_set...");
		//STEP 3
		FID_pvariables(con_BN,target);
		System.out.println("Create view FID_pvariables...");
		//STEP 4 
		insert_FunctorSet(con_setup);
		System.out.println("Insert target_parent_set into setup.Functorset...");
		//STEP 5
		insert_Expansions(con_setup);
		System.out.println("Insert FID-pvariables into setup.Expansions...");


		//RUN CT-GENERATOR--according to the process of the document
		BayesBaseCT_SortMerge.buildCT();
		System.out.println("The CT database is ready for use.");


	}



	public static void main(String[] args) throws SQLException, IOException{

		try {
			 	convert_BN();
		} catch (Exception ex) {
			System.err.println("Unable to convert BN families! ");
			System.err.println(ex);
		}

	}


	
	//STEP 1 : Create view that finds edges for the maximal length rchain in lattice_set
	public static String findLongestRChain(Connection con) throws SQLException{
		
		Statement st = con.createStatement();
		String newsql="DROP VIEW  IF EXISTS FinalDAG; "; 
		int rs = st.executeUpdate(newsql);
		newsql = "CREATE VIEW FinalDAG(Rchain) AS SELECT name FROM lattice_set order by length(name) desc limit 1 ;";
		rs = st.executeUpdate(newsql);

		ResultSet rst = st.executeQuery("SELECT name FROM lattice_set order by length(name) desc limit 1; ");
		//ArrayList<String> sets = new ArrayList<String>();
		String rchain = "";
		while(rst.next()){
			System.out.println(rst.getString("name"));
			//sets.add(rst.getString("name"));
			rchain = rst.getString("name");
		}
		
		return rchain;
	}


	//read fid from setup.target table
	public static String get_target(Connection con) throws SQLException{ // should be modified after test

		Statement st = con.createStatement();
		ResultSet rst = st.executeQuery("SELECT Fid FROM Target limit 1; ");
		String target = "";

		while(rst.next()){
			System.out.println(rst.getString("Fid"));
			//sets.add(rst.getString("name"));
			target = rst.getString("Fid");
		}

		return target;

	} 


	//STEP 2 : Create view for single FID in target, contains the parents of FID in FinalDAG and the child. 
	public static void target_parent_set(Connection con, String target, String maxRchain) throws SQLException{ // should be modified after test

		Statement st = con.createStatement();
		String newsql="DROP VIEW  IF EXISTS target_parent_set; "; 
		int rs = st.executeUpdate(newsql);
		newsql = "CREATE VIEW target_parent_set AS SELECT parent AS Fid FROM Path_BayesNets WHERE child = '" + target + "' and Rchain = '" + maxRchain + "' UNION SELECT '" + target + "' AS Fid; ";
		rs = st.executeUpdate(newsql);

	}


	//STEP 3 : Create view for single FID in target, contains the pvid of the FID
	public static void FID_pvariables(Connection con, String target) throws SQLException{  // should be modified after test

		Statement st = con.createStatement();
		String newsql="DROP VIEW  IF EXISTS FID_pvariables; "; 
		int rs = st.executeUpdate(newsql);
		newsql = "CREATE VIEW FID_pvariables AS SELECT pvid FROM FNodes_pvars WHERE Fid = '" + target + "'; ";
		rs = st.executeUpdate(newsql);

	}


	//STEP 4 : Insert target_parent_set into setup.Functorset
	public static void insert_FunctorSet(Connection con) throws SQLException{ // should be modified after test

		Statement st = con.createStatement();
		String newsql="TRUNCATE FunctorSet; "; 
		int rs = st.executeUpdate(newsql);
		newsql = "INSERT INTO FunctorSet SELECT * FROM unielwin_BN.target_parent_set;";
		rs = st.executeUpdate(newsql);

	}


	//STEP 5 : Insert FID-pvariables into setup.Expansions
	public static void insert_Expansions(Connection con) throws SQLException{ // should be modified after test

		Statement st = con.createStatement();
		String newsql="TRUNCATE Expansions; "; 
		int rs = st.executeUpdate(newsql);
		newsql = "INSERT INTO Expansions SELECT * FROM unielwin_BN.FID_pvariables;";
		rs = st.executeUpdate(newsql);

	}


	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
		dbname_CT = databaseName + "_CT";
		dbname_BN = databaseName + "_BN";
		dbname_setup = databaseName + "_setup";
	}


	public static Connection connectDB(String database) throws Exception{

		String CONN_STR = "jdbc:" + dbaddress + "/" + database;

		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}

		try{
			return ((Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword));
		} catch (Exception e){
			System.err.println("Could not connect to the database " + database );
		}
		
		return null;

	}


}