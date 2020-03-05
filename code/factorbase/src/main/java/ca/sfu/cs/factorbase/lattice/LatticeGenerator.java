
package ca.sfu.cs.factorbase.lattice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.mysql.jdbc.Connection;

import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.util.MySQLScriptRunner;


/**
 * Assumes that a table LatticeRnodes has been generated. See transfer.sql.
 */
public class LatticeGenerator {
    private static final int MAX_NUM_OF_PVARS = 10;
    private static final String delimiter = ",";


    /**
     * Generate the local relationship lattice (relationship lattice restricted based on the FunctorSet).
     *
     * @param dbConnection - connection to the database to copy the local relationship lattice to from the global
     *                       relationship lattice in the "_setup" database.
     * @param databaseName - name of the input database we are trying to learn the Bayesian Network for.
     * @return the local relationship lattice.
     * @throws SQLException if an issue occurs when attempting to copy the information.
     */
    public static RelationshipLattice generate(
        Connection dbConnection,
        String databaseName
    ) throws SQLException {
        // Transfer all the local relationship lattice information from the global relationship lattice.
        MySQLScriptRunner.callSP(
            dbConnection,
            "populateLattice"
        );

        try (
            Statement statement = dbConnection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT " +
                    "name, " +
                    "short_rnid, " +
                    "length " +
                "FROM " +
                    "lattice_set " +
                "JOIN " +
                    "lattice_mapping " +
                "ON " +
                    "lattice_set.name = lattice_mapping.orig_rnid;"
            )
        ) {
            RelationshipLattice lattice = new RelationshipLattice();

            while(results.next()) {
                FunctorNodesInfo rchainInfo = new FunctorNodesInfo(
                    results.getString("name"),
                    results.getString("short_rnid")
                );

                lattice.addRChainInfo(
                    rchainInfo,
                    results.getInt("length")
                );
            }

            return lattice;
        }
    }


    /**
     * Generate the global relationship lattice for the entire database.
     *
     * @param functorNodesInfos - {@code FunctorNodesInfo}s with the functor node information for all the RNodes in the
     *                            database.
     * @param dbConnection - connection to the database that has had the
     *                       latticegenerator_initialize.sql script executed on it.
     * @param tableName - the table containing all the RNode IDs for the entire database.
     * @param columnName - the column name of the specified table containing the RNode IDs.
     * @return the relationship lattice for the entire database.
     * @throws SQLException if an issue occurs when attempting to retrieve the information.
     */
    public static RelationshipLattice generateGlobal(
        Map<String, FunctorNodesInfo> functorNodesInfos,
        Connection dbConnection,
        String tableName,
        String columnName
    ) throws SQLException {
        Set<String> rnodeIDs = functorNodesInfos.keySet();

        init(dbConnection, rnodeIDs);
        generateTree(dbConnection, rnodeIDs, rnodeIDs.size());

        // Create a table of RChains with original rnids and short rnids.
        mapping_rnid(dbConnection);

        try(
            Statement statement = dbConnection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT * " +
                "FROM lattice_set " +
                "JOIN lattice_mapping " +
                "ON lattice_set.name = lattice_mapping.orig_rnid;"
            )
        ) {
            RelationshipLattice lattice = new RelationshipLattice();

            while(results.next()) {
                FunctorNodesInfo rchainInfo = extractRChainFunctorNodeInfo(
                    results.getString("name"),
                    results.getString("short_rnid"),
                    functorNodesInfos
                );

                lattice.addRChainInfo(
                    rchainInfo,
                    results.getInt("length")
                );
            }

            return lattice;
        }
    }


    /**
     * Retrieve all the functor node information for the given RChain.
     *
     * @param rchain - the name of the RChain.
     * @param shortRChain - the name of the RChain using short RNode IDs.
     * @param functorNodesInfos - {@code FunctorNodesInfo}s with the functor node information for all the RNodes in the
     *                            database that the RChain is from.
     * @return all the functor node information for the given RChain.
     */
    private static FunctorNodesInfo extractRChainFunctorNodeInfo(
        String rchain,
        String shortRChain,
        Map<String, FunctorNodesInfo> functorNodesInfos
    ) {
        String[] rnodeIDs = rchain.replace("),", ") ").split(" ");
        FunctorNodesInfo rchainFunctorNodeInfo = new FunctorNodesInfo(rchain, shortRChain, true, true);

        // for loop to merge all the functor node information into a single FunctorNodesInfo for the RChain.
        for (String rnodeID : rnodeIDs) {
            FunctorNodesInfo rnodeFunctorInfo = functorNodesInfos.get(rnodeID);
            // Some RNodes don't have any 1Nodes or 2Nodes so there isn't anything to merge for these cases.
            if (rnodeFunctorInfo != null) {
                rchainFunctorNodeInfo.merge(rnodeFunctorInfo);
            }
        }

        return rchainFunctorNodeInfo;
    }


    private static int init(Connection dbConnection, Collection<String> firstSets) throws SQLException {
        int maxNumberOfMembers = firstSets.size();
        Statement st = dbConnection.createStatement();

        for(String set : firstSets) {
            st.execute("INSERT INTO lattice_set (name, length) VALUES ('" + set + "', 1);");
            st.execute("INSERT INTO lattice_rel (parent, child, removed) VALUES ('EmptySet', '" + set + "', '" + set + "');");
            st.execute("INSERT INTO lattice_membership (name, member) VALUES ('" + set + "', '" + set + "');");
        }

        st.close();

        return maxNumberOfMembers;
    }


    private static void generateTree(Connection dbConnection, Collection<String> firstSets, int maxNumberOfMembers) throws SQLException {
        Statement st = dbConnection.createStatement();
        for(int setLength = 1; setLength < maxNumberOfMembers; setLength++) {
            ArrayList<String> sets = new ArrayList<String>();
            ResultSet rs = st.executeQuery("SELECT name FROM lattice_set WHERE length = " + setLength + ";");
            while(rs.next()) {
                String h = rs.getString("name");
                sets.add(h);
            }

            createNewSets(dbConnection, firstSets, sets);
        }

        st.close();
    }


    private static void createNewSets(Connection dbConnection, Collection<String> firstSets, ArrayList<String> sets) throws SQLException {
        for(String firstSet : firstSets) {
            for(String secondSet : sets) {
                HashSet<String> newSet = new HashSet<String>();
                String[] secondSetParts = nodeSplit(secondSet);

                if (!checkConstraints(dbConnection, firstSet, secondSetParts)) {
                    continue;
                }

                // Add set with length 1.
                newSet.add(firstSet);

                // Add all members of the set with length 1 less.
                Collections.addAll(newSet, secondSetParts);

                int newSetLength = newSet.size();
                String newSetName = nodeJoin(newSet);

                // Add it to db and createdSet.
                if(newSetName.compareTo(secondSet) != 0) {
                    // Insert ignore is used to remove duplicates by primary keys.
                    // Is this really necessary?  I'd like to enforce foreign key constraints. O.S.
                    Statement st = dbConnection.createStatement();
                    // Add new set.
                    st.execute("INSERT IGNORE INTO lattice_set (name, length) VALUES ('" + newSetName + "', " + newSetLength + ");");
                    // Add relation.
                    // Add first set to the table, first set is the removed child from the child to build the parent.
                    st.execute("INSERT IGNORE INTO lattice_rel (parent, child, removed) VALUES ('" + secondSet + "', '" + newSetName + "', '" + firstSet + "');");
                    // Add members.
                    // Add first member.
                    st.execute("INSERT IGNORE INTO lattice_membership (name, member) VALUES ('" + newSetName +"', '" + firstSet + "');");
                    // Add the rest.
                    for (String secondSetMembers : newSet) {
                        st.execute("INSERT IGNORE INTO lattice_membership (name, member) VALUES ('" + newSetName + "', '" + secondSetMembers + "');");
                    }

                    st.close();
                }
            }
        }
    }


    /**
     * Record mapping between original and short rnids.
     *
     * Example mappings:
     * orig_rnid                                              rnid
     * RA(prof0,student0),registration(course0,student0)      a,b
     * RA(prof0,student0)                                     a
     * registration(course0,student0)                         b
     *
     */
    private static void mapping_rnid(Connection dbConnection) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rst = st.executeQuery("SELECT name FROM lattice_set ORDER BY length;"); // Getting nodes from lattice_set table.

        ArrayList <String>list_rnid = new ArrayList<String>(); // For storing lattice_set name.

        while(rst.next()) {
            list_rnid.add(rst.getString("name"));
        }

        for(String rnid : list_rnid) {
            // Splitting any Rchains into its components.
            String[] rnodes = rnid.replace("),", ") ").split(" ");
            StringJoiner shortRnidCSV = new StringJoiner(",");

            // for loop to find the short RNode ID of the components of any Rchains.
            for(String rnode : rnodes) {
                // Getting short RNodes ID from LatticeRNodes table.
                ResultSet rst2 = st.executeQuery("SELECT short_rnid FROM LatticeRNodes WHERE orig_rnid = '" + rnode + "';");
                rst2.absolute(1); // Moving the cursor to the first item in the results.

                shortRnidCSV.add(rst2.getString(1));
            }

            String short_rnid = shortRnidCSV.toString();

            st.execute(
                "INSERT INTO lattice_mapping (orig_rnid, short_rnid) " +
                "VALUES ('" + rnid + "', '" + short_rnid + "');"
            );
        }

        st.close();
    }


    private static boolean checkConstraints(Connection dbConnection, String firstSet, String[] secondSetParts) throws SQLException {
        HashSet<String> firstSetKeys = new HashSet<String>();
        HashSet<String> secondSetKeys = new HashSet<String>();
        Statement st = dbConnection.createStatement();

        // Get primary key for first set.
        // Use rnid.
        ResultSet rs = st.executeQuery(
            "SELECT pvid1, pvid2 " +
            "FROM RNodes " +
            "WHERE rnid = '" + firstSet + "';"
        );
        while(rs.next()) {
            firstSetKeys.add(rs.getString("pvid1"));
            firstSetKeys.add(rs.getString("pvid2"));
        }

        // Get primary key for second set.
        for (String secondSet : secondSetParts) {
            rs = st.executeQuery(
                "SELECT pvid1, pvid2 " +
                "FROM RNodes " +
                "WHERE rnid = '" + secondSet + "';"
            );
            while(rs.next()) {
                secondSetKeys.add(rs.getString("pvid1"));
                secondSetKeys.add(rs.getString("pvid2"));
            }
        }

        st.close();

        // Check if the number of population variables exceeds the limit.
        HashSet<String> unionSetKeys = new HashSet<String>(firstSetKeys);
        unionSetKeys.addAll(secondSetKeys);
        if (unionSetKeys.size() > MAX_NUM_OF_PVARS) {
            return false;
        }

        // Check if there is a shared primary key.
        firstSetKeys.retainAll(secondSetKeys);

        return !firstSetKeys.isEmpty();
    }


    /**
     * Generate a new lattice node by joining a list of relation nodes.
     */
    private static String nodeJoin(HashSet<String> newSet) {
        List<String> newList = new ArrayList<String>();
        for (String setItem : newSet) {
            newList.add(setItem);
        }

        // Sort by alphabetical order.
        Collections.sort(newList);

        return String.join(delimiter, newList);
    }


    /**
     * Split a lattice node into a list of relation nodes.
     */
    private static String[] nodeSplit(String node) {
        // Some portion of original code deleted.
        String[] nodes = node.replace("),", ") ").split(" ");
        return nodes;
    }
}