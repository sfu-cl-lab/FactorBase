package ca.sfu.cs.factorbase.app;

import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.database.FactorBaseDataBaseInfo;
import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;
import ca.sfu.cs.factorbase.learning.BayesBaseCT_SortMerge;
import ca.sfu.cs.factorbase.learning.BayesBaseH;
import ca.sfu.cs.factorbase.util.LoggerConfig;

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


    public static void main(String[] args) throws Exception {
        // Load configurations and setup the logger.
        long start = System.currentTimeMillis();
        LoggerConfig.setGlobalLevel();
        Config config = new Config();
        long configEnd = System.currentTimeMillis();
        logger.info("Start Program...");
        logRunTime(logger, "Logger + Config Initialization", start, configEnd);

        long databaseStart = System.currentTimeMillis();
        FactorBaseDataBase factorBaseDatabase = new MySQLFactorBaseDataBase(
            new FactorBaseDataBaseInfo(config),
            config.getProperty("dbaddress"),
            config.getProperty("dbname"),
            config.getProperty("dbusername"),
            config.getProperty("dbpassword")
        );
        logRunTime(logger, "Creating Database Connection", databaseStart, System.currentTimeMillis());

        // Generate the initial databases if specified to.
        long setupStart = System.currentTimeMillis();
        if (config.getProperty("AutomaticSetup").equals("1")) {
            factorBaseDatabase.setupDatabase();
        } else {
            logger.info("Databases are assumed to be setup!");
        }
        logRunTime(logger, "Creating Setup Database", setupStart, System.currentTimeMillis());

        // Learn a Bayesian Network.
        boolean usePreCounting = config.getProperty("PreCounting").equals("1");
        if (usePreCounting) {
            long buildCTStart = System.currentTimeMillis();
            BayesBaseCT_SortMerge.buildCT();
            logRunTime(logger, "Creating CT Tables", buildCTStart, System.currentTimeMillis());
        }

        long bayesBaseHStart = System.currentTimeMillis();
        BayesBaseH.runBBH(factorBaseDatabase);
        logRunTime(logger, "Running BayesBaseH", bayesBaseHStart, System.currentTimeMillis());

        // Now eliminate temporary tables. Keep only the tables for the longest Rchain. Turn this off for debugging.
        long cleanupStart = System.currentTimeMillis();
        factorBaseDatabase.cleanupDatabase();
        logRunTime(logger, "Cleanup Database", cleanupStart, System.currentTimeMillis());

        logRunTime(logger, "Total", start, System.currentTimeMillis());

        logger.info("Program Done!");
    }


    /**
     * Helper method to write out the run times in a consistent format.
     * @param logger - the logger to write the runtime to.
     * @param stage - the part of the FactorBase program that was run.
     * @param start - the start time for the given stage (ms).
     * @param end - the end time for the given stage (ms).
     */
    private static void logRunTime(Logger logger, String stage, long start, long end) {
        logger.info("Runtime[" + stage + "]: " + String.valueOf(end - start) + "ms.");
    }
}