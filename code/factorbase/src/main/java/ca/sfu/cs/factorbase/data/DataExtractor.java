package ca.sfu.cs.factorbase.data;

import ca.sfu.cs.factorbase.exception.DataExtractionException;

/**
 * Methods expected to be implemented to enable the extraction of data from a data source.
 */
public interface DataExtractor {
    DataSet extractData() throws DataExtractionException;
}