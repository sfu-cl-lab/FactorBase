/* to  do: 
 * update the prior: zqian, June 20 2014
 * for FID who has parents the prior should be [sum(mult) group by FID ]/total_mult,
 * For FID do not have parents, prior == cp.
 * 
 * done: zqian June 20, 2014, 
 * keep a clean design for cp table by remove the mult column
 * 
 * */


/* May 26th, 2014 zqian
 * when link analysis is off, only consider the 1node and 2node which do not have any parent
 * ignore the tupes like `a,b` as rchain, `a` as child, '' as parent
 * */

/*Assume that dbname_db is all ready exist
 * Similar use as class CP.java
 * Mind the value of databaseName and databaseName2, right now is _copy_** because we are using oschulte03
 * Using FID instead of ChildValue here
 * Feb 13 Yan*/

import java.sql.*;

import java.util.ArrayList;
//import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.IOException;

public class local_CP {

    static Connection con1;
    // to be read from config
    static String databaseName, databaseName2, databaseName1;
    static String real_database;
    static String dbUsername = "";
    static String dbPassword = "";
    static String dbaddress = "";
    static String rchain;
    static int maxNumberOfMembers = 0;
    private static boolean linkAnalysis;

    //static long total_sum;

    public local_CP(String databaseName, String databaseName2) {
        local_CP.databaseName = databaseName;
        // local_CP.databaseName2 = databaseName2;

    }

    public static void main(String[] args) throws Exception {
        local_CP();
    }

    public static void local_CP() throws Exception {
        setVarsFromConfig();
        connectDB();
        local_CP_generator(rchain, con1);
        con1.close();
    }


    public static void local_CP_generator(String rchain, Connection con1) throws SQLException {
        Statement st = con1.createStatement();

        //calculate the total sum
         /*ResultSet rst1 = st.executeQuery("Select sum(MULT) From " + databaseName2 + "." + rchain.subSequence(0, rchain.length()-1) + "_CT");
		 rst1.absolute(1);
		 total_sum = rst1.getLong(1);
		 System.out.println("Total sum is " + total_sum);
		 */

        //get all FID has parents, prior = mult/sum
        ResultSet haspar = st.executeQuery("Select distinct child from " + databaseName + ".Path_BayesNets where parent<>'' and Rchain = '" + rchain + "';");
        ArrayList <String> FID_haspar = new ArrayList <String>();
        while (haspar.next()) {
            //delete apostrophe
            FID_haspar.add(haspar.getString(1).substring(1, haspar.getString(1).length() - 1));
        }

        for (int i = 0; i < FID_haspar.size(); ++i) {
            new_local_haspar(FID_haspar.get(i), con1);
            //the sum of CP of each FID should = 1
            //sumCP1(FID_haspar.get(i), con1);  // update the prior: should group by each FID, zqian, June 20 2014
        }

        //get all FID has no parents, prior = cp
        ResultSet nopar;
        // May 26th, 2014 zqian
        // when link analysis is off, only consider the 1node and 2node which do not have any parent
        // ignore the tuples like `a,b` as rchain, `a` as child, '' as parent
        if (linkAnalysis) {
            nopar = st.executeQuery("Select distinct child from " + databaseName + ".Path_BayesNets where parent='' and Rchain = '" + rchain + "' "
                    + " and child not in (select rnid from " + databaseName + ".RNodes );");
        } else
            nopar = st.executeQuery("Select distinct child from " + databaseName + ".Path_BayesNets where parent='' and Rchain = '" + rchain + "';");
        ArrayList <String> FID_nopar = new ArrayList <String>();
        while (nopar.next()) {
            //delete apostrophe
            FID_nopar.add(nopar.getString(1).substring(1, nopar.getString(1).length() - 1));
        }
        for (int i = 0; i < FID_nopar.size(); ++i) {
            new_local_nopar(FID_nopar.get(i), con1);
            //the sum of CP of each FID should = 1
            //sumCP1(FID_nopar.get(i), con1); // update the prior: cp==prior zqian, June 20 2014
        }


        st.close();
    }


    public static void new_local_haspar(String FID, Connection con1) throws SQLException {

        Statement st = con1.createStatement();

        String CTtable = "`" + FID + "_" + rchain.substring(1, rchain.length() - 1) + "_local_CT`";
        String localtable = "`" + FID + "_local_CP`";

        st.execute("Drop table if exists " + localtable + ";");

        //get all parents of this FID
        ResultSet rst = st.executeQuery("Select distinct parent from " + databaseName + ".Path_BayesNets where child='`" + FID + "`'"
                + " and Rchain='" + rchain + "' and parent <>'';");
        ArrayList <String> parents = new ArrayList <String>();
        while (rst.next()) {
            parents.add(rst.getString(1));
        }

        //create local cp table
        String createtable = "CREATE TABLE IF NOT EXISTS " + localtable + "( MULT decimal(41,0), `" + FID + "` varchar(20), ";
        for (int i = 0; i < parents.size(); ++i) {
            createtable = createtable + parents.get(i) + " varchar(20), ";
        }
        createtable = createtable + "CP float(7,6), prior float(7,6));";
        System.out.println("local: " + createtable);
        st.execute(createtable);

        //get value from cp table  change it to local_CT
        String insertvalue = "INSERT INTO " + localtable + "(MULT, `" + FID + "`";
        for (int i = 0; i < parents.size(); ++i) {
            insertvalue = insertvalue + ", " + parents.get(i);
        }
        insertvalue = insertvalue + ") SELECT MULT, `" + FID + "`";
        for (int i = 0; i < parents.size(); ++i) {
            insertvalue = insertvalue + ", " + parents.get(i);
        }
        insertvalue = insertvalue + " FROM " + CTtable + ";";
        System.out.println("local insert: " + insertvalue);
        st.execute(insertvalue);

        //calculate CP value
        st.execute("DROP TABLE IF EXISTS temp;");
        String createtemp = "CREATE TABLE IF NOT EXISTS temp SELECT sum(MULT) as parsum";
        for (int i = 0; i < parents.size(); ++i) {
            createtemp = createtemp + ", " + parents.get(i);
        }
        createtemp = createtemp + " FROM " + CTtable + " GROUP BY " + parents.get(0);
        for (int i = 1; i < parents.size(); ++i) {
            createtemp = createtemp + ", " + parents.get(i);
        }
        createtemp = createtemp + " ;";
        System.out.println("temp: " + createtemp);
        st.execute(createtemp);

        String updatecp = "UPDATE " + localtable + ", temp" + " SET " + localtable + ".CP=" + localtable + ".MULT/temp.parsum ";
        updatecp = updatecp + " WHERE " + localtable + "." + parents.get(0) + "=temp." + parents.get(0);
        for (int i = 1; i < parents.size(); ++i) {
            updatecp = updatecp + " AND " + localtable + "." + parents.get(i) + "=temp." + parents.get(i);
        }
        System.out.println("UpdateCP: " + updatecp);
        st.execute(updatecp);

        //calculate prior, total_sum should be sum(mult) of FID_rchain_CT
        ResultSet rst1 = st.executeQuery("Select sum(MULT) from " + localtable);
        rst1.absolute(1);
        long total_sum = rst1.getLong(1);
        // st.execute("UPDATE " + localtable + " SET prior = MULT / " + total_sum + ";"); // here MULT is NOT correct, zqian, June 19 2014


        //calculate prior value, zqian, June 19 2014
        st.execute("DROP TABLE IF EXISTS temp;");
        createtemp = "CREATE TABLE IF NOT EXISTS temp SELECT sum(MULT) as prior_parsum, `" + FID + "` FROM " + CTtable + " GROUP BY `" + FID + "` ;";
        System.out.println("temp: " + createtemp);
        st.execute(createtemp);

        String updateprior = "UPDATE " + localtable + ", temp" + " SET " + localtable + ".prior= temp.prior_parsum/" + total_sum
                + " WHERE " + localtable + ".`" + FID + "`=temp.`" + FID + "` ; ";

        System.out.println("updateprior: " + updateprior);
        st.execute(updateprior);
        // prior
        st.execute("DROP TABLE IF EXISTS temp;");
        st.execute("ALTER TABLE " + localtable + " DROP COLUMN `MULT`  ;"); // zqian June 20, 2014, keep a clean design for cp table by remove the mult column

        st.close();
    }


    public static void new_local_nopar(String FID, Connection con1) throws SQLException {
        System.out.println("\nlocal: No Parents FID");
        Statement st = con1.createStatement();

        String CTtable = "`" + FID + "_" + rchain.substring(1, rchain.length() - 1) + "_local_CT`";
        String localtable = "`" + FID + "_local_CP`";

        st.execute("Drop table if exists " + localtable + ";");

        //create local table
        String createtable = "CREATE TABLE IF NOT EXISTS " + localtable + " (MULT decimal(41,0), `" + FID + "` VARCHAR(20), CP float(7,6), prior float(7,6));";
        System.out.println("local: " + createtable);
        st.execute(createtable);

        //insert value into local table
        String insertvalue = "INSERT INTO " + localtable + "(MULT, `" + FID + "`) SELECT MULT, `" + FID + "` FROM " + CTtable + ";";
        System.out.println("local insert: " + insertvalue);
        st.execute(insertvalue);

        //calculate CP, here CP=prior=MULT/sum(MULT)
        ResultSet rst1 = st.executeQuery("Select sum(MULT) from " + localtable);
        rst1.absolute(1);
        long total_sum = rst1.getLong(1);
        st.execute("UPDATE " + localtable + " SET CP = MULT / " + total_sum + ";");

        //calculate prior, total_sum should be sum(mult) of FID_rchain_CT
        st.execute("UPDATE " + localtable + " SET prior = MULT / " + total_sum + ";");
        st.execute("ALTER TABLE " + localtable + " DROP COLUMN `MULT`  ;"); // zqian June 20, 2014, keep a clean design for cp table by remove the mult column

        st.close();
    }

    public static void sumCP1(String FID, Connection con1) throws SQLException {

        Statement st = con1.createStatement();

        String tablename = "`" + FID + "_local_CP`";

        //add an index column
        st.execute("ALTER TABLE " + tablename + " ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY (`id`) ;");

        ResultSet rst = st.executeQuery("Select id, prior from " + tablename + " order by prior desc;");
        rst.absolute(1);
        int target_id = rst.getInt(1);

        ResultSet rst1 = st.executeQuery("Select sum(prior) from " + tablename + " where id<>" + target_id);
        rst1.absolute(1);
        float sub_tot = rst1.getFloat(1);

        //change the CP of taget_id, make all CP sum to 1
        float newcp = 1 - sub_tot;
        st.execute("Update " + tablename + " Set prior = " + newcp + " Where id=" + target_id + ";");

        //drop index column
        st.execute("ALTER TABLE " + tablename + " DROP COLUMN `id` , DROP PRIMARY KEY ;");

        st.close();
    }

    public static void setVarsFromConfig() {
        Config conf = new Config();
        dbUsername = conf.getProperty("dbusername");
        dbPassword = conf.getProperty("dbpassword");
        dbaddress = conf.getProperty("dbaddress");
        real_database = conf.getProperty("dbname");
        databaseName = real_database + "_BN";
        // databaseName2 = real_database + "_copy_CT";
        databaseName1 = real_database + "_db";

        String la = conf.getProperty("LinkAnalysis");
        if (la.equals("1")) {
            linkAnalysis = true;
        } else {
            linkAnalysis = false;
        }

    }

    public static void connectDB() throws SQLException {
        String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName1;

        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        con1 = DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
        java.sql.Statement st = con1.createStatement();
        ResultSet maxl = st.executeQuery("Select max(length) From " + databaseName + ".lattice_set;");
        maxl.absolute(1);
        maxNumberOfMembers = maxl.getInt(1);

        ResultSet myrchain = st.executeQuery("select name as RChain from " + databaseName + ".lattice_set where lattice_set.length = " + maxNumberOfMembers + ";");
        myrchain.absolute(1);
        rchain = myrchain.getString(1);
        System.out.println("rchain: " + rchain + "\n");
        st.close();
    }
}
