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
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemImInitializationParams;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestGes extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGes(String name) {
        super(name);
    }

//    public void setUp() throws Exception {
//        TetradLogger.getInstance().addOutputStream(System.out);
//        TetradLogger.getInstance().setForceLog(true);
//    }
//
//
//    public void tearDown() {
//        TetradLogger.getInstance().setForceLog(false);
//        TetradLogger.getInstance().removeOutputStream(System.out);
//    }


    public void testBlank() {
        // Blank to keep the automatic JUnit runner happy.
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3 --> X4. Should produce X1 -- X2, X1 -- X3,
     * X2 --> X4, X3 --> X4.
     */
    public void rtestSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first orientation.
     */
    public void rtestSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first orientation.
     */
    public void rtestSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A---B,C---A,B-->C,C-->D,A-->D",
                knowledge);
    }

    public void rtestSearch3_5() {
        Dag dag = GraphUtils.randomDag(20, 0, 20, 5, 5, 5, false);

//        Node x = new GraphNode("X");
//
//        Dag dag = new Dag(Collections.singletonList(x));

        System.out.println(dag);

        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(100, false);

        Ges ges = new Ges(dataSet);
        Graph graph = ges.search();

        System.out.println(graph);

        Graph dag2 = SearchGraphUtils.chooseDagInPattern(graph);
        SemPm pm2 = new SemPm(dag2);

        SemEstimator est = new SemEstimator(dataSet, pm2);
        est.estimate();
        SemIm estIm = est.getEstimatedSem();
        double estBicScore = estIm.getBicScore();
        System.out.println("Estimate BIC = " + estBicScore);

        double gesBicScore = ges.scoreGraph(dag);
        System.out.println("GES score = " + gesBicScore);

        System.out.println("bic / ges = " + gesBicScore / estBicScore);
    }

    public void rtestSearch4() {
        int numVars = 40;
        int numEdges = numVars;
        int sampleSize = 200;
        boolean latentDataSaved = false;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm pm = new SemPm(trueGraph);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(sampleSize, false);

//        BayesPm bayesPm = new BayesPm(trueGraph);
//        MlBayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
//        DataSet dataSet = bayesIm.simulateData(sampleSize, latentDataSaved);

        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(trueGraph);
//        ges.setStructurePrior(0.1);
//        ges.setSamplePrior(10);

        // Run search
        Graph pattern = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println("\nResult graph:");
        System.out.println(pattern);

        int adjFp = GraphUtils.countAdjErrors(pattern, trueGraph);
        int adjFn = GraphUtils.countAdjErrors(trueGraph, pattern);

        System.out.println("adj fp = " + adjFp + " adjFn = " + adjFn);
    }

    /**
     * Iterated so I can collect stats.
     */
    public void rtestSearch4a() {
        int numVars = 30;


        int numEdges = numVars;
        int sampleSize = 1000;
        int numIterations = 10;

        double sumFp = 0.0;
        double sumFn = 0.0;

        NumberFormat nf = new DecimalFormat("0.00");
        System.out.println("\tADJ_FP\tADJ_FN");

        for (int count = 0; count < numIterations; count++) {
            Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                    5, false);

            SemPm pm = new SemPm(trueGraph);
            SemIm im = new SemIm(pm);
            DataSet dataSet = im.simulateData(sampleSize, false);

            Ges ges = new Ges(dataSet);
            ges.setTrueGraph(trueGraph);
            Graph pattern = ges.search();

            int adjFp = GraphUtils.countAdjErrors(pattern, trueGraph);
            int adjFn = GraphUtils.countAdjErrors(trueGraph, pattern);

            sumFp += adjFp;
            sumFn += adjFn;

            System.out.println((count + 1) + "\t" + adjFp + "\t" + adjFn);
        }

        double avgFp = sumFp / numIterations;
        double avgFn = sumFn / numIterations;

        System.out.println("Means" + "\t" + nf.format(avgFp) + "\t" + nf.format(avgFn));
    }

    public void testSearch5() {
        int numVars = 10;
        int numEdges = 20;
        int sampleSize = 20000;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

//        SemPm bayesPm = new SemPm(trueGraph);
//        SemIm bayesIm = new SemIm(bayesPm);

        System.out.println("********** SAMPLE SIZE = " + sampleSize);

//            RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);

//            BayesPm semPm = new BayesPm(trueGraph);
//            BayesIm bayesIm = new MlBayesIm(semPm, MlBayesIm.RANDOM);
//            DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        SemPm semPm = new SemPm(trueGraph);
        SemIm bayesIm = new SemIm(semPm);
        DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(trueGraph);

        // Run search
        Graph resultGraph = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
    }

    public void testSearch6() {
        Dag trueGraph = GraphUtils.randomDag(10, 10, false);

        int sampleSize = 1000;

        SemPm semPm = new SemPm(trueGraph);
        SemIm bayesIm = new SemIm(semPm);
        DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        Ges ges = new Ges(dataSet);

        Graph pattern = ges.search();

        System.out.println("True graph = " + SearchGraphUtils.patternForDag(trueGraph));
        System.out.println("Pattern = " + pattern);
    }

    public void testSearch7() {
//        TetradLogger.getInstance().clear();
//
//        TetradLogger.getInstance().removeOutputStream(System.out);
//        TetradLogger.getInstance().setForceLog(false);
//        TetradLogger.getInstance().setEventsToLog("insertedEdges", "deletedEdges", "directedEdges");

        Dag trueGraph = GraphUtils.randomDag(500, 500, false);

        int sampleSize = 1000;

        SemPm semPm = new SemPm(trueGraph);
        SemIm bayesIm = new SemIm(semPm);
        DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        Ges ges = new Ges(dataSet);

        long start = System.currentTimeMillis();

        Graph pattern = ges.search();

        long stop = System.currentTimeMillis();

        Graph truePattern = SearchGraphUtils.patternForDag(trueGraph);

        System.out.println(GraphUtils.graphComparisonString("GES pattern ", pattern, "True pattern", truePattern, false));


        System.out.println("Elapsed time = " + (start - stop) / 1000 + " seconds ");

    }

//    public void testSearch8() {
//        System.out.println("seed = " + RandomUtil.getInstance().getSeed());
//
//        int numNodes = 1000;
//
//        Dag trueGraph = GraphUtils.randomDag(numNodes, numNodes, false);
//        Graph truePattern = SearchGraphUtils.patternForDag(trueGraph);
//
//        System.out.println("True graph = " + trueGraph);
//
//        int sampleSize = 500;
////        int subsampleSize = 1000;
//
////        SemPm pm = new SemPm(trueGraph);
////        SemImInitializationParams params = new SemImInitializationParams();
//////        params.setCoefSymmetric(false);
////        SemIm im = new SemIm(pm, params);
//
////        DoubleMatrix2D impliedCovar = im.getImplCovar();
////        CovarianceMatrix trueCovar = new CovarianceMatrix(pm.getVariableNodes(), impliedCovar, sampleSize);
//
//        System.out.println("Large sem simulator");
//        LargeSemSimulator simulator = new LargeSemSimulator(trueGraph);
//
//        System.out.println("... simulating data");
//        DataSet dataSet = simulator.simulateDataAcyclic(sampleSize);
//
////        DataSet dataSet = new ColtDataSet(subsampleSize, _dataSet.getVariables());
////
////        for (int i = 0; i < dataSet.getNumRows(); i++) {
////            for (int j = 0; j < dataSet.getNumColumns(); j++) {
////                dataSet.setDouble(i, j, _dataSet.getDouble(i, j));
////            }
////        }
//
//        System.out.println("Making cov");
//        CovarianceMatrix cov = new CovarianceMatrix(dataSet);
////        System.out.println("Done making cov");
//
////        Jcpc ges = new Jcpc(new IndTestFisherZ(cov, 0.00001));
//
//        Ges3 ges = new Ges3(dataSet);
////        ges.setStoreGraphs(false);
//
//        ges.setTrueGraph(trueGraph);
////        ges.setMaxEdgesAdded((int) (numNodes * 1));
////        ges.setPenaltyDiscount(50);
//
//        long start = System.currentTimeMillis();
//
////        Graph pattern = ges.search();
//
//        long stop = System.currentTimeMillis();
//
//
////        System.out.println(GraphUtils.graphComparisonString("GES pattern ", pattern, "True pattern", truePattern, false));
////
////        System.out.println("Elapsed time = " + (stop - start) / 1000 + " seconds ");
////
////
////        System.out.println(pattern);
//
//
//        System.out.println("JCPC");
//
//        List<ICovarianceMatrix> covs = new ArrayList<ICovarianceMatrix>();
//        covs.add(cov);
//
//        double alpha = 1e-3;
//        CpcSmo cpc = new CpcSmo(covs);
//        cpc.setDepth(6);
//        cpc.setAlpha(alpha);
//        Graph initialGraph = cpc.search();
//        System.out.println(GraphUtils.graphComparisonString("CPC pattern ", initialGraph, "True pattern", truePattern, false));
//
//        Jcpc search = new Jcpc(new IndTestFisherZ(covs.get(0), alpha));
//
////        search.setPcDepth(5);
//        search.setInitialGraph(initialGraph);
//        search.setMaxAdjacencies(6);
//        search.setMaxDescendantPath(0);
//        search.setMaxIterations(30);
//        search.setOrientationDepth(4);
////        search.setPathBlockingSet(Jcpc.PathBlockingSet.SMALL);
//
//        Graph patternJcpc = search.search();
//        System.out.println(GraphUtils.graphComparisonString("JCPC pattern ", patternJcpc, "True pattern", truePattern, false));
//    }

    public void testSearch9() {
//        TetradLogger.getInstance().clear();

//        System.out.println("seed = " + RandomUtil.getInstance().getSeed());
//        RandomUtil.getInstance().setSeed(1298484989212L); // 1-4 not removed--fixed.
//        RandomUtil.getInstance().setSeed(1298492781796L);
//        RandomUtil.getInstance().setSeed(1298556603214L);
        System.out.println("seed = " + RandomUtil.getInstance().getSeed());

//        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(false);
//        TetradLogger.getInstance().setEventsToLog("insertedEdges", "deletedEdges", "directedEdges");

        Graph trueGraph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");

        trueGraph.addNode(x1);
        trueGraph.addNode(x2);
        trueGraph.addNode(x3);
        trueGraph.addNode(x4);
        trueGraph.addNode(x5);

        trueGraph.addDirectedEdge(x1, x3);
        trueGraph.addDirectedEdge(x2, x3);
        trueGraph.addDirectedEdge(x3, x4);
        trueGraph.addDirectedEdge(x4, x5);
        trueGraph.addDirectedEdge(x1, x5);
        trueGraph.addDirectedEdge(x2, x5);

        System.out.println("True graph = " + trueGraph);

//        RandomUtil.getInstance().setSeed(1298503214L);

        int sampleSize = 500000;

        System.out.println("Large sem simulator");
        SemPm pm = new SemPm(trueGraph);
        SemImInitializationParams params = new SemImInitializationParams();
//        params.setCoefSymmetric(false);
        SemIm im = new SemIm(pm, params);

//        DoubleMatrix2D impliedCovar = im.getImplCovar();
//        CovarianceMatrix trueCovar = new CovarianceMatrix(pm.getVariableNodes(), impliedCovar, sampleSize);

//        System.out.println(trueCovar);

//        LargeSemSimulator simulator = new LargeSemSimulator(trueGraph);

//        System.out.println(im);

        System.out.println("... simulating data");
        DataSet dataSet = im.simulateData(sampleSize, false);
//        DataSet dataSet = simulator.simulateDataAcyclic(sampleSize);

//        CovarianceMatrix cov = new CovarianceMatrix(dataSet);
//
//        for (int i = 1; i <= 5; i++) {
//            System.out.println("Variance of " + cov.getVariable("X" + i) + " = " + cov.getValue(i - 1, i - 1));
//        }

        Ges3 ges = new Ges3(dataSet);
        ges.setStoreGraphs(false);

        ges.setTrueGraph(trueGraph);
//        ges.setMaxEdgesAdded((int) (numNodes * 1));
//        ges.setPenaltyDiscount(5);

        long start = System.currentTimeMillis();

        Graph pattern = ges.search();

        long stop = System.currentTimeMillis();

        Graph truePattern = SearchGraphUtils.patternForDag(trueGraph);

        System.out.println(GraphUtils.graphComparisonString("GES pattern ", pattern, "True pattern", truePattern, false));

        System.out.println("Elapsed time = " + (stop - start) / 1000 + " seconds ");


        System.out.println(pattern);


        System.out.println("JCPC");

        Jcpc search = new Jcpc(new IndTestFisherZ(dataSet, 0.001));
        Graph patternJcpc = search.search();
        System.out.println(GraphUtils.graphComparisonString("JCPC pattern ", patternJcpc, "True pattern", truePattern, false));
        System.out.println(patternJcpc);
    }

    public void test10() {
        NumberFormat nf = new DecimalFormat("0.0000000");

//        for (int n = 25; n <= 10000; n += 25) {
//
//            double v = 50 + n * 0.05;
//
//            double y = Math.exp((v + Math.log(n)) / -(n / 2.0));
//            double p = ProbUtils.fCdf(y, n, n);
//
//            System.out.println(n + " " + nf.format(y) + " " + nf.format(p));
//        }

        for (int n = 25; n <= 10000; n += 25) {
            double _p = .01;

            // Find the value for v that will yield p = _p

            for (double v = 0.0; ; v += 1) {
                double f = Math.exp((v - Math.log(n)) / (n / 2.0));
                double p = 1 - ProbUtils.fCdf(f, n, n);
//                System.out.println(v + " " + p + " " + _p);

                if (p <= _p) {
                    System.out.println(n + " " + nf.format(p) + " " + nf.format(v));
                    break;
                }
            }
        }

//        for (int n = 25; n <= 10000; n+=25) {
//            double f = 1.0 / (Math.exp(- 2.0 * Math.log(n) / n));
//            double p = 1 - ProbUtils.fCdf(f, n, n);
//            System.out.println(n + " " + f + " "  + p);
//        }

//        double x = 1 - ProbUtils.fCdf(1.09, 5000, 5000);
//
//        System.out.println(x);


    }

//    public void testTemp() {
//        String s = "E1";
//
//        for (int i = 2; i <= 209; i++) {
//            s += "+E" + i;
//        }
//
//        System.out.println(s);
//    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci is equivalent to the given output
     * graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        DataSet dataSet = semIM.simulateData(500, false);

        // Set up search.
        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(graph);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = ges.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci is equivalent to the given output
     * graph.
     */
    private void checkWithKnowledge(String inputGraph, String outputGraph,
                                    Knowledge knowledge) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        DataSet dataSet = semIM.simulateData(1000, false);

        // Set up search.
        Ges ges = new Ges(dataSet);
        ges.setKnowledge(knowledge);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println(knowledge);
        System.out.println("Input graph:");
        System.out.println(graph);
        System.out.println("Result graph:");
        System.out.println(resultGraph);

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestGes.class);
    }
}



