package ca.sfu.cs.factorbase.data;

import java.util.List;
import java.util.Set;
import ca.sfu.cs.factorbase.exception.DataExtractionException;


/**
 * Class to generate CT tables from various data sources.
 */
public class ContingencyTableGenerator {

    private boolean isDiscrete;
    private long[][] data;
    private DataSetMetaData metadata;


    /**
     * Generates contingency tables based on the given data source.
     *
     * @param dataExtractor - {@code DataExtractor} that extracts information from a CT table source.
     * @throws DataExtractionException if unable to process the given data source.
     */
    public ContingencyTableGenerator(DataExtractor dataExtractor) throws DataExtractionException {
        DataSet dataset = dataExtractor.extractData();
        this.data = dataset.getData();
        this.metadata = dataset.getMetaData();
        this.isDiscrete = dataset.isDiscrete();
    }


    /**
     * Indicates whether or not the dataset given to the ContingencyTableGenerator only contains
     * discrete information.
     *
     * @return true if the dataset only contains discrete information; otherwise false.
     */
    public boolean isDiscrete() {
        return this.isDiscrete;
    }


    /**
     * Retrieve all the variable names for the dataset given to the ContingencyTableGenerator.
     *
     * @return all the variable names in column order.
     *
     * IMPORTANT: Tetrad seems to expect the variable names to be in column order, which is why we
     *            have this requirement.  If the variables are not in column order, different Bayes
     *            nets can be generated.
     */
    public List<String> getVariableNames() {
        return this.metadata.getVariableNames();
    }


    /**
     * Generate a CT table for the given variables using the dataset given to the ContingencyTableGenerator.
     *
     * @param childColumnIndex - the column index of the child variable to create the CT table for using the dataset
     *                           given to the ContingencyTableGenerator.
     * @param parentColumnIndices - the column indices of the parent variables to create the CT table for using the
     *                              dataset given to the ContingencyTableGenerator.
     * @param totalNumberOfStates - the total number of state combinations for the given child and parent variables.
     * @return a CT table for the given variables using the dataset given to the ContingencyTableGenerator.
     */
    public ContingencyTable generateCT(int childColumnIndex, int[] parentColumnIndices, int totalNumberOfStates) {
        int[] numberOfStatesPerVariable = this.getNumberOfStates(childColumnIndex, parentColumnIndices);
        long[] countsArray = new long[totalNumberOfStates];
        int totalNumberOfVariables = parentColumnIndices.length + 1;
        long[] rowValues = new long[totalNumberOfVariables];

        // for loop to process each row in the dataset.
        for (int rowIndex = 0; rowIndex < this.data.length; rowIndex++) {
            rowValues[0] = this.data[rowIndex][childColumnIndex];

            // for loop to process each parent column for the current row in the dataset.
            int insertIndex = 1;
            for (int selectedIndex : parentColumnIndices) {
                rowValues[insertIndex] = this.data[rowIndex][selectedIndex];
                insertIndex++;
            }

            countsArray[this.metadata.generateIndex(numberOfStatesPerVariable, rowValues)] += this.data[rowIndex][this.metadata.getCountColumnIndex()];
        }

        return new ContingencyTable(countsArray, childColumnIndex, parentColumnIndices, this.metadata);
    }


    /**
     * Create the Cartesian product of all the states for each random variable in the given list.
     *
     * @param variableColumnIndices - the column indices of the random variables to create the Cartesian product of
     *                                their possible states for.
     * @return a set of unique lists containing all possible combinations of the given random variables' states.
     */
    public Set<List<RandomVariableAssignment>> getStates(int[] variableColumnIndices) {
        return this.metadata.getStates(variableColumnIndices);
    }


    /**
     * Retrieve the possible states that a random variable can take.
     *
     * @param variableColumnIndex - the column index of the random variable to retrieve the possible states for.
     * @return a set of the possible states that the given random variable can take.
     */
    public Set<String> getStates(int variableColumnIndex) {
        return this.metadata.getStates(variableColumnIndex);
    }


    /**
     * Retrieve the number of possible states that the given child random variable can take as well as the number of
     * possible states that each of the given parent random variables can take.
     *
     * Note: The first number returned is for the child variable and the ones following it are for the parents with the
     *       order matching the ones given in {@code parentColumnIndices}.
     *
     * @param childColumnIndex - the column index of the child variable to get the number of possible states for.
     * @param parentColumnIndices - the column indices of the parent variables to get the number of possible states for.
     * @return the number of possible states for the child random variable and the given parent random variables.
     */
    private int[] getNumberOfStates(int childColumnIndex, int[] parentColumnIndices) {
        int totalNumberOfVariables = parentColumnIndices.length + 1;
        int[] numberOfStatesPerVariable = new int[totalNumberOfVariables];

        // Get the number of states for the child random variable.
        numberOfStatesPerVariable[0] = this.getNumberOfStates(childColumnIndex);

        // for loop to get the number of states for each of the given parent random variables.
        int insertIndex = 1;
        for (int variableColumnIndex : parentColumnIndices) {
            numberOfStatesPerVariable[insertIndex] = this.getNumberOfStates(variableColumnIndex);
            insertIndex++;
        }

        return numberOfStatesPerVariable;
    }


    /**
     * Retrieve the number of possible states that a random variable can take.
     *
     * @param variableColumnIndex - the column index of the random variable to retrieve the number of states for.
     * @return the number of possible states that the given random variable can take.
     */
    public int getNumberOfStates(int variableColumnIndex) {
        return this.metadata.getNumberOfStates(variableColumnIndex);
    }


    /**
     * Retrieve the column index for each of the given variables.
     *
     * @param variables - the names of the variables to get the column indices for.
     * @return column indices of the given variables.
     */
    public int[] getColumnIndices(Set<String> variables) {
        int[] columnIndices = new int[variables.size()];

        // for loop to get the associated column index for each variable name.
        int insertIndex = 0;
        for (String variable : variables) {
            columnIndices[insertIndex] = this.getColumnIndex(variable);
            insertIndex++;
        }

        return columnIndices;
    }


    /**
     * Retrieve the column index for the given variable.
     *
     * @param variable - the name of the variable to get the column index for.
     * @return column index of the given variable.
     */
    public int getColumnIndex(String variable) {
        return this.metadata.getColumnIndex(variable);
    }
}