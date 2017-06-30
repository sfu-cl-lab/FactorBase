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



public class MakeCnt {
	public MakeCnt(){
		
	}
	public void makeTablesize(MlBayesIm FinalIm,db database){
		//Elwin add the following code, for the counting tuples of Node i in the mintable:
		try {
		OutputStream file;
		
		file = new FileOutputStream( global.WorkingDirectory + "/" +global.schema + "_count" + ".txt" );
		String sentence = "";
		for(int i=0;i<FinalIm.getNumNodes();i++){
		
			int totalParentNode = 0;
			totalParentNode = FinalIm.getNumParents(i);
			ArrayList<Node> tempNodes = new ArrayList<Node>();
			Node node; 
			for (int j=0; j<totalParentNode;j++){
				node = FinalIm.getNode(FinalIm.getParent(i,j));
				if (!node.getName().startsWith("B(")) {tempNodes.add(node);}
			}
			node = FinalIm.getNode(i);
			if (!node.getName().startsWith("B(")) tempNodes.add(node);
			int newCount;
			
				newCount = database.countStar(getMinTableName(tempNodes));
			
			System.out.println("Node" + i + " use table " + getMinTableName(tempNodes) + " has count " + newCount);
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
}
