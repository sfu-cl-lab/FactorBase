package ca.sfu.cs.factorbase.tables;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import ca.sfu.cs.common.Configuration.Config;

import com.mysql.jdbc.Connection;

public class KeepTablesOnly {
    static String databaseName;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;
    static String dbname;
    private static Connection con_CT;
    private static Connection con_BN;
    private static String dbname_CT;
    private static String dbname_BN;
    private static String dbname_setup;
    private static Logger logger = Logger.getLogger(KeepTablesOnly.class.getName());


    public static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName = conf.getProperty("dbname");
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        dbname_CT = databaseName + "_CT";
        dbname_BN = databaseName + "_BN";
    }


    public static Connection connectDB(String database) throws Exception {

        String CONN_STR = "jdbc:" + dbaddress + "/" + database;

        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }

        try{
            return ((Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword));
        } catch (Exception e){
            logger.severe("Could not connect to the database " + database );
        }

        return null;
    }

    /**
     * Keeping only the longest RChain table in the CT database and deleting the remaining intermediaries.
     * Subject to change as per need for tables in the CT database.
     *
     * @throws SQLException if an error occurs when accessing the database.
     */
    public static ArrayList<String> findLongestRChain() throws SQLException {
        Statement st = con_BN.createStatement();
        ResultSet rst = st.executeQuery(
            "SELECT short_rnid AS name " +
            "FROM lattice_set " +
            "JOIN lattice_mapping " +
            "ON lattice_set.name = lattice_mapping.orig_rnid " +
            "WHERE lattice_set.length = (" +
                "SELECT MAX(length) " +
                "FROM lattice_set" +
            ");"
        );

        ArrayList<String> sets = new ArrayList<String>();
        while(rst.next()) {
            logger.fine(rst.getString("name"));
            String tables = rst.getString("name");
            sets.add(tables.substring(1, tables.length() - 1) + "_CT");
        }

        return sets;
    }


    /**
     * Keeps the tables in the given ArrayList and drops the others.
     *
     * @param con - a connection to the FactorBase database to remove the temporary tables from.
     * @param dbname - the name of the database to remove the temporary tables from.
     * @param tablenames - the names of the tables to save; i.e. these tables won't be dropped.
     *
     * @throws SQLException if an error occurs when removing the temporary tables.
     */
    public static void Drop_tmpTables(Connection con, String dbname, ArrayList<String> tablenames) throws SQLException {
        Statement st = con.createStatement();
        String NewSQL = "SELECT CONCAT('DROP TABLE `',table_name,'`;') AS result " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = '" + dbname;

        for(int i = 0; i < tablenames.size(); i++) {
            NewSQL = NewSQL + "' AND table_name != '" + (String) tablenames.get(i);
        }

        NewSQL += "';";

        logger.fine(NewSQL);
        ArrayList<String> sets = new ArrayList<String>();
        try {
            ResultSet res = st.executeQuery(NewSQL);
            while(res.next()) {
                sets.add(res.getString("result"));
            }

            for(String set : sets) {
                st.execute(set);
                logger.fine(set+" OK!");
            }
        }
        catch(SQLException e) {
            logger.severe("ERROR: "+ e);
        }
    }


    public static void Drop_tmpTables() {
        setVarsFromConfig();
        logger.fine("Set variables");

        try {
            con_CT = connectDB(dbname_CT);
        } catch (Exception e) {
            logger.severe("Could not connect to the database: " + dbname_CT);
        }

        try {
            con_BN = connectDB(dbname_BN);
        } catch (Exception e) {
            logger.severe("Could not connect to the database: " + dbname_BN);
        }

        try {
            ArrayList<String> tablenames = findLongestRChain();
            Drop_tmpTables(con_CT,dbname_CT,tablenames);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws SQLException, IOException {
        setVarsFromConfig();
        logger.fine("Set variables");

        try {
            con_CT = connectDB(dbname_CT);
        } catch (Exception e) {
            logger.severe("Could not connect to the database: " + dbname_CT );
        }

        // Keep tables in CT database.
        Drop_tmpTables();
    }
}

