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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test for the procedure which finds a DAG which is an instance of a pattern.
 *
 * @author Frank Wimberly
 */
public class TestPatternToDag extends TestCase {
    public TestPatternToDag(String name) {
        super(name);
    }

    public void testChickeringAlgorithm() {

        Node A = new GraphNode("A");
        Node B = new GraphNode("B");
        Node C = new GraphNode("C");
        Node D = new GraphNode("D");
        Node E = new GraphNode("E");

        Pattern pattern = new Pattern();
        pattern.addNode(A);
        pattern.addNode(B);
        pattern.addNode(C);
        pattern.addNode(D);
        pattern.addNode(E);

        pattern.addUndirectedEdge(A, B);
        pattern.addUndirectedEdge(B, C);
        pattern.addUndirectedEdge(C, E);
        pattern.addUndirectedEdge(A, D);
        pattern.addUndirectedEdge(B, D);

        pattern.addUndirectedEdge(C, D);
        pattern.addUndirectedEdge(E, D);

        Pattern pattern1 = new Pattern(pattern);
        PatternToDag search1 = new PatternToDag(pattern1);
        Dag dag1 = search1.patternToDagDorTarsi();

        Graph correctDag = GraphConverter.convert(
                "B-->A,D-->A,C-->B,D-->B,E-->C,D-->C,E-->D");

        System.out.println("Correct DAG");
        System.out.println(correctDag);

        System.out.println("Result 1:  ");
        System.out.println(dag1);

        assertTrue(correctDag.equals(dag1));

        Pattern pattern2 = new Pattern(pattern);
        PatternToDag search2 = new PatternToDag(pattern2);
        Graph dag2 = search2.patternToDagMeekRules();

        System.out.println("Result 2:  ");
        System.out.println(dag2);
    }

    public static Test suite() {
        return new TestSuite(TestPatternToDag.class);
    }
}



