package ca.sfu.cs.factorbase.exporter.csvexporter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.TSVDataExtractor;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

/**
 * zqian@Nov 21, fixed on bug for loading data into csv files (have to close the file)
 * if you do not close it, sometime the program may only load part of the data from resultset into .csv file, and not easy to find it.
 * and thus will cause inconsistent output of Ges Search since the data are different with your expectation.
 *
 * Nov 21 @zqian removing the tuples when mult =0 for false.csv
 * input:  _CT database
 * output: .csv
 * Jun 25 @zqian
 *
 * Suppose that we already finished the computing of all the CT tables given one database,
 * This program will exporting the CT tables into .csv files
 */
public class CSVPrecomputor {

    static Connection con2, con3;

    // To be read from config.
    static String databaseName, databaseName2, databaseName3;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;
    static String isDiscrete;

    static int maxNumberOfMembers = 0;

    private static Logger logger = Logger.getLogger(CSVPrecomputor.class.getName());


    public static Map<String, DataExtractor> runCSV() throws SQLException, IOException {
        initProgram();
        connectDB();

        // Get maxNumberOfMembers, max length of rchain.
        Statement st = con2.createStatement();
        ResultSet rst = st.executeQuery("SELECT MAX(length) FROM lattice_set;");
        rst.absolute(1);
        maxNumberOfMembers = rst.getInt(1);
        rst.close();
        st.close();

        logger.info("##### lattice is ready for use*"); // @zqian

        Map<String, DataExtractor> dataExtractors = Computing_CSV();

        // Disconnect from db.
        disconnectDB();

        return dataExtractors;
    }


    private static Map<String, DataExtractor> Computing_CSV() throws SQLException, IOException {
        long l = System.currentTimeMillis();
        Map<String, DataExtractor> dataExtractors = readPvarFromBN(con2);

        for (int len = 1; len <= maxNumberOfMembers; len++) {
            logger.info("\n processing Rchain.length = " + len); // @zqian
            readRNodesFromLattice(dataExtractors, len);
        }

        long l2 = System.currentTimeMillis();
        logger.info("\n CSVPrecomputor TOTAL Time(ms): " + (l2 - l) + " ms.\n");

        return dataExtractors;
    }


    private static void initProgram() throws IOException, SQLException {
        // Read config file.
        setVarsFromConfig();
        new File(databaseName + File.separator).mkdirs();
        new File(databaseName + File.separator + "csv" + File.separator).mkdirs();
    }


    private static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName = conf.getProperty("dbname");
        databaseName2 = databaseName + "_BN";
        databaseName3 = databaseName + "_CT";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        isDiscrete = conf.getProperty("Continuous");
    }


    private static void connectDB() throws SQLException {
        String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
        con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);

        String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName3;
        con3 = (Connection) DriverManager.getConnection(CONN_STR3, dbUsername, dbPassword);
    }


    private static Map<String, DataExtractor> readPvarFromBN(Connection con2) throws SQLException, IOException {
        Map<String, DataExtractor> dataExtractors = new HashMap<String, DataExtractor>();
        Statement st = con2.createStatement();

        // From main db.
        ResultSet rs = st.executeQuery("SELECT * FROM PVariables WHERE index_number = 0;"); // O.S. March 21 ignore variables that aren't main.
        while(rs.next()) {
            // Get pvid for further use.
            String pvid = rs.getString("pvid");
            String filePath = databaseName + File.separator + "csv" + File.separator + pvid + ".csv";
            logger.fine("pvid : " + pvid);

            // Create new statement.
            Statement st3 = con3.createStatement();

            String queryString= "SELECT * FROM `"+pvid.replace("`", "")+"_counts` WHERE MULT > 0;";
            ResultSet rs4 = st3.executeQuery(queryString);
            logger.fine("query string : "+queryString);

            // Create header.
            ArrayList<String> columns = getColumns(rs4);
            String csvHeader = String.join("\t", columns);
            logger.fine("\nCSV Header : " + csvHeader+ "\n");

            // Create csv file.
            RandomAccessFile csv = new RandomAccessFile(filePath, "rw");
            csv.writeBytes(csvHeader + "\n");

            ResultSet rs5 = st3.executeQuery(queryString);
            while(rs5.next()) {
                String csvString = "";
                for (String col : columns) {
                    csvString += rs5.getString(col) + "\t";
                }

                csvString = csvString.substring(0, csvString.length() - 1);
                csv.writeBytes(csvString + "\n");
            }

            csv.close(); // zqian@Nov 21

            // Close statements.
            st3.close();

            dataExtractors.put(pvid, new TSVDataExtractor(filePath, "MULT", !isDiscrete.equals("1")));
        }

        rs.close();
        st.close();

        return dataExtractors;
    }


    private static void readRNodesFromLattice(Map<String, DataExtractor> dataExtractors, int len) throws SQLException, IOException {
        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT short_rnid AS short_RChain, orig_rnid AS RChain " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = " + len + ";"
        );

        while(rs.next()) {
            // Get the short and full form rnids for further use.
            String rchain = rs.getString("RChain").replace("`", "");
            logger.fine("\n RChain : " + rchain);
            String shortRchain = rs.getString("short_RChain");
            logger.fine(" Short RChain : " + shortRchain);
            String filePath = databaseName + File.separator + "csv" + File.separator + rchain + ".csv";

            // Create new statement.
            Statement st3 = con3.createStatement();

            String queryString = "SELECT * FROM `" + shortRchain.replace("`", "") + "_CT` WHERE MULT > 0;";
            ResultSet rs5 = null;
            try {
                rs5 = st3.executeQuery(queryString);
            } catch ( MySQLSyntaxErrorException e ) {
                // Table doesn't exist.
                st3.close();
                break;
            }

            logger.fine("query string : "+queryString);

            // Create header.
            ArrayList<String> columns = getColumns(rs5);
            String csvHeader = String.join("\t", columns);
            logger.fine("\n CSV Header : " + csvHeader + "\n");

            // Create csv file, reading data from _CT table into .csv file.
            RandomAccessFile csv = new RandomAccessFile(filePath, "rw");
            csv.setLength(0); // File must be cleared before writing.

            csv.writeBytes(csvHeader + "\n");

            ResultSet rs6 = st3.executeQuery(queryString);
            while(rs6.next()) {
                String csvString = "";
                for (String col : columns) {
                    csvString += rs6.getString(col) + "\t";
                }

                csvString = csvString.substring(0, csvString.length() - 1);
                csv.writeBytes(csvString + "\n");
            }

            csv.close(); // zqian@Nov 21
            // Close statements.
            st3.close();

            dataExtractors.put(rchain, new TSVDataExtractor(filePath, "MULT", !isDiscrete.equals("1")));
        }

        rs.close();
        st.close();
    }


    private static ArrayList<String> getColumns(ResultSet rs) throws SQLException {
        ArrayList<String> cols = new ArrayList<String>();
        ResultSetMetaData metaData = rs.getMetaData();
        rs.next();

        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            cols.add(metaData.getColumnLabel(i));
        }

        return cols;
    }


    private static void disconnectDB() throws SQLException {
        con2.close();
        con3.close();
    }
}