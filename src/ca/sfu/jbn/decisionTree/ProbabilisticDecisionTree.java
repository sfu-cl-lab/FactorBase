package ca.sfu.jbn.decisionTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.frequency.BayesStat;
import ca.sfu.jbn.parameterLearning.Decisiontree;
import ca.sfu.jbn.parameterLearning.DecisiontreeStruct;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;

/**
 * Probabilistic Decision Tree
 * @author	Yuke Zhu
 * @since	May 19, 2012
 */

public class ProbabilisticDecisionTree {

	ArrayList<TreeNode> nodes;
	HashSet<Integer> targetDomain;
	String targetNode = null;
	String addition = null;
	RectangularDataSet dataSet = null;
	BayesStat stat = null;
	int m_root = 0, nodeCnt = 0;

	public ProbabilisticDecisionTree(String node, BayesStat bayesStat)
	{
		nodes = new ArrayList<TreeNode>();
		targetNode = node;
		targetDomain = new HashSet<Integer>();
		addition = "";
		stat = bayesStat;
	}

	public void buildTree(String node, RectangularDataSet dataSet, String addition)
	{			
		this.addition = addition;
		this.dataSet = dataSet;
		targetDomain = getNodeDomain(targetNode, dataSet, new TreeObservation());
		ArrayList<String> attrNode = new ArrayList<String>(dataSet.getVariableNames());
		TreeObservation obs = new TreeObservation();
		construct(attrNode, dataSet, obs, -1);
	}

	private int construct(ArrayList<String> attrNode, RectangularDataSet dataSet, TreeObservation obs, Integer parentId)
	{
		/* Obtain the valid model under observation */
		RectangularDataSet selectedData = getSelectedModel(dataSet, obs);
		
		/* Statistical variables */
		double minimumGain = -1.0;
		String bestAttr = null;

		/* Choose the best node by information gain */
		for (String attr : attrNode)
		{	
			HashSet<Integer> domain = getNodeDomain(attr, selectedData, obs);
			double informationGain = 0;
			for (int value : domain)
			{
				double valueProb = getProbability(attr, value, selectedData);
				TreeObservation newObs = new TreeObservation(obs);
				newObs.addObservation(attr, value);
				
				/* Calculate the entropy on each branch */				
				RectangularDataSet newDataSet = null;
				if (!attr.equals(targetNode)) 
					newDataSet = getSelectedModel(dataSet, newObs);
				else
					newDataSet = getSelectedModel(dataSet, obs);
				double entropy = 0;
				for (int targetValue : targetDomain)
				{
					double prob = getProbability(targetNode, targetValue, newDataSet);
					entropy += getEntropy(prob); 
				}
				informationGain += valueProb * entropy; 
			}

			/* Check the information gain, keep the smallest one */
			if (minimumGain < 0 || informationGain < minimumGain)
			{
				minimumGain = informationGain;
				bestAttr = attr;
			}
		}

		/* Recursively constructing */
		HashSet<Integer> domain = getNodeDomain(bestAttr, selectedData, obs);
		int bestAttrId = getNodeIndex(bestAttr, selectedData);
		TreeNode newNode = new TreeNode(bestAttr, nodeCnt, parentId);
		nodeCnt ++;

		/* Check if it is the target node */
		if (!bestAttr.equals(targetNode))
		{
			for (int value : domain)
			{				
				double valueProb = getProbability(bestAttr, value, selectedData);
				TreeObservation newObs = new TreeObservation(obs);
				newObs.addObservation(bestAttr, value);
				ArrayList<String> newAttrNode = new ArrayList<String>(attrNode);
				newAttrNode.remove(bestAttr);
				
				int childIdx = construct(newAttrNode, dataSet, newObs, bestAttrId);
				newNode.addNewChild(childIdx, valueProb, String.valueOf(value));
			}
		} else
		{
			/* Split on the target node */
			for (int value : domain)
			{
				TreeNode splitNode = new TreeNode(bestAttr, nodeCnt, newNode.getNodeId(), String.valueOf(value));
				double valueProb = getProbability(bestAttr, value, selectedData);			
				newNode.addNewChild(nodeCnt, valueProb, String.valueOf(value));
				nodes.add(splitNode);
				nodeCnt ++;
			}
		}
		nodes.add(newNode);
		return newNode.getNodeId();
	}

	@SuppressWarnings("unused")
	private void printOutDataSet(RectangularDataSet dataSet)
	{
		System.out.println();
		for (int i=0; i<dataSet.getVariables().size(); i++)
			System.out.print(dataSet.getVariable(i).getName() + "\t");
		System.out.println();
		for (int i=0; i<dataSet.getNumRows(); i++)
		{
			for (int j=0; j<dataSet.getNumColumns(); j++)
			{
				System.out.print(dataSet.getInt(i, j) + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public String getTraverseString(String method)
	{
		List<String> rules = new ArrayList<String>();
		List<Double> weights = new ArrayList<Double>();
		traverse(0, "", 1, rules, weights, method);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<rules.size(); i++)
		{
			String whole = weights.get(i) + " ";
			if (!addition.equals(""))
				whole += addition + rules.get(i);
			else                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
				whole += rules.get(i);
			/* Get rid of "_DUMMY_" in value */
			whole = whole.replace("_DUMMY", "");
			/* For dtlsn method, get rid of unit clause (later added) */
			if (method.equals("dtlsn") && !whole.contains("^")) continue;
			buf.append(whole + "\n");
		}
		return buf.toString();
	}

	public void traverse(int root, String line, double value, List<String> rules, List<Double> weights, String method)
	{
		final int defaultClassNum = 2; 
		TreeNode node = getTreeNode(root);
		
		if (node.isLeafNode())
		{
			// smoothing the weight
			double weight = (value + 0.01) * 100 / (100 + defaultClassNum);
			if (method.equals("dtlog"))
			{
				weight = Math.log(weight);
			}
			else if (method.equals("dtlsn")){

				String nodeName = node.getNodeName();
				String nodeValue = node.getNodeValue();
				
				ArrayList<String> queryName = new ArrayList<String>();
				ArrayList<String> queryValue = new ArrayList<String>();
				queryName.add(nodeName);
				queryValue.add(nodeValue);
				double p = stat.getProbability(queryName, queryValue);
				p = (p + 0.01) * 100 / (100 + defaultClassNum);				

				weight = Math.log(weight) - Math.log(p);
			}
			else if (method.equals("dtcpt")){

			}
			else{
				System.out.println("unknown method:"+method);
			}
			
			weights.add(weight);
			rules.add(line.substring(0, line.length() - 3));
		}
		else
		{
			for (int i=0; i<node.getChildrenId().size(); i++)
			{
				int id = node.getChildrenId().get(i);
				double p = node.getChildrenProb().get(i) * value;
				String pathValue = node.getChildrenPathValue().get(i); 

				String primaryKey = null;
				try {
					primaryKey = Parser.getInstance().getPrimaryKeyForAttr(node.getNodeName());
				}
				catch(Exception e)
				{
					/* Attributes are not learned by structure learning */
//					e.printStackTrace();
//					System.out.println("Cannot get primary key for " + node.getNodeName());
					continue;
				}
				
				String nodeName = node.getNodeName();				
				String nodeValue = pathValue;
				
				String clause = nodeName + "(" + primaryKey +
				"_inst," + nodeName.toUpperCase() + "_" + nodeValue + ")";		

				String newLine = line + clause + " ^ ";
				traverse(id, newLine, p, rules, weights, method);
			}
		}
	}

	/**
	 * Get tree node
	 * @param	index
	 * @return	TreeNode
	 */
	private TreeNode getTreeNode(int index)
	{
		for (int i=0; i<nodes.size(); i++)
		{
			TreeNode node = nodes.get(i);
			if (node.getNodeId() == index)
				return node;
		}
		return null;
	}

	/**
	 * Get the index of a node from its name
	 * @param	nodeName
	 * @param	dataSet
	 * @return	the index of the node with nodeName in this data set
	 */ 
	private int getNodeIndex(String nodeName, RectangularDataSet dataSet)
	{
		int columnNum = dataSet.getNumColumns();
		int columnIdx = -1;
		for (int i=0; i<columnNum; i++)
		{
			if (dataSet.getVariable(i).getName().equals(nodeName))
				columnIdx = i;
		}
		return columnIdx;
	}

	/**
	 * Get the probability of an attribute with a fixed value
	 * @param attr		attribute name
	 * @param value		attribute value
	 * @param dataSet	rectangular data set
	 * @return probability P(attr = value)
	 */
	private double getProbability(String attr, int value, RectangularDataSet dataSet)
	{	
		int cnt = 0, total = 0;
		int columnIdx = getNodeIndex(attr, dataSet);
			
		for (int i=0; i<dataSet.getNumRows(); i++)
		{
			int rowValue = dataSet.getInt(i, columnIdx);
			if (rowValue == value) cnt ++;
			total ++;
		}
		return 1.0 * cnt / total;
	}

	/**
	 * Calculate the entropy given probability
	 * @param	prob
	 * @return	entropy value
	 */
	private double getEntropy(double prob)
	{
		if (prob <= 0.0) return 0.0;
		else return - Math.log(prob) / Math.log(2) * prob;
	}

	/**
	 *	Find all possible values for the variable 
	 *	given the data set and observations
	 *
	 *	@author	Yuke Zhu
	 *	@since	May 23, 2012
	 */
	private HashSet<Integer> getNodeDomain(String attr, RectangularDataSet dataSet, TreeObservation obs)
	{
		HashSet<Integer> domain = new HashSet<Integer>();
		for (int i = 0; i < dataSet.getNumRows(); i++) {
			for (int j = 0; j < dataSet.getNumColumns(); j++) {
				Node variable = dataSet.getVariable(j);
				if (variable instanceof DiscreteVariable) {
					int value = dataSet.getInt(i, j);
					domain.add(value);
				} else {
					throw new IllegalStateException("Expecting a discrete variable.");
				}
			}
		}
		return domain;
	}

	private RectangularDataSet getSelectedModel(RectangularDataSet dataSet, TreeObservation obs)
	{
		int row = dataSet.getNumRows(), column = dataSet.getNumColumns();
		ArrayList<Integer> validRows = new ArrayList<Integer>();
		for(int i=0; i<row; i++)
		{
			boolean valid = true;
			for(int j=0; j<column; j++)
			{
				String nodename = dataSet.getVariable(j).getName();
				Integer value = dataSet.getInt(i, j);
				Integer observation = obs.observe(nodename);
				if (observation != null && value != observation)
				{
					valid = false;
					break;
				}
			}
			if (valid)
			{
				validRows.add(i);
			}
		}
		int[] selectedRows = new int[validRows.size()];
		for (int i=0; i<validRows.size(); i++)
		{
			selectedRows[i] = validRows.get(i);
		}
		RectangularDataSet selectedModel = dataSet.subsetRows(selectedRows);
		return selectedModel;
	}

	public static void main(String[] args) {
		
		// method = dtlog or dtmbn
		String method = "dtlog";
		global.schema="mondelwin";

		if (args.length != 0)
		{
			if (args.length < 3) {
				global.schema = args[0];
				method = args[1];
			} 
			else if (args.length == 4) {
				global.schema = args[0];
				global.dbURL = args[1];
				global.dbUser = args[2];
				global.dbPassword = args[3];
			} else {
				System.out
				.println("argument: dataset name <database connection><databse user><database password>");
				System.exit(1);
			}
		}
		// initialize xml converter to stage the xml file

		System.out.println(global.schema+" "+method);


		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		ReadSQL_MLN_Files r = new ReadSQL_MLN_Files();
		try {
			r.initialize();
			PrintStream out1 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + ".db"));
			PrintStream out2 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
					+ "predicate.mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
					+ "predicate_temp.mln"));

			r.read(out1, out2,out3);
			//System.out.println("DB file and MLN predicate file created");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		//System.out.println("Stucture learning begins");
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out
		.print("SLtime(ms):   ");
		System.out.println(l2 - l);

		try {
			//System.out.println("Parameter learning using Decision Trees begins");
			long l3 = System.currentTimeMillis();

			String rules = "";

			if (method.equals("dtmbn")){
				DecisiontreeStruct tree = new DecisiontreeStruct(bayes);
				rules = tree.decisionTreeLearner();
			}
			else {
				Decisiontree tree = new Decisiontree(bayes);
				rules = tree.decisionTreeLearner(method);
				if (method=="dtlsn"){
					//					append output
					String newoutput = tree.stat.getUnitClauseString();
					rules=rules+newoutput;
				}
			}


			long l4 = System.currentTimeMillis();
			System.out.print("PLtime(ms):  ");
			System.out.println(l4 - l3);
			Writer output = null;

			File file = null;

			if (method.equals("dtmbn")){ 
				file = new File(global.WorkingDirectory + "/" + global.schema
						+ "_VJ_dtstruct.mln");
			}
			else{
				file = new File(global.WorkingDirectory + "/" + global.schema
						+ "_VJ_"+method+".mln");
			}

			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write(rules.toString());
				output.close();
			} catch (Exception e) {

			}
		} catch (Exception e) {

			e.printStackTrace();
		}


		//db.closeDB();
	}

}
