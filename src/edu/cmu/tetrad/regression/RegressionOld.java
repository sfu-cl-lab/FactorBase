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

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.ProbUtils;

import java.io.PrintStream;
import java.text.NumberFormat;

/**
 * Implements a simple linear regression model.
 *
 * @author Joseph Ramsey
 */
public class RegressionOld {

    /**
     * The number formatter used for printing reports.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The significance level for labeling a variable as significant by
     * p-value.
     */
    private double alpha = 0.05;

    /**
     * The regressors that the target is being regressed to.
     */
    private double[][] regressors;

    /**
     * The sample size of the regression.
     */
    private int sampleSize;

    /**
     * Names of the regressor variable, in order.
     */
    private String[] regressorNames;

    /**
     * A graph in which the target is the child of each significant predictor.
     */
    private Graph outGraph;

    /**
     * Where verbose output is sent.
     */
    private PrintStream out = System.out;

    /**
     * Where error output is sent.
     */
    private PrintStream err = System.err;

    /**
     * Constructs a new LinRegr.
     */
    public RegressionOld() {
    }

    /**
     * Returns the two dimensional, column major array of regressor data. This
     * array is guaranteed to be rectangular.
     *
     * @return the regressor data.
     */
    public double[][] getRegressors() {
        return regressors;
    }

    /**
     * Sets the regressor data to the given array. The array should be column
     * major, and the columns should all be the same length. Variable names are
     * set to null each time a new regressor data array is set.
     *
     * @param regressors the regressor data.
     */
    public void setRegressors(double[][] regressors) {

        if (regressors == null) {
            throw new NullPointerException("Regressor data must not be null.");
        }

        this.sampleSize = regressors[0].length;

        for (double[] regressor : regressors) {
            if (regressor == null) {
                throw new NullPointerException(
                        "All regressor columns must be non-null.");
            }

            if (regressor.length != sampleSize) {
                throw new IllegalArgumentException(
                        "Regressor data must all be the same " + "length.");
            }
        }

        this.regressors = regressors;
        this.regressorNames = null;
    }

    /**
     * Returns the array of variable names. This is a String[] array, the length
     * of which is equal to the number of columns in the regressor data array.
     */
    public String[] getRegressorNames() {
        return regressorNames;
    }

    /**
     * Sets the variable names for the regressors. This should be a String[]
     * array, the length of which is equal to the number of columns in the
     * regressor data array.
     */
    public void setRegressorNames(String[] regressorNames) {

        if (regressorNames == null) {
            throw new NullPointerException(
                    "The variable names array must not be null.");
        }

        if (regressors == null) {
            throw new IllegalArgumentException(
                    "Please set the regressor data before " +
                            "setting the variable names; otherwise, " +
                            "I don't know whether you have the " +
                            "correct number of variable names.");
        }

        if (regressorNames.length != regressors.length) {
            throw new IllegalArgumentException(
                    "The number of variable names must " +
                            "match the number of regressors: " + regressorNames
                            .length + " != " + regressors.length);
        }

        this.regressorNames = regressorNames;
    }

    /**
     * Regresses the single-column target onto the regressors which have been
     * previously set, generating a regression result.
     *
     * @param target the column of target data to regress.
     * @return the regression result (see).
     */
    public RegressionResult regress(double[] target, String targetName) {
        double[][] x, xT, xTxInv, pr2, b;
        double[] c1, t, se, p;
        String summary;
        int k = regressors.length + 1;
        int n;
        double r2;

        if (target.length == sampleSize) {
            n = this.sampleSize;
        }
        else {
            throw new IllegalArgumentException(
                    "Target sample size must match regressor sample size.");
        }

        if (target.length < regressors.length) {
            throw new IllegalArgumentException(
                    "Fewer sample points than regressors."
            );
        }

        //The output graph will contain the target node and nodes corresponding to variables
        //with significant correlations with the target together with edges from them to the
        //target.
        outGraph = new EdgeListGraph();
        Node targetNode = new GraphNode(targetName);
        outGraph.addNode(targetNode);

        // make a new matrix x with all the columns of regressors
        // but with first column all 1.0's.
        x = new double[regressors.length + 1][];
        c1 = new double[regressors[0].length];

        for (int i = 0; i < regressors[0].length; i++) {
            c1[i] = 1.0;
        }

        x[0] = c1;

        System.arraycopy(regressors, 0, x, 1, regressors.length);

        // Vector y is the target. Making it a 2D matrix allows us
        // to use a matrix multiplication method I already wrote.
        double[][] y = new double[1][];

        y[0] = target;

        // Transpose x and multiply x-transpose by x --> xTxInv
        // (not yet inverted).
        //xT = transpose(x);
        xT = MatrixUtils.transpose(x);

        xTxInv = multiply(xT, x);
        //xTxInv = MatrixUtils.outerProduct(xT, x);

        // Invert xTxInv in place (now inverted)>
        //invertGaussJordan (xTxInv);   //This method implemented in this class
        xTxInv = MatrixUtils.inverse(
                xTxInv);  //An in place inverse method in MatrixUtils.

        // Multiply x-tranpose by the target yielding pr2.
        pr2 = multiply(xT, y);
        //pr2 = MatrixUtils.outerProduct(xT, y);

        // Multiply xTxInv by pr2 yielding b.
        b = multiply(xTxInv, pr2);
        //b = MatrixUtils.outerProduct(xTxInv, pr2);

        // T statistics for parameters...
        // 1. Calculate the residual sum of squares.
        double rss = residualSumOfSquares(x, y[0], b[0]);

        // 2. Calculate the square of the standard error for the
        // distribution.
        double se2 = rss / (n - k);

        // 3. Iterate through the diagonal of the xTxInv
        // array. The standard error for each parameter is its
        // diagonal value in this array multiplied by the square
        // root of the distribution standard error times this
        // diagonal value.
        t = new double[x.length];
        p = new double[x.length];

        summary = "\n REGRESSION RESULT";
        summary +=
                "\n n = " + n + ", k = " + k + ", alpha = " + alpha + "\n";

        // add the SSE and R^2
        String rssString = nf.format(rss);
        summary += " SSE = " + rssString + "\n";
        r2 = 1.0 - (rss / calculateSSM(y[0]));
        String r2String = nf.format(r2);
        summary += " R^2 = " + r2String + "\n\n";

        summary += " VAR\tCOEF\tSE\tT\tP\n";

        se = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            double s_ii = se2 * xTxInv[i][i];
            double se_bi = Math.pow(s_ii, 0.5);

            se[i] = se_bi;

            t[i] = b[0][i] / se_bi;
            p[i] = 2 * (1.0 - ProbUtils.tCdf(Math.abs(t[i]), n - k));

            // Note: the first column contains the regression constants.
            String variableName = (i > 0) ? regressorNames[i - 1] : "const";

            summary += " " + variableName + "\t" + nf.format(b[0][i]) +
                    "\t" + nf.format(se_bi) + "\t" + nf.format(t[i]) +
                    "\t" + nf.format(p[i]) + "\t" +
                    ((p[i] < alpha) ? "significant " : "") + "\n";

            //Add a node and edge to the output graph for significant predictors:
            if (p[i] < alpha) {
                Node predictorNode = new GraphNode(variableName);
                outGraph.addNode(predictorNode);
                Edge newEdge = new Edge(predictorNode, targetNode,
                        Endpoint.TAIL, Endpoint.ARROW);
                outGraph.addEdge(newEdge);
            }
        }

        return new RegressionResult(false, regressorNames, n, b[0], t,
                p, se, r2, rss, alpha, null, null);
    }

    /**
     * @return The graph with a node for the target variable and edges from
     *         significant regressors (if any) to the target.
     */
    public Graph getOutGraph() {
        return outGraph;
    }

    private double calculateSSM(double[] y) {
        // first calculate the mean
        double mean = 0.0;
        for (double aY : y) {
            mean += aY;
        }
        mean /= (y.length);

        double ssm = 0.0;
        for (double aY1 : y) {
            double d = mean - aY1;
            ssm += d * d;
        }
        return ssm;
    }

    /**
     * Calculates the residual sum of squares for parameter data x, actual
     * values y, and regression coefficients b--i.e., for each point in the
     * data, the predicted value for that point is calculated, and then it is
     * subtracted from the actual value. The sum of the squares of these
     * difference values over all points in the data is calculated and
     * returned.
     *
     * @param x the array of data.
     * @param y the target vector.
     * @param b the regression coefficients.
     * @return the residual sum of squares.
     */
    private double residualSumOfSquares(double[][] x, double[] y, double[] b) {
        double rss = 0.0;

        for (int i = 0; i < x[0].length; i++) {
            double yH = 0.0;

            for (int j = 0; j < b.length; j++) {
                yH += b[j] * x[j][i];
            }

            double d = y[i] - yH;

            rss += d * d;
        }

        return rss;
    }


    /**
     * Returns the outerProduct of two matrices. </p> The loop structure is
     * unconventional because it assumes the matrices are stored in column major
     * order.  Cf. the outerProduct method in MatrixUtils.
     *
     * @param m1 the first matrix.
     * @param m2 the second matrix.
     * @return the matrix outerProduct of m1 and m2.
     */
    private double[][] multiply(double[][] m1, double[][] m2) {

        int mi = m1[0].length;
        int mj = m1.length;
        int mk = m2.length;

        if (m2[0].length != mj) {
            throw new IllegalArgumentException(
                    "can't multiply these matrices!");
        }

        double[][] m3 = new double[mk][mi];

        for (int i = 0; i < mi; i++) {
            for (int k = 0; k < mk; k++) {
                for (int j = 0; j < mj; j++) {
                    m3[k][i] += m1[j][i] * m2[k][j];
                }
            }
        }

        return m3;
    }

    /**
     * Returns the alpha level.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha level.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the sample size.
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Returns the output stream. Reports are printed to this.
     */
    public PrintStream getOut() {
        return out;
    }

    /**
     * Sets the output stream.
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * Gets the error stream.
     */
    public PrintStream getErr() {
        return err;
    }

    /**
     * Sets the error stream.
     */
    public void setErr(PrintStream err) {
        this.err = err;
    }
}





