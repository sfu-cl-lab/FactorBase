/* Feb 7th 2014, zqian;
        //Make sure each node appear as a child
        // <child,''>, <child,parent>      
 * */
/*Change ChildValue to FID -- Feb 7 Yan
*/
/*
 * exporting the Bayes Net into a BIF file 
 * 1.get the structure from table  _BN.Path_BayesNets
 * 2.get the parameter from tables _CP_smoothed
 * 
 * 
 * Jun 25, zqian
 * 
 * Given a Bayes Net stored in the databases with structure and parameters,
 * this program could generator the BIF file which can be feeded into UBC tools.
 * 
 * For the position property, we may need to optimize it.
 * */

//query for Rchain=>SELECT distinct lattice_set.name FROM lattice_membership,lattice_set where length=( SELECT max(length) FROM lattice_set);


/*To do: Samarth, replace the short_rnid with orig_rnid*/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;


public class BIF_Generator {
	static Connection con;
	
	
	static Connection con1, con2, con3;

	//  to be read from config
	static String databaseName, databaseName2, databaseName3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	
	public static void main(String[] args) throws SQLException, IOException {
		
		//read config file
		setVarsFromConfig();
		connectDB();
		
		//mapping the orig_rnid back and create a new table: Final_Path_BayesNets.
		//Final_Path_BayesNets(con2);
		
		
		generate_bif(databaseName,"src/Bif_"+databaseName+".xml",con2);
		
		
	    
		
		disconnectDB();
	
		
	}
	
	
	public static void generate_bif(String network_name, String bif_file_name_withPath, Connection conn) throws SQLException, IOException{
		System.out.println("\n BIF Generator starts");
		
		Statement st=(Statement) conn.createStatement();
		File file = new File(bif_file_name_withPath);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        
        output.write(writeBifHeader());
        
        output.write(writeNetworkBegin(network_name));
        
        ArrayList<String> variables=new ArrayList<String>();//arrayList to store nodes
        ArrayList<Integer> outcomes=new ArrayList<Integer>();//no of outcomes for each node
        
        ArrayList<String> rnid=new ArrayList<String>();//rnid
        ArrayList<String> orig_rnid=new ArrayList<String>();//orig_rnid
        ResultSet rst=st.executeQuery("select * from lattice_mapping");
        while(rst.next()){
        	rnid.add(rst.getString("short_rnid").substring(1,rst.getString("short_rnid").length()-1 ));//removing apostrophe and then adding
        	orig_rnid.add(rst.getString("orig_rnid").substring(1,rst.getString("orig_rnid").length()-1));//removing apostrophe and then adding
        }
        
       // Feb 7th 2014, zqian;
        //Make sure each node appear as a child
        // <child,''>, <child,parent>
         rst=st.executeQuery("SELECT distinct child FROM Path_BayesNets ;");
        while(rst.next()){
      	  variables.add(rst.getString(1).substring(1,rst.getString(1).length()-1));//removing apostrophe and then adding 
                }
        
        
        
        ArrayList<String> values;
        int x=6000;int y=4000;//x and y positions of nodes
        int  a=0;//as a counter variable
        
        //writing the VARIABLE TAGS
        //change ChildValue to FID Feb 7 Yan
        for(int i=0;i<variables.size();i++){
      	  values=new ArrayList<String>();
      	  //POSSIBLE OUTCOMES FOR A VARIABLE
      	  ResultSet rst1=st.executeQuery("select distinct `" + variables.get(i) + "` from `"+variables.get(i)+"_CP_smoothed` order by `" + variables.get(i) + "` ;");
      	  while(rst1.next()){
      		  values.add(rst1.getString(1));
      	  }
      	  outcomes.add(values.size());
      	  
      	  //updated on Jun 28, mapping the orig_rnid back 
      	  //checking if the variable is rnid then replacing its name by orig_rnid eg: `a` to be replaced `RA(stud0,prof0)`
      	 int z=0;
     	  for(int s=0;s<rnid.size();s++){
     		  if(variables.get(i).equals(rnid.get(s))){
     			 
     			output.write(writeVariable(orig_rnid.get(s),values,x,y)); 
     			z=1;
     			break;
     		  }
     	  }
     	  if(z==0){
     	  output.write(writeVariable(variables.get(i),values,x,y));
     	  }
      	 
      	  
      	  a++;
      	  x=x+200;
      	  if(a==3){
      		  a=0;
      		  y=y+200;
      		  x=x-600;
      	  }
      	  
      	  
        }
        //***********************************************//
       //WRITING THE DEFINITION TAGS 
        
        ArrayList<String> given=null;
        ResultSet rst2;
      
        for(int i=0;i<variables.size();i++){
      	  
      	  
      	  given=new ArrayList<String>();
      	  String table_name="`"+variables.get(i)+"_CP_smoothed`";
      	  String probabilities="";
      	  
      	  
      	  rst2=st.executeQuery("SELECT distinct lattice_set.name FROM lattice_membership,lattice_set where length=( SELECT max(length) FROM lattice_set);");
      	  rst2.next();
      	  String Rchain=rst2.getString(1);
      	 // System.out.println("Rchain:"+Rchain);
      	  rst2=st.executeQuery("select distinct parent from Path_BayesNets where child= '`"+variables.get(i)+"`' and parent !=''and  Rchain='"+Rchain+"' ;");
      	  
      	
      	  int nop=0;//nop -> no. of. parents for a particular node 
      	  while(rst2.next())nop++;
      	  
      	  
      	  if(nop>0){
      	  rst2.beforeFirst();
      	  while(rst2.next()){
      		  given.add(rst2.getString(1).substring(1,rst2.getString(1).length()-1));
      	  }
      	  //print(given);
      	  //order of values according to xmlbif specifications
      	  //change ChildValue to FID -- Feb 7 Yan
      	  String order="`"+given.get(0)+"`";
      	  
      	  for(int k=1;k<given.size();k++){
      		  order=order+", `"+given.get(k)+"`";
      	  }
      	  order=order+", `" + variables.get(i) + "`";
      	  String query="select CP from "+table_name+" order by "+order+" ;";
      	 // System.out.println(query+"\n"); //zqian
      	  rst2=st.executeQuery(query);
      	  
      	  
      	  int size=0;
      	  while(rst2.next()){
      		  size++;
      	  }
      	  rst2.beforeFirst();
      	  int n=size/outcomes.get(i);
      	  for(int l=1;l<=n;l++){ 
      	   int subtot=0;
      	  // for(int j=1;j<=(outcomes.get(i)-1);j++){
      	  for(int j=1;j<=(outcomes.get(i));j++){
      	    rst2.next();
      	  // System.out.println(j + ": "  + rst2.getString(1)+"\n"); //zqian
      	    probabilities=probabilities+" "+rst2.getString(1);
      	    //KLD generator has been modified s.t. probabilities sum to 1, and have at most 6 digits are .  Oliver
      	    //getString(1) is assigned the conditional probability (CP) as a string
      		//subtot=subtot+Integer.parseInt((rst2.getString(1).substring(2,rst2.getString(1).length())));
      	//	System.out.println("subtot: " + subtot);
      		
      		//convert string representing a conditional probability to an integer. 
      		}
      		
      	 /*  rst2.next();
      		
      		  int last=10000-subtot;
      		  double  last1=((double)last/10000);
      		  probabilities=probabilities+" "+last1;*/
      		  
      		  }
      	 
      	  }
      	  //WHEN node or variable does not have any parents
      	  else{
      		  
      		  //change ChildValue to FID -- Feb 7 Yan
      		rst2=st.executeQuery("select CP from "+table_name+" order by `" + variables.get(i) + "`");
      		
      		while(rst2.next()){
      			
        		  probabilities=probabilities+" "+rst2.getString(1);
        		  
        	  }

      	 
      	  }
      	  //checking if the node or given values does not contain rnid, and then replacing the rnid with origrnid
      	  String node=variables.get(i);
      	  for(int s=0;s<rnid.size();s++){
      		  if(node.equals(rnid.get(s))){
      			  node=orig_rnid.get(s);break;
      		  }
      	  }
      	  
      	  ArrayList<String> given1=new ArrayList<String>();//given1 to store given values without rnid but with orig_rnid
      	for(int m=0;m<given.size();m++){
			  boolean flag=true;
			  
			  for(int s=0;s<rnid.size();s++){
				  if(given.get(m).equals(rnid.get(s))){
					  flag=false;
					  given1.add(orig_rnid.get(s));
					  break;
				  }
					 
			  }
			 if(flag){
				 given1.add(given.get(m));
			 }
			  
		  }
      	  
      	  
      	  
      	 output.write(writeDefinition(node, given1,probabilities));
      	  
      	  
        }
        
        output.write(writeNetworkEnd());
        
        output.close();
        st.close();
    	System.out.println("BIF Generator Ends for "+network_name);
		
	}
	
	public static void Final_Path_BayesNets( Connection conn, String rchain) throws SQLException, IOException{
		
		ArrayList<String>orig_rnid=new ArrayList<String>();
		ArrayList<String>rnid=new ArrayList<String>();
		Statement st1=(Statement) conn.createStatement();
		//st1.execute("drop table if exists `Final_Path_BayesNets` ;");
		//creating table Final_Path_BayesNets
		String query1="CREATE TABLE `Final_Path_BayesNets` ( Select * from `Path_BayesNets` where Rchain='"+rchain+"' and parent<>'' ) ;";
		//System.out.println(query1);
		//st1.execute(query1);
		//adding primary key to Final_Path_BayesNets
		//st1.execute("alter table `Final_Path_BayesNets` add primary key (  `Rchain`,`child`,`parent`)  ;");
		
		//ResultSet rst=st1.executeQuery("select * from lattice_mapping ;");
		/*
		while(rst.next()){
			orig_rnid.add(rst.getString("orig_rnid"));
			rnid.add(rst.getString("short_rnid"));
		}
		for(int i=0;i<rnid.size();i++){
			String query2="update  `Final_Path_BayesNets` set Rchain= '"+orig_rnid.get(i)+"' where Rchain = '"+rnid.get(i)+"' ;";
			String query3="update  `Final_Path_BayesNets` set child= '"+orig_rnid.get(i)+"' where child = '"+rnid.get(i)+"' ;";
			String query4="update  `Final_Path_BayesNets` set parent= '"+orig_rnid.get(i)+"' where parent = '"+rnid.get(i)+"' ;";
		 
			st1.execute(query2);
			st1.execute(query3);
			st1.execute(query4);
		}
		
		st1.close();
		*/
	}
	
	public static void print(ArrayList<String> al){
		for(int i=0;i<al.size();i++)
			System.out.println(al.get(i));
	}
	
	public static String writeBifHeader() {
	String s="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<BIF VERSION=\"0.3\"  xmlns=\"http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3 http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3/XMLBIFv0_3.xsd\">\n";
    return s;
	}
	
	public static String writeNetworkBegin(String name) {
		return "<NETWORK>\n<NAME>" + name + "</NAME>\n";
	}
	
	public static String writeNetworkEnd() {
		return "</NETWORK>\n</BIF>\n";
	}
	
	public static String writeVariable(String variable, ArrayList<String> outcomes,double x, double y) {
		String position="("+x+","+y+")";
		StringBuilder builder = new StringBuilder("<VARIABLE TYPE=\"nature\">\n");
		builder.append("\t<NAME>");
		builder.append(variable);
		builder.append("</NAME>\n");
		for (String outcome : outcomes) {
			builder.append("\t<OUTCOME>");
			builder.append(outcome);
			builder.append("</OUTCOME>\n");
		}
		builder.append("\t<PROPERTY> position=");
		builder.append(position);
		builder.append("</PROPERTY>\n");		
		builder.append("</VARIABLE>\n");
		return builder.toString();
	}
	
	public static String writeDefinition(String forVariable, ArrayList<String> givenVariables,String probabilities) {
		StringBuilder builder = new StringBuilder("<DEFINITION>\n");
		builder.append("\t<FOR>");
		builder.append(forVariable); 
		builder.append("</FOR>\n");
		for (String given : givenVariables) {
			builder.append("\t<GIVEN>");
			builder.append(given); 
			builder.append("</GIVEN>\n");
			
		}
		builder.append("\t<TABLE>");
		builder.append(probabilities);
		builder.append("</TABLE>\n");
		
		builder.append("</DEFINITION>\n");
		return builder.toString();
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

	
	
	public static void disconnectDB() throws SQLException {
		con1.close();
		con2.close();
		con3.close();
	}
}