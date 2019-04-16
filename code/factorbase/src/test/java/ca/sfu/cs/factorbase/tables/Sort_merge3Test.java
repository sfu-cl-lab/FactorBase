package ca.sfu.cs.factorbase.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.junit.Test;

import testframework.TestDatabaseConnection;

public class Sort_merge3Test {

    @Test
    public void testSort_merge() throws SQLException, IOException {
        String SORT_MERGE_TABLE = "`sort-merge-output`";
        TestDatabaseConnection db = new TestDatabaseConnection();

        Sort_merge3.sort_merge(
            "`sort-merge-t1`",
            "`sort-merge-t2`",
            SORT_MERGE_TABLE,
            db.con
        );

        Statement st = db.con.createStatement();
        ResultSet rs = st.executeQuery(
            MessageFormat.format(
                "SELECT * FROM {0}",
                SORT_MERGE_TABLE
            )
        );

        int counts = 0;
        while (rs.next()) {
            int mult = rs.getInt(1);
            String attribute1 = rs.getString(2);
            String attribute2 = rs.getString(3);

            switch (counts) {
            case 0:
                assertThat(mult, equalTo(9995));
                assertThat(attribute1, equalTo("match1"));
                assertThat(attribute2, equalTo("match2"));
                break;
            case 1:
                assertThat(mult, equalTo(996));
                assertThat(attribute1, equalTo("match3"));
                assertThat(attribute2, equalTo("match4"));
                break;
            case 2:
                assertThat(mult, equalTo(100));
                assertThat(attribute1, equalTo("match1"));
                assertThat(attribute2, equalTo("miss1"));
                break;
            case 3:
                assertThat(mult, equalTo(10));
                assertThat(attribute1, equalTo("miss1"));
                assertThat(attribute2, equalTo("match1"));
                break;
            case 4:
                assertThat(mult, equalTo(1));
                assertThat(attribute1, equalTo("miss1"));
                assertThat(attribute2, equalTo("miss2"));
                break;
            default:
                break;
            }

            counts++;
        }

        assertThat(counts, equalTo(5));

        // Clean up the output sort merge table.
        st.executeUpdate(
            MessageFormat.format(
                "DROP TABLE {0}",
                SORT_MERGE_TABLE
            )
        );
    }
}
