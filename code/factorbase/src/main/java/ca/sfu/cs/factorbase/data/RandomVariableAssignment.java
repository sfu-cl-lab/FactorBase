package ca.sfu.cs.factorbase.data;

import java.util.Objects;

/**
 * Class to hold information for the assignment state of a random variable.
 */
public class RandomVariableAssignment {
    private int columnIndex;
    private int stateIndex;


    /**
     * Store information for the assignment of a random variable.
     *
     * @param columnIndex - the column index of the random variable in the dataset.
     * @param stateIndex - the assignment for the random variable, using the index position of the state.
     */
    public RandomVariableAssignment(int columnIndex, int stateIndex) {
        this.columnIndex = columnIndex;
        this.stateIndex = stateIndex;
    }


    /**
     * Retrieve the column index of the random variable for the assignment.
     *
     * @return the column index of the random variable for the assignment.
     */
    public int getVariableColumnIndex() {
        return this.columnIndex;
    }


    /**
     * Retrieve the index position of the state.
     *
     * @return the index position of the state.
     */
    public int getStateIndex() {
        return this.stateIndex;
    }


    @Override
    public boolean equals(Object objectToCompare) {
        if (objectToCompare == this) {
            return true;
        } else if (!(objectToCompare instanceof RandomVariableAssignment)) {
            return false;
        }

        RandomVariableAssignment randomVariable = (RandomVariableAssignment) objectToCompare;

        return this.columnIndex == randomVariable.getVariableColumnIndex() && this.stateIndex == randomVariable.getStateIndex();
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.columnIndex, this.stateIndex);
    }


    @Override
    public String toString() {
        return this.columnIndex + " = " + this.stateIndex;
    }
}