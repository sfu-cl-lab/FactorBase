import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Local_CT {
    static String dbUsername;
    static String dbPassword;
    static String dbaddress;
    static String CT_databaseName;
    static String PB_databaseName;
    static String output_databaseName;
    static Connection con_CT;
    static Connection con_PB;
    static Connection con_output;
    static String Rchain ;
    static String FID ;
    static String big_CT_table ;

//    public Local_CT(String CT_databaseName, String PB_databaseName, String output_databaseName, Connection con_CT, Connection con_PB, Connection con_output) {
//
//        this.CT_databaseName = CT_databaseName;
//        this.PB_databaseName = PB_databaseName;
//        this.output_databaseName = output_databaseName;
//        this.con_CT = con_CT;
//        this.con_PB = con_PB;
//        this.con_output = con_output;
//
//
//    }

    public static void main(String[] args) throws Exception {
        setVarsFromConfig();
        connectDB();
        ComputeCTs();
    }

    public static void ComputeCTs() throws SQLException {

        Statement st_PB = con_PB.createStatement();
        System.out.println("select distinct parent from Path_BayesNets where child = '" + FID + "' and Rchain = '" + Rchain + "';");
        ResultSet rs_par = st_PB.executeQuery("select distinct parent from Path_BayesNets where child = '" + FID + "' and Rchain = '" + Rchain + "';");
        ArrayList <String> parlist = new ArrayList <String>();

        while (rs_par.next()) {
            System.out.println("hasparent node: " + rs_par.getString(1));
            parlist.add(rs_par.getString(1));
        }

        String groupbylist = "";
        int size_par = parlist.size();
        System.out.println(size_par);
        System.out.println();

        if (size_par > 0 && !parlist.get(0).isEmpty()) {
            groupbylist = FID;
            for (int i = 0; i < parlist.size(); i++) {
                groupbylist += ", " + parlist.get(i);
            }
        } else {
            groupbylist = FID;
        }

        Statement st_op = con_output.createStatement();
        String table_name = FID.substring(0, FID.length() - 1) + "_" + Rchain.substring(1, Rchain.length() - 1) + "_CT`";
        //System.out.println(table_name);
        st_op.execute("drop table if exists " + table_name + " ;");
        System.out.println("create table " + table_name + " as SELECT sum(MULT) as MULT, " + groupbylist + " FROM " + CT_databaseName + "." + big_CT_table + " group by " + groupbylist + " ;");
        st_op.execute("create table " + table_name + " as SELECT sum(MULT) as MULT, " + groupbylist + " FROM " + CT_databaseName + "." + big_CT_table + " group by " + groupbylist + " ;");
        System.out.println("Local CT is ready!");
    }


    public static void connectDB() throws SQLException {
        String CONN_STR = "jdbc:" + dbaddress + "/" + CT_databaseName;
        String CONN_STR1 = "jdbc:" + dbaddress + "/" + PB_databaseName;
        String CONN_STR2 = "jdbc:" + dbaddress + "/" + output_databaseName;
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        con_CT = (Connection) DriverManager.getConnection(CONN_STR, dbUsername, dbPassword);
        con_PB = (Connection) DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
        con_output = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
    }

    public static void setVarsFromConfig() {
        Config conf = new Config();
        //1: run Setup; 0: not run
        CT_databaseName = conf.getProperty("dbname") + "_CT";
        PB_databaseName = conf.getProperty("pathBayesNet");
        output_databaseName = conf.getProperty("dbOutputSchema");
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
    }
}
