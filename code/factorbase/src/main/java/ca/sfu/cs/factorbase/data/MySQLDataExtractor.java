package ca.sfu.cs.factorbase.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.util.Mapper;

/**
 * Class to extract information from a MySQL based dataset.
 */
public class MySQLDataExtractor implements DataExtractor {
    private PreparedStatement dbQuery;
    private String countsColumn;
    private boolean isDiscrete;


    /**
     * Create a data extractor for a MySQL based data source.
     *
     * @param dbQuery - {@code PreparedStatement} to generate a {@code ResultSet} containing the information to
     *                  extract.
     * @param countsColumn - the column that contains the count values for a CT table.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     */
    public MySQLDataExtractor(PreparedStatement dbQuery, String countsColumn, boolean isDiscrete) {
        this.dbQuery = dbQuery;
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
            metadata = this.extractMetaData(this.dbQuery, this.countsColumn);
            data = this.convertDataToStateIndices(this.dbQuery, metadata, this.countsColumn);
        } catch (SQLException e) {
            throw new DataExtractionException("An error occurred when attempting to extract information from the data source.", e);
        }

        return new DataSet(data, metadata, this.isDiscrete);
    }


    /**
     * Encode all the values in the given dataset into state Integers so that it's quicker to compute a state index
     * for the counts array of any CT table object that gets created by the {@code generateCT()} method.
     *
     * @param dbQuery - {@code PreparedStatement} to generate a {@code ResultSet} containing the information to
     *                  extract.
     * @param metadata - DataSetMetaData object containing the metadata for the results produced by the given
     *                   {@code PreparedStatement} when executed.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @return a 2D Long array representation of the given dataset.
     * @throws SQLException if there is a problem extracting the information from the database.
     */
    private long[][] convertDataToStateIndices(
        PreparedStatement dbQuery,
        DataSetMetaData metadata,
        String countsColumn
    ) throws SQLException {
        int numberOfRows = metadata.getNumberOfRows();
        int numberOfColumns = metadata.getNumberOfColumns();
        String[] header = metadata.getHeader();
        int countsColumnIndex = metadata.getColumnIndex(countsColumn);

        long[][] convertedData = new long[numberOfRows][numberOfColumns];

        try (ResultSet results = dbQuery.executeQuery()) {
            int rowIndex = 0;

            // while loop to process and extract information from the given ResultSet.
            while (results.next()) {
                // for loop to process the column data for each row.
                for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
                    if (columnIndex == countsColumnIndex) {
                        convertedData[rowIndex][columnIndex] = results.getLong(countsColumnIndex + 1);
                    } else {
                        convertedData[rowIndex][columnIndex] = metadata.getVariableStateIndex(
                            Mapper.generateVariableStateKey(header[columnIndex], results.getString(columnIndex + 1))
                        );
                    }
                }

                rowIndex++;
            }
        }

        return convertedData;
    }


    /**
     * Extract metadata from the for the results produced by the given {@code PreparedStatement} when executed.
     *
     * @param dbQuery - {@code PreparedStatement} to generate a {@code ResultSet} containing the metadata to
     *                  extract.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @return DataSetMetaData object containing the metadata for the given dataset.
     * @throws SQLException if there is a problem extracting the information from the database.
     */
    private DataSetMetaData extractMetaData(PreparedStatement dbQuery, String countsColumn) throws SQLException {
        Map<String, Set<String>> variableStates = new HashMap<String, Set<String>>();
        int numberOfRows = 0;
        int countsColumnIndex = -1;
        String[] header;

        try(ResultSet results = dbQuery.executeQuery()) {
            header = getHeader(results);

            // while loop to process and extract information from the given ResultSet.
            while (results.next()) {
                // for loop to process the data rows.
                for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
                    String variableName = header[columnIndex];
                    String value = results.getString(columnIndex + 1);
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


    /**
     * Extract the column names from the given {@code ResultSet}.
     *
     * @param results - the {@code ResultSet} to extract the column names from.
     * @return Array of the column names for the given {@code ResultSet}.
     * @throws SQLException if there is a problem extracting the information from the database.
     */
    private String[] getHeader(ResultSet results) throws SQLException {
        ResultSetMetaData metadata = results.getMetaData();
        int numberOfColumns = metadata.getColumnCount();
        String[] header = new String[numberOfColumns];

        for (int index = 0; index < numberOfColumns; index++) {
            header[index] = metadata.getColumnLabel(index + 1);
        }

        return header;
    }
}