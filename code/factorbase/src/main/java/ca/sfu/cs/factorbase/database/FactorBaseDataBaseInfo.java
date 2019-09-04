package ca.sfu.cs.factorbase.database;

import ca.sfu.cs.common.Configuration.Config;

/**
 * Class to store and pass database information related to FactorBase.
 */
public class FactorBaseDataBaseInfo {
    private String bnDatabaseName;
    private String ctDatabaseName;
    private String setupDatabaseName;
    private String countColumnName;
    private boolean isDiscrete;

    /**
     * Extract the database information related to FactorBase.
     *
     * @param config - configuration information used to help determine database information related to FactorBase.
     */
    public FactorBaseDataBaseInfo(Config config) {
        String dbName = config.getProperty("dbname");
        this.bnDatabaseName = dbName + "_BN";
        this.ctDatabaseName = dbName + "_CT";
        this.setupDatabaseName = dbName + "_setup";
        this.countColumnName = "MULT";
        this.isDiscrete = !config.getProperty("Continuous").equals("1");
    }


    /**
     * Retrieve the name of the BN database for FactorBase.
     *
     * @return the name of the BN database.
     */
    public String getBNDatabaseName() {
        return this.bnDatabaseName;
    }


    /**
     * Retrieve the name of the CT database for FactorBase.
     *
     * @return the name of the CT database.
     */
    public String getCTDatabaseName() {
        return this.ctDatabaseName;
    }


    /**
     * Retrieve the name of the setup database for FactorBase.
     *
     * @return the name of the setup database.
     */
    public String getSetupDatabaseName() {
        return this.setupDatabaseName;
    }


    /**
     * Retrieve the name of the column containing the counts information.
     *
     * @return the name of the column containing the counts information.
     */
    public String getCountColumnName() {
        return this.countColumnName;
    }


    /**
     * Indicates if the dataset only contains discrete data.
     *
     * @return true iff the dataset only contains discrete data, otherwise false.
     */
    public boolean isDiscrete() {
        return this.isDiscrete;
    }
}