package ca.sfu.cs.factorbase.app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.database.FactorBaseDataBaseInfo;
import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.exception.ScoringException;
import ca.sfu.cs.factorbase.lattice.RelationshipLattice;
import ca.sfu.cs.factorbase.learning.BayesBaseH;
import ca.sfu.cs.factorbase.learning.CountingStrategy;
import ca.sfu.cs.factorbase.learning.CountsManager;
import ca.sfu.cs.factorbase.util.LoggerConfig;
import ca.sfu.cs.factorbase.util.RuntimeLogger;

import nu.xom.ParsingException;

/**
 * July 3rd, 2014, zqian
 * input:
 * @database@ (original data based on ER diagram)
 * output:
 * @database@_BN (e.g. _CP, Path_BayesNets, Score)
 * @database@_CT (e.g. BiggestRchain_CT)
 * @database@_setup (preconditions for learning)
 */
public class RunBB {
    private static Logger logger = Logger.getLogger(RunBB.class.getName());


    public static void main(
        String[] args
    ) throws DataBaseException, SQLException, IOException, DataExtractionException, ParsingException, ScoringException {
        // Load configurations and setup the logger.
        long start = System.currentTimeMillis();
        LoggerConfig.setGlobalLevel();
        Config config = new Config();
        long configEnd = System.currentTimeMillis();
        logger.info("Start Program...");

        logger.info("Input Database: " + config.getProperty("dbname"));
        CountingStrategy countingStrategy = CountingStrategy.determineStrategy(
            config.getProperty("CountingStrategy")
        );
        logger.info("Counting Strategy: " + countingStrategy);

        RuntimeLogger.logRunTime(logger, "Logger + Config Initialization", start, configEnd);

        long databaseStart = System.currentTimeMillis();
        String databaseCollation = config.getProperty("dbcollation");
        FactorBaseDataBase factorBaseDatabase = new MySQLFactorBaseDataBase(
            new FactorBaseDataBaseInfo(config),
            config.getProperty("dbaddress"),
            config.getProperty("dbname"),
            config.getProperty("dbusername"),
            config.getProperty("dbpassword"),
            databaseCollation,
            countingStrategy
        );
        RuntimeLogger.logRunTime(logger, "Creating Database Connection", databaseStart, System.currentTimeMillis());

        // Generate the initial databases if specified to.
        long setupStart = System.currentTimeMillis();
        if (config.getProperty("AutomaticSetup").equals("1")) {
            factorBaseDatabase.setupDatabase();
        } else {
            logger.info("Databases are assumed to be setup!");
        }
        CountsManager.connectDB();
        RuntimeLogger.logRunTime(logger, "Creating Setup Database", setupStart, System.currentTimeMillis());

        // Generate the relationship lattice to guide the structure learning search.
        long globalLatticeStart = System.currentTimeMillis();
        RelationshipLattice globalLattice = factorBaseDatabase.getGlobalLattice();
        RuntimeLogger.logRunTime(logger, "Creating Global Lattice", globalLatticeStart, System.currentTimeMillis());

        // Learn a Bayesian Network.
        if (countingStrategy.isPrecount()) {
            long buildCTStart = System.currentTimeMillis();
            CountsManager.buildCT(countingStrategy);
            RuntimeLogger.logRunTime(logger, "Creating CT Tables", buildCTStart, System.currentTimeMillis());
        } else if (countingStrategy.isHybrid()) {
            long buildGlobalCountsStart = System.currentTimeMillis();
            CountsManager.buildRChainsGlobalCounts();
            RuntimeLogger.logRunTime(logger, "Creating Global Counts Tables", buildGlobalCountsStart, System.currentTimeMillis());
        }

        long bayesBaseHStart = System.currentTimeMillis();
        BayesBaseH.runBBH(
            factorBaseDatabase,
            globalLattice,
            databaseCollation,
            countingStrategy
        );
        RuntimeLogger.logRunTime(logger, "Running BayesBaseH", bayesBaseHStart, System.currentTimeMillis());

        // Now eliminate temporary tables. Keep only the tables for the longest Rchain. Turn this off for debugging.
        long cleanupStart = System.currentTimeMillis();
        factorBaseDatabase.cleanupDatabase();
        RuntimeLogger.logRunTime(logger, "Cleanup Database", cleanupStart, System.currentTimeMillis());

        // Disconnect from the database.
        CountsManager.disconnectDB();
        factorBaseDatabase.disconnect();

        RuntimeLogger.logRunTime(logger, "Total", start, System.currentTimeMillis());

        logger.info("Program Done!");
    }
}