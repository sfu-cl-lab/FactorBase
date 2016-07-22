import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class Config
{
    Properties configFile;
    FileReader fr;
    Reader reader;

    public Config()
    {
        configFile = new java.util.Properties();
        try {
        	//fr = new FileReader("src/config1.cfg"); // June 17, 2014, for FunctorWrapper, NOTE: real data for FunctorWrapper, cross validation: e.g. unielwin_Training1 
        	fr = new FileReader( "cfg/subsetctcomputation.cfg" );  // May 22, 2014 zqian, computing the score for link analysis off.
            reader = new BufferedReader(fr);
            configFile.load( reader );
        }catch(Exception eta){
            eta.printStackTrace();
        }
    }

    public String getProperty(String key)
    {
        return this.configFile.getProperty(key);
    }
    
    public int closeFile()
    {
    	try
    	{
    		reader.close();
    		fr.close();
    	}
    	catch ( IOException e )
    	{
    		System.out.println( "Failed to close file." );
    		e.printStackTrace();
    		return -1;
     	}
    	
    	return 0;
    }
}
