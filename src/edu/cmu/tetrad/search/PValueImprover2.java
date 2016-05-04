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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.ChoiceGenerator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Improves the P value of a SEM IM by adding, removing, or reversing single edges.
 *
 * @author Joseph Ramsey
 */

public final class PValueImprover2 {
    private DataSet dataSet;
    private Knowledge knowledge = new Knowledge();
    private Graph graph;
    private double alpha = 0.05;
    private double highPValueAlpha = 0.05;
    private final NumberFormat nf = new DecimalFormat("0.0#########");
    private Set<GraphWithPValue> significantModels = new HashSet<GraphWithPValue>();
    private Graph trueModel;
    private SemIm originalSemIm;
    private SemIm newSemIm;
    private Scorer scorer;

    public PValueImprover2(Graph graph, DataSet data) {
        if (graph == null) throw new NullPointerException("Graph not specified.");

        boolean allowArbitraryOrientations = true;
        boolean allowNewColliders = true;
        DagInPatternIterator iterator = new DagInPatternIterator(graph, getKnowledge(), allowArbitraryOrientations,
                allowNewColliders);
        graph = iterator.next();
        graph = SearchGraphUtils.patternForDag(graph);

        if (GraphUtils.containsBidirectedEdge(graph)) {
            throw new IllegalArgumentException("Contains bidirected edge.");
        }

        this.graph = graph;
        this.dataSet = data;
        this.dataSet = data;
        this.scorer = new DagScorer(dataSet);
    }

    public Graph search() {
        Graph bestGraph = SearchGraphUtils.patternForDag(new EdgeListGraph(getGraph()));
        Score score0 = scoreGraph(bestGraph);
        double bestScore = score0.getScore();
        this.originalSemIm = score0.getEstimatedSem();

        if (trueModel != null) {
            trueModel = GraphUtils.replaceNodes(trueModel, bestGraph.getNodes());
            trueModel = SearchGraphUtils.patternForDag(trueModel);
        }

        System.out.println("Initial Score = " + nf.format(bestScore));
        MeekRules meekRules = new MeekRules();
        meekRules.setKnowledge(getKnowledge());

        removeHighPValueEdges(bestGraph);
        increaseScoreLoop(bestGraph, meekRules, getAlpha());
        decreaseScoreLoop(bestGraph, meekRules, getAlpha());
        removeHighPValueEdges(bestGraph);

        Score score = scoreGraph(bestGraph);
        SemIm estSem = score.getEstimatedSem();

        this.newSemIm = estSem;

        return bestGraph;
    }

    private void increaseScoreLoop(Graph bestGraph, MeekRules meekRules, double alpha) {
        Move bestMove;
        double bestScore = scoreGraph(bestGraph).getScore();

        while (scoreGraph(bestGraph).getPValue() < alpha) {
            System.out.println("Trying to increase score above " + alpha);

            List<Move> moves = getMoves(bestGraph, true);

            bestMove = null;

            for (Move move : moves) {
//                if (move.getType() == Move.Type.SWAP) continue;

                graph = new EdgeListGraph(bestGraph);

                makeMove(getGraph(), move);

                SearchGraphUtils.basicPattern(getGraph());
                meekRules.orientImplied(getGraph());

                if (getKnowledge().isViolatedBy(getGraph())) {
                    continue;
                }

                if (SearchGraphUtils.dagFromPattern(getGraph()).existsDirectedCycle()) {
                    continue;
                }

                Graph dag = SearchGraphUtils.dagFromPattern(getGraph(), getKnowledge());

                if (dag == null) {
                    continue;
                }

                Score _score = scoreDag(dag);
                double score = _score.getScore();

//                if (score > Double.NEGATIVE_INFINITY) {
//                    if (trueModel == null) {
//                        System.out.println(move + " " + score);
//                    } else {
//                        Edge edge = trueModel.getEdge(move.getEdge().getNode1(), move.getEdge().getNode2());
//                        if (edge != null) {
//                            System.out.println(move + " " + score + " " +
//                                    edge);
//                        }
//                    }
//                }

                if (score >= bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }

            if (bestMove == null) {
                System.out.println("Nothing improved it.");
                break;
            } else {
                makeMove(bestGraph, bestMove);
                SearchGraphUtils.basicPattern(bestGraph);
                meekRules.orientImplied(bestGraph);

                graph = new EdgeListGraph(bestGraph);

                System.out.println(bestMove);
                System.out.println("Score = " + bestScore);
                System.out.println("P value = " + nf.format(scoreGraph(bestGraph).getPValue()));

                TetradLogger.getInstance().log("details", bestMove.toString());
                TetradLogger.getInstance().log("details", "Score = " + bestScore);
                TetradLogger.getInstance().log("details", "P value = " + nf.format(scoreGraph(bestGraph).getPValue()));
            }
        }
    }

    private void decreaseScoreLoop(Graph bestGraph, MeekRules meekRules, double alpha) {
        Move bestMove;
        double bestScore;
        double overallScore = scoreGraph(getGraph()).getScore();

        while (true) {
            System.out.println("Trying to decrease score to just above " + alpha);

            List<Move> moves = getMoves(bestGraph, false);
            bestMove = null;
            bestScore = Double.NEGATIVE_INFINITY;
            double bestPValue = Double.NEGATIVE_INFINITY;

            for (Move move : moves) {
                graph = new EdgeListGraph(bestGraph);
                makeMove(getGraph(), move);

                SearchGraphUtils.basicPattern(getGraph());
                meekRules.orientImplied(getGraph());

                if (getKnowledge().isViolatedBy(getGraph())) {
                    continue;
                }

                if (SearchGraphUtils.dagFromPattern(getGraph()).existsDirectedCycle()) {
                    continue;
                }

                Graph dag = SearchGraphUtils.dagFromPattern(getGraph(), getKnowledge());

                if (dag == null) {
                    continue;
                }

                Score _score = scoreDag(dag);
                double score = _score.getScore();

//                if (score > Double.NEGATIVE_INFINITY) {
//                    System.out.println(move + " " + _score.getPValue());
//
////                    if (trueModel != null) {
////                        Edge trueEdge = trueModel.getEdge(move.getFirstEdge().getNode1(), move.getFirstEdge().getNode2());
////
////                        if (move.getType() == Move.Type.ADD && trueEdge != null) {
////                            System.out.println(move + " " + score + " " +  _score.getPValue() + " " + overallScore + " " +
////                                    trueEdge);
////                        }
////                        if (move.getType() == Move.Type.REMOVE && trueEdge == null) {
////                            System.out.println(move + " " + score + " " +  _score.getPValue() + " " + overallScore + " " +
////                                    trueEdge);
////                        }
//////                        else if (move.getType() == Move.Type.REDIRECT) {
//////                            System.out.println(move + " " + score + " " + _score.getPValue() + " " + overallScore + " " +
//////                                    graph.getEdge(move.getEdge().getNode1(), move.getEdge().getNode2()) + " " +
//////                                    trueModel.getEdge(move.getEdge().getNode1(), move.getEdge().getNode2()));
//////                        }
////                    }
//                }

                if (score > bestScore && _score.getPValue() > alpha && score < overallScore) {
                    bestMove = move;
                    bestScore = score;
                    bestPValue = _score.getPValue();
                }
            }

            if (bestMove == null/* || bestPValue < alpha*/) {
                System.out.println("Nothing improved it.");
                break;
            } else {
                makeMove(bestGraph, bestMove);
                SearchGraphUtils.basicPattern(bestGraph);
                meekRules.orientImplied(bestGraph);
                overallScore = bestScore;

                graph = new EdgeListGraph(bestGraph);

                System.out.println(bestMove);
                System.out.println("Score = " + bestScore);
                System.out.println("P value = " + bestPValue);

                TetradLogger.getInstance().log("details", bestMove.toString());
                TetradLogger.getInstance().log("details", "Score = " + bestScore);
                TetradLogger.getInstance().log("details", "P value = " + bestPValue);
            }
        }
    }

    public PValueImprover2() {
        super();
    }

    private void removeHighPValueEdges(Graph bestGraph) {
        boolean changed = true;

        while (changed) {
            changed = false;
            Score score = scoreGraph(bestGraph);
            SemIm estSem = score.getEstimatedSem();

            for (Parameter param : estSem.getSemPm().getParameters()) {
                if (param.getType() != ParamType.COEF) {
                    continue;
                }

                double p = estSem.getPValue(param, 10000);
                Edge edge = bestGraph.getEdge(param.getNodeA(), param.getNodeB());

                if (p > getHighPValueAlpha()) {
                    System.out.println("Removing edge " + edge + " because it has p = " + p);
                    TetradLogger.getInstance().log("details", "Removing edge " + edge + " because it has p = " + p);
                    bestGraph.removeEdge(edge);
                    changed = true;
                }
            }
        }
    }

    private Edge makeMove(Graph graph, Move move) {
        Edge firstEdge = move.getFirstEdge();

        if (firstEdge != null && move.getType() == Move.Type.ADD) {
            graph.removeEdge(firstEdge.getNode1(), firstEdge.getNode2());
            graph.addEdge(firstEdge);
        } else if (firstEdge != null && move.getType() == Move.Type.REMOVE) {
            graph.removeEdge(firstEdge);
        } else if (firstEdge != null && move.getType() == Move.Type.REDIRECT) {
            graph.removeEdge(graph.getEdge(firstEdge.getNode1(), firstEdge.getNode2()));
            graph.addEdge(firstEdge);
        } else if (firstEdge != null && move.getSecondEdge() != null && move.getType() == Move.Type.COLLIDER) {
            Edge secondEdge = move.getSecondEdge();
            graph.removeEdge(graph.getEdge(firstEdge.getNode1(), firstEdge.getNode2()));
            graph.addEdge(firstEdge);
            graph.removeEdge(graph.getEdge(secondEdge.getNode1(), secondEdge.getNode2()));
            graph.addEdge(secondEdge);
        } else if (firstEdge != null && move.getSecondEdge() != null && move.getType() == Move.Type.COLLIDER_TRIANGLE) {
            Edge secondEdge = move.getSecondEdge();
            graph.removeEdge(firstEdge);
            Edge secondEdgeStar = graph.getEdge(secondEdge.getNode1(), secondEdge.getNode2());

            if (secondEdgeStar != null) {
                graph.removeEdge(secondEdgeStar);
            }

            graph.addEdge(secondEdge);
        }


        return firstEdge;
    }

    private List<Move> getMoves(Graph graph, boolean up) {
        List<Move> moves = new ArrayList<Move>();

        // Add moves:
        List<Node> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) {
                    continue;
                }

                if (graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    continue;
                }

                if (getKnowledge().edgeForbidden(nodes.get(i).getName(), nodes.get(j).getName())) {
                    continue;
                }

                Edge edge = Edges.directedEdge(nodes.get(i), nodes.get(j));

                moves.add(new Move(edge, Move.Type.ADD));
            }
        }

        // Remove moves:
        for (Edge edge : graph.getEdges()) {
            moves.add(new Move(edge, Move.Type.REMOVE));
        }

        // Reverse moves:
        for (Edge edge : graph.getEdges()) {
            if (knowledge.edgeForbidden(edge.getNode2().getName(), edge.getNode1().getName())) {
                continue;
            }

            if (Edges.isDirectedEdge(edge)) {
                moves.add(new Move(Edges.directedEdge(edge.getNode2(), edge.getNode1()), Move.Type.REDIRECT));
                moves.add(new Move(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()), Move.Type.REDIRECT));
            } else {
                moves.add(new Move(Edges.directedEdge(edge.getNode1(), edge.getNode2()), Move.Type.REDIRECT));
                moves.add(new Move(Edges.directedEdge(edge.getNode2(), edge.getNode1()), Move.Type.REDIRECT));
            }
        }

//         Make collider moves:
        for (Node b : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(b);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> set = GraphUtils.asList(choice, adj);

                Node a = set.get(0);
                Node c = set.get(1);

                if (!getGraph().isDefCollider(a, b, c)) {
                    Edge edge1 = Edges.directedEdge(a, b);
                    Edge edge2 = Edges.directedEdge(c, b);

                    if (getKnowledge().edgeForbidden(edge1.getNode1().getName(), edge1.getNode2().getName())) {
                        continue;
                    }

                    if (getKnowledge().edgeForbidden(edge2.getNode1().getName(), edge2.getNode2().getName())) {
                        continue;
                    }

                    moves.add(new Move(edge1, edge2, Move.Type.COLLIDER));

                    Edge edge3 = Edges.directedEdge(a, c);

                    moves.add(new Move(edge1, edge3, Move.Type.COLLIDER_TRIANGLE));


                    Edge edge4 = Edges.directedEdge(c, a);

                    moves.add(new Move(edge2, edge4, Move.Type.COLLIDER_TRIANGLE));
                } else if (up && graph.getEdges(a, b) != null && graph.getEdge(b, c) != null &&
                        graph.getEdge(a, b).pointsTowards(b) && graph.getEdge(b, c).pointsTowards(c)) {
                    moves.add(new Move(Edges.directedEdge(a, b), Edges.directedEdge(b, c), Move.Type.COLLIDER_TRIANGLE));
                } else if (!up && graph.getEdges(b, a) != null && graph.getEdge(a, c) != null &&
                        graph.getEdge(b, a).pointsTowards(a) && graph.getEdge(a, c).pointsTowards(c)) {
                    moves.add(new Move(Edges.directedEdge(b, a), Edges.directedEdge(a, c), Move.Type.COLLIDER_TRIANGLE));
                } else {
                    Edge edge1 = Edges.undirectedEdge(a, b);
                    Edge edge2 = Edges.undirectedEdge(c, b);

                    moves.add(new Move(edge1, edge2, Move.Type.COLLIDER));
                }
            }
        }

        return moves;
    }

    public Graph getGraph() {
        return graph;
    }

    public SemIm getOriginalSemIm() {
        return originalSemIm;
    }

    public SemIm getNewSemIm() {
        return newSemIm;
    }

    public double getHighPValueAlpha() {
        return highPValueAlpha;
    }

    public void setHighPValueAlpha(double highPValueAlpha) {
        this.highPValueAlpha = highPValueAlpha;
    }

    private static class Move {
        public enum Type {
            ADD, REMOVE, REDIRECT, COLLIDER, COLLIDER_TRIANGLE;
        }

        private Edge edge;
        private Edge secondEdge;
        private Type type;

        public Move(Edge edge, Type type) {
            this.edge = edge;
            this.type = type;
        }

        public Move(Edge edge, Edge secondEdge, Type type) {
            this.edge = edge;
            this.secondEdge = secondEdge;
            this.type = type;
        }

        public Edge getFirstEdge() {
            return this.edge;
        }

        public Edge getSecondEdge() {
            return secondEdge;
        }

        public Type getType() {
            return this.type;
        }

        @Override
		public String toString() {
            String s = (secondEdge != null) ? (secondEdge + ", ") : "";
            return "<" + edge + ", " + s + type + ">";

        }
    }


    private void saveModelIfSignificant(Graph graph) {
        double pValue = scoreGraph(graph).getPValue();

        if (pValue > getAlpha()) {
            getSignificantModels().add(new GraphWithPValue(graph, pValue));
        }
    }

    public static class GraphWithPValue {
        private Graph graph;
        private double pValue;

        public GraphWithPValue(Graph graph, double pValue) {
            this.graph = graph;
            this.pValue = pValue;
        }

        public Graph getGraph() {
            return graph;
        }

        public double getPValue() {
            return pValue;
        }

        @Override
		public int hashCode() {
            return 17 * graph.hashCode();
        }

        @Override
		public boolean equals(Object o) {
            if (o == null) return false;
            GraphWithPValue p = (GraphWithPValue) o;
            return (p.graph.equals(graph));
        }
    }

    public Score scoreGraph(Graph graph) {
        Graph dag = SearchGraphUtils.dagFromPattern(graph, getKnowledge());

        if (dag == null) {
            return Score.negativeInfinity();
        }

//        SemPm semPm = new SemPm(dag);
//        SemEstimator semEstimator = new SemEstimator(dataSet, semPm, new SemOptimizerEm());
//        semEstimator.estimate();
//        SemIm estimatedSem = semEstimator.getEstimatedSem();
//        return new Score(estimatedSem);

        scorer.score(dag);
        return new Score(scorer);


    }

    public Score scoreDag(Graph dag) {
//        SemPm semPm = new SemPm(dag);
//        SemEstimator semEstimator = new SemEstimator(dataSet, semPm, new SemOptimizerEm());
//        semEstimator.estimate();
//        SemIm estimatedSem = semEstimator.getEstimatedSem();
//        return new Score(estimatedSem);

        scorer.score(dag);
        return new Score(scorer);
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public Graph getTrueModel() {
        return trueModel;
    }

    public void setTrueModel(Graph trueModel) {
        this.trueModel = trueModel;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public Set<GraphWithPValue> getSignificantModels() {
        return significantModels;
    }

    public static class Score {
        private Scorer scorer;
        private double pValue;
        private double fml;
        private double chisq;

        public Score(Scorer scorer) {
            this.scorer = scorer;
            this.pValue = scorer.getPValue();
            this.fml = scorer.getFml();
            this.chisq = scorer.getChiSquare();
        }

        private Score() {
            this.scorer = null;
            this.pValue = 0.0;
            this.fml = Double.POSITIVE_INFINITY;
            this.chisq = 0.0;
        }

        public SemIm getEstimatedSem() {
            return scorer.getEstSem();
        }

        public double getPValue() {
            return pValue;
        }

        public double getScore() {
//            double fml = estimatedSem.getFml();
//            int freeParams = estimatedSem.getNumFreeParams();
//            int sampleSize = estimatedSem.getSampleSize();
//            return -(sampleSize - 1) * fml - (freeParams * Math.log(sampleSize));
//            return -getChisq();

//            if (getMaxEdgeP() > 0.05) {
//                return Double.NEGATIVE_INFINITY;
//            }

            return -fml;
        }

        public double getFml() {
            return fml;
        }

//        public double getChisq() {
//            return chisq;
//        }

//        public double getMaxEdgeP() {
//            double maxP = Double.NEGATIVE_INFINITY;
//
//            for (Parameter param : estimatedSem.getSemPm().getParameters()) {
//                if (param.getType() != ParamType.COEF) {
//                    continue;
//                }
//                double p = this.estimatedSem.getPValue(param, 10000);
//                if (p > maxP) maxP = p;
//            }
//
//            return maxP;
//        }

        public static Score negativeInfinity() {
            return new Score();
        }
    }

    /**
     * This method straightforwardly applies the standard definition of the numerical estimates of the second order
     * partial derivatives.  See for example Section 5.7 of Numerical Recipes in C.
     */
    public double secondPartialDerivative(FittingFunction f, int i, int j,
                                          double[] p, double delt) {
        double[] arg = new double[p.length];
        System.arraycopy(p, 0, arg, 0, p.length);

        arg[i] += delt;
        arg[j] += delt;
        double ff1 = f.evaluate(arg);

        arg[j] -= 2 * delt;
        double ff2 = f.evaluate(arg);

        arg[i] -= 2 * delt;
        arg[j] += 2 * delt;
        double ff3 = f.evaluate(arg);

        arg[j] -= 2 * delt;
        double ff4 = f.evaluate(arg);

        double fsSum = ff1 - ff2 - ff3 + ff4;

        return fsSum / (4.0 * delt * delt);
    }

    /**
     * Evaluates a fitting function for an array of parameters.
     *
     * @author Joseph Ramsey
     */
    static interface FittingFunction {

        /**
         * Returns the value of the function for the given array of parameter values.
         */
        double evaluate(double[] argument);

        /**
         * Returns the number of parameters.
         */
        int getNumParameters();
    }

    /**
     * Wraps a Sem for purposes of calculating its fitting function for given parameter values.
     *
     * @author Joseph Ramsey
     */
    static class SemFittingFunction implements FittingFunction {

        /**
         * The wrapped Sem.
         */
        private final SemIm sem;

        /**
         * Constructs a new PalFittingFunction for the given Sem.
         */
        public SemFittingFunction(SemIm sem) {
            this.sem = sem;
        }

        /**
         * Computes the maximum likelihood function value for the given parameters values as given by the optimizer.
         * These values are mapped to parameter values.
         */
        @Override
		public double evaluate(double[] parameters) {
            sem.setFreeParamValues(parameters);

            // This needs to be FML-- see Bollen p. 109.
            return sem.getFml();
        }

        /**
         * Returns the number of arguments. Required by the MultivariateFunction interface.
         */
        @Override
		public int getNumParameters() {
            return this.sem.getNumFreeParams();
        }
    }
}
