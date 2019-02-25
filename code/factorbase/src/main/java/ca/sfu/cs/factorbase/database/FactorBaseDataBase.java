package ca.sfu.cs.factorbase.database;

import ca.sfu.cs.factorbase.exception.DataBaseException;

/**
 * Methods expected to be implemented to enable the extraction of data from a database for FactorBase.
 */
public interface FactorBaseDataBase {
    /**
     * This method should setup all the extra tables required for FactorBase to learn a Bayesian
     * network for the provided database.
     *
     * @throws DataBaseException if an error occurs when attempting to access the database.
     */
    void setupDatabase() throws DataBaseException;
}
