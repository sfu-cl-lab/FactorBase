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
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;

import java.util.*;


/**
 * Implements FindOneFactorCluster by Erich Kummerfeld (adaptation of a two factor
 * sextet algorithm to a one factor tetrad algorithm).
 *
 * @author Joseph Ramsey
 */
public class FindOneFactorClusters {

    // The list of all variables.
    private List<Node> variables;

    // The tetrad test--using Ricardo's. Used only for Wishart.
    private TetradTest test;

    // The significance level.
    private double alpha;

    // Wishart or Bollen.
    private TestType testType;

    // The Bollen test. Testing two tetrads simultaneously.
    private BollenTingTetradTest bollenTingTest;

    // independence test.
    private IndependenceTest indTest;

    //========================================PUBLIC METHODS====================================//

    public FindOneFactorClusters(ICovarianceMatrix cov, TestType testType, double alpha) {
        this.variables = cov.getVariables();
        this.test = new ContinuousTetradTest(cov, testType, alpha);
        this.indTest = new IndTestFisherZ(cov, alpha);
        this.alpha = alpha;
        this.testType = testType;
        bollenTingTest = new BollenTingTetradTest(cov);
    }

    public FindOneFactorClusters(DataSet dataSet, TestType testType, double alpha) {
        this.variables = dataSet.getVariables();
        this.test = new ContinuousTetradTest(dataSet, testType, alpha);
        this.indTest = new IndTestFisherZ(dataSet, alpha);
        this.alpha = alpha;
        this.testType = testType;

        if (testType == TestType.TETRAD_BOLLEN) {
            bollenTingTest = new BollenTingTetradTest(dataSet);
            bollenTingTest.setCacheFourthMoments(false);
        }
    }

    public Graph search() {
        Set<Set<Integer>> allClusters = getClusters();
        return convertToGraph(allClusters);
    }

    public Graph search2() {
        Set<Set<Integer>> allClusters = getClusters();
        return convertToGraph(allClusters);
    }

    //========================================PRIVATE METHODS====================================//

    // This is the main algorithm.
    private Set<Set<Integer>> getClusters() {

        System.out.println("Running PC adjacency search...");
        Graph graph = new EdgeListGraph(variables);
        Fas5 fas = new Fas5(graph, indTest);
        fas.setDepth(1);
        graph = fas.search();
        System.out.println("...done.");


        System.out.println(graph);

        List<Integer> _variables = new ArrayList<Integer>();
        for (int i = 0; i < variables.size(); i++) _variables.add(i);

        Set<Set<Integer>> pureClusters = findPureClusters(_variables, graph);
        Set<Set<Integer>> mixedClusters = findMixedClusters(_variables, unionPure(pureClusters), graph);
        Set<Set<Integer>> allClusters = new HashSet<Set<Integer>>(pureClusters);
        allClusters.addAll(mixedClusters);
        return allClusters;

    }

    // Finds clusters of size 4 or higher.
    private Set<Set<Integer>> findPureClusters(List<Integer> _variables, Graph graph) {
        System.out.println("Original variables = " + variables);

        Set<Set<Integer>> clusters = new HashSet<Set<Integer>>();
        List<Integer> allVariables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        VARIABLES:
        while (!_variables.isEmpty()) {
            if (_variables.size() < 4) break;

            for (int x : _variables) {
                Node nodeX = variables.get(x);

                List<Node> adjX = graph.getAdjacentNodes(nodeX);

                if (adjX.size() < 3) {
                    continue;
                }

                ChoiceGenerator gen = new ChoiceGenerator(adjX.size(), 3);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    Node nodeY = adjX.get(choice[0]);
                    Node nodeZ = adjX.get(choice[1]);
                    Node nodeW = adjX.get(choice[2]);

                    int y = variables.indexOf(nodeY);
                    int w = variables.indexOf(nodeW);
                    int z = variables.indexOf(nodeZ);

                    Set<Integer> cluster = quartet(x, y, z, w);

                    if (!clique(cluster, graph)) {
                        continue;
                    }

                    // Note that purity needs to be assessed with respect to all of the variables in order to
                    // remove all latent-measure impurities between pairs of latents.
                    if (pure(cluster, allVariables)) {

                        O:
                        for (int o : _variables) {
                            if (cluster.contains(o)) continue;
                            cluster.add(o);
                            List<Integer> _cluster = new ArrayList<Integer>(cluster);

                            if (!allVariablesDependent(cluster)) {
                                cluster.remove(o);
                                continue O;
                            }

                            ChoiceGenerator gen2 = new ChoiceGenerator(_cluster.size(), 4);
                            int[] choice2;
                            int count = 0;

                            while ((choice2 = gen2.next()) != null) {
                                int x2 = _cluster.get(choice2[0]);
                                int y2 = _cluster.get(choice2[1]);
                                int z2 = _cluster.get(choice2[2]);
                                int w2 = _cluster.get(choice2[3]);

                                Set<Integer> quartet = quartet(x2, y2, z2, w2);

                                // Optimizes for large clusters.
                                if (quartet.contains(o)) {
                                    if (++count > 2) continue O;
                                }

                                if (quartet.contains(o) && !pure(quartet, allVariables)) {
                                    cluster.remove(o);
                                    continue O;
                                }
                            }
                        }

                        System.out.println("Cluster found: " + variablesForIndices(cluster));
                        clusters.add(cluster);
                        _variables.removeAll(cluster);

                        for (int p : cluster) {
                            graph.removeNode(variables.get(p));
                        }

                        continue VARIABLES;
                    }
                }
            }

            break;
        }

        return clusters;
    }

    //  Finds clusters of size 3.
    private Set<Set<Integer>> findMixedClusters(List<Integer> remaining, Set<Integer> unionPure, Graph graph) {
        Set<Set<Integer>> threeClusters = new HashSet<Set<Integer>>();

        REMAINING:
        while (true) {
            if (remaining.size() < 3) break;

            ChoiceGenerator gen = new ChoiceGenerator(remaining.size(), 3);
            int[] choice;

            while ((choice = gen.next()) != null) {
                int y = remaining.get(choice[0]);
                int z = remaining.get(choice[1]);
                int w = remaining.get(choice[2]);

                Set<Integer> cluster = new HashSet<Integer>();
                cluster.add(y);
                cluster.add(z);
                cluster.add(w);

//                if (!allVariablesDependent(cluster)) {
//                    continue;
//                }

                if (!clique(cluster, graph)) {
                    continue;
                }

                // Check all x as a cross check; really only one should be necessary.
                boolean allX = true;

                for (int x : unionPure) {
                    Set<Integer> _cluster = new HashSet<Integer>(cluster);
                    _cluster.add(x);

                    if (!quartetVanishes(_cluster)) {
                        allX = false;
                        break;
                    }
                }

                if (allX) {
                    threeClusters.add(cluster);
                    unionPure.addAll(cluster);
                    remaining.removeAll(cluster);

                    System.out.println("3-cluster found: " + variablesForIndices(cluster));

                    continue REMAINING;
                }
            }

            break;
        }

        return threeClusters;
    }

    private boolean clique(Set<Integer> cluster, Graph graph) {
        List<Integer> _cluster = new ArrayList<Integer>(cluster);

        for (int i = 0; i < cluster.size(); i++) {
            for (int j = i + 1; j < cluster.size(); j++) {
                Node nodei = variables.get(_cluster.get(i));
                Node nodej = variables.get(_cluster.get(j));

                if (!graph.isAdjacentTo(nodei, nodej)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean allVariablesDependent(Set<Integer> cluster) {
        List<Integer> _cluster = new ArrayList<Integer>(cluster);

        for (int i = 0; i < _cluster.size(); i++) {
            for (int j = i + 1; j < _cluster.size(); j++) {
                Integer _i = _cluster.get(i);
                Integer _j = _cluster.get(j);

                Node node1 = variables.get(_i);
                Node node2 = variables.get(_j);

                if (!indTest.isDependent(node1, node2)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<Node> variablesForIndices(Set<Integer> cluster) {
        List<Node> _cluster = new ArrayList<Node>();

        for (int c : cluster) {
            _cluster.add(variables.get(c));
        }

        Collections.sort(_cluster);

        return _cluster;
    }


    private boolean pure(Set<Integer> quartet, List<Integer> variables) {
        if (quartetVanishes(quartet)) {
            for (int o : variables) {
                if (quartet.contains(o)) continue;

                for (int p : quartet) {
                    Set<Integer> _quartet = new HashSet<Integer>(quartet);
                    _quartet.remove(p);
                    _quartet.add(o);

                    if (!quartetVanishes(_quartet)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    private Set<Integer> quartet(int x, int y, int z, int w) {
        Set<Integer> set = new HashSet<Integer>();
        set.add(x);
        set.add(y);
        set.add(z);
        set.add(w);

        if (set.size() < 4)
            throw new IllegalArgumentException("Quartet elements must be unique: <" + x + ", " + y + ", " + z + ", " + w + ">");

        return set;
    }

    private boolean quartetVanishes(Set<Integer> quartet) {
        if (quartet.size() != 4) throw new IllegalArgumentException("Expecting a quartet, size = " + quartet.size());

        Iterator<Integer> iter = quartet.iterator();
        int x = iter.next();
        int y = iter.next();
        int z = iter.next();
        int w = iter.next();

        return testVanishing(x, y, z, w);
    }

    private boolean testVanishing(int x, int y, int z, int w) {
        if (testType == TestType.TETRAD_BOLLEN) {
            Tetrad t1 = new Tetrad(variables.get(x), variables.get(y), variables.get(z), variables.get(w));
            Tetrad t2 = new Tetrad(variables.get(x), variables.get(y), variables.get(w), variables.get(z));
            double vanishing1 = bollenTingTest.getPValue(t1, t2);
            return vanishing1 > alpha;
        } else {
            return test.tetradHolds(x, y, z, w) && test.tetradHolds(x, y, w, z);
        }
    }

    private Graph convertSearchGraphNodes(Set<Set<Node>> clusters) {
        Graph graph = new EdgeListGraph(variables);

        List<Node> latents = new ArrayList<Node>();
        for (int i = 0; i < clusters.size(); i++) {
            Node latent = new GraphNode(MimBuild.LATENT_PREFIX + (i + 1));
            latent.setNodeType(NodeType.LATENT);
            latents.add(latent);
            graph.addNode(latent);
        }

        List<Set<Node>> _clusters = new ArrayList<Set<Node>>(clusters);

        for (int i = 0; i < latents.size(); i++) {
            for (Node node : _clusters.get(i)) {
                if (!graph.containsNode(node)) graph.addNode(node);
                graph.addDirectedEdge(latents.get(i), node);
            }
        }

        return graph;
    }

    private Graph convertToGraph(Set<Set<Integer>> allClusters) {
        Set<Set<Node>> _clustering = new HashSet<Set<Node>>();

        for (Set<Integer> cluster : allClusters) {
            Set<Node> nodes = new HashSet<Node>();

            for (int i : cluster) {
                nodes.add(variables.get(i));
            }

            _clustering.add(nodes);
        }

        return convertSearchGraphNodes(_clustering);
    }

    private Set<Integer> unionPure(Set<Set<Integer>> pureClusters) {
        Set<Integer> unionPure = new HashSet<Integer>();

        for (Set<Integer> cluster : pureClusters) {
            unionPure.addAll(cluster);
        }

        return unionPure;
    }
}


