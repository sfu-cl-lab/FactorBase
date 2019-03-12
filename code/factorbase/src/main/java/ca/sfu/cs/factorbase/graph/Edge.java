package ca.sfu.cs.factorbase.graph;

/**
 * Simple class to store information for an edge between a parent and a child node.
 */
public class Edge {

    private String parent;
    private String child;


    /**
     * Create an Edge object that represents an edge between the given child and parent nodes.
     * @param parent - the parent node.
     * @param child - the child node.
     */
    public Edge(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }

    /**
     * Retrieve the parent for the given edge.
     * @return the parent for the given edge.
     */
    public String getParent() {
        return this.parent;
    }

    /**
     * Retrieve the child for the given edge.
     * @return the child for the given edge.
     */
    public String getChild() {
        return this.child;
    }
}
