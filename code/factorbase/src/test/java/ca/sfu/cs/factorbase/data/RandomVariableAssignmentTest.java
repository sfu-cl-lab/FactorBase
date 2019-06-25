package ca.sfu.cs.factorbase.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.junit.Test;

public class RandomVariableAssignmentTest {

    private static final String NAME = "Name";
    private static final String VALUE = "Value";

    @Test
    public void constructor_InitializedCorrectly_WhenGivenValidParameters() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(NAME, VALUE);

        assertThat(assignment.getName(), equalTo(NAME));
        assertThat(assignment.getValue(), equalTo(VALUE));
        assertThat(assignment.hashCode(), equalTo(Objects.hash(NAME, VALUE)));
        assertThat(assignment.toString(), equalTo("Name = Value"));
    }

    @Test
    public void equals_ReturnsTrue_WhenRandomVariableAssignmentsAreEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(NAME, VALUE);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(NAME, VALUE);

        assertThat(assignment.equals(assignment2), is(true));
    }

    @Test
    public void equals_ReturnsFalse_WhenRandomVariableAssignmentsNamesAreNotEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(NAME, VALUE);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(NAME + "1", VALUE);

        assertThat(assignment.equals(assignment2), is(false));
    }

    @Test
    public void equals_ReturnsFalse_WhenRandomVariableAssignmentsValuesAreNotEqual() {
        RandomVariableAssignment assignment = new RandomVariableAssignment(NAME, VALUE);
        RandomVariableAssignment assignment2 = new RandomVariableAssignment(NAME, VALUE + "1");

        assertThat(assignment.equals(assignment2), is(false));
    }
}