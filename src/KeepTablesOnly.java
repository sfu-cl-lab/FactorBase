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
	static Connection tmp_con;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	//static String dbname;
	//static String databaseName1="PPLRuleDetectionJan23Movies";
	//static String databaseName2="PPLRuleDetectionJan23Movies";


	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}


	public static void main(String[] args) throws SQLException, IOException{
		
		setVarsFromConfig();
		String dbname = "UW_std";
		String[] tablenames = new String[]{"a_b","a_CT","b_CT"};

		String CONN_STR = "jdbc:" + dbaddress + "/" + dbname;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		tmp_con = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);

		Drop_tmpTables(tmp_con,dbname,tablenames);

	}
	
	


	public static void Drop_tmpTables(Connection con,String dbname,String[] tablenames) throws SQLException {
        //drop temporary CT tables
		//Keep tables which given by String[] tablenames
		
        
        Statement st = con.createStatement();
		String NewSQL = "select concat('drop table ',table_name,';') as result FROM information_schema.tables where table_schema = '" 
		+dbname;

		for(int i=0;i<tablenames.length;i++){
			NewSQL = NewSQL + "' and table_name != '" +tablenames[i];
		}
		NewSQL += "';";

		System.out.println(NewSQL);
		ArrayList<String> sets = new ArrayList<String>();
		try{
			ResultSet res = st.executeQuery(NewSQL);
			while(res.next()){
 
                sets.add(res.getString("result"));
 				//System.out.println(sets+" OK!");
            }

			for(String set : sets){
				st.execute(set);
				System.out.println(set+" OK!");
			}
			//String DropSQL = res.getString("result");
			
			//st.close();
		}
		catch(SQLException e){
			System.out.println("ERROR"+ e);
		}
			
		/*while(res.next()){
			sets.add(res.getString("dropsql"));
			Statement tmp = con.createStatement();
			String DropSQL = res.getString();
			Boolean rs = tmp.execute(DropSQL);
			System.out.println(" OK!");
		}*/
		//result.close();
		//st.close();

	}
 

		//select the delete tablenames,return a strinng
        /*st.execute("select concat('drop table ',table_name,';') as result FROM information_schema.tables where table_schema = " +dbname+ " and table_name != " +tablename+ ";" );  
		System.out.println(result);
		Statement tmp = con.createStatement();*/

 
}
 

