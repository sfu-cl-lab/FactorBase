import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import lattice.short_rnid_LatticeGenerator;

import org.apache.commons.lang.StringUtils;

import com.mysql.jdbc.Connection;

/*Jun 25 @zqian
 * 
 * Trying to conquer the bottleneck of creating false tables (the join issue) by implementing our sort_merge algorithm.
 * Great stuff!
 * 
 * Here we have some different versions.
 * for version 3, naive implementing of sort merge with "load into" command in terms of efficiency issue of mysql insertion.
 * for version 4, concatenating the order by columns into one column when version 3 can not finish the order by .
 * 
 * for version 5, it's a kind of more complicated approach by pre-compressing all the attribute columns into one column, and then employing concatenating trick again on order by part.
 *   this version still has some bugs that need to be investagiated.
 *   
 * Preconditions: database_BN  has been created with lattice information and functor information.
 *  
 * */
public class BayesBaseCT_SortMerge_cont {

	static Connection con1, con2, con3;
	//con1 for real database, con2 for _BN, con3 for _CT 
	//  to be read from config
	static String databaseName, databaseName2, databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static String opt2;
	static String cont;
	static String indi;

	static int maxNumberOfMembers = 0;
		
	
	public static void main(String[] args) throws Exception {
		      
		buildCT();

	}
	
	public static void buildCT() throws Exception {
	      
		initProgram();
		//connect to db using jdbc
		connectDB();
		//build _BN part1 from metadata_1.sql
       BZScriptRunner bzsr = new BZScriptRunner(databaseName,con2);
       bzsr.runScript("src/scripts/transfer.sql");
       
       /*
       bzsr.runScript("src/scripts_ADT/metadata_1.sql");
       
      
        bzsr.createSP("src/scripts_ADT/storedprocs.sql");
        bzsr.callSP("find_values");
*/
		//generate lattice tree
		//maxNumberOfMembers = LatticeGenerator.generate(con2);
		maxNumberOfMembers = short_rnid_LatticeGenerator.generate(con2);// rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
		System.out.println(" ##### lattice is ready for use* ");
		
		//build _BN part2: from metadata_2.sql 
		if(indi.equals("0")) {
			if(cont.equals("1")) {
				bzsr.runScript("src/scripts/metadata_2_cont.sql");
		}
		else {
			bzsr.runScript("src/scripts/metadata_2_nolink.sql");
		}
		}
		else {
			if(cont.equals("1")) {
				bzsr.runScript("src/scripts/metadata_2_cont_2.sql");
			}
			else {
				bzsr.runScript("src/scripts/metadata_2_nolink.sql");
			}
		}

        // building CT tables for Rchain
        CTGenerator();
				
        
        
		disconnectDB();
	}
	
	/** 
	 * //building the _CT tables for length >=2
	 * for(int len = 2; len <= 2; len++){
		
		
		//1. find rchain, find list of members of rchain. Suppose first member is rnid1.
		//2. initialize current_ct = rchain_counts after summing out the relational attributes of rnid1.
		//3. Current list = all members of rchain minus rndi1. find ct(table) for current list = . Select rows where all members of current list are true. Add 1nodes of rnid1.
		//4. Compute false table using the results of 2 and 3 (basically 2 - 3).
		//5. Union false table with current_ct to get new ct where all members of current list are true.
		//6. Repeat with current list as initial list until list is empty.
		
		//Example: 
		//1. Rchain = R3,R2,R1. first rnid1 = R3. 
		//2. Find `R3,R2,R1_counts`. Sum out fields from R3 to get `R2,R1-R3_flat1`.
		//3. Current list = R2,R1. Find `R2,R1_ct` where R1 = T, R2 = T. Add 1nodes of R3 (multiplying) to get `R2,R1-R3_star`.
		//4. Compute `R2,R1-R3_false` = `R2,R1-R3_star - `R2,R1-R3_flat1` union (as before)
		//5. Compute `R2,R1-R3_ct` = `R2,R1-R3_false` cross product `R3_join` union `R3,R2,R1_counts`.
		//6. Current list = R1. Current rnid = R2. Current ct_table = `R2,R1-R3_ct`.
	
		BuildCT_Rnodes_flat(len);
		
		BuildCT_Rnodes_star(len);
		
		BuildCT_Rnodes_CT(len);
	}
	 * 
	 */
	public static void CTGenerator() throws Exception{
		
		long l = System.currentTimeMillis(); //@zqian : CT table generating time
		   // handling Pvars, generating pvars_counts		
        BuildCT_Pvars();
        
        // preparing the _join part for _CT tables
		BuildCT_Rnodes_join();
		
		//building the RNodes_counts tables. should be called Rchains since it goes up the lattice.
		if(opt2.equals("1")) {
			for(int len = 1; len <= maxNumberOfMembers; len++)
				BuildCT_Rnodes_counts(len);
		}
		else {
			for(int len = 1; len <= maxNumberOfMembers; len++)
				BuildCT_Rnodes_counts2(len);
		}

		if (opt2.equals("1")) {
			// handling Rnodes with Lattice Moebius Transform        
			for(int len = 1; len <= 1; len++) //initialize first level of rchain lattice
			{
			
				//building the _flat tables
				BuildCT_Rnodes_flat(len);
		
				//building the _star tables
				BuildCT_Rnodes_star(len);

				//building the _false tables first and then the _CT tables
				BuildCT_Rnodes_CT(len);
			}
			
			//building the _CT tables. Going up the Rchain lattice
			for(int len = 2; len <= maxNumberOfMembers; len++)
			{ 
				BuildCT_RChain_flat(len);
			}
		}
		

		//delete the tuples with MULT=0 in the biggest CT table
		String BiggestRchain="";
		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );
		while(rs.next()){
			BiggestRchain = rs.getString("RChain");
			System.out.println("\n BiggestRchain : " + BiggestRchain);
		}
		Statement st1 = con3.createStatement();
		System.out.println("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );
		st1.execute("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );
		
		long l2 = System.currentTimeMillis();  //@zqian : measure structure learning time
		System.out.print("Building CT Time(ms): "+(l2-l)+" ms.\n");
	
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

	static void initProgram() throws IOException, SQLException {
		//read config file
		setVarsFromConfig();
		
	
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
		cont = conf.getProperty("Continuous");
		indi = conf.getProperty("Individual");
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
		
		
	
	public static void BuildCT_RChain_flat(int len) throws SQLException, IOException {
		System.out.println("\n ****************** \n" +
				"Building the _CT tables for Length = "+len +"\n" );

		long l = System.currentTimeMillis(); 

		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
		 int fc=0; int count =0;
		while(rs.next())
		{
			
			//System.out.print("fc ::"+ fc);
			//  get pvid for further use
			String rchain = rs.getString("RChain");
			System.out.println("\n rchain String : " + rchain );

			//  create new statement
			Statement st1 = con2.createStatement();
			ResultSet rs1 = st1.executeQuery("SELECT distinct parent, removed as rnid FROM lattice_rel  where child = '"+rchain+"' order by rnid ASC;"); // memebers of rchain
			
			while(rs1.next())
			{		
				
				String parent = rs1.getString("parent");
			//	System.out.println("\n parent : " + parent);
				String rnid = rs1.getString("rnid");
			//	System.out.println("\n rnid : " + rnid);
				
				String BaseName = "`"+rchain.replace("`", "")+"_"+rnid.replace("`", "")+"`";
			//	System.out.println("\n BaseName : " + BaseName + "\n");
				
				Statement st2 = con2.createStatement();
				Statement st3 = con3.createStatement();
					
				//  create select query string	
				ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM ADT_RChain_Star_Select_List WHERE rchain = '" + rchain + "' and '"+rnid+"' = rnid;");
				String selectString = makeCommaSepQuery(rs2, "Entries", " , ");			
			//	System.out.println("Select String : " + selectString);
				rs2.close();
				//  create mult query string
				ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM  ADT_RChain_Star_From_List WHERE rchain = '" + rchain + "' and '"+rnid+"' = rnid;");
				String MultString = makeStarSepQuery(rs3, "Entries", " * ");
			//	System.out.println("Mult String : " + MultString+ " as `MULT`");
				rs3.close();
				//  create from query string
				ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM  ADT_RChain_Star_From_List WHERE rchain = '" + rchain + "' and '"+rnid+"' = rnid;");
				String fromString = makeCommaSepQuery(rs4, "Entries", " , ");
			//	System.out.println("From String : " + fromString);			
				rs4.close();
				//  create where query string
				ResultSet rs5 = st2.executeQuery("SELECT DISTINCT Entries FROM  ADT_RChain_Star_Where_List WHERE rchain = '" + rchain + "' and '"+rnid+"' = rnid;");
				String whereString = makeCommaSepQuery(rs5, "Entries", " and ");
			//	System.out.println("Where String : " + whereString);
				rs5.close();
				//  create the final query
				String queryString ="";
				if (!whereString.isEmpty())		
					queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString  + " where " + whereString;
				else 
					queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString;
				//System.out.println("Query String : " + queryString );	
				
				//make the rnid shorter 
				String rnid_or=rnid;
			
				String cur_star_Table = "`"+rnid.replace("`", "")+len+"_"+fc+"_star`";
				String createStarString = "create table "+cur_star_Table +" as "+queryString;
					
			
				System.out.println("\n create star String : " + createStarString );
				st3.execute(createStarString);		//create star table		
			
				 //adding  covering index May 21
				//create index string
				ResultSet rs15 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_star_Table.replace("`","")+"';");
				String IndexString = makeIndexQuery(rs15, "Entries", " , ");
				//System.out.println("Index String : " + IndexString);
				//System.out.println("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");
				st3.execute("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");       
				
				//staring to create the _flat table
				String 	cur_CT_Table="`"+rchain.replace("`", "")+"_counts`";
				String queryStringflat = "select sum("+cur_CT_Table+".`MULT`) as 'MULT', "+selectString + " from " +cur_CT_Table+" group by  "+ selectString +";" ;
				
				String cur_flat_Table = "`"+rnid.replace("`", "")+len+"_"+fc+"_flat`";
			
				String createStringflat = "create table "+cur_flat_Table+" as "+queryStringflat;
						
				System.out.println("\n create flat String : " + createStringflat );			
				st3.execute(createStringflat);		//create flat table
			
				 //adding  covering index May 21
				//create index string
				ResultSet rs25 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_flat_Table.replace("`","")+"';");
				String IndexString2 = makeIndexQuery(rs25, "Entries", " , ");
				//System.out.println("Index String : " + IndexString2);
				//System.out.println("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");
				st3.execute("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");       
				
				/**********starting to create _flase table***using sort_merge*******************************/
				// starting to create _flase table : part1
				String cur_false_Table= "`"+rnid.replace("`", "")+len+"_"+fc+"_false`";
				
				//create false table					
//				Sort_merge5.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
				//Sort_merge4.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
				Sort_merge3.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);

				
				 //adding  covering index May 21
				//create index string
				ResultSet rs35 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_false_Table.replace("`","")+"';");
				String IndexString3 = makeIndexQuery(rs35, "Entries", " , ");
				//System.out.println("Index String : " + IndexString3);
				//System.out.println("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");
				st3.execute("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");       

				
				
		 
				// staring to create the CT table
				ResultSet rs_45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");
				String CTJoinString = makeUnionSepQuery(rs_45, "Entries", " , ");
				System.out.println("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");

				System.out.println("CT Join String : " + CTJoinString);
				
				String QueryStringCT = "select "+CTJoinString+" from "+cur_CT_Table + " union " + "select "+CTJoinString+" from " + cur_false_Table +", `" + rnid_or.replace("`", "") +"_join`";
				System.out.println("\n Query String for CT Table: "+ QueryStringCT);
				
				//String Next_CT_Table="OS_Dummy";
				String Next_CT_Table="";
				if (rs1.next())
					Next_CT_Table="`"+BaseName.replace("`", "")+"_CT`";
				else 				 
					Next_CT_Table="`"+rchain.replace("`", "")+"_CT`";
				
		
				//System.out.println("\n name for Next_CT_Table : "+Next_CT_Table);
			 
				System.out.println("create table "+Next_CT_Table+" as " + QueryStringCT);
				st3.execute("create table "+Next_CT_Table+" as " + QueryStringCT);	 //create CT table	
				rs1.previous();

				 //adding  covering index May 21
				//create index string
				ResultSet rs45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+Next_CT_Table.replace("`","")+"';");
				String IndexString4 = makeIndexQuery(rs45, "Entries", " , ");
				//System.out.println("Index String : " + IndexString4);
				//System.out.println("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");
				st3.execute("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");       

				fc++;	
				count++;
				
				//  close statements
				st2.close();			
				st3.close();
			}
			
			st1.close();
			rs1.close();


		}
		//System.out.println("count "+count+"\n");
		rs.close();
		st.close();
		long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
		System.out.print("Building Time(ms): "+(l2-l)+" ms.\n");
		System.out.println("\n Build CT_RChain_TABLES for length = "+len+" are DONE \n" );

	}
	
	/* building pvars_counts*/
public static void BuildCT_Pvars() throws SQLException, IOException {
		Statement st = con2.createStatement();
		st.execute("Drop schema if exists " + databaseName3 + ";");
		st.execute("Create schema if not exists " + databaseName3 + ";");
		ResultSet rs = st.executeQuery("select * from PVariables;");    
		while(rs.next()){
			//  get pvid for further use
			String pvid = rs.getString("pvid");
			System.out.println("pvid : " + pvid);
			//  create new statement
			Statement st2 = con2.createStatement();
			Statement st3 = con3.createStatement();
			//  create select query string
			ResultSet rs2 = st2.executeQuery("select Entries from ADT_PVariables_Select_List where pvid = '" + pvid + "';");
			String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
			//System.out.println("Select String : " + selectString);
			//  create from query string
			ResultSet rs3 = st2.executeQuery("select Entries from ADT_PVariables_From_List where pvid = '" + pvid + "';");
			String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
			//System.out.println("From String : " + fromString);
			
			ResultSet rs_6 = st2.executeQuery("select Entries from ADT_PVariables_GroupBy_List where pvid = '" + pvid + "';");
			String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
			//System.out.println("GroupBy String : " + GroupByString);
			//  create the final query			
			String queryString = "Select " + selectString + " from " + fromString;
			
			//no group by if continuous @Aug 27
			if(cont.equals("0")) {
			if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
			//System.out.println("Query String : " + queryString );
			}
			
			System.out.println("Create String : " + "create table "+pvid+"_counts"+" as "+queryString );
			st3.execute("create table "+pvid+"_counts"+" as "+queryString);	
			//adding  covering index May 21
			//create index string
			ResultSet rs4 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+pvid+"_counts';");
			String IndexString = makeIndexQuery(rs4, "Entries", " , ");
			//System.out.println("Index String : " + IndexString);
			st3.execute("alter table "+pvid+"_counts"+" add  index "+pvid+"_Index   ( "+IndexString+" );");
			
			//  close statements
			st2.close();
			st3.close();			
		}

		rs.close();
		st.close();
		
		System.out.println("\n Pvariables are DONE \n" );

	}

public static void BuildCT_Rnodes_counts(int len) throws SQLException, IOException {
	Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
	while(rs.next()){

		//  get pvid for further use
		String rchain = rs.getString("RChain");
		System.out.println("\n RChain : " + rchain);

		//  create new statement
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();

		//  create select query string
		ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_Select_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_Select_List.rnid;");
		String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
		//System.out.println("Select String : " + selectString);

		//  create from query string
		ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_From_List.rnid;");
		String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
		//System.out.println("From String : " + fromString);

		//  create where query string
		ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_Where_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_Where_List.rnid;");
		String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
		//System.out.println("Where String : " + whereString);

		String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;
		
		//no group by if continous @Aug 27
		//  create group by query string
		if(cont.equals("0")) {
		ResultSet rs_6 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_GroupBy_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_GroupBy_List.rnid;");
		String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
		//System.out.println("GroupBy String : " + GroupByString);
		//  create the final query
		
		if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
		}
		//System.out.println("Query String : " + queryString );
        
		String createString = "create table `"+rchain.replace("`", "") +"_counts`"+" as "+queryString;
        System.out.println("create String : " + createString );
		st3.execute(createString);		
		
		//adding  covering index May 21
		//create index string
		ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
		String IndexString = makeIndexQuery(rs5, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
		
		/* drops rnode column headers
		ResultSet rs7= 	st2.executeQuery("SELECT member  FROM lattice_membership WHERE NAME = '" + rchain + "'");
		while(rs7.next()){
			String member = rs7.getString("member");
			 //st3.execute("alter table `"+rchain.replace("`", "") +"_CT`" +" modify "+member+" varchar (5);");
			 //st3.execute("alter table `"+rchain.replace("`", "") +"_CT`" +" drop "+member+";");
			   st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" drop "+member+ ";");
			  // System.out.println("***********"+"alter table `"+rchain.replace("`", "") +"_CT`"+" drop "+member+ ";"+"\n");
			
		}
	*/
		//  close statements
		st2.close();
		st3.close();
		

	}

	rs.close();
	st.close();
	System.out.println("\n Rnodes_counts are DONE \n" );	

}

public static void BuildCT_Rnodes_counts2(int len) throws SQLException, IOException {
	Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
	while(rs.next()){

		//  get pvid for further use
		String rchain = rs.getString("RChain");
		System.out.println("\n RChain : " + rchain);

		//  create new statement
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();

		//  create select query string
		ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_Select_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_Select_List.rnid;");
		String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
		//System.out.println("Select String : " + selectString);

		//  create from query string
		ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_From_List.rnid;");
		String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
		//System.out.println("From String : " + fromString);

		//  create where query string
		ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_Where_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_Where_List.rnid;");
		String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
		//System.out.println("Where String : " + whereString);
		
		String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;
		//  create group by query string
		if(cont.equals("0")) {
		ResultSet rs_6 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, RNodes_GroupBy_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = RNodes_GroupBy_List.rnid;");
		String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
		//System.out.println("GroupBy String : " + GroupByString);
		
		//  create the final query
		
		if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
		//System.out.println("Query String : " + queryString );
		}
        
		String createString = "create table `"+rchain.replace("`", "") +"_counts`"+" as "+queryString;
        System.out.println("create String : " + createString );
		st3.execute(createString);
		
		String createString_CT = "create table `"+rchain.replace("`", "") +"_CT`"+" as "+queryString;
        System.out.println("create String : " + createString_CT );
		st3.execute(createString_CT);
		
		//adding  covering index May 21
		//create index string
		ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
		String IndexString = makeIndexQuery(rs5, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
		
		ResultSet rs5_CT = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_CT';");
		String IndexString_CT = makeIndexQuery(rs5_CT, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString_CT+" );");
		
		/* drops rnode column headers
		 * ResultSet rs7= 	st2.executeQuery("SELECT member  FROM lattice_membership WHERE NAME = '" + rchain + "'");
		while(rs7.next()){
			String member = rs7.getString("member");
			 //st3.execute("alter table `"+rchain.replace("`", "") +"_CT`" +" modify "+member+" varchar (5);");
			 //st3.execute("alter table `"+rchain.replace("`", "") +"_CT`" +" drop "+member+";");
			   st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" drop "+member+ ";");
			  // System.out.println("***********"+"alter table `"+rchain.replace("`", "") +"_CT`"+" drop "+member+ ";"+"\n");
			
		}*/
		
		//  close statements
		st2.close();
		st3.close();
		

	}

	rs.close();
	st.close();
	System.out.println("\n Rnodes_counts are DONE \n" );	

}


public static void BuildCT_Rnodes_flat(int len) throws SQLException, IOException {
	Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
	while(rs.next()){

		//  get pvid for further use
		String rchain = rs.getString("RChain");
		System.out.println("\n rchain String : " + rchain );

		//  create new statement
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();


		//  create select query string
		ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_Select_List  WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_1Nodes_Select_List.rnid;");
		String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
		//System.out.println("Select String : " + selectString);

		//  create from query string
		ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_FROM_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_1Nodes_FROM_List.rnid;");
		String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
		//System.out.println("From String : " + fromString);
		
		String queryString = "Select " + selectString + " from " + fromString ;
	//  create group by query string
		if(cont.equals("0")) {
		ResultSet rs_6 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_GroupBY_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_1Nodes_GroupBY_List.rnid;");
		String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
		//System.out.println("GroupBy String : " + GroupByString);
		
		//  create the final query
		
		if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
		//System.out.println("Query String : " + queryString );	
		}
		
		String createString = "create table `"+rchain.replace("`", "") +"_flat`"+" as "+queryString;
        System.out.println("\n create String : " + createString );
		st3.execute(createString);		
		
		//adding  covering index May 21
		//create index string
		ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_flat';");
		String IndexString = makeIndexQuery(rs5, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_flat`"+" add index `"+rchain.replace("`", "") +"_flat`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_flat`"+" add index `"+rchain.replace("`", "") +"_flat`   ( "+IndexString+" );");

		
		//  close statements
		st2.close();			
		st3.close();			

	}

	rs.close();
	st.close();
	System.out.println("\n Rnodes_flat are DONE \n" );

}

public static void BuildCT_Rnodes_star(int len) throws SQLException, IOException {
	Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
	while(rs.next()){

		//  get pvid for further use
		String rchain = rs.getString("RChain");
		System.out.println("\n rchain String : " + rchain );

		//  create new statement
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();

		//  create select query string
		ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_Select_List  WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_Star_Select_List.rnid;");
		String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
		//System.out.println("Select String : " + selectString);

		//  create from MULT string
		ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_Star_From_List.rnid;");
		String MultString = makeStarSepQuery(rs3, "Entries", " * ");
		//System.out.println("Mult String : " + MultString+ " as `MULT`");

		//  create from query string
		ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_Star_From_List.rnid;");
		String fromString = makeCommaSepQuery(rs4, "Entries", " , ");
		//System.out.println("From String : " + fromString);
					
		//  create the final query
		String queryString = "";
		if (!selectString.isEmpty()) {
			queryString = "Select " +  MultString+ " as `MULT` ,"+selectString + " from " + fromString ;
		} else {
			queryString = "Select " +  MultString+ " as `MULT`  from " + fromString ;
			
		}
		//System.out.println("Query String : " + queryString );	
		
		String createString = "create table `"+rchain.replace("`", "") +"_star`"+" as "+queryString;
        System.out.println("\n create String : " + createString );
		st3.execute(createString);		
		
		//adding  covering index May 21
		//create index string
		ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_star';");
		String IndexString = makeIndexQuery(rs5, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_star`"+" add index `"+rchain.replace("`", "") +"_star`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_star`"+" add index `"+rchain.replace("`", "") +"_star`   ( "+IndexString+" );");
		
		
		//  close statements
		st2.close();			
		st3.close();			


	}

	rs.close();
	st.close();
	System.out.println("\n Rnodes_star are DONE \n" );

}

public static void BuildCT_Rnodes_CT(int len) throws SQLException, IOException {
	Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
	while(rs.next()){

		//  get pvid for further use
		String rchain = rs.getString("RChain");
		System.out.println("\n rchain String : " + rchain );

		//  create new statement
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();		
		/**********starting to create _flase table***using sort_merge*******************************/
//		Sort_merge5.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
		//Sort_merge4.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
		Sort_merge3.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);

      //adding  covering index May 21
		//create index string
		ResultSet rs15 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_false';");
		String IndexString = makeIndexQuery(rs15, "Entries", " , ");
		//System.out.println("Index String : " + IndexString);
	//	System.out.println("alter table `"+rchain.replace("`", "") +"_false`"+" add index `"+rchain.replace("`", "") +"_false`   ( "+IndexString+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_false`"+" add index `"+rchain.replace("`", "") +"_false`   ( "+IndexString+" );");
		
        
        
        //building the _CT table        //expanding the columns // May 16
       // must specify the columns, or there's will a mistake in the table that mismatch the columns      
		ResultSet rs5 = st3.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
    	// reading the column names from information_schema.columns, and the output will remove the "`" automatically, 
		// however some columns contain "()" and MySQL does not support "()" well, so we have to add the "`" back. 
		String UnionColumnString = makeUnionSepQuery(rs5, "Entries", " , ");
			//System.out.println("Union Column String : " + UnionColumnString);
       
		String createCTString = "create table `"+rchain.replace("`", "") +"_CT`"+" as select "+UnionColumnString+ " from `"+rchain.replace("`", "") +"_counts` union " +
		"select "+UnionColumnString+" from `"+rchain.replace("`", "") +"_false`, `"+rchain.replace("`", "") +"_join`;" ;
        	System.out.println("\n create CT String : " + createCTString ); 
        st3.execute(createCTString);		
        
      //adding  covering index May 21
		//create index string
		ResultSet rs25 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+rchain.replace("`", "") +"_CT';");
		String IndexString2 = makeIndexQuery(rs25, "Entries", " , ");
		//System.out.println("Index String : " + IndexString2);
		//System.out.println("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_CT`   ( "+IndexString2+" );");
		st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_CT`   ( "+IndexString2+" );");
		
		
		//  close statements
		st2.close();			
		st3.close();
		

	}

	rs.close();
	st.close();
	System.out.println("\n Rnodes_false and Rnodes_CT  are DONE \n" );

}

//preparing the _join part for _CT tables
public static void BuildCT_Rnodes_join() throws SQLException, IOException 
{
    //set up the join tables that represent the case where a relationship is false and its attributes are undefined //
    
    Statement st = con2.createStatement();
	ResultSet rs = st.executeQuery("select rnid from RNodes ;");
	
	while(rs.next()){
	//  get rvid 
		String rnid = rs.getString("rnid");
			System.out.println("\n rnid : " + rnid);
		
		Statement st2 = con2.createStatement();
		Statement st3 = con3.createStatement();

		//  create ColumnString
		ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM Rnodes_join_columnname_list WHERE rnid = '" + rnid +"';");
		
		String ColumnString = makeCommaSepQuery(rs2, "Entries", " , ");
		ColumnString = rnid + " varchar(5) ," + ColumnString;			
		//if there's no relational attribute, then should remove the "," in the ColumnString	
		if (ColumnString.length() > 0 && ColumnString.charAt(ColumnString.length()-1)==',') 
		{		
			ColumnString = ColumnString.substring(0, ColumnString.length()-1);			   
		}		
			//System.out.println("Column String : " + ColumnString);
		String createString = "create table `"+rnid.replace("`", "") +"_join` ("+ ColumnString +");";		
			System.out.println("create String : " + createString);

		st3.execute(createString);	
		st3.execute("INSERT INTO `"+rnid.replace("`","")+"_join` ( "+rnid + ") values ('F');");
		
		st2.close();
		st3.close();
	
	}
	rs.close();
	st.close();
	System.out.println("\n Rnodes_joins are DONE \n" );	

    	
}

// for _false: union part
public static String makeTSepQuery(ResultSet rs, String colName, String del, String Rchain) throws SQLException {
	ArrayList<String> parts = new ArrayList<String>();

	while(rs.next()){
		//stringQuery += rs.getString(colName) + del;
		String temp=rs.getString(colName);
		String temp1=temp.replace("`", "");
		temp = "`"+Rchain.replace("`", "") +"_star`"+".`"+temp1+"`";			
		parts.add(temp);		
		
	}
	//stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

	return StringUtils.join(parts,del);
	//return stringQuery;
}
//	for _star  adding 
public static String makeStarSepQuery(ResultSet rs, String colName, String del) throws SQLException {
	ArrayList<String> parts = new ArrayList<String>();

	while(rs.next()){
		//stringQuery += rs.getString(colName) + del;
		String temp=rs.getString(colName);
		String temp1=temp.replace("`", "");
		temp = "`"+temp1+"`.`MULT` ";			
		parts.add(temp);		
		
	}
	//stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

	return StringUtils.join(parts,del);
	//return stringQuery;
}
//  for _CT part, adding "`"
public static String makeUnionSepQuery(ResultSet rs, String colName, String del) throws SQLException {
	
	ArrayList<String> parts = new ArrayList<String>();

	while(rs.next()){
		parts.add("`"+rs.getString(colName)+"`");
	}
	return StringUtils.join(parts,del);
}

//separate the entries by ","
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

//	for len>1, false table, part 1, where string
public static String makeFalseWhereSepQuery(ResultSet rs, String colName, String del, String cur_flat_Table,String cur_star_Table) throws SQLException {
	ArrayList<String> parts = new ArrayList<String>();

	while(rs.next()){
		//stringQuery += rs.getString(colName) + del;
		String temp=rs.getString(colName);
		String temp1=temp.replace("`", "");
		temp = "`"+cur_flat_Table.replace("`", "") +"`.`"+temp1+"` = "+ "`"+cur_star_Table.replace("`", "") +"`.`"+temp1+"`";			
		parts.add(temp);		
		
	}
	//stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

	return StringUtils.join(parts,del);
	//return stringQuery;
}

// for len>1, false table, part 1, part 2, select string
public static String makeFalseSepQuery(ResultSet rs, String colName, String del, String cur_flat_Table) throws SQLException {
	ArrayList<String> parts = new ArrayList<String>();

	while(rs.next()){
		//stringQuery += rs.getString(colName) + del;
		String temp=rs.getString(colName);
		String temp1=temp.replace("`", "");
		temp = "`"+cur_flat_Table.replace("`", "") +"`.`"+temp1+"`";			
		parts.add(temp);		
		
	}
	//stringQuery = stringQuery.substring(0, stringQuery.length() - del.length());

	return StringUtils.join(parts,del);
	//return stringQuery;
}

public static String makeIndexQuery(ResultSet rs, String colName, String del) throws SQLException {
	
	ArrayList<String> parts = new ArrayList<String>();
	int count=0;
	//String stringQuery = "";
	while(rs.next()&count<16){
		
			String temp =rs.getString(colName);
			temp= "`"+temp+"`";
		parts.add(temp+ " ASC");
		count ++;
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




}
