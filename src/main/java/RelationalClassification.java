//package relationalClassification;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.sql.DriverManager;
import java.sql.SQLException;

public class RelationalClassification {

    private static final Connection con_std;
    private static final Connection con_BN;
    private static final Connection con_CT;
    private static final Connection con_setup;
    private static final String databaseName_std;
    private static final String databaseName_BN;
    private static final String databaseName_CT;
    private static final String databaseName_setup;
    private static final String dbbase;
    private static final String dbUsername;
    private static final String dbPassword;
    private static final String dbaddress;
    private static final String IsLinkCorrelation;
    private static final String IsContinuous;
    private static final String functorid;

    public static void main(String args[]) throws Exception {
        computeRelationalClassification();
    }

    public static void computeRelationalClassification() throws Exception{
        setVarsFromConfig();
        boolean isBNSchema = connectDB(con_BN,  databaseName_BN);
        boolean isCTSchema = connectDB(con_CT, databaseName_CT);
        //DEBUG if CT schema exists
        // ToDo : Add other checks whether database exist
        if(isCTSchema && isBNSchema
                && (new ResultSet().execute("SHOW TABLES IN `" + databaseName_CT +"` ;" )) != null
                    && (new ResultSet().execute("SHOW TABLES IN `" + databaseName_BN +"` ;" )) != null ) {
            System.out.println("Required schema are populated.");
        }
        else{
            System.out.println("Running RunBB.runBBLearner() to " +
                "\n 1. build Contingency Tables (CT)" +
                "\n 2. CSV Generation" +
                "\n 3. Learning Bayes Net");
            RunBB.runBBLearner();
        }




//        //Make a copy
//        //Markov Blanket for given rchain
//        MarkovBlanket.runMakeMarkovBlanket(functorid);



    }

    public static void setVarsFromConfig(){
        Config conf = new Config();
        databaseName_std = conf.getProperty("dbname");
        //dbbase = conf.getProperty("dbbase");
        databaseName_BN = databaseName_std + "_BN";
        databaseName_CT = databaseName_std + "_CT";
        databaseName_setup = databaseName_std + "_setup";
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        IsLinkCorrelation = conf.getProperty("LinkCorrelations");
        IsContinuous = conf.getProperty("Continuous");
        //ToDo: Move to separate config??
        functorid = conf.getProperty("functorid");
    }

    public static boolean connectDB(Connection con, String database) throws Exception{

        String CONN_STR= "jdbc:" + dbaddress + "/" + database;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        try{
            con = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
        }
        catch(Exception e){
            return false;
        }
        return true;
    }


}