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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Estimates a SemIm given a CovarianceMatrix and a SemPm. (A DataSet may be
 * substituted for the CovarianceMatrix.) Uses regression to do the estimation,
 * so this is only for DAG models. But the DAG model may be reset on the fly
 * and the estimation redone. Variables whose parents have not changed will
 * not be reestimated. Intended to speed up estimation for algorithms that
 * require repeated estimation of DAG models over the same variables.
 * Assumes all variables are measured.
 *
 * @author Joseph Ramsey
 */
public final class DagScorer implements TetradSerializable, Scorer {
    static final long serialVersionUID = 23L;

    private ICovarianceMatrix covMatrix;
    private DataSet dataSet = null;
    private DoubleMatrix2D edgeCoef;
    private DoubleMatrix2D errorCovar;
    private Graph dag = null;
    private List<Node> variables;
    private DoubleMatrix2D implCovarC;
    private DenseDoubleMatrix2D implCovarMeasC;
    private DoubleMatrix2D sampleCovar;
    private double logDetSample;
    private double fml = Double.NaN;


    //=============================CONSTRUCTORS============================//

    /**
     * Constructs a new SemEstimator that uses the specified optimizer.
     *
     * @param dataSet      a DataSet, all of whose variables are contained in
     *                     the given SemPm. (They are identified by name.)
     */
    public DagScorer(DataSet dataSet) {
        this(new CovarianceMatrix(dataSet));
        this.dataSet = dataSet;
    }

    /**
     * Constructs a new SemEstimator that uses the specified optimizer.
     *
     * @param covMatrix    a covariance matrix, all of whose variables are
     *                     contained in the given SemPm. (They are identified by
     *                     name.)
     */
    public DagScorer(ICovarianceMatrix covMatrix) {
        if (covMatrix == null) {
            throw new NullPointerException(
                    "CovarianceMatrix must not be null.");
        }

        this.variables = covMatrix.getVariables();
        this.covMatrix = covMatrix;

        int m = this.getVariables().size();
        this.edgeCoef = new DenseDoubleMatrix2D(m, m);
        this.errorCovar = new DenseDoubleMatrix2D(m, m);
        this.sampleCovar = covMatrix.getMatrix();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Scorer serializableInstance() {
        return new DagScorer(CovarianceMatrix.serializableInstance());
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Runs the estimator on the data and SemPm passed in through the
     * constructor. Returns the fml score of the resulting model.
     */
    @Override
	public double score(Graph dag) {
        List<Node> changedNodes = getChangedNodes(dag);

        Algebra algebra = Algebra.ZERO;

        for (Node node : changedNodes) {
            int i1 = indexOf(node);
            getErrorCovar().set(i1, i1, 0);
            for (int _j = 0; _j < getVariables().size(); _j++) {
                getEdgeCoef().set(_j, i1, 0);
            }

            if (node.getNodeType() != NodeType.MEASURED) {
                continue;
            }

            int idx = indexOf(node);
            List<Node> parents = dag.getParents(node);

            for (int i = 0; i < parents.size(); i++) {
                Node nextParent = parents.get(i);
                if (nextParent.getNodeType() == NodeType.ERROR) {
                    parents.remove(nextParent);
                    break;
                }
            }

            double variance = getSampleCovar().get(idx, idx);

            if (parents.size() > 0) {
                DoubleMatrix1D nodeParentsCov = new DenseDoubleMatrix1D(parents.size());
                DoubleMatrix2D parentsCov = new DenseDoubleMatrix2D(parents.size(), parents.size());

                for (int i = 0; i < parents.size(); i++) {
                    int idx2 = indexOf(parents.get(i));
                    nodeParentsCov.set(i, getSampleCovar().get(idx, idx2));

                    for (int j = i; j < parents.size(); j++) {
                        int idx3 = indexOf(parents.get(j));
                        parentsCov.set(i, j, getSampleCovar().get(idx2, idx3));
                        parentsCov.set(j, i, getSampleCovar().get(idx3, idx2));
                    }
                }

                DoubleMatrix1D edges = algebra.mult(
                        algebra.inverse(parentsCov), nodeParentsCov);

                for (int i = 0; i < edges.size(); i++) {
                    int idx2 = indexOf(parents.get(i));
                    edgeCoef.set(idx2, indexOf(node), edges.get(i));
                }

                variance -= algebra.mult(nodeParentsCov, edges);
            }

            errorCovar.set(i1, i1, variance);
        }


        this.dag = dag;
        this.fml = Double.NaN;

        return getFml();
    }

    private int indexOf(Node node) {
        for (int i = 0; i < getVariables().size(); i++) {
            if (node.getName().equals(this.getVariables().get(i).getName())) {
                return i;
            }
        }

        throw new IllegalArgumentException("Dag must have the same nodes as the data.");
    }

    private List<Node> getChangedNodes(Graph dag) {
        if (this.dag == null) {
            return dag.getNodes();
        }

        if (!new HashSet<Node>(this.getVariables()).equals(new HashSet<Node>(dag.getNodes()))) {
            System.out.println(new TreeSet<Node>(dag.getNodes()));
            System.out.println(new TreeSet<Node>(variables));
            throw new IllegalArgumentException("Dag must have the same nodes as the data.");
        }

        List<Node> changedNodes = new ArrayList<Node>();

        for (Node node : dag.getNodes()) {
            if (!new HashSet<Node>(this.dag.getParents(node)).equals(new HashSet<Node>(dag.getParents(node)))) {
                changedNodes.add(node);
            }
        }

        return changedNodes;
    }

    @Override
	public ICovarianceMatrix getCovMatrix() {
        return covMatrix;
    }

    /**
     * Returns a string representation of the Sem.
     */
    @Override
	public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\nSemEstimator");

        return buf.toString();
    }

    //============================PRIVATE METHODS==========================//

    /**
     * The value of the maximum likelihood function for the current the model
     * (Bollen 107). To optimize, this should be minimized.
     */
    @Override
	public double getFml() {
        if (!Double.isNaN(this.fml)) {
            return this.fml;
        }

        DoubleMatrix2D implCovarMeas; // Do this once.

        try {
            implCovarMeas = implCovarMeas();
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D sampleCovar = sampleCovar();

        double logDetSigma = logDet(implCovarMeas);
        double traceSSigmaInv = traceABInv(sampleCovar, implCovarMeas);
        double logDetSample = logDetSample();
        int pPlusQ = getMeasuredNodes().size();

//        System.out.println("Sigma = " + implCovarMeas);
//        System.out.println("Sample = " + sampleCovar);

//        System.out.println("log(det(sigma)) = " + logDetSigma + " trace = " + traceSSigmaInv
//         + " log(det(sample)) = " + logDetSample + " p plus q = " + pPlusQ);

        double fml = logDetSigma + traceSSigmaInv - logDetSample - pPlusQ;

//        System.out.println("FML = " + fml);

        if (Math.abs(fml) < 0) {//1e-14) {
            fml = 0.0;
        }

        this.fml = fml;
        return fml;
    }

    @Override
	public double getLogLikelihood() {
        DoubleMatrix2D SigmaTheta; // Do this once.

        try {
            SigmaTheta = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D sStar = sampleCovar();

        double logDetSigmaTheta = logDet(SigmaTheta);
        double traceSStarSigmaInv = traceABInv(sStar, SigmaTheta);
        int pPlusQ = getMeasuredNodes().size();

        return -(getSampleSize() / 2.) * pPlusQ * Math.log(2 * Math.PI)
                - (getSampleSize() / 2.) * logDetSigmaTheta
                - (getSampleSize() / 2.) * traceSStarSigmaInv;
    }

//    public double getScore() {
//        DoubleMatrix2D sigmaTheta; // Do this once.
//
//        try {
//            sigmaTheta = implCovarMeas();
//        } catch (Exception e) {
////            e.printStackTrace();
//            return Double.NaN;
//        }
//
//        DoubleMatrix2D s = sampleCovar();
//        DoubleMatrix2D sInv = new Algebra().inverse(s);
//        DoubleMatrix2D prod = new Algebra().mult(sigmaTheta, sInv);
//        double trace = new Algebra().trace(prod);
//
//        double detSigmaTheta = new Algebra().det(sigmaTheta);
//        double detS = new Algebra().det(s);
//
//        return Math.log(detSigmaTheta) + trace - Math.log(detS) - getNumFreeParams();
//    }

    public double getFml2() {
        DoubleMatrix2D sigma; // Do this once.

        try {
            sigma = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D s = sampleCovar();

        DoubleMatrix2D sInv = new Algebra().inverse(s);

        DoubleMatrix2D prod = new Algebra().mult(sigma, sInv);
        DoubleMatrix2D identity = DoubleFactory2D.dense.identity(s.rows());
        prod.assign(identity, PlusMult.plusMult(-1));
        double trace = MatrixUtils.trace(new Algebra().mult(prod, prod));
        double f = 0.5 * trace;

//        System.out.println(f);

        return f;
    }

    /**
     * The negative  of the log likelihood function for the current model, with
     * the constant chopped off. (Bollen 134). This is an alternative, more
     * efficient, optimization function to Fml which produces the same result
     * when minimized.
     */
    @Override
	public double getTruncLL() {
        // Formula Bollen p. 263.

        DoubleMatrix2D Sigma = implCovarMeas();

        // Using (n - 1) / n * s as in Bollen p. 134 causes sinkholes to open
        // up immediately. Not sure why.
        DoubleMatrix2D S = sampleCovar();
        int n = getSampleSize();
        return -(n - 1) / 2. * (logDet(Sigma) + traceAInvB(Sigma, S));
//        return -(n / 2.0) * (logDet(Sigma) + traceABInv(S, Sigma));
//        return -(logDet(Sigma) + traceABInv(S, Sigma));
//        return -(n - 1) / 2 * (logDet(Sigma) + traceABInv(S, Sigma));
    }

    private DoubleMatrix2D sampleCovar() {
        return getSampleCovar();
    }

    private DoubleMatrix2D implCovarMeas () {
        computeImpliedCovar();
        return this.implCovarMeasC;
    }

    /**
     * Returns BIC score, calculated as chisq - dof. This is equal to getFullBicScore() up to a constant.
     */
    @Override
	public double getBicScore() {
        int dof = getDof();
        return getChiSquare() - dof * Math.log(getSampleSize());

//        return getChiSquare() + dof * Math.log(getSampleSize());

//        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(getVariableNodes(), getImplCovar(), getSampleSize());
//        Ges ges = new Ges(covarianceMatrix);
//        return -ges.getScore(getSemIm().getGraph());
    }

    @Override
	public double getAicScore() {
        int dof = getDof();
        return getChiSquare() - 2 * dof;
//
//        return getChiSquare() + dof * Math.log(sampleSize);

//        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(getVariableNodes(), getImplCovar(), getSampleSize());
//        Ges ges = new Ges(covarianceMatrix);
//        return -ges.getScore(getSemIm().getGraph());
    }

//    /**
//     * Returns the BIC score, without subtracting constant terms.
//     */
//    public double getFullBicScore() {
////        int dof = getSemIm().getDof();
//        int sampleSize = getSampleSize();
//        double penalty = getNumFreeParams() * Math.log(sampleSize);
////        double penalty = getSemIm().getDof() * Math.log(sampleSize);
//        double L = getLogLikelihood();
//        return -2 * L + penalty;
//    }

    @Override
	public double getKicScore() {
        double fml = getFml();
        int edgeCount = dag.getNumEdges();
        int sampleSize = getSampleSize();

        return -fml + (edgeCount * Math.log(sampleSize));
    }

    /**
     * Returns the chi square value for the model.
     */
    @Override
	public double getChiSquare() {
        return (getSampleSize() - 1) * getFml();
    }

    /**
     * Returns the p-value for the model.
     */
    @Override
	public double getPValue() {
        double pValue = 1.0 - ProbUtils.chisqCdf(getChiSquare(), getDof());
//        System.out.println("P value = " + pValue);
        return pValue;
//        return (1.0 - Probability.chiSquare(getChiSquare(), semPm.getDof()));
    }

    /**
     * Adds semantic checks to the default deserialization method. This
     * method must have the standard signature for a readObject method, and
     * the body of the method must begin with "s.defaultReadObject();".
     * Other than that, any semantic checks can be specified and do not need
     * to stay the same from version to version. A readObject method of this
     * form may be added to any class, even if Tetrad sessions were
     * previously saved out using a version of the class that didn't include
     * it. (That's what the "s.defaultReadObject();" is for. See J. Bloch,
     * Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject
            (ObjectInputStream
                    s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (getCovMatrix() == null) {
            throw new NullPointerException();
        }

    }

    /**
     * Computes the implied covariance matrices of the Sem. There are two:
     * <code>implCovar </code> contains the covariances of all the variables and
     * <code>implCovarMeas</code> contains covariance for the measured variables
     * only.
     */
    private void computeImpliedCovar() {

        // Note. Since the sizes of the temp matrices in this calculation
        // never change, we ought to be able to reuse them.
        this.implCovarC = MatrixUtils.impliedCovarC(edgeCoef().viewDice(), errCovar());

        // Submatrix of implied covar for measured vars only.
        int size = getMeasuredNodes().size();
        this.implCovarMeasC = new DenseDoubleMatrix2D(size, size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
//                Node iNode = getMeasuredNodes().get(i);
//                Node jNode = getMeasuredNodes().get(j);
//
//                int _i = getVariableNodes().indexOf(iNode);
//                int _j = getVariableNodes().indexOf(jNode);

                this.implCovarMeasC.set(i, j, this.implCovarC.get(i, j));
            }
        }
    }

    private DoubleMatrix2D errCovar() {
        return getErrorCovar();
    }

    private DoubleMatrix2D edgeCoef() {
        return getEdgeCoef();
    }

    private double logDet(DoubleMatrix2D matrix2D) {
        return Math.log(new Algebra().det(matrix2D));
    }

    private double traceAInvB(DoubleMatrix2D A, DoubleMatrix2D B) {

        // Note that at this point the sem and the sample covar MUST have the
        // same variables in the same order.
        DoubleMatrix2D inverse = new Algebra().inverse(A);
        DoubleMatrix2D product = new Algebra().mult(inverse, B);

        double trace = Algebra.ZERO.trace(product);

//        double trace = MatrixUtils.trace(product);

        if (trace < -1e-8) {
            throw new IllegalArgumentException("Trace was negative: " + trace);
        }

        return trace;
    }

    private double traceABInv(DoubleMatrix2D A, DoubleMatrix2D B) {

        // Note that at this point the sem and the sample covar MUST have the
        // same variables in the same order.
        DoubleMatrix2D inverse = null;
        try {
            inverse = Algebra.ZERO.inverse(B);
        } catch (Exception e) {
            System.out.println(B);
            e.printStackTrace();
        }
        DoubleMatrix2D product = new Algebra().mult(A, inverse);

        double trace = new Algebra().trace(product);

//        double trace = MatrixUtils.trace(product);

        if (trace < -1e-8) {
            throw new IllegalArgumentException("Trace was negative: " + trace);
        }

        return trace;
    }

    private double logDetSample() {
        if (logDetSample == 0.0 && sampleCovar() != null) {
            double det = MatrixUtils.determinant(sampleCovar());
            logDetSample = Math.log(det);
        }

        return logDetSample;
    }

//    private double traceSSigmaInv2(DoubleMatrix2D s,
//                                  DoubleMatrix2D sigma) {
//
//        // Note that at this point the sem and the sample covar MUST have the
//        // same variables in the same order.
//        DoubleMatrix2D inverse = new Algebra().inverse(sigma);
//
//        for (int i = 0; i < sigma.rows(); i++) {
//            for (int j = 0; j < sigma.columns(); j++) {
//                if (sigma.get(i, j) < 1e-10) {
//                    sigma.set(i, j, 0);
//                }
//            }
//        }
//
//        System.out.println("Sigma = " + sigma);
//
//        for (int i = 0; i < inverse.rows(); i++) {
//            for (int j = 0; j < inverse.columns(); j++) {
//                if (inverse.get(i, j) < 1e-10) {
//                    inverse.set(i, j, 0);
//                }
//            }
//        }
//
//        System.out.println("Inverse of signa = " + inverse);
//
//        for (int i = 0; i < getFreeParameters().size(); i++) {
//            System.out.println(i + ". " + getFreeParameters().get(i));
//        }
//
//        DoubleMatrix2D product = new Algebra().mult(s, inverse);
//
//        double v = MatrixUtils.trace(product);
//
//        if (v < -1e-8) {
//            throw new IllegalArgumentException("Trace was negative.");
//        }
//
//        return v;
//    }

    @Override
	public DataSet getDataSet() {
        return dataSet;
    }

    @Override
	public int getNumFreeParams() {
        return dag.getEdges().size() + dag.getNodes().size();
    }

    @Override
	public int getDof() {
        return (dag.getNodes().size() * (dag.getNodes().size() + 1)) / 2 - getNumFreeParams();
    }

     @Override
	public int getSampleSize() {
        return covMatrix.getSampleSize();
    }


    @Override
	public List<Node> getMeasuredNodes() {
        return this.getVariables();
    }

    @Override
	public DoubleMatrix2D getSampleCovar() {
        return sampleCovar;
    }

    @Override
	public DoubleMatrix2D getEdgeCoef() {
        return edgeCoef;
    }

    @Override
	public DoubleMatrix2D getErrorCovar() {
        return errorCovar;
    }

    @Override
	public List<Node> getVariables() {
        return variables;
    }

    @Override
	public SemIm getEstSem() {
        SemPm pm = new SemPm(dag);

        if (dataSet != null) {
            return new SemEstimator(dataSet, pm, new SemOptimizerRegression()).estimate();
        }
        else if (covMatrix != null) {
            return new SemEstimator(covMatrix, pm, new SemOptimizerRegression()).estimate();
        }
        else {
            throw new IllegalStateException();
        }
    }
}
