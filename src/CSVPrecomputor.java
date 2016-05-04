import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.mysql.jdbc.Connection;
/* zqian@Nov 21, fixed on bug for loading data into csv files (have to close the file)
 * if you do not close it, sometime the program may only load part of the data from resultset into .csv file, and not easy to find it.
 * and thus will cause inconsistent output of Ges Search since the data are different with your expectation.
 * 
 * Nov 21 @zqian removing the tuples when mult =0 for false.csv
 * input:  _CT database
 * output: True.csv, False.csv, .csv
 * Jun 25 @zqian
 * 
 * Suppose that we already finished the computing of all the CT tables given one database,
 * This program will exporting the CT tables into .csv files 
 * 
 * */
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

public class CSVPrecomputor {

	static Connection con1, con2, con3;

	//  to be read from config
	static String databaseName, databaseName2, databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

	static String opt2;
	
	static int maxNumberOfMembers = 0;

	static ArrayList<String> rnode_ids;
	static ArrayList<String> pvar_ids;		
	
	public static void main(String[] args) throws Exception {
		runCSV();
	}
		
	
	public static void runCSV() throws Exception {
		
		initProgram();
		connectDB();

        //get maxNumberOfMembers, max length of rchain
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT max(length) FROM lattice_set;");
        rst.absolute(1);
        maxNumberOfMembers = rst.getInt(1);
        rst.close();
        st.close();

		System.out.println(" ##### lattice is ready for use* "); //@zqian	
	    
        Computing_CSV();
       
		//disconnect from db
		disconnectDB();

	}

	
	public static void Computing_CSV() throws SQLException, IOException{
		long l = System.currentTimeMillis();
    	readPvarFromBN(con2);	
	
       for(int len = 1; len <= maxNumberOfMembers; len++)
       {
   		   System.out.println("\n processing Rchain.length = "+len); //@zqian	
   		  // readRNodesFromLatticeFalse(len);
		  // readRNodesFromLatticeTrue(len);
    	   readRNodesFromLattice(len);					
		   rnode_ids.clear(); //prepare for next loop	
			
		}
       long l2 = System.currentTimeMillis();
       System.out.print("\n CSVPrecomputor TOTAL Time(ms): "+(l2-l)+" ms.\n");
		
		
	}
    
	static void initProgram() throws IOException, SQLException {
		//read config file
		setVarsFromConfig();
		
		//init ids
		pvar_ids = new ArrayList<String>();
		rnode_ids = new ArrayList<String>();

		try{ 
			delete(new File(databaseName+"/"));
		}catch (Exception e){}

		new File(databaseName+"/" + File.separator).mkdirs();
		new File(databaseName+"/" + File.separator + "csv" + File.separator).mkdirs();
		
	
	
	}
	
	static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}

	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		databaseName2 = databaseName + "_BN";
		databaseName3 = databaseName + "_CT";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
		opt2 = conf.getProperty("LinkCorrelations");
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
		
		String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName3;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con3 = (Connection) DriverManager.getConnection(CONN_STR3, dbUsername, dbPassword);
        //handle warnings
       // handleWarnings();
	}

	
	public static void readPvarFromBN(Connection con2) throws SQLException, IOException {
			Statement st = con2.createStatement();
			  //from main db
			ResultSet rs = st.executeQuery("select * from PVariables where index_number = 0;");    //O.S. March 21 ignore variables that aren't main.
			while(rs.next()){

				//  get pvid for further use
				String pvid = rs.getString("pvid");
				System.out.println("pvid : " + pvid);

				//  create new statement
				Statement st3 = con3.createStatement();

				String queryString= "SELECT * FROM `"+pvid.replace("`", "")+"_counts` where mult>0 ;";
				ResultSet rs4 = st3.executeQuery(queryString);
				System.out.print("query string : "+queryString);
				
				//  create header
				ArrayList<String> columns = getColumns(rs4);
				String csvHeader = StringUtils.join(columns, "\t");
				System.out.println("\nCSV Header : " + csvHeader+ "\n");

				//  create csv file
				RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + pvid + ".csv", "rw");
				csv.writeBytes(csvHeader + "\n");

				ResultSet rs5 = st3.executeQuery(queryString);
				while(rs5.next()){
					String csvString = "";
					for(String col : columns){
						csvString += rs5.getString(col) + "\t";
					}
					csvString = csvString.substring(0, csvString.length() - 1);
					csv.writeBytes(csvString + "\n");
			        }
				csv.close(); // zqian@Nov 21
				//  add to ids for further use
				pvar_ids.add(pvid);

				//  close statements
				
				st3.close();
				
			}

			rs.close();
			st.close();
		}


	public static void readRNodesFromLattice(int len) throws SQLException, IOException {
		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
		while(rs.next()){

			//  get pvid for further use
			String rchain = rs.getString("RChain");
			System.out.println("\n RChain : " + rchain);

			//  create new statement
			Statement st3 = con3.createStatement();
			
			String queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` where mult>0;";
			ResultSet rs5 = null;
			try
			{
				rs5 = st3.executeQuery(queryString);
			}
			catch ( MySQLSyntaxErrorException e )
			{
				//Table doesn't exist
				st3.close();
				break;
			}
			System.out.print("query string : "+queryString);
			
			//  create header
			ArrayList<String> columns = getColumns(rs5);
			String csvHeader = StringUtils.join(columns, "\t");
			System.out.println("\n CSV Header : " + csvHeader+ "\n");

			//  create csv file, reading data from _CT table into .csv file
			RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + rchain.replace("`", "") + ".csv", "rw");
			csv.setLength(0); //File must be cleared before writing
			
			csv.writeBytes(csvHeader + "\n");

			ResultSet rs6 = st3.executeQuery(queryString);
			while(rs6.next()){
				String csvString = "";
				for(String col : columns){
					csvString += rs6.getString(col) + "\t";
				}
				csvString = csvString.substring(0, csvString.length() - 1);
				csv.writeBytes(csvString + "\n");
			}
			csv.close(); // zqian@Nov 21
			//  close statements
			st3.close();
			
			rnode_ids.add(rchain);
		}

		rs.close();
		st.close();
	}

	public static void readRNodesFromLatticeTrue(int len) throws SQLException, IOException {
		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
		while(rs.next()){

			//  get pvid for further use
			String rchain = rs.getString("RChain");
			System.out.println("\n RChain : " + rchain);

			//  create new statement
			Statement st3 = con3.createStatement();
			String whereString="";
			if (len>1)
			{
				// before: RChain : `a,b,c,f`
				 whereString = SplitString(rchain.replace("`", ""), "'T'");
				//after whereString :  `a` = 'T' and `b` = 'T' and `c` = 'T' and `f` = 'T'

			}
			else
				whereString = "`"+rchain.replace("`", "")+"` = 'T'";

			String queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` where  " + whereString + " and mult>0;";
					System.out.print("query string : "+queryString);
			ResultSet rs5 = st3.executeQuery(queryString);

			//  create header
			ArrayList<String> columns = getColumns(rs5);
			String csvHeader = StringUtils.join(columns, "\t");
			System.out.println("\n CSV Header : " + csvHeader+ "\n");

			//  create csv file, reading data from _CT table into .csv file
			RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + rchain.replace("`", "") + "True.csv", "rw");
			
			csv.writeBytes(csvHeader + "\n");

			ResultSet rs6 = st3.executeQuery(queryString);
			while(rs6.next()){
				String csvString = "";
				for(String col : columns){
					csvString += rs6.getString(col) + "\t";
				}
				csvString = csvString.substring(0, csvString.length() - 1);
				csv.writeBytes(csvString + "\n");
			}
			csv.close(); // zqian@Nov 21
			//  close statements
			st3.close();
			
			rnode_ids.add(rchain);

		}

		rs.close();
		st.close();
	}

	public static void readRNodesFromLatticeFalse(int len) throws SQLException, IOException {
		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
		while(rs.next()){

			//  get pvid for further use
			String rchain = rs.getString("RChain");
			System.out.println("\n RChain : " + rchain);

			//  create new statement
			Statement st3 = con3.createStatement();
			String queryString= "";
			String whereString="";
			
			if (len>1)
			{
				String subwhereString = "";
				 //sub select list : a,b,c
				 subwhereString =  SplitString(rchain.replace("`", ""), "'T'");
			     //sub where list: a = 'T' and b = 'T' and c = 'T'
				 queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` where mult >0 and  not ( "+subwhereString +" );";
			     //len>1:  SELECT * FROM unielwin_CT.`a,b,c_CT` where (a,b,c) not in (select a,b,c from unielwin_CT.`a,b,c_CT` where a='T' and b='T' and c = 'T' ); WRONG
				 //select * from unielwin_CT.`a,b,c_CT` where NOT(a='T' and b='T' and c = 'T') ; correct version
			}
			else
			{
				whereString = "`"+rchain.replace("`", "")+"` = 'F'";
				queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` where  " + whereString + " and mult >0 ;";
				//len =1: select * from a_CT where (a) not in (select a from a_CT where a ='T')
			}
		
//			String queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` where  " + whereString + " ;";
			
			System.out.print("\n query string : "+queryString);
			ResultSet rs5 = st3.executeQuery(queryString);
			
			
			//  create header
			ArrayList<String> columns = getColumns(rs5);
			String csvHeader = StringUtils.join(columns, "\t");
			System.out.println("\n CSV Header : " + csvHeader+ "\n");

	
			//  create csv file, reading data from _CT table into .csv file
			RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + rchain.replace("`", "") + "False.csv", "rw");
			
			csv.writeBytes(csvHeader + "\n");

			ResultSet rs6 = st3.executeQuery(queryString);
			while(rs6.next()){
				String csvString = "";
				for(String col : columns){
					csvString += rs6.getString(col) + "\t";
				}
				csvString = csvString.substring(0, csvString.length() - 1);
				csv.writeBytes(csvString + "\n");
			}
			csv.close(); // zqian@Nov 21
			//  close statements
			st3.close();
			
			rnode_ids.add(rchain);

		}

		rs.close();
		st.close();
	}


	/*
	 *  @parameter:  RChain :  `a,b,c,f`; del :"'T'"
	    @output: wherestring:  `a` = 'T' and `b` = 'T' and `c` = 'T' and `f` = 'T'
	 */
	public static String SplitString(String Rchain, String del) throws IOException {
		String[] Temp;
		ArrayList<String> parts = new ArrayList<String>();
		String delimiter = ",";
		
			Temp = Rchain.split(delimiter);
		for(int i =0; i<Temp.length; i++)
		{
			//System.out.println("Splitted temp::"+Temp[i]);
			parts.add("`"+Temp[i]+"` = "+ del);
		}	
		//System.out.println(StringUtils.join(parts," and "));
		
		return StringUtils.join(parts," and ");
		
		
	}

	
	public static String makeCommaSepQuery(ResultSet rs, String colName, String del) throws SQLException {
		ArrayList<String> parts = new ArrayList<String>();
		//String stringQuery = "";

		while(rs.next()){
			//stringQuery += rs.getString(colName) + del;
			parts.add(rs.getString(colName));
		}
		//stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

		return StringUtils.join(parts,del);
		//return stringQuery;
	}

	public static ArrayList<String> getColumns(ResultSet rs) throws SQLException {
		ArrayList<String> cols = new ArrayList<String>();
		ResultSetMetaData metaData = rs.getMetaData();
		rs.next();

		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			cols.add(metaData.getColumnLabel(i));
		}
		return cols;
	}

	public static void disconnectDB() throws SQLException {
		con1.close();
		con2.close();
		con3.close();
	}

	static void handleWarnings() throws SQLException {
        String warning = "";
        warning += buildWarningString(con2, "TernaryRelations", "of having a three column key");
        warning += buildWarningString(con2, "NoPKeys", "of not having a primary key");
        if(warning.length() > 0){
            JOptionPane.showMessageDialog(null, warning);
        }
    }

    public static String buildWarningString(java.sql.Connection con, String checkTableName, String reason) throws SQLException {
        String warningStr = "";

        Statement stmt = con.createStatement();
        ArrayList<String> ternaryrelationsTables = new ArrayList<String>();
        ResultSet rs = stmt.executeQuery("select TABLE_NAME from " + checkTableName + ";");
        while (rs.next()) {
            ternaryrelationsTables.add(rs.getString("TABLE_NAME"));
        }

        int tableNum = 0;
        for(String tableName : ternaryrelationsTables){
            tableNum++;
            warningStr += tableName + System.getProperty("line.separator");
        }

        if(tableNum > 0){
            String tableORtables = (tableNum == 1) ? "table is" : tableNum + " tables are";
            warningStr = "Warning: The following " + tableORtables + " ignored because " + reason + ":" + System.getProperty("line.separator") + System.getProperty("line.separator") + warningStr + System.getProperty("line.separator");
        }

        return warningStr;
    }


}
