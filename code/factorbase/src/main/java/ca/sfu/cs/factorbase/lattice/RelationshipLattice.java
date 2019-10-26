package ca.sfu.cs.factorbase.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sfu.cs.factorbase.data.FunctorNodesInfo;

/**
 * Class to store the information for a relationship lattice.
 */
public class RelationshipLattice {
    private Map<Integer, List<FunctorNodesInfo>> rchainInfosPerLevel = new HashMap<Integer, List<FunctorNodesInfo>>();
    private int latticeHeight = 0;
    private String longestRChain;
    private String longestRChainShortID;


    /**
     * Initialize a {@code RelationshipLattice} to store relationship lattice information.
     */
    public RelationshipLattice() {
    }


    /**
     * Retrieve the height of the lattice.
     *
     * @return the height of the lattice.
     */
    public int getHeight() {
        return this.latticeHeight;
    }


    /**
     * Retrieve the name of the longest RChain in the lattice.
     *
     * @return the name of the longest RChain in the lattice.
     */
    public String getLongestRChain() {
        return this.longestRChain;
    }


    /**
     * Retrieve the short version of the name of the longest RChain in the lattice.
     *
     * @return the short version of the name of the longest RChain in the lattice.
     */
    public String getLongestRChainShortID() {
        return this.longestRChainShortID;
    }


    /**
     * Add the given functor node information of an RChain to the specified lattice level.
     *
     * @param rchainInfo - the functor node information of the RChain to add.
     * @param level - the level (height) of the RChain in the lattice.
     */
    public void addRChainInfo(FunctorNodesInfo rchainInfo, int level) {
        // Update some information if the new RChain being added is the longest we've seen so far.
        if (level > this.latticeHeight) {
            this.latticeHeight = level;
            this.longestRChain = rchainInfo.getID();
            this.longestRChainShortID = rchainInfo.getShortID();
        }

        this.rchainInfosPerLevel.computeIfAbsent(
            level,
            _level -> new ArrayList<FunctorNodesInfo>()
        ).add(rchainInfo);
    }


    /**
     * Retrieve all the functor node information for the RChains with the specified length.
     *
     * @param length - the length of the RChains to retrieve.
     * @return the {@code FunctorNodesInfo}s for RChains with the specified length.
     */
    public List<FunctorNodesInfo> getRChainsInfo(int length) {
        return this.rchainInfosPerLevel.get(length);
    }
}