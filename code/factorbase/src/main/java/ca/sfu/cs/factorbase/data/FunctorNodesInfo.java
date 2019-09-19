package ca.sfu.cs.factorbase.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ca.sfu.cs.factorbase.util.Mapper;

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


    /**
     * Retrieve the number of states for the functornode at the given index.
     *
     * @param index - the index of the functornode to get the number of states for.
     * @return the number of states for the given functornode index.
     */
    public int getNumberOfStates(int index) {
        return this.functorNodes.get(index).getFunctorNodeStates().size();
    }


    /**
     * Retrieve the index of the functornode for the given functornode ID.
     *
     * @param functorNodeID - the ID of the functornode to retrieve the index for.
     * @return the index of the functornode for the given functornode ID.
     */
    public int getIndex(String functorNodeID) {
        return this.functorNodeIndices.get(functorNodeID);
    }


    /**
     * Retrieve the indices of the functornodes for the given functornode IDs.
     *
     * @param functorNodeIDs - the IDs of the functornodes to retrieve the indices for.
     * @return the indices of the functornodes for the given functornode IDs.
     */
    public int[] getIndices(Set<String> functorNodeIDs) {
        return Mapper.convertToIndices(functorNodeIDs, this::getIndex);
    }
}