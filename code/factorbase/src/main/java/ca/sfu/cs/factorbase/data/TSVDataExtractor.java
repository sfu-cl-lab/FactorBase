package ca.sfu.cs.factorbase.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.util.Mapper;

/**
 * Class to extract information from a TSV based dataset.
 */
public class TSVDataExtractor implements DataExtractor {
    private String sourceFile;
    private String countsColumn;
    private boolean isDiscrete;


    /**
     * Create a data extractor for a TSV based data source.
     *
     * @param sourceFile - the file to extract data from.
     * @param countsColumn - the column that contains the count values for a CT table.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     */
    public TSVDataExtractor(String sourceFile, String countsColumn, boolean isDiscrete) {
        this.sourceFile = sourceFile;
        this.countsColumn = countsColumn;
        this.isDiscrete = isDiscrete;
    }


    /* (non-Javadoc)
     * @see ca.sfu.cs.factorbase.data.DataExtractor#extractData()
     */
    @Override
    public DataSet extractData() throws DataExtractionException {
        try {
            return this.convertDataToStateIndices(this.sourceFile, this.countsColumn, this.isDiscrete);
        } catch (IOException e) {
            throw new DataExtractionException("An error occurred when attempting to extract information from the data source.", e);
        }
    }


    /**
     * Encode all the values in the given dataset into state Integers so that it's quicker to compute a state index
     * for the counts array of any CT table object that gets created by the {@code generateCT()} method.
     *
     * @param sourceFile - path to the TSV file containing the CT table information.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     * @return a 2D Long array representation of the given dataset.
     * @throws IOException if unable to process the given TSV file.
     */
    private DataSet convertDataToStateIndices(
        String sourceFile,
        String countsColumn,
        boolean isDiscrete
    ) throws IOException {
        long[][] convertedData;
        int numberOfRows;
        String[] header;
        int countsColumnIndex;
        Map<String, Integer> variableStateToIntegerEncoding = new HashMap<String, Integer>();
        Map<String, Set<String>> variableStates = new HashMap<String, Set<String>>();

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            numberOfRows = this.getNumberOfRows(sourceFile);
            header = reader.readLine().split("\t");
            int numberOfColumns = header.length;
            countsColumnIndex = this.getCountColumnIndex(header, countsColumn);
            convertedData = new long[numberOfRows][numberOfColumns];

            int[] indexStateCounter = new int[numberOfColumns];
            int rowIndex = 0;
            String row;

            // while loop to process and extract information from the given file.
            while ((row = reader.readLine()) != null) {
                String[] data = row.split("\t");

                // for loop to process the column data for each row.
                for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
                    if (columnIndex == countsColumnIndex) {
                        convertedData[rowIndex][columnIndex] = Long.valueOf(data[countsColumnIndex]);
                    } else {
                        String variableName = header[columnIndex];
                        String state = data[columnIndex];
                        if (!variableStates.containsKey(variableName)) {
                            variableStates.put(variableName, new HashSet<String>());
                        }

                        variableStates.get(variableName).add(state);

                        String stateKey = Mapper.generateVariableStateKey(header[columnIndex], state);

                        Integer stateIndex = variableStateToIntegerEncoding.get(stateKey);
                        if (stateIndex == null) {
                            stateIndex = this.getNextIndex(indexStateCounter, columnIndex);
                            variableStateToIntegerEncoding.put(stateKey, stateIndex);
                        }

                        convertedData[rowIndex][columnIndex] = stateIndex.longValue();
                    }
                }

                rowIndex++;
            }
        }

        DataSetMetaData metadata = new DataSetMetaData(
            Mapper.mapHeadersToColumnIndices(header),
            variableStates,
            numberOfRows,
            header,
            countsColumnIndex
        );

        return new DataSet(convertedData, metadata, isDiscrete);
    }


    /**
     * Retrieve the next state index for the given column.
     *
     * @param indexStateCounter - {@code int[]} where each position holds the next state index to return for the
     *                            associated column.
     * @param columnIndex - the column to get the next state index for.
     * @return the next state index for the given column.
     */
    private int getNextIndex(int[] indexStateCounter, int columnIndex) {
        int nextIndex = indexStateCounter[columnIndex];
        indexStateCounter[columnIndex]++;

        return nextIndex;
    }


    /**
     * Retrieve the number of rows in the given file, excluding the header.
     *
     * Note: The first row is assumed to be a header for the file and is ignored.
     *
     * @param sourceFile - path to the TSV file to retrieve the number of data rows for.
     * @return the number of data rows in the given file or -1 if the file is empty.
     * @throws IOException if unable to process the given file.
     */
    private int getNumberOfRows(String sourceFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            int lineCount = -1;
            while (reader.readLine() != null) {
                lineCount++;
            }

            return lineCount;
        }
    }


    /**
     * Retrieve the column index for the count column.
     *
     * @param header - the dataset column names in the order they appear in the dataset.
     * @param countColumn - the name of the column that contains the count information.
     * @return the index for the count column or -1 if a match isn't found.
     */
    private int getCountColumnIndex(String[] header, String countColumn) {
        for (int index = 0; index < header.length; index++) {
            if (header[index].equals(countColumn)) {
                return index;
            }
        }

        return -1;
    }
}