package ca.sfu.cs.factorbase.jbn;

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.search.PatternToDag;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;


public class BayesNet_Learning_main {


    public static void tetradLearner(String srcfile, String destfile) throws Exception {
        tetradLearner(srcfile, null, null, destfile);
    }


    public static void tetradLearner(String srcfile, String required, String forbidden, String destfile) throws Exception {
        DataSet dataset = null;

        File src = new File(srcfile);

        DataReader parser = new DataReader();
        parser.setDelimiter(DelimiterType.TAB);
        dataset = parser.parseTabular(src);
        Ges3 gesSearch = new Ges3(dataset);
        Knowledge knowledge = new Knowledge();

        /* load required knowledge */
        if (required != null) {
            Builder xmlParser = new Builder();
            Document doc = xmlParser.build(new File(required));
            Element root = doc.getRootElement();
            root = root.getFirstChildElement("NETWORK");
            Elements requiredEdges = root.getChildElements("DEFINITION");

            for (int i = 0; i < requiredEdges.size(); i++) {
                Element node = requiredEdges.get(i);
                Element child = node.getFirstChildElement("FOR");
                Elements parents = node.getChildElements("GIVEN");
                for (int j = 0; j < parents.size(); j++) {
                    Element parent = parents.get(j);
                    String childStr = child.getValue();
                    String parentStr = parent.getValue();
                    knowledge.setEdgeRequired(parentStr, childStr, true);
                }
            }
        }

        /* load forbidden knowledge */
        if (forbidden != null) {
            Builder xmlParser = new Builder();
            Document doc = xmlParser.build(new File(forbidden));
            Element root = doc.getRootElement();
            root = root.getFirstChildElement("NETWORK");
            Elements forbiddenEdges = root.getChildElements("DEFINITION");

            for (int i = 0; i < forbiddenEdges.size(); i++) {
                Element node = forbiddenEdges.get(i);
                Element child = node.getFirstChildElement("FOR");
                Elements parents = node.getChildElements("GIVEN");
                for (int j = 0; j < parents.size(); j++) {
                    Element parent = parents.get(j);
                    String childStr = child.getValue();
                    String parentStr = parent.getValue();
                    knowledge.setEdgeForbidden(parentStr, childStr, true);
                }
            }
        }

        System.out.println(knowledge);
        System.out.println("knowledge is DONE~~");
        /* set GES search parameters */
        gesSearch.setKnowledge(knowledge);
        gesSearch.setStructurePrior(1.0000);
        gesSearch.setSamplePrior(10.0000);
        /* learn a dag from data */
        Graph graph = gesSearch.search();
        Pattern pattern = new Pattern(graph);

        PatternToDag p2d = new PatternToDag(pattern);
        Dag dag = p2d.patternToDagMeek();

        System.out.println("DAG is DONE~~~");

        // Output dag into Bayes Interchange format.
        FileWriter fstream = new FileWriter(destfile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(BIFHeader.header);
        out.write("<BIF VERSION=\"0.3\">\n");
        out.write("<NETWORK>\n");
        out.write("<NAME>BayesNet</NAME>\n");

        int col = dataset.getNumColumns();
        int row = dataset.getNumRows();
        for (int i = 0; i < col; i++) {
            out.write("<VARIABLE TYPE=\"nature\">\n");
            out.write("\t<NAME>" + "`" + dataset.getVariable(i).getName() + "`" + "</NAME>\n"); // @zqian adding apostrophes to the name of bayes nodes
            HashSet<Object> domain = new HashSet<Object>();
            for (int j = 0; j < row; j++) {
                domain.add(dataset.getObject(j, i));
            }

            for (Object o : domain) {
                out.write("\t<OUTCOME>" + o + "</OUTCOME>\n");
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