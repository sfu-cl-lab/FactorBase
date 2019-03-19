package ca.sfu.cs.factorbase.tables;

/**
 * Aug 18, 2014. zqian
 * Handle the extreme case when there's only `mult` column.
 * Fixed the bug found on July 6th.
 */

/**
 * July 6th, 2014. zqian
 * Bug for processing the following case:
 * star: mult1
 * flat: mult2
 * false: mult1-mult2 ?
 * try: Financial_std_Training1_db.`operation(trans0)_a_star`
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;

/**
 * Sort merge version3
 * Here the compare function is also good without concatenating the order by columns.
 */
public class Sort_merge3 {
    private static Logger logger = Logger.getLogger(Sort_merge3.class.getName());


    public static void sort_merge(String table1, String table2, String table3, Connection conn) throws SQLException, IOException {
        logger.info("\nGenerating false table by Subtraction using Sort_merge, cur_false_Table is: " + table3);

        // !!!!!!!!!!!!!!!!! Remember to change the path and file name.
        File ftemp = new File("sort_merge.csv");
        if(ftemp.exists()) {
            ftemp.delete();
        }

        File file = new File("sort_merge.csv");
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        Statement st1 = conn.createStatement();
        Statement st2 = conn.createStatement();

        ArrayList<String> orderList = new ArrayList<String>();
        String order;

        /**
         * Code for getting the ORDER BY sequence using TABLE1, IT DOES NOT MATTER WHICH TABLE WE USE AS BOTH TABLES HAVE SAME COLUMNS.
         */
        ResultSet rst = st1.executeQuery("SHOW COLUMNS FROM " + table1 + ";");

        while(rst.next()) {
            orderList.add("`" + rst.getString(1) + "`");
        }
        rst.close();

        for(int i = 0; i < orderList.size(); i++) { // zqian, do not need this for loop, just filter the sql query by add condition clause: where field not like '%mult%'.
            if((orderList.get(i)).contains("MULT")) {
                orderList.remove(i);
                break;
            }
        }

        if (orderList.size() > 0) {
            order = " " + orderList.get(0) + " ";

            for(int i = 1; i < orderList.size(); i++) {
                order = order + ", " + orderList.get(i);
            }

            String temp = "MULT decimal ";
            for(int i = 0; i < orderList.size(); i++) {
                temp = temp + ", " + orderList.get(i) + " VARCHAR(45)";
            }

            st1.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st1.execute("CREATE TABLE " + table3 + " (" + temp + ");");

            // Code for merging the two tables.
            // BottleNeck, MOST expensive query for large table, more than 16 columns, zqian.
            // SELECT * INTO OUTFILE '/tmp/result.txt'; // Here the files are stored on the Server Side
            // and then load these files into memory?
            long time1 = System.currentTimeMillis();
            logger.info("\n rst1: " + "SELECT DISTINCT MULT, " + order + " FROM " + table1 + " ORDER BY " + order + ";");
            ResultSet rst1 = st1.executeQuery("SELECT DISTINCT MULT, " + order + " FROM " + table1 + " ORDER BY " + order + ";");
            ResultSet rst2 = st2.executeQuery("SELECT DISTINCT MULT, " + order + " FROM " + table2 + " ORDER BY " + order + ";");
            long time2 = System.currentTimeMillis();
//            System.out.print("order by time: " + (time2 - time1));

            // Finding the no. of rows in each table.
            int size1 = 0;
            int size2 = 0;

            while(rst1.next()) {
                size1++;
            }

            while(rst2.next()) {
                size2++;
            }

            // Finding the no of columns in a table.
            ResultSetMetaData rsmd = (ResultSetMetaData) rst1.getMetaData(); // DO NOT need to run another query, it should be orderList.size() + 1, zqian.
            int no_of_colmns = rsmd.getColumnCount();

            // Index variables for both tables.
            int i = 1;
            int j = 1;
            rst1.absolute(1);
            rst2.absolute(1);
            long time3 = System.currentTimeMillis();

            // Merging starting here.
            while(i <= size1 && j <= size2) {
                long val1 = 0;
                long val2 = 0;

                for(int k = 2; k <= no_of_colmns; k++) {
                    try {
//                        val1 = Integer.parseInt(rst1.getString(k));
//                        val2 = Integer.parseInt(rst2.getString(k));
                        val1 = Long.parseLong(rst1.getString(k));
                        val2 = Long.parseLong(rst2.getString(k));
                    } catch(java.lang.NumberFormatException e) {
                    } finally {
                        if(rst1.getString(k).compareTo(rst2.getString(k)) > 0) {
                            val1 = 1;
                            val2 = 0;
                        } else if(rst1.getString(k).compareTo(rst2.getString(k)) < 0) {
                            val1 = 0;
                            val2 = 1;
                        }
                    }

                    if(val1 < val2) {
                        String quer = rst1.getString(1);
                        for(int c = 2; c <= no_of_colmns; c++) {
                            quer = quer + "$" + rst1.getString(c);
                        }

                        output.write((quer) + "\n");
                        i++;
                        break;
                    } else if(val1 > val2) {
                        j++;
                        break;
                    }
                }

                if(val1 == val2) {
//                    String query = "" + (Integer.parseInt(rst1.getString(1)) - Integer.parseInt(rst2.getString(1)));
                    String query = "" + (Long.parseLong(rst1.getString(1)) - Long.parseLong(rst2.getString(1)));

                    for(int c = 2; c <= no_of_colmns; c++) {
                        query = query + "$" + rst1.getString(c);
                    }

                    output.write(query+"\n");
                    i++;
                    j++;
                }

                rst1.absolute(i);
                rst2.absolute(j);
            }

            if(i > 1) {
                rst1.absolute(i-1);
            } else {
                rst1.beforeFirst();
            }

            while(rst1.next()) {
                String query = rst1.getString(1);
                for(int c = 2; c <= no_of_colmns; c++) {
                    query = query + "$" + rst1.getString(c);
                }

                output.write((query) + "\n");
            }

            output.close();
            long time4 = System.currentTimeMillis();
//            System.out.print("\t insert time: " + (time4 - time3));
            st2.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st2.execute("CREATE TABLE " + table3 + " LIKE " + table1 + ";");
            st2.execute("LOAD DATA LOCAL INFILE 'sort_merge.csv' INTO TABLE " + table3 + " FIELDS TERMINATED BY '$' LINES TERMINATED BY '\\n';");

            rst1.close();
            rst2.close();
            st1.close();
            st2.close();

            long time5 = System.currentTimeMillis();
//            System.out.print("\t export csv file to sql: " + (time5 - time4));
            logger.info("\ntotal time: " + (time5 - time1) + "\n");

            // Delete the csv file, zqian.
            if(ftemp.exists()) {
                ftemp.delete();
            }
        } else { // Aug 18, 2014 zqian: Handle the extreme case when there's only `mult` column.
            logger.fine("\n \t Handle the extreme case when there's only `mult` column \n");
            st2.execute("DROP TABLE IF EXISTS " + table3 + ";");
            st2.execute("CREATE TABLE " + table3 + " LIKE " + table1 + ";");
            logger.fine("INSERT INTO " + table3 + " SELECT (" + table1 + ".mult - " + table2 + ".mult) AS mult FROM " + table1 + ", " + table2 + ";");
            st2.execute("INSERT INTO " + table3 + " SELECT (" + table1 + ".mult - " + table2 + ".mult) AS mult FROM " + table1 + ", " + table2 + ";");
        }
    }
}