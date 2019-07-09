package ca.sfu.cs.factorbase.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Class to generate CT tables from various data sources.
 */
public class ContingencyTableGenerator {

    private boolean isDiscrete;
    private long[][] data;
    private DataSetMetaData metadata;


    /**
     * Generates contingency tables based on a TSV data source.
     *
     * @param sourceFile - path to the TSV file containing the CT table information.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     * @throws IOException if unable to process the given TSV file.
     */
    public ContingencyTableGenerator (String sourceFile, String countsColumn, boolean isDiscrete) throws IOException {
        DataSetMetaData metadata = extractMetaData(sourceFile, countsColumn);
        this.data = convertDataToStateIndices(sourceFile, metadata, countsColumn);
        this.metadata = metadata;
        this.isDiscrete = isDiscrete;
    }


    /**
     * Extract metadata from the given TSV file.
     *
     * @param sourceFile - path to the TSV file containing the CT table information.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @return DataSetMetaData object containing the metadata for the given dataset.
     * @throws IOException if unable to process the given TSV file.
     */
    private DataSetMetaData extractMetaData(String sourceFile, String countsColumn) throws IOException {
        Map<String, Set<String>> variableStates = new HashMap<String, Set<String>>();
        String[] header;
        int numberOfRows = 0;
        int countsColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            // Extract header values.
            header = reader.readLine().split("\t");
            String row;

            // while loop to process and extract information from the given file.
            while ((row = reader.readLine()) != null) {
                String[] data = row.split("\t");

                // for loop to process the data rows.
                for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
                    String variableName = header[columnIndex];
                    String value = data[columnIndex];
                    if (!variableName.equals(countsColumn)) {
                        if (!variableStates.containsKey(variableName)) {
                            variableStates.put(variableName, new HashSet<String>());
                        }

                        variableStates.get(variableName).add(value);
                    } else {
                        countsColumnIndex = columnIndex;
                    }
                }

                numberOfRows++;
            }
        }

        Map<String, Integer> variableNameToColumnIndex = mapHeadersToColumnIndices(header);
        Map<String, Integer> variableStateToIntegerEncoding = mapVariableStateToInteger(variableStates);

        return new DataSetMetaData(variableNameToColumnIndex, variableStateToIntegerEncoding, variableStates, numberOfRows, header, countsColumnIndex);
    }


    /**
     * Generate a Map that has key:value pairs of column-name:column-index.
     *
     * @param header - header from the dataset.
     * @return Map object that maps each column name to its associated column index.
     */
    private Map<String, Integer> mapHeadersToColumnIndices(String[] header) {
        Map<String, Integer> headerToColumnIndex = new HashMap<String, Integer>();

        // for loop to create a Map that has key:value pairs of column-name:column-index.
        for (int index = 0; index < header.length; index++) {
            headerToColumnIndex.put(header[index], index);
        }

        return headerToColumnIndex;
    }


    /**
     * Generate a Map that has key:value pairs of variable-assignment:integer-encoding.
     *
     * @param variableStates - a Map containing key:value pairs of variable:possible-states.
     * @return Map object that maps each variable assignment to an integer value.
     */
    private Map<String, Integer> mapVariableStateToInteger(Map<String, Set<String>> variableStates) {
        Map<String, Integer> variableStateToInteger = new HashMap<String, Integer>();
        StringBuilder builder;

        // for loop to process each variable.
        for (String variable : variableStates.keySet()) {
            int stateIndex = 0;
            builder = new StringBuilder();

            // for loop to create an integer encoding for each possible assignment of the current variable.
            for (String state : variableStates.get(variable)) {
                builder.setLength(0);
                builder.append(variable);
                builder.append(" = ");
                builder.append(state);
                variableStateToInteger.put(builder.toString(), stateIndex);
                stateIndex++;
            }
        }

        return variableStateToInteger;
    }


    /**
     * Encode all the values in the given dataset into state Integers so that it's quicker to compute a state index
     * for the counts array of any CT table object that gets created by the {@code generateCT()} method.
     *
     * @param sourceFile - path to the TSV file containing the CT table information.
     * @param metadata - DataSetMetaData object containing the metadata for the given TSV file.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @return a 2D Long array representation of the given dataset.
     * @throws IOException if unable to process the given TSV file.
     */
    private long[][] convertDataToStateIndices(String sourceFile, DataSetMetaData metadata, String countsColumn) throws IOException {
        int numberOfRows = metadata.getNumberOfRows();
        int numberOfColumns = metadata.getNumberOfColumns();
        String[] header = metadata.getHeader();
        int countsColumnIndex = metadata.getColumnIndex(countsColumn);

        long[][] convertedData = new long[numberOfRows][numberOfColumns];

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            // Skip header values; use the ones from the given DataSetMetaData object.
            reader.readLine();
            String row;

            int rowIndex = 0;

            // while loop to process and extract information from the given file.
            while ((row = reader.readLine()) != null) {
                String[] data = row.split("\t");

                // for loop to process the column data for each row.
                for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
                    if (columnIndex == countsColumnIndex) {
                        convertedData[rowIndex][columnIndex] = Long.valueOf(data[countsColumnIndex]);
                    } else {
                        convertedData[rowIndex][columnIndex] = metadata.getVariableStateIndex(header[columnIndex] + " = " + data[columnIndex]);
                    }
                }

                rowIndex++;
            }
        }

        return convertedData;
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
    public Set<String> getVariableNames() {
        return this.metadata.getVariableNames();
    }


    /**
     * Generate a CT table for the given variables using the dataset given to the ContingencyTableGenerator.
     *
     * @param variables - the variables to create the CT table for using the dataset given to the ContingencyTableGenerator.
     * @return a CT table for the given variables using the dataset given to the ContingencyTableGenerator.
     */
    public ContingencyTable generateCT(List<String> variables) {
        List<Integer> selectedIndices = variables.stream().map(variable -> this.metadata.getColumnIndex(variable)).collect(Collectors.toList());
        List<Integer> statesPerVariable = variables.stream().map(variable -> this.metadata.getNumberOfStates(variable)).collect(Collectors.toList());
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