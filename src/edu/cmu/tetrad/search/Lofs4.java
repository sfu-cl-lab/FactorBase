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
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.AndersonDarlingTest;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.util.*;

import java.io.PrintStream;
import java.util.*;

/**
 * LOFS = Ling Orientation Fixed Structure.
 *
 * @author Joseph Ramsey
 */
public class Lofs4 {
    private Graph pattern;
    private List<DataSet> dataSets;
    private List<DoubleMatrix2D> dataSetMatrices;
    private double alpha = 1.0;
    private ArrayList<Regression> regressions;
    private List<Node> variables;
    private boolean r1Done = true;
    private boolean r2Done = true;
    private boolean r3Done = true;
    private boolean strongR2 = false;
    private boolean meekDone = false;
    private boolean r2Orient2Cycles = true;
    private boolean meanCenterResiduals = false;
    private Graph trueGraph = null;
    private static Map<String, Map<String, Integer>> countMap = new HashMap<String, Map<String, Integer>>();

    private Lofs.Score score = Lofs.Score.andersonDarling;
    private double epsilon = 1.0;
    private PrintStream dataOut = System.out;
    private Knowledge knowledge = new Knowledge();
    private static double logCoshExp = logCoshExp();
    private double expectedExp;
    private Rule rule = Rule.R5;

    //===============================CONSTRUCTOR============================//

    public Lofs4(Graph pattern, List<DataSet> dataSets)
            throws IllegalArgumentException {

        if (dataSets == null) {
            throw new IllegalArgumentException("Data set must be specified.");
        }

        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must be specified.");
        }

        this.pattern = pattern;

//        this.dataSets = new ArrayList<DataSet>();
//
//        for (DataSet dataSet : dataSets) {
//            DoubleMatrix2D _data = dataSet.getDoubleData();
//            _data = DataUtils.standardizeData(_data);
//            DataSet _dataSet = ColtDataSet.makeContinuousData(dataSet.getVariables(), _data);
//            this.dataSets.add(_dataSet);
//        }

        this.dataSets = dataSets;

        dataSetMatrices = new ArrayList<DoubleMatrix2D>();

        for (DataSet dataSet : dataSets) {
            dataSetMatrices.add(dataSet.getDoubleData());
        }

        regressions = new ArrayList<Regression>();
        this.variables = dataSets.get(0).getVariables();

        for (DataSet dataSet : dataSets) {
            regressions.add(new RegressionDataset(dataSet));
        }
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public enum Rule {
        R1, R2, R3, R4, R5, R6, R7, R8
    }

    public Graph orient() {
        Graph skeleton = GraphUtils.undirectedGraph(getPattern());
        Graph graph = new EdgeListGraph(skeleton.getNodes());

        List<Node> nodes = skeleton.getNodes();
//        Collections.shuffle(nodes);

        if (this.rule == Rule.R1) {
            ruleR1(skeleton, graph, nodes);
        } else if (this.rule == Rule.R2) {
            graph = GraphUtils.undirectedGraph(skeleton);
            ruleR2(graph, graph);
        } else if (this.rule == Rule.R3) {
            graph = GraphUtils.undirectedGraph(skeleton);
            ruleR3(graph);
        } else if (this.rule == Rule.R4) {
            graph = GraphUtils.undirectedGraph(skeleton);
            ruleR4(graph);
        } else if (this.rule == Rule.R5) {
            graph = GraphUtils.undirectedGraph(skeleton);
            ruleR5(graph);
            graph = GraphUtils.undirectedToBidirected(graph);
            graph = GraphUtils.bidirectedToTwoCycle(graph);
        } else if (this.rule == Rule.R6) {
            graph = GraphUtils.undirectedGraph(skeleton);
            graph = GraphUtils.undirectedToBidirected(ruleR6(graph));
            graph = GraphUtils.bidirectedToTwoCycle(graph);
        } else if (this.rule == Rule.R7) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return ruleR7(graph);
        } else if (this.rule == Rule.R8) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return ruleR8(graph);
//            return search2(graph.getNodes());
        }

        return graph;
    }

//    private Graph removeZeroEdges(Graph graph) {
//        graph = new EdgeListGraph(graph);
//
//        for (Edge edge : graph.getEdges()) {
//            if (Edges.isDirectedEdge(edge)) {
//                double p = avgRegressionP(edge.getNode2(), edge.getNode1());
//                if (p > getAlpha()) {
//                    graph.removeEdge(edge);
//                }
//            }
//        }
//
//        return graph;
//    }

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

            List<Node> adj = new ArrayList<Node>();

            for (Node _node : skeleton.getAdjacentNodes(node)) {
                if (knowledge.edgeForbidden(_node.getName(), node.getName())) {
                    continue;
                }

                adj.add(_node);
            }

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

        for (Edge edge : skeleton.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }
    }

    private void ruleR1b(Graph skeleton, Graph graph, List<Node> nodes) {
        for (Node node : nodes) {
            List<Node> parents = new ArrayList<Node>();
            double score = score(node, Collections.<Node>emptyList());

            while (true) {
                Node savedParent = null;
                List<Node> adj = skeleton.getAdjacentNodes(node);
                adj.removeAll(parents);

                for (Node _parent : adj) {
                    parents.add(_parent);
                    double _score = score(node, parents);

                    if (_score / score > 1 + epsilon || knowledge.edgeForbidden(node.getName(), _parent.getName())) {
                        savedParent = _parent;
                        score = _score;
                    }

                    parents.remove(_parent);
                }

                if (savedParent == null) {
                    break;
                }

                parents.add(savedParent);
                score = score(node, parents);

                TetradLogger.getInstance().log("score", "For " + node + " parents = " + parents + " score = " + -score);
            }

            if (normal(node, parents)) continue;

            for (Node _node : parents) {
                Edge parentEdge = Edges.directedEdge(_node, node);

                if (!graph.containsEdge(parentEdge)) {
                    graph.addEdge(parentEdge);
                }
            }
        }
    }

    private void ruleR1c(Graph skeleton, Graph graph, List<Node> nodes) {
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

            resolveOneEdgeMax2(graph, x, y, isStrongR2(), new EdgeListGraph(graph));
        }
    }

    private void resolveOneEdgeMax2(Graph graph, Node x, Node y, boolean strong, Graph oldGraph) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();

        List<Node> neighborsx = new ArrayList<Node>();

        for (Node _node : graph.getAdjacentNodes(x)) {
            if (!knowledge.edgeForbidden(_node.getName(), x.getName())) {
                neighborsx.add(_node);
            }
        }

//        List<Node> neighborsx = graph.getAdjacentNodes(x);
        neighborsx.remove(y);

        double max = Double.NEGATIVE_INFINITY;
        boolean left = false;
        boolean right = false;

        DepthChoiceGenerator genx = new DepthChoiceGenerator(neighborsx.size(), neighborsx.size());
        int[] choicex;

        while ((choicex = genx.next()) != null) {
            List<Node> condxMinus = GraphUtils.asList(choicex, neighborsx);

            List<Node> condxPlus = new ArrayList<Node>(condxMinus);
            condxPlus.add(y);

            double xPlus = score(x, condxPlus);
            double xMinus = score(x, condxMinus);

            List<Node> neighborsy = new ArrayList<Node>();

            for (Node _node : graph.getAdjacentNodes(y)) {
                if (!knowledge.edgeForbidden(_node.getName(), y.getName())) {
                    neighborsy.add(_node);
                }
            }

//            List<Node> neighborsy = graph.getAdjacentNodes(y);
            neighborsy.remove(x);

            DepthChoiceGenerator geny = new DepthChoiceGenerator(neighborsy.size(), neighborsy.size());
            int[] choicey;

            while ((choicey = geny.next()) != null) {
                List<Node> condyMinus = GraphUtils.asList(choicey, neighborsy);

//                List<Node> parentsY = oldGraph.getParents(y);
//                parentsY.remove(x);
//                if (!condyMinus.containsAll(parentsY)) {
//                    continue;
//                }

                List<Node> condyPlus = new ArrayList<Node>(condyMinus);
                condyPlus.add(x);

                double yPlus = score(y, condyPlus);
                double yMinus = score(y, condyMinus);

                // Checking them all at once is expensive but avoids lexical ordering problems in the algorithm.
                if (normal(y, condyPlus) || normal(x, condxMinus) || normal(x, condxPlus) || normal(y, condyMinus)) {
                    continue;
                }

                boolean forbiddenLeft = knowledge.edgeForbidden(y.getName(), x.getName());
                boolean forbiddenRight = knowledge.edgeForbidden(x.getName(), y.getName());

                double delta = 0.0;

                if (strong) {
                    if (yPlus <= xPlus + delta && xMinus <= yMinus + delta) {
                        double score = combinedScore(xPlus, yMinus);

                        if ((yPlus <= yMinus + delta && xMinus <= xPlus + delta) || forbiddenRight) {
                            StringBuilder builder = new StringBuilder();

                            builder.append("\nStrong " + y + "->" + x + " " + score);
                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
                            builder.append("\n   Parents(" + y + ") = " + condyMinus);

                            scoreReports.put(-score, builder.toString());

                            if (score > max) {
                                max = score;
                                left = true;
                                right = false;
                            }
                        } else {
                            StringBuilder builder = new StringBuilder();

                            builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
                            builder.append("\n   Parents(" + y + ") = " + condyMinus);

                            scoreReports.put(-score, builder.toString());
                        }
                    } else if ((xPlus <= yPlus + delta && yMinus <= xMinus + delta) || forbiddenLeft) {
                        double score = combinedScore(yPlus, xMinus);

                        if (yMinus <= yPlus + delta && xPlus <= xMinus + delta) {
                            StringBuilder builder = new StringBuilder();

                            builder.append("\nStrong " + x + "->" + y + " " + score);
                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
                            builder.append("\n   Parents(" + y + ") = " + condyMinus);

                            scoreReports.put(-score, builder.toString());

                            if (score > max) {
                                max = score;
                                left = false;
                                right = true;
                            }
                        } else {
                            StringBuilder builder = new StringBuilder();

                            builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                            builder.append("\n   Parents(" + x + ") = " + condxMinus);
                            builder.append("\n   Parents(" + y + ") = " + condyMinus);

                            scoreReports.put(-score, builder.toString());
                        }
                    } else if (yPlus <= xPlus + delta && yMinus <= xMinus + delta) {
                        double score = combinedScore(yPlus, xMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());
                    } else if (xPlus <= yPlus + delta && xMinus <= yMinus + delta) {
                        double score = combinedScore(yPlus, xMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());
                    }
                } else {
                    if ((yPlus <= xPlus + delta && xMinus <= yMinus + delta) || forbiddenRight) {
                        double score = combinedScore(xPlus, yMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nWeak " + y + "->" + x + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());

                        if (score > max) {
                            max = score;
                            left = true;
                            right = false;
                        }
                    } else if ((xPlus <= yPlus + delta && yMinus <= xMinus + delta) || forbiddenLeft) {
                        double score = combinedScore(yPlus, xMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nWeak " + x + "->" + y + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());

                        if (score > max) {
                            max = score;
                            left = false;
                            right = true;
                        }
                    } else if (yPlus <= xPlus + delta && yMinus <= xMinus + delta) {
                        double score = combinedScore(yPlus, xMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());
                    } else if (xPlus <= yPlus + delta && xMinus <= yMinus + delta) {
                        double score = combinedScore(yPlus, xMinus);

                        StringBuilder builder = new StringBuilder();

                        builder.append("\nNo directed edge " + x + "--" + y + " " + score);
                        builder.append("\n   Parents(" + x + ") = " + condxMinus);
                        builder.append("\n   Parents(" + y + ") = " + condyMinus);

                        scoreReports.put(-score, builder.toString());
                    }
                }
            }
        }

        for (double score : scoreReports.keySet()) {
            TetradLogger.getInstance().log("info", scoreReports.get(score));
        }

        graph.removeEdges(x, y);

        if (left) {
            graph.addDirectedEdge(y, x);
        }

        if (right) {
            graph.addDirectedEdge(x, y);
        }

        if (!graph.isAdjacentTo(x, y)) {
            graph.addUndirectedEdge(x, y);
        }
    }


    private void ruleR4(Graph graph) {
        List<Edge> edgeList1 = graph.getEdges();
//        Collections.shuffle(edgeList1);

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR4(graph, x, y);
        }
    }

    private void ruleR5(Graph graph) {
        List<Edge> edgeList1 = graph.getEdges();
//        Collections.shuffle(edgeList1);

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR5(graph, x, y);
        }
    }

//    private void ruleR3(Graph graph) {
//        List<Edge> edgeList1 = graph.getEdges();
//        Collections.shuffle(edgeList1);
//
//        for (Edge adj : edgeList1) {
//            Node x = adj.getNode1();
//            Node y = adj.getNode2();
//
//            resolveOneEdgeMaxR3(graph, x, y);
//        }
//    }

    private Graph ruleR3(Graph graph) {
        List<Edge> edgeList1 = graph.getEdges();

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR3(graph, x, y);
        }


        return graph;

    }

    private Graph ruleR6(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);
        Graph oldGraph = null; // EdgeListGraph(graph);
        int i = 0;

        while (!newGraph.equals(oldGraph) && ++i <= ((int) epsilon == 0 ? 10 : (int) epsilon)) {
            oldGraph = new EdgeListGraph(newGraph);
//        for (int i = 0; i < (int) epsilon; i++) {

            List<Edge> edgeList1 = oldGraph.getEdges();

            for (Edge adj : edgeList1) {
                Node x = adj.getNode1();
                Node y = adj.getNode2();

                resolveOneEdgeMaxR6(oldGraph, newGraph, x, y);
            }

        }

        return newGraph;
    }

    private Graph ruleR7(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);
        Graph oldGraph = null; // EdgeListGraph(graph);
        int i = 0;

        while (!newGraph.equals(oldGraph) && ++i <= ((int) epsilon == 0 ? 10 : (int) epsilon)) {
            oldGraph = new EdgeListGraph(newGraph);
//        for (int i = 0; i < (int) epsilon; i++) {

            List<Edge> edgeList1 = oldGraph.getEdges();

            for (Edge adj : edgeList1) {
                Node x = adj.getNode1();
                Node y = adj.getNode2();

                resolveOneEdgeMaxR7(oldGraph, newGraph, x, y);
            }

        }

        return newGraph;

    }

    private Graph ruleR8a(Graph graph) {
        List<Edge> edgeList1 = graph.getEdges();

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR8(graph, x, y);
        }


        return graph;

    }

    private Graph ruleR8(Graph graph) {
        for (Edge adj : graph.getEdges()) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR8(graph, x, y);
        }

        Graph graph2 = new EdgeListGraph(graph);

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

//            if (graph2.getEdges(x, y).size() == 2) {

            List<Node> parentsY = graph2.getParents(y);
            parentsY.remove(x);
            parentsY.add(x);

            double[] d = maximizeNonGaussianity3(y, parentsY);
            System.out.println(edge + " " + y + " " + parentsY + " " + Arrays.toString(d));

            double a = d[d.length - 1];
            System.out.println(edge + " " + a);

            if (Math.abs(a) < epsilon) {
                System.out.println("Removing edge " + edge);
                graph.removeEdge(edge);
            }
//            }
        }

//        for (Node x : graph.getNodes()) {
//            List<Node> parents = graph.getParents(x);
//            double[] coefs = maximizeNonGaussianity3(x, parents);
//
//            System.out.println(x + " " + parents + " " + Arrays.toString(coefs));
//
//            for (int i = 0; i < parents.size(); i++) {
//                if (coefs[i] < epsilon) {
//                    Edge edge = graph.getDirectedEdge(parents.get(i), x);
//                    System.out.println("Removing " + edge);
//                    graph.removeEdge(edge);
//                }
//            }
//        }

        return graph;

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

    private boolean normal(Node node, Node... parents) {
        List<Node> _parents = new ArrayList<Node>();

        for (Node _node : parents) {
            _parents.add(_node);
        }

        return normal(node, _parents);
    }

    private boolean normal(Node node, List<Node> parents) {
        if (getAlpha() > .999) {
            return false;
        }

        return pValue(node, parents) > getAlpha();
    }


    public void setEpsilon(double epsilon) {
//        if (epsilon < 0.0) {
//            throw new IllegalArgumentException();
//        }

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

    public boolean isR3Done() {
        return r3Done;
    }

    public void setR3Done(boolean r3Done) {
        this.r3Done = r3Done;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    private enum Direction {
        left, right, bidirected, twoCycle, undirected, nonadjacent, nondirected, halfright, halfleft
    }

    private void resolveOneEdgeMaxR3(Graph graph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List<Node> condxMinus = Collections.emptyList();
        List<Node> condxPlus = Collections.singletonList(y);
        List<Node> condyMinus = Collections.emptyList();
        List<Node> condyPlus = Collections.singletonList(x);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        double deltaX = xPlus - xMinus;
        double deltaY = yPlus - yMinus;

        System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);
        System.out.println("delta X = " + deltaX + " delta Y = " + deltaY);

        graph.removeEdges(x, y);

//        if (xMinus < yMinus) {
//            newGraph.addDirectedEdge(x, y);
//        } else {
//            newGraph.addDirectedEdge(y, x);
//        }


        if (deltaX < deltaY) {
            graph.addDirectedEdge(x, y);
        } else {
            graph.addDirectedEdge(y, x);
        }

//        if (Math.abs(deltaX) < Math.abs(deltaY)) {
//            newGraph.addDirectedEdge(x, y);
//        } else {
//            newGraph.addDirectedEdge(y, x);
//        }
    }

//    private void resolveOneEdgeMaxR3(Graph graph, Node x, Node y) {
//        if (RandomUtil.getInstance().nextDouble() > 0.5) {
//            Node temp = x;
//            x = y;
//            y = temp;
//        }
//
//        System.out.println("Resolving " + x + " === " + y);
//
//        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);
//
//        SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();
//
//        Direction direction = null;
//
//        List<Node> condxMinus = new ArrayList<Node>();
//        List<Node> condxPlus = new ArrayList<Node>(condxMinus);
//        condxPlus.add(y);
//
//        double xPlus = score(x, condxPlus);
//        double xMinus = score(x, condxMinus);
//
//        List<Node> condyMinus = new ArrayList<Node>();
//        List<Node> condyPlus = new ArrayList<Node>(condyMinus);
//        condyPlus.add(x);
//
//        double yPlus = score(y, condyPlus);
//        double yMinus = score(y, condyMinus);
//
//        double xMax = xPlus > xMinus ? xPlus : xMinus;
//        double yMax = yPlus > yMinus ? yPlus : yMinus;
//
//        double score = combinedScore(xMax, yMax);
//        TetradLogger.getInstance().log("info", "Score = " + score);
//
//        if (false) { //this.score == Lofs.Score.other) {
//            boolean standardize = false;
//
//            double[] _fX = expScoreUnstandardizedSList(x, Collections.<Node>emptyList());
//            AndersonDarlingTest testX = new AndersonDarlingTest(_fX);
//            double[] sColumnX = testX.getSColumn();
//
//            double[] _fXY = expScoreUnstandardizedSList(x, Collections.singletonList(y));
//            AndersonDarlingTest testXY = new AndersonDarlingTest(_fXY);
//            double[] sColumnXY = testXY.getSColumn();
//
//            double[] _fY = expScoreUnstandardizedSList(y, Collections.<Node>emptyList());
//            AndersonDarlingTest testY = new AndersonDarlingTest(_fY);
//            double[] sColumnY = testY.getSColumn();
//
//            double[] _fYX = expScoreUnstandardizedSList(y, Collections.singletonList(x));
//            AndersonDarlingTest testYX = new AndersonDarlingTest(_fYX);
//            double[] sColumnYX = testYX.getSColumn();
//
//            double pX = dependentTTest(sColumnX, sColumnXY, 0.0);
//            double pY = dependentTTest(sColumnY, sColumnYX, 0.0);
//
//            System.out.println("pX = " + pX + " pY = " + pY);
//            System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);
//
//            Endpoint xEndpoint = null;
//
//            boolean xUnchanged = pX > alpha;
//            boolean yUnchanged = pY > alpha;
//
//            boolean xLow = xPlus < xMinus + epsilon;
//            boolean xHigh = xPlus > xMinus + epsilon;
//            boolean yLow = yPlus < yMinus + epsilon;
//            boolean yHigh = yPlus > yMinus + epsilon;
//
//            if (xUnchanged) {
//                xEndpoint = Endpoint.CIRCLE;
//            } else if (xHigh) {
//                xEndpoint = Endpoint.ARROW;
//            } else if (xLow) {
//                xEndpoint = Endpoint.TAIL;
//            }
//
//            Endpoint yEndpoint = null;
//
//            if (yUnchanged) {
//                yEndpoint = Endpoint.CIRCLE;
//            } else if (yHigh) {
//                yEndpoint = Endpoint.ARROW;
//            } else if (yLow) {
//                yEndpoint = Endpoint.TAIL;
//            }
//
//            if (xEndpoint == null || yEndpoint == null) {
//                graph.removeEdges(x, y);
//                return;
//            }
//
//            Edge edge = new Edge(x, y, xEndpoint, yEndpoint);
//
//            graph.removeEdges(x, y);
//            graph.addEdge(edge);
//
//            return;
//        }
//
//        double sum1 = xPlus + yMinus;
//        double sum2 = yPlus + xMinus;
//
//        if (sum1 > sum2) {
//            direction = Direction.left;
//        } else if (sum2 > sum1) {
//            direction = Direction.right;
//        }
//
//        System.out.println("Sum 1 = xPlus + yMinus = " + sum1 + " sum2 = yPlus + xMinus = " + sum2);
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
//        } else if (direction == Direction.undirected) {
//            graph.addUndirectedEdge(x, y);
//        } else if (direction == Direction.nondirected) {
//            graph.addNondirectedEdge(x, y);
//        }
//
//        count(xPlus, xMinus, yPlus, yMinus, x, y, direction, graph);
//    }

    private void resolveOneEdgeMaxR4(Graph graph, Node x, Node y) {

        // Hoping the data aren't already mean centered.
        boolean saveMeanCenterSetting = meanCenterResiduals;
        meanCenterResiduals = false;

//        double avgMuX = 0;
//        double avgMuY = 0;

        double[] resX = residuals(x, Collections.<Node>emptyList(), false, true);
        double[] resY = residuals(y, Collections.<Node>emptyList(), false, true);

        double muX = StatUtils.mean(resX);
        double muY = StatUtils.mean(resY);

//        for (DataSet dataSet : dataSets) {
//
//
//            int colX = dataSet.getColumn(dataSet.getVariable(x.getName()));
//            double[] _x = new double[dataSet.getNumRows()];
//            for (int i = 0; i < dataSet.getNumRows(); i++) _x[i] = dataSet.getDouble(i, colX);
//            double _muX = StatUtils.mean(_x);
//            avgMuX += _muX;
//
//            int colY = dataSet.getColumn(dataSet.getVariable(y.getName()));
//            double[] _y = new double[dataSet.getNumRows()];
//            for (int i = 0; i < dataSet.getNumRows(); i++) _y[i] = dataSet.getDouble(i, colY);
//            double _muY = StatUtils.mean(_y);
//            avgMuY += _muY;
//        }
//
//        avgMuX /= dataSets.size();
//        avgMuY /= dataSets.size();

        graph.removeEdges(x, y);

        if (muX < muY) graph.addDirectedEdge(x, y);
        else if (muY < muX) graph.addDirectedEdge(y, x);
        else graph.addUndirectedEdge(x, y);

        meanCenterResiduals = saveMeanCenterSetting;
    }

    private void resolveOneEdgeMaxR3b(Graph graph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

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

        if (xMinus > yMinus) {
            System.out.println(y + " (" + yMinus + ") ==> " + x + " (" + xMinus + ")");
            direction = Direction.left;
        } else if (yMinus > xMinus) {
            System.out.println(x + " (" + xMinus + ") ==> " + y + " (" + yMinus + ")");
            direction = Direction.right;
        } else {
            direction = Direction.twoCycle;
        }

        graph.removeEdges(x, y);

        if (direction == Direction.twoCycle) {
            graph.addDirectedEdge(x, y);
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.left) {
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.right) {
            graph.addDirectedEdge(x, y);
        }

        count(xPlus, xMinus, yPlus, yMinus, x, y, direction, graph);
    }

    private void resolveOneEdgeMaxR5(Graph graph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

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

        boolean standardize = false;

        System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);

        Endpoint xEndpoint = null;

        double epsilon = 0;

        boolean xLow = xPlus < xMinus + epsilon;
        boolean xHigh = xPlus > xMinus + epsilon;
        boolean yLow = yPlus < yMinus + epsilon;
        boolean yHigh = yPlus > yMinus + epsilon;

        if (xHigh) {
            xEndpoint = Endpoint.ARROW;
        } else if (xLow) {
            xEndpoint = Endpoint.TAIL;
        }

        Endpoint yEndpoint = null;

        if (yHigh) {
            yEndpoint = Endpoint.ARROW;
        } else if (yLow) {
            yEndpoint = Endpoint.TAIL;
        }

        if (xEndpoint == null || yEndpoint == null) {
            graph.removeEdges(x, y);
            return;
        }

        Edge edge = new Edge(x, y, xEndpoint, yEndpoint);

        graph.removeEdges(x, y);
        graph.addEdge(edge);

        return;
    }


    private void resolveOneEdgeMaxR6(Graph oldGraph, Graph newGraph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List<Node> condxMinus = pathBlockingSet(oldGraph, x, y, false);
        List<Node> condxPlus = pathBlockingSet(oldGraph, x, y, true);
        List<Node> condyMinus = pathBlockingSet(oldGraph, y, x, false);
        List<Node> condyPlus = pathBlockingSet(oldGraph, y, x, true);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);
        System.out.println("delta X = " + (xPlus - xMinus) + " delta Y = " + (yPlus - yMinus));

        Endpoint xEndpoint = null;

        boolean xLow = xPlus < xMinus;
        boolean xHigh = xPlus > xMinus;
        boolean yLow = yPlus < yMinus;
        boolean yHigh = yPlus > yMinus;

        if (xHigh) {
            xEndpoint = Endpoint.ARROW;
        } else if (xLow) {
            xEndpoint = Endpoint.TAIL;
        }

        Endpoint yEndpoint = null;

        if (yHigh) {
            yEndpoint = Endpoint.ARROW;
        } else if (yLow) {
            yEndpoint = Endpoint.TAIL;
        }

        if (xEndpoint == null || yEndpoint == null) {
            newGraph.removeEdges(x, y);
            return;
        }

        Edge edge = new Edge(x, y, xEndpoint, yEndpoint);

        newGraph.removeEdges(x, y);
        newGraph.addEdge(edge);

        return;
    }

    private void resolveOneEdgeMaxR7(Graph oldGraph, Graph newGraph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List<Node> condxMinus = pathBlockingSet(oldGraph, x, y, false);
        List<Node> condxPlus = pathBlockingSet(oldGraph, x, y, true);
        List<Node> condyMinus = pathBlockingSet(oldGraph, y, x, false);
        List<Node> condyPlus = pathBlockingSet(oldGraph, y, x, true);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        double deltaX = xPlus - xMinus;
        double deltaY = yPlus - yMinus;

        System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);
        System.out.println("delta X = " + deltaX + " delta Y = " + deltaY);

        newGraph.removeEdges(x, y);

//        if (xMinus < yMinus) {
//            newGraph.addDirectedEdge(x, y);
//        } else {
//            newGraph.addDirectedEdge(y, x);
//        }


        if (deltaX < deltaY) {
            newGraph.addDirectedEdge(x, y);
        } else {
            newGraph.addDirectedEdge(y, x);
        }

//        if (Math.abs(deltaX) < Math.abs(deltaY)) {
//            newGraph.addDirectedEdge(x, y);
//        } else {
//            newGraph.addDirectedEdge(y, x);
//        }
    }

    private void resolveOneEdgeMaxR8(Graph graph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        double a = maximizeNonGaussianity(x, y);
        double b = maximizeNonGaussianity(y, x);

        System.out.println("a = " + a + " b = " + b);

        graph.removeEdges(x, y);

        if (isMeanCenterResiduals()) {
            if (Math.abs(b) >= Math.abs(a) + epsilon) {
                graph.addDirectedEdge(x, y);
            } else if (Math.abs(a) >= Math.abs(b) + epsilon) {
                graph.addDirectedEdge(y, x);
            } else {
                graph.addUndirectedEdge(x, y);
            }
        } else {
            if (Math.abs(b) >= epsilon) {
                System.out.println("Adding edge " + x + "-->" + y);
                graph.addDirectedEdge(x, y);
            }

            if (Math.abs(a) >= epsilon) {
                System.out.println("Adding edge " + y + "-->" + x);
                graph.addDirectedEdge(y, x);
            }
        }
    }

    private double maximizeNonGaussianity1a(Node x, Node y) {

        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < dataSets.size(); i++) {
            DataSet dataSet = dataSets.get(i);

            double maxV = 0.0;
            double _a = Double.NaN;

            for (double a = 0.0; a < 2.0; a += 0.01) {
                double v = ng(dataSet, x, y, a);

                System.out.println(a + "\t" + v);

                if (v > maxV) {
                    maxV = v;
                    _a = a;
                }

                if (v < maxV * getAlpha()) break;
            }

            if (Double.isNaN(_a)) {
                continue;
            }

            System.out.println("_a = " + _a);

            sum += _a;
            count++;
        }

        double avg = sum / count;

        System.out.println("Avg _a = " + avg);

        return avg;
    }

    private double maximizeNonGaussianity1b(Node x, Node y) {

        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double maxV = 0.0;
        double _a = Double.NaN;

//        for (double a = 0.0; a < 2.0; a += 0.04) {
//            double v = ng2(dataSets, dataSetMatrices, x, y, a);
//            System.out.println(a + "\t" + v);
//        }

        for (double a = 0.0; a < 2.0; a += 0.04) {
            double v = ng2(dataSets, dataSetMatrices, x, y, a);

            if (v > maxV) {
                maxV = v;
                _a = a;
            }

            if (v < maxV * getAlpha()) break;
        }

        System.out.println("_a = " + _a);

        return _a;
    }

    private double maximizeNonGaussianity1c(Node x, Node y) {

        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 50;

        double[] data = new double[numIntervals + 1];
        int[] counts = new int[numIntervals + 1];

        for (int s = 0; s <= numIntervals; s++) {
            for (DataSet dataSet : dataSets) {
                double a = min + s * ((max - min) / numIntervals);
                double v = ng(dataSet, x, y, a);

                if (!Double.isNaN(v)) {
                    data[s] += v;
                    counts[s]++;
                }
            }
        }

        for (int s = 0; s <= numIntervals; s++) {
            data[s] /= counts[s];
        }

        double maxV = 0.0;
        double _a = Double.NaN;

        for (int s = 0; s <= numIntervals; s++) {
            double a = min + s * ((max - min) / numIntervals);
            double v = data[s];

            System.out.println(a + "\t" + v);

            if (v > maxV) {
                maxV = v;
                _a = a;
            }

            if (v < maxV * getAlpha()) break;
        }

        System.out.println("_a = " + _a);

        return _a;
    }

    private double maximizeNonGaussianity1d(Node x, Node y) {

//        System.out.println("START");

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < dataSets.size(); i++) {
            DataSet dataSet = dataSets.get(i);

            double minV = Double.POSITIVE_INFINITY;
            double _a = Double.NaN;

            for (double a = 0.00; a < 1.0; a += 0.05) {
                double v = ng(dataSet, x, y, a);

                System.out.println(a + "\t" + v);

                if (v < minV) {
                    minV = v;
                    _a = a;
                }

//                if (v > minV + 0.1) break;
            }

            if (Double.isNaN(_a)) {
                continue;
            }

            System.out.println("_a = " + _a);

            sum += _a;
            count++;
        }

        double avg = sum / count;
        return avg;
    }

    private double maximizeNonGaussianity2(Node x, Node y) {

        // Want the peak before the last trough.

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < dataSets.size(); i++) {
            DataSet dataSet = dataSets.get(i);

            double penultimatePeak = Double.NaN;
            double lastPeak = Double.NaN;

            boolean ascending = true;

            double last = ng(dataSet, x, y, 0.0);

            for (double a = 0.0; a < 2.0; a += 0.05) {
                double v = ng(dataSet, x, y, a);

                System.out.println(a + "\t" + v);

                if (!ascending && v > last) {

                    // low point.

                    ascending = true;
                    penultimatePeak = lastPeak;
                } else if (ascending && v < last) {

                    // high point

                    ascending = false;
                    lastPeak = a;
                }

                last = v;
            }

//            if (Double.isNaN(penultimatePeak)) penultimatePeak = lastPeak;

            System.out.println("penultimate peak = " + penultimatePeak);

            if (Double.isNaN(penultimatePeak)) {
                continue;
            }

            sum += penultimatePeak;
            count++;
        }

        double avg = sum / count;
        return avg;
    }

    // Leaving the extremes out, report the biggest bump.

    private double maximizeNonGaussianity3(Node x, Node y) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double sum = 0.0;
        int count = 0;

        double min = 0.0;
        double max = 1.0;
        int numIntervals = 50;

        for (int i = 0; i < dataSets.size(); i++) {
            DataSet dataSet = dataSets.get(i);

            double greatestBump = 0.0;
            double greatestBumpValue = ng(dataSet, x, y, 0.0);
            boolean ascending = false;
            double lastA = Double.NaN;
            double lastValue = ng(dataSet, x, y, min);

            double bumpFound = Double.NaN;
            double bumpFoundValue = Double.NaN;

            for (int s = 0; s <= numIntervals; s++) {
                double a = min + s * ((max - min) / numIntervals);

                double v = ng(dataSet, x, y, a);

                System.out.println(a + "\t" + v);

                if (!ascending && v > lastValue) {
                    ascending = true;
                } else if (ascending && v < lastValue) {
                    ascending = false;

                    bumpFound = lastA;
                    bumpFoundValue = lastValue;
//                    }
                } else if (!ascending && v < lastValue) {
                    if (v < bumpFoundValue * alpha) {
                        if (bumpFoundValue > greatestBumpValue) {
                            greatestBump = bumpFound;
                            greatestBumpValue = bumpFoundValue;
                            bumpFound = Double.NaN;
                            bumpFoundValue = Double.NaN;
                        }
                    }
                }

                lastA = a;
                lastValue = v;
            }

            System.out.println("greatest bump = " + greatestBump);

            if (Double.isNaN(greatestBump)) {
                continue;
            }

            sum += greatestBump;
            count++;
        }

        double avg = sum / count;
        return avg;
    }

    private double maximizeNonGaussianity4(Node x, Node y) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 4.0;
        int numIntervals = 100;

        double[] data = new double[numIntervals + 1];
        int[] counts = new int[numIntervals + 1];

        for (int s = 0; s <= numIntervals; s++) {
            for (DataSet dataSet : dataSets) {
                double a = min + s * ((max - min) / numIntervals);
                double v = ng(dataSet, x, y, a);

                if (!Double.isNaN(v)) {
                    data[s] += v;
                    counts[s]++;
                }
            }
        }

        for (int s = 0; s <= numIntervals; s++) {
            data[s] /= counts[s];
        }

        double greatestBump = Double.NaN;
        double greatestBumpValue = Double.NaN;
        boolean ascending = false;
        double lastA = Double.NaN;
        double lastValue = data[0];

        double bumpFound = Double.NaN;
        double bumpFoundValue = Double.NaN;

        for (int s = 0; s <= numIntervals; s++) {
            double a = min + s * ((max - min) / numIntervals);
            double v = data[s];

            System.out.println(a + "\t" + v);

            if (!ascending && v > lastValue) {
                ascending = true;
            } else if (ascending && v < lastValue) {
                ascending = false;
                bumpFound = lastA;
                bumpFoundValue = lastValue;
            } else if (!ascending && v < lastValue) {
                if (v < bumpFoundValue * getAlpha()) {
                    if (bumpFoundValue > greatestBumpValue || Double.isNaN(greatestBumpValue)) {
                        greatestBump = bumpFound;
                        greatestBumpValue = bumpFoundValue;
                        bumpFound = Double.NaN;
                        bumpFoundValue = Double.NaN;
                    }
                }
            }

            lastA = a;
            lastValue = v;
        }

        if (Double.isNaN(greatestBump)) {
            greatestBump = 0.0;
        }

        System.out.println("greatest bump = " + greatestBump);

        return greatestBump;
    }

    // First bump, or zero.

    private double maximizeNonGaussianity5(Node x, Node y) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 50;

        double[] data = new double[numIntervals + 1];
        int[] counts = new int[numIntervals + 1];

        for (int s = 0; s <= numIntervals; s++) {
            for (DataSet dataSet : dataSets) {
                double a = min + s * ((max - min) / numIntervals);
                double v = ng(dataSet, x, y, a);

                if (!Double.isNaN(v)) {
                    data[s] += v;
                    counts[s]++;
                }
            }
        }

        for (int s = 0; s <= numIntervals; s++) {
            data[s] /= counts[s];
        }

        double firstBump = Double.NaN;
        double firstBumpValue = Double.NaN;
        boolean ascending = false;
        double lastA = Double.NaN;
        double lastValue = data[0];

        double bumpFound = Double.NaN;
        double bumpFoundValue = Double.NaN;

        for (int s = 0; s <= numIntervals; s++) {
            double a = min + s * ((max - min) / numIntervals);
            double v = data[s];

            System.out.println(a + "\t" + v);

            if (!ascending && v > lastValue) {
                ascending = true;
            } else if (ascending && v < lastValue) {
                ascending = false;
                bumpFound = lastA;
                bumpFoundValue = lastValue;
            } else if (!ascending && v < lastValue) {
                if (v < bumpFoundValue * getAlpha()) {
                    if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                        firstBump = bumpFound;
                        break;
                    }
                }
            }

            lastA = a;
            lastValue = v;
        }

        if (Double.isNaN(firstBump)) {
            firstBump = 0.0;
        }

        System.out.println("first bump = " + firstBump);

        return firstBump;
    }

    private double maximizeNonGaussianity6(Node x, Node y) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 50;

        double sum = 0.0;
        int count = 0;

        for (DataSet dataSet : dataSets) {
            double firstBump = Double.NaN;
            double firstBumpValue = Double.NaN;
            boolean ascending = false;
            double lastA = Double.NaN;
            double lastValue = ng(dataSet, x, y, min);

            double bumpFound = Double.NaN;
            double bumpFoundValue = Double.NaN;

            for (int s = 0; s <= numIntervals; s++) {
                double a = min + s * ((max - min) / numIntervals);
                double v = ng(dataSet, x, y, a);

                System.out.println(a + "\t" + v);

                if (!ascending && v > lastValue) {
                    ascending = true;
                } else if (ascending && v < lastValue) {
                    ascending = false;
                    bumpFound = lastA;
                    bumpFoundValue = lastValue;
                } else if (!ascending && v < lastValue) {
                    if (v < bumpFoundValue * getAlpha()) {
                        if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                            firstBump = bumpFound;
                            break;
                        }
                    }
                }

                lastA = a;
                lastValue = v;

            }

            if (Double.isNaN(firstBump)) {
                firstBump = 0.0;
            }

            System.out.println("first bump = " + firstBump);

            sum += firstBump;
            count++;
        }

        double avg = sum / count;
        return avg;
    }

    private double maximizeNonGaussianity(Node x, Node y) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 1.0;
        int numIntervals = 50;

        double firstBump = Double.NaN;
        double firstBumpValue = Double.NaN;
        boolean ascending = false;
        double lastA = Double.NaN;
        double lastValue = ng2(dataSets, dataSetMatrices, x, y, min);

        double bumpFound = Double.NaN;
        double bumpFoundValue = Double.NaN;

        for (int s = 0; s <= numIntervals; s++) {
            double a = min + s * ((max - min) / numIntervals);
            double v = ng2(dataSets, dataSetMatrices, x, y, a);

//            System.out.println(a + "\t" + v);

            if (!ascending && v > lastValue) {
                ascending = true;
            } else if (ascending && v < lastValue) {
                ascending = false;
                bumpFound = lastA;
                bumpFoundValue = lastValue;
            } else if (!ascending && v < lastValue) {
                if (v < bumpFoundValue * getAlpha()) {
                    if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                        firstBump = bumpFound;
                        break;
                    }
                }
            }

            lastA = a;
            lastValue = v;
        }

        if (Double.isNaN(firstBump)) {
            firstBump = 0.0;
        }

        System.out.println("first bump = " + firstBump);

        return firstBump;
    }

    // Optimizes non-Gaussianity of x given y, subracting out the influence of the other parents of y.

    private double maximizeNonGaussianity(Node x, Node y, List<Node> parents) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 40;

        LinkedList<Node> nodes = new LinkedList<Node>();
        LinkedList<Double> coefs = new LinkedList<Double>();

        for (int i = 0; i < parents.size(); i++) {
            Node g = parents.get(i);

            double firstBump = Double.NaN;
            double firstBumpValue = Double.NaN;
            boolean ascending = false;
            double lastA = Double.NaN;
            double lastValue = ng2(dataSets, dataSetMatrices, x, y, min);

            double bumpFound = Double.NaN;
            double bumpFoundValue = Double.NaN;

            for (int s = 0; s <= numIntervals; s++) {
                double a = min + s * ((max - min) / numIntervals);

                double v = ng2(dataSets, dataSetMatrices, x, parents.get(i), a);

//                System.out.println(a + "\t" + v);

                if (Double.isNaN(v)) {
                    continue;
                }

                if (!ascending && v > lastValue) {
                    ascending = true;
                } else if (ascending && v < lastValue) {
                    ascending = false;
                    bumpFound = lastA;
                    bumpFoundValue = lastValue;
                } else if (!ascending && v < lastValue) {
                    if (v < bumpFoundValue * getAlpha()) {
                        if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                            firstBump = bumpFound;
                            break;
                        }
                    }
                }

                lastA = a;
                lastValue = v;
            }

            if (Double.isNaN(firstBump)) {
                firstBump = 0.0;
            }

            System.out.println("first bump = " + firstBump);

            nodes.addLast(g);
            coefs.addLast(firstBump);
        }

        nodes.addLast(y);
        coefs.addLast(0.0);

        {
            double firstBump = Double.NaN;
            double firstBumpValue = Double.NaN;
            boolean ascending = false;
            double lastA = Double.NaN;
            double lastValue = ng2(dataSets, dataSetMatrices, x, y, min);

            double bumpFound = Double.NaN;
            double bumpFoundValue = Double.NaN;

            for (int s = 0; s <= numIntervals; s++) {
                double a = min + s * ((max - min) / numIntervals);

                coefs.removeLast();
                coefs.addLast(a);

                double[] coefs2 = new double[coefs.size()];

                for (int h = 0; h < coefs.size(); h++) coefs2[h] = coefs.get(h);

                double v = ng3(dataSets, dataSetMatrices, x, nodes, coefs2);

//                System.out.println(a + "\t" + v);

                if (Double.isNaN(v)) {
                    continue;
                }

                if (!ascending && v > lastValue) {
                    ascending = true;
                } else if (ascending && v < lastValue) {
                    ascending = false;
                    bumpFound = lastA;
                    bumpFoundValue = lastValue;
                } else if (!ascending && v < lastValue) {
                    if (v < bumpFoundValue * getAlpha()) {
                        if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                            firstBump = bumpFound;
                            break;
                        }
                    }
                }

                lastA = a;
                lastValue = v;
            }

            if (Double.isNaN(firstBump)) {
                firstBump = 0.0;
            }

            System.out.println("first bump = " + coefs.getLast());

            return firstBump;
        }
    }

    private double maximizeNonGaussianity2(Node x, Node y, List<Node> parents) {
        System.out.println("Maximizing non-Gaussianity for " + x + " given " + y);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 40;

        List<Node> nodes = new ArrayList<Node>(parents);
        nodes.add(y);
        double[] coef = new double[nodes.size()];
        double[] coef2 = new double[nodes.size()];

        int count = 0;

        do {
            System.arraycopy(coef, 0, coef2, 0, coef.length);

            for (int i = 0; i < nodes.size(); i++) {
                double firstBump = Double.NaN;
                double firstBumpValue = Double.NaN;
                boolean ascending = false;
                double lastA = Double.NaN;
                double lastValue = ng2(dataSets, dataSetMatrices, x, y, min);

                double bumpFound = Double.NaN;
                double bumpFoundValue = Double.NaN;

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng3(dataSets, dataSetMatrices, x, nodes, coef);

//                System.out.println(a + "\t" + v);

                    if (Double.isNaN(v)) {
                        continue;
                    }

                    if (!ascending && v > lastValue) {
                        ascending = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;
                        bumpFound = lastA;
                        bumpFoundValue = lastValue;
                    } else if (!ascending && v < lastValue) {
                        if (v < bumpFoundValue * getAlpha()) {
                            if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                                firstBump = bumpFound;
                                break;
                            }
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }

                if (Double.isNaN(firstBump)) {
                    firstBump = 0.0;
                }

                System.out.println("first bump = " + firstBump);

                coef[i] = firstBump;
            }
        } while (++count <= 3);
//    } while (!(distance(coef, coef2) < 0.01));

        return coef[nodes.size() - 1];
    }

    private double distance(double[] d1, double[] d2) {
        double sum = 0.0;

        for (int i = 0; i < d1.length; i++) {
            sum += Math.pow(d1[i] - d2[i], 2.0);
        }

        return Math.sqrt(sum);
    }

    private double[] maximizeNonGaussianity3(Node x, List<Node> parents) {
        System.out.println("Maximizing non-Gaussianity for " + parents + " given " + x);

        double min = 0.0;
        double max = 2.0;
        int numIntervals = 40;

        double[] coef = new double[parents.size()];
        double[] coef2 = new double[parents.size()];

        int count = 0;

        do {
            System.arraycopy(coef, 0, coef2, 0, coef.length);

            for (int i = 0; i < parents.size(); i++) {
                double firstBump = Double.NaN;
                double firstBumpValue = Double.NaN;
                boolean ascending = false;
                double lastA = Double.NaN;
                double lastValue = Double.NaN;

                double bumpFound = Double.NaN;
                double bumpFoundValue = Double.NaN;

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng3(dataSets, dataSetMatrices, x, parents, coef);

//                System.out.println(a + "\t" + v);

                    if (Double.isNaN(v)) {
                        continue;
                    }

                    if (!ascending && v > lastValue) {
                        ascending = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;
                        bumpFound = lastA;
                        bumpFoundValue = lastValue;
                    } else if (!ascending && v < lastValue) {
                        if (v < bumpFoundValue * getAlpha()) {
                            if (bumpFoundValue > firstBumpValue || Double.isNaN(firstBumpValue)) {
                                firstBump = bumpFound;
                                break;
                            }
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }

                if (Double.isNaN(firstBump)) {
                    firstBump = 0.0;
                }

                System.out.println("first bump = " + firstBump);

                coef[i] = firstBump;
            }
        } while (++count <= 5);
//    } while (!(distance(coef, coef2) < 0.01));

        return coef;
    }

    public double ng(DataSet dataSet, Node x, Node y, double a) {
        int _x = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
        int _y = dataSets.get(0).getColumn(dataSet.getVariable(y.getName()));

        DoubleMatrix2D data = dataSet.getDoubleData();

        int rows[] = new int[data.rows()];
        for (int i = 0; i < rows.length; i++) rows[i] = i;

        DoubleMatrix1D __x = data.viewSelection(rows, new int[]{_x}).viewColumn(0).copy();
        DoubleMatrix1D __y = data.viewSelection(rows, new int[]{_y}).viewColumn(0).copy();

        for (int i = 0; i < __x.size(); i++) {
            __x.set(i, __x.get(i) - a * __y.get(i));
        }

        double stat = aSquared(__x.toArray());
        return stat;
    }

    public double ng2(List<DataSet> dataSets, List<DoubleMatrix2D> dataSetMatrices, Node x, Node y, double a) {
        List<Double> _x = new ArrayList<Double>();
        List<Double> _y = new ArrayList<Double>();

        for (int k = 0; k < dataSets.size(); k++) {
            DataSet dataSet = dataSets.get(k);
            DoubleMatrix2D matrix = dataSetMatrices.get(k);

            int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
            int yColumn = dataSets.get(0).getColumn(dataSet.getVariable(y.getName()));

            int rows[] = new int[matrix.rows()];
            for (int i = 0; i < rows.length; i++) rows[i] = i;

            DoubleMatrix1D __x = matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0).copy();
            DoubleMatrix1D __y = matrix.viewSelection(rows, new int[]{yColumn}).viewColumn(0).copy();

            for (int i = 0; i < __x.size(); i++) {
                if (!Double.isNaN(__x.get(i)) && !Double.isNaN(__y.get(i))) {
                    _x.add(__x.get(i));
                    _y.add(__y.get(i));
                }
            }
        }

        for (int i = 0; i < _x.size(); i++) {
            _x.set(i, _x.get(i) - a * _y.get(i));
        }

        double[] __x = new double[_x.size()];

        for (int i = 0; i < _x.size(); i++) __x[i] = _x.get(i);

        double stat = aSquared(__x);
        return stat;
    }

    public double ng3(List<DataSet> dataSets, List<DoubleMatrix2D> dataSetMatrices, Node x, List<Node> parents, double[] coefs) {
        List<Double> _x = new ArrayList<Double>();
        List<List<Double>> _parents = new ArrayList<List<Double>>();

        for (int i = 0; i < parents.size(); i++) {
            _parents.add(new ArrayList<Double>());
        }

        for (int k = 0; k < dataSets.size(); k++) {
            DataSet dataSet = dataSets.get(k);
            DoubleMatrix2D matrix = dataSetMatrices.get(k);

            int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));

            int[] parentsColumns = new int[parents.size()];

            for (int i = 0; i < parents.size(); i++) {
                parentsColumns[i] = dataSets.get(0).getColumn(dataSet.getVariable(parents.get(i).getName()));
            }

            int rows[] = new int[matrix.rows()];
            for (int i = 0; i < rows.length; i++) rows[i] = i;

            DoubleMatrix1D __x = matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0).copy();

            List<DoubleMatrix1D> __parents = new ArrayList<DoubleMatrix1D>();

            for (int i = 0; i < parents.size(); i++) {
                __parents.add(matrix.viewSelection(rows, new int[]{parentsColumns[i]}).viewColumn(0).copy());
            }

            for (int i = 0; i < __x.size(); i++) {
                if (Double.isNaN(__x.get(i))) continue;

                for (int j = 0; j < parents.size(); j++) {
                    if (Double.isNaN(__parents.get(j).get(i))) {
                        continue;
                    }
                }

                _x.add(__x.get(i));

                for (int j = 0; j < parents.size(); j++) {
                    _parents.get(j).add(__parents.get(j).get(i));
                }
            }
        }

        for (int i = 0; i < _x.size(); i++) {
            double d = _x.get(i);

            for (int j = 0; j < parents.size(); j++) {
                d -= coefs[j] * _parents.get(j).get(i);
            }

            _x.set(i, d);
        }

        double[] __x = new double[_x.size()];

        for (int i = 0; i < _x.size(); i++) __x[i] = _x.get(i);

        double stat = aSquared(__x);
        return stat;
    }


    private double aSquared(double[] __x) {
        double stat = new AndersonDarlingTest(__x).getASquared();
        return stat;
    }

    private static double ng2(double[] x) {
        double[] _x = new double[x.length];

//        for (int k = 0; k < x.length; k++) {
//            double v = Math.log(Math.cosh((x[k])));
//            _x[k] = v;
//        }
//
//        double expected = StatUtils.mean(_x);
//        double diff = expected - logCoshExp;
//        double score = diff * diff;
//        return score;

        for (int k = 0; k < x.length; k++) {
            _x[k] = Math.exp(x[k]);
        }

        double expected = StatUtils.mean(_x);
//        return expected;
        double logExpected = Math.log(expected);
        double diff = logExpected - 0.5;
        return Math.abs(diff);

    }

    private List<Node> pathBlockingSet(Graph graph, Node x, Node y, boolean includeY) {
        return adjacencySet(graph, x, y, includeY);

//        List<Node> condSet = new LinkedList<Node>();
//
//        Set<Node> adj = new HashSet<Node>();
//        adj.addAll(graph.getAdjacentNodes(x));
//        adj.addAll(graph.getAdjacentNodes(y));
//
////        for (Node b : graph.getAdjacentNodes(x)) {
////            if ((graph.isParentOf(b, x) && !graph.isChildOf(b, y))
////                    || (graph.isParentOf(b, y) && !graph.isChildOf(b, y))) {
////                condSet.add(b);
////            }
////        }
//
////        for (Node b : graph.getAdjacentNodes(x)) {
////            if (graph.isParentOf(b, x)
////                    || graph.isParentOf(b, y)) {
////                condSet.add(b);
////            }
////        }
//
////        condSet = new ArrayList<Node>(adj);
//
//
//        for (Node b : graph.getAdjacentNodes(x)) {
//            if (!condSet.contains(b) && y != b && graph.getAdjacentNodes(b).size() > 1) {
//                condSet.add(b);
//            }
//
//            for (Node c : graph.getAdjacentNodes(b)) {
//                if (c == b) continue;
//
//                if ((graph.getEndpoint(x, b) == Endpoint.ARROW && graph.getEndpoint(c, b) == Endpoint.ARROW)
//                        && graph.getEndpoint(b, c) != Endpoint.ARROW
//                        && !condSet.contains(c)
//                        && graph.getAdjacentNodes(c).size() > 1) {
//                    condSet.add(c);
//                }
//            }
//        }
//
////        for (Node b : graph.getAdjacentNodes(x)) {
////            if (!condSet.contains(b) && graph.isParentOf(x, b) && !graph.isChildOf(b, y)) {
////                condSet.add(b);
////            }
////        }
//
//        condSet.remove(x);
//        condSet.remove(y);
//
//        if (includeY) {
//            condSet.add(y);
//        }
//
//        return condSet;
    }

    private List<Node> adjacencySet(Graph graph, Node x, Node y, boolean includeY) {
        Set<Node> adj = new HashSet<Node>(graph.getAdjacentNodes(x));
        adj.addAll(graph.getAdjacentNodes(y));

        adj.remove(x);
        adj.remove(y);

        if (includeY) {
            adj.add(y);
        }

        return new ArrayList<Node>(adj);
    }

    private Graph search2(List<Node> nodes) {
        Graph graph = new EdgeListGraph(nodes);

        for (Node y : nodes) {
            for (Node x : nodes) {
                if (y == x) continue;

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

                if (xPlus + epsilon < xMinus && yPlus > yMinus + epsilon) {
                    graph.addDirectedEdge(x, y);
                }
            }
        }

//        search2AtDepth(nodes, graph, 0);
//        search2AtDepth(nodes, graph, 1);
//        search2AtDepth(nodes, graph, 2);
//        search2AtDepth(nodes, graph, 3);
//        search2AtDepth(nodes, graph, 4);

        return graph;
    }

    private void search2AtDepth(List<Node> nodes, Graph graph, int depth) {
        for (Node y : nodes) {
            List<Node> parentsy = graph.getParents(y);

            EDGE:
            for (Node x : parentsy) {
                List<Node> _parentsy = new LinkedList<Node>(parentsy);
                _parentsy.remove(x);

                if (_parentsy.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(_parentsy.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, _parentsy);

                        List<Node> condyMinus = new ArrayList<Node>(condSet);
                        List<Node> condyPlus = new ArrayList<Node>(condyMinus);
                        condyPlus.add(x);

                        double yPlus = score(y, condyPlus);
                        double yMinus = score(y, condyMinus);

                        if (yPlus < yMinus) {
                            graph.removeEdges(y, x);
                            continue EDGE;
                        }
                    }
                }
            }
        }
    }

    private Graph searchLikePc(Graph graph, int depth) {
        List<Edge> edges = graph.getEdges();

        for (Edge _edge : edges) {
            String name1 = _edge.getNode1().getName();
            String name2 = _edge.getNode2().getName();

            if (knowledge.edgeForbidden(name1, name2) &&
                    knowledge.edgeForbidden(name2, name1)) {
                graph.removeEdge(_edge);
            }
        }

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }


        Map<Node, Set<Node>> adjacencies = new HashMap<Node, Set<Node>>();
        List<Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            adjacencies.put(node, new HashSet<Node>());
        }

        for (int d = 0; d <= _depth; d++) {
//            System.out.println("Depth " + d);

            boolean more;

            if (d == 0) {
                more = searchAtDepth0(nodes, adjacencies);
            } else {
                more = searchAtDepth(nodes, adjacencies, d);
            }

            if (!more) {
                break;
            }
        }

//        System.out.println("Adding edges to graph.");

        graph.removeEdges(graph.getEdges());

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                if (adjacencies.get(x).contains(y)) {
                    graph.addUndirectedEdge(x, y);
                }
            }
        }

        return graph;
    }

    private boolean searchAtDepth0(List<Node> nodes, Map<Node, Set<Node>> adjacencies) {
        List<Node> empty = Collections.emptyList();
        int removed = 0;

        for (int i = 0; i < nodes.size(); i++) {
//            if (i < 10) continue;

            Node x = nodes.get(i);
//            if ((i + 1) % 100 == 0) System.out.println("count " + (i + 1) + " of " + nodes.size() + " depth 0" + "**");

            for (int j = i + 1; j < nodes.size(); j++) {
                Node y = nodes.get(j);

                boolean independent;

                try {
                    independent = isIndependent(x, y, empty);
                } catch (Exception e) {
                    e.printStackTrace();
                    independent = false;
                }

                boolean noEdgeRequired =
                        knowledge.noEdgeRequired(x.getName(), y.getName());

                if (independent && noEdgeRequired) {
                    removed++;
                } else {
                    adjacencies.get(x).add(y);
                    adjacencies.get(y).add(x);
                }
            }
        }

//        System.out.println("Removed " +  removed);

        return true;
    }

    private boolean searchAtDepth(List<Node> nodes, Map<Node, Set<Node>> adjacencies, int depth) {
        int numRemoved = 0;
        int count = 0;

        for (Node x : nodes) {
//            if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());

            List<Node> adjx = new ArrayList<Node>(adjacencies.get(x));

            EDGE:
            for (Node y : adjx) {
                List<Node> _adjx = new ArrayList<Node>(adjacencies.get(x));
                _adjx.remove(y);

                if (_adjx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(_adjx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, _adjx);

                        boolean independent;

                        try {
                            independent = isIndependent(x, y, condSet);
                        } catch (Exception e) {
                            independent = false;
                        }

                        boolean noEdgeRequired =
                                knowledge.noEdgeRequired(x.getName(), y.getName());

                        if (independent && noEdgeRequired) {
                            adjacencies.get(x).remove(y);
                            adjacencies.get(y).remove(x);
                            numRemoved++;

                            continue EDGE;
                        }
                    }
                }
            }
        }

//        System.out.println("Num removed = " + numRemoved);
        return numRemoved > 0;
    }

    private void resolveOneEdgeMaxR6a(Graph graph, Node x, Node y) {
        if (RandomUtil.getInstance().nextDouble() > 0.5) {
            Node temp = x;
            x = y;
            y = temp;
        }

        System.out.println("Resolving " + x + " === " + y);

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

        if (false) { //this.score == Lofs.Score.other) {
            boolean standardize = false;

            double[] _fX = expScoreUnstandardizedSList(x, Collections.<Node>emptyList());
            AndersonDarlingTest testX = new AndersonDarlingTest(_fX);
            double[] sColumnX = testX.getSColumn();

            double[] _fXY = expScoreUnstandardizedSList(x, Collections.singletonList(y));
            AndersonDarlingTest testXY = new AndersonDarlingTest(_fXY);
            double[] sColumnXY = testXY.getSColumn();

            double[] _fY = expScoreUnstandardizedSList(y, Collections.<Node>emptyList());
            AndersonDarlingTest testY = new AndersonDarlingTest(_fY);
            double[] sColumnY = testY.getSColumn();

            double[] _fYX = expScoreUnstandardizedSList(y, Collections.singletonList(x));
            AndersonDarlingTest testYX = new AndersonDarlingTest(_fYX);
            double[] sColumnYX = testYX.getSColumn();

            double pX = dependentTTest(sColumnX, sColumnXY, 0.0);
            double pY = dependentTTest(sColumnY, sColumnYX, 0.0);

            System.out.println("pX = " + pX + " pY = " + pY);

            List<Node> adjX = graph.getAdjacentNodes(x);
            adjX.remove(x);

            List<Node> adjY = graph.getAdjacentNodes(y);
            adjY.remove(y);

            List<Node> adjXPlus = new ArrayList<Node>(adjX);
            adjXPlus.add(y);

            List<Node> adjYPlus = new ArrayList<Node>(adjY);
            adjYPlus.add(x);

            xMinus = expScoreUnstandardized(x, adjX);
            xPlus = expScoreUnstandardized(x, adjXPlus);
            yMinus = expScoreUnstandardized(y, adjY);
            yPlus = expScoreUnstandardized(y, adjYPlus);

//            xPlus = new AndersonDarlingTest(_fXY).getASquaredStar();
//            xMinus = new AndersonDarlingTest(_fX).getASquaredStar();
//            yPlus = new AndersonDarlingTest(_fYX).getASquaredStar();
//            yMinus = new AndersonDarlingTest(_fY).getASquaredStar();

            System.out.println("X- = " + xMinus + " X+ = " + xPlus + " Y- = " + yMinus + " Y+ = " + yPlus);

            Endpoint xEndpoint = null;

            boolean xUnchanged = pX > alpha;
            boolean yUnchanged = pY > alpha;

            boolean xLow = xPlus < xMinus + epsilon;
            boolean xHigh = xPlus > xMinus + epsilon;
            boolean yLow = yPlus < yMinus + epsilon;
            boolean yHigh = yPlus > yMinus + epsilon;

            if (xUnchanged) {
                xEndpoint = Endpoint.CIRCLE;
            } else if (xHigh) {
                xEndpoint = Endpoint.ARROW;
            } else if (xLow) {
                xEndpoint = Endpoint.TAIL;
            }

            Endpoint yEndpoint = null;

            if (yUnchanged) {
                yEndpoint = Endpoint.CIRCLE;
            } else if (yHigh) {
                yEndpoint = Endpoint.ARROW;
            } else if (yLow) {
                yEndpoint = Endpoint.TAIL;
            }

            if (xEndpoint == null || yEndpoint == null) {
                graph.removeEdges(x, y);
                return;
            }

            Edge edge = new Edge(x, y, xEndpoint, yEndpoint);

            graph.removeEdges(x, y);
            graph.addEdge(edge);

            return;
        }

        double sum1 = xPlus + yMinus;
        double sum2 = yPlus + xMinus;

        if (sum1 > sum2) {
            direction = Direction.left;
        } else if (sum2 > sum1) {
            direction = Direction.right;
        }

        System.out.println("Sum 1 = xPlus + yMinus = " + sum1 + " sum2 = yPlus + xMinus = " + sum2);

        graph.removeEdges(x, y);

        if (direction == Direction.bidirected) {
            graph.addBidirectedEdge(x, y);
        } else if (direction == Direction.twoCycle) {
            graph.addDirectedEdge(x, y);
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.left) {
            graph.addDirectedEdge(y, x);
        } else if (direction == Direction.right) {
            graph.addDirectedEdge(x, y);
        } else if (direction == Direction.undirected) {
            graph.addUndirectedEdge(x, y);
        } else if (direction == Direction.nondirected) {
            graph.addNondirectedEdge(x, y);
        }

        count(xPlus, xMinus, yPlus, yMinus, x, y, direction, graph);
    }

    private boolean isIndependent(Node x, Node y, List<Node> z) {
        System.out.println(SearchLogUtils.independenceFact(x, y, z));

//        double[] _fX = residuals(x, z, false, false);
//        AndersonDarlingTest testX = new AndersonDarlingTest(_fX);
//        double[] sColumnX = testX.getSColumn();
//
//        double[] zeroes = new double[sColumnX.length];
//        Arrays.fill(zeroes, 0);
//
//        double v = dependentTTest(sColumnX, zeroes, 0.0);
//        System.out.println(v);
//        return v < alpha;


        double v = score(x, z);
        System.out.println(v);
        return v > 150;
    }

    private double dependentTTest(double[] x, double[] y, double mu) {
        if (x.length != y.length)
            throw new IllegalArgumentException("x length = " + x.length + " y length = " + y.length);

        List<Double> _x = new ArrayList<Double>();
        List<Double> _y = new ArrayList<Double>();

        for (int i = 0; i < x.length; i++) {
            if (!Double.isNaN(x[i]) && !Double.isNaN(y[i]) && !Double.isInfinite(x[i]) && !Double.isInfinite(y[i])) {
                _x.add(x[i]);
                _y.add(y[i]);
            }
        }

        double[] x2 = new double[_x.size()];
        double[] y2 = new double[_y.size()];

        for (int i = 0; i < _x.size(); i++) {
            x2[i] = _x.get(i);
            y2[i] = _y.get(i);
        }


        int n = x2.length;

        double[] diff = new double[n];

        for (int i = 0; i < n; i++) {
            diff[i] = x2[i] - y2[i];
        }

        double meanDiff = StatUtils.mean(diff);
        double stdDevDiff = StatUtils.standardDeviation(diff);

        double t = (meanDiff - mu) / (stdDevDiff / Math.sqrt(n));

        return 2.0 * (1.0 - ProbUtils.tCdf(Math.abs(t), n - 1));
    }

    private double independentTTest(double meanX, double varX, int nX, double meanY, double varY, int nY) {
        double tX = (meanX - meanY) / Math.sqrt(varX / nX + varY / nY);
        int dfX = nX + nY - 2;
        return 2.0 * (1.0 - ProbUtils.tCdf(Math.abs(tX), dfX));
    }

    // Residual of x is independent of y.

    private boolean residualIndependent(Node x, Node y) {
        double[] _x = concatenate(x);
        double[] _y = concatenate(y);
        double[] eX = residuals(x, Collections.singletonList(y), false, true);
        double[] eY = residuals(y, Collections.singletonList(x), false, true);

        double r1 = StatUtils.correlation(_x, _y);
        double r2 = StatUtils.correlation(_x, eX);
        double r3 = StatUtils.correlation(_x, eY);
        double r4 = StatUtils.correlation(_y, eX);
        double r5 = StatUtils.correlation(_y, eY);
        double r6 = StatUtils.correlation(eX, eY);

        // return a judgement of whether these concatenated residuals are independent.
        double r = StatUtils.correlation(eX, _y);

        if (r > 1.) r = 1.;
        if (r < -1.) r = -1.;

        double fisherZ = Math.sqrt(eX.length - 1 - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        double pvalue = getPValue(fisherZ);
        return pvalue > alpha;

    }

    private double[] concatenate(Node y) {
        List<Double> v = new ArrayList<Double>();

        for (DataSet d : dataSets) {
            Node _y = d.getVariable(y.getName());
            int col = d.getColumn(_y);

            for (int i = 0; i < d.getNumRows(); i++) {
                v.add(d.getDouble(i, col));
            }
        }

        double[] _v = new double[v.size()];

        for (int i = 0; i < v.size(); i++) {
            _v[i] = v.get(i);
        }

        return _v;
    }

    /**
     * Returns the probability associated with the most recently computed independence test.
     */
    public double getPValue(double fisherZ) {
        return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));
    }

    private void count(double xPlus, double xMinus, double yPlus, double yMinus, Node x, Node y, Direction direction, Graph graph) {
//        double xRatio = xPlus / xMinus;
//        double yRatio = yPlus / yMinus;

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
            } else if (direction == Direction.left) {
                type = "X<-Y";
            } else if (direction == Direction.bidirected) {
                type = "X<->Y";
            } else if (direction == Direction.twoCycle) {
                type = "X<=>Y";
            }
        }

//        double xyRatio = xPlus / yMinus;
//        double yxRatio = yPlus / xMinus;
//
//        double xAdjRatio = 0;
//        double yAdjRatio = 0;
//
//        for (Node z : graph.getAdjacentNodes(x)) {
//            if (z == x) continue;
//            xAdjRatio += score(x, Collections.singletonList(z)) / score(x, Collections.<Node>emptyList());
//        }
//
//
//        if (graph.getAdjacentNodes(x).isEmpty()) {
//            xAdjRatio = 0;
//        } else {
//            xAdjRatio /= graph.getAdjacentNodes(x).size();
//        }
//
//        for (Node z : graph.getAdjacentNodes(y)) {
//            if (z == y) continue;
//            yAdjRatio += score(y, Collections.singletonList(z)) / score(y, Collections.<Node>emptyList());
//        }
//
//        if (graph.getAdjacentNodes(y).isEmpty()) {
//            yAdjRatio = 0;
//        } else {
//            yAdjRatio /= graph.getAdjacentNodes(y).size();
//        }
//
//        getDataOut().println(xRatio + "\t" + yRatio + "\t" + xyRatio + "\t" + yxRatio + "\t" + xAdjRatio + "\t" + yAdjRatio +
//                "\t" + (type == null ? "" : type));

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

    static class Pair {
        int index;
        double value;

        public Pair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @Override
		public String toString() {
            return "<" + index + ", " + value + ">";
        }
    }


    private double igci(double[] x, double[] y, int refMeasure, int estimator) {
        int m = x.length;

        if (m != y.length) {
            throw new IllegalArgumentException("Vectors must be the same length.");
        }

        switch (refMeasure) {
            case 1:
                // uniform reference measure

                double minx = min(x);
                double maxx = max(x);
                double miny = min(y);
                double maxy = max(y);

                for (int i = 0; i < x.length; i++) {
                    x[i] = (x[i] - minx) / (maxx - minx);
                    y[i] = (y[i] - miny) / (maxy - miny);
                }

                break;

            case 2:
                double meanx = StatUtils.mean(x);
                double stdx = StatUtils.standardDeviation(x);
                double meany = StatUtils.mean(y);
                double stdy = StatUtils.standardDeviation(y);

                // Gaussian reference measure
                for (int i = 0; i < x.length; i++) {
                    x[i] = (x[i] - meanx) / stdx;
                    y[i] = (y[i] - meany) / stdy;
                }

                break;

            default:
                throw new IllegalArgumentException("Warning: unknown reference measure - no scaling applied.");
        }


        double f;

        if (estimator == 1) {
            // difference of entropies

            double[] x1 = Arrays.copyOf(x, x.length);
            Arrays.sort(x1);

            x1 = removeNaN(x1);

            double[] y1 = Arrays.copyOf(y, y.length);
            Arrays.sort(y1);

            y1 = removeNaN(y1);

            int n1 = x1.length;
            double hx = 0.0;
            for (int i = 0; i < n1 - 1; i++) {
                double delta = x1[i + 1] - x1[i];
                if (delta != 0) {
                    hx = hx + Math.log(Math.abs(delta));
                }
            }

            hx = hx / (n1 - 1) + psi(n1) - psi(1);

            int n2 = y1.length;
            double hy = 0.0;
            for (int i = 0; i < n2 - 1; i++) {
                double delta = y1[i + 1] - y1[i];

                if (delta != 0) {
                    if (Double.isNaN(delta)) {
                        throw new IllegalArgumentException();
                    }

                    hy = hy + Math.log(Math.abs(delta));
                }
            }

            hy = hy / (n2 - 1) + psi(n2) - psi(1);

            f = hy - hx;
        } else if (estimator == 2) {
            // integral-approximation based estimator
            double a = 0;
            double b = 0;

            List<Pair> _x = new ArrayList<Pair>();

            for (int i = 0; i < x.length; i++) {
                _x.add(new Pair(i, x[i]));
            }

            Collections.sort(_x, new Comparator<Pair>() {
                @Override
				public int compare(Pair pair1, Pair pair2) {
                    return new Double(pair1.value).compareTo(new Double(pair2.value));
                }
            });

            List<Pair> _y = new ArrayList<Pair>();

            for (int i = 0; i < y.length; i++) {
                _y.add(new Pair(i, y[i]));
            }

            Collections.sort(_y, new Comparator<Pair>() {
                @Override
				public int compare(Pair pair1, Pair pair2) {
                    return new Double(pair1.value).compareTo(new Double(pair2.value));
                }
            });

            for (int i = 0; i < m - 1; i++) {
                double X1 = x[_x.get(i).index];
                double X2 = x[_x.get(i + 1).index];
                double Y1 = y[_x.get(i).index];
                double Y2 = y[_x.get(i + 1).index];

                if (X2 != X1 && Y2 != Y1) {
                    a = a + Math.log(Math.abs((Y2 - Y1) / (X2 - X1)));
                }

                X1 = x[_y.get(i).index];
                X2 = x[_y.get(i + 1).index];
                Y1 = y[_y.get(i).index];
                Y2 = y[_y.get(i + 1).index];

                if (Y2 != Y1 && X2 != X1) {
                    b = b + Math.log(Math.abs((X2 - X1) / (Y2 - Y1)));
                }
            }

            f = (a - b) / m;

        } else {
            throw new IllegalArgumentException("Estimator must be 1 or 2: " + estimator);
        }

        return f;

    }

    private double[] removeNaN(double[] x1) {
        int i;

        for (i = 0; i < x1.length; i++) {
            if (Double.isNaN(x1[i])) {
                break;
            }
        }

        i = i > x1.length ? x1.length : i;

        return Arrays.copyOf(x1, i);
    }

    // digamma

    double psi(double x) {
        double result = 0, xx, xx2, xx4;
        assert (x > 0);
        for (; x < 7; ++x)
            result -= 1 / x;
        x -= 1.0 / 2.0;
        xx = 1.0 / x;
        xx2 = xx * xx;
        xx4 = xx2 * xx2;
        result += Math.log(x) + (1. / 24.) * xx2 - (7.0 / 960.0) * xx4 + (31.0 / 8064.0) * xx4 * xx2 - (127.0 / 30720.0) * xx4 * xx4;
        return result;
    }

    private double min(double[] x) {
        double min = Double.POSITIVE_INFINITY;

        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) min = x[i];
        }

        return min;
    }

    private double max(double[] x) {
        double max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) max = x[i];
        }

        return max;
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
//        if (true) {
//            return meanResidual(y, parents);
//        }

        if (score == Lofs.Score.andersonDarling) {
            return andersonDarlingPASquareStar(y, parents);
        } else if (score == Lofs.Score.kurtosis) {
            return Math.abs(StatUtils.kurtosis(residuals(y, parents, true, true)));
        } else if (score == Lofs.Score.skew) {
            return Math.abs(StatUtils.skewness(residuals(y, parents, true, true)));
        } else if (score == Lofs.Score.fifthMoment) {
            return Math.abs(StatUtils.standardizedFifthMoment(residuals(y, parents, true, true)));
        } else if (score == Lofs.Score.absoluteValue) {
            return meanAbsolute(y, parents);
        } else if (score == Lofs.Score.exp) {
            return expScoreStandardized(y, parents);
        } else if (score == Lofs.Score.expUnstandardized) {
            return expScoreUnstandardized(y, parents);
        } else if (score == Lofs.Score.other) {
            return minusExpXSquaredDividedByTwo(y, parents);
        } else if (score == Lofs.Score.logcosh) {
            return logCoshScore(y, parents);
        }

        throw new IllegalStateException();
    }

    //=============================PRIVATE METHODS=========================//

    private double meanResidual(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, false, true);
        return StatUtils.mean(_f);
    }

    private double meanAbsolute(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, true, true);

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _f.length; k++) {
            f.set(k, Math.abs(f.get(k)));
        }

        double expected = Descriptive.mean(f);
        double diff = expected - Math.sqrt(2.0 / Math.PI);
        double score = diff * diff;

        return score;
    }

    private double expScoreUnstandardized(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, false, true);

        for (int k = 0; k < _f.length; k++) {
            double v = _f[k];
            _f[k] = v;
        }

        double expected = StatUtils.mean(_f);
        return expected;
    }

    private double[] expScoreUnstandardizedSList(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, false, true);

        for (int k = 0; k < _f.length; k++) {
            double v = _f[k];
            _f[k] = v;
        }

        return _f;
    }

    private double minusExpXSquaredDividedByTwo(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, true, true);

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _f.length; k++) {
            double v = Math.exp(-Math.pow(f.get(k), 2.0) / 2);
            f.set(k, v);
        }

        double mean = Descriptive.mean(f);
        double diff = mean - expectedExp();
        double score = diff * diff;

        return score;
    }

    private double expectedExp() {
        if (Double.isNaN(expectedExp)) {
            double nsum = 0.0;
            int ncount = 0;

            for (int i = 0; i < 100; i++) {
                double sample = RandomUtil.getInstance().nextNormal(0, 1);
                double v = Math.exp(-Math.pow(sample, 2.0) / 2);
                nsum += v;
                ncount++;
            }

            double navg = nsum / ncount;
            this.expectedExp = navg;
        }

        return this.expectedExp;
    }

    private double expScoreStandardized(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, true, true);

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _f.length; k++) {
            f.set(k, Math.exp(f.get(k)));
        }

        double expected = Descriptive.mean(f);
        double logExpected = Math.log(expected);
        double diff = logExpected - 0.5;
        return Math.abs(diff);
    }

    private static double logCoshExp() {
        double nsum = 0.0;

        for (int i = 0; i < 10000; i++) {
            double sample = RandomUtil.getInstance().nextNormal(0, 1);
            double v = Math.log(Math.cosh(sample));
            nsum += v;
        }

        double navg = nsum / 10000;
        return navg;
    }

    private double logCoshScore(Node node, List<Node> parents) {
        double[] _f = residuals(node, parents, true, true);

        DoubleArrayList f = new DoubleArrayList(_f);

        for (int k = 0; k < _f.length; k++) {
            double v = Math.log(Math.cosh((f.get(k))));
            f.set(k, v);
        }

        double expected = Descriptive.mean(f);
        double diff = expected - logCoshExp;
        double score = diff * diff;
        return score;
    }

    private double[] residuals(Node node, List<Node> parents, boolean standardize, boolean removeNaN) {
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
            DataSet dataSet = dataSets.get(m);

            int targetCol = dataSet.getColumn(target);

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                if (Double.isNaN(dataSet.getDouble(i, targetCol))) {
                    continue DATASET;
                }
            }

            for (int g = 0; g < regressors.size(); g++) {
                int regressorCol = dataSet.getColumn(regressors.get(g));

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    if (Double.isNaN(dataSet.getDouble(i, regressorCol))) {
                        continue DATASET;
                    }
                }
            }

            RegressionResult result = regressions.get(m).regress(target, regressors);
            double[] residualsSingleDataset = result.getResiduals().toArray();

            if (result.getCoef().length > 0) {
                double intercept = result.getCoef()[0];

                for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
                    residualsSingleDataset[i2] = residualsSingleDataset[i2] + intercept;
                }
            }

            if (isMeanCenterResiduals()) {
                double mean = StatUtils.mean(residualsSingleDataset);
                for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
                    residualsSingleDataset[i2] = residualsSingleDataset[i2] - mean;
                }
            }

            for (int k = 0; k < residualsSingleDataset.length; k++) {
                if (removeNaN && Double.isNaN(residualsSingleDataset[k])) continue;
                _residuals.add(residualsSingleDataset[k]);
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        if (standardize && removeNaN) {
            _f = DataUtils.standardizeData(_f);
        }

        return _f;
    }

    private double avgRegressionP(Node child, Node parent) {
        Node _target = child;
        List<Node> _regressors = Collections.singletonList(parent);
        Node target = getVariable(variables, _target.getName());
        List<Node> regressors = new ArrayList<Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        if (regressors.contains(child)) {
            throw new IllegalArgumentException();
        }

        double sum = 0.0;

        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = regressions.get(m).regress(target, regressors);
            int index = regressors.indexOf(parent);

            if (!Double.isNaN(result.getP()[1 + index])) {
                sum += result.getP()[0];
            }
        }

        return sum / dataSets.size();
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
        double[] _f = residuals(node, parents, true, true);
        return new AndersonDarlingTest(_f).getASquaredStar();
    }

    private double andersonDarlingPASquareStarB(Node node, List<Node> parents) {
//        List<Double> _residuals = new ArrayList<Double>();

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
                if (isMeanCenterResiduals()) {
                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
                }
            }

            double[] _f = new double[_residualsSingleDataset.size()];

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _f[k] = _residualsSingleDataset.get(k);
            }

            double a2 = new AndersonDarlingTest(_f).getASquaredStar();

            if (Double.isNaN(a2)) {
                continue;
            }

            sum += a2;
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
            DataSet dataSet = dataSets.get(m);

            int targetCol = dataSet.getColumn(target);

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                if (Double.isNaN(dataSet.getDouble(i, targetCol))) {
                    continue DATASET;
                }
            }

            for (int g = 0; g < regressors.size(); g++) {
                int regressorCol = dataSet.getColumn(regressors.get(g));

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    if (Double.isNaN(dataSet.getDouble(i, regressorCol))) {
                        continue DATASET;
                    }
                }
            }

            RegressionResult result = regressions.get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);

            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
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

        return new AndersonDarlingTest(_f).getP();
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
}
