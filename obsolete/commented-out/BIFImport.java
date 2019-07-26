package ca.sfu.cs.factorbase.exporter.bifexporter.bif;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import nu.xom.ParsingException;

public class BIFImport {

	/*
	 * Arguments:
	 * 	1st: filename: The name of the file to import (include directory information if appropriate)
	 * 	2nd: database name: The name of the database to access. It is assumed that the we wish to insert into the
	 * 			Entity_BayesNets table
	 */
	/*public static void main(String[] args) {
		if (args.length != 2) {
			logger.fine("Arguments incorrect");
			logger.fine("Expected filename database_name");
			return;
		}
		try {
			Import(args[0], args[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		
		logger.fine("Exited Normally");
	}*/
	
	/*
	 * Import a file into the database by getting all the pairs of links in the file and then writing them out
	 * Links in the file showing no parent are written with an emtpy string parent to the database
	 */
	private static Logger logger = Logger.getLogger(BIFImport.class.getName());
	
	public static void Import(String filename, String id, String tableName, Connection con) throws IOException, SQLException, ParsingException {
		ArrayList<String[]> pairs = BIF_IO.getLinksFromFile(filename);
		
		//System.out.print(id);
		for (String[] pair : pairs) {
            Statement st = con.createStatement();
            
            logger.fine("INSERT ignore INTO " + tableName + " VALUES (\'" + id + "\', \'" + pair[1] + "\', \'" + pair[0] + "\');");
            st.execute("INSERT ignore INTO " + tableName + " VALUES (\'" + id + "\', \'" + pair[1] + "\', \'" + pair[0] + "\');");
            
		}			
	}	
}


/*
 *BIFImport.Import("output" + File.separator + "xml" + File.separator + id.replace("`","") + ".xml", id, "Entity_BayesNets", con2); 
 */