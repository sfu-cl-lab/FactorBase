/*
 * Fast Moebius Transform
 * Author: Kurt Routley
 * Date: Oct 24, 2013
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import lattice.short_rnid_LatticeGenerator;

import org.apache.commons.lang.StringUtils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;


public class fmt
{
	private static String address, username, password;
	private static String SetupDBName, DataDBName, OutDBName, WorkDBName;
	private static Connection conSetup, conData, conOutput, conWork;
	
	public static void main(String[] args)
	{
		FastMoebiusTransform();
	}
	
	public static void FastMoebiusTransform()
	{
		if ( loadConfiguration() != 0 )
		{
			System.out.println( "Failed to load configuration." );
			return;
		}
		
		if ( computeFastMoebiusTransform() != 0 )
		{
			System.out.println( "Failed to compute Fast Moebius Transform." );
		}
		
		closeConnection( conWork );
		closeConnection( conSetup );
		closeConnection( conData );
		closeConnection( conOutput );
	}
	
	public static void FastMoebiusTransform( String argSetupDBName,
											 String argDataDBName,
											 String argOutDBName,
											 String argAddress,
											 String argUser,
											 String argPass )
	{
		address		= argAddress;
		username	= argUser;
		password	= argPass;
		SetupDBName	= argSetupDBName;
		DataDBName	= argDataDBName;
		OutDBName	= argOutDBName;
		
		WorkDBName = DataDBName + "_work";
		
		conSetup = connect( SetupDBName );
		if ( conSetup == null )
		{
			System.out.println( "Could not connect to Setup Database." );
			return;
		}
		
		conData = connect( DataDBName );
		if ( conData == null )
		{
			System.out.println( "Could not connect to Data Database." );
			closeConnection( conSetup );
			return;
		}
		
		conOutput = connect( OutDBName );
		
		if ( conOutput == null )
		{
			Statement st = null;
			try
			{
				st = conSetup.createStatement();

				st.execute( "CREATE SCHEMA " + OutDBName + ";" );
				
				st.close();
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
				closeConnection( conSetup );
				closeConnection( conData );
				closeConnection( conOutput );
				return;
			}
			
			conOutput = connect( OutDBName );
			
			if ( conOutput == null )
			{
				System.out.println( "Failed to connect to output database." );
				closeConnection( conSetup );
				closeConnection( conData );
				return;
			}
		}
		
		if ( computeFastMoebiusTransform() != 0 )
		{
			System.out.println( "Failed to compute Fast Moebius Transform." );
		}
		
		closeConnection( conSetup );
		closeConnection( conData );
		closeConnection( conOutput );
	}
	
	public static void FastMoebiusTransform( Connection setupCon,
											 Connection dataCon,
											 Connection outputCon )
	{
		conSetup	= setupCon;
		conData		= dataCon;
		conOutput	= outputCon;
		
		if ( computeFastMoebiusTransform() != 0 )
		{
			System.out.println( "Failed to compute Fast Moebius Transform." );
		}
	}
	
	private static int computeFastMoebiusTransform()
	{
		long startTime = System.currentTimeMillis();
		
		try {
			//Get table names if not set
			if ( SetupDBName == null )
			{
				SetupDBName = conSetup.getSchema();
			}
			
			if ( DataDBName == null )
			{
				DataDBName = conData.getSchema();
				WorkDBName = conWork.getSchema();
			}
			
			if ( OutDBName == null )
			{
					OutDBName = conOutput.getSchema();	
			}
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to get schema names." );
			e.printStackTrace();
			return -1;
		}
		
		//Transfer tables to working schema
		System.out.println( "Transfering necessary setup tables to working schema..." );
		
		if ( BZScriptRunner.runScript( "src/scripts/fmt_transfer.sql", 
									   SetupDBName, 
									   WorkDBName, 
									   DataDBName, 
									   conSetup ) != 0 )
		{
			System.out.println( "Failed to run transfer script." );
			return -2;
		}

		long copyTime = System.currentTimeMillis();
		System.out.println( "Transfer time: " + ( copyTime - startTime ) + "ms." );
		
		//Connect to working database
		conWork = connect( WorkDBName );
		
		if ( conWork == null )
		{
			System.out.println( "Failed to establish connection to working database." );
			return -3;
		}
		
		//Generate lattice for joins
		int maxNumberOfMembers = 0;
		
		try
		{
			maxNumberOfMembers = short_rnid_LatticeGenerator.generate( conWork );
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to generate lattice." );
			e.printStackTrace();
			return -4;
		}
		
		//Add necessary entry tables
		if ( BZScriptRunner.runScript( "src/scripts/fmt_entries.sql", 
									   SetupDBName, 
									   WorkDBName, 
									   DataDBName, 
									   conWork ) != 0 )
		{
			System.out.println( "Failed to run entry script." );
			return -2;
		}
		
		//Counts for PVariables
		if ( countPVars() != 0 )
		{
			return -1;
		}
		
		// preparing the _join part for _CT tables
		if ( rnodeJoin() != 0 )
		{
			return -2;
		}
		
		for( int len = 1; len <= maxNumberOfMembers; len++ )
		{
			if ( rnodesCount( len ) != 0 )
			{
				return -3;
			}
		}
       
		for( int len = 1; len <= 1; len++ )
		{
			if ( rnodeFlatCounts( len ) != 0 )
			{
				return -4;
			}

			if ( rnodeStarCounts( len ) != 0 )
			{
				return -5;
			}

			if ( rnodeCT( len ) != 0 )
			{
				return -6;
			}
		}
		
		for ( int len = 2; len <= maxNumberOfMembers; len++ )
		{
			if ( rchainFlatCount( len ) != 0 )
			{
				return -7;
			}
		}
		
//
//		//delete the tuples with MULT=0 in the biggest CT table
//		String BiggestRchain="";
//		Statement st = con2.createStatement();
//		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );
//		
		long finishTime = System.currentTimeMillis();
		
		System.out.println( "Total time to compute Fast Moebius Transform: " + 
							( finishTime - startTime ) +
							"ms." );
		
		return 0;
	}
	
	private static int countPVars()
	{
		try
		{
			Statement st = conSetup.createStatement();
			ResultSet rs = null;
			
			Statement st3 = conOutput.createStatement();
			Statement st4 = conWork.createStatement();
			
			//Get counts for each PVariable
			rs = st.executeQuery( "SELECT * FROM PVariables;" );
			
			while( rs.next() )
			{
				String pvid = rs.getString( "pvid" );
				System.out.println( "pvid : " + pvid );
				
				//  create select query string
				ResultSet rs4 = st4.executeQuery( "SELECT Entries FROM " + 
												  "ADT_PVariables_Select_List " + 
												  "WHERE pvid = '" + pvid + "';" );
				String selectString = makeCommaSepQuery( rs4, "Entries", " , " );
	
				//  create from query string
				rs4 = st4.executeQuery( "SELECT Entries FROM " + 
										"ADT_PVariables_From_List WHERE pvid = '" +
										pvid + "';" );
				String fromString = makeCommaSepQuery( rs4, "Entries", " , " );
	
				// create GROUP BY query string
				rs4 = st4.executeQuery( "SELECT Entries FROM " + 
										"ADT_PVariables_GroupBy_List WHERE pvid " +
										"= '" + pvid + "' AND Entries IN ( " + 
										"SELECT `1nid` FROM " + SetupDBName + 
										".`1Nodes` WHERE pvid = '" + pvid + "' );" );
				
				String GroupByString = makeCommaSepQuery( rs4, "Entries", " , " );
				
				rs4.close();
				
				/*
				 *  Check for groundings on pvid
				 *  If exist, add as where clause
				 */
				Statement st5 = conSetup.createStatement();
				ResultSet rsGrounding = null;
				try
				{
					rsGrounding = st5.executeQuery( "SELECT id FROM Groundings " + 
												   "WHERE pvid = '" + pvid + "';" );
				}
				catch( MySQLSyntaxErrorException e )
				{
					System.out.println( "No Groundings table." );
				}
				
				String whereString = "";
				
				if ( null != rsGrounding )
				{
					if ( rsGrounding.absolute(1) )
					{
						whereString += " WHERE " + pvid + "." + 
									   pvid.replaceAll( "[0-9]", "" ) + "_id = " + 
									   rsGrounding.getString(1);
					}
					
					rsGrounding.close();
				}
				
				st5.close();
						
				String queryString = "SELECT " + selectString + " FROM " + 
									 fromString + whereString;
				
				if ( !GroupByString.isEmpty() )
				{
					queryString = queryString + " GROUP BY "  + GroupByString;
				}
				
				st3.execute( "DROP TABLE IF EXISTS " + pvid + "_counts;" );
				System.out.println( "CREATE TABLE " + pvid + "_counts AS " + 
						 			queryString );
				st3.execute( "CREATE TABLE " + pvid + "_counts AS " + 
							 queryString );
				
				ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										"information_schema.columns WHERE " + 
										"table_schema = '" + OutDBName + "' and " +
										"table_name = '" + pvid + "_counts';" );
				String IndexString = makeIndexQuery( rs3, "Entries", " , " );
	
				st3.execute( "ALTER TABLE " + pvid + "_counts ADD INDEX " + pvid + 
							 "_Index ( " + IndexString + " );" );
				
				rs3.close();
			}
	
			st4.close();
			st3.close();
			rs.close();
			st.close();
		} 
		catch ( SQLException e )
		{
			e.printStackTrace();
			return -1;
		}
		
		System.out.println( "Finished counts for PVariables." );
		return 0;
	}
	
	private static int rnodeJoin()
	{
		Statement st4 = null;
		try
		{
			st4 = conWork.createStatement();
			ResultSet rs4 = st4.executeQuery("SELECT rnid FROM RNodes ;");
			
			Statement st5 = conWork.createStatement();
			
			while( rs4.next() )
			{
				String rnid = rs4.getString( "rnid" );
				System.out.println( "\n rnid : " + rnid );

				ResultSet rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM " + 
												  "Rnodes_join_columnname_list WHERE rnid = '" + 
												  rnid +"';" );
				
				String ColumnString = makeCommaSepQuery( rs5, "Entries", " , " );
				
				rs5.close();
				
				if ( ColumnString.isEmpty() )
				{
					ColumnString = rnid + " VARCHAR(5)";
				}
				else
				{
					ColumnString = rnid + " VARCHAR(5) ," + ColumnString;
				}
				
				String createString = "CREATE TABLE `" + rnid.replace("`", "") +
									  "_join` (" + ColumnString + ");";		
				System.out.println( "Create String : " + createString );

				Statement st3 = conOutput.createStatement();
				st3.execute( createString );	
				st3.execute( "INSERT INTO `" + rnid.replace("`","") + "_join` ( " + 
							 rnid + " ) values ('F');");
				st3.close();
			}
			
			st5.close();
			rs4.close();
			st4.close();
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to perform RNode Join." );
			e.printStackTrace();
			return -1;
		}
		
		System.out.println( "Finished RNode Join." );	
		
		return 0;
	}
	
	public static int rnodesCount( int len )
	{
		Statement st4 = null;
		try
		{
			st4 = conWork.createStatement();
			ResultSet rs4 = st4.executeQuery( "SELECT name AS RChain FROM " + 
					  "lattice_set WHERE " + 
					  "lattice_set.length = " + len + 
					  ";" );

			Statement st5 = conWork.createStatement();
			while( rs4.next() )
			{
				String rchain = rs4.getString( "RChain" );
				System.out.println( "\n RChain : " + rchain );
				
				ResultSet rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM " +
										  "lattice_membership, " + "" +
										  "RNodes_Select_List WHERE NAME = '" +
										  rchain + "' AND lattice_membership." +
										  "member = RNodes_Select_List.rnid;" );
				String selectString = makeCommaSepQuery( rs5, "Entries", " , ");
				rs5.close();
				
				rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM " + 
								"lattice_membership, RNodes_From_List " + 
								"WHERE NAME = '" + rchain + "' AND " + 
								"lattice_membership.member = " + 
								"RNodes_From_List.rnid;" );
				String fromString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				
				rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM " + 
								"lattice_membership, RNodes_Where_List " + 
								"WHERE NAME = '" + rchain + "' AND " + 
								"lattice_membership.member = " + 
								"RNodes_Where_List.rnid;");
				String whereString = makeCommaSepQuery(rs5, "Entries", " and ");
				rs5.close();
				
				String queryString = "Select " + selectString + " from " + 
							 fromString + " where " + whereString;
				
				rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM " + 
								"lattice_membership, RNodes_GroupBy_List" + 
								" WHERE NAME = '" + rchain + "' AND " + 
								"lattice_membership.member = " + 
								"RNodes_GroupBy_List.rnid;");
				String GroupByString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				
				if ( !GroupByString.isEmpty() )
				{
					queryString = queryString + " GROUP BY"  + GroupByString;
				}
				
				String createString = "CREATE TABLE `" + rchain.replace("`", "") +
							  "_counts`"+" AS " + queryString;
				System.out.println("Create String : " + createString );
				
				Statement st3 = conOutput.createStatement();
				st3.execute( createString );		
				
				ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										  "information_schema.columns where table_schema = '" + 
										  OutDBName + "' and table_name = '" + 
										  rchain.replace("`", "") + "_counts';" );
				String IndexString = makeIndexQuery( rs3, "Entries", " , " );
				rs3.close();
				
				st3.execute( "ALTER TABLE `" + rchain.replace("`", "") + "_counts` ADD INDEX `" + 
							 rchain.replace("`", "") + "_Index` ( " + IndexString + " );" );
				
				st3.close();
			}
			
			st5.close();
			rs4.close();
			st4.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to create RNode counts." );
			e.printStackTrace();
		}
		
		System.out.println("Finished RNode counts." );
		return 0;
	}
	
	private static int rnodeFlatCounts( int len )
	{
		try
		{
			Statement st4 = conWork.createStatement();
			
			ResultSet rs4 = st4.executeQuery( "SELECT name AS RChain FROM lattice_set WHERE " + 
					  						  "lattice_set.length = " + len + ";" );
			while ( rs4.next() )
			{
				String rchain = rs4.getString("RChain");
				System.out.println("\n rchain String : " + rchain );
				
				Statement st5 = conWork.createStatement();
				
				ResultSet rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM lattice_membership, " + 
										  "ADT_RNodes_1Nodes_Select_List  WHERE NAME = '" + rchain + 
										  "' AND lattice_membership.member = " + 
										  "ADT_RNodes_1Nodes_Select_List.rnid;" );
				String selectString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				
				rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM lattice_membership, " +
								"ADT_RNodes_1Nodes_FROM_List WHERE NAME = '" + rchain + 
								"' AND lattice_membership.member = " + 
								"ADT_RNodes_1Nodes_FROM_List.rnid;" );
				String fromString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				
				String queryString = "SELECT " + selectString + " FROM " + fromString ;
				
				rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM lattice_membership, " + 
								"ADT_RNodes_1Nodes_GroupBY_List WHERE NAME = '" + rchain + 
								"' AND lattice_membership.member = " + 
								"ADT_RNodes_1Nodes_GroupBY_List.rnid;" );
				String GroupByString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				st5.close();
				
				if ( !GroupByString.isEmpty() )
				{
					queryString = queryString + " GROUP BY "  + GroupByString;
				}
				
				String createString = "CREATE TABLE `" + rchain.replace("`", "") + "_flat` AS " + 
									  queryString;
				System.out.println("\n create String : " + createString );
				
				Statement st3 = conOutput.createStatement();
				st3.execute(createString);
				
				ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										  "information_schema.columns WHERE table_schema = '" + 
										  OutDBName + "' AND table_name = '" + 
										  rchain.replace("`", "") + "_flat';" );
				String IndexString = makeIndexQuery(rs3, "Entries", " , ");
				rs3.close();
				
				st3.execute( "ALTER TABLE `" + rchain.replace("`", "") + "_flat` ADD INDEX `" + 
					 rchain.replace("`", "") + "_flat` ( " + IndexString + " );" );
				st3.close();
			}
			
			rs4.close();
			st4.close();
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to create RNode Flat counts." );
			e.printStackTrace();
			return -1;
		}
		
		System.out.println( "Finished RNode Flat counts." );
		return 0;
	}
	
	private static int rnodeStarCounts( int len )
	{
		try
		{
			Statement st4 = conWork.createStatement();
			
			ResultSet rs4 = st4.executeQuery( "SELECT name AS RChain FROM lattice_set WHERE " + 
					  						  "lattice_set.length = " + len + ";" );
			while( rs4.next() )
			{
				String rchain = rs4.getString( "RChain" );
				System.out.println( "\n rchain String : " + rchain );
				
				Statement st5 = conWork.createStatement();
				
				ResultSet rs5 = st5.executeQuery( "SELECT DISTINCT Entries FROM lattice_membership, " + 
										  "ADT_RNodes_Star_Select_List  WHERE NAME = '" + rchain + 
										  "' AND lattice_membership.member = " + 
										  "ADT_RNodes_Star_Select_List.rnid;");
				String selectString = makeCommaSepQuery( rs5, "Entries", " , " );
				rs5.close();
				
				rs5 = st5.executeQuery("SELECT DISTINCT Entries FROM lattice_membership, ADT_RNodes_Star_From_List WHERE NAME = '" + rchain + "' AND lattice_membership.member = ADT_RNodes_Star_From_List.rnid;");
				String MultString = makeStarSepQuery( rs5, "Entries", " * " );
				
				rs5.beforeFirst();
				
				String fromString = makeCommaSepQuery( rs5, "Entries", " , " );
				
				rs5.close();
				st5.close();
				
				String queryString = "SELECT " +  MultString+ " AS `MULT`";
				
				if ( !selectString.isEmpty() )
				{
					queryString += "," + selectString;
				}
				
				queryString += " FROM " + fromString ;
				
				String createString = "CREATE TABLE `" + rchain.replace("`", "") + "_star` as " + queryString;
				
				Statement st3 = conOutput.createStatement();
				st3.execute( createString );
				
				ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										  "information_schema.columns WHERE table_schema = '" + 
										  OutDBName + "' AND table_name = '" + 
										  rchain.replace("`", "") + "_star';" );
				
				String IndexString = makeIndexQuery(rs3, "Entries", " , ");
				
				rs3.close();
				
				st3.execute( "ALTER TABLE `" + rchain.replace("`", "") + "_star` ADD INDEX `" + 
					 rchain.replace("`", "") + "_star` ( " + IndexString + " );" );
				
				st3.close();
			}
			
			rs4.close();
			st4.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to create RNode Star counts." );
			e.printStackTrace();
			return -1;
		}
		
		System.out.println( "Finished RNode Star counts." );
		return 0;
	}

	private static int loadConfiguration()
	{
		Properties configFile = new java.util.Properties();
		
		FileReader fr = null;
		try
		{
			fr = new FileReader( "cfg/fmtconfig.cfg" );
		} 
		catch ( FileNotFoundException e )
		{
			System.out.println( "Could not find configuration file." );
			return -1;
		}
		
		BufferedReader br = new BufferedReader( fr );
		
		try
		{
			configFile.load( br );
			br.close();
			fr.close();
		}
		catch (IOException e)
		{
			System.out.println( "Failed to read configuration file." );
			return -2;
		}
		
		if ( processConnections( configFile ) != 0 )
		{
			return -3;
		}
		
		return 0;
	}
	
	private static int processConnections( Properties configs )
	{
		address = configs.getProperty( "Address" );
		username = configs.getProperty( "User" );
		password = configs.getProperty( "Password" );
		SetupDBName = configs.getProperty( "SetupDBName" );
		DataDBName = configs.getProperty( "DataDBName" );
		OutDBName = configs.getProperty( "OutDBName" );
		
		WorkDBName = DataDBName + "_work";
		
		conSetup = connect( SetupDBName );
		if ( conSetup == null )
		{
			System.out.println( "Could not connect to Setup Database." );
			return -3;
		}
		
		try
		{
			Statement st = conSetup.createStatement();
			st.execute( "DROP SCHEMA IF EXISTS " + OutDBName + ";" );
			st.execute( "CREATE SCHEMA " + OutDBName + ";" );
			st.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create " + OutDBName );
			e.printStackTrace();
			return -1;
		}
		
		conData = connect( DataDBName );
		if ( conData == null )
		{
			System.out.println( "Could not connect to Data Database." );
			closeConnection( conSetup );
			return -3;
		}
		
		conOutput = connect( OutDBName );
		
		if ( conOutput == null )
		{
			System.out.println( "Failed to connect to output database." );
			closeConnection( conSetup );
			closeConnection( conData );
			return -3;
		}
		
		return 0;
	}
	
	private static Connection connect( String dbname )
	{
		String CONN_STR = "jdbc:" + address + "/" + dbname;
		
		try
		{
			java.lang.Class.forName( "com.mysql.jdbc.Driver" );
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println( "Failed to load JDBC Driver." );
			return null;
		}
		
		Connection con = null;
		
		try
		{
			con = (Connection) DriverManager.getConnection( CONN_STR, 
															username, 
															password );
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to establish connection to " + 
								address + " for database " + dbname + "." );
			return null;
		}
		
		return con;
	}
	
	private static void closeConnection( Connection con )
	{
		try
		{
			con.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Error occurred while trying to close " +
								"connection." );
		}
	}
	
	public static String makeCommaSepQuery( ResultSet rs, 
											String colName, 
											String del )
	{
		ArrayList<String> parts = new ArrayList<String>();
		
		try
		{
			while( rs.next() )
			{
				parts.add( rs.getString( colName ) );
			}
		}
		catch ( SQLException e ) 
		{
			e.printStackTrace();
			return null;
		}
		
		return StringUtils.join( parts, del );
	}
	
	public static String makeIndexQuery( ResultSet rs, 
										 String colName, 
										 String del )
	{
		ArrayList<String> parts = new ArrayList<String>();
		int count = 0;

		try
		{
			while( rs.next() & ( count<16 ) )
			{
				String temp = rs.getString( colName );
				temp = "`" + temp + "`";
				parts.add( temp+ " ASC" );
				count++;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}

		return StringUtils.join(parts,del);
	}
	
	public static String makeStarSepQuery( ResultSet rs, String colName, String del )
	{
		ArrayList<String> parts = new ArrayList<String>();

		try
		{
			while( rs.next() )
			{
				String temp = "`" + rs.getString(colName).replace("`", "") + "`.`MULT` ";			
				parts.add(temp);
			}
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to create query." );
			e.printStackTrace();
			return null;
		}

		return StringUtils.join(parts,del);
	}
	
	public static int rnodeCT( int len )
	{
		try 
		{
			Statement st4 = conWork.createStatement();
			
			ResultSet rs4 = st4.executeQuery( "SELECT name AS RChain FROM lattice_set WHERE " + 
								  "lattice_set.length = " + len + ";" );
			while ( rs4.next() )
			{
				String rchain = rs4.getString( "RChain" );
				System.out.println("\n rchain String : " + rchain );
				
				if ( sort_merge( "`" + rchain.replace("`", "") + "_star`", 
						 "`" + rchain.replace("`", "") + "_flat`",
						 "`" + rchain.replace("`", "") + "_false`" ) != 0 )
				{
					System.out.println( "Sort merge failed for rchain: " + rchain );
					return -1;
				}
				
				Statement st3 = conOutput.createStatement();
				ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										  "information_schema.columns WHERE table_schema = '" + 
										  OutDBName + "' AND table_name = '" + 
										  rchain.replace("`", "") + "_false';" );
				String IndexString = makeIndexQuery( rs3, "Entries", " , " );
				rs3.close();
				
				st3.execute( "ALTER TABLE `" + rchain.replace( "`", "" ) + "_false` ADD INDEX `" + 
					 rchain.replace( "`", "" ) + "_false` ( " + IndexString + " );" );
				
				rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns " + 
								"WHERE table_schema = '" + OutDBName + "' AND table_name = '" + 
								rchain.replace( "`", "" ) + "_counts';" );
				
				String UnionColumnString = makeUnionSepQuery( rs3, "Entries", " , " );
				rs3.close();
				
				String createCTString = "CREATE TABLE `" + rchain.replace("`", "") + "_CT` AS SELECT " + 
								UnionColumnString + " FROM `" + rchain.replace("`", "") + 
								"_counts` UNION SELECT " + UnionColumnString + " FROM `" + 
								rchain.replace("`", "") + "_false`, `" + 
								rchain.replace("`", "") +"_join`;" ;
				System.out.println("\n create CT table String : " + createCTString );
				st3.execute( createCTString );		
				
				rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns " + 
								"WHERE table_schema = '" + OutDBName + "' AND table_name = '" + 
								rchain.replace( "`", "" ) + "_CT';" );
				
				IndexString = makeIndexQuery( rs3, "Entries", " , " );
				rs3.close();
				
				st3.execute( "ALTER TABLE `" + rchain.replace( "`", "" ) + "_CT` ADD INDEX `" + 
					 rchain.replace( "`", "" ) + "_CT` ( " + IndexString + " );" );
				st3.close();
			}
			
			rs4.close();
			st4.close();
		} 
		catch ( SQLException e )
		{
			System.out.println( "Failed to create RNode CT." );
			e.printStackTrace();
			return -1;
		}
		
		System.out.println( "Finished creating RNode CT." );
		return 0;
	}
	
	public static int rchainFlatCount( int len )
	{
		long l = System.currentTimeMillis(); 

		try
		{
			Statement st4 = conWork.createStatement();
			
			ResultSet rs4 = st4.executeQuery( "SELECT name AS RChain FROM lattice_set WHERE " + 
					  "lattice_set.length = " + len + ";" );
			int fc = 0;
			
			while ( rs4.next() )
			{
				String rchain = rs4.getString("RChain");
				
				String cur_CT_Table = "`" + rchain.replace( "`", "" ) + "_counts`";  
				
				Statement st5 = conWork.createStatement();
				ResultSet rs5 = st5.executeQuery( "SELECT DISTINCT parent, removed AS rnid FROM " + 
										  "lattice_rel WHERE child = '" + rchain + 
										  "' ORDER BY rnid ASC;");
				
				while ( rs5.next() )
				{
					String rnid = rs5.getString( "rnid" );
					
					String BaseName = "`" + rchain.replace( "`", "" ) + "_" + rnid.replace( "`", "" ) + "`";
					
					Statement st6 = conWork.createStatement();
					
					ResultSet rs6 = st6.executeQuery( "SELECT DISTINCT Entries FROM " + 
												  "ADT_RChain_Star_Select_List WHERE rchain = '" + 
												  rchain + "' AND '" + rnid + "' = rnid;" );
					String selectString = makeCommaSepQuery( rs6, "Entries", " , " );			
					rs6.close();
					
					rs6 = st6.executeQuery( "SELECT DISTINCT Entries FROM ADT_RChain_Star_From_List WHERE " + 
										"rchain = '" + rchain + "' AND '" + rnid + "' = rnid;" );
					String MultString = makeStarSepQuery(rs6, "Entries", " * ");
					
					rs6.beforeFirst();
					String fromString = makeCommaSepQuery( rs6, "Entries", " , " );
					rs6.close();
					
					rs6 = st6.executeQuery( "SELECT DISTINCT Entries FROM ADT_RChain_Star_Where_List " + 
										"WHERE rchain = '" + rchain + "' AND '" + rnid + "' = rnid;" );
					String whereString = makeCommaSepQuery(rs6, "Entries", " AND " );
					rs6.close();
					st6.close();
					
					String queryString = "SELECT " +  MultString + " AS `MULT` ," + selectString + 
									 " FROM " + fromString;
					if ( !whereString.isEmpty() )
					{
						queryString += " WHERE " + whereString;
					}
					
					String rnid_or = rnid;
					
					String cur_star_Table = "`" + rnid.replace("`", "") + len + "_" + fc + "_star`";
					String createStarString = "CREATE TABLE " + cur_star_Table + " AS " + queryString;
					
					Statement st3 = conOutput.createStatement();
					
					st3.execute( createStarString );
					
					ResultSet rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
												  "information_schema.columns WHERE table_schema = '" + 
												  OutDBName + "' AND table_name = '" + 
												  cur_star_Table.replace("`","") + "';" );
					String IndexString = makeIndexQuery(rs3, "Entries", " , ");
					rs3.close();
					st3.execute( "ALTER TABLE " + cur_star_Table + " ADD INDEX " + cur_star_Table + 
							 " ( " + IndexString + " );" );       
					
					String cur_flat_Table = "`" + rnid.replace( "`", "" ) + len + "_" + fc + "_flat`";
					String queryStringflat = "SELECT SUM(" + cur_CT_Table + ".`MULT`) AS 'MULT', " + 
										 selectString + " FROM " + cur_CT_Table + " GROUP BY " +
										 selectString + ";";
					String createStringflat = "CREATE TABLE " + cur_flat_Table + " AS " + queryStringflat;		
					st3.execute( createStringflat );
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM information_schema.columns " + 
										"WHERE table_schema = '" + OutDBName + "' AND table_name = '" + 
										cur_flat_Table.replace("`","") + "';" );
					IndexString = makeIndexQuery( rs3, "Entries", " , " );
					rs3.close();
					
					st3.execute( "ALTER TABLE " + cur_flat_Table + " ADD INDEX " + cur_flat_Table + " ( " + 
							 IndexString + " );" );       
					
					String cur_false_Table= "`" + rnid.replace( "`", "" ) + len + "_" + fc + "_false`";
					
					if ( sort_merge(cur_star_Table,cur_flat_Table,cur_false_Table ) != 0 )
					{
						System.out.println( "Sort merge failed." );
						return -1;
					}
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										"information_schema.columns WHERE table_schema = '" + 
										OutDBName + "' AND table_name = '" + 
										cur_false_Table.replace( "`", "" ) + "';" );
					IndexString = makeIndexQuery( rs3, "Entries", " , " );
					rs3.close();
					
					st3.execute( "ALTER TABLE " + cur_false_Table + " ADD INDEX " + cur_false_Table + 
							 " ( " + IndexString + " );" );       
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										"information_schema.columns WHERE table_schema = '" + 
										OutDBName + "' AND table_name = '" + 
										cur_CT_Table.replace( "`", "" ) + "';" );
					String CTJoinString = makeUnionSepQuery( rs3, "Entries", " , " );
					rs3.close();
					
					System.out.println("CT Join String : " + CTJoinString);
					
					String QueryStringCT = "SELECT " + CTJoinString + " FROM " + cur_CT_Table + 
									   " UNION SELECT " + CTJoinString + " FROM " + 
									   cur_false_Table + ", `" + rnid_or.replace( "`", "" ) + "_join`";
					
					String Next_CT_Table="";
					if ( rs5.next() )
					{
						Next_CT_Table = "`" + BaseName.replace("`", "") + "_CT`";
					}
					else
					{
						Next_CT_Table = "`" + rchain.replace( "`", "" ) + "_CT`";
					}
					
					cur_CT_Table = Next_CT_Table;	
					
					st3.execute( "CREATE TABLE " + Next_CT_Table + " AS " + QueryStringCT);	
					rs5.previous();
					
					rs3 = st3.executeQuery( "SELECT column_name AS Entries FROM " + 
										"information_schema.columns WHERE table_schema = '" + 
										OutDBName + "' AND table_name = '" + 
										Next_CT_Table.replace( "`", "" ) + "';" );
					IndexString = makeIndexQuery( rs3, "Entries", " , " );
					rs3.close();
					
					st3.execute( "ALTER TABLE " + Next_CT_Table + " ADD INDEX " + Next_CT_Table + 
							 " ( " + IndexString + " );" );       
					
					fc++;
							
					st3.close();
				}
				
				rs5.close();
				st5.close();
			}
			
			rs4.close();
			st4.close();
		}
		catch ( SQLException e )
		{
			System.out.println( "Failed to create RChain Flat Count." );
			e.printStackTrace();
			return -1;
		}
		
		long l2 = System.currentTimeMillis(); //@zqian : measure structure learning time
		System.out.print("Building Time(ms): "+(l2-l)+" ms.\n");
		System.out.println( "Finished creating Rchain flat count." );
		return 0;
	}
	
	public static int sort_merge( String table1, String table2, String table3 )
	{
		long time1 = System.currentTimeMillis();
		
		File ftemp = new File( "sort_merge.csv" );
		if ( ftemp.exists() )
		{
			ftemp.delete();
		}
		
		File file = new File( "sort_merge.csv" );
		try
		{
			FileWriter fileW = new FileWriter( file );
			
			BufferedWriter output = new BufferedWriter( fileW );
			
			Statement st3 = conOutput.createStatement();
			
			ArrayList<String> orderList=new ArrayList<String>();
			String order = null;
			
			ResultSet rs3 = st3.executeQuery( "SHOW COLUMNS FROM " + table1 + " ;" );
			
			while ( rs3.next() )
			{
				orderList.add( "`" + rs3.getString(1) + "` " );
			}
			
			rs3.close();
			
			int len = orderList.size();
			
			for ( int i = 0; i < len; i++ )
			{
				if ( orderList.get(i).contains( "MULT" ) )
				{
					orderList.remove(i);
					break;
				}
			}
			
			len = orderList.size();
			
			if ( len > 0 )
			{
				order = " " + orderList.get(0) + " ";

				for ( int i = 1; i < len; i++ )
				{
					order += " , " + orderList.get(i);
				}
			}
			
			String temp= "MULT DECIMAL ";
			for ( int i = 0; i < len; i++ )
			{
				temp += " , " + orderList.get(i) + " VARCHAR(45) ";
			}
			
			st3.execute( "DROP TABLE IF EXISTS " + table3 + " ;" );
			st3.execute( "CREATE TABLE " + table3 + " ( " + temp + ");" );

			Statement st5 = conOutput.createStatement();
			ResultSet rs5 =null;
			
			if ( order != null )
			{
				rs3 = st3.executeQuery( "SELECT DISTINCT mult, " + order + " FROM " + table1 + 
										" ORDER BY " + order + " ;" );
				rs5 = st5.executeQuery( "SELECT DISTINCT mult, " + order + " FROM " + table2 + 
										" ORDER BY " + order + " ;" );
			}
			else
			{
				rs3 = st3.executeQuery( "SELECT DISTINCT mult FROM " + table1 + ";" );
				rs5 = st5.executeQuery( "SELECT DISTINCT mult FROM " + table2 + ";" );
			}
			
			int size1 = 0;
			if ( rs3.last() )
			{
				size1 = rs3.getRow();
			}
			
			int size2 = 0;
			if ( rs5.last() )
			{
				size2 = rs5.getRow();
			}
			
			ResultSetMetaData rsmd = (ResultSetMetaData) rs3.getMetaData();
			int no_of_colmns = rsmd.getColumnCount();
			
			int i=1;
			int j=1;
			
			rs3.absolute(1);
			rs5.absolute(1);
			
			while ( ( i <= size1 ) && ( j <= size2 ) )
			{
				int val1 = 0, val2 = 0;
				for ( int k = 2; k <= no_of_colmns; k++ )
				{
					try
					{
						val1=Integer.parseInt(rs3.getString(k));
						val2=Integer.parseInt(rs5.getString(k));
					}
					catch(java.lang.NumberFormatException e)
					{
						//Do nothing
					}
					finally
					{
						if( rs3.getString(k).compareTo( rs5.getString(k))>0)
						{
							val1=1; 
							val2=0;
						}
						else if(rs3.getString(k).compareTo(rs5.getString(k))<0)
						{
							val1=0;
							val2=1;
						}
					}
					
					if( val1 < val2 )
					{
						String quer = rs3.getString( 1 );
						for ( int c = 2; c <= no_of_colmns; c++ )
						{
							quer += "$"+ rs3.getString(c);
						}
						
						output.write( ( quer ) + "\n");
						i++;
						break;
					}
					else if( val1 > val2 )
					{
						j++;
						break;
					}
				}
				
				if ( val1 == val2 )
				{
					String query="";
					try
					{
						query += (Integer.parseInt( rs3.getString(1))-Integer.parseInt(rs5.getString(1)));
					}
					catch ( NumberFormatException e )
					{
						query += Integer.parseInt(rs3.getString(1));
					}
					
					for ( int c = 2; c <= no_of_colmns; c++ )
					{
						query += "$" + rs3.getString(c);
					}
					
					output.write( query + "\n" );
					
					i++;
					j++;
				}
				
				rs3.absolute(i);
				rs5.absolute(j);
			}
			
			rs5.close();
			st5.close();
			
			if( i > 1 )
			{
				rs3.absolute(i-1);
			}
			else
			{
				rs3.beforeFirst();
			}
			
			while( rs3.next() )
			{
				String query = rs3.getString( 1 );
				for ( int c = 2; c <= no_of_colmns; c++ )
				{
					query += "$" + rs3.getString( c );
				}
				
				output.write( query + "\n" );
			}
			
			rs3.close();
			
			output.close();
			fileW.close();
			
			st3.execute( "DROP TABLE IF EXISTS " + table3 + ";" );
			st3.execute( "CREATE TABLE " + table3 + " LIKE " + table1 + ";" );
			st3.execute( "LOAD DATA LOCAL INFILE 'sort_merge.csv' INTO TABLE " + table3 + 
						 " FIELDS TERMINATED BY '$' LINES TERMINATED BY '\\n';" );
			
			st3.close();
		}
		catch ( IOException e1 )
		{
			System.out.println( "Failed to open CSV file." );
			e1.printStackTrace();
			return -1;
		}
		catch ( SQLException e2 )
		{
			System.out.println( "SQL Error during Sort Merge." );
			e2.printStackTrace();
			return -2;
		}

		long time5=System.currentTimeMillis();
		System.out.println("\ntotal time: "+(time5-time1)+"\n");
		System.out.println( "Sort merge completed." );
		return 0;
	}
	
	public static String makeUnionSepQuery( ResultSet rs, String colName, String del )
	{	
		ArrayList<String> parts = new ArrayList<String>();

		try
		{
			while ( rs.next() )
			{
				parts.add( "`" + rs.getString( colName ) + "`" );
			}
		}
		catch (SQLException e)
		{
			System.out.println( "Failed to create union query." );
			e.printStackTrace();
			return null;
		}
		
		return StringUtils.join(parts,del);
	}
}
