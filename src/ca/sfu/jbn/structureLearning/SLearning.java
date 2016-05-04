package ca.sfu.jbn.structureLearning;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ca.sfu.jbn.common.*;

import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;

public class SLearning {
	
	private Parser parser = new Parser();
	private int slotChain = 0;
	private GesSearch gesSearch;
	private db db = new db();
	private ArrayList keys = new ArrayList();
	private ArrayList values= new ArrayList();
	private ArrayList entities = parser.getEntities();
	private ArrayList relations = parser.getRelations();
	private Knowledge addedknowledge;
	public EdgeListGraph graph = new EdgeListGraph();
	//instructorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr
	
	public SLearning(int numOfSlotChain) {
		slotChain = numOfSlotChain;
	}

	public void PreProcess(){
		RectangularDataSet dataset;
				
		ArrayList containerofKnow = new ArrayList<String>(); // To determine the knowledge holder
		ArrayList know = new ArrayList<Knowledge>(); //  contains knowledge of each entity
		DataParser DParser = new DataParser();
		 File folder = new File("dataset/MovieLens"); // insert name of folder
	     File[] listOfFiles = folder.listFiles();
		     //This for goes through all the entities and searches them
			    for (int i = 0; i < listOfFiles.length; i++) {
			    	 
			    	String fileName =listOfFiles[i].toString().substring(listOfFiles[i].toString().lastIndexOf("\\") + 1, listOfFiles[i].toString().indexOf("."));
			    	 try { 
			    		 if(entities.contains(fileName)){
			    			 
			    			 DParser.setDelimiter(DelimiterType.COMMA);
			    			 dataset = DParser.parseTabular(listOfFiles[i]);
			    			 containerofKnow.add(fileName);
			    			 int index = entities.indexOf(fileName);
			    			 dataset = removeprimaryentity(dataset, parser.getEntityId(index));
    			   		     know.add(objectSearch(dataset));
			    		    
			    		    			    		    			    		 } 
			  		}catch (IOException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		}
			    	  }
			    //This for Goes through all relations and searches them
			    for (int i = 0; i < listOfFiles.length; i++) {
			    	String fileName =listOfFiles[i].toString().substring(listOfFiles[i].toString().lastIndexOf("\\") + 1, listOfFiles[i].toString().indexOf("."));
			    	 try {
			    		 if(relations.contains(fileName)){
			    			 DParser.setDelimiter(DelimiterType.COMMA);
			    			 dataset = DParser.parseTabular(listOfFiles[i]);
			    			 int index = relations.indexOf(fileName);
			    			 ArrayList refEntities = new ArrayList();
			    			 refEntities = parser.getEntities(index);
			    			
			    			 dataset = JoinRemovePrimaryRelation(fileName, refEntities);
			    			 Knowledge temp = new Knowledge();
			    			 for(int y = 0; y < refEntities.size(); y++){
			    				 int ind =containerofKnow.indexOf(refEntities.get(y).toString());
			    					 temp = mergeKnowledge(temp, (Knowledge)know.get(ind));
			    			 }  			 
			    		     containerofKnow.add(fileName);
			    		   //  HashSet set = new HashSet();
			    		    // for(int j = 0; j < refEntities.size(); j++)
			    		     //   set.add(refEntities.get(j));
			    		    // map.put(fileName, set);
			    		     keys.add(fileName);
			    		     values.add(refEntities);
			    			
			    			 know.add( objectRelationSearch(dataset, temp, fileName));

			    		 } 
			    		 
			    		
			  		}catch (IOException e) {
			  			// TODO Auto-generated catch block
 			  			e.printStackTrace();
			  		}
			  		}
			    // check the set 
	    		 for(int k = 1; k <= slotChain; k++){
	    			 int size = keys.size();
	    			 for(int ind = 0; ind < size; ind++){
	    				 for(int r = ind + 1; r < size; r++ ){
	    					 ArrayList res = intersect((ArrayList)values.get(ind), (ArrayList)values.get(r));
	    					 if(res != null ){
	    						 if(!keys.contains(keys.get(ind)+","+keys.get(r))){
	    							 values.add(res);
	    							 String fileName = keys.get(ind)+","+keys.get(r);
	    							 keys.add(fileName);
	    							 // do the search
				    				  int index =containerofKnow.indexOf(keys.get(ind).toString());
				    				  int index2 =containerofKnow.indexOf(keys.get(r).toString());

	    							     Knowledge temp = mergeKnowledge((Knowledge)know.get(index2), (Knowledge)know.get(index));
	    								 containerofKnow.add(fileName);
	    								 dataset = JoinRemovePrimaryRelation(fileName, res);;
	    								  know.add(objectRelationSearch(dataset, temp, fileName ));
	    							 }
	    						 }
	    					 }
	    				 }
	    			 }
	    		 makeGraph((Knowledge)know.get(know.size() - 1));
	    		 }
	    		

////////////////////////////////////////			
private void makeGraph(Knowledge know2) {
	Knowledge know  = mergeKnowledge(know2, addedknowledge);
	ArrayList nodes = new ArrayList();
	for (Iterator<KnowledgeEdge> i = know.requiredEdgesIterator(); i.hasNext();) {
		 KnowledgeEdge pair = i.next();
		 String from = pair.getFrom();
		 String to = pair.getTo();
		 GraphNode node1;
		 GraphNode node2;
		 if(!nodes.contains(from)){
			 node1 = new GraphNode(from);
			 graph.addNode(node1);
			 nodes.add(from);

		 }
		 else
			 node1 = (GraphNode)graph.getNode(from);

		 if(!nodes.contains(to)){
			 node2 = new GraphNode(to);
			 graph.addNode(node2);
			 nodes.add(to);

		 }
		 else
			 node2 = (GraphNode)graph.getNode(to);
		 
		 graph.addDirectedEdge(node1, node2);
		 
	 }
	System.out.println("done");
	}



///////////////////////////////////////////////////////////////////////////////////////////////////////	
	private ArrayList intersect(ArrayList a1, ArrayList a2) {
		boolean haveIntersect = false;
		ArrayList res = new ArrayList();
		 for(int i=0; i < a1.size();i++){
			 String str1 = (String) a1.get(i);
			 if(!res.contains(a1.get(i)))
				 res.add(a1.get(i));
			 for(int j=0;j < a2.size();j++){
			 String str2 = (String) a2.get(j);
			 int result = str1.compareTo(str2);
			 if(!res.contains(a2.get(j)))
				 res.add(a2.get(j));

			 if (result > 0)
				 haveIntersect = true;
			 }
		}
		if(haveIntersect){
			//so we do not return (ra,registration,ra)
			if(res.size() > a1.size() && res.size()> a2.size()) 
				return res;
			
			
		}
		
			return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	private RectangularDataSet JoinRemovePrimaryRelation(String fileName, ArrayList refEntities) 	{
		
		ArrayList result = db.joinTabel(fileName, refEntities, 1);
		DataParser DParser = new DataParser();
		ArrayList id = new ArrayList();
		RectangularDataSet dataset;
		try {
			File f = printToFile(result, "write.txt");
			DParser.setDelimiter(DelimiterType.COMMA);
			dataset = DParser.parseTabular(f);
			for(int y = 0; y < refEntities.size(); y ++){
				int index = parser.getEntityIndex(refEntities.get(y).toString());
				ArrayList res = parser.getEntityId(index);
				for(int k = 0; k < res.size(); k++)
					id.add(res.get(k));
			}
			dataset = removeprimaryentity(dataset,id);
			return dataset;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

/////////////////////////////////////////////////////////////////
	private RectangularDataSet removeprimaryentity(RectangularDataSet dataset,ArrayList refEntities) {
		
		for(int i = 0; i < dataset.getNumColumns(); i++){
			String columnName = dataset.getVariable(i).getName();
			for(int j= 0; j < refEntities.size(); j++){
				if(refEntities.get(j).toString().equals(columnName)){
					dataset.removeColumn(dataset.getVariable(i));
					// Because one column is removed we need to reduce i to cover all columns
					i--;
				}
			}
			
		}
		
		return dataset;
		
	
	}
	///////////////////////////////////////////////////////////////////////////////////////////
	private Knowledge objectRelationSearch(RectangularDataSet dataset, 	Knowledge know1, String RelationName) {
		gesSearch = new GesSearch(dataset);
		Knowledge know= new Knowledge();
		
		gesSearch.setKnowledge(know1);
		gesSearch.setStructurePrior(1.0000);
		gesSearch.setSamplePrior(10.0000);
		Graph graph2 =gesSearch.search();
		
		Pattern g = new Pattern(graph2);
		PatternToDagSearch a = new PatternToDagSearch(g);
		Dag graph = a.patternToDagMeek();
		List edges = graph.getEdges();
		//System.out.println(edges.get(2));
		for(int i =0; i < graph.getNumNodes(); i++){
			Node node1 = graph.getNodes().get(i);
			for(int k = 0; k < graph.getNumNodes(); k++ ){
				Node node2 = graph.getNodes().get(k);
				if(i != k)
				{
					//System.out.println(node1.getName());
					if(graph.isDirectedFromTo(node1, node2)){
						know.setEdgeRequired(node1.getName(), node2.getName(), true);
						if(isExtraEdgeRequired(node1.getName(), node2.getName())){
							 addedknowledge.setEdgeRequired(RelationName, node2.getName(), true);
						}
						//if two nodes are from differnt entities add what needs to be added
						// B(relation)
					}
					else
						know.setEdgeForbidden(node1.getName(), node2.getName(), true);
						
				}
			

			}
		}

		
		return know;
		// TODO Auto-generated method stub
		
	}
	

//////////////////////////////////////////////////////////////////////////////////
	private boolean isExtraEdgeRequired(String name1, String name2) {
		
		int indexOfNode = name1.indexOf('.');
        String Entity1 = name1.substring(0, indexOfNode);
        indexOfNode = name2.indexOf('.');
        String Entity2 = name2.substring(0, indexOfNode);
        if(Entity1.equals(Entity2))
        	return false;
        
        if(relations.contains(Entity1)){
        	if (relations.contains(Entity2)){
        		return true;
        	}
        	// it shows that the second one is not a relation
        	else{
        		 int index = relations.indexOf(Entity1);
    			 ArrayList refEntities = new ArrayList();
    			 refEntities = parser.getEntities(index);
        		 if(refEntities.contains(Entity2))
        			 return false;
        	}
        }
        // It shows that the first one is an entity
        else{
        	int index = relations.indexOf(Entity2);
			 ArrayList refEntities = new ArrayList();
			 refEntities = parser.getEntities(index);
			 if(refEntities.contains(Entity1))
    			 return false;
        	
        }
        	
   
		return true;
	}



	///////////////////////////////////////// 
	private Knowledge objectSearch(RectangularDataSet dataSet){
		gesSearch = new GesSearch(dataSet);
		Knowledge know = new Knowledge();
		gesSearch.setKnowledge(know);
		gesSearch.setStructurePrior(1.0000);
		gesSearch.setSamplePrior(10.0000);
		
		Graph graph2 =gesSearch.search();
		Pattern g = new Pattern(graph2);
		PatternToDagSearch a = new PatternToDagSearch(g);
		Dag graph = a.patternToDagMeek();
		List edges = graph.getEdges();
		//System.out.println(edges.get(2));
		for(int i =0; i < graph.getNumNodes(); i++){
			Node node1 = graph.getNodes().get(i);
			for(int k = 0; k < graph.getNumNodes(); k++ ){
				Node node2 = graph.getNodes().get(k);
				if(i != k)
				{
					//System.out.println(node1.getName());
					if(graph.isDirectedFromTo(node1, node2)){
						know.setEdgeRequired(node1.getName(), node2.getName(), true);
					}
					else
						know.setEdgeForbidden(node1.getName(), node2.getName(), true);
						
				}

			}
		}
	//System.out.println(know.toString());
	return know;
	}
//////////////////////////////////////////////////////////////////

	
/////////////////////////////////////////////////////////////////////////
//this method add know2 to know1
/////////////////////////////////////////////////////////
 private Knowledge mergeKnowledge(Knowledge know1, Knowledge know2){
	 for (Iterator<KnowledgeEdge> i = know2.forbiddenEdgesIterator(); i.hasNext();) {
		 KnowledgeEdge pair = i.next();
		 String from = pair.getFrom();
		 String to = pair.getTo();
		 know1.setEdgeForbidden(from, to, true);
	 }
	 for (Iterator<KnowledgeEdge> i = know2.requiredEdgesIterator(); i.hasNext();) {
		 KnowledgeEdge pair = i.next();
		 String from = pair.getFrom();
		 String to = pair.getTo();
		 know1.setEdgeRequired(from, to, true);

	 }
    return know1;
 }
//////////////////////////////////////////////////////////////////
 public File printToFile(ArrayList res, String fileName){
	 Writer output = null;
	 File file = new File(fileName);
	 String result = new String();
	 try {
		 output = new BufferedWriter(new FileWriter(file));
		 for(int y = 0; y < res.size(); y++){
			 if(res.get(y).equals("\r\n"))
				 result= result.substring(0, result.length() - 1)+ "\r\n";
			 else
				 result+= res.get(y)+",";
		 }
		 output.write(result);
		 output.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return file;
	 
 }

 
/////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws IOException{
		SLearning a = new SLearning(2);
		a.PreProcess();
	}
	

}
