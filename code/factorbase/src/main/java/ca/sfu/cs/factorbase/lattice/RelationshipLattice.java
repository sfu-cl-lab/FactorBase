package ca.sfu.cs.factorbase.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to store the information for a relationship lattice.
 */
public class RelationshipLattice {
    private Map<Integer, List<String>> rchainsPerLevel = new HashMap<Integer, List<String>>();
    private int latticeHeight = 0;
    private String longestRChain;


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
     * Add the given RChain to the specified lattice level.
     *
     * @param rchain - the name of the RChain to add.
     * @param level - the level (height) of the RChain in the lattice.
     */
    public void addRChain(String rchain, int level) {
        // Update some information if the new RChain being added is the longest we've seen so far.
        if (level > this.latticeHeight) {
            this.latticeHeight = level;
            this.longestRChain = rchain;
        }

        this.rchainsPerLevel.computeIfAbsent(
            level,
            _level -> new ArrayList<String>()
        ).add(rchain);
    }


    /**
     * Retrieve all the RChains with the specified length.
     *
     * @param length - the length of the RChains to retrieve.
     * @return the RChains with the specified length.
     */
    public List<String> getRChains(int length) {
        return this.rchainsPerLevel.get(length);
    }
}