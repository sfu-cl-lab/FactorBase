
package lattice;
 
import com.mysql.jdbc.Connection;
 
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class short_rnid_LatticeGenerator {

    static Connection con2;

    static ArrayList<String> firstSets;
    static List<String> wholeSets;
    static int maxNumberOfMembers;
    final static int maxNumberOfPVars = 10;
    final static String delimiter = ",";
    
	static String databaseName, databaseName2;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;

    public static int generate(Connection con) throws SQLException {
        //connect to db using jdbc
        con2 = con;
        Statement tempst=con2.createStatement();
        //int len=tempst.execute("SELECT count(*) FROM unielwin_lattice.RNodes;");
        
        
        //generate shorter rnid, from a to z
        int fc=97; 
        char short_rnid;
        ResultSet temprs=tempst.executeQuery("select orig_rnid from RNodes;");
        ArrayList<String> tempList=new ArrayList<String>();
        while(temprs.next()) {
            tempList.add(temprs.getString("orig_rnid"));
        }
        for(int i=0;i<tempList.size();i++) {
        	short_rnid=(char)fc;           //explict type casting to convert integer to character
        	fc++;
        	tempst.execute("update RNodes set rnid='`" + short_rnid + "`' where orig_rnid='" + tempList.get(i) + "';");
        }
        tempst.close();
	



        //LATTICE read first sest from RFunctors
        readFirstSets();

        //LATTICE init -> init createdSet + truncate tables + add first sets to db
        init();

        //LATTICE generate lattice tree
        generateTree();
        
        // create a table of orig_rnid and rnid         
        mapping_rnid();

        return maxNumberOfMembers;
    }
    
    public static int generateTarget(Connection con) throws SQLException {
        //connect to db using jdbc
        con2 = con;
        Statement tempst=con2.createStatement();
        //int len=tempst.execute("SELECT count(*) FROM unielwin_lattice.RNodes;");
        
        
      /* // Aug. 21, 2014, do not need to do this step, since it's copied form _setup database
        // e.g. unielwin_Training1_BN-->> unielwin_Training1_target_setup  -->> unielwin_Training1_target_BN
        //  in this way, do not need to worry about the consistency of Rnods.
       //generate shorter rnid, from a to z
        int fc=97; 
        char short_rnid;
        ResultSet temprs=tempst.executeQuery("select orig_rnid from RNodes;");
        ArrayList<String> tempList=new ArrayList<String>();
        while(temprs.next()) {
            tempList.add(temprs.getString("orig_rnid"));
        }
        for(int i=0;i<tempList.size();i++) {
        	short_rnid=(char)fc;           //explict type casting to convert integer to character
        	fc++;
        	tempst.execute("update RNodes set rnid='`" + short_rnid + "`' where orig_rnid='" + tempList.get(i) + "';");
        }
        tempst.close();*/
	
      //  maxNumberOfMembers = 2;


        //LATTICE read first sest from RFunctors
        readFirstSets();

        //LATTICE init -> init createdSet + truncate tables + add first sets to db
        init();

        //LATTICE generate lattice tree
        generateTree();
        
        // create a table of orig_rnid and rnid         
        mapping_rnid();

        return maxNumberOfMembers;
    }

    //change orig_rnid to rnid, make it shorter
    public static void readFirstSets() throws SQLException {
        firstSets = new ArrayList<String>();
        wholeSets = new ArrayList<String>();
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery("select rnid from RNodes;");
        
        // while(rs.next())
        //System.out.println(rs.getString(1));
     
        while(rs.next()){
            /*firstSets.add(rs.getString("rnid"));*/
        	firstSets.add(rs.getString("rnid").substring(1,rs.getString("rnid").length()-1));
        }
/*        System.out.println("***********");
        for(String g:firstSets)
        	System.out.println(g);*/
        st.close();
    }

    public static void init() throws SQLException {
        
        maxNumberOfMembers = firstSets.size();
        Statement st = con2.createStatement();
        /* The create statements should already have been done, I'm leaving them in for now (O.S.) */
        st.execute("create table if not exists lattice_membership (name VARCHAR(20), member VARCHAR(20), PRIMARY KEY (name, member));");
        st.execute("create table if not exists lattice_rel (parent VARCHAR(20), child VARCHAR(20), removed VARCHAR(20), PRIMARY KEY (parent, child));");
        st.execute("create table if not exists lattice_set (name VARCHAR(20), length INT(11), PRIMARY KEY (name, length));");

        st.execute("truncate lattice_rel;");
        st.execute("truncate lattice_membership;");
        st.execute("truncate lattice_set;");
        

        for(String set : firstSets){
            st.execute("insert into lattice_set (name,length) values ('`" + set + "`',1);");   //adding apostrophe `
            st.execute("insert into lattice_rel (parent,child,removed) values ('EmptySet','`" + set + "`','`" + set + "`');");
            st.execute("insert into lattice_membership (name, member) values ('`" + set + "`', '`" + set + "`');");
        }
        st.close();
    }

    public static void generateTree() throws SQLException {
        Statement st = con2.createStatement();
        for(int setLength = 1; setLength < maxNumberOfMembers; setLength++){
            ArrayList<String> sets = new ArrayList<String>();
            ResultSet rs = st.executeQuery("select name from lattice_set where length = " + setLength + ";");
            int tem=0;
            while(rs.next()){
                String h= rs.getString("name").substring(1,rs.getString("name").length()-1 ) ;   //deleting apostrophe from beginning and end
                sets.add(h);
                //System.out.println(rs.getString("name").length());
            }
            
//            System.out.println(sets.size());
//            for (String s : sets)
//            System.out.println(s);
//            System.out.println(setLength);
//            System.out.println(sets);
            
            createNewSets(sets);
        }
        st.close();
    }

    public static void createNewSets(ArrayList<String> sets) throws SQLException {
        for(String firstSet : firstSets){
            for(String secondSet : sets){
            	//System.out.println(secondSet);
                HashSet<String> newSet = new HashSet<String>();
                String[] secondSetParts = nodeSplit(secondSet);
                //String[] secondSetParts = secondSet;
                
                
                //System.out.println("size of secondset:"+ secondSet.length());
                
//                System.out.println("********************");
//                System.out.println();
//                System.out.println(secondSetParts);
 //               for (String s : secondSetParts)
 //               System.out.print(s + " ");
//                System.out.println();
//                System.out.println(checkConstraints(firstSet, secondSetParts));
//                System.out.println("********************");
                
                //System.out.println(checkConstraints(firstSet, secondSetParts));
                if (!checkConstraints(firstSet, secondSetParts)) continue;
                //System.out.println("*****()*****");

                //add set with length 1
                newSet.add(firstSet);
               
                //add all members of the set with length 1 less
                Collections.addAll(newSet, secondSetParts);

                int newSetLength = newSet.size();
                String newSetName = nodeJoin(newSet);  
                //System.out.println(newSetName.compareTo(secondSet));

                //add it to db and createdSet
                if(newSetName.compareTo(secondSet) != 0) {
                    wholeSets.add(newSetName);
                    // insert ignore is used to remove duplicates by primary keys
                    // is this really necessary? I'd like to enforce foreign key constraints. O.S.
                    Statement st = con2.createStatement();
                    // add new set
                    //System.out.print(newSetName+"     ");
                    
                    //adding apostrophe `
                    newSetName="`"+newSetName+"`";
                    secondSet = "`" + secondSet + "`";
                    
                    st.execute("insert ignore into lattice_set (name,length) values ('" + newSetName + "'," + newSetLength + ");");
                    // add relation
                    // added first set to the tablse, first set is the removed child from the child to build the parent
					st.execute("insert ignore into lattice_rel (parent,child,removed) values ('" + secondSet + "','" + newSetName + "','`" + firstSet + "`');");
                    // add members
                    // add first member
                    st.execute("insert ignore into lattice_membership (name,member) values ('" + newSetName +"','`" + firstSet + "`');");
                    // add the rest
                    for (String secondSetMembers : newSet){
                        st.execute("insert ignore into lattice_membership (name,member) values ('" + newSetName + "','`" + secondSetMembers + "`');");
                    }
                    st.close();
                }
            }
        }
    }

    /*   orig_rnid													rind
     *	`RA(prof0,student0),registration(course0,student0)`		 	`a,b`
		`RA(prof0,student0)` 										`a`
		`registration(course0,student0)` 							`b`
      * */
    
public static void mapping_rnid() throws SQLException{
    	
    	Statement st=con2.createStatement();
    	st.execute("drop table if exists lattice_mapping ;");
    	st.execute("create table if not exists lattice_mapping(orig_rnid VARCHAR(200), rnid VARCHAR(20), PRIMARY KEY(orig_rnid,rnid));"); //zqian, max key length limitation, Oct 11, 2013
    
    	
    	ResultSet rst=st.executeQuery("select name from lattice_set order by length;");//getting nodes from lattice_set table
    	
    	ArrayList <String>list_rnid =new ArrayList<String>(); //for storing lattice_set name
    	
    	
    	while(rst.next()){
    		
    		String temp="";
    		
    			for(int i=0;i<rst.getString(1).length()-1;i++)
    			{if(rst.getString(1).charAt(i)!='`') 
    				temp=temp+rst.getString(1).charAt(i);
    			}
    		
    		temp="`"+temp+"`";
    		list_rnid.add(temp);
    		//System.out.println(temp);
    	
    		  	}
    	
    	
    	
    	for(int i=0;i<list_rnid.size();i++){
    		
    		String n[]=list_rnid.get(i).substring(1, list_rnid.get(i).length()-1).split(",");
    		String temp="";
    		
    		for(int j=0;j<n.length;j++){
    			n[j]="`"+n[j]+"`";
    			ResultSet rst2=st.executeQuery("select orig_rnid from RNodes where rnid='"+n[j]+"';" );//getting orignal Rnodes from Rnodes table
    			  rst2.absolute(1);			
    		     
    			temp=temp+rst2.getString(1)+",";
    			
    		}
    		
    		temp=temp.substring(1, temp.length()-1);
    		
    		String orig_rnid="";
    		for(int k=0;k<temp.length();k++){
    			if(temp.charAt(k)!='`')
    				orig_rnid=orig_rnid+temp.charAt(k);
    		}
    		orig_rnid="`"+orig_rnid+"`";
    		//System.out.println(orig_rnid);
    		
    		
    		
    		st.execute("insert into lattice_mapping (orig_rnid,rnid) values('"+orig_rnid+"','"+list_rnid.get(i)+"');");
    		//System.out.println(temp+"  "+list.get(i));
    	}
    	
    	st.close();
    }
    
    
    
    
    public static boolean checkConstraints(String firstSet, String[] secondSetParts) throws SQLException {
        
//    	System.out.println(firstSet);
    	
    	HashSet<String> firstSetKeys = new HashSet<String>();
        HashSet<String> secondSetKeys = new HashSet<String>();
        Statement st = con2.createStatement();

        // get primary key for first set
        
        //use rnid, the shorter one
        ResultSet rs = st.executeQuery("select pvid1, pvid2 from RNodes where rnid = '`" + firstSet + "`';");
        while(rs.next()){
            firstSetKeys.add(rs.getString("pvid1"));
            firstSetKeys.add(rs.getString("pvid2"));
        }

        // get primary key for second set
        //System.out.println(secondSetParts);
        for (String secondSet : secondSetParts)
        {
            rs = st.executeQuery("select pvid1, pvid2 from RNodes where rnid = '`" + secondSet + "`';");
            while(rs.next()){
                secondSetKeys.add(rs.getString("pvid1"));
                secondSetKeys.add(rs.getString("pvid2"));
            }
        }
        st.close();

        // check if the number of population variables exceeds the limit
        HashSet<String> unionSetKeys = new HashSet<String>(firstSetKeys);
        unionSetKeys.addAll(secondSetKeys);
        if (unionSetKeys.size() > maxNumberOfPVars) return false;

//        System.out.println("*************************");
//        System.out.println(firstSetKeys);
//        System.out.println(secondSetKeys);
//        System.out.println("*************************");
        
        // check if there is a shared primary key
        firstSetKeys.retainAll(secondSetKeys);
        return !firstSetKeys.isEmpty();
    }

    // generate a new lattice node by join a list of relation nodes
    public static String nodeJoin(HashSet<String> newSet) {
        List<String> newList = new ArrayList<String>();
        for (String setItem : newSet)
            newList.add(setItem);
        // sort by alphabetical order
        Collections.sort(newList);
        String joinStr = "";
        for (String listItem : newList)
            joinStr = joinStr + delimiter + listItem;
        if (joinStr.length() > 0) joinStr = joinStr.substring(1);
        //System.out.println(joinStr);
        return joinStr;
    }

    // split a lattice node into a list of relation nodes
    
    public static String[] nodeSplit(String node) {
    //some portion of original code deleted	
        String[] nodes = node.split( delimiter );
        
//        System.out.println("*********************");
//        System.out.println(node);
//        System.out.println("nodes:");
//        for (String s : nodes)
//        	System.out.println(s);
//        System.out.println("*********************");
        
        return nodes;
    }
    
    
    
    
    //main, connect to database
    public static void main(String[] args) throws Exception {
		databaseName = "UW_std";
		databaseName2 = databaseName + "_lattice";
		dbUsername = "sfu";
		dbPassword = "joinBayes";
		dbaddress = "mysql://kripke.cs.sfu.ca";

        //connect to db using jdbc
		connectDB();

		//generate lattice tree
		maxNumberOfMembers = short_rnid_LatticeGenerator.generate(con2);
		System.out.println(" ##### lattice is ready for use* ");
		
		disconnectDB();
    }

	public static void connectDB() throws SQLException {
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}
		
	public static void disconnectDB() throws SQLException {
		con2.close();
	}

}
