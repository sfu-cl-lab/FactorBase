///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;

import java.io.PrintStream;
import java.text.NumberFormat;

/**
 * Checks conditional independence of variable in a continuous data set using Fisher's Z test. See Spirtes, Glymour, and
 * Scheines, "Causation, Prediction and Search," 2nd edition, page 94.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 */
public final class IndTestFisherZ implements IndependenceTest {

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
    /**
     * The covariance matrix.
     */
    private final ICovarianceMatrix covMatrix;
    private final DoubleMatrix2D _covMatrix;
    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private List <Node> variables;
    /**
     * The significance level of the independence tests.
     */
    private double alpha;
    /**
     * The value of the Fisher's Z statistic associated with the las calculated partial correlation.
     */
    private double fisherZ;
    /**
     * The FisherZD independence test, used when Fisher Z throws an exception (i.e., when there's a collinearity).
     */
    private IndTestFisherZGeneralizedInverse deterministicTest;
    /**
     * Stores a reference to the dataset being analyzed.
     */
    private DataSet dataSet;

    /**
     * A stored p value, if the deterministic test was used.
     */
    private double pValue = Double.NaN;
    private PrintStream pValueLogger;
    private Map <Node, Integer> indexMap;
    private Map <String, Node> nameMap;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based on the correlation matrix implied by the
     * given data set (must be continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestFisherZ(DataSet dataSet, double alpha) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new CovarianceMatrix(dataSet);
        this._covMatrix = covMatrix.getMatrix();
        List <Node> nodes = covMatrix.getVariables();

        this.variables = Collections.unmodifiableList(nodes);
        this.indexMap = indexMap(variables);
        this.nameMap = mapNames(variables);
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZGeneralizedInverse(dataSet, alpha);
        this.dataSet = dataSet;
    }

    /**
     * Constructs a new Fisher Z independence test with the listed arguments.
     *
     * @param data      A 2D continuous data set with no missing values.
     * @param variables A list of variables, a subset of the variables of <code>data</code>.
     * @param alpha     The significance cutoff level. p values less than alpha will be reported as dependent.
     */
    public IndTestFisherZ(DoubleMatrix2D data, List <Node> variables, double alpha) {
        DataSet dataSet = ColtDataSet.makeContinuousData(variables, data);
        this.covMatrix = new CovarianceMatrix(dataSet);
        this._covMatrix = covMatrix.getMatrix();
        this.variables = Collections.unmodifiableList(variables);
        this.indexMap = indexMap(variables);
        this.nameMap = mapNames(variables);
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZGeneralizedInverse(dataSet, alpha);
    }

    /**
     * Constructs a new independence test that will determine conditional independence facts using the given correlation
     * matrix and the given significance level.
     */
    public IndTestFisherZ(ICovarianceMatrix corrMatrix, double alpha) {
        this.covMatrix = corrMatrix;
        this._covMatrix = corrMatrix.getMatrix();
        this.variables = Collections.unmodifiableList(corrMatrix.getVariables());
        this.indexMap = indexMap(variables);
        this.nameMap = mapNames(variables);
        setAlpha(alpha);
    }

    private Map <String, Node> mapNames(List <Node> variables) {
        Map <String, Node> nameMap = new HashMap <String, Node>();

        for (Node node : variables) {
            nameMap.put(node.getName(), node);
        }

        return nameMap;
    }

    private Map <Node, Integer> indexMap(List <Node> variables) {
        Map <Node, Integer> indexMap = new HashMap <Node, Integer>();

        for (int i = 0; i < variables.size(); i++) {
            indexMap.put(variables.get(i), i);
        }

        return indexMap;
    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    @Override
    public IndependenceTest indTestSubset(List <Node> vars) {
        if (vars.isEmpty()) {
            throw new IllegalArgumentException("Subset may not be empty.");
        }

        for (Node var : vars) {
            if (!variables.contains(var)) {
                throw new IllegalArgumentException(
                        "All vars must be original vars");
            }
        }

        int[] indices = new int[vars.size()];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = indexMap.get(vars.get(i));
        }

        ICovarianceMatrix newCovMatrix = covMatrix.getSubmatrix(indices);

        double alphaNew = getAlpha();
        return new IndTestFisherZ(newCovMatrix, alphaNew);
    }

    /**
     * Determines whether variable x is independent of variable y given a list of conditioning variables z.
     *
     * @param x the one variable being compared.
     * @param y the second variable being compared.
     * @param z the list of conditioning variables.
     * @return true iff x _||_ y | z.
     * @throws RuntimeException if a matrix singularity is encountered.
     */
    @Override
    public boolean isIndependent(Node x, Node y, List <Node> z) {
        double r = 0;

        if (z.isEmpty()) {
            Integer xi = indexMap.get(x);
            Integer yi = indexMap.get(y);

            if (xi == null || yi == null) {
                xi = indexMap.get(nameMap.get(x.getName()));
                yi = indexMap.get(nameMap.get(y.getName()));

                if (xi == null || yi == null) {
                    throw new IllegalArgumentException("Node not in map");
                }
            }

            double a = _covMatrix.get(xi, xi);
            double b = _covMatrix.get(xi, yi);
            double d = _covMatrix.get(yi, yi);

            r = -b / Math.sqrt(a * d);
        } else {
            try {
                DoubleMatrix2D submatrix = DataUtils.subMatrix(_covMatrix, indexMap, x, y, z);

                r = StatUtils.partialCorrelation(submatrix);

                if (Double.isNaN((r)) || r < -1. || r > 1.) throw new RuntimeException();
            } catch (Exception e) {
                return false;
            }
        }

        // Either dividing by a zero standard deviation (in which case it's dependent) or doing a regression
        // (effectively) with a multicolliarity
        if (Double.isNaN(r)) {
            return false;
        }

        if (r > 1.) r = 1.;
        if (r < -1.) r = -1.;

        this.fisherZ = Math.sqrt(sampleSize() - z.size() - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        if (Double.isNaN(this.fisherZ)) {
            return false;
        }

        boolean independent = getPValue() > alpha;

        if (independent) {
            TetradLogger.getInstance().log("independencies",
                    SearchLogUtils.independenceFactMsg(x, y, z, getPValue()));
        } else {
            if (pValueLogger != null) {
                pValueLogger.println(getPValue());
            }

            TetradLogger.getInstance().log("dependencies",
                    SearchLogUtils.dependenceFactMsg(x, y, z, getPValue()));
        }


        return independent;
    }

    @Override
    public boolean isIndependent(Node x, Node y, Node... z) {
        return isIndependent(x, y, Arrays.asList(z));
    }

    @Override
    public boolean isDependent(Node x, Node y, List <Node> z) {
        return !isIndependent(x, y, z);
    }

    @Override
    public boolean isDependent(Node x, Node y, Node... z) {
        List <Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);
    }

    /**
     * Returns the probability associated with the most recently computed independence test.
     */
    @Override
    public double getPValue() {
        if (!Double.isNaN(this.pValue)) {
            return Double.NaN;
        } else {
            return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));
        }
    }

    /**
     * Gets the current significance level.
     */
    @Override
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Sets the significance level at which independence judgments should be made.  Affects the cutoff for partial
     * correlations to be considered statistically equal to zero.
     */
    @Override
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
    }

    /**
     * Returns the list of variables over which this independence checker is capable of determinine independence
     * relations-- that is, all the variables in the given graph or the given data set.
     */
    @Override
    public List <Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable with the given name.
     */
    @Override
    public Node getVariable(String name) {
        return nameMap.get(name);
    }

    /**
     * Returns the list of variable varNames.
     */
    @Override
    public List <String> getVariableNames() {
        List <Node> variables = getVariables();
        List <String> variableNames = new ArrayList <String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    /**
     * If <code>isDeterminismAllowed()</code>, deters to IndTestFisherZD; otherwise throws
     * UnsupportedOperationException.
     */
    @Override
    public boolean determines(List <Node> z, Node x) throws UnsupportedOperationException {
        int[] parents = new int[z.size()];

        for (int j = 0; j < parents.length; j++) {
            parents[j] = covMatrix.getVariables().indexOf(z.get(j));
        }

        int i = covMatrix.getVariables().indexOf(x);

        DoubleMatrix2D matrix2D = covMatrix.getMatrix();
        double variance = matrix2D.get(i, i);

        if (parents.length > 0) {

            // Regress z onto i, yielding regression coefficients b.
            DoubleMatrix2D Czz =
                    matrix2D.viewSelection(parents, parents);
            DoubleMatrix2D inverse;
            try {
                inverse = new Algebra().inverse(Czz);
//                inverse = MatrixUtils.ginverse(Czz);
            } catch (Exception e) {
                return true;
            }

            DoubleMatrix1D Cyz = matrix2D.viewColumn(i);
            Cyz = Cyz.viewSelection(parents);
            DoubleMatrix1D b = new Algebra().mult(inverse, Cyz);

            variance -= new Algebra().mult(Cyz, b);
        }

        return variance < 0.01;
    }

    /**
     * Returns the data set being analyzed.
     */
    @Override
    public DataSet getData() {
        return dataSet;
    }

    public void shuffleVariables() {
        List <Node> nodes = new ArrayList(this.variables);
        Collections.shuffle(nodes);
        this.variables = Collections.unmodifiableList(nodes);
    }

    /**
     * Returns a string representation of this test.
     */
    @Override
    public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

    /**
     * Computes that value x such that P(abs(N(0,1) > x) < alpha.  Note that this is a two sided test of the null
     * hypothesis that the Fisher's Z value, which is distributed as N(0,1) is not equal to 0.0.
     */
    private double cutoffGaussian(double alpha) {
        double upperTail = 1.0 - alpha / 2.0;
        double epsilon = 1e-14;

        // Find an upper bound.
        double lowerBound = -1.0;
        double upperBound = 0.0;

        while (ProbUtils.normalCdf(upperBound) < upperTail) {
            lowerBound += 1.0;
            upperBound += 1.0;
        }

        while (upperBound >= lowerBound + epsilon) {
            double midPoint = lowerBound + (upperBound - lowerBound) / 2.0;

            if (ProbUtils.normalCdf(midPoint) <= upperTail) {
                lowerBound = midPoint;
            } else {
                upperBound = midPoint;
            }
        }

        return lowerBound;
    }

    private int sampleSize() {
        return covMatrix().getSampleSize();
    }

    private ICovarianceMatrix covMatrix() {
        return covMatrix;
    }

    public void setPValueLogger(PrintStream pValueLogger) {
        this.pValueLogger = pValueLogger;
    }
}


