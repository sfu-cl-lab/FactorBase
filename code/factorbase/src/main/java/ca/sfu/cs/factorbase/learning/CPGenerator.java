package ca.sfu.cs.factorbase.learning;

import com.mysql.jdbc.Connection;

import java.io.IOException;

/**
 * @Jun 6, zqian
 * generating CP tables by calling a store procedure in _BN database
 * And also computing BIC,AIC,loglikelihood
 *
 * August 9. This just adds Boolean values for the relationship values to the Attribute_Values tables. Todo: move this to Find_Values part.
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CPGenerator {
    public static void  Generator(String databaseName2, Connection con2) throws SQLException, IOException {
        Statement st1 = con2.createStatement();

        // Adding possible values of Rnodes into Attribute_Value. // Jun 6.
        ResultSet rs1 = st1.executeQuery("SELECT rnid FROM RNodes;");
        while(rs1.next()) {
            String rnid = rs1.getString("rnid");
            Statement st2 = con2.createStatement();
            st2.execute("SET SQL_SAFE_UPDATES = 0;");

            // Adding boolean values for rnodes.
            st2.execute("DELETE FROM  Attribute_Value WHERE column_name = '" + rnid + "';");
            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'T');"); // April 28, 2014, zqian.
            st2.execute("INSERT INTO Attribute_Value VALUES('" + rnid + "', 'F');"); // Keep consistency with CT table.
        }
    }
}