package ca.sfu.jbn.common;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Node;

public class makeBayesPm {
	private static db db = new db();
	private static Parser parser = new Parser();
	private static ArrayList relations; 
	public static boolean doesContainFalse  = false;
	
	public static void changeParseFile(String fileName){
		parser.setParseFile(fileName);
		
	}
	public static BayesPm makepm(EdgeListGraph graph)
	{

		List<Node> nodes =graph.getNodes();
	    graph.clear();
	    for (int i1 = nodes.size() - 1; i1 >= 0; i1--)
	         graph.addNode(nodes.get(i1));
	    
		relations = parser.getRelations();
		db = new db();
		Dag dag = new Dag(graph);
		BayesPm bayePm = new BayesPm(dag, 20, 20);
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
				// each node's name is in the form of (tableName.nameOfField)
				// indexOfNode = (node.getName()).indexOf('.');
				// nameOfNodeEntity = (node.getName()).substring(0,
				// indexOfNode);
				nameOfNodeEntity = parser.getTAbleofField(node);
				field = node.getName();
				numOfValue = db.count2(nameOfNodeEntity, field);

				categories = db.Values(nameOfNodeEntity, field);
				if(doesContainFalse)
					if (relations.contains(nameOfNodeEntity)) {
						numOfValue++;
					// add * value to the categories
					categories.add(new String("*"));
				}
			} else {
				if(doesContainFalse)
					numOfValue = 3;
				else
					numOfValue = 3;
				categories = new ArrayList();
				categories.add("true");
				categories.add("*");
				categories.add("false");
				// * shows unspecified var
//				if(doesContainFalse){
//					categories.add("*");
//					categories.add("false");
//				}
			}
		//	System.out.println(node);
		//	System.out.println(numOfValue);
			bayePm.setNumCategories(node, numOfValue);
			
			bayePm.setCategories(node, categories);

		}
		return bayePm;
	}

	

}