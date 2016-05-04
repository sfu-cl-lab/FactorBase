package ca.sfu.jbn.structureLearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ca.sfu.jbn.common.ChangeGraph;
import ca.sfu.jbn.common.CommonClasses;
import ca.sfu.jbn.common.GetDataset;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;

public class CopyOfS_learning {
	private Parser parser = new Parser();
	private int slotChain = 0;
	private GesSearch gesSearch;
	private db db = new db();
	private ArrayList keys = new ArrayList(); // used in the third phase and
	// initialized in the second phase
	private ArrayList entities = parser.getEntities();
	private ArrayList entity_att = parser.getEntity_att();
	private ArrayList relation_att = parser.getRelation_att();
	private ArrayList values = new ArrayList();
	private ArrayList relations = parser.getRelations();
	private Knowledge addedknowledge = new Knowledge();
	private ChangeGraph cd;
	public EdgeListGraph graph = new EdgeListGraph();
	public CommonClasses cc = new CommonClasses();

	public CopyOfS_learning(int numOfSlotChain) {
		slotChain = numOfSlotChain;
	}

	public BayesPm major() {
		// To determine the knowledge holder
		ArrayList<String> containerofKnow = new ArrayList<String>();
		// contains knowledge of each entity
		ArrayList know = new ArrayList<Knowledge>();
	
		firstPhase(know, containerofKnow);
		//System.out.println("firstphase done");

		secondPhase(know, containerofKnow);
		//System.out.println("secondphase done");

		 thirdPhase(know, containerofKnow);
		 //System.out.println("thirdphase done");

		graph = makeGraph(know, containerofKnow);
		GraphUtils.arrangeInCircle(graph, 250, 250, 200);
		// System.out.println(graph.getEdges().toString());
		// System.out.println("finish");
		return makepm();
	}

	private void thirdPhase(ArrayList know, ArrayList<String> containerofKnow) {
		RectangularDataSet dataset;
		for (int i = 0; i < relations.size(); i++) {
			String fileName = relations.get(i).toString();
			int index = relations.indexOf(fileName);
			ArrayList refEntities = new ArrayList();
			refEntities = parser.getEntities(index);
			for (int j= i+1; j < relations.size(); j++) {
				String fileName1 = relations.get(j).toString();
				int index1 = relations.indexOf(fileName1);
				ArrayList refEntities1 = new ArrayList();
				refEntities1 = parser.getEntities(index1);
				List list = cc.intersect(refEntities, refEntities1);
				if (list.size()>0){ 
				String fName = fileName + ","+ fileName1; 
				String tableName = db.joinNatural1(fName, list);
				}
			}
		}
		
	/*	for (int k = 1; k <= slotChain; k++) {
			int size = keys.size();
			for (int ind = 0; ind < size; ind++) {
				for (int r = ind + 1; r < size; r++) {
					// if res is null intersect is empty else the union is given
					// as output
					ArrayList res = null;
					if (!(keys.get(ind).toString().contains(
							keys.get(r).toString()) || keys.get(r).toString()
							.contains(keys.get(ind).toString())))
						res = cc.intersect((ArrayList) values.get(ind),
								(ArrayList) values.get(r));
					if (res != null) {
						if (!keys.contains(keys.get(ind) + "," + keys.get(r))) {
							values.add(res);
							String fileName = keys.get(ind) + "," + keys.get(r);
							keys.add(fileName);
							dataset = JoinRemovePrimaryRelation(fileName, res);
							// do the search
							int index = containerofKnow.indexOf(keys.get(ind)
									.toString());
							int index2 = containerofKnow.indexOf(keys.get(r)
									.toString());
							Knowledge temp = mergeKnowledge((Knowledge) know
									.get(index2), (Knowledge) know.get(index));
							containerofKnow.add(fileName);
							dataset = JoinRemovePrimaryRelation(fileName, res);
							know.add(objectRelationSearch(dataset, temp,
									fileName));
						}
					}
				}
			}
		}*/
	}

	// //////////////////////////////////////
	// Makes the graph from knowledge
	private EdgeListGraph makeGraph(ArrayList know,
			ArrayList<String> containerofKnow) {

		Knowledge mergeknow = new Knowledge();
		for (int i = 0; i < keys.size(); i++) {
			String temp = (String) keys.get(i);
			int index = containerofKnow.indexOf(temp);
			Knowledge know2 = (Knowledge) know.get(index);
			mergeknow = mergeKnowledge(mergeknow, know2);
		}
		if (keys.size() == 0) {
			for (int i = 0; i < know.size(); i++) {
				mergeknow = mergeKnowledge(mergeknow, (Knowledge) know.get(i));
			}
		}
		containerofKnow.add("mergeknow");
		know.add(mergeknow);
		ArrayList nodes = new ArrayList();
		for (Iterator<KnowledgeEdge> i = mergeknow.requiredEdgesIterator(); i
				.hasNext();) {
			KnowledgeEdge pair = i.next();
			String from = pair.getFrom();
			String to = pair.getTo();
			GraphNode node1;
			GraphNode node2;
			if (!nodes.contains(from)) {
				node1 = new GraphNode(from);
				graph.addNode(node1);
				nodes.add(from);

			} else
				node1 = (GraphNode) graph.getNode(from);

			if (!nodes.contains(to)) {
				node2 = new GraphNode(to);
				graph.addNode(node2);
				nodes.add(to);

			} else
				node2 = (GraphNode) graph.getNode(to);

			graph.addDirectedEdge(node1, node2);

		}
		mergeknow = mergeKnowledge(mergeknow, addedknowledge);
		// to add the RI as the final nodes
		for (Iterator<KnowledgeEdge> i = addedknowledge.requiredEdgesIterator(); i
				.hasNext();) {
			KnowledgeEdge pair = i.next();
			String from = pair.getFrom();
			String to = pair.getTo();
			GraphNode node1;
			GraphNode node2;
			if (!nodes.contains(from)) {
				node1 = new GraphNode(from);
				graph.addNode(node1);
				nodes.add(from);

			} else
				node1 = (GraphNode) graph.getNode(from);

			if (!nodes.contains(to)) {
				node2 = new GraphNode(to);
				graph.addNode(node2);
				nodes.add(to);

			} else
				node2 = (GraphNode) graph.getNode(to);

			graph.addDirectedEdge(node1, node2);

		}
		ChangeGraph cd = new ChangeGraph(graph);
		graph = cd.orderNode();
		// Pattern g = new Pattern(graph);
		// PatternToDagSearch a = new PatternToDagSearch(g);
		// Dag graph1 = a.patternToDagMeek();
		// graph.transferNodesAndEdges(graph1);

		return graph;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	private RectangularDataSet JoinRemovePrimaryRelation(String fileName,
			List refEntities) {
		// ArrayList result = db.joinNatural(fileName, refEntities);
		String tableName = db.joinNatural1(fileName, refEntities);
		RectangularDataSet dataset = null;

		// DataParser DParser = new DataParser();
		ArrayList id = new ArrayList();
		try {
			GetDataset g = new GetDataset();// .getInstance();
			dataset = GetDataset.getInstance().getData(tableName);
			// dataset = g.GetData(tableName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int y = 0; y < refEntities.size(); y++) {
			int index = parser.getEntityIndex(refEntities.get(y).toString());
			
			//System.out.println("index of " +refEntities.get(y).toString()+" is " + index);
			if (index<0) continue;
			
			ArrayList res = parser.getEntityId(index);
			for (int k = 0; k < res.size(); k++)
				id.add(res.get(k));
		}
		dataset = removeprimaryentity(dataset, id);
		return dataset;

		// return null;

	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// This function produces knowledge based on entities
	private void firstPhase(ArrayList know, ArrayList<String> containerofKnow) {
		RectangularDataSet dataset;
		DataParser DParser = new DataParser();
		for (int i = 0; i < entities.size(); i++) {
			String fileName = entities.get(i).toString();
		
			
			try {
				
				dataset = GetDataset.getInstance().getData(fileName);
				containerofKnow.add(fileName);
				int index = entities.indexOf(fileName);
				dataset = removeprimaryentity(dataset, parser.getEntityId(index));
				know.add(objectSearch(dataset));
				//System.out.println(fileName);
			} catch (Exception e) {
				System.out.println("Problem with dataset in first phase");
				e.printStackTrace();
			}
			
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// This function produces knowledge based on relationships in the database
	private void secondPhase(ArrayList know, ArrayList<String> containerofKnow) {
		RectangularDataSet dataset;
		DataParser DParser = new DataParser();
		for (int i = 0; i < relations.size(); i++) {
			String fileName = relations.get(i).toString();
			int index = relations.indexOf(fileName);
			ArrayList refEntities = new ArrayList();
			refEntities = parser.getEntities(index);
			dataset = JoinRemovePrimaryRelation(fileName, refEntities);
			Knowledge temp = new Knowledge();
			for (int y = 0; y < refEntities.size(); y++) {
				int ind = containerofKnow
						.indexOf(refEntities.get(y).toString());
				
				//elwin
				System.out.println(refEntities.get(y).toString());
				
				temp = mergeKnowledge(temp, (Knowledge) know.get(ind));
			}
			containerofKnow.add(fileName);
			keys.add(fileName);
			values.add(refEntities);
			know.add(objectRelationSearch(dataset, temp, fileName));
			// }
		}
	}

	// //////////////////////////////////////
	private Knowledge objectSearch(RectangularDataSet dataSet) {
		Knowledge know = new Knowledge();
		gesSearch = new GesSearch(dataSet);
		gesSearch.setStructurePrior(1.0000);
		gesSearch.setSamplePrior(10.0000);
		Graph graph2 = gesSearch.search();
		gesSearch = new GesSearch(dataSet, graph2);
		Pattern g = new Pattern(graph2);
		PatternToDagSearch a = new PatternToDagSearch(g);
		Dag graph = a.patternToDagMeek();
		List edges = graph.getEdges();
		for (int i = 0; i < graph.getNumNodes(); i++) {
			Node node1 = graph.getNodes().get(i);
			for (int k = 0; k < graph.getNumNodes(); k++) {
				Node node2 = graph.getNodes().get(k);
				if (i != k) {
					if (graph.isDirectedFromTo(node1, node2)) {
						know.setEdgeRequired(node1.getName(), node2.getName(),
								true);
					} else
						know.setEdgeForbidden(node1.getName(), node2.getName(),
								true);
				}
			}
		}
		return know;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	private Knowledge objectRelationSearch(RectangularDataSet dataset,
			Knowledge know1, String RelationName) {
		String tableOfNode1;
		String tableOfNode2;
		Knowledge know = new Knowledge();
		gesSearch = new GesSearch(dataset);
		gesSearch.setKnowledge(know1);
		gesSearch.setStructurePrior(1.0000);
		gesSearch.setSamplePrior(10.0000);
		Graph graph2 = gesSearch.search();
		Pattern g = new Pattern(graph2);
		PatternToDagSearch a = new PatternToDagSearch(g);
		Dag graph = a.patternToDagMeek();
		List edges = graph.getEdges();
		for (int i = 0; i < graph.getNumNodes(); i++) {
			Node node1 = graph.getNodes().get(i);
			tableOfNode1 = parser.getTAbleofField(node1);
			//if (relations.contains(tableOfNode1))
				//addedknowledge.setEdgeRequired("B(" + tableOfNode1 + ")", node1.getName(), true);
			for (int k = 0; k < graph.getNumNodes(); k++) {
				Node node2 = graph.getNodes().get(k);
				if (i != k) {
					if (graph.isDirectedFromTo(node1, node2)) {
						know.setEdgeRequired(node1.getName(), node2.getName(),
								true);
						tableOfNode2 = parser.getTAbleofField(node2);
						if (isExtraEdgeRequired(tableOfNode1, tableOfNode2)
								&& (!know1.edgeRequired(node1.getName(), node2
										.getName()))) {
							addedknowledge.setEdgeRequired("B(" + RelationName
									+ ")", node2.getName(), true);
						}

					} else
						know.setEdgeForbidden(node1.getName(), node2.getName(),
								true);
				}
			}
		}
		return know;
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// Adding edges that are attached to B(R)
	private boolean isExtraEdgeRequired(String Entity1, String Entity2) {
		if (Entity1.equals(Entity2))
			return false;
		if (entities.contains(Entity1) && entities.contains(Entity2))
			return true;

		if (relations.contains(Entity1)) {
			if (relations.contains(Entity2)) {
				return true;
			}
			// it shows that the second one is not a relation
			else {
				int index = relations.indexOf(Entity1);
				ArrayList refEntities = new ArrayList();
				refEntities = parser.getEntities(index);
				if (refEntities.contains(Entity2))
					return true;
			}
		}
		// It shows that the first one is an entity
		else {
			int index = relations.indexOf(Entity2);
			ArrayList refEntities = new ArrayList();
			refEntities = parser.getEntities(index);
			if (refEntities.contains(Entity1))
				return false;

		}
		return true;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// this method add know2 to know1

	private Knowledge mergeKnowledge(Knowledge know1, Knowledge know2) {
		// for (Iterator<KnowledgeEdge> i = know2.forbiddenEdgesIterator();
		// i.hasNext();) {
		// KnowledgeEdge pair = i.next();
		// String from = pair.getFrom();
		// String to = pair.getTo();
		// know1.setEdgeForbidden(from, to, true);
		// }
		for (Iterator<KnowledgeEdge> i = know2.requiredEdgesIterator(); i
				.hasNext();) {
			KnowledgeEdge pair = i.next();
			String from = pair.getFrom();
			String to = pair.getTo();
			know1.setEdgeRequired(from, to, true);

		}
		return know1;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Delete columns regarding ids
	private RectangularDataSet removeprimaryentity(RectangularDataSet dataset,
			ArrayList refEntities) {

		for (int i = 0; i < dataset.getNumColumns(); i++) {
			String columnName = dataset.getVariable(i).getName();
			for (int j = 0; j < refEntities.size(); j++) {
				if (refEntities.get(j).toString().equals(columnName)) {
					dataset.removeColumn(dataset.getVariable(i));
					// Because one column is removed we need to reduce i to
					// cover all columns
					i--;
				}
			}

		}

		return dataset;

	}

	// ////////////////////////////////////////////////////////////////
	public File printToFile(ArrayList res, String fileName) {
		Writer output = null;
		File file = new File(fileName);
		String result = new String();
		try {
			output = new BufferedWriter(new FileWriter(file));
			for (int y = 0; y < res.size(); y++) {
				if (res.get(y).equals("\r\n"))
					result = result.substring(0, result.length() - 1) + "\r\n";
				else
					result += res.get(y) + ",";
			}
			output.write(result);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;

	}

	// /////////////////////////////////////////////////////
	private BayesPm makepm() {
		Dag dag = new Dag(graph);
		BayesPm bayePm = new BayesPm(dag, 3, 3);

		int indexOfNode;
		Node node;
		int numOfValue;
		ArrayList categories = new ArrayList();

		// making bayesPm
		for (int i = 0; i < dag.getNodes().size(); i++) {
			node = (dag.getNodes().get(i));
			String nameOfNodeEntity = null;
			String field = null;
			numOfValue = 0;
			if (!node.getName().startsWith("B(")) {

				categories = ca.sfu.jbn.common.db.getInstance().Values(
						parser.getTAbleofField(node), node.getName());
				Collections.sort(categories);

				nameOfNodeEntity = parser.getTAbleofField(node);
				field = node.getName();
				numOfValue = db.count2(nameOfNodeEntity, field);
				// categories = db.Values(nameOfNodeEntity, field);
				if (relations.contains(nameOfNodeEntity)) {
					numOfValue++;
					// add * value to the categories
					categories.add(new String("*"));
				}
			} else {
				numOfValue = 3;
				categories = new ArrayList();
				categories.add("true");
				// * shows unspecified var
				categories.add("*");
				categories.add("false");
			}

			bayePm.setNumCategories(node, numOfValue);
			bayePm.setCategories(node, categories);

		}
		
		return bayePm;
	}

	// //////////////////////////////////////////////////

	public RectangularDataSet RemoveMissingValue(RectangularDataSet dataModel) {
		RectangularDataSet dataSet = dataModel;
		List<Node> variables = new LinkedList<Node>();

		for (int j = 0; j < dataSet.getNumColumns(); j++) {
			variables.add(dataSet.getVariable(j));
		}

		RectangularDataSet newDataSet = new ColtDataSet(0, variables);

		int newRow = -1;

		ROWS: for (int i = 0; i < dataSet.getNumRows(); i++) {
			for (int j = 0; j < dataSet.getNumColumns(); j++) {
				Node variable = dataSet.getVariable(j);

				if (((Variable) variable).isMissingValue(dataSet
						.getObject(i, j))) {
					continue ROWS;
				}
			}

			newRow++;

			for (int j = 0; j < dataSet.getNumColumns(); j++) {
				newDataSet.setObject(newRow, j, dataSet.getObject(i, j));
			}
		}
		return newDataSet;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println(start);
		CopyOfS_learning a = new CopyOfS_learning(2);
		a.major();
		System.out.println(System.currentTimeMillis() - start);

	}
}
