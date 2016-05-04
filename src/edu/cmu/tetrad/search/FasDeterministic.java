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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the "fast adjacency search" for deterministic searches. At a given stage of the search, an edge x *-* y is
 * removed from the graph if S determines either x or y. , where S is a subset of size d either of adj(x) or of adj(y),
 * where d is the depth of the search. The fast adjacency search performs this procedure for each pair of adjacent edges
 * in the graph and for each depth d = 0, 1, 2, ..., d1, where d1 is the first such depth at which no edges can be
 * removed.
 *
 * @author Joseph Ramsey.
 */
public class FasDeterministic {

    /**
     * The search graph. It is assumed going in that all of the true adjacencies of x are in this graph for every node
     * x. It is hoped (i.e. true in the large sample limit) that true adjacencies are never removed.
     */

    private Graph graph;

    /**
     * The independence test.
     */
    private IndependenceTest test;

    /**
     * Specification of which edges are forbidden or required.
     */
    private Knowledge knowledge;

    /**
     * The maximum number of variables conditioned on in any conditional independence test. The value is -1 if depth is
     * unlimited, or a non-negative integer otherwise.
     */
    private int depth = Integer.MAX_VALUE;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new FastAdjacencySearch.
     */
    public FasDeterministic(Graph graph, IndependenceTest test) {
        this.graph = graph;
        this.test = test;
    }

    //==========================PUBLIC METHODS===========================//

    /**
     * Discovers all adjacencies in data.  The procedure is to remove edges in the graph which connect pairs of
     * variables which are independent conditional on some other set of variables in the graph (the "sepset"). These are
     * removed in tiers.  First, edges which are independent conditional on zero other variables are removed, then edges
     * which are independent conditional on one other variable are removed, then two, then three, and so on, until no
     * more edges can be removed from the graph.  The edges which remain in the graph after this procedure are the
     * adjacencies in the data.
     *
     * @return a SepSet, which indicates which variables are independent conditional on which other variables
     */
    public SepsetMap search() {
        TetradLogger.getInstance().log("info", "Starting Fast Adjacency Search.");

        // Remove edges forbidden both ways.
        List<Edge> edges = graph.getEdges();

        for (Edge edge1 : edges) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            if (getKnowledge().edgeForbidden(name1, name2) &&
                    getKnowledge().edgeForbidden(name2, name1)) {
                graph.removeEdge(edge1);
                TetradLogger.getInstance().log("edgeRemoved", "Removed " + edge1 + " because it was " +
                        "forbidden by background knowledge.");
            }
        }

        String message = "Depth = " + ((getDepth() == Integer
                .MAX_VALUE) ? "Unlimited" : Integer.toString(getDepth()));
        TetradLogger.getInstance().log("info", message);

        SepsetMap sepset = new SepsetMap();

        int _depth = getDepth();

        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }

        for (int d = 0; d <= _depth; d++) {
            if (!searchAtDepth(graph, test, getKnowledge(), sepset, d)) {
                break;
            }
        }

        TetradLogger.getInstance().log("info", "Finishing Fast Adjacency Search.");

        return sepset;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0.");
        }

        this.depth = depth;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set knowledge to null");
        }
        this.knowledge = knowledge;
    }

    //==============================PRIVATE METHODS======================/

    /**
     * Removes from the list of nodes any that cannot be parents of x given the background knowledge.
     */
    private List<Node> possibleParents(Node x, Node y, List<Node> nodes,
                                       Knowledge knowledge) {
        List<Node> possibleParents = new LinkedList<Node>();
        String _x = x.getName();
        String _y = y.getName();

        for (Node z : nodes) {
            String _z = z.getName();

            if (possibleParentOf(_z, _x, _y, knowledge)) {
                possibleParents.add(z);
            }
        }

        return possibleParents;
    }

    /**
     * Returns true just in case z is a possible parent of both x and y, in the sense that edges are not forbidden from
     * z to either x or y, and edges are not required from either x or y to z, according to background knowledge.
     */
    private boolean possibleParentOf(String z, String x, String y,
                                     Knowledge knowledge) {
        if (knowledge.edgeForbidden(z, x)) {
            return false;
        }

        if (knowledge.edgeForbidden(z, y)) {
            return false;
        }

        if (knowledge.edgeRequired(x, z)) {
            return false;
        }

        return !knowledge.edgeRequired(y, z);
    }

    /**
     * Performs one depth step of the adjacency search.
     *
     * @param graph     The search graph. This will be modified.
     * @param test      The independence test.
     * @param knowledge Background knowledge.
     * @param sepset    A mapping from {x, y} node sets to separating sets.
     * @param depth     The depth at which this step will be done.
     * @return true if there are more changes possible, false if not.
     */
    private boolean searchAtDepth(Graph graph, IndependenceTest test,
                                  Knowledge knowledge, SepsetMap sepset, int depth) {
        boolean more = false;
        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        for (Node x : nodes) {
            List<Node> b = new LinkedList<Node>(graph.getAdjacentNodes(x));

            nextEdge:
            for (Node y : b) {

                // This is the standard algorithm, without the v1 bias.
                List<Node> adjx = graph.getAdjacentNodes(x);
                adjx.remove(y);
                List<Node> ppx = possibleParents(x, y, adjx, knowledge);

                boolean noEdgeRequired =
                        knowledge.noEdgeRequired(x.getName(), y.getName());

                if (ppx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(ppx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, ppx);

//                        if (condSet.contains(x) || condSet.contains(y)) {
//                            throw new IllegalArgumentException(condSet + " .. " + x + " " + y);
//                        }

                        List<Node> _x = Collections.singletonList(x);

                        boolean determines = test.determines(_x, y);
                        boolean splitdetermines = test.determines(condSet, x) || test.determines(condSet, y);

                        if (determines) {
                            TetradLogger.getInstance().log("info", _x + " determines " + y);
                        }

                        if (splitdetermines) {
                            TetradLogger.getInstance().log("info", condSet + " split determines " + x + " and " + y);
                        }

                        if (!determines && !splitdetermines) {
                            boolean independent = false;
                            try {
                                independent = test.isIndependent(x, y, condSet);
                            } catch (Exception e) {
                                TetradLogger.getInstance().log("info", "The score for independence test " +
                                        SearchLogUtils.independenceFact(x, y, condSet) + " was undefined.");
                            }

                            if (independent && noEdgeRequired) {
                                graph.removeEdge(x, y);
                                sepset.set(x, y, new LinkedList<Node>(condSet));
                                continue nextEdge;
                            }
                        }
                    }
                }
            }

            if (graph.getAdjacentNodes(x).size() - 1 > depth) {
                more = true;
            }
        }

        return more;
    }
}


