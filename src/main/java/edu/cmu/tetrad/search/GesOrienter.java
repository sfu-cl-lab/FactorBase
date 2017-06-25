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
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.TetradLogger;

import java.text.NumberFormat;

/**
 * An adaptation of GES to be used as a global reorientation procedure. Original code by Ricardo Silva, cleaned up by
 * Joe Ramsey. Only a few methods and constructors have changed from that version.
 *
 * @author Joseph Ramsey
 */
public final class GesOrienter implements Reorienter {

    /**
     * For linear algebra.
     */
    private final Algebra algebra = new Algebra();
    /**
     * Caches scores for discrete search.
     */
    private final LocalScoreCache localScoreCache = new LocalScoreCache();
    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private DataSet dataSet;
    /**
     * The correlation matrix for the data set.
     */
    private DoubleMatrix2D variances;
    /**
     * Sample size, either from the data set or from the variances.
     */
    private int sampleSize;
    /**
     * Specification of forbidden and required edges.
     */
    private IKnowledge knowledge = new Knowledge();
    /**
     * For discrete data scoring, the structure prior.
     */
    private double structurePrior;
    /**
     * For discrete data scoring, the sample prior.
     */
    private double samplePrior;
    /**
     * Map from variables to their column indices in the data set.
     */
    private HashMap <Node, Integer> hashIndices;
    /**
     * Array of variable names from the data set, in order.
     */
    private String varNames[];
    /**
     * List of variables in the data set, in order.
     */
    private List <Node> variables;
    /**
     * True iff the data set is discrete.
     */
    private boolean discrete;
    /**
     * The true graph, if known. If this is provided, asterisks will be printed out next to false positive added edges
     * (that is, edges added that aren't adjacencies in the true graph).
     */
    private Graph trueGraph;
    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;

    /**
     * Source graph, used as a source of possible adjacencies for edge orientation.
     */
    private EdgeListGraph sourceGraph;

    //===========================CONSTRUCTORS=============================//

    public GesOrienter(DataSet dataSet, IKnowledge knowledge) {
        setDataSet(dataSet);

        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;

        // structure prior was 0.001. The 1.0 value seems to work better for
        // discrete searches.
        initialize(10., 1.);
    }

    public GesOrienter(ICovarianceMatrix covMatrix) {
        setCorrMatrix(new CorrelationMatrix(covMatrix));
        initialize(10., 1.);  // structure prior was 0.001.
    }

    public GesOrienter(DataSet dataSet, Graph trueGraph) {
        setDataSet(dataSet);
        initialize(10., 1.);
        this.trueGraph = trueGraph;
    }

    //==========================PUBLIC METHODS==========================//

    /**
     * Get all nodes that are connected to Y by an undirected edge and not adjacent to X.
     */
    private static List <Node> getTNeighbors(Node x, Node y, Graph graph) {
        List <Node> tNeighbors = new LinkedList <Node>(graph.getAdjacentNodes(y));
        tNeighbors.removeAll(graph.getAdjacentNodes(x));

        for (int i = tNeighbors.size() - 1; i >= 0; i--) {
            Node z = tNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                tNeighbors.remove(z);
            }
        }

        return tNeighbors;
    }

    /**
     * Get all nodes that are connected to Y by an undirected edge and adjacent to X
     */
    private static List <Node> getHNeighbors(Node x, Node y, Graph graph) {
        List <Node> hNeighbors = new LinkedList <Node>(graph.getAdjacentNodes(y));
        hNeighbors.retainAll(graph.getAdjacentNodes(x));

        for (int i = hNeighbors.size() - 1; i >= 0; i--) {
            Node z = hNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);
            if (!Edges.isUndirectedEdge(edge)) {
                hNeighbors.remove(z);
            }
        }

        return hNeighbors;
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private static void delete(Node x, Node y, Set <Node> subset, Graph graph) {
        graph.removeEdges(x, y);

        for (Node aSubset : subset) {
            if (!graph.isParentOf(aSubset, x) && !graph.isParentOf(x, aSubset)) {
                graph.removeEdge(x, aSubset);
                graph.addDirectedEdge(x, aSubset);
            }
            graph.removeEdge(y, aSubset);
            graph.addDirectedEdge(y, aSubset);
        }
    }

    /**
     * Test if the candidate deletion is a valid operation (Theorem 17 from Chickering, 2002).
     */
    private static boolean validDelete(Node x, Node y, Set <Node> h,
                                       Graph graph) {
        List <Node> naYXH = GesOrienter.findNaYX(x, y, graph);
        naYXH.removeAll(h);
        return GesOrienter.isClique(naYXH, graph);
    }

    //===========================PRIVATE METHODS========================//

    /**
     * Find all nodes that are connected to Y by an undirected edge that are adjacent to X (that is, by undirected or
     * directed edge) NOTE: very inefficient implementation, since the current library does not allow access to the
     * adjacency list/matrix of the graph.
     */
    private static List <Node> findNaYX(Node x, Node y, Graph graph) {
        List <Node> naYX = new LinkedList <Node>(graph.getAdjacentNodes(y));
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (int i = 0; i < naYX.size(); i++) {
            Node z = naYX.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return naYX;
    }

    /**
     * Returns true iif the given set forms a clique in the given graph
     */
    private static boolean isClique(List <Node> set, Graph graph) {
        List <Node> setv = new LinkedList <Node>(set);
        for (int i = 0; i < setv.size() - 1; i++) {
            for (int j = i + 1; j < setv.size(); j++) {
                if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List <Set <Node>> powerSet(List <Node> nodes) {
        List <Set <Node>> subsets = new ArrayList <Set <Node>>();
        int total = (int) Math.pow(2, nodes.size());
        for (int i = 0; i < total; i++) {
            Set <Node> newSet = new HashSet <Node>();
            String selection = Integer.toBinaryString(i);
            for (int j = selection.length() - 1; j >= 0; j--) {
                if (selection.charAt(j) == '1') {
                    newSet.add(nodes.get(selection.length() - j - 1));
                }
            }
            subsets.add(newSet);
        }
        return subsets;
    }

    private static int getRowIndex(int dim[], int[] values) {
        int rowIndex = 0;
        for (int i = 0; i < dim.length; i++) {
            rowIndex *= dim[i];
            rowIndex += values[i];
        }
        return rowIndex;
    }

    private static void print(String message) {
        TetradLogger.getInstance().log("details", message);
    }

    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     */
    @Override
    public void orient(Graph graph) {
//        System.out.println("GES orientation");
        long startTime = System.currentTimeMillis();
        this.sourceGraph = new EdgeListGraph(graph);
        Graph graphCopy = new EdgeListGraph(graph);

        for (Edge edge : graph.getEdges()) {
            graph.removeEdge(edge);
        }

//        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));
        buildIndexing(graph);
//        addRequiredEdges(graph);
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        for (Edge edge : graphCopy.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
                System.out.println("GES Orienter missed this edge: " + edge);
            }
        }

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

//        return graph;

//        double score = scoreGraph(graph), newScore;
//        int iter = 0;
//        do {
//            newScore = fes(protograph, score);
//            if (newScore > score) {
//                score = newScore;
//                newScore = bes(graph, score);
//                if (score >= newScore) {
//                    break;
//                }
//                else {
//                    score = newScore;
//                }
//            }
//            else {
//                break;
//            }
//            //System.out.println("Current score = " + score);
//            iter++;
//        } while (iter < 100);
//        return graph;
    }

    public Graph search(List <Node> nodes) {
        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!dataSet().getVariables().containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in " + "the supplied data set.");
        }

        Graph graph = new EdgeListGraph(nodes);
        buildIndexing(graph);
        addRequiredEdges(graph);
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        return graph;
    }

    /*
    * Do an actual insertion
    * (Definition 12 from Chickering, 2002).
    **/

    public IKnowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     */
    @Override
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    /*
     * Test if the candidate insertion is a valid operation
     * (Theorem 15 from Chickering, 2002).
     **/

    private void initialize(double samplePrior, double structurePrior) {
        setStructurePrior(structurePrior);
        setSamplePrior(samplePrior);
    }

    /**
     * Forward equivalence search.
     */
    private double fes(Graph graph, double score) {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        GesOrienter.print("** FORWARD EQUIVALENCE SEARCH");
        double bestScore = score;
        GesOrienter.print("Initial Score = " + nf.format(bestScore));

        Node x, y;
        Set <Node> t = new HashSet <Node>();

        do {
            x = y = null;

            List <Edge> edges = new ArrayList <Edge>();

            for (Edge edge : sourceGraph.getEdges()) {
                edges.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
                edges.add(Edges.undirectedEdge(edge.getNode2(), edge.getNode1()));
            }

            for (Edge edge : edges) {
                Node _x = edge.getNode1();
                Node _y = edge.getNode2();

                if (_x == _y) {
                    continue;
                }

                if (graph.isAdjacentTo(_x, _y)) {
                    continue;
                }

                if (getKnowledge().edgeForbidden(_x.getName(),
                        _y.getName())) {
                    continue;
                }

                List <Node> tNeighbors = GesOrienter.getTNeighbors(_x, _y, graph);
                List <Set <Node>> tSubsets = GesOrienter.powerSet(tNeighbors);

                for (Set <Node> tSubset : tSubsets) {

                    if (!validSetByKnowledge(_x, _y, tSubset, true)) {
                        continue;
                    }

                    double insertEval = insertEval(_x, _y, tSubset, graph);
                    double evalScore = score + insertEval;

                    TetradLogger.getInstance().log("details", "Attempt adding " + _x + "-->" + _y +
                            " " + tSubset + " (" + evalScore + ")");

                    if (!(evalScore > bestScore && evalScore > score)) {
                        continue;
                    }

                    if (!validInsert(_x, _y, tSubset, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = tSubset;
                }
            }

            if (x != null) {
                insert(x, y, t, graph, bestScore);
                rebuildPattern(graph);
                score = bestScore;
            }
        } while (x != null);
        return score;
    }

    //---Background knowledge methods.

    /**
     * Backward equivalence search.
     */
    private double bes(Graph graph, double initialScore) {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        GesOrienter.print("");
        GesOrienter.print("** BACKWARD ELIMINATION SEARCH");
        GesOrienter.print("Initial Score = " + nf.format(initialScore));
        double score = initialScore;
        double bestScore = score;
        Node x, y;
        Set <Node> t = new HashSet <Node>();
        do {
            x = y = null;
            List <Edge> edges1 = graph.getEdges();
            List <Edge> edges = new ArrayList <Edge>();

            for (Edge edge : edges1) {
                Node _x = edge.getNode1();
                Node _y = edge.getNode2();

                if (Edges.isUndirectedEdge(edge)) {
                    edges.add(Edges.directedEdge(_x, _y));
                    edges.add(Edges.directedEdge(_y, _x));
                } else {
                    edges.add(edge);
                }
            }

            for (Edge edge : edges) {
                Node _x = Edges.getDirectedEdgeTail(edge);
                Node _y = Edges.getDirectedEdgeHead(edge);

                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                    continue;
                }

                List <Node> hNeighbors = GesOrienter.getHNeighbors(_x, _y, graph);
                List <Set <Node>> hSubsets = GesOrienter.powerSet(hNeighbors);

                for (Set <Node> hSubset : hSubsets) {
                    if (!validSetByKnowledge(_x, _y, hSubset, false)) {
                        continue;
                    }

                    double deleteEval = deleteEval(_x, _y, hSubset, graph);
                    double evalScore = score + deleteEval;

                    TetradLogger.getInstance().log("details", "Attempt removing " + _x + "-->" + _y + "(" +
                            evalScore + ")");

                    if (!(evalScore > bestScore)) {
                        continue;
                    }

                    if (!GesOrienter.validDelete(_x, _y, hSubset, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = hSubset;
                }


                if (Edges.isUndirectedEdge(edge)) {
                    _x = edge.getNode1();
                    _y = edge.getNode2();
                } else {
                    _x = Edges.getDirectedEdgeTail(edge);
                    _y = Edges.getDirectedEdgeHead(edge);
                }

                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                    continue;
                }

                hNeighbors = GesOrienter.getHNeighbors(_x, _y, graph);
                hSubsets = GesOrienter.powerSet(hNeighbors);

                for (Set <Node> hSubset1 : hSubsets) {
                    if (!validSetByKnowledge(_x, _y, hSubset1, false)) {
                        continue;
                    }

                    double deleteEval = deleteEval(_x, _y, hSubset1, graph);
                    double evalScore = score + deleteEval;

//                        print("Attempt removing " + _x + "-->" + _y + "(" + evalScore + ")");

                    if (!(evalScore > bestScore)) {
                        continue;
                    }

                    if (!GesOrienter.validDelete(_x, _y, hSubset1, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = hSubset1;
                }
            }
            if (x != null) {
                GesOrienter.print("DELETE " + graph.getEdge(x, y) + t.toString() + " (" +
                        nf.format(bestScore) + ")");
                GesOrienter.delete(x, y, t, graph);
                rebuildPattern(graph);
                score = bestScore;
            }
        } while (x != null);

        return score;
    }

    /**
     * Evaluate the Insert(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double insertEval(Node x, Node y, Set <Node> t, Graph graph) {
        Set <Node> set1 = new HashSet <Node>(GesOrienter.findNaYX(x, y, graph));
        set1.addAll(t);
        set1.addAll(graph.getParents(y));
        Set <Node> set2 = new HashSet <Node>(set1);
        set1.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    //--Auxiliary methods.

    /**
     * Evaluate the Delete(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double deleteEval(Node x, Node y, Set <Node> h, Graph graph) {
        Set <Node> set1 = new HashSet <Node>(GesOrienter.findNaYX(x, y, graph));
        set1.removeAll(h);
        set1.addAll(graph.getParents(y));
        Set <Node> set2 = new HashSet <Node>(set1);
        set1.remove(x);
        set2.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    private void insert(Node x, Node y, Set <Node> subset, Graph graph,
                        double score) {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        graph.addDirectedEdge(x, y);
        String label = trueGraph != null && trueEdge == null ? "*" : "";
        GesOrienter.print(graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) + " (" +
                nf.format(score) + ") " + label);

        for (Node aSubset : subset) {
            Edge oldEdge = graph.getEdge(aSubset, y);

            graph.removeEdge(aSubset, y);
            graph.addDirectedEdge(aSubset, y);

            GesOrienter.print("--- Directing " + oldEdge + " to " +
                    graph.getEdge(aSubset, y));
        }
    }

    private boolean validInsert(Node x, Node y, Set <Node> subset, Graph graph) {
        List <Node> naYXT = new LinkedList <Node>(subset);
        naYXT.addAll(GesOrienter.findNaYX(x, y, graph));

        if (!GesOrienter.isClique(naYXT, graph)) {
            return false;
        }

        return isSemiDirectedBlocked(x, y, naYXT, graph, new HashSet <Node>());
    }

    private void addRequiredEdges(Graph graph) {
        for (Iterator <KnowledgeEdge> it =
             this.getKnowledge().requiredEdgesIterator(); it.hasNext(); ) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator <Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (!graph.isAncestorOf(nodeB, nodeA)) {
                graph.removeEdges(nodeA, nodeB);
                graph.addDirectedEdge(nodeA, nodeB);
            }
        }
        for (Iterator <KnowledgeEdge> it =
             getKnowledge().forbiddenEdgesIterator(); it.hasNext(); ) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator <Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (graph.isAdjacentTo(nodeA, nodeB) &&
                    !graph.isChildOf(nodeA, nodeB)) {
//                System.out.println(graph);
                if (!graph.isAncestorOf(nodeA, nodeB)) {
                    graph.removeEdges(nodeA, nodeB);
                    graph.addDirectedEdge(nodeB, nodeA);
                }
            }
        }
    }

    /**
     * Use background knowledge to decide if an insert or delete operation does not orient edges in a forbidden
     * direction according to prior knowledge. If some orientation is forbidden in the subset, the whole subset is
     * forbidden.
     */
    private boolean validSetByKnowledge(Node x, Node y, Set <Node> subset,
                                        boolean insertMode) {
        if (insertMode) {
            for (Node aSubset : subset) {
                if (getKnowledge().edgeForbidden(aSubset.getName(),
                        y.getName())) {
                    return false;
                }
            }
        } else {
            for (Node nextElement : subset) {
                if (getKnowledge().edgeForbidden(x.getName(),
                        nextElement.getName())) {
                    return false;
                }
                if (getKnowledge().edgeForbidden(y.getName(),
                        nextElement.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies if every semidirected path from y to x contains a node in naYXT
     */
    private boolean isSemiDirectedBlocked(Node x, Node y, List <Node> naYXT,
                                          Graph graph, Set <Node> marked) {
        if (naYXT.contains(y)) {
            return true;
        }

        if (y == x) {
            return false;
        }

        for (Node node1 : graph.getNodes()) {
            if (node1 == y || marked.contains(node1)) {
                continue;
            }

            if (graph.isAdjacentTo(y, node1) && !graph.isParentOf(node1, y)) {
                marked.add(node1);

                if (!isSemiDirectedBlocked(x, node1, naYXT, graph, marked)) {
                    return false;
                }

                marked.remove(node1);
            }
        }

        return true;
    }

    /**
     * Completes a pattern that was modified by an insertion/deletion operator Based on the algorithm described on
     * Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        addRequiredEdges(graph);
        pdagWithBk(graph, getKnowledge());
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to adapt Chickering's suggested algorithm above
     * (dagToPdag) to incorporate background knowledge, so I am also implementing this algorithm based on Meek's 1995
     * UAI paper. Notice it is the same implemented in PcSearch. </p> *IMPORTANT!* *It assumes all colliders are
     * oriented, as well as arrows dictated by time order.*
     */
    private void pdagWithBk(Graph graph, IKnowledge knowledge) {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);
    }

    private void setDataSet(DataSet dataSet) {
        this.varNames = dataSet.getVariableNames().toArray(new String[0]);
        this.variables = dataSet.getVariables();
        this.dataSet = dataSet;
        this.discrete = dataSet.isDiscrete();

        if (!isDiscrete()) {
//            this.variances = dataSet.getCovarianceMatrix();
            this.variances = dataSet.getCovarianceMatrix();
        }

        this.sampleSize = dataSet.getNumRows();
    }

    private void buildIndexing(Graph graph) {
        this.hashIndices = new HashMap <Node, Integer>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < this.varNames.length; i++) {
                if (this.varNames[i].equals(next.getName())) {
                    this.hashIndices.put(next, i);
                    break;
                }
            }
        }
    }

    private double scoreGraph(Graph graph) {
        Graph dag = new EdgeListGraph(graph);
        SearchGraphUtils.pdagToDag(dag);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Collection <Node> parents = dag.getParents(next);
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(next.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int parentIndices[] = new int[parents.size()];
            Iterator <Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < getVariables().size(); i++) {
                    if (this.varNames[i].equals(nextParent.getName())) {
                        parentIndices[count++] = i;
                        break;
                    }
                }
            }

            if (this.isDiscrete()) {
                score += localBdeuScore(nextIndex, parentIndices);
            } else {
                score += localSemScore(nextIndex, parentIndices);
            }
        }
        return score;
    }

    //===========================SCORING METHODS===========================//

    private double scoreGraphChange(Node y, Set <Node> parents1,
                                    Set <Node> parents2) {
        int yIndex = hashIndices.get(y);
        int parentIndices1[] = new int[parents1.size()];

        int count = 0;
        for (Node aParents1 : parents1) {
            parentIndices1[count++] = (hashIndices.get(aParents1));
        }

        int parentIndices2[] = new int[parents2.size()];

        int count2 = 0;
        for (Node aParents2 : parents2) {
            parentIndices2[count2++] = (hashIndices.get(aParents2));
        }

        if (this.isDiscrete()) {
            double score1 = localBdeuScore(yIndex, parentIndices1);
            double score2 = localBdeuScore(yIndex, parentIndices2);
//            double score1 = localDiscreteBicScore(yIndex, parentIndices1);
//            double score2 = localDiscreteBicScore(yIndex, parentIndices2);
            return score1 - score2;
        } else {
            double score1 = localSemScore(yIndex, parentIndices1);
            double score2 = localSemScore(yIndex, parentIndices2);
            return score1 - score2;
        }
    }

    /**
     * Compute the local BDeu score of (i, parents(i)). See (Chickering, 2002).
     */
    private double localBdeuScore(int i, int parents[]) {
        double oldScore = localScoreCache.get(i, parents);

        if (!Double.isNaN(oldScore)) {
            return oldScore;
        }

        // Number of categories for i.
        int r = numCategories(i);

//        if (r < 2) {
//            String variable = dataSet != null ? dataSet.getVariable(i).getName()
//                    : "?";
//
//            throw new IllegalArgumentException(
//                    "Number of categories for " + variable +
//                            " must be at least 2.");
//        }

        // Numbers of categories of parents.
        int dims[] = new int[parents.length];

        for (int p = 0; p < parents.length; p++) {
            dims[p] = numCategories(parents[p]);
        }

        // Number of parent states.
        int q = 1;
        for (int p = 0; p < parents.length; p++) {
            q *= dims[p];
        }

        // Conditional cell counts of data for i given parents(i).
        int n_ijk[][] = new int[q][r];
        int n_ij[] = new int[q];

        int values[] = new int[parents.length];

        for (int n = 0; n < sampleSize(); n++) {
            for (int p = 0; p < parents.length; p++) {
                int parentValue = dataSet().getInt(n, parents[p]);

                if (parentValue == -99) {
                    throw new IllegalStateException("Complete data expected.");
                }

                values[p] = parentValue;
            }

            int childValue = dataSet().getInt(n, i);

            if (childValue == -99) {
                throw new IllegalStateException("Complete data expected.");

            }

            n_ijk[GesOrienter.getRowIndex(dims, values)][childValue]++;
        }

        // Row sums.
        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                n_ij[j] += n_ijk[j][k];
            }
        }

        //Finally, compute the score
        double score = (r - 1) * q * Math.log(getStructurePrior());

        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                score += ProbUtils.lngamma(
                        getSamplePrior() / (r * q) + n_ijk[j][k]);
            }

            score -= ProbUtils.lngamma(getSamplePrior() / q + n_ij[j]);
        }

        score += q * ProbUtils.lngamma(getSamplePrior() / q);
        score -= (r * q) * ProbUtils.lngamma(getSamplePrior() / (r * q));

        localScoreCache.add(i, parents, score);

        return score;
    }

    private int numCategories(int i) {
        return ((DiscreteVariable) dataSet().getVariable(i)).getNumCategories();
    }

//    private double localDiscreteBicScore(int i, int[] parents) {
//
//        // Number of categories for i.
//        int r = numCategories(i);
//
//        // Numbers of categories of parents.
//        int dims[] = new int[parents.length];
//
//        for (int p = 0; p < parents.length; p++) {
//            dims[p] = numCategories(parents[p]);
//        }
//
//        // Number of parent states.
//        int q = 1;
//        for (int p = 0; p < parents.length; p++) {
//            q *= dims[p];
//        }
//
//        // Conditional cell counts of data for i given parents(i).
//        double cell[][] = new double[q][r];
//
//        int values[] = new int[parents.length];
//
//        for (int n = 0; n < sampleSize(); n++) {
//            for (int p = 0; p < parents.length; p++) {
//                int value = dataSet().getInt(n, parents[p]);
//
//                if (value == -99) {
//                    throw new IllegalStateException("Complete data expected.");
//                }
//
//                values[p] = value;
//            }
//
//            int value = dataSet().getInt(n, i);
//
//            if (value == -99) {
//                throw new IllegalStateException("Complete data expected.");
//
//            }
//
//            cell[getRowIndex(dims, values)][value]++;
//        }
//
//        // Calculate row sums.
//        double rowSum[] = new double[q];
//
//        for (int j = 0; j < q; j++) {
//            for (int k = 0; k < r; k++) {
//                rowSum[j] += cell[j][k];
//            }
//        }
//
//        // Calculate log prob data given structure.
//        double score = 0.0;
//
//        for (int j = 0; j < q; j++) {
//            if (rowSum[j] == 0) {
//                continue;
//            }
//
//            for (int k = 0; k < r; k++) {
//                double count = cell[j][k];
//                double prob = count / rowSum[j];
//                score += count * Math.log(prob);
//            }
//        }
//
//        // Subtract penalty.
//        double numParams = q * (r - 1);
//        return score - numParams / 2. * Math.log(sampleSize());
//    }

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in a simple SEM model.
     */
    private double localSemScore(int i, int[] parents) {

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        double variance = getCorrMatrix().get(i, i);
        int m = sampleSize();
//        int n = getCorrMatrix().columns();
        double d = parents.length + 1;

        if (parents.length > 0) {

            // Regress z onto i, yielding regression coefficients b.
            DoubleMatrix2D Czz =
                    getCorrMatrix().viewSelection(parents, parents);
            DoubleMatrix2D inverse = algebra().inverse(Czz);

            DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
            Cyz = Cyz.viewSelection(parents);
            DoubleMatrix1D b = algebra().mult(inverse, Cyz);

            variance -= algebra().mult(Cyz, b);
        }

        return -(m / 2.) - (m / 2.) * Math.log(variance) -
                (d / 2.) * Math.log(m);
    }

    private int sampleSize() {
//        System.out.println("sample size = " + sampleSize);
        return this.sampleSize;

//        return dataSet().getNumRows();
    }

//    private double localSemScore2(int i, int parents[]) {
//        double oldScore = localScoreCache.get(i, parents);
//
//        if (!Double.isNaN(oldScore)) {
//            return oldScore;
//        }
//
//        // Calculate the unexplained variance of i given z1,...,zn
//        // considered as a naive Bayes model.
//        double variance = getCorrMatrix().get(i, i);
//        int m = sampleSize();
//        double d = parents.length + 1;
//
//        DoubleMatrix1D b = new DenseDoubleMatrix1D(0);
//
//        if (parents.length > 0) {
//
//            // Regress z onto i, yielding regression coefficients b.
//            DoubleMatrix2D Czz =
//                    getCorrMatrix().viewSelection(parents, parents);
//            DoubleMatrix2D inverse = algebra().inverse(Czz);
//
//            DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
//            Cyz = Cyz.viewSelection(parents);
//            b = algebra().mult(inverse, Cyz);
//
//            variance -= algebra().mult(Cyz, b);
//        }
//
//
//        DoubleMatrix1D values = new DenseDoubleMatrix1D(parents.length);
//        double logprob = 0.0;
//
//        for (int n = 0; n < sampleSize(); n++) {
//            for (int p = 0; p < parents.length; p++) {
//                double parentValue = dataSet().getDouble(n, parents[p]);
//
//                if (Double.isNaN(parentValue)) {
//                    throw new IllegalStateException("Complete data expected.");
//                }
//
//                values.set(p, parentValue);
//            }
//
//            double childValue = dataSet().getDouble(n, i);
//
//            double sum = 0.0;
//
//            for (int j = 0; j < dataSet().getNumRows(); j++) {
//                sum += dataSet().getDouble(j, i);
//            }
//
//            double mean = sum / dataSet().getNumRows();
//
//            if (Double.isNaN(childValue)) {
//                throw new IllegalStateException("Complete data expected.");
//            }
//
//            // Calculate the estimated child value.
//            double childValEst = algebra().mult(b, values);
//
//            Normal normal = new Normal(childValEst, Math.sqrt(variance), mersenneTwister);
//
//            double pvalue = 2.0 * (1.0 - normal.cdf(Math.abs(childValue)));
//            logprob += Math.log(pvalue);
//        }
//
//
//        //Finally, compute the score
//        double score = logprob - (d / 2.) * Math.log(m);
//
//        localScoreCache.add(i, parents, score);
//
//        return score;
//    }

    private List <Node> getVariables() {
        return variables;
    }

    private DoubleMatrix2D getCorrMatrix() {
        return variances;
    }

    private void setCorrMatrix(CorrelationMatrix corrMatrix) {
        this.variances = corrMatrix.getMatrix();
        this.varNames = corrMatrix.getVariableNames().toArray(new String[0]);
        this.variables = corrMatrix.getVariables();
        this.sampleSize = corrMatrix.getSampleSize();
    }

    private Algebra algebra() {
        return algebra;
    }

    private DataSet dataSet() {
        return dataSet;
    }

    private double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    private double getSamplePrior() {
        return samplePrior;
    }

    public void setSamplePrior(double samplePrior) {
        this.samplePrior = samplePrior;
    }

    private boolean isDiscrete() {
        return discrete;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}

