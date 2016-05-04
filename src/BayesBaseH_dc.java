import bif.BIFExport;
import bif.BIFImport;
import com.mysql.jdbc.Connection;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.LocalScoreCache;
import lattice.short_rnid_LatticeGenerator;
import nu.xom.ParsingException;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.*;
import java.util.ArrayList;
/*@ Jun 5, Zqian
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
import java.util.*;


public class BayesBaseH {
	
	/** Diljot,Chris:  Moved the declaration for the Hash for scores here in order to make 
	 * it global for all calls. Now the hashmap rests in the top of the hierarchy and is 
	 * passed to the function that is called multiple times. This ensures that the 
	 * hash is not cleared each time we do a new call. All the code that clears hash
	 * in Ges3 has been removed.
	 * This code reuses the scores between calls, although the output is not quite right yet.
	 * See BdeuScore.java doc for explanation.
	 * 
	 * The globalScoreHash is intialized and passed to the tetrad learner Bayes_Learning_Main.java 
	 * Then the tetrad learner uses same hash and passes it down to the functions in Ges3.java,
	 * thus using the same hash map. 
	 * This makes the hash usable between the different calls.
	 */
	//The hash map that we will be using to hash the scores, is accessible between multiple calls.
	
	private static Map<Node, Map<Set<Node>, Double>> globalScoreHash =  new WeakHashMap<Node, Map<Set<Node>, Double>>();;

	static Connection con1, con2,con3;

	//  to be read from config
	static String databaseName, databaseName2,databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

	static String opt3, cont;
	
	static int maxNumberOfMembers = 0;

	static ArrayList<String> rnode_ids;
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
        bzsr.runScript("src/scripts/bayesedges.sql");    	
        
      //get maxNumberOfMembers, max length of rchain
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT max(length) FROM lattice_set;");
        rst.absolute(1);
        maxNumberOfMembers = rst.getInt(1);
        
        
		System.out.println(" ##### lattice is ready for use* "); //@zqian	
	    
	   			
		
		//structure learning 
        StructureLearning(con2);
        
		//parameter learning
		System.out.println("\n ##### Results of parameter learning* "); //@zqian	
		
		//now compute conditional probability estimates and write them to @database@_BN
		String rchain=null;
		//Add setup options  Yan Sept. 10th
		if (!cont.equals("1")) {
			CPGenerator.Generator(databaseName,con2);
			CP mycp = new CP(databaseName2,databaseName3);
			mycp.cp();
			
			//score Bayes net: compute KL divergence, and log-likelihood (average probability of  node value given its Markov blanket, compared to database frequencies) 
			//KLD_generator.KLDGenerator(databaseName,con2);
			if (opt3.equals("1")) {
				KLD_generator.KLDGenerator(databaseName,con2);
			} else {
				ResultSet rst1 = st.executeQuery("Select name from lattice_set where length=" + maxNumberOfMembers + ";");
				rst1.absolute(1);
				rchain = rst1.getString(1);
				KLD_generator.smoothed_CP(rchain, con2);
			}
		
		
			//export the final result to xml. We assume that there is a single largest relationship chain, and write the Bayes net for that relationship chain to xml.
			//It seems that this can be skipped for speed
			exportResults(); 
			//generating the bif file, in order to feed into UBC tool (bayes.jar). Based on the largest relationship chain.
			BIF_Generator.generate_bif(databaseName,"Bif_"+databaseName+".xml",con2);
			
		}
 
		//Sep 19, zqian
		//mapping the orig_rnid back and create a new table: Final_Path_BayesNets.
		BIF_Generator.Final_Path_BayesNets(con2,rchain);
 
		//disconnect from db
		disconnectDB();
		
	}

	
	public static void StructureLearning(Connection conn) throws Exception{
		long l = System.currentTimeMillis(); //@zqian : measure structure learning time

		//handle pvars
		handlePVars();	 //import	 @zqian   
       
	    Statement st = conn.createStatement();
		st.execute("insert ignore into Path_Required_Edges select distinct  RNodes_pvars.rnid AS Rchain, Entity_BayesNets.child AS child,  Entity_BayesNets.parent AS parent  FROM  (RNodes_pvars, Entity_BayesNets)    WHERE (RNodes_pvars.pvid = Entity_BayesNets.pvid  AND Entity_BayesNets.parent <> '') ;");
		st.execute("insert ignore into Entity_Complement_Edges  select distinct  BN_nodes1.pvid AS pvid,   BN_nodes1.node AS child, BN_nodes2.node AS parent  FROM   Entity_BN_nodes AS BN_nodes1,   Entity_BN_nodes AS BN_nodes2    WHERE   BN_nodes1.pvid = BN_nodes2.pvid    AND (NOT (EXISTS( SELECT  *  FROM    Entity_BayesNets    WHERE  (Entity_BayesNets.pvid = BN_nodes1.pvid)   AND (Entity_BayesNets.child = BN_nodes1.node)  AND (Entity_BayesNets.parent = BN_nodes2.node))));");
	    st.execute("insert ignore into Path_Forbidden_Edges select distinct RNodes_pvars.rnid AS Rchain, Entity_Complement_Edges.child AS child, Entity_Complement_Edges.parent AS parent FROM (RNodes_pvars, Entity_Complement_Edges)   WHERE  (RNodes_pvars.pvid = Entity_Complement_Edges.pvid) ;");
	    st.close();
				
		//handle rnodes in a bottom-up way following the lattice
	    // Generating .CSV files by reading _CT tables directly (including TRUE relationship and FALSE relationship)
		handleRNodes_zqian(); //import			

		long l2 = System.currentTimeMillis();  //@zqian : measure structure learning time
		System.out.print("Structure Learning Time(ms): "+(l2-l)+" ms.\n");

		
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
			ResultSet rs = st.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_counts`;");
			while(rs.next()){
				NoTuples = rs.getString(1);
				System.out.println("NoTuples : " + NoTuples);
			}
			
			if(Integer.parseInt(NoTuples)>1){
				/* Diljot,Chris : using the globalScoreHash*/
				ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
						databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
						databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml",
						globalScoreHash
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

	 public static void handleRNodes_zqian() throws Exception {
    	
    	for(int len = 1; len <= maxNumberOfMembers; len++){
			
			readRNodesFromLattice(len);	 //create csv files for all rnodes
			
			//required edges
			for(String id : rnode_ids)			//rchain  
	        {   System.out.println("\n !!!!Staring  to Export The Required Edges to "+id.replace("`","") +  "_req.xml \n");
				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml", "Rchain", "Path_Required_Edges", id, con2);
			   // System.out.println("export to _req.xml::rnode_id::"+id); //@zqian Test	
			 }
			
			//forbidden edges
			for(String id : rnode_ids)  
			{   System.out.println("\n !!!!Staring  to Export The Forbidden Edges to"+id+ "_for.xml \n");
				BIFExport.Export(databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml", "Rchain", "Path_Forbidden_Edges", id, con2);
			    //System.out.println("export to _for.xml::rnode_id::"+id); //@zqian Test	

			}
						
			String NoTuples=""; 	
			for(String id : rnode_ids) { 
				System.out.println("\nStarting Learning the BN Structure of rnode_ids: " + id+"\n");
				Statement st = con3.createStatement();
				ResultSet rs = st.executeQuery("SELECT count(*) FROM `"+id.replace("`","")+"_counts`;");
				while(rs.next()){
					NoTuples = rs.getString(1);
					System.out.println("NoTuples : " + NoTuples);
				}
				if(Integer.parseInt(NoTuples)>1){
				/* Diljot,Chris : using the globalScoreHash from BayesBase.java in the function.*/
				ca.sfu.jbn.BayesNet_Learning_main.tetradLearner(
								databaseName+"/" + File.separator + "csv" + File.separator + id.replace("`","") + ".csv",
								databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_req.xml",
								databaseName+"/" + File.separator + "kno" + File.separator + id.replace("`","") + "_for.xml",
								databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml"
								,globalScoreHash
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
			st.execute("insert ignore into InheritedEdges select distinct lattice_rel.child AS Rchain, Path_BayesNets.child AS child,  Path_BayesNets.parent AS parent   FROM   Path_BayesNets,  lattice_rel,lattice_set    WHERE    lattice_rel.parent = Path_BayesNets.Rchain    AND Path_BayesNets.parent <> ''     and lattice_set.name=lattice_rel.parent	and lattice_set.length = "+(len)+"    ORDER BY Rchain;");
			// make inherited edges as required edges, while avoiding conflict edges			
			st.execute("insert ignore into Path_Required_Edges select distinct Rchain, child, parent  from  InheritedEdges,lattice_set   where Rchain = lattice_set.name and lattice_set.length ="+ (len+1)+" and  (Rchain , parent, child) NOT IN (select   *  from   InheritedEdges) ;");
			// for path_complemtment edges, rchain should be at current level (len) 
			st.execute("insert ignore into Path_Complement_Edges select distinct  BN_nodes1.Rchain AS Rchain,  BN_nodes1.node AS child, BN_nodes2.node AS parent    FROM   Path_BN_nodes AS BN_nodes1,   Path_BN_nodes AS BN_nodes2,   lattice_set    WHERE lattice_set.name=BN_nodes1.Rchain and lattice_set.length ="+ len+" and  ((BN_nodes1.Rchain = BN_nodes2.Rchain)   AND (NOT (EXISTS( SELECT *   FROM  Path_BayesNets  WHERE ((Path_BayesNets.Rchain = BN_nodes1.Rchain)   AND (Path_BayesNets.child = BN_nodes1.node)    AND (Path_BayesNets.parent = BN_nodes2.node))))));");
			// for path forbidden edges, rchain should be at higher level (len+1) , so its parent should be at current level (len)
			// make absent edges as forbidden edges, and give higher priority of required edges in case of conflict edges 	
			st.execute("insert ignore into Path_Forbidden_Edges select distinct  lattice_rel.child AS Rchain, Path_Complement_Edges.child AS child, Path_Complement_Edges.parent AS parent    FROM  Path_Complement_Edges,        lattice_rel,    lattice_set    WHERE  lattice_set.name = lattice_rel.parent and lattice_set.length = "+ len+" and  lattice_rel.parent = Path_Complement_Edges.Rchain  AND Path_Complement_Edges.parent <> ''   and  (lattice_rel.child , Path_Complement_Edges.child,  Path_Complement_Edges.parent) not in (select  Rchain,child,parent from  Path_Required_Edges) ;");

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

			//  create new statement
			Statement st3 = con3.createStatement();

			String queryString= "SELECT * FROM `"+pvid.replace("`", "")+"_counts` limit 1;";
			ResultSet rs4 = st3.executeQuery(queryString);
			System.out.print("query string : "+queryString);
			
			//  create header
			ArrayList<String> columns = getColumns(rs4);
			String csvHeader = StringUtils.join(columns, "\t");
			System.out.println("\nCSV Header : " + csvHeader+ "\n");

		if(FirstRunning==1)
		{
			//  create csv file
			RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + pvid + ".csv", "rw");
			//csv.writeBytes(csvHeader + "\n");

			ResultSet rs5 = st3.executeQuery(queryString);
			while(rs5.next()){
				String csvString = "";
				for(String col : columns){
					csvString += rs5.getString(col) + "\t";
				}
				csvString = csvString.substring(0, csvString.length() - 1);
				//csv.writeBytes(csvString + "\n");
		        }
		}
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
		
		String queryString= "SELECT * FROM `"+rchain.replace("`", "")+"_CT` limit 1;";
		ResultSet rs5 = st3.executeQuery(queryString);
		System.out.print("query string : "+queryString);
		
		//  create header
		ArrayList<String> columns = getColumns(rs5);
		String csvHeader = StringUtils.join(columns, "\t");
		System.out.println("\n CSV Header : " + csvHeader+ "\n");

		
	if(FirstRunning==1)
	{
		//  create csv file, reading data from _CT table into .csv file
		RandomAccessFile csv = new RandomAccessFile(databaseName+"/" + File.separator + "csv" + File.separator + rchain.replace("`", "") + ".csv", "rw");
		
		//csv.writeBytes(csvHeader + "\n");

		ResultSet rs6 = st3.executeQuery(queryString);
		while(rs6.next()){
			String csvString = "";
			for(String col : columns){
				csvString += rs6.getString(col) + "\t";
			}
			csvString = csvString.substring(0, csvString.length() - 1);
			//csv.writeBytes(csvString + "\n");
		}
	}
		//  close statements
		st3.close();
		
		rnode_ids.add(rchain);

	}

	rs.close();
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
	
// import to Path_BayesNets	
	public static void bif2(String id) throws SQLException, IOException, ParsingException {
		System.out.println(" Starting to Import the learned path into MySQL::**Path_BayesNets**"); //@zqian Test	

		Statement st = con2.createStatement();
		//st.execute("truncate Path_BayesNets;");
		int j=0;
		
			BIFImport.Import(databaseName+"/" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Path_BayesNets", con2);
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