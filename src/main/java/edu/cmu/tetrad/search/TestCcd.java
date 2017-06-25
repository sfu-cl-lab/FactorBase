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

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Tests the Ccd algorithm.
 *
 * @author Frank Wimberly
 */
public class TestCcd extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestCcd(String name) {
        super(name);
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestCcd.class);
    }

    /**
     * From "CcdTester".
     */
    public void testCcd() {
        Node a = new ContinuousVariable("A");
        Node b = new ContinuousVariable("B");
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");

        Graph graph = new EdgeListGraph();

        try {
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(x);
            graph.addNode(y);
        } catch (Exception e) {
            fail("Cant add nodes");
        }

        try {
            graph.addDirectedEdge(a, x);
        } catch (Exception e) {
            fail("Cant add edge A to X");
        }

        try {
            graph.addDirectedEdge(b, y);
        } catch (Exception e) {
            fail("Cant add edge B to Y");
        }

        try {
            graph.addDirectedEdge(x, y);
        } catch (Exception e) {
            fail("Cant add edge X to Y");
        }

        try {
            graph.addDirectedEdge(y, x);
        } catch (Exception e) {
            fail("Cant add edge Y to X");
        }

        IndependenceTest test = new IndTestDSep(graph);
        List <Node> listOfVars = test.getVariables();

        System.out.println("FIRST CASE");
        System.out.println("List of vars:  ");

        for (Node listOfVar : listOfVars) {
            System.out.println(listOfVar.getName());
        }

        Ccd ccd = new Ccd(test);
        Graph outPag = ccd.search();

        //Pag outPag = new Pag(out);
        //outPag.setUnderLineTriples(ccd.getUnderLineTriples());
        //outPag.setDottedUnderLineTriples(ccd.getDottedUnderLineTriples());

        System.out.println("Output PAG for 1st case:  ");
        System.out.println(outPag);
        System.out.println("\n\n");

        //Dag dag = new Dag(graph);

        boolean b1 = PagUtils.graphInPagStep1(outPag, graph);
        if (!b1) {
            fail();
        } else {
            System.out.println("Step 1 OK");
        }

        boolean b2 = PagUtils.graphInPagStep2(outPag, graph);
        if (!b2) {
            fail();
        } else {
            System.out.println("Step 2 OK");
        }

        boolean b3 = PagUtils.graphInPagStep3(outPag, graph);
        if (!b3) {
            fail();
        } else {
            System.out.println("Step 3 OK");
        }

        boolean b4 = PagUtils.graphInPagStep4(outPag, graph);
        if (!b4) {
            fail();
        } else {
            System.out.println("Step 4 OK");
        }

        boolean b5 = PagUtils.graphInPagStep5(outPag, graph);
        if (!b5) {
            fail();
        } else {
            System.out.println("Step 5 OK");
        }

        boolean b6 = PagUtils.graphInPagStep6(outPag, graph);
        if (!b6) {
            fail();
        } else {
            System.out.println("Step 6 OK");
        }

        System.out.println("\n\n");
    }

    /**
     * From CcdTesterC.
     */
    public void testCcdC() {

        /*
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        Node d = new GraphNode("D");
        Node e = new GraphNode("E");
        */

        Node a = new ContinuousVariable("A");
        Node b = new ContinuousVariable("B");
        Node c = new ContinuousVariable("C");
        Node d = new ContinuousVariable("D");
        Node e = new ContinuousVariable("E");

        a.setNodeType(NodeType.MEASURED);
        b.setNodeType(NodeType.MEASURED);
        c.setNodeType(NodeType.MEASURED);
        d.setNodeType(NodeType.MEASURED);
        e.setNodeType(NodeType.MEASURED);

        Graph graph = new EdgeListGraph();

        try {
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addNode(e);
        } catch (Exception ex) {
            fail("Cant add nodes");
        }

        try {
            graph.addDirectedEdge(a, b);
        } catch (Exception ex) {
            fail("Cant add edge A to B");
        }

        try {
            graph.addDirectedEdge(b, c);
        } catch (Exception ex) {
            fail("Cant add edge B to C");
        }

        try {
            graph.addDirectedEdge(c, b);
        } catch (Exception ex) {
            fail("Cant add edge C to B");
        }

        try {
            graph.addDirectedEdge(c, d);
        } catch (Exception ex) {
            fail("Cant add edge C to D");
        }

        try {
            graph.addDirectedEdge(d, c);
        } catch (Exception ex) {
            fail("Cant add edge D to C");
        }

        try {
            graph.addDirectedEdge(e, d);
        } catch (Exception ex) {
            fail("Cant add edge E to D");
        }

        IndependenceTest test = new IndTestDSep(graph);

        List <Node> listOfVars = test.getVariables();

        System.out.println("SECOND CASE");
        System.out.println("List of vars:  ");

        for (Node var : listOfVars) {
            System.out.println(var.getName());
        }

        Ccd ccd = new Ccd(test);
        //ccd.search();

        Graph outPag = ccd.search();

        //Pag outPag = new Pag(out);
        //outPag.setUnderLineTriples(ccd.getUnderLineTriples());
        //outPag.setDottedUnderLineTriples(ccd.getDottedUnderLineTriples());

        System.out.println("Output PAG for 2nd case:  ");
        System.out.println(outPag);
        System.out.println("\n\n");

        //Dag dag = new Dag(graph);

        boolean b1 = PagUtils.graphInPagStep1(outPag, graph);
        if (!b1) {
            fail();
        } else {
            System.out.println("Step 1 OK");
        }

        boolean b2 = PagUtils.graphInPagStep2(outPag, graph);
        if (!b2) {
            fail();
        } else {
            System.out.println("Step 2 OK");
        }

        boolean b3 = PagUtils.graphInPagStep3(outPag, graph);
        if (!b3) {
            fail();
        } else {
            System.out.println("Step 3 OK");
        }

        boolean b4 = PagUtils.graphInPagStep4(outPag, graph);
        if (!b4) {
            fail();
        } else {
            System.out.println("Step 4 OK");
        }

        boolean b5 = PagUtils.graphInPagStep5(outPag, graph);
        if (!b5) {
            fail();
        } else {
            System.out.println("Step 5 OK");
        }

        boolean b6 = PagUtils.graphInPagStep6(outPag, graph);
        if (!b6) {
            fail();
        } else {
            System.out.println("Step 6 OK");
        }

        System.out.println("\n\n");

    }

    public void testRandom() {
//        RandomUtil.getInstance().setSeed(502938L);

        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
        Dag dag = GraphUtils.randomDag(20, 0, 20, 3, 3, 4, false);
        Graph graph = GraphUtils.addCycles(dag, 7, 2);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(1000, false);

        Ccd ccd = new Ccd(new IndTestFisherZ(data, 0.01));

        Graph pag = ccd.search();

        System.out.println(pag);
    }
}



