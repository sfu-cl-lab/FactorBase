package ca.sfu.cs.factorbase.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ContingencyTableTestBase {

    private static final int VALID_VARIABLE = 2; // teachingability(prof0).
    private static final int INVALID_VARIABLE = 3; // Not a column.

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
        int childIndex = 1;
        int[] parentIndices = {2};
        this.contingencyTable = this.contingencyTableGenerator.generateCT(childIndex, parentIndices, 4);
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
        int[] variables = {1, 2};
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTableGenerator.getStates(variables);
        for (List<RandomVariableAssignment> assignmentCombination : assignmentCombinations) {
            assertThat(assignmentCombination.size(), is(equalTo(2)));
        }

        assertThat(assignmentCombinations.size(), is(equalTo(4)));
    }

    @Test
    public void getStates_CreatesProperCartesianProduct_WhenGivenEmptySetOfVariables() {
        Set<List<RandomVariableAssignment>> assignmentCombinations = this.contingencyTableGenerator.getStates(new int[0]);
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
        RandomVariableAssignment childAssignment = new RandomVariableAssignment(1, 1);
        List<RandomVariableAssignment> parentAssignments = Arrays.asList(
            new RandomVariableAssignment(2, 0)
        );

        long counts = this.contingencyTable.getCounts(childAssignment, parentAssignments);

        assertThat(counts, is(equalTo(1L)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCounts_ThrowsException_WhenGivenSubsetOfVariableAssignments() {
        RandomVariableAssignment childAssignment = new RandomVariableAssignment(1, 1);
        this.contingencyTable.getCounts(childAssignment, Arrays.asList());
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