package ca.sfu.cs.factorbase.util;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;

import ca.sfu.cs.common.Configuration.Config;

public class LoggerConfig {
    /**
     * Set the global logger level.
     * debug: show all log messages (including debug, info, warning and error messages).
     * info: only show info, warning and error messages (no debug message).
     * off: show no log message.
     *
     * @throws IllegalArgumentException if the specified value is not valid.
     */
    public static Map<String, Level> levelMap;

    public static void setGlobalLevel() throws IllegalArgumentException {
        buildLevelMap();
        Level loggerLevel = getLevelFromConfig();
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(loggerLevel);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(loggerLevel);
        }
    }

    /**
     * Build the Level table to map the LoggingLevel specified in the config file to the Level of JUL.
     */
    public static void buildLevelMap() {
        levelMap = new HashMap<String, Level>();
        levelMap.put("debug", Level.ALL);
        levelMap.put("info", Level.INFO);
        levelMap.put("off", Level.OFF);
    }

    /**
     * Get the logger level from config.cfg.
     *
     * @return the specified Level.
     * @throws IllegalArgumentException if the specified value is not valid.
     */
    private static Level getLevelFromConfig() throws IllegalArgumentException {
        String loggingLevel;
        Config conf = new Config();
        loggingLevel = conf.getProperty("LoggingLevel");

        if (levelMap.get(loggingLevel) == null) {
            throw new IllegalArgumentException("Invalid LoggingLevel setting. Please set the loggingLevel in config.cfg to debug/info/off!");
        }

        return levelMap.get(loggingLevel);
    }
}