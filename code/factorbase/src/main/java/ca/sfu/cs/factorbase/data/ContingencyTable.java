package ca.sfu.cs.factorbase.data;

import java.util.List;
import java.util.Set;

/**
 * The information available from a given contingency (CT) table.
 */
public interface ContingencyTable {
    /**
     * Retrieve the number of possible states that a random variable can take.
     *
     * @param variable - the name of the random variable to retrieve the number of states for.
     * @return the number of possible states that the given random variable can take.
     */
    int getNumberOfStates(String variable);

    /**
     * Create the Cartesian product of all the states for each random variable in the given list.
     *
     * @param variables - the names of the random variables to create the Cartesian product for their possible states.
     * @return a set of unique lists containing all possible combinations of the given random variables' states.
     */
    Set<List<RandomVariableAssignment>> getStates(Set<String> variables);

    /**
     * Retrieve the possible states that a random variable can take.
     *
     * @param variable - the name of the random variable to retrieve the possible states for.
     * @return a set of the possible states that the given random variable can take.
     */
    Set<String> getStates(String variable);

    /**
     * Retrieve the number of times a particular instance (grounding) occurs.
     *
     * Note: If only a subset of the random variables are assigned, this method is expected to return 0.
     *
     * @param randomVariableAssignments - list of attribute assignments for the grounding of interest.
     * @return the number of times the grounding occurs.
     */
    long getCounts(Set<RandomVariableAssignment> randomVariableAssignments);

    /**
     * The names of the random variables available in the dataset.
     *
     * @return a set of the names of random variables in the dataset.
     */
    Set<String> getVariableNames();

    /**
     * True iff the dataset only contains discrete data.
     *
     * @return True if the dataset only contains discrete data.
     */
    boolean isDiscrete();

    /**
     * Determine the total number of occurrences for the given random variable assignment.
     *
     * @param variableAssignment - the variable assignment to retrieve the total counts for.
     * @return the total number of occurrences for the given random variable assignment.
     */
    long getTotalInstances(RandomVariableAssignment variableAssignment);
}
