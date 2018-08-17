package ca.sfu.cs.factorbase.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import testframework.TestDatabaseConnection;


/**
 * Tests for the file QueryGenerator.java.
 *
 * Note: It is assumed that the script, tests-database.sql, which is found in the testsql
 * directory, has been run already.
 */
public class QueryGeneratorTest {

    private static TestDatabaseConnection db;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        db = new TestDatabaseConnection();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        db = null;
    }

    @Test
    public void createDifferenceQuery_ReturnsCorrectResults() throws SQLException {
        String query = QueryGenerator.createDifferenceQuery(
                "s_id,attr1,attr2,attr3",
                "s_id,attr1,attr2",
                "t1",
                "t2"
        );

        Statement st = db.con.createStatement();
        ResultSet rs = st.executeQuery(query);

        int count = 0;
        while (rs.next()) {
            switch (count) {
            case 0:
                assertThat(rs.getString(1), equalTo("A"));
                break;
            case 1:
                assertThat(rs.getString(1), equalTo("Jack2"));
                break;
            case 2:
                assertThat(rs.getString(1), equalTo("Kim"));
                break;
            case 3:
                assertThat(rs.getString(1), equalTo("Paul"));
                break;
            case 4:
                assertThat(rs.getString(1), equalTo("Paul2"));
                break;
            }
            count++;
        }

        assertThat(rs.getMetaData().getColumnCount(), equalTo(4));
        assertThat(count, equalTo(5));

        rs.close();
        st.close();
    }
}
