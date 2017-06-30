
package lattice;
 
import com.mysql.jdbc.Connection;
 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
 
/**
* Created with IntelliJ IDEA.
* User: abozorgk
* Date: 12/12/12
* Time: 2:50 PM
* To change this template use File | Settings | File Templates.
*/
public class LatticeGenerator {
 
 
    static Connection con2;
 
 
    static ArrayList<String> firstSets;
 
    static List<String> wholeSets;
 
    static int maxNumberOfMembers;
 
    final static int maxNumberOfPVars = 10;
 
    final static String delimiter = ",";
 
 
    public static int generate(Connection con) throws SQLException {
 
        //connect to db using jdbc
 
        con2 = con;
 
 
        //LATTICE read first sest from RFunctors
 
        readFirstSets();
 
 
        //LATTICE init -> init createdSet + truncate tables + add first sets to db
 
        init();
 
 
        //LATTICE generate lattice tree
 
        generateTree();
 
 
        return maxNumberOfMembers;
}
 
 
    public static void readFirstSets() throws SQLException {
 
        firstSets = new ArrayList<String>();
 
        wholeSets = new ArrayList<String>();
 
        Statement st = con2.createStatement();
 
        ResultSet rs = st.executeQuery("select rnid from RNodes;");
 
        while(rs.next()){
 
            firstSets.add(rs.getString("rnid"));
 
        }
 
        st.close();
}
 
 
    public static void init() throws SQLException {
 
        maxNumberOfMembers = firstSets.size();
 
        Statement st = con2.createStatement();
 
        /* The create statements should already have been done, I'm leaving them in for now (O.S.) */
 
        st.execute("create table if not exists lattice_membership (name VARCHAR(256), member VARCHAR(256), PRIMARY KEY (name, member));");
 
        st.execute("create table if not exists lattice_rel (parent VARCHAR(256), child VARCHAR(256), removed VARCHAR(256), PRIMARY KEY (parent, child));");
 
        st.execute("create table if not exists lattice_set (name VARCHAR(256), length INT(11), PRIMARY KEY (name, length));");
 
 
        st.execute("truncate lattice_rel;");
 
        st.execute("truncate lattice_membership;");
 
        st.execute("truncate lattice_set;");
 
        for(String set : firstSets){
 
            st.execute("insert into lattice_set (name,length) values ('" + set + "',1);");
 
            st.execute("insert into lattice_rel (parent,child,removed) values ('EmptySet','" + set + "','" + set + "');");
 
            st.execute("insert into lattice_membership (name, member) values ('" + set + "', '" + set + "');");
 
        }
 
        st.close();
}
 
 
    public static void generateTree() throws SQLException {
 
        Statement st = con2.createStatement();
 
        for(int setLength = 1; setLength < maxNumberOfMembers; setLength++){
 
            ArrayList<String> sets = new ArrayList<String>();
 
            ResultSet rs = st.executeQuery("select name from lattice_set where length = " + setLength + ";");
 
 
            while(rs.next()){
 
                sets.add(rs.getString("name"));
 
            }
 
 
//            System.out.println(sets.size());
 
//            for (String s : sets)
 
//            	 System.out.println(s);
 
//            System.out.println(setLength);
 
//            System.out.println(sets);
 
 
            createNewSets(sets);
 
        }
 
        st.close();
}
 
 
    public static void createNewSets(ArrayList<String> sets) throws SQLException {
 
        for(String firstSet : firstSets){
 
            for(String secondSet : sets){
 
 
                HashSet<String> newSet = new HashSet<String>();
 
                String[] secondSetParts = nodeSplit(secondSet);
 
 
//                System.out.println("********************");
 
//                System.out.println();
 
//                System.out.println(firstSet);
 
//                for (String s : secondSetParts)
 
//                	 System.out.print(s + " ");
 
//                System.out.println();
 
//                System.out.println(checkConstraints(firstSet, secondSetParts));
 
//                System.out.println("********************");
 
 
                if (!checkConstraints(firstSet, secondSetParts)) continue;
 
 
                //add set with length 1
 
                newSet.add(firstSet);
 
 
                //add all members of the set with length 1 less
 
                Collections.addAll(newSet, secondSetParts);
 
 
                int newSetLength = newSet.size();
 
                String newSetName = nodeJoin(newSet);
 
 
                //add it to db and createdSet
 
                if(newSetName.compareTo(secondSet) != 0) {
 
                    wholeSets.add(newSetName);
 
                    // insert ignore is used to remove duplicates by primary keys
 
                    // is this really necessary? I'd like to enforce foreign key constraints. O.S.
 
                    Statement st = con2.createStatement();
 
                    // add new set
 
                    st.execute("insert ignore into lattice_set (name,length) values ('" + newSetName + "'," + newSetLength + ");");
 
                    // add relation
 
                    // added first set to the tablse, first set is the removed child from the child to build the parent
 
                    st.execute("insert ignore into lattice_rel (parent,child,removed) values ('" + secondSet + "','" + newSetName + "','" + firstSet + "');");
 
                    // add members
 
                    // add first member
 
                    st.execute("insert ignore into lattice_membership (name,member) values ('" + newSetName + "','" + firstSet + "');");
 
                    // add the rest
 
                    for (String secondSetMembers : newSet){
 
                        st.execute("insert ignore into lattice_membership (name,member) values ('" + newSetName + "','" + secondSetMembers + "');");
 
                    }
 
                    st.close();
 
                }
 
            }
 
        }
}
 
 
    public static boolean checkConstraints(String firstSet, String[] secondSetParts) throws SQLException {
 
//  System.out.println(firstSet);
    	 
    	 HashSet<String> firstSetKeys = new HashSet<String>();
 
        HashSet<String> secondSetKeys = new HashSet<String>();
 
        Statement st = con2.createStatement();
 
 
        // get primary key for first set
 
        ResultSet rs = st.executeQuery("select pvid1, pvid2 from RNodes where rnid = '" + firstSet + "';");
 
        while(rs.next()){
 
            firstSetKeys.add(rs.getString("pvid1"));
 
            firstSetKeys.add(rs.getString("pvid2"));
 
        }
 
 
        // get primary key for second set
 
        for (String secondSet : secondSetParts)
 
        {
 
            rs = st.executeQuery("select pvid1, pvid2 from RNodes where rnid = '" + secondSet + "';");
 
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
 
        return joinStr;
}
 
 
    // split a lattice node into a list of relation nodes
 
    public static String[] nodeSplit(String node) {
    	 
 
        String[] nodes = node.split("`" + delimiter + "`");
 
        for (int i=0; i<nodes.length; i++)
 
        {
 
	 if (!nodes[i].startsWith("`"))
 
	 	 nodes[i] = "`" + nodes[i];
 
	 if (!nodes[i].endsWith("`"))
 
	 	 nodes[i] = nodes[i] + "`";
 
        }
 
 
//        System.out.println("*********************");
 
//        System.out.println(node);
 
//        System.out.println("nodes:");
 
//        for (String s : nodes)
 
//        	 System.out.println(s);
 
//        System.out.println("*********************");
 
 
        return nodes;
}
 
 
}
