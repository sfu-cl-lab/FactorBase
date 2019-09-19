package ca.sfu.cs.factorbase.data;

import java.util.List;

/**
 * The information available from a given contingency (CT) table.
 */
public class ContingencyTable {
    private long[] countsArray;
    private int childColumnIndex;
    private int[] parentColumnIndices;
    private DataSetMetaData metadata;


    /**
     * Create a Java representation of a CT table, which consists of counts and metadata.
     *
     * @param countsArray - array containing counts for the groundings of the given child and parent variables.
     * @param childColumnIndex - the column index (from the original dataset) of the child variable.
     * @param parentColumnIndices - the column indices (from the original dataset) of the parent variables.
     * @param metadata - metadata from the dataset used to generate the given Array {@code countsArray}.
     */
    public ContingencyTable(
        long[] countsArray,
        int childColumnIndex,
        int[] parentColumnIndices,
        DataSetMetaData metadata
    ) {
        this.countsArray = countsArray;
        this.childColumnIndex = childColumnIndex;
        this.parentColumnIndices = parentColumnIndices;
        this.metadata = metadata;
    }


    /**
     * Retrieve the number of times a particular instance (grounding) occurs.
     * <p>
     * <b>IMPORTANT</b>: The list of parent {@code RandomVariableAssignment}s must be in the same order as the parent
     *                   column indices passed to {@link ContingencyTableGenerator#generateCT(int, int[], int)}.
     * </p>
     *
     * @param childAssignment - the child random variable to get the counts for.
     * @param parentAssignments - the parent random variables to get the counts for.
     * @return the number of times the grounding occurs.
     */
    public long getCounts(RandomVariableAssignment childAssignment, List<RandomVariableAssignment> parentAssignments) {
        if (this.childColumnIndex != childAssignment.getVariableColumnIndex()) {
            throw new IllegalArgumentException(
                "Counts can only be retrieved from a contingency table when the full grounding is given with the " +
                "child variable given first followed by its parents."
            );
        }

        int totalNumberOfVariables = parentAssignments.size() + 1;
        RandomVariableAssignment[] selectedRandomVariables = new RandomVariableAssignment[totalNumberOfVariables];
        selectedRandomVariables[0] = childAssignment;

        /**
         * Validate if the full grounding is given for the parents and that they are in the same order as when the CT
         * table was generated.  Combine the parents with the child so that they can be used together to generate the
         * grounding key.
         */
        if (!parentAssignments.isEmpty()) {
            int insertIndex = 1;
            int index = 0;
            for (RandomVariableAssignment parentAssignment : parentAssignments) {
                if (this.parentColumnIndices[index] != parentAssignment.getVariableColumnIndex()) {
                    throw new IllegalArgumentException("Counts can only be retrieved from a contingency table when the full grounding is given.");
                }

                selectedRandomVariables[insertIndex] = parentAssignment;
                insertIndex++;
                index++;
            }
        } else if(this.parentColumnIndices.length != 0) {
            throw new IllegalArgumentException("Counts can only be retrieved from a contingency table when the full grounding is given.");
        }

        int countsIndex = this.metadata.generateIndex(selectedRandomVariables);
        return this.countsArray[countsIndex];
    }


    /**
     * Retrieve the metadata associated with the contingency table.
     *
     * @return the metadata for the contingency table.
     */
    public DataSetMetaData getMetaData() {
        return this.metadata;
    }


    /**
     * Retrieve the column indices of the parent variables.
     *
     * @return the column indices of the parent variables.
     */
    public int[] getParentColumnIndices() {
        return this.parentColumnIndices;
    }


    /**
     * Retrieve the column index of the child variable.
     *
     * @return the column index of the child variable.
     */
    public int getChildColumnIndex() {
        return this.childColumnIndex;
    }
}