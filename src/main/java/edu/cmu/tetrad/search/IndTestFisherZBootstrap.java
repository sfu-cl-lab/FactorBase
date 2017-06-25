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

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class IndTestFisherZBootstrap implements IndependenceTest {

    /**
     * The correlation matrix.
     */
    private final ICovarianceMatrix covMatrix;

    /**
     * The variables of the correlation matrix, in order. (Unmodifiable list.)
     */
    private final List <Node> variables;

    /**
     * The significance level of the independence tests.
     */
    private double alpha = 0.05;

    /**
     * The cutoff value for 'alpha' area in the two tails of the partial correlation distribution function.
     */
    private double thresh = Double.NaN;

    /**
     * The value of the Fisher's Z statistic associated with the las calculated partial correlation.
     */
    private double fishersZ;

    /**
     * The FisherZD independence test, used when Fisher Z throws an exception (i.e., when there's a collinearity).
     */
    private IndTestFisherZGeneralizedInverse deterministicTest;
    private DataSet dataSet;
    private int numBootstrapSamples;
    private int bootstrapSampleSize;
    private DoubleMatrix2D[] bootstrapSamples;
    private IndependenceTest[] tests;

    public IndTestFisherZBootstrap(DataSet dataSet, double alpha, int numBootstrapSamples, int bootstrapSampleSize) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new CovarianceMatrix(dataSet);
        this.dataSet = dataSet;

        this.variables = Collections.unmodifiableList(covMatrix.getVariables());
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZGeneralizedInverse(dataSet, alpha);

        this.numBootstrapSamples = numBootstrapSamples;
        this.bootstrapSampleSize = bootstrapSampleSize;
        this.bootstrapSamples = new DoubleMatrix2D[numBootstrapSamples];
        this.tests = new IndependenceTest[numBootstrapSamples];

        for (int i = 0; i < numBootstrapSamples; i++) {
            DoubleMatrix2D fullData = dataSet.getDoubleData();
            bootstrapSamples[i] = DataUtils.getBootstrapSample(fullData, bootstrapSampleSize);
            tests[i] = new IndTestFisherZ(bootstrapSamples[i], dataSet.getVariables(), alpha);

        }

    }

    @Override
    public IndependenceTest indTestSubset(List <Node> vars) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isIndependent(Node x, Node y, List <Node> z) {
//        DoubleMatrix2D fullData = dataSet.getDoubleData();
        int[] independentGuys = new int[numBootstrapSamples];

        for (int i = 0; i < numBootstrapSamples; i++) {
//            DoubleMatrix2D data = getBootstrapSample(fullData, bootstrapSampleSize);
//            IndTestFisherZ test = new IndTestFisherZ(bootstrapSamples[i], dataSet.getVariables(), alpha);
            boolean independent = tests[i].isIndependent(x, y, z);
            independentGuys[i] = independent ? 1 : 0;
        }

        int sum = 0;
        for (int i = 0; i < numBootstrapSamples; i++) sum += independentGuys[i];
        boolean independent = sum > numBootstrapSamples / 2;

        if (independent) {
            TetradLogger.getInstance().log("independencies",
                    SearchLogUtils.independenceFactMsg(x, y, z, getPValue()));
        } else {
            TetradLogger.getInstance().log("dependencies",
                    SearchLogUtils.dependenceFactMsg(x, y, z, getPValue()));
        }

        return independent;
    }

    @Override
    public boolean isIndependent(Node x, Node y, Node... z) {
        List <Node> zList = Arrays.asList(z);
        return isIndependent(x, y, zList);
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

    @Override
    public double getPValue() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
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

    @Override
    public boolean determines(List <Node> z, Node x1) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return "Fisher's Z Bootstrap";
    }


    @Override
    public DataSet getData() {
        return dataSet;
    }
}
