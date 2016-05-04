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
import edu.cmu.tetrad.data.ICovarianceMatrix;
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

public final class PValueImprover6 {
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
    private boolean checkingCycles = false;

    public PValueImprover6(Graph graph, DataSet data) {
//        if (graph == null) throw new NullPointerException("Graph not specified.");

        if (graph == null) graph = new EdgeListGraph(data.getVariables());

//        if (isCheckingCycles() && graph.existsDirectedCycle()) {
//            throw new IllegalArgumentException("Cyclic pattern: " + graph);
//        }

        boolean allowArbitraryOrientations = false;
        boolean allowNewColliders = true;
        DagInPatternIterator iterator = new DagInPatternIterator(graph, getKnowledge(), allowArbitraryOrientations,
                allowNewColliders);
        Graph graph2 = iterator.next();

//        graph = SearchGraphUtils.patternForDag(graph);

        if (graph2 == null) {
            DagIterator iterator2 = new DagIterator(graph);
            graph = iterator2.next();


//            if (isCheckingCycles() && graph != null && graph.existsDirectedCycle()) {
//                throw new IllegalArgumentException("Cyclic DAG");
//            }

        } else {
//            if (isCheckingCycles() && graph.existsDirectedCycle()) {
//                throw new IllegalArgumentException("Cyclic DAG");
//            }

            graph = graph2;
        }

        if (GraphUtils.containsBidirectedEdge(graph)) {
            throw new IllegalArgumentException("Contains bidirected edge.");
        }

        this.graph = graph;
        this.dataSet = data;
        this.scorer = new DagScorer(dataSet);
    }

    public Graph search() {
//        Graph bestGraph = SearchGraphUtils.patternForDag(new EdgeListGraph(getGraph()));
        Graph bestGraph = SearchGraphUtils.dagFromPattern(getGraph());
        Score score0 = scoreGraph(bestGraph);
        double bestScore = score0.getScore();
        this.originalSemIm = score0.getEstimatedSem();

        System.out.println("Graph from search = " + bestGraph);

        if (trueModel != null) {
            trueModel = GraphUtils.replaceNodes(trueModel, bestGraph.getNodes());
            trueModel = SearchGraphUtils.patternForDag(trueModel);
        }

        System.out.println("Initial Score = " + nf.format(bestScore));
        MeekRules meekRules = new MeekRules();
        meekRules.setKnowledge(getKnowledge());

//        removeHighPValueEdges(bestGraph);
        increaseScoreLoop(bestGraph, .8, true);
//        removeHighPValueEdges(bestGraph);
//        decreaseScoreLoop(bestGraph, getAlpha(), true);
//        removeHighPValueEdges(bestGraph);

        Score score = scoreGraph(bestGraph);
        SemIm estSem = score.getEstimatedSem();

        this.newSemIm = estSem;

        return bestGraph;
    }

    private void increaseScoreLoop(Graph bestGraph, double alpha, boolean verbose) {
        Move bestMove;
        Score score1 = scoreGraph(bestGraph);
        double bestScore = score1.getScore();
        double bestPValue = score1.getPValue();
        int bestDof = score1.getDof();

        System.out.println("Original p value = " + bestPValue);

//        while (scoreGraph(bestGraph).getPValue() < alpha) {
        if (verbose) {
            System.out.println("Trying to increase score above " + alpha);
        }

        List<Move> moves = new ArrayList<Move>();
        moves.addAll(getAddMoves(bestGraph));
//            moves.addAll(getRedirectMoves(bestGraph));
//            moves.addAll(getSwapMoves(bestGraph));
//            moves.addAll(getAddColliderMoves(bestGraph));
//            moves.addAll(getRemoveColliderMoves(bestGraph));

        bestMove = null;

        for (Move move : moves) {
            graph = new EdgeListGraph(bestGraph);

            makeMove(getGraph(), move, false);

            if (getKnowledge().isViolatedBy(getGraph())) {
                continue;
            }

            if (isCheckingCycles() && graph.existsDirectedCycle()) {
                continue;
            }

            Score _score = scoreDag(getGraph());
            double score = _score.getScore();
            double pValue = _score.getPValue();
            int dof = _score.getDof();

            System.out.println(move + " " + pValue);

//                System.out.println(move + " " + pValue);

            if (pValue == 0 && score > bestScore) {
                bestScore = score;
                bestPValue = pValue;
                bestMove = move;
            } else if (pValue > bestPValue) {
                bestScore = score;
                bestPValue = pValue;
                bestMove = move;
            }

//                if (score > bestScore) {
//                    bestScore = score;
//                    bestPValue = pValue;
//                    bestMove = move;
//                }
        }

//            if (bestMove == null) {
//                if (verbose) {
//                    System.out.println("Nothing improved it.");
//                }
//                break;
//            } else {
//                makeMove(bestGraph, bestMove, true);
//                graph = new EdgeListGraph(bestGraph);
//
//                if (verbose) {
//                    System.out.println(bestMove);
//                    System.out.println("Score = " + bestScore);
//                    System.out.println("P value = " + nf.format(scoreGraph(bestGraph).getPValue()));
//                }
//
//                TetradLogger.getInstance().log("details", bestMove.toString());
//                TetradLogger.getInstance().log("details", "Score = " + bestScore);
//                TetradLogger.getInstance().log("details", "P value = " + nf.format(scoreGraph(bestGraph).getPValue()));
//            }
//        }
    }

    private void increaseScoreLoopSameDf(Graph bestGraph, double alpha, boolean verbose) {
        Move bestMove;
        Score score1 = scoreGraph(bestGraph);
        double bestScore = score1.getScore();
        double bestPValue = score1.getPValue();
        int bestDof = score1.getDof();

        while (scoreGraph(bestGraph).getPValue() < alpha) {
            if (verbose) {
                System.out.println("Trying to increase score above " + alpha + " same df");
            }

            List<Move> moves = new ArrayList<Move>();
//            moves.addAll(getAddMoves(bestGraph));
            moves.addAll(getRedirectMoves(bestGraph));
            moves.addAll(getSwapMoves(bestGraph));
//            moves.addAll(getAddColliderMoves(bestGraph));
//            moves.addAll(getRemoveColliderMoves(bestGraph));

            bestMove = null;

            for (Move move : moves) {
                graph = new EdgeListGraph(bestGraph);

                makeMove(getGraph(), move, false);

                if (getKnowledge().isViolatedBy(getGraph())) {
                    continue;
                }

                if (isCheckingCycles() && graph.existsDirectedCycle()) {
                    continue;
                }

                Score _score = scoreDag(getGraph());
                double score = _score.getScore();
                double pValue = _score.getPValue();
                int dof = _score.getDof();

//                System.out.println(move + " " + pValue);

//                if (pValue == 0 && score > bestScore) {
//                    bestScore = score;
//                    bestPValue = pValue;
//                    bestMove = move;
//                }
//                else if (pValue > bestPValue) {
//                    bestScore = score;
//                    bestPValue = pValue;
//                    bestMove = move;
//                }

                if (score > bestScore) {
                    bestScore = score;
                    bestPValue = pValue;
                    bestMove = move;
                }
            }

            if (bestMove == null) {
                if (verbose) {
                    System.out.println("Nothing improved it.");
                }
                break;
            } else {
                makeMove(bestGraph, bestMove, true);
                graph = new EdgeListGraph(bestGraph);

                if (verbose) {
                    System.out.println(bestMove);
                    System.out.println("Score = " + bestScore);
                    System.out.println("P value = " + nf.format(scoreGraph(bestGraph).getPValue()));
                }

                TetradLogger.getInstance().log("details", bestMove.toString());
                TetradLogger.getInstance().log("details", "Score = " + bestScore);
                TetradLogger.getInstance().log("details", "P value = " + nf.format(scoreGraph(bestGraph).getPValue()));
            }
        }
    }

    private void decreaseScoreLoop(Graph bestGraph, double alpha, boolean verbose) {
        Move bestMove;
        double bestScore;
        Score score1 = scoreGraph(getGraph());
        double overallPValue = score1.getPValue();
        double overallScore = score1.getScore();
        int overallDof = score1.getDof();

//        for (Edge edge : bestGraph.getEdges()) {
//            Edge random;
//
//            if (RandomUtil.getInstance().nextDouble() > 0.5) {
//                random = new Edge(edge.getNode1(), edge.getNode2(), Endpoint.TAIL, Endpoint.ARROW);
//            }
//            else {
//                random = new Edge(edge.getNode2(), edge.getNode1(), Endpoint.TAIL, Endpoint.ARROW);
//            }
//
//            graph.removeEdge(graph.getEdge(edge.getNode1(), edge.getNode2()));
//            graph.addEdge(random);
//        }

        while (true) {
            if (verbose) {
                System.out.println("Trying to decrease score to just above " + alpha);
            }

            List<Move> moves = new ArrayList<Move>();
            moves.addAll(getRemoveMoves(bestGraph));
            moves.addAll(getRedirectMoves(bestGraph));
//            moves.addAll(getDoubleRemoveMoves(bestGraph));
//            moves.addAll(getAddColliderMoves(bestGraph));
//            moves.addAll(getRemoveColliderMoves(bestGraph));
//            moves.addAll(getSwapMoves(bestGraph));
//            moves.addAll(getRemoveTriangleMoves(bestGraph));

            bestMove = null;
            bestScore = Double.NEGATIVE_INFINITY;
            double bestPValue = Double.NEGATIVE_INFINITY;
            double bestPValueRemove = Double.NEGATIVE_INFINITY;
            int bestDof = -1;
            boolean jumpedDof = false;

            for (Move move : moves) {
                graph = new EdgeListGraph(bestGraph);
                makeMove(getGraph(), move, false);

                if (getKnowledge().isViolatedBy(getGraph())) {
                    continue;
                }

                if (isCheckingCycles() && getGraph().existsDirectedCycle()) {
                    continue;
                }

                Score _score = scoreDag(getGraph());
                double score = _score.getScore();
                double pValue = _score.getPValue();
                int dof = _score.getDof();

//                if (move.getType() == Move.Type.REMOVE && pValue > bestPValueRemove) {
//                    System.out.println(move + " " + pValue);
//                }

                if (dof > overallDof && pValue >= alpha && pValue > bestPValueRemove) {
                    bestMove = move;
                    bestScore = score;
                    bestPValue = _score.getPValue();
                    bestPValueRemove = _score.getPValue();
                    bestDof = _score.getDof();
                } else if (bestPValueRemove != Double.NEGATIVE_INFINITY &&
                        pValue > bestPValue && pValue >= alpha && pValue < overallPValue) {
                    bestMove = move;
                    bestScore = score;
                    bestPValue = _score.getPValue();
                    bestDof = _score.getDof();
                }

            }

            if (bestMove == null) {
                if (verbose) {
                    System.out.println("Nothing improved it.");
                }
                break;
            } else {
                makeMove(bestGraph, bestMove, false);
                overallPValue = bestPValue;
                overallScore = bestScore;
                overallDof = bestDof;

                graph = new EdgeListGraph(bestGraph);

                if (verbose) {
                    System.out.println(bestMove);
                    System.out.println("Score = " + bestScore);
                    System.out.println("P value = " + bestPValue);
                    System.out.println("DOF = " + bestDof);
                }

                TetradLogger.getInstance().log("details", bestMove.toString());
                TetradLogger.getInstance().log("details", "Score = " + bestScore);
                TetradLogger.getInstance().log("details", "P value = " + bestPValue);
                TetradLogger.getInstance().log("details", "DOF = " + bestDof);
            }
        }
    }

    public PValueImprover6() {
        super();
    }

    public Graph removeHighPValueEdges(Graph bestGraph) {
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

//                System.out.println("P value for edge " + edge + " = " + p);

                if (p > getHighPValueAlpha()) {
                    System.out.println("Removing edge " + edge + " because it has p = " + p);
                    TetradLogger.getInstance().log("details", "Removing edge " + edge + " because it has p = " + p);
                    bestGraph.removeEdge(edge);
                    changed = true;
                }
            }
        }

        return bestGraph;
    }

    private Edge makeMove(Graph graph, Move move, boolean finalMove) {
        Edge firstEdge = move.getFirstEdge();
        Edge secondEdge = move.getSecondEdge();

        if (firstEdge != null && move.getType() == Move.Type.ADD) {
            graph.removeEdge(firstEdge.getNode1(), firstEdge.getNode2());
            graph.addEdge(firstEdge);

            if (finalMove) {
                Node node1 = firstEdge.getNode1();
                Node node2 = firstEdge.getNode2();

                for (Node node3 : graph.getNodes()) {
                    if (graph.isAdjacentTo(node1, node3) && graph.isAdjacentTo(node2, node3)) {
                        System.out.println("TRIANGLE completed:");
                        System.out.println("\t" + graph.getEdge(node1, node3));
                        System.out.println("\t" + graph.getEdge(node2, node3));
                        System.out.println("\t" + graph.getEdge(node1, node2) + " added");
                    }
                }
            }


        } else if (firstEdge != null && move.getType() == Move.Type.REMOVE) {
            graph.removeEdge(firstEdge);
        } else if (firstEdge != null && move.getType() == Move.Type.DOUBLE_REMOVE) {
            graph.removeEdge(firstEdge);
            graph.removeEdge(secondEdge);
        } else if (firstEdge != null && move.getType() == Move.Type.REDIRECT) {
            graph.removeEdge(graph.getEdge(firstEdge.getNode1(), firstEdge.getNode2()));
            graph.addEdge(firstEdge);
        } else if (firstEdge != null && secondEdge != null && move.getType() == Move.Type.ADD_COLLIDER) {
            Edge existingEdge1 = graph.getEdge(firstEdge.getNode1(), firstEdge.getNode2());
            Edge existingEdge2 = graph.getEdge(secondEdge.getNode1(), secondEdge.getNode2());

            if (existingEdge1 != null) {
                graph.removeEdge(existingEdge1);
            }

            if (existingEdge2 != null) {
                graph.removeEdge(existingEdge2);
            }

            graph.addEdge(firstEdge);
            graph.addEdge(secondEdge);
        } else if (firstEdge != null && secondEdge != null && move.getType() == Move.Type.REMOVE_COLLIDER) {
            graph.removeEdge(firstEdge);
            graph.removeEdge(secondEdge);
        } else if (firstEdge != null && secondEdge != null && move.getType() == Move.Type.SWAP) {
            graph.removeEdge(firstEdge);
            Edge secondEdgeStar = graph.getEdge(secondEdge.getNode1(), secondEdge.getNode2());

            if (secondEdgeStar != null) {
                graph.removeEdge(secondEdgeStar);
            }

            graph.addEdge(secondEdge);
        }

        return firstEdge;
    }

    private List<Move> getAddMoves(Graph graph) {
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

                if (!graph.isAncestorOf(nodes.get(j), nodes.get(i))) {
                    Edge edge = Edges.directedEdge(nodes.get(i), nodes.get(j));
                    moves.add(new Move(edge, Move.Type.ADD));
                }
            }
        }

        return moves;
    }

    private List<Move> getRemoveMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();

        // Remove moves:
        for (Edge edge : graph.getEdges()) {
            moves.add(new Move(edge, Move.Type.REMOVE));
        }

        return moves;
    }

    private List<Move> getRedirectMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();

        // Reverse moves:
        for (Edge edge : graph.getEdges()) {
            if (knowledge.edgeForbidden(edge.getNode2().getName(), edge.getNode1().getName())) {
                continue;
            }

            moves.add(new Move(Edges.directedEdge(edge.getNode2(), edge.getNode1()), Move.Type.REDIRECT));

//            if (Edges.isDirectedEdge(edge)) {
////                if (graph.isAncestorOf(edge.getNode1(), edge.getNode2())) {
////                }
//            } else {
////                 if (graph.isAncestorOf(edge.getNode2(), edge.getNode1())) {
//                     moves.add(new Move(Edges.directedEdge(edge.getNode1(), edge.getNode2()), Move.Type.REDIRECT));
////                }
////                if (graph.isAncestorOf(edge.getNode1(), edge.getNode2())) {
//                    moves.add(new Move(Edges.directedEdge(edge.getNode2(), edge.getNode1()), Move.Type.REDIRECT));
////                }
//            }
        }

        return moves;
    }

    private List<Move> getAddColliderMoves(Graph graph) {
//         Make collider moves:

        List<Move> moves = new ArrayList<Move>();

        for (Node b : graph.getNodes()) {
            if (graph.getAdjacentNodes(b).isEmpty()) {
                List<Node> nodes = graph.getAdjacentNodes(b);

                if (nodes.size() >= 2) {
                    ChoiceGenerator gen = new ChoiceGenerator(nodes.size(), 2);
                    int[] choice;

                    while ((choice = gen.next()) != null) {
                        List<Node> _nodes = GraphUtils.asList(choice, nodes);
                        Node a = _nodes.get(0);
                        Node c = _nodes.get(1);

                        if (a == b || c == b) continue;

                        Edge edge1 = Edges.directedEdge(a, b);
                        Edge edge2 = Edges.directedEdge(c, b);

                        if (getKnowledge().edgeForbidden(edge1.getNode1().getName(), edge1.getNode2().getName())) {
                            continue;
                        }

                        if (getKnowledge().edgeForbidden(edge2.getNode1().getName(), edge2.getNode2().getName())) {
                            continue;
                        }

                        moves.add(new Move(edge1, edge2, Move.Type.ADD_COLLIDER));
                    }
                }
            }
        }

        return moves;
    }

    private List<Move> getSwapMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();

        for (Node b : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(b);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> set = GraphUtils.asList(choice, adj);

                Node a = set.get(0);
                Node c = set.get(1);

                if (graph.getEdge(a, b) != null && graph.getEdge(b, c) != null &&
                        graph.getEdge(a, b).pointsTowards(b) && graph.getEdge(b, c).pointsTowards(c)) {
                    moves.add(new Move(Edges.directedEdge(a, b), Edges.directedEdge(b, c), Move.Type.SWAP));
                } else if (graph.getEdge(b, a) != null && graph.getEdge(a, c) != null &&
                        graph.getEdge(b, a).pointsTowards(a) && graph.getEdge(a, c).pointsTowards(c)) {
                    moves.add(new Move(Edges.directedEdge(b, a), Edges.directedEdge(a, c), Move.Type.SWAP));
                }
            }
        }

        return moves;
    }

    private List<Move> getRemoveTriangleMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();

        for (Node b : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(b);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> set = GraphUtils.asList(choice, adj);

                Node a = set.get(0);
                Node c = set.get(1);

                Edge edge1 = graph.getEdge(a, b);
                Edge edge2 = graph.getEdge(b, c);
                Edge edge3 = graph.getEdge(a, c);

                if (edge1 != null && edge2 != null && edge3 != null &&
                        edge1.pointsTowards(a) && edge3.pointsTowards(c) &&
                        edge2.pointsTowards(c)) {
                    moves.add(new Move(Edges.directedEdge(b, c), Edges.directedEdge(c, a), Move.Type.SWAP));
                } else if (edge1 != null && edge2 != null && edge3 != null &&
                        edge3.pointsTowards(a) && edge1.pointsTowards(b) &&
                        edge2.pointsTowards(b)) {
                    moves.add(new Move(Edges.directedEdge(b, c), Edges.directedEdge(b, a), Move.Type.SWAP));
                }
            }
        }

        return moves;
    }

    private List<Move> getRemoveColliderMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();

        for (Node b : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(b);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> set = GraphUtils.asList(choice, adj);

                Node a = set.get(0);
                Node c = set.get(1);

                if (getGraph().isDefCollider(a, b, c)) {
                    Edge edge1 = Edges.directedEdge(a, b);
                    Edge edge2 = Edges.directedEdge(c, b);

                    moves.add(new Move(edge1, edge2, Move.Type.REMOVE_COLLIDER));
                }
            }
        }

        return moves;
    }

    private List<Move> getDoubleRemoveMoves(Graph graph) {
        List<Move> moves = new ArrayList<Move>();
        List<Edge> edges = graph.getEdges();

        // Remove moves:
        for (int i = 0; i < edges.size(); i++) {
            for (int j = i + 1; j < edges.size(); j++) {
                moves.add(new Move(edges.get(i), edges.get(j), Move.Type.DOUBLE_REMOVE));
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

    public boolean isCheckingCycles() {
        return checkingCycles;
    }

    public void setCheckingCycles(boolean checkingCycles) {
        this.checkingCycles = checkingCycles;
    }

    private static class Move {
        public enum Type {
            ADD, REMOVE, REDIRECT, ADD_COLLIDER, REMOVE_COLLIDER, SWAP, DOUBLE_REMOVE;
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
        Graph dag = graph; //SearchGraphUtils.dagFromPattern(graph, getKnowledge());

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
        private double bic;
        private double aic;
        private int dof;
        private ICovarianceMatrix cov;

        private ICovarianceMatrix impliedMatrix;

        public Score(Scorer scorer) {
            this.scorer = scorer;
            this.pValue = scorer.getPValue();
            this.fml = scorer.getFml();
            this.chisq = scorer.getChiSquare();
            this.bic = scorer.getBicScore();
            this.aic = scorer.getAicScore();
            this.dof = scorer.getDof();
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
//            return -chisq;
//            return -bic;
//            return -aic;
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

        public int getDof() {
            return dof;
        }

        public ICovarianceMatrix getCovarianceMatrix() {
            return cov;
        }

        public ICovarianceMatrix getImpliedMatrix() {
            return impliedMatrix;
        }

        public double getChiSquare() {
            return chisq;
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
