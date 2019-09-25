package ca.sfu.cs.factorbase.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to store information for a FunctorNode.
 */
public class FunctorNode {
    private String functorNodeID;
    private Set<String> functorNodeStates;


    /**
     * Create a FunctorNode to store its metadata.
     *
     * @param functorNodeID - the ID for the functor node.
     */
    public FunctorNode(String functorNodeID) {
        this.functorNodeID = functorNodeID;
        this.functorNodeStates = new HashSet<String>();
    }


    /**
     * Store a possible state for the functor node (duplicates will be ignored).
     *
     * @param stateValue - the value of the state for the functor node.
     */
    public void addState(String stateValue) {
        this.functorNodeStates.add(stateValue);
    }


    /**
     * Retrieve the ID for the functor node.
     *
     * @return the ID for the functor node.
     */
    public String getFunctorNodeID() {
        return this.functorNodeID;
    }


    /**
     * Retrieve the possible states for the functor node.
     *
     * @return the possible states for the functor node.
     */
    public Set<String> getFunctorNodeStates() {
        return this.functorNodeStates;
    }
}