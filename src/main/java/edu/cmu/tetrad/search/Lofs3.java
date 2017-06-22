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

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.AndersonDarlingTest;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.PrintStream;
import java.util.*;

/**
 * LOFS = Ling Orientation Fixed Structure.
 *
 * @author Joseph Ramsey
 */
public class Lofs3 {
    private Graph pattern;
    private List<DataSet> dataSets;
    private double alpha = 0.05;
    private ArrayList<Regression> regressions;
    private List<Node> variables;
    private boolean r1Done = true;
    private boolean r2Done = true;
    private boolean strongR2 = false;
    private boolean meekDone = false;
    private boolean r2Orient2Cycles = true;
    private boolean meanCenterResiduals = false;
    private Graph trueGraph = null;
    private static Map<String, Map<String, Integer>> countMap = new HashMap<String, Map<String, Integer>>();

    private Lofs.Score score = Lofs.Score.andersonDarling;
    private double epsilon = 0.0;
    private PrintStream dataOut = System.out;

    //===============================CONSTRUCTOR============================//

    public Lofs3(Graph pattern, List<DataSet> dataSets)
            throws IllegalArgumentException {

        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must be specified.");
        }

        if (dataSets == null) {
            throw new IllegalArgumentException("Data set must be specified.");
        }

        this.pattern = pattern;
        this.dataSets = dataSets;

        regressions = new ArrayList<Regression>();
        this.variables = dataSets.get(0).getVariables();

        for (DataSet dataSet : dataSets) {
            regressions.add(new RegressionDataset(dataSet));
        }
    }

    public Graph orient() {
        Graph skeleton = GraphUtils.undirectedGraph(getPattern());
        Graph graph = new EdgeListGraph(skeleton.getNodes());

        List<Node> nodes = skeleton.getNodes();
//        Collections.shuffle(nodes);

        if (isR1Done()) {
            ruleR1(skeleton, graph, nodes);
        }

        for (Edge edge : skeleton.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        if (isR2Done()) {
            ruleR2(skeleton, graph);
        }

        if (isMeekDone()) {
            new MeekRules().orientImplied(graph);
        }

        printCounts();

        return graph;
    }

    private void printCounts() {
        for (String key : countMap.keySet()) {
            System.out.println();
            System.out.println(key);
            System.out.println();

            Map<String, Integer> counts = countMap.get(key);

            for (String key2 : counts.keySet()) {
                System.out.println(key2 + "\t" + counts.get(key2));
            }
        }
    }

    private void ruleR1(Graph skeleton, Graph graph, List<Node> nodes) {
        for (Node node : nodes) {
            SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();

            List<Node> adj = skeleton.getAdjacentNodes(node);

            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
            int[] choice;
            double maxScore = Double.NEGATIVE_INFINITY;
            List<Node> parents = null;

            while ((choice = gen.next()) != null) {
                List<Node> _parents = GraphUtils.asList(choice, adj);

                double score = score(node, _parents);
                scoreReports.put(-score, _parents.toString());

                if (score > maxScore) {
                    maxScore = score;
                    parents = _parents;
                }
            }

            for (double score : scoreReports.keySet()) {
                TetradLogger.getInstance().log("score", "For " + node + " parents = " + scoreReports.get(score) + " score = " + -score);
            }

            TetradLogger.getInstance().log("score", "");

            if (parents == null) {
                continue;
            }

            if (normal(node, parents)) continue;

            for (Node _node : adj) {
                if (parents.contains(_node)) {
                    Edge parentEdge = Edges.directedEdge(_node, node);

                    if (!graph.containsEdge(parentEdge)) {
                        graph.addEdge(parentEdge);
                    }
                }
            }
        }
    }

    private void ruleR2(Graph skeleton, Graph graph) {
        List<Edge> edgeList1 = skeleton.getEdges();
//        Collections.shuffle(edgeList1);

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            if (!isR2Orient2Cycles() && isTwoCycle(graph, x, y)) {
                continue;
            }

            if (!isTwoCycle(graph, x, y) && !isUndirected(graph, x, y)) {
                continue;
            }

            resolveOneEdgeMax(graph, x, y);
        }
    }

    private boolean isTwoCycle(Graph graph, Node x, Node y) {
        List<Edge> edges = graph.getEdges(x, y);
        return edges.size() == 2;
    }

    private boolean isUndirected(Graph graph, Node x, Node y) {
        List<Edge> edges = graph.getEdges(x, y);
        if (edges.size() == 1) {
            Edge edge = graph.getEdge(x, y);
            return Edges.isUndirectedEdge(edge);
        }

        return false;
    }

    private boolean normal(Node node, List<Node> parents) {
        if (getAlpha() > .999) {
            return false;
        }

        return pValue(node, parents) > getAlpha();
    }

    public void setEpsilon(double epsilon) {
        if (epsilon < 0.0) {
            throw new IllegalArgumentException();
        }

        this.epsilon = epsilon;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public PrintStream getDataOut() {
        return dataOut;
    }

    public void setDataOut(PrintStream dataOut) {
        this.dataOut = dataOut;
    }

//    private void resolveOneEdgeMax(Graph graph, Node x, Node y, boolean strong, Graph oldGraph) {
//        if (RandomUtil.getInstance().nextDouble() > 0.5) {
//            Node temp = x;
//            x = y;
//            y = temp;
//        }
//
//        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);
//
//        SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();
//
//        List<Node> neighborsx = graph.getAdjacentNodes(x);
//        neighborsx.remove(y);
//
//        double max = Double.NEGATIVE_INFINITY;
//        boolean left = false;
//        boolean right = false;
//
//        boolean sawLeftArrow = false;
//        boolean sawRightArrow = false;
//        boolean sawBidirected = false;
//
//        DepthChoiceGenerator genx = new DepthChoiceGenerator(neighborsx.size(), neighborsx.size());
//        int[] choicex;
//
//        while((choicex = genx.next()) != null) {
//            List<Node> condxMinus = GraphUtils.asList(choicex, neighborsx);
//
//            List<Node> condxPlus = new ArrayList<Node>(condxMinus);
//            condxPlus.add(y);
//
//            double xPlus = score(x, condxPlus);
//            double xMinus = score(x, condxMinus);
//
//            List<Node> neighborsy = graph.getAdjacentNodes(y);
//            neighborsy.remove(x);
//
//            DepthChoiceGenerator geny = new DepthChoiceGenerator(neighborsy.size(), neighborsy.size());
//            int[] choicey;
//
//            while ((choicey = geny.next()) != null) {
//                List<Node> condyMinus = GraphUtils.asList(choicey, neighborsy);
//
////                List<Node> parentsY = oldGraph.getParents(y);
////                parentsY.remove(x);
////                if (!condyMinus.containsAll(parentsY)) {
////                    continue;
////                }
//
//                List<Node> condyPlus = new ArrayList<Node>(condyMinus);
//                condyPlus.add(x);
//
//                double yPlus = score(y, condyPlus);
//                double yMinus = score(y, condyMinus);
//
//                // Checking them all at once is expensive but avoids lexical ordering problems in the algorithm.
//                if (normal(y, condyPlus) || normal(x, condxMinus) || normal(x, condxPlus) || normal(y, condyMinus)) {
//                    continue;
//                }
//
//                double delta = 0.0;
//
//                if (strong) {
//                    if (yPlus <= yMinus  + delta && xMinus <= xPlus  + delta) {
//                        double score = combinedScore(xPlus, yMinus);
//
//                        if (yPlus <= xPlus + delta && xMinus <= yMinus + delta) {
//                            StringBuilder builder = new StringBuilder();
//
//                            builder.append("\nStrong " + y + "->" + x + " " + score);
//                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                            scoreReports.put(-score, builder.toString());
//
//                            if (score > max) {
//                                max = score;
//                                left = true;
//                                right = false;
//                            }
//
//                            sawLeftArrow = true;
//                        }
//                        else {
//                            StringBuilder builder = new StringBuilder();
//
//                            builder.append("\nNo directed edge " + x + "--" + y + " " + score);
//                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                            scoreReports.put(-score, builder.toString());
//                        }
//                    }
//                    else if (yMinus <= yPlus  + delta && xPlus <= xMinus  + delta) {
//                        double score = combinedScore(yPlus, xMinus);
//
//                        if (xPlus <= yPlus + delta  && yMinus <= xMinus  + delta) {
//                            StringBuilder builder = new StringBuilder();
//
//                            builder.append("\nStrong " + x + "->" + y + " " + score);
//                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                            scoreReports.put(-score, builder.toString());
//
//                            if (score > max) {
//                                max = score;
//                                left = false;
//                                right = true;
//                            }
//
//                            sawRightArrow = true;
//                        }
//                        else {
//                            StringBuilder builder = new StringBuilder();
//
//                            builder.append("\nNo directed edge " + x + "--" + y + " " + score);
//                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                            scoreReports.put(-score, builder.toString());
//                        }
//                    }
////                    else if (xMinus < xPlus && yMinus < yPlus && yMinus <= xPlus && xMinus <= yPlus) {
////                        double score = combinedScore(xPlus, yPlus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\n2 cycle " + x + "--" + y + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = true;
////                            right = true;
////                        }
////
////                        sawLeftArrow = true;
////                        sawRightArrow = true;
////                    }
////                    else if (yPlus <= xMinus + delta && xPlus <= yMinus + delta) {
////                        double score = combinedScore(xMinus, yMinus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\nCommon cause " + x + "--" + y + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = false;
////                            right = false;
////                        }
////
////                        sawBidirected = true;
////                    }
//                    else if (commonCauseCondition(xPlus, xMinus, yPlus, yMinus)) {
//                        double score = combinedScore(xPlus, yPlus);
//
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nCommon cause " + x + "--" + y + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//                        builder.append(inequalitiesString(xPlus, xMinus, yPlus, yMinus));
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            left = false;
//                            right = false;
//                        }
//
//                        sawBidirected = true;
//                    }
//                    else {
//                        TetradLogger.getInstance().log("info", "\nUnclassified:");
//                        TetradLogger.getInstance().log("info", inequalitiesString(xPlus, xMinus, yPlus, yMinus));
//                    }
//                }
//                else { // weak
//                    if (yPlus <= yMinus  + delta && xMinus <= xPlus  + delta) {
//                        double score = combinedScore(xPlus, yMinus);
//
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + y + "->" + x + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            left = true;
//                            right = false;
//                        }
//
//                        sawLeftArrow = true;
//                    }
//                    else if (yMinus <= yPlus  + delta && xPlus <= xMinus  + delta) {
//                        double score = combinedScore(yPlus, xMinus);
//
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + x + "->" + y + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            left = false;
//                            right = true;
//                        }
//
//                        sawRightArrow = true;
//                    }
////                    if (yPlus <= xPlus  + delta && xMinus <= yMinus  + delta) {
////                        double score = combinedScore(xPlus, yMinus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\nWeak " + y + "->" + x + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = true;
////                            right = false;
////                        }
////
////                        sawLeftArrow = true;
////                    }
////                    else if (xPlus <= yPlus  + delta && yMinus <= xMinus  + delta) {
////                        double score = combinedScore(yPlus, xMinus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\nWeak " + x + "->" + y + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = false;
////                            right = true;
////                        }
////
////                        sawRightArrow = true;
////                    }
////                    else if (yMinus <= xPlus  + delta && xMinus <= yPlus + delta) {
////                        double score = combinedScore(xPlus, yPlus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\n2 cycle " + x + "--" + y + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = true;
////                            right = true;
////                        }
////
////                        sawLeftArrow = true;
////                        sawRightArrow = true;
////                    }
////                    else if (yPlus <= xMinus + delta && xPlus <= yMinus + delta) {
////                        double score = combinedScore(xMinus, yMinus);
////
////                        StringBuilder builder = new StringBuilder();
////
////                        builder.append("\nCommon cause " + x + "--" + y + " " + score);
////                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
////                        builder.append(inequalitiesString(xPlus, xMinus, yPlus, yMinus));
////
////                        scoreReports.put(-score, builder.toString());
////
////                        if (score > max) {
////                            max = score;
////                            left = false;
////                            right = false;
////                        }
////
////                        sawBidirected = true;
////                    }
//                    else if (commonCauseCondition(xPlus, xMinus, yPlus, yMinus)) {
//                        double score = combinedScore(xPlus, yPlus);
//
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nCommon cause " + x + "--" + y + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            left = false;
//                            right = false;
//                        }
//
//                        sawBidirected = true;
//                    }
//                    else if (xPlus + delta > xMinus && yPlus + delta > yMinus) {
//                        double score = combinedScore(xPlus, yPlus);
//
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\n2 cycle " + x + "<=>" + y + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            left = true;
//                            right = true;
//                        }
//
//                        sawLeftArrow = true;
//                        sawRightArrow = true;
//                    }
//                    else {
//                        TetradLogger.getInstance().log("info", "\nUnclassified:");
//                        TetradLogger.getInstance().log("info", inequalitiesString(xPlus, xMinus, yPlus, yMinus));
//                    }
//                }
//            }
//        }
//
//        for (double score : scoreReports.keySet()) {
//            TetradLogger.getInstance().log("info", scoreReports.get(score));
//        }
//
//        graph.removeEdges(x, y);
//
//        if (!left && !right) {  // frown--can only be a 2 cycle.
//            graph.addBidirectedEdge(x, y);
//        }
//        else if (left && right) {
//            graph.addDirectedEdge(x, y);
//            graph.addDirectedEdge(y, x);
//        }
//        else if (left) {
//            graph.addDirectedEdge(y, x);
//        }
//        else if (right) {
//            graph.addDirectedEdge(x, y);
//        }
//
////        if (sawBidirected) {
////            Edge edge = Edges.bidirectedEdge(x, y);
////            if (!graph.containsEdge(edge)) graph.addEdge(edge);
////        }
//////        else {
////            if (sawLeftArrow) {
////                Edge edge = Edges.directedEdge(y, x);
////                if (!graph.containsEdge(edge)) graph.addEdge(edge);
////            }
////
////            if (sawRightArrow) {
////                Edge edge = Edges.directedEdge(x, y);
////                if (!graph.containsEdge(edge)) graph.addEdge(edge);
////            }
//////        }
//
////        if (sawLeftArrow) {
////            Edge edge = Edges.directedEdge(y, x);
////            if (!graph.containsEdge(edge)) graph.addEdge(edge);
////        }
////
////        if (sawRightArrow) {
////            Edge edge = Edges.directedEdge(x, y);
////            if (!graph.containsEdge(edge)) graph.addEdge(edge);
////        }
////
////        if (!sawLeftArrow && !sawRightArrow && sawBidirected) {
////            Edge edge = Edges.bidirectedEdge(x, y);
////            if (!graph.containsEdge(edge)) graph.addEdge(edge);
////        }
//
//        if (!graph.isAdjacentTo(x, y)) {
//            graph.addUndirectedEdge(x, y);
//        }
//    }

//    private boolean commonCauseCondition(double xPlus, double xMinus, double yPlus, double yMinus) {
//        boolean b1 = xPlus > yPlus && xPlus > yMinus && xMinus > yPlus && xMinus > yMinus && xPlus > xMinus && yMinus > yPlus;
//        boolean b2 = yPlus > xPlus && yPlus > xMinus && yMinus > xPlus && yMinus > xMinus && yPlus > yMinus && xMinus > xPlus;
//        return b1 || b2;
//    }
//
//    private boolean commonCauseCondition(double xPlus, double xMinus, double yPlus, double yMinus) {
//        boolean b1 = close(xPlus, yPlus) && close(xPlus, yMinus) && close(xMinus, yPlus) && close(xMinus, yMinus);
//        boolean b2 = close(yPlus, xPlus) && close(yPlus, xMinus) && close(yMinus, xPlus) && close(yMinus, xMinus);
//        return b1 || b2;
//    }

//    private boolean commonCauseCondition(double xPlus, double xMinus, double yPlus, double yMinus) {
//        boolean b1 = close(xPlus, xMinus);
//        boolean b2 = close(yPlus, yMinus);
//        return b1 && b2;
//    }

    private enum Direction {
        left, right, bidirected, twoCycle, nonadjacent
    }

//    private void resolveOneEdgeMax2(Graph graph, Node x, Node y) {
//        if (RandomUtil.getInstance().nextDouble() > 0.5) {
//            Node temp = x;
//            x = y;
//            y = temp;
//        }
//
//
//        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);
//
//        SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();
//
//        List<Node> neighborsx = graph.getAdjacentNodes(x);
//        neighborsx.remove(y);
//
//        double max = Double.NEGATIVE_INFINITY;
//
//        Direction direction = null;
//
//        DepthChoiceGenerator genx = new DepthChoiceGenerator(neighborsx.size(), neighborsx.size());
//        int[] choicex;
//
//        while ((choicex = genx.next()) != null) {
//            List<Node> condxMinus = new ArrayList<Node>(); //GraphUtils.asList(choicex, neighborsx);
//
//            List<Node> condxPlus = new ArrayList<Node>(condxMinus);
//            condxPlus.add(y);
//
//            double xPlus = score(x, condxPlus);
//            double xMinus = score(x, condxMinus);
//
//            List<Node> neighborsy = graph.getAdjacentNodes(y);
//            neighborsy.remove(x);
//
//            DepthChoiceGenerator geny = new DepthChoiceGenerator(neighborsy.size(), neighborsy.size());
//            int[] choicey;
//
//            while ((choicey = geny.next()) != null) {
//                List<Node> condyMinus = new ArrayList<Node>(); //GraphUtils.asList(choicey, neighborsy);
//
//                List<Node> condyPlus = new ArrayList<Node>(condyMinus);
//                condyPlus.add(x);
//
//                double yPlus = score(y, condyPlus);
//                double yMinus = score(y, condyMinus);
//
//                double xMax = xPlus > xMinus ? xPlus : xMinus;
//                double yMax = yPlus > yMinus ? yPlus : yMinus;
//
//                double score = combinedScore(xMax, yMax);
//                TetradLogger.getInstance().log("info", "Score = " + score);
//
////                System.out.println(x + " " + y + " " + xMinus + " " + xPlus + " " + yMinus + " " + yPlus);
//
//                // Checking them all at once is expensive but avoids lexical ordering problems in the algorithm.
////                if (normal(y, condyPlus) || normal(x, condxMinus) || normal(x, condxPlus) || normal(y, condyMinus)) {
////                    continue;
////                }
//
////                if (allXOrAllYMax(xPlus, xMinus, yPlus, yMinus, delta)) {
////                    StringBuilder builder = new StringBuilder();
////
////                    builder.append("\nWeak " + y + "<->" + x + " " + score);
////                    builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                    builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                    scoreReports.put(-score, builder.toString());
////
////                    if (score > max) {
////                        max = score;
////                        direction = Direction.bidirected;
////                    }
////
////                }
////                else
//                if (xPlus > xMinus + epsilon && yMinus > yPlus + epsilon) { // twisted left.
////                    if (xPlus > yPlus && yMinus > xMinus) {
//                    StringBuilder builder = new StringBuilder();
//
//                    builder.append("\nWeak " + y + "->" + x + " " + score);
//                    builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                    builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                    scoreReports.put(-score, builder.toString());
//
//                    if (score > max) {
//                        max = score;
//                        direction = Direction.left;
//                    }
////                    }
////                        else {
////                            StringBuilder builder = new StringBuilder();
////
////                            builder.append("\nWeak " + y + "<=>" + x + " " + score);
////                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                            scoreReports.put(-score, builder.toString());
////                            sawLeftArrow = true;
////
////                            if (score > max) {
////                                max = score;
//////                                leftDown = true;
//////                                rightDown = false;
////                                direction = Direction.bidirected;
////                            }
////                        }
//                } else if (xMinus > xPlus + epsilon && yPlus > yMinus + epsilon) { // twisted right
////                    if (xMinus > yMinus && yPlus > xPlus) {
//                    StringBuilder builder = new StringBuilder();
//
//                    builder.append("\nWeak " + x + "->" + y + " " + score);
//                    builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                    builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                    scoreReports.put(-score, builder.toString());
//
//                    if (score > max) {
//                        max = score;
//                        direction = Direction.right;
//                    }
////                    }
////                        else {
////                            StringBuilder builder = new StringBuilder();
////
////                            builder.append("\nWeak " + y + "<=>" + x + " " + score);
////                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
////                            builder.append("\n   Parents(" + y + ") = " + condyMinus);
////
////                            scoreReports.put(-score, builder.toString());
////                            sawLeftArrow = true;
////
////                            if (score > max) {
////                                max = score;
//////                                leftDown = true;
//////                                rightDown = false;
////                                direction = Direction.bidirected;
////                            }
////                        }
//                } else if (xPlus > xMinus + epsilon && yPlus > yMinus + epsilon) { // smile
//                    if (allXOrAllYMax(xPlus, xMinus, yPlus, yMinus)) {
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + y + "<->" + x + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            direction = Direction.bidirected;
//                        }
//
//                    } else {
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + y + "<=>" + x + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            direction = Direction.twoCycle;
//                        }
//                    }
//                } else if (xMinus > xPlus + epsilon && yMinus > yPlus + epsilon) {  // frown
//                    if (allXOrAllYMax(xPlus, xMinus, yPlus, yMinus)) {
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + y + "<->" + x + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            direction = Direction.bidirected;
//                        }
//
//                    } else {
//                        StringBuilder builder = new StringBuilder();
//
//                        builder.append("\nWeak " + y + "<=>" + x + " " + score);
//                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
//                        builder.append("\n   Parents(" + y + ") = " + condyMinus);
//
//                        scoreReports.put(-score, builder.toString());
//
//                        if (score > max) {
//                            max = score;
//                            direction = Direction.twoCycle;
//                        }
//                    }
//                }
//            }
//        }
//
//
//
//        for (double score : scoreReports.keySet()) {
//            TetradLogger.getInstance().log("info", scoreReports.get(score));
//        }
//
//        graph.removeEdges(x, y);
//
//        if (direction == Direction.bidirected) {
//            graph.addBidirectedEdge(x, y);
//        } else if (direction == Direction.twoCycle) {
//            graph.addDirectedEdge(x, y);
//            graph.addDirectedEdge(y, x);
//        } else if (direction == Direction.left) {
//            graph.addDirectedEdge(y, x);
//        } else if (direction == Direction.right) {
//            graph.addDirectedEdge(x, y);
//        }
//
//        if (!graph.isAdjacentTo(x, y)) {
//            graph.addUndirectedEdge(x, y);
//        }
//
//        System.out.println();
//    }

    private void resolveOneEdgeMax(Graph graph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();

        Direction direction = null;

        List<Node> condxMinus = new ArrayList<Node>();
        List<Node> condxPlus = new ArrayList<Node>(condxMinus);
        condxPlus.add(y);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        List<Node> condyMinus = new ArrayList<Node>();
        List<Node> condyPlus = new ArrayList<Node>(condyMinus);
        condyPlus.add(x);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        double deltaX = xPlus - xMinus;
        double deltaY = yPlus - yMinus;

        double ratioX = xPlus / xMinus;
        double ratioY = yPlus / yMinus;

        System.out.println("Ratio   X = " + ratioX + " ratio Y = " + ratioY);


        TetradLogger.getInstance().log("info", "deltaX = " + deltaX + " deltaY = " + deltaY);

        // Checking them all at once is expensive but avoids lexical ordering problems in the algorithm.
        if (normal(y, condyPlus) || normal(x, condxMinus) || normal(x, condxPlus) || normal(y, condyMinus)) {
            return;
        }

        StringBuilder builder = new StringBuilder();

//        double epsilon = 0.2;

        double tightBound = epsilon;

        boolean xUnchanged = 1 / ratioX < 1 + 2*epsilon && ratioX < 1 + 2*epsilon;
        boolean yUnchanged = 1 / ratioY < 1 + 2*epsilon && ratioY < 1 + 2*epsilon;

        boolean xUnchangedTight = 1 / ratioX < 1 + tightBound && ratioX < 1 + tightBound;
        boolean yUnchangedTight = 1 / ratioY < 1 + tightBound && ratioY < 1 + tightBound;

        boolean xLow = 1 / ratioX > 1 + epsilon;
        boolean xHigh = ratioX > 1 + epsilon;
        boolean yLow = 1 / ratioY > 1 + epsilon;
        boolean yHigh = ratioY > 1 + epsilon;

//        boolean xUnchanged = -epsilon < ratioX - 1 && ratioX - 1 < epsilon;
//        boolean yUnchanged = -epsilon < ratioY - 1 && ratioY - 1 < epsilon;
//        boolean xLow = ratioX - 1. < -epsilon;
//        boolean yHigh = ratioY - 1. > epsilon;
//        boolean xHigh = ratioX - 1. > epsilon;
//        boolean yLow = ratioY - 1. < -epsilon;

        if (xUnchangedTight && yUnchangedTight) {
            builder.append("\nOrienting " + y + "   " + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.nonadjacent;
        } else if (xUnchanged && yUnchanged) {
            builder.append("\nOrienting " + y + "<->" + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.bidirected;
        } else if (xLow && yHigh) {
            builder.append("\nOrienting " + x + "->" + y + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.right;
        } else if (xUnchanged && yHigh) {
            builder.append("\nOrienting " + x + "->" + y + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.right;
        } else if (xLow && yUnchanged) {
            builder.append("\nOrienting " + x + "->" + y + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.right;
        } else if (xHigh && yLow) {
            builder.append("\nOrienting " + y + "->" + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.left;
        } else if (xHigh && yUnchanged) {
            builder.append("\nOrienting " + y + "->" + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.left;
        } else if (xUnchanged && yLow) {
            builder.append("\nOrienting " + y + "->" + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.left;
        } else if ((xLow && yLow) || (xHigh && yHigh)) {
            builder.append("\nOrienting " + y + "<=>" + x + " " + score);
            scoreReports.put(-score, builder.toString());
            direction = Direction.twoCycle;
        } else {
            direction = Direction.nonadjacent;
        }

//        if (ratioX < 1 && ratioY > 1) {
//            builder.append("\nOrienting " + x + "->" + y + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.right;
//        } else if (xUnchanged && yUnchanged) {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        }
//        else if (ratioX > 1 && ratioY < 1) {
//            builder.append("\nOrienting " + y + "->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.left;
//        }
//        else if ((xLow && yLow) || (xHigh && yHigh)) {
//            builder.append("\nOrienting " + y + "<=>" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.twoCycle;
//        }

        // Drunken sailor...
//        if (ratioY - ratioX > epsilon) {
//            builder.append("\nOrienting " + x + "->" + y + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.right;
//        } else if (ratioX - ratioY > epsilon) {
//            builder.append("\nOrienting " + y + "->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.left;
//        } else {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        }

//        if (xPlus > yPlus && xMinus > yPlus && yMinus > yPlus /*&& xMinus > yMinus*/ &&xMinus > xPlus) {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        } else if (yPlus > xPlus && yMinus > xPlus && xMinus > xPlus /*&& yMinus > xMinus*/ && yMinus > yPlus) {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        } else if (deltaX < deltaY) {
//            builder.append("\nOrienting " + x + "->" + y + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.right;
//        } else if (deltaY < deltaX) {
//            builder.append("\nOrienting " + y + "->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.left;
//        }

//        if (xPlus > yPlus && xMinus > yPlus && yMinus > yPlus && xMinus > yMinus && xMinus > xPlus) {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        } else if (yPlus > xPlus && yMinus > xPlus && xMinus > xPlus && yMinus > xMinus && yMinus > yPlus) {
//            builder.append("\nOrienting " + y + "<->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.bidirected;
//        } else if (xMinus > xPlus && yPlus > yMinus) {
//            builder.append("\nOrienting " + x + "->" + y + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.right;
//        } else if (xPlus > xMinus && yMinus > yPlus) {
//            builder.append("\nOrienting " + y + "->" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.left;
////        } else if (xPlus > yPlus && xMinus > yPlus && yMinus > yPlus && xMinus > yMinus) {
////            builder.append("\nOrienting " + y + "<->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.bidirected;
////        } else if (yPlus > xPlus && yMinus > xPlus && xMinus > xPlus && yMinus > xMinus) {
////            builder.append("\nOrienting " + y + "<->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.bidirected;
////        } else if ((deltaX < -epsilon && deltaY < -epsilon /*&& xMinus - xPlus > -epsilon && yMinus - xPlus > -epsilon*/ && xMinus - yMinus > -epsilon && xPlus - yPlus > -epsilon )) {
////            builder.append("\nOrienting " + y + "<->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.bidirected;
////        } else if ((deltaX < -epsilon && deltaY < -epsilon /* &&  yMinus - yPlus > -epsilon && xMinus - yPlus > -epsilon*/ && yMinus - xMinus > -epsilon && yPlus - xPlus > -epsilon )) {
////            builder.append("\nOrienting " + y + "<->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.bidirected;
//        } else if (xMinus > xPlus && yMinus > xPlus && xMinus > yPlus && yPlus > xPlus && xMinus > yMinus) {
//            builder.append("\nOrienting " + y + "<=>" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.twoCycle;
//        } else if (yMinus > yPlus && xMinus > yPlus && yMinus > xPlus && xPlus > yPlus && yMinus > xMinus) {
//            builder.append("\nOrienting " + y + "<=>" + x + " " + score);
//            scoreReports.put(-score, builder.toString());
//            direction = Direction.twoCycle;
////        } else {
////            builder.append("\nOrienting " + y + "<=>" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.twoCycle;
////        } else if (deltaX < epsilon && deltaY > epsilon) {
////            builder.append("\nOrienting " + x + "->" + y + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.right;
////        } else if (deltaX < -epsilon && deltaY > -epsilon) {
////            builder.append("\nOrienting " + x + "->" + y + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.right;
////        } else if (deltaX > -epsilon && deltaY < -epsilon) {
////            builder.append("\nOrienting " + y + "->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.left;
////        } else if (deltaX > epsilon && deltaY < epsilon) {
////            builder.append("\nOrienting " + y + "->" + x + " " + score);
////            scoreReports.put(-score, builder.toString());
////            direction = Direction.left;
//        } else if (deltaX > -epsilon && deltaX < epsilon && deltaY > -epsilon && deltaY < epsilon) {
//            // skip these.
//        }

        for (double _score : scoreReports.keySet()) {
            TetradLogger.getInstance().log("info", scoreReports.get(_score));
        }

        graph.removeEdges(x, y);

        if (direction == Direction.nonadjacent) {
            graph.removeEdges(x, y);
        }else if (direction == Direction.bidirected) {
            graph.addBidirectedEdge(x, y);
        } else if (direction == Direction.twoCycle) {
            graph.addDirectedEdge(x, y);
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.left) {
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.right) {
            graph.addDirectedEdge(x, y);
        }

//        if (!graph.isAdjacentTo(x, y)) {
//            graph.addUndirectedEdge(x, y);
//        }

        count(xPlus, xMinus, yPlus, yMinus, x, y, direction);
    }

//    private boolean greater(double xPlus, double xMinus, double delta) {
//        return xPlus - xMinus > -delta;
//    }

    private boolean allXOrAllYMax(double xPlus, double xMinus, double yPlus, double yMinus) {
        boolean b1 = xPlus > yPlus && xPlus > yMinus && xMinus > yPlus && xMinus > yMinus;
        boolean b2 = yPlus > xPlus && yPlus > xMinus && yMinus > xPlus && yMinus > xMinus;
        boolean b3 = b1 || b2;
        return b3;
    }

    private boolean commonCauseCondition(double xPlus, double xMinus, double yPlus, double yMinus) {
//        boolean b0 = xPlus > xMinus && yPlus > yMinus;
//        boolean b1 = close(xPlus, yPlus) && close(xPlus, yMinus) && close(xMinus, yPlus) && close(xMinus, yMinus);
//        boolean b2 = close(yPlus, xPlus) && close(yPlus, xMinus) && close(yMinus, xPlus) && close(yMinus, xMinus);
//        boolean b1 = xPlus > yPlus && xPlus > yMinus && xMinus > yPlus && xMinus > yMinus;
//        boolean b2 = yPlus > xPlus && yPlus > xMinus && yMinus > xPlus && yMinus > xMinus;

        boolean b0 = xPlus < xMinus && yPlus < yMinus;

        return b0; // && (b1 || b2);
    }


    private boolean close(double x, double y) {
//        double v = ((x + y) / 2.) * .5;
        return Math.abs(x - y) < 5;
    }

    private int nodesIndex = 0;

    private void count(double xPlus, double xMinus, double yPlus, double yMinus, Node x, Node y, Direction direction) {
//        if (nodesIndex == 1) return;

        double xRatio = xPlus / xMinus;
        double yRatio = yPlus / yMinus;

        String type = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());

            List<Edge> edges = trueGraph.getEdges(_x, _y);
            Edge edge = null;

            if (edges.size() == 1) {
                edge = edges.get(0);
            }

            if (edge != null && Edges.isDirectedEdge(edge) && edge.pointsTowards(_y)) {
                type = "X->Y";
            } else if (edge != null && Edges.isDirectedEdge(edge) && edge.pointsTowards(_x)) {
                type = "X<-Y";
            } else if (edge != null && Edges.isBidirectedEdge(edge)) {
                type = "X<->Y";
            } else if (edge != null && edge.getProximalEndpoint(_x) == Endpoint.CIRCLE && edge.getProximalEndpoint(_y) == Endpoint.ARROW) {
                type = "Xo->Y";
            } else if (edge != null && edge.getProximalEndpoint(_x) == Endpoint.ARROW && edge.getProximalEndpoint(_y) == Endpoint.CIRCLE) {
                type = "X<-oY";
            } else if (edge != null && Edges.isUndirectedEdge(edge)) {
                type = "NOEDGE";
            } else if (edges.size() == 2) {
                type = "X<=>Y";
            }

        } else {
            if (direction == Direction.right) {
                type = "X->Y";
            }
            else if (direction == Direction.left) {
                type = "X<-Y";
            }
            else if (direction == Direction.bidirected) {
                type = "X<->Y";
            }
            else if (direction == Direction.twoCycle) {
                type = "X<=>Y";
            }
        }

        double xyRatio = xPlus / yMinus;
        double yxRatio = yPlus / xMinus;

        getDataOut().println(xRatio + "\t" + yRatio + "\t" + xyRatio + "\t" + yxRatio + "\t" + (type == null ? "" : type));

        System.out.println(++nodesIndex + "\t" + x + "\t" + y);

        if (!countMap.containsKey(type)) {
            countMap.put(type, new HashMap<String, Integer>());
        }

        if (greaterThan(xPlus, yPlus, epsilon)) {
            increment(countMap, type, "X|Y > Y|X");
        }

        if (greaterThan(yPlus, xPlus, epsilon)) {
            increment(countMap, type, "Y|X > X|Y");
        }

        if (greaterThan(xPlus, yMinus, epsilon)) {
            increment(countMap, type, "X|Y > Y");
        }

        if (greaterThan(yMinus, xPlus, epsilon)) {
            increment(countMap, type, "Y > X|Y");
        }

        if (greaterThan(xMinus, yPlus, epsilon)) {
            increment(countMap, type, "X > Y|X");
        }

        if (greaterThan(yPlus, xMinus, epsilon)) {
            increment(countMap, type, "Y|X > X");
        }

        if (greaterThan(xMinus, yMinus, epsilon)) {
            increment(countMap, type, "X > Y");
        }

        if (greaterThan(yMinus, xMinus, epsilon)) {
            increment(countMap, type, "Y > X");
        }

        if (greaterThan(yPlus, yMinus, epsilon)) {
            increment(countMap, type, "Y|X > Y");
        }

        if (greaterThan(yMinus, yPlus, epsilon)) {
            increment(countMap, type, "Y > Y|X");
        }

        if (greaterThan(xPlus, xMinus, epsilon)) {
            increment(countMap, type, "X|Y > X");
        }

        if (greaterThan(xMinus, xPlus, epsilon)) {
            increment(countMap, type, "X > X|Y");
        }
    }

    private boolean greaterThan(double x, double y, double epsilon) {
        return x - y > epsilon;
    }

    private void increment(Map<String, Map<String, Integer>> map, String key, String s) {
        if (map.get(key).get(s) == null) {
            map.get(key).put(s, 1);
        }

        map.get(key).put(s, map.get(key).get(s) + 1);
    }

    private double combinedScore(double score1, double score2) {
        return score1 + score2;
    }

    private double score(Node y, List<Node> parents) {
        if (score == Lofs.Score.andersonDarling) {
            return andersonDarlingPASquareStar(y, parents);
        } else if (score == Lofs.Score.kurtosis) {
            return Math.abs(StatUtils.kurtosis(residual(y, parents)));
        } else if (score == Lofs.Score.skew) {
            return Math.abs(StatUtils.skewness(residual(y, parents)));
        } else if (score == Lofs.Score.fifthMoment) {
            return Math.abs(StatUtils.standardizedFifthMoment(residual(y, parents)));
        } else if (score == Lofs.Score.absoluteValue) {
            return localScoreA(y, parents);
        }

        throw new IllegalStateException();
    }

    //=============================PRIVATE METHODS=========================//

    private double localScoreA(Node node, List<Node> parents) {
        double score = 0.0;

        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];


        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _residuals.size(); k++) {
            f.set(k, Math.abs(f.get(k)));
        }

        double _mean = Descriptive.mean(f);
        double diff = _mean - Math.sqrt(2.0 / Math.PI);
        score += diff * diff;

        return score;
    }

    private double localScoreCosh(Node node, List<Node> parents) {
        double score = 0.0;

        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _residuals.size(); k++) {
            f.set(k, -2 * Math.log(Math.cosh((Math.PI / (2 * Math.sqrt(3))) * f.get(k))));
        }

        double _mean = Descriptive.mean(f);
        double diff = _mean - Math.sqrt(2.0 / Math.PI);
        score += diff * diff;

        return score;
    }

    private double localScoreB(Node node, List<Node> parents) {

        double score = 0.0;
        double maxScore = Double.NEGATIVE_INFINITY;

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();
            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
            }

            double[] _f = new double[_residualsSingleDataset.size()];

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _f[k] = _residualsSingleDataset.get(k);
            }

            DoubleArrayList f = new DoubleArrayList(_f);

            for (int k = 0; k < f.size(); k++) {
                f.set(k, Math.abs(f.get(k)));
            }

            double _mean = Descriptive.mean(f);
            double diff = _mean - Math.sqrt(2.0 / Math.PI);
            score += diff * diff;

            if (score > maxScore) {
                maxScore = score;
            }
        }


        double avg = score / dataSets.size();

        return avg;
    }

    private double andersonDarlingPASquareStar(Node node, List<Node> parents) {
        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);

//            System.out.println(Arrays.toString(result.getCoef()));

            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            // By centering the individual residual columns, all moments of the mixture become weighted averages of the moments
            // of the individual columns. http://en.wikipedia.org/wiki/Mixture_distribution#Finite_and_countable_mixtures
            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2)) / std);
                if (isMeanCenterResiduals()) {
                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
                }
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        return new AndersonDarlingTest(_f).getASquaredStar();
    }

    private double andersonDarlingPASquareStarB(Node node, List<Node> parents) {
        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        double sum = 0.0;

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            // By centering the individual residual columns, all moments of the mixture become weighted averages of the moments
            // of the individual columns. http://en.wikipedia.org/wiki/Mixture_distribution#Finite_and_countable_mixtures
            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2)) / std);
                if (isMeanCenterResiduals()) {
                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
                }
            }

            double[] _f = new double[_residuals.size()];

            for (int k = 0; k < _residuals.size(); k++) {
                _f[k] = _residuals.get(k);
            }

            sum += new AndersonDarlingTest(_f).getASquaredStar();
        }

        return sum / dataSets.size();
    }

    private double pValue(Node node, List<Node> parents) {
        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
                if (isMeanCenterResiduals()) {
                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
                }
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2)));
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        return new AndersonDarlingTest(_f).getP();
    }

    private double[] residual(Node node, List<Node> parents) {
        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List<Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);
//            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
//                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2)));
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        return _f;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha is in range [0, 1]");
        }

        this.alpha = alpha;
    }

    private Graph getPattern() {
        return pattern;
    }

    private Node getVariable(List<Node> variables, String name) {
        for (Node node : variables) {
            if (name.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    public boolean isR1Done() {
        return r1Done;
    }

    public void setR1Done(boolean r1Done) {
        this.r1Done = r1Done;
    }

    public boolean isR2Done() {
        return r2Done;
    }

    public void setR2Done(boolean r2Done) {
        this.r2Done = r2Done;
    }

    public boolean isMeekDone() {
        return meekDone;
    }

    public void setMeekDone(boolean meekDone) {
        this.meekDone = meekDone;
    }

    public boolean isStrongR2() {
        return strongR2;
    }

    public void setStrongR2(boolean strongR2) {
        this.strongR2 = strongR2;
    }

    public void setR2Orient2Cycles(boolean r2Orient2Cycles) {
        this.r2Orient2Cycles = r2Orient2Cycles;
    }

    public boolean isR2Orient2Cycles() {
        return r2Orient2Cycles;
    }

    public Lofs.Score getScore() {
        return score;
    }

    public void setScore(Lofs.Score score) {
        if (score == null) {
            throw new NullPointerException();
        }

        this.score = score;
    }

    public boolean isMeanCenterResiduals() {
        return meanCenterResiduals;
    }

    public void setMeanCenterResiduals(boolean meanCenterResiduals) {
        this.meanCenterResiduals = meanCenterResiduals;
    }

    private void trainSvm() {

    }

}
