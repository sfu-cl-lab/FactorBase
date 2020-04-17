package ca.sfu.cs.factorbase.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Sets;

/**
 * Class to hold metadata for a dataset.  Also contains logic to create unique indices given a set of variables or
 * numbers.
 */
public class DataSetMetaData {
    private Map<String, Integer> variableNameToColumnIndex;
    private List<String> variableNames;
    private List<Set<String>> variableStates;
    private int numberOfRows;
    private String[] header;
    private int countsColumnIndex;


    /**
     * Create an object to store metadata information for a dataset.
     *
     * @param variableNameToColumnIndex - Map object that maps each variable name to its associated column index in the dataset.
     * @param variableStates - a Map containing key:value pairs of variable:possible-states.
     * @param numberOfRows - the total number of rows in the dataset.
     * @param header - the header values for the dataset.
     * @param countsColumnIndex - the column index for the column containing the count value.
     */
    public DataSetMetaData(
        Map<String, Integer> variableNameToColumnIndex,
        List<Set<String>> variableStates,
        int numberOfRows,
        String[] header,
        int countsColumnIndex
    ) {
        this.variableNameToColumnIndex = variableNameToColumnIndex;
        this.variableStates = variableStates;
        this.numberOfRows = numberOfRows;
        this.header = header;
        this.countsColumnIndex = countsColumnIndex;
        this.variableNames = IntStream.range(0, header.length).filter(columnIndex -> columnIndex != countsColumnIndex).mapToObj(filteredIndex -> header[filteredIndex]).collect(Collectors.toList());
    }


    /**
     * Generate an index value for the given set of {@code RandomVariableAssignment}s.
     * <p>
     * <b>IMPORTANT</b>: To ensure that this acts as an injective function, be sure to pass in the variables in a
     *                   consistent order (e.g. column index order) when calling this method.
     * </p>
     * <p>
     * Note: This method is based on the getRowIndex() method from Tetrad's BDeuScore.java file.  Basically we are
     *       generating a unique number by treating each column as a different based number and converting it to a
     *       base 10 number.
     * </p>
     *
     * @param randomVariableAssignments - a set of {@code RandomVariableAssignment}s to generate an index for.
     * @return Integer value index for the given set of {@code RandomVariableAssignment}s.
     */
    public int generateIndex(RandomVariableAssignment[] randomVariableAssignments) {
        int index = 0;

        for (RandomVariableAssignment assignment : randomVariableAssignments) {
            index *= this.getNumberOfStates(assignment.getVariableColumnIndex());
            index += assignment.getStateIndex();
        }

        return index;
    }


    /**
     * Generate an index value for the given lists of numbers containing information for a row from the dataset.
     * <p>
     * <b>IMPORTANT</b>: To ensure that this acts as an injective function, be sure to pass in the two arrays in a
     *                   consistent order (e.g. column index order) when calling this method.
     * </p>
     * <p>
     * Note: This method is based on the getRowIndex() method from Tetrad's BDeuScore.java file.  Basically we are
     *       generating a unique number by treating each column as a different based number and converting it to a
     *       base 10 number.
     * </p>
     *
     * @param statesPerVariable - list of numbers where each number indicates the number of states for a column.
     * @param rowValues - matching list of Integer encoded state values for a row from the dataset.
     * @return Integer value index for the given list of number of states per variable and the list of Integer
     *         encoded values for the given row.
     */
    public int generateIndex(int[] statesPerVariable, double[] rowValues) {
        int index = 0;

        for (int variableIndex = 0; variableIndex < statesPerVariable.length; variableIndex++) {
            index *= statesPerVariable[variableIndex];
            index += rowValues[variableIndex];
        }

        return index;
    }


    /**
     * Retrieve the number of possible states for the given variable.
     *
     * @param variableColumnIndex - the column index of the variable to retrieve the number of states for.
     * @return the number of possible states for the given variable.
     */
    public int getNumberOfStates(int variableColumnIndex) {
        try {
            return this.variableStates.get(variableColumnIndex).size();
        } catch(IndexOutOfBoundsException e) {
            return 0;
        }
    }


    /**
     * Create the Cartesian product of all the states for each random variable in the given list.
     *
     * @param variableColumnIndices - the column indices of the random variables to create the Cartesian product of
     *                                their possible states for.
     * @return a set of unique lists containing all possible combinations of the given random variables' states.
     */
    public Set<List<RandomVariableAssignment>> getStates(int[] variableColumnIndices) {
        int listSize = variableColumnIndices.length;
        List<Set<RandomVariableAssignment>> variableStateCombinations = new ArrayList<Set<RandomVariableAssignment>>(listSize);

        for (int variableColumnIndex : variableColumnIndices) {
            Set<RandomVariableAssignment> states = IntStream.range(
                0,
                this.getNumberOfStates(variableColumnIndex)
            ).mapToObj(
                stateIndex -> new RandomVariableAssignment(variableColumnIndex, stateIndex)
            ).collect(
                Collectors.toSet()
            );

            variableStateCombinations.add(states);
        }

        return Sets.cartesianProduct(variableStateCombinations);
    }


    /**
     * Retrieve the possible states that a random variable can take.
     *
     * @param variableColumnIndex - the column index of the variable to retrieve the number of states for.
     * @return a set of the possible states that the given random variable can take.
     */
    public Set<String> getStates(int variableColumnIndex) {
        try {
            return this.variableStates.get(variableColumnIndex);
        } catch(IndexOutOfBoundsException e) {
            return new HashSet<String>();
        }
    }


    /**
     * Retrieve all the variable names for the dataset in column order.
     *<p>
     *<b>IMPORTANT</b>: Tetrad seems to expect the variable names to be in column order, which is why we
     *                  have this requirement.  If the variables are not in column order, different Bayes
     *                  nets can be generated.
     *</p>
     *
     * @return all the variable names in column order.
     */
    public List<String> getVariableNames() {
        return this.variableNames;
    }


    /**
     * Retrieve the column index for the given variable.
     *
     * @param variable - the name of the variable to get the column index for.
     * @return column index of the given variable.
     */
    public int getColumnIndex(String variable) {
        return this.variableNameToColumnIndex.get(variable);
    }


    /**
     * Retrieve the total number of rows in the dataset.
     *
     * @return the total number of rows in the dataset.
     */
    public int getNumberOfRows() {
        return this.numberOfRows;
    }


    /**
     * Retrieve the total number of columns in the dataset.
     *
     * @return the total number of columns in the dataset.
     */
    public int getNumberOfColumns() {
        return this.variableNameToColumnIndex.size();
    }


    /**
     * The header values for the dataset.
     *
     * @return header values for the dataset.
     */
    public String[] getHeader() {
        return this.header;
    }


    /**
     * Retrieve the column index for the column in the dataset containing the count value.
     *
     * @return the column index for the count column.
     */
    public int getCountColumnIndex() {
        return this.countsColumnIndex;
    }
}