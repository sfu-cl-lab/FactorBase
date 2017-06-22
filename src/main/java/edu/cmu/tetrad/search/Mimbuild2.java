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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.StatUtils;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jun 17, 2010 Time: 9:39:48 AM To change this template use File |
 * Settings | File Templates.
 */
public class Mimbuild2 {
    private Graph mim;
    private int minClusterSize = 1;
    private int numCovSamples = 1000;
    private List<Node> latents;
    private List<Node> _latents;
    private Graph structureGraph;
    private List<List<Node>> clustering;
    private double alpha = 1e-6;
    private Knowledge knowledge = new Knowledge();
    private ICovarianceMatrix covMatrix;

    public Mimbuild2() {
    }

    public void setTrueMim(Graph mim) {
        this.mim = mim;
    }

    public Graph search(List<List<Node>> clustering, DataSet data) {
        return search(clustering, null, data);
    }

    public Graph search(List<List<Node>> clustering, List<String> names, DataSet data) {
        // Translate clustering into data variables.

        List<List<Node>> _clustering = new ArrayList<List<Node>>();

        for (List<Node> cluster : clustering) {
            List<Node> _cluster = new ArrayList<Node>();

            for (Node node : cluster) {
                _cluster.add(data.getVariable(node.getName()));
            }

            _clustering.add(_cluster);
        }

        clustering = _clustering;


        System.out.println("Create a latent for each cluster.");

        List<Node> latents = defineLatents(clustering, names);

        System.out.println("Zero mean data.");

        DataUtils.zeroMean(data);
        DoubleMatrix2D _data = data.getDoubleData();

        printClusterSizes(clustering);

        List<Node> allNodes = new ArrayList<Node>();

        for (List<Node> cluster : clustering) {
            allNodes.addAll(cluster);
        }

        printSubgraph(mim, allNodes);
        printClusterSizes(clustering);

        System.out.println("Remove small clusters.");

        List<Node> _latents = removeSmallClusters(latents, clustering, getMinClusterSize());

        if (clustering.isEmpty()) {
            System.out.println("There were no clusters after small clusters were removed..");
        }

        printClusterSizes(clustering);
        printClustering(clustering);

        this.clustering = clustering;

        System.out.println("Calculate loadings");

        Node[][] indicators = getIndicators2(clustering, data);
        double[][] loadings = getLoadings2(clustering, data);
//            double[][] loadings = trueLoadings(mimClustering, mim, sim);

        System.out.println("Estimate covariance matrix over latents");

        DoubleMatrix2D cov = getCov(data, _data, _latents, loadings, indicators);
        ICovarianceMatrix covMatrix = new CovarianceMatrix(_latents, cov, data.getNumRows());
        IndependenceTest testFisherZ = new IndTestFisherZ(covMatrix, getAlpha());

        System.out.println("Run pattern search over latent cov matrix");

        Cpc patternSearch = new Cpc(testFisherZ);
        patternSearch.setKnowledge(getKnowledge());
        patternSearch.setDepth(3);
//
//        Jpc patternSearch = new Jpc(testFisherZ);
//
//        patternSearch.setAlgorithmType(Jpc.AlgorithmType.PC);
//        patternSearch.setMaxAdjacencies(100);
//        patternSearch.setMaxIterations(100);
//        patternSearch.setMaxDescendantPath(30);
//        patternSearch.setPcDepth(-1);
////
        Graph _graph = patternSearch.search();
        System.out.println("Pattern search = " + _graph);

        this._latents = _latents;
        this.structureGraph = new EdgeListGraph(_graph);
        GraphUtils.fruchtermanReingoldLayout(this.structureGraph);

        this.covMatrix = covMatrix;

        return this.structureGraph;
    }

    private List<Node> defineLatents(List<List<Node>> clustering, List<String> names) {
        if (names != null) {
            List<Node> latents = new ArrayList<Node>();

            for (String name : names) {
                Node node = new ContinuousVariable(name);
                node.setNodeType(NodeType.LATENT);
                latents.add(node);
            }

            return latents;
        } else {
            List<Node> latents;

            if (this.latents == null) {
                latents = new ArrayList<Node>();

                for (int i = 0; i < clustering.size(); i++) {
                    Node node = new ContinuousVariable("_L" + (i + 1));
                    node.setNodeType(NodeType.LATENT);
                    latents.add(node);
                }
            } else {
                latents = this.latents;
            }

            return latents;
        }
    }


    private void printClusterSizes(List<List<Node>> clustering) {
        System.out.print("\nCluster sizes");

        for (List<Node> cluster : clustering) {
            System.out.print("\t" + cluster.size());
        }

        System.out.println();
    }

    private void printClustering(List<List<Node>> clustering) {
        for (int i = 0; i < clustering.size(); i++) {
            System.out.println("Cluster " + i + ": " + clustering.get(i));
        }
    }

    private void printSubgraph(Graph mim, List<Node> nodes) {
        if (mim != null) {
            System.out.println(mim.subgraph(nodes));
        }


//        List<Node> _nodes = new ArrayList<Node>();
//
//        for (Node node2 : nodes) {
//            _nodes.add(mim.getNode(node2.getName()));
//        }
//
////                        Graph subgraph = mim.subgraph(_nodes);
//        List<Edge> edges = mim.getEdges();
//
//        for (int t = 0; t < edges.size(); t++) {
//            Edge edge = edges.get(t);
//
//            if (_nodes.contains(edge.getNode1()) || _nodes.contains(edge.getNode2())) {
//                System.out.println((t + 1) + ". " + edges.get(t));
//            }
//        }
    }

    private List<Node> removeSmallClusters(List<Node> latents, List<List<Node>> clustering, int minimumSize) {
        List<Node> _latents = new ArrayList<Node>(latents);

        for (int i = _latents.size() - 1; i >= 0; i--) {
            Collections.shuffle(clustering.get(i));

            if (clustering.get(i).size() < minimumSize) {
                clustering.remove(clustering.get(i));
                Node latent = _latents.get(i);
                _latents.remove(latent);
                System.out.println("Removing " + latent);
            }

        }
        return _latents;
    }

    private Node[][] getIndicators1(List<List<Node>> clustering, DataSet data) {
        Node[][] indicators = new Node[clustering.size()][];

        for (int i = 0; i < clustering.size(); i++) {
            List<Node> _indicators = clustering.get(i);
            indicators[i] = _indicators.toArray(new Node[0]);
        }

        return indicators;
    }

    private Node[][] getIndicators2(List<List<Node>> clustering, DataSet data) {
        Node[][] indicators = new Node[clustering.size()][];

        for (int i = 0; i < clustering.size(); i++) {
            List<Node> _indicators = clustering.get(i);
            indicators[i] = _indicators.toArray(new Node[0]);
        }

        return indicators;
    }

//    private Node[][] getIndicators3(List<List<Node>> clustering, DataSet data) {
//        Node[][] indicators = new Node[clustering.size()][];
//
//        for (int i = 0; i < clustering.size(); i++) {
//            List<Node> _indicators = clustering.get(i);
//            Node[] indicatorsPlus = new Node[_indicators.size() * _indicators.size()];
//
//            for (int k = 0; k < _indicators.size(); k++) {
//                for (int k2 = 0; k2 < _indicators.size(); k2++) {
////                    String name = _indicators.get(k).getName();
////                    ContinuousVariable ar = new ContinuousVariable(name + "." + k2);
//                    indicatorsPlus[k2 * _indicators.size() + k] = _indicators.get(k);
//                }
//            }
//
//            indicators[i] = indicatorsPlus;
//        }
//
//        return indicators;
//    }

//    private double[][] getLoadings1(List<List<Node>> clustering, DataSet data) {
//        double[][] loadings = new double[clustering.size()][];
//
//        for (int i = 0; i < clustering.size(); i++) {
//            List<Node> _indicators = clustering.get(i);
//
//            DataSet data1 = dataSubset(data, _indicators);
//
//            FactorAnalysis analysis = new FactorAnalysis(data1);
//            DoubleMatrix2D unrotatedSolution = analysis.successiveResidual();
//
//            loadings[i] = unrotatedSolution.viewColumn(0).toArray();
//        }
//
//        return loadings;
//    }

    private double[][] getLoadings2(List<List<Node>> clustering, DataSet data) {
        double[][] loadings = new double[clustering.size()][];


        for (int i = 0; i < clustering.size(); i++) {
            List<Node> cluster = clustering.get(i);
            System.out.println("Estimating loadings for cluster " + i);

            if (cluster.isEmpty()) throw new IllegalArgumentException("Empty cluster.");

            List<Node> indicators = cluster;
            DataSet data1 = dataSubset(data, indicators);

            Node latent = new ContinuousVariable("L");
            latent.setNodeType(NodeType.LATENT);

            Graph _graph = new EdgeListGraph();
            _graph.addNode(latent);

            for (Node indicator : indicators) {
                _graph.addNode(indicator);
                _graph.addDirectedEdge(latent, indicator);
            }

            SemPm pm = new SemPm(_graph);

            Parameter parameter = pm.getParameter(latent, indicators.get(0));
            parameter.setFixed(true);
            parameter.setStartingValue(1.0);

//            Parameter parameter2 = pm.getParameter(latent, latent);
//            parameter2.setFixed(true);
//            parameter2.setStartingValue(1.0);

            SemOptimizer optimizer = new SemOptimizerPalCds();

            SemEstimator semEstimator = new SemEstimator(data1, pm, optimizer);
            SemIm estIm = semEstimator.estimate();

            loadings[i] = new double[indicators.size()];

            System.out.println("====" + latent);

            for (int j = 0; j < indicators.size(); j++) {
                double edgeCoef = estIm.getEdgeCoef(latent, indicators.get(j));
                System.out.println(edgeCoef);
                loadings[i][j] = edgeCoef;
            }
        }

        return loadings;
    }

//    private double[][] getLoadings3(List<List<Node>> clustering, DataSet data) {
//        double[][] loadings = new double[clustering.size()][];
//
//        for (int i = 0; i < clustering.size(); i++) {
//            List<Node> indicators = clustering.get(i);
//            DataSet data1 = dataSubset(data, indicators);
//
//            Node latent = new ContinuousVariable("L");
//            latent.setNodeType(NodeType.LATENT);
//            Graph _graph = new EdgeListGraph();
//            _graph.addNode(latent);
//
//            for (Node indicator : indicators) {
//                _graph.addNode(indicator);
//                _graph.addDirectedEdge(latent, indicator);
//            }
//
//            loadings[i] = new double[indicators.size() * indicators.size()];
//
//            for (int k = 0; k < indicators.size(); k++) {
////                System.out.println("MMM " + _graph.getNodes());
//
//                SemPm pm = new SemPm(_graph);
//
//                Parameter parameter = pm.getParameter(latent, indicators.get(k));
//                parameter.setFixed(true);
//                parameter.setInitializedRandomly(false);
//                parameter.setStartingValue(1.0);
//
//                SemEstimator semEstimator = new SemEstimator(data1, pm);
//                SemIm estIm = semEstimator.estimate();
//
//                for (int j = 0; j < indicators.size(); j++) {
//                    double edgeCoef = estIm.getEdgeCoef(latent, indicators.get(j));
////                System.out.println(edgeCoef);
//                    loadings[i][k * indicators.size() + j] = edgeCoef;
//                }
//
//            }
//
//        }
//
//        return loadings;
//    }

    private DoubleMatrix2D getCov(DataSet data, DoubleMatrix2D _data, List<Node> latents, double[][] loadings, Node[][] indicators) {
        DoubleMatrix2D cov = new DenseDoubleMatrix2D(latents.size(), latents.size());

        double[] vars = new double[latents.size()];

        Map<Node, Integer> nodeMap = new HashMap<Node, Integer>();
        List<Node> variables = data.getVariables();

        for (int i = 0; i < data.getNumColumns(); i++) {
            nodeMap.put(variables.get(i), i);
        }

        for (int m = 0; m < latents.size(); m++) {
            System.out.println("Calculating variance " + m);
            vars[m] = var(_data, loadings, indicators, nodeMap, m);
            cov.set(m, m, vars[m]);
        }

        for (int m = 0; m < latents.size(); m++) {
            System.out.println("Calculating covariances for variable " + m);

            for (int n = m + 1; n < latents.size(); n++) {
                double thetamn = covar(_data, loadings, indicators, nodeMap, m, n);
                cov.set(m, n, thetamn);
                cov.set(n, m, thetamn);
            }
        }

        return cov;
    }

    private double var(DoubleMatrix2D _data, double[][] loadings, Node[][] indicators,
                       Map<Node, Integer> nodeMap, int m) {
        double numerator = 0.0;
        double denominator = 0.0;

        if (indicators[m].length == 1) {
            double[] coli = _data.viewColumn(nodeMap.get(indicators[m][0])).toArray();
            double var = StatUtils.variance(coli);
            return 1.0;
        }

        for (int i = 0; i < loadings[m].length; i++) {
            for (int j = 0; j < loadings[m].length; j++) {
                if (i == j) continue;

                double ai = loadings[m][i];
                double aj = loadings[m][j];

//                double[] coli = _data.viewColumn(data.getColumn(indicators[m][i])).toArray();
//                double[] colj = _data.viewColumn(data.getColumn(indicators[m][j])).toArray();

                double[] coli = _data.viewColumn(nodeMap.get(indicators[m][i])).toArray();
                double[] colj = _data.viewColumn(nodeMap.get(indicators[m][j])).toArray();

                double covij = StatUtils.covariance(coli, colj);

                numerator += ai * aj * covij;
                denominator += ai * ai * aj * aj;
            }
        }

        return numerator / denominator;
    }

//    private double var2(DoubleMatrix2D _data, double[][] loadings, Node[][] indicators,
//                        Map<Node, Integer> nodeMap, int m) {
//        double numerator = 0.0;
//        double denominator = 0.0;
//
//        for (int count = 0; count < getNumCovSamples(); count++) {
//            int i = RandomUtil.getInstance().nextInt(loadings[m].length);
//            int j = RandomUtil.getInstance().nextInt(loadings[m].length);
//
//            if (i == j) {
//                count--;
//                continue;
//            }
//
//            double ai = loadings[m][i];
//            double aj = loadings[m][j];
//
////            double[] coli = _data.viewColumn(data.getColumn(indicators[m][i])).toArray();
////            double[] colj = _data.viewColumn(data.getColumn(indicators[m][j])).toArray();
//
////            double[] coli = _data.viewColumn(nodeMap.get(indicators[m][i])).toArray();
////            double[] colj = _data.viewColumn(nodeMap.get(indicators[m][j])).toArray();
////
////            double covij = StatUtils.covariance(coli, colj);
//
//            DoubleMatrix1D coli = _data.viewColumn(nodeMap.get(indicators[m][i]));
//            DoubleMatrix1D colj = _data.viewColumn(nodeMap.get(indicators[m][j]));
//
//            double covij = covar(coli, colj);
//
//            numerator += ai * aj * covij;
//            denominator += ai * ai * aj * aj;
//        }
//
//        return numerator / denominator;
//    }

    private double covar(DoubleMatrix2D _data, double[][] loadings, Node[][] indicators,
                         Map<Node, Integer> nodeMap, int m, int n) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < loadings[m].length; i++) {
            for (int j = 0; j < loadings[n].length; j++) {
                double ai = loadings[m][i];
                double aj = loadings[n][j];

//                double[] coli = _data.viewColumn(data.getColumn(indicators[m][i])).toArray();
//                double[] colj = _data.viewColumn(data.getColumn(indicators[n][j])).toArray();

                double[] coli = _data.viewColumn(nodeMap.get(indicators[m][i])).toArray();
                double[] colj = _data.viewColumn(nodeMap.get(indicators[n][j])).toArray();

                double covij = StatUtils.covariance(coli, colj);

                numerator += ai * aj * covij;
                denominator += ai * ai * aj * aj;
            }
        }

        return numerator / denominator;
    }

//    private double covar2(DoubleMatrix2D _data, double[][] loadings, Node[][] indicators,
//                          Map<Node, Integer> nodeMap, int m, int n) {
//        double numerator = 0.0;
//        double denominator = 0.0;
//
//        for (int count = 0; count < getNumCovSamples(); count++) {
//            int i = RandomUtil.getInstance().nextInt(loadings[m].length);
//            int j = RandomUtil.getInstance().nextInt(loadings[n].length);
//
//            double ai = loadings[m][i];
//            double aj = loadings[n][j];
//
////            double[] coli = _data.viewColumn(data.getColumn(indicators[m][i])).toArray();
////            double[] colj = _data.viewColumn(data.getColumn(indicators[n][j])).toArray();
//
////            double[] coli = _data.viewColumn(nodeMap.get(indicators[m][i])).toArray();
////            double[] colj = _data.viewColumn(nodeMap.get(indicators[n][j])).toArray();
////
////            double covij = StatUtils.covariance(coli, colj);
//
//            DoubleMatrix1D coli = _data.viewColumn(nodeMap.get(indicators[m][i]));
//            DoubleMatrix1D colj = _data.viewColumn(nodeMap.get(indicators[n][j]));
//
//            double covij = covar(coli, colj);
//
//            numerator += ai * aj * covij;
//            denominator += ai * ai * aj * aj;
//        }
//
//
//        return numerator / denominator;
//    }

    // Assumes the data is zero-centered.

//    private double covar(DoubleMatrix1D col1, DoubleMatrix1D col2) {
//        double sum = 0.0;
//
//        for (int i = 0; i < col1.size(); i++) {
//            sum += col1.get(i) * col2.get(i);
//        }
//
//        return sum / col1.size();
//    }

    private DataSet dataSubset(DataSet data, List<Node> childrenL1) {
        DataSet data1 = data.subsetColumns(childrenL1);
        return data1;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public void setMinClusterSize(int minClusterSize) {
        if (minClusterSize < 2) {
            throw new IllegalArgumentException("Must have at least 2 indicators in every cluster, or else the " +
                    "program will go into convulsions: " + minClusterSize);
        }

        this.minClusterSize = minClusterSize;
    }

//    public int getNumCovSamples() {
//        return numCovSamples;
//    }

    public void setNumCovSamples(int numCovSamples) {
        this.numCovSamples = numCovSamples;
    }

    public void setLatentsBeforeSearch(List<Node> latents) {
        this.latents = latents;
    }

    public List<Node> latentsAfterSearch() {
        return _latents;
    }

    /**
     * Returns the full discovered graph, with latents and indicators.
     *
     * @param newNodes True iff new nodes should be picked for the latents.  This is useful if you want to lay out the
     *                 structure graph differently from the full graph.
     */
    public Graph getFullGraph(boolean newNodes) {
        Graph graph;

        if (newNodes) {
            graph = GraphUtils.newNodes(structureGraph);
        } else {
            graph = structureGraph;
        }

        for (int i = 0; i < _latents.size(); i++) {
            Node latent = _latents.get(i);
            List<Node> measuredGuys = getClustering().get(i);

            for (Node measured : measuredGuys) {
                if (!graph.containsNode(measured)) {
                    graph.addNode(measured);
                }

                graph.addDirectedEdge(latent, measured);
            }
        }

        return graph;
    }

    public List<List<Node>> getClustering() {
        return clustering;
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

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public ICovarianceMatrix getCovMatrix() {
        return this.covMatrix;
    }
}

