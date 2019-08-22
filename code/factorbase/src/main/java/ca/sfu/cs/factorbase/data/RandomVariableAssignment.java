package ca.sfu.cs.factorbase.data;

import java.util.Objects;

/**
 * Class to hold information for a random variable with an assignment state.
 */
public class RandomVariableAssignment {

    private String name;
    private int value;


    /**
     * Store information for an assignment for a random variable.
     *
     * @param name - the name of the random variable.
     * @param value - the assignment for the random variable, using the index position of the state.
     */
    public RandomVariableAssignment(String name, int value) {
        this.name = name;
        this.value = value;
    }


    /**
     * Retrieve the name of the random variable for the assignment.
     *
     * @return the name of the random variable for the assignment.
     */
    public String getName() {
        return this.name;
    }


    /**
     * Retrieve the index of the state.
     *
     * @return the index of the state.
     */
    public int getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object objectToCompare) {
        if (objectToCompare == this) {
            return true;
        } else if (!(objectToCompare instanceof RandomVariableAssignment)) {
            return false;
        }

        RandomVariableAssignment randomVariable = (RandomVariableAssignment) objectToCompare;

        return this.name.equals(randomVariable.getName()) && this.value  == randomVariable.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }

    @Override
    public String toString() {
        return this.name + " = " + this.value;
    }
}