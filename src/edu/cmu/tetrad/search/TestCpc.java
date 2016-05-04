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
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.IndTestFisherZ;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestCpc extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestCpc(String name) {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    @Override
	public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3 --> X4. Should produce X1 -- X2, X1 -- X3,
     * X2 --> X4, X3 --> X4.
     */
    public void testSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first orientation.
     */
    public void testSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first orientation.
     */
    public void testSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A-->B,C-->B,A-->D,C-->D",
                knowledge);
    }

    public void showInefficiency() {

        int numVars = 20;
        int numEdges = 20;
        int maxSample = 2000;
        boolean latentDataSaved = false;
        int increment = 1;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm semPm = new SemPm(trueGraph);
        SemIm semIm = new SemIm(semPm);
        DataSet _dataSet = semIm.simulateData(maxSample, latentDataSaved);
        Graph previousResult = null;

        for (int n = 3; n <= maxSample; n += increment) {
            int[] rows = new int[n];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = i;
            }

            DataSet dataSet = _dataSet.subsetRows(rows);
            IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);

            Cpc search = new Cpc(test);
            Graph resultGraph = search.search();

            if (previousResult != null) {
                List<Edge> resultEdges = resultGraph.getEdges();
                List<Edge> previousEdges = previousResult.getEdges();

                List<Edge> addedEdges = new LinkedList<Edge>();

                for (Edge edge : resultEdges) {
                    if (!previousEdges.contains(edge)) {
                        addedEdges.add(edge);
                    }
                }

                List<Edge> removedEdges = new LinkedList<Edge>();

                for (Edge edge : previousEdges) {
                    if (!resultEdges.contains(edge)) {
                        removedEdges.add(edge);
                    }
                }

                if (!addedEdges.isEmpty() && !removedEdges.isEmpty()) {
                    System.out.println("\nn = " + n + ":");

                    if (!addedEdges.isEmpty()) {
                        System.out.println("Added: " + addedEdges);
                    }

                    if (!removedEdges.isEmpty()) {
                        System.out.println("Removed: " + removedEdges);
                    }
                }
            }
            previousResult = resultGraph;
        }

        System.out.println("Final graph = " + previousResult);
    }

    public void test7() {
        int numVars = 6;
        int numEdges = 6;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm semPm = new SemPm(trueGraph);
        SemIm semIm = new SemIm(semPm);
        DataSet _dataSet = semIm.simulateData(1000, false);

        IndependenceTest test = new IndTestFisherZ(_dataSet, 0.05);

        Cpc search = new Cpc(test);
        Graph resultGraph = search.search();

    }

    public void test8() {
        try {
//            TetradLogger.getInstance().addOutputStream(System.out);
//            TetradLogger.getInstance().setForceLog(true);
            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);
            DataSet dataSet = reader.parseTabular(new File("test_data/i37.txt"));
            IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);
            Cpc cpc = new Cpc(test);
            Graph epattern = cpc.search();
            for (Node node : epattern.getNodes()) {
                System.out.println("Adjacents for " + node + " are " + epattern.getAdjacentNodes(node));
            }
            System.out.println(epattern);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tripleAccuracy() {
        int success = 0;
        int fail = 0;
        int totBidirected = 0;
        int numCyclic = 0;

        int numCorrectColliders = 0;
        int numCorrectNoncolliders = 0;
        int numEstColliders = 0;
        int numEstNoncolliders = 0;
        int numEstAmbiguous = 0;

        for (int i = 0; i < 100; i++) {
            TetradLogger.getInstance().log("info", "# " + (i + 1));

            Graph graph = GraphUtils.randomDag(20, 0, 20, 4, 4, 4, false);
            SemPm pm = new SemPm(graph);
            SemIm im = new SemIm(pm);
            DataSet dataSet = im.simulateData(1000, false);
            IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);
            Cpc search = new Cpc(test);
            Graph graph2 = search.search();

            ChoiceGenerator cg = new ChoiceGenerator(graph.getNumNodes(), 3);
            int[] choice;
            List<Node> nodes = graph.getNodes();

            while ((choice = cg.next()) != null) {
                Node node0 = nodes.get(choice[0]);
                Node node1 = nodes.get(choice[1]);
                Node node2 = nodes.get(choice[2]);


                Node node02 = graph2.getNode(node0.getName());
                Node node12 = graph2.getNode(node1.getName());
                Node node22 = graph2.getNode(node2.getName());

                if (graph2.isAdjacentTo(node02, node12) && graph2.isAdjacentTo(node12, node22)) {
                    if (graph2.isAmbiguousTriple(node02, node12, node22)) {
                        numEstAmbiguous++;
                    } else

//                    if (!graph2.isAmbiguous(new Triple(node02, node12, node22))) {
//                        continue;
//                    }

                        if (graph2.isDefCollider(node02, node12, node22)) {
                            numEstColliders++;

                            if (graph.isDefCollider(node0, node1, node2)) {
                                numCorrectColliders++;
                            }
                        } else {
                            numEstNoncolliders++;

                            if (graph.isAdjacentTo(node0, node1) && graph.isAdjacentTo(node1, node2)) {
                                if (!graph.isDefCollider(node0, node1, node2)) {
                                    numCorrectNoncolliders++;
                                }
                            }
                        }
                }
            }
        }

        double percentCorrectColliders = 100 * ((double) numCorrectColliders / numEstColliders);
        double percentCorrectNoncolliders = 100 * ((double) numCorrectNoncolliders / numEstNoncolliders);
        double percentAmbiguousTriples = 100 * ((double) numEstAmbiguous / (numEstColliders + numEstNoncolliders));
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        TetradLogger.getInstance().log("info", "# estimated colliders = " + numEstColliders);
        TetradLogger.getInstance().log("info", "# estimated noncolliders = " + numEstNoncolliders);
        TetradLogger.getInstance().log("info", "# estimated ambiguous = " + numEstAmbiguous);
        TetradLogger.getInstance().log("info", "# correct colliders = " + numCorrectColliders);
        TetradLogger.getInstance().log("info", "# correct noncolliders = " + numCorrectNoncolliders);
        TetradLogger.getInstance().log("info", "% correct colliders = " + nf.format(percentCorrectColliders));
        TetradLogger.getInstance().log("info", "% correct noncolliders = " + nf.format(percentCorrectNoncolliders));
        TetradLogger.getInstance().log("info", "% ambiguous triples = " + nf.format(percentAmbiguousTriples));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci is equivalent to the given output
     * graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);

        // Set up search.
        IndependenceTest independence = new IndTestDSep(graph);
        GraphSearch pcSearch = new Cpc(independence);

        // Run search
        Graph resultGraph = pcSearch.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
        System.out.println("\nTrue graph:");
        System.out.println(trueGraph);

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
//        IndependenceTest independence = new IndTestGraph(graph);
        IndependenceTest independence = new IndTestFisherZ(dataSet, 0.05);
        Cpc cpc = new Cpc(independence);
        cpc.setKnowledge(knowledge);

        // Run search
        Graph resultGraph = cpc.search();

        // Build comparison graph.
//        Graph trueGraph = GraphConverter.convert(outputGraph);
        GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
//        System.out.println("\nKnowledge:");
        System.out.println(knowledge);
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
//        System.out.println("\nTrue graph:");
//        System.out.println(trueGraph);

        // Do test.
//        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestCpc.class);
    }
}

