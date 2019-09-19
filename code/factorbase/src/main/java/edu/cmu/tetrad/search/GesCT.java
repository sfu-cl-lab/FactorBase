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
import java.util.stream.Collectors;

import ca.sfu.cs.factorbase.data.ContingencyTableGenerator;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.exception.ScoringException;
import ca.sfu.cs.factorbase.search.BDeuScore;
import ca.sfu.cs.factorbase.search.BDeuScoreOnDemand;
import ca.sfu.cs.factorbase.search.DiscreteLocalScore;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

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

public class GesCT {

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

    /**
     * True iff the data set is discrete.
     */
    private boolean discrete;

    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * The maximum number of edges the algorithm will add to the graph.
     */
    private int maxEdgesAdded = -1;

    /**
     * The score for discrete searches.
     */
    private DiscreteLocalScore discreteScore;

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

    /**
     * Create a GesCT using a contingency table that has already been generated.
     *
     * @param ctTableGenerator - {@code ContingencyTableGenerator} that extracts information from the contingency table
     *                           of interest.
     * @param samplePrior - the equivalent sample size (N').
     * @param structurePrior - the prior probability for the network structure.
     */
    public GesCT(ContingencyTableGenerator ctTableGenerator, double samplePrior, double structurePrior) {
        List<String> varNames = ctTableGenerator.getVariableNames();
        this.variables = varNames.stream().map(name -> new GraphNode(name)).collect(Collectors.toList());
        this.discrete = ctTableGenerator.isDiscrete();

        if (!isDiscrete()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
        }

        if (ctTableGenerator != null) {
            this.discreteScore = new BDeuScore(ctTableGenerator, samplePrior, structurePrior);
        }
    }

    /**
     * Create a GesCT creating contingency tables as needed using the given FactorBaseDataBase.
     *
     * @param database - {@code FactorBaseDataBase} to help generate contingency tables as needed.
     * @param functorNodesInfo - the information for the functornodes of interest.
     * @param samplePrior - the equivalent sample size (N').
     * @param structurePrior - the prior probability for the network structure.
     */
    public GesCT(
        FactorBaseDataBase database,
        FunctorNodesInfo functorNodesInfo,
        double samplePrior,
        double structurePrior
    ) {
        this.variables = functorNodesInfo.getFunctorNodes().stream().map(
            functorNode -> new GraphNode(functorNode.getFunctorNodeID())
        ).collect(Collectors.toList());

        this.discrete = functorNodesInfo.isDiscrete();

        if (!isDiscrete()) {
            throw new UnsupportedOperationException("Not Implemented Yet!");
        }

        if (functorNodesInfo != null) {
            this.discreteScore = new BDeuScoreOnDemand(database, functorNodesInfo, samplePrior, structurePrior);
        }
    }

    //==========================PUBLIC METHODS==========================//


    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     *
     * @return the resulting Pattern.
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    public Graph search() throws ScoringException {
        Graph graph = new EdgeListGraph(getVariables());

        scoreHash = new WeakHashMap<Node, Map<Set<Node>, Double>>();

        for (Node node : graph.getNodes()) {
            scoreHash.put(node, new HashMap<Set<Node>, Double>());
        }

        addRequiredEdges(graph);

        // Method 1-- original.

        // Don't need to score the original graph; the BIC scores all up to a constant.
//        double score = 0;
        double score = scoreGraph(graph);
        //Oct 30, bug? Arrow implies non-ancestor
        //score=0;
        storeGraph(new EdgeListGraph(graph), score);

       // System.out.println("######## finished the storing");
        // Do forward search.
        score = fes(graph, score);
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
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    @SuppressWarnings("unchecked")
    private double fes(Graph graph, double score) throws ScoringException {

        List<Node> nodes = graph.getNodes();

        sortedArrows = new TreeSet<Arrow>();
        lookupArrows = (HashSet<Arrow>[][]) new HashSet[nodes.size()][nodes.size()];

        nodesHash = new HashMap<Node, Integer>();
        int index = -1;

        for (Node node : nodes) {
            nodesHash.put(node, ++index);
        }

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


    private double bes(Graph graph, double score) throws ScoringException {
        List<Node> nodes = graph.getNodes();

        initializeArrowsBackward(graph);

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

    private void initializeArrowsForward(List<Node> nodes, Graph graph) throws ScoringException {
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
    private void initializeArrowsBackward(Graph graph) throws ScoringException {
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

    private void reevaluateFoward(Graph graph, List<Node> nodes, Arrow arrow) throws ScoringException {
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

    private void reevaluateBackward(Graph graph, List<Node> nodes, Arrow arrow) throws ScoringException {
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

    private void calculateArrowsForward(int i, int j, List<Node> nodes, Graph graph) throws ScoringException {
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

    private void calculateArrowsBackward(int i, int j, List<Node> nodes, Graph graph) throws ScoringException {
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
     *
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    private double insertEval(Node x, Node y, Set<Node> t, Set<Node> naYX, Graph graph) throws ScoringException {

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
     *
     * @throws ScoringException if an error occurs when trying to compute the score for the graphs being generated.
     */
    private double deleteEval(Node x, Node y, Set<Node> h, Set<Node> naYX, Graph graph) throws ScoringException {

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

        graph.addDirectedEdge(x, y);

        for (Node _t : t) {
            Edge oldEdge = graph.getEdge(_t, y);

            if (oldEdge == null) throw new IllegalArgumentException("Not adjacent: " + _t + ", " + y);

            if (!Edges.isUndirectedEdge(oldEdge)) {
                throw new IllegalArgumentException("Should be undirected: " + oldEdge);
            }

            graph.removeEdge(_t, y);
            graph.addDirectedEdge(_t, y);
        }
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private void delete(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log, double bump) {
        graph.removeEdge(x, y);

        for (Node h : subset) {
            graph.removeEdge(y, h);
            graph.addDirectedEdge(y, h);

            if (Edges.isUndirectedEdge(graph.getEdge(y, h))) {
                if (!graph.isAdjacentTo(x, h)) throw new IllegalArgumentException("Not adjacent: " + x + ", " + h);

                graph.removeEdge(x, h);
                graph.addDirectedEdge(x, h);
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


    //===========================SCORING METHODS===========================//

    public double scoreGraph(Graph graph) throws ScoringException {
        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        double score = 0.;

        for (Node y : dag.getNodes()) {
            Set<Node> parents = new HashSet<Node>(dag.getParents(y));
            Set<String> parentNames = parents.stream().map(node -> node.getName()).collect(Collectors.toSet());

            if (this.isDiscrete()) {
                score += localDiscreteScore(y.getName(), parentNames);
            } else {
                throw new UnsupportedOperationException("Not Implemented Yet!");
            }
        }

        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,Set<Node> parents2) throws ScoringException {
        Double score1 = computeScore(y, parents1);
        Double score2 = computeScore(y, parents2);

        return score1 - score2;
    }

    private Double computeScore(Node child, Set<Node> parents) throws ScoringException {
        Double score = scoreHash.get(child).get(parents);

        if (score == null) {
            Set<String> parentNames = parents.stream().map(node -> node.getName()).collect(Collectors.toSet());

            if (isDiscrete()) {
                score = localDiscreteScore(child.getName(), parentNames);
            } else {
                throw new UnsupportedOperationException("Not Implemented Yet!");
            }

            scoreHash.get(child).put(parents, score);
        }

        return score;
    }

    /**
     * Compute the local BDeu score of (i, parents(i)). See (Chickering, 2002).
     *
     * @throws ScoringException if there is an issue when computing the score.
     */
    private double localDiscreteScore(String child, Set<String> parents) throws ScoringException {
        return getDiscreteScore().localScore(child, parents);
    }

    private List<Node> getVariables() {
        return variables;
    }

    private boolean isDiscrete() {
        return discrete;
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

    public DiscreteLocalScore getDiscreteScore() {
        return discreteScore;
    }
}