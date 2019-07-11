package ca.sfu.cs.factorbase.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

/**
 * Class to hold metadata for a dataset.  Also contains logic to create unique indices given a set of variables or
 * numbers.
 */
public class DataSetMetaData {
    private Map<String, Integer> variableNameToColumnIndex;
    private Set<String> variableNames;
    private Map<String, Integer> variableStateToIntegerEncoding;
    private Map<String, Set<String>> variableStates;
    private int numberOfRows;
    private String[] header;
    private int countsColumnIndex;


    /**
     * Create an object to store metadata information for a dataset.
     *
     * @param variableNameToColumnIndex - Map object that maps each variable name to its associated column index in the dataset.
     * @param variableStateToIntegerEncoding - Map object that maps each variable assignment to an integer value.
     * @param variableStates - a Map containing key:value pairs of variable:possible-states.
     * @param numberOfRows - the total number of rows in the dataset.
     * @param header - the header values for the dataset.
     * @param countsColumnIndex - the column index for the column containing the count value.
     */
    public DataSetMetaData(
        Map<String, Integer> variableNameToColumnIndex,
        Map<String, Integer> variableStateToIntegerEncoding,
        Map<String, Set<String>> variableStates,
        int numberOfRows,
        String[] header,
        int countsColumnIndex
    ) {
        this.variableNameToColumnIndex = variableNameToColumnIndex;
        this.variableStateToIntegerEncoding = variableStateToIntegerEncoding;
        this.variableStates = variableStates;
        this.numberOfRows = numberOfRows;
        this.header = header;
        this.countsColumnIndex = countsColumnIndex;
        this.variableNames = variableNameToColumnIndex.keySet().stream().filter(name -> !name.equals(header[countsColumnIndex])).collect(Collectors.toSet());
    }


    /**
     * Generate an index value for the given set of {@code RandomVariableAssignment}s.
     *
     * @param randomVariableAssignments - a set of {@code RandomVariableAssignment}s to generate an index for.
     * @return Integer value index for the given set of {@code RandomVariableAssignment}s.
     *
     * Note: This method is based on the getRowIndex() method from Tetrad's BDeuScore.java file.
     */
    public int generateIndex(List<RandomVariableAssignment> randomVariableAssignments) {
        randomVariableAssignments.sort(
            (assignment1, assignment2) -> {
                Integer index1 = this.variableNameToColumnIndex.get(assignment1.getName());
                Integer index2 = this.variableNameToColumnIndex.get(assignment2.getName());
                return index1.compareTo(index2);
            }
        );

        int index = 0;

        for (RandomVariableAssignment assignment : randomVariableAssignments) {
            index *= this.getNumberOfStates(assignment.getName());
            index += this.variableStateToIntegerEncoding.get(assignment.toString());
        }

        return index;
    }


    /**
     * Generate an index value for the given lists of numbers containing information for a row from the dataset.
     * <p>
     * IMPORTANT: To ensure that this acts as an injective function, be sure to pass in the variables in column index
     *            order for both lists when calling this method.
     * </p>
     *
     * @param statesPerVariable - list of numbers where each number indicates the number of states for a column.
     * @param rowValues - matching list of Integer encoded state values for a row from the dataset.
     * @return Integer value index for the given list of number of states per variable and the list of Integer
     *         encoded values for the given row.
     *
     * Note: This method is based on the getRowIndex() method from Tetrad's BDeuScore.java file.
     */
    public int generateIndex(List<Integer> statesPerVariable, List<Long> rowValues) {
        int index = 0;

        for (int variableIndex = 0; variableIndex < statesPerVariable.size(); variableIndex++) {
            index *= statesPerVariable.get(variableIndex);
            index += rowValues.get(variableIndex);
        }

        return index;
    }


    /**
     * Retrieve the number of possible states for the given variable.
     *
     * @param variable - the name of the variable to get the number of states for.
     * @return the number of possible states for the given variable.
     */
    public int getNumberOfStates(String variable) {
        Set<String> states = this.variableStates.getOrDefault(variable, new HashSet<String>());
        return states.size();
    }


    /**
     * Create the Cartesian product of all the states for each random variable in the given list.
     *
     * @param variables - the names of the random variables to create the Cartesian product for their possible states.
     * @return a set of unique lists containing all possible combinations of the given random variables' states.
     */
    public Set<List<RandomVariableAssignment>> getStates(Set<String> variables) {
        List<Set<RandomVariableAssignment>> variableStateCombinations = new ArrayList<Set<RandomVariableAssignment>>();

        for (String variable : variables) {
            Set<RandomVariableAssignment> states = new HashSet<RandomVariableAssignment>();
            states.addAll(this.getStates(variable).stream().map(
                state -> new RandomVariableAssignment(variable, state)
            ).collect(Collectors.toSet()));
            variableStateCombinations.add(states);
        }

        return Sets.cartesianProduct(variableStateCombinations);
    }


    /**
     * Retrieve the possible states that a random variable can take.
     *
     * @param variable - the name of the random variable to retrieve the possible states for.
     * @return a set of the possible states that the given random variable can take.
     */
    public Set<String> getStates(String variable) {
        return this.variableStates.getOrDefault(variable, new HashSet<String>());
    }


    /**
     * Retrieve all the variable names for the dataset.
     *
     * @return all the variable names in column order.
     *
     * IMPORTANT: Tetrad seems to expect the variable names to be in column order, which is why we
     *            have this requirement.  If the variables are not in column order, different Bayes
     *            nets can be generated.
     */
    public Set<String> getVariableNames() {
        return this.variableNames;
    }


    /**
     * Retrieve the column index for the given variable.
     *
     * @param variable the name of the variable to get the column index for.
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
     * Retrieve the Integer encoding for the given variable and a particular state for it.
     *
     * @param variableAssignment - the variable and its assigned state to get the Integer encoding for.
     * @return the Integer encoding for the given variable state.
     */
    public long getVariableStateIndex(String variableAssignment) {
        return this.variableStateToIntegerEncoding.get(variableAssignment);
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