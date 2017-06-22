package MLNExporter;
/*
 * Description: This class is BNNode.
 *	   		   It allows the users to store the node information of Bayesian network
 *
 * Function: getName - get the value of node name
 *			 getID - return the value of node ID (shows in the database)
 *			getMLNStr - get the value of MLNStr
 *			BNNode - create a new node
 *			isR - give True if it is relationship, otherwise, give False 
 * 
 * Bugs: None
 *
 * Version: 1.0
 *
 */

public class BNNode {
	private String name;
	private String ID;
	private String MLNStr;
	private boolean isR;
/*
 * create a new BNNode
 * @param name the name of the node
 * @param ID the ID of the node
 * @param MLNStr  the MLNStr of this node
 * @param isR a boolean value
 */
	public BNNode(String name, String ID, String MLNStr, boolean isR){
		this.name = name;
		this.ID = ID;
		this.MLNStr = MLNStr;
		this.isR = isR;
	}
/*
 * get the value of node name	
 */
	public String getName() {
		return name;
	}

	/*
	 * get the value of node ID (shows in the database)
	 */
	public String getID() {
		return ID;
	}
/*
 * get the value of MLNStr
 */
	public String getMLNStr() {
		return MLNStr;
	}

/*
 * give True if it is a relationship, otherwise, give False
 */
	public boolean isR() {
		return isR;
	}
}
