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

package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.RandomUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Tests the functions of EndpointMatrixGraph and EdgeListGraph through the
 * Graph interface.
 *
 * @author Joseph Ramsey
 */
public final class TestGraphUtils extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGraphUtils(String name) {
        super(name);
    }

    public void testCreateRandomDag() {
        //        while (true) {
        Dag dag = GraphUtils.randomDag(50, 0, 50, 4, 3, 3, false);
        System.out.println(dag);
        //        }
    }

    public void testDirectedPaths() {
        Graph graph = GraphUtils.randomDag(6, 0, 10, 3, 3, 3, false);

        System.out.println("Graph = " + graph);

        for (int i = 0; i < graph.getNodes().size(); i++) {
            for (int j = 0; j < graph.getNodes().size(); j++) {
                Node node1 = graph.getNodes().get(i);
                Node node2 = graph.getNodes().get(j);

                System.out.println("Node1 = " + node1 + " Node2 = " + node2);

                List<List<Node>> directedPaths = GraphUtils.directedPathsFromTo(graph, node1, node2);

                for (int k = 0; k < directedPaths.size(); k++) {
                    System.out.println("Path " + k + ": " + directedPaths.get(k));
                }
            }
        }
    }

    public void testTreks() {
        Graph graph = GraphUtils.randomDag(10, 0, 15, 3, 3, 3, false);

        System.out.println("Graph = " + graph);

        for (int i = 0; i < graph.getNodes().size(); i++) {
            for (int j = 0; j < graph.getNodes().size(); j++) {
                Node node1 = graph.getNodes().get(i);
                Node node2 = graph.getNodes().get(j);

                System.out.println("Node1 = " + node1 + " Node2 = " + node2);

                List<List<Node>> treks = GraphUtils.treks(graph, node1, node2);

                for (int k = 0; k < treks.size(); k++) {
                    System.out.print("Trek " + k + ": ");
                    List<Node> trek = treks.get(k);

                    System.out.print(trek.get(0));

                    for (int m = 1; m < trek.size(); m++) {
                        Node n0 = trek.get(m - 1);
                        Node n1 = trek.get(m);

                        Edge edge = graph.getEdge(n0, n1);

                        Endpoint endpoint0 = edge.getProximalEndpoint(n0);
                        Endpoint endpoint1 = edge.getProximalEndpoint(n1);

                        System.out.print(endpoint0 == Endpoint.ARROW ? "<" : "-");
                        System.out.print("-");
                        System.out.print(endpoint1 == Endpoint.ARROW ? ">" : "-");

                        System.out.print(n1);
                    }

                    System.out.println();
                }
            }
        }
    }

    public void testGraphToDot() {
        long seed = 28583848283L;
        RandomUtil.getInstance().setSeed(seed);

        Graph g = GraphUtils.randomDag(5, 5, false);

        System.out.println(g);

        System.out.println(GraphUtils.graphToDot(g));

    }

    //    public void rtestMaxPathLength() {
    //        int numTests = 10;
    //        int n = 40;
    //        int k = 80;
    //
    //        System.out.println("numTests = " + numTests);
    //        System.out.println("n = " + n);
    //        System.out.println("k = " + k);
    //
    //        int sum = 0;
    //        int min = Integer.MAX_VALUE;
    //        int max = 0;
    //
    //        for (int i = 0; i < numTests; i++) {
    //            Dag dag = GraphUtils.createRandomDagC(n, 0, k);
    //            List tiers = dag.getTiers();
    //            sum += tiers.size();
    //            if (tiers.size() < min) {
    //                min = tiers.size();
    //            }
    //            if (tiers.size() > max) {
    //                max = tiers.size();
    //            }
    //        }
    //
    //        double ave = sum / (double) numTests;
    //
    //        System.out.println("OLD: Min = " + min + ", Max = " + max +
    //                ", average = " + ave);
    //
    //        sum = max = 0;
    //        min = Integer.MAX_VALUE;
    //
    //        for (int i = 0; i < numTests; i++) {
    //            Dag dag = GraphUtils.createRandomDagB(n, 0, k, 0.0, 0.0, 0.0);
    //            List tiers = dag.getTiers();
    //            sum += tiers.size();
    //            if (tiers.size() < min) {
    //                min = tiers.size();
    //            }
    //            if (tiers.size() > max) {
    //                max = tiers.size();
    //            }
    //        }
    //
    //        ave = sum / (double) numTests;
    //
    //        System.out.println("1: Min = " + min + ", Max = " + max +
    //                ", average = " + ave);
    //
    //        sum = max = 0;
    //        min = Integer.MAX_VALUE;
    //        int totK = 0;
    //
    //        for (int i = 0; i < numTests; i++) {
    ////            System.out.print(".");
    //            Dag dag = GraphUtils.createRandomDagC(n, 0, k, 0.0, 0.0, 0.0);
    //            System.out.println("test " + (i + 1) + ": num edges = " + dag.getNumEdges());
    //            System.out.flush();
    //
    //            List tiers = dag.getTiers();
    //            sum += tiers.size();
    //            if (tiers.size() < min) {
    //                min = tiers.size();
    //            }
    //            if (tiers.size() > max) {
    //                max = tiers.size();
    //            }
    //
    //            totK += dag.getNumEdges();
    //        }
    //
    //        ave = sum / (double) numTests;
    //
    //        System.out.println("\n2: Min = " + min + ", Max = " + max +
    //                ", average = " + ave + ", avenumedges = " + totK / (double) numTests);
    //    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestGraphUtils.class);
    }
}



