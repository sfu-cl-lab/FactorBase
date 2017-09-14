/* zqian June 18, 2014
 * add the flag variable to tell if we use local ct or not.
 * Flag_UseLocal_CT = true, For TestScoreComputation to score the BN structure;
 * Flag_UseLocal_CT = false,  For FunctorWrapper to learn the weight. 
 * */

/*May 26, 2014 zqian, when link analysis is off, adding rnode as child in Path_BayesNet for the largest rchain,
 * after propagate the rnode edges in  PropagateContextEdges()
      // `a,b` as rchain, `a` as child, '' as parent
      // `a,b` as rchain, `b` as child, '' as parent
 */
/* Feb 7th 2014, zqian; updated on May 26, 2014 zqian, not suitable for link off
        //Make sure each node appear as a child in Path_BayesNet
        // <child,''>, <child,parent>
 // fixed : for each lattice point,  add one extra step after the structure learning is done
  *
 * */


/* Nov 25@ zqian, commont out all the codes related to Path_Forbidden_Edges,
 * @ Jun 5, Zqian
 * This is the Extended version1 based on LAJ algorithm which could discover
 * @1. the Attributes Correlations given the Existence of Links
 * @2. the Correlations between different Link Types
 * @3.  the Attributes Correlations given the Absence of Links
 * in a Hierarchical Way.
 *
 *
 * The difference between with BayesBase.java is as follows:
 * @1. pre-compute the CT tables using ADTree tricky with which trick there's no need to access the False Relationship.
 * @2. learn the Bayes Nets using CT tables which contain both True and False relationship.
 *
 *
 * Oliver and I believe this method will do the Best.
 * */
/*Jun 25, zqian
 *
 * For the BayesBase learning program, the csv generator part could be removed
 * since all the files could be prepared in advance by run the program CSVPrecomputor.java
 * */

import bif.BIFExport;
import bif.BIFImport;
import bif.BIF_IO;

import MLNExporter.*;
import com.mysql.jdbc.Connection;

import nu.xom.ParsingException;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class BayesBaseH {

	static Connection con1, con2,con3;

	//  to be read from config
	static String databaseName, databaseName2,databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

	static String opt3, cont;
	static boolean linkAnalysis;
	static boolean Flag_UseLocal_CT; //zqian June 18, 2014

	static int maxNumberOfMembers = 0;

	static ArrayList<String> rnode_ids;
	static ArrayList<String> rnode_ids_1;
	static ArrayList<String> pvar_ids;

	static int FirstRunning=1; // iff Running Time == 1, then generate the csv files
	                          // else, just reuse the old csv files. May 1st @zqian.


	public static void main(String[] args) throws Exception {
		runBBH();
	}

	public static void runBBH() throws Exception {
		initProgram(FirstRunning);
		connectDB();

		//build tables for structure learning
        BZScriptRunner bzsr = new BZScriptRunner(databaseName,con2);
        //set up the bayes net models O.S. Sep 12, 2017
        bzsr.runScript("scripts/model_manager.sql");
        //bzsr.runScript("scripts/bayesedges.sql");
        //get maxNumberOfMembers (max length of rchain)
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT max(length) FROM lattice_set;");
        rst.absolute(1);
        maxNumberOfMembers = rst.getInt(1);
        // get the longest rchain
		String rchain=null;
		ResultSet rst1 = st.executeQuery("Select name from lattice_set where length=" + maxNumberOfMembers + ";");
		rst1.absolute(1);
		rchain = rst1.getString(1);
		System.out.println(" ##### lattice is ready for use* "); //@zqian
		//structure learning
		StructureLearning(con2);
		/**
		 * OS: Nov 17, 2016. It can happen that Tetrad learns a forbidden edge. Argh. To catch this, we delete forbidden edges from any insertion. But then
		 * it can happen that a node has no edge at all, not even with an empty parent. In that case the Bif generator gets messed up. So we catch such
		 * orphaned nodes in the next statement.
		 */
		System.out.println("Inserting the Missing Fid as Child into Path_Bayes_Nets \n");
		st.execute("insert ignore into Path_BayesNets select '"
				+ rchain
				+ "' as Rchain, Fid as child,'' as parent from FNodes  where Fid not in (select distinct child from Path_BayesNets where "
				+ "Rchain='"
				+ rchain
				+ "')");
		
		//mapping the orig_rnid back and create a new table: Final_Path_BayesNets. //Sep 19, zqian
		BIF_Generator.Final_Path_BayesNets(con2,rchain);
		//parameter learning
		//Add setup options  Yan Sept. 10th
				//Continuous
		if (!cont.equals("1")) {
			//now compute conditional probability estimates and write them to @database@_BN
			System.out.println("\n Structure Learning is DONE.  ready for parameter learning. "); //@zqian
			//export the final result to xml. We assume that there is a single largest relationship chain, and write the Bayes net for that relationship chain to xml.
			// only export the structure, prepare for the pruning phase, Oct 23, 2013
			exportResults();
		//	System.out.println("\n**** Pruning starts "); // Oct 23
		//	Pruning();

			//	@zqian	for TestScoreComputation, use local ct to compute local cp.		
			if (Flag_UseLocal_CT){
				System.out.println("\n For BN_ScoreComputation.  use local_CT to compute the local_CP. ");
			}
			else {
				// for FuncotrWrapper, do NOT have to use the local_CT, or HAVE TO change the weight learning part. June 18 2014
				CPGenerator.Generator(databaseName,con2); // // May 22, 2014 zqian, computing the score for link analysis off.
				CP mycp = new CP(databaseName2,databaseName3);
				mycp.cp();
				System.out.println("\n Parameter learning is done.");
				//  for FuncotrWrapper	
				
			}
			
			// Export to .mln file,banff workshop demo, 2015
			//Exporter.strBuilder(con1,con2,databaseName);
			//score Bayes net: compute KL divergence, and log-likelihood (average probability of  node value given its Markov blanket, compared to database frequencies)
//May 7th, zqian, For RDN do not need to do the smoothing
			//COMPUTE KLD
			long l = System.currentTimeMillis(); //@zqian : measure structure learning time

			if (opt3.equals("1")) {
				System.out.println("\n KLD_generator.KLDGenerator.");
				KLD_generator.KLDGenerator(databaseName,con2);
			} else {
				System.out.println("\n KLD_generator.smoothed_CP.");
				KLD_generator.smoothed_CP(rchain, con2);
			}

			//generating the bif file, in order to feed into UBC tool (bayes.jar). Based on the largest relationship chain.
			//need cp tables
			BIF_Generator.generate_bif(databaseName,"Bif_"+databaseName+".xml",con2);

			long l2 = System.currentTimeMillis();  //@zqian : measure structure learning time
			System.out.print("smoothed_CP Time(ms): "+(l2-l)+" ms.\n");


		}

		//now compute conditional probability estimates and write them to @database@_BN
		else
			System.out.println("\n Structure Learning is DONE. \n NO parameter learning for Continuous data."); //@zqian


		//disconnect from db
		disconnectDB();



	}


	public static void StructureLearning(Connection conn) throws Exception{
		long l = System.currentTimeMillis(); //@zqian : measure structure learning time

		//handle pvars
		handlePVars();	 //import	 @zqian

	    Statement st = conn.createStatement();
		st.execute("insert ignore into Path_Required_Edges select distinct  RNodes_pvars.rnid AS Rchain, Entity_BayesNets.child AS child,  Entity_BayesNets.parent AS parent  FROM  (RNodes_pvars, Entity_BayesNets)    WHERE (RNodes_pvars.pvid = Entity_BayesNets.pvid  AND Entity_BayesNets.parent <> '') ;");
		st.execute("Drop table if exists Entity_BN_Nodes; ");
		st.execute("CREATE   table Entity_BN_Nodes AS   SELECT      Entity_BayesNets.pvid AS pvid,   Entity_BayesNets.child AS node  FROM      Entity_BayesNets ORDER BY pvid;");
		st.execute("insert ignore into Entity_Complement_Edges  select distinct  BN_nodes1.pvid AS pvid,   BN_nodes1.node AS child, BN_nodes2.node AS parent  FROM   Entity_BN_Nodes AS BN_nodes1,   Entity_BN_Nodes AS BN_nodes2    WHERE   BN_nodes1.pvid = BN_nodes2.pvid    AND (NOT (EXISTS( SELECT  *  FROM    Entity_BayesNets    WHERE  (Entity_BayesNets.pvid = BN_nodes1.pvid)   AND (Entity_BayesNets.child = BN_nodes1.node)  AND (Entity_BayesNets.parent = BN_nodes2.node))));");
	    st.execute("insert ignore into Path_Forbidden_Edges select distinct RNodes_pvars.rnid AS Rchain, Entity_Complement_Edges.child AS child, Entity_Complement_Edges.parent AS parent FROM (RNodes_pvars, Entity_Complement_Edges)   WHERE  (RNodes_pvars.pvid = Entity_Complement_Edges.pvid) ;");
	    st.close();

		//handle rnodes in a bottom-up way following the lattice
	    // Generating .CSV files by reading _CT tables directly (including TRUE relationship and FALSE relationship)
		handleRNodes_zqian(); //import
		//population lattice
	    //p_handleRNodes_zqian();
		 PropagateContextEdges();
		 //OS May 23. 2014 This looks like a much too complicated way to find the context edges. How about this: 1. Use a view Contextedges to find the context edges for each Rchain.
		 //2. Union these edges over the rchain, insert them into the largest rchain. 
		 //3.Make sure you insert "<rnid> null" into PathBN as well.
		 // zqian, when link is off, check the local_ct for rnode?

		long l2 = System.currentTimeMillis();  //@zqian : measure structure learning time
		System.out.print("\n*****************\nStructure Learning Time(ms): "+(l2-l)+" ms.\n");


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

	static void initProgram(int FirstRunning) throws IOException, SQLException {
		//read config file
		setVarsFromConfig();

		//init ids
		pvar_ids = new ArrayList<String>();
		rnode_ids = new ArrayList<String>();
		rnode_ids_1 = new ArrayList<String>();
	if(FirstRunning==1)
	{
	//	try{ //@ali
		//	delete(new File(databaseName+"/"));
	//	}catch (Exception e){}

		new File(databaseName+"/" + File.separator).mkdirs();
	//	new File(databaseName+"/" + File.separator + "csv" + File.separator).mkdirs();
		new File(databaseName+"/" + File.separator + "kno" + File.separator).mkdirs();
		new File(databaseName+"/" + File.separator + "res" + File.separator).mkdirs();
		new File(databaseName+"/" + File.separator + "xml" + File.separator).mkdirs();
	}

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
		opt3 = conf.getProperty("ComputeKLD");
		cont = conf.getProperty("Continuous");
		String strLinkAnalysis = conf.getProperty( "LinkCorrelations" );
		if ( strLinkAnalysis.equalsIgnoreCase( "1" ) )	{
			linkAnalysis = true;
		}
		else{
			linkAnalysis = false;
		}
		//zqian June 18, 2014		
		String UseLocal_CT = conf.getProperty( "UseLocal_CT" );
		//System.out.println(UseLocal_CT);
		if ( UseLocal_CT.equalsIgnoreCase( "1" ) )	{
			Flag_UseLocal_CT = true;
		}
		else{
			Flag_UseLocal_CT = false;
		}
		
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

	/* Jun 14
	 * 	if the tuples great than 1, then employ teradlearner
		else just insert the 1nid as child into entity_bayesnet.
	 * *
	 */
	public static void handlePVars() throws Exception {
		//read pvar -> create csv files
		readPvarFromBN();

		String NoTuples="";
		for(String id : pvar_ids)
		{
			System.out.println("\nStarting Learning the BN Structure of pvar_ids: " + id+"\n");
			Statement st = con3.createStatement();
			ResultSet rs = st.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_counts`;"); //Optimize this query, too slow, Nov 13, zqian
			while(rs.next()){
				NoTuples = rs.getString(1);
				System.out.println("NoTuples : " + NoTuples);
			}

			if(Integer.parseInt(NoTuples)>1){
				ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
						databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
						databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
					);
				bif1(id);
			}
			else{
				Statement st2 = con2.createStatement();
				// insert the BN nodes into Entity_BayesNet
				System.out.println("SELECT 1nid FROM 1Nodes,EntityTables	where 1Nodes.pvid = concat(EntityTables.Table_name,'0') and 1Nodes.pvid = '"+id+"';");
				ResultSet rs2 = st2.executeQuery("SELECT 1nid FROM 1Nodes,EntityTables	where 1Nodes.pvid = concat(EntityTables.Table_name,'0') and 1Nodes.pvid = '"+id+"';");
				String child ="";
				while(rs2.next()){
					Statement st3 = con2.createStatement();
					child= rs2.getString("1nid");
					System.out.println("INSERT ignore INTO Entity_BayesNets VALUES ('"+id+"', '"+child+"', '');");
					st3.execute("INSERT ignore INTO Entity_BayesNets VALUES ('"+id+"', '"+child+"', '');");
					st3.close();
					}

				rs2.close();
				st2.close();
			}

			System.out.println("\nEnd for " + id+"\n");

		}

		pvar_ids.clear();
	}


//	public static void handleRNodes_zqian() throws Exception {
//
//    	for(int len = 1; len <= maxNumberOfMembers; len++){
//
//			readRNodesFromLattice(len);	 //create csv files for all rnodes
//
////			//required edges
////			for(String id : rnode_ids)			//rchain
////	        {   System.out.println("\n !!!!Staring  to Export The Required Edges to "+id.replace("`","") +  "_req.xml \n");
////				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
////			   // System.out.println("export to _req.xml::rnode_id::"+id); //@zqian Test
////			 }
////
////			//forbidden edges
////			for(String id : rnode_ids)
////			{   System.out.println("\n !!!!Staring  to Export The Forbidden Edges to "+id.replace("`","")+ "_for.xml \n");
////				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
////			    //System.out.println("export to _for.xml::rnode_id::"+id); //@zqian Test
////			}
//			for(String id : rnode_ids)  {
//				Statement st_t = con2.createStatement();
//				//st_t.execute("delete from Path_Required_Edges where Rchain = '"+id+"' and (child,parent) in (select child,parent from Path_Forbidden_Edges where Rchain ='"+id+"' );"); // Oct 2nd
//				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
//				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
//			}
//			for(String id : rnode_ids)
//			{// 1. feeding into True.CSV and learning the edges involving 1nodes/2nodes to 1nodes/2nodes
//						ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
//										databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + "True.csv",
//										databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
//										databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
//										databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + "True.xml"
//									);
//						BIFImport.Import(databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + "True.xml", id, "Path_Required_Edges", con2);
//
//						System.out.println("TRUE BN Structure Learning for rnode_id::"+id+"is done."); //@zqian Test
//			}
//
//			String NoTuples="";
//			for(String id : rnode_ids) {
//				System.out.println("\nStarting Learning the BN Structure of rnode_ids: " + id+"\n");
//				Statement st3 = con3.createStatement();
//				ResultSet rs = st3.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_CT`;"); // Oct 2nd, Why not check the csv file directly?  faster for larter CT? Oct 23
//				while(rs.next()){
//					NoTuples = rs.getString(1);
//					System.out.println("NoTuples : " + NoTuples);
//				}
//				if(Integer.parseInt(NoTuples)>1){
//				ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
//								databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
//								databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
//								databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
//								databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
//							);
//				System.out.println("The BN Structure Learning for rnode_id::"+id+"is done."); //@zqian Test
//				bif2(id) ; // import to db   @zqian
//				}
//			}
//
//			 // import to db   @zqian
//
//
//			Statement st = con2.createStatement();
//			// find new edges learned for this rchain
//			//st.execute("insert ignore into LearnedEdges select distinct   Path_BayesNets.Rchain,  Path_BayesNets.child,  Path_BayesNets.parent from    Path_BayesNets,    lattice_set,    lattice_rel where    Path_BayesNets.parent <> ''  and lattice_set.name = lattice_rel.parent         and lattice_set.length = "+len+"         and  (Path_BayesNets.Rchain , Path_BayesNets.child, Path_BayesNets.parent) not in (select  *  from  Path_Required_Edges); ");
//			// insert the context edges into path_bayesnet, the parent should be Rnode
//			//st.execute("insert ignore into Path_BayesNets select distinct  LearnedEdges.Rchain as Rchain, LearnedEdges.child as child,  lattice_membership.member as parent from    LearnedEdges,     lattice_membership,     lattice_rel,    lattice_set where     LearnedEdges.Rchain = lattice_membership.name  and lattice_set.length ="+ len+" and Rchain = lattice_set.name  ");
//			// propagate all edges to next level
//			st.execute("insert ignore into InheritedEdges select distinct lattice_rel.child AS Rchain, Path_BayesNets.child AS child,  Path_BayesNets.parent AS parent   FROM   Path_BayesNets,  lattice_rel,lattice_set    WHERE    lattice_rel.parent = Path_BayesNets.Rchain    AND Path_BayesNets.parent <> ''     and lattice_set.name=lattice_rel.parent	and lattice_set.length = "+(len)+"    ORDER BY Rchain;");
//			// make inherited edges as required edges, while avoiding conflict edges
//			//original query.
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges) ; ");
//			//OCT 23, remove edges pointing to RNodes
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  and child not in ( select rnid from RNodes);");
//			//OCT 28, remove edges from RNodes to RNodes
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  and  (child not in (select rnid  from RNodes)   or parent not in(select    rnid   from  RNodes) ) ");
//			//(child not in (select rnid  from RNodes)   or parent not in(select    rnid   from  RNodes) )
////####      Design One Required Edges:  ONLY propagate edges from/to 1Nodes/2Nodes  + SchemaEdges( such edges already inserted into required edges in the scripts,i.e. RNodes to 2Nodes)
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)   and child not in (select rnid from RNodes) and parent not in (select rnid from RNodes) union select distinct Rchain,child,parent from schemaedges,lattice_set where lattice_set.length = "+ (len+1)+" and Rchain = lattice_set.name " );
//
////####      Design Two Required Edges:	 propagate edges from/to 1Nodes/2Nodes  + SchemaEdges + RNodes to 1Nodes/2Nodes
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)   and child not in (select rnid from RNodes) " );
//
////####      Design Three Required Edges:	 propagate edges from/to 1Nodes/2Nodes  + SchemaEdges + RNodes to 1Nodes/2Nodes (same as Design Two)
//			st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)   and child not in (select rnid from RNodes) " );
////####      Design Four: Do NOT differenciated Nodes Type, consider them(1Nodes,2Nodes,RNodes) as the same
//			//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges) ; ");
//
//			// for path_complemtment edges, rchain should be at current level (len)
//			st.execute("insert ignore into Path_Complement_Edges select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set    WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  ((BN_nodes1.Rchain = BN_nodes2.Rchain)   AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)    AND (Path_BayesNets.parent = BN_nodes2.node)))))) ;");
////			st.execute("insert ignore into Path_Complement_Edges select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set    WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  ((BN_nodes1.Rchain = BN_nodes2.Rchain)   AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)    AND (Path_BayesNets.parent = BN_nodes2.node)))))) and BN_nodes2.node not in (select rnid from RNodes);");
//
//			// for path forbidden edges, rchain should be at higher level (len+1) , so its parent should be at current level (len)
//			// make absent edges as forbidden edges, and give higher priority of required edges in case of conflict edges
//			//original query.
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) ;");
//			//OCT 23, remove edges pointing to RNodes
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and Path_Complement_Edges.child not in ( select rnid from RNodes);");
//			//OCT 28, remove edges envolve RNodes
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and (Path_Complement_Edges.child not in ( select rnid from RNodes) and Path_Complement_Edges.parent not in ( select rnid from RNodes) );");
//			//OCT 30, should propagate edges pointing to RNodes from lower lever to higher level, so remove edges from RNodes.
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and  Path_Complement_Edges.parent not in ( select rnid from RNodes) ;");
////####      Design One Forbidden Edges:  ONLY propagate edges from/to 1Nodes/2Nodes
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges)  and Path_Complement_Edges.child not in (select rnid from RNodes) and Path_Complement_Edges.parent not in (select rnid from RNodes);");
////####      Design Two Forbidden Edges:  ONLY propagate edges from/to 1Nodes/2Nodes, (same as Design one)
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges)  and Path_Complement_Edges.child not in (select rnid from RNodes) and Path_Complement_Edges.parent not in (select rnid from RNodes);");
////####      Design Three Forbidden Edges:	propagate edges from/to 1Nodes/2Nodes  + 1Nodes/2Nodes to RNodes
//			st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges)  and Path_Complement_Edges.parent not in (select rnid from RNodes);");
//
////####      Design Four: Do NOT differenciated Nodes Type, consider them(1Nodes,2Nodes,RNodes) as the same
//			//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) ;");
//
//
//
//
//
//			st.close();
//
//
//			rnode_ids.clear(); //prepare for next loop
//
//			System.out.println(" Import is done for length = "+len+"."); //@zqian Test
//
//		}
//    }
//

	 public static void handleRNodes_zqian() throws Exception {

	    	for(int len = 1; len <= maxNumberOfMembers; len++){

				readRNodesFromLattice(len);	 //create csv files for all rnodes

				//required edges
				for(String id : rnode_ids)			//rchain
		        {   System.out.println("\n !!!!Staring  to Export The Required Edges to "+id.replace("`","") +  "_req.xml \n");
		        	BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
				   // System.out.println("export to _req.xml::rnode_id::"+id); //@zqian Test

				 }
				//Nov25
				//forbidden edges
				for(String id : rnode_ids)
				{   System.out.println("\n !!!!Staring  to Export The Forbidden Edges to "+id.replace("`","")+ "_for.xml \n");
					BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
				    //System.out.println("export to _for.xml::rnode_id::"+id); //@zqian Test
				}


				String NoTuples="";
				for(String id : rnode_ids) {
					System.out.println("\nStarting Learning the BN Structure of rnode_ids: " + id+"\n");
					Statement st = con3.createStatement();
					ResultSet rs = st.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_CT`;"); // Oct 2nd, Why not check the csv file directly?  faster for larter CT? Oct 23
						while(rs.next()){
							NoTuples = rs.getString(1);
							System.out.println("NoTuples : " + NoTuples);
						}
					if(Integer.parseInt(NoTuples)>1){
					ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
									databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
									databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
									databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
									databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
								);
					System.out.println("The BN Structure Learning for rnode_id::"+id+"is done."); //@zqian Test
					bif2(id) ; // import to db   @zqian
					
					}
				}
				


				 // import to db   @zqian


				Statement st = con2.createStatement();



				// find new edges learned for this rchain
				//st.execute("insert ignore into LearnedEdges select distinct   Path_BayesNets.Rchain,  Path_BayesNets.child,  Path_BayesNets.parent from    Path_BayesNets,    lattice_set,    lattice_rel where    Path_BayesNets.parent <> ''  and lattice_set.name = lattice_rel.parent         and lattice_set.length = "+len+"         and  (Path_BayesNets.Rchain , Path_BayesNets.child, Path_BayesNets.parent) not in (select  *  from  Path_Required_Edges); ");
				// insert the context edges into path_bayesnet, the parent should be Rnode
				//st.execute("insert ignore into Path_BayesNets select distinct  LearnedEdges.Rchain as Rchain, LearnedEdges.child as child,  lattice_membership.member as parent from    LearnedEdges,     lattice_membership,     lattice_rel,    lattice_set where     LearnedEdges.Rchain = lattice_membership.name  and lattice_set.length ="+ len+" and Rchain = lattice_set.name  ");
				// propagate all edges to next level
				st.execute("insert ignore into InheritedEdges "
						+ "select distinct lattice_rel.child AS Rchain, Path_BayesNets.child AS child,  Path_BayesNets.parent AS parent   "
						+ "FROM   Path_BayesNets,  lattice_rel,lattice_set   "
						+ " WHERE    lattice_rel.parent = Path_BayesNets.Rchain    AND Path_BayesNets.parent <> ''     "
						+ "and lattice_set.name=lattice_rel.parent	and lattice_set.length = "+(len)+"    ORDER BY Rchain;");

				if ( !linkAnalysis )
				{

                    // find new edges learned for this rchain
                    st.execute("insert ignore into LearnedEdges select distinct   Path_BayesNets.Rchain,  Path_BayesNets.child,  Path_BayesNets.parent from    Path_BayesNets,    lattice_set,    lattice_rel where    Path_BayesNets.parent <> ''  and lattice_set.name = lattice_rel.parent         and lattice_set.length = "+len+"         and  (Path_BayesNets.Rchain , Path_BayesNets.child, Path_BayesNets.parent) not in (select  *  from  Path_Required_Edges); ");
                //April 1: we insert the context edges at the end. Adding them during learning causes problem with link analysis off.
                    // insert the context edges into path_bayesnet, the parent should be Rnode 
                    //st.execute("insert ignore into Path_BayesNets select distinct  LearnedEdges.Rchain as Rchain, LearnedEdges.child as child,  lattice_membership.member as parent from    LearnedEdges,     lattice_membership,     lattice_rel,    lattice_set where     LearnedEdges.Rchain = lattice_membership.name  and lattice_set.length ="+ len+" and Rchain = lattice_set.name  ");
 
            /*      st.execute( "create table ContextEdges as select distinct LearnedEdges.Rchain as Rchain, LearnedEdges.child as child, lattice_rel.parent as parent from LearnedEdges, lattice_set, lattice_rel where LearnedEdges.Rchain = lattice_set.name and lattice_set.name = lattice_rel.parent and lattice_set.length = '" + len + "';" );*/
                //  st.execute( "INSERT IGNORE INTO Path_BayesNets SELECT * FROM ContextEdges;" );
                //  st.execute( "DROP TABLE ContextEdges;" );
                    // propagate all edges to next level 
                    st.execute("insert ignore into InheritedEdges select distinct lattice_rel.child AS Rchain, Path_BayesNets.child AS child,  Path_BayesNets.parent AS parent   FROM   Path_BayesNets,  lattice_rel,lattice_set    WHERE    lattice_rel.parent = Path_BayesNets.Rchain    AND Path_BayesNets.parent <> ''     and lattice_set.name=lattice_rel.parent   and lattice_set.length = "+(len)+"    ORDER BY Rchain;");
                    
                    // KURT: Alternate LearnedEdges
                    st.execute( "INSERT IGNORE INTO NewLearnedEdges SELECT " + 
                                "Path_BayesNets.Rchain, Path_BayesNets.child, " + 
                                "Path_BayesNets.parent FROM Path_BayesNets, " + 
                                "lattice_set WHERE Path_BayesNets.parent <> '' AND " + 
                                "Path_BayesNets.Rchain = lattice_set.name AND " + 
                                "lattice_set.length = " + len + " AND ( " + 
                                "Path_BayesNets.Rchain, Path_BayesNets.child, " + 
                                "Path_BayesNets.parent ) NOT IN ( SELECT * FROM " + 
                                "Path_Required_Edges );" );
                    st.execute( "INSERT IGNORE INTO InheritedEdges SELECT DISTINCT " + 
                                "NewLearnedEdges.Rchain AS Rchain, NewLearnedEdges.child " + 
                                "AS child, lattice_membership.member AS parent FROM " + 
                                "NewLearnedEdges, lattice_membership WHERE " + 
                                "NewLearnedEdges.Rchain = lattice_membership.name;" );
                    st.execute( "INSERT IGNORE INTO Path_BayesNets SELECT * FROM InheritedEdges;" );
                    
                  /*    st.execute( "INSERT IGNORE INTO RNodeEdges as select distinct LearnedEdges.Rchain as Rchain," +
                    " LearnedEdges.child as child,lattice_membership.member as parent from LearnedEdges," +
                    "lattice_membership where LearnedEdges.Rchain = lattice_membership.name;");
                        */
            
        /*  st.execute( "INSERT IGNORE INTO Path_BayesNets SELECT * FROM ContextEdges; ");*/
				}
				// make inherited edges as required edges, while avoiding conflict edges
				//original query.
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges) ; ");
				//OCT 23, remove edges pointing to RNodes
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  and child not in ( select rnid from RNodes);");
				//OCT 28, remove edges from RNodes to RNodes
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  and  (child not in (select rnid  from RNodes)   or parent not in(select    rnid   from  RNodes) ) ");
				//(child not in (select rnid  from RNodes)   or parent not in(select    rnid   from  RNodes) )
	//####      Design One Required Edges:  ONLY propagate edges from/to 1Nodes/2Nodes  + SchemaEdges( such edges already inserted into required edges in the scripts,i.e. RNodes to 2Nodes)
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)   and child not in (select rnid from RNodes) and parent not in (select rnid from RNodes) union select distinct Rchain,child,parent from schemaedges,lattice_set where lattice_set.length = "+ (len+1)+" and Rchain = lattice_set.name " );

	//####      Design Two Required Edges:	 propagate edges from/to 1Nodes/2Nodes  + SchemaEdges + RNodes to 1Nodes/2Nodes
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)   and child not in (select rnid from RNodes) " );

	//####      Design Three Required Edges:	 propagate edges from/to 1Nodes/2Nodes  + SchemaEdges + RNodes to 1Nodes/2Nodes (same as Design Two)
				st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   "
						+ "where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" "
								+ "and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  "
								+ " and child not in (select rnid from RNodes) " );
	//####      Design Four: Do NOT differenciated Nodes Type, consider them(1Nodes,2Nodes,RNodes) as the same
				//st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges) ; ");

				// for path_complemtment edges, rchain should be at current level (len)
//Nov25
				st.execute("insert ignore into Path_Complement_Edges "
						+ "select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    "
						+ "FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set   "
						+ " WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  "
								+ "((BN_nodes1.Rchain = BN_nodes2.Rchain)   "
								+ "AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  "
								+ "WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)   "
								+ " AND (Path_BayesNets.parent = BN_nodes2.node)))))) ;");
//				st.execute("insert ignore into Path_Complement_Edges select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set    WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  ((BN_nodes1.Rchain = BN_nodes2.Rchain)   AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)    AND (Path_BayesNets.parent = BN_nodes2.node)))))) and BN_nodes2.node not in (select rnid from RNodes);");

				// for path forbidden edges, rchain should be at higher level (len+1) , so its parent should be at current level (len)
				// make absent edges as forbidden edges, and give higher priority of required edges in case of conflict edges
				//original query.
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) ;");
				//OCT 23, remove edges pointing to RNodes
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and Path_Complement_Edges.child not in ( select rnid from RNodes);");
				//OCT 28, remove edges envolve RNodes
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and (Path_Complement_Edges.child not in ( select rnid from RNodes) and Path_Complement_Edges.parent not in ( select rnid from RNodes) );");
				//OCT 30, should propagate edges pointing to RNodes from lower lever to higher level, so remove edges from RNodes.
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) and  Path_Complement_Edges.parent not in ( select rnid from RNodes) ;");
	//####      Design One Forbidden Edges:  ONLY propagate edges from/to 1Nodes/2Nodes
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges)  and Path_Complement_Edges.child not in (select rnid from RNodes) and Path_Complement_Edges.parent not in (select rnid from RNodes);");
	//####      Design Two Forbidden Edges:  ONLY propagate edges from/to 1Nodes/2Nodes, (same as Design one)
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges)  and Path_Complement_Edges.child not in (select rnid from RNodes) and Path_Complement_Edges.parent not in (select rnid from RNodes);");
	//####      Design Three Forbidden Edges:	propagate edges from/to 1Nodes/2Nodes  + 1Nodes/2Nodes to RNodes
//Nov25
				st.execute("insert ignore into Path_Forbidden_Edges "
						+ "select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    "
						+ "FROM  Path_Complement_Edges,        lattice_rel,    lattice_set   "
						+ " WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" "
								+ "and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   "
								+ "and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) "
								+ "not in (select  Rchain,child,parent from  Path_Required_Edges)  "
								+ "and Path_Complement_Edges.parent not in (select rnid from RNodes);");

	//####      Design Four: Do NOT differenciated Nodes Type, consider them(1Nodes,2Nodes,RNodes) as the same
				//st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) ;");

				st.close();


				rnode_ids.clear(); //prepare for next loop

				System.out.println(" Import is done for length = "+len+"."); //@zqian Test

			}
	    }


	 public static void PropagateContextEdges() throws Exception {
         Statement st = con2.createStatement();
         st.execute("drop table  if exists RNodeEdges ;");
         st.execute("Create Table RNodeEdges like Path_BayesNets;");
         st.execute("drop table  if exists ContextEdges ;");
         st.execute( "create table ContextEdges as select distinct NewLearnedEdges.Rchain as Rchain," +
                    " NewLearnedEdges.child as child,lattice_membership.member as parent from NewLearnedEdges," +
                    "lattice_membership where NewLearnedEdges.Rchain = lattice_membership.name;");
         st.execute("insert ignore into RNodeEdges select Rchain, child, parent from ContextEdges, lattice_set where " +
                "lattice_set.name=ContextEdges.Rchain and lattice_set.length=1;");
 
         for(int len = 2; len <= maxNumberOfMembers; len++){
             st.execute("insert ignore into RNodeEdges select  Rchain, child, parent from    ContextEdges," +
                    "  lattice_set  where    lattice_set.name = ContextEdges.Rchain and lattice_set.length="+len+" union "+
                    " select distinct lattice_rel.child, RNodeEdges.child, RNodeEdges.parent from  lattice_set, lattice_rel," +
                    " RNodeEdges where lattice_set.length="+len+" and   lattice_rel.child = lattice_set.name and RNodeEdges.Rchain = lattice_rel.parent;");
         }
         st.execute( "INSERT IGNORE INTO Path_BayesNets SELECT * FROM RNodeEdges; ");
//start      
//adding rnode as child in Path_BayesNet for the largest rchain, May 26, 2014 zqian
         // `a,b` as rchain, `a` as child, '' as parent
      // `a,b` as rchain, `b` as child, '' as parent
         String largest_rchain="";
         Statement st_largest=con2.createStatement();
         ResultSet rs_largest= st_largest.executeQuery(" Select name as Rchain from lattice_set where length=( SELECT max(length) FROM lattice_set); ");
         rs_largest.absolute(1);
         largest_rchain = rs_largest.getString(1);
     	//System.out.println("\n largest_rchain : " + largest_rchain);
         st_largest.close();
         
         Statement st1 = con2.createStatement();
     	 ResultSet rs = st1.executeQuery("select name as RChain from lattice_set where lattice_set.length = 1 ;");
     	 while(rs.next()){
     		//  get rvid for further use
     		String rchain = rs.getString("RChain");
     		//System.out.println("\n RChain : " + rchain);
     		rnode_ids_1.add(rchain);
     	  }
     	 st1.close();
     	Statement st_temp = con2.createStatement();
     	 for(String id : rnode_ids_1)  {//Feb 7th 2014, zqian; updated May 26, 2014 zqian
			//System.out.println("id: "+id);
			st_temp.execute("insert ignore into Path_BayesNets (select '"+ largest_rchain +"' as Rchain, '"+id+"'  as child, ''  as parent ) ;");
			//System.out.println ("insert ignore into Path_BayesNets (select '"+ largest_rchain +"' as Rchain, '"+id+"'  as child, ''  as parent ) ;");
         }
			st_temp.close();
// end for adding rnode as child, May 26th, 2014 zqian     	 
     	 st.close();
    
     }
 
	 
/* Jan 28, hard-coding the population lattice learning
 * do the learning for level 1 (a;b;c)
 * and then skip the level 2 (a,b; a,c; b,c)
 * jump to level 3 directly (a,b,c)
 * */
	 public static void p_handleRNodes_zqian() throws Exception {

	    	for (int len = 1; len <= maxNumberOfMembers; len++)
	    	{
				readRNodesFromLattice(len);	 //create csv files for all rnodes

				//required edges
				for(String id : rnode_ids)			//rchain
		        {   System.out.println("\n !!!!Staring  to Export The Required Edges to "+id.replace("`","") +  "_req.xml \n");
		        	BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
				   // System.out.println("export to _req.xml::rnode_id::"+id); //@zqian Test

				 }
				//Nov25
				//forbidden edges
				for(String id : rnode_ids)
				{   System.out.println("\n !!!!Staring  to Export The Forbidden Edges to "+id.replace("`","")+ "_for.xml \n");
					BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
				    //System.out.println("export to _for.xml::rnode_id::"+id); //@zqian Test
				}


				String NoTuples="";
				for(String id : rnode_ids) {
					System.out.println("\nStarting Learning the BN Structure of rnode_ids: " + id+"\n");
					Statement st = con3.createStatement();
					ResultSet rs = st.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_CT`;"); // Oct 2nd, Why not check the csv file directly?  faster for larter CT? Oct 23
						while(rs.next()){
							NoTuples = rs.getString(1);
							System.out.println("NoTuples : " + NoTuples);
						}
					if(Integer.parseInt(NoTuples)>1 && len!=2){ // skip the level 2, Zqian @ Jan 28 2014, for hep,fin,imdb
						//if(Integer.parseInt(NoTuples)>1 && len!=1){ // skip the level 2, Zqian @ Jan 28 2014, for muta
					ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
									databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
									databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
									databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
									databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
								);
					System.out.println("The BN Structure Learning for rnode_id::"+id+"is done."); //@zqian Test
					bif2(id) ; // import to db   @zqian
					//Feb 7th 2014, zqian;
					//Make sure each node appear as a child in Path_BayesNet
					System.out.print("id: "+id);
					Statement st_temp = con2.createStatement();
					ResultSet rs_temp = st_temp.executeQuery(" select distinct parent From Path_BayesNets where Rchain ='"+id+"'  and parent not in "
							+ "(select distinct child from Path_BayesNets where Rchain ='"+id+"');");
					if (!rs_temp.next()) {
					st_temp.execute("insert into Path_BayesNets (select '"+ id +"' as Rchain,"
							+ "( select distinct parent From Path_BayesNets where Rchain ='"+id+"'  and parent not in "
									+ "(select distinct child from Path_BayesNets where Rchain ='"+id+"') ) as child,"
							+ " '' )");
					}
					st_temp.close();

					}
					else if (len==2)  //for hep,fin,imdb
						//else if (len==1)  // muta
					{	Statement st_temp = con2.createStatement();

						st_temp.execute("delete from Path_BayesNets where Rchain = '"+id+"' and (child,parent) in (select child,parent from Path_Forbidden_Edges where Rchain ='"+id+"' );"); // Oct 2nd
						//bif2(id) ; // just import the knowledge to path_bayesNets Jan 28
						//BIFImport.Import(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", id, "Path_BayesNets", con2);

						ArrayList<String[]> pairs = BIF_IO.getLinksFromFile(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml");

						//System.out.print(id);
						for (String[] pair : pairs) {
				           // Statement st = con.createStatement();

				            System.out.println("INSERT ignore INTO " + "Path_BayesNets" + " VALUES (\'" + id + "\', \'`" + pair[1] + "`\', \'`" + pair[0] + "`\');");
				            st_temp.execute("INSERT ignore INTO " + "Path_BayesNets" + " VALUES (\'" + id + "\', \'`" + pair[1] + "`\', \'`" + pair[0] + "`\');");

						}


					}
				}


				 // import to db   @zqian


				Statement st = con2.createStatement();
				// find new edges learned for this rchain
				// propagate all edges to next level
				st.execute("insert ignore into InheritedEdges "
						+ "select distinct lattice_rel.child AS Rchain, Path_BayesNets.child AS child,  Path_BayesNets.parent AS parent   "
						+ "FROM   Path_BayesNets,  lattice_rel,lattice_set   "
						+ " WHERE    lattice_rel.parent = Path_BayesNets.Rchain    AND Path_BayesNets.parent <> ''     "
						+ "and lattice_set.name=lattice_rel.parent	and lattice_set.length = "+(len)+"    ORDER BY Rchain;");
				// make inherited edges as required edges, while avoiding conflict edges

	//####      Design Three Required Edges:	 propagate edges from/to 1Nodes/2Nodes  + SchemaEdges + RNodes to 1Nodes/2Nodes (same as Design Two)
				st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   "
						+ "where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" "
								+ "and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges)  "
								+ " and child not in (select rnid from RNodes) " );

				// for path_complemtment edges, rchain should be at current level (len)
//Nov25
				st.execute("insert ignore into Path_Complement_Edges "
						+ "select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    "
						+ "FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set   "
						+ " WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  "
								+ "((BN_nodes1.Rchain = BN_nodes2.Rchain)   "
								+ "AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  "
								+ "WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)   "
								+ " AND (Path_BayesNets.parent = BN_nodes2.node)))))) ;");
//####      Design Three Forbidden Edges:	propagate edges from/to 1Nodes/2Nodes  + 1Nodes/2Nodes to RNodes
//Nov25
				st.execute("insert ignore into Path_Forbidden_Edges "
						+ "select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    "
						+ "FROM  Path_Complement_Edges,        lattice_rel,    lattice_set   "
						+ " WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" "
								+ "and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   "
								+ "and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) "
								+ "not in (select  Rchain,child,parent from  Path_Required_Edges)  "
								+ "and Path_Complement_Edges.parent not in (select rnid from RNodes);");
				st.close();


				rnode_ids.clear(); //prepare for next loop

				System.out.println(" Import is done for length = "+len+"."); //@zqian Test

	 }

}



public static void readPvarFromBN() throws SQLException, IOException {
		Statement st = con2.createStatement();
		  //from main db
		ResultSet rs = st.executeQuery("select * from PVariables where index_number = 0;");    //O.S. March 21 ignore variables that aren't main.
		while(rs.next()){

			//  get pvid for further use
			String pvid = rs.getString("pvid");
			System.out.println("pvid : " + pvid);

			//  add to ids for further use
			pvar_ids.add(pvid);
		}
		//  close statements

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
		rnode_ids.add(rchain);
		//rnode_ids_1.add(rchain);
		
	}

	//rs.close();
	st.close();
}

// import to Entity_BayesNets
	public static void bif1(String id) throws SQLException, IOException, ParsingException {
		//import	 @zqian
		System.out.println(" Starting to Import the learned path into MySQL::**Entity_BayesNets**"); //@zqian Test
        Statement st = con2.createStatement();
		//st.execute("truncate Entity_BayesNets;");
		int i=0;
		//for(String id : pvar_ids)
		//{
			//System.out.println(databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml "+ id + " Entity_BayesNets "+con2);
			BIFImport.Import(databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Entity_BayesNets", con2);
		    System.out.println("*** imported Entity_BayesNets "+pvar_ids.get(i++)+" into database");

		//}
		System.out.println(" \n !!!!!!!!!Import is done for **Entity_BayesNets** \n"); //@zqian Test

		st.close();
	}

// import to Path_BayesNets	//zqian@Oct 2nd 2013
	public static void bif2(String id) throws SQLException, IOException, ParsingException {
		System.out.println(" Starting to Import the learned path into MySQL::**Path_BayesNets**"); //@zqian

		Statement st = con2.createStatement();
		//st.execute("truncate Path_BayesNets;");
		int j=0;

			BIFImport.Import(databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Path_BayesNets", con2);
			//zqian@Oct 2nd 2013
			//delete the edges which is already forbidden in a lower level before inserting into the database.
		//Nov 25
			st.execute("delete from Path_BayesNets where Rchain = '"+id+"' and (child,parent) in (select child,parent from Path_Forbidden_Edges where Rchain ='"+id+"' );"); // Oct 2nd

			System.out.println("*** imported Path_BayesNets "+rnode_ids.get(j++)+"into database");

		System.out.println(" Import is done for **Path_BayesNets**"); //@zqian Test
		st.close();
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

	public static void exportResults() throws SQLException, IOException {
		Statement st = con2.createStatement();

		ResultSet rs = st.executeQuery("select name from lattice_set where length = " + maxNumberOfMembers + ";");
		while (rs.next()){
			String setName = rs.getString("name");
			BIFExport.Export(databaseName+"/" + File.separator + "res" + File.separator + setName.replace("`","") + ".xml", "Rchain", "Path_BayesNets", setName, con2);
		}
 	    st.close();
	}

	public static void disconnectDB() throws SQLException {
		con1.close();
		con2.close();
		con3.close();
	}
}
