package ca.sfu.cs.factorbase.search;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ca.sfu.cs.factorbase.data.ContingencyTable;
import ca.sfu.cs.factorbase.data.ContingencyTableGenerator;
import ca.sfu.cs.factorbase.data.RandomVariableAssignment;
import ca.sfu.cs.factorbase.search.DiscreteLocalScore;
import edu.cmu.tetrad.util.ProbUtils;

/**
 * Class to compute the BDeuScore for a given child and its parents.
 */
public class BDeuScore implements DiscreteLocalScore {

    private ContingencyTableGenerator contingencyTableGenerator;
    private double samplePrior;
    private double structurePrior;
    private Map<Integer, Double> cache = new HashMap<Integer, Double>();


    /**
     * Create a new BDeuScore object for the given dataset and using the given hyperparameters.
     * @param ctGenerator - {@code ContingencyTableGenerator} object to create the CT tables necessary for computing
     *                      the BDeuScore of a given child and its parents.
     * @param samplePrior - the equivalent sample size (N').
     * @param structurePrior - the prior probability for the network structure.
     */
    public BDeuScore (ContingencyTableGenerator ctGenerator, double samplePrior, double structurePrior) {
        this.contingencyTableGenerator = ctGenerator;
        this.samplePrior = samplePrior;
        this.structurePrior = structurePrior;
    }


    @Override
    public double localScore(String child, Set<String> parents) {
        int childColumnIndex = this.contingencyTableGenerator.getColumnIndex(child);
        int[] parentColumnIndices = this.contingencyTableGenerator.getColumnIndices(parents);
        int cacheKey = Objects.hash(childColumnIndex, parentColumnIndices);

        if (this.cache.containsKey(cacheKey)) {
            return this.cache.get(cacheKey);
        }

        // Number of child states.
        int r = this.contingencyTableGenerator.getNumberOfStates(childColumnIndex);

        // Number of parent states.
        int q = 1;
        for (int parent : parentColumnIndices) {
            q *= this.contingencyTableGenerator.getNumberOfStates(parent);
        }

        ContingencyTable ct = this.contingencyTableGenerator.generateCT(childColumnIndex, parentColumnIndices, r * q);

        // Calculate score.
        BigDecimal score = new BigDecimal((r - 1) * q * Math.log(this.structurePrior));

        for (List<RandomVariableAssignment> parentAssignments : this.contingencyTableGenerator.getStates(parentColumnIndices)) {
            long countsSum = 0;
            long counts;
            for (int childStateIndex = 0; childStateIndex < r; childStateIndex++) {
                RandomVariableAssignment childAssignment = new RandomVariableAssignment(childColumnIndex, childStateIndex);
                counts = ct.getCounts(childAssignment, parentAssignments);
                countsSum += counts;
                score = score.add(new BigDecimal(ProbUtils.lngamma(this.samplePrior / (r * q) + counts)));
            }

            score = score.subtract(new BigDecimal(ProbUtils.lngamma(this.samplePrior / q + countsSum)));
        }

        score = score.add(new BigDecimal(q * ProbUtils.lngamma(this.samplePrior / q)));
        score = score.subtract(new BigDecimal((r * q) * ProbUtils.lngamma(this.samplePrior / (r * q))));

        this.cache.put(cacheKey, score.doubleValue());

        return score.doubleValue();
    }
}