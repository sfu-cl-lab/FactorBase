package ca.sfu.cs.factorbase.exporter.bifexporter;

/**
 * Feb 7th 2014, zqian:
 * Make sure each node appear as a child
 * <child,''>, <child,parent>
 */
/**
 * Change ChildValue to FID -- Feb 7 Yan
 */
/**
 * exporting the Bayes Net into a BIF file
 * 1.get the structure from table  _BN.Path_BayesNets
 * 2.get the parameter from tables _CP_smoothed
 *
 * Jun 25, zqian
 *
 * Given a Bayes Net stored in the databases with structure and parameters,
 * this program could generator the BIF file which can be feeded into UBC tools.
 *
 * For the position property, we may need to optimize it.
 */

// query for Rchain => SELECT DISTINCT lattice_set.name FROM lattice_membership, lattice_set WHERE length = (SELECT MAX(length) FROM lattice_set);

/* To do: Samarth, replace the short_rnid with orig_rnid */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;


public class BIF_Generator {
    public static final String BIF_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<BIF VERSION=\"0.3\"  xmlns=\"http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3 http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3/XMLBIFv0_3.xsd\">\n";
    private static Logger logger = Logger.getLogger(BIF_Generator.class.getName());


    public static void generate_bif(String network_name, String bif_file_name_withPath, Connection con) throws SQLException, IOException {
        logger.fine("\nBIF Generator starts");

        Statement st = (Statement) con.createStatement();
        File file = new File(bif_file_name_withPath);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        output.write(writeBifHeader());
        output.write(writeNetworkBegin(network_name));

        ArrayList<String> variables = new ArrayList<String>(); // ArrayList to store nodes.
        ArrayList<Integer> outcomes = new ArrayList<Integer>(); // Number of outcomes for each node.
        ArrayList<String> rnid = new ArrayList<String>(); // rnid.
        ArrayList<String> orig_rnid = new ArrayList<String>(); // orig_rnid
        ResultSet rst = st.executeQuery("SELECT * FROM lattice_mapping");
        while (rst.next()) {
            rnid.add(rst.getString("short_rnid"));
            orig_rnid.add(rst.getString("orig_rnid"));
        }

        // Feb 7th 2014, zqian;
        // Make sure each node appear as a child
        // <child,''>, <child,parent>
        rst = st.executeQuery("SELECT DISTINCT child FROM Path_BayesNets;");
        while (rst.next()) {
            variables.add(rst.getString("child"));
        }

        ArrayList<String> values;
        int x = 6000; int y = 4000; // x and y positions of nodes.
        int a = 0; // As a counter variable.

        // Writing the VARIABLE TAGS.
        // Change ChildValue to FID Feb 7 Yan.
        for (int i = 0; i < variables.size(); i++) {
            values = new ArrayList<String>();

            // POSSIBLE OUTCOMES FOR A VARIABLE.
            ResultSet rst1 = st.executeQuery("SELECT DISTINCT `" + variables.get(i) + "` FROM `" + variables.get(i) + "_CP_smoothed` ORDER BY `" + variables.get(i) + "`;");
            while (rst1.next()) {
                values.add(rst1.getString(1));
            }

            outcomes.add(values.size());

            // Updated on Jun 28, mapping the orig_rnid back.
            // Checking if the variable is rnid then replacing its name by orig_rnid eg: `a` to be replaced `RA(stud0,prof0)`.
            int z = 0;
            for (int s = 0; s < rnid.size(); s++) {
                if (variables.get(i).equals(rnid.get(s))) {
                    output.write(writeVariable(orig_rnid.get(s), values, x, y));
                    z = 1;
                    break;
                }
            }

            if (z == 0) {
                output.write(writeVariable(variables.get(i), values, x, y));
            }

            a++;
            x = x + 200;
            if (a == 3) {
                a = 0;
                y = y + 200;
                x = x - 600;
            }
        }

        //***********************************************//
        //WRITING THE DEFINITION TAGS

        ArrayList<String> given = null;
        ResultSet rst2;

        for (int i = 0; i < variables.size(); i++) {
            given = new ArrayList<String>();
            String table_name = "`" + variables.get(i) + "_CP_smoothed`";
            String probabilities = "";

            rst2 = st.executeQuery("SELECT DISTINCT lattice_set.name FROM lattice_membership, lattice_set WHERE length = (SELECT MAX(length) FROM lattice_set);");
            rst2.next();
            String Rchain = rst2.getString(1);
            rst2 = st.executeQuery(
                "SELECT DISTINCT parent " +
                "FROM Path_BayesNets " +
                "WHERE child = '" + variables.get(i) + "' " +
                "AND parent <> '' " +
                "AND Rchain = '" + Rchain + "';"
            );

            int nop = 0; // nop -> number of parents for a particular node.
            while (rst2.next()) {
                nop++;
            }

            if (nop > 0) {
                rst2.beforeFirst();
                while (rst2.next()) {
                    given.add(rst2.getString("parent"));
                }

                // Order of values according to xmlbif specifications.
                // Change ChildValue to FID -- Feb 7 Yan.
                String order = "`" + given.get(0) + "`";

                for (int k = 1; k < given.size(); k++) {
                    order = order + ", `" + given.get(k) + "`";
                }

                order = order + ", `" + variables.get(i) + "`";
                String query = "SELECT CP FROM " + table_name + " ORDER BY " + order + ";";
                rst2.close();
                rst2 = st.executeQuery(query);

                int size = 0;
                while (rst2.next()) {
                    size++;
                }
                rst2.beforeFirst();
                int n = size / outcomes.get(i);
                for (int l = 1; l <= n; l++) {
                    for (int j = 1; j <= (outcomes.get(i)); j++) {
                        rst2.next();
                        probabilities = probabilities + " " + rst2.getBigDecimal(1);
                    }
                }
            }

            // When node or variable does not have any parents.
            else {
                // Change ChildValue to FID -- Feb 7 Yan.
                rst2.close();
                rst2 = st.executeQuery("SELECT CP FROM " + table_name + " ORDER BY `" + variables.get(i) + "`");

                while (rst2.next()) {
                    probabilities = probabilities + " " + rst2.getBigDecimal(1);
                }
            }

            // Checking if the node or given values does not contain rnid, and then replacing the rnid with orig_rnid.
            String node = variables.get(i);
            for (int s = 0; s < rnid.size(); s++) {
                if (node.equals(rnid.get(s))) {
                    node = orig_rnid.get(s);
                    break;
                }
            }

            ArrayList<String> given1 = new ArrayList<String>(); // Given1 to store given values without rnid but with orig_rnid.
            for (int m = 0; m < given.size(); m++) {
                boolean flag = true;
                for (int s = 0; s < rnid.size(); s++) {
                    if (given.get(m).equals(rnid.get(s))) {
                        flag = false;
                        given1.add(orig_rnid.get(s));
                        break;
                    }
                }

                if (flag) {
                    given1.add(given.get(m));
                }
            }

            output.write(writeDefinition(node, given1, probabilities));
        }

        output.write(writeNetworkEnd());
        output.close();
        st.close();
        logger.fine("BIF Generator Ends for " + network_name);
    }


    public static void Final_Path_BayesNets(Connection conn, String rchain) throws SQLException, IOException {
        // OS Sep 13, 2017. This should be unncessary since we can make a view instead.
        ArrayList<String>orig_rnid = new ArrayList<String>();
        ArrayList<String>rnid = new ArrayList<String>();
        Statement st1 = (Statement) conn.createStatement();

        // Creating table Final_Path_BayesNets.
        String query1 = "CREATE TABLE `Final_Path_BayesNets` (SELECT * FROM `Path_BayesNets` WHERE Rchain = '" + rchain + "' AND parent<> '');";
        logger.fine(query1);
        st1.execute(query1);

        // Adding primary key to Final_Path_BayesNets.
        st1.execute("ALTER TABLE `Final_Path_BayesNets` ADD PRIMARY KEY (`Rchain`, `child`, `parent`);");
        ResultSet rst = st1.executeQuery("SELECT * FROM lattice_mapping;");

        while (rst.next()) {
            orig_rnid.add(rst.getString("orig_rnid"));
            rnid.add(rst.getString("short_rnid"));
        }

        for (int i = 0; i < rnid.size(); i++) {
            String query2 = "UPDATE `Final_Path_BayesNets` SET Rchain = '" + orig_rnid.get(i) + "' WHERE Rchain = '" + rnid.get(i) + "';";
            String query3 = "UPDATE `Final_Path_BayesNets` SET child = '" + orig_rnid.get(i) + "' WHERE child = '" + rnid.get(i) + "';";
            String query4 = "UPDATE `Final_Path_BayesNets` SET parent = '" + orig_rnid.get(i) + "' WHERE parent = '" + rnid.get(i) + "';";

            st1.execute(query2);
            st1.execute(query3);
            st1.execute(query4);
        }

        st1.close();
    }


    private static String writeBifHeader() {
        return BIF_HEADER;
    }


    private static String writeNetworkBegin(String name) {
        return "<NETWORK>\n<NAME>" + name + "</NAME>\n";
    }


    private static String writeNetworkEnd() {
        return "</NETWORK>\n</BIF>\n";
    }


    private static String writeVariable(String variable, ArrayList<String> outcomes, double x, double y) {
        String position = "(" + x + "," + y + ")";
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


    private static String writeDefinition(String forVariable, ArrayList<String> givenVariables, String probabilities) {
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
}