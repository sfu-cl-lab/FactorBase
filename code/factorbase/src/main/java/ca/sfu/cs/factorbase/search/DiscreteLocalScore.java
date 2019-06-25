package ca.sfu.cs.factorbase.search;

import java.util.Set;

public interface DiscreteLocalScore {
    /**
     * Compute the score for the given family of nodes.
     * @param child - name of child node.
     * @param parents - name of parent nodes.
     * @return the score of the given family of nodes.
     */
    double localScore(String child, Set<String> parents);
}
