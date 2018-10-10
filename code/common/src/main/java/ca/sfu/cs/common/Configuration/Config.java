package ca.sfu.cs.common.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    public static final String SCRIPTS_DIRECTORY = "scripts/";
    public static final String DEFAULT_CONFIGFILE = "config.cfg";

    Properties configFile;
    FileReader fr;
    Reader reader;
    
    private static Logger logger = Logger.getLogger(Config.class.getName());

    public Config() {
        configFile = new java.util.Properties();

        String config = System.getProperty("config");
        if (config == null) {
            config = DEFAULT_CONFIGFILE;
        }

        try {
            fr = new FileReader(config);
            reader = new BufferedReader(fr);
            configFile.load(reader);
        } catch(Exception eta) {
            eta.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return this.configFile.getProperty(key);
    }

    public int closeFile() {
        try {
            reader.close();
            fr.close();
        } catch (IOException e) {
            logger.severe("Failed to close file.");
            e.printStackTrace();
            return -1;
        }

        return 0;
    }
}