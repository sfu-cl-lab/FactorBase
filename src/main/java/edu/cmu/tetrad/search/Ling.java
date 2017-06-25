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
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphGroup;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.dist.Distribution;
import edu.cmu.tetrad.util.dist.GaussianPower;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * The code used within this class is largely Gustave Lacerda's, which corresponds to his essay, Discovering Cyclic
 * Causal Models by Independent Components Analysis. The code models the LiNG algorithm.
 * <p/>
 * Created by IntelliJ IDEA. User: Mark Whitehouse Date: Nov 28, 2008 Time: 8:03:29 PM To change this template use File
 * | Settings | File Templates.
 */
public class Ling implements GraphGroupSearch {

    /**
     * Number of samples used when simulating data.
     */
    private int numSamples;

    /**
     * This algorithm uses thresholding to zero out small covariance values. This variable defines the value at which
     * the thresholding occurs.
     */
    private double threshold = .5;

    /**
     * Time needed to process the search method.
     */
    private long elapsedTime = 0L;

    /**
     * Either passed in through the constructor or simulated using a graph.
     */
    private DataSet dataSet;

    //=============================CONSTRUCTORS============================//

    /**
     * The algorithm only requires a DataSet to process. Passing in a Dataset and then running the search algorithm is
     * an effetive way to use LiNG.
     *
     * @param d a DataSet over which the algorithm can process
     */
    public Ling(DataSet d) {
        dataSet = d;
    }

    /**
     * When you don't have a Dataset, supply a GraphWithParameters and the number of samples to draw and the algorithm
     * will generate a DataSet.
     *
     * @param graphWP a graph with parameters from GraphWithParameters
     * @param samples the number of samples the algorithm draws in order to generate a DataSet
     */
    public Ling(GraphWithParameters graphWP, int samples) {
        numSamples = samples;
        makeDataSet(graphWP);
    }

    /**
     * When you don't have a Dataset, supply a Graph and the number of samples to draw and the algorithm will generate a
     * DataSet.
     *
     * @param g       a graph from Graph
     * @param samples the number of samples the algorithm draws in order to generate a DataSet
     */
    public Ling(Graph g, int samples) {
        numSamples = samples;
        //get the graph shown in Example 1
        GraphWithParameters graphWP = new GraphWithParameters(g);
        makeDataSet(graphWP);
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Processes the search algorithm.
     *
     * @param n The number of variables.
     * @return StoredGraphs
     */
    private static DoubleMatrix1D getErrorCoeffsIdentity(int n) {
        DoubleMatrix1D errorCoefficients = new DenseDoubleMatrix1D(n);
        for (int i = 0; i < n; i++) {
            errorCoefficients.set(i, 1);
        }
        return errorCoefficients;
    }

    private static DoubleMatrix2D simulateCyclic(GraphWithParameters dwp, DoubleMatrix1D errorCoefficients, int n, Distribution distribution) {
        DoubleMatrix2D reducedForm = reducedForm(dwp);

        DoubleMatrix2D vectors = new DenseDoubleMatrix2D(dwp.getGraph().getNumNodes(), n);
        for (int j = 0; j < n; j++) {
            DoubleMatrix1D vector = simulateReducedForm(reducedForm, errorCoefficients, distribution);
            vectors.viewColumn(j).assign(vector);
        }
        return vectors;
    }

    private static DoubleMatrix2D reducedForm(GraphWithParameters graph) {
        int n = graph.getGraph().getNumNodes();
        DoubleMatrix2D graphMatrix = graph.getGraphMatrix().getDoubleData();
        DoubleMatrix2D identityMinusGraphMatrix = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n), 1, graphMatrix, -1);
        return MatrixUtils.inverse(identityMinusGraphMatrix);
    }

    private static DoubleMatrix1D simulateReducedForm(DoubleMatrix2D reducedForm, DoubleMatrix1D errorCoefficients, Distribution distr) {
        int n = reducedForm.rows();
        DoubleMatrix1D vector = new DenseDoubleMatrix1D(n);
        DoubleMatrix1D samples = new DenseDoubleMatrix1D(n);

        for (int j = 0; j < n; j++) { //sample from each noise term
            double sample = distr.nextRandom();
            double errorCoefficient = errorCoefficients.get(j);
            samples.set(j, sample * errorCoefficient);
        }

        for (int i = 0; i < n; i++) { //for each observed variable, i.e. dimension
            double sum = 0;
            for (int j = 0; j < n; j++) {
                double coefficient = reducedForm.get(i, j);
                double sample = samples.get(j);
                sum += coefficient * sample;
            }
            vector.set(i, sum);
        }
        return vector;
    }

    //==============================PRIVATE METHODS====================//

    private static List <Integer> makeAllRows(int n) {
        List <Integer> l = new ArrayList <Integer>();
        for (int i = 0; i < n; i++) {
            l.add(i);
        }
        return l;
    }

    private static List <List <Integer>> nRookColumnAssignments(DoubleMatrix2D mat, List <Integer> availableRows) {
        List <List <Integer>> concats = new ArrayList <List <Integer>>();

        int n = availableRows.size();

        for (int i = 0; i < n; i++) {
            int currentRowIndex = availableRows.get(i);

            if (mat.get(currentRowIndex, 0) != 0) {
                if (mat.columns() > 1) {
                    Vector <Integer> newAvailableRows = (new Vector <Integer>(availableRows));
                    newAvailableRows.removeElement(currentRowIndex);
                    DoubleMatrix2D subMat = mat.viewPart(0, 1, mat.rows(), mat.columns() - 1);
                    List <List <Integer>> allLater = nRookColumnAssignments(subMat, newAvailableRows);

                    for (List <Integer> laterPerm : allLater) {
                        laterPerm.add(0, currentRowIndex);
                        concats.add(laterPerm);
                    }
                } else {
                    List <Integer> l = new ArrayList <Integer>();
                    l.add(currentRowIndex);
                    concats.add(l);
                }
            }
        }

        return concats;
    }

    // used to produce dataset if one is not provided as the input to the constructor

    private static DoubleMatrix2D permuteRows(DoubleMatrix2D mat, List <Integer> permutation) {
        int n = mat.columns();

        DoubleMatrix2D permutedMat = new DenseDoubleMatrix2D(n, n);
        for (int j = 0; j < n; j++) {
            DoubleMatrix1D row = mat.viewRow(j);
            permutedMat.viewRow(permutation.get(j)).assign(row);
        }
        return permutedMat;
    }

    // graph matrix is B
    // mixing matrix (reduced form) is A

    private static DataSet computeBhatMatrix(DoubleMatrix2D normalizedZldW, int n, List <Node> nodes) {//, List<Integer> perm) {
        DoubleMatrix2D mat = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n), 1, normalizedZldW, -1);
        return ColtDataSet.makeContinuousData(nodes, mat);
    }

    //check against model in which: A =  ..... / (1 - xyzw)

    private static boolean allEigenvaluesAreSmallerThanOneInModulus(DoubleMatrix2D mat) {

        EigenvalueDecomposition dec = new EigenvalueDecomposition(mat);
        DoubleMatrix1D realEigenvalues = dec.getRealEigenvalues();
        DoubleMatrix1D imagEigenvalues = dec.getImagEigenvalues();

        boolean allEigenvaluesSmallerThanOneInModulus = true;
        for (int i = 0; i < realEigenvalues.size(); i++) {
            double realEigenvalue = realEigenvalues.get(i);
            double imagEigenvalue = imagEigenvalues.get(i);
            double modulus = Math.sqrt(Math.pow(realEigenvalue, 2) + Math.pow(imagEigenvalue, 2));
//			double argument = Math.atan(imagEigenvalue/realEigenvalue);
//			double modulusCubed = Math.pow(modulus, 3);
//			System.out.println("eigenvalue #"+i+" = " + realEigenvalue + "+" + imagEigenvalue + "i");
//			System.out.println("eigenvalue #"+i+" has argument = " + argument);
//			System.out.println("eigenvalue #"+i+" has modulus = " + modulus);
//			System.out.println("eigenvalue #"+i+" has modulus^3 = " + modulusCubed);

            if (modulus >= 1) {
                allEigenvaluesSmallerThanOneInModulus = false;
            }
        }
        return allEigenvaluesSmallerThanOneInModulus;
    }

    //given the W matrix, outputs the list of SEMs consistent with the observed distribution.

    /**
     * Returns the DataSet that was either provided to the class or the DataSet that the class generated.
     *
     * @return DataSet   Returns a dataset of the data used by the algorithm.
     */
    public DataSet getData() {
        return dataSet;
    }

    /**
     * The search method is used to process LiNG. Call search when you want to run the algorithm.
     */
    @Override
    public StoredGraphs search() {

        DoubleMatrix2D ica_A, ica_W;


        StoredGraphs graphs = new StoredGraphs();

        try {
            long sTime = (new Date()).getTime();

            // Using this Fast ICA to get the logging.
            DoubleMatrix2D data = dataSet.getDoubleData(); //.viewDice(); // long columns
            FastIca fastIca = new FastIca(data.copy(), data.columns());
            fastIca.setVerbose(false);
//        fastIca.setAlgorithmType(FastIca.DEFLATION);
//        fastIca.setFunction(FastIca.LOGCOSH);
//        fastIca.setTolerance(1e-20);
            FastIca.IcaResult result = fastIca.findComponents();
            ica_A = result.getA().viewDice();
            ica_W = MatrixUtils.inverse(ica_A);
            int n = ica_W.rows();

//            System.out.println("FastICA done!");

            // The original Fast ICA call.
//            DoubleMatrix2D data = dataSet.getDoubleData().viewDice(); // long rows
//			double[][] inV = MatrixUtils.convert(data);
//			FastICA fica = new FastICA(inV, data.rows());
//			ica_A = MatrixUtils.convertToColt(fica.getMixingMatrix());
//			ica_W = MatrixUtils.inverse(ica_A);
//			int n = ica_W.rows();
//
//			//if W is not square or does not have enough dimensions, throw exception
//			if (ica_W.rows()!=ica_W.columns()){
//				throw new RuntimeException("W is not square!");
//			}
//
//			if (ica_W.rows()!=dataSet.getNumColumns())
//				throw new RuntimeException("W does not have the right number of dimensions!");

            //this is the heart of our method:
            graphs = findCandidateModels(dataSet.getVariables(), ica_W, n, true);

//            for (int i = 0; i < graphs.getNumGraphs(); i++) {
//                System.out.println("Solution " + (i + 1) + " (" + (graphs.isStable(i) ? "stable" : "unstable") + ")");
//
//                System.out.println(graphs.getGraph(i));
//
//                System.out.println(graphs.getData(i));
//            }

            elapsedTime = (new Date()).getTime() - sTime;
        } catch (Exception e) {
            e.printStackTrace();
        }


        // return empty object if
        return graphs;
    }

    // uses the thresholding criterion

    /**
     * Calculates the time used when processing the search method.
     */
    @Override
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Sets the value at which thresholding occurs on Fast ICA data. Default is .05.
     *
     * @param t The value at which the thresholding is set
     */
    public void setThreshold(double t) {
        threshold = t;
    }

    private void makeDataSet(GraphWithParameters graphWP) {
        //define the "Gaussian-squared" distribution
        Distribution gp2 = new GaussianPower(2);

        //the coefficients of the error terms  (here, all 1s)
        DoubleMatrix1D errorCoefficients = getErrorCoeffsIdentity(graphWP.getGraph().getNumNodes());

        //generate data from the SEM
        DoubleMatrix2D inVectors = simulateCyclic(graphWP, errorCoefficients, numSamples, gp2);

        //reformat it
        dataSet = ColtDataSet.makeContinuousData(graphWP.getGraph().getNodes(), inVectors.viewDice());
    }

    private StoredGraphs findCandidateModels(List <Node> variables, DoubleMatrix2D matrixW, int n, boolean approximateZeros) {

        DoubleMatrix2D normalizedZldW;
        List <PermutationMatrixPair> zldPerms;

        StoredGraphs gs = new StoredGraphs();

        System.out.println("Calculating zeroless diagonal permutations...");

        TetradLogger.getInstance().log("lingDetails", "Calculating zeroless diagonal permutations.");
        zldPerms = zerolessDiagonalPermutations(matrixW, approximateZeros);

        System.out.println("Calculated zeroless diagonal permutations.");

        //for each W~, compute a candidate B, and score it
        for (PermutationMatrixPair zldPerm : zldPerms) {
            TetradLogger.getInstance().log("lingDetails", "" + zldPerm);
            System.out.println(zldPerm);

            normalizedZldW = MatrixUtils.normalizeDiagonal(zldPerm.getMatrixW());
            // Note: add method to deal with this data
            zldPerm.setMatrixBhat(computeBhatMatrix(normalizedZldW, n, variables)); //B~ = I - W~
            boolean isStableMatrix = allEigenvaluesAreSmallerThanOneInModulus(zldPerm.getMatrixBhat().getDoubleData());
            GraphWithParameters graph = new GraphWithParameters(zldPerm.getMatrixBhat());

            gs.addGraph(graph.getGraph());
            gs.addStable(isStableMatrix);
            gs.addData(zldPerm.getMatrixBhat());

        }

        TetradLogger.getInstance().log("stableGraphs", "Stable Graphs:");

        for (int d = 0; d < gs.getNumGraphs(); d++) {
            if (!gs.isStable(d)) {
                continue;
            }

            TetradLogger.getInstance().log("stableGraphs", "" + gs.getGraph(d));

            if (TetradLogger.getInstance().getLoggerConfig() != null &&
                    TetradLogger.getInstance().getLoggerConfig().isEventActive("stableGraphs")) {
                TetradLogger.getInstance().log("wMatrices", "" + gs.getData(d));
            }
        }

        TetradLogger.getInstance().log("unstableGraphs", "Unstable Graphs:");

        for (int d = 0; d < gs.getNumGraphs(); d++) {
            if (gs.isStable(d)) {
                continue;
            }

            TetradLogger.getInstance().log("unstableGraphs", "" + gs.getGraph(d));

            if (TetradLogger.getInstance().getLoggerConfig() != null &&
                    TetradLogger.getInstance().getLoggerConfig().isEventActive("unstableGraphs")) {
                TetradLogger.getInstance().log("wMatrices", "" + gs.getData(d));
            }
        }

        return gs;
    }

    //	B^ = I - W~'

    private List <PermutationMatrixPair> zerolessDiagonalPermutations(DoubleMatrix2D ica_W, boolean approximateZeros) {

        List <PermutationMatrixPair> permutations = new Vector <PermutationMatrixPair>();

        if (approximateZeros) {
            setInsignificantEntriesToZero(ica_W);
        }

        //find assignments
        DoubleMatrix2D mat = ica_W.viewDice();
        //returns all zeroless-diagonal column-permutations

        System.out.println("AAA");

        List <List <Integer>> nRookAssignments = nRookColumnAssignments(mat, makeAllRows(mat.rows()));

        System.out.println("BBB");

        //for each assignment, add the corresponding permutation to 'permutations'
        for (List <Integer> permutation : nRookAssignments) {
            DoubleMatrix2D matrixW = permuteRows(ica_W, permutation).viewDice();
            PermutationMatrixPair permMatrixPair = new PermutationMatrixPair(permutation, matrixW);
            permutations.add(permMatrixPair);
        }

        System.out.println("CCC");

        return permutations;
    }

    private void setInsignificantEntriesToZero(DoubleMatrix2D mat) {
        int n = mat.rows();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(mat.get(i, j)) < threshold)
                    mat.set(i, j, 0);
            }
        }
    }

    /**
     * Adds semantic checks to the default deserialization method. This method must have the standard signature for a
     * readObject method, and the body of the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from version to version. A readObject method of
     * this form may be added to any class, even if Tetrad sessions were previously saved out using a version of the
     * class that didn't include it. (That's what the "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for
     * help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

    }

    /**
     * This small class is used to store graph permutations. It contains basic methods for adding and accessing graphs.
     * <p/>
     * It is likely that this class will move elesewhere once the role of algorithms that produce multiple graphs is
     * better defined.
     */

    public static class StoredGraphs implements GraphGroup {

        /**
         * Graph permutations are stored here.
         */
        private List <Graph> graphs = new ArrayList <Graph>();

        /**
         * Store data for each graph in case the data is needed later
         */
        private List <DataSet> dataSet = new ArrayList <DataSet>();

        /**
         * Boolean valued vector that contains the stability information for its corresponding graph. stable = true
         * means the graph has all eigenvalues with modulus < 1.
         */
        private List <Boolean> stable = new ArrayList <Boolean>();

        /**
         * Gets the number of graphs stored by the class.
         *
         * @return Returns the number of graphs stored in the class
         */
        @Override
        public int getNumGraphs() {
            return graphs.size();
        }

        /**
         * Returns a specific graph at index g.
         *
         * @param g The index of the graph to be returned
         * @return Returns a Graph
         */
        @Override
        public Graph getGraph(int g) {
            return graphs.get(g);
        }

        /**
         * Returns the data for a specific graph at index d.
         *
         * @param d The index of the graph for which the DataSet is being returned
         * @return Returns a DataSet
         */
        public DataSet getData(int d) {
            return dataSet.get(d);
        }

        /**
         * Returns whether or not the graph at index s is stable.
         *
         * @param s The index of the graph at which to return the boolean stability information for the permutation
         * @return Returns the shriknig variable value for a specific graph.
         */
        public boolean isStable(int s) {
            return stable.get(s);
        }

        /**
         * Gives a method for adding classes to the class.
         *
         * @param g The graph to add
         */
        @Override
        public void addGraph(Graph g) {
            graphs.add(g);
        }

        /**
         * A method for adding graph data to the class.
         *
         * @param d The graph to add
         */
        public void addData(DataSet d) {
            dataSet.add(d);
        }

        /**
         * Allows for the adding of shinking information to its corresponding graph. This should be used at the same time as
         * addGraph() if it is to be used. Otherwise, add a method to add data at a specific index.
         *
         * @param s The stability value to set for a graph.
         */
        public void addStable(Boolean s) {
            stable.add(s);
        }
    }

}
