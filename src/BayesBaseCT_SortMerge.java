/*zqian, April 1st, 2014 fixed the bug of too many connections by adding con4.close()*/


/*Jun 25,2013 @zqian
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

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import lattice.short_rnid_LatticeGenerator;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;


public class BayesBaseCT_SortMerge {

	private static Connection con_std;
    private static Connection con_BN;
    private static Connection con_CT;
    private static Connection con_setup;
	private static String databaseName_std;
    private static String databaseName_BN;
    private static String databaseName_CT;
    private static String databaseName_setup;
	private static String dbbase;
	private static String dbUsername;
	private static String dbPassword;
	private static String dbaddress;
    private static String linkCorrelation;
    private static String continuous;
    /*
     * cont is Continuous
     * ToDo: Refactor
     */
    private static String cont;

	private static int maxNumberOfMembers = 0;

    public static void main(String[] args) throws Exception {
		      
		buildCT();

	}

    /**
     * @Overload
     * buildCT
     * @throws Exception
     */
	public static void buildCT() throws Exception {

        setVarsFromConfig();
        //connect to db using jdbc
        con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);
        //build _BN copy from _setup Nov 1st, 2013 Zqiancompute the subset given fid and it's parents
        BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,con_setup);
        bzsr.runScript("scripts/transfer.sql");

        con_BN = connectDB(databaseName_BN);
        con_CT = connectDB(databaseName_CT);

        //generate lattice tree
        //maxNumberOfMembers = LatticeGenerator.generate(con2);
        // rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
        maxNumberOfMembers = short_rnid_LatticeGenerator.generate(con_BN);


        System.out.println(" ##### lattice is ready for use* ");

// may not need to run this script any more, using LatticeRnodes table //
        bzsr.runScript("scripts/add_orig_rnid.sql");
        //build _BN part2: from metadata_2.sql

       // bzsr.runScript("scripts/metadata.sql");
       // this metadata is now created as views in db_setup, transferred by transfer.sql

        // empty query error,fixed by removing one duplicated semicolon. Oct 30, 2013
        //ToDo: No support for executing LinkCorrelation=0;
        if (cont.equals("1")) {
            bzsr.runScript("scripts/metaqueries_cont.sql");
        } else if (linkCorrelation.equals("1")) { //LinkCorrelations
            bzsr.runScript("scripts/metaqueries.sql");
        } else {
            bzsr.runScript("scripts/metaqueries.sql");
            // modified on Feb. 3rd, 2015, zqian, to include rnode as columns
        //			bzsr.runScript("scripts/metadata_2_nolink.sql");
        }
        bzsr.runScript("scripts/model_manager.sql");
        //why are we running the model manager first? //
        bzsr.runScript("scripts/metaqueries_RChain.sql");

        // building CT tables for Rchain
        CTGenerator();
        disconnectDB();
	}

	/**
	 *
	 * @param con_std
	 * @param con_BN
	 * @param con_CT
	 * @param con_setup
	 * @param databaseName_std
	 * @param linkCorrelation
	 * @param cont
	 * @throws Exception
	 */
	public static void buildCT( Connection con_std, Connection con_setup, Connection con_BN, Connection con_CT,
								String databaseName_std, String linkCorrelation,
								String cont) throws Exception {

        setVars(con_std, con_setup, con_BN, con_CT, databaseName_std,
                linkCorrelation, cont);
		

        BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,con_setup);
        bzsr.runScript("scripts/transfer.sql");

        
        maxNumberOfMembers = short_rnid_LatticeGenerator.generate(con_BN);
        
        if (cont.equals("1")) {
            bzsr.runScript("scripts/metadata_2_cont.sql");
        } else if (linkCorrelation.equals("1")) { //LinkCorrelations
            bzsr.runScript("scripts/metadata_2.sql");
        } else {
            bzsr.runScript("scripts/metadata_2.sql");
            // modified on Feb. 3rd, 2015, zqian, to include rnode as columns
            //			bzsr.runScript("scripts/metadata_2_nolink.sql");
        }
        // building CT tables for Rchain
        CTGenerator();
	}
    /**
     * Populates the <databaseName>_BN schema with tables. (Refer transfer2.sql)
     * Generates lattice  tree. (Refer Class: short_rnid_LatticeGenerator , Method: generateTarget())
     * Executes metadata_3.sql
     *
     * @return int : maximum Number Of Members
     * @throws Exception
     */
	public static int buildCTTarget() throws Exception {
	      
		setVarsFromConfigForTarget();
		//connect to db using jdbc
		con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);
		//build _BN part1 from metadata_1.sql

		BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,dbbase,con_setup);
		bzsr.runScript("scripts/transfer2.sql");
		con_BN = connectDB(databaseName_BN);
    	con_CT = connectDB(databaseName_CT);

		//generate lattice tree
		maxNumberOfMembers = short_rnid_LatticeGenerator.generateTarget(con_BN);// rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
		System.out.println(" ##### lattice is ready for use* ");
		
		//build _BN part2: from metadata_2.sql      
		if (cont.equals("1")) {
			bzsr.runScript("scripts/metadata_3_cont.sql");
		} else if (linkCorrelation.equals("1")) { //LinkCorrelations
			bzsr.runScript("scripts/metadata_3.sql");
		} else {
			bzsr.runScript("scripts/metadata_3_nolink.sql");
			// modified on Feb. 3rd, 2015, zqian, to include rnode as columns
		}
		
        // building CT tables for Rchain
        CTGenerator();
        
		disconnectDB();
		
		return maxNumberOfMembers;
	}


    /**
     * @Overload
     * buildSubCTTarget
     * Compute the subset given factor id (fid) and it's parents
     * @update Aug. 19, 2014, zqian
     * @param functorId
     * @param database_BN
     * @param database_target
     * @param database_target_bn
     * @param database_db
     * @return
     * @throws Exception
     */

	public static int buildSubCTTarget(String functorId,String database_BN,String database_target,String database_target_bn,String database_db) throws Exception { 
	      
		setVarsFromConfigForTarget();
		//connect to db using jdbc
		con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);
		//build _BN part1 from metadata_1.sql

		BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,dbbase,con_setup);
		bzsr.runScript("scripts/transfer2.sql");
		con_BN = connectDB(databaseName_BN);
        con_CT = connectDB(databaseName_CT);

		//generate lattice tree
		maxNumberOfMembers = short_rnid_LatticeGenerator.generateTarget(con_BN);// rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
		System.out.println(" ##### lattice is ready for use* ");
		
		//build _BN part2: from metadata_2.sql      
		if (cont.equals("1")) {
			bzsr.runScript("scripts/metadata_3_cont.sql");
		} else if (linkCorrelation.equals("1")) { //LinkCorrelations
			bzsr.runScript("scripts/metadata_3.sql");
		} else {
			bzsr.runScript("scripts/metadata_3_nolink.sql");
			//buildSubCTTarget modified on Feb. 3rd, 2015, zqian, to include rnode as columns
		}

        // building CT tables for Rchain
        // CTGenerator();
        //compute the subset given fid and it's markov blanket
        //SubsetCTComputation.computeTargetSubset_CTs(functorId,database_BN,database_target,database_target_bn,database_db); //unielwin_training1_target_db

        disconnectDB();
		
		return maxNumberOfMembers;
	}


    /**
     *  @Overload
     *  buildSubCTTarget
     *  Compute the subset given fid and it's child with child's parents
     *  @param functorId
     *  @param database_BN
     *  @param database_target
     *  @param database_target_bn
     *  @param database_db
     *  @param child
     *  @param child1
     *  @return int
     */
	public static int buildSubCTTarget(String functorId,String database_BN,String database_target,String database_target_bn,String database_db,String child,String child1) throws Exception { 
	      
		setVarsFromConfigForTarget();
		//connect to db using jdbc
		con_std = connectDB(databaseName_std);
        con_setup = connectDB(databaseName_setup);

		//build _BN part1 from metadata_1.sql

		BZScriptRunner bzsr = new BZScriptRunner(databaseName_std,dbbase,con_setup);
		bzsr.runScript("scripts/transfer2.sql");
		con_BN = connectDB(databaseName_BN);
    con_CT = connectDB(databaseName_CT);

		//generate lattice tree
		maxNumberOfMembers = short_rnid_LatticeGenerator.generateTarget(con_BN);// rnid mapping. maxNumberofMembers = maximum size of lattice element. Should be called LatticeHeight
		System.out.println(" ##### lattice is ready for use* ");
		
		//build _BN part2: from metadata_2.sql      
		if (cont.equals("1")) {
			bzsr.runScript("scripts/metadata_3_cont.sql");
		}
		else if (linkCorrelation.equals("1")) {
			bzsr.runScript("scripts/metadata_3.sql");
		}
		else {
			bzsr.runScript("scripts/metadata_3_nolink.sql");

			// modified on Feb. 3rd, 2015, zqian, to include rnode as columns
		}

        // building CT tables for Rchain
        // CTGenerator();
        //compute the subset given fid and it's markov blanket
        //SubsetCTComputation.computeTargetSubset_CTs(functorId,database_BN,database_target,database_target_bn,database_db,child,child1); //unielwin_training1_target_db
        
		disconnectDB();
		
		return maxNumberOfMembers;
	}
	
	
	/**
	 *  Building the _CT tables for length >=2
	 * 	For each length
     * 	1. find rchain, find list of members of rc-hain. Suppose first member is rnid1.
     * 	2. initialize current_ct = rchain_counts after summing out the relational attributes of rnid1.
     * 	3. Current list = all members of rchain minus rndi1. find ct(table) for current list = . Select rows where all members of current list are true. Add 1nodes of rnid1.
     * 	4. Compute false table using the results of 2 and 3 (basically 2 - 3).
     * 	5. Union false table with current_ct to get new ct where all members of current list are true.
     * 	6. Repeat with current list as initial list until list is empty.
     * 	Example:
     * 	1. Rchain = R3,R2,R1. first rnid1 = R3.
     * 	2. Find `R3,R2,R1_counts`. Sum out fields from R3 to get `R2,R1-R3_flat1`.
     * 	3. Current list = R2,R1. Find `R2,R1_ct` where R1 = T, R2 = T. Add 1nodes of R3 (multiplying) to get `R2,R1-R3_star`.
     * 	4. Compute `R2,R1-R3_false` = `R2,R1-R3_star - `R2,R1-R3_flat1` union (as before)
     * 	5. Compute `R2,R1-R3_ct` = `R2,R1-R3_false` cross product `R3_join` union `R3,R2,R1_counts`.
     * 	6. Current list = R1. Current rnid = R2. Current ct_table = `R2,R1-R3_ct`.
     *
     * 	BuildCT_Rnodes_flat(len);
     *
     * 	BuildCT_Rnodes_star(len);
     *
     * 	BuildCT_Rnodes_CT(len);
     *
	 * @throws Exception
	 */
	public static void CTGenerator() throws Exception{
		
		long l = System.currentTimeMillis(); //@zqian : CT table generating time
		   // handling Pvars, generating pvars_counts		
        BuildCT_Pvars();
        
        // preparing the _join part for _CT tables
		BuildCT_Rnodes_join();
		
		//building the RNodes_counts tables. should be called Rchains since it goes up the lattice.
		if(linkCorrelation.equals("1")) {
			long l_1 = System.currentTimeMillis(); //@zqian : measure structure learning time
			for(int len = 1; len <= maxNumberOfMembers; len++){
				BuildCT_Rnodes_counts(len);
			}
			long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
			System.out.print("Building Time(ms) for Rnodes_counts: "+(l2-l_1)+" ms.\n");
		}
		else {
			System.out.println("link off !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for(int len = 1; len <= maxNumberOfMembers; len++)
				BuildCT_Rnodes_counts2(len);
			//count2 simply copies the counts to the CT tables
			//copying the code seems very inelegant OS August 22

		}
                                                                      
		if (linkCorrelation.equals("1")) {
			// handling Rnodes with Lattice Moebius Transform
            //initialize first level of rchain lattice
			for(int len = 1; len <= 1; len++) {
				System.out.print("Building Time(ms) for Rchain =1 \n");
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
				System.out.println("now we're here for Rchain!");
				System.out.print("Building Time(ms) for Rchain >=2 \n");
				BuildCT_RChain_flat(len);
				System.out.println(" Rchain! are done");
			}
		}
		

		//delete the tuples with MULT=0 in the biggest CT table
		String BiggestRchain="";
		Statement st_BN= con_BN.createStatement();
		ResultSet rs = st_BN.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );

		boolean RChainCreated = false;
		while(rs.next())
		{
			RChainCreated = true;
			BiggestRchain = rs.getString("RChain");
			System.out.println("\n BiggestRchain : " + BiggestRchain);
		}
		
		st_BN.close();
		
		if ( RChainCreated )
		{
			Statement st_CT = con_CT.createStatement();
			try
			{
				st_CT.execute("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );
				System.out.println("delete from `"+BiggestRchain.replace("`", "") +"_CT` where MULT='0';" );

			}
			catch ( MySQLSyntaxErrorException e )
			{
				//Do nothing
			}
			st_CT.close();
		}
		
		long l2 = System.currentTimeMillis();  //@zqian
		System.out.print("Building Time(ms) for ALL CT tables:  "+(l2-l)+" ms.\n");
	}

    /**
     * handleWarnings
     * @throws SQLException
     */
    static void handleWarnings() throws SQLException {
        String warning = "";
        warning += buildWarningString(con_BN, "TernaryRelations", "of having a three column key");
        warning += buildWarningString(con_BN, "NoPKeys", "of not having a primary key");
        if(warning.length() > 0){
            JOptionPane.showMessageDialog(null, warning);
        }
    }

    /**
     * buildWarningString
     * @param con
     * @param checkTableName
     * @param reason
     * @return
     * @throws SQLException
     */
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

    /**
     * setVarsFromConfigForTarget
     */
	public static void setVarsFromConfigForTarget()
	{
		Config conf = new Config();
		databaseName_std = conf.getProperty("dbname");
		dbbase = databaseName_std;
		databaseName_std += "_target";
		databaseName_BN = databaseName_std + "_BN";
		databaseName_CT = databaseName_std + "_CT";
		databaseName_setup = databaseName_std + "_setup";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
		linkCorrelation = conf.getProperty("LinkCorrelations");
		cont = conf.getProperty("Continuous");
		
		if ( conf.closeFile() != 0 )
		{
			System.out.println( "Failed to close file!" );
		}
	}

    /**
     * setVarsFromConfig
     * ToDo : Remove Duplicate definitions across java files
     */
	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName_std = conf.getProperty("dbname");
		//dbbase = conf.getProperty("dbbase");
		databaseName_BN = databaseName_std + "_BN";
		databaseName_CT = databaseName_std + "_CT";
		databaseName_setup = databaseName_std + "_setup";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
		linkCorrelation = conf.getProperty("LinkCorrelations");
		cont = conf.getProperty("Continuous");
	}

    /**
     * Set vars for paramterised call to method buildCT
     * @param connection_std
     * @param connection_BN
     * @param connection_CT
     * @param connection_setup
     * @param databaseName_std
     * @param linkCorrelation
     * @param continuous
     */
    public static void setVars( Connection connection_std, Connection connection_setup, 
    							Connection connection_BN, Connection connection_CT, 
    							String databaseName_std, 
                                String linkCorrelation,
                                String continuous){

		BayesBaseCT_SortMerge.databaseName_std = databaseName_std;
		BayesBaseCT_SortMerge.databaseName_BN = databaseName_std + "_BN";
		BayesBaseCT_SortMerge.databaseName_CT = databaseName_std + "_CT";
		BayesBaseCT_SortMerge.databaseName_setup = databaseName_std + "_setup";
		BayesBaseCT_SortMerge.linkCorrelation = linkCorrelation;
		BayesBaseCT_SortMerge.cont = continuous;
		
		BayesBaseCT_SortMerge.con_std = connection_std;
		BayesBaseCT_SortMerge.con_BN = connection_BN;
		BayesBaseCT_SortMerge.con_CT = connection_CT;
		BayesBaseCT_SortMerge.con_setup = connection_setup;
	}
    
    /**
     * Connect to database via MySQL JDBC driver
     */
	public static Connection connectDB(String databaseName) throws SQLException{
		String CONN_STR = "jdbc:" + dbaddress + "/" + databaseName;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		return (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
	}
		
		
    /**
     * Building the _CT tables. Going up the Rchain lattice ( When rchain.length >=2)
     * @param int : length of the RChain
     * @throws  SQLException
     * @throws  IOException
     */
	public static void BuildCT_RChain_flat(int len) throws SQLException, IOException {
		System.out.println("\n ****************** \n" +
				"Building the _CT tables for Length = "+len +"\n" );

		long l = System.currentTimeMillis(); 

		Statement st = con_BN.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        int fc=0;
		while(rs.next())
		{
			long l1 = System.currentTimeMillis(); 

			//System.out.print("fc ::"+ fc);
			//  get pvid for further use
			String rchain = rs.getString("RChain");
			System.out.println("\n rchain String : " + rchain );
			// Oct 16 2013
			// initialize the cur_CT_Table, at very beginning we will use _counts table to create the _flat table
			String 	cur_CT_Table="`"+rchain.replace("`", "")+"_counts`";  
			System.out.println(" cur_CT_Table : " + cur_CT_Table);

			//  create new statement
			Statement st1 = con_BN.createStatement();
			ResultSet rs1 = st1.executeQuery("SELECT distinct parent, removed as rnid FROM lattice_rel  where child = '"+rchain+"' order by rnid ASC;"); // memebers of rchain
			
			while(rs1.next())
			{		
				long l2 = System.currentTimeMillis(); 
				String parent = rs1.getString("parent");
				//	System.out.println("\n parent : " + parent);
				String rnid = rs1.getString("rnid");
				//	System.out.println("\n rnid : " + rnid);	
				String BaseName = "`"+rchain.replace("`", "")+"_"+rnid.replace("`", "")+"`";
				System.out.println(" BaseName : " + BaseName );
				
				Statement st2 = con_BN.createStatement();
				Statement st3 = con_CT.createStatement();
					
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
				ResultSet rs15 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_star_Table.replace("`","")+"';");
				String IndexString = makeIndexQuery(rs15, "Entries", " , ");
				//System.out.println("Index String : " + IndexString);
				//System.out.println("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");
				st3.execute("alter table "+cur_star_Table+" add index "+cur_star_Table+"   ( "+IndexString+" );");       
				long l3 = System.currentTimeMillis(); 
				System.out.print("Building Time(ms) for "+cur_star_Table+ " : "+(l3-l2)+" ms.\n");
				//staring to create the _flat table
				// Oct 16 2013
				// here is the wrong version that always uses _counts table to generate the _flat table. 
				//String 	cur_CT_Table="`"+rchain.replace("`", "")+"_counts`";
				// cur_CT_Table should be the one generated in the previous iteration
				// for the very first iteration, it's _counts table
				System.out.println("cur_CT_Table is : " + cur_CT_Table);

				String cur_flat_Table = "`"+rnid.replace("`", "")+len+"_"+fc+"_flat`";
				String queryStringflat = "select sum("+cur_CT_Table+".`MULT`) as 'MULT', "+selectString + " from " +cur_CT_Table+" group by  "+ selectString +";" ;
				String createStringflat = "create table "+cur_flat_Table+" as "+queryStringflat;
				System.out.println("\n create flat String : " + createStringflat );			
				st3.execute(createStringflat);		//create flat table
			
				 //adding  covering index May 21
				//create index string
				ResultSet rs25 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_flat_Table.replace("`","")+"';");
				String IndexString2 = makeIndexQuery(rs25, "Entries", " , ");
				//System.out.println("Index String : " + IndexString2);
				//System.out.println("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");
				st3.execute("alter table "+cur_flat_Table+" add index "+cur_flat_Table+"   ( "+IndexString2+" );");
				long l4 = System.currentTimeMillis(); 
				System.out.print("Building Time(ms) for "+cur_flat_Table+ " : "+(l4-l3)+" ms.\n");
				/**********starting to create _flase table***using sort_merge*******************************/
				// starting to create _flase table : part1
				String cur_false_Table= "`"+rnid.replace("`", "")+len+"_"+fc+"_false`";
				
				//create false table					
				//Sort_merge5.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
				//Sort_merge4.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con3);
				Sort_merge3.sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table,con_CT);

				
				 //adding  covering index May 21
				//create index string
				ResultSet rs35 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_false_Table.replace("`","")+"';");
				String IndexString3 = makeIndexQuery(rs35, "Entries", " , ");
				//System.out.println("Index String : " + IndexString3);
				//System.out.println("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");
				st3.execute("alter table "+cur_false_Table+" add index "+cur_false_Table+"   ( "+IndexString3+" );");       
				long l5 = System.currentTimeMillis(); 
				System.out.print("Building Time(ms) for "+cur_false_Table+ " : "+(l5-l4)+" ms.\n");
		 
				// staring to create the CT table
				ResultSet rs_45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");
				String CTJoinString = makeUnionSepQuery(rs_45, "Entries", " , ");
				//System.out.println("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName3+"' and table_name = '"+cur_CT_Table.replace("`","")+"';");
				System.out.println("CT Join String : " + CTJoinString);
				
				String QueryStringCT = "select "+CTJoinString+" from "+cur_CT_Table + " union " + "select "+CTJoinString+" from " + cur_false_Table +", `" + rnid_or.replace("`", "") +"_join`";
				//System.out.println("\n Query String for CT Table: "+ QueryStringCT);
				
				//String Next_CT_Table="OS_Dummy";
				String Next_CT_Table="";
				if (rs1.next())
					Next_CT_Table="`"+BaseName.replace("`", "")+"_CT`";
				else 				 
					Next_CT_Table="`"+rchain.replace("`", "")+"_CT`";
					
				// Oct 16 2013
				// preparing the CT table for next iteration
				cur_CT_Table = Next_CT_Table;	
				
				//System.out.println("\n name for Next_CT_Table : "+Next_CT_Table);
			 
				System.out.println("\n create CT table string: create table "+Next_CT_Table+" as " + QueryStringCT +"\n*****\n");
				st3.execute("create  table "+Next_CT_Table+" as " + QueryStringCT);	 //create CT table	
				rs1.previous();

				//adding  covering index May 21
				//create index string
				ResultSet rs45 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+Next_CT_Table.replace("`","")+"';");
				String IndexString4 = makeIndexQuery(rs45, "Entries", " , ");
				//System.out.println("Index String : " + IndexString4);
				//System.out.println("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");
				st3.execute("alter table "+Next_CT_Table+" add index "+Next_CT_Table+"   ( "+IndexString4+" );");       

				fc++;	
				
				//  close statements
				st2.close();			
				st3.close();
				long l6 = System.currentTimeMillis(); 
				System.out.print("Building Time(ms) for "+cur_CT_Table+ " : "+(l6-l5)+" ms.\n");
			}
			st1.close();
			rs1.close();
		}
		//System.out.println("count "+count+"\n");
		rs.close();
		st.close();
		long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
		//System.out.print("Building Time(ms): "+(l2-l)+" ms.\n");
		System.out.println("\n Build CT_RChain_TABLES for length = "+len+" are DONE \n" );
	}

	/* building pvars_counts*/
	public static void BuildCT_Pvars() throws SQLException, IOException {
		long l = System.currentTimeMillis(); //@zqian : measure structure learning time
		Statement st = con_BN.createStatement();
		st.execute("Drop schema if exists " + databaseName_CT + ";");
		st.execute("Create schema if not exists " + databaseName_CT + ";");
		ResultSet rs = st.executeQuery("select * from PVariables;");

		while(rs.next()){
			//  get pvid for further use
			String pvid = rs.getString("pvid");
			System.out.println("pvid : " + pvid);
			//  create new statement
			Statement st2 = con_BN.createStatement();
			Statement st3 = con_CT.createStatement();
			//  create select query string
			ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'SELECT';");
			String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
			//System.out.println("Select String : " + selectString);
			//  create from query string
			ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'FROM';");
			String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
			//System.out.println("From String : " + fromString);

			ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'GROUPBY';");
			String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
			//System.out.println("GroupBy String : " + GroupByString);

			/*
			 *  Check for groundings on pvid
			 *  If exist, add as where clause
			 */
			System.out.println( "con_BN:SELECT id FROM Groundings WHERE pvid = '"+pvid+"';" );
			
			ResultSet rsGrounding = null;
			String whereString = "";
			
			try
			{
				rsGrounding = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + pvid + "' and ClauseType = 'WHERE';");
			}
			catch( MySQLSyntaxErrorException e )
			{
				System.out.println( "No WHERE clause for groundings" );
			}
			
			if ( null != rsGrounding )
			{

				whereString = makeCommaSepQuery(rsGrounding, "Entries", " AND ");

			}
			
			System.out.println( "whereString:" + whereString );
			
			// OLD CODE
			
// 			ResultSet rsGrounding = null;
// 			try
// 			{
// 				rsGrounding = st2.executeQuery("SELECT id FROM Groundings WHERE pvid = '"+pvid+"';");
// 			}
// 			catch( MySQLSyntaxErrorException e )
// 			{
// 				System.out.println( "No Groundings table." );
// 			}

// 			String whereString = "";

// 			if ( null != rsGrounding )
// 			{
// 				if ( rsGrounding.absolute(1) )
// 				{
// 					System.out.println( "Grounding for pvid=" + pvid + ":" +
// 										rsGrounding.getString(1) );

// 					/*
// 					 * Get pvar id name
// 					 */
// 					Statement st0 = con_BN.createStatement();

// 					ResultSet rs0 = st0.executeQuery( "SELECT TABLE_NAME FROM PVariables " +
// 													  "WHERE pvid = '" + pvid + "';" );

// 					if ( !rs0.first() )
// 					{
// 						System.out.println( "Failed to get pvid." );
// 						return;
// 					}

// 					String pvidTableName = rs0.getString( 1 );

// 					rs0.close();

// 					rs0 = st0.executeQuery( "SELECT COLUMN_NAME FROM EntityTables " +
// 							  				"WHERE TABLE_NAME = '" + pvidTableName + "';" );

// 					if ( !rs0.first() )
// 					{
// 						System.out.println( "Failed to get pvid." );
// 						return;
// 					}

// 					String pvidActualId = rs0.getString( 1 );

// 					rs0.close();

// 					st0.close();

// 					whereString += " where " + pvid + "." +
// 								   pvidActualId + " = " +
// 								   rsGrounding.getString(1);
// 					System.out.println( "whereString:" + whereString );
// 				}

// 				rsGrounding.close();
// 			}
// // stop old code here

			//  create the final query
			String queryString = "Select " + selectString + " from " +
								 fromString + whereString;
								 
//this seems unnecessarily complicated even to deal with continuos variables. OS August 22, 2017

			if (!cont.equals("1"))
				if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;

			//System.out.println("Query String : " + queryString );
			System.out.println("Create String : " + "create table "+pvid+"_counts"+" as "+queryString );
			st3.execute("create table "+pvid+"_counts"+" as "+queryString);
			//adding  covering index May 21
			//create index string
			ResultSet rs4 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+pvid+"_counts';");
			String IndexString = makeIndexQuery(rs4, "Entries", " , ");
			//System.out.println("Index String : " + IndexString);
			st3.execute("alter table "+pvid+"_counts"+" add  index "+pvid+"_Index   ( "+IndexString+" );");

			//  close statements
			st2.close();
			st3.close();
		}

		rs.close();
		st.close();
		long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
		System.out.print("Building Time(ms) for Pvariables counts: "+(l2-l)+" ms.\n");
		System.out.println("\n Pvariables are DONE \n" );
	}

    /**
     * building the RNodes_counts tables
     *
     */
    public static void BuildCT_Rnodes_counts(int len) throws SQLException, IOException {

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get rnid for further use
            String rchain = rs.getString("RChain");
            System.out.println("\n RChain : " + rchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query stringt
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT';");
                                              
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //System.out.println("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'FROM';" );
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //System.out.println("From String : " + fromString);

            //  create where query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'WHERE';" );
            String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
            //System.out.println("Where String : " + whereString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;

            //  create group by query string
            // this seems unnecessarily complicated - isn't there always a group by clause? Okay not with continuous data, but still. Continuous probably requires a different approach. OS August 22
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'GROUPBY';");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //System.out.println("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                //System.out.println("Query String : " + queryString );
            }

            String createString = "create table `"+rchain.replace("`", "") +"_counts`"+" as "+queryString;
            System.out.println("create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //System.out.println("Index String : " + IndexString);
            //System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");


            //  close statements
            st2.close();
            st3.close();


        }

        rs.close();
        st.close();

        System.out.println("\n Rnodes_counts are DONE \n" );
    }

    /**
     * building the RNodes_counts tables,count2 simply copies the counts to the CT tables
     */
    public static void BuildCT_Rnodes_counts2(int len) throws SQLException, IOException {
       
         Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get rnid for further use
            String rchain = rs.getString("RChain");
            System.out.println("\n RChain : " + rchain);

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query stringt
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'SELECT';");
                                              
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //System.out.println("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'FROM';" );
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //System.out.println("From String : " + fromString);

            //  create where query string
            ResultSet rs4 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'WHERE';" );
            String whereString = makeCommaSepQuery(rs4, "Entries", " and ");
            //System.out.println("Where String : " + whereString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString + " where " + whereString;

            //  create group by query string
            // this seems unnecessarily complicated - isn't there always a group by clause? Okay not with continuous data, but still. Continuous probably requires a different approach. OS August 22
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + rchain + "' and ClauseType = 'GROUPBY';");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //System.out.println("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                //System.out.println("Query String : " + queryString );
            }

            String createString = "create table `"+rchain.replace("`", "") +"_counts`"+" as "+queryString;
            System.out.println("create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
            String IndexString = makeIndexQuery(rs5, "Entries", " , ");
            //System.out.println("Index String : " + IndexString);
            //System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");

            String createString_CT = "create table `"+rchain.replace("`", "") +"_CT`"+" as "+queryString;
            System.out.println("create String : " + createString_CT );
            st3.execute(createString_CT);

            //adding  covering index May 21
            //create index string
            
            ResultSet rs5_CT = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_CT';");
            String IndexString_CT = makeIndexQuery(rs5_CT, "Entries", " , ");
            //System.out.println("Index String : " + IndexString);
            //System.out.println("alter table `"+rchain.replace("`", "") +"_counts`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_CT`"+" add index `"+rchain.replace("`", "") +"_Index`   ( "+IndexString_CT+" );");


            //  close statements
            st2.close();
            st3.close();


        }

        rs.close();
        st.close();
        System.out.println("\n Rnodes_counts are DONE \n" );
    }

    /**
     * building the _flat tables
     */
    public static void BuildCT_Rnodes_flat(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get pvid for further use
            String rchain = rs.getString("RChain");
            System.out.println("\n rchain String : " + rchain );

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();


            //  create select query string
            ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_Select_List  WHERE NAME = '" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_1Nodes_Select_List.rnid;");
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //System.out.println("Select String : " + selectString);

            //  create from query string
            ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_FROM_List WHERE NAME = '" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_1Nodes_FROM_List.rnid;");
            String fromString = makeCommaSepQuery(rs3, "Entries", " , ");
            //System.out.println("From String : " + fromString);

            //  create the final query
            String queryString = "Select " + selectString + " from " + fromString ;

            //  create group by query string
            if (!cont.equals("1")) {
                ResultSet rs_6 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_1Nodes_GroupBY_List WHERE NAME = 	'" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_1Nodes_GroupBY_List.rnid;");
                String GroupByString = makeCommaSepQuery(rs_6, "Entries", " , ");
                //System.out.println("GroupBy String : " + GroupByString);

                if (!GroupByString.isEmpty()) queryString = queryString + " group by"  + GroupByString;
                System.out.println("Query String : " + queryString );
            }

            String createString = "create table `"+rchain.replace("`", "") +"_flat`"+" as "+queryString;
            System.out.println("\n create String : " + createString );
            st3.execute(createString);

            //adding  covering index May 21
            //create index string
            ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_flat';");
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
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        System.out.print("Building Time(ms) for Rnodes_flat: "+(l2-l)+" ms.\n");
        System.out.println("\n Rnodes_flat are DONE \n" );
    }

    /**
     * building the _star tables
     */
    public static void BuildCT_Rnodes_star(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get pvid for further use
            String rchain = rs.getString("RChain");
            System.out.println("\n rchain String : " + rchain );

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create select query string
            ResultSet rs2 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_Select_List  WHERE NAME = '" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_Star_Select_List.rnid;");
            String selectString = makeCommaSepQuery(rs2, "Entries", " , ");
            //System.out.println("Select String : " + selectString);

            //  create from MULT string
            ResultSet rs3 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_Star_From_List.rnid;");
            String MultString = makeStarSepQuery(rs3, "Entries", " * ");
            //System.out.println("Mult String : " + MultString+ " as `MULT`");

            //  create from query string
            ResultSet rs4 = st2.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.orig_rnid = ADT_RNodes_Star_From_List.rnid;");
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
            ResultSet rs5 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_star';");
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
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        System.out.print("Building Time(ms) for Rnodes_star: "+(l2-l)+" ms.\n");
        System.out.println("\n Rnodes_star are DONE \n" );
    }

    /**
     * building the _false tables first and then the _CT tables
     */
    public static void BuildCT_Rnodes_CT(int len) throws SQLException, IOException {
        long l = System.currentTimeMillis(); //@zqian : measure structure learning time
        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = " + len + ";");
        while(rs.next()){

            //  get pvid for further use
            String rchain = rs.getString("RChain");
            System.out.println("\n rchain String : " + rchain );

            //  create new statement
            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();
            /**********starting to create _flase table***using sort_merge*******************************/
    		//Sort_merge5.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
            //Sort_merge4.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con3);
            Sort_merge3.sort_merge("`"+rchain.replace("`", "")+"_star`","`"+rchain.replace("`", "") +"_flat`","`"+rchain.replace("`", "") +"_false`",con_CT);

          	//adding  covering index May 21
            //create index string
            ResultSet rs15 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_false';");
            String IndexString = makeIndexQuery(rs15, "Entries", " , ");
            //System.out.println("Index String : " + IndexString);
        	//	System.out.println("alter table `"+rchain.replace("`", "") +"_false`"+" add index `"+rchain.replace("`", "") +"_false`   ( "+IndexString+" );");
            st3.execute("alter table `"+rchain.replace("`", "") +"_false`"+" add index `"+rchain.replace("`", "") +"_false`   ( "+IndexString+" );");



            //building the _CT table        //expanding the columns // May 16
           	// must specify the columns, or there's will a mistake in the table that mismatch the columns
            ResultSet rs5 = st3.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_counts';");
            // reading the column names from information_schema.columns, and the output will remove the "`" automatically,
            // however some columns contain "()" and MySQL does not support "()" well, so we have to add the "`" back.
            String UnionColumnString = makeUnionSepQuery(rs5, "Entries", " , ");
            //System.out.println("Union Column String : " + UnionColumnString);

            String createCTString = "create table `"+rchain.replace("`", "") +"_CT`"+" as select "+UnionColumnString+ " from `"+rchain.replace("`", "") +"_counts` union " +
            "select "+UnionColumnString+" from `"+rchain.replace("`", "") +"_false`, `"+rchain.replace("`", "") +"_join`;" ;
            System.out.println("\n create CT table String : " + createCTString );
            st3.execute(createCTString);

          	//adding  covering index May 21
            //create index string
            ResultSet rs25 = st2.executeQuery("select column_name as Entries from information_schema.columns where table_schema = '"+databaseName_CT+"' and table_name = '"+rchain.replace("`", "") +"_CT';");
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
        long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
        System.out.print("Building Time(ms) for Rnodes_false and Rnodes_CT: "+(l2-l)+" ms.\n");
        System.out.println("\n Rnodes_false and Rnodes_CT  are DONE \n" );
    }

    /**
     * preparing the _join part for _CT tables
     * @throws SQLException
     * @throws IOException
     */
    public static void BuildCT_Rnodes_join() throws SQLException, IOException {
        //set up the join tables that represent the case where a relationship is false and its attributes are undefined //

        Statement st = con_BN.createStatement();
        ResultSet rs = st.executeQuery("select orig_rnid, short_rnid from LatticeRNodes ;");

        while(rs.next()){
        //  get rnid
            String short_rnid = rs.getString("short_rnid");
            System.out.println("\n short_rnid : " + rnid);
            String orig_rnid = rs.getString("orig_rnid");
            System.out.println("\n orig_rnid : " + rnid);

            Statement st2 = con_BN.createStatement();
            Statement st3 = con_CT.createStatement();

            //  create ColumnString
            ResultSet rs2 = st2.executeQuery("select distinct Entries from MetaQueries where Lattice_Point = '" + short_rnid + "' and TableType = 'JOIN';");

            String ColumnString = makeCommaSepQuery(rs2, "Entries", " , ");
            ColumnString = orig_rnid + " varchar(5) ," + ColumnString;
            //if there's no relational attribute, then should remove the "," in the ColumnString
            if (ColumnString.length() > 0 && ColumnString.charAt(ColumnString.length()-1)==',')
            {
                ColumnString = ColumnString.substring(0, ColumnString.length()-1);
            }
                //System.out.println("Column String : " + ColumnString);
            String createString = "create table `"+short_rnid.replace("`", "") +"_join` ("+ ColumnString +");";
                System.out.println("create String : " + createString);

            st3.execute(createString);
            st3.execute("INSERT INTO `"+short_rnid.replace("`","")+"_join` ( "+orig_rnid + ") values ('F');");

            st2.close();
            st3.close();

        }
        rs.close();
        st.close();
        System.out.println("\n Rnodes_joins are DONE \n" );
    }

    /**
     * for _false: union part
     * @param rs
     * @param colName
     * @param del
     * @param Rchain
     * @return
     * @throws SQLException
     */
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

    /**
     * for _star  adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
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


    /**
     * for _CT part, adding "`"
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeUnionSepQuery(ResultSet rs, String colName, String del) throws SQLException {
	
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            parts.add("`"+rs.getString(colName)+"`");
        }
        return StringUtils.join(parts,del);
    }

    /**
     * separate the entries by ","
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
    public static String makeCommaSepQuery(ResultSet rs, String colName, String del) throws SQLException {
		
		ArrayList<String> parts = new ArrayList<String>();
		while(rs.next()){
			parts.add(rs.getString(colName));
		}
		return StringUtils.join(parts,del);
	}

    /**
     * for len>1, false table, part 1, where string
     * @param rs
     * @param colName
     * @param del
     * @param cur_flat_Table
     * @param cur_star_Table
     * @return
     * @throws SQLException
     */
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

    /**
     * for len>1, false table, part 1, part 2, select string
     * @param rs
     * @param colName
     * @param del
     * @param cur_flat_Table
     * @return
     * @throws SQLException
     */
    public static String makeFalseSepQuery(ResultSet rs, String colName, String del, String cur_flat_Table) throws SQLException {
        ArrayList<String> parts = new ArrayList<String>();

        while(rs.next()){
            String temp=rs.getString(colName);
            String temp1=temp.replace("`", "");
            temp = "`"+cur_flat_Table.replace("`", "") +"`.`"+temp1+"`";
            parts.add(temp);
        }

        return StringUtils.join(parts,del);
    }

    /**
     * Making Index Query by adding "`" and appending ASC
     * @param rs
     * @param colName
     * @param del
     * @return
     * @throws SQLException
     */
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

        return StringUtils.join(parts,del);
    }

    /**
     * Get columns from the metaData
     * @param rs
     * @return
     * @throws SQLException
     */
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

    /**
     * Disconnect all the databases
     * @throws SQLException
     */
	public static void disconnectDB() throws SQLException {
		con_std.close();
		con_BN.close();
		con_CT.close();
		con_setup.close();
	}
}

