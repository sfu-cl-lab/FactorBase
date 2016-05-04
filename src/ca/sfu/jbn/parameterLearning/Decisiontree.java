package ca.sfu.jbn.parameterLearning;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.Distribution;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import ca.sfu.jbn.common.GetDataset;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.decisionTree.ProbabilisticDecisionTree;
import ca.sfu.jbn.frequency.BayesStat;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;

public class Decisiontree {
	public db database;
	private BayesPm bayesPm;
	private Dag dag;
	private Parser parser = new Parser();
	boolean outputWeight = true;
	public String method = "dtls";

	public BayesIm FinalIm = null;
	public BayesStat stat = null;
	
	String struct = null;

	/**
	 * Predicates that appears in the header in .mln file
	 */
	private HashSet<String> head_predicates = new HashSet<String>();

	/**
	 * Constructor
	 */
	public Decisiontree(BayesPm bayespm) {
		this.database = new db();
		this.bayesPm = bayespm;
		this.dag = bayesPm.getDag();

		ParamTet t = new ParamTet(bayesPm);
		FinalIm = null;
		try {
			FinalIm = t.paramterlearning();
			//			System.out.println(a);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		stat  = new BayesStat(FinalIm);

	}

	/**
	 * Learn on decision tree.
	 * 
	 * @return the content of a mln file
	 * @throws Exception 
	 */
	public String decisionTreeLearner(String newmethod) throws Exception {
		method = newmethod;
		StringBuffer all_rules = new StringBuffer();
		List<Node> nodeList = dag.getNodes();

		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			RectangularDataSet dataset =GetRelatedDataset(node);
			if (dataset == null) continue;

			// if there's a B(..) in the nodes, say B(RA), there should be a predicate
			// B_RA(student_id_inst,prof_id_inst) in every local rule we generate
			// The predicate only contains pk in the table 
			StringBuffer addition = new StringBuffer();

			addition = getBooleanPredicates(node);

			if (node.toString().contains("_dummy")) continue;
			String localrules = WekaTreelearner(node, dataset, addition.toString());
//			String localrules = ProbabilisticDecisionTreeLearner(node, dataset, addition.toString());
			
			all_rules.append(localrules);
		}

		StringBuffer output = new StringBuffer();

		/* Read predicate from <schema>predicate.mln*/
		BufferedReader br = new BufferedReader(new FileReader(
				global.WorkingDirectory + "/" + global.schema + "predicate.mln"));
		String line;
		while ((line = br.readLine()) != null) {
			output.append(line);
			output.append("\n");
		}
		output.append("\n");
		
		if (method.equals("dtmbn"))
		{
			String[] weightedRules = all_rules.toString().split("\n");
			String structureOnly = "";
			for (int k=0; k<weightedRules.length; k++)
			{
				String singleRule = weightedRules[k];
				int idx = singleRule.indexOf(' ');
				singleRule = singleRule.substring(idx+1);
				structureOnly += (singleRule + "\n");
			}
			output.append(structureOnly);
//			System.out.println(structureOnly);
//			this.struct = "";
//			extractRules(all_rules.toString());
//			output.append(this.struct);
		} else
		{
			output.append(all_rules.toString());
		}
		
//		System.out.println(output);
		
		return output.toString();
	}

	public void extractRules(String s)
	{
		String [] rules = s.split("\n");
		
		ArrayList<List<String>> list = new ArrayList<List<String>>();
		
		for (String line : rules)
		{
			List<String> newRule = new ArrayList<String>(); 
			String rule = line.substring(line.indexOf(" ")+1);
			String[] clauses = rule.split(" \\^ ");
			for (String clause : clauses)
			{
				String attr = clause.substring(0, clause.indexOf("("));
				String newClause = clause;
				if (clause.contains(attr.toUpperCase()))
				{
					newClause = clause.substring(0, clause.indexOf(attr.toUpperCase()));
					newClause = newClause + attr.toUpperCase() + "_*)";
				}
				newRule.add(newClause);
			}
			if (!list.contains(newRule)) list.add(newRule);
		}
		
		for (List<String> rule : list)
		{
			traverseRules(rule, 0);
		}
	}
	
	private void traverseRules(List<String> rule, int depth)
	{
		if (depth >= rule.size())
		{
			for (int i=0; i<rule.size(); i++)
			{
//				System.out.print(rule.get(i));
				struct += rule.get(i);
				if (i != rule.size() - 1)
				{
//					System.out.print(" ^ ");
					struct += " ^ ";
				}
			}
			struct += "\n";
//			System.out.println();
			return ;
		}
		String clause = rule.get(depth);
		if (clause.contains("*"))
		{
			
			System.out.println(clause);
			
			String attr = clause.substring(0, clause.indexOf("("));
			int index = FinalIm.getNodeIndex(FinalIm.getNode(attr));
			List<String> domain = new ArrayList<String>();
			int colNum = FinalIm.getNumColumns(index);
			for (int k = 0; k < colNum; k++) {
				String value = FinalIm.getBayesPm().getCategory(FinalIm.getNode(index), k);
				if(value.equals(global.theChar) || value.equals("*")) continue;
				domain.add(value);
			}
			int numValues = domain.size();
			List<String> newRule = new ArrayList<String>(rule);
			for (int j=0; j<numValues; j++)
			{
				String value = domain.get(j);
				String clauseWithValue = clause.replace("*", value);
				newRule.add(depth, clauseWithValue);
				newRule.remove(depth+1);
				traverseRules(newRule, depth+1);
			}		
		} else
		{
			List<String> newRule = new ArrayList<String>(rule);
			traverseRules(newRule, depth+1);
		}
		
	}
	
	private StringBuffer getBooleanPredicates(Node node) throws SQLException {
		List<Node> nodes = dag.getParents(node);
		nodes.add(node);
		StringBuffer addition = new StringBuffer();
		for (Node n : nodes) {
			String nodeName = n.getName();
			if (nodeName.startsWith("B(") && nodeName.endsWith(")")) {

				String tableName = nodeName.substring(2, nodeName.length()-1);
				// Predicate being added
				StringBuffer predicate = new StringBuffer();
				// Predicate should be added to the header
				StringBuffer predicate_head = new StringBuffer();

				predicate.append("B_"+tableName+"(");
				predicate_head.append("B_"+tableName+"(");
				ArrayList<String> cols = GetDataset.getInstance().database.getColumns(tableName);
				for (String s : cols) {

					//elwin add
					//s=s.replaceAll("_dummy", "");

					if ((s.endsWith("id"))||(s.endsWith("id_dummy"))) {
						predicate.append(s+"_inst, ");
						predicate_head.append(s+"_type, ");
					}
				}
				//delete last two chars
				predicate.deleteCharAt(predicate.length()-1);
				predicate.deleteCharAt(predicate.length()-1);
				predicate_head.deleteCharAt(predicate_head.length()-1);
				predicate_head.deleteCharAt(predicate_head.length()-1);
				//add ')'
				predicate.append(")");
				predicate_head.append(")\n");

				addition.append(predicate.toString());
				addition.append(" ^ ");
				head_predicates.add(predicate_head.toString());
			}
		}
		return addition;
	}

	private RectangularDataSet GetRelatedDataset(Node node) throws Exception {
		List<Node> nodes = dag.getParents(node);
		nodes.add(node);
		RectangularDataSet dataset = getDataset(nodes);
		return dataset;
	}
	
	/**
	 * Classify node in the data set using Probabilistic Decision Tree
	 * 
	 * @author	Yuke Zhu
	 * @param	addition Add a predicate that every rule should contain
	 * @return 	A set of rules in the String derived from probabilistic decision tree
	 */
	private String ProbabilisticDecisionTreeLearner(Node node, RectangularDataSet dataset, String addition) {		
		String nodename = node.toString();
		ProbabilisticDecisionTree tree = new ProbabilisticDecisionTree(nodename, stat);
		tree.buildTree(nodename, dataset, addition);
		
		System.out.println("Node: " + nodename);
		System.out.println(dataset.getVariableNames());
		System.out.println(tree.getTraverseString(method));
		
		return tree.getTraverseString(method);
	}

	/**
	 * Classify node in the dataset
	 * 
	 * @param addition Add a predicate that every rule should contain
	 * @return A set of rules in the String derived from decision tree
	 */
	private String WekaTreelearner(Node node, RectangularDataSet dataset, String addition) {

		dataset.setOutputDelimiter(',');
		String csvData = dataset.toString();
				
		//csvData = csvData.replaceAll("_dummy", "");
		Instances train = csv2arff(csvData);
		//System.out.println(node);
		//System.out.println(csvData);
		// Rules generated by the tree
		ArrayList<String> rules = new ArrayList<String>();
		// Initial state of a rule, may contains predicates
		ArrayList<String> init_rule = new ArrayList<String>();

		if (addition.length() > 0) {
			init_rule.add(addition);
		}
		
		try {

			train.setClass(train.attribute(node.getName()));
			// train.setClassIndex(train.numAttributes() - 1);

			J48 j48 = new J48();
			j48.setUnpruned(true);
			j48.setUseLaplace(true);
			j48.buildClassifier(train);
			// Yuke modified on 2012 May 18
			
			traverse(getClassifierTree(j48), init_rule, rules, node);
		} catch (Exception e) {
//			e.printStackTrace();
		}

		StringBuffer res = new StringBuffer();
		for (String s : rules) {


			res.append(s);
		}
		
		return res.toString();
	}
	
	/**
	 * Traverse the ClassifierTree
	 * 
	 * @param node
	 *        current node of the ClassifierTree
	 * @param current_rule
	 *        current stack of predicates
	 * @param global_rules
	 *        global rules that will be returned
	 * @throws Exception
	 */
	private void traverse(ClassifierTree node, ArrayList<String> current_rule,
			ArrayList<String> global_rules,Node nodex) throws Exception {

		//		System.out.println(node);
		if (isLeaf(node)) {
			// get probability
			Distribution d = getlocalModel(node).distribution();
					
//			System.out.println(numClass);
//			for (int i=0; i<numClass; i++)
//			{
////				System.out.println(d.perClass(i));
//				String value = ((Instances) getInstances(node)).classAttribute().value(i);
//				if (value.contains(".0"))
//					value = value.substring(0, value.length() - 2);
//				System.out.println(value + " " + d.perClass(i));
//			}	
			String method_bak = method;
			for (int i=0; i<d.numClasses(); i++)
			{
				/* extract the value */
				String value = (getInstances(node)).classAttribute().value(i);
				
				/* integrate a new method "pruned decision tree" */
				if (method.startsWith("dtpn"))
				{
					String maxValue = (getInstances(node)).classAttribute().value(getlocalModel(node).distribution().maxClass());
					if (!value.equals(maxValue)) continue;
					else
					{
						method = "dt" + method.substring(4);
					}
				}
				
				if (value.contains(".0"))
					value = value.substring(0, value.length() - 2);
				
				// using Laplace to calculate weight
				double weight = (d.perClass(i) + 1) / (d.total() + 2);
					
				//add by elwin
				if (method.equals("dtls")){
					weight = Math.log(weight) - Math.log(1.0/d.numClasses());
				}
				else if (method.equals("dtlog")){
					weight = Math.log(weight);
				}
				else if (method.equals("dtlsn")){
					String nodeName = nodex.toString();
					
					ArrayList<String> queryName = new ArrayList<String>();
					ArrayList<String> queryValue = new ArrayList<String>();
					queryName.add(nodeName);
					queryValue.add(value);
					
					double p = stat.getProbability(queryName, queryValue);
					p = (p + 0.01)*100/(100 + d.numClasses());
					weight = Math.log(weight) - Math.log(p);	
				}
				else if (method.equals("dtcpt")){
	
				}
				else if (method.equals("dtmbn")){
					
				} else {
					System.out.println("unknown method:"+method);
				}
	
				String last_predicate = makePredicate(node, 0);
				last_predicate = last_predicate.substring(0, last_predicate.length() - 1);
				last_predicate += value;
	
				//elwin add to eliminate .0
				if (last_predicate.contains(".0")) 
					last_predicate = last_predicate.substring(0, last_predicate.length() - 2);

				last_predicate += ")";	
				current_rule.add(last_predicate);
	
				// save this rule
				StringBuffer sb = new StringBuffer();
	
				if (outputWeight) {
					sb.append(weight);
					sb.append(" ");
				}
	
				for (String r : current_rule) {
					sb.append(r);
				}
				sb.append("\n");
				
				global_rules.add(sb.toString());
	
				// reset current_rule
				current_rule.remove(current_rule.size() - 1);
				
				method = method_bak;
			}
			
			
			return;
		}

		for (int i = 0; i < getSons(node).length; i++) {
			// add current node's predicate
			current_rule.add(makePredicate(node, i));
			current_rule.add(" ^ ");
			traverse(getSons(node)[i], current_rule, global_rules,nodex);
			// reset current_rule
			current_rule.remove(current_rule.size() - 1);
			current_rule.remove(current_rule.size() - 1);
		}
	}

	/**
	 * Make a predicate from a ClassifierTree Node and an index
	 */
	private String makePredicate(ClassifierTree node, int index) {
		try {
			StringBuffer sb = new StringBuffer(); // predicate add in the rules
			String sbstr = "";
			StringBuffer sb_head = new StringBuffer(); // predicate in the head

			String str = null;
			str = getlocalModel(node).leftSide(getInstances(node));
			if (str.length() == 0) {
				str = getInstances(node).classAttribute().name();
			}

			//elwin
			//System.out.println("str is "+str);
			String nodeName = str;
			//str=str.replaceAll("_dummy", "");

			//elwin

			//		

			// store pk in cols
			String table = parser.getTAbleofField(str);
			//elwin
			//System.out.println("table is "+table);
			ArrayList<String> cols = database.getColumns(table);
			for (int i = 0; i < cols.size(); i++) {
				//System.out.println("Has col "+cols.get(i));
				if ((cols.get(i).endsWith("id"))||(cols.get(i).endsWith("id_dummy"))) {
				} else {
					cols.remove(i);
					i--;
				}
			}

			sb.append(str.replaceAll("_dummy", "") + "(");
			//sbstr+=str + "(";
			sb_head.append(str + "(");
			if (cols.size() > 0) {
				sb.append(cols.get(0)+"_inst");

				sb_head.append(cols.get(0)+"_type");
				for (int i = 1; i < cols.size(); i++) {
					sb.append(", "+cols.get(i)+"_inst");
					sb_head.append(", "+cols.get(i)+"_type");
				}
				sb.append(", ");
				sb_head.append(", ");
			}
			str=str.replace("_dummy", "");
			sb.append(str.toUpperCase() + "_");
			sb_head.append(str+"_type");
			str = getlocalModel(node).rightSide(index, getInstances(node));
			//elwin add to eliminate .0
			if (str.contains(".0")) str=str.substring(0, str.length()-2);

			//int nodeValue = Integer.parseInt(str);

			if (str.startsWith(" = ")) {
				sb.append(str.substring(3));
			}



			sb.append(")");
			sb_head.append(")\n");

			//			String sentence = sb;
			//			sb = sentence.replace(nodeName.replaceAll("_dummy", "") + "_inst",
			//					nodeName.toUpperCase().replaceAll("_DUMMY", "") + "_" + nodeValue);
			//			


			head_predicates.add(sb_head.toString());

			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * find the correct join for the nodes and put the child node as the last
	 * column
	 * 
	 * @param nodeList
	 * @return dataset
	 * @throws Exception 
	 */
	private RectangularDataSet getDataset(List<Node> nodeList)
	throws Exception {
		//        HashSet<String> tables = new HashSet<String>();
		ArrayList<Node> nodes = new ArrayList<Node>();

//		System.out.println("Get dataset");
//		System.out.println(nodeList);
		
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			String nodeName = node.getName();

			if (!(nodeName.startsWith("B(") && nodeName.endsWith(")")))
				nodes.add(node);
		}

		if(nodes.size()>0){
			String tablename = getMinTableName(nodes);
			RectangularDataSet ds = GetDataset.getInstance().getData(tablename); 
			//        ArrayList<String> tableList = new ArrayList<String>(tables);
			//if (parser.getRelations().contains(tablename)){
			//ArrayList ent = parser.getEntities(tablename);
			//}
			//        RectangularDataSet ds = JoinRemovePrimaryRelation((String) tableList.get(0), ent);
			ArrayList<String> nodes_str = new ArrayList<String>();
			for (Node n : nodes) nodes_str.add(n.getName());
			for (int i = 0; i < ds.getNumColumns(); i++) {
				Node n = ds.getVariable(i);
				if (n.getName().endsWith("id")) {
					head_predicates.add(n.getName()+"("+n.getName()+"_type)\n");
				}
				if (!nodes_str.contains(ds.getVariable(i).getName())) {
					ds.removeColumn(i);
					i--;
				}
			}
			return ds;
		}
		else{
			return null;    
		}
	}

	//    private RectangularDataSet JoinRemovePrimaryRelation(String fileName,
	//            ArrayList<String> refEntities) {
	//        String tableName = database.joinNatural1(fileName, refEntities);
	//        RectangularDataSet dataset = null;
	//
	//        ArrayList id = new ArrayList();
	//        try {
	//            dataset = GetDataset.getInstance().GetData(tableName);
	//        } catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//        for (int y = 0; y < refEntities.size(); y++) {
	//            int index = parser.getEntityIndex(refEntities.get(y).toString());
	//            ArrayList res = parser.getEntityId(index);
	//            for (int k = 0; k < res.size(); k++)
	//                id.add(res.get(k));
	//        }
	//        return dataset;
	//    }

	/**
	 * Convert csv data to arff data
	 * 
	 * @param csvStr
	 *            a string of csv file
	 * @return constructed Instances (arff file)
	 */
	private Instances csv2arff(String csvStr) {
		CSVLoader loader = new CSVLoader();
		Instances data = null;
		ByteArrayInputStream csv = new ByteArrayInputStream(csvStr.getBytes());

		try {
			loader.setSource(csv);
			loader.setNominalAttributes("first-last"); // force nominal
			//System.out.println("h1!");
			data = loader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("h2!");   
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		return saver.getInstances();
	}

	/**
	 * Use reflection to get private field m_root in a j48 tree
	 */
	private static ClassifierTree getClassifierTree(J48 j48) throws Exception {
		Field field = J48.class.getDeclaredField("m_root");
		field.setAccessible(true);
		ClassifierTree mRootTree = (ClassifierTree) field.get(j48);
		return mRootTree;
	}

	/**
	 * Use reflection to get private field m_localModel in a ClassifierTree
	 */
	private static ClassifierSplitModel getlocalModel(ClassifierTree ct)
	throws Exception {
		Field field = ClassifierTree.class.getDeclaredField("m_localModel");
		field.setAccessible(true);
		ClassifierSplitModel m_localModel = (ClassifierSplitModel) field
		.get(ct);
		return m_localModel;
	}

	/**
	 * Use reflection to get private field m_train in a ClassifierTree
	 */
	private static Instances getInstances(ClassifierTree ct) throws Exception {
		Field field = ClassifierTree.class.getDeclaredField("m_train");
		field.setAccessible(true);
		Instances m_train = (Instances) field.get(ct);
		return m_train;
	}

	/**
	 * Use reflection to get private field m_sons in a ClassifierTree
	 */
	private static ClassifierTree[] getSons(ClassifierTree ct) throws Exception {
		Field field = ClassifierTree.class.getDeclaredField("m_sons");
		field.setAccessible(true);
		ClassifierTree[] m_sons = (ClassifierTree[]) field.get(ct);
		return m_sons;
	}

	/**
	 * Use reflection to get private field m_isLeaf in a ClassifierTree
	 */
	private static boolean isLeaf(ClassifierTree ct) throws Exception {
		Field field = ClassifierTree.class.getDeclaredField("m_isLeaf");
		field.setAccessible(true);
		boolean m_isLeaf = (Boolean) field.get(ct);
		return m_isLeaf;
	}

	private String getMinTableName(List<Node> nodes) throws SQLException {
		String returnTableName = "";
		List<String> allTables = db.getInstance().getTableNames();
		List<String> nodesString = new ArrayList<String>();
		for (Node n : nodes) {
			nodesString.add(n.getName());
		}
		for (String oneTable : allTables) {
			List<String> fields = db.getInstance().describeTable(oneTable);
			if (fields.containsAll(nodesString)) {
				returnTableName = oneTable;
				break;
			}
		}

		// There's no single table which contains all the attributes we want
		// So we create a new view here

		//System.out.println(returnTableName);
		return returnTableName;
	}

	/**
	 * Test Code
	 */
	public static void main(String[] args) throws Exception {

		Decisiontree a = new Decisiontree(null);
		ArrayList<Node> listForNode = new ArrayList<Node>();
		a.getMinTableName(listForNode);

		String filename;
		if (args.length > 0) {
			filename = args[0];
		} else {
			filename = "/Users/YOYO/Desktop/zoo.arff";
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Instances train = new Instances(reader);
			reader.close();
			train.setClassIndex(train.numAttributes() - 1);

			J48 j48 = new J48();
			j48.setUnpruned(true);
			j48.setUseLaplace(true); // using Laplace

			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(j48);

			// train and make predictions
			fc.buildClassifier(train);

			ClassifierTree ct = getClassifierTree(j48);
			// System.out.println(ct);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
