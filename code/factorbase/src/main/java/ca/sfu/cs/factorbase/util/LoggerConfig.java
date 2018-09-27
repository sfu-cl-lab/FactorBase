package ca.sfu.cs.factorbase.util;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;

import ca.sfu.cs.common.Configuration.Config;

public class LoggerConfig {
	private static Level loggerLevel;
	
	/**
	 * Set the global logger level
	 * debug: show all log messages(including debug, info, warning and error messages)
	 * info: only show info, warning and error messages(no debug message)
	 * off: show no log message
	 */
	public static void setGlobalLevel(){		
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		
		rootLogger.setLevel(loggerLevel);
		for (Handler h : rootLogger.getHandlers()) {
		    h.setLevel(loggerLevel);
		}
	}
	
	/**
	 * get the logger level from config.cfg
	 * @return the specified Level
	 * @throws Exception if the specified value is not valid
	 */
	public static void getLevelFromConfig() throws Exception{
		String loggingLevel;
		Config conf = new Config();
		loggingLevel = conf.getProperty("LoggingLevel");
		
		Map<String, Level> levelTable = new HashMap<String, Level>();
		levelTable.put("debug", Level.ALL);
		levelTable.put("info", Level.INFO);
		levelTable.put("off", Level.OFF);
		
		if(levelTable.get(loggingLevel) == null) {
			 throw new IllegalArgumentException("Invlid LoggingLevel setting. Please set the loggingLevel in config.cfg to debug/info/off!");
		}
		else
			loggerLevel = levelTable.get(loggingLevel);
	}
	
	/**
	 * set the global level, for test usage
	 * @param level: the logger level expected to be set
	 */
	public static void setLevel(Level level) {
		loggerLevel = level;
	}
}