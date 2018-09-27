package ca.sfu.cs.factorbase.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.ArrayList;

public class LoggerConifgTest {
	private static Level defaultRootLevel;
	private static Level defaultHandlerLevel;

	@ Before
	public void getDefaultLoggerLevel() {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		defaultRootLevel = rootLogger.getLevel();
		for(Handler h: rootLogger.getHandlers()) {
			defaultHandlerLevel = h.getLevel();
		}
	}
	@ After
	public void restoreDefaultLoggerLevel() {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(defaultRootLevel);
		for(Handler h: rootLogger.getHandlers()) {
			h.setLevel(defaultHandlerLevel);
		}
	}
	@ Test
	public void setGlobalLevel_setSuccessful(){
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		
		Level currentLevel;
		ArrayList<Level> expectedLevels = new ArrayList<Level>();
		expectedLevels.add(Level.ALL);
		expectedLevels.add(Level.INFO);
		expectedLevels.add(Level.OFF);
		
		for(Level expectedLevel: expectedLevels) {
			LoggerConfig.setLevel(expectedLevel);
			LoggerConfig.setGlobalLevel();
			
			currentLevel = rootLogger.getLevel();
			Assert.assertEquals(currentLevel, expectedLevel);
			
			for(Handler h: rootLogger.getHandlers()) {
				currentLevel = h.getLevel();
				Assert.assertEquals(currentLevel, expectedLevel);
			}
		}
	}
}