package testframework;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Properties;

public class TestConfigFile {
	public static final String tempFileName = "config_temp.cfg";
	
	public File file;
	
	/**
	 * generate the temporary configuration file config_temp.cfg properties
	 * @param properties
	 * @throws IOException
	 */
	public TestConfigFile(Properties properties) throws IOException{
		file = new File(tempFileName);
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		BufferedWriter writter = new BufferedWriter(new FileWriter(file, true));
		properties.store(writter, "");
		writter.close();
	}
	/**
	 * set the 
	 * @param property - the property key name to set
	 * @param value - the property value to set
	 * @throws IOException
	 */
	public void setPropertyValue(String property, String value) throws IOException{
		Properties properties = new Properties();
		
		Reader reader = new BufferedReader(new FileReader(file));
		BufferedWriter writter = new BufferedWriter(new FileWriter(file));
		
		properties.load(reader);		
		properties.setProperty(property, value);
		properties.store(writter, "");
		
		reader.close();
		writter.close();
	}
}
