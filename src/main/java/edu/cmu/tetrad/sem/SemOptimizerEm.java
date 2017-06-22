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

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.Collections;
import java.util.List;

//TODO: allow for fixed parameters

/**
 * Optimizes a DAG SEM with hidden variables using expectation-maximization. IT
 * SHOULD NOT BE USED WITH SEMs THAT ARE NOT DAGS. For DAGs without hidden
 * variables, SemEstimatorRegression should be more efficient. </p> IT ALSO
 * ASSUMES THAT ALL VARIABLE NODES APPEAR FIRST IN semIm.getSemPm().getDag().getNodes(),
 * I.E., ERROR NODES ARE INSERTED ONLY AFTER MEASURED/LATENT NODES IN THIS
 * LIST.
 *
 * @author Ricardo Silva
 */

public class SemOptimizerEm implements SemOptimizer {
    static final long serialVersionUID = 23L;
    private static final double FUNC_TOLERANCE = 1.0e-4;

    /**
     * @serial Can be null.
     */
    private SemIm semIm;

    /**
     * @serial Can be null.
     */
    private double[][] expectedCovariance;

    // The following variables do not need to be serialized out, since they
    // are reset each time the optimize() method is called and are not
    // accessed otherwise.
    private transient Graph graph;
    private transient double[][] yCov;
    private transient double[][] yCovModel;
    private transient double[][] yzCovModel;
    private transient double[][] zCovModel;
    private transient int numObserved;
    private transient int numLatent;
    private transient int[] idxLatent;
    private transient int[] idxObserved;
    private transient int[][] parents;
    private transient Node[] errorParent;
    private transient double[][] nodeParentsCov;
    private transient double[][][] parentsCov;

    //==============================CONSTRUCTORS==========================//

    /**
     * Blank constructor.
     */
    public SemOptimizerEm() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerEm serializableInstance() {
        return new SemOptimizerEm();                
    }

    //=============================PUBLIC METHODS=========================//

    @Override
	public void optimize(SemIm semIm) {
        if (semIm == null) {
            throw new IllegalArgumentException();
        }

        if (DataUtils.containsMissingValue(semIm.getSampleCovar())) {
            throw new IllegalArgumentException("Please remove or impute missing values.");
        }

        initialize(semIm);
        updateMatrices();
        double score, newScore = scoreSemIm();
        do {
            score = newScore;
//            System.out.println("FML = " + score + " Chisq = " + this.semIm.getChiSquare());
            expectation();
            maximization();
            updateMatrices();
            newScore = scoreSemIm();
        } while (newScore - score > FUNC_TOLERANCE);
    }

    public double[][] getExpectedCovarianceMatrix() {
        return this.expectedCovariance;
    }

    //==============================PRIVATE METHODS========================//

    private void initialize(SemIm semIm) {
        this.semIm = semIm;
        this.graph = semIm.getSemPm().getGraph();
        this.yCov = semIm.getSampleCovar().toArray();
        this.numObserved = this.numLatent = 0;
        List<Node> nodes = this.graph.getNodes();
        Collections.sort(nodes);

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.getNodeType() == NodeType.LATENT) {
                this.numLatent++;
            }
            else if (node.getNodeType() == NodeType.MEASURED) {
                this.numObserved++;
            }
            else if (node.getNodeType() == NodeType.ERROR) {
                continue;
            }
        }

        // If trying this on a model with no latents. -jdramsey
        if (numLatent == 0) numLatent = 1;

        this.idxLatent = new int[this.numLatent];
        this.idxObserved = new int[this.numObserved];
        int countLatent = 0, countObserved = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.getNodeType() == NodeType.LATENT) {
                this.idxLatent[countLatent++] = i;
            }
            else if (node.getNodeType() == NodeType.MEASURED) {
                this.idxObserved[countObserved++] = i;
            }
            else if (node.getNodeType() == NodeType.ERROR) {
                continue;
            }
        }
        this.expectedCovariance = new double[this.numObserved + this.numLatent]
                [this.numObserved + this.numLatent];
        for (int i = 0; i < this.numObserved; i++) {
            for (int j = i; j < this.numObserved; j++) {
                this.expectedCovariance[this.idxObserved[i]][this.idxObserved[j]] =
                        this.expectedCovariance[this.idxObserved[j]][this.idxObserved[i]] =
                                this.yCov[i][j];
            }
        }
        this.yCovModel = new double[this.numObserved][this.numObserved];
        this.yzCovModel = new double[this.numObserved][this.numLatent];
        this.zCovModel = new double[this.numLatent][this.numLatent];


        this.parents = new int[this.numLatent + this.numObserved][];
        this.errorParent = new Node[this.numLatent + this.numObserved];
        this.nodeParentsCov = new double[this.numLatent + this.numObserved][];
        this.parentsCov = new double[this.numLatent + this.numObserved][][];
        for (Node node : nodes) {
            if (node.getNodeType() == NodeType.ERROR) {
                continue;
            }
            int idx = nodes.indexOf(node);
            List parents = this.graph.getParents(node);
            this.errorParent[idx] = node;
            for (int i = 0; i < parents.size(); i++) {
                Node nextParent = (Node) parents.get(i);
                if (nextParent.getNodeType() == NodeType.ERROR) {
                    this.errorParent[idx] = nextParent;
                    parents.remove(nextParent);
                    continue;
                }
            }
            if (parents.size() > 0) {
                this.parents[idx] = new int[parents.size()];
                this.nodeParentsCov[idx] = new double[parents.size()];
                this.parentsCov[idx] =
                        new double[parents.size()][parents.size()];
                for (int i = 0; i < parents.size(); i++) {
                    this.parents[idx][i] =
                            nodes.indexOf(parents.get(i));
                }
            }
            else {
                this.parents[idx] = null;
            }
        }
    }

    private void expectation() {
        double delta[][] = MatrixUtils.product(
                MatrixUtils.inverse(this.yCovModel), this.yzCovModel);
        double Delta[][] = MatrixUtils.subtract(this.zCovModel,
                MatrixUtils.product(MatrixUtils.transpose(this.yzCovModel),
                        delta));
        double yzE[][] = MatrixUtils.product(this.yCov, delta);
        double[][] m1 = MatrixUtils.product(MatrixUtils.transpose(delta), this.yCov);
        double zzE[][] = MatrixUtils.sum(MatrixUtils.product(m1, delta), Delta);
        for (int i = 0; i < this.numLatent; i++) {
            for (int j = i; j < this.numLatent; j++) {
                this.expectedCovariance[this.idxLatent[i]][this.idxLatent[j]] =
                        this.expectedCovariance[this.idxLatent[j]][this.idxLatent[i]] =
                                zzE[i][j];
            }
            for (int j = 0; j < this.numObserved; j++) {
                this.expectedCovariance[this.idxLatent[i]][this.idxObserved[j]] =
                        this.expectedCovariance[this.idxObserved[j]][this.idxLatent[i]] =
                                yzE[j][i];
            }
        }
    }

    private void maximization() {
        for (Node node : this.graph.getNodes()) {
            if (node.getNodeType() == NodeType.ERROR) {
                continue;                                       
            }
            int idx = this.graph.getNodes().indexOf(node);
            double variance = this.expectedCovariance[idx][idx];
            if (this.parents[idx] != null) {
                for (int i = 0; i < this.parents[idx].length; i++) {
                    int idx2 = this.parents[idx][i];
                    this.nodeParentsCov[idx][i] =
                            this.expectedCovariance[idx][idx2];
                    for (int j = i; j < this.parents[idx].length; j++) {
                        int idx3 = this.parents[idx][j];
                        this.parentsCov[idx][i][j] =
                                this.parentsCov[idx][j][i] =
                                        this.expectedCovariance[idx2][idx3];
                    }
                }
                double edges[] = MatrixUtils.product(
                        MatrixUtils.inverse(this.parentsCov[idx]),
                        this.nodeParentsCov[idx]);
                for (int i = 0; i < edges.length; i++) {
                    int idx2 = this.parents[idx][i];

//                    try {
//                        if (this.semIm.getSemPm().getParameter(this.graph.getNodes().get(idx2), node).isFixed()) {
//                            continue;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                        System.out.println();
//                    }

                    try {
                        this.semIm.setParamValue(
                                this.graph.getNodes().get(idx2), node,
                                edges[i]);
                    }
                    catch (IllegalArgumentException e) {
                        //Dont't do anything: it is just a fixed parameter
                    }
                }
                variance -= MatrixUtils.innerProduct(this.nodeParentsCov[idx],
                        edges);
            }
            try {
//                if (this.semIm.getSemPm().getParameter(this.errorParent[idx],
//                        this.errorParent[idx]).isFixed()) {
//                    continue;
//                }

                this.semIm.setParamValue(this.errorParent[idx],
                        this.errorParent[idx], variance);
            }
            catch (IllegalArgumentException e) {
                //Don't do anything: it is just a fixed parameter
            }
        }
    }

    private void updateMatrices() {
        DoubleMatrix2D implCovarC = this.semIm.getImplCovar();
        double impliedCovar[][] = implCovarC.toArray();
        for (int i = 0; i < this.numObserved; i++) {
            for (int j = i; j < this.numObserved; j++) {
                this.yCovModel[i][j] = this.yCovModel[j][i] =
                        impliedCovar[this.idxObserved[i]][this.idxObserved[j]];
            }
            for (int j = 0; j < this.numLatent; j++) {
                this.yzCovModel[i][j] =
                        impliedCovar[this.idxObserved[i]][this.idxLatent[j]];
            }
        }
        for (int i = 0; i < this.numLatent; i++) {
            for (int j = i; j < this.numLatent; j++) {
                this.zCovModel[i][j] = this.zCovModel[j][i] =
                        impliedCovar[this.idxLatent[i]][this.idxLatent[j]];
            }
        }
    }

    private double scoreSemIm() {
        double score = semIm.getFml();

        if (Double.isNaN(score) || score == Double.NEGATIVE_INFINITY || score == Double.POSITIVE_INFINITY) {
            score = Double.POSITIVE_INFINITY;
        }

//        System.out.println("FML = " + score);
        TetradLogger.getInstance().log("optimization", "FML = " + score);
        return -score;
    }
}



