/*current error CREATE TABLE unielwin_target_final_CT.`diff(course0)_CT` like unielwin_target_db.`local_CT`;
Table 'unielwin_target_db.local_CT' doesn't exist*/


import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class TestWrapper {
	
	static Connection con0, con1, conFinal,con_preprocess;
	static String databaseName, databaseName1, databaseName2, databaseName3;
	//				unielwin, unielwin_target, unielwin_target_setup, unielwin_target_final_CT,
	static String databaseName4, databaseName5,	databaseName6;
	// 				unielwin_target_CT, unielwin_target_final, unielwin_target_db
	//databaseName4 is not used
	//databaseName6 is used but not created. Used in buildsubct (todo??)
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	static int groundingCount;
	static String functorId;
	static boolean processingRNode = false;
	static boolean tableNameIsTooLong = false;
	static boolean  CrossValidation;
	// Global variable: true to use nodes in Markov Blanket, false to use only functor's parents and associated RNodes
	static boolean useMarkovBlanket = true;
	static ArrayList<String> CurrendID_List = new ArrayList<String>();
	static String current_id1="";
	static String functorId_In_BN="";
	public static void main(String[] args) throws Exception
	{   
		long time1=System.currentTimeMillis();
		/*
		 *Target  Database Setup (i.e. Testing Database)
		 * @database@_target_setup, @database@_target
		 * 
		 */
		MakeTargetSetup.runMakeTargetSetup();
		
		/*
		 *Read configure file for TestWrapper
		 */
		setVarsFromConfig();
		/*
		 * drop the `mult` column in @database@_BN.*_cp tables
		 * remapping the rnid with orig_rnid, e.g. a --> RA(prof0,student0)
		 * */
		pre_process( );
		
		connectDBTargetSetup();		
		connectDB_preprocess();

		/* get the each functor, e.g. @database@_BN.FNodes
		 * */
		ArrayList<String> functors = GetFunctors();

		Statement st = con0.createStatement(); // connect to @database@_target_setup
		Statement st1 = con_preprocess.createStatement(); // connect to @database@_bn

		st.execute( "DROP SCHEMA IF EXISTS " + databaseName3 + ";" ); //@database@_target_final_CT
		st.execute( "CREATE SCHEMA " + databaseName3 + ";" );
		st.execute( "DROP SCHEMA IF EXISTS " + databaseName5 + ";" ); //@database@_target_final
		st.execute( "CREATE SCHEMA " + databaseName5 + ";" );
		
		connectDBTargetFinalCT();
		connectDBTargetFinal();
		
		functorId = "";
		String node = "";
		String table = "";
		
		if ( useMarkovBlanket )	{
			node = "TargetMBNode";
			table = "TargetMB";
		}
		else{
			node = "TargetParent";
			table = "TargetParents";
		}
		
		//functorId = "`teachingability(prof0)`";//zqian April 10th
		//functorId= "`intelligence(student0)`";
		//functorId= "`b`";
		//functorId= "`a`";
		//functorId = "`rating(course0)`";
		//functorId ="`diff(course0)`";
		//functorId ="`ranking(student0)`";// no rchain
		
		functorId="";
		for ( int i = 0; i < functors.size(); i++ )  //zqian April 10th
		{
			tableNameIsTooLong=false;
			String target_common_select_string = ProcessTargetParent(st,st1,functors,node,table,i);
			// Aug. 20, compute the subCTs for target and its children
			long ctt2 = System.currentTimeMillis();
			ProcessTargetChildren(st,st1,functors,node,table,i,target_common_select_string);
		
			
			/*********	processing target's children**************END*****************/	
			long ctt3 = System.currentTimeMillis();
	//		System.out.println( "\n extract the target_parent_ct and target_child_ct, run time is: " + ( ctt3 - ctt2 ) + "ms. \n ******************************** \n\n" );
			if (!tableNameIsTooLong)
			{
	//&&&&&&&&&&&&&&
				/* Extend the final_CT tables for each functorId with all possible values */ 
				Extend_Final_CT();
				long ctt4 = System.currentTimeMillis();
				System.out.println( "\n Extend the final_CT tables with all possible values, run time is: " + ( ctt4 - ctt3 ) + "ms. \n ******************************** \n\n" );
			
				// remove the n/a ??
				Update_CT(CurrendID_List,current_id1 );
				long ctt14 = System.currentTimeMillis();
				System.out.println( "\n Computing the Frequency, run time is: " + ( ctt14 - ctt4 ) + "ms. \n ******************************** \n\n" );
	
				// Create *_final table, target_parent/target_child_final	 
				//createSubFinal_zqian( functorArgs ); //using natural join,
				
				//create the target_sum by summation
				//compute the probability only for entries with the highest score and store them in target_score table, April 30
				//createFinal_zqian( functorArgs );
				
				// update the null value to 0, May 7th, zqian
				// June 30, 2014, some weights are positive? `intelligence(student0)`, double check
				createSubFinal2_zqian(functorId_In_BN,current_id1 ); // using natural right join
				long ctt5 = System.currentTimeMillis();
				System.out.println( "\n create final tables for each final_CT, run time is: " + ( ctt5 - ctt14 ) + "ms. \n ******************************** \n\n" );
			
				//createFinal2_zqian( functorArgs ); // set null to 0
				createFinal3_zqian( CurrendID_List ); // set null to -50
				long ctt6 = System.currentTimeMillis();
				System.out.println( "\n create Score tables , run time is: " + ( ctt6 - ctt5 ) + "ms. \n ******************************** \n\n" );
				
				/* prepare for the performance analysis, July 9th 2014, zqian
				 * to do: 
				 * 1. extract the primary keys, target node from @database@final_CT.target_CT into new table target_True
				 * 		i.e. extract `student_id(student0)`,`intelligence(student0)` from unielwin_traning1_final_CT.`intelligence(student0)_CT`
				 *      	to create table unielwin_traning1_final_CT.`intelligence(student0)_True`
				 * 2. do the natural join 
				 * 		with @database@final.target_Score ?
				 * */
				/*may Not always be consistent, some could have duplicated value, Sep. 29, 2014 */
				//Extract_Target_True_CT_zqian( functorId_In_BN, current_id1  );
				
			}
			// Cleanup for next functor
			st.execute( "DROP TABLE 1Nodes;" );
			st.execute( "DROP TABLE 2Nodes;" );
			st.execute( "DROP TABLE RNodes;" );
		}
		
		st.close();		
		disconnectDBTargetFinalCT();
		disconnectDB();
		disconnectDBFinal();
		disconnectDB_preprocess();	
		long time2=System.currentTimeMillis();
		
		System.out.println( "Total TestWrapper run time: " + ( time2 - time1 ) +"ms." );
		
		
		
	}
	
	public static String  ProcessTargetParent(Statement st,Statement st1, ArrayList<String> functors,String node,String table, int i  ) throws Exception {
		/* Create Markov Blanket in @database@_BN for each functor, keep consistency */
		MarkovBlanket.runMakeMarkovBlanket();
		functorId = functors.get(i);
		/* Store functorId in @database@_BN  */
		/*so why didn't you just pass a functorID? Vidhi June 16 2017 */
		 functorId_In_BN=functorId;
		// Get pvars for functor, e.g. student0_counts */
		ArrayList<String> functorArgs = GetFunctorArgs( functorId );
		/* Store the associated primary keys for each functor: e.g. course_id(course0)  */
		//ArrayList<String> 
		CurrendID_List = new ArrayList<String>();
		System.out.println("\n**********\nFunctor: '"+functorId+"'");
		
		/* @database@_target_setup*/			
		st.execute( "DROP TABLE IF EXISTS 1Nodes,2Nodes,RNodes;" );
		// 1Nodes
		System.out.println( "setup 1node: \n CREATE TABLE  if not exists 1Nodes AS SELECT * FROM " + databaseName + "_BN.1Nodes WHERE 1nid IN (SELECT " +
			    node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or 1nid='" +   functorId + "';" );
		st.execute( "CREATE TABLE  if not exists 1Nodes AS SELECT * FROM " + databaseName + "_BN.1Nodes WHERE 1nid IN (SELECT " +
				    node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or 1nid='" +   functorId + "';" );
		
		
		// 2Nodes 
		System.out.println( "CREATE TABLE  if not exists 2Nodes AS SELECT * FROM " + databaseName + "_BN.2Nodes WHERE 2nid IN (SELECT " +
			    node + " FROM " + databaseName + "_BN." + table +   " WHERE TargetNode = '" + functorId + "') or 2nid='" +  functorId + "';" );
		st.execute( "CREATE TABLE  if not exists 2Nodes AS SELECT * FROM " + databaseName + "_BN.2Nodes WHERE 2nid IN (SELECT " +
				    node + " FROM " + databaseName + "_BN." + table +   " WHERE TargetNode = '" + functorId + "') or 2nid='" +  functorId + "';" );
		
		// RNodes 
		System.out.println( "CREATE TABLE  if not exists RNodes AS SELECT * FROM " +  databaseName + "_BN.RNodes WHERE rnid IN (SELECT " +
				node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or rnid " + 
			    "IN (SELECT rnid FROM " + databaseName + "_BN.RNodes_2Nodes WHERE 2nid IN (SELECT  2nid FROM " +
			    databaseName + "_target_setup.2Nodes)) or rnid='" + functorId + "';" );
		st.execute( "CREATE TABLE  if not exists RNodes AS SELECT * FROM " +  databaseName + "_BN.RNodes WHERE rnid IN (SELECT " +
					node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or rnid " + 
				    "IN (SELECT rnid FROM " + databaseName + "_BN.RNodes_2Nodes WHERE 2nid IN (SELECT  2nid FROM " +
				    databaseName + "_target_setup.2Nodes)) or rnid='" + functorId + "';" );
		
		/* Store functor for fast grounding processing  */
		st.execute( "DROP TABLE IF EXISTS Test_Node;" );			 
		st.execute( "CREATE TABLE `Test_Node` (  `FID` varchar(199) NOT NULL, PRIMARY KEY (`FID`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
		st.execute(" INSERT INTO `Test_Node` (`FID`) VALUES('"+functorId+"') ;");
		long ctt1 = System.currentTimeMillis(); 
		System.out.println( "\n\n*****\nEntering BayesBaseCT_SortMerge.buildCTTarget() for target and its parents ");
	//	int max = BayesBaseCT_SortMerge.buildCTTarget();
		
		// to do : generate local ct based on the associated columns, Aug. 8th, 2014, zqian
		int max = BayesBaseCT_SortMerge.buildSubCTTarget(functorId,databaseName+"_BN",databaseName1,databaseName1+"_BN",databaseName6);
		/*the main hard work */
		
		long ctt2 = System.currentTimeMillis();
		System.out.println( "\nBayesBaseCT_SortMerge.buildCTTarget() for target and its parents run time is: " + ( ctt2 - ctt1 ) + "ms. \n ******************************** \n\n" );
		
		/* replace rnid with orig_rnid, be consistent with ct and cp tables for weight learning, June 24, 2014 zqian */
		st1.execute("update `TargetParents`,`RNodes` set  `TargetParents`.TargetNode = `RNodes`.orig_rnid where `TargetParents`.TargetNode = `RNodes`.rnid; ");
		st1.execute("update `TargetParents`,`RNodes` set  `TargetParents`.TargetParent = `RNodes`.orig_rnid where `TargetParents`.TargetParent = `RNodes`.rnid; ");
		st1.execute("update `TargetChildren`, `RNodes` set  `TargetChildren`.TargetNode = `RNodes`.orig_rnid where `TargetChildren`.TargetNode = `RNodes`.rnid; ");
		st1.execute("update `TargetChildren`, `RNodes` set  `TargetChildren`.TargetChild = `RNodes`.orig_rnid where `TargetChildren`.TargetChild = `RNodes`.rnid; ");
		st1.execute("update `TargetChildrensParents`, `RNodes` set  `TargetChildrensParents`.TargetNode = `RNodes`.orig_rnid where `TargetChildrensParents`.TargetNode = `RNodes`.rnid; ");
		st1.execute("update `TargetChildrensParents`, `RNodes` set  `TargetChildrensParents`.TargetChildParent = `RNodes`.orig_rnid where `TargetChildrensParents`.TargetChildParent = `RNodes`.rnid; ");
		st1.execute("update `TargetMB`, `RNodes` set  `TargetMB`.TargetNode = `RNodes`.orig_rnid where `TargetMB`.TargetNode = `RNodes`.rnid;  ");
		st1.execute("update `TargetMB`, `RNodes` set  `TargetMB`.TargetMBNode = `RNodes`.orig_rnid where `TargetMB`.TargetMBNode = `RNodes`.rnid; ");

		/* find the corresponding ct tables in @database@_target_ct database*/
		String rchainQuery = "SELECT name FROM " + databaseName1 + "_BN.lattice_set WHERE length = " + max + ";"; //@database@_target
		ResultSet rsRchain = st.executeQuery( rchainQuery );
		String rchain = "";
		boolean rchainExists = false;			
		if ( rsRchain.absolute(1)){
			rchainExists = true;
			rchain = rsRchain.getString(1);
		}			
		rsRchain.close();
		// make rnid be consistent between the training and testing/target database 
		if ( rchainExists ){   
			System.out.println( "biggest Rchain in testing database : "+rchain +" for Functor: '"+functorId+"'");
			ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `b`				
			if ( rNodeName_t.absolute( 1 ) ){
				functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
			}				
			rNodeName_t.close();
			
			/*copy table from @database@_target_ct to @database@_target_final_ct*/
			st.execute("DROP TABLE IF EXISTS "+ databaseName3 +".`"+ functorId.replace("`","")+"_CT` ;"); //@database@_target_final_ct
			//System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "+ databaseName4+".`"+rchain.replace("`","")+"_CT`;" );
//			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as "
//					+ " select * from "+ databaseName4+".`"+rchain.replace("`","")+"_CT`;" ); //@database@_target_ct
			//also copy the index, July 17,2014, zqian
//			System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
//			System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
//			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
//			st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`"+rchain.replace("`","")+"_CT`;" ); //@database@_target_ct
//		
			System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
			System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" );
			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
			st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" ); //@database@_target_ct
						
			/* get the mapping of RNodes in @database@_target_BN */
			ResultSet rNodes = st.executeQuery( "SELECT orig_rnid, rnid FROM  " + databaseName1 + "_BN.RNodes;" ); //@database@_target
			ArrayList<String> rnids = new ArrayList<String>(); // `a`
			ArrayList<String> origrnids = new ArrayList<String>();	//`RA(pfrof0,student0)`	
			while ( rNodes.next() )	{
				origrnids.add( rNodes.getString(1));
				rnids.add( rNodes.getString(2));
			}		
			rNodes.close();
			for ( int j = 0; j < rnids.size(); j++ ) {
				System.out.println( "SHOW COLUMNS FROM " + databaseName3 + ".`" +functorId.replace("`","")+"_CT` WHERE Field = '" 
						+rnids.get(j).replace("`", "") + "';" );
				ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " + databaseName3 + ".`" +functorId.replace("`","")+"_CT` WHERE Field = '" 
						+rnids.get(j).replace("`", "") + "';" );
				if ( !rsTypes.absolute(1) ) {//zqian, no rnode in the header, so do not need to replace
					System.out.println( "NO Need to do the mapping for table `" +functorId.replace("`","")+"_CT` ;" );
					rsTypes.close();
					continue;
				}
				String type = rsTypes.getString(2);
				rsTypes.close();
				System.out.println( "ALTER TABLE " +  databaseName3 + ".`" +functorId.replace("`","")+"_CT` CHANGE COLUMN " 
						+	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
				st.execute( "ALTER TABLE " +  databaseName3 + ".`" +functorId.replace("`","")+"_CT` CHANGE COLUMN " 
						+	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
			}
			
		}
		else{
			System.out.println( "NO Rchain for Functor: '"+functorId+"'");
			//copy _counts table from @database@_target_ct to @database@_target_final_ct
			st.execute("DROP TABLE IF EXISTS "+ databaseName3 +".`"+ functorId.replace("`","")+"_CT` ;"); //@database@_target_final_ct
//			System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "
//					+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "
//					+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " 	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			System.out.println(" insert " + databaseName3 +".`"+ functorId.replace("`","")+"_CT`  select * from "	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " 	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			st.execute("insert " + databaseName3 +".`"+ functorId.replace("`","")+"_CT`  select * from "	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//		
			System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
			System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" );
			st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
			st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" ); //@database@_target_ct
		
			
		}
		
		// Aug. 20, compute the subCTs for target and its parents
		
		/*********	processing target's parents***************Begin****************/			
		// June 24 2014
		String current_id="";
		current_id1="";
		String target_common_select_string ="";
		//should contain, current and its parents
		String target_parent_select_string= "";
		int target_primaykey_counts=0;
		String target_primaykey_counts_query ="select count(distinct 1nid) from "+databaseName1+"_BN.Test_1nid ;";  //functorId in unielwin_target_BN
		//System.out.println( "target_primaykey_counts_query : "+ target_primaykey_counts_query );
		Statement st_temp = con_preprocess.createStatement();
		ResultSet rstarget_primaykey_counts = st_temp.executeQuery( target_primaykey_counts_query );	
		if (  rstarget_primaykey_counts.absolute( 1 ) )	{
			target_primaykey_counts = rstarget_primaykey_counts.getInt(1);  // counts 
		}
		rstarget_primaykey_counts.close();	
		System.out.println( " target_primaykey_counts : "+ target_primaykey_counts +" : " );
		String target_primaykey_query ="select distinct 1nid from "+databaseName1+"_BN.Test_1nid ;";  //`course_id(course0)`
		//System.out.println( "zqian : "+ target_primaykey_query );
		Statement st_te = con_preprocess.createStatement();
		ResultSet rstarget_primaykey = st_te.executeQuery( target_primaykey_query );	
		while ( rstarget_primaykey.next() && target_primaykey_counts !=0 )	{  
			current_id= rstarget_primaykey.getString(1);
			System.out.print(current_id);
			CurrendID_List.add(rstarget_primaykey.getString(1));
			current_id1 += current_id+ " ,"; // all the primary keys
			target_parent_select_string += current_id + " ,";
		}
		System.out.println();
		target_parent_select_string += " mult, "+ functorId + " ,";
		target_common_select_string  = target_parent_select_string;
		
		//concat each parent of the target 
		int target_parents_counts=0;
		String target_parents_counts_query ="SELECT count(TargetParent) FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+functorId+"' ;";  //functorId in unielwin_BN
		//System.out.println( "zqian : "+ target_parents_counts_query );
		Statement st_temp1 = con0.createStatement();
		ResultSet rstarget_parents_counts = st_temp1.executeQuery( target_parents_counts_query );	
		if (  rstarget_parents_counts.absolute( 1 ) ){
			target_parents_counts = rstarget_parents_counts.getInt(1);  // counts 
		}
		rstarget_parents_counts.close();	
		System.out.println( " target_parents_counts: "+ target_parents_counts );
		String target_parents_query ="SELECT TargetParent FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+functorId+"' ;";  //functorId in unielwin_BN
		//System.out.println( "zqian : "+ target_parents_query );
		Statement st_tem = con0.createStatement();
		ResultSet rstarget_parents = st_tem.executeQuery( target_parents_query );	
		// zqian: concat each parent of the target 
		while ( rstarget_parents.next() && target_parents_counts !=0 ){  
			String current= rstarget_parents.getString(1);
			//System.out.println("zqian: "+current+";");
			target_parent_select_string += current + " ,";
		}
		// for some target node that does not have any parents	
		if (target_parents_counts == 0){	
			System.out.println("zqian: NO parents for target "+ functorId   );
			//target_parent_select_string = "";
		}	
		String create_string = "create table "+databaseName3+".`"+functorId.replace("`", "")+"_parent_final_CT` as  select "
										+ target_parent_select_string.substring(0, target_parent_select_string.lastIndexOf(",")-1) 
													+ " From "+databaseName3+".`"+functorId.replace("`", "")+"_CT` ;" ;
		System.out.println (" create_target_parent_final_ct_string:  "+ create_string );
		st.execute("Drop table if exists "+databaseName3+".`"+functorId.replace("`", "")+"_parent_final_CT` ;" );
		st.execute(create_string );
		/*********	processing target's parents******************END*************/	
		long ctt3 = System.currentTimeMillis();
		System.out.println( "\n extract the target_parent_ct, run time is: " + ( ctt3 - ctt2 ) + "ms. \n ******************************** \n\n" );
			
		return target_common_select_string;
			
	}
	
	public static void ProcessTargetChildren( Statement st,Statement st1, ArrayList<String> functors,String node,String table, int i,String target_common_select_string  ) throws Exception {
		/* Create Markov Blanket in @database@_BN for each functor, keep consistency */
		MarkovBlanket.runMakeMarkovBlanket();
		/*********  processing target's children*****************Begin**************/	
		//--if target is rnode then have to rule out the associated 2node --// April 25			
		String target_children ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId_In_BN+"' "
									+ " and (TargetChild) not in ( SELECT 2nid FROM  "
											+ databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId_In_BN+"' );";  //functorId in unielwin_BN
		System.out.println( "zqian : "+ target_children );
		Statement s_temp = con0.createStatement();
		ResultSet rstarget_children = s_temp.executeQuery( target_children );
		// begin while 1
		while ( rstarget_children.next() ){
			String current= rstarget_children.getString(1);
			String current_In_BN=current;
			System.out.println(" Target Child  : "+current+" current_In_BN : "+ current_In_BN);
			ResultSet rNodeName_tt = st.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	current + "';" ); // `b`				
			if ( rNodeName_tt.absolute( 1 ) ){
				current = rNodeName_tt.getString(1); //`registration(course0,student0)`
			}				
			rNodeName_tt.close();
			System.out.println(" Target Child  : "+current+" current_In_BN : "+ current_In_BN);
			/* Create Markov Blanket in @database@_BN for each functor, keep consistency */
			MarkovBlanket.runMakeMarkovBlanket();
			functorId = functors.get(i);
			/* Store functorId in @database@_BN  */
			functorId_In_BN=functorId;
			// Get pvars for functor, e.g. student0_counts */
			ArrayList<String> functorArgs = GetFunctorArgs( functorId );
			/* Store the associated primary keys for each functor: e.g. course_id(course0)  */
			//ArrayList<String> CurrendID_List = new ArrayList<String>();
			//CurrendID_List = new ArrayList<String>();
			System.out.println("\n**********\nFunctor: '"+functorId+"'");
			
			/* @database@_target_setup*/			
			st.execute( "DROP TABLE IF EXISTS 1Nodes,2Nodes,RNodes;" );
			// 1Nodes
			System.out.println( "setup 1node: \n CREATE TABLE  if not exists 1Nodes AS SELECT * FROM " + databaseName + "_BN.1Nodes WHERE 1nid IN (SELECT " +
				    node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or 1nid='" +   functorId + "';" );
			st.execute( "CREATE TABLE  if not exists 1Nodes AS SELECT * FROM " + databaseName + "_BN.1Nodes WHERE 1nid IN (SELECT " +
					    node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or 1nid='" +   functorId + "';" );
			
			
			// 2Nodes 
			System.out.println( "CREATE TABLE  if not exists 2Nodes AS SELECT * FROM " + databaseName + "_BN.2Nodes WHERE 2nid IN (SELECT " +
				    node + " FROM " + databaseName + "_BN." + table +   " WHERE TargetNode = '" + functorId + "') or 2nid='" +  functorId + "';" );
			st.execute( "CREATE TABLE  if not exists 2Nodes AS SELECT * FROM " + databaseName + "_BN.2Nodes WHERE 2nid IN (SELECT " +
					    node + " FROM " + databaseName + "_BN." + table +   " WHERE TargetNode = '" + functorId + "') or 2nid='" +  functorId + "';" );
			
			// RNodes 
			System.out.println( "CREATE TABLE  if not exists RNodes AS SELECT * FROM " +  databaseName + "_BN.RNodes WHERE rnid IN (SELECT " +
					node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or rnid " + 
				    "IN (SELECT rnid FROM " + databaseName + "_BN.RNodes_2Nodes WHERE 2nid IN (SELECT  2nid FROM " +
				    databaseName + "_target_setup.2Nodes)) or rnid='" + functorId + "';" );
			st.execute( "CREATE TABLE  if not exists RNodes AS SELECT * FROM " +  databaseName + "_BN.RNodes WHERE rnid IN (SELECT " +
						node + " FROM " + databaseName + "_BN." + table +  " WHERE TargetNode = '" + functorId + "') or rnid " + 
					    "IN (SELECT rnid FROM " + databaseName + "_BN.RNodes_2Nodes WHERE 2nid IN (SELECT  2nid FROM " +
					    databaseName + "_target_setup.2Nodes)) or rnid='" + functorId + "';" );
			
			/* Store functor for fast grounding processing  */
			st.execute( "DROP TABLE IF EXISTS Test_Node;" );			 
			st.execute( "CREATE TABLE `Test_Node` (  `FID` varchar(199) NOT NULL, PRIMARY KEY (`FID`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
			st.execute(" INSERT INTO `Test_Node` (`FID`) VALUES('"+functorId+"') ;");
			long ctt1 = System.currentTimeMillis(); 
			System.out.println( "\n\n*****\nEntering BayesBaseCT_SortMerge.buildCTTarget() for child ");
		//	int max = BayesBaseCT_SortMerge.buildCTTarget();
			
			// to do : generate local ct based on the associated columns, Aug. 8th, 2014, zqian
			int max = BayesBaseCT_SortMerge.buildSubCTTarget(functorId,databaseName+"_BN",databaseName1,databaseName1+"_BN",databaseName6,current_In_BN,current);
			
			long ctt2 = System.currentTimeMillis();
			System.out.println( "\nBayesBaseCT_SortMerge.buildCTTarget()  run time is: " + ( ctt2 - ctt1 ) + "ms. \n ******************************** \n\n" );
			
			// replace rnid with orig_rnid, be consistent with ct and cp tables for weight learning, June 24, 2014 zqian 
			st1.execute("update `TargetParents`,`RNodes` set  `TargetParents`.TargetNode = `RNodes`.orig_rnid where `TargetParents`.TargetNode = `RNodes`.rnid; ");
			st1.execute("update `TargetParents`,`RNodes` set  `TargetParents`.TargetParent = `RNodes`.orig_rnid where `TargetParents`.TargetParent = `RNodes`.rnid; ");
			st1.execute("update `TargetChildren`, `RNodes` set  `TargetChildren`.TargetNode = `RNodes`.orig_rnid where `TargetChildren`.TargetNode = `RNodes`.rnid; ");
			st1.execute("update `TargetChildren`, `RNodes` set  `TargetChildren`.TargetChild = `RNodes`.orig_rnid where `TargetChildren`.TargetChild = `RNodes`.rnid; ");
			st1.execute("update `TargetChildrensParents`, `RNodes` set  `TargetChildrensParents`.TargetNode = `RNodes`.orig_rnid where `TargetChildrensParents`.TargetNode = `RNodes`.rnid; ");
			st1.execute("update `TargetChildrensParents`, `RNodes` set  `TargetChildrensParents`.TargetChildParent = `RNodes`.orig_rnid where `TargetChildrensParents`.TargetChildParent = `RNodes`.rnid; ");
			st1.execute("update `TargetMB`, `RNodes` set  `TargetMB`.TargetNode = `RNodes`.orig_rnid where `TargetMB`.TargetNode = `RNodes`.rnid;  ");
			st1.execute("update `TargetMB`, `RNodes` set  `TargetMB`.TargetMBNode = `RNodes`.orig_rnid where `TargetMB`.TargetMBNode = `RNodes`.rnid; ");

			/* find the corresponding ct tables in @database@_target_ct database*/
			String rchainQuery = "SELECT name FROM " + databaseName1 + "_BN.lattice_set WHERE length = " + max + ";"; //@database@_target
			ResultSet rsRchain = st.executeQuery( rchainQuery );
			String rchain = "";
			boolean rchainExists = false;			
			if ( rsRchain.absolute(1)){
				rchainExists = true;
				rchain = rsRchain.getString(1);
			}			
			rsRchain.close();
			
			
			// make rnid be consistent between the training and testing/target database 
			if ( rchainExists ){   
					System.out.println( "biggest Rchain in testing database : "+rchain +" for Functor: '"+functorId+"'");
					ResultSet rNodeName_t = st.executeQuery( "SELECT orig_rnid FROM " + databaseName +"_BN.RNodes WHERE rnid = '" +	functorId + "';" ); // `b`				
					if ( rNodeName_t.absolute( 1 ) ){
						functorId = rNodeName_t.getString(1); //`registration(course0,student0)`
					}				
					rNodeName_t.close();
					
					/*copy table from @database@_target_ct to @database@_target_final_ct*/
					st.execute("DROP TABLE IF EXISTS "+ databaseName3 +".`"+ functorId.replace("`","")+"_CT` ;"); //@database@_target_final_ct
					//System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "+ databaseName4+".`"+rchain.replace("`","")+"_CT`;" );
	//				st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as "
	//						+ " select * from "+ databaseName4+".`"+rchain.replace("`","")+"_CT`;" ); //@database@_target_ct
					//also copy the index, July 17,2014, zqian
	//				System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
	//				System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
	//				st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`"+rchain.replace("`","")+"_CT`;" );
	//				st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`"+rchain.replace("`","")+"_CT`;" ); //@database@_target_ct
	//			
					System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
					System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" );
					st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
					st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" ); //@database@_target_ct
					
					/* get the mapping of RNodes in @database@_target_BN */
					ResultSet rNodes = st.executeQuery( "SELECT orig_rnid, rnid FROM  " + databaseName1 + "_BN.RNodes;" ); //@database@_target
					ArrayList<String> rnids = new ArrayList<String>(); // `a`
					ArrayList<String> origrnids = new ArrayList<String>();	//`RA(pfrof0,student0)`	
					while ( rNodes.next() )	{
						origrnids.add( rNodes.getString(1));
						rnids.add( rNodes.getString(2));
					}		
					rNodes.close();
					for ( int j = 0; j < rnids.size(); j++ ) {
						System.out.println( "SHOW COLUMNS FROM " + databaseName3 + ".`" +functorId.replace("`","")+"_CT` WHERE Field = '" 
								+rnids.get(j).replace("`", "") + "';" );
						ResultSet rsTypes = st.executeQuery( "SHOW COLUMNS FROM " + databaseName3 + ".`" +functorId.replace("`","")+"_CT` WHERE Field = '" 
								+rnids.get(j).replace("`", "") + "';" );
						if ( !rsTypes.absolute(1) ) {//zqian, no rnode in the header, so do not need to replace
							System.out.println( "NO Need to do the mapping for table `" +functorId.replace("`","")+"_CT` ;" );
							rsTypes.close();
							continue;
						}
						String type = rsTypes.getString(2);
						rsTypes.close();
						System.out.println( "ALTER TABLE " +  databaseName3 + ".`" +functorId.replace("`","")+"_CT` CHANGE COLUMN " 
								+	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
						st.execute( "ALTER TABLE " +  databaseName3 + ".`" +functorId.replace("`","")+"_CT` CHANGE COLUMN " 
								+	rnids.get(j) + " " + origrnids.get(j) + " " + type + ";" );
					}
					
					System.out.println( "zqian :target_common_select_string  "+ target_common_select_string );
					
					String current_parents_select_string = target_common_select_string + current +" ,";
					/*String current_parents_query ="SELECT TargetParent FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+current+"' "
														+ " and ( TargetParent ) NOT IN (select '"+functorId +"' );";*/  
					
					String current_parents_query ="SELECT TargetParent FROM "+databaseName+"_BN.TargetParents where TargetNode ='"+current+"' "
							+ " and ( TargetParent ) NOT IN (select '"+functorId +"' );";  
					System.out.println( "zqian : "+ current_parents_query );
					Statement st_t = con0.createStatement();
					ResultSet rscurrent_parents = st_t.executeQuery( current_parents_query );
					// begin while 2
					while (rscurrent_parents.next()){
						String sub_current= rscurrent_parents.getString(1);
						System.out.println(" parent of TargetChild:  "+sub_current+";");
						current_parents_select_string += sub_current + " ,";					
					}// end while 2
					// throw the exception Oct. 2nd. 2014
					try {
						st.execute("Drop table if exists "+databaseName3+".`"+functorId.replace("`", "")+"_"+current.replace("`", "")+"_final_CT` ;" );
						String create_string1 = "create table "+databaseName3+".`"+functorId.replace("`", "")+"_"+current.replace("`", "")+"_final_CT` as  select "
												+ current_parents_select_string.substring(0, current_parents_select_string.lastIndexOf(",")-1) 
														+ " From "+databaseName3+".`"+functorId.replace("`", "")+"_CT` ;" ;
						System.out.println (" create_target_child_final_ct_string:  "+create_string1 +"\n");
						st.execute(create_string1 );
					}
					catch(MySQLSyntaxErrorException e) {
						tableNameIsTooLong=true;
						System.out.println("Oops, the following table name is too long: "+ functorId.replace("`", "")+"_"+current.replace("`", "")+"_final_CT" );
					}
					
				
					
			}
			else{
				System.out.println( "NO Rchain for Functor: '"+functorId+"'");
				//copy _counts table from @database@_target_ct to @database@_target_final_ct
				st.execute("DROP TABLE IF EXISTS "+ databaseName3 +".`"+ functorId.replace("`","")+"_CT` ;"); //@database@_target_final_ct
//				System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "
//						+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//				st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` as select * from "
//						+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//				System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " 	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//				System.out.println(" insert " + databaseName3 +".`"+ functorId.replace("`","")+"_CT`  select * from "	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//				st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " 	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//				st.execute("insert " + databaseName3 +".`"+ functorId.replace("`","")+"_CT`  select * from "	+ databaseName4+".`"+functorArgs.get(0).replace("`","")+"_counts`;" );
//			
				System.out.println( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
				System.out.println( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" );
				st.execute( "CREATE TABLE " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` like " + databaseName6+".`local_CT`;" );
				st.execute( "insert  " + databaseName3 +".`"+ functorId.replace("`","")+"_CT` select * from "+ databaseName6+".`local_CT`;" ); //@database@_target_ct
			
				
			}
			
			
			
		}
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
		databaseName6	= databaseName1 + "_db";
		dbUsername		= conf.getProperty("dbusername");
		dbPassword		= conf.getProperty("dbpassword");
		dbaddress		= conf.getProperty("dbaddress");
		//Sep 11, 2014,zqian
				String temp = conf.getProperty("CrossValidation");
				if ( temp.equals( "1" ) )
				{
					CrossValidation = true;
				}
				else
				{
					CrossValidation = false;
				}
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
		
		//System.out.println( "Getting functors for d_gender..." );
	ResultSet rs = st1.executeQuery( "SELECT distinct Fid from FNodes order by Type ;" );
				//+ "    where Fid ='`runningtime(movies0)`';" ); 
//		ResultSet rs = st1.executeQuery( "SELECT distinct Fid from " + "FNodes "    
//		+ " where Type = 'Rnode'  order by Type ;" ); //processing 1node first, Aug. 26
		// ignore Rnode for Financial dataset , Aug.6th 	
		
		//only processing the target nodes with binary values, May 1st,Sep 9th, 2014
		/* rs = st1.executeQuery( "SELECT distinct Fid from  FNodes "
				+ " where FunctorName in "
				+ "( select column_name from "
				+ "  (SELECT count(*) as Number , column_name  FROM Attribute_Value group by column_name) C "
				+ "  where C.Number =2"
				+ ") "
				+ " and Type = '1Node' and main ='1' ;" ); // processing 1Node first, only the main functor, May 6th
*/				
				//+ " and Type = 'Rnode' and main ='1' ;" ); // try Rnode , May 8th
//		 // processing Gender in MovieLens_std, May 12
/*		ResultSet rs = st1.executeQuery( "SELECT distinct Fid from  FNodes "
				+ " where FunctorName = 'd_gender' ;" ); 
	*/	
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
	
	/*Extend the final_CT tables for each functorId with all possible values, June 30, 2014, zqian */
	public static void Extend_Final_CT(  ) throws SQLException
	{

		System.out.println( "Extend the final_CT tables for : "+functorId  );
		Statement st = conFinal.createStatement();
		Statement st1 = conFinal.createStatement();
		String functorId_t = functorId; // `registration(course0,student0)`
		String functorId_temp = functorId; 

		//check if current is Rnode or not, do the mapping again
		ResultSet r_t = st.executeQuery(  "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" + functorId_t + "';" ); // `registration(course0,student0)`
		if ( r_t.absolute( 1 ) ){
					//System.out.println("functorId_temp: "+r_t.getString(1)+";");
					functorId_temp = r_t.getString(1);  //`b`
		}				
		r_t.close();
		
		
		ResultSet temp_ct_tables = st1.executeQuery( "show tables from "+  databaseName3 + "  like '"+functorId_t.replace("`","")+"%_final_CT' ;");
		while ( temp_ct_tables.next() )	{
			String current_ct = temp_ct_tables.getString(1);
			//System.out.println("ALTER TABLE "+databaseName3+ ".`"+current_ct+"` DROP COLUMN `"+functorId_t.replace("`","")+"`;");
		// Sep ,12 ,test
			if (CrossValidation){
				st.execute("ALTER TABLE "+databaseName3+ ".`"+current_ct+"` DROP COLUMN `"+functorId_t.replace("`","")+"` ;");
			}
			String create_string = " CREATE TABLE "+databaseName3+ ".`"+current_ct+"_e` as select " ; // table limits to 64 char,  ??? need to fix this
			//System.out.println("show columns from  "+  databaseName3 + ".`"+current_ct+"` ;");
			ResultSet temp_ct_columns = st.executeQuery( "show columns from  "+  databaseName3 + ".`"+current_ct+"` ;");
			String index_string ="ALTER TABLE "+databaseName3+ ".`"+current_ct+"` ADD INDEX  ( ";
			while (temp_ct_columns.next()){
				create_string += "`"+temp_ct_columns.getString(1)+"`" + " ,";
				index_string +=  "`"+temp_ct_columns.getString(1)+"`" + " ,";
			}
			index_string = index_string.substring(0, index_string.lastIndexOf(",")-1) + " ) ;"; 
			
			if (CrossValidation){
				create_string += "value as `"+functorId_t.replace("`","")+"` From " +databaseName3+ ".`"+current_ct+"` , " +databaseName+"_BN.Attribute_Value "
					+ "where column_name = (SELECT FunctorName FROM " +databaseName+"_BN.FNodes where Fid = '`"+functorId_temp.replace("`","")+"`' ) ;";
			}
			// Sep ,12 ,test
			if (!CrossValidation) 
			{
				create_string =create_string.substring(0, create_string.lastIndexOf(",")-1)+ " From " +databaseName3+ ".`"+current_ct+"` ; " ;
			}
			System.out.println("index_string :" + index_string);
			st.execute(index_string);
			System.out.println(create_string);
			st.execute(create_string);
			//temp_ct_columns.close();
		}
		//temp_ct_tables.close();
		
		System.out.println("Left Extend_Final_CT : "+functorId);	
		
		st.close();
		st1.close();
	}
	
	//for 1node and 2node, set the mult to 0 given false relationships, compute frequencies, May 2nd
	public static void Update_CT(  ArrayList<String> CurrendID_List, String current_id  ) throws SQLException
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
	
				String WhereString ="";
				String index_string = "";
				int numArgs = CurrendID_List.size();
				System.out.println(" CurrendID_List.size() "+ CurrendID_List.size());
				for ( int i = 0; i < numArgs; i++ )   
				{
					WhereString += "`"+current_ct.replace("`","")+"`."+CurrendID_List.get( i ) +" = temp."+ CurrendID_List.get( i ) +" and ";
					index_string += CurrendID_List.get( i ) +" ,";					
				}	
				System.out.print (" index_string "+ WhereString);
				index_string = index_string.substring(0, index_string.lastIndexOf(",")-1) + " "; 

				
				st3.execute("ALTER TABLE `" +current_ct.replace("`","")+"` ADD COLUMN `sum_mult` BIGINT(21) NULL DEFAULT 0 AFTER `"+functorId_temp.replace("`","") +"` ;");
				st3.execute("drop table if exists temp; ");
				// May 5th, the sum_mult should equal to sum(mult), since we did the natural join for the binary nodes
				//updated on Aug.13 2014, computing the sum_mult based on the primary keys
				System.out.println("index_string : "+ index_string);
				st3.execute("alter table `"+current_ct.replace("`","")+"` add index ( "+index_string + " ) ;");
				//Sep. 22, 2014, fix the bug some value of freq. greater than 1
				if (CrossValidation){
					st3.execute("create table temp as SELECT "+ current_id+ "  sum(`mult`)/2 as sum_mult  FROM `"+current_ct.replace("`","")+"`  group by "+ current_id.substring(0,  current_id.lastIndexOf(",")-1)+"   ;");
				}
				else
					st3.execute("create table temp as SELECT "+ current_id+ "  sum(`mult`) as sum_mult  FROM `"+current_ct.replace("`","")+"`  group by "+ current_id.substring(0,  current_id.lastIndexOf(",")-1)+"   ;");
				// to do: add index for id list?
				st3.execute("alter table temp add index ( "+index_string + " ) ;");
				st3.execute("update  `"+current_ct.replace("`","")+"`, temp set `"+current_ct.replace("`","")+"`.sum_mult = temp.sum_mult "
						+ "where "+ WhereString.substring(0,  WhereString.lastIndexOf("and")-1)+ " ;");
				
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
	public static void createSubFinal2_zqian( String functorId_In_BN, String current_id  )	{
		//functorId = "`a`";
		System.out.println("\nzqian: come to createSubFinal2_zqian + functorId: "+functorId);	
		String functorId_t=functorId; //store the funcorId in unielwin_target_BN
		String IndexString="";

		try	{
			String actualNodeName = "";
			Statement st = null;			
			st = conFinal.createStatement();
			actualNodeName =functorId;
		// for target_parent_final
			// target_cp, target_parent_final_CT	
			String createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_parent_final` "
					+ " AS SELECT " + current_id+ "`" +actualNodeName.replace( "`" , "" )+"`, ";				
			IndexString = current_id+ "`" +actualNodeName.replace( "`" , "" )+"`";
//			createTable += "SUM(mult * log(" + databaseName + "_BN.`" +  //zqian // sum[mult*log(cp/prior) +log(prior)], June 30, 2014
//					functorId_In_BN.replace( "`" , "" ) + "_CP`.CP) " 
//					+"- mult * log(" + databaseName + "_BN.`" +  functorId_In_BN.replace( "`" , "" ) + "_CP`.prior) " 
//					+"+ log("+ databaseName + "_BN.`" +functorId_In_BN.replace( "`" , "" ) + "_CP`.prior) )" + 
//					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
//					   functorId_In_BN.replace( "`" , "" ) + "_CP` natural right join  " + databaseName3 + ".`" + 
//						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";	
			 //zqian, freq*log(cp), ILP 2014
			createTable += "SUM(freq * log(" + databaseName + "_BN.`" +  
					functorId_In_BN.replace( "`" , "" ) + "_CP`.CP)  )" + 
					   "AS `weight_product"+"_parent` FROM " + databaseName + "_BN.`" +
					   functorId_In_BN.replace( "`" , "" ) + "_CP` natural right join  " + databaseName3 + ".`" + 
						  actualNodeName.replace("`", "")  +"_parent_final_CT_e` ";	

			createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, " + current_id ;
						
			System.out.println("zqian : "+ createTable.substring(0, createTable.lastIndexOf(",")-1)+" ;" );
			st.execute( createTable.substring(0, createTable.lastIndexOf(",")-1) + " ;" );
			//System.out.println("July 17: alter table `"+ actualNodeName.replace( "`" , "" ) +"_parent_final` add index `" + actualNodeName.replace( "`" , "" ) +"_parent_Index` ( " +IndexString + " );");
			st.execute("alter table `"+ actualNodeName.replace( "`" , "" ) +"_parent_final` add index `" + actualNodeName.replace( "`" , "" ) +"_parent_Index` ( " +IndexString + " );");
			
		// for target_child_final
			// child_cp, target_child_final_CT
			// String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' ;";  //functorId in unielwin_BN
			//--if target is rnode then have to rule out the associated 2node --// April 25			
			String target_child ="SELECT TargetChild FROM "+databaseName+"_BN.TargetChildren where TargetNode ='"+functorId+"' "
					+ " and (TargetChild) not in ( SELECT 2nid FROM  "+databaseName+"_BN.RNodes_2Nodes where rnid = '"+functorId_In_BN+"' );"; 
			
			System.out.println( "zqian : "+ target_child );
			Statement st_temp1 = con0.createStatement();
			ResultSet rstarget_child = st_temp1.executeQuery( target_child );
			while ( rstarget_child.next() )
			{  
				String current= rstarget_child.getString(1);
				String current_for_cp=current;
				System.out.println("zqian: "+current+";");
				String sub_createTable = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_final` "
						+ "AS SELECT " + current_id +" `" +actualNodeName.replace( "`" , "" )+"`, ";
				//check if current is Rnode or not, do the mapping again
				ResultSet r_t = st.executeQuery(  "SELECT rnid FROM " + databaseName +"_BN.RNodes WHERE orig_rnid = '" + current + "';" ); // `registration(course0,student0)`
				if ( r_t.absolute( 1 ) )
				{
					System.out.println("current_for_cp: "+r_t.getString(1)+";");
					current_for_cp = r_t.getString(1);  //`b`
				}				
				r_t.close();
//				sub_createTable += "SUM(mult * log(" + databaseName + "_BN.`" + current_for_cp.replace( "`" , "" ) + "_CP`.CP) "+  //zqian // sum[mult*log(cp/prior) +log(prior)], June 30, 2014
//									"- mult * log(" + databaseName + "_BN.`" + current_for_cp.replace( "`" , "" ) + "_CP`.prior) "+	
//									" +  log("+ databaseName + "_BN.`"+current_for_cp.replace( "`" , "" ) +"_CP`.prior) ) " + 
//											"AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
//													current_for_cp.replace( "`" , "" ) + "_CP` natural right join   " + databaseName3 + ".`" + 
//															actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
				 //zqian, freq*log(cp), ILP 2014
				sub_createTable += "SUM(freq * log(" + databaseName + "_BN.`" + current_for_cp.replace( "`" , "" ) + "_CP`.CP) ) " + 
						"AS `weight_product"+"_" +current.replace( "`" , "" )+ "` FROM " + databaseName + "_BN.`" +
								current_for_cp.replace( "`" , "" ) + "_CP` natural right join   " + databaseName3 + ".`" + 
										actualNodeName.replace("`", "")+"_" +current.replace( "`" , "" ) + "_final_CT_e` ";
				
				sub_createTable += " GROUP BY `" +actualNodeName.replace( "`" , "" )+"`, " + current_id ;;
				
			
				
				System.out.println("zqian : "+ sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1)+" ;" );
				st.execute( sub_createTable.substring(0, sub_createTable.lastIndexOf(",")-1) + " ;" );				
				//System.out.println("July 17 alter table `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_final` add index `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_Index` ( " +IndexString + " );");
				st.execute("alter table `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_final` add index `" + actualNodeName.replace( "`" , "" ) +"_" +current.replace( "`" , "" )+ "_Index` ( " +IndexString + " );");
			
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
		System.out.println("\nzqian: Leave createSubFinal2_zqian + functorId: "+functorId +"\n******************************** \n");	
	}
	 // set null to 0
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
	 // set null to -50
	public static void createFinal3_zqian( ArrayList<String> CurrendID_List  ){	
		System.out.println("zqian: come to createFinal3_zqian!");	
		try	{
			String actualNodeName = functorId;
			Statement st = null;			
			st = conFinal.createStatement();

			String target_size ="select count(*) FROM INFORMATION_SCHEMA.tables where table_schema =  '"+databaseName5+"'and table_name like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
			System.out.println( "zqian : "+ target_size );
			Statement st_temp1 = con0.createStatement();
			int size=0;
			ResultSet rstarget_size = st_temp1.executeQuery( target_size );
			if (  rstarget_size.absolute( 1 ) )	{
				//size = rstarget_size.getString(1);
				size =rstarget_size.getInt(1);
				System.out.println( "zqian : size  "+ size );
			}
			 rstarget_size.close();					
			 if (size >1)  {
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
						current= rstarget_showcolumns.getString(1);	// update the null value 
						select_string += " COALESCE( `"+current.replace( "`" , "")+"`." +"`weight_product_" +current.substring(current.indexOf("_")+1, current.lastIndexOf("_"))+"`, -50) +";
						from_string += databaseName5+".`"+ current + "` natural join ";

					}
					select_string =select_string.substring(0, select_string.lastIndexOf("+")-1)+ " ) as Score , "; // change to score
					from_string = from_string.substring(0, from_string.lastIndexOf("natural join")-1); //remove the last "natural join"
					
					String createTable_temp= "CREATE TABLE temp AS SELECT ";
					String createTable_temp_t= "CREATE TABLE temp_t AS SELECT ";
					String groupby_string=" group by ";
					String SubQueryWhereString= "";
					String IndexString="";
					String IndexString1="";
					int numArgs = CurrendID_List.size();				
					for ( int i = 0; i < numArgs; i++ )   
					{
						select_string += " `"+current.replace("`", "")+"`."+CurrendID_List.get( i ) + ", ";
						createTable_temp += CurrendID_List.get( i ) + ", ";
						createTable_temp_t +=  CurrendID_List.get( i ) + ", ";
						IndexString +=  CurrendID_List.get( i ) + ", ";
						groupby_string += CurrendID_List.get( i ) + ", ";
						SubQueryWhereString += "A."+CurrendID_List.get( i ) +" = B."+ CurrendID_List.get( i ) +" and ";
					}	
					
					IndexString1= IndexString.substring(0, IndexString.lastIndexOf(","));
					IndexString += "`"+actualNodeName.replace( "`" , "" ) +"`";
					select_string = select_string.substring(0, select_string.lastIndexOf(","));  // remove the last ","
					createTable += select_string + " from  " + from_string +" ;";  // remove the last ","
					//create the target_sum table
					System.out.println("zqian createTable : "+ createTable );
					st.execute( createTable );	
					
					//System.out.println(" alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum` add index `"+actualNodeName.replace("`","")+ "_Index` ( "+IndexString+ " ) ;");
					st.execute(" alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum` add index `"+actualNodeName.replace("`","")+ "_Index` ( "+IndexString+ " ) ;");
			
					groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
					//zqian, freq*log(cp), ILP 2014
					createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, " 
							+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
							+ groupby_string +" ;"; 
					// July 9th, 2014, zqian
					//  DOUBLE value is out of range in 'exp(`Financial_std_Training1_target_final`.`amount(loan0)_sum`.`Score`)'					
					//createTable_temp += " max(score)  From `" + actualNodeName.replace( "`" , "" ) +"_sum` " + groupby_string +" ;"; 
					
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
					st.execute("alter table temp add index temp (" +IndexString1 + ");");
					System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
					st.execute( createTable_temp_t );	
					//System.out.println("alter table temp_t add index temp (" +IndexString1 + ");");
					st.execute("alter table temp_t add index temp (" +IndexString1 + ");");
					String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
					System.out.println("zqian createTable_score  : "+ createTable_score  );
					st.execute( createTable_score );	
					//st.execute( " DROP TABLE IF EXISTS temp; ");
					//st.execute( " DROP TABLE IF EXISTS temp_t; ");
					
					
			 }
			 else  {
				//rename the table 
					String target_showcolumns ="show tables from "+databaseName5+" like '"+actualNodeName.replace( "`" , "" )+"%' ;";  //functorId in unielwin_BN
					System.out.println( "zqian : "+ target_showcolumns );
					Statement st_temp = con0.createStatement();
					//String name="";
					ResultSet rstarget_showcolumns = st_temp.executeQuery( target_showcolumns );
					if (  rstarget_showcolumns.absolute( 1 ) ){
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
						String IndexString="";
						String IndexString1="";
						int numArgs = CurrendID_List.size();				
						for ( int i = 0; i < numArgs; i++ )	{
							createTable_temp += CurrendID_List.get( i ) + ", ";
							createTable_temp_t +=  CurrendID_List.get( i ) + ", ";
							groupby_string += CurrendID_List.get( i ) + ", ";
							IndexString += CurrendID_List.get( i ) + ", ";
							SubQueryWhereString += "A."+CurrendID_List.get( i ) +" = B."+ CurrendID_List.get( i ) +" and ";
						}	
						
						IndexString1= IndexString.substring(0, IndexString.lastIndexOf(","));
						IndexString += "`"+actualNodeName.replace( "`" , "" ) +"`";
						groupby_string = groupby_string.substring(0, groupby_string.lastIndexOf(","));  // remove the last ","
						 //zqian, freq*log(cp), ILP 2014
						createTable_temp += " max(score) , sum(score), max(exp(score))/sum(exp(score)) as prob, "
								+ "log(max(exp(score))/sum(exp(score))) as loglikelihood From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
								+ groupby_string +" ;"; 
						//July 9th, 2014, zqian DOUBLE value is out of range
//						createTable_temp += " max(score) From `" + actualNodeName.replace( "`" , "" ) +"_sum` "
//								+ groupby_string +" ;"; 
						
						
						SubQueryWhereString= SubQueryWhereString.substring(0, SubQueryWhereString.lastIndexOf("and")-1);
						
						createTable_temp_t += " `" + actualNodeName.replace( "`" , "" ) + "` FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` A "
								+ " WHERE A.Score = ( select max(B.Score) FROM `" + actualNodeName.replace( "`" , "" ) +"_sum` B  WHERE "+SubQueryWhereString+ " ) "
								 + groupby_string  +" ;"; 
						//System.out.println(" alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum` add index `"+actualNodeName.replace("`","")+ "_Index` ( "+IndexString+ " ) ;");
						st_t.execute(" alter table "+databaseName5+".`"+actualNodeName.replace("`","")+ "_sum` add index `"+actualNodeName.replace("`","")+ "_Index` ( "+IndexString+ " ) ;");
						//create the target_Score table					
						st.execute( " DROP TABLE IF EXISTS temp; ");
						st.execute( " DROP TABLE IF EXISTS temp_t; ");
						System.out.println("zqian createTable_temp  : "+ createTable_temp  );
						st.execute( createTable_temp );	
						st.execute("alter table temp add index temp (" +IndexString1 + ");");
						System.out.println("zqian createTable_temp_t  : "+ createTable_temp_t  );
						st.execute( createTable_temp_t );
						st.execute("alter table temp_t add index temp (" +IndexString1 + ");");
						String createTable_score = "CREATE TABLE `" + actualNodeName.replace( "`" , "" ) +"_Score` AS SELECT * from temp natural join temp_t";	
						System.out.println("zqian createTable_score  : "+ createTable_score  );
						st.execute( createTable_score );	
						st_t.close();
					}
					rstarget_showcolumns.close();					
			 }	
		
			st.close();	
		}
		
		catch (SQLException e)	{
			System.out.println( "Failed to create statement!" );
			e.printStackTrace();
			return;
		}
		
		System.out.println("zqian: Leave createFinal3_zqian! \n");	
	}
	

	public static void Extract_Target_True_CT_zqian( String functorId_In_BN, String current_id  )	{
		//functorId = "`a`";
		System.out.println("\nzqian: Extract the True values for  functorId: "+functorId);	
		String functorId_t=functorId; //store the funcorId in unielwin_target_BN
		try	{
			String actualNodeName = "";
			Statement st = null;			
			st = conFinal.createStatement();
			actualNodeName =functorId;

			String createTable = "CREATE TABLE  "+ databaseName3 +".`" + actualNodeName.replace( "`" , "" ) +"_True` "
					+ " AS SELECT distinct " + current_id+ " `" +actualNodeName.replace( "`" , "" )+ "` From " + databaseName3 + ".`" + 
						  actualNodeName.replace("`", "")  +"_CT` ";	
		
						
			System.out.println("zqian : "+ createTable +" ;" );
			st.execute( createTable + " ;" );			
			st.close();	
		}			
		catch (SQLException e)
		{
			System.out.println( "Failed to create The True CT for functorId: "+functorId + " !" );
			e.printStackTrace();
			return;
		}
		functorId = functorId_t; //store the funcorId in unielwin_target_BN
		System.out.println("\nzqian: Leave Extract_Target_True_CT_zqian + functorId: "+functorId +"\n******************************** \n");	
	}


}
