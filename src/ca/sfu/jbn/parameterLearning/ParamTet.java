package ca.sfu.jbn.parameterLearning;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.sfu.jbn.common.GetDataset;
import ca.sfu.jbn.common.GraphMAker;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.common.makeBayesPm;
import ca.sfu.jbn.frequency.BayesProbCounter;
import ca.sfu.jbn.frequency.BayesStat;
import ca.sfu.jbn.frequency.Query;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesEstimator;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Node;

public class ParamTet {
	
	public db database = new db();
	private BayesPm bayesPm;

	//private static Parser parser = new Parser();

	private Parser parser = new Parser();

	private Dag dag;
	private ArrayList<String> checkedNodes = new ArrayList<String>(); // check

	// to do
	// each
	// node
	// just
	// once

	public ParamTet(BayesPm bayespm) {
//		database = new db();
		bayesPm = bayespm;
		dag = bayesPm.getDag();

	}

	public ParamTet() {
//		database = new db();
		GraphMAker a = new GraphMAker("");

		EdgeListGraph graph = a.getGraph();
		bayesPm = new BayesPm(makeBayesPm.makepm(graph));
		dag = new Dag(bayesPm.getDag());

	}

	public BayesIm paramterlearning() throws SQLException, IOException{
		ArrayList<String> tables= null;
		try {
			tables = database.getTableNames();
		} catch (SQLException e) {

		}


		// get name of all
		// tables in
		// database
		MlBayesIm FinalIm = null;
		// boolean flag = true;
		FinalIm = new MlBayesIm(bayesPm); // Im will all the right conditional
		// probabilities

		// fill the relationship tables
		fillinBvalues(FinalIm);
		
		
		
		
		//	Elwin commented, use new calculate method!
	
		
		Comparator<String> strc = new CT();
		Collections.sort(tables, strc );


		for (String TableName : tables) {
			ArrayList<Node> nodes = new ArrayList<Node>();
			RectangularDataSet dataset;

			nodes = retrieveNode(TableName, bayesPm); // takes in a table name

			if (nodes.size() == 0)
				continue; // and returns the set of nodes for which their
			// parents are in the same table
			BayesPm Tempbayespm = FindBayesPm(nodes, bayesPm, TableName); // find
			// a
			// bayesPM
			// from the
			// 'big' bayespm
			// , based on
			// the set of
			// nodes
			BayesIm tempIm = new MlBayesIm(Tempbayespm);
			try {
				dataset = GetDataset.getInstance().getData(TableName);
				MlBayesEstimator Mi = new MlBayesEstimator();
				tempIm = Mi.estimate(Tempbayespm, dataset); // UpdatedBayesIm toAdd
				// = new
				// UpdatedBayesIm(tempIm);



				mergeMi(FinalIm, tempIm, nodes);



			} catch (Exception e) {
				// TODO Auto-generated catch blockcheraa
				e.printStackTrace();
			}

		}
		//end of new method

		
//		fillAll(FinalIm);
		
		fillNan(FinalIm);

		writeToBinFile(FinalIm);
		return FinalIm;

	}

	private void fillAll(MlBayesIm FinalIm){
		int nodeNum = FinalIm.getNumNodes();
		BayesStat stat = new BayesStat(FinalIm,parser,database);
		long tt = 0;
		// for each node
		for (int i = 0; i < nodeNum; i++) {

			// each row in the table contains a number of entries
			Node node = FinalIm.getNode(i);
			String nodeName = node.getName(); 
			
			if (nodeName.startsWith("B(")) continue;
			
			int rowNum = FinalIm.getNumRows(i);
			int colNum = FinalIm.getNumColumns(i);

			for (int j = 0; j < rowNum; j++) {

				int numValues=colNum;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals(global.theChar)) numValues--;

				for (int k = 0; k < colNum; k++) {
					
					boolean flag = true;

					Query query = new Query();
					query.evidenceName = new ArrayList<String>();
					query.evidenceValue = new ArrayList<String>();

					query.queryName = nodeName;
					query.queryValNum = numValues;
					query.queryValue = new ArrayList<String>();

					String nodeValue = FinalIm.getBayesPm().getCategory(
							node, k);
					query.queryValue.add(nodeValue);

					int[] parents = FinalIm.getParents(i);
					int[] parentValues = FinalIm.getParentValues(i, j);

					query.evidenceNum = FinalIm.getNumParents(i);

					for (int parentIndex = 0; parentIndex < parents.length; parentIndex++) {
						String parentValue = FinalIm
						.getBayesPm()
						.getCategory(
								FinalIm
								.getNode(parents[parentIndex]),
								parentValues[parentIndex]);
						
						// for each parentIndex
						int pIndex = parents[parentIndex];
						
						String parentName = FinalIm.getNode(pIndex).getName();
						
						if (parentName.startsWith("B(")){
							parentName=parentName.substring(2, parentName.length()-1);
						}
						
						
						if (parentValue.equals("*")){
							flag = false;
							break;
						}
						
						if (!parentValue.equals(global.theChar))
						{
								query.evidenceName.add(parentName);
								query.evidenceValue.add(parentValue);
						}					
						//System.out.println(parentName+" "+parentValue);
					}
					//query.print();
					if (!flag) continue;		
					long start = System.currentTimeMillis();
					double prob = stat.getRowProbabilities(query).get(0);
					long end = System.currentTimeMillis();
					if(!query.evidenceValue.contains("false") && !query.queryValue.contains("false")){
						tt += end - start;
					}
					
					
//					System.out.println(query.queryValue.get(0));
//					query.print();
//					System.out.println(prob);
					FinalIm.setProbability(i, j, k, prob);
				}
			}
		}
		//System.out.println("PLtime(ms)(True): " + tt);
	}

	private void fillNan(MlBayesIm FinalIm){
		boolean changeZero = false; 
		double zeroFillNum = 0.01;


		int nodeNum = FinalIm.getNumNodes();

		// for each node
		for (int i = 0; i < nodeNum; i++) {


			Node node = FinalIm.getNode(i);
			boolean flag= false;
			boolean flag2=false;
			int parentindex = 0;
			int categoryNum = 0;
			if (FinalIm.getParents(i).length>0){
				String parentName = FinalIm.getNode(FinalIm.getParent(i, 0)).getName();

				//find table-relation attribute constraint
				if (parentName.startsWith("B(")){
					String sourceTable = parser.getTableofField(node);

					String thisname = parentName.substring(2,parentName.length()-1);
					if (thisname.equals(sourceTable)) flag = true;
					

					//System.out.println("1: "+node.getName()+" "+sourceTable+" "+parentName.toString());
				}

				//find same table-relation constraint
				for(int j=1;j<FinalIm.getNumParents(i);j++){
					Node node2 = FinalIm.getNode(FinalIm.getParent(i, j));
					String sourceTable2 = parser.getTableofField(node2);
					String sourceTable = parser.getTableofField(node);
					if (sourceTable.equals(sourceTable2)){
						if (flag){

							categoryNum = FinalIm.getParentDim(i, j);
							//System.out.println("2: " + node2.getName()+" "+sourceTable+" "+sourceTable2+" "+categoryNum);
							flag2=true;
							parentindex=j;

						}
					}
				}
			}

			// each row in the table contains a number of entries
			int rowNum = FinalIm.getNumRows(i);
			int colNum = FinalIm.getNumColumns(i);
			for (int j = 0; j < rowNum; j++) {



				int numValues=colNum;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals("*")) numValues--;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals(""+global.theChar)) numValues--;
				for (int k = 0; k < colNum; k++) {

					if(flag2&&((j+1)%categoryNum==0)){//check the same table constraint

						FinalIm.setProbability(i, j, k, 0.000);
						if (k==colNum-1) FinalIm.setProbability(i, j, k, 1.000);
					}
					else{


						//if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), k).equals("*")) continue;

						// first output the weight
						Double prob = FinalIm.getProbability(i, j, k);
						//if (prob.isNaN()) System.out.println(prob);

						//prob = (prob + 0.001)*1000/(1000+numValues);

						if (prob.isNaN()) prob = 1.0/numValues;

						if (k>=numValues) prob=0.0;

						FinalIm.setProbability(i, j, k, prob);
						if (flag){
							if (j>=rowNum*2/3){
								FinalIm.setProbability(i, j, k, 0.000);
								if (k==colNum-1){
									FinalIm.setProbability(i, j, k, 1.000);
								}
							}
						}//endif
					}

					if (changeZero) {
						
						if (FinalIm.getProbability(i,j, k)==0)
							FinalIm.setProbability(i, j, k, zeroFillNum);
					}
				}//endcolum for (most inside)

			}//end row for
		}//end node for
	}

	public void writeToBinFile(BayesIm FinalIm) {
		OutputStream file;
		try {
			file = new FileOutputStream( global.WorkingDirectory + "/" +global.schema + ".bin" );

			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			output.writeObject(FinalIm);
			output.close();
			//System.out.println("finished output to file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
 
	}

	private void fillinBvalues(MlBayesIm finalIm) {
		List<Node> allNodes = finalIm.getDag().getNodes();
		for (Node node : allNodes) {
			if (node.getName().startsWith("B(")) {
				int nodeIndex = finalIm.getNodeIndex(node);
				String name = node.getName();
				name = name.substring(2, name.length() - 1);
				int countR = db.getInstance().countStar(name);
				List<String> tables = Parser.getInstance().getEntities(name);
				int countTables = 1;
				for (String table : tables) {
					countTables *= db.getInstance().countStar(table);
				}
				double value = countR * 1.0 / countTables;
				finalIm.setProbability(nodeIndex, 0, 0, value);

				//elwin added
				finalIm.setProbability(nodeIndex, 0, 1, 0);

				finalIm.setProbability(nodeIndex, 0, 2, 1 - value);
			}

		}

	}

	public List<List<String>> getComb(int numberOfNodes) {
		List<List<String>> returnList = new ArrayList<List<String>>();
		if (numberOfNodes == 1) {
			List<String> patternT = new ArrayList<String>();
			List<String> patternStar = new ArrayList<String>();
			List<String> patternF = new ArrayList<String>();
			patternT.add("T");
			patternStar.add("*");
			patternF.add("F");
			returnList.add(patternT);
			returnList.add(patternStar);
			returnList.add(patternF);

		} else {
			List<List<String>> formerPattern = getComb(numberOfNodes - 1);
			for (List<String> eachPattern : formerPattern) {
				List<String> patternT = new ArrayList<String>(eachPattern);
				List<String> patternStar = new ArrayList<String>(eachPattern);
				List<String> patternF = new ArrayList<String>(eachPattern);
				patternT.add(0, "T");
				patternStar.add(0, "*");
				patternF.add(0, "F");
				returnList.add(patternT);
				returnList.add(patternStar);
				returnList.add(patternF);
			}
		}
		return returnList;
	}

	private void mergeMi(BayesIm FinalIm, BayesIm tempIm, ArrayList<Node> nodes)
	throws SQLException {
		for (Node node : nodes) {
			List<Node> parents = bayesPm.getDag().getParents(node);
			List<Node> boolParents = new ArrayList<Node>();
			boolean hasB = false;

			for (Node parent : parents) {
				if (parent.getName().contains("B(")) {
					boolParents.add(parent);
					hasB = true;

				}
			}
			//node doesn't have boolean parents
			if (!hasB) {
				int nodeIndexDestination = FinalIm.getNodeIndex(node);
				int nodeIndexSource = tempIm.getNodeIndex(node);
				int columnNum = tempIm.getNumColumns(nodeIndexSource);
				int rowNum = tempIm.getNumRows(nodeIndexSource);
				for (int i = 0; i < rowNum; i++) {
					for (int j = 0; j < columnNum; j++) {
						double value = tempIm.getProbability(nodeIndexSource,
								i, j);
						int[] parentValues=tempIm.getParentValues(nodeIndexSource, i);
						int destRowNum=FinalIm.getRowIndex(nodeIndexDestination, parentValues);
						//String catValue=tempIm.getBayesPm().getCategory(node, j);
						//int destColNum=tempIm.getBayesPm().getCategoryIndex(node, catValue);
						FinalIm.setProbability(nodeIndexDestination, destRowNum, j,
								value);
					}
				}
			} else // If one node's parent is relationship 
			{
				int numberOfBoolParents = boolParents.size();
				List<List<String>> combinations = getComb(numberOfBoolParents);

				//elwin comment
				//System.out.println(node.toString());
				//System.out.println(combinations);

				for (List<String> eachCombination : combinations) {

					// if all true, fill in the values
					if (!eachCombination.contains("*")
							&& !eachCombination.contains("F")) {

						insertAllTrue(numberOfBoolParents, FinalIm, tempIm,
								node);

					} else {
						// if no F, then estimates parameters
						if (!eachCombination.contains("F")) {
							insertWithStar(eachCombination, FinalIm, node);
						}
						// if has F lookup
						else {
							insertWithF(eachCombination, FinalIm, tempIm,node);

						}
					}
				}
			}

		}

	}

	private void insertWithF(List<String> eachCombination, BayesIm finalIm,
			BayesIm tempIm, Node node) {


		//parser.getTAbleofField(node)




		int nodeIndexDestination = finalIm.getNodeIndex(node);
		int nodeIndexSource = tempIm.getNodeIndex(node);
		int columnNum = finalIm.getNumColumns(nodeIndexDestination);
		int rowNum = tempIm.getNumRows(nodeIndexSource);
		int Findex = eachCombination.indexOf("F");

		//get the parent with false value
		int[] parentsNode = tempIm.getParents(nodeIndexSource);

		//elwin commented
		//Node falseParentNode = tempIm.getNode(parentsNode[Findex]);

		//		double prob = 1.0;
		//		for(int i = 0 ; i<finalIm.getNumParents(nodeIndexDestination); i++){
		//			prob *= finalIm.getProbability(finalIm.getParent(nodeIndexDestination, i), 0, 0);
		//		}
		//		

		//elwin added
		//System.out.println(node.getName());
		int tempindex = finalIm.getNodeIndex(node);
		String parentName = finalIm.getNode(finalIm.getParent(tempindex, 0)).getName();
		String entityTable = parser.getTableofField(node);

		//System.out.println(parentsNode[0]);
		//String parentName = finalIm.getNode(parentsNode[0]).getName();
		//System.out.println(entityTable+" "+parentName);
		boolean flag= false;
		if (parentName.contains(entityTable)) flag = true;
		//System.out.println(flag);
		if (flag) {
			for (int i = 0; i < rowNum; i++) {
				for (int j = 0; j < columnNum; j++) {

					// int[]
					// parentDim=tempIm.getParentDims(nodeIndexSource);

					int[] parTrueValue = finalIm.getParentValues(
							nodeIndexDestination, i);
					int[] parFalseValue = parTrueValue.clone();
					int[] parStartValue = parTrueValue.clone();
					parFalseValue[Findex] = 2;
					parStartValue[Findex] = 1;
					//elwin
					int rowFalseIndex = finalIm.getRowIndex(nodeIndexDestination,
							parFalseValue);
					//System.out.println(rowFalseIndex);
					try {

						finalIm.setProbability(nodeIndexDestination,
								rowFalseIndex, j, 0);
						if  (j==columnNum-1){
							finalIm.setProbability(nodeIndexDestination,
									rowFalseIndex, j, 1);
						}


					} catch (Exception e) {
						System.out.println("prob is negative in " + node);
					}
				}
			}

		}

		if (!flag){
			for (int i = 0; i < rowNum; i++) {
				for (int j = 0; j < columnNum; j++) {

					// int[]
					// parentDim=tempIm.getParentDims(nodeIndexSource);

					int[] parTrueValue = finalIm.getParentValues(
							nodeIndexDestination, i);
					int[] parFalseValue = parTrueValue.clone();
					int[] parStartValue = parTrueValue.clone();
					parFalseValue[Findex] = 2;
					parStartValue[Findex] = 1;

					//elwin
					double prob = finalIm.getProbability(Findex, 0, 0);


					int rowTrueIndex = finalIm.getRowIndex(nodeIndexDestination,
							parTrueValue);
					int rowStarIndex = finalIm.getRowIndex(nodeIndexDestination,
							parStartValue);
					int rowFalseIndex = finalIm.getRowIndex(nodeIndexDestination,
							parFalseValue);
					double valuetrue = finalIm.getProbability(nodeIndexDestination,
							i, j);
					double valueStar = finalIm.getProbability(nodeIndexDestination,
							rowStarIndex, j);
					double valueFalse = (valueStar - valuetrue * prob) / (1 - prob);
					try {

						if ((valueStar != 0) || (valueStar != valuetrue)) {
							finalIm.setProbability(nodeIndexDestination,
									rowFalseIndex, j, valueFalse);
						}

					} catch (Exception e) {
						System.out.println("prob is negative in " + node);
					}
				}
			}
		}

	}

	private void insertWithStar(List<String> eachCombination, BayesIm finalIm,
			Node node) {
		List<Node> subset = getNodesForStaredParents(node, eachCombination);
		String miniTableName = "";
		try {
			miniTableName = getMinTableName(subset);

			ArrayList<Node> listForNode = new ArrayList<Node>();
			listForNode.add(node);
			BayesPm Tempbayespm = FindBayesPm(listForNode, bayesPm,
					miniTableName);
			// List<Node> temp = Tempbayespm.getDag().getParents(node);
			// temp.removeAll(subset);
			// Tempbayespm.getDag().removeNodes(temp);
			RectangularDataSet dataset;
			dataset = GetDataset.getInstance().getData(miniTableName);
			MlBayesEstimator Mi = new MlBayesEstimator();

			BayesIm tempstarIm = Mi.estimate(Tempbayespm, dataset); // UpdatedBayesIm
			// toAdd
			// = new
			// UpdatedBayesIm(tempIm);

			int nodeIndexDestination = finalIm.getNodeIndex(node);
			int nodeIndexSource = tempstarIm.getNodeIndex(node);
			int sourceColumnNum = tempstarIm.getNumColumns(nodeIndexSource);
			int sourceRowNum = tempstarIm.getNumRows(nodeIndexSource);
			int destColumnNum = finalIm.getNumColumns(nodeIndexDestination);
			int destRowNum = finalIm.getNumRows(nodeIndexDestination);

			List<Node> destParents = new ArrayList<Node>();

			List<Node> sourceParents = new ArrayList<Node>();

			int[] destPar = finalIm.getParents(nodeIndexDestination);
			for (int i = 0; i < destPar.length; i++) {
				int currentPar = destPar[i];
				destParents.add(finalIm.getNode(currentPar));
			}

			int[] sourcePar = tempstarIm.getParents(nodeIndexSource);

			for (int i = 0; i < sourcePar.length; i++) {
				int currentPar = sourcePar[i];
				sourceParents.add(tempstarIm.getNode(currentPar));
			}

			ArrayList<Integer> occurance = new ArrayList<Integer>();
			for (int i = 0; i < destParents.size(); i++) {
				if (sourceParents.contains(destParents.get(i))) {
					occurance.add(1);
				} else
					occurance.add(0);
			}

			// TODO
			// true part
			for (int i = 0; i < sourceRowNum; i++) {
				for (int j = 0; j < sourceColumnNum; j++) {

					// int[]
					// parentDim=tempIm.getParentDims(nodeIndexSource);

					int[] parentsValue = tempstarIm.getParentValues(
							nodeIndexSource, i);
					int[] parValue = new int[destParents.size()];
					int[] parValueFalse = new int[destParents.size()];

					int indexCounter = 0;
					for (String value : eachCombination) {
						if (value.equals("*"))
							parValue[indexCounter++] = 1;

						else if (value.equals("T")) {
							parValue[indexCounter++] = 0;
						} else
							throw new Exception(
									"F value should not occur here.");
					}

					ArrayList<Integer> unrelatedValues = new ArrayList<Integer>();
					int[] parentsNode = tempstarIm.getParents(nodeIndexSource);

					int counter = 0;
					for (int k = indexCounter; k < occurance.size(); k++) {
						if (occurance.get(k) == 1) {
							//here we need to look up the real value of the parent
							int oriParValue=parentsValue[counter++];

							String stringOriParValue=tempstarIm.getBayesPm().getCategory(tempstarIm.getNode(parentsNode[counter-1]), oriParValue);


							//we now need to use the real value to find out its category index in the finalIm
							int categoryIndex=finalIm.getBayesPm().getCategoryIndex(tempstarIm.getNode(parentsNode[counter-1]), stringOriParValue);

							parValue[k] = categoryIndex;
						} else {parValue[k] = 0;
						unrelatedValues.add(k);
						}
					}
					int rowIndex = finalIm.getRowIndex(nodeIndexDestination,
							parValue);
					double value = tempstarIm.getProbability(nodeIndexSource,
							i, j);
					//value of j is also not reliable here
					String stringOriValue=tempstarIm.getBayesPm().getCategory(node, j);
					//we now need to use the real value to find out its category index in the finalIm
					int newCategoryIndex=finalIm.getBayesPm().getCategoryIndex(node, stringOriValue);

					finalIm
					.setProbability(nodeIndexDestination, rowIndex, newCategoryIndex,
							value);
					//finalIm.setProbability(nodeIndexDestination, rowIndex, j,
					//		value);
					for (Integer unrelatedValue : unrelatedValues) {
						Node unrelatedNode = destParents.get(unrelatedValue);
						int numberOfValues = bayesPm
						.getNumCategories(unrelatedNode);
						for (int nodeValue = 0; nodeValue < numberOfValues; nodeValue++) {
							int[] tempParValue = new int[parValue.length];
							for (int valueCounter = 0; valueCounter < parValue.length; valueCounter++) {
								tempParValue[valueCounter] = parValue[valueCounter];
							}
							tempParValue[unrelatedValue] = nodeValue;
							int tempRowIndex = finalIm.getRowIndex(
									nodeIndexDestination, tempParValue);
							finalIm.setProbability(nodeIndexDestination,
									tempRowIndex, newCategoryIndex, value);
						}
					}

					// parValue[0] = 2;
					// int falserow = finalIm.getRowIndex(nodeIndexDestination,
					// parValue);
					// parValue[0] = 0;
					// int truerow = finalIm.getRowIndex(nodeIndexDestination,
					// parValue);
					// double trueprob = finalIm.getProbability(
					// nodeIndexDestination, i, j);
					// finalIm.setProbability(nodeIndexDestination, falserow, j,
					// value - trueprob);

				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertAllTrue(int numberOfBoolParents, BayesIm finalIm,
			BayesIm tempIm, Node node) {
		// TODO Auto-generated method stub

		int nodeIndexDestination = finalIm.getNodeIndex(node);
		int nodeIndexSource = tempIm.getNodeIndex(node);
		int columnNum = tempIm.getNumColumns(nodeIndexSource);
		int rowNum = tempIm.getNumRows(nodeIndexSource);

		for (int i = 0; i < rowNum; i++) {
			for (int j = 0; j < columnNum; j++) {

				// int[]
				// parentDim=tempIm.getParentDims(nodeIndexSource);

				int[] parentsValue = tempIm.getParentValues(nodeIndexSource, i);
				int[] parValue = new int[parentsValue.length
				                         + numberOfBoolParents];
				for (int l = 0; l < numberOfBoolParents; l++) {
					parValue[l] = 0;
				}
				int[] parentsNode = tempIm.getParents(nodeIndexSource);

				for (int k = 0; k < parentsValue.length; k++) {
					//this copy is wrong
					//parValue[k + numberOfBoolParents] = parentsValue[k];
					//here is the fix
					int oriParValue=parentsValue[k];


					String stringOriParValue=tempIm.getBayesPm().getCategory(tempIm.getNode(parentsNode[k]), oriParValue);

					//we now need to use the real value to find out its category index in the finalIm
					int categoryIndex=finalIm.getBayesPm().getCategoryIndex(tempIm.getNode(parentsNode[k]), stringOriParValue);



					parValue[k + numberOfBoolParents] =categoryIndex;
				}

				int rowIndex = finalIm.getRowIndex(nodeIndexDestination,
						parValue);

				// int[] parentsValue =
				// FinalIm.getParents(nodeIndexSource);
				double value = tempIm.getProbability(nodeIndexSource, i, j);

				//value of j is also not reliable here
				String stringOriValue=tempIm.getBayesPm().getCategory(node, j);
				//we now need to use the real value to find out its category index in the finalIm
				int newCategoryIndex=finalIm.getBayesPm().getCategoryIndex(node, stringOriValue);

				finalIm
				.setProbability(nodeIndexDestination, rowIndex, newCategoryIndex,
						value);
			}

		}
	}

	private BayesPm FindBayesPm(ArrayList<Node> nodes, BayesPm bayesPm,
			String tableName) {
		BayesPm returnBayesPm;
		Dag dag = new Dag(bayesPm.getDag());
		List<Node> oldNodes = dag.getNodes();
		List<Node> parentList = new ArrayList<Node>();
		for (Node n : nodes) {
			parentList.addAll(bayesPm.getDag().getParents(n));

		}
		List<Node> tempParentList = new ArrayList<Node>(parentList);
		for (Node tempNode1 : tempParentList) {
			if (tempNode1.getName().contains("B(")) {
				parentList.remove(tempNode1);
			}
		}
		for (Node n : oldNodes) {
			if (!nodes.contains(n) && !parentList.contains(n)) {
				dag.removeNode(n);
			}
		}

		Dag tempDag = new Dag(dag);
		for (Node oneNode : tempDag.getNodes()) {
			ArrayList<String> fields = db.getInstance()
			.describeTable(tableName);
			if (!fields.contains(oneNode.getName())) {
				dag.removeNode(oneNode);
			}
		}

		returnBayesPm = new BayesPm(dag, bayesPm);
		List<Node> nodes1 = returnBayesPm.getMeasuredNodes();
		// returnBayesPm.removeCategories();
		for (Node eachNode : nodes1) {
			// returnBayesPm.getCategory(eachNode,returnBayesPm.getCategoryIndex(eachNode,
			// "*")).;
			//String tbname = parser.getTAbleofField(eachNode);
			ArrayList categories = db.getInstance().Values(tableName, eachNode.getName());

			// returnBayesPm.getDag().removeNode(eachNode);
			returnBayesPm.setCategoriesSFU(eachNode, categories);

			// returnBayesPm.
		}
		return returnBayesPm;
	}

	private ArrayList<Node> retrieveNode(String tableName, BayesPm bayesPm){
		ArrayList<Node> nodesInTable = new ArrayList<Node>();
		ArrayList<Node> returnList = new ArrayList<Node>();
		List<String> nodeNames = bayesPm.getVariableNames();
		ArrayList<String> nodes=null;
		try {
			nodes = database.getColumns(tableName);
		} catch (SQLException e) {
			System.out.println("Problem in Parameter learning while retrieving nodes");
			e.printStackTrace();
		}
		Dag dag = bayesPm.getDag();
		for (String node : nodes) {
			Node tempNode = bayesPm.getNode(node);
			if (tempNode != null) {
				List<Node> basket = dag.getParents(tempNode);
				basket.add(tempNode);
				List<Node> tempBasket = new ArrayList<Node>();
				tempBasket.addAll(basket);
				for (Node tempNode1 : tempBasket) {
					if (tempNode1.getName().contains("B(")) {
						basket.remove(tempNode1);
					}
				}
				if (ListInList(basket, nodes)) {
					if (!checkedNodes.contains(node)) {
						nodesInTable.add(tempNode);
						checkedNodes.add(node);
					}
				}
			}
		}
		return nodesInTable;
	}

	private boolean ListInList(List<Node> basket, ArrayList<String> nodes) {
		boolean returnValue = true;
		for (Node node : basket) {
			if (!(nodes.contains(node.getName()))) {
				returnValue = false;
			}
		}
		return returnValue;
	}

	// cd.orderNode();

	/**
	 * 
	 * @param childNode
	 * @param staredParents
	 * @return the set of nodes
	 */
	private List<Node> getNodesForStaredParents(Node childNode,
			List<String> oneCombination) {
		// ArrayList<Node> returnedNodes =
		List<Node> parentForChild = bayesPm.getDag().getParents(childNode);
		List<Node> parents = new ArrayList<Node>();
		List<Node> nodes = bayesPm.getDag().getNodes();
		for (Node n : nodes) {
			if (parentForChild.contains(n)) {
				parents.add(n);
			}
		}

		List<Node> boolParents = getBoolean(parents);

		List<Node> trueParents = new ArrayList<Node>();
		int counter = 0;
		for (String value : oneCombination) {
			if (value.equals("T")) {
				trueParents.add(boolParents.get(counter));
			}
			counter++;
		}

		parents = removeBoolean(parents);
		List<Node> tempParents = new ArrayList<Node>(parents);

		// delete from parents the nodes connected to the childNode by
		// staredParents
		String childTable = Parser.getInstance().getTAbleofField(childNode);

		for (Node n : tempParents) {

			String tableName = Parser.getInstance().getTAbleofField(n);
			if (tableName.equals(childTable))
				continue;
			else if (checkRelation(tableName, childTable, trueParents))
				continue;
			else {
				parents.remove(n);
			}

		}

		parents.add(childNode);
		return parents;

	}

	private List<Node> getBoolean(List<Node> nodes) {
		ArrayList<Node> newNodes = new ArrayList<Node>();
		for (Node n : nodes) {
			if (n.getName().startsWith("B(")) {
				newNodes.add(n);
			}
		}

		return newNodes;
	}

	private boolean checkRelation(String tableName1, String tableName2,
			List<Node> trueParents) {
		ArrayList<List<String>> relations = new ArrayList<List<String>>();

		for (Node trueParentNode : trueParents) {
			String trueParent = trueParentNode.getName();
			if (trueParent.startsWith("B(")) {
				trueParent = trueParent.substring(2, trueParent.length() - 1);
			}

			List<String> tables = Parser.getInstance().getEntities(trueParent);
			if (tables.contains(tableName1) && tables.contains(tableName2)) {
				return true;
			} else if (tableName1.equals(trueParent)
					&& tables.contains(tableName2)) {
				return true;

			} else if (tableName2.equals(trueParent)
					&& tables.contains(tableName1)) {
				return true;
			}
		}
		return false;
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

		return returnTableName;

	}

	private List<Node> removeBoolean(List<Node> listOfNodes) {
		List<Node> returnNodes = new ArrayList<Node>(listOfNodes);
		for (Node n : listOfNodes) {
			if (n.getName().startsWith("B(")) {
				returnNodes.remove(n);
			}
		}
		return returnNodes;

	}

	public static void main(String[] args) {

		//System.out.println(new BayesProbCounter().learnBayes());
		global.schema = "MovieLens";
		new BayesProbCounter().learnBayes();

	}
	// //private void mergeStar(BayesIm finalIm, BayesIm tempstarIm, Node node)
	// {
	//
	// int nodeIndexDestination = finalIm.getNodeIndex(node);
	// int nodeIndexSource = tempstarIm.getNodeIndex(node);
	// int sourceColumnNum = tempstarIm.getNumColumns(nodeIndexSource);
	// int sourceRowNum = tempstarIm.getNumRows(nodeIndexSource);
	// int destColumnNum = finalIm.getNumColumns(nodeIndexDestination);
	// int destRowNum = finalIm.getNumRows(nodeIndexDestination);
	//
	// List<Node> destParents = new ArrayList<Node>();
	//
	// List<Node> sourceParents = new ArrayList<Node>();
	//
	// int[] destPar = finalIm.getParents(nodeIndexDestination);
	// for (int i = 0; i < destPar.length; i++) {
	// int currentPar = destPar[i];
	// destParents.add(finalIm.getNode(currentPar));
	// }
	//
	// int[] sourcePar = tempstarIm.getParents(nodeIndexSource);
	//
	// for (int i = 0; i < sourcePar.length; i++) {
	// int currentPar = sourcePar[i];
	// sourceParents.add(tempstarIm.getNode(currentPar));
	// }
	//
	// ArrayList<Integer> occurance = new ArrayList<Integer>();
	// for (int i = 0; i < destParents.size(); i++) {
	// if (sourceParents.contains(destParents.get(i))) {
	// occurance.add(1);
	// } else
	// occurance.add(0);
	// }
	//
	// // TODO
	// // true part
	// for (int i = 0; i < sourceRowNum; i++) {
	// for (int j = 0; j < sourceColumnNum; j++) {
	//
	// // int[]
	// // parentDim=tempIm.getParentDims(nodeIndexSource);
	//
	// int[] parentsValue = tempstarIm.getParentValues(
	// nodeIndexSource, i);
	// int[] parValue = new int[destParents.size()];
	//
	// int counter = 0;
	// parValue[0] = 1;
	// for (int k = 1; k < occurance.size(); k++) {
	// if (occurance.get(k) == 1) {
	// parValue[k] = parentsValue[counter++];
	// } else
	// parValue[k] = 0;
	// }
	// int rowIndex = finalIm.getRowIndex(nodeIndexDestination,
	// parValue);
	// double value = tempstarIm.getProbability(nodeIndexSource, i, j);
	// finalIm
	// .setProbability(nodeIndexDestination, rowIndex, j,
	// value);
	// parValue[0] = 2;
	// int falserow = finalIm.getRowIndex(nodeIndexDestination,
	// parValue);
	// parValue[0] = 0;
	// int truerow = finalIm.getRowIndex(nodeIndexDestination,
	// parValue);
	// double trueprob = finalIm.getProbability(nodeIndexDestination,
	// i, j);
	// finalIm.setProbability(nodeIndexDestination, falserow, j, value
	// - trueprob);
	//
	// }
	// }
	//
	// }

}
class CT implements Comparator<String>{

	@Override
	public int compare(String arg0, String arg1) {
		int l1 = arg0.length();
		int l2 = arg1.length();
		if (l1<l2) return -1;
		if (l1>l2) return 1;

		// TODO Auto-generated method stub
		return 0;
	}



}