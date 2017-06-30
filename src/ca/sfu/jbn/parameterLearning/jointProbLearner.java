package ca.sfu.jbn.parameterLearning;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ca.sfu.jbn.common.ChangeGraph;
import ca.sfu.jbn.common.GraphMAker;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.makeBayesPm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Node;

public class jointProbLearner {
	
	private BayesPm bayePm;
	public DirichletBayesIm dirich;
	//public DirichletBayesIm dirichJoint;
	private EdgeListGraph graph = null;
	private Dag dag;
	public db database ;
	public Map relation = new HashMap();
	private Parser parser = new Parser();
	private ChangeGraph cd;
	private GraphMAker graphmaker;
	private ArrayList entity_att = parser.getEntity_att();
	private ArrayList relation_att = parser.getRelation_att();
	public ArrayList relations = parser.getRelations();
	private boolean containsFalse = true;
	public static boolean use =true;
	 private RandomAccessFile f;
	// ////////////////
	// Constructor
	// //////////////
	
		public jointProbLearner(BayesPm bayespm) {
			database = new db();
			bayePm = bayespm;
			dag = bayePm.getDag();
			// initialize dirichletBayesIm
			dirich = DirichletBayesIm.blankDirichletIm(bayespm);
		//	dirichJoint = DirichletBayesIm.blankDirichletIm(bayePm);

			probOfRelations();

			// cd.orderNode();
		}
	 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 
	 public jointProbLearner() {
		database = new db();
		GraphMAker a = new GraphMAker("C:/Documents and Settings/hkhosrav/Desktop/graphClassification.txt");
		graph = a.getGraph();
		bayePm = makeBayesPm.makepm(graph);
		dag = bayePm.getDag();
		// initialize dirichletBayesIm
		dirich = DirichletBayesIm.blankDirichletIm(bayePm);
		//dirichJoint = DirichletBayesIm.blankDirichletIm(bayePm);
		probOfRelations();
		try {
			f = new RandomAccessFile("FalseTime.txt","rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
	////////////////////////////////
	public jointProbLearner(EdgeListGraph graph2, String fileName) {
		parser.setParseFile("relation2.xml");
		entity_att = parser.getEntity_att();
		relation_att = parser.getRelation_att();
		relations = parser.getRelations();
		database = new db();
		GraphMAker a = new GraphMAker(
		"C:/Documents and Settings/hkhosrav/Desktop/newMovieLens.txt");
		graph = a.getGraph();
		makeBayesPm.changeParseFile(fileName);
		bayePm = makeBayesPm.makepm(graph);
		dag = bayePm.getDag();
		dirich = DirichletBayesIm.blankDirichletIm(bayePm);
	//	dirichJoint = DirichletBayesIm.blankDirichletIm(bayePm);
		try {
			f = new RandomAccessFile("FalseTime.txt","rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
	/////////////////////////////////////////
	public void changeParseFile(String fileName){
		parser.setParseFile(fileName);
		parser.makePrser();
		
	}
///////////////////
	public EdgeListGraph getGraph() {
		return graph;
	}
	/////////////////////////
	public void setGraph(EdgeListGraph graph) {
		this.graph = graph;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////


	// ////////////////////////////////////////////////////////
	private String checkIndependency(String tableName, String where1) {
		


		String[] conditions = where1.split(" and ");
		for (int i = 1; i < conditions.length; i++) {
			String field = conditions[i].substring(0, conditions[i].indexOf("="));
			if (!tableName.contains(parser.getTAbleofField(field))) {
				//if(!where1.endsWith(" "))
					//where1+= " ";
				String temp = where1; 
				where1 = where1.replaceAll("and "+conditions[i]+" ", "");
				if(temp.equals(where1))
					where1 = where1.replaceAll("and "+conditions[i], "");
			}
			
		}
		//where1 = new String(result.substring(0, result.length() -5));
		
		//added becuase of the stupidity of java and string problems. 
		if(where1.length() <= 3)
			return "";
		return where1;

	}

	// /////////////////////////////////////////////
	public DirichletBayesIm computeCPT() {
		int[] parents;
		int[] parentsValues;
		double res = 0;
		String where;
		String where1;
		ArrayList BParents;
		ArrayList RIIndex;
		int validation;

		// for each node of dag
		for (int i = 0; i < dag.getNodes().size(); i++) {
			int nodeIndex = dirich.getNodeIndex(dag.getNodes().get(i));
			
			Node node = (dag.getNodes().get(i));
			parents = dirich.getParents(nodeIndex);
			
			BParents = new ArrayList();
			RIIndex = new ArrayList();
			// find RI parents
			findRIParents(parents, BParents, RIIndex);
			String tableName = new String();
			String[] tableNameComma = new String[1];

			for (int j = 0; j < dirich.getNumRows(nodeIndex); j++) {
				// j is the RowIndex
				parentsValues = dirich.getParentValues(nodeIndex, j);
				boolean isThereFalse = false;
				boolean isThereStar = false;
				int indexOfFalse = -1;
				String field = new String();
				String nameOfNodeEntity = new String();
				ArrayList indexOfStarsRI = new ArrayList();

				// this block finds the most left RI which is not true
				for (int k = 0; k < RIIndex.size(); k++) {
					int ind = Integer.parseInt(RIIndex.get(k).toString());

					if (bayePm.getCategory(dirich.getNode(parents[ind]),
							parentsValues[ind]).equals("false")) {
						isThereFalse = true;
						// index of the most left False
						indexOfFalse = k;
						break;

					}
					if (bayePm.getCategory(dirich.getNode(parents[ind]),
							parentsValues[ind]).equals("*")) {
						// it holds the index of stared RIs
						isThereStar = true;
						// index of stared RI
						indexOfStarsRI.add(k);

					}

				} // end of for ,for finding RI's value

				ArrayList staredVarIndex = new ArrayList(); // Contains all
				// stared variables
				
				validation = validation(BParents, RIIndex, parents, parentsValues,
						staredVarIndex, dirich.getNodeIndex(dirich.getNode("B("+parser.getTAbleofField(node)+")")));
				if (validation!= -1) {

					if (node.getName().startsWith("B(")) {

						String temp = node.getName();
						temp = temp.substring(2, temp.length() - 1);
						double probOfRelation;
						////////////////////
						///
						/// Or added because of classification
						////
						//////////////////
						if (relation.containsKey(temp))
							probOfRelation = Double.parseDouble(relation.get(
									temp).toString());
						else if (relation.containsKey("New"+temp))
							probOfRelation = Double.parseDouble(relation.get(
									"New"+temp).toString());
						else {
							probOfRelation = findProbOfSlotChainRelation(temp);
						}
						// When b()=T
						dirich.setPseudocount(nodeIndex, j, 0, probOfRelation);
			//			dirichJoint.setPseudocount(nodeIndex, j, 0, probOfRelation);
						// When b()=*
						if(containsFalse){
						dirich.setPseudocount(nodeIndex, j, 1, 0);
			//			dirichJoint.setPseudocount(nodeIndex, j, 1, 0);

						// When B()= F
						dirich.setPseudocount(nodeIndex, j, 2,1 - probOfRelation);
			//			dirichJoint.setPseudocount(nodeIndex, j, 2,1 - probOfRelation);
						}
					} else {
						nameOfNodeEntity = parser.getTAbleofField(node);
						field = node.getName();
						// tableName shows the table from which values should be
						// retrieved
						tableName = new String(nameOfNodeEntity);
						

						// it means all of the RI Indicators are true or star
						// **********************************
						// *******************************
						// */
						if (!isThereFalse) {
							double res3 = 0.0;
							where1 = computeForTrueorStar(BParents, RIIndex,
									parents, parentsValues, tableName);
							tableNameComma[0] = new String("");
							tableName = makeTable(nameOfNodeEntity, BParents,
									RIIndex, parents, parentsValues, tableName, tableNameComma);
//							
//							
//							if(tableName.equals("accountcardclientdisp"))
//								System.out.println("fffffffffffffffffffffffffffffffff");
//							
//							
							int numofCategories = bayePm
									.getNumCategories(dirich.getNode(nodeIndex));
							where1 = checkIndependency(tableName, where1);
							for (int val = 0; val < numofCategories; val++) {
								if(validation != 1){
									
								String column = new String(bayePm.getCategory(
										dirich.getNode(nodeIndex), val));
								where = field + "= '" + column + "'";


								res = database.count(tableName, where + where1);// +
								// database.findJoinCond(tableName));
								// System.out.println("*************************************************");
								// System.out.println(where + where1 +
								// database.findJoinCond(tableName));
								double prob = 0;
								res3 = this.getDenom(tableNameComma[0]);
								prob = res/res3;
								dirich.setPseudocount(nodeIndex, j, val, prob);
				//				dirichJoint.setPseudocount(nodeIndex, j, val, res);

							}
				
							}
						}

						/*
						 * It means that there is at least one false RI
						 */
						else {
							long falseTime =System.currentTimeMillis(); 
							// the boolean and the related code is added to fill in rows where an attribute of a relationship needs to be * and the B() that relation is false
							boolean isSameParentFalse = false;
							if(parents[indexOfFalse]== dirich.getNodeIndex(dirich.getNode("B("+parser.getTAbleofField(node)+")")))
								isSameParentFalse = true;
							int[] newParentValues = new int[parents.length];
							for (int t = 0; t < parents.length; t++)
								newParentValues[t] = parentsValues[t];
							double prob = 0;
							double prob2 = 0;
							double probJoint1 = 0;
							double probJoint2 = 0;
							
							// val is column index; different values of the
							// child node
							for (int val = 0; val < bayePm.getNumCategories(dirich.getNode(nodeIndex)); val++) {
								if(isSameParentFalse){
									if(val == bayePm.getNumCategories(dirich.getNode(nodeIndex)) -1)
									dirich.setPseudocount(nodeIndex, j, val,1);
									else
										dirich.setPseudocount(nodeIndex, j, val,0);
						//				dirichJoint.setPseudocount(nodeIndex, j, val,0);

								}
								else{
								for (int counter = 0; counter < 2; counter++) {
									// one time true and one time *
									newParentValues[indexOfFalse] = counter;
									int row = dirich.getRowIndex(nodeIndex,
											newParentValues);
									// when RI is true
									if (counter == 0){
										prob = dirich.getPseudocount(nodeIndex,
												row, val);
						//			probJoint1 = dirichJoint.getPseudocount(nodeIndex, row, val);
									}
									// when RI is *
									else{
										prob2 = dirich.getPseudocount(
												nodeIndex, row, val);
						//			probJoint2 = dirichJoint.getPseudocount(nodeIndex, row, val);
									}
								}

								double probOfRelation = dirich.getPseudocount(
										parents[indexOfFalse], 0, 0);
								//elwin the main calculation for False
								dirich.setPseudocount(nodeIndex, j, val,
										Math.abs(prob2 - prob ));
						//		dirichJoint.setPseudocount(nodeIndex, j, val, Math.abs(probJoint2 - probJoint1));

							}

						}
							long falseduration =System.currentTimeMillis()- falseTime; 
							System.out.print(falseduration);
							// try {
						//		f.writeLong(falseduration);
						//		f.writeBytes("    ");
						//	} catch (IOException e) {
								// TODO Auto-generated catch block
						//		e.printStackTrace();
						//	}
				
						}
					// ///////////////////////////////////////////////
					}

				}// end of validation
				else{
					// when validation equals -1, there is a contradiction among parents
					for (int val = 0; val < bayePm
					.getNumCategories(dirich.getNode(nodeIndex)); val++) 
					dirich.setPseudocount(nodeIndex, j, val,1.0/bayePm.getNumCategories(dirich.getNode(nodeIndex)));
					
				}
				if(validation ==1)
					dirich.setPseudocount(nodeIndex, j, bayePm.getNumCategories(dirich.getNode(nodeIndex))- 1, 1);
			}
		}// end of B(relation)
		System.out.println("dirichhhhhhhhhhhhhhhhhh"+ dirich.toString());

		return dirich;
	}

	// ////////////////////////////////////////////
	private void findRIParents(int[] parents, ArrayList BParents,ArrayList RIIndex) {
		// finding boolean PArents
		for (int k = 0; k < parents.length; k++) {
			String parentName = dirich.getNode(parents[k]).getName();
			if (parentName.startsWith("B(")) {

				String nameOfBoolean = parentName.substring(2, parentName
						.length() - 1);
				// name of table
				BParents.add(nameOfBoolean);
				// index of RI
				RIIndex.add(Integer.toString(k));
			}
		}// end of for of finding parents' RIs

	}

	// ////////////////////////////////////////////////////////////////////////////////
	private String computeForTrueorStar(ArrayList Bparents,
			ArrayList indexofRI, int[] parents, int[] parentsValues,
			String tableName2) {
		String where1 = new String();
		boolean isRelation = false;
		for (int u = 0; u < parents.length; u++) {
			String parentName = dirich.getNode(parents[u]).getName();
			if (!parentName.startsWith("B(")) {
				String nameOfParentEntity = parser.getTAbleofField(dirich
						.getNode(parents[u]));
				String fieldOfParent = dirich.getNode(parents[u]).getName();
				if (!bayePm.getCategory(dirich.getNode(parents[u]),
						parentsValues[u]).equals("*")) {
					where1 += " and ";
					where1 += fieldOfParent
							+ "= '"
							+ bayePm.getCategory(dirich.getNode(parents[u]),
									parentsValues[u]) + "'";
				}
				// for (int y = 0; y < Bparents.size(); y++) {
				// int ind = Integer.parseInt(indexofRI.get(y).toString());
				// if (nameOfParentEntity.equals(Bparents.get(y))) {
				// isRelation = true;
				// if (!(bayePm.getCategory(dirich.getNode(parents[ind]),
				// parentsValues[ind]).equals("*"))) {
				// if (!tableNameArray.contains(nameOfParentEntity))
				// tableNameArray.add(nameOfParentEntity);
				// }
				// }
				//
				// }
				// // tableName2 = makeTable(Bparents, indexofRI, parents,
				// // parentsValues, tableName2);
				// if (!isRelation) {
				// if (!tableNameArray.contains(nameOfParentEntity))
				// tableNameArray.add(nameOfParentEntity);
				//
				// }
				//
			}
		}

		return where1;
	}

	//////////////////////////////////////////////////////////////////////////

	private String makeTable(String nodeEntity, ArrayList Bparents,
			ArrayList indexofRI, int[] parents, int[] parentsValues,
			String tableName2, String[] tableNameComma) {

		// if the node is from a relationship get all the entities that are
		// relted to it

		ArrayList tableNameArray = new ArrayList();
		tableNameArray.add(tableName2);
		if (relations.contains(nodeEntity)) {
			int index = relations.indexOf(nodeEntity);
			ArrayList refEntities = new ArrayList();
			refEntities = parser.getEntities(index);
			for (int i = 0; i < refEntities.size(); i++) {
				if (!tableNameArray.contains(refEntities.get(i).toString()))
					tableNameArray.add(refEntities.get(i).toString());
			}
		}
	/*	for (int i = 0; i < parents.length; i++) {
			String fieldOfParent = dirich.getNode(parents[i]).getName();
			String parentTable = parser.getTAbleofField(fieldOfParent);
			if (relations.contains(parentTable)
					&& !(bayePm.getCategory(dirich.getNode(parents[i]),
							parentsValues[i]).equals("*"))) {
				if (!tableNameArray.contains(parentTable)) {
					tableNameArray.add(parentTable);
					int index = relations.indexOf(parentTable);
					ArrayList refEntities = new ArrayList();
					refEntities = parser.getEntities(index);
					for (int j = 0; j < refEntities.size(); j++) {
						if (!tableNameArray.contains(refEntities.get(j)
								.toString()))
							tableNameArray.add(refEntities.get(j).toString());
					}
				}
			}
		}*/

		for (int i = 0; i < Bparents.size(); i++) {
			String[] temp = new String[10];
			temp = Bparents.get(i).toString().split(",");
			int kkk = Integer.parseInt(indexofRI.get(i).toString());
			if (!(bayePm.getCategory(dirich.getNode(kkk), parentsValues[kkk]).equals("*"))) {
				for (int j = 0; j < temp.length; j++) {
					if (!tableNameArray.contains(temp[j])) {
						tableNameArray.add(temp[j]);
						int index = relations.indexOf(temp[j]);
						ArrayList refEntities = new ArrayList();
						refEntities = parser.getEntities(index);
						for (int k = 0; k < refEntities.size(); k++) {
							if (!tableNameArray.contains(refEntities.get(k)
									.toString()))
								tableNameArray.add(refEntities.get(k)
										.toString());
						}
					}
				}
			}
			

		}

		Object[] temp = tableNameArray.toArray();
		Arrays.sort(temp);
		tableName2 = new String();
		for (int i = 0; i < temp.length; i++)
			tableName2 += temp[i];
		tableNameComma[0] = (String)temp[0];
		for (int i = 1; i < temp.length; i++)
			tableNameComma[0] += ","+temp[i];
		database.joinForClassification(tableName2, tableNameComma[0]);
		return tableName2;
			}

	// //////////////////////////////////////////////////
	private double findProbOfSlotChainRelation(String tableName) {

		double denominator = 1;
		String[] tables = tableName.split(",");
		ArrayList refEntities = new ArrayList();
		ArrayList table = new ArrayList(Arrays.asList(tables));
		int ind = table.size();
		for (int j = 0; j < ind; j++) {
			ArrayList ent = parser.getEntities(table.get(j).toString());
			for (int k = 0; k < ent.size(); k++) {
				if (!refEntities.contains(ent.get(k))) {
					refEntities.add(ent.get(k));
					table.add(ent.get(k));
					denominator *= database.countStar(ent.get(k).toString());
				}

			}
		}
		double res = database.probOFJoin(table);
		double result = res / denominator;
		relation.put(tableName, result);
		return result;
	}

	// /////////////////////////////////////////////////////////////////
	public void probOfRelations() {
		ArrayList rel = parser.getRelations();
		for (int i = 0; i < rel.size(); i++) {
			double res = 1;
			double count = database.countStar(rel.get(i).toString());
			ArrayList ent = parser.getEntities(i);
			for (int j = 0; j < ent.size(); j++) {
				res *= database.countStar(ent.get(j).toString());
			}
			relation.put(rel.get(i), count / res);
			// dirich.setPseudocount(nodeIndex, j, val, (prob2 - prob *
			// probOfRelation) / (1 - probOfRelation));
		}

	}

	// ////////////////////////////////////////////////////////////////////
	//
	// if the row is valid, this function returns the index of stared vars; else
	// it returns null
	// //////////////////////////////////////////////////////////////////////
	private int validation(ArrayList bParents, ArrayList riIndex,
			int[] parents, int[] parentsValues, ArrayList staredVarIndex, int nodeTable) {
		ArrayList staredRIIndex = new ArrayList();
		String falseRI = null;
		boolean ParentHasSameTable = false;
		for (int i = 0; i < riIndex.size(); i++) {
			int index = Integer.parseInt(riIndex.get(i).toString());
			if (bayePm.getCategory(dirich.getNode(parents[index]),
					parentsValues[index]).equals("*")
					|| (bayePm.getCategory(dirich.getNode(parents[index]),parentsValues[index]).equals("false"))) {
				staredRIIndex.add(riIndex.get(i));

			}
		}

		// checking the attributes which have false or stared parent
		for (int u = 0; u < parents.length; u++) {
			String parentName = dirich.getNode(parents[u]).getName();
			String nameOfParentEntity = parser.getTAbleofField(dirich
					.getNode(parents[u]));
			if (bayePm
					.getCategory(dirich.getNode(parents[u]), parentsValues[u])
					.equals("*"))
				staredVarIndex.add(Integer.toString(u));
			for (int y = 0; y < staredRIIndex.size(); y++) {
				if(parents[Integer.parseInt(staredRIIndex.get(y).toString())]== nodeTable){
					ParentHasSameTable = true;
					continue;
				}
				
				if (bParents.get(Integer.parseInt(staredRIIndex.get(y).toString()))
						.equals(nameOfParentEntity)) {
					if (!bayePm.getCategory(dirich.getNode(parents[u]),
							parentsValues[u]).equals("*"))
						return -1;
					
					

				}

			}
		

		}
		//It retruns 1 when the node and its parent are from the same table, and parent has * or False
		if(ParentHasSameTable)
			return 1;
		return 0;
	}
	///////////////////////////
	
	public DirichletBayesIm copytoNewPM(){
		BayesPm bPm2 = new BayesPm(dag, bayePm);
		ArrayList categories = new ArrayList();
		categories.add("true");
		categories.add("false");
		
		for (int i = 0; i < dag.getNodes().size(); i++) {
			Node node = (dag.getNodes().get(i));
			
		
			if (node.getName().startsWith("B(")) {
			//	bPm2.setNumCategories(node, 2);
				bPm2.setCategories(node, categories);
			}
		}
		DirichletBayesIm Bayesim =  DirichletBayesIm.blankDirichletIm(bPm2);
	//	MlBayesIm Bayesim =  new MlBayesIm(bPm2);
		for (int i = 0; i < bPm2.getDag().getNodes().size(); i++) {
			int nodeIndex = Bayesim.getNodeIndex(dag.getNodes().get(i));
			int[] parents = dirich.getParents(nodeIndex);
			ArrayList BParents = new ArrayList();
			ArrayList RIIndex = new ArrayList();
			findRIParents(parents, BParents, RIIndex);
			
			for (int j = 0; j < Bayesim.getNumRows(nodeIndex); j++) {
				
				int[] parentsValues = Bayesim.getParentValues(nodeIndex, j);
				
				int[] parentsValuesOld = dirich.getParentValues(nodeIndex, j);
				for(int l = 0; l < RIIndex.size(); l++){
					if(parentsValues[l]==1)
						parentsValuesOld[l]= 2;
				}
				int rowIndex = dirich.getRowIndex(nodeIndex, parentsValuesOld);
				
				int numofCategories = bPm2.getNumCategories(dirich.getNode(nodeIndex));
				int numofCategoryofFirst = bayePm.getNumCategories(dirich.getNode(nodeIndex));
				if(numofCategories != numofCategoryofFirst){
					Bayesim.setPseudocount(nodeIndex, j, 0, dirich.getPseudocount(nodeIndex, rowIndex, 0));
					Bayesim.setPseudocount(nodeIndex, j, 1, dirich.getPseudocount(nodeIndex, rowIndex, 2));
				}
				else	
				for(int val =0; val < numofCategories; val++){
					if(Double.isNaN(dirich.getPseudocount(nodeIndex, rowIndex, val)))
						Bayesim.setPseudocount(nodeIndex, j, val,1.0/ numofCategories);
					else
						Bayesim.setPseudocount(nodeIndex, j, val, dirich.getPseudocount(nodeIndex, rowIndex, val));
				}
			}
		}
		
		return Bayesim;
	}
/////////////////////////////////////////////////////////////////////////////
	public int NUMOFRelationRow(String name){
		return database.countStar(name);
	}

	// /////////////////////////////////////////////////////////////
	 public static double getConditionalProbability(int nodeIndex, int rowIndex, int colIndex, double[][][] psedo, int num) {
		 double res = 0;
		 for(int i = 0; i < num; i++){
			// System.out.println(psedo[nodeIndex][rowIndex][ i]);
			 res+= psedo[nodeIndex][rowIndex][ i];

		 }
		 if(psedo[nodeIndex][rowIndex][ colIndex]== 0)
			 return 0;
		 return psedo[nodeIndex][rowIndex][colIndex]/res;
	 }
	// /////////////////////////////////////////////////////////////
	 public double getDenom(String tableName){
		 double size = 1;
		 String[] tables = tableName.split(",");
		 for(int i = 0; i < tables.length; i++){
			 if(!relations.contains(tables[i]))
				 size *= database.countStar(tables[i]);
		 }
		 return size;
	 }
	
	///////////////////////////////////////////////////////
	public static void main(String[] args) {
		// jointProbLearner c = new jointProbLearner();
		// c.preprocess()
		// c.probOfRelations();
		// c.computeCPT();

		// System.out.println(c.findJoinCond("registration,registration"));

	}

}
