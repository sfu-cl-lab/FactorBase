package ca.sfu.jbn.decisionTree;

import java.util.ArrayList;

public class TreeNode {

	private int nodeId = 0;				/* Node index */
	private int nodeParentId = 0;		/* Parent index */
	private String nodeName = null;
	private String nodeValue = null;
	private ArrayList<Integer> childrenId = null;
	private ArrayList<Double> childrenProb = null;
	private ArrayList<String> childrenPathValue = null;

	private boolean leafnode = false;
	
	private final static String nil = "*";

	/* Constructor for inner nodes */
	public TreeNode(String nodeName, int nodeId, int nodeParentId)
	{
		setNodeName(nodeName);
		setNodeId(nodeId);
		setNodeParentId(nodeParentId);
		setNodeValue(nil);
		childrenId = new ArrayList<Integer>();
		childrenProb = new ArrayList<Double>();
		childrenPathValue = new ArrayList<String>();
	}
	
	/* Constructor for leaf nodes */
	public TreeNode(String nodeName, int nodeId, int nodeParentId, String value)
	{
		setNodeName(nodeName);
		setNodeId(nodeId);
		setNodeParentId(nodeParentId);
		setNodeValue(value);
		leafnode = true;
		childrenId = new ArrayList<Integer>();
		childrenProb = new ArrayList<Double>();
		childrenPathValue = new ArrayList<String>();
	}

	public boolean isLeafNode()
	{
		return leafnode;
	}
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getNodeParentId() {
		return nodeParentId;
	}

	public void setNodeParentId(int nodeParentId) {
		this.nodeParentId = nodeParentId;
	}

	public ArrayList<Integer> getChildrenId() {
		return childrenId;
	}
	
	public ArrayList<Double> getChildrenProb() {
		return childrenProb;
	}
	
	public ArrayList<String> getChildrenPathValue() {
		return childrenPathValue;
	}

	public void addNewChild(int id, double prob, String pathValue)
	{
		childrenId.add(id);
		childrenProb.add(prob);
		childrenPathValue.add(pathValue);
	}

	public String getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(String nodeValue) {
		this.nodeValue = nodeValue;
	}
	
	@Override
	public String toString()
	{
		String a = nodeId + " " + nodeName + " " + nodeValue + " " + nodeParentId + " ";
		String b = "childId:\t" + childrenId + "\npathValue:\t" + childrenPathValue + 
		"\nchildProb:\t" + childrenProb; 
		return a + "\n" + b + "\n";
		
	}

}
