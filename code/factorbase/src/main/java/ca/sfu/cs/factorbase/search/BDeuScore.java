package ca.sfu.cs.factorbase.search;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sfu.cs.factorbase.data.ContingencyTable;
import ca.sfu.cs.factorbase.data.RandomVariableAssignment;
import ca.sfu.cs.factorbase.search.DiscreteLocalScore;
import edu.cmu.tetrad.util.ProbUtils;

public class BDeuScore implements DiscreteLocalScore {

    private ContingencyTable contingencyTable;
    private double samplePrior;
    private double structurePrior;


    public BDeuScore (ContingencyTable contingencyTable, double samplePrior, double structurePrior) {
        this.contingencyTable = contingencyTable;
        this.samplePrior = samplePrior;
        this.structurePrior = structurePrior;
    }


    @Override
    public double localScore(String child, Set<String> parents) {
        // Number of child states.
        int r = this.contingencyTable.getNumberOfStates(child);

        // Number of parent states.
        int q = 1;
        for (String parent : parents) {
            q *= this.contingencyTable.getNumberOfStates(parent);
        }

        // Calculate score.
        BigDecimal score = new BigDecimal((r - 1) * q * Math.log(this.structurePrior));

        for (List<RandomVariableAssignment> parentState : this.contingencyTable.getStates(parents)) {
            long countsSum = 0;
            long counts;
            for (String childState : this.contingencyTable.getStates(child)) {
                RandomVariableAssignment childVariable = new RandomVariableAssignment(child, childState);
                if (parents.size() == 0) {
                    counts = this.contingencyTable.getTotalInstances(childVariable);
                } else {
                    Set<RandomVariableAssignment> augmentedSet = new HashSet<RandomVariableAssignment>(parentState);
                    augmentedSet.add(childVariable);

                    counts = this.contingencyTable.getCounts(augmentedSet);
                }

                countsSum += counts;
                score = score.add(new BigDecimal(ProbUtils.lngamma(this.samplePrior / (r * q) + counts)));
            }

            score = score.subtract(new BigDecimal(ProbUtils.lngamma(this.samplePrior / q + countsSum)));
        }

        score = score.add(new BigDecimal(q * ProbUtils.lngamma(this.samplePrior / q)));
        score = score.subtract(new BigDecimal((r * q) * ProbUtils.lngamma(this.samplePrior / (r * q))));

        return score.doubleValue();
    }
}