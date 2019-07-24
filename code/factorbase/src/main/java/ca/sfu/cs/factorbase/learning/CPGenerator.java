package ca.sfu.cs.factorbase.learning;

import ca.sfu.cs.common.Configuration.Config;

import com.mysql.jdbc.Connection;

import java.io.IOException;
import java.sql.*;

/**
 * @Jun 6, zqian
 * generating CP tables by calling a store procedure in _BN database
 * And also computing BIC,AIC,loglikelihood
 *
 * August 9. This just adds Boolean values for the relationship values to the Attribute_Values tables. Todo: move this to Find_Values part.
 */
import java.util.logging.Logger;

public class CPGenerator {

    static Connection  con2;

    static String databaseName, databaseName2 ,databaseName3;
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;

    private static Logger logger = Logger.getLogger(CPGenerator.class.getName());

    public static void main(String[] args) throws Exception {
        //read config file
        setVarsFromConfig();

        // Connect to db using jdbc.
        connectDB();
        logger.info("Parameter learning for: " + databaseName2);

        CPGenerator.Generator(databaseName,con2);
        CP mycp = new CP(databaseName2,databaseName3);
        mycp.cp();
        logger.info("\n Parameter learning is done.");
        con2.close();
    }


    public static void  Generator(String databaseName2, Connection con2) throws SQLException, IOException {
//        long l = System.currentTimeMillis();
        Statement st1 = con2.createStatement();

        // Adding possible values of Rnodes into Attribute_Value. // Jun 6.
        ResultSet rs1 = st1.executeQuery("SELECT rnid FROM RNodes;");
        while(rs1.next()) {
            String rnid = rs1.getString("rnid");
//            logger.fine("rnid : " + rnid);
            Statement st2 = con2.createStatement();
            st2.execute("SET SQL_SAFE_UPDATES = 0;");

            // Adding boolean values for rnodes.
            st2.execute("DELETE FROM  Attribute_Value WHERE column_name = '" + rnid + "';");
//            logger.fine("DELETE FROM  Attribute_Value WHERE column_name = '" + rnid + "';");
//            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'True');");
//            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'False');");
            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'T');"); // April 28, 2014, zqian.
            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'F');"); // Keep consistency with CT table.
        }

        // We used to make stored procedure. Now we do CP estimation in Java.
//        st1.execute("drop procedure if exists `CP_Generator`;");
/*
        // Build stored procedure in _BN.
        BZScriptRunner bzsr = new BZScriptRunner(databaseName2, con2);
        bzsr.CP_createSP("scripts/CPGenerator.sql");
//        logger.fine("Creating the stored procedure is done " + databaseName);
        bzsr.callSP("CP_Generator");
//        logger.fine("CP_Generator is done for " + databaseName);
        long l2 = System.currentTimeMillis();
        System.out.print("Parameter Learning Time(ms): " + (l2 - l) + " ms.\n");

        ResultSet rs2 = st1.executeQuery("SELECT SUM(LogLikelihood) AS FinalLogLikelihood, SUM(Parameters) AS FreeParameters, SUM(BIC) AS FinalBIC, SUM(AIC) AS FinalAIC FROM Scores;");
        while(rs2.next()) {
            String FinalLogLikelihood = rs2.getString("FinalLogLikelihood");
            logger.fine("FinalLogLikelihood: " + FinalLogLikelihood);
            String FreeParameters = rs2.getString("FreeParameters");
            logger.fine("FreeParameters: " + FreeParameters);
            String FinalBIC = rs2.getString("FinalBIC");
            logger.fine("FinalBIC: " + FinalBIC);
            String FinalAIC = rs2.getString("FinalAIC");
            logger.fine("FinalAIC: " + FinalAIC);
         }
*/
    }


    public static void setVarsFromConfig() {
        Config conf = new Config();
        databaseName = conf.getProperty("dbname");
        databaseName2 = databaseName + "_BN";
        databaseName3 = databaseName + "_CT";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
    }


    public static void connectDB() throws SQLException {

        String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName2;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            logger.severe("Unable to load MySQL JDBC driver");
        }
        con2 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
    }
}