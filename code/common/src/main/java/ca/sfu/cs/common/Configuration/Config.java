package ca.sfu.cs.common.Configuration;

import java.io.FileReader;
import java.util.Properties;

public class Config {
    public static final String SCRIPTS_DIRECTORY = "scripts/";
    public static final String DEFAULT_CONFIGFILE = "config.cfg";

    Properties configFile;
    
    public Config() {
        configFile = new java.util.Properties();

        String config = System.getProperty("config");
        if (config == null) {
            config = DEFAULT_CONFIGFILE;
        }

        try (FileReader fileReader = new FileReader(config)) {
            configFile.load(fileReader);
        } catch(Exception eta) {
            eta.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return this.configFile.getProperty(key);
    }
}