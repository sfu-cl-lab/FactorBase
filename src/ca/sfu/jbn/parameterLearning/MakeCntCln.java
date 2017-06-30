/*********************************************************
 * Use Makecnt class to generate txt file for the dataset
 * The table size will be stored in txt file
 * DATASETNAME_count.txt
 * The number in row i represent the table size of node i
 * The node is arranged in FinalIM order
 * 
 * place this file in ca.sfu.jbn.parameterLearning package
 * call it in ParamTet.java, after 
 * 
 * writeToBinFile(FinalIm);
 * 
 *  
 *********************************************************/

package ca.sfu.jbn.parameterLearning;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Node;



public class MakeCntCln {
	public MakeCntCln(){
		
	}
	public void makeTablesize(MlBayesIm FinalIm,db database){
		//Elwin add the following code, for the counting tuples of Node i in the mintable:
		try {
		OutputStream file;
		
		file = new FileOutputStream( global.WorkingDirectory + "/" +global.schema + "_clean_count" + ".txt" );
		String sentence = "";
		for(int i=0;i<FinalIm.getNumNodes();i++){
		
			int totalParentNode = 0;
			int newCount=1;
			int tempCount = 1;
			
			
			ArrayList<Node> tempNodes = new ArrayList<Node>();
			ArrayList<String> tableNames = new ArrayList<String>();
			ArrayList<String> fields = new ArrayList<String>();
			
			Node node;
			String tempName = "";
			node = FinalIm.getNode(i);
			tempNodes.clear();
			
			System.out.println("Node" + i + " is <"+ node.getName()+">");
			if (!node.getName().startsWith("B(")) {
				tempNodes.add(node);
				tempName = getMinTableName(tempNodes);
				
				fields = database.describeTable(tempName);
				System.out.println("table <" + getMinTableName(tempNodes) + "> has description: " + database.describeTable(tempName));
				
				for (int t = 0; t< fields.size();t++){
					if (fields.get(t).contains("_id")){
						tempName = fields.get(t);
						
						
						//!! must add this sentence to work here:
						//tempName = database.getTheRefTableName(tempName);
				
						System.out.println("Node" + i + " want to use table <" + tempName+">");
						if (!tableNames.contains(tempName)){
							tableNames.add(tempName);
							tempCount = database.countStar(tempName);
							newCount = newCount * tempCount;
							System.out.println("Node" + i + " use table <" + tempName + "> has count " + tempCount);
						}
						else {
							System.out.println("But table <"+ tempName +"> has already been counted!");
								
						}
							
					}
				}
				

			}
			else {
				System.out.println("Node" + i + " is a relationship node!!");
			}
			
			totalParentNode = FinalIm.getNumParents(i);
			for (int j=0; j<totalParentNode;j++){
				tempNodes.clear();
				node = FinalIm.getNode(FinalIm.getParent(i,j));
				
				System.out.println("Node" + i + " has parent <" + node.getName()+">");
				if (!node.getName().startsWith("B(")) {
					tempNodes.add(node);
					tempName = getMinTableName(tempNodes);
					
					
					fields = database.describeTable(tempName);
					System.out.println("table <" + tempName + "> has description: " + database.describeTable(tempName));
					
					for (int t = 0; t< fields.size();t++){
						if (fields.get(t).contains("_id")){
							tempName = fields.get(t);
							
							
							//must implement this sentence
							//tempName = database.getTheRefTableName(tempName);
							
							
							System.out.println("Node" + i + " want to use table <" + tempName+">");
							if (!tableNames.contains(tempName)){
								tableNames.add(tempName);
								tempCount = database.countStar(tempName);
								newCount = newCount * tempCount;
								System.out.println("Node" + i + " use table <" + tempName + "> has count " + tempCount);
							}
							else {
								System.out.println("But table <"+ tempName +"> has already been counted!");
									
							}
								
						}
					}
				}
				else {
					System.out.println("Node" + i + " is a relationship node!!");
				}
			}
			
						
			//newCount = database.countStar(getMinTableName(tempNodes));
			
			System.out.println("Node" + i + " finally has count " + newCount + "\n");
			sentence = newCount + "\n";
			file.write(sentence.getBytes());
			
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		//End of adding code. By Elwin
	}
	public String getMinTableName(List<Node> nodes) throws SQLException {
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
}
