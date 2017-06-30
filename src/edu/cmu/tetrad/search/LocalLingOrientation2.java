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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Lingam Pattern algorithm as specified in Hoyer et al., "Causal discovery of linear acyclic models with
 * arbitrary distributions," UAI 2008. The test for normality used for residuals is Anderson-Darling, following ad.test
 * in the nortest package of R. The default alpha level is 0.05--that is, p values from AD below 0.05 are taken to
 * indicate nongaussianity.
 * <p/>
 * It is assumed that the pattern is the result of a pattern search such as PC or GES. In any case, it is important that
 * the residuals be independent for ICA to work.
 *
 * @author Joseph Ramsey
 */
public class LocalLingOrientation2 {
    private Graph pattern;
    private List<DataSet> dataSets;
    private double alpha = 0.05;
    private ArrayList<Regression> regressions;
    private List<Node> variables;

    //===============================CONSTRUCTOR============================//

    public LocalLingOrientation2(Graph pattern, List<DataSet> dataSets)
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

    public Graph search() {
//        Graph skeleton = new EdgeListGraph(getPattern());// GraphUtils.undirectedGraph(getPattern());
        Graph skeleton = GraphUtils.undirectedGraph(getPattern());
        Graph graph = new EdgeListGraph(skeleton.getNodes());

        List<Node> nodes = skeleton.getNodes();
        Collections.shuffle(nodes);

        for (Node node : nodes) {
            List<Node> adj = skeleton.getAdjacentNodes(node);

            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
            int[] choice;
            double minP = Double.POSITIVE_INFINITY;
            List<Node> parents = null;

            while ((choice = gen.next()) != null) {
                List<Node> _parents = GraphUtils.asList(choice, adj);
                if (_parents.isEmpty()) continue;

                double p = score(node, _parents);

                if (p > getAlpha()) continue;

                if (p < minP) {
                    minP = p;
                    parents = _parents;
                }
            }

            if (parents == null) {
                continue;
            }

            for (Node _node : adj) {
                if (parents.contains(_node)) {
                    Edge parentEdge = Edges.directedEdge(_node, node);

                    if (!graph.containsEdge(parentEdge) /* && graph.getEdge(parentEdge.getNode1(),
                            parentEdge.getNode2()) != null*/) {
                        graph.addEdge(parentEdge);
                    }
                }
//                else {
//                    Edge childEdge = Edges.directedEdge(node, _node);
//
//                    if (!graph.containsEdge(childEdge)) {
//                        graph.addEdge(childEdge);
//                    }
//                }
            }
        }

        for (Edge edge : skeleton.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        // Resolve 2-cycles.
        for (Edge adj : skeleton.getEdges()) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();
            List<Edge> edges = graph.getEdges(x, y);
            if (edges.size() == 1) continue;

            List<Node> parents = graph.getParents(x);
            double score1 = score(x, parents);

            parents.remove(y);
            double score2 = score(x, parents);

            double diffx = Math.log10(score1 - score2);

            parents = graph.getParents(y);
            double score3 = score(y, parents);

            parents.remove(x);
            double score4 = score(y, parents);

            double diffy = Math.log10(score3 - score4);

            if (diffx < diffy) {
                graph.removeEdge(Edges.directedEdge(x, y));
            } else {
                graph.removeEdge(Edges.directedEdge(y, x));
            }
        }


        // Resolve undirected edge.
//        for (Edge edge : graph.getEdges()) {
//            if (!Edges.isUndirectedEdge(edge)) {
//                continue;
//            }
//
//            Node x = edge.getNode1();
//            Node y = edge.getNode2();
//
//            List<Node> parents = graph.getParents(x);
//            double score1 = score(x, parents);
//
//            parents.remove(y);
//            double score2 = score(x, parents);
//
//            double diffx = Math.log10(score1 - score2);
//
//            parents = graph.getParents(y);
//            double score3 = score(y, parents);
//
//            parents.remove(x);
//            double score4 = score(y, parents);
//
//            double diffy = Math.log10(score3 - score4);
//
//            if (diffx < diffy) {
//                graph.removeEdge(edge);
//                graph.addEdge(Edges.directedEdge(y, x));
//            }
//            else {
//                graph.removeEdge(edge);
//                graph.addEdge(Edges.directedEdge(x, y));
//            }
//        }

        // Replace 2-cycles by undirected edges.
//        for (Edge edge : graph.getEdges()) {
//            if (!graph.containsEdge(edge)) {
//                continue;
//            }
//
//            Node node1 = edge.getNode1();
//            Node node2 = edge.getNode2();
//            Edge reversedEdge = new Edge(node1, node2, edge.getEndpoint2(), edge.getEndpoint1());
//
//            if (graph.containsEdge(reversedEdge)) {
//                graph.removeEdge(edge);
//                graph.removeEdge(reversedEdge);
//                graph.addUndirectedEdge(node1, node2);
//            }
//        }

        new MeekRules().orientImplied(graph);

//        for (Edge edge : graph.getEdges()) {
//            Edge reversed = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(), edge.getEndpoint1());
//
//            if (!graph.containsEdge(reversed)) {
//                graph.removeEdge(edge);
//                graph.addEdge(reversed);
//            }
//        }

        System.out.println(graph);


        return graph;
    }


    public Graph search2() {
        Graph skeleton = new EdgeListGraph(getPattern());// GraphUtils.undirectedGraph(getPattern());
//        Graph skeleton = GraphUtils.undirectedGraph(getPattern());
        Graph graph = new EdgeListGraph(skeleton.getNodes());

        List<Node> nodes = skeleton.getNodes();
        Collections.shuffle(nodes);

//        for (Node node : nodes) {
//            List<Node> adj = skeleton.getAdjacentNodes(node);
//
//            DepthChoiceGenerator gen = new DepthChoiceGenerator(adj.size(), adj.size());
//            int[] choice;
//            double minP = Double.POSITIVE_INFINITY;
//            List<Node> parents = null;
//
//            while ((choice = gen.next()) != null) {
//                List<Node> _parents = GraphUtils.asList(choice, adj);
//                if (_parents.isEmpty()) continue;
//
////                double p = andersonDarlingPB(node, _parents);
//                double p = -localScoreB(node, _parents);
//
////                if (p > getAlpha()) continue;
//
//                if (p < minP) {
//                    minP = p;
//                    parents = _parents;
//                }
//            }
//
//            if (parents == null) {
//                continue;
//            }
//
//            for (Node _node : adj) {
//                if (parents.contains(_node)) {
//                    Edge parentEdge = Edges.directedEdge(_node, node);
//
//                    if (!graph.containsEdge(parentEdge) /* && graph.getEdge(parentEdge.getNode1(),
//                            parentEdge.getNode2()) != null*/) {
//                        graph.addEdge(parentEdge);
//                    }
//                }
////                else {
////                    Edge childEdge = Edges.directedEdge(node, _node);
////
////                    if (!graph.containsEdge(childEdge)) {
////                        graph.addEdge(childEdge);
////                    }
////                }
//            }
//        }

//        for (Edge edge : skeleton.getEdges()) {
//            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
//                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
//            }
//        }

        boolean changed = true;

        while (changed) {
            changed = false;

            // Resolve undirected edge.
            for (Edge edge : skeleton.getEdges()) {
                Node x = edge.getNode1();
                Node y = edge.getNode2();

                List<Node> parents = graph.getParents(x);
                double score1 = score(x, parents);

                parents.remove(y);
                double score2 = score(x, parents);

                double diffx = Math.log10(score1 - score2);

                parents = graph.getParents(y);
                double score3 = score(y, parents);

                parents.remove(x);
                double score4 = score(y, parents);

                double diffy = Math.log10(score3 - score4);

                if (diffx < diffy) {
                    Edge newEdge = Edges.directedEdge(y, x);

                    if (!graph.containsEdge(newEdge)) {
                        graph.addEdge(newEdge);
                        System.out.println("Added " + newEdge);
                        changed = true;
                    }
                } else {
                    Edge newEdge = Edges.directedEdge(x, y);

                    if (!graph.containsEdge(newEdge)) {
                        graph.addEdge(newEdge);
                        System.out.println("Added " + newEdge);
                        changed = true;
                    }
                }
            }
        }


        // Resolve 2-cycles.
        for (Edge adj : skeleton.getEdges()) {
            Node x = adj.getNode1();
            Node y = adj.getNode2();
            List<Edge> edges = graph.getEdges(x, y);
            if (edges.size() == 1) continue;

            List<Node> parents = graph.getParents(x);
            double score1 = score(x, parents);

            parents.remove(y);
            double score2 = score(x, parents);

            double diffx = Math.log10(score1 - score2);

            parents = graph.getParents(y);
            double score3 = score(y, parents);

            parents.remove(x);
            double score4 = score(y, parents);

            double diffy = Math.log10(score3 - score4);

            if (diffx < diffy) {
                graph.removeEdge(Edges.directedEdge(x, y));
            } else {
                graph.removeEdge(Edges.directedEdge(y, x));
            }
        }
//
//

        // Replace 2-cycles by undirected edges.
//        for (Edge edge : graph.getEdges()) {
//            if (!graph.containsEdge(edge)) {
//                continue;
//            }
//
//            Node node1 = edge.getNode1();
//            Node node2 = edge.getNode2();
//            Edge reversedEdge = new Edge(node1, node2, edge.getEndpoint2(), edge.getEndpoint1());
//
//            if (graph.containsEdge(reversedEdge)) {
//                graph.removeEdge(edge);
//                graph.removeEdge(reversedEdge);
//                graph.addUndirectedEdge(node1, node2);
//            }
//        }

        new MeekRules().orientImplied(graph);

//        for (Edge edge : graph.getEdges()) {
//            Edge reversed = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(), edge.getEndpoint1());
//
//            if (!graph.containsEdge(reversed)) {
//                graph.removeEdge(edge);
//                graph.addEdge(reversed);
//            }
//        }

        System.out.println(graph);


        return graph;
    }

    private double score(Node y, List<Node> parents) {
        return andersonDarlingPA(y, parents);
//        return localScoreB(y, parents);
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

        return maxScore;

//        return score / dataSets.size();
    }

    private double andersonDarlingPA(Node node, List<Node> parents) {
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
            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }
        }

        double[] _f = new double[_residuals.size()];

        for (int k = 0; k < _residuals.size(); k++) {
            _f[k] = _residuals.get(k);
        }

        double p = new AndersonDarlingTest(_f).getP();

        System.out.println("Anderson Darling p for " + node + " given " + parents + " = " + p);

        return p;
    }

    private double andersonDarlingPB(Node node, List<Node> parents) {
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
        int count = 0;

        double minP = Double.POSITIVE_INFINITY;

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

//            double mean = Descriptive.mean(_residualsSingleDataset);
//            double std = Descriptive.standardDeviation(Descriptive.variance(_residualsSingleDataset.size(),
//                    Descriptive.sum(_residualsSingleDataset), Descriptive.sumOfSquares(_residualsSingleDataset)));

//            for (int i2 = 0; i2 < _residualsSingleDataset.size(); i2++) {
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean) / std);
//                _residualsSingleDataset.set(i2, (_residualsSingleDataset.get(i2) - mean));
//            }

            for (int k = 0; k < _residualsSingleDataset.size(); k++) {
                _residuals.add(_residualsSingleDataset.get(k));
            }

            double[] _f = new double[_residuals.size()];


            for (int k = 0; k < _residuals.size(); k++) {
                _f[k] = _residuals.get(k);
            }

            double p = new AndersonDarlingTest(_f).getP();

            if (Double.isNaN(p)) continue;

            sum += p;
            count++;

            if (p < minP) minP = p;
        }

        double avg = sum / count;

        System.out.println("Min Anderson Darling p for " + node + " given " + parents + " = " + minP);

        return avg;
//
//        return minP;

//        return sum;
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
}
