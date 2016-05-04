package ca.sfu.jbn.common;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class ChangeGraph {
	
	public EdgeListGraph graph;
	
	public Parser parser = new Parser();
	/////////////////////////////////////
	public ChangeGraph(EdgeListGraph g){
		graph = g;
		
	}
	/////////////////////////////////////////
	public EdgeListGraph ChangaeGraph(){
	Dag dag = new Dag();
    for (int i = 0; i < graph.getNodes().size(); i++) {
        Node node = graph.getNodes().get(i);
        if (!node.getName().startsWith("B(")) {
            int indexOfNode = (node.getName()).indexOf('.');
            System.out.println("ww       "+node.getName());
            String nameOfNodeEntity = (node.getName()).substring(0, indexOfNode);
            if (graph.getParents(node) != null) {
                for (int j = 0; j < (graph.getParents(node)).size(); j++) {
                    if (!graph.getParents(node).get(j).getName().startsWith("B(")) {
                        int indexOfParent = (graph.getParents(node).get(j).getName()).indexOf('.');
                        String nameOfParentEntity = (graph.getParents(node).get(j).getName()).substring(0, indexOfParent);
                        if ((!nameOfParentEntity.equals(nameOfNodeEntity)) && isRelation(nameOfParentEntity)) {
                            GraphNode bool = new GraphNode("B(" + nameOfParentEntity + ')');
                            System.out.println("bool    "+ bool.getName());
                            graph.addNode(bool);
                            //graph.addNode(bool);
                            /*
                            for putting RI nodes in the head of list of parents of each node
                            */

                            Edge a = Edges.directedEdge(bool, node);
                            graph.addDirectedEdge(bool, node);

                        }
                    }
                }
            }
        }
    }
    return orderNode();
     
}
	//////////////////////////////////
	
public EdgeListGraph orderNode(){
	List nodes = graph.getNodes();
    graph.clear();
    for (int i1 = nodes.size() - 1; i1 >= 0; i1--)
         graph.addNode((Node) nodes.get(i1));
   return graph;
		
	}
//////////////////////////////////////////////////////////
    // determine whether the table is an entity or a relation
    // return True if the table is a relation
	

    public boolean isRelation(String nameOfParentEntity) {

        ArrayList a = parser.getRelations();
        if (a.contains(nameOfParentEntity)) {
            return true;
        } else
            return false;  //To change body of created methods use File | Settings | File Templates.
    }

    //////////////////////////////////// ,//////////////////////////////////////
}
