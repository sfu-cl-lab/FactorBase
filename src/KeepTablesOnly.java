import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;

public class KeepTablesOnly{
	static String databaseName;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String dbname;
	private static Connection con_CT;
	private static Connection con_BN;
	private static String dbname_CT;
	private static String dbname_BN;
	private static String dbname_setup;
	//private static String[] keep_CT_tablenames = new String[]{"a,b_CT","a,b_a_CT","b_CT","a_CT"};

	
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

	//Keeping only the longest RChain table in CT database and deleting the remaining intermediataries
	//Subject to change as per nee dfor tables in CT database
	public static ArrayList<String> findLongestRChain() throws SQLException{
		Statement st = con_BN.createStatement();
		ResultSet rst = st.executeQuery("SELECT name FROM lattice_set;");
		ArrayList<String> sets = new ArrayList<String>();
		while(rst.next()){
			System.out.println(rst.getString("name"));
			String tables = rst.getString("name");
			sets.add(tables.substring(1, tables.length()-1) + "_CT");
		}
		

		return sets;
	}

//the main function. Keeps the tables in ArrayList and drops the others //
	//@Overload
	public static void Drop_tmpTables(Connection con,String dbname,ArrayList<String> tablenames) throws SQLException {
        //drop temporary CT tables
		//Keep tables which given by String[] tablenames
		
        
        Statement st = con.createStatement();
		String NewSQL = "select concat('drop table `',table_name,'`;') as result FROM information_schema.tables where table_schema = '" 
		+dbname;

		for(int i=0;i<tablenames.size();i++){
			NewSQL = NewSQL + "' and table_name != '" + (String)tablenames.get(i);
		}
		NewSQL += "';";

		System.out.println(NewSQL);
		ArrayList<String> sets = new ArrayList<String>();
		try{
			ResultSet res = st.executeQuery(NewSQL);
			while(res.next()){
 
                sets.add(res.getString("result"));
 				//System.out.println(sets+" +++ ");
            }

			for(String set : sets){
				st.execute(set);
				System.out.println(set+" OK!");
			}
		
			
			//st.close();
		}
		catch(SQLException e){
			System.out.println("ERROR"+ e);
		}		

	}

	//@Overload
	public static void Drop_tmpTables(){
		setVarsFromConfig();
		System.out.println("Set variables");

		try{
			con_CT = connectDB(dbname_CT);
		} catch (Exception e) {
			System.err.println("Could not connect to the database " + dbname_CT );
			//throw new Exception();
		}
		//
		try{
			con_BN = connectDB(dbname_BN);
		} catch (Exception e) {
			System.err.println("Could not connect to the database " + dbname_BN );
			//throw new Exception();
		}
		
		try{
			ArrayList<String> tablenames = findLongestRChain();
			Drop_tmpTables(con_CT,dbname_CT,tablenames);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//
		/*
		con_CT.close();
		con_BN.close();
		*/
	}


	public static void main(String[] args) throws SQLException, IOException{
		
		setVarsFromConfig();
		System.out.println("Set variables");

		try{
			con_CT = connectDB(dbname_CT);
		} catch (Exception e) {
			System.err.println("Could not connect to the database " + dbname_CT );
		}
		
		//keep tables in CT database
		Drop_tmpTables();

	}



 

		//select the delete tablenames,return a strinng
        /*st.execute("select concat('drop table ',table_name,';') as result FROM information_schema.tables where table_schema = " +dbname+ " and table_name != " +tablename+ ";" );  
		System.out.println(result);
		Statement tmp = con.createStatement();*/

 
}
 

