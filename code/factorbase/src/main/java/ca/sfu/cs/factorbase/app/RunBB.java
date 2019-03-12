package ca.sfu.cs.factorbase.app;

import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.database.MySQLFactorBaseDataBase;
import ca.sfu.cs.factorbase.exporter.csvexporter.CSVPrecomputor;
import ca.sfu.cs.factorbase.tables.BayesBaseCT_SortMerge;
import ca.sfu.cs.factorbase.tables.BayesBaseH;
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
        long t1 = System.currentTimeMillis();
        LoggerConfig.setGlobalLevel();
        Config config = new Config();
        logger.info("Start Program...");

        FactorBaseDataBase factorBaseDatabase = new MySQLFactorBaseDataBase(
            config.getProperty("dbaddress"),
            config.getProperty("dbname"),
            config.getProperty("dbusername"),
            config.getProperty("dbpassword")
        );

        // Generate the setup database if specified to.
        if (config.getProperty("AutomaticSetup").equals("1")) {
            factorBaseDatabase.setupDatabase();
            logger.info("Setup database is ready.");
        } else {
            logger.info("Setup database exists.");
        }

        // Learn a Bayesian Network.
        BayesBaseCT_SortMerge.buildCT();
        logger.info("The CT database is ready for use.");
        logger.info("*********************************************************");
        CSVPrecomputor.runCSV();
        logger.info("CSV files are generated.");
        logger.info("*********************************************************");
        BayesBaseH.runBBH(factorBaseDatabase);
        logger.info("\nFinish running BayesBaseH.");
        logger.info("*********************************************************");

        // Now eliminate temporary tables. Keep only the tables for the longest Rchain. Turn this off for debugging.
        logger.info("Cleaning CT database");
        factorBaseDatabase.cleanupDatabase();

        long t2 = System.currentTimeMillis();
        logger.info("Total Running time is " + (t2 - t1) + "ms.");
    }
}