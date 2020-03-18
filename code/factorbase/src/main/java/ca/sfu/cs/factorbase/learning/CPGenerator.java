package ca.sfu.cs.factorbase.learning;

import java.io.IOException;
import java.sql.Connection;

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
    public static void  Generator(
        String databaseName,
        Connection con
    ) throws SQLException, IOException {
        try (
            Statement rnodeStatement = con.createStatement();
            Statement statement = con.createStatement();
            ResultSet results = rnodeStatement.executeQuery("SELECT rnid FROM RNodes;")
        ) {
            // Adding possible values of Rnodes into Attribute_Value. // Jun 6.
            while(results.next()) {
                String rnid = results.getString("rnid");
                statement.execute("SET SQL_SAFE_UPDATES = 0;");

                // Adding boolean values for rnodes.
                // TODO: Figure out if this DELETE query is actually necessary.
                statement.execute(
                    "DELETE FROM " +
                        databaseName + "_setup.Attribute_Value " +
                    "WHERE " +
                        "column_name = '" + rnid + "';"
                );
                statement.execute(
                    "INSERT INTO " + databaseName + "_setup.Attribute_Value " +
                    "VALUES('" + rnid + "', 'T');"
                );
                statement.execute(
                    "INSERT INTO " + databaseName + "_setup.Attribute_Value " +
                    "VALUES('" + rnid + "', 'F');"
                );
            }
        }
    }
}