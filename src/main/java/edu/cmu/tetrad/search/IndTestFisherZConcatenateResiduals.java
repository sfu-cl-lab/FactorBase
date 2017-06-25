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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Calculates independence from pooled residuals.
 *
 * @author Joseph Ramsey
 */
public final class IndTestFisherZConcatenateResiduals implements IndependenceTest {


    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private final List <Node> variables;

    private ArrayList <Regression> regressions;

    private List <DataSet> dataSets;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;
    /**
     * The value of the Fisher's Z statistic associated with the last calculated partial correlation.
     */
    private double fisherZ;

    private double pValue = Double.NaN;

    //==========================CONSTRUCTORS=============================//

    public IndTestFisherZConcatenateResiduals(List <DataSet> dataSets, double alpha) {
        this.dataSets = dataSets;
        regressions = new ArrayList <Regression>();
        this.variables = dataSets.get(0).getVariables();

        for (DataSet dataSet : dataSets) {
            regressions.add(new RegressionDataset(dataSet));
        }

        setAlpha(alpha);
    }

    //==========================PUBLIC METHODS=============================//

    @Override
    public IndependenceTest indTestSubset(List <Node> vars) {
        throw new UnsupportedOperationException();
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

        // Calculate the residual of x and y conditional on z for each data set and concatenate them.
        double[] residualsX = residuals(x, z);
        double[] residualsY = residuals(y, z);

        List <Double> residualsXFiltered = new ArrayList <Double>();
        List <Double> residualsYFiltered = new ArrayList <Double>();

        for (int i = 0; i < residualsX.length; i++) {
            if (!Double.isNaN(residualsX[i]) && !Double.isNaN(residualsY[i])) {
                residualsXFiltered.add(residualsX[i]);
                residualsYFiltered.add(residualsY[i]);
            }
        }

        residualsX = new double[residualsXFiltered.size()];
        residualsY = new double[residualsYFiltered.size()];

        for (int i = 0; i < residualsXFiltered.size(); i++) {
            residualsX[i] = residualsXFiltered.get(i);
            residualsY[i] = residualsYFiltered.get(i);
        }


        if (residualsX.length != residualsY.length) throw new IllegalArgumentException("Missing values handled.");
        int sampleSize = residualsX.length;

        // return a judgement of whether these concatenated residuals are independent.
        double r = StatUtils.correlation(residualsX, residualsY);

        if (r > 1.) r = 1.;
        if (r < -1.) r = -1.;

        this.fisherZ = Math.sqrt(sampleSize - z.size() - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        if (Double.isNaN(this.fisherZ)) {
            return false;
//            throw new IllegalArgumentException("The Fisher's Z " +
//                    "score for independence fact " + x + " _||_ " + y + " | " +
//                    z + " is undefined. r = " + r);
        }

        double pvalue = getPValue();
        return pvalue > alpha;

    }

    private boolean containsNaN(DataSet dataSet, Node x) {
        int column = dataSet.getColumn(x);

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            if (Double.isNaN(dataSet.getDouble(i, column))) {
                return true;
            }
        }

        return false;
    }


    private double[] residuals(Node node, List <Node> parents) {
        List <Double> _residuals = new ArrayList <Double>();

        Node _target = node;
        List <Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            double[] residualsSingleDataset = result.getResiduals().toArray();

            if (true) {
                double mean = StatUtils.mean(residualsSingleDataset);
                for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
                    residualsSingleDataset[i2] = residualsSingleDataset[i2] - mean;
                }
            }

            double mean = StatUtils.mean(residualsSingleDataset);
            for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
                residualsSingleDataset[i2] = residualsSingleDataset[i2] - mean;
            }

            for (int k = 0; k < residualsSingleDataset.length; k++) {
                _residuals.add(residualsSingleDataset[k]);
            }
        }

        double[] _f = new double[_residuals.size()];


        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        return _f;
    }

    private Node getVariable(List <Node> variables, String name) {
        for (Node node : variables) {
            if (name.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    @Override
    public boolean isIndependent(Node x, Node y, Node... z) {
        List <Node> zList = Arrays.asList(z);
        boolean independent = isIndependent(x, y, zList);

        return independent;
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
//        this.thresh = Double.NaN;
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
        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
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
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean determines(List z, Node x) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    @Override
    public DataSet getData() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if determinism is allowed.
     */
    public boolean isDeterminismAllowed() {
        return false;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setDeterminismAllowed(boolean determinismAllowed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation of this test.
     */
    @Override
    public String toString() {
        return "Fisher Z, Concatenating Residuals";
    }
}
