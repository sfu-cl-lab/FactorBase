package ca.sfu.cs.factorbase.exporter.bifexporter.bif;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import nu.xom.ParsingException;

public class BIFImport {

    /**
     * Import a file into the database by getting all the pairs of links in the file and then writing them out
     * Links in the file showing no parent are written with an emtpy string parent to the database
     */
    private static Logger logger = Logger.getLogger(BIFImport.class.getName());

    public static void Import(String filename, String id, String tableName, Connection con) throws IOException, SQLException, ParsingException {
        ArrayList<String[]> pairs = BIF_IO.getLinksFromFile(filename);
        for (String[] pair : pairs) {
            Statement st = con.createStatement();
            logger.fine("INSERT ignore INTO " + tableName + " VALUES (\'" + id + "\', \'" + pair[1] + "\', \'" + pair[0] + "\');");
            st.execute("INSERT ignore INTO " + tableName + " VALUES (\'" + id + "\', \'" + pair[1] + "\', \'" + pair[0] + "\');");
        }
    }
}