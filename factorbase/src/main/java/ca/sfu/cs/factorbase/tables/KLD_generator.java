package ca.sfu.cs.factorbase.tables;

/* updated on Feb 6, 2014, fix the bug sum not equals to 1
 *Feb 6 Yan 
 * Change ChildValue to FID
 * Fix bug: CP sum up to 1 in IMDB
*/
/* Merge three programs together: pair, join_CP, markov_Blanket.
 * See workflow document, workflow.txt. For parameter estimation workflow, see flow_diagram_for_CP.tif.
 * Goal: generate smoothed CP table, 
 *       use smoothed conditional probability to calculate KLD for the biggest rchain,
 *       calculate CLL for each node
 *      And also generate a plot for cll_db and cll_jp. Part not working.
 * */

/*
 * Jun 25 zqian
 * This program computing some parameters will be used in the evaluation section.
 * And it's usefull for BIF_Generator.java
 * could output a simple plot of cll_db and cll_jp.
 * */

/*Feb 6 Yan 
 * Change ChildValue to FID
 * Fix bug: CP sum up to 1 in IMDB*/

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ca.sfu.cs.factorbase.app.Config;


public class KLD_generator {
	static String databaseName, databaseName2,databaseName3;
	static String dbUsername = "";
	static String dbPassword = "";
	static String dbaddress = "";
	static Connection con2;
	
	//list of columns in original CP table (node_CP) w/o score columns, e.g.: ChildeValue, b, grade, sat...
	static ArrayList<String> list;
	//list of node columns in the biggest rchain table, also in KLD table
	static ArrayList<String> column_names=new ArrayList<String>();
	//list of conditional probability columns for each node in the rchian, used in KLD table
	static ArrayList<String> column_names_CP=new ArrayList<String>();
	
	
	public static void main(String[] args) throws Exception   {
		setVarsFromConfig();
		connectDB();
		
//		long l = System.currentTimeMillis();
		
		KLDGenerator(databaseName,con2);
				
//		long l2 = System.currentTimeMillis();  //@zqian : measure structure learning time
//		System.out.print("smoothed_CP Time(ms): "+(l2-l)+" ms.\n");
		
	    con2.close();
	}
	public static void setVarsFromConfig(){
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		databaseName2 = databaseName + "_BN";
		databaseName3 = databaseName + "_CT";
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	
	public static void KLDGenerator(String database,Connection con2) throws Exception{
		System.out.println("KLD Generator starts");
	 	String Rchain="";
		Statement st = con2.createStatement();
		ResultSet rs = st.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );
		while(rs.next()){
			Rchain = rs.getString("RChain");
			//System.out.println("\n Longest  RChain : " + Rchain);
		}
		rs.close();
		st.close();
		
		smoothed_CP(Rchain,con2); //updated the pairs generator Jun 19
		System.out.println("smoothed CP tables are already to use.");
	//	System.out.println("************************");

		
		create_join_CP(database,Rchain,con2);  //input the biggest rchain
		System.out.println("KLD table is already to use.");
	//	System.out.println("************************");
		
		generate_CLL(Rchain,con2);
		System.out.println("CLL tables are already to use.");
		//System.out.println("************************");
		
		plot_CLL(database,Rchain,con2);
		
		
		System.out.println("\nKLD Generator Ends");
		
	}
	
	
	/**
	 * @throws Exception ****************************************************************************************************************/
	//main function to generate all smoothed CP table 
	public static void smoothed_CP(String rchain,Connection con2) throws Exception {
		
		java.sql.Statement st = con2.createStatement();
		
		//find all the nodes that have parents and store them in list final_tables1
		ArrayList<String> final_tables1 = new ArrayList<String>();
		
		// need to specify Rchain 
		
		ResultSet rst = st.executeQuery("SELECT distinct child FROM Path_BayesNets where parent<>'' AND Rchain='" + rchain + "' ;");
		while(rst.next()) {
			//System.out.println(rst.getString(1).substring(0,rst.getString(1).length()-1)+"_final`");
			final_tables1.add(rst.getString(1).substring(0,rst.getString(1).length()-1) + "_CP`");
		}
		
		//generate full pairs table and smooth CP table for each node in final_tables1
		for(int i=0; i<final_tables1.size(); i++) {
		//	System.out.println("child: " + final_tables1.get(i));
			new_table_smoothed(final_tables1.get(i),con2); //updating the full pairs generator
			
		}
		
		
		/**another situation for some nodes that do NOT have any parent, OCT 22, zqian*/
		System.out.println("for some nodes that do NOT have any parent.");
		//find all the nodes that do NOT have parents and store them in list final_table_smoothed
		//rst = st.executeQuery("Select Fid from FNodes where Fid not in (SELECT distinct child FROM Path_BayesNets where parent<>'')");
		rst=st.executeQuery("SELECT child FROM Path_BayesNets WHERE rchain='" + rchain + "' AND child not in (SELECT distinct child FROM Path_BayesNets WHERE parent<>'' AND Rchain = '" + rchain + "');");
		ArrayList<String> final_tables_smoothed = new ArrayList<String>();
		while(rst.next()){
			//System.out.println("parent: " + rst.getString(1));
			final_tables_smoothed.add(rst.getString(1));
		}
		
		//generate smooth CP table for each node in final_tables_smoothed
		for(int i=0; i<final_tables_smoothed.size(); i++){
			//create smoothed CP table for each node
			String table_name = final_tables_smoothed.get(i).substring(0,final_tables_smoothed.get(i).length()-1 ) + "_CP_smoothed`";
			String orig_table = final_tables_smoothed.get(i).substring(0,final_tables_smoothed.get(i).length()-1 ) + "_CP`";// fixed Oct 22, zqian, missing on '`'
            System.out.println("create table " + table_name + " as select * from " + orig_table + " ;"); //zqian Oct 22, testing

			String nodeName = final_tables_smoothed.get(i);
			
			st.execute("drop table if exists " + table_name + " ;");
			st.execute("create table " + table_name + " as select * from " + orig_table + " ;");
			System.out.println("create table " + table_name + " as select * from " + orig_table + " ;"); //zqian Oct 22, testing

			//update values of mult an CP. We add a virtual count of 1 to each pair (family state). This is where the smoothing happens.
			st.execute("update " + table_name+" set mult=mult+1");
			ResultSet rst1 = st.executeQuery("select sum(mult) from " + table_name + "; ");
			rst1.absolute(1);
			long sum_mult = rst1.getLong(1);
			st.execute("update " + table_name + " set cp=mult/" + sum_mult + ";");
			
			//make CP sum up=1
		//	System.out.println("CP already *******************");
			System.out.println("Select " + nodeName + " From " + table_name + ";");
			ResultSet rst2 = st.executeQuery("Select " + nodeName + " From " + table_name + ";");
			rst2.absolute(1);
		//	System.out.println(rst2.getString(1));
			String CV = rst2.getString(1);
			ResultSet rst3 = st.executeQuery("select sum(CP) from " + table_name + "where " + nodeName + " <> '" + CV+"';");
			rst3.absolute(1);
			float SubTotal = rst3.getFloat(1);
			//System.out.println("SubTotal : "+ SubTotal);
			if (SubTotal >= 1.0)
				SubTotal = 1 ;
			// Nov 12 zqian, this may cause minus cp value which is not acceptable of _bif file.
			//  round off issue: compare with a tiny enough number instead of using absolut value
			String query_temp2= ("update " + table_name + "set CP = (1 - " + SubTotal + ") where " + nodeName + " = '" + CV +"';");
			//System.out.println("may generate minus CP value"+query_temp2);// Nov 12 zqian
			st.execute(query_temp2);  
			
		}
			
	    st.close();
	}
	
	//generate full pairs table and smooth CP table for one node
	public static void new_table_smoothed(String table_name,Connection con2) throws Exception{
		
		java.sql.Statement st = con2.createStatement();
		String name = table_name.substring(0, table_name.length()-1) + "_smoothed`";
		
		//create smooth CP table
		st.execute("drop table if exists " + name + ";");
		//System.out.println("create table " + name + " like " + table_name + " ;");
		st.execute("create table " + name + " like " + table_name + " ;");
		
		
		//add index to 1.(MULT, ChildValue, parents....) and 2.(parent nodes...)
		ResultSet rst = st.executeQuery("show columns from " + name);
		ArrayList<String> indexlist1 = new ArrayList<String>();
		
		//rst.absolute(0);
		while(rst.next()) indexlist1.add(rst.getString(1));
		
		//delete parentsum, CP and likelihood, local_mult
		indexlist1.remove(indexlist1.size()-1);
		indexlist1.remove(indexlist1.size()-1);
		indexlist1.remove(indexlist1.size()-1);
		indexlist1.remove(indexlist1.size()-1);
        if (((String)indexlist1.get(indexlist1.size() - 1)).equals("ParentSum")) {
            indexlist1.remove(indexlist1.size() - 1);
        }

		ArrayList<String> indexlist2 = new ArrayList<String>(indexlist1);
		//delete MULT, FID
		indexlist2.remove(0);
		indexlist2.remove(0);
		
		//create clauses to add those indexes
		//index1: MULT, current node and its parents
		String index1 = "Alter table " + name + " add index " + name.substring(0, name.length()-1) + "1`(";
		for (int i=0; i<indexlist1.size(); ++i) {
			index1 = index1 + "`" + indexlist1.get(i) + "` ASC";
			if ((i+1)<indexlist1.size()) index1 = index1 + ", ";
		}
		index1 = index1 + ");";
		st.execute(index1);
		
		//index2: only parents
		String index2 = "Alter table " + name + " add index " + name.substring(0, name.length()-1) + "2`(";
		for (int i=0; i<indexlist2.size(); ++i) {
			index2 = index2 + "`" + indexlist2.get(i) + "` ASC";
			if ((i+1)<indexlist2.size()) index2 = index2 + ", ";
		}
		index2 = index2 + ");";
		st.execute(index2);
		
		
		//generate full pairs table
//		pair_table(table_name,con2);
		pairs(table_name,con2); // updated on Jun 19, much faster than sql joins
	
		
		//get all the columns, generate them as a clause
		String columns="";
		for(int i=0;i<list.size();i++){
			columns=columns+" , `"+list.get(i)+"` ";
		}
		
		//query1: insert the exist rows into smoothed CP table
		String query1="insert "+name+ " ( MULT ";
		for(int i=0;i<list.size();i++){
			query1=query1+", `"+list.get(i)+"` ";
			
		}
		query1=query1+") select MULT ";
		for(int i=0;i<list.size();i++){
			query1=query1+", `"+list.get(i)+"` ";
			
		}
		query1=query1+" from "+table_name+";";
	//	System.out.println("query1:"+query1);
		st.execute(query1);
		
		// zqian@ Oct 21, 2013, Bottleneck??

		//query2: insert not exists pairs into smoothed CP table
		String query2="insert "+name +"( MULT "+columns+" ) (select MULT "+columns +"from "+table_name.subSequence(0, table_name.length()-1)+"_pairs`"
		         +" where ("+columns.substring(2)+" ) not in ( select "+columns.substring(2)+ "from "+table_name+" )) ;";
		System.out.println("bottleneck? query2:"+query2);
		st.execute(query2);
		
		//st.execute("drop table if exists " + table_name.subSequence(0, table_name.length()-1) + "_pairs` ;");
		
		//add all MULT by 1, Laplace
		st.execute("update "+name+"set MULT=MULT+1;");
		
		//update parent sum, and conditional probability 
		 // updated on Feb 6, 2014, fix the bug sum not equals to 1
		update_ps(name,con2);
		//update_ps_compress(name,con2);
		st.close();
		
	}
	
	/*  generate full pairs table for one node 
	 * fast pairs generator, created by YanSun @ Jun 19
	 * using recursive loops in java instead of simple inefficient sql joins
	 * */
	public static void pairs(String table_name,Connection con2) throws Exception{
		
		String name = table_name.subSequence(0, table_name.length()-1) + "_pairs`";
		
		java.sql.Statement st = con2.createStatement();
		ResultSet rst = st.executeQuery("show columns from " + table_name);

		//get all value columns in original CP table
		list = new ArrayList<String>();
		rst.absolute(1);
		while(rst.next()) list.add(rst.getString(1));
		
		//delete parentsum, CP and likelihood, local_mult
		list.remove(list.size()-1);
		list.remove(list.size()-1);
		list.remove(list.size()-1);
		list.remove(list.size()-1);
        if (((String)list.get(list.size() - 1)).equals("ParentSum")) {
            list.remove(list.size() - 1);
        }

		//don't know why there is an mult=0 column in pair table, doesm't matter
		st.execute("DROP TABLE IF EXISTS " + name + ";");
		String createclause = "CREATE TABLE " + name + "( MULT int ";
		for (int i=0; i<list.size(); ++i) {
			createclause = createclause + ", `" + list.get(i) + "` VARCHAR(20) NOT NULL";
		}
		createclause = createclause + " );";
		System.out.println("createclause: " + createclause);
		st.execute(createclause);
		
		//add index to all columns in pair table
		String index_p = "alter table " + name + " add index " + name + "( MULT ASC";
		for (int i=0; i<list.size(); ++i) {
			index_p = index_p + ", `" + list.get(i) + "` ASC";
		}
		index_p = index_p + ");";
		st.execute(index_p);
		
//		System.out.println("SELECT DISTINCT `" + list.get(0) + "` FROM " + table_name);
		ResultSet rst1 = st.executeQuery("SELECT DISTINCT `" + list.get(0) + "` FROM " + table_name);
		ArrayList<String> join = new ArrayList<String>();
		ArrayList<String> join_pre = new ArrayList<String>();
		
		
		
		while(rst1.next()) {
			join.add(rst1.getString(1));
			join_pre.add(rst1.getString(1));
		}
		
		for (int k=1; k<list.size(); ++k) {
			ResultSet rst_value = st.executeQuery("SELECT DISTINCT `" + list.get(k) + "` FROM " + table_name);
			ArrayList<String> values = new ArrayList<String>();
			while (rst_value.next()) values.add(rst_value.getString(1));
			
			join.clear();
			
			for (int i=0; i<join_pre.size(); ++i) {
				for (int j=0; j<values.size(); ++j) {
					join.add(join_pre.get(i) + "," + values.get(j));
				}
			}
			
			join_pre.clear();
			for (int i=0; i<join.size(); ++i) {
				join_pre.add(join.get(i));
				//System.out.println(join.get(i));
			}
		}
		
		
		
		//write in csv file
		String filename = table_name.subSequence(1, table_name.length()-1) + ".csv";
		File ftemp=new File(filename);
		if(ftemp.exists()) ftemp.delete();
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		for (int i=0; i<join.size(); ++i) {
			writer.print("0," +  join.get(i) + "\n");
		}		
		writer.close();
	
		//load csv file into database
		st.execute("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE " + name + "  FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n';");
		
		if(ftemp.exists()) ftemp.delete();
		
	}
	
	
	
	/*generate full pairs table for one node. By "pair" we mean pair of child-value + parent-state
	 * 
	 * this is the naive approach using many joins, which is very time consuming if there're many attributes  zqian@Jun 19  
	 *  Yan fixed use by using files rather than SQL queries
	 */
	//we don't use pair_table any more, it is not efficient
	/*public static void pair_table(String table_name,Connection con2) throws SQLException{
		
		String name=table_name.subSequence(0, table_name.length()-1)+"_pairs`";
		
		java.sql.Statement st=con2.createStatement();
		ResultSet rst=st.executeQuery("show columns from "+ table_name);
		//System.out.println("show columns from "+ table_name);
		
		//get all value columns in original CP table
		list=new ArrayList<String>();
		rst.absolute(1);
		while(rst.next()) list.add(rst.getString(1));
		//delete parentsum, CP and likelihood, local_mult
		list.remove(list.size()-1);
		list.remove(list.size()-1);
		list.remove(list.size()-1);
		list.remove(list.size()-1);
		
		//generate clause for create full pairs table 
		String fromclause=" "+table_name+" as `0`";
		String selectclause=" `0`.`"+list.get(0)+"` ";
		for(int i=1;i<list.size();i++){
			fromclause= fromclause+", "+table_name+" as `"+i+"`";
			selectclause=selectclause+", `"+i+"`.`"+list.get(i)+"` ";
		}
		
		fromclause=fromclause+";";
		
		st.execute("drop table if exists "+name+";");
		
		String query="create table "+name+" select distinct "+selectclause+" from "+fromclause;
	//	System.out.println(query);
		st.execute(query);
		
		
		st.close();	
		
	}
	*/
	
	
	public static void update_ps(String table_final_smoothed,Connection con2) throws SQLException{
		//Updates parent sum (counts), using the extra virtual observation. We use the Laplace correction.
		//The computation is similar to the previous computation of conditional probabilities.
		//The smoothed probabilities are normalized and have at most 6 significant digits. 
		
		java.sql.Statement st=con2.createStatement();
		java.sql.Statement st1=con2.createStatement();
		
		String nodeName = "`" + list.get(0) + "`";
		String parents="";
		for(int i=1;i<list.size();i++){
			parents=parents+" , `"+list.get(i)+"` ";
		}
		
		st.execute("drop table if exists temp1 ;");
		String query1="create table temp1 as select sum(mult) as parsum " +parents+" from "+ table_final_smoothed+" group by "+ parents.substring(2);
		System.out.println( "query1 : "+ query1);
		
		st.execute(query1);
		
		//add index to temp1 (parsum and all parents)
		String index_t = "Alter table temp1 add index temp1( `parsum` ASC";
		for (int i=1; i<list.size(); ++i) {
			index_t = index_t + ", `" + list.get(i) + "` ASC";
		}
		index_t = index_t + ");";
		st.execute(index_t);
		
		// zqian@ Oct 21, 2013, Bottleneck??
		String query2="update "+table_final_smoothed+" set ParentSum = ( select temp1.parsum from temp1 where ";
			
		//compute parent count as before. Later we apply smoothing.
		
		String compare=" temp1.`"+list.get(1)+"`= "+table_final_smoothed+".`"+list.get(1)+"` ";
		for(int i=2;i<list.size();i++){
			compare=compare+"and temp1.`"+list.get(i)+"`= "+table_final_smoothed+".`"+list.get(i)+"` ";
		}
			
		query2=query2+" "+compare+");";	
		System.out.println( "query2 : "+ query2);

		st.execute(query2);
		st.execute("update "+table_final_smoothed+"set CP = MULT / ParentSum ;");
		
		//update Cp sum up=1; Changed Feb 6 (prev not work in imdb combine big CP and 0)
		//change CildValue to FID
		//ResultSet rst_temp = st.executeQuery("Select distinct " + nodeName + " from " + table_final_smoothed + ";");
		//rst_temp.absolute(1);
		//String CV1 = rst_temp.getString(1);   //the first value of current node
		
		ResultSet rst_temp1 = st.executeQuery("Select distinct" + parents.substring(2) + " from temp1;");
		String whereclause="";
		System.out.println("parents.substring(2) : " + parents.substring(2) );
		//rst_temp1.absolute(0);
		while(rst_temp1.next()) {
			for (int i=1; i<list.size(); ++i) {
				whereclause = whereclause + "`" + list.get(i) + "`='" + rst_temp1.getString(i) + "' AND ";
			}
			System.out.println("whereclause : " + whereclause );

			//let CV1 be the childvalue with the largest CP  Feb 6 Yan
			System.out.println("Select distinct " + nodeName + " from " + table_final_smoothed + " where " + whereclause.substring(0, whereclause.length()-4) + " order by CP desc;");
			ResultSet rst_temp = st1.executeQuery("Select distinct " + nodeName + " from " + table_final_smoothed + " where " + whereclause.substring(0, whereclause.length()-4) + " order by CP desc;");

			String CV1 = null; 
			if (rst_temp.absolute(1)){
				CV1 = 	rst_temp.getString(1);
				System.out.println(nodeName + " CV1: " + CV1);
				
				//System.out.println(whereclause);
				ResultSet rst_temp2 = st1.executeQuery("Select sum(CP) from " + table_final_smoothed + "where " + whereclause + " " + nodeName + " <> '" + CV1 + "';");
				rst_temp2.absolute(1);
				float SubTot = rst_temp2.getFloat(1);
				//if (SubTot >= 1.0)
				//	SubTot = 1 ;
				//System.out.println("SubTot : "+ SubTot);
				String query_temp1 = "Update " + table_final_smoothed + " Set CP = 1-" + SubTot + " Where " + whereclause + " " + nodeName + "='" + CV1 + "';";
				//System.out.println("may generate minus CP value"+query_temp1);// Nov 12 zqian
				st1.execute(query_temp1);	
				
			}				
			
			whereclause = "";
			
		}
	
		//st.execute("drop table temp1 ;");
	   // st.close();
	}
	
	
	public static void update_ps_compress(String table_final_smoothed,Connection con2) throws SQLException{
		//Updates parent sum (counts), using the extra virtual observation. We use the Laplace correction.
		//The computation is similar to the previous computation of conditional probabilities.
		//The smoothed probabilities are normalized and have at most 6 significant digits. 
		
		java.sql.Statement st=con2.createStatement();
		java.sql.Statement st1=con2.createStatement();
		String parents="";
		for(int i=1;i<list.size();i++){
			parents=parents+" , `"+list.get(i)+"` ";
		}
		
		st.execute("drop table if exists temp1 ;");
		String query1="create table temp1 as select sum(mult) as parsum " +parents+" from "+ table_final_smoothed +" group by "+ parents.substring(2);
		st.execute(query1);
		
		//add index to temp1
		
		String index_t = "Alter table temp1 add index temp1( `parsum` ASC";
		for (int i=1; i<list.size(); ++i) {
			index_t = index_t + ", `" + list.get(i) + "` ASC";
		}
		index_t = index_t + ");";
		st.execute(index_t);
		
		//compress all parents columns. Yan@Nov 8. 
		//add a new column cprs=`parent1`,`parent2`,...  Same as temp1
		//update parentsum as by compare these two cprs columns in two tables.
		//feel free to drop this column (cprs) in smoothed CP table.	
		st.execute("Alter Table " + table_final_smoothed + " Add column `parent_cprs` VARCHAR(200);");
		st.execute("Alter Table temp1 Add column `parent_cprs` VARCHAR(200);");
		String sql_cprs = "Update " + table_final_smoothed + " Set `parent_cprs` = Concat( `" + list.get(1) + "` ";
		String sql_cprs_temp = "Update temp1 Set `parent_cprs` = Concat( `" + list.get(1) + "` ";
		for (int i=2; i<list.size(); ++i) {
			sql_cprs = sql_cprs + ", `" + list.get(i) + "` ";
			sql_cprs_temp = sql_cprs_temp + ", `" + list.get(i) + "` ";
		}
		sql_cprs = sql_cprs + ");";
		sql_cprs_temp = sql_cprs_temp + ");";
		st.execute(sql_cprs);
		st.execute(sql_cprs_temp);
		
		
		// zqian@ Oct 21, 2013, Bottleneck??
		String query2="update "+table_final_smoothed+", temp1 set " + table_final_smoothed + ".ParentSum = temp1.parsum where temp1.`parent_cprs`="  + table_final_smoothed + ".`parent_cprs`";
			
		//compute parent count as before. Later we apply smoothing.
		
		//String compare=" temp1.`"+list.get(1)+"`= "+table_final_smoothed+".`"+list.get(1)+"` ";
		//for(int i=2;i<list.size();i++){
		//	compare=compare+"and temp1.`"+list.get(i)+"`= "+table_final_smoothed+".`"+list.get(i)+"` ";
		//}
			
		
		st.execute(query2);
		st.execute("update "+table_final_smoothed+"set CP = MULT / ParentSum ;");
		
		//update Cp sum up=1; Changed Feb 6 (prev not work in imdb combine big CP and 0)
		//change CildValue to FID
		//ResultSet rst_temp = st.executeQuery("Select distinct " + nodeName + " from " + table_final_smoothed + ";");
		//rst_temp.absolute(1);
		//String CV1 = rst_temp.getString(1);   //the first value of current node
		
		ResultSet rst_temp1 = st.executeQuery("Select distinct" + parents.substring(2) + " from temp1;");
		String whereclause="";
		//rst_temp1.absolute(0);
		while(rst_temp1.next()) {
			for (int i=1; i<list.size(); ++i) {
				whereclause = whereclause + "`" + list.get(i) + "`='" + rst_temp1.getString(i) + "' AND ";
			}
			
			//let CV1 be the childvalue with the largest CP
			ResultSet rst_temp = st1.executeQuery("Select distinct " + list.get(0) + " from " + table_final_smoothed + " where " + whereclause.substring(0, whereclause.length()-4) + " order by CP desc;");
			rst_temp.absolute(0);
			String CV1 = rst_temp.getString(1);
			
			//System.out.println(whereclause);
			ResultSet rst_temp2 = st1.executeQuery("Select sum(CP) from " + table_final_smoothed + "where " + whereclause + " " + list.get(0) + " <> '" + CV1 + "';");
			rst_temp2.absolute(1);
			float SubTot = rst_temp2.getFloat(1);
			//if (SubTot >= 1.0)
			//	SubTot = 1 ;
			//System.out.println("SubTot : "+ SubTot);
			String query_temp1 = "Update " + table_final_smoothed + " Set CP = 1-" + SubTot + " Where " + whereclause + " " + list.get(0) + "='" + CV1 + "';";
			//System.out.println("may generate minus CP value"+query_temp1);// Nov 12 zqian
			st1.execute(query_temp1);
			
			whereclause = "";
			
		}
		
		//feel free to drop this column (cprs) in smoothed CP table.
		st.execute("Alter table " + table_final_smoothed + " Drop Column `parent_cprs`;");
	
		//st.execute("drop table temp1 ;");
	   // st.close();
	}

	
	/******************************************************************************************************************/
	

	
	
	
	/******************************************************************************************************************/
	//main function to generate KLD table for given Rchain
	public static void create_join_CP(String databaseCT,String rchain,Connection con2) throws SQLException{
		
		String table_name = databaseCT+"_CT."+rchain.substring(0,rchain.length()-1) + "_CT`";
		//System.out.println(databaseCT+"_CT");
		String newTable_name = rchain.substring(0,rchain.length()-1) + "_CT_KLD`";
		//System.out.println(table_name);
		// table_name is input contingency table, newTable_name will be  output KLD table
		Statement st=con2.createStatement();
        ResultSet rst=st.executeQuery("show columns from "+table_name+" ;");
       // System.out.println("show columns from "+table_name+" ;");
        while(rst.next()){
        	column_names.add("`"+rst.getString(1)+"`");
        	column_names_CP.add("`"+rst.getString(1)+"_CP`");
        	// add conditional probability column for each attribute column
        }
        
        
        st.execute("drop table if exists " + newTable_name);
        String query1="create table " + newTable_name + " ( id int(11) NOT NULL AUTO_INCREMENT, MULT BIGINT(21), " ;
        // make query string for creating KLD table
        // auto index each row in table
        for(int i=1; i<column_names.size(); i++){
        	query1 = query1 + column_names.get(i) + "  VARCHAR(45)," + column_names_CP.get(i) + " float(7,6) ,";
        // add column headers		
        }
        query1 = query1 + " JP float,  JP_DB  float, KLD  float default 0, PRIMARY KEY (id) ) engine=INNODB;";
        //System.out.println(query1);
        st.execute(query1);
                
        // copy the values from the mult table. CP values are null at this point.
        String query2 = "insert into " + newTable_name + " ( MULT";
        for(int i=1; i<column_names.size(); i++){
        	query2 = query2 + ", " + column_names.get(i);
        }
        query2 = query2 + " ) select * from "+table_name+"; "; // Nov 28 @ zqian, do not use "*" in terms of query optimization
        														// Nov 28 @ zqian, adding covering index to CT table for query string 
        System.out.println(query2);
        st.execute(query2);
          
        System.out.println("\n insert into KLD table conditional probability for each node"); // zqian
        //insert CP value to attributes
        insert_CP_Values(rchain, newTable_name,con2);
        System.out.println("\n compute Bayes net joint probabilities "); //zqian
        //compute Bayes net joint probabilities          
        cal_KLD(newTable_name,con2);
        
        
        st.close();
	}
	
	
	//insert into KLD table conditional probability for each node
	//add index to speed up the update statement
	public static void insert_CP_Values(String rchain, String newTable_name,Connection con2) throws SQLException{
		Statement st=con2.createStatement();
		
		// handle functor nodes with no parents from Path_BayesNet where rchain is given
		//ResultSet rst1=st.executeQuery("Select Fid from FNodes where Fid not in (SELECT distinct child FROM Path_BayesNets where parent<>'')");
		ResultSet rst1=st.executeQuery("SELECT child FROM Path_BayesNets WHERE rchain='" + rchain + "' AND child not in (SELECT distinct child FROM Path_BayesNets WHERE parent<>'' AND rchain='" + rchain + "');");
		
		
		ArrayList<String> no_parents=new ArrayList<String>();
		while(rst1.next()){
			no_parents.add(rst1.getString(1).substring(0,rst1.getString(1).length()-1));
			//System.out.println("child: " + rst1.getString(1).substring(0,rst1.getString(1).length()-1));
			// remove final apostrophe
		}
		
		String temp = "";
		String query1 = "";
		// for each functor node with no parents, copy conditional probability from _CP table
		//change ChildeValue to FID -- Jan 24 Yan
		for(int i=0;i<no_parents.size();i++){
			query1="update "+newTable_name;
			temp=no_parents.get(i)+"_CP_smoothed`";
	        query1=query1+" ,"+temp+" set "+no_parents.get(i)+"_CP` ="+temp+".CP where "+temp+"." + no_parents.get(i) + "` = "+ newTable_name+"."+no_parents.get(i)+"`"; 
	        System.out.println(no_parents.get(i) + "` : " + query1); 
	        st.execute(query1);
		}
		
		
		//handle nodes with parents from Path_BayesNet where rchain is given
		ResultSet rst2=st.executeQuery("SELECT distinct child FROM Path_BayesNets WHERE parent<>'' AND Rchain='" + rchain + "';");
		
		ArrayList<String> with_parents=new ArrayList<String>();
		ArrayList<String> parents;
		// stores list of parents for current child/attribute/functor node
		while(rst2.next()){
			with_parents.add(rst2.getString(1).substring(0,rst2.getString(1).length()-1));
			//System.out.println("parent: " + rst2.getString(1).substring(0,rst2.getString(1).length()-1));
		}
		
		temp = "";
		String query2 = "";
		   
		for(int i=0;i<with_parents.size();i++){
			parents=new ArrayList<String>();
		    temp=with_parents.get(i)+"_CP_smoothed`"; //temp contains name of CP table for current child
		    ResultSet rs_temp=st.executeQuery("show columns from "+temp);
		    // SQL query to find list of parents for current child. 
			rs_temp.absolute(2);
			// start with 3 row in result set, which is the 3rd column in CP table. The first two columns are MULT and FID
			     
		    while(rs_temp.next()){
		    	String s = rs_temp.getString(1);
		        if(s.charAt(0)!='`') parents.add("`"+rs_temp.getString(1)+"`");
		        else parents.add(rs_temp.getString(1));
		    }
		    
		    
		    //remove the last 4 rows, which are the last 4 columns in CP table. 
		    //these are CP, parent_sum, local_mult, log-likelihood. // remove local_mult column Dec 4th, zqian
		    parents.remove(parents.size()-1);
		    parents.remove(parents.size()-1);
		    parents.remove(parents.size()-1);		    
		    parents.remove(parents.size()-1);
		    parents.remove(parents.size()-1);
		    
		    //Nov 28 @ zqian, only add index to KLD table?  helpful for update?
		    
		    //add index to CP_smoothed and big KLD table, current node and its parents
		    String index2 = "ALTER TABLE " + newTable_name  + " ADD INDEX " + with_parents.get(i) + "` ( " + with_parents.get(i) + "` ASC ";
		    for (int j=0; j<parents.size(); ++j) {
		    	index2 = index2 + ", " + parents.get(j) + " ASC ";
		    }
		    index2 = index2 + " ) ;";
		    System.out.println("index2 : "+ index2);
		    st.execute(index2);
		    
		    query2="update "+newTable_name;
					
			query2=query2+" ,"+temp+" set "+with_parents.get(i)+"_CP` ="+temp+".CP where "+temp+"." + with_parents.get(i) + "` = "+ newTable_name+"."+with_parents.get(i)+"`"; 
			// where parents agree on values
			for (int j=0; j<parents.size(); ++j) {
				query2 = query2 + "and " + newTable_name + "." + parents.get(j) + "=" + temp + "." + parents.get(j)+ " ";
			}

			System.out.println(with_parents.get(i) + "` : " + query2);
			st.execute(query2);
		}
		
		st.close();
	}
	
	
	//calculate joined probability and KLD
	public static void cal_KLD(String newTable_name,Connection con2) throws SQLException{
		Statement st = con2.createStatement();
		
		String query_jp = "update " + newTable_name + " set JP=  " + column_names_CP.get(1);
        for(int l=2; l<column_names_CP.size(); l++){
        	query_jp = query_jp + " * " + column_names_CP.get(l);
        }
        System.out.println("KLD1: " + query_jp);
        st.execute(query_jp);
        
	    ResultSet rst = st.executeQuery("select sum(MULT) from " + newTable_name + " ;");
	    rst.absolute(1);
	    Long mult_sum = rst.getLong(1);   
	    String query1 = "update " + newTable_name + " set JP_DB=MULT/" + mult_sum+" ;";
	    System.out.println("query 1: " + query1);
	    st.execute(query1); 
	    
	    String query2="update " + newTable_name + " set KLD= (JP_DB*(log(JP_DB)-log(JP))) where MULT<>0;"; 
	    System.out.println("KLD2: " + query2);
	    st.execute(query2);
	    
	    
	    st.close();
	
	}

	
	/******************************************************************************************************************/
	
	
	
	
	
	/******************************************************************************************************************/
	//main function for generate method for all nodes in table
	//Rchain need to be given
	public static void generate_CLL(String Rchain_value,Connection con2) throws SQLException{
		Statement st=con2.createStatement();
		
		//select all nodes in given Rchain
		//should be Path_BayesNets
		ResultSet rst=st.executeQuery("SELECT distinct child from Path_BayesNets where Rchain ='"+Rchain_value+"';");
		ArrayList<String> node_list=new ArrayList<String>();
		
		while(rst.next()){
			node_list.add(rst.getString(1));
		}
		
		//generate CLL table for every node
		for(int i=0;i<node_list.size();i++){
			generate_CLL_node(node_list.get(i), Rchain_value,con2);
		
		}
		
		st.close();	
	}
	
	
	//generate CLL table for one node
	public static void generate_CLL_node(String Node_name, String Rchain_value,Connection con2) throws SQLException{
		Statement st=con2.createStatement();
		String table_name=Node_name.substring(0,Node_name.length()-1)+"_CLL`";
		//get markov blanket for given node in rchain
		ArrayList<String> node_blanket= markov_blank(Node_name, Rchain_value,con2);
		
		
		//mrk: made markov blanket as a string for query
		String mrk="";
		for(int i=0;i<node_blanket.size();i++){
			mrk=mrk+", "+node_blanket.get(i);
		}	
		st.execute("drop table if exists "+table_name+" ;" );
		
		if (mrk!="") {
			
			//create statement for CLL table 
			String query1="create table "+table_name+" ( "+Node_name +" VARCHAR (45)";	
			for(int i=0;i<node_blanket.size();i++){
				//System.out.println(node_blanket.get(i));
				query1=query1+" , "+node_blanket.get(i)+" VARCHAR (45) ";
			}
			query1=query1+", JP_DB float, JP_DB_blanket float, CLL_DB float,JP float,JP_blanket float, CLL_JP float , AbsDif Float) engine=INNODB;";
			//System.out.println(query1);
			st.execute(query1);
		
			//insert node values, JP and JP_DB into CLL table
			String query2="insert into "+table_name+ "( "+Node_name;
			query2=query2+mrk;	
			query2=query2+" , JP_DB, JP ) select "+Node_name;
			query2=query2+mrk;		
			query2=query2+" , sum(JP_DB), Sum(JP) from "+Rchain_value.substring(0,Rchain_value.length()-1)+"_CT_KLD` "+"group by "+Node_name;
			//to optimize  
			//Nov 28 @ zqian, adding covering index for group by list
			query2=query2+mrk;
			System.out.println("CLL query2: " + query2);
			st.execute(query2);
		
			//create a temp table to sum up JP and JP_DB, group by markov blanket
			st.execute("drop table if exists temp ;");
			System.out.println("Node_name "+Node_name+"\n mrk "+mrk);
		
			System.out.println("create temp: " + "create table temp select sum(JP_DB) as JP_DB_sum, sum(JP) as JP_sum "+mrk+" from "+table_name+" group by "+mrk.substring(1) + "\n");
			//to optimize  
			//Nov 28 @ zqian, adding covering index for group by list			
			st.execute("create table temp select sum(JP_DB) as JP_DB_sum, sum(JP) as JP_sum "+mrk+" from "+table_name+" group by "+mrk.substring(1));
		
			//intsert the sum of JP and JP_DB into CLL table
			String query3="update "+table_name+", temp set JP_DB_blanket=temp.JP_DB_sum, JP_blanket=temp.JP_sum where ";
			query3=query3+"temp."+node_blanket.get(0)+"="+table_name+"."+node_blanket.get(0);
			for(int i=1;i<node_blanket.size();i++){
				query3=query3+" and temp."+node_blanket.get(i)+"="+table_name+"."+node_blanket.get(i) ;
			}
			System.out.println("CLL query2: " + query3);
			st.execute(query3);
		} else {
			String query1 = "create table " + table_name + " ( " + Node_name + " VARCHAR (45)";
			query1 = query1 + ", JP_DB float, JP_DB_blanket float, CLL_DB float,JP float,JP_blanket float, CLL_JP float , AbsDif Float) engine=INNODB;";
			st.execute(query1);
			String query2 = "insert into " + table_name + "( " + Node_name + " , JP_DB, JP ) select " + Node_name;
			query2 = query2 + " , sum(JP_DB), Sum(JP) from " + Rchain_value.substring(0,Rchain_value.length()-1) + "_CT_KLD` " + "group by " + Node_name;
			System.out.println("CLL query2: " + query2);
			st.execute(query2);
			
			//because the blanke is empty, set the joint probability of the blanket equals to the joint probability of the point.
			st.execute("update " + table_name + " set JP_DB_blanket=JP_DB, JP_blanket=JP;");
			
		}
		
		
		//calculate CLL
		st.execute("update "+table_name+"set CLL_DB=log(JP_DB/JP_DB_blanket), CLL_JP=log(JP/JP_blanket) "); //updated on Jun 28
		
		st.execute("update " + table_name + " Set AbsDif = abs(CLL_DB-CLL_JP)");
		st.close();
	}

	
	//generate markov blanket for one node given its rchain, and return the result in a list
	public static ArrayList<String> markov_blank(String node_name, String Rchain_value,Connection con2) throws SQLException{
		
		//list of the children of the node 
		ArrayList<String> children=new ArrayList<String>();   
		//list of markov blanket
		ArrayList<String> blanket=new ArrayList<String>();  
		//first store markov blanket in a set to reduce the duplicate
	    Set<String> markov_blanket=new HashSet<String>();
	    
	    Statement st=con2.createStatement();
	    //select the parents of the node
	    String query1="select distinct parent from `Path_BayesNets` where child = '"+node_name+"' and Rchain= '"+Rchain_value+"' and parent <> '';";
	    //System.out.println(query1);
		ResultSet rst1=st.executeQuery(query1);
		while(rst1.next()){
			//add parents to markov blanket
			markov_blanket.add(rst1.getString(1));
			
		}
		
		//select the children of the node
		String query2="select distinct child from `Path_BayesNets` where parent = '"+node_name+"' and Rchain= '"+Rchain_value+"';";
	//	System.out.println(query2);
		ResultSet rst2=st.executeQuery(query2);
	
		while(rst2.next()){
			//add children to markov blanket and children list
			children.add(rst2.getString(1));
			markov_blanket.add(rst2.getString(1));
		}     
		
		//select the spouses of the node
		for(int i=0;i<children.size();i++){
			ResultSet rst_temp;
			String temp=children.get(i);
			String  query_temp="select distinct parent from `Path_BayesNets` where child = '"+temp+"' and Rchain= '"+Rchain_value+"' and parent <>'' and parent<> '"+node_name+"' ;";
			//System.out.println(query_temp);
			rst_temp=st.executeQuery(query_temp);
			while(rst_temp.next())markov_blanket.add(rst_temp.getString(1));
		}
				
		
		Object[] list_blanket=markov_blanket.toArray();
		for(int i=0;i<list_blanket.length;i++){
			blanket.add(list_blanket[i].toString()) ; 		
		}
		
		st.close();
		return blanket;
	}
	
	
	/******************************************************************************************************************/


	
	public static void plot_CLL(String database,String Rchain_value,Connection con2) throws SQLException, IOException {
		Statement st = con2.createStatement();
		ArrayList<Double> cll_db = new ArrayList<Double>();
		ArrayList<Double> cll_jp = new ArrayList<Double>();
		ResultSet rst_p = st.executeQuery("SELECT distinct child from Path_BayesNets where Rchain ='" + Rchain_value + "';");
		ArrayList<String> node_list=new ArrayList<String>();
		
		while(rst_p.next()){
			node_list.add(rst_p.getString(1));
		}
		

		for (int i=0; i<node_list.size(); ++i) {
			String table_name = node_list.get(i).substring(0, node_list.get(i).length()-1) + "_CLL`"; 
			ResultSet rst_cll = st.executeQuery("SELECT (CLL_DB) from " + table_name);
			//ResultSet rst_cll = st.executeQuery("SELECT avg(CLL_DB) from " + table_name);
			while(rst_cll.next()) {
				cll_db.add(rst_cll.getDouble(1));
			}
			rst_cll = st.executeQuery("SELECT (CLL_JP) from " + table_name);
			//rst_cll = st.executeQuery("SELECT avg(CLL_JP) from " + table_name);
			while(rst_cll.next()) {
				cll_jp.add(rst_cll.getDouble(1));
			}
		}
		
		double [] x = new double[cll_db.size()];
		double [] y = new double[cll_jp.size()];
		
		for (int i=0; i<cll_db.size(); ++i) {
			x[i] = cll_db.get(i);
			y[i] = cll_jp.get(i);
		}
		
		//calculate the average of cll_db & cll_jp
		double cll_db_sum = 0;
		double cll_jp_sum = 0;
		double MeanError= 0;
		for (int i=0; i<cll_db.size(); ++i) {
			cll_db_sum = cll_db_sum + x[i];
			cll_jp_sum = cll_jp_sum + y[i];
			//MeanError += Math.abs(x[i]-y[i]); 
			MeanError += (x[i]-y[i]);
		}
		
		int cll_db_size = cll_db.size();
		int cll_jp_size = cll_jp.size();
		double ave_all_db = cll_db_sum / cll_db_size;
		double ave_all_jp = cll_jp_sum / cll_jp_size;
		System.out.println("The average for cll_db is " + ave_all_db + "\n" );
		System.out.println("The average for cll_jp is " + ave_all_jp + "\n" );
		MeanError=MeanError/cll_db_size;
		System.out.println("\n The Mean Difference of CLL is " + MeanError   );
		System.out.println("\n The Relative Mean Difference of CLL is " + Math.abs(MeanError/ave_all_db)   ); // zqian Dec 5th

		//calculate the variance
		double var_sum = 0;
		double var = 0;
		for (int i=0; i<cll_db.size(); ++i) {
			var_sum = var_sum + Math.pow(((x[i]- y[i]) - MeanError), 2);
		}
		//var = Math.sqrt(var_sum)/cll_db_size;
		var = Math.sqrt(var_sum/cll_db_size);
		System.out.println("The Standard Deviation is " + var + "\n");
//		
//		//save as csv file
//		BufferedWriter br = new BufferedWriter(new FileWriter("cll.csv"));
//		StringBuilder sb = new StringBuilder();
//		 sb.append("cll_db");
//		 sb.append(",");
//		 sb.append("cll_jp");
//		 sb.append("\n");
//		for (int i=0; i<cll_db.size(); ++i) {
//		 sb.append(x[i]);
//		 sb.append(",");
//		 sb.append(y[i]);
//		 sb.append("\n");		 
//		}
//
//		br.write(sb.toString());
//		br.close();
//		
//		double a = getXc( x , y ) ;  
//		double b = getC( x , y , a ) ; 
//        double[][] line;
//        line = new double[2][2];
//        line[0][0] = 0;
//        line[0][1] = b;
//        line[1][0] = 1;
//        line[1][1] = a + b;	
//      //  System.out.println("Line " + a + "x + " +b );
//		
//		
//        // create your PlotPanel (you can use it as a JPanel)
//        Plot2DPanel plot = new Plot2DPanel();
//
//        // define the legend position
//        plot.addLegend("SOUTH");
//
//        // add a line plot to the PlotPanel
//        plot.addScatterPlot("cll plot", x, y);
//        plot.addLinePlot("cll_estimation", line);
//        plot.setAxisLabel(0,"cll_db");
//        plot.setAxisLabel(1, "cll_jp");
//        plot.setFixedBounds(0, 0, 1);
//        plot.setFixedBounds(1, 0, 1);
//
//        // put the PlotPanel in a JFrame like a JPanel
//        JFrame frame = new JFrame("CLL PLOT");
//        frame.setSize(600, 600);
//        frame.setContentPane(plot);
//        frame.setVisible(true);
//        
        
        
        String KLD="";		 	
        Statement st1 = con2.createStatement();
	
		ResultSet rs = st1.executeQuery("select name as RChain from lattice_set where lattice_set.length = (SELECT max(length)  FROM lattice_set);" );
		rs.next();
		ResultSet rs1 = st1.executeQuery("SELECT sum(kld) as KLD FROM "+database+"_BN"+".`"+rs.getString("RChain").replace("`", "")+"_CT_KLD`;");
		rs1.next();
		KLD = rs1.getString("KLD");
		System.out.println(" KLD is : " + KLD);
		System.out.println("database: " + database);
		rs1.close();
		st1.close();
		rs.close();
		st.close();
        
	}
	public static double getXc( double[] x , double[] y ){  
        int n = x.length ;  
        return ( n * pSum( x , y ) - sum( x ) * sum( y ) )   
                / ( n * sqSum( x ) - Math.pow(sum(x), 2) ) ;  
    }  
      

    public static double getC( double[] x , double[] y , double a ){  
        int n = x.length ;  
        return sum( y ) / n - a * sum( x ) / n ;  
    }  
    
    public static double getC( double[] x , double[] y ){  
        int n = x.length ;  
        double a = getXc( x , y ) ;  
        return sum( y ) / n - a * sum( x ) / n ;  
    }  
      
    private static double sum(double[] ds) {  
        double s = 0 ;  
        for( double d : ds ) s = s + d ;  
        return s ;  
    }  
      
    private static double sqSum(double[] ds) {  
        double s = 0 ;  
        for( double d : ds ) s = s + Math.pow(d, 2) ;  
        return s ;  
    }  
      
    private static double pSum( double[] x , double[] y ) {  
        double s = 0 ;  
        for( int i = 0 ; i < x.length ; i++ ) s = s + x[i] * y[i] ;  
        return s ;  
    }  
  	
	
	
	public static void connectDB() throws SQLException {
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con2 = DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}


}
