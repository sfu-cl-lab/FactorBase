package ca.sfu.cs.factorbase.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ContingencyTableTestBase {

    private static final String VALID_VARIABLE = "teachingability(prof0)";
    private static final String INVALID_VARIABLE = "notacolumn";

    private ContingencyTableGenerator contingencyTableGenerator;
    private ContingencyTable contingencyTable;

    /**
     * The logic for creating a contingency table generator from a specific type of dataset before running all the tests.
     *
     * <p><b>IMPORTANT:</b> Make sure the dataset you use for the contingency table is the same as the content found in:</p>
     * <p>code/factorbase/src/test/resources/inputfiles/prof0.tsv</p>
     *
     * @return a contingency table generator.
     */
    protected abstract ContingencyTableGenerator createInstance();

    /**
     * The logic for cleaning up a contingency table after running all the tests.
     */
    protected abstract void cleanupInstance();

    @Before
    public void setUp() throws Exception {
        this.contingencyTableGenerator = createInstance();
        this.contingencyTable = this.contingencyTableGenerator.generateCT(Arrays.asList("popularity(prof0)", "teachingability(prof0)"));
    }

    @After
    public void tearDown() throws Exception {
        this.cleanupInstance();
        this.contingencyTable = null;
    }

    @Test
    public void getNumberOfStates_ReturnsCorrectResults_WhenGivenValidVariable() {
        long numberOfStates = this.contingencyTableGenerator.getNumberOfStates(VALID_VARIABLE);
        assertThat(numberOfStates, is(equalTo(2L)));
    }

    @Test
    public void getNumberOfStates_ReturnsZero_WhenGivenInvalidVariable() {
        long numberOfStates = this.contingencyTableGenerator.getNumberOfStates(INVALID_VARIABLE);
        assertThat(numberOfStates, is(equalTo(0L)));
    }

    @Test
    public void getStates_CreatesProperCartesianProduct_WhenGivenSetOfVariables() {
        Set<String> variables = new HashSet<>(Arrays.asList("popularity(prof0)", "teachingability(prof0)"));
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTableGenerator.getStates(variables);
        for (List<RandomVariableAssignment> assignmentCombination : assignmentCombinations) {
            assertThat(assignmentCombination.size(), is(equalTo(2)));
        }

        assertThat(assignmentCombinations.size(), is(equalTo(4)));
    }

    @Test
    public void getStates_CreatesProperCartesianProduct_WhenGivenEmptySetOfVariables() {
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTableGenerator.getStates(new HashSet<String>());
        assertThat(assignmentCombinations.size(), is(equalTo(1)));

        List<RandomVariableAssignment> assignmentCombination = assignmentCombinations.iterator().next();
        assertThat(assignmentCombination.isEmpty(), is(true));
    }

    @Test
    public void getStates_ReturnsCorrectNumberOfStates_WhenGivenValidVariableName() {
        Set<String> variableStates = this.contingencyTableGenerator.getStates(VALID_VARIABLE);

        assertThat(variableStates, containsInAnyOrder("2", "3"));
    }

    @Test
    public void getStates_ReturnsEmptySet_WhenGivenInvalidVariableName() {
        Set<String> variableStates = this.contingencyTableGenerator.getStates(INVALID_VARIABLE);

        assertThat(variableStates.size(), is(equalTo(0)));
    }


    @Test
    public void getCounts_ReturnsCorrectValue_WhenGivenAllVariableAssignments() {
        List<RandomVariableAssignment> assignments = Arrays.asList(
            new RandomVariableAssignment("popularity(prof0)", 1),
            new RandomVariableAssignment("teachingability(prof0)", 0)
        );

        long counts = this.contingencyTable.getCounts(assignments);

        assertThat(counts, is(equalTo(1L)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCounts_ThrowsException_WhenGivenSubsetOfVariableAssignments() {
        List<RandomVariableAssignment> assignments = Arrays.asList(
            new RandomVariableAssignment("popularity(prof0)", 1)
        );
        this.contingencyTable.getCounts(assignments);
    }

    @Test
    public void getVariableNames_ReturnsCorrectValues_WhenDatasetIsValid() {
        List<String> variableNames = this.contingencyTableGenerator.getVariableNames();

        assertThat(variableNames, IsIterableContainingInOrder.contains(
            "popularity(prof0)",
            "teachingability(prof0)"
        ));
    }

    @Test
    public void isDiscrete_ReturnsCorrectValues_WhenDatasetIsValid() {
        boolean isDiscrete = this.contingencyTableGenerator.isDiscrete();

        assertThat(isDiscrete, is(true));
    }
}