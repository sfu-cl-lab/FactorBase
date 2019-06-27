package ca.sfu.cs.factorbase.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ContingencyTableTestBase<T extends ContingencyTable> {

    private static final String VALID_VARIABLE = "teachingability(prof0)";
    private static final String INVALID_VARIABLE = "notacolumn";

    private T contingencyTable;

    /**
     * The logic for creating a contingency table from a specific type of dataset before running all the tests.
     *
     * <p><b>IMPORTANT:</b> Make sure the dataset you use for the contingency table is the same as the content found in:</p>
     * <p>code/factorbase/src/test/resources/inputfiles/prof0.tsv</p>
     *
     * @return a contingency table.
     */
    protected abstract T createInstance();

    /**
     * The logic for cleaning up a contingency table after running all the tests.
     */
    protected abstract void cleanupInstance();

    @Before
    public void setUp() throws Exception {
        this.contingencyTable = createInstance();
    }

    @After
    public void tearDown() throws Exception {
        this.cleanupInstance();
        this.contingencyTable = null;
    }

    @Test
    public void getNumberOfStates_ReturnsCorrectResults_WhenGivenValidVariable() {
        long numberOfStates = this.contingencyTable.getNumberOfStates(VALID_VARIABLE);
        assertThat(numberOfStates, is(equalTo(2L)));
    }

    @Test
    public void getNumberOfStates_ReturnsZero_WhenGivenInvalidVariable() {
        long numberOfStates = this.contingencyTable.getNumberOfStates(INVALID_VARIABLE);
        assertThat(numberOfStates, is(equalTo(0L)));
    }

    @Test
    public void getStates_CreatesProperCartesianProduct_WhenGivenSetOfVariables() {
        Set<String> variables = new HashSet<>(Arrays.asList("popularity(prof0)", "teachingability(prof0)"));
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTable.getStates(variables);
        for (List<RandomVariableAssignment> assignmentCombination : assignmentCombinations) {
            assertThat(assignmentCombination.size(), is(equalTo(2)));
        }

        assertThat(assignmentCombinations.size(), is(equalTo(4)));
    }

    @Test
    public void getStates_CreatesProperCartesianProduct_WhenGivenEmptySetOfVariables() {
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTable.getStates(new HashSet<String>());
        assertThat(assignmentCombinations.size(), is(equalTo(1)));

        List<RandomVariableAssignment> assignmentCombination = assignmentCombinations.iterator().next();
        assertThat(assignmentCombination.isEmpty(), is(true));
    }

    @Test
    public void getStates_ReturnsCorrectNumberOfStates_WhenGivenValidVariableName() {
        Set<String> variableStates = this.contingencyTable.getStates(VALID_VARIABLE);

        assertThat(variableStates, containsInAnyOrder("2", "3"));
    }

    @Test
    public void getStates_ReturnsEmptySet_WhenGivenInvalidVariableName() {
        Set<String> variableStates = this.contingencyTable.getStates(INVALID_VARIABLE);

        assertThat(variableStates.size(), is(equalTo(0)));
    }

    @Test
    public void getCounts_ReturnsCorrectValue_WhenGivenAllVariableAssignments() {
        Set<RandomVariableAssignment> assignments = new HashSet<RandomVariableAssignment>(
            Arrays.asList(
                new RandomVariableAssignment("popularity(prof0)", "2"),
                new RandomVariableAssignment("teachingability(prof0)", "2")
            )
        );

        long counts = this.contingencyTable.getCounts(assignments);

        assertThat(counts, is(equalTo(1L)));
    }

    @Test
    public void getCounts_ReturnsCorrectValue_WhenGivenSubsetOfVariableAssignments() {
        Set<RandomVariableAssignment> assignments = new HashSet<RandomVariableAssignment>(
            Arrays.asList(
                new RandomVariableAssignment("popularity(prof0)", "2")
            )
        );

        long counts = this.contingencyTable.getCounts(assignments);

        assertThat(counts, is(equalTo(4L)));
    }

    @Test
    public void getVariableNames_ReturnsCorrectValues_WhenDatasetIsValid() {
        Set<String> variableNames = this.contingencyTable.getVariableNames();

        assertThat(variableNames, containsInAnyOrder(
            "popularity(prof0)",
            "teachingability(prof0)"
        ));
    }

    @Test
    public void isDiscrete_ReturnsCorrectValues_WhenDatasetIsValid() {
        boolean isDiscrete = this.contingencyTable.isDiscrete();

        assertThat(isDiscrete, is(true));
    }
}
