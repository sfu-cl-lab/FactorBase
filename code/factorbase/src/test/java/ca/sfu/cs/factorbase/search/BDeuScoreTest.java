package ca.sfu.cs.factorbase.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.sfu.cs.factorbase.data.ContingencyTableGenerator;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.TSVDataExtractor;


/**
 * Tests for the file BDeuScore.java.
 */
public class BDeuScoreTest {
    public static final double SAMPLE_PRIOR = 10.0000;
    public static final double STRUCTURE_PRIOR = 1.0000;
    public static final String COUNTS_COLUMN = "MULT";
    public static final String POPULARITY = "popularity(prof0)";
    public static final String TEACHINGABILITY = "teachingability(prof0)";

    private static ContingencyTableGenerator ctGenerator;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        URL url = BDeuScoreTest.class.getClassLoader().getResource("inputfiles/prof0.tsv");
        DataExtractor dataExtractor = new TSVDataExtractor(url.getFile(), COUNTS_COLUMN, true);
        ctGenerator = new ContingencyTableGenerator(dataExtractor);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        ctGenerator = null;
    }

    @Test
    public void localScore_ReturnsCorrectResults_WhenNoParents() throws SQLException {
        BDeuScore score = new BDeuScore(ctGenerator, SAMPLE_PRIOR, STRUCTURE_PRIOR);
        Double scoreValue = score.localScore(POPULARITY, new HashSet<>(new ArrayList<String>()));
        assertThat(scoreValue, equalTo(-4.269697449704091));
    }

    @Test
    public void localScore_ReturnsCorrectResults_WhenSingleParent() throws SQLException {
        BDeuScore score = new BDeuScore(ctGenerator, SAMPLE_PRIOR, STRUCTURE_PRIOR);
        Double scoreValue = score.localScore(POPULARITY, new HashSet<>(Arrays.asList(TEACHINGABILITY)));
        assertThat(scoreValue, equalTo(-3.935739532045626));
    }
}