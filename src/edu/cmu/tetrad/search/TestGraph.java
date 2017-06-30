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

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.SearchLogUtils;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Tests the functions of EndpointMatrixGraph and EdgeListGraph through the Graph interface.
 *
 * @author Joseph Ramsey
 */
public final class TestGraph extends TestCase {
    private Node x1, x2, x3, x4, x5;
    private Graph graph;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGraph(String name) {
        super(name);
    }

    @Override
	public void setUp() {
        x1 = new GraphNode("x1");
        x2 = new GraphNode("x2");
        x3 = new GraphNode("x3");
        x4 = new GraphNode("x4");
        x5 = new GraphNode("x5");
        graph = new EdgeListGraph();
        //        graph = new EndpointMatrixGraph();
    }


    /**
     * Tests to see if d separation facts are symmetric.
     */
    public void testDSeparation() {
        Graph graph = GraphUtils.randomDag(20, 0, 30, 3, 3, 3, false);
        List<Node> nodes = graph.getNodes();

        int depth = 2;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                List<Node> theRest = new ArrayList<Node>(nodes);
                theRest.remove(x);
                theRest.remove(y);

                DepthChoiceGenerator gen = new DepthChoiceGenerator(theRest.size(), depth);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    List<Node> z = new LinkedList<Node>();

                    for (int k = 0; k < choice.length; k++) {
                        z.add(theRest.get(choice[k]));
                    }

                    if (graph.isDSeparatedFrom(x, y, z) != graph.isDSeparatedFrom(y, x, z)) {
                        fail(SearchLogUtils.independenceFact(x, y, z) + " should have same d-sep result as " +
                                SearchLogUtils.independenceFact(y, x, z));
                    }
                }

            }
        }
    }

    /**
     * Tests to see if running PC on a graph using d separation returns the pattern of that graph.
     */
    public void testDSeparation2() {
        Graph graph = GraphUtils.randomDag(20, 0, 30, 3, 3, 3, false);
        IndTestDSep test = new IndTestDSep(graph);
        Pc pc = new Pc(test);
        Graph pattern = pc.search();
        Graph pattern2 = SearchGraphUtils.patternFromDag(pattern);
        assertEquals(pattern, pattern2);
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(edu.cmu.tetrad.graph.TestEdgeListGraph.class);
    }
}
