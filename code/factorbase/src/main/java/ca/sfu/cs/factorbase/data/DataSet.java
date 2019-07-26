package ca.sfu.cs.factorbase.data;

/**
 * Class to hold information from a data source.
 */
public class DataSet {
    private boolean isDiscrete;
    private long[][] data;
    private DataSetMetaData metadata;


    /**
     * Create a Java representation of a dataset, which consists of data and metadata.
     *
     * @param data - a representation of the dataset where each state has been converted into an indexed value.
     * @param metadata - the associated metadata for the given {@code data}.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     */
    public DataSet(long[][] data, DataSetMetaData metadata, boolean isDiscrete) {
        this.data = data;
        this.metadata = metadata;
        this.isDiscrete = isDiscrete;
    }


    /**
     * Retrieve the data for the dataset.
     *
     * @return a 2D array representation of the dataset where each state has been converted into an indexed value.
     */
    public long[][] getData() {
        return this.data;
    }


    /**
     * The metadata for the associated 2D array representation of the dataset.
     *
     * @return the metadata for the dataset.
     */
    public DataSetMetaData getMetaData() {
        return this.metadata;
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
}