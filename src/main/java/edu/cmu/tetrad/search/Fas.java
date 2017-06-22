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

import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.SearchLogUtils;
import edu.cmu.tetrad.search.SepsetMap;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the "fast adjacency search" used in several causal algorithms in this package. In the fast adjacency
 * search, at a given stage of the search, an edge X*-*Y is removed from the graph if X _||_ Y | S, where S is a subset
 * of size d either of adj(X) or of adj(Y), where d is the depth of the search. The fast adjacency search performs this
 * procedure for each pair of adjacent edges in the graph and for each depth d = 0, 1, 2, ..., d1, where d1 is either
 * the maximum depth or else the first such depth at which no edges can be removed. The interpretation of this adjacency
 * search is different for different algorithms, depending on the assumptions of the algorithm. A mapping from {x, y} to
 * S({x, y}) is returned for edges x *-* y that have been removed.
 *
 * @author Joseph Ramsey.
 */
public class Fas {

    /**
     * The search graph. It is assumed going in that all of the true adjacencies of x are in this graph for every node
     * x. It is hoped (i.e. true in the large sample limit) that true adjacencies are never removed.
     */
    private Graph graph;

    /**
     * The independence test. This should be appropriate to the types
     */
    private IndependenceTest test;

    /**
     * Specification of which edges are forbidden or required.
     */
    private IKnowledge knowledge = new Knowledge();

    /**
     * The maximum number of variables conditioned on in any conditional independence test. If the depth is -1, it will
     * be taken to be the maximum value, which is 1000. Otherwise, it should be set to a non-negative integer.
     */
    private int depth = 1000;

    /**
     * The number of independence tests.
     */
    private int numIndependenceTests;


    /**
     * The logger, by default the empty logger.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * The true graph, for purposes of comparison. Temporary.
     */
    private Graph trueGraph;

    /**
     * The number of false dependence judgements, judged from the true graph using d-separation. Temporary.
     */
    private int numFalseDependenceJudgments;

    /**
     * The number of dependence judgements. Temporary.
     */
    private int numDependenceJudgement;

    /**
     * The sepsets found during the search.
     */
    private SepsetMap sepset;

    /**
     * True if this is being run by FCI--need to skip the knowledge forbid step.
     */
    private boolean fci = false;

//    private List<Double> pValues = new ArrayList<Double>();

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new FastAdjacencySearch.
     */
    public Fas(Graph graph, IndependenceTest test) {
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
    public Graph search() {
        this.logger.log("info", "Starting Fast Adjacency Search.");
        // Remove edges forbidden both ways.
        List<Edge> edges = graph.getEdges();

//        logger.log("info", "Edges: " + edges);

        if (!isFci()) {
            for (Edge _edge : edges) {
                String name1 = _edge.getNode1().getName();
                String name2 = _edge.getNode2().getName();

                if (knowledge.edgeForbidden(name1, name2) &&
                        knowledge.edgeForbidden(name2, name1)) {
                    graph.removeEdge(_edge);

                    this.logger.log("edgeRemoved", "Removed " + _edge + " because it was " +
                            "forbidden by background knowledge.");

                }
            }
        }

//        this.logger.info("Depth = " + ((depth == Integer
//               .MAX_VALUE) ? "Unlimited" : Integer.toString(depth)));

        SepsetMap sepset = new SepsetMap();

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }

        for (int d = 0; d <= _depth; d++) {
//            System.out.println("Depth " + d);

            boolean more = searchAtDepth(graph, test, getKnowledge(), sepset, d);

//            System.out.println("more = " + more);

            if (!more) {
                break;
            }
        }

//        verifySepsetIntegrity(sepset);

        this.logger.log("info", "Finishing Fast Adjacency Search.");

        this.sepset = sepset;
        return graph;
    }

//    private void verifySepsetIntegrity(SepsetMap sepset) {
//        for (Node x : graph.getNodes()) {
//            for (Node y : graph.getNodes()) {
//                if (x == y) {
//                    continue;
//                }
//
//                if (graph.isAdjacentTo(y, x) && sepset.get(x, y) != null) {
//                    System.out.println(x + " " + y + " integrity check failed.");
//                }
//            }
//        }
//    }


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

    public IKnowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(IKnowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set knowledge to null");
        }
        this.knowledge = knowledge;
    }

    //==============================PRIVATE METHODS======================/

    /**
     * Removes from the list of nodes any that cannot be parents of x given the background knowledge.
     */
    private List<Node> possibleParents(Node x, List<Node> adjx,
                                       IKnowledge knowledge) {
        List<Node> possibleParents = new LinkedList<Node>();
        String _x = x.getName();

        for (Node z : adjx) {
            String _z = z.getName();

            if (possibleParentOf(_z, _x, knowledge)) {
                possibleParents.add(z);
            }
        }

        return possibleParents;
    }

    /**
     * Returns true just in case z is a possible parent of x, in the sense that edges are not forbidden from z to x, and
     * edges are not required from either x to z, according to background knowledge.
     */
    private boolean possibleParentOf(String z, String x, IKnowledge knowledge) {
        return !knowledge.edgeForbidden(z, x) && !knowledge.edgeRequired(x, z);
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
                                  IKnowledge knowledge, SepsetMap sepset, int depth) {


//        System.out.println("depth = " + depth);
//        List<Double> pValues = new ArrayList<Double>();

        boolean more = false;
        List<Node> nodes = new LinkedList<Node>(graph.getNodes());
        int removed = 0;

        for (Node x : nodes) {
            List<Node> b = new LinkedList<Node>(graph.getAdjacentNodes(x));

//            System.out.println("Adjacent nodes for " + x + " = " + b);
//            System.out.println("Depth = " + depth);

            nextEdge:
            for (Node y : b) {

                // This is the standard algorithm, without the v1 bias.
                List<Node> adjx = graph.getAdjacentNodes(x);
                adjx.remove(y);
                List<Node> ppx = possibleParents(x, adjx, knowledge);

//                System.out.println("Possible parents for removing " + x + " --- " + y + " are " + ppx);

                boolean noEdgeRequired =
                        knowledge.noEdgeRequired(x.getName(), y.getName());

                if (ppx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(ppx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, ppx);

                        boolean independent = false;
                        try {
                            independent = test.isIndependent(x, y, condSet);
                        } catch (Exception e) {
                            independent = false;
                        }
                        numIndependenceTests++;

                        if (trueGraph != null) {
                            IndTestDSep testDSep = new IndTestDSep(trueGraph);
                            Node _x = testDSep.getVariable(x.getName());
                            Node _y = testDSep.getVariable(y.getName());

                            List<Node> _condSet = new ArrayList<Node>();

                            for (Node node : condSet) {
                                _condSet.add(testDSep.getVariable(node.getName()));
                            }

                            boolean trueIndep = testDSep.isIndependent(_x, _y, _condSet);
                            if (!independent && trueIndep) numFalseDependenceJudgments++;
                            if (!independent) numDependenceJudgement++;

                            double p = test.getPValue();

                            if (p > 0.8 && !trueIndep) {
                                System.out.println("ROGUE DEPENDENCY! P = " + p + " fact = " +
                                        SearchLogUtils.independenceFact(x, y, condSet));
                            }
                        }

//                        try {
//                            pValues.add(test.getLikelihoodRatioP());
//
//                            double[] p = new double[pValues.size()];
//                            for (int t = 0; t < p.length; t++) p[t] = pValues.get(t);
//
//                            System.out.println("FDR cutoff = " + StatUtils.fdr(test.getAlpha(), p, false));
//                        } catch (Exception e) {
//                            //
//                        }

//                        System.out.println("condSet = " + condSet + " independent = " + independent);

                        if (independent && noEdgeRequired) {
//                            Edge edge = graph.getEdge(x, y);
                            graph.removeEdge(x, y);
                            removed++;
                            sepset.set(x, y, new LinkedList<Node>(condSet));
//                            this.logger.log("info", SearchLogUtils.independenceFact(x, y, condSet));
                            continue nextEdge;
                        }
                    }
                }
            }

            if (graph.getAdjacentNodes(x).size() - 1 > depth) {
                more = true;
            }
        }

//        System.out.println("more = " + more);

//        System.out.println("Removed = " + removed);
        return more;
    }

    public int getNumIndependenceTests() {
        return numIndependenceTests;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public int getNumFalseDependenceJudgments() {
        return numFalseDependenceJudgments;
    }

    public int getNumDependenceJudgments() {
        return numDependenceJudgement;
    }

    public SepsetMap getSepsets() {
        return sepset;
    }

    public boolean isFci() {
        return fci;
    }

    public void setFci(boolean fci) {
        this.fci = fci;
    }
}


