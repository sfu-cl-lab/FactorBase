package ca.sfu.cs.factorbase.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.junit.Test;

public class RandomVariableAssignmentTest {
    private static final int COLUMN_INDEX = 1;
    private static final int STATE_INDEX = 0;

    @Test
    public void constructor_InitializedCorrectly_WhenGivenValidParameters() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX);

        assertThat(assignment.getVariableColumnIndex(), equalTo(COLUMN_INDEX));
        assertThat(assignment.getStateIndex(), equalTo(STATE_INDEX));
        assertThat(assignment.hashCode(), equalTo(Objects.hash(COLUMN_INDEX, STATE_INDEX)));
        assertThat(assignment.toString(), equalTo(COLUMN_INDEX + " = " + STATE_INDEX));
    }

    @Test
    public void equals_ReturnsTrue_WhenRandomVariableAssignmentsAreEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX);

        assertThat(assignment.equals(assignment2), is(true));
    }

    @Test
    public void equals_ReturnsFalse_WhenRandomVariableAssignmentsNamesAreNotEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(COLUMN_INDEX + 1, STATE_INDEX);

        assertThat(assignment.equals(assignment2), is(false));
    }

    @Test
    public void equals_ReturnsFalse_WhenRandomVariableAssignmentsValuesAreNotEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(COLUMN_INDEX, STATE_INDEX + 1);

        assertThat(assignment.equals(assignment2), is(false));
    }
}