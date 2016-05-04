package ca.sfu.jbn;

//import edu.cmu.tetrad.data.DataParser;    //noe exists in new tetrad; replace with DataReader. Aug 21 Yan
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.Knowledge;
//import edu.cmu.tetrad.data.RectangularDataSet;  //not exists in new tetrad; replace with DataSet. Aug 21 Yan
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;
import edu.cmu.tetrad.search.*;
//import edu.cmu.tetrad.search.GesSearch;
//import edu.cmu.tetrad.search.GesSearch3;
//import edu.cmu.tetrad.search.PatternToDagSearch;  //not exists in new tetrad; replace with PatternToDag. Aug 21 Yan
import edu.cmu.tetrad.search.PatternToDag; 
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.estimate.MultiNomialBMAEstimator;
import weka.classifiers.bayes.net.search.global.K2;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;

public class BayesNet_Learning_main {

	public static void wekaLearner(String arffFile) throws Exception
	{
		Instances ins = DataSource.read( arffFile );
		ins.setClassIndex(0);

		K2 learner = new K2();

		MultiNomialBMAEstimator estimator = new MultiNomialBMAEstimator();
		estimator.setUseK2Prior(true);

		EditableBayesNet bn = new EditableBayesNet( ins );
		bn.initStructure();

		learner.buildStructure(bn, ins);
		estimator.estimateCPTs(bn);	

		System.out.println(bn);
	}

	public static void tetradLearner(String srcfile, String destfile) throws Exception
	{
		tetradLearner(srcfile, null, null, destfile);
	}
	
	public static void tetradLearner(String srcfile, String required, String forbidden, String destfile) throws Exception
	{
		/* initialization */
		//TetradLogger.getInstance().addOutputStream(System.out);
		//RectangularDataSet dataset = null;
		//RectangularDataSet is replaced by DataSet in new tetrad.  Aug 21 Yan
	    DataSet dataset = null;
		
		File src = new File(srcfile);
		
		//DataParser parser = new DataParser();
		//DataParser is replaced by DataReader in new tetrad.  Aug 21 Yan
		DataReader parser = new DataReader();
		parser.setDelimiter(DelimiterType.TAB);
     	dataset = parser.parseTabular(src);     
		System.out.print("isMulipliersCollapsed: " +dataset.isMulipliersCollapsed()+" \n");
		Ges3 gesSearch = new Ges3(dataset);
		Knowledge knowledge = new Knowledge();

		/* load required knowledge */
		if (required != null)
		{
			Builder xmlParser = new Builder();
			//Document doc = xmlParser.build(required);
            Document doc = xmlParser.build(new File(required));
			Element root = doc.getRootElement();
			root = root.getFirstChildElement("NETWORK");
			Elements requiredEdges = root.getChildElements("DEFINITION");

			for (int i=0; i<requiredEdges.size(); i++)
			{
				Element node = requiredEdges.get(i);
				Element child = node.getFirstChildElement("FOR");
				Elements parents = node.getChildElements("GIVEN");
				for (int j=0; j<parents.size(); j++)
				{
					Element parent = parents.get(j);
					String childStr = child.getValue(), parentStr = parent.getValue();
					//System.out.println("Test, Required: " + parentStr + " " + childStr);
					knowledge.setEdgeRequired(parentStr, childStr, true);
				}
			}
		}

		/* load forbidden knowledge */
		if (forbidden != null)
		{
			Builder xmlParser = new Builder();
			//Document doc = xmlParser.build(forbidden);
            Document doc = xmlParser.build(new File(forbidden));
			Element root = doc.getRootElement();
			root = root.getFirstChildElement("NETWORK");
			Elements forbiddenEdges = root.getChildElements("DEFINITION");

			for (int i=0; i<forbiddenEdges.size(); i++)
			{
				Element node = forbiddenEdges.get(i);
				Element child = node.getFirstChildElement("FOR");
				Elements parents = node.getChildElements("GIVEN");
				for (int j=0; j<parents.size(); j++)
				{
					Element parent = parents.get(j);
					String childStr = child.getValue(), parentStr = parent.getValue();
					//System.out.println("Test, Forbidden: " + parentStr + " " + childStr);
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
	//	System.out.println("here you are ~~");
		/* learn a dag from data */
		Graph graph = gesSearch.search();
		//System.out.println("gesSearch.search() is done, oct 30");
		Pattern pattern = new Pattern(graph);
		
		//PatternToDagSearch is replaced by PatternToDag in new tetrad. Aug 21 Yan
		//PatternToDagSearch p2d = new PatternToDagSearch(pattern);
		PatternToDag p2d = new PatternToDag(pattern);
		//System.out.println("Entering patternToDagMeek(), oct 30");
		Dag dag = p2d.patternToDagMeek();
		//Dag dag = p2d.patternToDagDorTarsi();   //#######################################?????
		
		//System.out.println("patternToDagMeek() is done, oct 30");
		//System.out.println("Final DAG Starts");
		//System.out.println(dag);
		System.out.println("DAG is DONE~~~");
		
		///////////////////////////////////////////
		//April 16th @zqian here the score can not be used for comparison since it's based on the much smaller join
	/*	BayesProperties scorer = new BayesProperties(dataset,dag);
		System.out.println("%%%% Tuples of Data = " + dataset.getNumRows());
		System.out.println("%%%% BIC score = "+scorer.getBic());		
		System.out.println("%%%% AIC score = "+scorer.getAic());
		System.out.println("%%%% loglikelihood = " + scorer.getloglikelihood());
		System.out.println("%%%% P-value = " + scorer.getPValue());
        System.out.println("%%%% Chisq = " + scorer.getPValueChisq());
        System.out.println("%%%% Dof = " + scorer.getPValueDf()); 
        */
        ////////////////////// computing these score is very time consuming
        
        
        /* output dag into Bayes Interchange format */
		FileWriter fstream = new FileWriter(destfile);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(BIFHeader.header);
		out.write("<BIF VERSION=\"0.3\">\n");
		out.write("<NETWORK>\n");
		out.write("<NAME>BayesNet</NAME>\n");

		int col = dataset.getNumColumns(), row = dataset.getNumRows();
		for (int i=0; i<col; i++)
		{
			out.write("<VARIABLE TYPE=\"nature\">\n");
			out.write("\t<NAME>" + "`"+dataset.getVariable(i).getName() +"`"+ "</NAME>\n"); //@zqian adding apostrophes to the name of bayes nodes
			HashSet<Object> domain = new HashSet<Object>();
			for (int j=0; j<row; j++)
				domain.add(dataset.getObject(j, i));
			for (Object o : domain)
			{
				out.write("\t<OUTCOME>" + o + "</OUTCOME>\n");
			}
			out.write("</VARIABLE>\n");
		}

		List<Node> nodes = dag.getNodes();
		int nodesNum = nodes.size();
		for (int i=0; i<nodesNum; i++)
		{
			Node current = nodes.get(i);
			List<Node> parents = dag.getParents(current);
			int parentsNum = parents.size();
			out.write("<DEFINITION>\n");
			out.write("\t<FOR>" +"`"+ current +"`"+ "</FOR>\n"); //@zqian
			for (int j=0; j<parentsNum; j++)
				out.write("\t<GIVEN>" +"`"+ parents.get(j) +"`"+ "</GIVEN>\n");//@zqian
			out.write("</DEFINITION>\n");
		}

		out.write("</NETWORK>\n");
		out.write("</BIF>\n");
		out.close();
		
		
		/*//@zqian : redirectinig the dag to file: dag.txt.
		// only need the final dag
		File file = new File("dag.txt");
		PrintStream console = System.out;
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		System.out.println(dag);
		System.setOut(console);
		*/
	}

	/*pruning phase, zqian @ Oct 23 2013*/
	public static void tetradLearner_BES(String srcfile, String required, String destfile) throws Exception
	{
		/* initialization */
		
		//RectangularDataSet is replaced by DataSet in new tetrad.  Aug 21 Yan
	    DataSet dataset = null;
	    File src = new File(srcfile);
			
		//DataParser is replaced by DataReader in new tetrad.  Aug 21 Yan
		DataReader parser = new DataReader();
		parser.setDelimiter(DelimiterType.TAB);
     	dataset = parser.parseTabular(src);     	     	
		System.out.print("isMulipliersCollapsed: " +dataset.isMulipliersCollapsed()+" \n");	
	
		//GesSearch gesSearch = new GesSearch(dataset);
		Ges3 gesSearch = new Ges3(dataset);
		Knowledge knowledge = new Knowledge();

		/* load required knowledge */
		if (required != null)
		{
			Builder xmlParser = new Builder();
			//Document doc = xmlParser.build(required);
            Document doc = xmlParser.build(new File(required));
			Element root = doc.getRootElement();
			root = root.getFirstChildElement("NETWORK");
			Elements requiredEdges = root.getChildElements("DEFINITION");

			for (int i=0; i<requiredEdges.size(); i++)
			{
				Element node = requiredEdges.get(i);
				Element child = node.getFirstChildElement("FOR");
				Elements parents = node.getChildElements("GIVEN");
				for (int j=0; j<parents.size(); j++)
				{
					Element parent = parents.get(j);
					String childStr = child.getValue(), parentStr = parent.getValue();
					//System.out.println("Test, Required: " + parentStr + " " + childStr);
					knowledge.setEdgeRequired(parentStr, childStr, true);
				}
			}
		}

		
		System.out.println(knowledge);
		System.out.println("*************knowledge is DONE~~ \n");
		/* set GES search parameters */
		gesSearch.setKnowledge(knowledge);
		
		/* pruning part */
		Graph graph = gesSearch.Pruning_BES(); //
		/* pruning part */
		Pattern pattern = new Pattern(graph);
		
		//PatternToDagSearch is replaced by PatternToDag in new tetrad. Aug 21 Yan
		PatternToDag p2d = new PatternToDag(pattern);
		Dag dag = p2d.patternToDagMeek();
		System.out.println("Final DAG Starts");
		System.out.println(dag);
		System.out.println("DAG is DONE~~~");
		
		
        
        
        /* output dag into Bayes Interchange format */
		FileWriter fstream = new FileWriter(destfile);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(BIFHeader.header);
		out.write("<BIF VERSION=\"0.3\">\n");
		out.write("<NETWORK>\n");
		out.write("<NAME>BayesNet</NAME>\n");

		int col = dataset.getNumColumns(), row = dataset.getNumRows();
		for (int i=0; i<col; i++)
		{
			out.write("<VARIABLE TYPE=\"nature\">\n");
			out.write("\t<NAME>" + "`"+dataset.getVariable(i).getName() +"`"+ "</NAME>\n"); //@zqian adding apostrophes to the name of bayes nodes
			HashSet<Object> domain = new HashSet<Object>();
			for (int j=0; j<row; j++)
				domain.add(dataset.getObject(j, i));
			for (Object o : domain)
			{
				out.write("\t<OUTCOME>" + o + "</OUTCOME>\n");
			}
			out.write("</VARIABLE>\n");
		}

		List<Node> nodes = dag.getNodes();
		int nodesNum = nodes.size();
		for (int i=0; i<nodesNum; i++)
		{
			Node current = nodes.get(i);
			List<Node> parents = dag.getParents(current);
			int parentsNum = parents.size();
			out.write("<DEFINITION>\n");
			out.write("\t<FOR>" +"`"+ current +"`"+ "</FOR>\n"); //@zqian
			for (int j=0; j<parentsNum; j++)
				out.write("\t<GIVEN>" +"`"+ parents.get(j) +"`"+ "</GIVEN>\n");//@zqian
			out.write("</DEFINITION>\n");
		}

		out.write("</NETWORK>\n");
		out.write("</BIF>\n");
		out.close();
		
		
		/*//@zqian : redirectinig the dag to file: dag.txt.
		// only need the final dag
		File file = new File("dag.txt");
		PrintStream console = System.out;
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		System.out.println(dag);
		System.setOut(console);
		*/
	}

	
	public static void jbnMain(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			System.out.println("Usage: bayesLearner <csv>");
			System.out.println("Usage: bayerLearner <csv> <required knowledge> <forbidden knowledge>");
			System.exit(0);
		}
		
		if (args.length < 3)
		{
			BayesNet_Learning_main.tetradLearner(args[0], null, null, "bif.xml");
		} else
		{
			BayesNet_Learning_main.tetradLearner(args[0], args[1], args[2], "bif.xml");
		}
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

	public static void main(String[] args)
	{
		System.out.print(header);
	}
}

/* 
data.arff
@relation test

@attribute x {0,1}
@attribute y {0,1,2}
@attribute z {0,1}

@data
0,1,0
1,0,1
1,1,1
1,2,1
0,0,0

data.dat
int	ranking	gpa
1	1	2
1	1	1
3	2	3
1	2	2
2	1	1
2	2	1
1	1	2
2	1	3
1	1	1
 */
