
package ca.sfu.cs.factorbase.lattice;
 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.mysql.jdbc.Connection;

// assumes that a table LatticeRnodes has been generated. See transfer.sql //

public class short_rnid_LatticeGenerator {

    static Connection con2;

    static ArrayList<String> firstSets;
    static int maxNumberOfMembers;
    final static int maxNumberOfPVars = 10;
    final static String delimiter = ",";
    
	
	private static Logger logger = Logger.getLogger(short_rnid_LatticeGenerator.class.getName());

    public static int generate(Connection con) throws SQLException {
        //connect to db using jdbc
        con2 = con;
        Statement tempst=con2.createStatement();
        
        logger.info("ENTER INTO SHORT LATTICE generate");
        //generate shorter rnid, from a to z
        int fc=97; 
        char short_rnid;
        ResultSet temprs=tempst.executeQuery("select orig_rnid from LatticeRNodes;");
        logger.fine("about to execute the following query: select orig_rnid from LatticeRNodes");
        ArrayList<String> tempList=new ArrayList<String>();
        while(temprs.next()) {
            tempList.add(temprs.getString("orig_rnid"));
        }
        for(int i=0;i<tempList.size();i++) {
        	short_rnid=(char)fc;           //explict type casting to convert integer to character
        	fc++;
        	tempst.execute("update LatticeRNodes set short_rnid='`" + short_rnid + "`' where orig_rnid='" + tempList.get(i) + "';");
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


    //change orig_rnid to rnid, make it shorter
    public static void readFirstSets() throws SQLException {
        firstSets = new ArrayList<String>();
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery("select orig_rnid from LatticeRNodes;");
        
     
        while(rs.next()){
            // Remove the flanking backticks from the orig_rnid before adding them to the set.
            firstSets.add(rs.getString("orig_rnid").substring(1, rs.getString("orig_rnid").length() - 1));
            logger.fine("The orig_rnid is : " + rs.getString("orig_rnid"));
        }

        st.close();
    }

    public static void init() throws SQLException {
        
        maxNumberOfMembers = firstSets.size();
        Statement st = con2.createStatement();
    
        st.execute("create table if not exists lattice_membership (name VARCHAR(398), member VARCHAR(398), PRIMARY KEY (name, member));");
        st.execute("create table if not exists lattice_rel (parent VARCHAR(398), child VARCHAR(398), removed VARCHAR(199), rnid VARCHAR(199), PRIMARY KEY (parent, child));");
        st.execute("create table if not exists lattice_set (name VARCHAR(199), length INT(11), PRIMARY KEY (name, length));");

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
            while(rs.next()){
                String h= rs.getString("name").substring(1,rs.getString("name").length()-1 ) ;   //deleting apostrophe from beginning and end
                sets.add(h);
            }
            
            
            createNewSets(sets);
        }
        st.close();
    }

    public static void createNewSets(ArrayList<String> sets) throws SQLException {
        for(String firstSet : firstSets){
            for(String secondSet : sets){
                HashSet<String> newSet = new HashSet<String>();
                String[] secondSetParts = nodeSplit(secondSet);
                
                
                
                if (!checkConstraints(firstSet, secondSetParts)) continue;

                //add set with length 1
                newSet.add(firstSet);
               
                //add all members of the set with length 1 less
                Collections.addAll(newSet, secondSetParts);

                int newSetLength = newSet.size();
                String newSetName = nodeJoin(newSet);  

                //add it to db and createdSet
                if(newSetName.compareTo(secondSet) != 0) {
                    // insert ignore is used to remove duplicates by primary keys
                    // is this really necessary? I'd like to enforce foreign key constraints. O.S.
                    Statement st = con2.createStatement();
                    // add new set
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
// record mapping between original and short rnids //
public static void mapping_rnid() throws SQLException{
    	
    	Statement st=con2.createStatement();
    	st.execute("drop table if exists lattice_mapping ;");
    	st.execute("create table if not exists lattice_mapping(orig_rnid VARCHAR(200), short_rnid VARCHAR(20), PRIMARY KEY(orig_rnid,short_rnid));"); //zqian, max key length limitation, Oct 11, 2013
    
    	
    	ResultSet rst=st.executeQuery("select name from lattice_set order by length;");//getting nodes from lattice_set table
    	
    	ArrayList <String>list_rnid =new ArrayList<String>(); //for storing lattice_set name
    	
        while(rst.next()){
            // Make sure that all the special characters are escaped properly by only having backticks that flank the
            // string.
            String cleanedName = rst.getString(1).replace("`", "");
            cleanedName = "`" + cleanedName + "`";
            list_rnid.add(cleanedName);
        }

        for(String rnid : list_rnid) {
            // Splitting any Rchains into its components.
            String[] rnodes = rnid.substring(1, rnid.length() - 1).replace("),", ") ").split(" ");
            String short_rnid = "";

            // for loop to find the short RNode ID of the components of any Rchains.
            for(String rnode : rnodes){
                rnode = "`" + rnode + "`";

                // Getting short RNodes ID from LatticeRNodes table.
                ResultSet rst2 = st.executeQuery("select short_rnid from LatticeRNodes where orig_rnid = '"+ rnode +"';");
                rst2.absolute(1); // Moving the cursor to the first item in the results.

                short_rnid = short_rnid + rst2.getString(1) + ",";
            }

            // Remove trailing comma (,).
            short_rnid = short_rnid.substring(0, short_rnid.length() - 1);

            // Make sure that all the special characters are escaped properly by only having backticks flanking the
            // string.
            short_rnid = short_rnid.replace("`", "");
            short_rnid = "`" + short_rnid + "`";

            st.execute(
                "insert into lattice_mapping (orig_rnid,short_rnid) " + 
                "values ('" + rnid + "','" + short_rnid +"');"
            );
        }

        st.close();
    }

    public static boolean checkConstraints(String firstSet, String[] secondSetParts) throws SQLException {
        
    	
    	HashSet<String> firstSetKeys = new HashSet<String>();
        HashSet<String> secondSetKeys = new HashSet<String>();
        Statement st = con2.createStatement();

        // get primary key for first set
        
        // use rnid
        ResultSet rs = st.executeQuery("select pvid1, pvid2 from LatticeRNodes where orig_rnid = '`" + firstSet + "`';");
        while(rs.next()){
            firstSetKeys.add(rs.getString("pvid1"));
            firstSetKeys.add(rs.getString("pvid2"));
        }

        // get primary key for second set
        for (String secondSet : secondSetParts)
        {
            rs = st.executeQuery("select pvid1, pvid2 from LatticeRNodes where orig_rnid = '`" + secondSet + "`';");
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

        return joinStr;
    }

    // split a lattice node into a list of relation nodes
    
    public static String[] nodeSplit(String node) {
    //some portion of original code deleted	
        String[] nodes = node.replace("),", ") ").split(" ");
        
        return nodes;
    }
}
