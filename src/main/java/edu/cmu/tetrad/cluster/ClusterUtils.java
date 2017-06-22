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

package edu.cmu.tetrad.cluster;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.MimBuild;
import edu.cmu.tetrad.util.RandomUtil;

import java.io.*;
import java.util.*;

/**
 * Some general utilities for dealing with clustering input and output.
 *
 * @author Joseph Ramsey
 */
public class ClusterUtils {
    public static DoubleMatrix2D restrictToRows(DoubleMatrix2D data, List<Integer> rows) {
        int[] _rows = asArray(rows);
        int[] _cols = new int[data.columns()];
        for (int j = 0; j < data.columns(); j++) _cols[j] = j;
        return data.viewSelection(_rows, _cols);
    }
                                   
    private static int[] asArray(List<Integer> indices) {
        int[] _indices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) _indices[i] = indices.get(i);
        return _indices;
    }

    public static List<List<Integer>> convertClusterIndicesToLists(List<Integer> clusterIndices) {
        int max = 0;

        for (int i = 0; i < clusterIndices.size(); i++) {
            if (clusterIndices.get(i) > max) max = clusterIndices.get(i);
        }

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();

        for (int i = 0; i <= max; i++) {
            clusters.add(new LinkedList<Integer>());
        }

        for (int i = 0; i < clusterIndices.size(); i++) {
            Integer index = clusterIndices.get(i);

            if (index == -1) continue;

            clusters.get(index).add(i);
        }

        return clusters;
    }

    public static void printPcaTranslations(DoubleMatrix2D selection, int k) {
        System.out.println("\nPCA:");

        DoubleMatrix2D covariance = Statistic.covariance(selection);
        EigenvalueDecomposition decomposition = null;
        try {
            decomposition = new EigenvalueDecomposition(covariance);
        } catch (Exception e) {
            return;
        }
        DoubleMatrix1D eigenvalues = decomposition.getRealEigenvalues();

        System.out.println("Eigenvalues: " + eigenvalues);

        DoubleMatrix2D featureVector = decomposition.getV();
        selection = new Algebra().mult(featureVector.viewDice(), selection.viewDice().copy()).viewDice();

        DoubleArrayList cluster0 = new DoubleArrayList(selection.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(selection.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(selection.viewColumn(2).toArray());

        double min0 = Descriptive.min(cluster0);
        double min1 = Descriptive.min(cluster1);
        double min2 = Descriptive.min(cluster2);

        double max0 = Descriptive.max(cluster0);
        double max1 = Descriptive.max(cluster1);
        double max2 = Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("Cluster " + k + ":");
        System.out.println("Dimension 0 = " + min0 + " to " + max0 + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Dimension 1 = " + min1 + " to " + max1 + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Dimension 2 = " + min2 + " to " + max2 + " mean = " + mean2 + " SD = " + sd2);
    }

    public static PrintWriter writeOutPrototypesVertically(DoubleMatrix2D prototypes,
                                                          String path
                                                          ) throws FileNotFoundException {
        System.out.println("Writing prototypes to file " + path);
        File file = new File(path);
        new File(file.getParent()).mkdirs();
        PrintWriter out = new PrintWriter(file);

        prototypes = prototypes.viewDice();

        for (int i = 0; i < prototypes.rows(); i++) {
            for (int j = 0; j < prototypes.columns(); j++) {
                out.print(prototypes.get(i, j));

                if (j < prototypes.columns() - 1) {
                    out.print("\t");
                }
            }

            out.println();
        }

//        out.println(prototypes.viewDice());
        out.close();
        return out;
    }

    static double standardDeviation(DoubleArrayList array) {
        double sumX = Descriptive.sum(array);
        double sumSqX = Descriptive.sumOfSquares(array);
        double variance = Descriptive.variance(array.size(), sumX, sumSqX);
        return Descriptive.standardDeviation(variance);
    }

    /**
     * Converting the data to standardized form and clustering using
     * squared error loss is equivalent to clustering in correlation space.
     * Note that the standardization is ROWWISE.
     */
    public static DoubleMatrix2D convertToStandardized(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int i = 0; i < data.rows(); i++) {
            double sum = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.columns();

            double norm = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                double v = data.get(i, j) - mean;
                norm += v * v;
            }

            norm = Math.sqrt(norm);

            for (int j = 0; j < data.columns(); j++) {
                data2.set(i, j, (data.get(i, j) - mean) / norm);
            }
        }

        return data2;
    }

    public static DoubleMatrix2D convertToSeriesZScores(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int i = 0; i < data.rows(); i++) {
            DoubleMatrix1D row = data.viewRow(i);
            DoubleArrayList _row = new DoubleArrayList(row.toArray());

            double mean = Descriptive.mean(_row);
            double sd = ClusterUtils.standardDeviation(_row);

            for (int j = 0; j < data.columns(); j++) {
                data2.set(i, j, (data.get(i, j) - mean) / sd);
            }
        }

        return data2;
    }

    public static void initRandomly(DoubleMatrix2D x) {
        for (int i = 0; i < x.rows(); i++) {
            for (int j = 0; j < x.columns(); j++) {
                x.set(i, j, RandomUtil.getInstance().nextDouble());
            }
        }
    }

    public static List<Integer> getTopFractionScoreRows(DoubleMatrix1D scores, 
                                                 double topFraction,
                                                 DoubleMatrix2D timeSeries) {
        List<Integer> _points = new ArrayList<Integer>();
        final Map<Integer, Double> _values = new HashMap<Integer, Double>();

        for (int i = 0; i < timeSeries.rows(); i++) {
            _points.add(i);
            _values.put(i, scores.get(i));
        }

        Collections.sort(_points, new Comparator<Integer>() {
            @Override
			public int compare(Integer o1, Integer o2) {
                double v1 = _values.get(o1);
                double v2 = _values.get(o2);
                return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
            }
        });

        List<Integer> points = new ArrayList<Integer>();

        for (int i = (int) ((1.0 - topFraction) * _points.size()); i < _points.size();
             i++) {
            points.add(_points.get(i));
        }
        return points;
    }

    public static List<Integer> getAboveThresholdRows(DoubleMatrix1D scores,
                                                      double cutoff,
                                                      DoubleMatrix2D timeSeries) {
        List<Integer> includedRows = new ArrayList<Integer>();

        for (int i = 0; i < timeSeries.rows(); i++) {
            double score = scores.get(i);

            if (score > cutoff) {
                includedRows.add(i);
            }
        }

        return includedRows;
    }

    public static List<Integer> getSignificantlyChangingRows(DoubleMatrix2D data,
                                                             int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        List<Integer> includedRows = new ArrayList<Integer>();

        for (int i = 0; i < data.rows(); i++) {
            if (Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold) {
                includedRows.add(i);
            }
        }

        return includedRows;
    }

    public static boolean isSignificantlyChangingUp(DoubleMatrix2D data, int i,
                                                 int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        return Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold
                && data.get(i, tIndex) > data.get(i, tIndex - 1);
    }

    public static boolean isSignificantlyChangingDown(DoubleMatrix2D data, int i,
                                                   int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        return Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold
                && data.get(i, tIndex) < data.get(i, tIndex - 1);
    }

    /**
     * Returns the top fraction threshold for the entire data set--that is,
     * if all of the values in the dataset were sorted bottom to top, the
     * value the tresholds the top fraction is given.
     * @param data A 2D real data set.
     * @param fraction A number between 0 and 1, inclusive.
     * @return The top frction threshold.
     */
    public static double getTopFactionThresholdOverall(DoubleMatrix2D data,
                                                              double fraction) {
        int numEntries = data.rows() * data.columns();
        int numTopFraction = (int) (numEntries * fraction);
        TreeSet<Double> set = new TreeSet<Double>();

        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double datum = data.get(i, j);

                if (set.size() < numTopFraction) {
                    set.add(datum);
                }
                else {
                    if (datum > set.first()) {
                        set.remove(set.first());
                        set.add(datum);
                    }
                }
            }
        }

        return set.first();
    }

    /**
     * Returns a list of view of the data corresponding to the given clusters.
     */
    public static List<DoubleMatrix2D> getClusterViews(DoubleMatrix2D xyzData,
                                                       List<List<Integer>> clusters) {
        List<DoubleMatrix2D> views = new ArrayList<DoubleMatrix2D>();

        int[] cols = new int[xyzData.columns()];
        for (int j = 0; j < xyzData.columns(); j++) cols[j] = j;

        for (int k = 0; k < clusters.size(); k++) {
            int clusterSize = clusters.get(k).size();
            int[] rows = new int[clusterSize];

            for (int i = 0; i < clusterSize; i++) {
                rows[i] = clusters.get(k).get(i);
            }

            DoubleMatrix2D clusterView = xyzData.viewSelection(rows, cols);
            views.add(clusterView);
        }

        return views;
    }


    public static DoubleMatrix2D restrictToTimeSeries(DoubleMatrix2D data) {
        return data.viewPart(0, 3, data.rows(), data.columns() - 3);
    }

    public static DoubleMatrix2D restrictToXyz(DoubleMatrix2D data) {
        return data.viewPart(0, 0, data.rows(), 3);
    }


    /**
     * Prints the XYZ extents of the given XYZ dataset--that is, the minimum and
     * maximum of each dimension. It is assumed that the dataset has three
     * columns.
     */
    public static void printXyzExtents(DoubleMatrix2D xyzData) {
        if (!(xyzData.columns() == 3)) {
            throw new IllegalArgumentException();
        }

        if (xyzData.rows() == 0) {
            return;
        }

        DoubleArrayList cluster0 = new DoubleArrayList(xyzData.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(xyzData.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(xyzData.viewColumn(2).toArray());

        double min0 = Descriptive.min(cluster0);
        double min1 = Descriptive.min(cluster1);
        double min2 = Descriptive.min(cluster2);

        double max0 = Descriptive.max(cluster0);
        double max1 = Descriptive.max(cluster1);
        double max2 = Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("X = " + min0 + " to " + max0 + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Y = " + min1 + " to " + max1 + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Z = " + min2 + " to " + max2 + " mean = " + mean2 + " SD = " + sd2);
    }

    /**
     * Prints XYZ extents for each of the given clusters, where the clusters
     * point to rows in the given data set <code>xyzData</code>
     */
    public static void printXyzExtents(DoubleMatrix2D xyzData,
                                       List<List<Integer>> clusters) {
        List<DoubleMatrix2D> views = getClusterViews(xyzData, clusters);

        for (int i = 0; i < views.size(); i++) {
            System.out.println("Cluster " + i);
            printXyzExtents(views.get(i));
        }
    }

    public static DoubleMatrix2D loadMatrix(String path, int n, int m,
                                            boolean ignoreFirstRow,
                                            boolean ignoreFirstCol) throws IOException {
        System.out.println("Loading data from " + path);

        File file = new File(path);
        BufferedReader in = new BufferedReader(new FileReader(file));

        // Skip first line.
        if (ignoreFirstRow) {
            in.readLine();
        }

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, m);

        for (int i = 0; i < n; i++) {
            if ((i + 1) % 1000 == 0) System.out.println("Loading " + (i + 1));

            String line = in.readLine();
            RegexTokenizer tokenizer = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

            if (ignoreFirstCol) {
                tokenizer.nextToken();
            }

            for (int j = 0; j < m; j++) {
                double datum = Double.parseDouble(tokenizer.nextToken());
                data.set(i, j, datum);
            }
        }
        return data;
    }

    public static void writerClustersToGnuPlotFile(DoubleMatrix2D xyzData,
                                                   List<List<Integer>> clusters,
                                                   String path) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(path));

        for (int j = 0; j < clusters.size(); j++) {
            List<Integer> cluster = clusters.get(j);

            if (cluster.isEmpty()) {
                continue;
            }

            for (int i : cluster) {
                double x = xyzData.get(i, 0);
                double y = xyzData.get(i, 1);
                double z = xyzData.get(i, 2);
                out.println((z) + " " + (x) + " " + (y));
            }

            out.println();
            out.println();
        }

        out.close();
    }

    public static void writeClusterToGnuPlotFile(DoubleMatrix2D xyzData,
                                                   List<List<Integer>> clusters,
                                                   List<List<Integer>> colors,
                                                   String path
    ) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(path));

        for (int j = 0; j < clusters.size(); j++) {
            List<Integer> cluster = clusters.get(j);
            List<Integer> clusterColors = colors.get(j);

            if (cluster.isEmpty()) {
                continue;
            }

            for (int _i = 0; _i < cluster.size(); _i++) {
                int i = cluster.get(_i);

                double x = xyzData.get(i, 0);
                double y = xyzData.get(i, 1);
                double z = xyzData.get(i, 2);

                int color = clusterColors.get(_i);

                out.println(z + " " + x + " " + y + " " + color);
            }

            out.println();
            out.println();
        }

        out.close();
    }

    public static DoubleMatrix2D convertToMeanCentered(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = new DenseDoubleMatrix2D(data.rows(), data.columns());

        for (int i = 0; i < data.rows(); i++) {
            double sum = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.columns();

            for (int j = 0; j < data.columns(); j++) {
                double v = data.get(i, j) - mean;
                data2.set(i, j, v);
            }
        }

        return data2;
    }

    public static List<int[]> convertListToInt(List<List<Node>> partition, List<Node> nodes) {
        List<int[]> _partition = new ArrayList<int[]>();

        for (int i = 0; i < partition.size(); i++) {
            List<Node> cluster = partition.get(i);
            int[] _cluster = new int[cluster.size()];

            for (int j = 0; j < cluster.size(); j++) {
                for (int k = 0; k < nodes.size(); k++) {
                    if (nodes.get(k).getName().equals(cluster.get(j).getName())) {
                        _cluster[j] = k;
                    }
                }
            }

            _partition.add(_cluster);
        }

        return _partition;
    }

    public static List<List<Node>> convertIntToList(List<int[]> partition, List<Node> nodes) {
        List<List<Node>> _partition = new ArrayList<List<Node>>();

        for (int i = 0; i < partition.size(); i++) {
            int[] cluster = partition.get(i);
            List<Node> _cluster = new ArrayList<Node>();

            for (int j = 0; j < cluster.length; j++) {
                _cluster.add(nodes.get(cluster[j]));
            }

            _partition.add(_cluster);
        }

        return _partition;
    }

    public static List<List<Node>> clustersToPartition(Clusters clusters, List<Node> variables) {
        List<List<Node>> inputPartition = new ArrayList<List<Node>>();

        for (int i = 0; i < clusters.getNumClusters(); i++) {
            List<Node> cluster = new ArrayList<Node>();

            for (String nodeName : clusters.getCluster(i)) {
                for (int j = 0; j < variables.size(); j++) {
                    if (variables.get(j).getName().equals(nodeName)) {
                        cluster.add(variables.get(j));
                    }
                }
            }

            inputPartition.add(cluster);
        }

        return inputPartition;
    }

    public static Clusters partitionToClusters(List<List<Node>> partition) {
        Clusters clusters = new Clusters();

        for (int i = 0; i < partition.size(); i++) {
            List<Node> cluster = partition.get(i);

            for (int j = 0; j < cluster.size(); j++) {
                clusters.addToCluster(i, cluster.get(j).getName());
            }
        }

        return clusters;
    }

    public static Graph convertSearchGraph(List<int[]> clusters, String[] varNames) {
        List<Node> nodes = new ArrayList<Node>();

        if (clusters == null) {
            nodes.add(new GraphNode("No_model."));
            return new EdgeListGraph(nodes);
        }
//        boolean someEmpty = false;
//
//        for (int[] cluster : clusters) {
//            if (cluster.length == 0) someEmpty = true;
//        }
//
//        if (someEmpty) {
//            nodes.add(new GraphNode("No_model."));
//            return new EdgeListGraph(nodes);
//        }

        Set<Node> latentsSet = new HashSet<Node>();
        for (int i = 0; i < clusters.size(); i++) {
            Node latent = new GraphNode(MimBuild.LATENT_PREFIX + (i + 1));
            latent.setNodeType(NodeType.LATENT);
            nodes.add(latent);
            latentsSet.add(latent);
        }
        for (int i = 0; i < clusters.size(); i++) {
            int indicators[] = clusters.get(i);
            for (int j = 0; j < indicators.length; j++) {
                String indicatorName;
                indicatorName = varNames[indicators[j]];
                Node indicator = new GraphNode(indicatorName);
                nodes.add(indicator);
            }
        }
        Graph graph = new EdgeListGraph(nodes);
        int acc = clusters.size();
        for (int i = 0; i < clusters.size(); i++) {
            int indicators[] = clusters.get(i);
            for (int j = 0; j < indicators.length; j++) {
                graph.setEndpoint(nodes.get(i), nodes.get(acc),
                        Endpoint.ARROW);
                graph.setEndpoint(nodes.get(acc), nodes.get(i),
                        Endpoint.TAIL);
                acc++;
            }
            for (int j = i + 1; j < clusters.size(); j++) {
                graph.setEndpoint(nodes.get(i), nodes.get(j),
                        Endpoint.ARROW); //TODO: put CIRCLES back
                graph.setEndpoint(nodes.get(j), nodes.get(i),
                        Endpoint.TAIL);
            }
        }

//        if (graph != null && correlationMatrix != null) {
//            MimBuildEstimator estimator = new MimBuildEstimator(
//                    correlationMatrix, new SemPm(graph), 10, 5);
//            estimator.estimate();
//            System.out.println("chisq = " +
//                    estimator.getEstimatedSem().getChiSquare());
//            System.out.println(
//                    "pvalue = " + estimator.getEstimatedSem().getPValue());
//        }

        return graph;
    }

    public static Set<Node> getAllNodesInClusters(List<List<Node>> clustering) {
        Set<Node> allNodes = new HashSet<Node>();

        for (List<Node> cluster : clustering) {
            allNodes.addAll(cluster);
        }
        return allNodes;
    }

    public static List<List<Node>> initializeZeroClusters(int numClusters) {
        List<List<Node>> clustering = new ArrayList<List<Node>>();

        for (int i = 0; i < numClusters; i++) {
            clustering.add(new ArrayList<Node>());
        }
        return clustering;
    }

    public static void addNodesToSubclusters(List<List<Node>> clustering, List<List<Node>> subclustering, int maxSize) {
        for (int i = 0; i < clustering.size(); i++) {
            List<Node> cluster = clustering.get(i);
            List<Node> subcluster = subclustering.get(i);
            Collections.shuffle(cluster);

            for (Node node : cluster) {
                if (subcluster.size() >= maxSize) break;
                if (subcluster.contains(node)) continue;
                subcluster.add(node);
            }
        }
    }

    public static List<List<Node>> mimClustering(List<Node> latents, Graph mim, DataModel data) {
        List<List<Node>> clustering = new ArrayList<List<Node>>();

        for (Node node : latents) {
            List<Node> adj = mim.getAdjacentNodes(node);
            adj.removeAll(latents);
            adj = GraphUtils.replaceNodes(adj, data.getVariables());

            clustering.add(adj);
        }
        return clustering;

    }

    public static List<List<Node>> mimClustering(Graph mim, List<Node> variables) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node);
            }
        }

        List<List<Node>> clustering = new ArrayList<List<Node>>();

        for (Node _latent : latents) {
            List<Node> adj = mim.getAdjacentNodes(_latent);
            adj.removeAll(latents);
//            adj.add(_latent);
            adj = GraphUtils.replaceNodes(adj, variables);

            clustering.add(adj);
        }
        
        return clustering;

    }

    public static Clusters mimClusters(Graph mim) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node);
            }
        }

        Clusters clusters = new Clusters();

        for (int i = 0; i < latents.size(); i++) {
            Node _latent = latents.get(i);
            List<Node> adj = mim.getAdjacentNodes(_latent);
            adj.removeAll(latents);

            clusters.setClusterName(i, _latent.getName());

            for (int j = 0; j < adj.size(); j++) {
                Node n = adj.get(j);
                clusters.addToCluster(i, n.getName());
            }
        }

        return clusters;

    }

    public static List<List<Node>> getClusterSelection(int maxClusterSelectionSize, DataSet data, List<List<Node>> clustering) {
        List<List<Node>> clusterSelection = initializeZeroClusters(clustering.size());

        addNodesToSubclusters(clustering, clusterSelection, maxClusterSelectionSize);

        for (List<Node> cluster : clusterSelection) {
            GraphUtils.replaceNodes(cluster, data.getVariables());
        }
        return clusterSelection;
    }
}

