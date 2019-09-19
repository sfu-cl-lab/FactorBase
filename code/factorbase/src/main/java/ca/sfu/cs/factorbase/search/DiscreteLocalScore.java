package ca.sfu.cs.factorbase.search;

import java.util.Set;

import ca.sfu.cs.factorbase.exception.ScoringException;

public interface DiscreteLocalScore {
    /**
     * Compute the score for the given family of nodes.
     *
     * @param child - name of child node.
     * @param parents - name of parent nodes.
     * @return the score of the given family of nodes.
     * @throws ScoringException if an error occurs when trying to compute the score.
     */
    double localScore(String child, Set<String> parents) throws ScoringException;
}
