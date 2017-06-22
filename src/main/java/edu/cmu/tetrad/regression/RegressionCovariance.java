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

package edu.cmu.tetrad.regression;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.ProbUtils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Implements a regression model from correlations--that is, from a correlation
 * matrix, a list of standard deviations, and a list of means.
 *
 * @author Joseph Ramsey
 */
public class RegressionCovariance implements Regression {

    /**
     * Decimal format for all numbers.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The correlation matrix.
     */
    private CorrelationMatrix correlations;

    /**
     * The standard deviations for the variable in <code>correlations</code>.
     */
    private DoubleMatrix1D sd;

    /**
     * The means for the variables in <code>correlations</code>. May be null.
     */
    private DoubleMatrix1D means;

    /**
     * The alpha level, determining which coefficients will be considered
     * significant.
     */
    private double alpha = 0.05;

    /**
     * Returns the graph of significant regressors into the target
     */
    private Graph graph = null;

    //=========================CONSTRUCTORS===========================//

    /**
     * Constructs a covariance-based regression model using the given covariance
     * matrix, assuming that no means are specified.
     *
     * @param covariances The covariance matrix.
     */
    public RegressionCovariance(ICovarianceMatrix covariances) {
        this(covariances, zeroMeans(covariances.getDimension()));
    }

    /**
     * Constructs a covariance-based regression model, assuming that the
     * covariance matrix and the means are as specified.
     *
     * @param covariances The covariance matrix, for variables <V1,...,Vn>
     * @param means       A vector of means, for variables <V1,...,Vn>. May be
     *                    null.
     */
    public RegressionCovariance(ICovarianceMatrix covariances, DoubleMatrix1D means) {
        this(new CorrelationMatrix(covariances), standardDeviations(covariances),
                means);
    }

    /**
     * Constructs a new covariance-based regression model, assuming the given
     * correlations, standard deviations, and means are all specified.
     *
     * @param correlations       The correlation matrix, for variables
     *                           <V1,...,Vn>.
     * @param standardDeviations Standard deviations for variables <V1,..,Vn>.
     *                           Must not be null.
     * @param means              3 for variables <V1,...,Vn>. May be null.
     */
    public RegressionCovariance(CorrelationMatrix correlations,
                                DoubleMatrix1D standardDeviations,
                                DoubleMatrix1D means) {
        if (correlations == null) {
            throw new NullPointerException();
        }

        if (standardDeviations == null || standardDeviations.size() != correlations.getDimension()) {
            throw new IllegalArgumentException();
        }

        if (means != null && means.size() != correlations.getDimension()) {
            throw new IllegalArgumentException();
        }

        this.correlations = correlations;
        this.sd = standardDeviations;
        this.means = means;
    }

    //===========================PUBLIC METHODS==========================//

    /**
     * Sets the cutoff for significance. Parameters with p values less than this
     * will be labeled as significant.
     *
     * @param alpha The significance level.
     */
    @Override
	public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the graph of significant regressors into the target
     * @return This graph.
     */
    @Override
	public Graph getGraph() {
        return this.graph;
    }

    /**
     * Regresses the given target on the given regressors, yielding a regression
     * plane, in which coefficients are given for each regressor plus the
     * constant (if means have been specified, that is, for the last), and se,
     * t, and p values are given for each regressor.
     *
     * @param target     The variable being regressed.
     * @param regressors The list of regressors.
     * @return the regression plane.
     */
    @Override
	public RegressionResult regress(Node target, List<Node> regressors) {
        DoubleMatrix2D allCorrelations = correlations.getMatrix();

        List<Node> variables = correlations.getVariables();

        int yIndex = variables.indexOf(target);

        int[] xIndices = new int[regressors.size()];

        for (int i = 0; i < regressors.size(); i++) {
            xIndices[i] = variables.indexOf(regressors.get(i));
        }

        DoubleMatrix2D rX = allCorrelations.viewSelection(xIndices, xIndices);
        DoubleMatrix2D rY = allCorrelations.viewSelection(xIndices, new int[]{yIndex});

        DoubleMatrix2D bStar = new Algebra().mult(new Algebra().inverse(rX),
                rY.copy());

        DoubleMatrix1D b = new DenseDoubleMatrix1D(bStar.rows() + 1);

        for (int k = 1; k < b.size(); k++) {
            double sdY = sd.get(yIndex);
            double sdK = sd.get(xIndices[k - 1]);
            b.set(k, bStar.get(k - 1, 0) * (sdY / sdK));
        }

        b.set(0, Double.NaN);

        if (means != null) {
            double b0 = means.get(yIndex);

            for (int i = 0; i < xIndices.length; i++) {
                b0 -= b.get(i + 1) * means.get(xIndices[i]);
            }

            b.set(0, b0);
        }

        int[] allIndices = new int[1 + regressors.size()];
        allIndices[0] = yIndex;

        for (int i = 1; i < allIndices.length; i++) {
            allIndices[i] = variables.indexOf(regressors.get(i - 1));
        }

        DoubleMatrix2D r = allCorrelations.viewSelection(allIndices, allIndices);
        DoubleMatrix2D rInv = new Algebra().inverse(r);

        int n = correlations.getSampleSize();
        int k = regressors.size() + 1;

        double vY = rInv.get(0, 0);
        double r2 = 1.0 - (1.0 / vY);
        double tss = n * sd.get(yIndex) * sd.get(yIndex); // Book says n - 1.
        double rss = tss * (1.0 - r2);
        double seY = Math.sqrt(rss / (n - k));

        DoubleMatrix1D sqErr = new DenseDoubleMatrix1D(allIndices.length);
        DoubleMatrix1D t = new DenseDoubleMatrix1D(allIndices.length);
        DoubleMatrix1D p = new DenseDoubleMatrix1D(allIndices.length);

        sqErr.set(0, Double.NaN);
        t.set(0, Double.NaN);
        p.set(0, Double.NaN);

        DoubleMatrix2D rxInv = new Algebra().inverse(rX);

        for (int i = 0; i < regressors.size(); i++) {
            double _r2 = 1.0 - (1.0 / rxInv.get(i, i));
            double _tss = n * sd.get(xIndices[i]) * sd.get(xIndices[i]);
            double _se = seY / Math.sqrt(_tss * (1.0 - _r2));

            double _t = b.get(i + 1) / _se;
            double _p = 2 * (1.0 - ProbUtils.tCdf(Math.abs(_t), n - k));

            sqErr.set(i + 1, _se);
            t.set(i + 1, _t);
            p.set(i + 1, _p);
        }

        // Graph
        this.graph = createGraph(target, allIndices, regressors, p);

        String[] vNames = createVarNamesArray(regressors);
        double[] bArray = b.toArray();
        double[] tArray = t.toArray();
        double[] pArray = p.toArray();
        double[] seArray = sqErr.toArray();

        return new RegressionResult(false, vNames, n,
                bArray, tArray, pArray, seArray, r2, rss, alpha, null, null);
    }

    @Override
	public RegressionResult regress(Node target, Node... regressors) {
        List<Node> _regressors = Arrays.asList(regressors);
        return regress(target, _regressors);
    }

    //===========================PRIVATE METHODS==========================//

    private String[] createVarNamesArray(List<Node> regressors) {
        String[] vNames = getVarNamesArray(regressors);

        for (int i = 0; i < regressors.size(); i++) {
            vNames[i] = regressors.get(i).getName();
        }
        return vNames;
    }

    private String[] getVarNamesArray(List<Node> regressors) {
        return new String[regressors.size()];
    }

    private Graph createGraph(Node target, int[] allIndices, List<Node> regressors, DoubleMatrix1D p) {
        Graph graph = new EdgeListGraph();
        graph.addNode(target);

        for (int i = 0; i < allIndices.length; i++) {
            String variableName = (i > 0) ? regressors.get(i - 1).getName() : "const";

            //Add a node and edge to the output graph for significant predictors:
            if (p.get(i) < alpha) {
                Node predictorNode = new GraphNode(variableName);
                graph.addNode(predictorNode);
                Edge newEdge = new Edge(predictorNode, target,
                        Endpoint.TAIL, Endpoint.ARROW);
                graph.addEdge(newEdge);
            }
        }
        
        return graph;
    }

    private String createSummary(int n, int k, double rss, double r2, int[] allIndices, List<Node> regressors, DoubleMatrix1D b, DoubleMatrix1D se, DoubleMatrix1D t, DoubleMatrix1D p) {
        String summary = "\n REGRESSION RESULT";
        summary += "\n n = " + n + ", k = " + k + ", alpha = " + alpha + "\n";

        // add the SSE and R^2
        String rssString = nf.format(rss);
        summary += " SSE = " + rssString + "\n";
        String r2String = nf.format(r2);
        summary += " R^2 = " + r2String + "\n\n";
        summary += " VAR\tCOEF\tSE\tT\tP\n";

        for (int i = 0; i < allIndices.length; i++) {
            String variableName = (i > 0) ? regressors.get(i - 1).getName() : "const";

            summary += " " + variableName + "\t" + nf.format(b.get(i)) +
                    "\t" + nf.format(se.get(i)) + "\t" + nf.format(t.get(i)) +
                    "\t" + nf.format(p.get(i)) + "\t" +
                    ((p.get(i) < alpha) ? "significant " : "") + "\n";
        }
        return summary;
    }

    private static DoubleMatrix1D zeroMeans(int numVars) {
        return new DenseDoubleMatrix1D(numVars);
    }

    private static DoubleMatrix1D standardDeviations(ICovarianceMatrix covariances) {
        DoubleMatrix1D standardDeviations = new DenseDoubleMatrix1D(covariances.getDimension());

        for (int i = 0; i < covariances.getDimension(); i++) {
            standardDeviations.set(i, Math.sqrt(covariances.getValue(i, i)));
        }

        return standardDeviations;
    }
}

