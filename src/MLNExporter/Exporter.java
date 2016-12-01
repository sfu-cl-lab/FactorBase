package MLNExporter;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Properties;
/*
 * Description: This class is Exporter.
 *	   		   It allows the users to export the information for MLN file
 *
 * Function: setConnection - set a connection with the database
 *			 setSchema - set a schema
 *			 headerBuilder - connect to the database and explore the correct information of header(all entities) which will be used by MLN
 *			 getNNode - get the information of BNNodes
 *			 bodyBuilder - Build the body part of MLN file based on the relationship between parent and child nodes
 *			
 * 
 * Bugs: none
 *
 * Version: 1.1
 *
 */
public class Exporter {
	
	
	static Connection con1, con2;

	//  to be read from config
	static String databaseName, databaseName2;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	
public static void main(String[] args) throws SQLException, IOException {
		
		//read config file
		setVarsFromConfig();
		connectDB();
		
		Exporter.strBuilder(con1,con2,databaseName);
		
		
	    
		
		disconnectDB();
	
		
	}
public static void setVarsFromConfig(){
	 Properties configFile;
	    FileReader fr;
	    Reader reader;
	 configFile = new java.util.Properties();
     try {
     	fr = new FileReader("src/config.cfg"); 
         reader = new BufferedReader(fr);
         configFile.load( reader );
     }catch(Exception eta){
         eta.printStackTrace();
     }	
     databaseName = configFile.getProperty("dbname");
	databaseName2 = databaseName + "_BN";
	dbUsername = configFile.getProperty("dbusername");
	dbPassword = configFile.getProperty("dbpassword");
	dbaddress = configFile.getProperty("dbaddress");
}



public static void connectDB() throws SQLException {
	String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName;
	try {
		java.lang.Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception ex) {
		System.err.println("Unable to load MySQL JDBC driver");
	}
	con1 = (Connection) DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);

	String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
	try {
		java.lang.Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception ex) {
		System.err.println("Unable to load MySQL JDBC driver");
	}
	con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	
	
}



public static void disconnectDB() throws SQLException {
	con1.close();
	con2.close();

}
	
	/*
	 * connect to the database and explore the correct information of header(all entities) which will be used by MLN
	 * @param con  a connection with the useful database
	 */
	public static String headerBuilder(Connection con, String Schema, PrintWriter writer){
		
		String header = "";
		ResultSet tables = null;
		ResultSet keys = null;
		ResultSet vars = null;
		Statement stmt = null;
		String[] tbList = null; // store the table names
		String[] pKeys = null;
		
		
		try{
			System.out.println("Writing Header...");
			stmt = con.createStatement();
			if(stmt.execute("SHOW TABLES")){
				tables = stmt.getResultSet();
			}
			tbList = new String[GetSize(tables)];
			for(int i = 0; i < tbList.length; i++){ // figure out all table names in the schema and stored into table list
				tables.previous();
				tbList[i] = tables.getString(1);
			}
			MergeSort.mSort(tbList, 0, tbList.length - 1);
			for(int i = 0; i < tbList.length; i++){
				if(stmt.execute("SHOW KEYS FROM " + tbList[i] + " WHERE KEY_NAME = 'PRIMARY'"))
					keys = stmt.getResultSet();
					pKeys = new String[GetSize(keys)];
					for(int j = 0; keys.previous(); j++){
						
						pKeys[j] = keys.getString("COLUMN_NAME");
						
					}
					MergeSort.mSort(pKeys, 0, pKeys.length - 1);
					if(stmt.execute("SELECT column_name FROM information_schema.COLUMNS WHERE table_name = '" + tbList[i] + "' and table_schema = '" + Schema + "'")){
						vars = stmt.getResultSet();
					}
					if(pKeys.length == 1){
						header += pKeys[0] + "(" + pKeys[0] + "_type)\n";
						}
					else{
						header += "B_" + tbList[i] + "(" + pKeys[0] + "_type";
						for(int j = 1; j < pKeys.length; j++){
								header += "," + pKeys[j] + "_type";
						}
						header+= ")\n";
					}
					while (vars.next()){
						boolean isPKey = false;
						for(int j = 0;  j < pKeys.length; j++){
							if(vars.getString("column_name").compareTo(pKeys[j]) == 0)
								isPKey = true;
						}
						if(!isPKey){
							header += vars.getString("column_name") + "(";
							header += pKeys[0] + "_type";
							for(int j = 1; j < pKeys.length; j++){
								header += "," + pKeys[j] + "_type";
							}
							header += "," + vars.getString(1) + "_type";
							if(pKeys.length == 1)
								header += "!";
							header += ")\n";
						}
						
							
					}

			}
			writer.printf("%s%n", header);
			writer.flush();
		}
		catch(SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		finally{
			if (tables != null) {
		        try {
		        	tables.close();
		        } catch (SQLException sqlEx) { } // ignore
		    }
			if (keys != null) {
		        try {
		        	keys.close();
		        } catch (SQLException sqlEx) { } // ignore
		    }
			if (vars != null) {
		        try {
		        	vars.close();
		        } catch (SQLException sqlEx) { } // ignore
		    }
			
		    
		    
		}
		
		return header;
	}
	/*
	 * get the information which stored in the BNNode
	 * @param N  Can be R, 1 and 2 for the purpose of accessing 1Nodes, 2Nodes and RNodes
	 * @param con  connect to the database
	 * @param nodes  an array of the nodes which stored the information of child/parent node
	 */
	public static int getNNode(String N, Connection con, BNNode[] nodes, int counter, String Schema) throws SQLException{
		
		ResultSet temp = null;
		String t = "";
		String MLNStr = "";
		String ID = "";
		Statement stmt = con.createStatement();
		
	
		stmt.execute("SELECT * FROM " + Schema + "." + N + "Nodes");
		temp = stmt.getResultSet();
		while(temp.next()){
			ID = "";
			MLNStr = "";
			t = temp.getString(N + "nid").substring(1);
			t = t.substring(0, t.length() - 1);
			int last = 0;
			
			for(int i = 0; i < t.length(); i++){//build mln string
				if(t.charAt(i) == ',' || t.charAt(i) == ')'){
					MLNStr += t.substring(last, i - 1);
					MLNStr += "_id_inst";
					last = i;
				}
			}
			MLNStr += "," + temp.getString("COLUMN_NAME").toUpperCase() + "_";
			ID = temp.getString(N + "nid").substring(1);
			ID = ID.substring(0, ID.length() - 1);
			nodes[counter] = new BNNode(temp.getString("COLUMN_NAME"), ID, MLNStr, false);//create corresponding BNNode
			counter++;
		}		
		return counter;
			
	}
	/*
	 * Build the body part of MLN file based on the relationship between parent and child nodes
	 * @ param con  connect to the database
	 */
	public static void bodyBuilder(Connection con, String Schema, PrintWriter writer){
		try {
			System.out.println("Writing Rules..");
			ResultSet temp = null;
			BNNode[] nodes = null;
			BNNode[] columnNames= null;
			String t = "";
			String table = "";
			String line = "";
			String MLNStr = "";
			String ID = "";
			int counter = 0;
			int complement = 0;
			Statement stmt = con.createStatement();
			stmt.execute("SELECT * FROM " + Schema + ".NumAttributes");//determine the number of nodes
			nodes = new BNNode[GetSize(stmt.getResultSet())];
			stmt.execute("SELECT * FROM " + Schema + ".RNodes");//get all relationship nodes
			temp = stmt.getResultSet();
			while(temp.next()){
				ID = "";
				MLNStr = "B_";
				t = temp.getString("orig_rnid").substring(1);//get orig_rnid without ''
				t = t.substring(0, t.length() - 1);
				int last = 0;
				
				for(int i = 0; i < t.length(); i++){//build mln string from orig_rnid
					if(t.charAt(i) == ',' || t.charAt(i) == ')'){
						MLNStr += t.substring(last, i - 1);
						MLNStr += "_id_inst";
						last = i;
					}
				}
				MLNStr += ")";
				ID = temp.getString("rnid").substring(1);//get rnid without ''
				ID = ID.substring(0, ID.length() - 1);
				nodes[counter] = new BNNode(ID, ID, MLNStr, true);//create corresponding BNNode
				counter++;
			}
			
			for(int i = 1; nodes[nodes.length - 1] == null; i++){//get the rest of the nodes in table 1Nodes, 2Nodes, 3Nodes...
				counter = getNNode(new Integer(i).toString(), con, nodes, counter, Schema);
			}
			
			for (int i = 0; i < nodes.length; i++)
			{
				String CP_tablename = nodes[i].getID() + "_CP";
//				stmt.execute("SELECT * FROM " + BNSchema + "." + CP_tablename);
//				temp = stmt.getResultSet();
				stmt.execute("SELECT column_name FROM information_schema.COLUMNS WHERE table_name = '" + CP_tablename + "' and table_schema = '" + Schema + "'");
				temp = stmt.getResultSet();
				counter = 0;
				complement = 0;
				while(temp.next()){
					complement += 1;;
					if(temp.getString("column_name").compareTo(nodes[i].getID()) == 0){
						counter++;
						break;
					}
				}
				while(temp.next() && temp.getString("column_name" ).compareTo("ParentSum") != 0 && temp.getString("column_name" ).compareTo("CP") != 0){
					counter ++;
				}
				columnNames = new BNNode[counter];//create new BNNode array
			
				columnNames[0] = nodes[i];
				
				while(temp.previous() && counter != 1){
					for(int j = 0; j < nodes.length; j++){
						if(temp.getString(1).compareTo(nodes[j].getID()) == 0){//get the corresponding nodes
							columnNames[counter - 1] = nodes[j];
							break;
						}
					}
					counter--;
				}
				stmt.execute("SELECT * FROM " + Schema + ".`"+ CP_tablename +"`");
				temp = stmt.getResultSet();
				while(temp.next()){
					double lnCP = Math.log(Double.parseDouble(temp.getString("CP")));
					DecimalFormat valueFormat = new DecimalFormat("0.0000000000");
					line += String.valueOf(valueFormat.format(lnCP)) + " ";
					 for(int k = 0; k < columnNames.length; k++){
						 if(temp.getString(complement + k).compareTo("N/A") == 0){
							 line = "";
						 }
						 else{
							 if(columnNames[k].isR()){//if colum is a relationship
								 if(temp.getString(complement + k).compareTo("F") == 0){
									 line += "!" + columnNames[k].getMLNStr();
								 }
								 else{
									 line += columnNames[k].getMLNStr();
								 }
							 }
							 else{//if column is not a relationship
								 line += columnNames[k].getMLNStr() + temp.getString(complement + k) +")";
							 }
							 if(k != columnNames.length - 1)
								 line+=" ^ ";
						 }
						 
							 
					 }
					 if(line.compareTo("") != 0)
						line += "\n";
					 table += line;
					 line = "";
					 
				}
				
				writer.print(table);
				writer.flush();
				table = "";
						
			}
			System.out.println("Writing Unit Clause...");
			//Write the unit clause of the MLN
			stmt.execute("SELECT * FROM " + Schema + ".Attribute_Value");
			temp = stmt.getResultSet();
			line = "";
			table = "";
			while(temp.next()){
				for(int i = 0; i < nodes.length; i++){
					if(nodes[i].isR()){
						if(temp.getString("COLUMN_NAME").compareTo("`" + nodes[i].getID() + "`") == 0){
							if(temp.getString("VALUE").compareTo("False") == 0 )
								line += "!" + nodes[i].getMLNStr();
							else
								line += nodes[i].getMLNStr();
							break;
						}
					}
					else{
						if(temp.getString("COLUMN_NAME").compareTo(nodes[i].getName()) == 0){
							line += nodes[i].getMLNStr() + temp.getString("VALUE") + ")";
						}
					}
				}
				line += "\n";
				table += line;
				line = "";
			}
			writer.printf("%s", table);
			writer.flush();
			writer.close();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/*
	 * 
	 */
	public static void strBuilder(Connection con1, Connection con2, String Schema){
			try {
				System.out.println("Start Writing MLN...");
				File file;
				file = new File(Schema + ".mln");
				file.delete();
				PrintWriter writer = new PrintWriter(new FileOutputStream( new File(Schema + ".mln"), true));
				headerBuilder(con1, Schema, writer); //type declaration
				bodyBuilder(con2, Schema + "_BN", writer); //builds the formulas with weights
				writer.close();
				System.out.println("Finished Writing MLN...");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	public static int GetSize(ResultSet result){
		int counter = 0;
		try{
			while(result.next())
			{
				counter++;
			}
		}
		catch(SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}		
		return counter;	
	}
	
}
