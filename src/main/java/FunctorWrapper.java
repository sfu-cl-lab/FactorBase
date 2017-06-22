/* Check the RunningFlow.txt for first time running. May 20th, zqian
 * */
/*May 7th, 
 * Using natural join to compute the _Score, we may lose some combinations occur in testing data, but NOT in training data.
 * two possible solutions:
 * 1. statistically, it makes sense to ignore some instance that do not occur in training data, or assign the probability to 0
 *  and then only evaluate the instance occurred.
 *  ACC = (# of correct values in _Score table compared with testing data )/(# of instances occurred in the _Score table) 
 * 2. or could use natural right join to make sure all combinations in the _ct_e tables occurs in the _Score table,(i.e. _cp natural right join _ct)
 * but need to pay special attention to the null value by using coalesce(column,0)
 * i.e. Syntax: COALESCE(x, y) = (CASE WHEN V1 IS NOT NULL THEN V1 ELSE V2 END)
 * ACC = (# of correct values in _Score table compared with testing data )/(# of instances in testing data)
 * 
 * Tried on the on Mondial_Traing1/_Test1, choose option 1 based on higher ACC ?
 * 
 * 3. instead on set to 0, try using prior probability? May 9th
 * 
 * */

/*May 6th, only processing 1Nodes with with binary values,  
 * ready for cross validation, copy the target database from testing database in MakeTargetSetup.java,  modify argValues in FunctorWrapper.java 
 *  for extended ct tables: set freq to 0 if is null
 * */
/*May 5th. in the target_score_e table: sum_mult should before the natural join, otherwise the sum is double counted
 * At present, divide the sum_mult by two, since we only processing binary nodes.
 * But eventully, we have to do the summation before the natural join.
 * 
 * May 1st, only processing the target nodes with binary values.
 * */

/*do not do this.
 * multiple classes, evaluation? missing some combinations, try to use _CP_smoothed,*/

/*April 30, compute the probability only for entries with the highest score and store them in target_score table
 * */

/* the table name limit is 64 characters, so HAVE TO change the name convention. #### to do ###
 * e.g. `capability(prof0,student0)_salary(prof0,student0)_final_CT_extended` is too long
 * so currently using `capability(prof0,student0)_salary(prof0,student0)_CT`
 * 	and `capability(prof0,student0)_salary(prof0,student0)_CT_e` (short for %_extended) should be fine.
 * 
 * Name Convention:
 * the local CT tables and extended CT tables
 * -- for each functorId
 * 		-- for its parents: target_parent_ct, target_parent_ct_e (i.e. natural join %_ct with Attribute_Value)
 * 		-- for its children:
 * 	   		-- for each child: target_child_ct, target_child_ct_e
 * 
 * the local score tables (i.e. natural join %_ct_e with _CP)
 * -- for each functorId
 * 		-- for its parents: target_parent_final
 * 		-- for its children:
 * 	   		-- for each child: target_child_final
 * 
 * the total score
 * -- for each functorId
 * 	 -- target_sum
 *      
 * */

/*Notes:
 1. if the target node is rnode, then remove the associated 2Nodes in the family. (done)
  -- in the BN, given by the schema edges, rnode is always the parent of associated 2node
  -- so only need to handle the target_child_ct tables
  
 2. for evaluation: create the extended _final_ct_extended table: (done)
  --for each functorId
   -- for each _final_ct 
    -- drop the target column in final_ct table, 
     -- create _fianl_ct_extended by join final_ct with Attribute_Value with specific target node
      --  natural join _extended table with _cp table

3. if the target node is 2node, then only considering the the situation when Rnode is True. (DONE)
	--for 2node in table Attribute_Value, there are only values give Rnode is True. 
	
 */

/* April 2014, zqian, converting a BN to dependency network(DN), using FunctorWrapper to add groundings for local CT tables 
 * 
 * Author: Kurt Routley
 * Date: September 23, 2013 
 */

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class FunctorWrapper
{
	static Connection con0, con1, conFinal,con_preprocess;
	static String databaseName, databaseName1, databaseName2, databaseName3, databaseName4, databaseName5;
	// 				unielwin, unielwin_target, unielwin_target_setup, unielwin_target_final_CT, unielwin_target_CT, unielwin_target_final
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static int groundingCount;
	static String functorId;
	static boolean processingRNode = false;
	// Global variable: true to use nodes in Markov Blanket, false to use only functor's parents and associated RNodes
	static boolean useMarkovBlanket = true;

	public static void main(String[] args) throws Exception
	{
		long time1=System.currentTimeMillis();
		/*
		 * Assume @database@_target_setup is already created
		 * FUTURE: Need to create this if not already done.
		 */
		/*
		 * Create Markov Blanket
		 */
		MarkovBlanket.runMakeMarkovBlanket();
		/*
		 *Target  Database Setup
		 */
		MakeTargetSetup.runMakeTargetSetup();
		
		/*
		 *Read for FunctorWrapper
		 */
		setVarsFromConfig();
		
		//pre_process( );
		
		connectDBTargetSetup();
		
		ArrayList<String> functors = GetFunctors();
		
		Statement st = con0.createStatement(); // connect to unielwin_target_setup
		
		st.execute( "DROP SCHEMA IF EXISTS " + databaseName3 + ";" ); //unielwin_target_final_CT
		st.execute( "CREATE SCHEMA " + databaseName3 + ";" );
		st.execute( "DROP SCHEMA IF EXISTS " + databaseName5 + ";" ); //unielwin_target_final
		st.execute( "CREATE SCHEMA " + databaseName5 + ";" );
		
		connectDBTargetFinalCT();
		connectDBTargetFinal();
		
		functorId = "";
		String node = "";
		String table = "";
		
		if ( useMarkovBlanket )
		{
			node = "TargetMBNode";
			table = "TargetMB";
		}
		else
		{
			node = "TargetParent";
			table = "TargetParents";
		}
		
		//functorId = "`teachingability(prof0)`";//zqian April 10th
		//functorId= "`intelligence(student0)`";
		//functorId= "`b`";
		//functorId= "`a`";
		//functorId = "`rating(course0)`";

		for ( int i = 0; i < functors.size(); i++ )  //zqian April 10th
		{
			functorId = functors.get(i); //zqian April 10th
			// unielwin_target_setup
			// 1Nodes  
			st.execute( "CREATE TABLE  if not exists 1Nodes AS SELECT * FROM " + databaseName + "_BN.1Nodes WHERE 1nid IN (SELECT " +
					    node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or 1nid='" +   functorId + "';" );
			
			// 2Nodes 
			st.execute( "CREATE TABLE  if not exists 2Nodes AS SELECT * FROM " + databaseName + "_BN.2Nodes WHERE 2nid IN (SELECT " +
					    node + " FROM " + databaseName + "_BN." + table +   " WHERE TargetNode = '" + functorId + "') or 2nid='" +  functorId + "';" );
			
			// RNodes 
			st.execute( "CREATE TABLE  if not exists RNodes AS SELECT * FROM " +  databaseName + "_BN.RNodes WHERE rnid IN (SELECT " +
						node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or rnid " + 
					    "IN (SELECT rnid FROM " + databaseName + "_BN.RNodes_2Nodes WHERE 2nid IN (SELECT  2nid FROM " +
					    databaseName + "_target_setup.2Nodes)) or rnid='" + functorId + "';" );
			
			// Get pvars for functor */
			System.out.println("Functor: '"+functorId+"'");
			ArrayList<String> functorArgs = GetFunctorArgs( functorId );
			
			// Perform Groundings assignments, Run CTGenerator for each Grounding 
			ArrayList<String> functorArgValues = new ArrayList<String>();
			// counting the grounding number 
			groundingCount = 1;
			// create the sub_CT tables with groundings
			
			st.execute( "DROP TABLE IF EXISTS Test_Node;" );			 
			st.execute( "CREATE TABLE `Test_Node` (  `FID` varchar(199) NOT NULL, PRIMARY KEY (`FID`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
			st.execute(" INSERT INTO `Test_Node` (`FID`) VALUES('"+functorId+"') ;");
			
			
			GetGroundings( functorArgs, functorArgValues, 0, functorId  );
			
			// Post-processing on *_final_CT column names for RNodes 
			postProcess( st );
			
			// Extend the final_CT tables for each functorId with all possible values 
			Extend_Final_CT();
			// remove the n/a ??
			Update_CT();
			// Create *_final table, target_parent/target_child_final	 
			//createSubFinal_zqian( functorArgs ); //using natural join,
			
			//create the target_sum by summation
			//compute the probability only for entries with the highest score and store them in target_score table, April 30
			//createFinal_zqian( functorArgs );
			
			// update the null value to 0, May 7th, zqian
			createSubFinal2_zqian( functorArgs ); // using natural right join
			//createFinal2_zqian( functorArgs ); // set null to 0
			createFinal3_zqian( functorArgs ); // set null to -50
			// Cleanup for next functor
			st.execute( "DROP TABLE 1Nodes;" );
			st.execute( "DROP TABLE 2Nodes;" );
			st.execute( "DROP TABLE RNodes;" );
		}
		
		st.close();		
		disconnectDBTargetFinalCT();
		disconnectDB();
		disconnectDBFinal();
		
		long time2=System.currentTimeMillis();
		
		System.out.println( "Total FunctorWrapper time: " + ( time2 - time1 ) );
		
		
		
	}
	
	public static void setVarsFromConfig()
	{
		Config conf		= new Config();
		databaseName	= conf.getProperty("dbname");
		databaseName1	= databaseName + "_target";
		databaseName2	= databaseName1 + "_setup";
		databaseName3	= databaseName1 + "_final_CT";
		databaseName4	= databaseName1 + "_CT";
		databaseName5	= databaseName1 + "_final";
		dbUsername		= conf.getProperty("dbusername");
		dbPassword		= conf.getProperty("dbpassword");
		dbaddress		= conf.getProperty("dbaddress");
	}

	public static void connectDBTargetSetup() throws SQLException
	{
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
		try 
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch ( Exception ex ) 
		{
			System.err.println( "Unable to load MySQL JDBC driver" );
		}
		con0 = (Connection) DriverManager.getConnection( CONN_STR2, 
														 dbUsername, 
														 dbPassword );
	}
	
	public static void connectDBTargetFinalCT() throws SQLException
	{
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName3;
		try 
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch ( Exception ex ) 
		{
			System.err.println( "Unable to load MySQL JDBC driver" );
		}
		con1 = (Connection) DriverManager.getConnection( CONN_STR2, 
														 dbUsername, 
														 dbPassword );
	}
	
	public static void connectDBTargetFinal() throws SQLException
	{
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName5;
		try 
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch ( Exception ex ) 
		{
			System.err.println( "Unable to load MySQL JDBC driver" );
		}
		conFinal = (Connection) DriverManager.getConnection( CONN_STR2, 
														 dbUsername, 
														 dbPassword );
	}
	
	public static void connectDB_preprocess() throws SQLException
	{
		String CONN_STR_preprocess = "jdbc:" + dbaddress + "/" + databaseName+"_BN";
		try 
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch ( Exception ex ) 
		{
			System.err.println( "Unable to load MySQL JDBC driver" );
		}
		con_preprocess = (Connection) DriverManager.getConnection( CONN_STR_preprocess, 
														 dbUsername, 
														 dbPassword );
	}
	
	public static void disconnectDB_preprocess() throws SQLException
	{
		con_preprocess.close();
	}
	
	public static void disconnectDB() throws SQLException
	{
		con0.close();
	}
	
	public static void disconnectDBTargetFinalCT() throws SQLException
	{
		con1.close();
	}
	
	public static void disconnectDBFinal()
	{
		try
		{
			conFinal.close();
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to close connection to final table." );
		}
	}
	
	// get the each functor, e.g. unielwin_BN
	public static ArrayList<String> GetFunctors() throws SQLException
	{
		Statement st1 = con0.createStatement();
		
		System.out.println( "Getting functors..." );
		
		ResultSet rs = st1.executeQuery( "SELECT distinct Fid from " + "FNodes;" );
		
		
		//only processing the target nodes with binary values, May 1st
//		ResultSet rs = st1.executeQuery( "SELECT distinct Fid from  FNodes "
//				+ " where FunctorName in "
//				+ "( select column_name from "
//				+ "  (SELECT count(*) as Number , column_name  FROM Attribute_Value group by column_name) C "
//				+ "  where C.Number =2"
//				+ ") "
//				+ " and Type = '1Node' and main ='1' ;" ); // processing 1Node first, only the main functor, May 6th
//				
				//+ " and Type = 'Rnode' and main ='1' ;" ); // try Rnode , May 8th
//		 // processing Gender in MovieLens_std, May 12
//		ResultSet rs = st1.executeQuery( "SELECT distinct Fid from  FNodes "
//				+ " where FunctorName = 'Gender' ;" ); 
		
		ArrayList<String> functors = new ArrayList<String>();
		
		while ( rs.next() )
		{
			functors.add( rs.getString( 1 ) );
		}
		
		
		st1.close();
		
		
		return functors;
	}
	
	public static ArrayList<String> GetFunctorArgs( String functorId )throws SQLException
	{
		Statement st1 = con0.createStatement();
		
		System.out.println( "Getting functor args..." );

		st1.execute( "USE " + databaseName2 + ";" );
		
		System.out.println( "SELECT pvid FROM FNodes_pvars WHERE Fid = '" + functorId + "';" );
		ResultSet rs = st1.executeQuery ( "SELECT pvid FROM FNodes_pvars " + "WHERE Fid = '" + functorId + "';" );
		
		ArrayList<String> argList = new ArrayList<String>();
		
		processingRNode = false;
		
		if ( !rs.first() )
		{
			System.out.println( "FNodes_pvars: Result set is empty, go to  RNodes_pvars !" );
			rs.close();
			
			rs = st1.executeQuery( "SELECT pvid FROM RNodes_pvars WHERE rnid " +  "= '" + functorId + "';" );
			
			if ( !rs.first() )
			{
				System.out.println( " RNodes_pvars :Result set is empty!" );
				rs.close();
				st1.close();
				
				System.out.println( "Functor " + functorId + " has " + argList.size() +	" arguments." );
				
				return argList;
			}
			
			processingRNode = true;
		}
		
		do
		{
			System.out.println("Added an argument: " + rs.getString(1) );
			argList.add( rs.getString(1) );
		} while ( rs.next() );
		
		rs.close();
		st1.close();
		
		System.out.println( "Functor " + functorId + " has " + argList.size() +" arguments." );
		
		return argList;
	}
	
	//static String functorId_orig= functorId;
	public static void GetGroundings( ArrayList<String> args,  ArrayList<String> argValues,  int index ,String functorId_orig) throws SQLException, Exception
	{		
		Statement st1 = con0.createStatement();
		/* Get table for pvar *//* Get pvar id name		 */
		ResultSet rs = st1.executeQuery( "SELECT TABLE_NAME FROM PVariables WHERE pvid = '" + args.get(index) +  "';" );
		rs.first();		
		String pvidTableName = rs.getString( 1 );
		rs.close();
		
		rs = st1.executeQuery( "SELECT COLUMN_NAME FROM " +  databaseName + "_BN.EntityTables WHERE TABLE_NAME = '" + pvidTableName + "';" );		
		if ( !rs.first() )
		{
			System.out.println( "Failed to get pvid." );			
			/* Paranoia: Cleanup */
			rs.close();
			st1.close();			
			return;
		}
		
		String pvid = rs.getString( 1 );
		rs.close();
		
//		/* Get argument values for pvar	 */
//			System.out.println( "SELECT " + pvid + " FROM " + databaseName + "." +   pvidTableName + ";" );
//		rs = st1.executeQuery( "SELECT " + pvid +  " FROM " + databaseName + "." +  pvidTableName + ";" );
//		
		/* Get argument values for pvar	 from Target dataset, May 6th, zqian, cross validation*/
		System.out.println( "SELECT " + pvid + " FROM " + databaseName1 + "." +   pvidTableName + ";" );
	rs = st1.executeQuery( "SELECT " + pvid +  " FROM " + databaseName1 + "." +  pvidTableName + ";" );
		
		while ( !rs.isClosed() && rs.next() )
		{
			argValues.add( rs.getString(1) );
			
			/* Get the rest of the argument assignments		 */
			if ( index != args.size()-1 )
			{
				GetGroundings( args, argValues, index + 1, functorId_orig  );
				argValues.remove(index);
				continue;
			}
			
			Statement st2 = con0.createStatement();
			st2.execute( "DROP TABLE IF EXISTS Groundings;" );
			st2.execute( "CREATE TABLE Groundings ( pvid varchar(40) NOT NULL, id varchar(256) NOT NULL, PRIMARY KEY (pvid,id) );" );
			
			for ( int i = 0; i < args.size(); i++ )
			{
				System.out.println("GROUNDINGS PREP: INSERT INTO Groundings Values( '" +  args.get(i) + "', '" + argValues.get(i) + "' );" );
				st2.execute ( "INSERT INTO Groundings Values( '" +  args.get(i) + "', '" + argValues.get(i) + "' );" );
			}			
			
			int max = BayesBaseCT_SortMerge.buildCTTarget();			
			
			/*
			 * Store Results
			 * 	1. Store Groundings in vertical alignment for join
			 *  2. Get largest rchain to get counts
			 *  3. Join GroundingsVertical to `<rchain>_counts` 
			 *  4. Store in functor final CT
			 */
			Statement st3 = con1.createStatement();			
			String tableString = "CREATE TABLE GroundingsVertical (GroundingsNumber INT";
			for ( int i = 0; i < args.size(); i++ )
			{
				tableString += ", "+args.get(i) + " INT";
			}
			tableString += ");";
				System.out.println( tableString );
			st3.execute( tableString );
			
			String insertString = "INSERT INTO GroundingsVertical Values (" + groundingCount;
			for ( int i = 0; i < argValues.size(); i++ )
			{
				insertString += ", " + argValues.get(i);
			}			
			insertString += ");";
				System.out.println( insertString );
			st3.execute( insertString );
			
			/*  2. */
			String rchainQuery = "SELECT name FROM " + databaseName1 + "_BN.lattice_set WHERE length = " + max + ";";
			System.out.println( rchainQuery );
			ResultSet rsRchain = st3.executeQuery( rchainQuery );
			
			String rchain = "";
			boolean rchainExists = false;			
			if ( rsRchain.absolute(1))
			{
				rchainExists = true;
				rchain = rsRchain.getString(1);
			}
			else
			{
				rchain = args.get(index);			
			}
			rsRchain.close();

			// process the rnid, April 11, zqian
			if ( processingRNode )
			{
				ResultSet rNodeName_t = st3.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	functorId_orig + "';" ); // `b`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
				}				
				rNodeName_t.close();				
				ResultSet rNodeName_t1 = st3.executeQuery( "SELECT rnid FROM " + databaseName1 +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`a`
				}
				rNodeName_t1.close();				
			}
/*********	// processing target's parents***************Begin****************/			
			// April 23,
			int target_parents_counts=0;
			String target_parents_counts_query ="SELECT count(TargetParent) FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+functorId_orig+"' ;";  //functorId in unielwin_BN
			System.out.println( "zqian : "+ target_parents_counts_query );
			Statement st_temp1 = con0.createStatement();
			ResultSet rstarget_parents_counts = st_temp1.executeQuery( target_parents_counts_query );	
			if (  rstarget_parents_counts.absolute( 1 ) )
			{
				target_parents_counts = rstarget_parents_counts.getInt(1);  // counts 
			}
			rstarget_parents_counts.close();	
			System.out.println( "zqian : rstarget_parents_counts "+ target_parents_counts );

			//should contain, current and its parents, zqian April 22nd, 2014, mapping the rnid?
			String target_parent_select_string= "mult, "+ functorId + " ,";
			String target_parents_query ="SELECT TargetParent FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+functorId_orig+"' ;";  //functorId in unielwin_BN

			System.out.println( "zqian : "+ target_parents_query );
			Statement st_temp = con0.createStatement();
			ResultSet rstarget_parents = st_temp.executeQuery( target_parents_query );	
			// zqian: concat each parent of the target 
			// begin while
			while ( rstarget_parents.next() && target_parents_counts !=0 )
			{  
				String current= rstarget_parents.getString(1);
				System.out.println("zqian: "+current+";");
				//check if current is Rnode or not, do the mapping again
				ResultSet r_t = st3.executeQuery( "SELECT Type FROM " + databaseName +"_BN.FNodes WHERE Fid = '" +	current + "';" ); // `b`
				if ( r_t.absolute( 1 ) )
				{
					System.out.println("zqian: "+r_t.getString(1)+";");
					if ( r_t.getString(1).compareTo("Rnode")==0)
					{
						ResultSet rNodeName_t = st3.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	current + "';" ); // `b`
						if ( rNodeName_t.absolute( 1 ) )
						{
							current = rNodeName_t.getString(1); //`registration(course0,student0)`
						}
						rNodeName_t.close();
						ResultSet rNodeName_t1 = st3.executeQuery( "SELECT rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE orig_rnid = '" +	current + "';" );
						if ( rNodeName_t1.absolute( 1 ) )
						{
							current = rNodeName_t1.getString(1);  //`a`
						}
						rNodeName_t1.close();		
					}  
				}				
				r_t.close();	
				target_parent_select_string += current + " ,";
			}//endwhile
			// for some target node that does not have any parents	
			if (target_parents_counts == 0)
			{	
				System.out.println("zqian: NO parents for target "+ functorId   );
				target_parent_select_string= "mult, "+ functorId + " ,";
			}			
		
			String tempTableString = "";
			if ( rchainExists )
			{
				tempTableString = "CREATE TABLE tempJoin AS SELECT * FROM " +  "(SELECT * FROM GroundingsVertical) G " +
						  "JOIN (SELECT "+target_parent_select_string.substring(0, target_parent_select_string.lastIndexOf(",")-1) +" FROM " + databaseName4 +  ".`" + rchain.replace( "`","" ) +  "_CT`) A;";
			}
			else
			{  // need to update using the very similar query
				tempTableString = "CREATE TABLE tempJoin AS SELECT * FROM " +  "(SELECT * FROM GroundingsVertical) G " +
								  "JOIN (SELECT * FROM " + databaseName4 +   ".`" + rchain.replace( "`","" ) +  "_counts`) A;";
			}
			
			System.out.println("zqian: "+ tempTableString);
			
			st3.execute( tempTableString );
			
			/*
			 *  Check if table already exists, If it doesn't, create it, Otherwise, insert new values into existing
			 */
			boolean exists = false;
			String functorIdhld = functorId;
			
			if ( processingRNode )
			{
				ResultSet rNodeName = st3.executeQuery( "SELECT orig_rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" );
				
				if ( rNodeName.absolute( 1 ) )
				{
					functorId = rNodeName.getString(1);
				}
				
				rNodeName.close();
			}
			
			String checkCTExists = "SHOW TABLES LIKE '" +  functorId.replace( "`", "" ) + "_parent_final_CT';";  //target_parent_final_ct
			System.out.println( checkCTExists );
			
			ResultSet rsExists = st3.executeQuery( checkCTExists );				
			if ( rsExists.absolute(1) )
			{
				exists = true;
			}				
			rsExists.close();
			
			if ( exists )
			{
				String insertNewGrounding = "INSERT INTO `" + functorId.replace( "`", "" ) + "_parent_final_CT` SELECT distinct * FROM " +	"tempJoin;"; //target_parent_final_ct
				System.out.println( insertNewGrounding );
				st3.execute( insertNewGrounding );
			}
			else
			{
				String createString = "CREATE TABLE `" +  functorId.replace( "`", "" ) +  "_parent_final_CT` SELECT distinct * FROM " +	"tempJoin;"; //target_parent_final_ct
				System.out.println( createString );
				st3.execute( createString );
			}
			
			functorId = functorIdhld;
			
			st3.execute( "DROP TABLE tempJoin" );
/*********	// processing target's parents******************END*************/	
			
/*********	// processing target's children*****************Begin**************/				
			// target_child_final_CT
			    //for each child of the target  // current
				//( --if target is rnode then have to rule out the associated 2node --) //April 25    
					// check the no. of its parents
						// if ==0, mult,functorId, current,
						// else, mult,functorId,current, current_parents
			
			//--if target is rnode then have to rule out the associated 2node --// April 25			
			String target_children ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId_orig+"' "
					+ " and (TargetChild) not in ( SELECT 2nid FROM  "+databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId_orig+"' );";  //functorId in unielwin_BN
			
			//String target_children ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId_orig+"' ;";  //functorId in unielwin_BN

			System.out.println( "zqian : "+ target_children );
			Statement s_temp = con0.createStatement();
			ResultSet rstarget_children = s_temp.executeQuery( target_children );
			// begin while 1
			while ( rstarget_children.next() )
			{ 
				String current= rstarget_children.getString(1);
				String current_orig=current;  // `b`, in unielwin_BN
				System.out.println("zqian: "+current+";");
				//
				//check if current is Rnode or not, do the mapping again
				ResultSet r_t = st3.executeQuery( "SELECT Type FROM " + databaseName +"_BN.FNodes WHERE Fid = '" +	current + "';" ); // `b`
				if ( r_t.absolute( 1 ) )
				{
					System.out.println("zqian: "+r_t.getString(1)+";");
					if ( r_t.getString(1).compareTo("Rnode")==0)
					{
						ResultSet rNodeName_t = st3.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	current + "';" ); // `b`
						if ( rNodeName_t.absolute( 1 ) )
						{
							current = rNodeName_t.getString(1); //`registration(course0,student0)`
						}
						rNodeName_t.close();
						ResultSet rNodeName_t1 = st3.executeQuery( "SELECT rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE orig_rnid = '" +	current + "';" );
						if ( rNodeName_t1.absolute( 1 ) )
						{
							current = rNodeName_t1.getString(1);  //`a`
						}
						rNodeName_t1.close();		
					}  
				}				
				r_t.close();	
				//
				
				String current_parents_select_string= "mult, "+ functorId +" ," + current +" ,";
				
				
				String current_parents_query ="SELECT TargetParent FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+current_orig+"' "
						+ " and ( TargetParent ) NOT IN (select '"+functorId_orig +"' );";  

				System.out.println( "zqian : "+ current_parents_query );
				Statement st_t = con0.createStatement();
				ResultSet rscurrent_parents = st_t.executeQuery( current_parents_query );	
				// begin while 2
				while (rscurrent_parents.next())
				{
					String sub_current= rscurrent_parents.getString(1);
					System.out.println("zqian: sub_current:  "+sub_current+";");
					//check if current is Rnode or not, do the mapping again
					ResultSet sub_r_t = st3.executeQuery( "SELECT Type FROM " + databaseName +"_BN.FNodes WHERE Fid = '" +	sub_current + "';" ); // `b`
					if ( sub_r_t.absolute( 1 ) )
					{
						System.out.println("zqian: "+sub_r_t.getString(1)+";");
						if ( sub_r_t.getString(1).compareTo("Rnode")==0)
						{
							ResultSet rNodeName_t = st3.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	sub_current + "';" ); // `b`
							if ( rNodeName_t.absolute( 1 ) )
							{
								sub_current = rNodeName_t.getString(1); //`registration(course0,student0)`
							}
							rNodeName_t.close();
							ResultSet rNodeName_t1 = st3.executeQuery( "SELECT rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE orig_rnid = '" +	sub_current + "';" );
							if ( rNodeName_t1.absolute( 1 ) )
							{
								sub_current = rNodeName_t1.getString(1);  //`a`
							}
							rNodeName_t1.close();		
						}  
					}				
					sub_r_t.close();	
					//
					current_parents_select_string += sub_current + " ,";
				}
				// end while 2
				String sub_tempTableString = "";
				if ( rchainExists )
				{
					sub_tempTableString = "CREATE TABLE tempJoin AS SELECT * FROM " +  "(SELECT * FROM GroundingsVertical) G " +
							  "JOIN (SELECT "+current_parents_select_string.substring(0, current_parents_select_string.lastIndexOf(",")-1) +" FROM " + databaseName4 +  ".`" + rchain.replace( "`","" ) +  "_CT`) A;";
				}
				else
				{  // need to update using the very similar query
					sub_tempTableString = "CREATE TABLE tempJoin AS SELECT * FROM " +  "(SELECT * FROM GroundingsVertical) G " +
									  "JOIN (SELECT * FROM " + databaseName4 +   ".`" + rchain.replace( "`","" ) +  "_counts`) A;";
				}
				System.out.println("zqian: "+ sub_tempTableString);
				st3.execute( sub_tempTableString );
				// functorId_current_final_CT
				//
				boolean sub_exists = false;
				String sub_functorIdhld = functorId;
				
				if ( processingRNode )
				{
					ResultSet rNodeName = st3.executeQuery( "SELECT orig_rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" );
					
					if ( rNodeName.absolute( 1 ) )
					{
						functorId = rNodeName.getString(1);
					}
					
					rNodeName.close();
				}
				
				String sub_checkCTExists = "SHOW TABLES LIKE '" + functorId.replace( "`", "" ) + "_"+current_orig.replace( "`", "" )+"_final_CT';";  //target_parent_final_ct
				System.out.println( sub_checkCTExists );
				
				ResultSet sub_rsExists = st3.executeQuery( sub_checkCTExists );				
				if ( sub_rsExists.absolute(1) )
				{
					sub_exists = true;
				}				
				sub_rsExists.close();
				
				if ( sub_exists )
				{
					String insertNewGrounding = "INSERT INTO `" + functorId.replace( "`", "" ) + "_"+current_orig.replace( "`", "" )+"_final_CT` SELECT distinct * FROM " +	"tempJoin;"; //target_current_final_ct
					System.out.println( insertNewGrounding );
					st3.execute( insertNewGrounding );
				}
				else
				{
					String createString = "CREATE TABLE `" +  functorId.replace( "`", "" ) + "_"+current_orig.replace( "`", "" )+"_final_CT` SELECT distinct * FROM " +	"tempJoin;"; //target_current_final_ct
					System.out.println( createString );
					st3.execute( createString );
				}
				
				functorId = sub_functorIdhld;
				
				st3.execute( "DROP TABLE tempJoin" );
				//
				st_t.close();
			}
			// end while 1			
/*********	// processing target's children**************END*****************/			
			st3.execute( "DROP TABLE GroundingsVertical" );
			st3.close();
			st2.execute( "DROP TABLE Groundings;" );
			st2.close();
			st_temp.close();
			st_temp1.close();
			s_temp.close();
			argValues.remove(index);
			
			groundingCount++;
		}
		
		rs.close();
		
		st1.close();
	}
	
	/*updated on Sep. 29th, 2014, same as TestWrapper.java*/
	/* check if there's `mult` column or not, if not, then return; if yes, do the processing. July 9th, 2014*/
	public static void pre_process( ) throws SQLException{ 
		/* pre processing, drop the `MULT` column for all CP tables, otherwise can NOT using natural join for _ct and _CP, April 3rd, zqian
		 * remapping the column headers for _cp tables, form rnid to orig_rnid April 15th, zqian
		 * (i.e. a --> RA(prof0,student0); b --> registration(course0,student0) )
		 * */
		System.out.println( "\npre processing, drop the `MULT` column for all CP tables in "+  databaseName + "_BN database;" );
		connectDB_preprocess();
		Statement st = con_preprocess.createStatement();
		Statement st1 = con_preprocess.createStatement();
		Statement st2 = con_preprocess.createStatement();

		ArrayList<String> cp_tables = new ArrayList<String>();
		/* check if there's `mult` column or not, if not, then return; if yes, do the processing. July 9th, 2014*/
		ResultSet temp_tables = st.executeQuery( "show tables from "+  databaseName + "_BN" + "  like '%_CP' ;");
		if ( temp_tables.next() ){  
			ResultSet rsColumn_t2  = st2.executeQuery ( "SHOW COLUMNS FROM `"+ temp_tables.getString(1)+ "` WHERE Field ='MULT';" );
			//System.out.println( "SHOW COLUMNS FROM `"+ temp_tables.getString(1)+ "` WHERE Field ='MULT';" );
			if (!rsColumn_t2.absolute(1)) {
				System.out.println("\n***Already Did the Pre_Process!***\n");
				
			}
			
			else {
				ResultSet temp_cp_tables = st.executeQuery( "show tables from "+  databaseName + "_BN" + "  like '%_CP' ;");
				while ( temp_cp_tables.next() )	{  
						System.out.println("ALTER TABLE `"+temp_cp_tables.getString(1)+"` DROP COLUMN `MULT`;");
						st1.execute("ALTER TABLE `"+temp_cp_tables.getString(1)+"` DROP COLUMN `MULT`;");
						cp_tables.add( temp_cp_tables.getString(1)); // get all the cp tables
				}
				
				
			}
		}
		
		
		
	
		// get the mapping of RNodes
		ResultSet rNodes = st.executeQuery( "SELECT orig_rnid, rnid FROM  " + databaseName + "_BN.RNodes;" );
		ArrayList<String> rnids = new ArrayList<String>(); // `a`
		ArrayList<String> origrnids = new ArrayList<String>();	//`RA(pfrof0,student0)`	
		while ( rNodes.next() )
		{
			origrnids.add( rNodes.getString(1));
			rnids.add( rNodes.getString(2));
		}		
		rNodes.close();
		//remapping the column headers for _cp tables
		for ( int j = 0; j < rnids.size(); j++ )
		{
			for ( int i = 0; i < cp_tables.size(); i++ )
			{
				System.out.println( "SHOW COLUMNS FROM " + databaseName + "_BN.`" + cp_tables.get(i) +"` WHERE Field = '" +rnids.get(j).replace("`", "") + "';" );
				ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " + databaseName + "_BN.`" + cp_tables.get(i) +"` WHERE Field = '" +rnids.get(j).replace("`", "") + "';" );
				
				if ( !rsTypes.absolute(1) ) //zqian, no rnode in the header, so do not need to replace
				{
					System.out.println( "NO Need to do the mapping " + cp_tables.get(i) );
					rsTypes.close();
					continue;
				}
				
				String type = rsTypes.getString(2);
				rsTypes.close();
				
				System.out.println( "ALTER TABLE " + databaseName + "_BN.`" + cp_tables.get(i)+ "` CHANGE COLUMN " +	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
				st.execute( "ALTER TABLE " + databaseName + "_BN.`" + cp_tables.get(i)+ "` CHANGE COLUMN " +	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
			}
		}
		
		// create FNodes_Mapping, May 2nd, zqian
		st1.execute("drop table if exists "+databaseName+"_BN.FNodes_Mapping; ");
		st1.execute("create table "+databaseName+"_BN.FNodes_Mapping as select * from "+databaseName+"_BN.FNodes; ");
		st1.execute("update "+databaseName+"_BN.FNodes_Mapping, "+databaseName+"_BN.RNodes set Fid = orig_rnid  where Fid=rnid; ");
		//keep the Attribute for Rnode be consistency, May 2nd
		st1.execute("update "+databaseName+"_BN.Attribute_Value set value = 'F' where value = 'False' ;");
		st1.execute("update "+databaseName+"_BN.Attribute_Value set value = 'T' where value = 'True' ;");
		
		
		st.close();
		st1.close();
		st2.close();

		disconnectDB_preprocess();		
		
	}
	
	
	/*public static void pre_process( ) throws SQLException
	{  pre processing, drop the `MULT` column for all CP tables, otherwise can NOT using natural join for _ct and _CP, April 3rd, zqian
		 * remapping the column headers for _cp tables, form rnid to orig_rnid April 15th, zqian
		 * (i.e. a --> RA(prof0,student0); b --> registration(course0,student0) )
		 * 
		System.out.println( "pre processing, drop the `MULT` column for all CP tables in "+  databaseName + "_BN database;" );
		connectDB_preprocess();
		Statement st = con_preprocess.createStatement();
		Statement st1 = con_preprocess.createStatement();
		ArrayList<String> cp_tables = new ArrayList<String>();
		ResultSet temp_cp_tables = st.executeQuery( "show tables from "+  databaseName + "_BN" + "  like '%_CP' ;");

		while ( temp_cp_tables.next() )
		{  
			System.out.println("ALTER TABLE `"+temp_cp_tables.getString(1)+"` DROP COLUMN `MULT`;");
			st1.execute("ALTER TABLE `"+temp_cp_tables.getString(1)+"` DROP COLUMN `MULT`;");
			cp_tables.add( temp_cp_tables.getString(1)); // get all the cp tables
			
		}
	
		// get the mapping of RNodes
		ResultSet rNodes = st.executeQuery( "SELECT orig_rnid, rnid FROM  " + databaseName + "_BN.RNodes;" );
		ArrayList<String> rnids = new ArrayList<String>(); // `a`
		ArrayList<String> origrnids = new ArrayList<String>();	//`RA(pfrof0,student0)`	
		while ( rNodes.next() )
		{
			origrnids.add( rNodes.getString(1));
			rnids.add( rNodes.getString(2));
		}		
		rNodes.close();
		//remapping the column headers for _cp tables
		for ( int j = 0; j < rnids.size(); j++ )
		{
			for ( int i = 0; i < cp_tables.size(); i++ )
			{
				System.out.println( "SHOW COLUMNS FROM " + databaseName + "_BN.`" + cp_tables.get(i) +"` WHERE Field = '" +rnids.get(j).replace("`", "") + "';" );
				ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " + databaseName + "_BN.`" + cp_tables.get(i) +"` WHERE Field = '" +rnids.get(j).replace("`", "") + "';" );
				
				if ( !rsTypes.absolute(1) ) //zqian, no rnode in the header, so do not need to replace
				{
					System.out.println( "NO Need to do the mapping " + cp_tables.get(i) );
					rsTypes.close();
					continue;
				}
				
				String type = rsTypes.getString(2);
				rsTypes.close();
				
				System.out.println( "ALTER TABLE " + databaseName + "_BN.`" + cp_tables.get(i)+ "` CHANGE COLUMN " +	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
				st.execute( "ALTER TABLE " + databaseName + "_BN.`" + cp_tables.get(i)+ "` CHANGE COLUMN " +	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
			}
		}
		
		// create FNodes_Mapping, May 2nd, zqian
		st1.execute("drop table if exists "+databaseName+"_BN.FNodes_Mapping; ");
		st1.execute("create table "+databaseName+"_BN.FNodes_Mapping as select * from "+databaseName+"_BN.FNodes; ");
		st1.execute("update "+databaseName+"_BN.FNodes_Mapping, "+databaseName+"_BN.RNodes set Fid = orig_rnid  where Fid=rnid; ");
		//keep the Attribute for Rnode be consistency, May 2nd
		st1.execute("update "+databaseName+"_BN.Attribute_Value set value = 'F' where value = 'False' ;");
		st1.execute("update "+databaseName+"_BN.Attribute_Value set value = 'T' where value = 'True' ;");
		st.close();
		st1.close();
		disconnectDB_preprocess();		
		
	}
	*/
	public static void postProcess( Statement st ) throws SQLException
	{	 //functorId = "`a`";
		System.out.println("zqian: comes to postProcess!");	

		System.out.println( "SELECT orig_rnid, rnid FROM " + databaseName1 + "_BN.RNodes;" );
		ResultSet rNodes = st.executeQuery( "SELECT orig_rnid, rnid FROM  " + databaseName1 + "_BN.RNodes;" );

		ArrayList<String> rnids = new ArrayList<String>();
		ArrayList<String> origrnids = new ArrayList<String>();
		
		while ( rNodes.next() )
		{
			origrnids.add( rNodes.getString(1));
			rnids.add( rNodes.getString(2));
		}		
		rNodes.close();
		
		String realFunctorId = functorId; //`a`
		String functorId_temp = functorId;
		System.out.println("zqian: processingRNode " + processingRNode);
		if ( processingRNode )
		{
			// functorId = "`a`";
			ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
			if ( rNodeName_t.absolute( 1 ) )
			{
				functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
				functorId_temp =functorId;
				System.out.println("zqian: functorId_temp " + functorId_temp);
			}				
			rNodeName_t.close();
			
			ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
			if ( rNodeName_t1.absolute( 1 ) )
			{
				functorId = rNodeName_t1.getString(1);  //`b`
				System.out.println("zqian: functorId_ " + functorId);

			}
			rNodeName_t1.close();		
			
		}

		Statement st_temp = con0.createStatement();
		Statement st_temp1 = con0.createStatement();

	
		for ( int i = 0; i < rnids.size(); i++ )
		{
			System.out.println( "SHOW COLUMNS FROM " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_parent_final_CT` " +
								"WHERE Field = '" +	rnids.get(i).replace("`", "") + "';" );
			ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " +	 databaseName3 + ".`" +	 functorId_temp.replace("`", "") +"_parent_final_CT` "
					+ "WHERE Field = '" +  rnids.get(i).replace("`", "") + "';");
			
			if ( !rsTypes.absolute(1) ) //zqian, no rnode in the header, so do not need to replace
			{
				System.out.println( "no rnode " + 	origrnids.get(i) + " in column header " );
				rsTypes.close();
				functorId = realFunctorId;  //`a`
				//return;
				continue;
			}
			
			String type = rsTypes.getString( 2 );
			rsTypes.close();
			
			System.out.println( "ALTER TABLE " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_parent_final_CT` CHANGE COLUMN " +	
								rnids.get(i) + " " + origrnids.get(i) + " " + type + ";" );
			st.execute( "ALTER TABLE " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_parent_final_CT` CHANGE COLUMN " +
								rnids.get(i) + " " + origrnids.get(i) + " " + type + ";" );
		}
		
		// processing  target's children 	
		//String target_children ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' ;";  // `b`
		//--if target is rnode then have to rule out the associated 2node --// April 25			
		String target_children ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' "
				+ " and (TargetChild) not in ( SELECT 2nid FROM  "+databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId+"' );"; 
		
		
		
		System.out.println( target_children );
		ResultSet rstarget_children = st_temp1.executeQuery( target_children );
	
		// zqian: for each child of the target 
		while ( rstarget_children.next() )
		{  
			String current= rstarget_children.getString(1);
			String current_orig=current;
			System.out.println("zqian: "+current+";");	
				//check if current is Rnode or not, do the mapping again
				ResultSet r_tt = st_temp.executeQuery( "SELECT Type FROM " + databaseName +"_BN.FNodes WHERE Fid = '" +	current + "';" ); // `b`
				if ( r_tt.absolute( 1 ) )
				{
					//System.out.println("zqian: "+r_t.getString(1)+";");
					if ( r_tt.getString(1).compareTo("Rnode")==0)
					{
						ResultSet rNodeName_t = st_temp.executeQuery( "SELECT orig_rnid " + "FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	current + "';" ); // `b`
						if ( rNodeName_t.absolute( 1 ) )
						{
							current = rNodeName_t.getString(1); //`registration(course0,student0)`
						}
						rNodeName_t.close();
						
						ResultSet rNodeName_t1 = st_temp.executeQuery( "SELECT rnid " + "FROM " + databaseName1 +"_BN.RNodes WHERE orig_rnid = '" +	current + "';" );
						
						if ( rNodeName_t1.absolute( 1 ) )
						{
							current = rNodeName_t1.getString(1);  //`a`
						}
						rNodeName_t1.close();		
	
					};  
				}				
				r_tt.close();					
					
		
			for ( int i = 0; i < rnids.size(); i++ )
			{
				System.out.println( "SHOW COLUMNS FROM " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_"+current_orig.replace( "`", "" )+ "_final_CT` " +
									"WHERE Field = '" +	rnids.get(i).replace("`", "") + "';" );
				ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " +	 databaseName3 + ".`" +	 functorId_temp.replace("`", "") +"_"+current_orig.replace( "`", "" )+ 
													 "_final_CT` WHERE Field = '" +  rnids.get(i).replace("`", "") + "';");
				if ( !rsTypes.absolute(1) ) //zqian, no rnode in the header, so do not need to replace
				{
					System.out.println( "Failed to get type for column " + origrnids.get(i) );
					rsTypes.close();
					functorId = realFunctorId;  //`a`
					//return;
					continue;
				}
				
				String type = rsTypes.getString( 2 );
				rsTypes.close();
				
				System.out.println( "ALTER TABLE " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_"+current_orig.replace( "`", "" )+ "_final_CT` CHANGE COLUMN " +	
									rnids.get(i) + " " + origrnids.get(i) + " " + type + ";" );
				st.execute( "ALTER TABLE " + databaseName3 + ".`" + functorId_temp.replace("`", "") +"_"+current_orig.replace( "`", "" )+ "_final_CT` CHANGE COLUMN " +
									rnids.get(i) + " " + origrnids.get(i) + " " + type + ";" );
			}
		}
		st_temp.close();
		st_temp1.close();
		functorId = realFunctorId;
		System.out.println("zqian: Leave postProcess  + functorId: "+functorId);	

	}
	
	public static void Extend_Final_CT(  ) throws SQLException
	{
		System.out.println( "Extend the final_CT tables for each functorId with all possible values: functorId "+functorId  );
		Statement st = conFinal.createStatement();
		Statement st1 = conFinal.createStatement();
		String functorId_t = functorId; // `a` 
		String functorId_temp = functorId; // `a` 
		
		ResultSet r_t = st.executeQuery( "SELECT Type FROM " + databaseName1 +"_BN.FNodes WHERE Fid = '" +	functorId_temp + "';" ); // `a`
		if ( r_t.absolute( 1 ) )
			{
				System.out.println("zqian, April 28th, functorId Type is : "+r_t.getString(1)+";");
				if ( r_t.getString(1).compareTo("Rnode")==0)
				{
					ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" + functorId_temp + "';" ); // `a`
					if ( rNodeName_t.absolute( 1 ) )
					{
						functorId_temp = rNodeName_t.getString(1); //`registration(course0,student0)`
						System.out.println("zqian: functorId_temp" + functorId_temp);
					}
					rNodeName_t.close();	
					ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId_temp + "';" );
					if ( rNodeName_t1.absolute( 1 ) )
					{
						functorId = rNodeName_t1.getString(1);  //`b`
						System.out.println("zqian: functorId" + functorId);
					}
					rNodeName_t1.close();	
					
				}
			}				
		r_t.close();	
		
		ResultSet temp_ct_tables = st1.executeQuery( "show tables from "+  databaseName3 + "  like '"+functorId_temp.replace("`","")+"%' ;");
		while ( temp_ct_tables.next() )
		{   String current_ct = temp_ct_tables.getString(1);
			System.out.println("ALTER TABLE "+databaseName3+ ".`"+current_ct+"` DROP COLUMN `"+functorId_temp.replace("`","")+"`;");
			st.execute("ALTER TABLE "+databaseName3+ ".`"+current_ct+"` DROP COLUMN `"+functorId_temp.replace("`","")+"` ;");
			String create_string = " CREATE TABLE "+databaseName3+ ".`"+current_ct+"_e` as select " ; // table limits to 64 char,  ??? need to fix this
			System.out.println("show columns from  "+  databaseName3 + ".`"+current_ct+"` ;");
			ResultSet temp_ct_columns = st.executeQuery( "show columns from  "+  databaseName3 + ".`"+current_ct+"` ;");
			while (temp_ct_columns.next())
			{
				create_string += "`"+temp_ct_columns.getString(1)+"`" + " ,";
			}
			create_string += "value as `"+functorId_temp.replace("`","")+"` From " +databaseName3+ ".`"+current_ct+"` , " +databaseName+"_BN.Attribute_Value "
					+ "where column_name = (SELECT FunctorName FROM " +databaseName+"_BN.FNodes where Fid = '`"+functorId.replace("`","")+"`' ) ;";
			
			System.out.println(create_string);
			st.execute(create_string);
			//temp_ct_columns.close();
		}
		//temp_ct_tables.close();
		
		functorId =functorId_t;  //`a`
		
		System.out.println("zqian: Left Extend_Final_CT + functorId: "+functorId);	
		
		st.close();
		st1.close();
	}
	
	//for 1node and 2node, set the mult to 0 given false relationships, compute frequencies, May 2nd
	public static void Update_CT(  ) throws SQLException
	{
		
		Statement st = con1.createStatement();
		Statement st1 = con1.createStatement();
		Statement st2 = con1.createStatement();
		Statement st3 = con1.createStatement();
		String functorId_t = functorId; // `a` 
		String functorId_temp = functorId; // `a`
		if ( processingRNode )
		{
			// functorId = "`a`";
			ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
			if ( rNodeName_t.absolute( 1 ) )
			{
				functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
				functorId_temp =functorId;
				System.out.println("zqian: functorId_temp " + functorId_temp);
			}				
			rNodeName_t.close();
			
			ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
			if ( rNodeName_t1.absolute( 1 ) )
			{
				functorId = rNodeName_t1.getString(1);  //`b`
				System.out.println("zqian: functorId_ " + functorId);

			}
			rNodeName_t1.close();		
			
		}
		//
		{
			System.out.println( "show tables like '" +	functorId_temp.replace("`","") + "_%_e' ;" );
			ResultSet rsTable_t1 = st.executeQuery( "show tables like '" +	functorId_temp.replace("`","") + "_%_e' ;" );
		
			System.out.println( "Update_CT for "+functorId  );
			while ( rsTable_t1.next()  )
			{   
				String current_ct = rsTable_t1.getString(1);
				ResultSet rsColumn_t2 = st1.executeQuery( "show columns from  `" +	current_ct.replace("'","") + "`  where Field  like '%,%'; " );
				System.out.println( "show columns from  `" +	current_ct.replace("'","") + "`  where Field  like '%,%'; " );
				if ( !processingRNode )	
				{
					while (  rsColumn_t2.next() )
					{   //
						String current_ct_column = rsColumn_t2.getString(1); 
						ResultSet rs_temp = st2.executeQuery( "select   Type, Fid from "+databaseName+"_BN.FNodes_Mapping where  Fid =  '`" +	current_ct_column.replace("`","") + "`' ; " );
						System.out.println( "select  Type, Fid from "+databaseName+"_BN.FNodes_Mapping where  Fid =  '`" +	current_ct_column.replace("`","") + "`' ; " );
						if ( rs_temp.absolute( 1 ) )
						{
							if ( rs_temp.getString(1).compareTo("2Node")==0 )
							{
								System.out.println(" update `"+current_ct.replace("`","")+"` set mult =0 where `"+current_ct_column.replace("`","")+"` = 'N/A' ; ");
								st3.execute(" update `"+current_ct.replace("`","")+"` set mult =0 where `"+current_ct_column.replace("`","")+"` = 'N/A' ; ");
							}
							
							if ( rs_temp.getString(1).compareTo("Rnode")==0 )
							{
								System.out.println(" update `"+current_ct.replace("`","")+"` set mult =0 where `"+current_ct_column.replace("`","")+"` = 'F' ; ");
								st3.execute(" update `"+current_ct.replace("`","")+"` set mult =0 where `"+current_ct_column.replace("`","")+"` = 'F' ; ");
							}				
						}
						rs_temp.close();			
					}
				}
	
				st3.execute("ALTER TABLE `" +current_ct.replace("`","")+"` ADD COLUMN `sum_mult` BIGINT(21) NULL DEFAULT 0 AFTER `"+functorId_temp.replace("`","") +"` ;");
				st3.execute("drop table if exists temp; ");
				// May 5th, the sum_mult should equal to sum(mult), since we did the natural join for the binary nodes
				st3.execute("create table temp as SELECT `GroundingsNumber`,  sum(`mult`)/2 as sum_mult  FROM `"+current_ct.replace("`","")+"`  group by  `GroundingsNumber` ;");
				st3.execute("update  `"+current_ct.replace("`","")+"`, temp set `"+current_ct.replace("`","")+"`.sum_mult = temp.sum_mult "
						+ "where `"+current_ct.replace("`","")+"`.`GroundingsNumber` = temp.`GroundingsNumber` ;");
				st3.execute("ALTER TABLE `" +current_ct.replace("`","")+"` ADD COLUMN `freq` DOUBLE NULL DEFAULT '0'  AFTER `sum_mult` ;");
				st3.execute("update `" +current_ct.replace("`","")+"` set freq = mult/sum_mult ;");
				st3.execute("update `" +current_ct.replace("`","")+"` set freq = '0' where freq is NULL ;"); // May 6th, reset the freq to 0
	
				System.out.println("DONE for table `"+functorId_temp.replace("`","") +"`");
			}
			
			functorId = functorId_t;
			System.out.println( "leaving Update_CT  "+functorId  +"\n");
		}
		
	}
	//lose some combinations occur in testing data, but NOT in training data
	//option 1
	public static void createSubFinal_zqian( ArrayList<String> args  )
	{	//functorId = "`a`";
		System.out.println("zqian: come to createSubFinal_zqian + functorId: "+functorId);	
		String functorId_t=functorId; //store the funcorId in unielwin_target_BN
	 	String functorId_orig="";
		try
		{
			String actualNodeName = "";
			Statement st = null;
			
			st = conFinal.createStatement();
			
			if ( processingRNode )
			{				
				ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					functorId_orig =functorId;
					actualNodeName =functorId;
					System.out.println("zqian: functorId_orig " + functorId_orig);
				}				
				rNodeName_t.close();
				
				ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`b`
				}
				rNodeName_t1.close();					
			}
			else
			{
				actualNodeName = functorId;
			}
			
		// for target_parent_final
			// target_cp, target_parent_final_CT	
			String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_parent_final` AS SELECT `" +actualNodeName.replace( "`" , "" )+"`, ";				
			int numArgs = args.size();				
			for ( int i = 0; i < numArgs; i++ )
			{
				createTable += args.get( i ) + ", ";
			}				
//			createTable += "SUM(MULT * log(" + databaseName + "_BN.`" +  //zqian // counts
//					   functorId.replace( "`" , "" ) + "_CP`.CP)) " + 
//					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
//					   functorId.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
//						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";			
			createTable += "SUM(freq * log(" + databaseName + "_BN.`" +  //zqian // frequencies, May 2nd
					   functorId.replace( "`" , "" ) + "_CP`.CP)) " + 
					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
					   functorId.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";	
			
			createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, ";
			
			for ( int i = 0; i < numArgs; i++ )
			{
				createTable += args.get( i ) + " ,";
			}
			
			System.out.println("zqian : "+ createTable.substring(0, createTable.lastIndexOf(",")-1)+" ;" );
			st.execute( createTable.substring(0, createTable.lastIndexOf(",")-1) + " ;" );
			
			
		// for target_child_final
			// child_cp, target_child_final_CT
			// String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' ;";  //functorId in unielwin_BN
			//--if target is rnode then have to rule out the associated 2node --// April 25			
			String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' "
					+ " and (TargetChild) not in ( SELECT 2nid FROM  "+databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId+"' );"; 
			
			System.out.println( "zqian : "+ target_child );
			Statement st_temp1 = con0.createStatement();
			ResultSet rstarget_child = st_temp1.executeQuery( target_child );
			while ( rstarget_child.next() )
			{  
				String current= rstarget_child.getString(1);
				System.out.println("zqian: "+current+";");
				String sub_createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_final` AS SELECT `" +actualNodeName.replace( "`" , "" )+"`, ";
				
				int sub_numArgs = args.size();
				
				for ( int i = 0; i < sub_numArgs; i++ )
				{
					sub_createTable += args.get( i ) + ", ";
				}			
//
//				sub_createTable += "SUM(MULT * log(" + databaseName + "_BN.`" +  //zqian //counts
//						current.replace( "`" , "" ) + "_CP`.CP)) " + 
//						   "AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
//						   current.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
//							  actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
//				
				sub_createTable += "SUM(freq * log(" + databaseName + "_BN.`" +  //zqian //freqencies, May 2nd
						current.replace( "`" , "" ) + "_CP`.CP)) " + 
						   "AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
						   current.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
							  actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
			
				sub_createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, ";
				
				for ( int i = 0; i < sub_numArgs; i++ )
				{
					sub_createTable += args.get( i ) + " ,";
				}
				
				System.out.println("zqian : "+ sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1)+" ;" );
				st.execute( sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1) + " ;" );				
				
			}
			st.close();	
		}		
		
		catch (SQLException e)
		{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		functorId = functorId_t; //store the funcorId in unielwin_target_BN
		System.out.println("zqian: Leave createSubFinal_zqian + functorId: "+functorId);	
	}
	
	public static void createFinal_zqian( ArrayList<String> args  )
	{	System.out.println("zqian: come to createFinal_zqian!");	
	 	String functorId_orig="";
		try
		{
			String actualNodeName = "";
			Statement st = null;			
			st = conFinal.createStatement();
			System.out.println("zqian: processingRNode " + processingRNode);

//			if ( processingRNode & functorId.compareTo("`a`")==0 )//just for testing, need to remove during the running April 17
			if ( processingRNode  )//just for testing, need to remove during the running April 17
			{
				ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					functorId_orig =functorId;
					actualNodeName =functorId;
					System.out.println("zqian: functorId_orig " + functorId_orig);
				}				
				rNodeName_t.close();
				
				ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`b`
				}
				rNodeName_t1.close();					
			}
			else
			{
				actualNodeName = functorId;
			}

			String target_size ="select count(*) FROM INFORMATION_SCHEMA.tables where table_schema =  '"+databaseName5+"'and table_name like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
			System.out.println( "zqian : "+ target_size );
			Statement st_temp1 = con0.createStatement();
			int size=0;
			ResultSet rstarget_size = st_temp1.executeQuery( target_size );
			if (  rstarget_size.absolute( 1 ) )
			{
				//size = rstarget_size.getString(1);
				size =rstarget_size.getInt(1);
				System.out.println( "zqian : size  "+ size );
			}
			 rstarget_size.close();					
			 if (size >1) 
			 {
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					///**
					//-- show tables like 'actualNodeName.replace("`","")';
					//-- if size of the result size >1: 
					//-- create the query string for summation
					//--    sum(table1.weight_product + table2.weight_product + ... )
					//-- create the join string
					//--    table1, table2, ...
					//-- create the where string 
					//--    table
					//-- else : rename the table as 'actualNodeName.replace("`","")' 
					//--   by   RENAME TABLE `group` TO `member`
					//*/
//								// do the sum
//								//prepare the create string for target_final table
//								//String create_final = "create table if not exists  `"+functorId.replace( "`" , "" )+"_final` AS SELECT " ;
					//	
					
					String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_sum` AS SELECT `" + actualNodeName.replace( "`" , "" )+"`, ";		
					String current="";
					String select_string=" ( ";
					String from_string="";
									
					while ( rstarget_showcolumns.next() )
					{ 
						current= rstarget_showcolumns.getString(1);
						select_string += " `"+current.replace( "`" , "")+"`." +"`weight_product_" +current.substring(current.indexOf("_")+1, current.lastIndexOf("_"))+"` +";
						from_string += databaseName5+".`"+ current + "` natural join ";

					}
					select_string =select_string.substring(0, select_string.lastIndexOf("+")-1)+ " ) as Score , "; // change to score
					from_string = from_string.substring(0, from_string.lastIndexOf("natural join")-1); //remove the last "natural join"
					
					String createTable_temp= "CREATE TABLE temp AS SELECT ";
					String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
					String groupby_string=" group by ";
					String SubQueryWhereString= "";
					int numArgs = args.size();				
					for ( int i = 0; i < numArgs; i++ )   
					{
						select_string += " `"+current.replace("`", "")+"`."+args.get( i ) + ", ";
						createTable_temp += args.get( i ) + ", ";
						createTable_temp_t +=  args.get( i ) + ", ";
						groupby_string += args.get( i ) + ", ";
						SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
					}	
					
					select_string = select_string.substring(0, select_string.lastIndexOf(","));  // remove the last ","
					createTable += select_string + " from  " + from_string +" ;";  // remove the last ","
					//create the target_sum table
					System.out.println("zqian createTable : "+ createTable );
					st.execute( createTable );	
					
					
					groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
					
					createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
							+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
							+ groupby_string +" ;"; 
					
					SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
					
					createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
							+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
							 + groupby_string  +" ;"; 
					
					//April 30, zqian
					//create the target_Score table					
					st.execute( " DROP TABLE IF EXISTS temp; ");
					st.execute( " DROP TABLE IF EXISTS temp_t; ");
					System.out.println("zqian createTable_temp  : "+ createTable_temp  );
					st.execute( createTable_temp );	
					System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
					st.execute( createTable_temp_t );	
					String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
					System.out.println("zqian createTable_score  : "+ createTable_score  );
					st.execute( createTable_score );	
					//st.execute( " DROP TABLE IF EXISTS temp; ");
					//st.execute( " DROP TABLE IF EXISTS temp_t; ");
					
					
			 }
			 else 
			 {
				//rename the table 
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					//String name="";
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					if (  rstarget_showcolumns.absolute( 1 ) )
					{
						//size = rstarget_size.getString(1);
						//name =rstarget_showcolumns.getString(1);
						System.out.println( "zqian :   "+ rstarget_showcolumns.getString(1) );
						Statement st_t = con0.createStatement();
						System.out.println( "rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;" );
						st_t.execute("rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;");
						// rename the weight_product_parent column to Score, April 27 
						System.out.println( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;" );
						st_t.execute( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;") ;
						//create the target_Score table					
						//April 30, zqian
						String createTable_temp= "CREATE TABLE temp AS SELECT ";
						String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
						String groupby_string=" group by ";
						String SubQueryWhereString= "";
						int numArgs = args.size();				
						for ( int i = 0; i < numArgs; i++ )   
						{
							createTable_temp += args.get( i ) + ", ";
							createTable_temp_t +=  args.get( i ) + ", ";
							groupby_string += args.get( i ) + ", ";
							SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
						}	

						groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
						
						createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
								+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
								+ groupby_string +" ;"; 
						
						SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
						
						createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
								+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
								 + groupby_string  +" ;"; 
						
						//create the target_Score table					
						st.execute( " DROP TABLE IF EXISTS temp; ");
						st.execute( " DROP TABLE IF EXISTS temp_t; ");
						System.out.println("zqian createTable_temp  : "+ createTable_temp  );
						st.execute( createTable_temp );	
						System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
						st.execute( createTable_temp_t );	
						String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
						System.out.println("zqian createTable_score  : "+ createTable_score  );
						st.execute( createTable_score );	
						
						
						
						
						st_t.close();

					}
					rstarget_showcolumns.close();					
			 }	
		
			st.close();	
		}
		
		catch (SQLException e)
		{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		System.out.println("zqian: Leave createFinal_zqian!");	
	}
	
	//lose some combinations occur in testing data, but NOT in training data
	//option 2
	public static void createSubFinal2_zqian( ArrayList<String> args  )
	{	//functorId = "`a`";
		System.out.println("zqian: come to createSubFinal2_zqian + functorId: "+functorId);	
		String functorId_t=functorId; //store the funcorId in unielwin_target_BN
	 	String functorId_orig="";
		try
		{
			String actualNodeName = "";
			Statement st = null;
			
			st = conFinal.createStatement();
			
			if ( processingRNode )
			{				
				ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					functorId_orig =functorId;
					actualNodeName =functorId;
					System.out.println("zqian: functorId_orig " + functorId_orig);
				}				
				rNodeName_t.close();
				
				ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`b`
				}
				rNodeName_t1.close();					
			}
			else
			{
				actualNodeName = functorId;
			}
			
		// for target_parent_final
			// target_cp, target_parent_final_CT	
			String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_parent_final` AS SELECT `" +actualNodeName.replace( "`" , "" )+"`, ";				
			int numArgs = args.size();				
			for ( int i = 0; i < numArgs; i++ )
			{
				createTable += args.get( i ) + ", ";
			}				
//			createTable += "SUM(MULT * log(" + databaseName + "_BN.`" +  //zqian // counts
//					   functorId.replace( "`" , "" ) + "_CP`.CP)) " + 
//					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
//					   functorId.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
//						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";			
			createTable += "SUM(freq * log(" + databaseName + "_BN.`" +  //zqian // frequencies, May 2nd
					   functorId.replace( "`" , "" ) + "_CP`.CP)) " + 
					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
					   functorId.replace( "`" , "" ) + "_CP` natural right join  " + databaseName3 + ".`" + 
						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";	
			
			createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, ";
			
			for ( int i = 0; i < numArgs; i++ )
			{
				createTable += args.get( i ) + " ,";
			}
			
			System.out.println("zqian : "+ createTable.substring(0, createTable.lastIndexOf(",")-1)+" ;" );
			st.execute( createTable.substring(0, createTable.lastIndexOf(",")-1) + " ;" );
			
			
		// for target_child_final
			// child_cp, target_child_final_CT
			// String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' ;";  //functorId in unielwin_BN
			//--if target is rnode then have to rule out the associated 2node --// April 25			
			String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' "
					+ " and (TargetChild) not in ( SELECT 2nid FROM  "+databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId+"' );"; 
			
			System.out.println( "zqian : "+ target_child );
			Statement st_temp1 = con0.createStatement();
			ResultSet rstarget_child = st_temp1.executeQuery( target_child );
			while ( rstarget_child.next() )
			{  
				String current= rstarget_child.getString(1);
				System.out.println("zqian: "+current+";");
				String sub_createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_final` AS SELECT `" +actualNodeName.replace( "`" , "" )+"`, ";
				
				int sub_numArgs = args.size();
				
				for ( int i = 0; i < sub_numArgs; i++ )
				{
					sub_createTable += args.get( i ) + ", ";
				}			
//
//				sub_createTable += "SUM(MULT * log(" + databaseName + "_BN.`" +  //zqian //counts
//						current.replace( "`" , "" ) + "_CP`.CP)) " + 
//						   "AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
//						   current.replace( "`" , "" ) + "_CP` natural join  " + databaseName3 + ".`" + 
//							  actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
//				
				sub_createTable += "SUM(freq * log(" + databaseName + "_BN.`" +  //zqian //freqencies, May 2nd
						current.replace( "`" , "" ) + "_CP`.CP)) " + 
						   "AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
						   current.replace( "`" , "" ) + "_CP` natural right join   " + databaseName3 + ".`" + 
							  actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
			
				sub_createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, ";
				
				for ( int i = 0; i < sub_numArgs; i++ )
				{
					sub_createTable += args.get( i ) + " ,";
				}
				
				System.out.println("zqian : "+ sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1)+" ;" );
				st.execute( sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1) + " ;" );				
				
			}
			st.close();	
		}		
		
		catch (SQLException e)
		{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		functorId = functorId_t; //store the funcorId in unielwin_target_BN
		System.out.println("zqian: Leave createSubFinal2_zqian + functorId: "+functorId);	
	}
	
	public static void createFinal2_zqian( ArrayList<String> args  )
	{	System.out.println("zqian: come to createFinal2_zqian!");	
	 	String functorId_orig="";
		try
		{
			String actualNodeName = "";
			Statement st = null;			
			st = conFinal.createStatement();
			System.out.println("zqian: processingRNode " + processingRNode);

//			if ( processingRNode & functorId.compareTo("`a`")==0 )//just for testing, need to remove during the running April 17
			if ( processingRNode  )//just for testing, need to remove during the running April 17
			{
				ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					functorId_orig =functorId;
					actualNodeName =functorId;
					System.out.println("zqian: functorId_orig " + functorId_orig);
				}				
				rNodeName_t.close();
				
				ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`b`
				}
				rNodeName_t1.close();					
			}
			else
			{
				actualNodeName = functorId;
			}

			String target_size ="select count(*) FROM INFORMATION_SCHEMA.tables where table_schema =  '"+databaseName5+"'and table_name like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
			System.out.println( "zqian : "+ target_size );
			Statement st_temp1 = con0.createStatement();
			int size=0;
			ResultSet rstarget_size = st_temp1.executeQuery( target_size );
			if (  rstarget_size.absolute( 1 ) )
			{
				//size = rstarget_size.getString(1);
				size =rstarget_size.getInt(1);
				System.out.println( "zqian : size  "+ size );
			}
			 rstarget_size.close();					
			 if (size >1) 
			 {
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					///**
					//-- show tables like 'actualNodeName.replace("`","")';
					//-- if size of the result size >1: 
					//-- create the query string for summation
					//--    sum(table1.weight_product + table2.weight_product + ... )
					//-- create the join string
					//--    table1, table2, ...
					//-- create the where string 
					//--    table
					//-- else : rename the table as 'actualNodeName.replace("`","")' 
					//--   by   RENAME TABLE `group` TO `member`
					//*/
//								// do the sum
//								//prepare the create string for target_final table
//								//String create_final = "create table if not exists  `"+functorId.replace( "`" , "" )+"_final` AS SELECT " ;
					//	
					
					String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_sum` AS SELECT `" + actualNodeName.replace( "`" , "" )+"`, ";		
					String current="";
					String select_string=" ( ";
					String from_string="";
									
					while ( rstarget_showcolumns.next() )
					{ 
						current= rstarget_showcolumns.getString(1);	// update the null value to 0, May 7th, zqian
						select_string += " COALESCE( `"+current.replace( "`" , "")+"`." +"`weight_product_" +current.substring(current.indexOf("_")+1, current.lastIndexOf("_"))+"`, 0) +";
						from_string += databaseName5+".`"+ current + "` natural join ";

					}
					select_string =select_string.substring(0, select_string.lastIndexOf("+")-1)+ " ) as Score , "; // change to score
					from_string = from_string.substring(0, from_string.lastIndexOf("natural join")-1); //remove the last "natural join"
					
					String createTable_temp= "CREATE TABLE temp AS SELECT ";
					String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
					String groupby_string=" group by ";
					String SubQueryWhereString= "";
					int numArgs = args.size();				
					for ( int i = 0; i < numArgs; i++ )   
					{
						select_string += " `"+current.replace("`", "")+"`."+args.get( i ) + ", ";
						createTable_temp += args.get( i ) + ", ";
						createTable_temp_t +=  args.get( i ) + ", ";
						groupby_string += args.get( i ) + ", ";
						SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
					}	
					
					select_string = select_string.substring(0, select_string.lastIndexOf(","));  // remove the last ","
					createTable += select_string + " from  " + from_string +" ;";  // remove the last ","
					//create the target_sum table
					System.out.println("zqian createTable : "+ createTable );
					st.execute( createTable );	
					
					
					groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
					
					createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
							+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
							+ groupby_string +" ;"; 
					
					SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
					
					createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
							+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
							 + groupby_string  +" ;"; 
					
					//April 30, zqian
					//create the target_Score table					
					st.execute( " DROP TABLE IF EXISTS temp; ");
					st.execute( " DROP TABLE IF EXISTS temp_t; ");
					System.out.println("zqian createTable_temp  : "+ createTable_temp  );
					st.execute( createTable_temp );	
					System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
					st.execute( createTable_temp_t );	
					String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
					System.out.println("zqian createTable_score  : "+ createTable_score  );
					st.execute( createTable_score );	
					//st.execute( " DROP TABLE IF EXISTS temp; ");
					//st.execute( " DROP TABLE IF EXISTS temp_t; ");
					
					
			 }
			 else 
			 {
				//rename the table 
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					//String name="";
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					if (  rstarget_showcolumns.absolute( 1 ) )
					{
						//size = rstarget_size.getString(1);
						//name =rstarget_showcolumns.getString(1);
						System.out.println( "zqian :   "+ rstarget_showcolumns.getString(1) );
						Statement st_t = con0.createStatement();
						System.out.println( "rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;" );
						st_t.execute("rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;");
						// rename the weight_product_parent column to Score, April 27 
						System.out.println( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;" );
						st_t.execute( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;") ;
						// update the null value to 0, May 7th, zqian
						System.out.println( "update "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  set `weight_product_parent` = COALESCE(weight_product_parent,0);" );
						st_t.execute( "update "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  set `weight_product_parent` = COALESCE(weight_product_parent,0);" ) ;
								
						//create the target_Score table					
						//April 30, zqian
						String createTable_temp= "CREATE TABLE temp AS SELECT ";
						String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
						String groupby_string=" group by ";
						String SubQueryWhereString= "";
						int numArgs = args.size();				
						for ( int i = 0; i < numArgs; i++ )   
						{
							createTable_temp += args.get( i ) + ", ";
							createTable_temp_t +=  args.get( i ) + ", ";
							groupby_string += args.get( i ) + ", ";
							SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
						}	

						groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
						
						createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
								+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
								+ groupby_string +" ;"; 
						
						SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
						
						createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
								+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
								 + groupby_string  +" ;"; 
						
						//create the target_Score table					
						st.execute( " DROP TABLE IF EXISTS temp; ");
						st.execute( " DROP TABLE IF EXISTS temp_t; ");
						System.out.println("zqian createTable_temp  : "+ createTable_temp  );
						st.execute( createTable_temp );	
						System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
						st.execute( createTable_temp_t );	
						String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
						System.out.println("zqian createTable_score  : "+ createTable_score  );
						st.execute( createTable_score );	
						
						
						
						
						st_t.close();

					}
					rstarget_showcolumns.close();					
			 }	
		
			st.close();	
		}
		
		catch (SQLException e)
		{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		System.out.println("zqian: Leave createFinal2_zqian!");	
	}
	
	public static void createFinal3_zqian( ArrayList<String> args  )
	{	System.out.println("zqian: come to createFinal3_zqian!");	
	 	String functorId_orig="";
		try
		{
			String actualNodeName = "";
			Statement st = null;			
			st = conFinal.createStatement();
			System.out.println("zqian: processingRNode " + processingRNode);

//			if ( processingRNode & functorId.compareTo("`a`")==0 )//just for testing, need to remove during the running April 17
			if ( processingRNode  )//just for testing, need to remove during the running April 17
			{
				ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName1 +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `a`				
				if ( rNodeName_t.absolute( 1 ) )
				{
					functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					functorId_orig =functorId;
					actualNodeName =functorId;
					System.out.println("zqian: functorId_orig " + functorId_orig);
				}				
				rNodeName_t.close();
				
				ResultSet rNodeName_t1 = st.executeQuery( "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" +	functorId + "';" );
				if ( rNodeName_t1.absolute( 1 ) )
				{
					functorId = rNodeName_t1.getString(1);  //`b`
				}
				rNodeName_t1.close();					
			}
			else
			{
				actualNodeName = functorId;
			}

			String target_size ="select count(*) FROM INFORMATION_SCHEMA.tables where table_schema =  '"+databaseName5+"'and table_name like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
			System.out.println( "zqian : "+ target_size );
			Statement st_temp1 = con0.createStatement();
			int size=0;
			ResultSet rstarget_size = st_temp1.executeQuery( target_size );
			if (  rstarget_size.absolute( 1 ) )
			{
				//size = rstarget_size.getString(1);
				size =rstarget_size.getInt(1);
				System.out.println( "zqian : size  "+ size );
			}
			 rstarget_size.close();					
			 if (size >1) 
			 {
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					///**
					//-- show tables like 'actualNodeName.replace("`","")';
					//-- if size of the result size >1: 
					//-- create the query string for summation
					//--    sum(table1.weight_product + table2.weight_product + ... )
					//-- create the join string
					//--    table1, table2, ...
					//-- create the where string 
					//--    table
					//-- else : rename the table as 'actualNodeName.replace("`","")' 
					//--   by   RENAME TABLE `group` TO `member`
					//*/
//								// do the sum
//								//prepare the create string for target_final table
//								//String create_final = "create table if not exists  `"+functorId.replace( "`" , "" )+"_final` AS SELECT " ;
					//	
					
					String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_sum` AS SELECT `" + actualNodeName.replace( "`" , "" )+"`, ";		
					String current="";
					String select_string=" ( ";
					String from_string="";
									
					while ( rstarget_showcolumns.next() )
					{ 
						current= rstarget_showcolumns.getString(1);	// update the null value to 0, May 7th, zqian
						select_string += " COALESCE( `"+current.replace( "`" , "")+"`." +"`weight_product_" +current.substring(current.indexOf("_")+1, current.lastIndexOf("_"))+"`, -50) +";
						from_string += databaseName5+".`"+ current + "` natural join ";

					}
					select_string =select_string.substring(0, select_string.lastIndexOf("+")-1)+ " ) as Score , "; // change to score
					from_string = from_string.substring(0, from_string.lastIndexOf("natural join")-1); //remove the last "natural join"
					
					String createTable_temp= "CREATE TABLE temp AS SELECT ";
					String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
					String groupby_string=" group by ";
					String SubQueryWhereString= "";
					int numArgs = args.size();				
					for ( int i = 0; i < numArgs; i++ )   
					{
						select_string += " `"+current.replace("`", "")+"`."+args.get( i ) + ", ";
						createTable_temp += args.get( i ) + ", ";
						createTable_temp_t +=  args.get( i ) + ", ";
						groupby_string += args.get( i ) + ", ";
						SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
					}	
					
					select_string = select_string.substring(0, select_string.lastIndexOf(","));  // remove the last ","
					createTable += select_string + " from  " + from_string +" ;";  // remove the last ","
					//create the target_sum table
					System.out.println("zqian createTable : "+ createTable );
					st.execute( createTable );	
					
					
					groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
					
					createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
							+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
							+ groupby_string +" ;"; 
					
					SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
					
					createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
							+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
							 + groupby_string  +" ;"; 
					
					//April 30, zqian
					//create the target_Score table					
					st.execute( " DROP TABLE IF EXISTS temp; ");
					st.execute( " DROP TABLE IF EXISTS temp_t; ");
					System.out.println("zqian createTable_temp  : "+ createTable_temp  );
					st.execute( createTable_temp );	
					System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
					st.execute( createTable_temp_t );	
					String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
					System.out.println("zqian createTable_score  : "+ createTable_score  );
					st.execute( createTable_score );	
					//st.execute( " DROP TABLE IF EXISTS temp; ");
					//st.execute( " DROP TABLE IF EXISTS temp_t; ");
					
					
			 }
			 else 
			 {
				//rename the table 
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					//String name="";
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					if (  rstarget_showcolumns.absolute( 1 ) )
					{
						//size = rstarget_size.getString(1);
						//name =rstarget_showcolumns.getString(1);
						System.out.println( "zqian :   "+ rstarget_showcolumns.getString(1) );
						Statement st_t = con0.createStatement();
						System.out.println( "rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;" );
						st_t.execute("rename table "+databaseName5+".`"+rstarget_showcolumns.getString(1) + "` to "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  ;");
						// rename the weight_product_parent column to Score, April 27 
						System.out.println( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;" );
						st_t.execute( "alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  CHANGE COLUMN `weight_product_parent` `Score` DOUBLE NULL DEFAULT NULL;") ;
						// update the null value to 0, May 7th, zqian
						System.out.println( "update "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  set `Score` = COALESCE(`Score`,-50);" );
						st_t.execute( "update "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum`  set `Score` = COALESCE(`Score`,-50);" ) ;
								
						//create the target_Score table					
						//April 30, zqian
						String createTable_temp= "CREATE TABLE temp AS SELECT ";
						String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
						String groupby_string=" group by ";
						String SubQueryWhereString= "";
						int numArgs = args.size();				
						for ( int i = 0; i < numArgs; i++ )   
						{
							createTable_temp += args.get( i ) + ", ";
							createTable_temp_t +=  args.get( i ) + ", ";
							groupby_string += args.get( i ) + ", ";
							SubQueryWhereString += "A."+args.get( i ) +" = B."+ args.get( i ) +" and ";
						}	

						groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
						
						createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
								+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
								+ groupby_string +" ;"; 
						
						SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
						
						createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
								+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
								 + groupby_string  +" ;"; 
						
						//create the target_Score table					
						st.execute( " DROP TABLE IF EXISTS temp; ");
						st.execute( " DROP TABLE IF EXISTS temp_t; ");
						System.out.println("zqian createTable_temp  : "+ createTable_temp  );
						st.execute( createTable_temp );	
						System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
						st.execute( createTable_temp_t );	
						String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
						System.out.println("zqian createTable_score  : "+ createTable_score  );
						st.execute( createTable_score );	
						
						
						
						
						st_t.close();

					}
					rstarget_showcolumns.close();					
			 }	
		
			st.close();	
		}
		
		catch (SQLException e)
		{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		System.out.println("zqian: Leave createFinal3_zqian!");	
	}
	
	
	
}
