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

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.TetradLogger;

/**
 * A clean-up of Ricardo's tetrad-based purify.
 *
 * @author Joe Ramsey
 */
public class PurifyTetradBasedH implements IPurify {
    boolean listTetrads = false;
    private TetradTest tetradTest;
    private int maxClusterSize = 5;
    private List <Node> nodes;
    private HashMap <Node, Integer> nodeMap;
    private Graph mim;
    private Tetrad foundTetrad;
    private List <Double> pvalueList = new ArrayList <Double>();

    public PurifyTetradBasedH(TetradTest tetradTest, int maxClusterSize) {
        this.tetradTest = tetradTest;
        this.nodes = tetradTest.getVariables();

        this.nodeMap = new HashMap <Node, Integer>();

        for (int index = 0; index < nodes.size(); index++) {
            nodeMap.put(nodes.get(index), index);
        }

        if (maxClusterSize < 1)
            throw new IllegalArgumentException("Max cluster size must be at least 1: " + maxClusterSize);
        this.maxClusterSize = maxClusterSize;
    }

    @Override
    public List <List <Node>> purify(List <List <Node>> clustering) {

        // The inputs nodes may not be object identical to the ones from the tetrad test, so we map them over then
        // back by their names.
        List <Node> originalNodes = new ArrayList <Node>();

        for (List <Node> cluster : clustering) {
            originalNodes.addAll(cluster);
        }

        List <List <Node>> _clustering = new ArrayList <List <Node>>();

        for (List <Node> cluster : clustering) {
            List <Node> converted = GraphUtils.replaceNodes(cluster, nodes);
            _clustering.add(converted);
        }

        List <List <Node>> result = combinedSearch(_clustering, getMaxClusterSize());
        List <List <Node>> convertedResult = new ArrayList <List <Node>>();

        for (List <Node> cluster : result) {
            List <Node> converted = GraphUtils.replaceNodes(cluster, originalNodes);
            convertedResult.add(converted);
        }

        double fdrCutoff = StatUtils.fdr(0.0001, pvalueList, false);
        System.out.println("fdrCutoff = " + fdrCutoff);

        return convertedResult;
    }


    private List <List <Node>> combinedSearch(List <List <Node>> clustering, int maxSize) {
        double cutoff = tetradTest.getSignificance();

        List <List <Node>> _clustering = initializeZeroClusters(clustering.size());
        Set <Node> allNodes = getAllNodesInClusters(clustering);
        addPureNodes(clustering, _clustering, allNodes, cutoff, maxSize);

        return _clustering;
    }

    private void addNodesToSubclusters(List <List <Node>> clustering, List <List <Node>> subclustering, int maxSize) {
        for (int i = 0; i < clustering.size(); i++) {
            List <Node> cluster = clustering.get(i);
            List <Node> subcluster = subclustering.get(i);
            Collections.shuffle(cluster);

            for (Node node : cluster) {
                if (subcluster.size() >= maxSize) break;
                if (subcluster.contains(node)) continue;
                subcluster.add(node);
            }
        }
    }

    private List <List <Node>> initializeZeroClusters(int numClusters) {
        List <List <Node>> clustering = new ArrayList <List <Node>>();

        for (int i = 0; i < numClusters; i++) {
            clustering.add(new ArrayList <Node>());
        }
        return clustering;
    }

    private Set <Node> getAllNodesInClusters(List <List <Node>> clustering) {
        Set <Node> allNodes = new HashSet <Node>();

        for (List <Node> cluster : clustering) {
            allNodes.addAll(cluster);
        }
        return allNodes;
    }

    private void addPureNodes(List <List <Node>> clustering, List <List <Node>> _clustering, Set <Node> allNodes, double cutoff, int maxSize) {
        List <Node> _allNodes = new ArrayList <Node>(allNodes);
        Collections.shuffle(_allNodes);

        List <Node> nodesToTry = new ArrayList <Node>(_allNodes);
        int index = 0;

        while (!nodesToTry.isEmpty()) {

            NODE:
            for (Node node : new ArrayList <Node>(nodesToTry)) {

                for (int i = 0; i < _clustering.size(); i++) {
                    List <Node> cluster = _clustering.get(i);
                    List <Node> originalCluster = clustering.get(i);

                    if (!originalCluster.contains(node)) {
                        continue;
                    }

                    if (cluster.contains(node)) {
                        nodesToTry.remove(node);
                        continue NODE;
                    }

                    if (cluster.size() >= maxSize) {
                        nodesToTry.removeAll(clustering.get(i));
                        continue NODE;
                    }

                    cluster.add(node);
                    boolean exists;

                    exists = existsImpurity(node, _clustering, cutoff);

                    if (exists) {
                        for (List <Node> __cluster : _clustering) {
                            __cluster.remove(node);
                        }

                        if (mim != null) {
                            printSubgraph(node);
                        }
                    } else {
                        ++index;
                        System.out.println("Added " + node + " index = " + (index));
                        TetradLogger.getInstance().log("details", "Added " + node + " index = " + (index));
                    }

                    nodesToTry.remove(node);
                    continue NODE;
                }
            }
        }
    }

    private void printSubgraph(Node node) {
        System.out.println("Subgraph for node " + node + ", tetrad " + getFoundTetrad() + ":");
        TetradLogger.getInstance().log("details", "Subgraph for node " + node + ", tetrad " + getFoundTetrad() + ":");

        List <Node> _nodes = new ArrayList <Node>();

        for (Node node2 : getFoundTetrad().getNodes()) {
            _nodes.add(mim.getNode(node2.getName()));
        }

//                        Graph subgraph = mim.subgraph(_nodes);
        List <Edge> edges = mim.getEdges();

        for (int t = 0; t < edges.size(); t++) {
            Edge edge = edges.get(t);

            if (_nodes.contains(edge.getNode1()) || _nodes.contains(edge.getNode2())) {
                System.out.println((t + 1) + ". " + edges.get(t));
                TetradLogger.getInstance().log("details", (t + 1) + ". " + edges.get(t));
            }
        }
    }

    private boolean existsImpurity(Node node, List <List <Node>> clustering, double cutoff) {
        for (List <Node> cluster : clustering) {
//            if (!cluster.contains(node)) {
//                continue;
//            }

            boolean exists = existsWithinClusterImpurity(node, cluster, cutoff);
            if (exists) return true;
        }

        return existsCrossConstructImpurity(node, clustering, cutoff);
    }

    private boolean existsWithinClusterImpurity(Node node, List <Node> cluster, double cutoff) {
        if (cluster.size() < 4) return false;
        ChoiceGenerator gen = new ChoiceGenerator(cluster.size(), 4);
        int[] choice;

        while ((choice = gen.next()) != null) {
            List <Node> _cluster = GraphUtils.asList(choice, cluster);
            boolean exists = existsThreeTetradImpurity(node, _cluster, cutoff);
            if (exists) return true;
        }

        return false;
    }

    private boolean existsCrossConstructImpurity(Node node, List <List <Node>> clustering, double cutoff) {
        for (int p1 = 0; p1 < clustering.size(); p1++) {
            for (int p2 = p1 + 1; p2 < clustering.size(); p2++) {
                List <Node> cluster1 = clustering.get(p1);
                List <Node> cluster2 = clustering.get(p2);

//                if (!(cluster1.contains(node) || cluster2.contains(node))) {
//                    continue;
//                }

                if (cluster1.size() >= 3 && cluster2.size() >= 1) {
                    ChoiceGenerator gen1 = new ChoiceGenerator(cluster1.size(), 3);
                    int[] choice1;

                    while ((choice1 = gen1.next()) != null) {
                        ChoiceGenerator gen2 = new ChoiceGenerator(cluster2.size(), 1);
                        int[] choice2;

                        while ((choice2 = gen2.next()) != null) {
                            List <Node> crossCluster = new ArrayList <Node>();
                            for (int i : choice1) crossCluster.add(cluster1.get(i));
                            for (int i : choice2) crossCluster.add(cluster2.get(i));

//                            if (!crossCluster.contains(node)) {
//                                continue;
//                            }

                            boolean exists = existsThreeTetradImpurity(node, crossCluster, cutoff);
                            if (exists) return true;
                        }
                    }
                }

                if (cluster2.size() >= 3 && cluster1.size() >= 1) {
                    ChoiceGenerator gen1 = new ChoiceGenerator(cluster2.size(), 3);
                    int[] choice1;

                    while ((choice1 = gen1.next()) != null) {
                        ChoiceGenerator gen2 = new ChoiceGenerator(cluster1.size(), 1);
                        int[] choice2;

                        while ((choice2 = gen2.next()) != null) {
                            List <Node> crossCluster = new ArrayList <Node>();
                            for (int i : choice1) crossCluster.add(cluster2.get(i));
                            for (int i : choice2) crossCluster.add(cluster1.get(i));

//                            if (!crossCluster.contains(node)) {
//                                continue;
//                            }

                            boolean exists = existsThreeTetradImpurity(node, crossCluster, cutoff);
                            if (exists) return true;
                        }
                    }
                }

                if (cluster1.size() >= 2 && cluster2.size() >= 2) {
                    ChoiceGenerator gen1 = new ChoiceGenerator(cluster1.size(), 2);
                    int[] choice1;

                    while ((choice1 = gen1.next()) != null) {
                        ChoiceGenerator gen2 = new ChoiceGenerator(cluster2.size(), 2);
                        int[] choice2;

                        while ((choice2 = gen2.next()) != null) {
                            List <Node> crossCluster = new ArrayList <Node>();
                            for (int i : choice1) crossCluster.add(cluster1.get(i));
                            for (int i : choice2) crossCluster.add(cluster2.get(i));

//                            if (!crossCluster.contains(node)) {
//                                continue;
//                            }

                            boolean exists = existsTetrads2By2Impurity(node, crossCluster, cutoff);
                            if (exists) return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean existsThreeTetradImpurity(Node node, List <Node> cluster, double cutoff) {
        if (cluster.size() != 4) throw new IllegalStateException("Expected a 4-node cluster: " + cluster);

        Node ci = cluster.get(0);
        Node cj = cluster.get(1);
        Node ck = cluster.get(2);
        Node cl = cluster.get(3);

        if (ci != node && cj != node && ck != node && cl != node) {
            return false;
        }

        double p1 = pValue(ci, cl, cj, ck);

        if (p1 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, cj, ck, cl, p1);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            TetradLogger.getInstance().log("details", "Found " + tetrad);
            return true;
        }

        double p2 = pValue(ci, ck, cj, cl);

        if (p2 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, cj, cl, ck, p2);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            TetradLogger.getInstance().log("details", "Found " + tetrad);
            return true;
        }

        double p3 = pValue(ci, cj, ck, cl);

        if (p3 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, ck, cl, cj, p3);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            TetradLogger.getInstance().log("details", "Found " + tetrad);
            return true;
        }

        return false;
    }

    private boolean existsTetrads2By2Impurity(Node node, List <Node> cluster, double cutoff) {
        if (cluster.size() != 4) throw new IllegalStateException("Expected a 4-node cluster: " + cluster);

        Node ci = cluster.get(0);
        Node cj = cluster.get(1);
        Node ck = cluster.get(2);
        Node cl = cluster.get(3);

        if (ci != node && cj != node && ck != node && cl != node) {
            return false;
        }

        double p1 = pValue(ci, cj, ck, cl);

        if (p1 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, ck, cl, cj, p1);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            TetradLogger.getInstance().log("details", "Found " + tetrad);
            return true;
        }

        return false;
    }

    private double pValue(Node ci, Node cj, Node ck, Node cl) {
        double p1 = tetradTest.tetradPValue(nodeMap.get(ci), nodeMap.get(ck), nodeMap.get(cl), nodeMap.get(cj));

        pvalueList.add(p1);

        return p1;
    }

    @Override
    public void setTrueGraph(Graph mim) {
        this.mim = mim;
    }

    public Tetrad getFoundTetrad() {
        return foundTetrad;
    }

    public int getMaxClusterSize() {
        return maxClusterSize;
    }
}
