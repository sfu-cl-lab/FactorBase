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

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ChoiceGenerator;

/**
 * A clean-up of Ricardo's tetrad-based purify.
 *
 * @author Joe Ramsey
 */
public class PurifyTetradBasedG implements IPurify {
    boolean listTetrads = false;
    private TetradTest tetradTest;
    private List <Node> nodes;
    private Tetrad foundTetrad;
    private HashMap <Node, Integer> nodeMap;

    public PurifyTetradBasedG(TetradTest tetradTest) {
        this.tetradTest = tetradTest;
        this.nodes = tetradTest.getVariables();
        this.nodeMap = new HashMap <Node, Integer>();

        for (int index = 0; index < nodes.size(); index++) {
            nodeMap.put(nodes.get(index), index);
        }
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

        List <List <Node>> result = combinedSearch(_clustering);
        List <List <Node>> convertedResult = new ArrayList <List <Node>>();

        for (List <Node> cluster : result) {
            List <Node> converted = GraphUtils.replaceNodes(cluster, originalNodes);
            convertedResult.add(converted);
        }

//        System.out.println("Converted: " + convertedResult);

        return convertedResult;
    }

    @Override
    public void setTrueGraph(Graph mim) {
        throw new UnsupportedOperationException();
    }

    private List <List <Node>> combinedSearch(List <List <Node>> clustering) {
        double cutoff = tetradTest.getSignificance();

        List <List <Node>> _clustering = new ArrayList <List <Node>>();

        for (List <Node> cluster : clustering) {
            _clustering.add(new ArrayList <Node>(cluster));
        }

        Set <Node> allNodes = new HashSet <Node>();

        for (List <Node> cluster : _clustering) {
            allNodes.addAll(cluster);
        }

        List <Node> _allNodes = new ArrayList <Node>(allNodes);
        Collections.shuffle(_allNodes);

        for (Node node : _allNodes) {
            boolean exists = existsImpurity(node, _clustering, cutoff);

            if (exists) {
                for (List <Node> cluster : _clustering) {
                    cluster.remove(node);
                    System.out.println("Removing " + node);
                }
            }
        }

//        System.out.println("G");
//        System.out.println("g-mimClustering 1: " + _clustering);

//        List<Node> nodesToTry = new ArrayList<Node>(_allNodes);
//
//        while (!nodesToTry.isEmpty()) {
//
//            NODE:
//            for (Node node : new ArrayList<Node>(nodesToTry)) {
//
//                for (int i = 0; i < _clustering.size(); i++) {
//                    List<Node> cluster = _clustering.get(i);
//                    List<Node> originalCluster = mimClustering.get(i);
//
//                    if (!originalCluster.contains(node)) {
//                        continue;
//                    }
//
//                    if (cluster.contains(node)) {
//                        nodesToTry.remove(node);
//                        continue NODE;
//                    }
//
//                    if (cluster.size() >= 10) {
//                        nodesToTry.remove(node);
//                        continue NODE;
//                    }
//
//                    cluster.add(node);
//
//                    boolean exists = existsImpurity(node, _clustering, cutoff);
//
//                    if (exists) {
//                        cluster.remove(node);
//                    } else {
////                        System.out.println("Added " + node);
//                    }
//
//                    nodesToTry.remove(node);
//                    continue NODE;
//                }
//            }
//        }

        System.out.println("mimClustering: " + _clustering);

        return _clustering;
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

        double p1 = tetradTest.tetradPValue(nodeMap.get(ci), nodeMap.get(cj), nodeMap.get(ck), nodeMap.get(cl));

        if (p1 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, cj, ck, cl, p1);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            return true;
        }

        double p2 = tetradTest.tetradPValue(nodeMap.get(ci), nodeMap.get(cj), nodeMap.get(cl), nodeMap.get(ck));

        if (p2 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, cj, cl, ck, p2);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            return true;
        }

        double p3 = tetradTest.tetradPValue(nodeMap.get(ci), nodeMap.get(ck), nodeMap.get(cl), nodeMap.get(cj));

        if (p3 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, ck, cl, cj, p3);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
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

        double p1 = tetradTest.tetradPValue(nodeMap.get(ci), nodeMap.get(ck), nodeMap.get(cl), nodeMap.get(cj));

        if (p1 < cutoff) {
            Tetrad tetrad = new Tetrad(ci, ck, cl, cj, p1);
            this.foundTetrad = tetrad;
            System.out.println("Found " + tetrad);
            return true;
        }

        return false;
    }
}
