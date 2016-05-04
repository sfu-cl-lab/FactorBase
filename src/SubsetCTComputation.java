/*// May 26, 2014 zqian, load the largest rchain	
 * 
 * */

/*
 * SubsetCTComputation.java
 * 
 * Author: Kurt Routley
 * Date Created: Thursday, February 20, 2014
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.mysql.jdbc.Connection;

public class SubsetCTComputation
{
	private static Connection con;
	private static String rchain;
	private static String dbaddress;
	private static String dbusername;
	private static String dbpassword;
	private static String dbBNschema;
	private static String dbDataSchema;
	private static String dbOutputSchema;
	private static String pathBayesNet;
	private static boolean linkAnalysis;
	private static boolean continuous;
	private static boolean usingGroundings;
	
	public static void main( String[] args ) throws SQLException
	{
		long t1 = System.currentTimeMillis();
		
		//compute_subset_CT(  );
		//						functorId, database_BN, database_target, database_target_db (storing sub CTs)
		computeTargetSubset_CTs("`RA(prof0,student0)`","unielwin_Training1_BN","unielwin_Training1_target","unielwin_Training1_target_BN","unielwin_Training1_target_db");
		
		long t2 = System.currentTimeMillis();
		
		System.out.println( "Total runtime: " + ( t2 - t1 ) + "ms" );
	}
	
	public static int compute_subset_CT( ) throws SQLException
	{
		
		/*
		 * Load Configuration
		 */
		System.out.println( "Loading configuration..." );
		if ( loadConfig() != 0 )
		{
			System.out.println( "Failed to load configuration." );
			return -1;
		}
		
		/*
		 * Connect to Database
		 */
		System.out.println( "Connecting to database..." );
		if ( connectDB( "" ) != 0 )
		{
			System.out.println( "Failed to connect to database." );
			return -2;
		}
		
// May 26, 2014 zqian, load the largest rchain		
		 String largest_rchain="";
         Statement st_largest=con.createStatement();
         ResultSet rs_largest= st_largest.executeQuery(" Select name as Rchain from "+dbBNschema+".lattice_set where length=( SELECT max(length) FROM "+dbBNschema+".lattice_set); ");
         rs_largest.absolute(1);
         largest_rchain = rs_largest.getString(1);
     	//System.out.println("\n largest_rchain : " + largest_rchain);
         st_largest.close();
		
		/*
		 * Initialize local variables
		 */
		rchain = largest_rchain.replace( "`", "" );
		
		
		/*
		 * Clear temporary subtables
		 */
		System.out.println( "Dropping temporary tables..." );
		if ( clearTemporaryTables() != 0 )
		{
			System.out.println( "Failed to clear temporary tables." );
			return -6;
		}
		/*
		 * Create OutputSchema if it doesn't exist
		 */
		System.out.println( "Creating new schema..." );
		if ( createSubDatabase() != 0 )
		{
			System.out.println( "Failed to interact with " + dbOutputSchema );
			return -3;
		}
		
		/*
		 * Create temporary subtables to get subcounts
		 */
		System.out.println( "Creating temporary tables..." );
		if ( createSubTables() != 0 )
		{
			System.out.println( "Failed to create subtables." );
			return -4;
		}
		
		/*
		 * Get counts wrt rchain, fid
		 */
		System.out.println( "Getting counts..." );
		if ( getSubCounts() != 0 )
		{
			System.out.println( "Failed to get subset CT tables." );
			return -5;
		}
		
		
		/*
		 * Clear temporary subtables
		 */
		System.out.println( "Dropping temporary tables..." );
		if ( clearTemporaryTables() != 0 )
		{
			System.out.println( "Failed to clear temporary tables." );
			return -6;
		}
		
		/*
		 * Disconnect from DB
		 */
		System.out.println( "Disconnecting..." );
		if ( disconnectDB() != 0 )
		{
			System.out.println( "Failed to disconnect from database." );
			return -7;
		}
		
		return 0;
	}
	
	/*
	 * Read in configuration from cfg/subsetctcomputation.cfg
	 */
	private static int loadConfig()
	{
		Properties configFile = new java.util.Properties();
		FileReader fr = null;
		
		try
		{
			fr = new FileReader( "cfg/subsetctcomputation.cfg" );
		}
		catch ( FileNotFoundException e )
		{
			System.out.println( "Failed to find configuration file!" );
			return -1;
		}
		
		BufferedReader br = new BufferedReader( fr );
		
		try
		{
			configFile.load( br );
			
			dbaddress		= configFile.getProperty( "dbaddress" );
			dbusername		= configFile.getProperty( "dbusername" );
			dbpassword		= configFile.getProperty( "dbpassword" );
			
			/*
			 * dbBNschema is the database containing the pathBayesNet
			 */
			dbBNschema		= configFile.getProperty( "dbBNschema" );
			
			/*
			 * dbDataSchema is the database containing the original data
			 */
			dbDataSchema	= configFile.getProperty( "dbDataSchema" );
			
			/*
			 * dbOutputSchema is the database to write the new local CT tables to.
			 */
			dbOutputSchema	= configFile.getProperty( "dbOutputSchema" );
			pathBayesNet	= configFile.getProperty( "pathBayesNet" );
			String la		= configFile.getProperty( "LinkAnalysis" );
			if ( la.equals( "1" ) )
			{
				linkAnalysis = true;
			}
			else
			{
				linkAnalysis = false;
			}
			String cont		= configFile.getProperty( "Continuous" );
			if ( cont.equals( "1" ) )
			{
				continuous = true;
			}
			else
			{
				continuous = false;
			}
			
			String ground = configFile.getProperty( "Grounded" );
			if ( ground.equals( "1" ) )
			{
				usingGroundings = true;
			}
			else
			{
				usingGroundings = false;
			}

			br.close();
			fr.close();
		}
		catch ( IOException e )
		{
			System.out.println( "Failed to load configuration file." );
			return -2;
		}
		
		return 0;
	}
	
	/*
	 * Connects to supplied schema
	 */
	private static int connectDB( String schema )
	{
		String CONN_STR = "jdbc:" + dbaddress + "/" + schema;
		try 
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch ( Exception ex ) 
		{
			System.err.println( "Unable to load MySQL JDBC driver" );
			ex.printStackTrace();
			return -1;
		}
		
		try
		{
			con = (Connection) DriverManager.getConnection( CONN_STR, 
															dbusername, 
															dbpassword );
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to connect to database." );
			e.printStackTrace();
			return -2;
		}
		
		return 0;
	}
	
	/*
	 * Closes connection
	 */
	private static int disconnectDB()
	{
		try
		{
			con.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to close database connection." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Create output database if it doesn't already exist
	 */
	private static int createSubDatabase()
	{
		try
		{
			/*
			 * Create the output schema if it doesn't already exist
			 */
			Statement st = con.createStatement();
			st.execute( "drop database IF EXISTS " + dbOutputSchema + ";" ); // // May 22, 2014 zqian, computing the score for link analysis off.
			st.execute( "CREATE SCHEMA IF NOT EXISTS " + dbOutputSchema + ";" );
			
			/*
			 * Reconnect with output schema as default schema
			 */
			
			st.close();
			
			if ( disconnectDB() != 0 )
			{
				System.out.println( "Failed to disconnect from database." );
				return -1;
			}
			
			if ( connectDB( dbOutputSchema ) != 0 )
			{
				System.out.println( "Failed to connect to " + dbOutputSchema );
				return -2;
			}
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create subdatabase." );
			e.printStackTrace();
			return -3;
		}
		
		return 0;
	}
	
	/*
	 * Prepare FID specific table for CT computation
	 */
	private static int createSubTables()
	{
		try
		{
			Statement st = con.createStatement();
			
			/*
			 * If link analysis is off, we need to be able to connect 1nodes
			 * with different pvids when there is no 2node in the family
			 */
//			if ( !linkAnalysis )
//			{
				/*
				 * LearnedEdges
				 */
//				st.execute( "DROP TABLE IF EXISTS " + dbBNschema + ".LearnedEdges;" );
//				st.execute( "CREATE TABLE " + dbBNschema + ".LearnedEdges AS " + 
//							"SELECT " + dbBNschema + "." + pathBayesNet + ".Rchain, " + 
//							dbBNschema + "." + pathBayesNet + ".child, " + 
//							dbBNschema + "." + pathBayesNet + ".parent FROM " + 
//							dbBNschema + "." + pathBayesNet + " WHERE " + 
//							dbBNschema + "." + pathBayesNet + ".parent <> '' AND ( " + 
//							dbBNschema + "." + pathBayesNet + ".Rchain , " + 
//							dbBNschema + "." + pathBayesNet + ".child, " + 
//							dbBNschema + "." + pathBayesNet + ".parent ) NOT " + 
//							"IN ( SELECT * FROM " + dbBNschema + ".Path_Required_Edges );" );
//				
//				/*
//				 * ContextEdges
//				 */
//				st.execute( "DROP TABLE IF EXISTS " + dbBNschema + ".ContextEdges;" );
//				st.execute( "CREATE TABLE " + dbBNschema + ".ContextEdges AS " + 
//							"SELECT DISTINCT " + dbBNschema + ".LearnedEdges.Rchain " + 
//							"AS Rchain, " + dbBNschema + ".LearnedEdges.child " + 
//							"AS child, " + dbBNschema + ".lattice_membership.member " + 
//							"AS parent FROM " + dbBNschema + ".LearnedEdges, " + 
//							dbBNschema + ".lattice_membership WHERE " + dbBNschema + 
//							".LearnedEdges.Rchain = " + dbBNschema + 
//							".lattice_membership.name;" );
				
				/*
				 * Inject relation edges into pathBayesNet
				 */
//				ResultSet rs = st.executeQuery( "SELECT * FROM ContextEdges;" );
//				
//				while ( rs.next() )
//				{
//					String Rchain = rs.getString( 1 );
//					String child = rs.getString( 2 );
//					String parent = rs.getString( 3 );
//					
//					Statement st2 = con.createStatement();
//					
//					
//					
//					st2.close();
//				}
//				
//				rs.close();
//				st.execute( "INSERT IGNORE INTO " + dbBNschema + "." + pathBayesNet + 
//							" SELECT * FROM " + dbBNschema + ".ContextEdges;" );
//			}
			
			/*
			 * Get FID with parents
			 */
			st.execute( "DROP TABLE IF EXISTS FidWithParents;" );
			st.execute( "CREATE TABLE FidWithParents AS SELECT DISTINCT child AS " + 
						"FID, parent AS Parent FROM " + dbBNschema + "." +
						pathBayesNet + " WHERE rchain='`" + rchain + "`' AND " + 
						"( child IN ( SELECT 1nid FROM " + dbBNschema + ".1Nodes ) OR child IN " + 
						"( SELECT 2nid FROM " + dbBNschema + ".2Nodes ) OR " + 
						"child IN ( SELECT rnid FROM " + dbBNschema + ".RNodes ) ) AND parent <> ''" + 
						"UNION SELECT DISTINCT child AS FID, child AS Parent " + 
						"FROM " + dbBNschema + "." + pathBayesNet + " WHERE " + 
						"rchain = '`" + rchain + "`';"  );
			
			/*
			 * PVariables
			 */
			st.execute( "DROP TABLE IF EXISTS PVariables;" );
			st.execute( "CREATE TABLE PVariables AS SELECT * FROM " + dbBNschema + ".PVariables;" );
			
			ArrayList<String> fids = new ArrayList<String>();
			
			ResultSet rs = st.executeQuery( "SELECT DISTINCT child FROM " + 
											dbBNschema + "." + pathBayesNet +
											" WHERE rchain = '`" + rchain + "`';" );
			
			while ( rs.next() )
			{
				String fid = rs.getString( 1 ).replace("`", "");
				fids.add( fid );
			}
			
			rs.close();
			
			int len = fids.size();
			
			for ( int i = 0; i < len; i++ )
			{
				String fid = fids.get( i );
				System.out.println("Fid is :" + fid);
				/*
				 * 1Nodes
				 * Only select 1Nodes which are either the FID or the FID's
				 * parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_1Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
						    dbBNschema + ".1Nodes WHERE 1nid IN ( SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`' ) OR 1nid = '`" + fid + "`';" );
				
				/*
				 * 2Nodes
				 * Only select 2Nodes which are either the FID or the FID's parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_2Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_2Nodes` AS SELECT * FROM " + 
						    dbBNschema + ".2Nodes WHERE 2nid IN ( SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`' ) OR 2nid = '`" + fid + "`';" );
				
				/*
				 * RNodes
				 * RNodes associated to parent 2Nodes are also included so we
				 * can get the 2Node values from the RNode data tables. This
				 * does not effect the final count for each FID
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes` AS SELECT * FROM " + 
							dbBNschema + ".RNodes WHERE rnid IN (SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`') or rnid " + 
						    "IN (SELECT rnid FROM " + dbBNschema + ".RNodes_2Nodes " + 
						    "WHERE 2nid IN (SELECT 2nid FROM `" + fid + 
						    "_2Nodes`)) OR rnid = '`" + fid + "`' ;" ); // need to be updated, zqian May 23, 2014
						    		//+ " or rnid IN ( SELECT rnid FROM " + dbBNschema + ".RNodes_1Nodes WHERE 1nid IN (SELECT 1nid FROM `" + fid +"_1Nodes`) and pvid in (SELECT pvid1 FROM " + dbBNschema + ".RNodes)) ;" );
				
				/*
				 * ForeignKeyColumns
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ForeignKeyColumns`;" );
				st.execute( "CREATE TABLE `" + fid + "_ForeignKeyColumns` AS " + 
							"SELECT * FROM " + dbBNschema + ".ForeignKeyColumns " + 
							"WHERE " + dbBNschema + ".ForeignKeyColumns.TABLE_NAME " + 
							"IN ( SELECT `" + fid + "_RNodes`.TABLE_NAME FROM " + 
							"`" + fid + "_RNodes` ) ;" );
				
				/*
				 * lattice_membership, lattice_set, lattice_rel, lattice_mapping
				 */
				if ( createLocalLattice( fid ) != 0 )
				{
					System.out.println( "Failed to create local lattice." );
					return -2;
				}
				
				/*
				 * FNodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_FNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_FNodes` AS SELECT 1nid " + 
							"AS Fid, COLUMN_NAME AS FunctorName, '1Node' AS " + 
							"Type, main FROM `" + fid + "_1Nodes` UNION SELECT " + 
							"2nid AS Fid, COLUMN_NAME AS FunctorName, '2Node' " + 
							"AS Type, main FROM `" + fid + "_2Nodes` UNION " + 
							"SELECT rnid AS FID, TABLE_NAME AS FunctorName, " + 
							"'Rnode' AS Type, main FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_1Nodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_1Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_1Nodes` AS " + 
							"SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid1 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + 
							"_RNodes`.pvid1 UNION SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid2 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + "_RNodes`.pvid2;" );
				
				/*
				 * RNodes_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Select_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_Select_List` AS SELECT rnid, " + 
							"concat( 'count(*)', ' as \"MULT\"' ) AS Entries " + 
							"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
							"rnid, CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) " + 
							"AS Entries FROM `" + fid + "_RNodes_1Nodes` " + 
							"UNION DISTINCT SELECT temp.rnid, temp.Entries " + 
							"FROM ( SELECT DISTINCT rnid, CONCAT( rnid, '.', " + 
							"COLUMN_NAME, ' AS ', 2nid ) AS Entries FROM `" + 
							fid + "_2Nodes` NATURAL JOIN `" + fid + "_RNodes` " + 
							"ORDER BY `" + fid + "_RNodes`.rnid, COLUMN_NAME ) " + 
							"AS temp UNION DISTINCT SELECT rnid, rnid AS " + 
							"Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_pvars
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_pvars`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_pvars` AS SELECT " + 
							"DISTINCT rnid, pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid1 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME1 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = " + 
							"PVariables.TABLE_NAME UNION SELECT DISTINCT rnid, " + 
							"pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid2 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME2 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;" );
				
				/*
				 * RNodes_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_From_List` AS " + 
							"SELECT DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', pvid ) AS Entries FROM `" + 
							fid + "_RNodes_pvars` UNION DISTINCT SELECT " + 
							"DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', rnid ) AS Entries FROM `" + 
							fid + "_RNodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, concat( '(SELECT \"T\" AS ', rnid, ' ) AS ', " + 
							"concat( '`temp_', replace(rnid, '`', ''), '`')) " + 
							"AS Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_Where_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Where_List`;" );
				
				if ( usingGroundings )
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` UNION " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', " + dbBNschema + ".Groundings.id ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` NATURAL " + 
							"JOIN " + dbBNschema + ".Groundings;" );
				}
				else
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
								"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
								"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
				}
				
				/*
				 * ADT_PVariables_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_Select_List`" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_Select_List` " + 
							"AS SELECT pvid, CONCAT( 'count(*)', ' AS \"MULT\"' ) " + 
							"AS Entries FROM PVariables UNION SELECT pvid, " + 
							"CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) AS " + 
							"Entries FROM `" + fid + "_1Nodes` NATURAL JOIN " + 
							"PVariables;" );
				
				/*
				 * ADT_PVariables_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_From_List` AS SELECT " + 
							"pvid, CONCAT( '" + dbDataSchema + ".', TABLE_NAME, " + 
							"' AS ', pvid ) AS Entries FROM PVariables;" );
				
				/*
				 * ADT_PVariables_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_GroupBy_List` " + 
							"AS SELECT pvid, 1nid AS Entries FROM `" + fid + 
							"_1Nodes` NATURAL JOIN PVariables;" );
				
				/*
				 * Rnodes_join_columnname_list
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_Rnodes_join_columnname_list`;" );
				st.execute( "CREATE TABLE `" + fid + "_Rnodes_join_columnname_list` " + 
							"AS SELECT DISTINCT rnid, concat( 2nid, " + 
							"' VARCHAR(5) DEFAULT ', ' \"N/A\" ' ) AS Entries " + 
							"FROM `" + fid + "_2Nodes` NATURAL JOIN `" + fid + 
							"_RNodes`;" );
				
				/*
				 * RNodes_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_GroupBy_List` AS " + 
							"SELECT DISTINCT rnid, 1nid AS Entries FROM `" + fid + 
							"_RNodes_1Nodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, 2nid AS Entries FROM `" + fid + "_2Nodes` " + 
							"NATURAL JOIN `" + fid + "_RNodes` UNION DISTINCT " + 
							"SELECT rnid, rnid FROM `" + fid + "_RNodes`;" );
				
				if ( linkAnalysis )
				{
					/*
					 * ADT_RNodes_1Nodes_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_Select_List` " + 
								"AS SELECT rnid, CONCAT( 'SUM(`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`.`MULT`)',' AS \"MULT\"' ) AS Entries " + 
								"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
								"rnid, 1nid AS Entries FROM `" + fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_FROM_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_FROM_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_FROM_List` " + 
								"AS SELECT rnid, CONCAT( '`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`' ) AS Entries FROM `" + fid + "_RNodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_GroupBY_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_Select_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_From_List` " + 
								"AS SELECT DISTINCT rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( pvid, '`', '' ), '_counts`' ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
					
					/*
					 * RChain_pvars
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_RChain_pvars`;" );
					st.execute( "CREATE TABLE `" + fid + "_RChain_pvars` AS " + 
								"SELECT DISTINCT `" + fid + "_lattice_membership`.name " + 
								"AS rchain, pvid FROM `" + fid + "_lattice_membership`, `" + 
								fid +  "_RNodes_pvars` WHERE `" + fid + 
								"_RNodes_pvars`.rnid = `" + fid + "_lattice_membership`.member;" );
					
					/*
					 * ADT_RChain_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Select_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed AS " + 
								"rnid, `" + fid + "_RNodes_GroupBy_List`.Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + "_lattice_membership`, `" + 
								fid + "_RNodes_GroupBy_List` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet'  AND `" + fid + "_lattice_membership`.name " + 
								"= `" + fid + "_lattice_rel`.parent AND `" + fid + "_RNodes_GroupBy_List`.rnid " + 
								"= `" + fid + "_lattice_membership`.member UNION " + 
								"SELECT DISTINCT `" + fid + "_lattice_rel`.child AS rchain, `" + 
								fid + "_lattice_rel`.removed AS rnid, `" + fid + "_1Nodes`.`1nid` " + 
								"AS Entries FROM `" + fid + "_lattice_rel`, `" + fid + "_RNodes_pvars`, `" + 
								fid + "_1Nodes` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid = `" + fid + 
								"_lattice_rel`.removed AND `" + fid + "_RNodes_pvars`.pvid " + 
								"= `" + fid + "_1Nodes`.pvid AND `" + fid + "_1Nodes`.pvid " + 
								"NOT IN ( SELECT pvid FROM `" + fid + "_RChain_pvars` " + 
								"WHERE `" + fid + "_RChain_pvars`.rchain = 	`" + fid + 
								"_lattice_rel`.parent ) UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.removed AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, `" + fid + "_1Nodes`.`1nid` AS Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + 
								"_RNodes_pvars`, `" + fid + "_1Nodes` WHERE `" + 
								fid + "_lattice_rel`.parent = 'EmptySet' AND `" + 
								fid + "_RNodes_pvars`.rnid = `" + fid + "_lattice_rel`.removed " + 
								"AND `" + fid + "_RNodes_pvars`.pvid = `" + fid + "_1Nodes`.pvid;" );
					
					/*
					 * ADT_RChain_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_From_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( '`" + fid + "_', REPLACE( `" + fid + "_lattice_rel`.parent, " + 
								"'`', '' ), '_CT`' ) AS Entries FROM `" + fid + 
								"_lattice_rel` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.child AS rchain, `" + fid + 
								"_lattice_rel`.removed AS rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( `" + fid + "_RNodes_pvars`.pvid, '`', " + 
								"'' ), '_counts`' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_RNodes_pvars` " + 
								"WHERE `" + fid + "_lattice_rel`.parent <> " + 
								"'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid " + 
								"= `" + fid + "_lattice_rel`.removed AND `" + fid + 
								"_RNodes_pvars`.pvid NOT IN ( SELECT pvid FROM `" + 
								fid + "_RChain_pvars` WHERE `" + fid + 
								"_RChain_pvars`.rchain = `" + fid + "_lattice_rel`.parent );" );
					
					/*
					 * ADT_RChain_Star_Where_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Where_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Where_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( `" + fid + "_lattice_membership`.member, " + 
								"' = \"T\"' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_lattice_membership` " + 
								"WHERE `" + fid + "_lattice_rel`.child = `" + fid + 
								"_lattice_membership`.name AND `" + fid + 
								"_lattice_membership`.member > `" + fid + 
								"_lattice_rel`.removed AND `" + fid + 
								"_lattice_rel`.parent <> 'EmptySet';" );
				}
			}
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create subtables." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}

	public static int computeTargetSubset_CTs(String functorId, String database_BN,String database_target,String database_target_bn,String database_db ) throws SQLException
	{
		
		/*
		 * Load Configuration
		 */
		System.out.println( "Loading configuration..." );
		if ( loadConfig() != 0 )
		{
			System.out.println( "Failed to load configuration." );
			return -1;
		}
		
		/*
		 * Connect to Database
		 */
		System.out.println( "Connecting to database..." );
		if ( connectDB( "" ) != 0 )
		{
			System.out.println( "Failed to connect to database." );
			return -2;
		}
		/*
		 * Create OutputSchema if it doesn't exist
		 */
		System.out.println( "Creating new schema..." );
		if ( createTargetSubDatabase(database_db) != 0 ) //unielwin_training1_target_db
		{
			System.out.println( "Failed to interact with " + database_db );
			return -3;
		}
		
		/*
		 * Create temporary subtables to get subcounts
		 */
		System.out.println( "Creating temporary tables..." );
		if ( createTargetSubTables(database_BN,database_target,database_target_bn,functorId) != 0 )
		{
			System.out.println( "Failed to create subtables." );
			return -4;
		}
		
		/*
		 * Get counts wrt rchain, fid
		 */
		System.out.println( "Getting counts..." );
		if ( getTargetSubCounts(database_BN,database_db,functorId) != 0 )
		{
			System.out.println( "Failed to get subset CT tables." );
			return -5;
		}
		
		
		/*
		 * Clear temporary subtables
		 */
		System.out.println( "Dropping temporary tables..." );
		if ( clearTemporaryTables() != 0 )
		{
			System.out.println( "Failed to clear temporary tables." );
			return -6;
		}
		
		/*
		 * Disconnect from DB
		 */
		System.out.println( "Disconnecting..." );
		if ( disconnectDB() != 0 )
		{
			System.out.println( "Failed to disconnect from database." );
			return -7;
		}
		
		return 0;
	}
	public static int computeTargetSubset_CTs(String functorId, String database_BN,String database_target,String database_target_bn,String database_db,String child,String child1 ) throws SQLException
	{
		
		/*
		 * Load Configuration
		 */
		System.out.println( "Loading configuration..." );
		if ( loadConfig() != 0 )
		{
			System.out.println( "Failed to load configuration." );
			return -1;
		}
		
		/*
		 * Connect to Database
		 */
		System.out.println( "Connecting to database..." );
		if ( connectDB( "" ) != 0 )
		{
			System.out.println( "Failed to connect to database." );
			return -2;
		}
		/*
		 * Create OutputSchema if it doesn't exist
		 */
		System.out.println( "Creating new schema..." );
		if ( createTargetSubDatabase(database_db) != 0 ) //unielwin_training1_target_db
		{
			System.out.println( "Failed to interact with " + database_db );
			return -3;
		}
		
		/*
		 * Create temporary subtables to get subcounts
		 */
		System.out.println( "Creating temporary tables..." );
		if ( createTargetSubTables(database_BN,database_target,database_target_bn,functorId,child,child1) != 0 )
		{
			System.out.println( "Failed to create subtables." );
			return -4;
		}
		
		/*
		 * Get counts wrt rchain, fid
		 */
		System.out.println( "Getting counts..." );
		if ( getTargetSubCounts(database_BN,database_db,functorId) != 0 )
		{
			System.out.println( "Failed to get subset CT tables." );
			return -5;
		}
		
		
		/*
		 * Clear temporary subtables
		 */
		System.out.println( "Dropping temporary tables..." );
		if ( clearTemporaryTables() != 0 )
		{
			System.out.println( "Failed to clear temporary tables." );
			return -6;
		}
		
		/*
		 * Disconnect from DB
		 */
		System.out.println( "Disconnecting..." );
		if ( disconnectDB() != 0 )
		{
			System.out.println( "Failed to disconnect from database." );
			return -7;
		}
		
		return 0;
	}
	
	private static int createTargetSubDatabase(String database_db)
	{
		try
		{
			
			dbOutputSchema = database_db; //unielwin_training1_target_db
			/*
			 * Create the output schema if it doesn't already exist
			 */
			Statement st = con.createStatement();
			st.execute( "drop database IF EXISTS " + dbOutputSchema + ";" ); 
			st.execute( "CREATE SCHEMA IF NOT EXISTS " + dbOutputSchema + ";" );
			
			/*
			 * Reconnect with output schema as default schema
			 */
			
			st.close();
			
			if ( disconnectDB() != 0 )
			{
				System.out.println( "Failed to disconnect from database." );
				return -1;
			}
			
			if ( connectDB( dbOutputSchema ) != 0 )
			{
				System.out.println( "Failed to connect to " + dbOutputSchema );
				return -2;
			}
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create subdatabase." );
			e.printStackTrace();
			return -3;
		}
		
		return 0;
	}
	
	private static int createTargetSubTables(String database_BN,String database_target,String database_target_bn,String functorId)
	{
		try
		{
			// conntect to database_db?
			Statement st = con.createStatement();			
			dbBNschema= database_BN;
			dbDataSchema= database_target;
			/*
			 * Get FID with parents // modify this based on each family configuration?
			 */
			st.execute( "DROP TABLE IF EXISTS FidWithParents;" );
			st.execute( "CREATE TABLE FidWithParents AS SELECT TargetNode AS " + 
						"FID, TargetParent AS Parent FROM " + dbBNschema + ".TargetParents "
								+ "WHERE TargetNode='`" + functorId.replace("`","") + "`' ;"  );
				
			/*
			 * PVariables
			 */
			st.execute( "DROP TABLE IF EXISTS PVariables;" );
			st.execute( "CREATE TABLE PVariables AS SELECT * FROM " + database_target_bn + ".PVariables;" );
			
						
	
				//String fid = fids.get( i );
				String fid = functorId.replace("`","");
				System.out.println("Fid is :" + fid);
				/*
				 * 1Nodes
				 * Only select 1Nodes which are either the FID or the FID's
				 * parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_1Nodes`;" );
//				System.out.println( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
//						database_target_bn + ".1Nodes ;" ); // contain extra 1node, Aug. 22, zqian
//				st.execute( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " +   // Aug 19 2014, including the primary keys for testing database, zqian
//						database_target_bn + ".1Nodes ;" );
				
				System.out.println( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
						database_target_bn + ".1Nodes WHERE 1nid IN ( SELECT " + 
					    "Parent FROM FidWithParents WHERE FID = '`" + 
					    fid + "`' ) OR 1nid = '`" + fid + "`'  "
					    		+ " union  select * from "+database_target_bn+".Test_1nid ;" );
				
				st.execute( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
						database_target_bn + ".1Nodes WHERE 1nid IN ( SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`' ) OR 1nid = '`" + fid + "`'  "
						    		+ " union  select * from "+database_target_bn+".Test_1nid ;" );
				
				/*
				 * 2Nodes
				 * Only select 2Nodes which are either the FID or the FID's parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_2Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_2Nodes` AS SELECT * FROM " + 
						database_target_bn + ".2Nodes WHERE 2nid IN ( SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`' ) OR 2nid = '`" + fid + "`';" );
				
				/*
				 * RNodes
				 * RNodes associated to parent 2Nodes are also included so we
				 * can get the 2Node values from the RNode data tables. This
				 * does not effect the final count for each FID
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes` AS SELECT * FROM " + 
						database_target_bn + ".RNodes WHERE rnid IN (SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`') or rnid " + 
						    "IN (SELECT rnid FROM " + database_target_bn + ".RNodes_2Nodes " + 
						    "WHERE 2nid IN (SELECT 2nid FROM `" + fid + 
						    "_2Nodes`)) OR rnid = '`" + fid + "`' ;" ); // need to be updated, zqian May 23, 2014
						    		//+ " or rnid IN ( SELECT rnid FROM " + dbBNschema + ".RNodes_1Nodes WHERE 1nid IN (SELECT 1nid FROM `" + fid +"_1Nodes`) and pvid in (SELECT pvid1 FROM " + dbBNschema + ".RNodes)) ;" );
				
				/*
				 * ForeignKeyColumns
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ForeignKeyColumns`;" );
				st.execute( "CREATE TABLE `" + fid + "_ForeignKeyColumns` AS " + 
							"SELECT * FROM " + database_target_bn + ".ForeignKeyColumns " + 
							"WHERE " + database_target_bn + ".ForeignKeyColumns.TABLE_NAME " + 
							"IN ( SELECT `" + fid + "_RNodes`.TABLE_NAME FROM " + 
							"`" + fid + "_RNodes` ) ;" );
				
				/*
				 * lattice_membership, lattice_set, lattice_rel, lattice_mapping
				 */
				if ( createLocalLattice( fid ) != 0 )
				{
					System.out.println( "Failed to create local lattice." );
					return -2;
				}
				
				/*
				 * FNodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_FNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_FNodes` AS SELECT 1nid " + 
							"AS Fid, COLUMN_NAME AS FunctorName, '1Node' AS " + 
							"Type, main FROM `" + fid + "_1Nodes` UNION SELECT " + 
							"2nid AS Fid, COLUMN_NAME AS FunctorName, '2Node' " + 
							"AS Type, main FROM `" + fid + "_2Nodes` UNION " + 
							"SELECT rnid AS FID, TABLE_NAME AS FunctorName, " + 
							"'Rnode' AS Type, main FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_1Nodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_1Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_1Nodes` AS " + 
							"SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid1 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + 
							"_RNodes`.pvid1 UNION SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid2 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + "_RNodes`.pvid2;" );
				
				/*
				 * RNodes_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Select_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_Select_List` AS SELECT rnid, " + 
							"concat( 'count(*)', ' as \"MULT\"' ) AS Entries " + 
							"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
							"rnid, CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) " + 
							"AS Entries FROM `" + fid + "_RNodes_1Nodes` " + 
							"UNION DISTINCT SELECT temp.rnid, temp.Entries " + 
							"FROM ( SELECT DISTINCT rnid, CONCAT( rnid, '.', " + 
							"COLUMN_NAME, ' AS ', 2nid ) AS Entries FROM `" + 
							fid + "_2Nodes` NATURAL JOIN `" + fid + "_RNodes` " + 
							"ORDER BY `" + fid + "_RNodes`.rnid, COLUMN_NAME ) " + 
							"AS temp UNION DISTINCT SELECT rnid, rnid AS " + 
							"Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_pvars
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_pvars`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_pvars` AS SELECT " + 
							"DISTINCT rnid, pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid1 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME1 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = " + 
							"PVariables.TABLE_NAME UNION SELECT DISTINCT rnid, " + 
							"pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid2 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME2 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;" );
				
				/*
				 * RNodes_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_From_List` AS " + 
							"SELECT DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', pvid ) AS Entries FROM `" + 
							fid + "_RNodes_pvars` UNION DISTINCT SELECT " + 
							"DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', rnid ) AS Entries FROM `" + 
							fid + "_RNodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, concat( '(SELECT \"T\" AS ', rnid, ' ) AS ', " + 
							"concat( '`temp_', replace(rnid, '`', ''), '`')) " + 
							"AS Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_Where_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Where_List`;" );
				
				if ( usingGroundings )
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` UNION " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', " + database_target_bn + ".Groundings.id ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` NATURAL " + 
							"JOIN " + database_target_bn + ".Groundings;" );
				}
				else
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
								"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
								"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
				}
				
				/*
				 * ADT_PVariables_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_Select_List`" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_Select_List` " + 
							"AS SELECT pvid, CONCAT( 'count(*)', ' AS \"MULT\"' ) " + 
							"AS Entries FROM PVariables UNION SELECT pvid, " + 
							"CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) AS " + 
							"Entries FROM `" + fid + "_1Nodes` NATURAL JOIN " + 
							"PVariables;" );
				
				/*
				 * ADT_PVariables_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_From_List` AS SELECT " + 
							"pvid, CONCAT( '" + dbDataSchema + ".', TABLE_NAME, " + 
							"' AS ', pvid ) AS Entries FROM PVariables;" );
				
				/*
				 * ADT_PVariables_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_GroupBy_List` " + 
							"AS SELECT pvid, 1nid AS Entries FROM `" + fid + 
							"_1Nodes` NATURAL JOIN PVariables;" );
				
				/*
				 * Rnodes_join_columnname_list
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_Rnodes_join_columnname_list`;" );
				st.execute( "CREATE TABLE `" + fid + "_Rnodes_join_columnname_list` " + 
							"AS SELECT DISTINCT rnid, concat( 2nid, " + 
							"' VARCHAR(5) DEFAULT ', ' \"N/A\" ' ) AS Entries " + 
							"FROM `" + fid + "_2Nodes` NATURAL JOIN `" + fid + 
							"_RNodes`;" );
				
				/*
				 * RNodes_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_GroupBy_List` AS " + 
							"SELECT DISTINCT rnid, 1nid AS Entries FROM `" + fid + 
							"_RNodes_1Nodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, 2nid AS Entries FROM `" + fid + "_2Nodes` " + 
							"NATURAL JOIN `" + fid + "_RNodes` UNION DISTINCT " + 
							"SELECT rnid, rnid FROM `" + fid + "_RNodes`;" );
				
				if ( linkAnalysis )
				{
					/*
					 * ADT_RNodes_1Nodes_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_Select_List` " + 
								"AS SELECT rnid, CONCAT( 'SUM(`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`.`MULT`)',' AS \"MULT\"' ) AS Entries " + 
								"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
								"rnid, 1nid AS Entries FROM `" + fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_FROM_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_FROM_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_FROM_List` " + 
								"AS SELECT rnid, CONCAT( '`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`' ) AS Entries FROM `" + fid + "_RNodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_GroupBY_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_Select_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_From_List` " + 
								"AS SELECT DISTINCT rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( pvid, '`', '' ), '_counts`' ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
					
					/*
					 * RChain_pvars
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_RChain_pvars`;" );
					st.execute( "CREATE TABLE `" + fid + "_RChain_pvars` AS " + 
								"SELECT DISTINCT `" + fid + "_lattice_membership`.name " + 
								"AS rchain, pvid FROM `" + fid + "_lattice_membership`, `" + 
								fid +  "_RNodes_pvars` WHERE `" + fid + 
								"_RNodes_pvars`.rnid = `" + fid + "_lattice_membership`.member;" );
					
					/*
					 * ADT_RChain_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Select_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed AS " + 
								"rnid, `" + fid + "_RNodes_GroupBy_List`.Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + "_lattice_membership`, `" + 
								fid + "_RNodes_GroupBy_List` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet'  AND `" + fid + "_lattice_membership`.name " + 
								"= `" + fid + "_lattice_rel`.parent AND `" + fid + "_RNodes_GroupBy_List`.rnid " + 
								"= `" + fid + "_lattice_membership`.member UNION " + 
								"SELECT DISTINCT `" + fid + "_lattice_rel`.child AS rchain, `" + 
								fid + "_lattice_rel`.removed AS rnid, `" + fid + "_1Nodes`.`1nid` " + 
								"AS Entries FROM `" + fid + "_lattice_rel`, `" + fid + "_RNodes_pvars`, `" + 
								fid + "_1Nodes` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid = `" + fid + 
								"_lattice_rel`.removed AND `" + fid + "_RNodes_pvars`.pvid " + 
								"= `" + fid + "_1Nodes`.pvid AND `" + fid + "_1Nodes`.pvid " + 
								"NOT IN ( SELECT pvid FROM `" + fid + "_RChain_pvars` " + 
								"WHERE `" + fid + "_RChain_pvars`.rchain = 	`" + fid + 
								"_lattice_rel`.parent ) UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.removed AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, `" + fid + "_1Nodes`.`1nid` AS Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + 
								"_RNodes_pvars`, `" + fid + "_1Nodes` WHERE `" + 
								fid + "_lattice_rel`.parent = 'EmptySet' AND `" + 
								fid + "_RNodes_pvars`.rnid = `" + fid + "_lattice_rel`.removed " + 
								"AND `" + fid + "_RNodes_pvars`.pvid = `" + fid + "_1Nodes`.pvid;" );
					
					/*
					 * ADT_RChain_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_From_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( '`" + fid + "_', REPLACE( `" + fid + "_lattice_rel`.parent, " + 
								"'`', '' ), '_CT`' ) AS Entries FROM `" + fid + 
								"_lattice_rel` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.child AS rchain, `" + fid + 
								"_lattice_rel`.removed AS rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( `" + fid + "_RNodes_pvars`.pvid, '`', " + 
								"'' ), '_counts`' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_RNodes_pvars` " + 
								"WHERE `" + fid + "_lattice_rel`.parent <> " + 
								"'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid " + 
								"= `" + fid + "_lattice_rel`.removed AND `" + fid + 
								"_RNodes_pvars`.pvid NOT IN ( SELECT pvid FROM `" + 
								fid + "_RChain_pvars` WHERE `" + fid + 
								"_RChain_pvars`.rchain = `" + fid + "_lattice_rel`.parent );" );
					
					/*
					 * ADT_RChain_Star_Where_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Where_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Where_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( `" + fid + "_lattice_membership`.member, " + 
								"' = \"T\"' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_lattice_membership` " + 
								"WHERE `" + fid + "_lattice_rel`.child = `" + fid + 
								"_lattice_membership`.name AND `" + fid + 
								"_lattice_membership`.member > `" + fid + 
								"_lattice_rel`.removed AND `" + fid + 
								"_lattice_rel`.parent <> 'EmptySet';" );
			}
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create subtables." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	private static int createTargetSubTables(String database_BN,String database_target,String database_target_bn,String functorId, String child, String child1)
	{
		try
		{	
			// conntect to database_db?
			Statement st = con.createStatement();			
			dbBNschema= database_BN;
			dbDataSchema= database_target;
			/*
			 * Get FID with child and its parents 	 */
			
				st.execute( "DROP TABLE IF EXISTS FidWithParents;" );
					
				System.out.println( "CREATE TABLE FidWithParents AS SELECT '`"+ functorId.replace("`","")+ "`' AS " + 
						"FID, TargetParent AS Parent FROM " + dbBNschema + ".TargetParents  "
								+ "WHERE TargetNode='`" + child.replace("`","") + "`'"
										+ " union SELECT '`"+ functorId.replace("`","")+ "`' AS " + 
						"FID, '`" + child.replace("`","") + "`'"+" AS Parent ;");	
				st.execute( "CREATE TABLE FidWithParents AS SELECT '`"+ functorId.replace("`","")+ "`' AS " + 
						"FID, TargetParent AS Parent FROM " + dbBNschema + ".TargetParents  "
								+ "WHERE TargetNode='`" + child.replace("`","") + "`'"
										+ " union SELECT '`"+ functorId.replace("`","")+ "`' AS " + 
						"FID, '`" + child.replace("`","") + "`'"+" AS Parent ;");			
				
			
			/*
			 * PVariables
			 */
			st.execute( "DROP TABLE IF EXISTS PVariables;" );
			st.execute( "CREATE TABLE PVariables AS SELECT * FROM " + database_target_bn + ".PVariables;" );
			
						
	
				//String fid = fids.get( i );
				String fid = functorId.replace("`","");
				System.out.println("Fid is :" + fid);
				/*
				 * 1Nodes
				 * Only select 1Nodes which are either the FID or the FID's
				 * parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_1Nodes`;" );
//				System.out.println( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
//						database_target_bn + ".1Nodes ;" ); // contain extra 1node, Aug. 22, zqian
//				st.execute( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " +   // Aug 19 2014, including the primary keys for testing database, zqian
//						database_target_bn + ".1Nodes ;" );
//				
				System.out.println( "zqian target bn 1node: CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
						dbBNschema + ".1Nodes WHERE 1nid IN ( SELECT " + 
			    "TargetParent FROM "+dbBNschema + ".TargetParents WHERE TargetNode = '`" + 
			    child.replace("`","") + "`' ) OR 1nid = '`" + child.replace("`","") + "`' "
			    		+ "  union  select * from "+database_target_bn+".Test_1nid ;" );
				st.execute( "CREATE TABLE `" + fid + "_1Nodes` AS SELECT * FROM " + 
						dbBNschema + ".1Nodes WHERE 1nid IN ( SELECT " + 
					    "TargetParent FROM "+dbBNschema + ".TargetParents WHERE TargetNode = '`" + 
					    child.replace("`","") + "`' ) OR 1nid = '`" + child.replace("`","") + "`' "
					    		+ "  union  select * from "+database_target_bn+".Test_1nid ;" );
				
				/*
				 * 2Nodes
				 * Only select 2Nodes which are either the FID or the FID's parents
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_2Nodes`;" );
				System.out.println( "zqian target bn 2node: CREATE TABLE `" + fid + "_2Nodes` AS SELECT * FROM " + 
						dbBNschema + ".2Nodes WHERE 2nid IN ( SELECT " + 
			    "TargetParent FROM "+dbBNschema + ".TargetParents WHERE TargetNode = '`" + 
			    child.replace("`","") + "`' ) OR 2nid = '`" + child.replace("`","") + "`' "
			    		+ "  ;" );
				st.execute( "CREATE TABLE `" + fid + "_2Nodes` AS SELECT * FROM " + 
						dbBNschema + ".2Nodes WHERE 2nid IN ( SELECT " + 
			    "TargetParent FROM "+dbBNschema + ".TargetParents WHERE TargetNode = '`" + 
			    child.replace("`","") + "`' ) OR 2nid = '`" + child.replace("`","") + "`' "
			    		+ "  ;" );
				
				/*
				 * RNodes
				 * RNodes associated to parent 2Nodes are also included so we
				 * can get the 2Node values from the RNode data tables. This
				 * does not effect the final count for each FID
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes` AS SELECT * FROM " + 
						database_target_bn + ".RNodes WHERE rnid IN (SELECT " + 
						    "Parent FROM FidWithParents WHERE FID = '`" + 
						    fid + "`') or rnid " + 
						    "IN (SELECT rnid FROM " + database_target_bn + ".RNodes_2Nodes " + 
						    "WHERE 2nid IN (SELECT 2nid FROM `" + fid + 
						    "_2Nodes`)) OR rnid = '`" + fid + "`' ;" ); // need to be updated, zqian May 23, 2014
						    		//+ " or rnid IN ( SELECT rnid FROM " + dbBNschema + ".RNodes_1Nodes WHERE 1nid IN (SELECT 1nid FROM `" + fid +"_1Nodes`) and pvid in (SELECT pvid1 FROM " + dbBNschema + ".RNodes)) ;" );
				
				/*
				 * ForeignKeyColumns
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ForeignKeyColumns`;" );
				st.execute( "CREATE TABLE `" + fid + "_ForeignKeyColumns` AS " + 
							"SELECT * FROM " + database_target_bn + ".ForeignKeyColumns " + 
							"WHERE " + database_target_bn + ".ForeignKeyColumns.TABLE_NAME " + 
							"IN ( SELECT `" + fid + "_RNodes`.TABLE_NAME FROM " + 
							"`" + fid + "_RNodes` ) ;" );
				
				/*
				 * lattice_membership, lattice_set, lattice_rel, lattice_mapping
				 */
				if ( createLocalLattice( fid ) != 0 )
				{
					System.out.println( "Failed to create local lattice." );
					return -2;
				}
				
				/*
				 * FNodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_FNodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_FNodes` AS SELECT 1nid " + 
							"AS Fid, COLUMN_NAME AS FunctorName, '1Node' AS " + 
							"Type, main FROM `" + fid + "_1Nodes` UNION SELECT " + 
							"2nid AS Fid, COLUMN_NAME AS FunctorName, '2Node' " + 
							"AS Type, main FROM `" + fid + "_2Nodes` UNION " + 
							"SELECT rnid AS FID, TABLE_NAME AS FunctorName, " + 
							"'Rnode' AS Type, main FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_1Nodes
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_1Nodes`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_1Nodes` AS " + 
							"SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid1 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + 
							"_RNodes`.pvid1 UNION SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, " + 
							"pvid2 AS pvid FROM `" + fid + "_RNodes`, `" + fid + 
							"_1Nodes` WHERE `" + fid + "_1Nodes`.pvid = `" + fid + "_RNodes`.pvid2;" );
				
				/*
				 * RNodes_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Select_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_Select_List` AS SELECT rnid, " + 
							"concat( 'count(*)', ' as \"MULT\"' ) AS Entries " + 
							"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
							"rnid, CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) " + 
							"AS Entries FROM `" + fid + "_RNodes_1Nodes` " + 
							"UNION DISTINCT SELECT temp.rnid, temp.Entries " + 
							"FROM ( SELECT DISTINCT rnid, CONCAT( rnid, '.', " + 
							"COLUMN_NAME, ' AS ', 2nid ) AS Entries FROM `" + 
							fid + "_2Nodes` NATURAL JOIN `" + fid + "_RNodes` " + 
							"ORDER BY `" + fid + "_RNodes`.rnid, COLUMN_NAME ) " + 
							"AS temp UNION DISTINCT SELECT rnid, rnid AS " + 
							"Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_pvars
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_pvars`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_pvars` AS SELECT " + 
							"DISTINCT rnid, pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid1 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME1 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = " + 
							"PVariables.TABLE_NAME UNION SELECT DISTINCT rnid, " + 
							"pvid, PVariables.TABLE_NAME, `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME, `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_COLUMN_NAME FROM `" + fid + 
							"_ForeignKeyColumns`, `" + fid + "_RNodes`, " + 
							"PVariables WHERE pvid2 = pvid AND `" + fid + 
							"_ForeignKeyColumns`.TABLE_NAME = `" + fid + 
							"_RNodes`.TABLE_NAME AND `" + fid + 
							"_ForeignKeyColumns`.COLUMN_NAME = `" + fid + 
							"_RNodes`.COLUMN_NAME2 AND `" + fid + 
							"_ForeignKeyColumns`.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;" );
				
				/*
				 * RNodes_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_From_List` AS " + 
							"SELECT DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', pvid ) AS Entries FROM `" + 
							fid + "_RNodes_pvars` UNION DISTINCT SELECT " + 
							"DISTINCT rnid, CONCAT( '" + dbDataSchema + 
							".', TABLE_NAME, ' AS ', rnid ) AS Entries FROM `" + 
							fid + "_RNodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, concat( '(SELECT \"T\" AS ', rnid, ' ) AS ', " + 
							"concat( '`temp_', replace(rnid, '`', ''), '`')) " + 
							"AS Entries FROM `" + fid + "_RNodes`;" );
				
				/*
				 * RNodes_Where_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Where_List`;" );
				
				if ( usingGroundings )
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` UNION " + 
							"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
							"' = ', " + database_target_bn + ".Groundings.id ) AS " + 
							"Entries FROM `" + fid + "_RNodes_pvars` NATURAL " + 
							"JOIN " + database_target_bn + ".Groundings;" );
				}
				else
				{
					st.execute( "CREATE TABLE `" + fid + "_RNodes_Where_List` AS " + 
								"SELECT rnid, CONCAT( rnid, '.', COLUMN_NAME, " + 
								"' = ', pvid, '.', REFERENCED_COLUMN_NAME ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
				}
				
				/*
				 * ADT_PVariables_Select_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_Select_List`" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_Select_List` " + 
							"AS SELECT pvid, CONCAT( 'count(*)', ' AS \"MULT\"' ) " + 
							"AS Entries FROM PVariables UNION SELECT pvid, " + 
							"CONCAT( pvid, '.', COLUMN_NAME, ' AS ', 1nid ) AS " + 
							"Entries FROM `" + fid + "_1Nodes` NATURAL JOIN " + 
							"PVariables;" );
				
				/*
				 * ADT_PVariables_From_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_From_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_From_List` AS SELECT " + 
							"pvid, CONCAT( '" + dbDataSchema + ".', TABLE_NAME, " + 
							"' AS ', pvid ) AS Entries FROM PVariables;" );
				
				/*
				 * ADT_PVariables_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_ADT_PVariables_GroupBy_List` " + 
							"AS SELECT pvid, 1nid AS Entries FROM `" + fid + 
							"_1Nodes` NATURAL JOIN PVariables;" );
				
				/*
				 * Rnodes_join_columnname_list
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_Rnodes_join_columnname_list`;" );
				st.execute( "CREATE TABLE `" + fid + "_Rnodes_join_columnname_list` " + 
							"AS SELECT DISTINCT rnid, concat( 2nid, " + 
							"' VARCHAR(5) DEFAULT ', ' \"N/A\" ' ) AS Entries " + 
							"FROM `" + fid + "_2Nodes` NATURAL JOIN `" + fid + 
							"_RNodes`;" );
				
				/*
				 * RNodes_GroupBy_List
				 */
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_GroupBy_List`;" );
				st.execute( "CREATE TABLE `" + fid + "_RNodes_GroupBy_List` AS " + 
							"SELECT DISTINCT rnid, 1nid AS Entries FROM `" + fid + 
							"_RNodes_1Nodes` UNION DISTINCT SELECT DISTINCT " + 
							"rnid, 2nid AS Entries FROM `" + fid + "_2Nodes` " + 
							"NATURAL JOIN `" + fid + "_RNodes` UNION DISTINCT " + 
							"SELECT rnid, rnid FROM `" + fid + "_RNodes`;" );
				
				if ( linkAnalysis )
				{
					/*
					 * ADT_RNodes_1Nodes_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_Select_List` " + 
								"AS SELECT rnid, CONCAT( 'SUM(`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`.`MULT`)',' AS \"MULT\"' ) AS Entries " + 
								"FROM `" + fid + "_RNodes` UNION SELECT DISTINCT " + 
								"rnid, 1nid AS Entries FROM `" + fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_FROM_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_FROM_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_FROM_List` " + 
								"AS SELECT rnid, CONCAT( '`" + fid + "_', REPLACE( rnid, '`', '' ), " + 
								"'_counts`' ) AS Entries FROM `" + fid + "_RNodes`;" );
					
					/*
					 * ADT_RNodes_1Nodes_GroupBY_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_Select_List` " + 
								"AS SELECT DISTINCT rnid, 1nid AS Entries FROM `" + 
								fid + "_RNodes_1Nodes`;" );
					
					/*
					 * ADT_RNodes_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RNodes_Star_From_List` " + 
								"AS SELECT DISTINCT rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( pvid, '`', '' ), '_counts`' ) AS " + 
								"Entries FROM `" + fid + "_RNodes_pvars`;" );
					
					/*
					 * RChain_pvars
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_RChain_pvars`;" );
					st.execute( "CREATE TABLE `" + fid + "_RChain_pvars` AS " + 
								"SELECT DISTINCT `" + fid + "_lattice_membership`.name " + 
								"AS rchain, pvid FROM `" + fid + "_lattice_membership`, `" + 
								fid +  "_RNodes_pvars` WHERE `" + fid + 
								"_RNodes_pvars`.rnid = `" + fid + "_lattice_membership`.member;" );
					
					/*
					 * ADT_RChain_Star_Select_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Select_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Select_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed AS " + 
								"rnid, `" + fid + "_RNodes_GroupBy_List`.Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + "_lattice_membership`, `" + 
								fid + "_RNodes_GroupBy_List` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet'  AND `" + fid + "_lattice_membership`.name " + 
								"= `" + fid + "_lattice_rel`.parent AND `" + fid + "_RNodes_GroupBy_List`.rnid " + 
								"= `" + fid + "_lattice_membership`.member UNION " + 
								"SELECT DISTINCT `" + fid + "_lattice_rel`.child AS rchain, `" + 
								fid + "_lattice_rel`.removed AS rnid, `" + fid + "_1Nodes`.`1nid` " + 
								"AS Entries FROM `" + fid + "_lattice_rel`, `" + fid + "_RNodes_pvars`, `" + 
								fid + "_1Nodes` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid = `" + fid + 
								"_lattice_rel`.removed AND `" + fid + "_RNodes_pvars`.pvid " + 
								"= `" + fid + "_1Nodes`.pvid AND `" + fid + "_1Nodes`.pvid " + 
								"NOT IN ( SELECT pvid FROM `" + fid + "_RChain_pvars` " + 
								"WHERE `" + fid + "_RChain_pvars`.rchain = 	`" + fid + 
								"_lattice_rel`.parent ) UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.removed AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, `" + fid + "_1Nodes`.`1nid` AS Entries " + 
								"FROM `" + fid + "_lattice_rel`, `" + fid + 
								"_RNodes_pvars`, `" + fid + "_1Nodes` WHERE `" + 
								fid + "_lattice_rel`.parent = 'EmptySet' AND `" + 
								fid + "_RNodes_pvars`.rnid = `" + fid + "_lattice_rel`.removed " + 
								"AND `" + fid + "_RNodes_pvars`.pvid = `" + fid + "_1Nodes`.pvid;" );
					
					/*
					 * ADT_RChain_Star_From_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_From_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_From_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( '`" + fid + "_', REPLACE( `" + fid + "_lattice_rel`.parent, " + 
								"'`', '' ), '_CT`' ) AS Entries FROM `" + fid + 
								"_lattice_rel` WHERE `" + fid + "_lattice_rel`.parent " + 
								"<> 'EmptySet' UNION SELECT DISTINCT `" + fid + 
								"_lattice_rel`.child AS rchain, `" + fid + 
								"_lattice_rel`.removed AS rnid, CONCAT( '`" + fid + "_', " + 
								"REPLACE( `" + fid + "_RNodes_pvars`.pvid, '`', " + 
								"'' ), '_counts`' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_RNodes_pvars` " + 
								"WHERE `" + fid + "_lattice_rel`.parent <> " + 
								"'EmptySet' AND `" + fid + "_RNodes_pvars`.rnid " + 
								"= `" + fid + "_lattice_rel`.removed AND `" + fid + 
								"_RNodes_pvars`.pvid NOT IN ( SELECT pvid FROM `" + 
								fid + "_RChain_pvars` WHERE `" + fid + 
								"_RChain_pvars`.rchain = `" + fid + "_lattice_rel`.parent );" );
					
					/*
					 * ADT_RChain_Star_Where_List
					 */
					st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Where_List`;" );
					st.execute( "CREATE TABLE `" + fid + "_ADT_RChain_Star_Where_List` " + 
								"AS SELECT DISTINCT `" + fid + "_lattice_rel`.child " + 
								"AS rchain, `" + fid + "_lattice_rel`.removed " + 
								"AS rnid, CONCAT( `" + fid + "_lattice_membership`.member, " + 
								"' = \"T\"' ) AS Entries FROM `" + fid + 
								"_lattice_rel`, `" + fid + "_lattice_membership` " + 
								"WHERE `" + fid + "_lattice_rel`.child = `" + fid + 
								"_lattice_membership`.name AND `" + fid + 
								"_lattice_membership`.member > `" + fid + 
								"_lattice_rel`.removed AND `" + fid + 
								"_lattice_rel`.parent <> 'EmptySet';" );
			}
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create subtables." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
/*
 * */
private static int getTargetSubCounts(String database_BN,String database_db,String functorId)
	{
		try
		{
			Statement st = con.createStatement();
			dbBNschema= database_BN;		
			/*
			 * Iterate over each FID in the pathBayesNet
			 */
			ResultSet rs ;				

			
				//String fid = fids.get( i );
				String fid = functorId.replace("`","");
				System.out.println("Fid is :" + fid);
				/*
				 * Get <fid>_<pvid>_counts
				 */
				if ( BuildCT_Pvars( fid ) != 0 )
				{
					System.out.println( "Failed to get pvariable counts." );
					return -2;
				}
				
				/*
				 * Get false values for <fid>_<rnid>_join
				 */
				if ( BuildCT_Rnodes_join( fid ) != 0 )
				{
					System.out.println( "Failed to create Rnodes join table." );
					return -3;
				}
				
				/*
				 * Get <fid>_<rnid>_counts
				 */
				if ( BuildCT_Rnodes_counts( fid ) != 0 )
				{
					System.out.println( "Failed to get Rnodes counts." );
					return -4;
				}
				
				/*
				 * If link analysis is off, then the <fid>_<rnid>_counts become the <fid>_<rnid>_CT tables
				 */
				if ( !linkAnalysis )
				{
					if ( noLinkRchainCT( fid ) != 0 )
					{
						System.out.println( "Failed to get Rchain CTs." );
						return -5;
					}
				}
				else
				{
					/*
					 * Create <fid>_<rnid>_flat table to count true values
					 */
					if ( BuildCT_Rnodes_flat( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes flat tables." );
						return -6;
					}
					
					/*
					 * Create <fid>_<rnid>_star table to count number of joined values
					 */
					if ( BuildCT_Rnodes_star( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes star tables." );
						return -7;
					}
					
					/*
					 * Create <fid>_<rnid>_CT table
					 */
					if ( BuildCT_Rnodes_CT( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes CTs." );
						return -8;
					}
					
					/*
					 * Create <fid>_<rchain>_CT tables for each node in lattice.
					 */
					System.out.println( "SELECT length FROM `" + fid +"_lattice_set` ORDER BY `" + fid + "_lattice_set`.length DESC;" );
					rs = st.executeQuery( "SELECT length FROM `" + fid +  "_lattice_set` ORDER BY `" + fid +"_lattice_set`.length DESC;" );
					
					if ( rs.first() )
					{
						int maxLen = Integer.parseInt( rs.getString( 1 ) );
						
						for ( int j = 2; j <= maxLen; j++ )
						{
							if ( BuildCT_RChain_flat( fid, j ) != 0 )
							{
								System.out.println( "Failed to create rchain flat table." );
								return -9;
							}
						}
					}
				}
				
				//System.out.println("Fid is "+ fid);
				/*
				 * Create <fid>_<rchain>_local_CT table
				 */
				String BiggestRchain = "";

				rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + "_lattice_set` WHERE `" + fid + "_lattice_set`.length = "
						+ "( SELECT MAX(length) FROM `" + fid + "_lattice_set` );" );
				
				boolean RChainCreated = false;
				
				while ( rs.next() )
				{
					RChainCreated = true;
					BiggestRchain = rs.getString( 1 );
				}
				
				rs.close();
				System.out.println("BiggestRchain is "+ BiggestRchain+ "\t rchain is : "+ rchain);
				
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_local_CT`;" );
				st.execute( "DROP TABLE IF EXISTS "+database_db+".`" + fid + "_CT`;" );
				//st.execute( "DROP TABLE IF EXISTS unielwin_Training1_target_final_CT.`" + fid + "_CT`;" );
				
				if ( RChainCreated )
				{
					st.execute( "DELETE FROM `" + fid + "_" + BiggestRchain.replace( "`", "" ) +"_CT` WHERE MULT = '0';" );
					
					// local_ct has not INDEX !!! Oct.14, 2014,zqian
					if ( linkAnalysis )
					{
						System.out.println( "CREATE TABLE "+database_db+".`local_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );

						//st.execute( "CREATE TABLE "+database_db+".`" + fid + "_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );
						st.execute( "CREATE TABLE "+database_db+".`local_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );
						//st.execute( "CREATE TABLE unielwin_Training1_target_final_CT.`" + fid + "_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );

					}
					else
					{ // May 26 zqian, for rnid using linkon_CT
						//st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" + BiggestRchain.replace( "`", "") + "_counts`;" );
						//if fid in rnids
						Statement st_temp = con.createStatement();
						ResultSet rs_temp = st_temp.executeQuery(" select rnid from "+ dbBNschema+".RNodes where rnid = '`"+fid+"`' ; " );
						String temp = "";
						while (rs_temp.next()) { 
								temp = rs_temp.getString(1).replace("`", "");
								System.out.println("temp is "+ temp);
						}
						if (temp.compareTo(fid)==0)
						{
						// select sum(mult) as mult, fid from ct_linkon_a,b_ct group by fid;
							st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT sum(mult) as mult, `"+fid+"`  FROM "+dbDataSchema+"_CT_linkon.`" + rchain + "_CT`  group by "+fid+";" );
							System.out.println( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT sum(mult) as mult, `"+fid+"`  FROM "+dbDataSchema+"_CT_linkon.`" + rchain + "_CT`  group by "+fid+";" );
						// update the mult to be local_mult
							Statement st1 = con.createStatement();
							ResultSet rs1 = st1.executeQuery("select Tuples from  "+dbBNschema+"_local_BBH3.Pvars_Not_In_Family where child = '`"+ fid +"`' ;");
							Statement st2 = con.createStatement();
							long local = 1;
						 		while(rs1.next()){
									local = Long.parseLong (rs1.getString("Tuples"));
									System.out.println("local is "+ local);
									String sql = "update `" + fid + "_" + rchain + "_local_CT` set mult = mult/ "+local + " ;";
									System.out.println(sql);
									st2.execute(sql);			
								}
						 		if (!rs1.first()) {
						 	 		System.out.println("local is 1, ******" );
						 			String sql = "update `" + fid + "_" + rchain + "_local_CT` set mult = mult/ "+local + " ;";
						 			st2.execute(sql);	
						 	 	}
													
							
						}
						else
							st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" + BiggestRchain.replace( "`", "") + "_counts`;" );


					}
					
					/*
					 * Drop columns which aren't parents
					 */
					if ( dropExtraColumns( fid ) != 0 )
					{
						System.out.println( "Failed to drop extra columns." );
						return -10;
					}
				}
				else
				{
					/*
					 * No RNodes are parents of this FID. Must be a 1Node.
					 * Get the associated pvid
					 */
					rs = st.executeQuery( "SELECT pvid FROM `" + fid +"_1Nodes` WHERE `1nid` = '`" + fid + "`';" );
					
					rs.first();
					String pvid = rs.getString( 1 );
					rs.close();
					System.out.println( "CREATE TABLE "+database_db+".`local_CT` AS SELECT * FROM `" + fid + "_" +pvid + "_counts`;" );

					//st.execute( "CREATE TABLE "+database_db+".`" + fid + "_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );
					st.execute( "CREATE TABLE "+database_db+".`local_CT` AS SELECT * FROM `" + fid + "_" +pvid + "_counts`;" );
				
				//	st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" +pvid + "_counts`;" );
				}
			
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to generate subcounts." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Create a new lattice using the fid and its parents
	 */
	private static int createLocalLattice( String fid )
	{
		try
		{
			/*
			 * Get the RNodes to form the lattice
			 */
			Statement st = con.createStatement();
			
			ArrayList<String> localRnodes = new ArrayList<String>();
			
			ResultSet rs = st.executeQuery( "SELECT rnid FROM `" + fid + "_RNodes;" );
			
			while ( rs.next() )
			{
				String rnid = rs.getString( 1 ).replace("`", "");
				localRnodes.add( rnid );
			}
			
			rs.close();
			
			st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_membership`;" );
			st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_rel`;" );
			st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_set`;" );
			
			st.execute( "CREATE TABLE `" + fid + "_lattice_membership` ( name " + 
						"VARCHAR(20), member VARCHAR(20), PRIMARY KEY (name, member));" );
			st.execute( "CREATE TABLE `" + fid + "_lattice_rel` (parent " + 
						"VARCHAR(20), child VARCHAR(20), removed VARCHAR(20), " + 
						"PRIMARY KEY (parent, child));" );
			st.execute( "CREATE TABLE `" + fid + "_lattice_set` ( name " + 
						"VARCHAR(20), length INT(11), PRIMARY KEY (name, length))" );
			
			/*
			 * Insert RNodes into lattice
			 */
			int len = localRnodes.size();
			
			for ( int i = 0; i < len; i++ )
			{
				String rnid = localRnodes.get( i );
				st.execute( "INSERT INTO `" + fid + "_lattice_set` ( name, length ) VALUES " + 
							"( '`" + rnid + "`', 1 );" );
	            st.execute( "INSERT INTO `" + fid + "_lattice_rel` ( parent, " + 
	            			"child, removed ) VALUES ( 'EmptySet', '`" + rnid + 
	            			"`', '`" + rnid + "`' );" );
	            st.execute( "INSERT INTO `" + fid + "_lattice_membership` ( " + 
	            			"name, member ) VALUES ( '`" + rnid + "`', '`" + 
	            			rnid + "`' );" );
	            
	            ArrayList<String> starterSet = new ArrayList<String>();
	            starterSet.add( rnid );
	            
	            /*
	             * Join RNodes to create lattice in recursive fashion
	             */
	            if ( createNewSets( fid, localRnodes, starterSet, i, 1 ) != 0 )
	            {
	            	System.out.println( "Failed to create new sets." );
	            	return -2;
	            }
			}
			
			/*
			 * Get all nodes from lattice, get original names for lattice mapping
			 */
			st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_mapping;" );
			st.execute( "CREATE TABLE IF NOT EXISTS `" + fid + "_lattice_mapping` " + 
						"( orig_rnid VARCHAR(200), rnid VARCHAR(20), PRIMARY " + 
						"KEY( orig_rnid, rnid ) );" );
			
			ArrayList<String> names = new ArrayList<String>();
			
			rs = st.executeQuery( "SELECT name FROM `" + fid + "_lattice_set`;" );
			
			while ( rs.next() )
			{
				String name = rs.getString( 1 ).replace("`", "");
				names.add( name );
			}
			
			rs.close();
			
			rs = st.executeQuery( "SELECT orig_rnid, rnid FROM `" + fid + "_RNodes`;" );
			
			HashMap<String, String> rnidMappings = new HashMap<String, String>();
			
			while ( rs.next() )
			{
				String value = rs.getString(1).replace( "`", "" );
				String key = rs.getString( 2 ).replace( "`", "" );
				rnidMappings.put( key, value);
			}
			
			rs.close();
			
			int len2 = names.size();
			
			for ( int i = 0; i < len2; i++ )
			{
				String allNames = names.get( i );
				String[] nameSet = allNames.split( "," );
				
				int len3 = nameSet.length;
				
				String newMapping = "";
				
				for ( int j = 0; j < len3; j++ )
				{
					if ( j < len3 - 1 )
					{
						newMapping += rnidMappings.get( nameSet[j] ) + ",";
						continue;
					}
					
					newMapping += rnidMappings.get( nameSet[j] );
				}
				
				st.execute( "INSERT INTO `" + fid + "_lattice_mapping` VALUES ( '`" + 
							newMapping + "`', '`" + allNames + "`' );" );
			}
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to create local lattice." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Join RNodes to form lattice in recursive fashion
	 * Ordering of RNodes is preserved
	 * Suppose a, b, c, d are RNodes
	 * First iterate over a. a,b a,c and a,d are added and used recursively
	 * Then iterating over b, b,c and b,d are added and used recursively
	 * Finally, iterating over c, c,d is added and used recursively
	 * 
	 * On the a,b recursive call, a,b,c and a,b,d are added and used recursively
	 * On the a,c recursive call, a,c,d is added and used recursively
	 */
	private static int createNewSets( String fid, ArrayList<String> rnids, ArrayList<String> partialSet, int index, int length )
	{
		try
		{
			/*
			 * Add each RNode to newSet and call recursively
			 * SsecondSet contains the recursive parent set
			 * e.g. secondSet is a,b and newSet will be a,b,c
			 */
			int len = rnids.size();
			
			for ( int i = index + 1; i < len; i++ )
			{
				Statement st = con.createStatement();
				
				String newSet = "";
				String secondSet = "";
				
				int len2 = partialSet.size();
				
				for ( int j = 0; j < len2; j++ )
				{
					newSet += partialSet.get( j ) + ",";
					
					if ( j != len2 - 1 )
					{
						secondSet += partialSet.get( j ) + ","; //zqian July 4, 2014
						continue;
					}
					
					secondSet += partialSet.get( j );
				}
				System.out.println("SecondSet:"+secondSet );
				newSet += rnids.get( i );
				
				st.execute( "INSERT IGNORE INTO `" + fid + "_lattice_set` ( " + 
							"name, length ) VALUES ('`" + newSet + "`'," + 
							( length + 1 ) + " );" );
				
				System.out.println( "INSERT IGNORE INTO `" + fid + "_lattice_rel` ( " + 
						"parent, child, removed ) VALUES ( '`" + secondSet + "`', '`" + newSet + "`', '`" + rnids.get( i ) + "`' );");
				st.execute( "INSERT IGNORE INTO `" + fid + "_lattice_rel` ( " + 
							"parent, child, removed ) VALUES ( '`" + secondSet + "`', '`" + newSet + "`', '`" + rnids.get( i ) + "`' );");
				
				/*
				 * Add all subsets of newSet into <fid>_lattice_set
				 */
				for ( int j = 0; j < len2; j++ )
				{
					String otherSet = "";
					
					for ( int k = 0; k < len2; k++ )
					{
						if ( k == j )
						{
							continue;
						}
						
						otherSet += partialSet.get( k ) + ","; 
					}
					
					otherSet += rnids.get( i );
					
					st.execute( "INSERT IGNORE INTO `" + fid + "_lattice_rel` ( " + 
							    "parent, child, removed ) VALUES ( '`" + otherSet + "`', '`" + newSet + "`', '`" + partialSet.get( j ) + "`' );" );
				}

				/*
				 * Add all members of new set
				 */
                st.execute( "INSERT IGNORE INTO `" + fid + "_lattice_membership` " + 
                			"( name, member ) VALUES ( '`" + newSet + "`', '`" + rnids.get( i ) + "`' );" );

                for ( String secondSetMembers : partialSet )
                {
                    st.execute( "INSERT IGNORE INTO `" + fid + 
                    			"_lattice_membership` ( name, member ) VALUES " + 
                    			"( '`" + newSet + "`', '`" + secondSetMembers + "`');");
                }
                
                /*
                 * Recursive call on new set
                 */
                ArrayList<String> newPartialSet = new ArrayList<String>();
                newPartialSet.addAll( partialSet );
                newPartialSet.add( rnids.get( i ) );
                
                if ( createNewSets( fid, rnids, newPartialSet, i, length + 1 ) != 0 )
                {
                	System.out.println( "Failed to create new set." );
                	return -2;
                }
                
				st.close();
			}
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to add new sets." );
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	/*
	 * Clear the temporary FID specific tables
	 */
	private static int clearTemporaryTables()
	{
		try
		{
			Statement st = con.createStatement();
			
			ArrayList<String> fids = new ArrayList<String>();
						
			ResultSet rs = st.executeQuery( "SELECT DISTINCT child FROM " + 
											dbBNschema + "." + pathBayesNet +
											" WHERE rchain = '`" + rchain + "`';" );
			
			while ( rs.next() )
			{
				String fid = rs.getString( 1 ).replace( "`", "" );
				fids.add( fid );
			}
			
			rs.close();
			
			int len = fids.size();
			st.execute("use "+ dbOutputSchema + ";");
			for ( int i = 0; i < len; i++ )
			{
				String fid = fids.get( i );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_1Nodes`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_2Nodes`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ForeignKeyColumns`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_membership`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_rel`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_set`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_lattice_mapping`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_FNodes`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_1Nodes`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Select_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_pvars`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_From_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_Where_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_Select_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_From_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_PVariables_GroupBy_List`" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_Rnodes_join_columnname_list`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RNodes_GroupBy_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_Select_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_FROM_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_1Nodes_GroupBY_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_Select_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RNodes_Star_From_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_RChain_pvars`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Select_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_From_List`;" );
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_ADT_RChain_Star_Where_List`;" );
			}
			
			/*
			 * These tables are shared by all FIDs
			 */
		//	st.execute( "DROP TABLE IF EXISTS PVariables;" );
		//	st.execute( "DROP TABLE IF EXISTS FidWithParents;" );
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to clear subtables." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Build the PVariable counts for each FID
	 */
	private static int BuildCT_Pvars( String fid )
	{
		try
		{  // connect to database_db
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT * FROM PVariables;" ); // PVariables.Tuples is NOT Correct, should be no. of tuples of testing database. Aug. 19, zqian
			
			while ( rs.next() )
			{
				String pvid = rs.getString( "pvid");
				
				Statement st2 = con.createStatement();
				
				/*
				 * Form the "SELECT" part of the query
				 */
				ResultSet rs2 = st2.executeQuery( "SELECT Entries FROM `" + fid +"_ADT_PVariables_Select_List` " + 
												  "WHERE pvid = '" + pvid + "';" );
				
				String select = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				/*
				 * Form the "FROM" part of the query
				 */
				rs2 = st2.executeQuery( "SELECT Entries FROM `" + fid +"_ADT_PVariables_From_List` WHERE " + 
										"pvid = '" + pvid + "';" );
				
				String from = makeCommaSeparatedQuery( rs2, "Entries", ", " );

				rs2.close();
				
				/*
				 * Form the "GROUP BY" part of the query
				 */
				rs2 = st2.executeQuery( "SELECT Entries FROM `" + fid +	"_ADT_PVariables_GroupBy_List` WHERE " + 
										"pvid = '" + pvid + "';" );
				String groupBy = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( ( null == select ) || ( null == from ) || ( null == groupBy ) )
				{
					System.out.println( "Failure assembling query strings." );
					return -2;
				}
				
				String query = "SELECT " + select + " FROM " + from;
				
				if ( !groupBy.isEmpty() )
				{
					query += " GROUP BY " + groupBy;
				}
				
				/*
				 * Create <fid>_<pvid>_counts table
				 */
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + pvid + "_counts`;" );
				st2.execute( "CREATE TABLE `" + fid + "_" + pvid + "_counts` AS " +
							query + ";" );
				
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns "
						+ "WHERE table_schema = '" + dbOutputSchema + "' AND table_name = '" + fid + "_" + pvid + "_counts';");
				
				/*
				 * Add index for faster computation
				 */
				String index = makeIndexQuery( rs2, "Entries", ", " );
				
				if ( null == index )
				{
					System.out.println( "Failed to create index." );
					return -3;
				}
				
				rs2.close();
				
				st2.execute( "ALTER TABLE `" + fid + "_" + pvid + "_counts` ADD INDEX ( " + index + " );" );
				
				st2.close();
			}
			
			rs.close();
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to build counts for pvars." );
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	/*
	 * Create <fid>_<rnode>_join table to get False values
	 */
	private static int BuildCT_Rnodes_join( String fid )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT rnid FROM `" + fid + "_RNodes`;" );
			
			while ( rs.next() )
			{
				String rnid = rs.getString( 1 ).replace("`","");
				
				Statement st2 = con.createStatement();
				
				ResultSet rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM " + 
												  "`" + fid + "_Rnodes_join_columnname_list` " + 
												  "WHERE rnid = '`" + rnid + "`';");
				
				String ColumnString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == ColumnString )
				{
					System.out.println( "Failed to assemble query." );
					return -2;
				}
				
				String finalString = "`" + rnid + "` VARCHAR( 5 )";
				
				if ( !ColumnString.isEmpty() )
				{
					finalString += ", " + ColumnString;
				}
				
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + rnid + "_join`;" );
				st2.execute( "CREATE TABLE `" + fid + "_" + rnid + "_join` " + "(" + finalString + ");" );
				st2.execute( "INSERT INTO `" + fid + "_" + rnid + "_join`(`" + rnid + "`) VALUES ( 'F' );" );				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to build rnode join table for fid = " + fid );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Generate the <fid>_<rchain>_local_CT tables
	 */
	private static int getSubCounts()
	{
		try
		{
			Statement st = con.createStatement();
			
			ArrayList<String> fids = new ArrayList<String>();
			
			/*
			 * Iterate over each FID in the pathBayesNet
			 */
			ResultSet rs = st.executeQuery( "SELECT DISTINCT child FROM " +	dbBNschema + "." + pathBayesNet +" WHERE rchain = '`" + rchain + "`';" );
			
			while ( rs.next() )
			{
				String fid = rs.getString( 1 ).replace( "`", "" );
				fids.add( fid );
			}
			
			rs.close();
			
			int len = fids.size();
			
			for ( int i = 0; i < len; i++ )
			{
				String fid = fids.get( i );
				
				/*
				 * Get <fid>_<pvid>_counts
				 */
				if ( BuildCT_Pvars( fid ) != 0 )
				{
					System.out.println( "Failed to get pvariable counts." );
					return -2;
				}
				
				/*
				 * Get false values for <fid>_<rnid>_join
				 */
				if ( BuildCT_Rnodes_join( fid ) != 0 )
				{
					System.out.println( "Failed to create Rnodes join table." );
					return -3;
				}
				
				/*
				 * Get <fid>_<rnid>_counts
				 */
				if ( BuildCT_Rnodes_counts( fid ) != 0 )
				{
					System.out.println( "Failed to get Rnodes counts." );
					return -4;
				}
				
				/*
				 * If link analysis is off, then the <fid>_<rnid>_counts become the <fid>_<rnid>_CT tables
				 */
				if ( !linkAnalysis )
				{
					if ( noLinkRchainCT( fid ) != 0 )
					{
						System.out.println( "Failed to get Rchain CTs." );
						return -5;
					}
				}
				else
				{
					/*
					 * Create <fid>_<rnid>_flat table to count true values
					 */
					if ( BuildCT_Rnodes_flat( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes flat tables." );
						return -6;
					}
					
					/*
					 * Create <fid>_<rnid>_star table to count number of joined values
					 */
					if ( BuildCT_Rnodes_star( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes star tables." );
						return -7;
					}
					
					/*
					 * Create <fid>_<rnid>_CT table
					 */
					if ( BuildCT_Rnodes_CT( fid, 1 ) != 0 )
					{
						System.out.println( "Failed to create Rnodes CTs." );
						return -8;
					}
					
					/*
					 * Create <fid>_<rchain>_CT tables for each node in lattice.
					 */
					System.out.println( "SELECT length FROM `" + fid + 
										  "_lattice_set` ORDER BY `" + fid + 
										  "_lattice_set`.length DESC;" );
					rs = st.executeQuery( "SELECT length FROM `" + fid + 
										  "_lattice_set` ORDER BY `" + fid + 
										  "_lattice_set`.length DESC;" );
					
					if ( rs.first() )
					{
						int maxLen = Integer.parseInt( rs.getString( 1 ) );
						
						for ( int j = 2; j <= maxLen; j++ )
						{
							if ( BuildCT_RChain_flat( fid, j ) != 0 )
							{
								System.out.println( "Failed to create rchain flat table." );
								return -9;
							}
						}
					}
				}
				
				System.out.println("Fid is "+ fid);
				/*
				 * Create <fid>_<rchain>_local_CT table
				 */
				String BiggestRchain = "";

				rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + "_lattice_set` WHERE `" + fid + "_lattice_set`.length = "
						+ "( SELECT MAX(length) FROM `" + fid + "_lattice_set` );" );
				
				boolean RChainCreated = false;
				
				while ( rs.next() )
				{
					RChainCreated = true;
					BiggestRchain = rs.getString( 1 );
				}
				
				rs.close();
				
				st.execute( "DROP TABLE IF EXISTS `" + fid + "_" + rchain + "_local_CT`;" );
				
				if ( RChainCreated )
				{
					st.execute( "DELETE FROM `" + fid + "_" + BiggestRchain.replace( "`", "" ) +"_CT` WHERE MULT = '0';" );
					
					if ( linkAnalysis )
					{
						st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" +BiggestRchain.replace( "`", "") + "_CT`;" );
					}
					else
					{ // May 26 zqian, for rnid using linkon_CT
						//st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" + BiggestRchain.replace( "`", "") + "_counts`;" );
						//if fid in rnids
						Statement st_temp = con.createStatement();
						ResultSet rs_temp = st_temp.executeQuery(" select rnid from "+ dbBNschema+".RNodes where rnid = '`"+fid+"`' ; " );
						String temp = "";
						while (rs_temp.next()) { 
								temp = rs_temp.getString(1).replace("`", "");
								System.out.println("temp is "+ temp);
						}
						if (temp.compareTo(fid)==0)
						{
						// select sum(mult) as mult, fid from ct_linkon_a,b_ct group by fid;
							st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT sum(mult) as mult, `"+fid+"`  FROM "+dbDataSchema+"_CT_linkon.`" + rchain + "_CT`  group by "+fid+";" );
							System.out.println( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT sum(mult) as mult, `"+fid+"`  FROM "+dbDataSchema+"_CT_linkon.`" + rchain + "_CT`  group by "+fid+";" );
						// update the mult to be local_mult
							Statement st1 = con.createStatement();
							ResultSet rs1 = st1.executeQuery("select Tuples from  "+dbBNschema+"_local_BBH3.Pvars_Not_In_Family where child = '`"+ fid +"`' ;");
							Statement st2 = con.createStatement();
							long local = 1;
						 		while(rs1.next()){
									local = Long.parseLong (rs1.getString("Tuples"));
									System.out.println("local is "+ local);
									String sql = "update `" + fid + "_" + rchain + "_local_CT` set mult = mult/ "+local + " ;";
									System.out.println(sql);
									st2.execute(sql);			
								}
						 		if (!rs1.first()) {
						 	 		System.out.println("local is 1, ******" );
						 			String sql = "update `" + fid + "_" + rchain + "_local_CT` set mult = mult/ "+local + " ;";
						 			st2.execute(sql);	
						 	 	}
													
							
						}
						else
							st.execute( "CREATE TABLE `" + fid + "_" + rchain + "_local_CT` AS SELECT * FROM `" + fid + "_" + BiggestRchain.replace( "`", "") + "_counts`;" );


					}
					
					/*
					 * Drop columns which aren't parents
					 */
					if ( dropExtraColumns( fid ) != 0 )
					{
						System.out.println( "Failed to drop extra columns." );
						return -10;
					}
				}
				else
				{
					/*
					 * No RNodes are parents of this FID. Must be a 1Node.
					 * Get the associated pvid
					 */
					rs = st.executeQuery( "SELECT pvid FROM `" + fid + 
										  "_1Nodes` WHERE `1nid` = '`" + fid + 
										  "`';" );
					
					rs.first();
					String pvid = rs.getString( 1 );
					rs.close();
					
					st.execute( "CREATE TABLE `" + fid + "_" + rchain + 
								"_local_CT` AS SELECT * FROM `" + fid + "_" + 
								pvid + "_counts`;" );
				}
			}
			
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to generate subcounts." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Get <fid>_<rnid>_counts tables
	 */
	private static int BuildCT_Rnodes_counts( String fid )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" +fid + "_lattice_set` ORDER BY length ASC;" );
			
			while ( rs.next() )
			{
				/*
				 * Form "SELECT" portion of query
				 */
				String Rchain = rs.getString( 1 );
				
				Statement st2 = con.createStatement();
				
				ResultSet rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" +fid + "_lattice_membership`," + 
												  " `" + fid + "_RNodes_Select_List` " + "WHERE NAME = '" + Rchain + 
												  "' AND `" + fid + "_lattice_membership`." +
												  "member = `" + fid + "_RNodes_Select_List`.rnid;" );
				
				String selectString = makeCommaSeparatedQuery( rs2, "Entries", ", ");
				rs2.close();
				
				if ( null == selectString )
				{
					System.out.println( "Failed to assemble select query." );
					return -2;
				}
				
				/*
				 * Form "FROM" portion of query
				 */
				rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid +"_lattice_membership`, `" + 
										fid +"_RNodes_From_List` WHERE NAME = '" +Rchain + "' AND `" + 
										fid +"_lattice_membership`.member = `" + fid +"_RNodes_From_List`.rnid;" );
				
				String fromString = makeCommaSeparatedQuery(rs2, "Entries", ", ");
				rs2.close();
				
				if ( null == fromString )
				{
					System.out.println( "Failed to assemble from query." );
					return -3;
				}
				
				/*
				 * Form "WHERE" clause
				 */
				rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid +"_lattice_membership`, `" + 
										fid +"_RNodes_Where_List` WHERE NAME = '" +	Rchain + "' AND `" + 
										fid +"_lattice_membership`.member = `" + fid +"_RNodes_Where_List`.rnid;" );
				
				String whereString = makeCommaSeparatedQuery( rs2, "Entries", " and " );
				rs2.close();
				
				if ( null == whereString )
				{
					System.out.println( "Failed to assemble where query." );
					return -4;
				}
				
				String queryString = "SELECT " + selectString + " FROM " + fromString + " WHERE " + whereString;
				
				/*
				 * Add "GROUP BY" clause if not continuous data
				 */
				if ( !continuous )
				{
					rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" + 
											fid + "_RNodes_GroupBy_List` WHERE " +"NAME = '" + Rchain + "' AND `" + 
											fid +"_lattice_membership`.member = `" + 
											fid + "_RNodes_GroupBy_List`.rnid;" );
					
					String GroupByString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
					rs2.close();
					
					if ( null == GroupByString )
					{
						System.out.println( "Failed to assemble group by query." );
						return -5;
					}
					
					if ( !GroupByString.isEmpty() )
					{
						queryString += " GROUP BY " + GroupByString;
					}
				}
				
				/*
				 * Create <fid>_<rchain>_counts table
				 */
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + Rchain.replace( "`", "" ) + "_counts`;" );
				
				st2.execute( "CREATE TABLE `" + fid + "_" + Rchain.replace( "`", "" ) + "_counts` AS " + queryString );
				
				/*
				 * Add index for faster computation
				 */
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns WHERE " + 
										"table_schema = '" + dbOutputSchema + "' AND table_name = '" + fid + "_" +Rchain.replace( "`", "" ) + "_counts';" );
				
				String IndexString = makeIndexQuery( rs2, "Entries", ", " );
				
				rs2.close();
				
				st2.execute( "ALTER TABLE `" + fid + "_" + Rchain.replace("`", "") + "_counts` ADD INDEX ( " + IndexString + " );" );
				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to generate rnode counts." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Helper function to create a comma separated query
	 */
	private static String makeCommaSeparatedQuery( ResultSet rs, String colName, String del )
	{
		ArrayList<String> queryParts = new ArrayList<String>();
		
		try
		{
			while ( rs.next() )
			{
				String part = rs.getString( colName );
				queryParts.add( part );
			}
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to assemble query parts." );
			e.printStackTrace();
			return null;
		}
		
		String query = StringUtils.join( queryParts, del );
		
		return query;
	}
	
	/*
	 * Helper function for multiplying MULTs
	 */
	private static String makeStarSeparatedQuery( ResultSet rs, 
												  String colName, 
												  String del)
	{
		ArrayList<String> parts = new ArrayList<String>();

		try
		{
			while ( rs.next() )
			{
				String temp = "`" + rs.getString(colName).replace( "`", "" ) + 
							  "`.`MULT` ";	
				parts.add( temp );
			}
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to assemble query parts." );
			e.printStackTrace();
			return null;
		}
		
		String query = StringUtils.join( parts, del );

		return query;
	}
	
	/*
	 * Helper function for combining columns in union
	 */
	private static String makeUnionSeparatedQuery( ResultSet rs, 
												   String colName, 
												   String del )
	{
		ArrayList<String> parts = new ArrayList<String>();
		
		try
		{
			while ( rs.next() )
			{
				parts.add( "`" + rs.getString( colName ) + "`" );
			}
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to assemble union parts." );
			e.printStackTrace();
			return null;
		}
		
		return StringUtils.join( parts, del );
	}
	
	/*
	 * Helper function for assembling values for index
	 */
	private static String makeIndexQuery( ResultSet rs, String colName, String del )
	{	
		ArrayList<String> parts = new ArrayList<String>();
		
		int count = 0;
		try
		{
			while( ( rs.next() ) && ( count<16 ) )
			{
				String temp = "`" + rs.getString( colName ) + "` ASC";
				parts.add( temp );
				count++;
			}
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create index." );
			e.printStackTrace();
			return null;
		}

		String index = StringUtils.join( parts, del );
		return index;
	}
	
	/*
	 * If Link Analysis is off, then <fid>_<rnid>_counts is the same as the
	 * <fid>_<rnid>_CT table
	 */
	private static int noLinkRchainCT( String fid )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + 
											"_lattice_set` ORDER BY length ASC;" );
			
			while ( rs.next() )
			{
				String Rchain = rs.getString( 1 );
				
				Statement st2 = con.createStatement();
				
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + 
							 Rchain.replace( "`", "" ) + "_CT`;" );
				
				st2.execute( "CREATE TABLE `" + fid + "_" + 
							 Rchain.replace( "`", "" ) + "_CT` AS SELECT * " + 
							 "FROM `" + fid + "_" + Rchain.replace( "`", "" ) + 
							 "_counts`;" );
				
				ResultSet rs2 = st2.executeQuery( "SELECT column_name AS Entries " + 
												  "FROM information_schema.columns " + 
												  "WHERE table_schema = '" + 
												  dbOutputSchema + "' AND " + 
												  "table_name = '"+ fid + "_" + 
												  Rchain.replace( "`", "" ) + "_CT';" );
				
				String IndexString_CT = makeIndexQuery( rs2, "Entries", ", ");
				rs2.close();
				
				st2.execute( "ALTER TABLE `"+ fid + "_" + Rchain.replace( "`", "" ) + 
							 "_CT` ADD INDEX ( " + IndexString_CT+ " );" );
				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL failure." );
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	/*
	 * Build <fid>_<rnid>_flat table
	 */
	private static int BuildCT_Rnodes_flat( String fid, int len )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" +fid + "_lattice_set` WHERE `" + 
										fid +"_lattice_set`.length = " + len + ";" );
			
			while ( rs.next() )
			{
				String RChain = rs.getString( 1 );
				
				Statement st2 = con.createStatement();
				
				/*
				 * Form "SELECT" portion of query
				 */
				ResultSet rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" + 
													fid +"_ADT_RNodes_1Nodes_Select_List` " + "WHERE NAME = '" + RChain +  "' AND `" + 
													fid + "_lattice_membership`.member " + "= `" + fid + "_ADT_RNodes_1Nodes_Select_List`.rnid;" );

				String selectString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == selectString )
				{
					System.out.println( "Failed to assemble select query." );
					return -2;
				}
				
				/*
				 * Form "FROM" portion of query
				 */
				rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" +
										fid +"_ADT_RNodes_1Nodes_FROM_List` WHERE NAME = '" + RChain + "' AND `" + 
										fid +"_lattice_membership`.member = `" + 
										fid +"_ADT_RNodes_1Nodes_FROM_List`.rnid;" );
				
				String fromString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == fromString )
				{
					System.out.println( "Failed to assemble from query." );
					return -3;
				}
				
				String queryString = "SELECT " + selectString + " FROM " + fromString;
				
				/*
				 * Add "GROUP BY" clause if not continuous data
				 */
				if ( !continuous )
				{
					rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" +
											fid + "_ADT_RNodes_1Nodes_GroupBY_List` WHERE NAME = '" + RChain + "' AND `" + 
										    fid + "_lattice_membership`.member = `" + 
										    fid + "_ADT_RNodes_1Nodes_GroupBY_List`.rnid;" );
					
					String GroupByString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
					rs2.close();
					
					if ( null == GroupByString )
					{
						System.out.println( "Failed to assemble group by query." );
						return -4;
					}
					
					if ( !GroupByString.isEmpty() )
					{
						queryString += " GROUP BY " + GroupByString;
					}
				}
				
				/*
				 * Create <fid>_<rchain>_flat table
				 */
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + RChain.replace( "`", "" ) + "_flat`;" );
				
				st2.execute( "CREATE TABLE `" + fid + "_" + RChain.replace( "`", "" ) + 
							"_flat` AS " + queryString + ";" );
				
				/*
				 * Add index for faster computation
				 */
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns WHERE " + 
										"table_schema = '" + dbOutputSchema +"' and table_name = '" +
											fid + "_" +	RChain.replace( "`", "" ) + "_flat';" );
				
				String IndexString = makeIndexQuery( rs2, "Entries", ", " );
				
				rs2.close();
				
				if ( null == IndexString )
				{
					System.out.println( "Failed to generate index statement." );
					return -5;
				}
				
				st2.execute( "ALTER TABLE `" + fid + "_" + RChain.replace( "`", "" ) + "_flat` ADD INDEX ( " + IndexString + " );" );
				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL failure." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Build <fid>_<rchian>_star table
	 */
	private static int BuildCT_Rnodes_star( String fid, int len )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + "_lattice_set` WHERE `" + 
											fid +"_lattice_set`.length = " + len + ";" );
			
			while ( rs.next() )
			{
				/*
				 * Form "SELECT" clause
				 */
				String RChain = rs.getString( 1 );
				
				Statement st2 = con.createStatement();
				
				ResultSet rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" + 
												  fid + "_ADT_RNodes_Star_Select_List` WHERE NAME = '" + RChain + "' AND `" +
												  fid +"_lattice_membership`.member = `" + 
												  fid + "_ADT_RNodes_Star_Select_List`.rnid;" );
				String selectString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == selectString )
				{
					System.out.println( "Failed to assemble select query." );
					return -2;
				}
				
				/*
				 * Form "MULT" clause
				 */
				rs2 = st2.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + "_lattice_membership`, `" + 
										fid +"_ADT_RNodes_Star_From_List` WHERE NAME = '" + RChain + "' AND `" + 
										fid +"_lattice_membership`.member = `" + 
										fid +"_ADT_RNodes_Star_From_List`.rnid;" );
				String MultString = makeStarSeparatedQuery( rs2, "Entries", " * " );
				
				if ( null == MultString )
				{
					System.out.println( "Failed to assemble mult computation." );
					return -3;
				}
				
				/*
				 * Form "FROM" clause using the same query results as MULT
				 */
				rs2.beforeFirst();
				
				String fromString = makeCommaSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == fromString )
				{
					System.out.println( "Failed to assemble from query." );
					return -4;
				}
				
				/*
				 * Create <fid>_<rchain>_star table
				 */
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + RChain.replace( "`", "" ) +"_star`;" );
				
				if ( selectString.isEmpty() )
				{
					st2.execute( "CREATE TABLE `" + fid + "_" +  RChain.replace( "`", "" ) +"_star` AS SELECT " + MultString + " AS `MULT` FROM " + fromString + ";" );
				}
				else
				{
					st2.execute( "CREATE TABLE `" + fid + "_" + RChain.replace( "`", "" ) +"_star` AS SELECT " + MultString + " AS `MULT`, " + selectString + " FROM " + fromString + ";" );
				}
				
				/*
				 * Add index for increased computation speed
				 */
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns WHERE " + 
										"table_schema = '" + dbOutputSchema + "' AND table_name = '" + 
										fid + "_" +	RChain.replace( "`", "" ) +"_star';" );
				
				String IndexString = makeIndexQuery( rs2, "Entries", ", " );
				rs2.close();
				
				st2.execute( "ALTER TABLE `" + fid + "_" + RChain.replace( "`", "" ) + "_star` ADD INDEX ( " + IndexString + " );" );
				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL failure." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	/*
	 * Get <fid>_<rchain>_CT table
	 */
	private static int BuildCT_Rnodes_CT( String fid, int len )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + "_lattice_set` WHERE `" + 
											fid +"_lattice_set`.length = " + len + ";" );
			
			while ( rs.next() )
			{
				String RChain = rs.getString( "RChain" );
				
				/*
				 * Sort merge on <fid>_<rchain>_star and <fid>_<rchain>_flat to
				 * create <fid>_<rchain>_false table
				 */
				Sort_merge3.sort_merge( "`" + fid + "_" + RChain.replace( "`", "" ) + "_star`",
										"`" + fid + "_" + RChain.replace( "`", "" ) + "_flat`",
										"`" + fid + "_" + RChain.replace( "`", "" ) + "_false`", con );
				
				/*
				 * Add index for faster computation
				 */
				Statement st2 = con.createStatement();
				
				ResultSet rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns " + 
												  "WHERE table_schema = '" +  dbOutputSchema + "' AND table_name = '" + 
												  fid + "_" + RChain.replace( "`", "" ) + "_false';" );
				
				String IndexString = makeIndexQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == IndexString )
				{
					System.out.println( "Failed to generate index statement." );
					return -4;
				}
				
				st2.execute( "ALTER TABLE `" + fid + "_" + RChain.replace( "`", "" ) +"_false` ADD INDEX ( " + IndexString + " );" );
				
				/*
				 * Create CT Table
				 */
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns WHERE table_schema = '" + 
										dbOutputSchema + "' AND table_name = '" + fid + "_" + RChain.replace( "`", "" ) + "_counts';" );
				
				String UnionColumnString = makeUnionSeparatedQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == UnionColumnString )
				{
					System.out.println( "Failed to generate union statement." );
					return -5;
				}
				
				st2.execute( "DROP TABLE IF EXISTS `" + fid + "_" + RChain.replace( "`", "" ) + "_CT`;" );
				st2.execute( "CREATE TABLE `" + fid + "_" + RChain.replace( "`", "" ) + "_CT` AS SELECT " + UnionColumnString + " FROM `" + 
							 fid + "_" + RChain.replace( "`", "" ) + "_counts` " +  "UNION SELECT " + UnionColumnString + " FROM `" + 
							 fid + "_" + RChain.replace( "`", "" ) + "_false`, `" + fid + "_" + RChain.replace( "`", "" ) + "_join`;" );
				
				/*
				 * Add index for faster computation
				 */
				rs2 = st2.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns WHERE table_schema = '" + 
										dbOutputSchema + "' AND table_name = '" + fid + "_" + RChain.replace( "`", "" ) + "_CT';" );
				
				IndexString = makeIndexQuery( rs2, "Entries", ", " );
				rs2.close();
				
				if ( null == IndexString )
				{
					System.out.println( "Failed to assemble index." );
					return -6;
				}
				
				st2.execute( "ALTER TABLE `" + fid + "_" + RChain.replace( "`", "" ) + "_CT` ADD INDEX ( " + IndexString + " );" );
				
				st2.close();
			}
			
			rs.close();
			
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL Failure." );
			e.printStackTrace();
			return -1;
		}
		catch ( IOException ie )
		{
			System.out.println( "IO Exception!" );
			ie.printStackTrace();
			return -2;
		}
		
		return 0;
	}
	
	/*
	 * Create <fid>_<rchain>_flat table
	 */
	private static int BuildCT_RChain_flat( String fid, int len )
	{
		try
		{
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT name AS RChain FROM `" + fid + 
											"_lattice_set` WHERE `" + fid + 
											"_lattice_set`.length = " + len + ";" );
			
			int fc = 0;
			
			while ( rs.next() )
			{
				String RChain = rs.getString( 1 );
				
				/*
				 * Based off of columns in <fid>_<rchain>_counts table
				 */
				String 	cur_CT_Table= "`" + fid + "_" + RChain.replace( "`", "" ) + "_counts`";
				
				Statement st2 = con.createStatement();
				
				ResultSet rs2 = st2.executeQuery( "SELECT distinct parent, " + 
												  "removed AS rnid FROM `" + fid + 
												  "_lattice_rel` WHERE child = '" + 
												  RChain + "' ORDER BY rnid ASC;" );
				
				while ( rs2.next() )
				{
					/*
					 * Form "SELECT" clause
					 */
					String rnid = rs2.getString( "rnid" );
					String BaseName = "`" + RChain.replace( "`", "" ) + "_" + 
									  rnid.replace( "`", "" ) + "`";
					
					Statement st3 = con.createStatement();
					
					ResultSet rs3 = st3.executeQuery( "SELECT DISTINCT Entries " + 
													  "FROM `" + fid + 
													  "_ADT_RChain_Star_Select_List` " + 
													  "WHERE rchain = '" + RChain + 
													  "' AND '" + rnid + "' = rnid;" );
					String selectString = makeCommaSeparatedQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == selectString )
					{
						System.out.println( "Failed to assemble select statement." );
						return -2;
					}
					
					/*
					 * Form "MULT" computation for select
					 */
					rs3 = st3.executeQuery( "SELECT DISTINCT Entries FROM `" + fid + 
											"_ADT_RChain_Star_From_List` WHERE rchain = '" + 
											RChain + "' AND '" + rnid + "' = rnid;" );
					
					String MultString = makeStarSeparatedQuery( rs3, "Entries", " * " );
					
					if ( null == MultString )
					{
						System.out.println( "Failed to assemble MULT computation." );
						return -3;
					}
					
					/*
					 * Use previous query to form "FROM" clause
					 */
					rs3.beforeFirst();
					String fromString = makeCommaSeparatedQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == fromString )
					{
						System.out.println( "Failed to assemble from statement." );
						return -4;
					}
					
					/*
					 * Form "WHERE" clause
					 */
					rs3 = st3.executeQuery( "SELECT DISTINCT Entries FROM `" + 
											fid + "_ADT_RChain_Star_Where_List` " + 
											"WHERE rchain = '" + RChain + 
											"' AND '" + rnid + "' = rnid;" );
					String whereString = makeCommaSeparatedQuery( rs3, "Entries", " and " );
					rs3.close();
					
					if ( null == whereString )
					{
						System.out.println( "Failed to assemble where statement." );
						return -5;
					}
					
					
					String queryString = "SELECT " +  MultString + " AS `MULT`";
					
					if ( !selectString.isEmpty() )
					{
						queryString += ", " + selectString;
					}
					
					queryString += " FROM " + fromString;
					
					if ( !whereString.isEmpty() )
					{
						queryString += " WHERE " + whereString;
					}
					
					/*
					 * Create star table
					 */
					String cur_star_Table = "`" + fid + "_" + rnid.replace( "`", "" ) + 
											len + "_" + fc + "_star`";
					
					st3.execute( "DROP TABLE IF EXISTS " + cur_star_Table + ";" );
					
					System.out.println( "CREATE TABLE " + cur_star_Table + " AS " + 
								 queryString + ";" );
					st3.execute( "CREATE TABLE " + cur_star_Table + " AS " + 
								 queryString + ";" );
					
					/*
					 * Add index for faster computation
					 */
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
											"information_schema.columns WHERE " + 
											"table_schema = '" + dbOutputSchema + 
											"' AND table_name = '" + 
											cur_star_Table.replace("`","") + "';" );
					
					String IndexString = makeIndexQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == IndexString )
					{
						System.out.println( "Failed to assemble index." );
						return -6;
					}
					
					st3.execute( "ALTER TABLE " + cur_star_Table + " ADD INDEX ( " + IndexString + " );" );
					
					/*
					 * Create flat table
					 */
					String cur_flat_Table = "`" + fid + "_" + rnid.replace( "`", "" ) + 
											len + "_" + fc + "_flat`";
					String queryStringflat = "SELECT SUM( " + cur_CT_Table + 
											 ".`MULT` ) AS 'MULT'";
					
					if ( !selectString.isEmpty() )
					{
						queryStringflat += ", " + selectString;
					}
					
					queryStringflat += " FROM " + cur_CT_Table;
					
					if ( !selectString.isEmpty() )
					{
						queryStringflat += " GROUP BY " + selectString;
					}
					
					st3.execute( "DROP TABLE IF EXISTS " + cur_flat_Table + ";" );
					
					System.out.println("CREATE TABLE " + cur_flat_Table + " AS " + queryStringflat + ";" );
					st3.execute( "CREATE TABLE " + cur_flat_Table + " AS " + queryStringflat + ";" );
					
					/*
					 * Add index for faster computation
					 */
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
											"information_schema.columns WHERE " + 
											"table_schema = '" + dbOutputSchema + 
											"' AND table_name = '" + 
											cur_flat_Table.replace( "`", "" ) + "';" );
					
					IndexString = makeIndexQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == IndexString )
					{
						System.out.println( "Failed to assemble flat index." );
						return -7;
					}
					
					st3.execute( "ALTER TABLE " + cur_flat_Table + 
								 " ADD INDEX ( " + IndexString + " );" );
					
					/*
					 * Perform sort merge to create false table
					 */
					String cur_false_Table = "`" + fid + "_" + rnid.replace( "`", "" ) + 
											 len + "_" + fc + "_false`";
					
					Sort_merge3.sort_merge( cur_star_Table, cur_flat_Table, cur_false_Table, con );
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
											"information_schema.columns WHERE table_schema = '" + 
											dbOutputSchema + "' AND table_name = '" + 
											cur_false_Table.replace( "`", "" ) + "';" );
					
					/*
					 * Add index for faster computation
					 */
					IndexString = makeIndexQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == IndexString )
					{
						System.out.println( "Failed to assemble false index." );
						return -9;
					}
					
					st3.execute( "ALTER TABLE " + cur_false_Table + " ADD INDEX ( " + 
								 IndexString + " );" );
					
					/*
					 * Create CT table
					 */
					System.out.println( "SELECT column_name AS Entries " + 
							"FROM information_schema.columns " + 
							"WHERE table_schema = '" + dbOutputSchema +
							"' AND table_name = '" + cur_CT_Table.replace( "`", "" ) + "';" );
					rs3 = st3.executeQuery( "SELECT column_name AS Entries " + 
											"FROM information_schema.columns " + 
											"WHERE table_schema = '" + dbOutputSchema +
											"' AND table_name = '" + cur_CT_Table.replace( "`", "" ) + "';" );
					
					String CTJoinString = makeUnionSeparatedQuery( rs3, "Entries", ", ");
					rs3.close();
					
					if ( null == CTJoinString )
					{
						System.out.println( "Failed to assemble union." );
						return -10;
					}
					
					String QueryStringCT = "SELECT " + CTJoinString + " FROM " + 
										   cur_CT_Table + " UNION SELECT " + CTJoinString +
										   " FROM " + cur_false_Table + ", `" + 
										   fid + "_" + rnid.replace( "`", "" ) + 
										   "_join`";
					
					String Next_CT_Table = "";
					
					if ( rs2.next() )
					{
						Next_CT_Table = "`" + fid + "_" + BaseName.replace( "`", "" ) + "_CT`";
					}
					else
					{
						Next_CT_Table = "`" + fid + "_" + RChain.replace( "`", "" ) + "_CT`";
					}
					
					rs2.previous();
					
					cur_CT_Table = Next_CT_Table;
					
					st3.execute( "DROP TABLE IF EXISTS " + Next_CT_Table + ";" );
					
					System.out.println( "CREATE TABLE " + Next_CT_Table + " AS " + QueryStringCT + ";" );
					st3.execute( "CREATE TABLE " + Next_CT_Table + " AS " + QueryStringCT + ";" );
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
											"information_schema.columns WHERE table_schema = '" + 
											dbOutputSchema + "' AND table_name = '" + 
											Next_CT_Table.replace( "`", "" ) + "';" );
					
					/*
					 * Add index for faster computation
					 */
					IndexString = makeIndexQuery( rs3, "Entries", ", " );
					rs3.close();
					
					if ( null == IndexString )
					{
						System.out.println( "Failed to assemble index." );
						return -11;
					}
					
					st3.execute( "ALTER TABLE " + Next_CT_Table + " ADD INDEX ( " + 
								 IndexString + " );" );
					
					fc++;
					
					st3.close();
				}
				
				rs2.close();
				st2.close();
			}
			
			rs.close();
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "SQL failure." );
			e.printStackTrace();
			return -1;
		}
		catch ( IOException ie )
		{
			System.out.println( "IO Exception." );
			ie.printStackTrace();
			return -8;
		}
		return 0;
	}
	
	private static int dropExtraColumns( String fid )
	{
		try
		{
			Statement st = con.createStatement();
			Statement st2 = con.createStatement();
			
			ResultSet rs = st.executeQuery( "SELECT column_name AS Entries " + 
											"FROM information_schema.columns " + 
											"WHERE table_schema = '" + 
											dbOutputSchema + "' AND " + 
											"table_name = '" + fid + "_" + 
											rchain + "_local_CT';" );
			
			ResultSet rs2 = st2.executeQuery( "SELECT Parent FROM " + 
											 "FidWithParents WHERE FID = '`" + 
											 fid + "`';" );
			
			while ( rs.next() )
			{
				String columnName = "`" + rs.getString( 1 ) + "`";
				
				if ( columnName.equalsIgnoreCase( "`MULT`" ) )
				{
					continue;
				}
				
				boolean notAParent = true;
				
				while ( rs2.next() )
				{
					if ( columnName.equalsIgnoreCase( rs2.getString( 1 ) ) )
					{
						notAParent = false;
						break;
					}
				}
				
				if ( notAParent )
				{
					Statement st3 = con.createStatement();
					
					st3.execute( "ALTER TABLE `" + fid + "_" + rchain + 
								 "_local_CT` DROP COLUMN " + columnName  + ";" );
					
					st3.close();
				}
				
				rs2.beforeFirst();
			}
			
			rs2.close();
			
			rs.close();
			
			st2.close();
			st.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to remove extra columns." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
}