package ca.sfu.cs.factorbase.jbn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import ca.sfu.cs.factorbase.data.ContingencyTable;
import ca.sfu.cs.factorbase.data.TSVContingencyTable;
import ca.sfu.cs.factorbase.graph.Edge;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;
import edu.cmu.tetrad.search.Ges3;
import edu.cmu.tetrad.search.PatternToDag;


public class BayesNet_Learning_main {


    public static void tetradLearner(String srcfile, String destfile, boolean isDiscrete) throws Exception {
        tetradLearner(srcfile, null, null, destfile, isDiscrete);
    }


    public static void tetradLearner(
        String srcfile,
        List<Edge> requiredEdges,
        List<Edge> forbiddenEdges,
        String destfile,
        boolean isDiscrete
    ) throws Exception {
        ContingencyTable dataset = new TSVContingencyTable(srcfile, "MULT", isDiscrete);

        Ges3 gesSearch = new Ges3(
            dataset,
            10.0000,
            1.0000
        );
        Knowledge knowledge = new Knowledge();

        // Load required edge knowledge.
        if (requiredEdges != null) {
            for (Edge edge : requiredEdges) {
                knowledge.setEdgeRequired(edge.getParent(), edge.getChild(), true);
            }
        }

        // Load forbidden edge knowledge.
        if (forbiddenEdges != null) {
            for (Edge edge : forbiddenEdges) {
                knowledge.setEdgeForbidden(edge.getParent(), edge.getChild(), true);
            }
        }

        System.out.println(knowledge);
        System.out.println("knowledge is DONE~~");
        gesSearch.setKnowledge(knowledge);

        /* learn a dag from data */
        Graph graph = gesSearch.search();
        Pattern pattern = new Pattern(graph);

        PatternToDag p2d = new PatternToDag(pattern);
        Graph dag = p2d.patternToDagMeek();

        System.out.println("DAG is DONE~~~");

        // Output dag into Bayes Interchange format.
        FileWriter fstream = new FileWriter(destfile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(BIFHeader.header);
        out.write("<BIF VERSION=\"0.3\">\n");
        out.write("<NETWORK>\n");
        out.write("<NAME>BayesNet</NAME>\n");

        for (String variable : dataset.getVariableNames()) {
            out.write("<VARIABLE TYPE=\"nature\">\n");
            out.write("\t<NAME>" + "`" + variable + "`" + "</NAME>\n"); // @zqian adding back ticks to the name of bayes nodes

            for (String state : dataset.getStates(variable)) {
                out.write("\t<OUTCOME>" + state + "</OUTCOME>\n");
            }

            out.write("</VARIABLE>\n");
        }

        List<Node> nodes = dag.getNodes();
        int nodesNum = nodes.size();
        for (int i = 0; i < nodesNum; i++) {
            Node current = nodes.get(i);
            List<Node> parents = dag.getParents(current);
            int parentsNum = parents.size();
            out.write("<DEFINITION>\n");
            out.write("\t<FOR>" + "`" + current + "`" + "</FOR>\n"); // @zqian
            for (int j = 0; j < parentsNum; j++) {
                out.write("\t<GIVEN>" + "`" + parents.get(j) + "`" + "</GIVEN>\n"); // @zqian
            }

            out.write("</DEFINITION>\n");
        }

        out.write("</NETWORK>\n");
        out.write("</BIF>\n");
        out.close();
    }
}


class BIFHeader {


    public final static String header =
        "<?xml version=\"1.0\"?>\n" +
        "<!-- DTD for the XMLBIF 0.3 format -->\n" +
        "<!DOCTYPE BIF [\n" +
        "	<!ELEMENT BIF ( NETWORK )*>\n" +
        "		<!ATTLIST BIF VERSION CDATA #REQUIRED>\n" +
        "	<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n" +
        "	<!ELEMENT NAME (#PCDATA)>\n" +
        "	<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n" +
        "		<!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n" +
        "	<!ELEMENT OUTCOME (#PCDATA)>\n" +
        "	<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n" +
        "	<!ELEMENT FOR (#PCDATA)>\n" +
        "	<!ELEMENT GIVEN (#PCDATA)>\n" +
        "	<!ELEMENT TABLE (#PCDATA)>\n" +
        "	<!ELEMENT PROPERTY (#PCDATA)>\n" +
        "]>\n\n";
}