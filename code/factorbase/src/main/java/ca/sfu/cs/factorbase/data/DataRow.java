package ca.sfu.cs.factorbase.data;

public class DataRow {

    private RandomVariableAssignment[] assignments;
    private long counts;


    public DataRow(RandomVariableAssignment[] assignments, long counts) {
        this.assignments = assignments;
        this.counts = counts;
    }

    public RandomVariableAssignment[] getAssignments() {
        return this.assignments;
    }

    public long getCounts() {
        return this.counts;
    }
}
