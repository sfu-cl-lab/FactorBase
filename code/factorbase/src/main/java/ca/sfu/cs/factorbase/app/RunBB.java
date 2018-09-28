package ca.sfu.cs.factorbase.app;

import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.exporter.csvexporter.CSVPrecomputor;
import ca.sfu.cs.factorbase.tables.BayesBaseCT_SortMerge;
import ca.sfu.cs.factorbase.tables.BayesBaseH;
import ca.sfu.cs.factorbase.tables.KeepTablesOnly;
import ca.sfu.cs.factorbase.tables.MakeSetup;
import ca.sfu.cs.factorbase.util.LoggerConfig;

/* July 3rd, 2014, zqian
 * input: 
 * @database@ (original data based on ER diagram)
 * output: 
 * @database@_BN (e.g. _CP, Path_BayesNets, Score)
 * 			@database@_CT (e.g. BiggestRchain_CT)
 * 			@database@_setup (preconditions for learning)
 * 
 * */
public class RunBB {
	static String isAutomaticSetup;
	private static Logger logger = Logger.getLogger(RunBB.class.getName());
	
	public static void main(String[] args) throws Exception {
		LoggerConfig.setGlobalLevel();
		long t1 = System.currentTimeMillis(); 
		logger.info("Start Program...");
		setVarsFromConfig();
		if (isAutomaticSetup.equals("1")) {
			MakeSetup.runMS();
			logger.info("Setup database is ready.");
		} else {
			logger.info("Setup database exists.");
		}
		runBBLearner();
		
		long t2 = System.currentTimeMillis(); 
		logger.info("Total Running time is " + (t2-t1) + "ms.");
	}
	
	
	public static void setVarsFromConfig(){
		Config conf = new Config();
		//1: run Setup; 0: not run
		isAutomaticSetup = conf.getProperty("AutomaticSetup");
	}
	
	public static void runBBLearner() throws Exception {
		
		//assumes that dbname is in config file and that dbname_setup exists.

		BayesBaseCT_SortMerge.buildCT();
		logger.info("The CT database is ready for use.");
		logger.info("*********************************************************");
		CSVPrecomputor.runCSV();
		logger.info("CSV files are generated.");
		logger.info("*********************************************************");
		BayesBaseH.runBBH();
		logger.info("\nFinish running BayesBaseH.");
		logger.info("*********************************************************");
		logger.info("Cleaning CT database");
		//Now eliminate temporary tables. Keep only the tables for the longest Rchain. Turn this off for debugging.//
		KeepTablesOnly.Drop_tmpTables();

	}

}
