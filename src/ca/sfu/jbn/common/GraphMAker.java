package ca.sfu.jbn.common;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

public class GraphMAker {
	private EdgeListGraph graph = new EdgeListGraph();
	
	
	public GraphMAker(String fileName){
		if(fileName== "")
			return;
		try {
			FileInputStream f = new FileInputStream(fileName);
			
			this.parseFile(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/////////////////////////////
	private void parseFile(FileInputStream f) {
		DataInputStream in = 
            new DataInputStream(f);
		try {
			while (in.available() !=0)
			{
				 String temp = in.readLine();
								 // Checking that temp is not  null
				 if(temp.length()> 2){
					// temp = temp.substring(temp.indexOf(". ")+ 2);
					 String node1 = temp.substring(0, temp.indexOf("-") - 1);
					// int length = ;
					// if(length ==  temp.length() -1)
						// length = temp.length() -1 ;
					 
					 String node2 = temp.substring(temp.indexOf(">")+ 2,temp.length());
					// System.out.println(node1);
					 //System.out.println(node2);
					 
				     Node nodE1 = graph.getNode(node1);
				     if(nodE1== null){
				    	 nodE1 =new GraphNode(node1);
						 graph.addNode(nodE1 );
				     }
					 
				     Node nodE2 = graph.getNode(node2);
				     if(nodE2== null){
				    	 nodE2 =new GraphNode(node2);
						 graph.addNode(nodE2 );
					}
				     if(nodE1.getName() != nodE2.getName())
					 graph.addDirectedEdge(nodE1, nodE2);
				 
				 }
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//////////////////////////////////////////////	
	public EdgeListGraph getGraph(){
		return graph;
	}
	
	//////////////////////////////////
	public static void main(String[] args){
		GraphMAker a = new GraphMAker("C:/Documents and Settings/hkhosrav/Desktop/graphCora.txt");
		
	}
	
}
