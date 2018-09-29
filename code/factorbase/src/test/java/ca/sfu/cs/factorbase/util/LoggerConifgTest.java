package ca.sfu.cs.factorbase.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;

import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import java.io.IOException;

import testframework.TestConfigFile ;;

public class LoggerConifgTest {
	private static Logger rootLogger;
	private static TestConfigFile conf;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException{
		Properties properties = new Properties();
		properties.setProperty("LoggingLevel", "");
		conf = new TestConfigFile(properties);
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		conf.file.delete();
		conf = null;
	}
	
	@Test
	public void setGlobalLevel_setSuccessful() throws Exception{
		System.setProperty("config", conf.file.getName());
		
		rootLogger = LogManager.getLogManager().getLogger("");		
		Map<String, Level> levelMap = LoggerConfig.levelMap;
		Iterator<String> LoggingLevelItr = levelMap.keySet().iterator();
		
		// Set the LoggingLevel to different Level to see if the result of rootLogger and handler is the same as we desired
		String LoggingLevel;
		Level currentLevel;
		Level expectedLevel;
		while(LoggingLevelItr.hasNext()) {
			LoggingLevel = LoggingLevelItr.next();
			expectedLevel = levelMap.get(LoggingLevel);
			
			conf.setPropertyValue("LoggingLevel", LoggingLevel);
			LoggerConfig.setGlobalLevel();
			currentLevel = rootLogger.getLevel();
			assertThat(currentLevel, equalTo(expectedLevel));
			
			for(Handler h: rootLogger.getHandlers()) {
				currentLevel = h.getLevel();
				assertThat(currentLevel, equalTo(expectedLevel));
			}
		}
	}
}