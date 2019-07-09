package ca.sfu.cs.factorbase.data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The information available from a given contingency (CT) table.
 */
public class ContingencyTable {

    private long[] countsArray;
    private List<String> variables;
    private DataSetMetaData metadata;


    /**
     * Create a Java representation of a CT table, which consists of counts and metadata.
     * @param countsArray - array containing counts for the groundings of all the variables in the
     *                      given List {@code variables}.
     * @param variables - List containing all the variables for the CT table.
     * @param metadata - metadata from the dataset used to generate the given Array {@code countsArray}.
     */
    public ContingencyTable(long[] countsArray, List<String> variables, DataSetMetaData metadata) {
        this.countsArray = countsArray;
        this.variables = variables;
        this.metadata = metadata;
    }


    /**
     * Retrieve the number of times a particular instance (grounding) occurs.
     *
     * @param randomVariableAssignments - list of attribute assignments for the grounding of interest.
     * @return the number of times the grounding occurs.
     */
    public long getCounts(List<RandomVariableAssignment> randomVariableAssignments) {
        if (!this.variables.equals(randomVariableAssignments.stream().map(assignment -> assignment.getName()).collect(Collectors.toList()))) {
            throw new IllegalArgumentException("Counts can only be retrieved from a contingency table when the full grounding is given.");
        }

        int stateIndex = this.metadata.generateIndex(randomVariableAssignments);
        return this.countsArray[stateIndex];
    }
}