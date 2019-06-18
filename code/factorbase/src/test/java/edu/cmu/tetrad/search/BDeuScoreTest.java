package edu.cmu.tetrad.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;


/**
 * Tests for the file BDeuScore.java.
 */
public class BDeuScoreTest {
    public static final double SAMPLE_PRIOR = 10.0000;
    public static final double STRUCTURE_PRIOR = 1.0000;
    public static final int POPULARITY = 0;
    public static final int TEACHINGABILITY = 1;

    private static DataSet dataset;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        URL url = BDeuScoreTest.class.getClassLoader().getResource("inputfiles/prof0.tsv");
        DataReader parser = new DataReader();
        parser.setDelimiter(DelimiterType.TAB);
        dataset = parser.parseTabular(new File(url.getFile()));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        dataset = null;
    }

    @Test
    public void localScore_ReturnsCorrectResults_WhenNoParents() throws SQLException {
        BDeuScore score = new BDeuScore(dataset, SAMPLE_PRIOR, STRUCTURE_PRIOR);
        Double scoreValue = score.localScore(POPULARITY, new int[] {});
        assertThat(scoreValue, equalTo(-4.26969744970409));
    }

    @Test
    public void localScore_ReturnsCorrectResults_WhenSingleParent() throws SQLException {
        BDeuScore score = new BDeuScore(dataset, SAMPLE_PRIOR, STRUCTURE_PRIOR);
        Double scoreValue = score.localScore(POPULARITY, new int[] {TEACHINGABILITY});
        assertThat(scoreValue, equalTo(-3.935739532045625));
    }
}