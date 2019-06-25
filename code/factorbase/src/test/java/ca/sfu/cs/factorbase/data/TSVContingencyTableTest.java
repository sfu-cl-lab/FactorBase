package ca.sfu.cs.factorbase.data;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the file TSVContingencyTable.java.
 */
public class TSVContingencyTableTest extends ContingencyTableTestBase<ContingencyTable> {

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
    protected ContingencyTable createInstance() {
        URL url = TSVContingencyTableTest.class.getClassLoader().getResource("inputfiles/prof0.tsv");

        try {
            return new TSVContingencyTable(url.getFile(), "MULT", true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void cleanupInstance() {
        // Nothing extra to clean up.
    }
}