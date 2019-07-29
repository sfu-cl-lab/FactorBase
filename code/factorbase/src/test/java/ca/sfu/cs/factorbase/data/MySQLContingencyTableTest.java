package ca.sfu.cs.factorbase.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;

import ca.sfu.cs.factorbase.exception.DataExtractionException;
import testframework.TestDatabaseConnection;

/**
 * Tests for contingency tables created using the MySQL based DataExtractor.
 */
public class MySQLContingencyTableTest extends ContingencyTableTestBase {

    /* (non-Javadoc)
     * @see ca.sfu.cs.factorbase.data.ContingencyTableTestBase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see ca.sfu.cs.factorbase.data.ContingencyTableTestBase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected ContingencyTableGenerator createInstance() {
        TestDatabaseConnection db = new TestDatabaseConnection();
        PreparedStatement statement;

        try {
            statement = db.con.prepareStatement("SELECT * FROM prof0_counts");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        DataExtractor dataExtractor = new MySQLDataExtractor(statement, "MULT", true);

        try {
            return new ContingencyTableGenerator(dataExtractor);
        } catch (DataExtractionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void cleanupInstance() {
        // Nothing extra to clean up.
    }
}