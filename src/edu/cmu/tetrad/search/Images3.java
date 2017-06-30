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
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.StandardizedSemIm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * GesSearch is an implentation of the GES algorithm, as specified in Chickering (2002) "Optimal structure
 * identification with greedy search" Journal of Machine Learning Research. It works for both BayesNets and SEMs.
 * <p/>
 * Some code optimization could be done for the scoring part of the graph for discrete models (method scoreGraphChange).
 * Some of Andrew Moore's approaches for caching sufficient statistics, for instance.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class Images3 implements GraphSearch, IImages {

    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private List<DataSet> dataSets;

    /**
     * The covariance matrices for the data set.
     */
    private List<DoubleMatrix2D> covariances;

//    /**
//     * The data set, various variable subsets of which are to be scored.
//     */
//    private DataSet dataSet;

//    /**
//     * The covariance matrix for the data set.
//     */
//    private DoubleMatrix2D covariances;

    /**
     * Sample size, either from the data set or from the variances.
     */
    private int sampleSize;

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

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
     * For linear algebra.
     */
    private final Algebra algebra = new Algebra();                        // s = vv * x, or s12 = Theta.1 * Theta.12

    /**
     * Caches scores for discrete search.
     */
    private final LocalScoreCache localScoreCache = new LocalScoreCache();

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;

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
     * Penalty discount--the BIC penalty is multiplied by this (for continuous variables).
     */
    private double penaltyDiscount = 1.0;

//    private boolean useFCutoff = false;
//
//    private double fCutoffP = 0.01;

    /**
     * The maximum number of edges the algorithm will add to the graph.
     */
    private int maxEdgesAdded = -1;

    /**
     * The score for discrete searches.
     */
    private LocalDiscreteScore discreteScore;

    /**
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();

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

    private double bic;
    private Map<DataSet, List<Node>> missingVariables;
    private Graph returnGraph;
    private int maxNumEdges = -1;


    //===========================CONSTRUCTORS=============================//

    public Images3(List<DataSet> dataSets) {
        setDataSets(dataSets);
    }

    //==========================PUBLIC METHODS==========================//


    @Override
	public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    @Override
	public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     *
     * @return the resulting Pattern.
     */
    @Override
	public Graph search() {
        long startTime = System.currentTimeMillis();

        topGraphs = new TreeSet<ScoredGraph>();

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

        storeGraph(new EdgeListGraph(graph), score);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

//        score = fes(graph, score);
//        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;
        this.logger.log("graph", "\nReturning this graph: " + graph);

        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.flush();

//        return new ArrayList<ScoredGraph>(topGraphs).get(topGraphs.size() - 1).getGraph();
        return graph;

//        // Method 2-- Ricardo's tweak.
//        double score = scoreGraph(graph), newScore;
//
//        storeGraph(graph, score);
//
//        int iter = 0;
//        do {
//            newScore = fes(graph, score);
//            if (newScore > score) {
//                score = newScore;
//                newScore = bes(graph, score);
//
//                if (newScore > score) {
//                    score = newScore;
//                }
//                else {
//                    break;
//                }
//            }
//            else {
//                break;
//            }
//            //System.out.println("Current score = " + score);
//            iter++;
//        } while (iter < 100);
//
//        long endTime = System.currentTimeMillis();
//        this.elapsedTime = endTime - startTime;
//        this.logger.log("graph", "\nReturning this graph: " + graph);
//
//        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
//        this.logger.flush();
//
//        return graph;
    }

    @Override
	public Graph search(List<Node> nodes) {
        topGraphs = new TreeSet<ScoredGraph>();

        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!variables.containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in the supplied data set.");
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

        TetradLogger.getInstance().log("graph", "\nReturning this graph: " + graph);
        TetradLogger.getInstance().log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");

        this.returnGraph = graph;

        return graph;
    }

    @Override
	public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     *
     * @param knowledge the knowledge object, specifying forbidden and required edges.
     */
    @Override
	public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    @Override
	public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
	public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        getListeners().add(l);
    }

    @Override
	public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    @Override
	public void setPenaltyDiscount(double penaltyDiscount) {
        if (penaltyDiscount < 0) {
            throw new IllegalArgumentException("Penalty discount must be >= 0: "
                    + penaltyDiscount);
        }

        this.penaltyDiscount = penaltyDiscount;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    @Override
	public double getScore(Graph dag) {
        return scoreGraph(dag);
    }

    @Override
	public SortedSet<ScoredGraph> getTopGraphs() {
        return topGraphs;
    }

    @Override
	public int getNumPatternsToStore() {
        return numPatternsToStore;
    }

    @Override
	public void setNumPatternsToStore(int numPatternsToStore) {
        if (numPatternsToStore < 1) {
            throw new IllegalArgumentException("Must store at least one pattern: " + numPatternsToStore);
        }

        this.numPatternsToStore = numPatternsToStore;
    }

    public boolean isStoreGraphs() {
        return storeGraphs;
    }

    public void setStoreGraphs(boolean storeGraphs) {
        this.storeGraphs = storeGraphs;
    }


    //===========================PRIVATE METHODS========================//

//    private void initialize(double samplePrior, double structurePrior) {
//        setStructurePrior(structurePrior);
//        setSamplePrior(samplePrior);
//    }

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence search.
     * @param score The score in the state prior to the forward equivalence search
     * @return the score in the state after the forward equivelance search. Note that the graph is changed as a
     *         side-effect to its state after the forward equivelance search.
     */
    private double fes(Graph graph, double score) {

        List<Node> nodes = graph.getNodes();

        sortedArrows = new TreeSet<Arrow>();
        lookupArrows = new HashSet[nodes.size()][nodes.size()];

        nodesHash = new HashMap<Node, Integer>();
        int index = -1;

        for (Node node : nodes) {
            nodesHash.put(node, ++index);
        }

        TetradLogger.getInstance().log("info", "** FORWARD EQUIVALENCE SEARCH");
        TetradLogger.getInstance().log("info", "Initial Score = " + nf.format(score));

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

//            if (covariances != null && minJump == 0 && isUseFCutoff()) {
//                double _p = getfCutoffP();
//                double v;
//
//                // Find the value for v that will yield p = _p
//
//                for (v = 0.0; ; v += 0.25) {
//                    int n = sampleSize();
//                    double f = Math.exp((v - Math.log(n)) / (n / 2.0));
//                    double p = 1 - ProbUtils.fCdf(f, n, n);
//
//                    if (p <= _p) {
//                        break;
//                    }
//                }
//
//                minJump = v;
//            }

            score = score + bump;
            insert(x, y, t, graph, score, true, bump);
            rebuildPattern(graph);

            storeGraph(graph, score);

            reevaluateFoward(graph, nodes, arrow);

            if (getMaxNumEdges() != -1 && graph.getNumEdges() >= getMaxNumEdges()) {
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
        TetradLogger.getInstance().log("info", "Initial Score = " + nf.format(score));

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

    private void initializeArrowsForward(List<Node> nodes, Graph graph) {
        Set<Node> empty = Collections.emptySet();

        for (int j = 0; j < nodes.size(); j++) {
            for (int i = 0; i < nodes.size(); i++) {
                if (j == i) continue;

                Node _x = nodes.get(i);
                Node _y = nodes.get(j);

                if (getKnowledge().edgeForbidden(_x.getName(),
                        _y.getName())) {
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

    private void initializeArrowsBackward(Graph graph) {
        List<Node> nodes = graph.getNodes();
        sortedArrowsBackwards = new TreeSet<Arrow>();
        lookupArrowsBackwards = new HashSet[nodes.size()][nodes.size()];

        List<Edge> graphEdges = graph.getEdges();

        for (Edge edge : graphEdges) {
            Node _x = edge.getNode1();
            Node _y = edge.getNode2();

            int i = nodesHash.get(edge.getNode1());
            int j = nodesHash.get(edge.getNode2());

            if (!getKnowledge().noEdgeRequired(_x.getName(),
                    _y.getName())) {
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

            if (bump > minNeg) {
                if (lookupArrowsBackwards[i][j] == null) {
                    lookupArrowsBackwards[i][j] = new HashSet<Arrow>();
                }

                sortedArrowsBackwards.add(arrow);
                lookupArrowsBackwards[i][j].add(arrow);
            }
        }
    }

//    /**
//     * True iff the f cutoff should be used in the forward search.
//     */
//    public boolean isUseFCutoff() {
//        return useFCutoff;
//    }
//
//    public void setUseFCutoff(boolean useFCutoff) {
//        this.useFCutoff = useFCutoff;
//    }

//    /**
//     * The P value for the f cutoff, if used.
//     */
//    public double getfCutoffP() {
//        return fCutoffP;
//    }
//
//    public void setfCutoffP(double fCutoffP) {
//        if (fCutoffP < 0.0 || fCutoffP > 1.0) {
//            throw new IllegalArgumentException();
//        }
//
//        this.fCutoffP = fCutoffP;
//    }


    @Override
	public void setMinJump(double minJump) {
        this.minJump = minJump;
    }

    private static class Arrow implements Comparable {
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
		public int compareTo(Object o) {
            Arrow info = (Arrow) o;
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

        return scoreGraphChange(y, set1, set2);
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

    /*
    * Do an actual insertion
    * (Definition 12 from Chickering, 2002).
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
//            System.out.println(graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) +
//                    " " + t +
//                    " (" + nf.format(score) + ", " + bump + ") " + label);
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
//                System.out.println("--- Directing " + oldEdge + " to " +
//                        graph.getEdge(_t, y));
            }
        }
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private void delete(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log, double bump) {

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
//            System.out.println((graph.getNumEdges() - 1) + ". DELETE " + oldEdge +
//                    " " + subset +
//                    " (" + nf.format(score)  + ", " + bump + ") " + label);
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

    /*
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
                graph.removeEdges(nodeA, nodeB);
                graph.addDirectedEdge(nodeA, nodeB);
                TetradLogger.getInstance().log("insertedEdges", "Adding edge by knowledge: " + graph.getEdge(nodeA, nodeB));
            }
        }
        for (Iterator<KnowledgeEdge> it =
                getKnowledge().forbiddenEdgesIterator(); it.hasNext();) {
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
            if (nodeA != null && nodeB != null && graph.isAdjacentTo(nodeA, nodeB) &&
                    !graph.isChildOf(nodeA, nodeB)) {
                if (!graph.isAncestorOf(nodeA, nodeB)) {
                    graph.removeEdges(nodeA, nodeB);
                    graph.addDirectedEdge(nodeB, nodeA);
                    TetradLogger.getInstance().log("insertedEdges", "Adding edge by knowledge: " + graph.getEdge(nodeB, nodeA));
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

    private void setDataSets(List<DataSet> dataSets) {
        List<String> varNames = dataSets.get(0).getVariableNames();

        for (int i = 2; i < dataSets.size(); i++) {
            List<String> _varNames = dataSets.get(i).getVariableNames();

            if (!varNames.equals(_varNames)) {
                throw new IllegalArgumentException("Variable names not consistent.");
            }
        }

        this.varNames = varNames.toArray(new String[varNames.size()]);
        this.sampleSize = dataSets.get(0).getNumRows();

        this.variables = dataSets.get(0).getVariables();
        this.dataSets = dataSets;
        this.discrete = dataSets.get(0).isDiscrete();

        if (!isDiscrete()) {
            this.covariances = new ArrayList<DoubleMatrix2D>();

            for (int i = 0; i < dataSets.size(); i++) {
                this.covariances.add(dataSets.get(i).getCovarianceMatrix());
            }
        }

        missingVariables = new HashMap<DataSet, List<Node>>();

        for (DataSet dataSet : dataSets) {
            missingVariables.put(dataSet, new ArrayList<Node>());
        }

        for (DataSet dataSet : dataSets) {
            for (Node node : dataSet.getVariables()) {
                int index = dataSet.getVariables().indexOf(node);
                boolean missing = true;

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    if (!Double.isNaN(dataSet.getDouble(i, index))) {
                        missing = false;
                        break;
                    }
                }

                if (missing) {
                    missingVariables.get(dataSet).add(node);
                }
            }
        }

        setKnowledge(dataSets.get(0).getKnowledge());
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

    private RegressionDataset regression;

    @Override
	public double scoreGraph(Graph graph) {
        TetradLogger.getInstance().log("info", "Scoring graph");

        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Collection<Node> parents = dag.getParents(next);
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(next.getName())) {
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

            score += localSemScore(nextIndex, parentIndices);
        }

        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,
                                    Set<Node> parents2) {
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

        double score1 = localSemScore(yIndex, parentIndices1);
        double score2 = localSemScore(yIndex, parentIndices2);

        return score1 - score2;
    }

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in a simple SEM model.
     */
    private double localSemScore(int i, int[] parents) {
        double sum = 0.0;

        for (int dataIndex = 0; dataIndex < dataSets().size(); dataIndex++) {
            double score = localSemScoreOneDataSet(dataIndex, i, parents);
            sum += score;
        }

        return sum / dataSets().size();
    }

    private double localSemScoreOneDataSet(int dataIndex, int i, int[] parents) {
        DataSet dataSet = dataSets.get(dataIndex);

        double c = getPenaltyDiscount();
        int n = dataSets().get(dataIndex).getNumRows();
        parents = eliminateMissing(parents, dataIndex);
        double k = parents.length + 1;

        if (missingVariables.get(dataSet).contains(dataSet.getVariable(i))) {
            return 0;
//            int df = parents.length > 0 ? 1 : 0;
//            return - c * df * Math.log(n);
//            return -c * k * Math.log(n);
        }

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        double variance = getCovMatrices().get(dataIndex).get(i, i);
//        int n = dataSets().get(dataIndex).getNumRows();

        if (parents.length > 0) {

            // Regress z onto i, yielding regression coefficients b.
            DoubleMatrix2D Czz =
                    getCovMatrices().get(dataIndex).viewSelection(parents, parents);
            DoubleMatrix2D inverse;
            try {
//                inverse = algebra().inverse(Czz);
                inverse = MatrixUtils.ginverse(Czz);
            }
            catch (Exception e) {
                StringBuilder buf = new StringBuilder();
                buf.append("Could not invert matrix for variables: ");

                for (int j = 0; j < parents.length; j++) {
                    buf.append(variables.get(parents[j]));

                    if (j < parents.length - 1) {
                        buf.append(", ");
                    }
                }

                throw new IllegalArgumentException(buf.toString());
            }

            DoubleMatrix1D Cyz = getCovMatrices().get(dataIndex).viewColumn(i);
            Cyz = Cyz.viewSelection(parents);
            DoubleMatrix1D b = algebra().mult(inverse, Cyz);

            variance -= algebra().mult(Cyz, b);
        }

        if (variance == 0.0) {
            throw new IllegalArgumentException("Zero residual detected in data set #" + (dataIndex + 1)
                  + "; please check data for multicollinearity");
        }

        // Notice when it sums it up for all data sets it will be m * k parameters.
        // 2L - k ln n
        double score = -n * Math.log(variance) - n * Math.log(2 * Math.PI) - n - k * c * Math.log(n);

        return score;
    }

    private int[] eliminateMissing(int[] parents, int dataIndex) {
        List<Integer> _parents = new ArrayList<Integer>();
        DataSet dataSet = dataSets.get(dataIndex);

        for (int k : parents) {
            if (!missingVariables.get(dataSet).contains(dataSet.getVariable(k))) {
                _parents.add(k);
            }
        }

        int[] _parents2 = new int[_parents.size()];

        for (int i = 0; i < _parents.size(); i++) {
            _parents2[i] = _parents.get(i);
        }

        return _parents2;
    }


    private int sampleSize() {
        return this.sampleSize;
    }

    private List<Node> getVariables() {
        return variables;
    }

    private List<DoubleMatrix2D> getCovMatrices() {
        return covariances;
    }

    private Algebra algebra() {
        return algebra;
    }

    private List<DataSet> dataSets() {
        return dataSets;
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

    private void storeGraph(Graph graph, double score) {
//        if (Double.isInfinite(score)) {
//            System.out.println();
//        }

        if (!isStoreGraphs()) return;

        if (topGraphs.isEmpty() || score > topGraphs.first().getScore()) {
            Graph graphCopy = new EdgeListGraph(graph);

            topGraphs.add(new ScoredGraph(graphCopy, score));

            if (topGraphs.size() > getNumPatternsToStore()) {
                topGraphs.remove(topGraphs.first());
            }
        }
    }

    @Override
	public Map<Edge, Integer> getBoostrapCounts(int numBootstraps) {
        if (returnGraph == null) {
            returnGraph = search();
        }
        return bootstrapImagesCounts(dataSets, returnGraph.getNodes(), getKnowledge(), numBootstraps, getPenaltyDiscount());

    }

    @Override
	public String bootstrapPercentagesString(int numBootstraps) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        StringBuilder builder = new StringBuilder(
                "For " + numBootstraps + " repetitions, the percentage of repetitions in which each " +
                        "edge occurs in the IMaGES pattern for that repetition. In each repetition, for each " +
                        "input data set, a sample the size of that data set chosen randomly and with replacement. " +
                        "Images is run on the collection of these data sets. 100% for an edge means that that " +
                        "edge occurs in all such randomly chosen samples, over " + numBootstraps + " repetitions; " +
                        "0% means it never occurs. Edges not mentioned occur in 0% of the random samples.\n\n"
        );

        Map<Edge, Integer> counts = getBoostrapCounts(numBootstraps);
        builder.append(edgePercentagesString(counts, returnGraph.getEdges(), "The estimated pattern", null, numBootstraps));

        return builder.toString();
    }

    @Override
	public String gesCountsString() {
        if (returnGraph == null) {
            returnGraph = search();
        }
        Map<Edge, Integer> counts = getGesCounts(dataSets(), returnGraph.getNodes(), getKnowledge(), getPenaltyDiscount());
        return gesEdgesString(counts, dataSets());
    }

    private Map<Edge, Integer> getGesCounts(List<DataSet> dataSets, List<Node> nodes, Knowledge knowledge, double penalty) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        Map<Edge, Integer> counts = new HashMap<Edge, Integer>();

        for (DataSet dataSet : dataSets) {
            Ges ges = new Ges(dataSet);
            ges.setKnowledge(knowledge);
            ges.setPenaltyDiscount(penalty);
            Graph pattern = ges.search();

            incrementCounts(counts, pattern, nodes);
        }

        return counts;
    }

    @Override
	public Map<Edge, Double> averageStandardizedCoefficients() {
        if (returnGraph == null) {
            returnGraph = search();
        }

        return averageStandardizedCoefficients(returnGraph);
    }

    @Override
	public Map<Edge, Double> averageStandardizedCoefficients(Graph graph) {

        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        Map<Edge, Double> coefs = new HashMap<Edge, Double>();

        for (DataSet dataSet : dataSets) {
            SemPm pm = new SemPm(dag);
            Graph _graph = pm.getGraph();
            SemEstimator estimator = new SemEstimator(dataSet, pm);
            SemIm im = estimator.estimate();
            StandardizedSemIm im2 = new StandardizedSemIm(im);

            for (Edge edge : _graph.getEdges()) {
                edge = translateEdge(edge, dag);

                if (coefs.get(edge) == null) {
                    coefs.put(edge, 0.0);
                }

                coefs.put(edge, coefs.get(edge) + im2.getParameterValue(edge));
            }
        }

        for (Edge edge : coefs.keySet()) {
            coefs.put(edge, coefs.get(edge) / dataSets.size());
        }

        return coefs;
    }

    @Override
	public String averageStandardizedCoefficientsString() {
        if (returnGraph == null) {
            returnGraph = search();
        }

        Graph graph = GraphUtils.randomDag(returnGraph.getNodes(), 12, true);
        return averageStandardizedCoefficientsString(graph);
    }

    @Override
	public String averageStandardizedCoefficientsString(Graph graph) {
        Map<Edge, Double> coefs = averageStandardizedCoefficients(graph);
        return edgeCoefsString(coefs, graph.getEdges(), "Estimated graph",
                "Average standardized coefficient");
    }

    @Override
	public String logEdgeBayesFactorsString(Graph dag) {
        Map<Edge, Double> coefs = logEdgeBayesFactors(dag);
        return logBayesPosteriorFactorsString(coefs, scoreGraph(dag), dag.getEdges());
    }

    @Override
	public Map<Edge, Double> logEdgeBayesFactors(Graph dag) {
        Map<Edge, Double> logBayesFactors = new HashMap<Edge, Double>();
        double withEdge = scoreGraph(dag);

        for (Edge edge : dag.getEdges()) {
            dag.removeEdge(edge);
            double withoutEdge = scoreGraph(dag);
            double difference = withoutEdge - withEdge;
            logBayesFactors.put(edge, difference);
            dag.addEdge(edge);
        }

        return logBayesFactors;
    }


    private Edge translateEdge(Edge edge, Graph graph) {
        Node node1 = graph.getNode(edge.getNode1().getName());
        Node node2 = graph.getNode(edge.getNode2().getName());
        return new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
    }

    private String gesEdgesString(Map<Edge, Integer> counts, List<DataSet> dataSets) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        return edgePercentagesString(counts, returnGraph.getEdges(), "Estimated graph",
                "Percentage of GES results each edge participates in", dataSets.size());
    }


    /**
     * Bootstraps images counts at a particular penalty level.
     *
     * @param dataSets      The data sets from which bootstraps are drawn. These must share the same variable set, be
     *                      continuous, but may have different sample sizes.
     * @param nodes         The nodes over which edge counts are to be done. Why not specify this in advance?
     * @param knowledge     Knowledge under which IMaGES should operate.
     * @param numBootstraps The number of bootstrap samples to be drawn.
     * @param penalty       The penalty discount at which the bootstrap analysis is to be done.
     * @return A map from edges to counts, where the edges are over the nodes of the datasets.
     */
    private Map<Edge, Integer> bootstrapImagesCounts(List<DataSet> dataSets, List<Node> nodes, Knowledge knowledge,
                                                     int numBootstraps, double penalty) {
        List<Node> dataVars = dataSets.get(0).getVariables();

        for (DataSet dataSet : dataSets) {
            if (!dataSet.getVariables().equals(dataVars)) {
                throw new IllegalArgumentException("Data sets must share the same variable set.");
            }
        }

        Map<Edge, Integer> counts = new HashMap<Edge, Integer>();

        for (int i = 0; i < numBootstraps; i++) {
            List<DataSet> bootstraps = new ArrayList<DataSet>();

            for (DataSet dataSet : dataSets) {
                bootstraps.add(DataUtils.getBootstrapSample(dataSet, dataSet.getNumRows()));
//                bootstraps.add(dataSet);
            }

            Images3 images = new Images3(bootstraps);
            images.setPenaltyDiscount(penalty);

//            ImagesFirstNontriangular images = new ImagesFirstNontriangular(bootstraps);

            images.setKnowledge(knowledge);
            Graph pattern = images.search();
            incrementCounts(counts, pattern, nodes);
        }

        return counts;
    }

    private void incrementCounts(Map<Edge, Integer> counts, Graph pattern, List<Node> nodes) {
        Graph _pattern = GraphUtils.replaceNodes(pattern, nodes);

        for (Edge e : _pattern.getEdges()) {
            if (counts.get(e) == null) {
                counts.put(e, 0);
            }

            counts.put(e, counts.get(e) + 1);
        }
    }

    /**
     * Prints edge counts, with edges in the order of the adjacencies in <code>edgeList</code>.
     *
     * @param counts           A map from edges to counts.
     * @param edgeList         A list of edges, the true edges or estimated edges.
     * @param edgeListLabel    A label for the edge list, e.g. "True edges" or "Estimated edges".
     * @param percentagesLabel
     * @param numBootstraps
     */
    private String edgePercentagesString(Map<Edge, Integer> counts, List<Edge> edgeList, String edgeListLabel,
                                         String percentagesLabel, int numBootstraps) {
        NumberFormat nf = new DecimalFormat("0");
        StringBuilder builder = new StringBuilder();

        if (percentagesLabel != null) {
            builder.append("\n" + percentagesLabel + ":\n\n");
        }

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            int total = 0;

            for (Edge _edge : new HashMap<Edge, Integer>(counts).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    total += counts.get(_edge);
                    double percentage = counts.get(_edge) / (double) numBootstraps * 100.;
                    builder.append((i + 1) + ". " + _edge + " " + nf.format(percentage) + "%\n");
                    counts.remove(_edge);
                }
            }

            double percentage = total / (double) numBootstraps * 100.;
            builder.append("   (Sum = " + nf.format(percentage) + "%)\n\n");
        }

        // The left over edges.
        builder.append("Edges not adjacent in the estimated pattern:\n\n");

//        for (Edge edge : counts.keySet()) {
//            double percentage = counts.get(edge) / (double) numBootstraps * 100.;
//            builder.append(edge + " " + nf.format(percentage) + "%\n");
//        }

        for (Edge edge : new ArrayList<Edge>(counts.keySet())) {
            if (!counts.keySet().contains(edge)) continue;

            int total = 0;

            for (Edge _edge : new HashMap<Edge, Integer>(counts).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    total += counts.get(_edge);
                    double percentage = counts.get(_edge) / (double) numBootstraps * 100.;
                    builder.append(_edge + " " + nf.format(percentage) + "%\n");
                    counts.remove(_edge);
                }
            }

            double percentage = total / (double) numBootstraps * 100.;
            builder.append("   (Sum = " + nf.format(percentage) + "%)\n\n");
        }

        builder.append("\nThe estimated pattern, for reference:\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            builder.append(((i + 1) + ". " + edge + "\n"));
        }

        return builder.toString();
    }

    private String edgeCoefsString(Map<Edge, Double> coefs, List<Edge> edgeList, String edgeListLabel,
                                   String percentagesLabel) {
        NumberFormat nf = new DecimalFormat("0.00");
        StringBuilder builder = new StringBuilder();

        builder.append("\n" + edgeListLabel + ":\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            builder.append(((i + 1) + ". " + edge + "\n"));
        }

        builder.append("\n" + percentagesLabel + ":\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);

            for (Edge _edge : new HashMap<Edge, Double>(coefs).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    double coef = coefs.get(_edge);
                    builder.append((i + 1) + ". " + _edge + " " + nf.format(coef) + "\n");
                    coefs.remove(_edge);
                }
            }
        }


        return builder.toString();
    }

    private String logBayesPosteriorFactorsString(final Map<Edge, Double> coefs, double modelScore, List<Edge> edgeList) {
        NumberFormat nf = new DecimalFormat("0.00");
        StringBuilder builder = new StringBuilder();

        SortedMap<Edge, Double> sortedCoefs = new TreeMap<Edge, Double>(new Comparator<Edge>() {
            @Override
			public int compare(Edge edge1, Edge edge2) {
                return new Double(coefs.get(edge1)).compareTo(new Double(coefs.get(edge2)));
            }
        });

        sortedCoefs.putAll(coefs);

        builder.append("Model score: " + nf.format(modelScore) + "\n\n");

        builder.append("Edge Posterior Log Bayes Factors:\n\n");

        builder.append("For a DAG in the IMaGES pattern with model score m, for each edge e in the " +
                "DAG, the model score that would result from removing each edge, calculating " +
                "the resulting model score m(e), and then reporting m(e) - m. The score used is " +
                "the IMScore, L - SUM_i{kc ln n(i)}, L is the maximum likelihood of the model, " +
                "k isthe number of parameters of the model, n(i) is the sample size of the ith " +
                "data set, and c is the penalty discount. Note that the more negative the score, " +
                "the more important the edge is to the posterior probability of the IMaGES model. " +
                "Edges are given in order of their importance so measured.\n\n");

        int i = 0;

        for (Edge edge : sortedCoefs.keySet()) {
            builder.append((++i) + ". " + edge + " " + nf.format(sortedCoefs.get(edge)) + "\n");
        }


//        for (int i = 0; i < edgeList.size(); i++) {
//            Edge edge = edgeList.get(i);
//
//            for (Edge _edge : new HashMap<Edge, Double>(sortedCoefs).keySet()) {
//                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
//                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
//                    double coef = sortedCoefs.get(_edge);
//                    builder.append((i + 1) + ". " + _edge + " " + nf.format(coef) + "\n");
//                    sortedCoefs.remove(_edge);
//                }
//            }
//        }


        return builder.toString();
    }


    @Override
	public double getModelScore() {
        return bic;
    }

    @Override
	public int getMaxNumEdges() {
        return maxNumEdges;
    }

    @Override
	public void setMaxNumEdges(int maxNumEdges) {
        if (maxNumEdges < -1) throw new IllegalArgumentException();

        this.maxNumEdges = maxNumEdges;
    }

}




