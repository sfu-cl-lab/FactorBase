package ca.sfu.cs.factorbase.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to hold all the relevant FunctorNode information for a given ID (PVar/RNode).
 */
public class FunctorNodesInfo {
    private String id;
    private List<FunctorNode> functorNodes;
    private Map<String, Integer> functorNodeIndices;
    private boolean valuesAreDiscrete;
    private int nextFunctorNodeIndex = 0;


    /**
     * Create a FunctorNodesInfo to store the functornode information for the given ID.
     *
     * @param id - the ID associated with the functornode information being stored.
     * @param valuesAreDiscrete - true if all the functornode states are discrete; otherwise false.
     */
    public FunctorNodesInfo(String id, boolean valuesAreDiscrete) {
        this.id = id;
        this.valuesAreDiscrete = valuesAreDiscrete;
        this.functorNodes = new ArrayList<FunctorNode>();
        this.functorNodeIndices = new HashMap<String, Integer>();
    }


    /**
     * Store a functornode associated with the ID returned by {@link FunctorNodesInfo#getID()}, duplicates will be
     * ignored.
     *
     * @param functorNode - the functornode to add.
     */
    public void addFunctorNode(FunctorNode functorNode) {
        this.functorNodes.add(functorNode);
        this.functorNodeIndices.put(functorNode.getFunctorNodeID(), nextFunctorNodeIndex);
        nextFunctorNodeIndex++;
    }


    /**
     * The ID associated with the stored functornodes.
     *
     * @return the ID associated with the stored functornodes.
     */
    public String getID() {
        return this.id;
    }


    /**
     * Retrieve the functornodes associated with the ID returned by {@link FunctorNodesInfo#getID()}.
     *
     * @return the functornodes associated with the ID returned by {@link FunctorNodesInfo#getID()}.
     */
    public List<FunctorNode> getFunctorNodes() {
        return this.functorNodes;
    }


    /**
     * Determine if all the functornode states are discrete.
     *
     * @return true if all the functornode states are discrete; otherwise false.
     */
    public boolean isDiscrete() {
        return this.valuesAreDiscrete;
    }
}