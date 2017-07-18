import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.*;
//import java.sql.Connection;

public class KeepTablesOnly{
	static String databaseName;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String dbname;
	private static Connection tmp_con;
	private static String dbname_CT;
	private static String dbname_BN;
	private static String dbname_setup;
	private static String[] keep_CT_tablenames = new String[]{"a,b_CT","a,b_a_CT","b_CT","a_CT"};



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


	public static void main(String[] args) throws SQLException, IOException{
		
		setVarsFromConfig();
		System.out.println("Set variables");

		try{
			tmp_con = connectDB(dbname_CT);
		} catch (Exception e) {
			System.err.println("Could not connect to the database " + dbname_CT );
		}
		
		//keep tables in CT database
		Drop_tmpTables(tmp_con,dbname_CT,keep_CT_tablenames);

	}
	
	


	public static void Drop_tmpTables(Connection con,String dbname,String[] tablenames) throws SQLException {
        //drop temporary CT tables
		//Keep tables which given by String[] tablenames
		
        
        Statement st = con.createStatement();
		String NewSQL = "select concat('drop table `',table_name,'`;') as result FROM information_schema.tables where table_schema = '" 
		+dbname;

		for(int i=0;i<tablenames.length;i++){
			NewSQL = NewSQL + "' and table_name != '" + tablenames[i];
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



 

		//select the delete tablenames,return a strinng
        /*st.execute("select concat('drop table ',table_name,';') as result FROM information_schema.tables where table_schema = " +dbname+ " and table_name != " +tablename+ ";" );  
		System.out.println(result);
		Statement tmp = con.createStatement();*/

 
}
 

