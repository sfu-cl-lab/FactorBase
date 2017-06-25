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
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.jet.math.Mult;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.sem.ConjugateDirectionSearch;
import edu.cmu.tetrad.util.*;
import pal.math.*;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * LOFS = Ling Orientation Fixed Structure.
 *
 * @author Joseph Ramsey
 */
public class Lofs2 {
    private static Map <String, Map <String, Integer>> countMap = new HashMap <String, Map <String, Integer>>();
    private static double logCoshExp = logCoshExp();
    private static Map <Integer, double[]> savedValues = new HashMap <Integer, double[]>();
    private Graph pattern;
    private List <DataSet> originalDataSets;
    private List <DataSet> dataSets;
    private List <DoubleMatrix2D> matrices;
    private double alpha = 1.0;
    private List <Regression> regressions;
    private List <Node> variables;
    private boolean r1Done = true;
    private boolean r2Done = true;
    private boolean r3Done = true;
    private boolean strongR2 = false;
    private boolean meekDone = false;
    private boolean r2Orient2Cycles = true;
    private boolean centerResiduals = false;
    private Graph trueGraph = null;
    private Lofs.Score score = Lofs.Score.andersonDarling;
    private double epsilon = 1.0;
    private PrintStream dataOut = System.out;
    private Knowledge knowledge = new Knowledge();
    private double expectedExp;
    private Rule rule = Rule.R5;
    private double delta = 0.0;
    private double zeta = 0.0;

    //===============================CONSTRUCTOR============================//

    public Lofs2(Graph pattern, List <DataSet> dataSets)
            throws IllegalArgumentException {

        if (dataSets == null) {
            throw new IllegalArgumentException("Data set must be specified.");
        }

        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must be specified.");
        }

        this.pattern = pattern;
        this.variables = dataSets.get(0).getVariables();

        this.dataSets = dataSets;
    }

    //==========================PUBLIC=========================================//

    private static double logCoshExp() {
        return 0.3745232061467262;

//        double nsum = 0.0;
//        int n = 100000000;
//
//        for (int i = 0; i < n; i++) {
//            double sample = RandomUtil.getInstance().nextNormal(0, 1);
//            double v = Math.log(Math.cosh(sample));
//            nsum += v;
//        }
//
//        double navg = nsum / n;
//
//        System.out.println("Logcoshexp = " + navg + " " + Math.sqrt(3));
//
//        return navg;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Graph orient() {

        Graph skeleton = GraphUtils.undirectedGraph(getPattern());
        Graph graph = new EdgeListGraph(skeleton.getNodes());

        List <Node> nodes = skeleton.getNodes();
//        Collections.shuffle(nodes);

        if (this.rule == Rule.R1TimeLag) {
            ruleR1TimeLag(skeleton, graph, nodes);
        } else if (this.rule == Rule.R1) {
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
            Graph graph1 = ruleR6(graph);
            graph = GraphUtils.undirectedToBidirected(graph1);
            graph = GraphUtils.bidirectedToTwoCycle(graph);
        } else if (this.rule == Rule.R7) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return ruleR7(graph);
        } else if (this.rule == Rule.R8) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return r8(graph, dataSets);
        } else if (this.rule == Rule.R9) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return r9(graph);
        } else if (this.rule == Rule.R10) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return tanhGraph(graph);
//        } else if (this.rule == Rule.R10b) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return tanhNGraph(graph);
//        } else if (this.rule == Rule.R11) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return tanhGraphStar(graph);
//        } else if (this.rule == Rule.R12) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return tanhGraphStar2(graph);
        } else if (this.rule == Rule.R11) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return skewGraph(graph);
//        } else if (this.rule == Rule.R14) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return skewStarGraph(graph);
//        } else if (this.rule == Rule.R15) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return skewStar2Graph(graph);
        } else if (this.rule == Rule.R12) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return robustSkewGraph(graph);
//        } else if (this.rule == Rule.R17) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return robustSkewGraphStar(graph);
//        } else if (this.rule == Rule.R18) {
//            graph = GraphUtils.undirectedGraph(skeleton);
//            return robustSkewGraphStar2(graph);
//        } else if (this.rule == Rule.R19) {
//            ruleR19(skeleton, graph, nodes);
        } else if (this.rule == Rule.IGCI) {
            graph = GraphUtils.undirectedGraph(skeleton);
            return igci(graph);
        }

        return graph;
    }

    public boolean isOrientStrongerDirection() {
        return isCenterResiduals();
    }

    public void setOrientStrongerDirection(boolean orientStrongerDirection) {
        setCenterResiduals(orientStrongerDirection);
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

    public boolean isR2Orient2Cycles() {
        return r2Orient2Cycles;
    }

    public void setR2Orient2Cycles(boolean r2Orient2Cycles) {
        this.r2Orient2Cycles = r2Orient2Cycles;
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

    public boolean isCenterResiduals() {
        return centerResiduals;
    }

    public void setCenterResiduals(boolean meanCenterResiduals) {
        this.centerResiduals = meanCenterResiduals;
    }

    //==========================PRIVATE=======================================//

    private List <Regression> getRegressions() {
        if (this.regressions == null) {
            List <Regression> regressions = new ArrayList <Regression>();
            this.variables = dataSets.get(0).getVariables();

            for (DataSet dataSet : dataSets) {
                regressions.add(new RegressionDataset(dataSet));
            }

            this.regressions = regressions;
        }

        return this.regressions;
    }

    private void setDataSets(List <DataSet> dataSets) {

        List <DataSet> copy = new ArrayList <DataSet>();

        for (DataSet dataSet : dataSets) {
            DataSet _copy = new ColtDataSet((ColtDataSet) dataSet);
            copy.add(_copy);
        }

        this.dataSets = copy;

        matrices = new ArrayList <DoubleMatrix2D>();

        for (DataSet dataSet : copy) {
            matrices.add(dataSet.getDoubleData());
        }
    }

    private void printStats() {
//        for (DataSet dataSet : dataSets) {
//            DoubleMatrix2D matrix = dataSet.getDoubleData();
//
//            for (int c = 0; c < matrix.columns(); c++) {
//                double[] f = matrix.viewColumn(c).toArray();
//                System.out.println("AD score = " + new AndersonDarlingTest(f).getASquaredStar());
//            }
//        }

//        for (Node node : dataSets.get(0).getVariables()) {
//            double[] f = residuals(node, Collections.<Node>emptyList(), true, true);
//            System.out.println("=== AD score = " + new AndersonDarlingTest(f).getASquaredStar());
//        }
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

            Map <String, Integer> counts = countMap.get(key);

            for (String key2 : counts.keySet()) {
                System.out.println(key2 + "\t" + counts.get(key2));
            }
        }
    }

    private void ruleR1TimeLag(Graph skeleton, Graph graph, List <Node> nodes) {
        List <DataSet> timeSeriesDataSets = new ArrayList <DataSet>();
        Knowledge knowledge = null;
        List <Node> dataNodes = null;

        for (DataModel dataModel : dataSets) {
            if (!(dataModel instanceof DataSet)) {
                throw new IllegalArgumentException("Only tabular data sets can be converted to time lagged form.");
            }

            DataSet dataSet = (DataSet) dataModel;
            DataSet lags = TimeSeriesUtils.createLagData(dataSet, 1);
            if (dataSet.getName() != null) {
                lags.setName(dataSet.getName());
            }
            timeSeriesDataSets.add(lags);

            if (knowledge == null) {
                knowledge = lags.getKnowledge();
            }

            if (dataNodes == null) {
                dataNodes = lags.getVariables();
            }
        }

//        IImages images = new Images3(timeSeriesDataSets);
//        images.setKnowledge(knowledge);
//        Graph pattern = images.search();


        Graph laggedSkeleton = new EdgeListGraph(dataNodes);

        for (Edge edge : skeleton.getEdges()) {
            String node1 = edge.getNode1().getName();
            String node2 = edge.getNode2().getName();

            Node node10 = laggedSkeleton.getNode(node1 + ":0");
            Node node20 = laggedSkeleton.getNode(node2 + ":0");

            laggedSkeleton.addUndirectedEdge(node10, node20);

            Node node11 = laggedSkeleton.getNode(node1 + ":1");
            Node node21 = laggedSkeleton.getNode(node2 + ":1");

            laggedSkeleton.addUndirectedEdge(node11, node21);
        }

        for (Node node : skeleton.getNodes()) {
            String _node = node.getName();

            Node node0 = laggedSkeleton.getNode(_node + ":0");
            Node node1 = laggedSkeleton.getNode(_node + ":1");

            laggedSkeleton.addUndirectedEdge(node0, node1);
        }

        Lofs2 lofs = new Lofs2(laggedSkeleton, timeSeriesDataSets);
        lofs.setKnowledge(knowledge);
        lofs.setRule(Rule.R1);
        Graph _graph = lofs.orient();

        graph.removeEdges(graph.getEdges());

        for (Edge edge : _graph.getEdges()) {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();
            Endpoint end1 = edge.getEndpoint1();
            Endpoint end2 = edge.getEndpoint2();

            String index1 = node1.getName().split(":")[1];
            String index2 = node2.getName().split(":")[1];

            if ("1".equals(index1) || "1".equals(index2)) continue;

            String name1 = node1.getName().split(":")[0];
            String name2 = node2.getName().split(":")[0];

            Node _node1 = graph.getNode(name1);
            Node _node2 = graph.getNode(name2);

            Edge _edge = new Edge(_node1, _node2, end1, end2);
            graph.addEdge(_edge);
        }
    }

    private void ruleR1(Graph skeleton, Graph graph, List <Node> nodes) {
        List <DataSet> centeredData = DataUtils.centerData(this.dataSets);
        setDataSets(centeredData);

        for (Node node : nodes) {
            SortedMap <Double, String> scoreReports = new TreeMap <Double, String>();

            List <Node> adj = new ArrayList <Node>();

            for (Node _node : skeleton.getAdjacentNodes(node)) {
                if (knowledge.edgeForbidden(_node.getName(), node.getName())) {
                    continue;
                }

                adj.add(_node);
            }

            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
            int[] choice;
            double maxScore = Double.NEGATIVE_INFINITY;
            List <Node> parents = null;

            while ((choice = gen.next()) != null) {
                List <Node> _parents = GraphUtils.asList(choice, adj);

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

//            if (normal(node, parents)) continue;

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

    private void ruleR1b(Graph skeleton, Graph graph, List <Node> nodes) {
        for (Node node : nodes) {
            List <Node> parents = new ArrayList <Node>();
            double score = score(node, Collections. <Node>emptyList());

            while (true) {
                Node savedParent = null;
                List <Node> adj = skeleton.getAdjacentNodes(node);
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

    private void ruleR1c(Graph skeleton, Graph graph, List <Node> nodes) {
        for (Node node : nodes) {
            SortedMap <Double, String> scoreReports = new TreeMap <Double, String>();

            List <Node> adj = skeleton.getAdjacentNodes(node);

            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
            int[] choice;
            double maxScore = Double.NEGATIVE_INFINITY;
            List <Node> parents = null;

            while ((choice = gen.next()) != null) {
                List <Node> _parents = GraphUtils.asList(choice, adj);

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
        List <DataSet> centeredData = DataUtils.centerData(this.dataSets);
        setDataSets(centeredData);

        List <Edge> edgeList1 = skeleton.getEdges();
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

    private void resolveOneEdgeMax2Old(Graph graph, Node x, Node y, boolean strong, Graph oldGraph) {
        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        SortedMap <Double, String> scoreReports = new TreeMap <Double, String>();

        List <Node> neighborsx = new ArrayList <Node>();

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
            List <Node> condxMinus = GraphUtils.asList(choicex, neighborsx);

            List <Node> condxPlus = new ArrayList <Node>(condxMinus);
            condxPlus.add(y);

            double xPlus = score(x, condxPlus);
            double xMinus = score(x, condxMinus);

            List <Node> neighborsy = new ArrayList <Node>();

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
                List <Node> condyMinus = GraphUtils.asList(choicey, neighborsy);

//                List<Node> parentsY = oldGraph.getParents(y);
//                parentsY.remove(x);
//                if (!condyMinus.containsAll(parentsY)) {
//                    continue;
//                }

                List <Node> condyPlus = new ArrayList <Node>(condyMinus);
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

    private void resolveOneEdgeMax2(Graph graph, Node x, Node y, boolean strong, Graph oldGraph) {
        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        SortedMap <Double, String> scoreReports = new TreeMap <Double, String>();

        List <Node> neighborsx = new ArrayList <Node>();

        for (Node _node : graph.getAdjacentNodes(x)) {
            if (!knowledge.edgeForbidden(_node.getName(), x.getName())) {
//                if (!knowledge.edgeForbidden(x.getName(), _node.getName())) {
                neighborsx.add(_node);
            }
        }

//        neighborsx.remove(y);

        double max = Double.NEGATIVE_INFINITY;
        boolean left = false;
        boolean right = false;

        DepthChoiceGenerator genx = new DepthChoiceGenerator(neighborsx.size(), neighborsx.size());
        int[] choicex;

        while ((choicex = genx.next()) != null) {
            List <Node> condxMinus = GraphUtils.asList(choicex, neighborsx);

            if (condxMinus.contains(y)) continue;
            if (!neighborsx.contains(y)) continue;

            List <Node> condxPlus = new ArrayList <Node>(condxMinus);

            condxPlus.add(y);

            double xPlus = score(x, condxPlus);
            double xMinus = score(x, condxMinus);

            List <Node> neighborsy = new ArrayList <Node>();

            for (Node _node : graph.getAdjacentNodes(y)) {
                if (!knowledge.edgeForbidden(_node.getName(), y.getName())) {
//                    if (!knowledge.edgeForbidden(y.getName(), _node.getName())) {
                    neighborsy.add(_node);
                }
            }

            DepthChoiceGenerator geny = new DepthChoiceGenerator(neighborsy.size(), neighborsy.size());
            int[] choicey;

            while ((choicey = geny.next()) != null) {
                List <Node> condyMinus = GraphUtils.asList(choicey, neighborsy);

                if (condyMinus.contains(x)) continue;
                if (!neighborsy.contains(x)) continue;

                List <Node> condyPlus = new ArrayList <Node>(condyMinus);
                condyPlus.add(x);

                double yPlus = score(y, condyPlus);
                double yMinus = score(y, condyMinus);

                // Checking them all at once is expensive but avoids lexical ordering problems in the algorithm.
//                if (normal(y, condyPlus) || normal(x, condxMinus) || normal(x, condxPlus) || normal(y, condyMinus)) {
//                    continue;
//                }

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


    private Graph ruleR3(Graph graph) {
        List <DataSet> standardized = DataUtils.standardizeData(this.dataSets);
        setDataSets(standardized);

        printStats();

        List <Edge> edgeList1 = graph.getEdges();

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR3(graph, x, y);
        }

        return graph;

    }

    private void resolveOneEdgeMaxR3(Graph graph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List <Node> condxMinus = Collections.emptyList();
        List <Node> condxPlus = Collections.singletonList(y);
        List <Node> condyMinus = Collections.emptyList();
        List <Node> condyPlus = Collections.singletonList(x);

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

        System.out.println("NG(X) = " + xMinus + " NG(X|Y) = " + xPlus + " NG(Y) = " + yMinus + " NG(Y|X) = " + yPlus);
        System.out.println("delta X = " + deltaX + " delta Y = " + deltaY);

        graph.removeEdges(x, y);

//        if (xMinus < yMinus) {
//            graph.addDirectedEdge(x, y);
//        } else {
//            graph.addDirectedEdge(y, x);
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

    private void ruleR4(Graph graph) {
        setDataSets(dataSets);

        List <Edge> edgeList1 = graph.getEdges();
//        Collections.shuffle(edgeList1);

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR4(graph, x, y);
        }
    }

    private void resolveOneEdgeMaxR4(Graph graph, Node x, Node y) {

        // Hoping the data aren't already mean centered.
        boolean saveMeanCenterSetting = centerResiduals;
        centerResiduals = false;

        double[] resX = residuals(x, Collections. <Node>emptyList(), false, true);
        double[] resY = residuals(y, Collections. <Node>emptyList(), false, true);

        double muX = StatUtils.mean(resX);
        double muY = StatUtils.mean(resY);

        graph.removeEdges(x, y);

        if (muX < muY) graph.addDirectedEdge(x, y);
        else if (muY < muX) graph.addDirectedEdge(y, x);
        else graph.addUndirectedEdge(x, y);

        centerResiduals = saveMeanCenterSetting;
    }

    private void ruleR5(Graph graph) {
        List <DataSet> standardized = DataUtils.standardizeData(this.dataSets);
        setDataSets(standardized);

        List <Edge> edgeList1 = graph.getEdges();
//        Collections.shuffle(edgeList1);

        for (Edge adj : edgeList1) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();

            resolveOneEdgeMaxR5(graph, x, y);
        }
    }

    private void resolveOneEdgeMaxR5(Graph graph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List <Node> condxMinus = new ArrayList <Node>();
        List <Node> condxPlus = new ArrayList <Node>(condxMinus);
        condxPlus.add(y);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        List <Node> condyMinus = new ArrayList <Node>();
        List <Node> condyPlus = new ArrayList <Node>(condyMinus);
        condyPlus.add(x);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        boolean standardize = false;

        System.out.println("NG(X) = " + xMinus + " NG(X|Y) = " + xPlus + " NG(Y) = " + yMinus + " NG(Y|X) = " + yPlus);

        Endpoint xEndpoint = null;

//        double epsilon = 0;

        boolean xLow = xPlus < xMinus - epsilon;
        boolean xHigh = xPlus > xMinus + epsilon;
        boolean yLow = yPlus < yMinus - epsilon;
        boolean yHigh = yPlus > yMinus + epsilon;

        if (xHigh) {
            xEndpoint = Endpoint.ARROW;
        } else if (xLow) {
            xEndpoint = Endpoint.TAIL;
        } else {
            xEndpoint = Endpoint.CIRCLE;
        }

        Endpoint yEndpoint = null;

        if (yHigh) {
            yEndpoint = Endpoint.ARROW;
        } else if (yLow) {
            yEndpoint = Endpoint.TAIL;
        } else {
            yEndpoint = Endpoint.CIRCLE;
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

    private Graph ruleR6(Graph graph) {
        List <DataSet> standardized = DataUtils.standardizeData(this.dataSets);
        setDataSets(standardized);

        Graph newGraph = new EdgeListGraph(graph);
        Graph oldGraph = null; // EdgeListGraph(graph);
        int i = 0;

        while (!newGraph.equals(oldGraph) && ++i <= ((int) epsilon == 0 ? 10 : (int) epsilon)) {
            oldGraph = new EdgeListGraph(newGraph);
//        for (int i = 0; i < (int) epsilon; i++) {

            List <Edge> edgeList1 = oldGraph.getEdges();

            for (Edge adj : edgeList1) {
                Node x = adj.getNode1();
                Node y = adj.getNode2();

                resolveOneEdgeMaxR6(oldGraph, newGraph, x, y);
            }

        }

        return newGraph;
    }


    private void resolveOneEdgeMaxR6(Graph oldGraph, Graph newGraph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List <Node> condxMinus = pathBlockingSet(oldGraph, x, y, false);
        List <Node> condxPlus = pathBlockingSet(oldGraph, x, y, true);
        List <Node> condyMinus = pathBlockingSet(oldGraph, y, x, false);
        List <Node> condyPlus = pathBlockingSet(oldGraph, y, x, true);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

//        if (Double.isNaN(xPlus)) {
//            System.out.println();
//            score(x, condxPlus);
//        }

//        double xMax = xPlus > xMinus ? xPlus : xMinus;
//        double yMax = yPlus > yMinus ? yPlus : yMinus;
//
//        double score = combinedScore(xMax, yMax);
//        TetradLogger.getInstance().log("info", "Score = " + score);

        System.out.println("NG(X|N) = " + xMinus + " NG(X|Y,N) = " + xPlus + " NG(Y|N) = " + yMinus + " NG(Y|X,N) = " + yPlus);
        System.out.println("delta X = " + (xPlus - xMinus) + " delta Y = " + (yPlus - yMinus));

        Endpoint xEndpoint;

        boolean xLow = xPlus < xMinus - epsilon;
        boolean xHigh = xPlus > xMinus + epsilon;
        boolean yLow = yPlus < yMinus - epsilon;
        boolean yHigh = yPlus > yMinus + epsilon;

        if (xHigh) {
            xEndpoint = Endpoint.ARROW;
        } else if (xLow) {
            xEndpoint = Endpoint.TAIL;
        } else {
            xEndpoint = Endpoint.CIRCLE;
        }

        Endpoint yEndpoint;

        if (yHigh) {
            yEndpoint = Endpoint.ARROW;
        } else if (yLow) {
            yEndpoint = Endpoint.TAIL;
        } else {
            yEndpoint = Endpoint.CIRCLE;
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

    private Graph ruleR7(Graph graph) {
        List <DataSet> standardized = DataUtils.standardizeData(this.dataSets);
        setDataSets(standardized);

        Graph newGraph = new EdgeListGraph(graph);
        Graph oldGraph = null; // EdgeListGraph(graph);
        int i = 0;

        do {
            oldGraph = new EdgeListGraph(newGraph);

            List <Edge> edgeList1 = oldGraph.getEdges();

            for (Edge adj : edgeList1) {
                Node x = adj.getNode1();
                Node y = adj.getNode2();

                resolveOneEdgeMaxR7Iterated(oldGraph, newGraph, x, y);
            }

        } while (!newGraph.equals(oldGraph) && ++i < 1);

        return newGraph;

    }

    private Graph ruleR8Pooled(Graph graph, List <DataSet> dataSets) {
        dataSets = DataUtils.centerData(dataSets);
        DataSet concatData = DataUtils.concatenateDataSets(dataSets);
        List <DataSet> concatData2 = Collections.singletonList(concatData);
        return r8(graph, concatData2);
    }

//    private Graph ruleR8Pooled(Graph graph) {
//        Graph graph2 = new EdgeListGraph(graph.getNodes());
//        Map<Edge, Double> coef = new HashMap<Edge, Double>();
//
//        for (Node node : graph.getNodes()) {
//            List<Node> adj = graph.getAdjacentNodes(node);
//            double[] _a = maximizeNonGaussianityPooled(dataSets, matrices, node, adj);
//
//            for (int i = 0; i < adj.size(); i++) {
//                Node _node = adj.get(i);
//                double a = _a[i];
//
//                System.out.println("Coefficient for " + _node + " --> " + node + " = " + a);
//
//                Edge edge = Edges.directedEdge(_node, node);
//
//                if (Math.abs(a) > epsilon) {
//                    graph2.addEdge(edge);
//                }
//
//                coef.put(edge, a);
//            }
//        }
//
//        if (isMeanCenterResiduals()) {
//            List<Node> nodes = graph.getNodes();
//            Graph graph3 = new EdgeListGraph(nodes);
//
//            for (int i = 0; i < nodes.size(); i++) {
//                for (int j = i + 1; j < nodes.size(); j++) {
//                    Node node1 = nodes.get(i);
//                    Node node2 = nodes.get(j);
//
//                    Edge edge1 = Edges.directedEdge(node1, node2);
//                    Edge edge2 = Edges.directedEdge(node2, node1);
//
//                    double a = Double.NaN;
//                    double b = Double.NaN;
//
//                    if (coef.get(edge1) != null) {
//                        a = coef.get(edge1);
//                    }
//
//                    if (coef.get(edge2) != null) {
//                        b = coef.get(edge2);
//                    }
//
//                    if (!Double.isNaN(a) && !Double.isNaN(b)) {
//                        if (Math.abs(a) > Math.abs(b)) {
//                            graph3.addEdge(edge1);
//                        } else if (Math.abs(a) < Math.abs(b)) {
//                            graph3.addEdge(edge2);
//                        } else if (Math.abs(a) > 0 && Math.abs(b) > 0 && Math.abs(a) == Math.abs(b)) {
//                            graph3.addUndirectedEdge(node1, node2);
//                        }
//
////                        if (a > b) {
////                            graph3.addEdge(edge1);
////                        } else if (a < b) {
////                            graph3.addEdge(edge2);
////                        } else if (a == b) {
////                            graph3.addUndirectedEdge(node1, node2);
////                        }
//                    }
//                }
//            }
//
//            return graph3;
//        }
//
//        return graph2;
//    }

    // Average of coefficients.

    private Graph r8(Graph graph, List <DataSet> dataSets) {
//        List<DoubleMatrix2D> matrices = new ArrayList<DoubleMatrix2D>();
//
//        for (DataSet dataSet : dataSets) {
//            matrices.add(dataSet.getDoubleData());
//        }

        setDataSets(dataSets);
        Graph graph2 = new EdgeListGraph(graph.getNodes());
        Map <Edge, Double> coef = new HashMap <Edge, Double>();

        for (Node node : graph.getNodes()) {
            List <Node> adj = graph.getAdjacentNodes(node);

            double b[][] = new double[this.dataSets.size()][];

            for (int index = 0; index < this.dataSets.size(); index++) {
                double[] a = maximizeNonGaussianityGoodStartingPoint(index, node, adj);
                b[index] = a;
            }

            double[] _a = new double[b[0].length];

            for (int k = 0; k < b[0].length; k++) {
                double sum = 0.0;
                int count = 0;

                for (int l = 0; l < b.length; l++) {
                    double u = b[l][k];

                    if (!Double.isNaN(u)) {
//                        sum += Math.abs(u);
                        sum += u;
                        count++;
                    }
                }

                double avg = sum / count;
                _a[k] = avg;
            }

            for (int i = 0; i < adj.size(); i++) {
                Node _node = adj.get(i);
                double a = _a[i];

                System.out.println("Coefficient for " + _node + " --> " + node + " = " + a);

                Edge edge = Edges.directedEdge(_node, node);

                if (Math.abs(a) > epsilon) {
                    graph2.addEdge(edge);
                }

                coef.put(edge, a);
            }
        }

        if (isOrientStrongerDirection()) {
            List <Node> nodes = graph.getNodes();
            Graph graph3 = new EdgeListGraph(nodes);

            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node node1 = nodes.get(i);
                    Node node2 = nodes.get(j);

                    Edge edge1 = Edges.directedEdge(node1, node2);
                    Edge edge2 = Edges.directedEdge(node2, node1);

                    double a = Double.NaN;
                    double b = Double.NaN;

                    if (coef.get(edge1) != null) {
                        a = coef.get(edge1);
                    }

                    if (coef.get(edge2) != null) {
                        b = coef.get(edge2);
                    }

                    if (Double.isNaN(a)) a = 0.0;
                    if (Double.isNaN(b)) b = 0.0;

                    if (!Double.isNaN(a) && !Double.isNaN(b)) {
                        if (Math.abs(a) > Math.abs(b)) {
                            graph3.addEdge(edge1);
                        } else if (Math.abs(a) < Math.abs(b)) {
                            graph3.addEdge(edge2);
                        } else if (Math.abs(a) > 0 && Math.abs(b) > 0 && Math.abs(a) == Math.abs(b)) {
                            graph3.addUndirectedEdge(node1, node2);
                        }

//                        if (a > b) {
//                            graph3.addEdge(edge1);
//                        } else if (a < b) {
//                            graph3.addEdge(edge2);
//                        } else if (a == b) {
//                            graph3.addUndirectedEdge(node1, node2);
//                        }
                    }
                }
            }

            return graph3;
        }

        return graph2;
    }

    public Graph r9(Graph graph) {
        List <DataSet> centered = DataUtils.centerData(dataSets);
        setDataSets(centered);

        if (false) {
            whitenData();
        }

        List <Node> nodes = dataSets.get(0).getVariables();
        int numNodes = nodes.size();

        System.out.println(nodes);

        double bound = zeta;

        double min = -bound;
        double max = bound;

        final List <Mapping> allMappings = createMappings(graph, nodes, numNodes);

        double[][] WAll = new double[numNodes][numNodes];
        int[][] _count = new int[numNodes][numNodes];

        for (int k = 0; k < dataSets.size(); k++) {
            double[][] W = estimateW(k, matrices, numNodes, min, max, allMappings);

//            System.out.println("W = " + MatrixUtils.toString(W));

            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (i == j) {
                        WAll[i][j] += W[i][j]; // These should be 1
                        _count[i][j]++;
                        continue;
                    }

                    double coef = -W[i][j];

                    if (isStrongR2()) {
                        if (Double.isNaN(coef)) {
                            continue;
                        }

                        double _bound = bound - 0.01;
                        if (coef >= _bound || coef <= -_bound) {
                            continue;
                        }

                        if (Math.abs(coef) > epsilon) {
                            WAll[i][j] += 1.0;
                        }

                        _count[i][j]++;
                    } else {
                        if (Double.isNaN(coef)) {
                            continue;
                        }

                        double _bound = bound - 0.01;
                        if (coef >= _bound || coef <= -_bound) {
                            continue;
                        }

                        if (false) { //dataSets.size() > 1) {
                            WAll[i][j] += -Math.abs(coef);
                        } else {
                            WAll[i][j] += -coef;
                        }

                        _count[i][j]++;
                    }

                    // Note WAll[i][j] will be zero if the conditions are not
                    // satisfied for any data set.
                }
            }

//            System.out.println("Wall = " + MatrixUtils.toString(WAll));
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                WAll[i][j] /= _count[i][j] == 0 ? 1 : _count[i][j];
            }
        }

        double[][] W = WAll;

//        DoubleMatrix2D W2 = new DenseDoubleMatrix2D(W);
//        DoubleMatrix2D X = concatData.getDoubleData();
//        DoubleMatrix2D e = new Algebra().mult(W2, X.viewDice());
//        DoubleMatrix2D cov = new Algebra().mult(e, e.viewDice());
////        System.out.println("cov = " + cov);
//        CovarianceMatrix cov2 = new CovarianceMatrix(concatData.getVariables(), cov, concatData.getNumRows());
//        CorrelationMatrix corr = new CorrelationMatrix(cov2);
//        System.out.println(corr);

        Graph _graph = new EdgeListGraph(nodes);
        Map <Edge, Double> coef = new HashMap <Edge, Double>();

        for (Mapping mapping : allMappings) {
            int i = mapping.getI();
            int j = mapping.getJ();

            Node node1 = nodes.get(j);
            Node node2 = nodes.get(i);

            if (_graph.isAdjacentTo(node1, node2)) continue;

            if (isStrongR2()) {
                double _coef = W[i][j];
                double _coef2 = W[j][i];

                Edge edge1 = Edges.directedEdge(node1, node2);
                coef.put(edge1, _coef);

                if (_coef >= delta) {
                    _graph.addEdge(edge1);
                }

                Edge edge2 = Edges.directedEdge(node2, node1);
                coef.put(edge2, _coef2);

                if (_coef2 >= delta) {
                    _graph.addEdge(edge2);
                }

                if (Math.abs(_coef) < delta && Math.abs(_coef2) < delta) {
                    _graph.addUndirectedEdge(node1, node2);
                }
            } else {
                double _coef = -W[i][j];
                double _coef2 = -W[j][i];

                Edge edge1 = Edges.directedEdge(node1, node2);
                coef.put(edge1, _coef);

                if (Math.abs(_coef) >= epsilon) {
                    _graph.addEdge(edge1);
                }

                Edge edge2 = Edges.directedEdge(node2, node1);
                coef.put(edge2, _coef2);

                if (Math.abs(_coef2) >= epsilon) {
                    _graph.addEdge(edge2);
                }

                if (Math.abs(_coef) < epsilon && Math.abs(_coef2) < epsilon) {
                    _graph.addUndirectedEdge(node1, node2);
                }
            }
        }

        System.out.println();
        System.out.println(MatrixUtils.toString(W));

        if (isOrientStrongerDirection()) {
            List <Node> _nodes = graph.getNodes();
            Graph graph3 = new EdgeListGraph(_nodes);

            for (int i = 0; i < _nodes.size(); i++) {
                for (int j = i + 1; j < _nodes.size(); j++) {
                    Node node1 = _nodes.get(i);
                    Node node2 = _nodes.get(j);

                    Edge edge1 = Edges.directedEdge(node1, node2);
                    Edge edge2 = Edges.directedEdge(node2, node1);

                    double a = Double.NaN;
                    double b = Double.NaN;

                    if (coef.get(edge1) != null) {
                        a = coef.get(edge1);
                    }

                    if (coef.get(edge2) != null) {
                        b = coef.get(edge2);
                    }

                    if (Double.isNaN(a)) a = 0.0;
                    if (Double.isNaN(b)) b = 0.0;

                    if (Math.abs(a) > Math.abs(b)) {
                        graph3.addEdge(edge1);
                    } else if (Math.abs(a) < Math.abs(b)) {
                        graph3.addEdge(edge2);
                    } else if (Math.abs(a) > 0 && Math.abs(b) > 0 && Math.abs(a) == Math.abs(b)) {
                        graph3.addUndirectedEdge(node1, node2);
                    }
                }
            }

            return graph3;
        }

        return _graph;
    }

    private void whitenData() {
        List <DataSet> whitenedData = new ArrayList <DataSet>();

        for (DataSet dataSet : dataSets) {

            // whiten...
            DoubleMatrix2D X = dataSet.getDoubleData().viewDice();
            int n = X.rows();
            int p = X.columns();
            int numComponents = p;

            Algebra alg = new Algebra();

            DoubleMatrix2D V = alg.mult(X, X.viewDice());
            V.assign(Mult.div(n));

            SingularValueDecomposition s = new SingularValueDecomposition(V);
            DoubleMatrix2D D = s.getS();

            for (int i = 0; i < D.rows(); i++) {
                D.set(i, i, 1.0 / Math.sqrt(D.get(i, i)));
            }

            DoubleMatrix2D E = s.getU();
            DoubleMatrix2D K1 = alg.mult(E, D);
            DoubleMatrix2D K2 = alg.mult(K1, E.viewDice());
            DoubleMatrix2D X1 = alg.mult(K2, X);

//
//                DoubleMatrix2D E = s.getU();
//                DoubleMatrix2D K = alg.mult(D, E.viewDice());
//                K = K.assign(Mult.mult(-1)); // This SVD gives -U from R's SVD.
//                K = K.viewPart(0, 0, numComponents, p);
//
//                DoubleMatrix2D X1 = alg.mult(K, X);

            whitenedData.add(ColtDataSet.makeData(dataSet.getVariables(), X1.viewDice()));
        }

        dataSets = whitenedData;
    }

    private double[][] estimateW(int dataIndex, List <DoubleMatrix2D> matrices, int numNodes, double min, double max, List <Mapping> allMappings) {
        System.out.println("Analyzing data set " + (dataIndex + 1));

        double[][] W = initializeW(numNodes);
        maxMappings(dataIndex, matrices, min, max, W, allMappings);

//        System.out.println(MatrixUtils.toString(W));
        return W;
    }

    private double[][] initializeW(int numNodes) {

        // Initialize W to I.
        double[][] W = new double[numNodes][numNodes];

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i == j) {
                    W[i][j] = 1.0;
                } else {
                    W[i][j] = 0.0;
                }
            }
        }
        return W;
    }

    private List <Mapping> createMappings(Graph graph, List <Node> nodes, int numNodes) {

        // Mark as parameters all non-adjacencies from the graph, excluding self edges.
        final List <Mapping> allMappings = new ArrayList <Mapping>();

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i == j) continue;

                Node v1 = nodes.get(i);
                Node v2 = nodes.get(j);

                Node w1 = graph.getNode(v1.getName());
                Node w2 = graph.getNode(v2.getName());

                if (graph.isAdjacentTo(w1, w2)) {
                    allMappings.add(new Mapping(i, j));
                }
            }
        }
        return allMappings;
    }

    private void maxMappings(final int dataIndex, final List <DoubleMatrix2D> dataSetMatrices, final double min,
                             final double max, final double[][] W, final List <Mapping> allMappings) {

        final int numNodes = W.length;

        for (int i = 0; i < numNodes; i++) {
            double maxScore = Double.NEGATIVE_INFINITY;
            double[] maxRow = new double[numNodes];

            double sensibleScore = Double.NEGATIVE_INFINITY;
            double[] sensibleRow = new double[numNodes];

            for (Mapping mapping : mappingsForRow(i, allMappings)) {
                W[mapping.getI()][mapping.getJ()] = 0;
            }

            try {
                optimizeNonGaussianity(i, dataIndex, dataSetMatrices, min, max, W, allMappings);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                continue;
            }

            double v = ngFullData(i, dataIndex, dataSetMatrices, W);

            if (Double.isNaN(v)) continue;
            if (v >= 9999) continue;

            double[] row = new double[numNodes];
            for (int k = 0; k < numNodes; k++) row[k] = W[i][k];

            if (v > maxScore) {
                maxScore = v;
                maxRow = row;
            }

//            boolean existsBigNumber = false;
//
//            for (int h = 0; h < numNodes; h++) {
//                if (row[h] != 1.0 && Math.abs(row[h]) >= max) {
//                    existsBigNumber = true;
//                }
//            }
//
//            if (v > sensibleScore && !existsBigNumber) {
//                sensibleScore = v;
//                sensibleRow = row;
//            }

//            if (sensibleScore > Double.NEGATIVE_INFINITY) {
//                for (int k = 0; k < numNodes; k++) W[i][k] = sensibleRow[k];
//            } else {
            for (int k = 0; k < numNodes; k++) W[i][k] = maxRow[k];
//            }
        }
    }

    private void optimizeNonGaussianity(final int rowIndex, final int dataIndex, final List <DoubleMatrix2D> dataSetMatrices,
                                        final double min, final double max, final double[][] W, List <Mapping> allMappings) {
        final List <Mapping> mappings = mappingsForRow(rowIndex, allMappings);

        double[] values = new double[mappings.size()];

        for (int k = 0; k < mappings.size(); k++) {
            Mapping mapping = mappings.get(k);
            values[k] = W[mapping.getI()][mapping.getJ()];
        }

        if (isStrongR2() && savedValues != null) {
            double[] _values = savedValues.get(rowIndex);

            if (_values != null && values.length == _values.length) {
                values = Arrays.copyOf(_values, _values.length);
            }
        }

        MultivariateFunction function = new MultivariateFunction() {
            @Override
            public double evaluate(double[] values) {
                for (int i = 0; i < values.length; i++) {
                    Mapping mapping = mappings.get(i);
                    W[mapping.getI()][mapping.getJ()] = values[i];
                }

                double v = ngFullData(rowIndex, dataIndex, dataSetMatrices, W);

                if (Double.isNaN(v)) return 10000;

                return -(v);
            }

            @Override
            public int getNumArguments() {
                return mappings.size();
            }

            @Override
            public double getLowerBound(int i) {
                return min;
            }

            @Override
            public double getUpperBound(int i) {
                return max;
            }

            public OrthogonalHints getOrthogonalHints() {
                return OrthogonalHints.Utils.getNull();
            }
        };

        function = new BoundsCheckedFunction(function, 10000);

        final double func_tolerance = 0.0000001;
        final double param_tolerance = 0.0000001;


//        class SimpleSearch {
//            public SimpleSearch() {
//            }
//
//            public void search(MultivariateFunction function, double[] values) {
//                double interval = 0.001;
//                findValley(function, values, interval);
////                double value = function.evaluate(values);
////
////                double[] copy = Arrays.copyOf(values, values.length);
////                double bump = 0.2;
////                double[] minValues = Arrays.copyOf(values, values.length);
////
////                for (int i = 0; i < copy.length; i++) {
////                    copy[i] += bump;
////                    findValley(function, copy, interval);
////                    double _value = function.evaluate(copy);
////
////                    if (_value < value) {
////                        value = _value;
////                        minValues = Arrays.copyOf(copy, copy.length);
////                    }
////
////                    copy[i] -= 2 * bump;
////                    findValley(function, copy, interval);
////                    _value = function.evaluate(copy);
////
////                    if (_value < value) {
////                        value = _value;
////                        minValues = Arrays.copyOf(copy, copy.length);
////                    }
////
////                    copy[i] += bump;
////                }
////
////                System.arraycopy(minValues, 0, values, 0, values.length);
//            }
//
//            private void findValley(MultivariateFunction function, double[] values, double interval) {
//                double value = function.evaluate(values);
//                double lastValue;
//                double[] gradient;
//
//                do {
//                    lastValue = value;
//                    gradient = NumericalDerivative.gradient(function, values);
//
//                    for (int i = 0; i < values.length; i++) {
//                        values[i] += gradient[i] * interval;
//                    }
//
//                    value = function.evaluate(values);
////                } while (length(gradient) > 1.0);
//                } while (value < lastValue - 0.1);
//            }
//
//            private double length(double[] v) {
//                double sum = 0.0;
//
//                for (int i = 0; i < v.length; i++) {
//                    sum += v[i] * v[i];
//                }
//
//                return Math.sqrt(sum);
//            }
//        }

//        SimpleSearch search = new SimpleSearch();
//        search.search(function, values);

//        if (mappings.size() == 0) {
////                search = new ConjugateGradientSearch();
//            MultivariateMinimum search;
//            search = new OrthogonalSearch();
////            search = new ConjugateGradientSearch();
//
//            search.optimize(function, values, func_tolerance, param_tolerance);
//        } else if (mappings.size() == 1) {
////                search = new ConjugateGradientSearch();
////            MultivariateMinimum search;
////            search = new OrthogonalSearch();
//////            search = new ConjugateGradientSearch();
////
////            search.optimize(function, values, func_tolerance, param_tolerance);
//
//            SimpleSearch search = new SimpleSearch();
//            search.search(function, values);
//
//            search.search(function, values);
//        } else
        {
            MultivariateMinimum search = new ConjugateDirectionSearch();
//
//            RandomSearch search = new RandomSearch();

//            {
//                double _fx = Double.POSITIVE_INFINITY;
//
//                @Override
//                public boolean stopCondition(double fx, double[] doubles, double x, double tolfx, boolean b) {
//                    System.out.println("fx = " + fx);
//                    if (Math.abs(fx - _fx) < 0.5) {
//                        System.out.println("true");
//                        return true;
//                    }
//                    _fx = fx;
//
//                    return super.stopCondition(fx, doubles, x, tolfx, b);    //To change body of overridden methods use File | Settings | File Templates.
//                }
//            };

            search.optimize(function, values, func_tolerance, param_tolerance);

//            MultivariateMinimum search2 = new edu.cmu.tetrad.sem.ConjugateDirectionSearch();
//            search2.optimize(function, values, func_tolerance, param_tolerance);

            savedValues.put(rowIndex, Arrays.copyOf(values, values.length));
        }

    }

    private List <Mapping> mappingsForRow(int rowIndex, List <Mapping> allMappings) {
        final List <Mapping> mappings = new ArrayList <Mapping>();

        for (Mapping mapping : allMappings) {
            if (mapping.getI() == rowIndex) mappings.add(mapping);
        }
        return mappings;
    }

    private void optimizeOrthogonality(final int rowIndex, final double min, final double max, final double[][] W, List <Mapping> allMappings, final int numNodes) {
        final List <Mapping> mappings = mappingsForRow(rowIndex, allMappings);

        double[] values = new double[mappings.size()];

        for (int k = 0; k < mappings.size(); k++) {
            Mapping mapping = mappings.get(k);
            values[k] = W[mapping.getI()][mapping.getJ()];
        }

        MultivariateFunction function = new MultivariateFunction() {
            double metric;

            @Override
            public double evaluate(double[] values) {
                for (int i = 0; i < values.length; i++) {
                    Mapping mapping = mappings.get(i);
                    W[mapping.getI()][mapping.getJ()] = values[i];
                }

                double sum = 0.0;

                for (int g = 0; g <= numNodes; g++) {
                    for (int h = g + 1; h < numNodes; h++) {
                        double dotProduct = 0.0;

                        for (int k = 0; k < numNodes; k++) {
                            sum += W[g][k] * W[h][k];
                        }

                        sum += Math.abs(dotProduct);
                    }
                }

                return sum;
            }

            @Override
            public int getNumArguments() {
                return mappings.size();
            }

            @Override
            public double getLowerBound(int i) {
                return min;
            }

            @Override
            public double getUpperBound(int i) {
                return max;
            }

            public OrthogonalHints getOrthogonalHints() {
                return OrthogonalHints.Utils.getNull();
            }
        };

        final double func_tolerance = 0.0001;
        final double param_tolerance = 0.0001;

        MultivariateMinimum search;

        if (mappings.size() <= 1) {
//                search = new ConjugateGradientSearch();
            search = new OrthogonalSearch();
        } else {
            search = new ConjugateDirectionSearch();
        }

        search.optimize(function, values, func_tolerance, param_tolerance);
    }


    private DoubleMatrix2D getRow(DoubleMatrix2D w2, int i) {
        DoubleMatrix2D row = new DenseDoubleMatrix2D(1, w2.columns());
        return row;
    }


    private void maxMappings2(final int dataIndex, final List <DoubleMatrix2D> dataSetMatrices, final double min,
                              final double max, final double[][] W, final List <Mapping> mappings) {

        double[] values = new double[mappings.size()];

        MultivariateFunction function = new MultivariateFunction() {
            double metric;

            @Override
            public double evaluate(double[] values) {
                for (int i = 0; i < values.length; i++) {
                    Mapping mapping = mappings.get(i);
                    W[mapping.getI()][mapping.getJ()] = values[i];
                }

                return -sumNonGaussianities(dataIndex, dataSetMatrices, W);
            }

            @Override
            public int getNumArguments() {
                return mappings.size();
            }

            @Override
            public double getLowerBound(int i) {
                return min;
            }

            @Override
            public double getUpperBound(int i) {
                return max;
            }

            public OrthogonalHints getOrthogonalHints() {
                return null;
            }
        };

        final double func_tolerance = 0.0001;
        final double param_tolerance = 0.0001;

        MultivariateMinimum search = new ConjugateDirectionSearch();
//        MultivariateMinimum search = new ConjugateGradientSearch();
//        MultivariateMinimum search = new OrthogonalSearch();
        search.optimize(function, values, func_tolerance, param_tolerance);
    }

    private void maxMappings3(Graph graph, List <Node> nodes, final int dataIndex, final List <DoubleMatrix2D> dataSetMatrices, final double min,
                              final double max, final double[][] _W, final List <Mapping> allMappings) {

        graph = GraphUtils.replaceNodes(graph, nodes);
        final double[][] W = MatrixUtils.copyOf(_W);

        System.out.println(MatrixUtils.toString(W));

        int numNodes = W.length;

        final List <Mapping> mappings = new ArrayList <Mapping>();

        for (int i = 0; i < numNodes; i++) {
            final int rowIndex = i;

            System.out.println("Row: " + rowIndex);

            mappings.clear();

            final List <Integer> rowIndices = new ArrayList <Integer>();
            rowIndices.add(i);

            for (Node node : graph.getAdjacentNodes(nodes.get(i))) {
                rowIndices.add(nodes.indexOf(node));
            }

            System.out.println(rowIndices);

            for (int h : rowIndices) {
                for (Mapping mapping : allMappings) {
                    if (mapping.getI() == h) {
                        mappings.add(mapping);
                    }
                }
            }

            double[] values = new double[mappings.size()];

            for (int j = 0; j < mappings.size(); j++) {
                Mapping m = mappings.get(j);
                double value = W[m.getI()][m.getJ()];
                values[j] = Double.isNaN(value) ? 0 : value;
            }

            MultivariateFunction function = new MultivariateFunction() {
                double metric;

                @Override
                public double evaluate(double[] values) {
                    for (int i = 0; i < values.length; i++) {
                        Mapping mapping = mappings.get(i);
                        W[mapping.getI()][mapping.getJ()] = values[i];
                    }

                    double v = -sumNonGaussianities(rowIndices, dataIndex, dataSetMatrices, W);
//                    System.out.println(v);
                    return -v;

//                    return -sumNonGaussianities(dataIndex, dataSetMatrices, W);
                }

                @Override
                public int getNumArguments() {
                    return mappings.size();
                }

                @Override
                public double getLowerBound(int i) {
                    return min;
                }

                @Override
                public double getUpperBound(int i) {
                    return max;
                }

                public OrthogonalHints getOrthogonalHints() {
                    return null;
                }
            };

            final double func_tolerance = 0.000001;
            final double param_tolerance = 0.000001;

//        RandomSearch search = new RandomSearch();
//
//        search.optimize(function, values, func_tolerance, param_tolerance);

//        MultivariateMinimum search = new OrthogonalSearch();

            MultivariateMinimum search;

            if (mappings.size() <= 2) {
//                search = new ConjugateGradientSearch();
                search = new OrthogonalSearch();
            } else {
                search = new edu.cmu.tetrad.sem.ConjugateDirectionSearch();
            }

            search.optimize(function, values, func_tolerance, param_tolerance);

            for (int j = 0; j < numNodes; j++) {
                _W[i][j] = W[i][j];
            }
        }
    }

    public double sumNonGaussianities(int dataIndex, List <DoubleMatrix2D> dataSetMatrices, double[][] W) {
        double sum = 0.0;

        for (int i = 0; i < W.length; i++) {
            double ng = ngFullData(i, dataIndex, dataSetMatrices, W);

            if (!Double.isNaN(ng)) {
                sum += ng;
            }
        }

        return sum;
    }

    public double sumNonGaussianities(List <Integer> rows, int dataIndex, List <DoubleMatrix2D> dataSetMatrices, double[][] W) {
        double sum = 0.0;

        for (int i : rows) {
            double ng = ngFullData(i, dataIndex, dataSetMatrices, W);

            if (!Double.isNaN(ng)) {
                sum += ng;
            }
        }

        return sum;
    }

    public double ngFullData(int rowIndex, int dataIndex, List <DoubleMatrix2D> dataSetMatrices, double[][] W) {
        DoubleMatrix2D data = dataSetMatrices.get(dataIndex);
        double[] col = new double[data.rows()];

        for (int i = 1; i < data.rows(); i++) {
            double d = 0.0;

            // Node _x given parents. Its coefficient is fixed at 1. Also, coefficients for all
            // other variables not neighbors of _x are fixed at zero.
            for (int j = 0; j < data.columns(); j++) {
                double coef = W[rowIndex][j];
                Double value = data.get(i, j);
                double product = coef * value;

//                if (Double.isNaN(product)) {
//                    System.out.println();
//                    return Double.NaN;
//                }


//                d += product;

                if (!Double.isNaN(product)) {
                    d += product;
                }
            }

            col[i] = d;
        }

        col = removeNaN(col);

        if (col.length == 0) {
            System.out.println();
            return Double.NaN;
        }

//        DoubleMatrix2D W2 = new DenseDoubleMatrix2D(W);
//        DoubleMatrix2D X = data;
//        new Algebra().mult(W2, X.viewDice());
//        DoubleMatrix2D cov = new Algebra().mult(W2, W2.viewDice());
//        DoubleMatrix2D corr = DataUtils.corr(cov);
//
//        double corrSum = 0.0;
//
//        for (int i = 0; i < W.length; i++) {
//            if (rowIndex == i) continue;
//            corrSum += Math.abs(corr.get(rowIndex, i));
//        }

        return aSquared(col);
//        return Math.abs(StatUtils.kurtosis(removeNaN(col)));
//        return logcosh(removeNaN(col));
//        return exp2(removeNaN(col));

//        col = removeNaN(col);

//        double v1 = 0, v2 = 0;
//
//        for (int k = 0; k < col.length; k++) {
//            double u = col[k];
//            v1 += Math.pow(Math.log(Math.cosh(u)) - .37457, 2.0);
//            v2 += Math.pow(u * Math.exp(Math.pow(-u, 2) / 2), 2.0);
//        }
//
//        double h = 7.4129 * v1 / col.length + 79.047 * v2 / col.length;
//
//        return h;

//        for (int k = 0; k < col.length; k++) {
//            double v = Math.log(Math.cosh((col[k])));
//            col[k] = v;
//        }


    }

    private Graph tanhGraph(Graph graph) {
        DataSet dataSet;

        if (isCenterResiduals()) {
            List <DataSet> standardized = DataUtils.standardizeData(dataSets);
            dataSet = DataUtils.concatenateDataSets(standardized);
        } else {
            List <DataSet> centered = DataUtils.centerData(dataSets);
            DataSet concat = DataUtils.concatenateDataSets(centered);
            dataSet = DataUtils.standardizeData(concat);
        }

        Graph _graph = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

            double sumX = 0.0;
            int countX = 0;

            Node _x = dataSet.getVariable(x.getName());
            Node _y = dataSet.getVariable(y.getName());

            List <double[]> ret = prepareData(dataSet, _x, _y, false, false);
            double[] xData = ret.get(0);
            double[] yData = ret.get(1);

            for (int i = 0; i < xData.length; i++) {
                double x0 = xData[i];
                double y0 = yData[i];

                double termX = (x0 * Math.tanh(y0) - Math.tanh(x0) * y0);

                sumX += termX;
                countX++;
            }

            double R = sumX / countX;

            System.out.println("R = " + R);

            double rhoX = regressionCoef(xData, yData);
            R *= rhoX;

            if (R > 0) {
                _graph.addDirectedEdge(x, y);
            } else {
                _graph.addDirectedEdge(y, x);
            }
        }

        return _graph;
    }

    private Graph skewGraph(Graph graph) {
        DataSet dataSet;

        if (isCenterResiduals()) {
            List <DataSet> standardized = DataUtils.standardizeData(dataSets);
            dataSet = DataUtils.concatenateDataSets(standardized);
        } else {
            List <DataSet> centered = DataUtils.centerData(dataSets);
            DataSet concat = DataUtils.concatenateDataSets(centered);
            dataSet = DataUtils.standardizeData(concat);
        }

        Graph _graph = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

            double sumX = 0.0;
            int countX = 0;

            Node _x = dataSet.getVariable(x.getName());
            Node _y = dataSet.getVariable(y.getName());

            List <double[]> ret = prepareData(dataSet, _x, _y, false, false);
            double[] xData = ret.get(0);
            double[] yData = ret.get(1);

            for (int i = 0; i < xData.length; i++) {
                double x0 = xData[i];
                double y0 = yData[i];

                double termX = x0 * x0 * y0 - x0 * y0 * y0;

                sumX += termX;
                countX++;
            }

            double R = sumX / countX;

            double rhoX = regressionCoef(xData, yData);
            R *= rhoX;

            if (R > 0) {
                _graph.addDirectedEdge(x, y);
            } else {
                _graph.addDirectedEdge(y, x);
            }
        }

        return _graph;

    }

    // Their described method

    private Graph robustSkewGraph(Graph graph) {
        DataSet dataSet;

        if (isCenterResiduals()) {
            List <DataSet> standardized = DataUtils.standardizeData(dataSets);
            dataSet = DataUtils.concatenateDataSets(standardized);
        } else {
            List <DataSet> centered = DataUtils.centerData(dataSets);
            DataSet concat = DataUtils.concatenateDataSets(centered);
            dataSet = DataUtils.standardizeData(concat);
        }

        Graph _graph = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

            double sumX = 0.0;
            int countX = 0;

            Node _x = dataSet.getVariable(x.getName());
            Node _y = dataSet.getVariable(y.getName());

            List <double[]> ret = prepareData(dataSet, _x, _y, false, false);
            double[] xData = ret.get(0);
            double[] yData = ret.get(1);

            for (int i = 0; i < xData.length; i++) {
                double x0 = xData[i];
                double y0 = yData[i];

                double termX = -x0 * g(y0) + y0 * g(x0);

                sumX += termX;
                countX++;
            }

            double R = sumX / countX;

            double rhoX = regressionCoef(xData, yData);
            System.out.println("rhoX = " + rhoX);

            R *= rhoX;

            if (R > 0) {
                _graph.addDirectedEdge(x, y);
            } else {
                _graph.addDirectedEdge(y, x);
            }
        }

        return _graph;
    }

//    private Graph tanhNGraph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, false, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = -(x0 * Math.tanh(y0) - Math.tanh(x0) * y0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            System.out.println("R = " + R);
//
//            double rhoX = regressionCoef(xData, yData);
//            R *= rhoX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

    // With data preparation.

//    private Graph tanhGraphStar(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, true);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = (x0 * Math.tanh(y0) - Math.tanh(x0) * y0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            System.out.println("R = " + R);
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

//    private Graph tanhGraphStar2(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, false, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = (x0 * Math.tanh(y0) - Math.tanh(x0) * y0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            System.out.println("R = " + R);
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

//    private Graph c4Graph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, true);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = x0 * x0 * x0 * y0 - x0 * y0 * y0 * y0;
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
////            double rhoX = regressionCoef(xData, yData);
////            R *= rhoX;
//
////            if (isStrongR2()) {
////                double kurt = StatUtils.kurtosis(xData);
////                R *= kurt;
////            }
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//
//    }

    // Their described method. Equation 26.

//    private Graph skewGraph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = x0 * x0 * y0 - x0 * y0 * y0;
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            double rhoX = regressionCoef(xData, yData);
//            R *= rhoX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//
//    }

    private List <double[]> prepareData(DataSet concatData, Node _x, Node _y, boolean skewCorrection, boolean coefCorrection) {
        int xIndex = concatData.getColumn(_x);
        int yIndex = concatData.getColumn(_y);

        double[] xData = concatData.getDoubleData().viewColumn(xIndex).toArray();
        double[] yData = concatData.getDoubleData().viewColumn(yIndex).toArray();

        List <Double> xValues = new ArrayList <Double>();
        List <Double> yValues = new ArrayList <Double>();

        for (int i = 0; i < concatData.getNumRows(); i++) {
            if (!Double.isNaN(xData[i]) && !Double.isNaN(yData[i])) {
                xValues.add(xData[i]);
                yValues.add(yData[i]);
            }
        }

        xData = new double[xValues.size()];
        yData = new double[yValues.size()];

        for (int i = 0; i < xValues.size(); i++) {
            xData[i] = xValues.get(i);
            yData[i] = yValues.get(i);
        }

        if (skewCorrection) {
            double xSkew = StatUtils.skewness(xData);
            double ySkew = StatUtils.skewness(yData);

            for (int i = 0; i < xData.length; i++) xData[i] *= Math.signum(xSkew);
            for (int i = 0; i < yData.length; i++) yData[i] *= Math.signum(ySkew);
        }

        if (coefCorrection) {
            double coefX = 0;
            try {
                coefX = regressionCoef(xData, yData);
            } catch (Exception e) {
                coefX = Double.NaN;
            }

            double coefY = 0;

            try {
                coefY = regressionCoef(yData, xData);
            } catch (Exception e) {
                coefY = Double.NaN;
            }

            for (int i = 0; i < xData.length; i++) xData[i] *= Math.signum(coefX);
            for (int i = 0; i < yData.length; i++) yData[i] *= Math.signum(coefY);

        }

        List <double[]> ret = new ArrayList <double[]>();
        ret.add(xData);
        ret.add(yData);

        return ret;
    }

    // Our data prep.

//    private Graph skewStarGraph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, true);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = x0 * x0 * y0 - x0 * y0 * y0;
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//
//    }

//    private Graph skewStar2Graph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, false, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = x0 * x0 * y0 - x0 * y0 * y0;
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//
//    }

//    private Graph robustSkewGraph(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = -x0 * g(y0) + y0 * g(x0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            double rhoX = regressionCoef(xData, yData);
//            System.out.println("rhoX = " + rhoX);
//
//            R *= rhoX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

    private double regressionCoef(double[] xValues, double[] yValues) {
        List <Node> v = new ArrayList <Node>();
        v.add(new GraphNode("x"));
        v.add(new GraphNode("y"));

        DoubleMatrix2D bothData = new DenseDoubleMatrix2D(xValues.length, 2);

        for (int i = 0; i < xValues.length; i++) {
            bothData.set(i, 0, xValues[i]);
            bothData.set(i, 1, yValues[i]);
        }

        Regression regression2 = new RegressionDataset(bothData, v);

        RegressionResult result = null;
        try {
            result = regression2.regress(v.get(0), v.get(1));
        } catch (Exception e) {
            return Double.NaN;
        }
        return result.getCoef()[1];
    }

//    private Graph robustSkewGraphStar(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, true, true);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = -x0 * g(y0) + y0 * g(x0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

//    private Graph robustSkewGraphStar2(Graph graph) {
//        DataSet dataSet;
//
//        if (isCenterResiduals()) {
//            List<DataSet> standardized = DataUtils.standardizeData(dataSets);
//            dataSet = DataUtils.concatenateDataSets(standardized);
//        } else {
//            List<DataSet> centered = DataUtils.centerData(dataSets);
//            DataSet concat = DataUtils.concatenateDataSets(centered);
//           dataSet = DataUtils.standardizeData(concat);
//        }
//
//        Graph _graph = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            double sumX = 0.0;
//            int countX = 0;
//
//            Node _x = dataSet.getVariable(x.getName());
//            Node _y = dataSet.getVariable(y.getName());
//
//            List<double[]> ret = prepareData(dataSet, _x, _y, false, false);
//            double[] xData = ret.get(0);
//            double[] yData = ret.get(1);
//
//            for (int i = 0; i < xData.length; i++) {
//                double x0 = xData[i];
//                double y0 = yData[i];
//
//                double termX = -x0 * g(y0) + y0 * g(x0);
//
//                sumX += termX;
//                countX++;
//            }
//
//            double R = sumX / countX;
//
//            if (R > 0) {
//                _graph.addDirectedEdge(x, y);
//            } else {
//                _graph.addDirectedEdge(y, x);
//            }
//        }
//
//        return _graph;
//    }

    private DataSet stripNaN(DataSet concatData, Node x, Node y) {
        DoubleMatrix2D data = concatData.getDoubleData();
        int colx = concatData.getColumn(x);
        int coly = concatData.getColumn(y);
        List <Integer> goodrows = new ArrayList <Integer>();

        for (int i = 0; i < data.rows(); i++) {
            if (Double.isNaN(data.get(i, colx))) {
                continue;
            }

            if (Double.isNaN(data.get(i, coly))) {
                continue;
            }

            goodrows.add(i);
        }

        int[] cols = new int[data.columns()];
        for (int i = 0; i < data.columns(); i++) cols[i] = i;
        int[] rows = new int[goodrows.size()];
        for (int i = 0; i < goodrows.size(); i++) rows[i] = goodrows.get(i);

        DoubleMatrix2D reducedData = data.viewSelection(rows, cols);
        DataSet reducedDataSet = ColtDataSet.makeContinuousData(concatData.getVariables(), reducedData);
        return reducedDataSet;
    }

    private double g(double x) {
        return Math.log(Math.cosh(Math.max(x, 0)));

    }

    // Strips rows out that appear as NaN for x or y.

    private Graph ruleR102(Graph graph) {
        Graph graph2 = new EdgeListGraph(graph.getNodes());
        Map <Edge, Double> coef = new HashMap <Edge, Double>();

        for (Node node : graph.getNodes()) {
            List <Node> adj = graph.getAdjacentNodes(node);
            double b[][] = new double[dataSets.size()][];

            for (int index = 0; index < dataSets.size(); index++) {
                double[] a = maximizeNonGaussianity(index, dataSets, matrices, node, adj);
                b[index] = a;
            }

            double[] _a = new double[b[0].length];

            for (int k = 0; k < b[0].length; k++) {
                int sum = 0;
                int count = 0;

                for (int l = 0; l < b.length; l++) {
                    double u = b[l][k];

                    if (!Double.isNaN(u)) {
                        if (Math.abs(u) >= epsilon) {
                            sum += 1;
                        }

                        count++;
                    }
                }

                double avg = sum / (double) count;
                _a[k] = avg;
            }


            for (int i = 0; i < adj.size(); i++) {
                Node _node = adj.get(i);
                double a = _a[i];

                System.out.println("Ratio for " + _node + " --> " + node + " = " + a);

                Edge edge = Edges.directedEdge(_node, node);

                if (Math.abs(a) > getAlpha()) {
                    graph2.addEdge(edge);
                }

                coef.put(edge, a);
            }
        }

        if (isOrientStrongerDirection()) {
            List <Node> nodes = graph.getNodes();
            Graph graph3 = new EdgeListGraph(nodes);

            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node node1 = nodes.get(i);
                    Node node2 = nodes.get(j);

                    Edge edge1 = Edges.directedEdge(node1, node2);
                    Edge edge2 = Edges.directedEdge(node2, node1);

                    double a = Double.NaN;
                    double b = Double.NaN;

                    if (coef.get(edge1) != null) {
                        a = coef.get(edge1);
                    }

                    if (coef.get(edge2) != null) {
                        b = coef.get(edge2);
                    }

                    if (!Double.isNaN(a) && !Double.isNaN(b)) {
                        if (Math.abs(a) > Math.abs(b)) {
                            graph3.addEdge(edge1);
                        } else if (Math.abs(a) < Math.abs(b)) {
                            graph3.addEdge(edge2);
                        } else if (Math.abs(a) > 0 && Math.abs(b) > 0 && Math.abs(a) == Math.abs(b)) {
                            graph3.addUndirectedEdge(node1, node2);
                        }

//                        if (a > b) {
//                            graph3.addEdge(edge1);
//                        } else if (a < b) {
//                            graph3.addEdge(edge2);
//                        } else if (a == b) {
//                            graph3.addUndirectedEdge(node1, node2);
//                        }
                    }
                }
            }

            return graph3;
        }

        return graph2;
    }

    private boolean isTwoCycle(Graph graph, Node x, Node y) {
        List <Edge> edges = graph.getEdges(x, y);
        return edges.size() == 2;
    }

    private boolean isUndirected(Graph graph, Node x, Node y) {
        List <Edge> edges = graph.getEdges(x, y);
        if (edges.size() == 1) {
            Edge edge = graph.getEdge(x, y);
            return Edges.isUndirectedEdge(edge);
        }

        return false;
    }

//    private void ruleR19(Graph skeleton, Graph graph, List<Node> nodes) {
//        List<DataSet> centeredData = DataUtils.centerData(this.dataSets);
//        setDataSets(centeredData);
//
//        for (Node node : nodes) {
//            SortedMap<Double, String> scoreReports = new TreeMap<Double, String>();
//
//            List<Node> adj = new ArrayList<Node>();
//
//            for (Node _node : skeleton.getAdjacentNodes(node)) {
//                if (knowledge.edgeForbidden(_node.getName(), node.getName())) {
//                    continue;
//                }
//
//                adj.add(_node);
//            }
//
//            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
//            int[] choice;
//            double maxScore = Double.NEGATIVE_INFINITY;
//            List<Node> parents = null;
//
//            while ((choice = gen.next()) != null) {
//                List<Node> _parents = GraphUtils.asList(choice, adj);
//
//                double score = score(node, _parents);
//                scoreReports.put(-score, _parents.toString());
//
////                if (_parents.isEmpty()) {
////                    continue;
////                }
//
//                if (score > maxScore) {
//                    maxScore = score;
//                    parents = _parents;
//                }
//            }
//
//            for (double score : scoreReports.keySet()) {
//                TetradLogger.getInstance().log("score", "For " + node + " parents = " + scoreReports.get(score) + " score = " + -score);
//            }
//
//            TetradLogger.getInstance().log("score", "");
//
//            if (parents == null) {
//                continue;
//            }
//
//            if (normal(node, parents)) continue;
//
//            for (Node _node : adj) {
//                if (parents.contains(_node)) {
//                    Edge parentEdge = Edges.directedEdge(_node, node);
//
//                    if (!graph.containsEdge(parentEdge)) {
//                        graph.addEdge(parentEdge);
//                    }
//                }
//            }
//        }
//
//        for (Edge edge : skeleton.getEdges()) {
//            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
//                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
//            }
//        }
//    }

    private boolean normal(Node node, Node... parents) {
        List <Node> _parents = new ArrayList <Node>();

        for (Node _node : parents) {
            _parents.add(_node);
        }

        return normal(node, _parents);
    }

    private boolean normal(Node node, List <Node> parents) {
        if (getAlpha() > .999) {
            return false;
        }

        return pValue(node, parents) > getAlpha();
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public void setZeta(double zeta) {
        this.zeta = zeta;
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

    private void resolveOneEdgeMaxR7(Graph oldGraph, Graph newGraph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List <Node> condxMinus = pathBlockingSet(oldGraph, x, y, false);
        List <Node> condxPlus = pathBlockingSet(oldGraph, x, y, true);
        List <Node> condyMinus = pathBlockingSet(oldGraph, y, x, false);
        List <Node> condyPlus = pathBlockingSet(oldGraph, y, x, true);

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

        System.out.println("NG(X|N) = " + xMinus + " NG(X|Y,N) = " + xPlus + " NG(Y|N) = " + yMinus + " NG(Y|X,N) = " + yPlus);
        System.out.println("delta X = " + deltaX + " delta Y = " + deltaY);

        newGraph.removeEdges(x, y);

        if (deltaX < deltaY) {
            newGraph.addDirectedEdge(x, y);
        } else {
            newGraph.addDirectedEdge(y, x);
        }
    }

    private double distance(double[] d1, double[] d2) {
        double sum = 0.0;

        for (int i = 0; i < d1.length; i++) {
            sum += Math.pow(d1[i] - d2[i], 2.0);
        }

        return Math.sqrt(sum);
    }

    private void resolveOneEdgeMaxR7Iterated(Graph oldGraph, Graph newGraph, Node x, Node y) {
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        List <Node> condxMinus = pathBlockingSet2(oldGraph, x, y, false);
        List <Node> condxPlus = pathBlockingSet2(oldGraph, x, y, true);
        List <Node> condyMinus = pathBlockingSet2(oldGraph, y, x, false);
        List <Node> condyPlus = pathBlockingSet(oldGraph, y, x, true);

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

        System.out.println("NG(X|N) = " + xMinus + " NG(X|Y,N) = " + xPlus + " NG(Y|N) = " + yMinus + " NG(Y|X,N) = " + yPlus);
        System.out.println("delta X = " + deltaX + " delta Y = " + deltaY);

        newGraph.removeEdges(x, y);

        if (deltaX < deltaY) {
            newGraph.addDirectedEdge(x, y);
        } else {
            newGraph.addDirectedEdge(y, x);
        }
    }

    private double[] maximizeNonGaussianity1(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
        double min = -1.0;
        double max = 1.0;
        int numIntervals = 50;

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];
        double[] coef2 = new double[nodes.size()];

        double[] sums = new double[nodes.size()];
        double[] avg = new double[nodes.size()];
        double[] lastAvg = new double[nodes.size()];
        int count = 0;

        int h = 0;

        do {
            for (int i = 0; i < nodes.size(); i++) {
                double firstBump = min;
                coef[i] = min;
                double firstBumpValue = ng(index, dataSets, matrices, x, nodes, coef);

                boolean ascending = false;
                double lastA = min;
                double lastValue = firstBumpValue;

                double bumpFound = min;
                double bumpFoundValue = firstBumpValue;

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng(index, dataSets, matrices, x, nodes, coef);

                    if (Double.isNaN(v)) {
                        firstBump = Double.NaN;
                        break;
                    }

                    if (!ascending && v > lastValue) {
                        ascending = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;
                        bumpFound = lastA;
                        bumpFoundValue = lastValue;
                    } else if (!ascending && v < lastValue) {
                        if (bumpFoundValue > firstBumpValue) {
                            firstBump = bumpFound;
                            break;
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }

                // Print out values in an interval.
                {
                    double[] _coef = Arrays.copyOf(coef, coef.length);
                    double _min = -100.0;
                    double _max = 100.0;
                    int _numIntervals = 25;

                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
                        double a = _min + s2 * ((_max - _min) / _numIntervals);
                        _coef[i] = a;
                        double v = ng(index, dataSets, matrices, x, nodes, _coef);

                        System.out.println(a + "\t" + v);
                    }

                    System.out.println("Bump = " + firstBump);
                }

                coef[i] = firstBump;
            }

            for (int m = 0; m < coef.length; m++) {
                sums[m] += coef[m];
            }

            count++;

            for (int m = 0; m < sums.length; m++) {
                avg[m] = sums[m] / count;
            }

            if (Arrays.equals(coef, coef2)) {
                System.out.println(Arrays.toString(coef));
                return coef;
            }

            if (distance(avg, lastAvg) < 0.001) {
                break;
            }

            System.arraycopy(avg, 0, lastAvg, 0, avg.length);
            System.arraycopy(coef, 0, coef2, 0, coef.length);
        } while (++h <= 30);

        System.out.println(Arrays.toString(avg));

        return avg;
    }

    private double[] maximizeNonGaussianity2(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
        double min = -5.0;
        double max = 5.0;
        int numIntervals = 50;

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];
        double[] coef2 = new double[nodes.size()];

        double[] sums = new double[nodes.size()];
        double[] avg = new double[nodes.size()];
        double[] lastAvg = new double[nodes.size()];
        int count = 0;

        int h = 0;

        do {
            for (int i = 0; i < nodes.size(); i++) {
//                System.out.println(nodes.get(i) + "-->" + x);

                double maxBump = Double.NaN;
                double maxBumpValue = Double.NEGATIVE_INFINITY;

                boolean ascending = false;
                boolean ascended = false;

                coef[i] = min;
                double lastA = min;
                double lastValue = ng(index, dataSets, matrices, x, nodes, coef);

                double bumpFound;
                double bumpFoundValue;

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng(index, dataSets, matrices, x, nodes, coef);

                    if (!ascending && v > lastValue) {
                        ascending = true;
                        ascended = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;
                        bumpFound = lastA;
                        bumpFoundValue = lastValue;

                        if (bumpFoundValue > maxBumpValue && ascended) {
                            maxBump = bumpFound;
                            maxBumpValue = bumpFoundValue;
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }

                // Print out values in an interval.
                {
                    double[] _coef = Arrays.copyOf(coef, coef.length);
                    double _min = -5.0;
                    double _max = 5.0;
                    int _numIntervals = 25;

                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
                        double a = _min + s2 * ((_max - _min) / _numIntervals);
                        _coef[i] = a;
                        double v = ng(index, dataSets, matrices, x, nodes, _coef);

                        System.out.println(a + "\t" + v);
                    }

                    System.out.println("Max bump = " + maxBump);
                }

                coef[i] = maxBump;
            }

            for (int m = 0; m < coef.length; m++) {
                sums[m] += coef[m];
            }

            count++;

            for (int m = 0; m < sums.length; m++) {
                avg[m] = sums[m] / count;
            }

            if (Arrays.equals(coef, coef2)) {
                System.out.println(Arrays.toString(coef));
                return coef;
            }

            if (distance(avg, lastAvg) < 0.001) {
                break;
            }

            System.arraycopy(avg, 0, lastAvg, 0, avg.length);
            System.arraycopy(coef, 0, coef2, 0, coef.length);
        } while (++h <= 30);

        System.out.println(Arrays.toString(avg));

        return avg;
    }

    private double[] maximizeNonGaussianity3(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
        double min = 0.0;
        double max = 2.0;
        int numIntervals = 50;

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];
        double[] coef2 = new double[nodes.size()];

        double[] sums = new double[nodes.size()];
        double[] avg = new double[nodes.size()];
        double[] lastAvg = new double[nodes.size()];
        int count = 0;

        int h = 0;

        do {
            for (int i = 0; i < nodes.size(); i++) {
                double firstBump = min;
                coef[i] = min;
                double firstBumpValue = ng(index, dataSets, matrices, x, nodes, coef);

                boolean ascended = false;

                boolean ascending = false;
                double lastA = min;
                double lastValue = firstBumpValue;

                double bumpFound = min;
                double bumpFoundValue = firstBumpValue;

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng(index, dataSets, matrices, x, nodes, coef);

                    if (Double.isNaN(v)) {
                        firstBump = Double.NaN;
                        break;
                    }

                    if (!ascending && v > lastValue) {
                        ascending = true;
                        ascended = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;
                        bumpFound = lastA;
                        bumpFoundValue = lastValue;
                    } else if (!ascending && v < lastValue) {
                        if (ascended) {
                            firstBump = bumpFound;
                            break;
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }
//
//                for (int s = 0; s <= numIntervals; s++) {
//                    double _min = -10.0;
//                    double _max = 10.0;
//                    double a = _min + s * ((_max - _min) / numIntervals);
//                    double[] _coef = Arrays.copyOf(coef, coef.length);
//                    _coef[i] = a;
//                    double v = ng(index, dataSets, matrices, x, nodes, _coef);
//
//                    System.out.println(a + "\t" + v);
//                }

                System.out.println("Bump = " + firstBump);
                coef[i] = firstBump;
            }

            for (int m = 0; m < coef.length; m++) {
                sums[m] += coef[m];
            }

            count++;

            for (int m = 0; m < sums.length; m++) {
                avg[m] = sums[m] / count;
            }

            if (Arrays.equals(coef, coef2)) {
                System.out.println(Arrays.toString(coef));
                return coef;
            }

            if (distance(avg, lastAvg) < 0.001) {
                break;
            }

            System.arraycopy(avg, 0, lastAvg, 0, avg.length);
            System.arraycopy(coef, 0, coef2, 0, coef.length);
        } while (++h <= 30);

        System.out.println(Arrays.toString(avg));

        return avg;
    }

    private double[] maximizeNonGaussianity4(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
        double min = -2.0;
        double max = 2.0;
        int numIntervals = 100;

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];
        double[] coef2 = new double[nodes.size()];

        double[] sums = new double[nodes.size()];
        double[] avg = new double[nodes.size()];
        double[] lastAvg = new double[nodes.size()];
        int count = 0;

        int h = 0;

        do {
            for (int i = 0; i < nodes.size(); i++) {
                List <Double> localMaxima = new ArrayList <Double>();
                List <Double> localMaximaValues = new ArrayList <Double>();

                boolean ascending = false;
                boolean ascended = false;

                coef[i] = min;
                double lastA = min;
                double lastValue = ng(index, dataSets, matrices, x, nodes, coef);

                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);
                    coef[i] = a;
                    double v = ng(index, dataSets, matrices, x, nodes, coef);

                    if (!ascending && v > lastValue) {
                        ascending = true;
                        ascended = true;
                    } else if (ascending && v < lastValue) {
                        ascending = false;

                        if (ascended) {
                            localMaxima.add(lastA);
                            localMaximaValues.add(lastValue);
                        }
                    }

                    lastA = a;
                    lastValue = v;
                }

                double minA = Double.NaN;

                for (int k = 0; k < localMaxima.size(); k++) {
                    if (/*localMaxima.get(k) >= 0.0 &&*/ (Double.isNaN(minA) || (Math.abs(localMaxima.get(k)) < minA))) {
                        minA = localMaxima.get(k);
                    }
                }

//                // Print out values in an interval.
//                {
//                    double[] _coef = Arrays.copyOf(coef, coef.length);
//                    double _min = -5.0;
//                    double _max = 5.0;
//                    int _numIntervals = 25;
//
//                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
//                        double a = _min + s2 * ((_max - _min) / _numIntervals);
//                        _coef[i] = a;
//                        double v = ng(index, dataSets, matrices, x, nodes, _coef);
//
//                        System.out.println(a + "\t" + v);
//                    }
//
//                    System.out.println("Max bump = " + minA);
//                }

                coef[i] = minA;
            }

            for (int m = 0; m < coef.length; m++) {
                sums[m] += coef[m];
            }

            count++;

            for (int m = 0; m < sums.length; m++) {
                avg[m] = sums[m] / count;
            }

            if (Arrays.equals(coef, coef2)) {
                System.out.println(Arrays.toString(coef));
                return coef;
            }

            if (distance(avg, lastAvg) < 0.001) {
                break;
            }

            System.arraycopy(avg, 0, lastAvg, 0, avg.length);
            System.arraycopy(coef, 0, coef2, 0, coef.length);
        } while (++h <= 30);

        double[] c = new double[nodes.size()];
        Arrays.fill(c, Double.NaN);
        return c;
    }

    // convergent coef or average of coefficients.

    private double[] maximizeNonGaussianity(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                            Node x, List <Node> parents) {
//        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);

//        boolean printSquare = false;
//
//        if (parents.size() == 2 && printSquare) {
//            NumberFormat nf = new DecimalFormat("0.0000");
//            double bound = 1.0;
//
//            double _min = -bound;
//            double _max = bound;
//            int _numIntervals = 20;
//
//            for (int i = 0; i <= _numIntervals; i++) {
//                for (int j = 0; j <= _numIntervals; j++) {
//                    double ai = _min + i * ((_max - _min) / _numIntervals);
//                    double aj = _min + j * ((_max - _min) / _numIntervals);
//
//                    double[] coef = {ai, aj};
//
//                    double ng = ng(index, dataSets, matrices, x, parents, coef);
//
//                    System.out.println(nf.format(ai) + "\t" + nf.format(aj) +
//                            "\t" + nf.format(ng));
//                }
//            }
//        }

        double bound = zeta;

        double min = -bound;
        double max = bound;
        int numIntervals = (int) (max * 10.0);

        if (min >= 0) {
            throw new IllegalStateException("Min must be less than zero.");
        }

        if (max <= 0) {
            throw new IllegalStateException("Max must be greater than zero.");
        }

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];

        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int i = 0; i < nodes.size(); i++) {
//                coef[i] = 0.0;
//                double ngX = ng(index, dataSets, matrices, x, nodes, coef);

                boolean ascendingLeft = false;
                boolean ascendingRight = false;

                double bumpLeft = Double.NEGATIVE_INFINITY;
                double bumpRight = Double.NEGATIVE_INFINITY;

                double lastA = Double.NaN;
                double lastValueLeft = Double.NaN;
                double lastValueRight = Double.NaN;

                boolean foundBumpLeft = false;
                boolean foundBumpRight = false;

                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = -1; s <= numIntervals; s++) {
                    double a = s * (Math.max(-min, max) / numIntervals);

                    if (Double.isNaN(lastA)) {
                        coef[i] = a;
                        double _vRight = ng(index, dataSets, matrices, x, nodes, coef);

                        coef[i] = -a;
                        double _vLeft = ng(index, dataSets, matrices, x, nodes, coef);

                        if (Double.isNaN(_vRight) || Double.isNaN(_vLeft)) {
                            continue;
                        }

                        lastA = a;
                        lastValueLeft = _vLeft;
                        lastValueRight = _vRight;
                        continue;
                    }

                    if (-a >= min) {
                        coef[i] = -a;
                        double vLeft = ng(index, dataSets, matrices, x, nodes, coef);

                        if (!ascendingLeft && vLeft > lastValueLeft) {
                            ascendingLeft = true;
                        } else if (ascendingLeft && vLeft < lastValueLeft) {
                            bumpLeft = -lastA;
                            foundBumpLeft = true;
                        }

                        lastValueLeft = vLeft;
                    }

                    if (a <= max) {
                        coef[i] = a;
                        double vRight = ng(index, dataSets, matrices, x, nodes, coef);

                        if (!ascendingRight && vRight > lastValueRight) {
                            ascendingRight = true;
                        } else if (ascendingRight && vRight < lastValueRight) {
                            bumpRight = lastA;
                            foundBumpRight = true;
                        }

                        lastValueRight = vRight;
                    }

                    lastA = a;

                    if (foundBumpLeft || foundBumpRight) break;
                }

                if (foundBumpRight) {
                    coef[i] = bumpRight;
                } else if (foundBumpLeft) {
                    coef[i] = bumpLeft;
                } else {
                    coef[i] = Double.NaN;
                }

                boolean printValues = false;

                if (printValues) {

                    // Print out values in an interval.
                    if (true) {
                        double[] _coef = Arrays.copyOf(coef, coef.length);
                        double _min = -max;
                        double _max = max;
                        int _numIntervals = 2 * numIntervals;

                        for (int s2 = 0; s2 <= _numIntervals; s2++) {
                            double a = _min + s2 * ((_max - _min) / _numIntervals);
                            _coef[i] = a;
                            double v = ng(index, dataSets, matrices, x, nodes, _coef);

                            System.out.println(a + "\t" + v);
                        }

                        System.out.println("Data for " + nodes.get(i) + " to " + x);
                        System.out.println("Bump = " + coef[i]);
                    }
                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                        continue;
                    } else if (coef[i] == _coef[i]) {
                        continue;
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
//                        System.out.println("h = " + h + " " + Arrays.toString(coef));
                        return coef;
                    } else {
                        break H;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 50);

        double[] c = new double[nodes.size()];
        Arrays.fill(c, Double.NaN);

//        System.out.println("h = " + (h - 1) + " " + Arrays.toString(c));
        return c;
    }

    private double[] maximizeNonGaussianitySmoothed1(int dataIndex, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                                     Node x, List <Node> parents) {
//        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);

//        boolean printSquare = false;
//
//        if (parents.size() == 2 && printSquare) {
//            NumberFormat nf = new DecimalFormat("0.0000");
//            double bound = 1.0;
//
//            double _min = -bound;
//            double _max = bound;
//            int _numIntervals = 20;
//
//            for (int i = 0; i <= _numIntervals; i++) {
//                for (int j = 0; j <= _numIntervals; j++) {
//                    double ai = _min + i * ((_max - _min) / _numIntervals);
//                    double aj = _min + j * ((_max - _min) / _numIntervals);
//
//                    double[] coef = {ai, aj};
//
//                    double ng = ng(index, dataSets, matrices, x, parents, coef);
//
//                    System.out.println(nf.format(ai) + "\t" + nf.format(aj) +
//                            "\t" + nf.format(ng));
//                }
//            }
//        }

        WhichMax whichMax = WhichMax.first;

        double bound = 2;

        double min = -bound;
        double max = bound;
        double density = 10; // per unit

        double _radius = 0.0;
        int radius = (int) (_radius * density);
//        System.out.println("radius = " + radius);

        double range = Math.max(-min, max) + _radius;

        int numIntervals = (int) (range * density);

        if (min >= 0) {
            throw new IllegalStateException("Min must be less than zero.");
        }

        if (max <= 0) {
            throw new IllegalStateException("Max must be greater than zero.");
        }

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];

        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
//                System.out.println("Node index = " + nodeIndex);

                boolean ascendingLeft = false;
                boolean ascendingRight = false;

                double bumpLeft = Double.NEGATIVE_INFINITY;
                double bumpRight = Double.NEGATIVE_INFINITY;

                double lastA = Double.NaN;
                double lastValueLeft = Double.NaN;
                double lastValueRight = Double.NaN;

                boolean foundBumpLeft = false;
                boolean foundBumpRight = false;

                double maxBump = Double.NEGATIVE_INFINITY;

                double[] ng = new double[2 * numIntervals + 1];

                for (int i = 0; i < ng.length; i++) ng[i] = Double.NaN;

//                int numBumps = 0;
//                List<Double> bumps = new ArrayList<Double>();
//                List<Double> bumpValues = new ArrayList<Double>();
//                List<Double> bumpDepths = new ArrayList<Double>();

                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = -2; s <= numIntervals; s++) {
                    double a = s * (range / numIntervals);

                    if (-a >= min) {
                        double vLeft = smooth(matrices, ng, -s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                        if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {
                            double depth = getBumpDepth(-(s - 1), ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
//                            System.out.println("DEPTH " + depth);

//                            if (!bumps.contains(-lastA) && lastA != 0.0) {
//                                numBumps++;
//                                bumps.add(-lastA);
//                                bumpValues.add(lastValueLeft);
//                                bumpDepths.add(depth);
//                            }

                            if (Math.abs(lastA) < alpha) {
                                maxBump = lastValueRight;
                                bumpRight = 0; // lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                                break;
                            }

//                            if (depth > maxBump) {
//                                maxBump = depth;
//                                bumpLeft = -lastA;
//                                foundBumpLeft = true;
//                                foundBumpRight = false;
//                            }

                            if (lastValueLeft > maxBump) {
                                maxBump = lastValueLeft;
                                bumpLeft = -lastA;
                                foundBumpLeft = true;
                                foundBumpRight = false;
                            }
                        }

                        if (vLeft > lastValueLeft) {
                            ascendingLeft = true;
                        }
                        if (vLeft < lastValueLeft) {
                            ascendingLeft = false;
                        }

                        lastValueLeft = vLeft;
                    }

                    if (a <= max) {
                        double vRight = smooth(matrices, ng, s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                        if (ascendingRight && vRight < lastValueRight) {
                            double depth = getBumpDepth(s - 1, ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
//                            System.out.println("DEPTH " + depth);

//                            if (!bumps.contains(lastA)) {
//                                numBumps++;
//                                bumps.add(lastA);
//                                bumpValues.add(lastValueRight);
//                                bumpDepths.add(depth);
//                            }

//                            if (depth > maxBump && lastA >= 0.0) {
//                                maxBump = depth;
//                                bumpRight = lastA;
//                                foundBumpRight = true;
//                                foundBumpLeft = false;
//                            }

                            if (Math.abs(lastA) < alpha) {
                                maxBump = lastValueRight;
                                bumpRight = 0; //lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                                break;
                            }

                            if (lastValueRight > maxBump) {
                                maxBump = lastValueRight;
                                bumpRight = lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                            }
                        }

                        if (vRight > lastValueRight) {
                            ascendingRight = true;
                        }
                        if (vRight < lastValueRight) {
                            ascendingRight = false;
                        }

                        lastValueRight = vRight;
                    }

                    lastA = a;

                    if (whichMax == WhichMax.first) {
                        if (foundBumpLeft || foundBumpRight) break;
                    } else {
                        // skip.
                    }
                }

//                NumberFormat nf = new DecimalFormat("0.0000");

//                if (numBumps > 0) {
//                    System.out.println("NUMBUMPS = " + numBumps);
//                    for (int i = 0; i < bumps.size(); i++) {
//                        System.out.println(nf.format(bumps.get(i))
//                                + "\t" + nf.format(bumpValues.get(i))
//                                + "\t" + nf.format(bumpDepths.get(i)));
//                    }
//
//
//                }

                if (foundBumpRight) {
                    coef[nodeIndex] = bumpRight;
                } else if (foundBumpLeft) {
                    coef[nodeIndex] = bumpLeft;
                } else {
                    coef[nodeIndex] = Double.NaN;
                }

//                if (false) { //Double.isNaN(coef[nodeIndex])) {
//
//                    // Print out values in an interval.
//                    double[] _coef = Arrays.copyOf(coef, coef.length);
//                    double _min = -max;
//                    double _max = max;
//                    int _numIntervals = 2 * numIntervals;
//
//                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
//                        double a = _min + s2 * ((_max - _min) / _numIntervals);
//                        _coef[nodeIndex] = a;
//                        double v = ng(dataIndex, dataSets, matrices, x, nodes, _coef);
//
//                        System.out.println(a + "\t" + v);
//                    }
//
//                    System.out.println("Data for " + nodes.get(nodeIndex) + " to " + x);
//                    System.out.println("Bump = " + coef[nodeIndex]);
//                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                        continue;
                    } else if (coef[i] == _coef[i]) {
                        continue;
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
                        System.out.println("h = " + h + " " + Arrays.toString(coef));
                        return coef;
                    } else {
                        break H;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 10);

        return coef;

//        double[] c = new double[nodes.size()];
//        Arrays.fill(c, Double.NaN);
//
//        System.out.println("h = " + (h - 1) + " " + Arrays.toString(c));
//        return c;
    }

    private double[] maximizeNonGaussianitySmoothed2(int dataIndex, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                                     Node x, List <Node> parents) {
//        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);

//        boolean printSquare = false;
//
//        if (parents.size() == 2 && printSquare) {
//            NumberFormat nf = new DecimalFormat("0.0000");
//            double bound = 1.0;
//
//            double _min = -bound;
//            double _max = bound;
//            int _numIntervals = 20;
//
//            for (int i = 0; i <= _numIntervals; i++) {
//                for (int j = 0; j <= _numIntervals; j++) {
//                    double ai = _min + i * ((_max - _min) / _numIntervals);
//                    double aj = _min + j * ((_max - _min) / _numIntervals);
//
//                    double[] coef = {ai, aj};
//
//                    double ng = ng(index, dataSets, matrices, x, parents, coef);
//
//                    System.out.println(nf.format(ai) + "\t" + nf.format(aj) +
//                            "\t" + nf.format(ng));
//                }
//            }
//        }

        WhichMax whichMax = WhichMax.first;

        double bound = 2;

        double min = -bound;
        double max = bound;
        double density = 10; // per unit

        double _radius = 0.0;
        int radius = (int) (_radius * density);
//        System.out.println("radius = " + radius);

        double range = Math.max(-min, max) + _radius;

        int numIntervals = (int) (range * density);

        if (min >= 0) {
            throw new IllegalStateException("Min must be less than zero.");
        }

        if (max <= 0) {
            throw new IllegalStateException("Max must be greater than zero.");
        }

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];

        for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
//            System.out.println("Node index = " + nodeIndex);

            boolean ascendingLeft = false;
            boolean ascendingRight = false;

            double bumpLeft = Double.NEGATIVE_INFINITY;
            double bumpRight = Double.NEGATIVE_INFINITY;

            double lastA = Double.NaN;
            double lastValueLeft = Double.NaN;
            double lastValueRight = Double.NaN;

            boolean foundBumpLeft = false;
            boolean foundBumpRight = false;

            double maxBump = Double.NEGATIVE_INFINITY;

            double[] ng = new double[2 * numIntervals + 1];

            for (int i = 0; i < ng.length; i++) ng[i] = Double.NaN;

            int numBumps = 0;
            List <Double> bumps = new ArrayList <Double>();
            List <Double> bumpValues = new ArrayList <Double>();
            List <Double> bumpDepths = new ArrayList <Double>();

            // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
            // maximum to the right or left of -1, searching both directions at once.
            for (int s = -2; s <= numIntervals; s++) {
                double a = s * (range / numIntervals);

                if (-a >= min) {
                    double vLeft = smooth(matrices, ng, -s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                    if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {
                        double depth = getBumpDepth(-(s - 1), ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
                        System.out.println("DEPTH " + depth);

                        if (!bumps.contains(-lastA) && lastA != 0.0) {
                            numBumps++;
                            bumps.add(-lastA);
                            bumpValues.add(lastValueLeft);
                            bumpDepths.add(depth);
                        }

                        if (Math.abs(lastA) < alpha) {
                            maxBump = lastValueRight;
                            bumpRight = 0; //lastA;
                            foundBumpRight = true;
                            foundBumpLeft = false;
                            break;
                        }

//                            if (depth > maxBump) {
//                                maxBump = depth;
//                                bumpLeft = -lastA;
//                                foundBumpLeft = true;
//                                foundBumpRight = false;
//                            }

                        if (lastValueLeft > maxBump) {
                            maxBump = lastValueLeft;
                            bumpLeft = -lastA;
                            foundBumpLeft = true;
                            foundBumpRight = false;
                        }
                    }

                    if (vLeft > lastValueLeft) {
                        ascendingLeft = true;
                    }
                    if (vLeft < lastValueLeft) {
                        ascendingLeft = false;
                    }

                    lastValueLeft = vLeft;
                }

                if (a <= max) {
                    double vRight = smooth(matrices, ng, s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                    if (ascendingRight && vRight < lastValueRight) {
                        double depth = getBumpDepth(s - 1, ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
                        System.out.println("DEPTH " + depth);

                        if (!bumps.contains(lastA)) {
                            numBumps++;
                            bumps.add(lastA);
                            bumpValues.add(lastValueRight);
                            bumpDepths.add(depth);
                        }

//                            if (depth > maxBump && lastA >= 0.0) {
//                                maxBump = depth;
//                                bumpRight = lastA;
//                                foundBumpRight = true;
//                                foundBumpLeft = false;
//                            }

                        if (Math.abs(lastA) < alpha) {
                            maxBump = lastValueRight;
                            bumpRight = 0; //lastA;
                            foundBumpRight = true;
                            foundBumpLeft = false;
                            break;
                        }

                        if (lastValueRight > maxBump) {
                            maxBump = lastValueRight;
                            bumpRight = lastA;
                            foundBumpRight = true;
                            foundBumpLeft = false;
                        }
                    }

                    if (vRight > lastValueRight) {
                        ascendingRight = true;
                    }
                    if (vRight < lastValueRight) {
                        ascendingRight = false;
                    }

                    lastValueRight = vRight;
                }

                lastA = a;

                if (whichMax == WhichMax.first) {
//                        if (numBumps == 2) break;
                    if (foundBumpLeft || foundBumpRight) break;
                } else {
                    // skip.
                }
            }

            NumberFormat nf = new DecimalFormat("0.0000");

            if (numBumps > 0) {
                System.out.println("NUMBUMPS = " + numBumps);
                for (int i = 0; i < bumps.size(); i++) {
                    System.out.println(nf.format(bumps.get(i))
                            + "\t" + nf.format(bumpValues.get(i))
                            + "\t" + nf.format(bumpDepths.get(i)));
                }


            }

            if (foundBumpRight) {
                coef[nodeIndex] = bumpRight;
            } else if (foundBumpLeft) {
                coef[nodeIndex] = bumpLeft;
            } else {
                coef[nodeIndex] = Double.NaN;
            }

//                double[] coef2 = Arrays.copyOf(coef, coef.length);
//                coef2[nodeIndex] = 100000;
//                double v2 = ng(dataIndex, dataSets, matrices, x, nodes, coef2);
//                System.out.println("ng " + nodeIndex + " " + v2);

            if (false) { //Double.isNaN(coef[nodeIndex])) {

                // Print out values in an interval.
                if (true) {
                    double[] _coef = Arrays.copyOf(coef, coef.length);
                    double _min = -max;
                    double _max = max;
                    int _numIntervals = 2 * numIntervals;

                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
                        double a = _min + s2 * ((_max - _min) / _numIntervals);
                        _coef[nodeIndex] = a;
                        double v = ng(dataIndex, dataSets, matrices, x, nodes, _coef);

                        System.out.println(a + "\t" + v);
                    }

                    System.out.println("Data for " + nodes.get(nodeIndex) + " to " + x);
                    System.out.println("Bump = " + coef[nodeIndex]);
                }
            }
        }

        return coef;
    }

    private double[] maximizeNonGaussianityGoodStartingPoint1(int dataIndex, Node x, List <Node> parents) {

        Collections.sort(parents);

        WhichMax whichMax = WhichMax.max;

        double min = -1.5;
        double max = 1.5;
        double density = 10; // per unit

        double _radius = 0.0;
        int radius = (int) (_radius * density);

        double range = Math.max(-min, max) + _radius;

        int numIntervals = (int) (range * density);

        if (min >= max) {
            throw new IllegalStateException("Min must be < max.");
        }

        double[] coef = new double[parents.size()];
        double[] maxCoef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);
        maxCoef = optimizeCoef3(maxCoef, dataIndex, x, whichMax, min, max, radius, range, numIntervals, parents);
        return maxCoef;
    }

    private double[] maximizeNonGaussianityGoodStartingPoint1b(int dataIndex, Node x, List <Node> parents) {

        Collections.sort(parents);

//        WhichMax whichMax = WhichMax.max;

        double min = -1.0;
        double max = 1.0;
        double density = 20; // per unit

        double _radius = 0.0;
//        int radius = (int) (_radius * density);

        double range = Math.max(-min, max) + _radius;

        int numIntervals = (int) (range * density);

        if (min >= max) {
            throw new IllegalStateException("Min must be < max.");
        }

        double[] coef = new double[parents.size()];

        int h = 0;
        double[] prev = Arrays.copyOf(coef, coef.length);

        H:
        do {
            coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);

            if (Arrays.equals(coef, prev)) {
                break;
            }

            prev = Arrays.copyOf(coef, coef.length);
        } while (++h < 20);

//        for (int i = 0; i < 10; i++) {
//            coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);
//        }
//        maxCoef = optimizeCoef4(maxCoef, dataIndex, x, whichMax, min, max, radius, range, numIntervals, parents);
//        System.out.println("coef = " + Arrays.toString(coef));
        return coef;
    }

    private double[] maximizeNonGaussianityGoodStartingPointb(int dataIndex, Node x, List <Node> parents) {

        Collections.sort(parents);

//        WhichMax whichMax = WhichMax.max;

        double min = -2.0;
        double max = 2.0;
        double density = 20; // per unit

        double _radius = 0.0;
//        int radius = (int) (_radius * density);

        double range = Math.max(-min, max) + _radius;

        int numIntervals = (int) (range * density);

        if (min >= max) {
            throw new IllegalStateException("Min must be < max.");
        }

        double[] coef = new double[parents.size()];

        int h = 0;
        double[] prev = Arrays.copyOf(coef, coef.length);
        coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);

        for (int i = 0; i < coef.length; i++) {
            if (coef[i] == 1.0) coef[i] = 0;
        }


//        H:
//        do {
//            coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);
//
//            if (Arrays.equals(coef, prev)) {
//                break;
//            }
//
//            prev = Arrays.copyOf(coef, coef.length);
//        } while (++h < 20);

//        for (int i = 0; i < 10; i++) {
//            coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);
//        }
//        maxCoef = optimizeCoef4(maxCoef, dataIndex, x, whichMax, min, max, radius, range, numIntervals, parents);
//        System.out.println("coef = " + Arrays.toString(coef));
        return coef;
    }

    private double[] maximizeNonGaussianityGoodStartingPoint(final int dataIndex, final Node x, final List <Node> parents) {
        System.out.println("\nstart");

//        Collections.sort(parents);

//        WhichMax whichMax = WhichMax.max;

        final double min = -2;
        final double max = 2;
        final double density = 10; // per unit
        double range = Math.max(-min, max);

        int numIntervals = (int) (range * density);

        double[] coef = new double[parents.size()];
//        coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);
        int h = 0;
        double[] prev = Arrays.copyOf(coef, coef.length);

        double maxValue = Double.NEGATIVE_INFINITY;

        H:
        do {
            double[] _coef = findGoodIntersection(coef, dataIndex, x, parents, min, max, range, numIntervals);

            double value = ng(dataIndex, dataSets, matrices, x, parents, _coef);

            if (value > maxValue) {
                coef = _coef;
                maxValue = value;
            }

            if (Arrays.equals(coef, prev)) {
                break;
            }

            prev = Arrays.copyOf(coef, coef.length);
        } while (++h < 5);

        double[] _coef = findMaxPoint(dataIndex, x, parents, min, max, coef);

        double value = ng(dataIndex, dataSets, matrices, x, parents, _coef);

        if (value > maxValue) {
            coef = _coef;
            maxValue = value;
        }

//        for (int i = 0; i < coef.length; i++) {
//            if (coef[i] >= 1.0) coef[i] = 0;
//        }

        return coef;
    }

    private double[] findMaxPoint(final int dataIndex, final Node x, final List <Node> parents, final double min, final double max, double[] coef) {
        coef = Arrays.copyOf(coef, coef.length);

        MultivariateFunction function = new MultivariateFunction() {
            double metric;

            @Override
            public double evaluate(double[] _coef) {
                double v = ng(dataIndex, dataSets, matrices, x, parents, _coef);

//                    System.out.println(Arrays.toString(_coef) + " ---> " + v);

                return -v;
            }

            @Override
            public int getNumArguments() {
                return parents.size();
            }

            @Override
            public double getLowerBound(int i) {
                return min;
            }

            @Override
            public double getUpperBound(int i) {
                return max;
            }

            public OrthogonalHints getOrthogonalHints() {
                return null;
            }
        };

        final double func_tolerance = 0.001;
        final double param_tolerance = 0.001;

//        MultivariateMinimum search = new OrthogonalSearch();
        MultivariateMinimum search = new ConjugateGradientSearch();

        search.optimize(function, coef, func_tolerance, param_tolerance);

        return coef;
    }

    private double[] findGoodIntersection(double[] coef, int dataIndex, Node x, List <Node> parents, double min, double max, double range, int numIntervals) {
        List <List <Double>> bumpList = new ArrayList <List <Double>>();

        for (int i = 0; i < parents.size(); i++) {
            double[] coef2 = Arrays.copyOf(coef, coef.length);
            List <Double> bumps = findBumpsForIndex(coef2, i, dataIndex, x, min, max,
                    range, numIntervals, parents);

//            List<Double> bumps = new ArrayList<Double>();
//
//            for (int k = -5; k <= 5; k+=1) {
//                bumps.add(k / 5.0);
//            }

//            List<Double> _bumps = new ArrayList<Double>(bumps);
//
//            for (int j = 0; j < bumps.size(); j++) {
//                if (bumps.get(j) > 1.0 || bumps.get(j) < -1.0) {
//                    _bumps.remove(bumps.get(j));
////                    System.out.println("Removing " + bumps.get(j));
//                }
//            }


//            List<Double> _bumps = new ArrayList<Double>();
//            double __min = Double.POSITIVE_INFINITY;
//
//            for (int k = 0; k < bumps.size(); k++) {
//                if (Math.abs(bumps.get(k)) < Math.abs(__min)) {
//                    __min = bumps.get(k);
//                }
//            }
//
//            if (!Double.isInfinite(__min)) {
//                _bumps.add(__min);
//            }
//
//            System.out.println("bumps = " + bumps + " " + " min = " + _bumps);
//            bumpList.add(_bumps);

            bumpList.add(bumps);
        }

        double[] maxCoef = null; //Arrays.copyOf(coef, coef.length);
        double _max = Double.NEGATIVE_INFINITY; // ng(dataIndex, dataSets, matrices, x, parents, maxCoef);

        int[] sizes = new int[bumpList.size()];

        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = bumpList.get(i).size();
        }

        CombinationGenerator gen = new CombinationGenerator(sizes);
        int[] comb;

        LOOP:
        while ((comb = gen.next()) != null) {
            double[] _coef = new double[comb.length];

            for (int i = 0; i < comb.length; i++) {
                if (bumpList.get(i).isEmpty()) {
                    _coef[i] = 0.0;
                } else {
                    _coef[i] = bumpList.get(i).get(comb[i]);
                }
            }

//            _coef = findMaxPoint(dataIndex, x, parents, min, max, _coef);

            double value = ng(dataIndex, dataSets, matrices, x, parents, _coef);

//            System.out.println("_coef = " + Arrays.toString(_coef) + " value = " + value);

            if (value > _max) {
                _max = value;
                maxCoef = _coef;
            }
        }

        if (maxCoef == null) maxCoef = new double[parents.size()]; // zeroes

        return maxCoef;
    }

    private double[] optimizeCoef(double[] coef, int dataIndex, Node x, WhichMax whichMax, double min, double max,
                                  int radius, double range, int numIntervals, List <Node> nodes) {
//        double[] coef = new double[nodes.size()];

        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
                boolean ascendingLeft = false;
                boolean ascendingRight = false;

                double bumpLeft = Double.NEGATIVE_INFINITY;
                double bumpRight = Double.NEGATIVE_INFINITY;

                double lastA = Double.NaN;
                double lastValueLeft = Double.NaN;
                double lastValueRight = Double.NaN;

                boolean foundBumpLeft = false;
                boolean foundBumpRight = false;

                double maxBump = Double.NEGATIVE_INFINITY;

                double[] ng = new double[2 * numIntervals + 1];

                for (int i = 0; i < ng.length; i++) ng[i] = Double.NaN;

                double center = coef[nodeIndex];

//                int numBumps = 0;
//                List<Double> bumps = new ArrayList<Double>();
//                List<Double> bumpValues = new ArrayList<Double>();
//                List<Double> bumpDepths = new ArrayList<Double>();

                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = -2; s <= numIntervals; s++) {
                    double a = s * (range / numIntervals);

                    if (center - a >= min) {
//                        double vLeft = smooth(ng, -s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
                        coef[nodeIndex] = center - a;
                        double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef);

                        if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {

//                            if (!bumps.contains(-lastA) && lastA != 0.0) {
//                                numBumps++;
//                                bumps.add(-lastA);
//                                bumpValues.add(lastValueLeft);
//                                bumpDepths.add(depth);
//                            }

                            if (Math.abs(center - lastA) < alpha) {
//                                maxBump = lastValueLeft;
                                bumpLeft = 0; // lastA;
                                foundBumpRight = false;
                                foundBumpLeft = true;
                                break;
                            }

                            if (lastValueLeft > maxBump) {
                                maxBump = lastValueLeft;
                                bumpLeft = center - lastA;
                                foundBumpLeft = true;
                                foundBumpRight = false;
                            }
                        }

                        if (vLeft > lastValueLeft) {
                            ascendingLeft = true;
                        }
                        if (vLeft < lastValueLeft) {
                            ascendingLeft = false;
                        }

                        lastValueLeft = vLeft;
                    }

                    if (center + a <= max) {
//                        double vRight = smooth(ng, s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
                        coef[nodeIndex] = center + a;
                        double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef);

                        if (ascendingRight && vRight < lastValueRight) {
//                            double depth = getBumpDepth(s - 1, ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
//                            System.out.println("DEPTH " + depth);

//                            if (!bumps.contains(lastA)) {
//                                numBumps++;
//                                bumps.add(lastA);
//                                bumpValues.add(lastValueRight);
//                                bumpDepths.add(depth);
//                            }

                            if (Math.abs(center + lastA) < alpha) {
//                                maxBump = lastValueRight;
                                bumpRight = 0; //lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                                break;
                            }

                            if (lastValueRight > maxBump) {
                                maxBump = lastValueRight;
                                bumpRight = center + lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                            }
                        }

                        if (vRight > lastValueRight) {
                            ascendingRight = true;
                        }
                        if (vRight < lastValueRight) {
                            ascendingRight = false;
                        }

                        lastValueRight = vRight;
                    }

                    lastA = a;

                    if (whichMax == WhichMax.first) {
                        if (foundBumpLeft || foundBumpRight) break;
                    } else {
                        // skip.
                    }
                }

                if (foundBumpRight) {
                    coef[nodeIndex] = bumpRight;
                } else if (foundBumpLeft) {
                    coef[nodeIndex] = bumpLeft;
                } else {
                    coef[nodeIndex] = Double.NaN;
                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                        continue;
                    } else if (coef[i] == _coef[i]) {
                        continue;
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
//                        System.out.println("h = " + h + " " + Arrays.toString(coef));
                        return coef;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 10);

        return coef;
    }

    private double[] optimizeCoef2(double[] coef, int dataIndex, Node x, WhichMax whichMax, double min, double max,
                                   int radius, double range, int numIntervals, List <Node> nodes) {
        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
                boolean ascendingLeft = false;
                boolean ascendingRight = false;

                double bumpLeft = Double.NEGATIVE_INFINITY;
                double bumpRight = Double.NEGATIVE_INFINITY;

                double lastA = Double.NaN;
                double lastValueLeft = Double.NaN;
                double lastValueRight = Double.NaN;

                boolean foundBumpLeft = false;
                boolean foundBumpRight = false;

                double maxBump = Double.NEGATIVE_INFINITY;

                double[] ng = new double[2 * numIntervals + 1];

                for (int i = 0; i < ng.length; i++) ng[i] = Double.NaN;

//                int numBumps = 0;
//                List<Double> bumps = new ArrayList<Double>();
//                List<Double> bumpValues = new ArrayList<Double>();
//                List<Double> bumpDepths = new ArrayList<Double>();

                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = -2; s <= numIntervals; s++) {
                    double a = s * (range / numIntervals);

                    if (-a >= min) {
                        coef[nodeIndex] = -a;
                        double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef);
//                        double vLeft = smooth(ng, -s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                        if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {

//                            if (!bumps.contains(-lastA) && lastA != 0.0) {
//                                numBumps++;
//                                bumps.add(-lastA);
//                                bumpValues.add(lastValueLeft);
//                                bumpDepths.add(depth);
//                            }
//
//                            if (Math.abs(lastA) < alpha) {
//                                maxBump = lastValueLeft;
//                                bumpLeft = 0; // lastA;
//                                foundBumpRight = false;
//                                foundBumpLeft = true;
//                                break;
//                            }

                            if (lastValueLeft > maxBump) {
                                maxBump = lastValueLeft;
                                bumpLeft = -lastA;
                                foundBumpLeft = true;
                                foundBumpRight = false;
                            }
                        }

                        if (vLeft > lastValueLeft) {
                            ascendingLeft = true;
                        }
                        if (vLeft < lastValueLeft) {
                            ascendingLeft = false;
                        }

                        lastValueLeft = vLeft;
                    }

                    if (a <= max) {
                        coef[nodeIndex] = a;
                        double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef);
//                        double vRight = smooth(ng, s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

                        if (ascendingRight && vRight < lastValueRight) {
                            double depth = getBumpDepth(s - 1, ng, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
//                            System.out.println("DEPTH " + depth);

//                            if (!bumps.contains(lastA)) {
//                                numBumps++;
//                                bumps.add(lastA);
//                                bumpValues.add(lastValueRight);
//                                bumpDepths.add(depth);
//                            }

//                            if (Math.abs(lastA) < alpha) {
//                                maxBump = lastValueRight;
//                                bumpRight = 0; //lastA;
//                                foundBumpRight = true;
//                                foundBumpLeft = false;
//                                break;
//                            }

                            if (lastValueRight > maxBump) {
                                maxBump = lastValueRight;
                                bumpRight = lastA;
                                foundBumpRight = true;
                                foundBumpLeft = false;
                            }
                        }

                        if (vRight > lastValueRight) {
                            ascendingRight = true;
                        }
                        if (vRight < lastValueRight) {
                            ascendingRight = false;
                        }

                        lastValueRight = vRight;
                    }

                    lastA = a;

                    if (whichMax == WhichMax.first) {
                        if (foundBumpLeft || foundBumpRight) break;
                    } else {
                        // skip.
                    }
                }


                if (Math.abs(Math.max(bumpLeft, bumpRight)) < alpha) {
                    bumpLeft = 0;
                    bumpRight = 0;
                }

                if (foundBumpRight) {
                    coef[nodeIndex] = bumpRight;
                } else if (foundBumpLeft) {
                    coef[nodeIndex] = bumpLeft;
                } else {
                    coef[nodeIndex] = 0;
                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                        continue;
                    } else if (coef[i] == _coef[i]) {
                        continue;
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
//                        System.out.println("h = " + h + " " + Arrays.toString(coef));
                        return coef;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 10);

        return coef;
    }

    private double[] optimizeCoef3(double[] coef, int dataIndex, Node x, WhichMax whichMax, double min, double max,
                                   int radius, double range, int numIntervals, List <Node> nodes) {
        List <double[]> coefHistory = new ArrayList <double[]>();

//        int h = 0;
//
//        H:
//        do {
        double[] coef2 = coef; // Arrays.copyOf(coef, coef.length);
        double[] sums = new double[coef.length];
        int[] counts = new int[coef.length];

        for (int i = 0; i < nodes.size() * 10; i++) {
            int nodeIndex = i % nodes.size(); //RandomUtil.getInstance().nextInt(nodes.size());

            double[] coef3 = coef; //Arrays.copyOf(coef, coef.length);

            boolean ascendingLeft = false;
            boolean ascendingRight = false;

            double bumpLeft = Double.NEGATIVE_INFINITY;
            double bumpRight = Double.NEGATIVE_INFINITY;

            double lastA = Double.NaN;
            double lastValueLeft = Double.NaN;
            double lastValueRight = Double.NaN;

            boolean foundBumpLeft = false;
            boolean foundBumpRight = false;

            double maxBump = Double.NEGATIVE_INFINITY;

            // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
            // maximum to the right or left of -1, searching both directions at once.
            for (int s = -2; s <= numIntervals; s++) {
                double a = s * (range / numIntervals);

                if (-a >= min) {
                    coef3[nodeIndex] = -a;
                    double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef3);

                    if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {
                        if (lastValueLeft > maxBump) {
                            maxBump = lastValueLeft;
                            bumpLeft = -lastA;
                            foundBumpLeft = true;
                            foundBumpRight = false;
                        }
                    }

                    if (vLeft > lastValueLeft) {
                        ascendingLeft = true;
                    }
                    if (vLeft < lastValueLeft) {
                        ascendingLeft = false;
                    }

                    lastValueLeft = vLeft;
                }

                if (a <= max) {
                    coef3[nodeIndex] = a;
                    double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef3);

                    if (ascendingRight && vRight < lastValueRight && lastA >= 0.0) {
                        if (lastValueRight > maxBump) {
                            maxBump = lastValueRight;
                            bumpRight = lastA;
                            foundBumpRight = true;
                            foundBumpLeft = false;
                        }
                    }

                    if (vRight > lastValueRight) {
                        ascendingRight = true;
                    }
                    if (vRight < lastValueRight) {
                        ascendingRight = false;
                    }

                    lastValueRight = vRight;
                }

                lastA = a;

                if (whichMax == WhichMax.first) {
                    if (foundBumpLeft || foundBumpRight) break;
                } else {
                    // skip.
                }
            }

            double _maxBump = 0.0;

            if (foundBumpRight) {
                _maxBump = bumpRight;
            } else if (foundBumpLeft) {
                _maxBump = bumpLeft;
            }

            if (Math.abs(_maxBump) < alpha) {
                coef2[nodeIndex] = 0.0;
            } else {
                coef2[nodeIndex] = _maxBump;
            }

            sums[nodeIndex] += coef2[nodeIndex];
            counts[nodeIndex] += 1;

//            if (Math.abs(_maxBump) < alpha) {
//                coef2[nodeIndex] = 0;
//            } else if (foundBumpRight) {
//                coef2[nodeIndex] = bumpRight;
//            } else if (foundBumpLeft) {
//                coef2[nodeIndex] = bumpLeft;
//            } else {
////                coef2[nodeIndex] = 0;
//            }
        }

        double max2 = Double.NEGATIVE_INFINITY;
        int index2 = -1;

//            for (int i = 0; i < nodes.size(); i++) {
//
//                if (coef2[i] > max2 && coef[i] != coef2[i]) {
//                    max2 = coef2[i];
//                    index2 = i;
//                }
//
////                double diff = Math.abs(coef2[i] - coef[i]);
////
////                if (diff > max2 && diff != 0) {
////                    max = diff;
////                    index2 = i;
////                }
//            }

        coef = Arrays.copyOf(coef2, coef2.length);

//        if (index2 > -1) {
//            coef[index2] = coef2[index2];
//        }
//
//        for (int j = coefHistory.size() - 1; j >= 0; j--) {
//            double[] _coef = coefHistory.get(j);
//            boolean equals = true;
//
//            for (int i = 0; i < coef.length; i++) {
//                if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
//                    continue;
//                } else if (coef[i] == _coef[i]) {
//                    continue;
//                }
//
//                equals = false;
//                break;
//            }
//
//            if (equals) {
//                if (j == coefHistory.size() - 1) {
//                    return coef;
//                }
//            }
//        }

        coefHistory.add(Arrays.copyOf(coef, coef.length));
//        } while (++h <= 100);

//        return coef;

        for (int i = 0; i < coef.length; i++) {
            coef[i] = sums[i] / counts[i];

            if (counts[i] < 2) throw new IllegalArgumentException();
        }

        return coef;
    }

    private double[] optimizeCoef4(List <DoubleMatrix2D> matrices, double[] coef, int dataIndex, Node x, WhichMax whichMax, double min, double max,
                                   int radius, double range, int numIntervals, List <Node> nodes) {
        List <double[]> coefHistory = new ArrayList <double[]>();

//        int h = 0;
//
//        H:
//        do {
        double[] coef2 = coef; // Arrays.copyOf(coef, coef.length);
        double[] sums = new double[coef.length];
        int[] counts = new int[coef.length];

        for (int i = 0; i < nodes.size() * 10; i++) {
            int nodeIndex = i % nodes.size(); //RandomUtil.getInstance().nextInt(nodes.size());

            double[] coef3 = coef; //Arrays.copyOf(coef, coef.length);

            boolean ascendingLeft = false;
            boolean ascendingRight = false;

            double bumpLeft = Double.NEGATIVE_INFINITY;
            double bumpRight = Double.NEGATIVE_INFINITY;

            double lastA = Double.NaN;
            double lastValueLeft = Double.NaN;
            double lastValueRight = Double.NaN;

            boolean foundBumpLeft = false;
            boolean foundBumpRight = false;

            double maxBump = Double.NEGATIVE_INFINITY;

            // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
            // maximum to the right or left of -1, searching both directions at once.
            for (int s = -2; s <= numIntervals; s++) {
                double a = s * (range / numIntervals);

                if (-a >= min) {
                    coef3[nodeIndex] = -a;
                    double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef3);

                    if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {
                        if (lastValueLeft > maxBump) {
                            maxBump = lastValueLeft;
                            bumpLeft = -lastA;
                            foundBumpLeft = true;
                            foundBumpRight = false;
                        }
                    }

                    if (vLeft > lastValueLeft) {
                        ascendingLeft = true;
                    }
                    if (vLeft < lastValueLeft) {
                        ascendingLeft = false;
                    }

                    lastValueLeft = vLeft;
                }

                if (a <= max) {
                    coef3[nodeIndex] = a;
                    double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef3);

                    if (ascendingRight && vRight < lastValueRight && lastA >= 0.0) {
                        if (lastValueRight > maxBump) {
                            maxBump = lastValueRight;
                            bumpRight = lastA;
                            foundBumpRight = true;
                            foundBumpLeft = false;
                        }
                    }

                    if (vRight > lastValueRight) {
                        ascendingRight = true;
                    }
                    if (vRight < lastValueRight) {
                        ascendingRight = false;
                    }

                    lastValueRight = vRight;
                }

                lastA = a;

                if (whichMax == WhichMax.first) {
                    if (foundBumpLeft || foundBumpRight) break;
                } else {
                    // skip.
                }
            }

            double _maxBump = 0.0;

            if (foundBumpRight) {
                _maxBump = bumpRight;
            } else if (foundBumpLeft) {
                _maxBump = bumpLeft;
            }

            if (Math.abs(_maxBump) < alpha) {
                coef2[nodeIndex] = 0.0;
            } else {
                coef2[nodeIndex] = _maxBump;
            }

            sums[nodeIndex] += coef2[nodeIndex];
            counts[nodeIndex] += 1;

//            if (Math.abs(_maxBump) < alpha) {
//                coef2[nodeIndex] = 0;
//            } else if (foundBumpRight) {
//                coef2[nodeIndex] = bumpRight;
//            } else if (foundBumpLeft) {
//                coef2[nodeIndex] = bumpLeft;
//            } else {
////                coef2[nodeIndex] = 0;
//            }
        }

        double max2 = Double.NEGATIVE_INFINITY;
        int index2 = -1;

//            for (int i = 0; i < nodes.size(); i++) {
//
//                if (coef2[i] > max2 && coef[i] != coef2[i]) {
//                    max2 = coef2[i];
//                    index2 = i;
//                }
//
////                double diff = Math.abs(coef2[i] - coef[i]);
////
////                if (diff > max2 && diff != 0) {
////                    max = diff;
////                    index2 = i;
////                }
//            }

        coef = Arrays.copyOf(coef2, coef2.length);

//        if (index2 > -1) {
//            coef[index2] = coef2[index2];
//        }
//
//        for (int j = coefHistory.size() - 1; j >= 0; j--) {
//            double[] _coef = coefHistory.get(j);
//            boolean equals = true;
//
//            for (int i = 0; i < coef.length; i++) {
//                if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
//                    continue;
//                } else if (coef[i] == _coef[i]) {
//                    continue;
//                }
//
//                equals = false;
//                break;
//            }
//
//            if (equals) {
//                if (j == coefHistory.size() - 1) {
//                    return coef;
//                }
//            }
//        }

        coefHistory.add(Arrays.copyOf(coef, coef.length));
//        } while (++h <= 100);

//        return coef;

        for (int i = 0; i < coef.length; i++) {
            coef[i] = sums[i] / counts[i];

            if (counts[i] < 2) throw new IllegalArgumentException();
        }

        return coef;
    }

    private List <Double> findBumpsForIndex(double[] coef, int nodeIndex, int dataIndex, Node x, double min, double max,
                                            double range, int numIntervals, List <Node> nodes) {
//        double[] coef = new double[nodes.size()];

        boolean ascendingLeft = false;
        boolean ascendingRight = false;

        double lastA = Double.NaN;
        double lastValueLeft = Double.NaN;
        double lastValueRight = Double.NaN;

        double[] ng = new double[2 * numIntervals + 1];

        for (int i = 0; i < ng.length; i++) ng[i] = Double.NaN;

        int numBumps = 0;
        List <Double> bumps = new ArrayList <Double>();
        List <Double> bumpValues = new ArrayList <Double>();

        // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
        // maximum to the right or left of -1, searching both directions at once.
        for (int s = -numIntervals; s <= numIntervals; s++) {
            double a = s * (range / numIntervals);

            coef[nodeIndex] = a;


//            if (-a >= min) {
//                double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef);
//
//                if (ascendingLeft && vLeft < lastValueLeft && lastA >= 0.0) {
//                    if (!bumps.contains(-lastA) && lastA != 0.0) {
//                        numBumps++;
//                        bumps.add(-lastA);
//                        bumpValues.add(lastValueLeft);
//                    }
//                }
//
//                if (vLeft > lastValueLeft) {
//                    ascendingLeft = true;
//                }
//                if (vLeft < lastValueLeft) {
//                    ascendingLeft = false;
//                }
//
//                lastValueLeft = vLeft;
//            }

            if (a >= min && a <= max) {
                double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef);

                if (ascendingRight && vRight < lastValueRight) {
                    if (!bumps.contains(lastA)) {
                        numBumps++;
                        bumps.add(lastA);
                        bumpValues.add(lastValueRight);
                    }
                }

                if (vRight > lastValueRight) {
                    ascendingRight = true;
                }
                if (vRight < lastValueRight) {
                    ascendingRight = false;
                }

                lastValueRight = vRight;
            }

            lastA = a;
        }

//        NumberFormat nf = new DecimalFormat("0.0000");
//
//        System.out.println("NUMBUMPS = " + numBumps);
//        for (int i = 0; i < bumps.size(); i++) {
//            System.out.println(nf.format(bumps.get(i))
//                    + "\t" + nf.format(bumpValues.get(i)));
//        }

        return bumps;
    }

    private double getBumpDepth1(List <DoubleMatrix2D> matrices, double d, int numIntervals, double range, double[] coef,
                                 int dataIndex, int nodeIndex, Node x, List <Node> nodes) {
        coef = Arrays.copyOf(coef, coef.length);

        double ng_s = ng(dataIndex, dataSets, matrices, x, nodes, coef);

        double lastValueLeft = ng_s;
        List <Double> leftValues = new ArrayList <Double>();

        for (int t = 0; t >= -100; t--) {
            double a = d + t * (range / numIntervals);
            coef[nodeIndex] = a;
            double vLeft = ng(dataIndex, dataSets, matrices, x, nodes, coef);
            leftValues.add(vLeft);

            if (vLeft > lastValueLeft) {
                break;
            }

            if (!Double.isNaN(vLeft)) {
                lastValueLeft = vLeft;
            }
        }

        double lastValueRight = ng_s;
        List <Double> rightValues = new ArrayList <Double>();

        for (int t = 0; t + numIntervals <= 1000; t++) {
            double a = d + t * (range / numIntervals);
            coef[nodeIndex] = a;
            double vRight = ng(dataIndex, dataSets, matrices, x, nodes, coef);
            rightValues.add(vRight);

            if (vRight > lastValueRight) {
                break;
            }

            if (!Double.isNaN(vRight)) {
                lastValueRight = vRight;
            }

        }

        double max = Math.max(lastValueLeft, lastValueRight);
        return ng_s - max;
    }

    private double getBumpDepth(int s, double[] ng, int numIntervals, int radius, double range, double[] coef, int dataIndex, int nodeIndex, Node x, List <Node> nodes) {
        double ng_s = smooth(matrices, ng, s, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);

        double lastValueLeft = ng_s;
        List <Double> leftValues = new ArrayList <Double>();

        for (int t = s; t + numIntervals >= 0; t--) {
            double vLeft = smooth(matrices, ng, t, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
            leftValues.add(vLeft);

            if (vLeft > lastValueLeft) {
                continue;
            }

            if (!Double.isNaN(vLeft)) {
                lastValueLeft = vLeft;
            }
        }

        double lastValueRight = ng_s;
        List <Double> rightValues = new ArrayList <Double>();

        for (int t = s; t + numIntervals <= ng.length; t++) {
            double vRight = smooth(matrices, ng, t, numIntervals, radius, range, coef, dataIndex, nodeIndex, x, nodes);
            rightValues.add(vRight);

            if (vRight > lastValueRight) {
                break;
            }

            if (!Double.isNaN(vRight)) {
                lastValueRight = vRight;
            }

        }

//        double avg = (lastValueLeft + lastValueRight) / 2.0;
//        return ng_s - avg;

        double max = Math.max(lastValueLeft, lastValueRight);
        return ng_s - max;
    }

    private double smooth(List <DoubleMatrix2D> matrices, double[] ng, int s, int numIntervals, int radius, double range,
                          double[] coef, int dataIndex, int nodeIndex, Node x, List <Node> parents) {
        double sum = 0.0;
        int count = 0;

        for (int i = numIntervals + s - radius; i <= numIntervals + s + radius; i++) {
            if (i < 0) continue;
            if (i >= ng.length) continue;

            if (Double.isNaN(ng[i])) {
                double a = s * range / numIntervals;
                coef[nodeIndex] = a;
                ng[i] = ng(dataIndex, dataSets, matrices, x, parents, coef);
            }

            sum += ng[i];
            count++;
        }

        return sum / count;
    }

    private double[] maximizeNonGaussianityb(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
//        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);

//        boolean printSquare = false;
//
//        if (parents.size() == 2 && printSquare) {
//            NumberFormat nf = new DecimalFormat("0.0000");
//            double bound = 1.0;
//
//            double _min = -bound;
//            double _max = bound;
//            int _numIntervals = 20;
//
//            for (int i = 0; i <= _numIntervals; i++) {
//                for (int j = 0; j <= _numIntervals; j++) {
//                    double ai = _min + i * ((_max - _min) / _numIntervals);
//                    double aj = _min + j * ((_max - _min) / _numIntervals);
//
//                    double[] coef = {ai, aj};
//
//                    double ng = ng(index, dataSets, matrices, x, parents, coef);
//
//                    System.out.println(nf.format(ai) + "\t" + nf.format(aj) +
//                            "\t" + nf.format(ng));
//                }
//            }
//        }

        double bound = 2.0;

        double min = -bound;
        double max = bound;
        int numIntervals = (int) (max * 6.0);

        if (min >= 0) {
            throw new IllegalStateException("Min must be less than zero.");
        }

        if (max <= 0) {
            throw new IllegalStateException("Max must be greater than zero.");
        }

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];

        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int i = 0; i < nodes.size(); i++) {
//                coef[i] = 0.0;
//                double ngX = ng(index, dataSets, matrices, x, nodes, coef);

                boolean ascendingLeft = false;
                boolean ascendingRight = false;

                double bumpLeft = Double.NEGATIVE_INFINITY;
                double bumpRight = Double.NEGATIVE_INFINITY;

                double lastA = Double.NaN;
                double lastValueLeft = Double.NaN;
                double lastValueRight = Double.NaN;

                boolean foundBumpLeft = false;
                boolean foundBumpRight = false;

                double maxBumpLeft = Double.NEGATIVE_INFINITY;
                double maxBumpRight = Double.NEGATIVE_INFINITY;

                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = -1; s <= numIntervals; s++) {
                    double a = s * (Math.max(-min, max) / numIntervals);

                    if (Double.isNaN(lastA)) {
                        coef[i] = a;
                        double _vRight = ng(index, dataSets, matrices, x, nodes, coef);

                        coef[i] = -a;
                        double _vLeft = ng(index, dataSets, matrices, x, nodes, coef);

                        if (Double.isNaN(_vRight) || Double.isNaN(_vLeft)) {
                            continue;
                        }

                        lastA = a;
                        lastValueLeft = _vLeft;
                        lastValueRight = _vRight;
                        continue;
                    }

                    if (-a >= min) {
                        coef[i] = -a;
                        double vLeft = ng(index, dataSets, matrices, x, nodes, coef);

                        if (!ascendingLeft && vLeft > lastValueLeft) {
                            ascendingLeft = true;
                        } else if (ascendingLeft && vLeft < lastValueLeft) {
                            if (lastValueLeft > maxBumpLeft) {
                                bumpLeft = -lastA;
                                maxBumpLeft = lastValueLeft;
                            }
                        }

                        lastValueLeft = vLeft;
                    }

                    if (a <= max) {
                        coef[i] = a;
                        double vRight = ng(index, dataSets, matrices, x, nodes, coef);

                        if (!ascendingRight && vRight > lastValueRight) {
                            ascendingRight = true;
                        } else if (ascendingRight && vRight < lastValueRight) {
                            if (lastValueRight > maxBumpRight) {
                                bumpRight = lastA;
                                maxBumpRight = lastValueRight;
                            }
                        }

                        lastValueRight = vRight;
                    }

                    lastA = a;

//                    if (foundBumpLeft || foundBumpRight) break;
                }

                if (maxBumpRight > maxBumpLeft && maxBumpRight > Double.NEGATIVE_INFINITY) {
                    coef[i] = bumpRight;
                } else if (maxBumpLeft > maxBumpRight && maxBumpLeft > Double.NEGATIVE_INFINITY) {
                    coef[i] = bumpLeft;
                } else {
                    coef[i] = Double.NaN;
                }

//                if (foundBumpRight) {
//                    coef[i] = bumpRight;
//                } else if (foundBumpLeft) {
//                    coef[i] = bumpLeft;
//                } else {
//                    coef[i] = Double.NaN;
//                }

                boolean printValues = true;

                if (printValues) {

                    // Print out values in an interval.
                    double[] _coef = Arrays.copyOf(coef, coef.length);
                    double _min = -max;
                    double _max = max;
                    int _numIntervals = 2 * numIntervals;

                    for (int s2 = 0; s2 <= _numIntervals; s2++) {
                        double a = _min + s2 * ((_max - _min) / _numIntervals);
                        _coef[i] = a;
                        double v = ng(index, dataSets, matrices, x, nodes, _coef);

                        System.out.println(a + "\t" + v);
                    }

                    System.out.println("Data for " + nodes.get(i) + " to " + x);
                    System.out.println("Bump = " + coef[i]);
                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                        continue;
                    } else if (coef[i] == _coef[i]) {
                        continue;
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
//                        System.out.println("h = " + h + " " + Arrays.toString(coef));
                        return coef;
                    } else {
                        break H;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 50);

        double[] c = new double[nodes.size()];
        Arrays.fill(c, Double.NaN);

//        System.out.println("h = " + (h - 1) + " " + Arrays.toString(c));
        return c;
    }

    private double[] maximizeNonGaussianity6(int index, List <DataSet> dataSets, List <DoubleMatrix2D> matrices,
                                             Node x, List <Node> parents) {
//        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);

        double min = 0.0;
        double max = 1.0;
        int numIntervals = 25;

        List <Node> nodes = new ArrayList <Node>(parents);
        double[] coef = new double[nodes.size()];

        List <double[]> coefHistory = new ArrayList <double[]>();

        int h = 0;

        H:
        do {
            for (int i = 0; i < nodes.size(); i++) {
                coef[i] = 0.0;

                double maxA = Double.NaN;
                double maxV = Double.NEGATIVE_INFINITY;


                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
                // maximum to the right or left of -1, searching both directions at once.
                for (int s = 0; s <= numIntervals; s++) {
                    double a = min + s * ((max - min) / numIntervals);

                    coef[i] = a;
                    double vRight = ng(index, dataSets, matrices, x, nodes, coef);

                    coef[i] = -a;
                    double vLeft = ng(index, dataSets, matrices, x, nodes, coef);

                    if (vRight > maxV) {
                        maxA = a;
                        maxV = vRight;
                    }

                    if (vLeft > maxV) {
                        maxA = -a;
                        maxV = vLeft;
                    }
                }

                if (maxA <= -1.0 || maxA >= 1.0) maxA = Double.NaN;

                coef[i] = maxA;

                boolean printValues = false;

                if (printValues) {

                    // Print out values in an interval.
                    if (true /*Double.isNaN(coef[i]) /*!foundBump && !undefinedNongaussianity*/) {
                        double[] _coef = Arrays.copyOf(coef, coef.length);
                        double _min = -max;
                        double _max = max;
                        int _numIntervals = 2 * numIntervals;

                        for (int s2 = 0; s2 <= _numIntervals; s2++) {
                            double a = _min + s2 * ((_max - _min) / _numIntervals);
                            _coef[i] = a;
                            double v = ng(index, dataSets, matrices, x, nodes, _coef);

                            System.out.println(a + "\t" + v);
                        }

                        System.out.println("Data for " + nodes.get(i) + " to " + x);
                        System.out.println("Bump = " + coef[i]);
                    }
                }
            }

            for (int j = coefHistory.size() - 1; j >= 0; j--) {
                double[] _coef = coefHistory.get(j);
                boolean equals = true;

                for (int i = 0; i < coef.length; i++) {
                    if (Double.isNaN(coef[i]) || Double.isNaN(_coef[i])) {
                        if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
                            continue;
                        }
                    } else {
                        if (coef[i] == _coef[i]) {
                            continue;
                        }
                    }

                    equals = false;
                    break;
                }

                if (equals) {
                    if (j == coefHistory.size() - 1) {
                        return coef;
                    } else {
                        break H;
                    }
                }
            }

            coefHistory.add(Arrays.copyOf(coef, coef.length));
        } while (++h <= 1000);

        double[] c = new double[nodes.size()];
        Arrays.fill(c, Double.NaN);

        return c;
    }

    public double ng(int index, List <DataSet> dataSets, List <DoubleMatrix2D> dataSetMatrices, Node x, List <Node> parents, double[] coefs) {
        List <Double> _x = new ArrayList <Double>();
        List <List <Double>> _parents = new ArrayList <List <Double>>();

        for (int i = 0; i < parents.size(); i++) {
            _parents.add(new ArrayList <Double>());
        }

        DataSet dataSet = dataSets.get(index);
        DoubleMatrix2D matrix = dataSetMatrices.get(index);

        int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));

        int[] parentsColumns = new int[parents.size()];

        for (int i = 0; i < parents.size(); i++) {
            parentsColumns[i] = dataSets.get(0).getColumn(dataSet.getVariable(parents.get(i).getName()));
        }

        int rows[] = new int[matrix.rows()];
        for (int i = 0; i < rows.length; i++) rows[i] = i;

        DoubleMatrix1D __x = (matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0)).copy();

        List <DoubleMatrix1D> __parents = new ArrayList <DoubleMatrix1D>();

        for (int i = 0; i < parents.size(); i++) {
            __parents.add((matrix.viewSelection(rows, new int[]{parentsColumns[i]}).viewColumn(0)).copy());
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

        List <Double> _x2 = new ArrayList <Double>();

        for (int i = 0; i < _x.size(); i++) {

            // Node _x given parents. Its coefficient is fixed at 1. Also, coefficients for all
            // other variables not neighbors of _x are fixed at zero.
            double d = _x.get(i);

            if (Double.isNaN(d)) {
                continue;
            }

            for (int j = 0; j < parents.size(); j++) {
                double coef = coefs[j];

                if (Double.isNaN(coef)) {
                    continue;
                }

                Double parentValue = _parents.get(j).get(i);

                if (Double.isNaN(parentValue)) {
                    continue;
                }

                d -= coef * parentValue;
            }

            _x2.add(d);
        }

        double[] __x2 = new double[_x2.size()];

        for (int i = 0; i < _x2.size(); i++) __x2[i] = _x2.get(i);

//        double stat = Math.abs(StatUtils.kurtosis(__x2));

//        double expected = StatUtils.mean(__x2);
//        double diff = expected - logCoshExp;
//        double stat = diff * diff;

        double stat = aSquared(__x2);

//        return logCoshScore(x, parents);
//
        return stat;
    }

    public Graph optimalNongaussian1(Graph graph, List <DataSet> dataSets, List <DoubleMatrix2D> matrices) {
//        dataSets = DataUtils.centerData(dataSets);

//        DataSet dataSet = dataSets.get(0);
//        DoubleMatrix2D matrix = matrices.get(0);

        int numNodes = dataSets.get(0).getNumColumns();
        List <Node> nodes = dataSets.get(0).getVariables();
        System.out.println(nodes);

        double[][] WAll = new double[numNodes][numNodes];

        List <DataSet> residuals = new ArrayList <DataSet>();
        List <DoubleMatrix2D> allW = new ArrayList <DoubleMatrix2D>();

        for (DataSet dataSet : dataSets) {
            double[][] W = estimateW(graph, matrices, numNodes, nodes);

            DoubleMatrix2D W2 = new DenseDoubleMatrix2D(W);
            DoubleMatrix2D X = dataSet.getDoubleData();

            DoubleMatrix2D e = new Algebra().mult(W2, X.viewDice());
            DataSet residual = ColtDataSet.makeData(nodes, e.viewDice());
            residuals.add(residual);
            allW.add(W2);

        }

        DataSet concat = DataUtils.concatenateDataSets(residuals);
        DoubleMatrix2D E = concat.getDoubleData();
        int numDataSets = 1;

        for (int k = 0; k < numDataSets; k++) {
            DoubleMatrix2D W = allW.get(k);
            DoubleMatrix2D A = new Algebra().inverse(W);
            DoubleMatrix2D X2 = new Algebra().mult(A, E.viewDice());
            DataSet dataSet = ColtDataSet.makeContinuousData(nodes, X2.viewDice());

            double[][] W2 = estimateW(graph, matrices, numNodes, nodes);

            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    WAll[i][j] += W2[i][j];
                }
            }
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                WAll[i][j] /= numDataSets;
            }
        }

        Graph _graph = new EdgeListGraph(nodes);

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;
                double coef = WAll[i][j];

                if (Math.abs(coef) >= epsilon) {
                    _graph.addDirectedEdge(nodes.get(j), nodes.get(i));
                }
            }
        }

        return _graph;
    }

    private double[][] estimateW(Graph graph, List <DoubleMatrix2D> matrices, int numNodes, List <Node> nodes) {
        double bound = 5;

        double min = -bound;
        double max = bound;

        // create W
        double[][] W = initializeW(numNodes);

        // Initialize mappings.
        final List <Mapping> allMappings = new ArrayList <Mapping>();

        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i == j) continue;

                Node v1 = nodes.get(i);
                Node v2 = nodes.get(j);

                Node w1 = graph.getNode(v1.getName());
                Node w2 = graph.getNode(v2.getName());

                if (graph.isAdjacentTo(w1, w2)) {
                    allMappings.add(new Mapping(i, j));
                }
            }
        }

        maxMappings(0, matrices, min, max, W, allMappings);

        System.out.println();
        System.out.println(MatrixUtils.toString(W));


//        DoubleMatrix2D W2 = new DenseDoubleMatrix2D(W);
//        DoubleMatrix2D X = dataSet.getDoubleData();
//        new Algebra().mult(W2, X.viewDice());
//        DoubleMatrix2D cov = new Algebra().mult(W2, W2.viewDice());
//        System.out.println("cov = " + cov);
//        CovarianceMatrix cov2 = new CovarianceMatrix(dataSet.getVariables(), cov, dataSet.getNumRows());
//        CorrelationMatrix corr = new CorrelationMatrix(cov2);
//        System.out.println(corr);

        Graph _graph = new EdgeListGraph(nodes);

        for (Mapping mapping : allMappings) {
            int i = mapping.getI();
            int j = mapping.getJ();

            Node node1 = nodes.get(j);
            Node node2 = nodes.get(i);

            if (_graph.isAdjacentTo(node1, node2)) continue;

            double _coef = -W[i][j];
            double _coef2 = -W[j][i];

            double _bound = bound;

            if (_coef >= _bound || _coef <= -_bound) _coef = 0.0;
            if (_coef2 >= _bound || _coef2 <= -_bound) _coef2 = 0.0;

            W[i][j] = -_coef;
            W[j][i] = -_coef2;

        }
        return W;
    }

//    private double[] maximizeNonGaussianityPooled(List<DataSet> dataSets, List<DoubleMatrix2D> matrices,
//                                                  Node x, List<Node> parents) {
////        System.out.println("Examining non-Gaussianity of " + x + " given " + parents);
//
//        double min = -50.0;
//        double max = 50.0;
//        int numIntervals = 250;
//
//        if (min >= 0) {
//            throw new IllegalStateException("Min must be less than zero.");
//        }
//
//        if (max <= 0) {
//            throw new IllegalStateException("Max must be greater than zero.");
//        }
//
//        List<Node> nodes = new ArrayList<Node>(parents);
//        double[] coef = new double[nodes.size()];
//
//        List<double[]> coefHistory = new ArrayList<double[]>();
//
//        int h = 0;
//
//        H:
//        do {
//            for (int i = 0; i < nodes.size(); i++) {
//                coef[i] = 0.0;
//                double ngX = ngPooled(dataSets, matrices, x, nodes, coef);
//
//                boolean ascendingLeft = false;
//                boolean ascendingRight = false;
//
//                double bumpLeft = ngX;
//                double bumpRight = ngX;
//
//                double lastA = Double.NaN;
//                double lastValueLeft = Double.NaN;
//                double lastValueRight = Double.NaN;
//
//                boolean foundBumpLeft = false;
//                boolean foundBumpRight = false;
//
//                // Starting at -1 just in case there's a local maximum at 0. Looking for the first local
//                // maximum to the right or left of -1, searching both directions at once.
//                for (int s = -1; s <= numIntervals; s++) {
//                    double a = s * (Math.max(-min, max) / numIntervals);
//
//                    if (Double.isNaN(lastA)) {
//                        coef[i] = a;
//                        double _vRight = ngPooled(dataSets, matrices, x, nodes, coef);
//
//                        coef[i] = -a;
//                        double _vLeft = ngPooled(dataSets, matrices, x, nodes, coef);
//
//                        if (Double.isNaN(_vRight) || Double.isNaN(_vLeft)) {
//                            continue;
//                        }
//
//                        lastA = a;
//                        lastValueLeft = _vLeft;
//                        lastValueRight = _vRight;
//                        continue;
//                    }
//
//                    if (-a >= min) {
//                        coef[i] = -a;
//                        double vLeft = ngPooled(dataSets, matrices, x, nodes, coef);
//
//                        if (!ascendingLeft && vLeft > lastValueLeft) {
//                            ascendingLeft = true;
//                        } else if (ascendingLeft && vLeft < lastValueLeft) {
//                            if (lastValueLeft >= bumpLeft) {
//                                bumpLeft = -lastA;
//                                foundBumpLeft = true;
//                            }
//                        }
//
//                        lastValueLeft = vLeft;
//                    }
//
//                    if (a <= max) {
//                        coef[i] = a;
//                        double vRight = ngPooled(dataSets, matrices, x, nodes, coef);
//
//                        if (!ascendingRight && vRight > lastValueRight) {
//                            ascendingRight = true;
//                        } else if (ascendingRight && vRight < lastValueRight) {
//                            if (lastValueRight >= bumpRight) {
//                                bumpRight = lastA;
//                                foundBumpRight = true;
//                            }
//                        }
//
//                        lastValueRight = vRight;
//                    }
//
//                    lastA = a;
//
//                    if (foundBumpLeft || foundBumpRight) break;
//                }
//
//                if (foundBumpRight) {
//                    coef[i] = bumpRight;
//                } else if (foundBumpLeft) {
//                    coef[i] = bumpLeft;
//                } else {
//                    coef[i] = Double.NaN;
//                }
//
//                boolean printValues = false;
//
//                if (printValues) {
//
//                    // Print out values in an interval.
//                    if (true) {
//                        double[] _coef = Arrays.copyOf(coef, coef.length);
//                        double _min = -max;
//                        double _max = max;
//                        int _numIntervals = 2 * numIntervals;
//
//                        for (int s2 = 0; s2 <= _numIntervals; s2++) {
//                            double a = _min + s2 * ((_max - _min) / _numIntervals);
//                            _coef[i] = a;
//                            double v = ngPooled(dataSets, matrices, x, nodes, _coef);
//
//                            System.out.println(a + "\t" + v);
//                        }
//
//                        System.out.println("Data for " + nodes.get(i) + " to " + x);
//                        System.out.println("Bump = " + coef[i]);
//                    }
//                }
//            }
//
//            for (int j = coefHistory.size() - 1; j >= 0; j--) {
//                double[] _coef = coefHistory.get(j);
//                boolean equals = true;
//
//                for (int i = 0; i < coef.length; i++) {
//                    if (Double.isNaN(coef[i]) || Double.isNaN(_coef[i])) {
//                        if (Double.isNaN(coef[i]) && Double.isNaN(_coef[i])) {
//                            continue;
//                        }
//                    } else {
//                        if (coef[i] == _coef[i]) {
//                            continue;
//                        }
//                    }
//
//                    equals = false;
//                    break;
//                }
//
//                if (equals) {
//                    if (j == coefHistory.size() - 1) {
//                        System.out.println("h = " + h + " " + Arrays.toString(coef));
//                        return coef;
//                    } else {
//                        break H;
//                    }
//                }
//            }
//
//            coefHistory.add(Arrays.copyOf(coef, coef.length));
//        } while (++h <= 1000);
//
//        double[] c = new double[nodes.size()];
//        Arrays.fill(c, Double.NaN);
//
//        System.out.println("h = " + (h - 1) + " " + Arrays.toString(c));
//        return c;
//    }

    private double logcosh(double[] col) {
        double[] _col = removeNaN(col);
        double[] standardizedCol = DataUtils.standardizeData(_col);

        double sum = 0.0;

        for (int i = 0; i < standardizedCol.length; i++) {
//            sum += Math.abs(standardizedCol[i]);
            sum += Math.log(Math.cosh(standardizedCol[i]));
        }

        double expected = sum / standardizedCol.length;
//        double diff = expected - Math.sqrt(2.0 / Math.PI);
        double diff = expected - logCoshExp;
        double score = diff * diff;

        return expected;
    }


//    public double ng(DataSet dataSet, Node x, Node y, double a) {
//        int _x = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
//        int _y = dataSets.get(0).getColumn(dataSet.getVariable(y.getName()));
//
//        DoubleMatrix2D data = dataSet.getDoubleData();
//
//        int rows[] = new int[data.rows()];
//        for (int i = 0; i < rows.length; i++) rows[i] = i;
//
//        DoubleMatrix1D __x = data.viewSelection(rows, new int[]{_x}).viewColumn(0).copy();
//        DoubleMatrix1D __y = data.viewSelection(rows, new int[]{_y}).viewColumn(0).copy();
//
//        for (int i = 0; i < __x.size(); i++) {
//            __x.set(i, __x.get(i) - a * __y.get(i));
//        }
//
//        double stat = aSquared(__x.toArray());
//        return stat;
//    }
//
//    public double ng2(List<DataSet> dataSets, List<DoubleMatrix2D> dataSetMatrices, Node x, Node y, double a) {
//        List<Double> _x = new ArrayList<Double>();
//        List<Double> _y = new ArrayList<Double>();
//
//        for (int k = 0; k < dataSets.size(); k++) {
//            DataSet dataSet = dataSets.get(k);
//            DoubleMatrix2D matrix = dataSetMatrices.get(k);
//
//            int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
//            int yColumn = dataSets.get(0).getColumn(dataSet.getVariable(y.getName()));
//
//            int rows[] = new int[matrix.rows()];
//            for (int i = 0; i < rows.length; i++) rows[i] = i;
//
//            DoubleMatrix1D __x = matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0).copy();
//            DoubleMatrix1D __y = matrix.viewSelection(rows, new int[]{yColumn}).viewColumn(0).copy();
//
//            for (int i = 0; i < __x.size(); i++) {
//                if (!Double.isNaN(__x.get(i)) && !Double.isNaN(__y.get(i))) {
//                    _x.add(__x.get(i));
//                    _y.add(__y.get(i));
//                }
//            }
//        }
//
//        for (int i = 0; i < _x.size(); i++) {
//            _x.set(i, _x.get(i) - a * _y.get(i));
//        }
//
//        double[] __x = new double[_x.size()];
//
//        for (int i = 0; i < _x.size(); i++) __x[i] = _x.get(i);
//
//        double stat = aSquared(__x);
//        return stat;
//    }

//    public double ng3(List<DataSet> dataSets, List<DoubleMatrix2D> dataSetMatrices, Node x, List<Node> parents, double[] coefs) {
//        List<Double> z = new ArrayList<Double>();
//
//        for (int k = 0; k < dataSets.size(); k++) {
//            DataSet dataSet = dataSets.get(k);
//            DoubleMatrix2D matrix = dataSetMatrices.get(k);
//
//            int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
//
//            int[] parentsColumns = new int[parents.size()];
//
//            for (int i = 0; i < parents.size(); i++) {
//                parentsColumns[i] = dataSets.get(0).getColumn(dataSet.getVariable(parents.get(i).getName()));
//            }
//
//            int rows[] = new int[matrix.rows()];
//            for (int i = 0; i < rows.length; i++) rows[i] = i;
//
//            DoubleMatrix1D __x = matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0).copy();
//
//            List<DoubleMatrix1D> __parents = new ArrayList<DoubleMatrix1D>();
//
//            for (int i = 0; i < parents.size(); i++) {
//                __parents.add(matrix.viewSelection(rows, new int[]{parentsColumns[i]}).viewColumn(0).copy());
//            }
//
//            for (int i = 0; i < __x.size(); i++) {
//                double d = __x.get(i);
//
//                for (int j = 0; j < parents.size(); j++) {
//                    d -= coefs[j] * __parents.get(j).get(i);
//                }
//
//                if (!Double.isNaN(d)) {
//                    z.add(d);
//                }
//            }
//        }
//
//        double[] _z = new double[z.size()];
//
//        for (int i = 0; i < z.size(); i++) _z[i] = z.get(i);
//
//        double stat = aSquared(_z);
//        return stat;
//    }
//
//    public double ng3SingleDataset(DataSet dataSet, DoubleMatrix2D matrix, Node x, List<Node> parents, double[] coefs) {
//        List<Double> z = new ArrayList<Double>();
//
//        int xColumn = dataSets.get(0).getColumn(dataSet.getVariable(x.getName()));
//
//        int[] parentsColumns = new int[parents.size()];
//
//        for (int j = 0; j < parents.size(); j++) {
//            parentsColumns[j] = dataSets.get(0).getColumn(dataSet.getVariable(parents.get(j).getName()));
//        }
//
//        for (int i = 0; i < matrix.rows(); i++) {
//            double d = matrix.get(i, xColumn);
//
//            for (int j = 0; j < parents.size(); j++) {
//                d -= coefs[j] * matrix.get(i, parentsColumns[j]);
//            }
//
//            if (!Double.isNaN(d)) {
//                z.add(d);
//            }
//        }
//
//        double[] _z = new double[z.size()];
//
//        for (int i = 0; i < z.size(); i++) _z[i] = z.get(i);
//
////        return StatUtils.kurtosis(_z);
//        return aSquared(_z);
////        return -expScoreStandardized(_z);
//    }

    private double exp2(double[] col) {
        double[] _col = removeNaN(col);
        double[] standardizedCol = DataUtils.standardizeData(_col);

        double sum = 0.0;

        for (int i = 0; i < standardizedCol.length; i++) {
//            sum += Math.abs(standardizedCol[i]);
            double y = standardizedCol[i];
            sum += -Math.exp(-Math.pow(y, 2.0) / 2.0);
        }

        double expected = sum / standardizedCol.length;
//        double diff = expected - Math.sqrt(2.0 / Math.PI);
//        double diff = expected - logCoshExp;
//        double score = diff * diff;

        return expected;
    }

    public double ngPooled(List <DataSet> dataSets, List <DoubleMatrix2D> dataSetMatrices, Node x, List <Node> parents, double[] coefs) {
        List <Double> _x = new ArrayList <Double>();
        List <List <Double>> _parents = new ArrayList <List <Double>>();

        for (int i = 0; i < parents.size(); i++) {
            _parents.add(new ArrayList <Double>());
        }

        double sum = 0.0;

        for (int index = 0; index < dataSets.size(); index++) {
            DataSet dataSet = dataSets.get(index);
            DoubleMatrix2D matrix = dataSetMatrices.get(index);

            int xColumn = dataSet.getColumn(dataSet.getVariable(x.getName()));

            int[] parentsColumns = new int[parents.size()];

            for (int i = 0; i < parents.size(); i++) {
                parentsColumns[i] = dataSets.get(0).getColumn(dataSet.getVariable(parents.get(i).getName()));
            }

            int rows[] = new int[matrix.rows()];
            for (int i = 0; i < rows.length; i++) rows[i] = i;

            DoubleMatrix1D __x = matrix.viewSelection(rows, new int[]{xColumn}).viewColumn(0).copy();

            List <DoubleMatrix1D> __parents = new ArrayList <DoubleMatrix1D>();

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

            List <Double> _x2 = new ArrayList <Double>();

            for (int i = 0; i < _x.size(); i++) {
                double d = _x.get(i);

                for (int j = 0; j < parents.size(); j++) {
                    double coef = coefs[j];

                    if (Double.isNaN(coef)) {
                        coef = 0.0;
                    }

                    Double parentValue = _parents.get(j).get(i);

                    if (Double.isNaN(parentValue)) {
                        parentValue = 0.0;
                    }

                    d -= coef * parentValue;
                }

//            if (!Double.isNaN(d)) {
                _x2.add(d);
//            }
            }

            double[] __x2 = new double[_x2.size()];

            for (int i = 0; i < _x2.size(); i++) __x2[i] = _x2.get(i);

//        double stat = Math.abs(StatUtils.kurtosis(__x2));

//        double expected = StatUtils.mean(__x2);
//        double diff = expected - logCoshExp;
//        double stat = diff * diff;

            double stat = aSquared(__x2);

            sum += stat;
        }

        return sum / dataSets.size();
    }

    private double aSquared(double[] __x) {
        double stat = new AndersonDarlingTest(__x).getASquaredStar();
        return stat;
    }

    private List <Node> pathBlockingSet(Graph graph, Node x, Node y, boolean includeY) {
        return adjacencySet(graph, x, y, includeY);
    }

    private List <Node> pathBlockingSet2(Graph graph, Node x, Node y, boolean includeY) {
        return adjacencySet2(graph, x, y, includeY);
    }

    private List <Node> adjacencySet(Graph graph, Node x, Node y, boolean includeY) {
//        Set<Node> adj = new HashSet<Node>(pathBlockingSetExcluding(graph, x, y, Collections.singleton(y)));

        Set <Node> adj = new HashSet <Node>(graph.getAdjacentNodes(x));
        adj.addAll(graph.getAdjacentNodes(y));
//
//        ---added
//        for (Node node : new HashSet<Node>(adj)) {
//            if (graph.isChildOf(node, x) && graph.isChildOf(node, y)) {
//                adj.remove(node);
//            }
//        }

        adj.remove(x);
        adj.remove(y);

        if (includeY) {
            adj.add(y);
        }

        return new ArrayList <Node>(adj);
    }

    private List <Node> adjacencySet2(Graph graph, Node x, Node y, boolean includeY) {
//        Set<Node> adj = new HashSet<Node>(pathBlockingSetExcluding(graph, x, y, Collections.singleton(y)));

        Set <Node> adj = new HashSet <Node>(graph.getAdjacentNodes(x));
        adj.addAll(graph.getAdjacentNodes(y));

        for (Node node : new HashSet <Node>(adj)) {
            if (graph.isChildOf(node, x) && graph.isChildOf(node, y)) {
                adj.remove(node);
            }
        }

        adj.remove(x);
        adj.remove(y);

        if (includeY) {
            adj.add(y);
        }

        return new ArrayList <Node>(adj);
    }

    private List <Node> pathBlockingSetExcluding(Graph graph, Node x, Node y, Set <Node> excludedNodes) {
        List <Node> condSet = new LinkedList <Node>();

        for (Node b : graph.getAdjacentNodes(x)) {
            if (!condSet.contains(b) && !excludedNodes.contains(b)) {
                condSet.add(b);
            }

            if (!graph.isParentOf(b, x)) {
                for (Node parent : graph.getParents(b)) {
                    if (!condSet.contains(parent) && !excludedNodes.contains(parent)) {
                        condSet.add(parent);
                    }
                }
            }
        }

        for (Node parent : graph.getParents(y)) {
            if (!condSet.contains(parent) && !excludedNodes.contains(parent)) {
                condSet.add(parent);
            }
        }

        condSet.remove(x);
        condSet.remove(y);

        return condSet;
    }

    private Graph search2(List <Node> nodes) {
        Graph graph = new EdgeListGraph(nodes);

        for (Node y : nodes) {
            for (Node x : nodes) {
                if (y == x) continue;

                List <Node> condxMinus = new ArrayList <Node>();
                List <Node> condxPlus = new ArrayList <Node>(condxMinus);
                condxPlus.add(y);

                double xPlus = score(x, condxPlus);
                double xMinus = score(x, condxMinus);

                List <Node> condyMinus = new ArrayList <Node>();
                List <Node> condyPlus = new ArrayList <Node>(condyMinus);
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

    private void search2AtDepth(List <Node> nodes, Graph graph, int depth) {
        for (Node y : nodes) {
            List <Node> parentsy = graph.getParents(y);

            EDGE:
            for (Node x : parentsy) {
                List <Node> _parentsy = new LinkedList <Node>(parentsy);
                _parentsy.remove(x);

                if (_parentsy.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(_parentsy.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List <Node> condSet = GraphUtils.asList(choice, _parentsy);

                        List <Node> condyMinus = new ArrayList <Node>(condSet);
                        List <Node> condyPlus = new ArrayList <Node>(condyMinus);
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
        List <Edge> edges = graph.getEdges();

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


        Map <Node, Set <Node>> adjacencies = new HashMap <Node, Set <Node>>();
        List <Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            adjacencies.put(node, new HashSet <Node>());
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

    private boolean searchAtDepth0(List <Node> nodes, Map <Node, Set <Node>> adjacencies) {
        List <Node> empty = Collections.emptyList();
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

    private boolean searchAtDepth(List <Node> nodes, Map <Node, Set <Node>> adjacencies, int depth) {
        int numRemoved = 0;
        int count = 0;

        for (Node x : nodes) {
//            if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());

            List <Node> adjx = new ArrayList <Node>(adjacencies.get(x));

            EDGE:
            for (Node y : adjx) {
                List <Node> _adjx = new ArrayList <Node>(adjacencies.get(x));
                _adjx.remove(y);

                if (_adjx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(_adjx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List <Node> condSet = GraphUtils.asList(choice, _adjx);

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
        System.out.println("Resolving " + x + " === " + y);

        TetradLogger.getInstance().log("info", "\nEDGE " + x + " --- " + y);

        SortedMap <Double, String> scoreReports = new TreeMap <Double, String>();

        Direction direction = null;

        List <Node> condxMinus = new ArrayList <Node>();
        List <Node> condxPlus = new ArrayList <Node>(condxMinus);
        condxPlus.add(y);

        double xPlus = score(x, condxPlus);
        double xMinus = score(x, condxMinus);

        List <Node> condyMinus = new ArrayList <Node>();
        List <Node> condyPlus = new ArrayList <Node>(condyMinus);
        condyPlus.add(x);

        double yPlus = score(y, condyPlus);
        double yMinus = score(y, condyMinus);

        double xMax = xPlus > xMinus ? xPlus : xMinus;
        double yMax = yPlus > yMinus ? yPlus : yMinus;

        double score = combinedScore(xMax, yMax);
        TetradLogger.getInstance().log("info", "Score = " + score);

        if (false) { //this.score == Lofs.Score.other) {
            boolean standardize = false;

            double[] _fX = expScoreUnstandardizedSList(x, Collections. <Node>emptyList());
            AndersonDarlingTest testX = new AndersonDarlingTest(_fX);
            double[] sColumnX = testX.getSColumn();

            double[] _fXY = expScoreUnstandardizedSList(x, Collections.singletonList(y));
            AndersonDarlingTest testXY = new AndersonDarlingTest(_fXY);
            double[] sColumnXY = testXY.getSColumn();

            double[] _fY = expScoreUnstandardizedSList(y, Collections. <Node>emptyList());
            AndersonDarlingTest testY = new AndersonDarlingTest(_fY);
            double[] sColumnY = testY.getSColumn();

            double[] _fYX = expScoreUnstandardizedSList(y, Collections.singletonList(x));
            AndersonDarlingTest testYX = new AndersonDarlingTest(_fYX);
            double[] sColumnYX = testYX.getSColumn();

            double pX = dependentTTest(sColumnX, sColumnXY, 0.0);
            double pY = dependentTTest(sColumnY, sColumnYX, 0.0);

            System.out.println("pX = " + pX + " pY = " + pY);

            List <Node> adjX = graph.getAdjacentNodes(x);
            adjX.remove(x);

            List <Node> adjY = graph.getAdjacentNodes(y);
            adjY.remove(y);

            List <Node> adjXPlus = new ArrayList <Node>(adjX);
            adjXPlus.add(y);

            List <Node> adjYPlus = new ArrayList <Node>(adjY);
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

    private boolean isIndependent(Node x, Node y, List <Node> z) {
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

        List <Double> _x = new ArrayList <Double>();
        List <Double> _y = new ArrayList <Double>();

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
        List <Double> v = new ArrayList <Double>();

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

            List <Edge> edges = trueGraph.getEdges(_x, _y);
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
            countMap.put(type, new HashMap <String, Integer>());
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

    // Residual of x is independent of y.

    private Graph igci(Graph graph) {
//        List<DataSet> centered = DataUtils.centerData(dataSets);
//        DataSet concat = DataUtils.concatenateDataSets(centered);
//        DataSet concatData = DataUtils.standardizeData(concat);

        DataSet dataSet = dataSets.get(0);
        DoubleMatrix2D matrix = dataSet.getDoubleData();

        Graph _graph = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();


            Node _x = dataSet.getVariable(x.getName());
            Node _y = dataSet.getVariable(y.getName());

            int xIndex = dataSet.getVariables().indexOf(_x);
            int yIndex = dataSet.getVariables().indexOf(_y);

            double[] xCol = matrix.viewColumn(xIndex).toArray();
            double[] yCol = matrix.viewColumn(yIndex).toArray();

            double f = igci(xCol, yCol, 1, 1);

            if (f < 0) {
                _graph.addDirectedEdge(x, y);
            } else {
                _graph.addDirectedEdge(y, x);
            }
        }

        return _graph;
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

            List <Pair> _x = new ArrayList <Pair>();

            for (int i = 0; i < x.length; i++) {
                _x.add(new Pair(i, x[i]));
            }

            Collections.sort(_x, new Comparator <Pair>() {
                @Override
                public int compare(Pair pair1, Pair pair2) {
                    return new Double(pair1.value).compareTo(new Double(pair2.value));
                }
            });

            List <Pair> _y = new ArrayList <Pair>();

            for (int i = 0; i < y.length; i++) {
                _y.add(new Pair(i, y[i]));
            }

            Collections.sort(_y, new Comparator <Pair>() {
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

    private void increment(Map <String, Map <String, Integer>> map, String key, String s) {
        if (map.get(key).get(s) == null) {
            map.get(key).put(s, 1);
        }

        map.get(key).put(s, map.get(key).get(s) + 1);
    }

    // digamma

    private double combinedScore(double score1, double score2) {
        return score1 + score2;
    }

    private double score(Node y, List <Node> parents) {
//        if (true) {
//            return meanResidual(y, parents);
//        }

        if (score == Lofs.Score.andersonDarling) {
            return andersonDarlingPASquareStar(y, parents);
        } else if (score == Lofs.Score.kurtosis) {
            return entropy(y, parents);
//            return Math.abs(StatUtils.kurtosis(residuals(y, parents, true, true)));
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
//            return minusExpXSquaredDividedByTwo(y, parents);
        } else if (score == Lofs.Score.logcosh) {
            return logCoshScore(y, parents);
        }

        throw new IllegalStateException();
    }

    private double meanResidual(Node node, List <Node> parents) {
        double[] _f = residuals(node, parents, false, true);
        return StatUtils.mean(_f);
    }

    private double meanAbsolute(Node node, List <Node> parents) {
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

    private double expScoreUnstandardized(Node node, List <Node> parents) {
        double[] _f = residuals(node, parents, false, true);

        for (int k = 0; k < _f.length; k++) {
            double v = _f[k];
            _f[k] = v;
        }

        double expected = StatUtils.mean(_f);
        return expected;
    }

    private double[] expScoreUnstandardizedSList(Node node, List <Node> parents) {
        double[] _f = residuals(node, parents, false, true);

        for (int k = 0; k < _f.length; k++) {
            double v = _f[k];
            _f[k] = v;
        }

        return _f;
    }

    private double expScoreStandardized(Node node, List <Node> parents) {
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

    //=============================PRIVATE METHODS=========================//

    private double expScoreStandardized(double[] arr) {
        DoubleArrayList f = new DoubleArrayList(arr);

        for (int k = 0; k < arr.length; k++) {
            f.set(k, Math.exp(f.get(k)));
        }

        double expected = Descriptive.mean(f);
        return expected;
    }

    private double logCoshScore(Node node, List <Node> parents) {
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

    private double[] residuals(Node node, List <Node> parents, boolean standardize, boolean removeNaN) {
        List <Double> _residuals = new ArrayList <Double>();

        Node _target = node;
        List <Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

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

            RegressionResult result = getRegressions().get(m).regress(target, regressors);
            double[] residualsSingleDataset = result.getResiduals().toArray();

            if (result.getCoef().length > 0) {
                double intercept = result.getCoef()[0];

                for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
                    residualsSingleDataset[i2] = residualsSingleDataset[i2] + intercept;
                }
            }

//            if (isMeanCenterResiduals()) {
//                double mean = StatUtils.mean(residualsSingleDataset);
//                for (int i2 = 0; i2 < residualsSingleDataset.length; i2++) {
//                    residualsSingleDataset[i2] = residualsSingleDataset[i2] - mean;
//                }
//            }

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
        List <Node> _regressors = Collections.singletonList(parent);
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        if (regressors.contains(child)) {
            throw new IllegalArgumentException();
        }

        double sum = 0.0;

        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = getRegressions().get(m).regress(target, regressors);
            int index = regressors.indexOf(parent);

            if (!Double.isNaN(result.getP()[1 + index])) {
                sum += result.getP()[0];
            }
        }

        return sum / dataSets.size();
    }

//    private double minusExpXSquaredDividedByTwo(Node node, List<Node> parents) {
//        double[] _f = residuals(node, parents, true, true);
//
//        DoubleArrayList f = new DoubleArrayList(_f);
//
//        for (int k = 0; k < _f.length; k++) {
//            double v = Math.exp(-Math.pow(f.get(k), 2.0) / 2);
//            f.set(k, v);
//        }
//
//        double mean = Descriptive.mean(f);
//        double diff = mean - expectedExp();
//        double score = diff * diff;
//
//        return score;
//    }

//    private double expectedExp() {
//        if (Double.isNaN(expectedExp)) {
//            double nsum = 0.0;
//            int ncount = 0;
//
//            for (int i = 0; i < 100; i++) {
//                double sample = RandomUtil.getInstance().nextNormal(0, 1);
//                double v = Math.exp(-Math.pow(sample, 2.0) / 2);
//                nsum += v;
//                ncount++;
//            }
//
//            double navg = nsum / ncount;
//            this.expectedExp = navg;
//        }
//
//        return this.expectedExp;
//    }

    private double localScoreB(Node node, List <Node> parents) {

        double score = 0.0;
        double maxScore = Double.NEGATIVE_INFINITY;

        Node _target = node;
        List <Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = getRegressions().get(m).regress(target, regressors);
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

    private double andersonDarlingPASquareStar(Node node, List <Node> parents) {
        double[] _f = residuals(node, parents, true, true);
//        if (false) { //parents.isEmpty()) {
////            System.out.println("Skew " + node + " | " + parents + " = " + StatUtils.skewness(_f));
//            System.out.println("=== AD Score " + node + " | " + parents + " = " + new AndersonDarlingTest(_f).getASquaredStar());
//        }
        return new AndersonDarlingTest(_f).getASquaredStar();
    }

    private double entropy(Node node, List <Node> parents) {
        int numBins = (int) epsilon;

        double[] _f = residuals(node, parents, true, true);

        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;

        for (double x : _f) {
            if (x < min) min = x;
            if (x > max) max = x;
        }

        int[] v = new int[numBins];
        double width = max - min;

        for (int i = 0; i < _f.length; i++) {
            double x = _f[i];
            double x3 = (x - min) / width; // 0 to 1
            int bin = (int) (x3 * (numBins - 1));  // 0 to numBins - 1
            v[bin]++;
        }

        // Calculate entropy.
        double sum = 0.0;

        for (int i = 0; i < v.length; i++) {
            if (v[i] != 0) {
                double p = v[i] / (double) (numBins - 1);
                sum += p * Math.log(p);
            }
        }

        return -sum;
    }

    private double andersonDarlingPASquareStarB(Node node, List <Node> parents) {
//        List<Double> _residuals = new ArrayList<Double>();

        Node _target = node;
        List <Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

        for (Node _regressor : _regressors) {
            Node variable = getVariable(variables, _regressor.getName());
            regressors.add(variable);
        }

        double sum = 0.0;

        DATASET:
        for (int m = 0; m < dataSets.size(); m++) {
            RegressionResult result = getRegressions().get(m).regress(target, regressors);
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

            // By centering the individual residual columns, all moments of the mixture become weighted averages of the moments
            // of the individual columns. http://en.wikipedia.org/wiki/Mixture_distribution#Finite_and_countable_mixtures
//            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                if (isMeanCenterResiduals()) {
//                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
//                }
//            }

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

    private double pValue(Node node, List <Node> parents) {
        List <Double> _residuals = new ArrayList <Double>();

        Node _target = node;
        List <Node> _regressors = parents;
        Node target = getVariable(variables, _target.getName());
        List <Node> regressors = new ArrayList <Node>();

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

            RegressionResult result = getRegressions().get(m).regress(target, regressors);
            DoubleMatrix1D residualsSingleDataset = result.getResiduals();

            for (int h = 0; h < residualsSingleDataset.size(); h++) {
                if (Double.isNaN(residualsSingleDataset.get(h))) {
                    continue DATASET;
                }
            }

            DoubleArrayList _residualsSingleDataset = new DoubleArrayList(residualsSingleDataset.toArray());

            double mean = Descriptive.mean(_residualsSingleDataset);

//            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                if (isMeanCenterResiduals()) {
//                    _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
//                }
//            }

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

    private Graph getPattern() {
        return pattern;
    }

    private Node getVariable(List <Node> variables, String name) {
        for (Node node : variables) {
            if (name.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    private double[] leaveOutNaN(double[] data) {
        List <Double> _leaveOutMissing = new ArrayList <Double>();

        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                _leaveOutMissing.add(data[i]);
            }
        }

        double[] _data = new double[_leaveOutMissing.size()];

        for (int i = 0; i < _leaveOutMissing.size(); i++) _data[i] = _leaveOutMissing.get(i);

        return _data;
    }

    public enum Rule {
        IGCI, R1TimeLag, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R10b, R11, R11b, R12, R13, R14, R15, R16, R17, R18, R19, R20
    }


    private enum Direction {
        left, right, bidirected, twoCycle, undirected, nonadjacent, nondirected, halfright, halfleft
    }

    private enum WhichMax {
        first, max
    }

    private static class Mapping {
        private int i = -1;
        private int j = -1;

        public Mapping(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }
    }

    private static class Pair {
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

    private class RandomSearch {
        private MultivariateFunction function;
        private double func_tolerance;
        private double param_tolerance;
        private MultivariateMinimum search2;

        public void optimize(MultivariateFunction f, double[] values, double func_tolerance, double param_tolerance) {
            double[] p = values;
            this.func_tolerance = func_tolerance;
            this.param_tolerance = param_tolerance;

            this.function = f;

            search2 = new ConjugateDirectionSearch();


            System.out.println("starting values = " + Arrays.toString(values));

//            double[] p = new double[values.length]; // start with 0
            double[] pRef = new double[p.length];

            findLowerRandom(f, p, 2.0, 400, true);
            findLowerRandom(f, p, 1.0, 200, true);
            findLowerRandom(f, p, 0.5, 50, true);
            findLowerRandom(f, p, 0.1, 50, true);
            findLowerRandom(f, p, 0.01, 50, true);
            findLowerRandom(f, p, 0.001, 50, true);

//            iterateFindLowerRandom(f, p, 2.0, 400);
//            iterateFindLowerRandom(f, p, 1.0, 400);
//            iterateFindLowerRandom(f, p, 0.5, 100);
//            iterateFindLowerRandom(f, p, 0.1, 50);
//            iterateFindLowerRandom(f, p, 0.01, 50);
//            iterateFindLowerRandom(f, p, 0.001, 50);

//            while (true) {
//                System.arraycopy(p, 0, pRef, 0, p.length);
//
//                iterateFindLowerRandom(f, p, 0.05, 500);
//                iterateFindLowerRandom(f, p, 0.01, 500);
//                iterateFindLowerRandom(f, p, 0.005, 500);
//
//                if (pointsEqual(p, pRef)) break;
//            }

//            while (true) {
//                System.arraycopy(p, 0, pRef, 0, p.length);
//
//                values = p;
//                double width = semIm.getFml() / 40.0;
//            if (width > 1.0) width = 1.0;
//
//                iterateFindLowerRandom(f, p, width, 500);
//
//                if (pointsEqual(p, pRef)) break;
//            }

//        for (int factor = 40; factor <= 500; factor += 40) {
//            System.arraycopy(p, 0, pRef, 0, p.length);
//
//            semIm.setFreeParamValues(p);
//            double width = semIm.getFml() / factor;
////            if (width > 1.0) width = 1.0;
//
//            iterateFindLowerRandom(f, p, width, 25);
//
////            if (pointsEqual(p, pRef)) break;
//        }


//        logger.getInstance().log("info", "Wiggling each parameter in turn...");
//
//        slideIndividualParameters(f, p, pRef, .1);
//        slideIndividualParameters(f, p, pRef, .01);
//        slideIndividualParameters(f, p, pRef, .001);
//        slideIndividualParameters(f, p, pRef, .0001);
//        slideIndividualParameters(f, p, pRef, .00001);
//        slideIndividualParameters(f, p, pRef, .000001);

//            semIm.setFreeParamValues(p);

        }

        private void iterateFindLowerRandom(MultivariateFunction fcn, double[] p,
                                            double range, int iterations) {
            while (true) {
                boolean found = false;
                try {
                    found = findLowerRandom(fcn, p, range, iterations, false);
                } catch (Exception e) {
                    return;
                }

                if (!found) {
                    return;
                }
            }
        }

        /**
         * Returns true iff a new point was found with a lower score.
         */
        private boolean findLowerRandom1(MultivariateFunction fcn, double[] p,
                                         double width, int numPoints, boolean exhaustive) {
            double fMin = fcn.evaluate(p);
            double fInit = fMin;

            if (Double.isNaN(fMin)) {
                throw new IllegalArgumentException("Center point must evaluate!");
            }

            // This point will remain fixed, the center of the search.
            double[] fixedP = new double[p.length];
            System.arraycopy(p, 0, fixedP, 0, p.length);

//        boolean changed = false;

            // This point will move around randomly. If it ever has a lower
            // score than p, it will be copied into p (and returned).
            double[] pTemp = new double[p.length];
            System.arraycopy(p, 0, pTemp, 0, p.length);

            for (int i = 0; i < numPoints; i++) {
                randomPointAboutCenter(pTemp, fixedP, width);
                double f = fcn.evaluate(pTemp);

                if (f < fMin) {
                    double partial = getPartial(fMin, f, fixedP, pTemp);

                    fMin = f;
                    System.arraycopy(pTemp, 0, p, 0, pTemp.length);
                    TetradLogger.getInstance().log("optimization", "Cube width = " + width + " FML = " + f);

                    System.out.println("Cube width = " + width + " partial = " + partial + " FML = " + f);

                    if (!exhaustive) {
                        return true;
                    }
                }
            }

            if (exhaustive && fcn.evaluate(pTemp) < fInit) {
                return true;
            }

            return false;
        }

        private boolean findLowerRandom(MultivariateFunction fcn, double[] p,
                                        double width, int numPoints, boolean exhaustive) {
            double fMin = fcn.evaluate(p);
            double fInit = fMin;

            if (Double.isNaN(fMin)) {
                throw new IllegalArgumentException("Center point must evaluate!");
            }

            // This point will remain fixed, the center of the search.
            double[] fixedP = new double[p.length];
            System.arraycopy(p, 0, fixedP, 0, p.length);

            int count = 0;

//        boolean changed = false;

            // This point will move around randomly. If it ever has a lower
            // score than p, it will be copied into p (and returned).
            double[] pTemp = new double[p.length];
            System.arraycopy(p, 0, pTemp, 0, p.length);

            while (count < 200) {
                randomPointAboutCenter(pTemp, fixedP, width);
                double f = fcn.evaluate(pTemp);

                if (f < fMin) {
                    count = 0;

                    search2.optimize(function, pTemp, func_tolerance, param_tolerance);

                    double distance = distance(p, fixedP);

                    fMin = f;
                    System.arraycopy(pTemp, 0, p, 0, pTemp.length);

                    System.out.println("Cube width = " + width + " distance = " + distance + " Score = " + f);
                } else {
                    count++;
                }
            }

            if (fcn.evaluate(pTemp) < fInit) {
                return true;
            }

            return false;
        }

        private double getPartial(double fBefore, double fAfter, double[] pBefore, double[] pAfter) {
            double distance = distance(pBefore, pAfter);
            double height = fAfter - fBefore;
            return height / distance;
        }

        private double distance(double[] pBefore, double[] pAfter) {
            double sum = 0.0;

            for (int i = 0; i < pBefore.length; i++) {
                double diff = pAfter[i] - pBefore[i];
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        }

        private void randomPointAboutCenter(double[] pTemp, double[] fixedP, double width) {
            for (int j = 0; j < pTemp.length; j++) {
                double v = RandomUtil.getInstance().nextDouble();
                pTemp[j] = fixedP[j] + (-width / 2.0 + width * v);
            }
        }

        private boolean pointsEqual(double[] p, double[] pTemp) {
            for (int i = 0; i < p.length; i++) {
                if (p[i] != pTemp[i]) {
                    return false;
                }
            }

            return true;
        }
    }
}
