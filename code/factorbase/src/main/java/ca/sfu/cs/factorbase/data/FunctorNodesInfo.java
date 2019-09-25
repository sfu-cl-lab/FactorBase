package ca.sfu.cs.factorbase.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold all the relevant FunctorNode information for a given ID (PVar/RNode).
 */
public class FunctorNodesInfo {
    private String id;
    private Map<String, FunctorNode> functorNodes;
    private boolean valuesAreDiscrete;


    /**
     * Create a FunctorNodesInfo to store the functor node information for the given ID.
     *
     * @param id - the ID associated with the functor node information being stored.
     * @param valuesAreDiscrete - true if all the functor node states are discrete; otherwise false.
     */
    public FunctorNodesInfo(String id, boolean valuesAreDiscrete) {
        this.id = id;
        this.valuesAreDiscrete = valuesAreDiscrete;
        this.functorNodes = new HashMap<String, FunctorNode>();
    }


    /**
     * Store a functor node associated with the ID returned by {@link FunctorNodesInfo#getID()}, duplicates will be
     * ignored.
     *
     * @param functorNode - the functor node to add.
     */
    public void addFunctorNode(FunctorNode functorNode) {
        this.functorNodes.put(functorNode.getFunctorNodeID(), functorNode);
    }


    /**
     * The ID associated with the stored functor nodes.
     *
     * @return the ID associated with the stored functor nodes.
     */
    public String getID() {
        return this.id;
    }


    /**
     * Retrieve the functor nodes associated with the ID returned by {@link FunctorNodesInfo#getID()}.
     *
     * @return the functor nodes associated with the ID returned by {@link FunctorNodesInfo#getID()}.
     */
    public Collection<FunctorNode> getFunctorNodes() {
        return this.functorNodes.values();
    }


    /**
     * Retrieve the functor nodes associated with the ID returned by {@link FunctorNodesInfo#getID()}, mapped by their ID.
     *
     * @return the functor nodes associated with the ID returned by {@link FunctorNodesInfo#getID()}.
     */
    public Map<String, FunctorNode> getMappedFunctorNodes() {
        return this.functorNodes;
    }


    /**
     * Determine if all the functor node states are discrete.
     *
     * @return true if all the functor node states are discrete; otherwise false.
     */
    public boolean isDiscrete() {
        return this.valuesAreDiscrete;
    }


    /**
     * Retrieve the number of states for the given functor node ID.
     *
     * @param functorNodeID - the ID of the functor node to get the number of states for.
     * @return the number of states for the given functor node ID.
     */
    public int getNumberOfStates(String functorNodeID) {
        return this.functorNodes.get(functorNodeID).getFunctorNodeStates().size();
    }


    /**
     * Merge the information from the given {@code FunctorNodesInfo} into this one.
     *
     * @param functorNodesInfo - the {@code FunctorNodesInfo} with the information to merge into this one.
     */
    public void merge(FunctorNodesInfo functorNodesInfo) {
        this.functorNodes.putAll(functorNodesInfo.getMappedFunctorNodes());
        this.valuesAreDiscrete = this.valuesAreDiscrete && functorNodesInfo.isDiscrete();
    }
}