package ca.sfu.cs.factorbase.util;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;

import ca.sfu.cs.common.Configuration.Config;

public class LoggerConfig {
	static String loggingLevel;
	
	/**
	 * Set the global logger level
	 * debug: show all log messages(including debug, info, warning and error messgaes)
	 * info: only show info, warning and error messgaes(no debug message)
	 * off: show no log message
	 * @throws Exception
	 */
	public static void setGlobalLevel() throws Exception{
		getLoggerLevel();		
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		if(loggingLevel.equals("debug")) {
			rootLogger.setLevel(Level.ALL);
			for (Handler h : rootLogger.getHandlers()) {
			    h.setLevel(Level.ALL);
			}
		}
		else if(loggingLevel.equals("info")) {
			rootLogger.setLevel(Level.INFO);
			for (Handler h : rootLogger.getHandlers()) {
			    h.setLevel(Level.INFO);
			}
		}
		else if(loggingLevel.equals("off")){
			rootLogger.setLevel(Level.OFF);
			for (Handler h : rootLogger.getHandlers()) {
			    h.setLevel(Level.OFF);
			}
		}		
	}
	
	/**
	 * get the logger level from config.cfg
	 * @throws Exception
	 */
	private static void getLoggerLevel() throws Exception{
		Config conf = new Config();
		loggingLevel = conf.getProperty("LoggingLevel");
		if(!loggingLevel.equals("debug") && !loggingLevel.equals("info") && !loggingLevel.equals("off")) {
			 throw new IllegalArgumentException("Invlid LoggingLevel setting. Please set the loggingLevel in config.cfg to debug/info/off!");
		}
	}
}