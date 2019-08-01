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
        DataSetMetaData metadata;
        long[][] data;
        try {
            metadata = this.extractMetaData(this.sourceFile, this.countsColumn);
            data = this.convertDataToStateIndices(this.sourceFile, metadata, this.countsColumn);
        } catch (IOException e) {
            throw new DataExtractionException("An error occurred when attempting to extract information from the data source.", e);
        }

        return new DataSet(data, metadata, this.isDiscrete);
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
                        convertedData[rowIndex][columnIndex] = metadata.getVariableStateIndex(
                            Mapper.generateVariableStateKey(header[columnIndex], data[columnIndex])
                        );
                    }
                }

                rowIndex++;
            }
        }

        return convertedData;
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

        Map<String, Integer> variableNameToColumnIndex = Mapper.mapHeadersToColumnIndices(header);
        Map<String, Integer> variableStateToIntegerEncoding = Mapper.mapVariableStateToInteger(variableStates);

        return new DataSetMetaData(variableNameToColumnIndex, variableStateToIntegerEncoding, variableStates, numberOfRows, header, countsColumnIndex);
    }
}