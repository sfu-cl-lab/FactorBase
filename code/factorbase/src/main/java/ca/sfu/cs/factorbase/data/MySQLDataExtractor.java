package ca.sfu.cs.factorbase.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        try {
            return this.convertDataToStateIndices(this.dbQuery, this.countsColumn, this.isDiscrete);
        } catch (SQLException e) {
            throw new DataExtractionException("An error occurred when attempting to extract information from the data source.", e);
        }
    }


    /**
     * Encode all the values in the given dataset into state Integers so that it's quicker to compute a state index
     * for the counts array of any CT table object that gets created by the {@code generateCT()} method.
     *
     * @param dbQuery - {@code PreparedStatement} to generate a {@code ResultSet} containing the information to
     *                  extract.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     * @return a 2D Long array representation of the given dataset.
     * @throws SQLException if there is a problem extracting the information from the database.
     */
    private DataSet convertDataToStateIndices(
        PreparedStatement dbQuery,
        String countsColumn,
        boolean isDiscrete
    ) throws SQLException {
        long[][] convertedData;
        int numberOfRows;
        String[] header;
        int countsColumnIndex;
        Map<String, Integer> variableStateToIntegerEncoding = new HashMap<String, Integer>();
        List<Set<String>> variableStates = new ArrayList<Set<String>>();

        try (ResultSet results = dbQuery.executeQuery()) {
            numberOfRows = this.getNumberOfRows(results);
            header = this.getHeader(results);
            int numberOfColumns = header.length;
            countsColumnIndex = this.getCountColumnIndex(header, countsColumn);
            convertedData = new long[numberOfRows][numberOfColumns];

            // for loop to create a HashSet to store the unique states for each column except the counts column.
            for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
                if (columnIndex == countsColumnIndex) {
                    variableStates.add(null);
                } else {
                    variableStates.add(new HashSet<String>());
                }
            }

            int[] indexStateCounter = new int[numberOfColumns];
            int rowIndex = 0;

            // while loop to process and extract information from the given ResultSet.
            while (results.next()) {
                // for loop to process the column data for each row.
                for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
                    if (columnIndex == countsColumnIndex) {
                        convertedData[rowIndex][columnIndex] = results.getLong(countsColumnIndex + 1);
                    } else {
                        String state = results.getString(columnIndex + 1);
                        variableStates.get(columnIndex).add(state);

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
        } finally {
            dbQuery.close();
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
     * Extract the column names from the given {@code ResultSet}.
     *
     * @param results - the {@code ResultSet} to extract the column names from.
     * @return the column names for the given {@code ResultSet}.
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


    /**
     * Retrieve the number of rows in the given {@code ResultSet}.
     *
     * @param results - {@code ResultSet} to retrieve the number of rows for.
     * @return the number of rows in the given {@code ResultSet}.
     * @throws SQLException if there is a problem extracting the information from the database.
     */
    private int getNumberOfRows(ResultSet results) throws SQLException {
        int rowCount = 0;

        if (results.last()) {
            rowCount = results.getRow();
            results.beforeFirst();
        }

        return rowCount;
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