package ca.sfu.cs.factorbase.data;

import java.net.URL;

import org.junit.After;
import org.junit.Before;

import ca.sfu.cs.factorbase.exception.DataExtractionException;

/**
 * Tests for contingency tables created using the TSV based DataExtractor.
 */
public class TSVContingencyTableTest extends ContingencyTableTestBase {

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
        URL url = TSVContingencyTableTest.class.getClassLoader().getResource("inputfiles/prof0.tsv");
        DataExtractor dataExtractor = new TSVDataExtractor(url.getFile(), "MULT", true);

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