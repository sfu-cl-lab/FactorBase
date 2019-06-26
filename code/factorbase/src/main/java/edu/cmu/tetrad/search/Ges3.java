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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

/**
 * GesSearch is an implementation of the GES algorithm, as specified in Chickering (2002) "Optimal structure
 * identification with greedy search" Journal of Machine Learning Research. It works for both BayesNets and SEMs.
 * <p/>
 * Some code optimization could be done for the scoring part of the graph for discrete models (method scoreGraphChange).
 * Some of Andrew Moore's approaches for caching sufficient statistics, for instance.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public class Ges3 {

    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private DataSet dataSet;

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * Map from variables to their column indices in the data set.
     */
    private HashMap<Node, Integer> hashIndices;

    /**
     * Array of variable names from the data set, in order.
     */
    private String varNames[];

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

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
     * For formatting printed numbers.
     */
    private final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * Listeners for graph change events.
     */
    private transient List<PropertyChangeListener> listeners;

    /**
     * The maximum number of edges the algorithm will add to the graph.
     */
    private int maxEdgesAdded = -1;

    /**
     * The score for discrete searches.
     */
    private LocalDiscreteScore discreteScore;

    /**
     * The top n graphs found by the algorithm, where n is <code>numPatternsToStore</code>.
     */
    private SortedSet<ScoredGraph> topGraphs = new TreeSet<ScoredGraph>();

    /**
     * The number of top patterns to store.
     */
    private int numPatternsToStore = 10;

    SortedSet<Arrow> sortedArrows;
    Set<Arrow>[][] lookupArrows;
    SortedSet<Arrow> sortedArrowsBackwards;
    Set<Arrow>[][] lookupArrowsBackwards;
    Map<Node, Map<Set<Node>, Double>> scoreHash;
    private Map<Node, Integer> nodesHash;
    private boolean storeGraphs = true;


    //===========================CONSTRUCTORS=============================//

    public Ges3(DataSet dataSet, double samplePrior, double structurePrior) {
        setDataSet(dataSet);
        if (dataSet != null) {
            setDiscreteScore(new BDeuScore(dataSet, samplePrior, structurePrior));
        }
    }

    //==========================PUBLIC METHODS==========================//


    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     *
     * @return the resulting Pattern.
     */
    public Graph search() {
//        long startTime = System.currentTimeMillis();

        // Check for missing values.
        if (dataSet != null && DataUtils.containsMissingValue(dataSet)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));

        scoreHash = new WeakHashMap<Node, Map<Set<Node>, Double>>();

        for (Node node : graph.getNodes()) {
            scoreHash.put(node, new HashMap<Set<Node>, Double>());
        }

        fireGraphChange(graph);
        buildIndexing(graph);
        addRequiredEdges(graph);

        // Method 1-- original.

        // Don't need to score the original graph; the BIC scores all up to a constant.
//        double score = 0;
        double score = scoreGraph(graph);
        System.out.println("zqian########## get the score for input Graph :"+score);
        //Oct 30, bug? Arrow implies non-ancestor
        //score=0;
        storeGraph(new EdgeListGraph(graph), score);

       // System.out.println("######## finished the storing");
        // Do forward search.
        score = fes(graph, score);
//zqian
       System.out.println("Fes Search is Done, here is the final BDeu Score "+ score +"\n");
        // Do backward search.
        score = bes(graph, score);
//zqian
 //       System.out.println("Bes Search is Done, here is  BDeu Score "+ score +"\n");
//        score = fes(graph, score);
//        bes(graph, score);

//        long endTime = System.currentTimeMillis();
//        this.elapsedTime = endTime - startTime;
//        this.logger.log("graph", "\nReturning this graph: " + graph);
//        TetradLogger.getInstance().log("info", "Final Model BIC = " + nf.format(score));
//
//        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
//        this.logger.flush();

//        return new ArrayList<ScoredGraph>(topGraphs).get(topGraphs.size() - 1).getGraph();
        return graph;

    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     *
     * @param knowledge the knowledge object, specifying forbidden and required edges.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    public int getMaxEdgesAdded() {
        return maxEdgesAdded;
    }

    public int getNumPatternsToStore() {
        return numPatternsToStore;
    }

    public boolean isStoreGraphs() {
        return storeGraphs;
    }


    //===========================PRIVATE METHODS========================//

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence search.
     * @param score The score in the state prior to the forward equivalence search
     * @return the score in the state after the forward equivalence search. Note that the graph is changed as a
     *         side-effect to its state after the forward equivalence search.
     */
    @SuppressWarnings("unchecked")
    private double fes(Graph graph, double score) {

        List<Node> nodes = graph.getNodes();

        sortedArrows = new TreeSet<Arrow>();
        lookupArrows = (HashSet<Arrow>[][]) new HashSet[nodes.size()][nodes.size()];

        nodesHash = new HashMap<Node, Integer>();
        int index = -1;

        for (Node node : nodes) {
            nodesHash.put(node, ++index);
        }

        TetradLogger.getInstance().log("info", "** FORWARD EQUIVALENCE SEARCH");
        TetradLogger.getInstance().log("info", "Initial Model BIC = " + nf.format(score)); //zqian ?? BIC or BDeu

        initializeArrowsForward(nodes, graph);

        while (!sortedArrows.isEmpty()) {
            Arrow arrow = sortedArrows.first();
            sortedArrows.remove(arrow);

            Node _x = nodes.get(arrow.getX());
            Node _y = nodes.get(arrow.getY());

            if (graph.isAdjacentTo(_x, _y)) {
                continue;
            }

            if (!findNaYX(_x, _y, graph).equals(arrow.getNaYX())) {
                reevaluateFoward(graph, nodes, arrow);
                continue;
            }

            if (!new HashSet<Node>(getTNeighbors(_x, _y, graph)).containsAll(arrow.getHOrT())) {
                reevaluateFoward(graph, nodes, arrow);
                continue;
            }

            if (!validInsert(_x, _y, arrow.getHOrT(), arrow.getNaYX(), graph)) {
                continue;
            }

            Node x = nodes.get(arrow.getX());
            Node y = nodes.get(arrow.getY());
            Set<Node> t = arrow.getHOrT();
            double bump = arrow.getBump();

            score = score + bump;
            insert(x, y, t, graph, score, true, bump);
            rebuildPattern(graph);

            storeGraph(graph, score);

            reevaluateFoward(graph, nodes, arrow);

            if (getMaxEdgesAdded() != -1 && graph.getNumEdges() >= getMaxEdgesAdded()) {
                break;
            }
        }

        return score;
    }

    double minJump = 0;
    private double minNeg = 0; //-1000000;


    private double bes(Graph graph, double score) {
        List<Node> nodes = graph.getNodes();

        TetradLogger.getInstance().log("info", "** BACKWARD EQUIVALENCE SEARCH");
       TetradLogger.getInstance().log("info", "Initial Model BIC = " + nf.format(score)); // here is BIC socre or BDeu Score //zqian ??
      // System.out.println("Within BES Search ");// + score );//+ " " + graph); // Oct 23

        initializeArrowsBackward(graph);
      //  System.out.println("sortedArrowsBackwards.isEmpty() is : " + sortedArrowsBackwards.isEmpty() );
        while (!sortedArrowsBackwards.isEmpty()) {
            Arrow arrow = sortedArrowsBackwards.first();
            sortedArrowsBackwards.remove(arrow);

            Node _x = nodes.get(arrow.getX());
            Node _y = nodes.get(arrow.getY());

            if (!graph.isAdjacentTo(_x, _y)) {
                continue;
            }

            if (!findNaYX(_x, _y, graph).equals(arrow.getNaYX())) {
                reevaluateBackward(graph, nodes, arrow);
                continue;
            }

            if (!new HashSet<Node>(getHNeighbors(_x, _y, graph)).containsAll(arrow.getHOrT())) {
                reevaluateBackward(graph, nodes, arrow);
                continue;
            }

            if (!validDelete(arrow.getHOrT(), arrow.getNaYX(), graph)) {
                continue;
            }

            Node x = nodes.get(arrow.getX());
            Node y = nodes.get(arrow.getY());
            Set<Node> h = arrow.getHOrT();
            double bump = arrow.getBump();

            score = score + bump;
            delete(x, y, h, graph, score, true, bump);
            rebuildPattern(graph);

            storeGraph(graph, score);

            reevaluateBackward(graph, nodes, arrow);
        }

        return score;
    }

    private void initializeArrowsForward(List<Node> nodes, Graph graph) {
        Set<Node> empty = Collections.emptySet();

        for (int j = 0; j < nodes.size(); j++) {

            for (int i = 0; i < nodes.size(); i++) {
                if (j == i) continue;

                Node _x = nodes.get(i);
                Node _y = nodes.get(j);

                if (getKnowledge().edgeForbidden(_x.getName(),_y.getName())) {
                    continue;
                }

                Set<Node> naYX = empty;
                Set<Node> t = empty;

                if (!validSetByKnowledge(_x, _y, t, true)) {
                    continue;
                }

                double bump = insertEval(_x, _y, t, naYX, graph);

                if (bump > minJump) {
                    Arrow arrow = new Arrow(bump, i, j, t, naYX, nodes);
                    lookupArrows[i][j] = new HashSet<Arrow>();
                    sortedArrows.add(arrow);
                    lookupArrows[i][j].add(arrow);
                }
            }
        }


    }

    @SuppressWarnings("unchecked")
    private void initializeArrowsBackward(Graph graph) {
        List<Node> nodes = graph.getNodes();
        sortedArrowsBackwards = new TreeSet<Arrow>();
        lookupArrowsBackwards = (HashSet<Arrow>[][]) new HashSet[nodes.size()][nodes.size()];

        List<Edge> graphEdges = graph.getEdges();
        for (Edge edge : graphEdges) {
            Node _x = edge.getNode1();
            Node _y = edge.getNode2();

           // System.out.println("Within initializeArrowsBackward "); // Oct 23

            int i = nodesHash.get(edge.getNode1());
            int j = nodesHash.get(edge.getNode2());
          //  System.out.println("i :" +i +"\n j :" +j); // Oct 23

            if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                continue;
            }

            if (Edges.isDirectedEdge(edge)) {
                calculateArrowsBackward(i, j, nodes, graph);
            } else {
                calculateArrowsBackward(i, j, nodes, graph);
                calculateArrowsBackward(j, i, nodes, graph);
            }
        }
    }

    private void reevaluateFoward(Graph graph, List<Node> nodes, Arrow arrow) {
        Node x = nodes.get(arrow.getX());
        Node y = nodes.get(arrow.getY());

        for (int _w = 0; _w < nodes.size(); _w++) {
            Node w = nodes.get(_w);
            if (w == x) continue;
            if (w == y) continue;

            if (!graph.isAdjacentTo(w, x)) {
                calculateArrowsForward(_w, arrow.getX(), nodes, graph);

                if (graph.isAdjacentTo(w, y)) {
                    calculateArrowsForward(arrow.getX(), _w, nodes, graph);
                }
            }

            if (!graph.isAdjacentTo(w, y)) {
                calculateArrowsForward(_w, arrow.getY(), nodes, graph);

                if (graph.isAdjacentTo(w, x)) {
                    calculateArrowsForward(arrow.getY(), _w, nodes, graph);
                }
            }
        }
    }

    private void reevaluateBackward(Graph graph, List<Node> nodes, Arrow arrow) {
        Node x = nodes.get(arrow.getX());
        Node y = nodes.get(arrow.getY());

        for (Node w : graph.getAdjacentNodes(x)) {
            int _w = nodesHash.get(w);

            calculateArrowsBackward(_w, arrow.getX(), nodes, graph);
            calculateArrowsBackward(arrow.getX(), _w, nodes, graph);
        }

        for (Node w : graph.getAdjacentNodes(y)) {
            int _w = nodesHash.get(w);

            calculateArrowsBackward(_w, arrow.getX(), nodes, graph);
            calculateArrowsBackward(arrow.getX(), _w, nodes, graph);
        }
    }

    private void calculateArrowsForward(int i, int j, List<Node> nodes, Graph graph) {
        if (i == j) {
            return;
        }

        Node _x = nodes.get(i);
        Node _y = nodes.get(j);

        if (graph.isAdjacentTo(_x, _y)) {
            return;
        }

        if (getKnowledge().edgeForbidden(_x.getName(),
                _y.getName())) {
            return;
        }

        Set<Node> naYX = findNaYX(_x, _y, graph);

        if (lookupArrows[i][j] != null) {
            for (Arrow arrow : lookupArrows[i][j]) {
                sortedArrows.remove(arrow);
            }

            lookupArrows[i][j] = null;
        }

        List<Node> tNeighbors = getTNeighbors(_x, _y, graph);
        List<Set<Node>> tSubsets = powerSet(tNeighbors);

        for (Set<Node> t : tSubsets) {
            if (!validSetByKnowledge(_x, _y, t, true)) {
                continue;
            }

            double bump = insertEval(_x, _y, t, naYX, graph);
            Arrow arrow = new Arrow(bump, i, j, t, naYX, nodes);

//            System.out.println(arrow);

            if (bump > minJump) {
                if (lookupArrows[i][j] == null) {
                    lookupArrows[i][j] = new HashSet<Arrow>();
                }
                sortedArrows.add(arrow);
                lookupArrows[i][j].add(arrow);
            }
        }
    }

    private void calculateArrowsBackward(int i, int j, List<Node> nodes, Graph graph) {
        if (i == j) {
            return;
        }

        Node _x = nodes.get(i);
        Node _y = nodes.get(j);

        if (!graph.isAdjacentTo(_x, _y)) {
            return;
        }

        if (!getKnowledge().noEdgeRequired(_x.getName(),
                _y.getName())) {
            return;
        }

        Set<Node> naYX = findNaYX(_x, _y, graph);

        if (lookupArrowsBackwards[i][j] != null) {
            for (Arrow arrow : lookupArrowsBackwards[i][j]) {
                sortedArrowsBackwards.remove(arrow);
            }

            lookupArrowsBackwards[i][j] = null;
        }

        List<Node> hNeighbors = getHNeighbors(_x, _y, graph);
        List<Set<Node>> hSubsets = powerSet(hNeighbors);

        for (Set<Node> h : hSubsets) {
            double bump = deleteEval(_x, _y, h, naYX, graph);
            Arrow arrow = new Arrow(bump, i, j, h, naYX, nodes);

//            System.out.println("Calculate backwards " + arrow);

            if (bump > minNeg) {
                if (lookupArrowsBackwards[i][j] == null) {
                    lookupArrowsBackwards[i][j] = new HashSet<Arrow>();
                }

                sortedArrowsBackwards.add(arrow);
                lookupArrowsBackwards[i][j].add(arrow);
            }
        }
    }

    private static class Arrow implements Comparable<Arrow> {
        private double bump;
        private int x;
        private int y;
        private Set<Node> hOrT;
        private Set<Node> naYX;
        private List<Node> nodes;

        public Arrow(double bump, int x, int y, Set<Node> hOrT, Set<Node> naYX, List<Node> nodes) {
            this.bump = bump;
            this.x = x;
            this.y = y;
            this.hOrT = hOrT;
            this.naYX = naYX;
            this.nodes = nodes;
        }

        public double getBump() {
            return bump;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Set<Node> getHOrT() {
            return hOrT;
        }

        public Set<Node> getNaYX() {
            return naYX;
        }

        // Sorting is by bump, high to low.

        @Override
        public int compareTo(Arrow o) {
            Arrow info = o;
            return new Double(info.getBump()).compareTo(new Double(getBump()));
        }

        @Override
        public String toString() {
            return "Arrow<" + nodes.get(x) + "->" + nodes.get(y) + " bump = " + bump + " t = " + hOrT + " naYX = " + naYX + ">";
        }
    }


    /**
     * Get all nodes that are connected to Y by an undirected edge and not adjacent to X.
     */
    private static List<Node> getTNeighbors(Node x, Node y, Graph graph) {
        List<Node> tNeighbors = graph.getAdjacentNodes(y);
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
    private static List<Node> getHNeighbors(Node x, Node y, Graph graph) {
        List<Node> hNeighbors = graph.getAdjacentNodes(y);
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
     * Evaluate the Insert(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double insertEval(Node x, Node y, Set<Node> t, Set<Node> naYX, Graph graph) {

        // set1 contains x; set2 does not.
        Set<Node> set2 = new HashSet<Node>(naYX);
        set2.addAll(t);
        set2.addAll(graph.getParents(y));
        Set<Node> set1 = new HashSet<Node>(set2);
        set1.add(x);

        double score = scoreGraphChange(y, set1, set2);

        return score;
    }

    /**
     * Evaluate the Delete(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double deleteEval(Node x, Node y, Set<Node> h, Set<Node> naYX, Graph graph) {

        // set2 contains x; set1 does not.
        Set<Node> set2 = new HashSet<Node>(naYX);
        set2.removeAll(h);
        set2.addAll(graph.getParents(y));
        set2.add(x);
        Set<Node> set1 = new HashSet<Node>(set2);
        set1.remove(x);

        return scoreGraphChange(y, set1, set2);
    }

    /**
     * Do an actual insertion , (Definition 12 from Chickering, 2002).
     **/
    private void insert(Node x, Node y, Set<Node> t, Graph graph, double score, boolean log, double bump) {
        if (graph.isAdjacentTo(x, y)) {
            throw new IllegalArgumentException(x + " and " + y + " are already adjacent in the graph.");
        }

        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        graph.addDirectedEdge(x, y);

        if (log) {
            String label = trueGraph != null && trueEdge != null ? "*" : "";
            TetradLogger.getInstance().log("insertedEdges", graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) +
                    " " + t +
                    " (" + nf.format(score) + ") " + label);
            System.out.println(graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) +
                    " " + t +
                    " (" + nf.format(score) + ", " + bump + ") " + label);
        }

        for (Node _t : t) {
            Edge oldEdge = graph.getEdge(_t, y);

            if (oldEdge == null) throw new IllegalArgumentException("Not adjacent: " + _t + ", " + y);

            if (!Edges.isUndirectedEdge(oldEdge)) {
                throw new IllegalArgumentException("Should be undirected: " + oldEdge);
            }

            graph.removeEdge(_t, y);
            graph.addDirectedEdge(_t, y);

            if (log) {
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(_t, y));
                System.out.println("--- Directing " + oldEdge + " to " +
                        graph.getEdge(_t, y));
            }
        }
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private void delete(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log, double bump) {
        System.out.println("here comes the delete step"); //zqian
        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        if (log) {
            Edge oldEdge = graph.getEdge(x, y);

            String label = trueGraph != null && trueEdge != null ? "*" : "";
            TetradLogger.getInstance().log("deletedEdges", (graph.getNumEdges() - 1) + ". DELETE " + oldEdge +
                    " " + subset +
                    " (" + nf.format(score) + ") " + label);
            System.out.println((graph.getNumEdges() - 1) + ". DELETE " + oldEdge +
                    " " + subset +
                    " (" + nf.format(score) + ", " + bump + ") " + label);
        }

        graph.removeEdge(x, y);

        for (Node h : subset) {
            graph.removeEdge(y, h);
            graph.addDirectedEdge(y, h);

            if (log) {
                Edge oldEdge = graph.getEdge(y, h);
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(y, h));
            }

            if (Edges.isUndirectedEdge(graph.getEdge(y, h))) {
                if (!graph.isAdjacentTo(x, h)) throw new IllegalArgumentException("Not adjacent: " + x + ", " + h);

                graph.removeEdge(x, h);
                graph.addDirectedEdge(x, h);

                if (log) {
                    Edge oldEdge = graph.getEdge(x, h);
                    TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                            graph.getEdge(x, h));
                }
            }
        }
    }

    /**
     * Test if the candidate insertion is a valid operation
     * (Theorem 15 from Chickering, 2002).
     **/
    private boolean validInsert(Node x, Node y, Set<Node> t, Set<Node> naYX, Graph graph) {
        Set<Node> union = new HashSet<Node>(t);
        union.addAll(naYX);

        if (!isClique(union, graph)) {
            return false;
        }

        if (existsUnblockedSemiDirectedPath(y, x, union, graph)) {
            return false;
        }

        return true;
    }

    /**
     * Test if the candidate deletion is a valid operation (Theorem 17 from Chickering, 2002).
     */
    private static boolean validDelete(Set<Node> h, Set<Node> naXY, Graph graph) {
        Set<Node> set = new HashSet<Node>(naXY);
        set.removeAll(h);
        return isClique(set, graph);
    }

    //---Background knowledge methods.

    /**/
    private void addRequiredEdges(Graph graph) {
        for (Iterator<KnowledgeEdge> it =
                this.getKnowledge().requiredEdgesIterator(); it.hasNext();) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
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
                graph.removeEdge(nodeA, nodeB);

                /**************/
                if(!(nodeA == null || nodeB == null)){/*******************/ /*changed August 27. Need to not add edges with one part null.*/
                graph.addDirectedEdge(nodeA, nodeB);
                TetradLogger.getInstance().log("insertedEdges", "Adding edge by knowledge: " + graph.getEdge(nodeA, nodeB));
            }
            }
        }
    }

    /**
     * Use background knowledge to decide if an insert or delete operation does not orient edges in a forbidden
     * direction according to prior knowledge. If some orientation is forbidden in the subset, the whole subset is
     * forbidden.
     */
    private boolean validSetByKnowledge(Node x, Node y, Set<Node> subset,
                                        boolean insertMode) {
        if (insertMode) {
            for (Node node : subset) {
                if (getKnowledge().edgeForbidden(node.getName(),
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

    //--Auxiliary methods.

    /**
     * Find all nodes that are connected to Y by an undirected edge that are adjacent to X (that is, by undirected or
     * directed edge) NOTE: very inefficient implementation, since the current library does not allow access to the
     * adjacency list/matrix of the graph.
     */
    private static Set<Node> findNaYX(Node x, Node y, Graph graph) {
        Set<Node> naYX = new HashSet<Node>(graph.getAdjacentNodes(y));
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (Node z : new HashSet<Node>(naYX)) {
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return naYX;
    }

    /**
     * Returns true iif the given set forms a clique in the given graph.
     */
    private static boolean isClique(Set<Node> _nodes, Graph graph) {
        List<Node> nodes = new LinkedList<Node>(_nodes);
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (!graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean existsUnblockedSemiDirectedPath(Node node1, Node node2, Set<Node> cond, Graph graph) {
        return existsUnblockedSemiDirectedPathVisit(node1, node2,
                new LinkedList<Node>(), graph, cond);
    }

    private boolean existsUnblockedSemiDirectedPathVisit(Node node1, Node nodes2,
                                                         LinkedList<Node> path, Graph graph, Set<Node> cond) {
        if (cond.contains(node1)) return false;
        if (path.size() > 6) return false;
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseSemiDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (nodes2 == child) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsUnblockedSemiDirectedPathVisit(child, nodes2, path, graph, cond)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    private static List<Set<Node>> powerSet(List<Node> nodes) {
        List<Set<Node>> subsets = new ArrayList<Set<Node>>();
        int total = (int) Math.pow(2, nodes.size());
        for (int i = 0; i < total; i++) {
            Set<Node> newSet = new HashSet<Node>();
            String selection = Integer.toBinaryString(i);

            int shift = nodes.size() - selection.length();

            for (int j = nodes.size() - 1; j >= 0; j--) {
                if (j >= shift && selection.charAt(j - shift) == '1') {
                    newSet.add(nodes.get(j));
                }
            }
            subsets.add(newSet);
        }

        return subsets;
    }

    /**
     * Completes a pattern that was modified by an insertion/deletion operator Based on the algorithm described on
     * Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        addRequiredEdges(graph);
        pdagWithBk(graph, getKnowledge());

        TetradLogger.getInstance().log("rebuiltPatterns", "Rebuilt pattern = " + graph);
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to adapt Chickering's suggested algorithm above
     * (dagToPdag) to incorporate background knowledge, so I am also implementing this algorithm based on Meek's 1995
     * UAI paper. Notice it is the same implemented in PcSearch. </p> *IMPORTANT!* *It assumes all colliders are
     * oriented, as well as arrows dictated by time order.*
     */
    private void pdagWithBk(Graph graph, Knowledge knowledge) {
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);
    }

    private void setDataSet(DataSet dataSet) {
        List<String> _varNames = dataSet.getVariableNames();

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = dataSet.getVariables();
        this.dataSet = dataSet;
        this.discrete = dataSet.isDiscrete();

        if (!isDiscrete()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
        }
    }

    private void buildIndexing(Graph graph) {
        this.hashIndices = new HashMap<Node, Integer>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < this.varNames.length; i++) {
                if (this.varNames[i].equals(next.getName())) {
                    this.hashIndices.put(next, i);
                    break;
                }
            }
        }
    }


    //===========================SCORING METHODS===========================//

    public double scoreGraph(Graph graph) {
        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        double score = 0.;

        for (Node y : dag.getNodes()) {
            Set<Node> parents = new HashSet<Node>(dag.getParents(y));
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(y.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int parentIndices[] = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
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
            //	System.out.println("zqian##########Node " + nextIndex +" has "+count+" Parents." );
                //System.out.println("zqian##########Entering localDiscreteScore.");

                score += localDiscreteScore(nextIndex, parentIndices);
            } else {
                throw new UnsupportedOperationException("Not Implemented Yet!");
            }
        }
       // System.out.println("zqian##########Leaving scoreGraph(Graph) which has "+dag.getNodes().size()+ "Nodes.");

        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,Set<Node> parents2) {
        int yIndex = hashIndices.get(y);

        Double score1 = scoreHash.get(y).get(parents1);

        if (score1 == null) {
            int parentIndices1[] = new int[parents1.size()];

            int count = 0;
            for (Node aParents1 : parents1) {
                parentIndices1[count++] = (hashIndices.get(aParents1));
            }

            if (isDiscrete()) {
                score1 = localDiscreteScore(yIndex, parentIndices1);
            } else {
                throw new UnsupportedOperationException("Not Implemented Yet!");
            }

            scoreHash.get(y).put(parents1, score1);
        }

        Double score2 = scoreHash.get(y).get(parents2);

        if (score2 == null) {
            int parentIndices2[] = new int[parents2.size()];

            int count2 = 0;
            for (Node aParents2 : parents2) {
                parentIndices2[count2++] = (hashIndices.get(aParents2));
            }

            if (isDiscrete()) {
                score2 = localDiscreteScore(yIndex, parentIndices2);
            } else {
                throw new UnsupportedOperationException("Not Implemented Yet!");
            }

            scoreHash.get(y).put(parents2, score2);
        }

        // That is, the score for the variable set that contains x minus the score
        // for the variable set that does not contain x.
        return score1 - score2;
    }

    /**
     * Compute the local BDeu score of (i, parents(i)). See (Chickering, 2002).
     */
    private double localDiscreteScore(int i, int parents[]) {
        return getDiscreteScore().localScore(i, parents);
    }

    private List<Node> getVariables() {
        return variables;
    }

    private boolean isDiscrete() {
        return discrete;
    }

    private void fireGraphChange(Graph graph) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(new PropertyChangeEvent(this, "graph", null, graph));
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }
    // store top N graphs based on score , zqian
    private void storeGraph(Graph graph, double score) {
        if (!isStoreGraphs()) return;

        if (topGraphs.isEmpty() || score > topGraphs.first().getScore()) { // compare with the lowest score
            //Oct 30, bug? Arrow implies non-ancestor
            Graph graphCopy = new EdgeListGraph(graph);

            //System.out.println("Storing " + score + " " + graphCopy);
            if (topGraphs.size() > getNumPatternsToStore()) {  //should also check the size  before adding
                topGraphs.remove(topGraphs.first());
            }
            topGraphs.add(new ScoredGraph(graphCopy, score));

            if (topGraphs.size() > getNumPatternsToStore()) {  //should also check the size  before adding 
                topGraphs.remove(topGraphs.first());
            }
        }
    }

    public LocalDiscreteScore getDiscreteScore() {
        return discreteScore;
    }

    public void setDiscreteScore(LocalDiscreteScore discreteScore) {
        if (discreteScore.getDataSet() != dataSet) {
            throw new IllegalArgumentException("Must use the same data set.");
        }
        this.discreteScore = discreteScore;
    }
}