package ca.sfu.cs.factorbase.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @param variables - the variables to create the CT table for using the dataset given to the ContingencyTableGenerator.
     * @return a CT table for the given variables using the dataset given to the ContingencyTableGenerator.
     */
    public ContingencyTable generateCT(List<String> variables) {
        List<Integer> selectedIndices = variables.stream().map(variable -> this.metadata.getColumnIndex(variable)).sorted().collect(Collectors.toList());
        List<Integer> statesPerVariable = selectedIndices.stream().map(index -> this.metadata.getNumberOfStates(this.metadata.getHeader()[index])).collect(Collectors.toList());
        int totalNumberOfStates = statesPerVariable.stream().reduce(1, Math::multiplyExact);
        long[] countsArray = new long[totalNumberOfStates];

        for (int rowIndex = 0; rowIndex < this.data.length; rowIndex++) {
            List<Long> rowValues = new ArrayList<Long>();
            for (int selectedIndex : selectedIndices) {
                rowValues.add(this.data[rowIndex][selectedIndex]);
            }

            countsArray[this.metadata.generateIndex(statesPerVariable, rowValues)] += this.data[rowIndex][this.metadata.getCountColumnIndex()];
        }

        return new ContingencyTable(countsArray, variables, this.metadata);
    }


    /**
     * Create the Cartesian product of all the states for each random variable in the given list.
     *
     * @param variables - the names of the random variables to create the Cartesian product for their possible states.
     * @return a set of unique lists containing all possible combinations of the given random variables' states.
     */
    public Set<List<RandomVariableAssignment>> getStates(Set<String> variables) {
        return this.metadata.getStates(variables);
    }


    /**
     * Retrieve the possible states that a random variable can take.
     *
     * @param variable - the name of the random variable to retrieve the possible states for.
     * @return a set of the possible states that the given random variable can take.
     */
    public Set<String> getStates(String variable) {
        return this.metadata.getStates(variable);
    }


    /**
     * Retrieve the number of possible states that a random variable can take.
     *
     * @param variable - the name of the random variable to retrieve the number of states for.
     * @return the number of possible states that the given random variable can take.
     */
    public int getNumberOfStates(String variable) {
        return this.metadata.getNumberOfStates(variable);
    }
}