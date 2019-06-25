package ca.sfu.cs.factorbase.data;

import java.util.Objects;

public class RandomVariableAssignment {

    private String name;
    private String value;

    public RandomVariableAssignment(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
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

        return this.name.equals(randomVariable.getName()) && this.value.equals(randomVariable.getValue());
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
