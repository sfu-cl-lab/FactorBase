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
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.util.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

public final class Ges3 implements GraphSearch, GraphScorer {

    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private DataSet dataSet;

    /**
     * The covariance matrix for the data set.
     */
    private DoubleMatrix2D covariances;

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

    private boolean useFCutoff = false;

    private double fCutoffP = 0.01;

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


    //===========================CONSTRUCTORS=============================//

    public Ges3(DataSet dataSet) {
        setDataSet(dataSet);
        if (dataSet != null) {
            setDiscreteScore(new BDeuScore(dataSet, 10, 1.0));
//            discreteScore = new MdluScore(dataSet, .001);
        }
        initialize(10., 0.001);
    }

    public Ges3(ICovarianceMatrix covMatrix) {
        setCovMatrix(covMatrix);
        if (dataSet != null) {
//            setDiscreteScore(new BDeuScore(dataSet, 10, .001));
            discreteScore = new MdluScore(dataSet, .001);
        }
        initialize(10., 1.0);
    }

    //==========================PUBLIC METHODS==========================//


    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

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
//        long startTime = System.currentTimeMillis();

        // Check for missing values.
        if (covariances != null && DataUtils.containsMissingValue(covariances)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

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
    
    /*pruning phase using BES search @ zqian, Oct 23, 2013*/
    public Graph Pruning_BES() {
        long startTime = System.currentTimeMillis();

        // Check for missing values.
        if (covariances != null && DataUtils.containsMissingValue(covariances)) {
            throw new IllegalArgumentException("Please remove or impute missing values first.");
        }

        // Check for missing values.
        if (dataSet != null && DataUtils.containsMissingValue(dataSet)) {
            throw new IllegalArgumentException("Please remove or impute missing values first.");
        }
        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));

        scoreHash = new WeakHashMap<Node, Map<Set<Node>, Double>>();

        for (Node node : graph.getNodes()) {
            scoreHash.put(node, new HashMap<Set<Node>, Double>());
        }

        fireGraphChange(graph);
        buildIndexing(graph);
        addRequiredEdges(graph);

        double score = scoreGraph(graph);
        storeGraph(new EdgeListGraph(graph), score);
      
        System.out.println("NO FES search, score before BES :"+ score +"\n");
        // Do backward search.

        score = Pruning_bes(graph, score);
        System.out.println("Bes Search is Done, here is  BDeu Score "+ score +"\n");

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        return graph;

    }
    
    public Graph search(List<Node> nodes) {
        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!dataSet().getVariables().containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in " + "the supplied data set.");
        }

        Graph graph = new EdgeListGraph(nodes);
        buildIndexing(graph);
        addRequiredEdges(graph);
        double score = 0; //scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        this.logger.log("graph", "\nReturning this graph: " + graph);

        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.flush();
        return graph;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.     *
     * @param knowledge the knowledge object, specifying forbidden and required edges.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    public void setStructurePrior(double structurePrior) {
        if (getDiscreteScore() != null) {
            getDiscreteScore().setStructurePrior(structurePrior);
        }
        this.structurePrior = structurePrior;
    }

    public void setSamplePrior(double samplePrior) {
        if (getDiscreteScore() != null) {
            getDiscreteScore().setSamplePrior(samplePrior);
        }
        this.samplePrior = samplePrior;
    }

    @Override
	public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getListeners().add(l);
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        if (penaltyDiscount < 0) {
            throw new IllegalArgumentException("Penalty discount must be >= 0: "
                    + penaltyDiscount);
        }

        this.penaltyDiscount = penaltyDiscount;
    }

    public int getMaxEdgesAdded() {
        return maxEdgesAdded;
    }

    public void setMaxEdgesAdded(int maxEdgesAdded) {
        if (maxEdgesAdded < -1) throw new IllegalArgumentException();

        this.maxEdgesAdded = maxEdgesAdded;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public double getScore(Graph dag) {
        return scoreGraph(dag);
    }

    public SortedSet<ScoredGraph> getTopGraphs() {
        return topGraphs;
    }

    public int getNumPatternsToStore() {
        return numPatternsToStore;
    }

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

    private void initialize(double samplePrior, double structurePrior) {
        setStructurePrior(structurePrior);
        setSamplePrior(samplePrior);
    }

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

            if (covariances != null && minJump == 0 && isUseFCutoff()) {
                double _p = getfCutoffP();
                double v;

                // Find the value for v that will yield p = _p

                for (v = 0.0; ; v += 0.25) {
                    int n = sampleSize();
                    double f = Math.exp((v - Math.log(n)) / n);
                    double p = 1 - ProbUtils.fCdf(f, n, n);

                    if (p <= _p) {
                        break;
                    }
                }

                minJump = v;
            }

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

    private double Pruning_bes(Graph graph, double score) {
        List<Node> nodes = graph.getNodes();

        nodesHash = new HashMap<Node, Integer>();  // Oct 23 2013, copied from fes
        int index = -1;
        for (Node node : nodes) {
            nodesHash.put(node, ++index);
        }
       System.out.println("Within Pruning_BES Search " + score );//+ " " + graph); // Oct 23
       
       Pruning_initializeArrowsBackward(graph); //??
        
       System.out.println("sortedArrowsBackwards.isEmpty() is : " + sortedArrowsBackwards.isEmpty() );
       // here is the key?  often empty?
        while (!sortedArrowsBackwards.isEmpty()) {
            Arrow arrow = sortedArrowsBackwards.first();
            sortedArrowsBackwards.remove(arrow);

            Node _x = nodes.get(arrow.getX());
            Node _y = nodes.get(arrow.getY());

            if (!graph.isAdjacentTo(_x, _y)) {
                continue;
            }

            if (!findNaYX(_x, _y, graph).equals(arrow.getNaYX())) {
            	Pruning_reevaluateBackward(graph, nodes, arrow);
                continue;
            }

            if (!new HashSet<Node>(getHNeighbors(_x, _y, graph)).containsAll(arrow.getHOrT())) {
            	Pruning_reevaluateBackward(graph, nodes, arrow);
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
            Pruning_rebuildPattern(graph);

            storeGraph(graph, score);

            Pruning_reevaluateBackward(graph, nodes, arrow);
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

    private void initializeArrowsBackward(Graph graph) {
        List<Node> nodes = graph.getNodes();
        sortedArrowsBackwards = new TreeSet<Arrow>();
        lookupArrowsBackwards = new HashSet[nodes.size()][nodes.size()];
        

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

    private void Pruning_initializeArrowsBackward(Graph graph) {
        List<Node> nodes = graph.getNodes();
        sortedArrowsBackwards = new TreeSet<Arrow>();
        lookupArrowsBackwards = new HashSet[nodes.size()][nodes.size()];
        

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
            
            System.out.println("Edges.isDirectedEdge(edge) :"+ Edges.isDirectedEdge(edge));
            
            if (Edges.isDirectedEdge(edge)) {
            	 
            	Pruning_calculateArrowsBackward(i, j, nodes, graph);
                
            } else {
            	Pruning_calculateArrowsBackward(i, j, nodes, graph);
            	Pruning_calculateArrowsBackward(j, i, nodes, graph);
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

    private void Pruning_reevaluateBackward(Graph graph, List<Node> nodes, Arrow arrow) {
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

    private void Pruning_calculateArrowsBackward(int i, int j, List<Node> nodes, Graph graph) {
        if (i == j) {
            return;
        }

        Node _x = nodes.get(i);
        Node _y = nodes.get(j);

        if (!graph.isAdjacentTo(_x, _y)) {
            return;
        }

        if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
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

    /**
     * True iff the f cutoff should be used in the forward search.
     */
    public boolean isUseFCutoff() {
        return useFCutoff;
    }

    public void setUseFCutoff(boolean useFCutoff) {
        this.useFCutoff = useFCutoff;
    }

    /**
     * The P value for the f cutoff, if used.
     */
    public double getfCutoffP() {
        return fCutoffP;
    }

    public void setfCutoffP(double fCutoffP) {
        if (fCutoffP < 0.0 || fCutoffP > 1.0) {
            throw new IllegalArgumentException();
        }

        this.fCutoffP = fCutoffP;
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

    /** Do an actual insertion , (Definition 12 from Chickering, 2002).   
     *  **/
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

    /*
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
    */
     
    
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

    private void Pruning_rebuildPattern(Graph graph) {
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
            this.covariances = dataSet.getCovarianceMatrix();
        }

        this.sampleSize = dataSet.getNumRows();
    }

    private void setCovMatrix(ICovarianceMatrix covarianceMatrix) {
        this.covariances = covarianceMatrix.getMatrix();
        List<String> _varNames = covarianceMatrix.getVariableNames();

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = covarianceMatrix.getVariables();
        this.sampleSize = covarianceMatrix.getSampleSize();
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

    private static int getRowIndex(int dim[], int[] values) {
        int rowIndex = 0;
        for (int i = 0; i < dim.length; i++) {
            rowIndex *= dim[i];
            rowIndex += values[i];
        }
        return rowIndex;
    }

    //===========================SCORING METHODS===========================//

    @Override
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
                score += localSemScore(nextIndex, parentIndices);
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
                score1 = localSemScore(yIndex, parentIndices1);
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
                score2 = localSemScore(yIndex, parentIndices2);
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



    private int numCategories(int i) {
        return ((DiscreteVariable) dataSet().getVariable(i)).getNumCategories();
    }

    private RegressionDataset regression;

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in a simple SEM model.
     */
    private double localSemScore(int i, int[] parents) {

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        double variance = getCovMatrix().get(i, i);
        int n = sampleSize();
        double k = parents.length + 1;

//        if (regression == null) {
//            regression = new RegressionDataset(dataSet());
//        }
//
//        List<Node> variables = dataSet.getVariables();
//        Node target = variables.get(i);
//
//        List<Node> regressors = new ArrayList<Node>();
//
//        for (int parent : parents) {
//            regressors.add(variables.get(parent));
//        }
//
//        RegressionResult result = regression.regress(target, regressors);
//
//        double[] residuals = result.getResiduals().toArray();
//
//        double _variance = StatUtils.variance(residuals);

        if (parents.length > 0) {

            // Regress z onto i, yielding regression coefficients b.
            DoubleMatrix2D Czz = getCovMatrix().viewSelection(parents, parents);
            DoubleMatrix2D inverse = invert(Czz, parents);
            DoubleMatrix1D Cyz = getCovMatrix().viewColumn(i);
            Cyz = Cyz.viewSelection(parents);
            DoubleMatrix1D b = algebra().mult(inverse, Cyz);

//            System.out.println("B = " + MatrixUtils.toString(b.toArray()));

            variance -= algebra().mult(Cyz, b);
        }

        if (variance == 0) {
            StringBuilder b = localModelString(i, parents);
            this.logger.log("info", b.toString());
            this.logger.log("info", "Zero residual variance; returning negative infinity.");
            return Double.NEGATIVE_INFINITY;
        }

        double penalty = getPenaltyDiscount();

        // This is the full -BIC formula.
//        return -0.5 n * Math.log(variance) - n * Math.log(2. * Math.PI) - n
//                - penalty * k * Math.log(n);
//        return -.5 * n * (Math.log(variance) + Math.log(2 * Math.PI) + 1) - penalty * k * Math.log(n);

        // 2L - k ln n = 2 * BIC
//        return -n * (Math.log(variance) + Math.log(2 * Math.PI) + 1) - penalty * k * Math.log(n);

        // This is the formula with contant terms for fixed n removed.
        return -n * Math.log(variance) - penalty * k * Math.log(n);
    }

    private StringBuilder localModelString(int i, int[] parents) {
        StringBuilder b = new StringBuilder();
        b.append(("*** "));
        b.append(variables.get(i));

        if (parents.length == 0) {
            b.append(" with no parents");
        } else {
            b.append(" with parents ");

            for (int j = 0; j < parents.length; j++) {
                b.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    b.append(",");
                }
            }
        }
        return b;
    }

    private DoubleMatrix2D invert(DoubleMatrix2D czz, int[] parents) {
        DoubleMatrix2D inverse;
        try {
//            inverse = algebra().inverse(czz);
//                inverse = MatrixUtils.inverse(czz);
            inverse = MatrixUtils.ginverse(czz);
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
        return inverse;
    }

    private int sampleSize() {
        return this.sampleSize;
    }

    private List<Node> getVariables() {
        return variables;
    }

    private DoubleMatrix2D getCovMatrix() {
        return covariances;
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

    private double getSamplePrior() {
        return samplePrior;
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




