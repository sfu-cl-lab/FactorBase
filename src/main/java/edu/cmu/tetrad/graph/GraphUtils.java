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

package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.PointXy;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TextTable;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import nu.xom.*;

import java.text.NumberFormat;
import java.util.regex.Matcher;


/**
 * Basic graph utilities.
 *
 * @author Joseph Ramsey
 */
public final class GraphUtils {

    /**
     * Arranges the nodes in the graph in a line, organizing by cluster
     */
    private static final int NODE_GAP = 50;

    /**
     * Arranges the nodes in the graph in a circle.
     *
     * @param centerx
     * @param centery
     * @param radius  The radius of the circle in pixels; a good default is
     *                150.
     */
    public static void circleLayout(Graph graph, int centerx, int centery,
                                    int radius) {
        List <Node> nodes = graph.getNodes();

        Collections.sort(nodes, new Comparator <Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        double rad = 6.28 / nodes.size();
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            int centerX = centerx + (int) (radius * Math.cos(phi));
            int centerY = centery + (int) (radius * Math.sin(phi));

            node.setCenterX(centerX);
            node.setCenterY(centerY);

            phi += rad;
        }
    }

    public static void arrangeByGraphTiers(Graph graph) {
        List <List <Node>> tiers = getTiers(graph);

        int y = 0;

        for (List <Node> tier1 : tiers) {
            y += 50;
            int x = 0;

            for (Object aTier : tier1) {
                x += 90;
                Node node = (Node) aTier;
                node.setCenterX(x);
                node.setCenterY(y);
            }
        }
    }

    public static void hierarchicalLayout(Graph graph) {
        LayeredDrawing layout = new LayeredDrawing(graph);
        layout.doLayout();
    }

    public static void kamadaKawaiLayout(Graph graph,
                                         boolean randomlyInitialized, double naturalEdgeLength,
                                         double springConstant, double stopEnergy) {
        KamadaKawaiLayout layout = new KamadaKawaiLayout(graph);
        layout.setRandomlyInitialized(randomlyInitialized);
        layout.setNaturalEdgeLength(naturalEdgeLength);
        layout.setSpringConstant(springConstant);
        layout.setStopEnergy(stopEnergy);
        layout.doLayout();
    }

    public static void fruchtermanReingoldLayout(Graph graph) {
        FruchtermanReingoldLayout layout = new FruchtermanReingoldLayout(graph);
        layout.doLayout();
    }

    /**
     * Finds the set of nodes which have no children, followed by the set of
     * their parents, then the set of the parents' parents, and so on.  The
     * result is returned as a List of Lists.
     *
     * @return the tiers of this digraph.
     */
    public static List <List <Node>> getTiers(Graph graph) {
        Set <Node> found = new HashSet <Node>();
        Set <Node> notFound = new HashSet <Node>();
        List <List <Node>> tiers = new LinkedList <List <Node>>();

        // first copy all the nodes into 'notFound'.
        notFound.addAll(graph.getNodes());

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // current tier.
        int notFoundSize = 0;
        boolean jumpstart = false;

        while (!notFound.isEmpty()) {
            List <Node> thisTier = new LinkedList <Node>();

            for (Object aNotFound : notFound) {
                Node node = (Node) aNotFound;
                List <Node> adj = graph.getAdjacentNodes(node);
                List <Node> parents = new LinkedList <Node>();

                for (Object anAdj : adj) {
                    Node _node = (Node) anAdj;
                    Edge edge = graph.getEdge(node, _node);

                    //                    if (Edges.isDirectedEdge(edge) &&
                    //                            Edges.getDirectedEdgeHead(edge) == node) {
                    //                        parents.add(_node);
                    //                    }

                    if (edge.getProximalEndpoint(node) == Endpoint.ARROW &&
                            edge.getDistalEndpoint(node) == Endpoint.TAIL) {
                        parents.add(_node);
                    }
                }

                if (found.containsAll(parents)) {
                    thisTier.add(node);
                } else if (jumpstart) {
                    for (Object parent : parents) {
                        Node _node = (Node) parent;
                        if (!found.contains(_node)) {
                            thisTier.add(_node);
                        }
                    }

                    if (!found.contains(node)) {
                        thisTier.add(node);
                    }

                    jumpstart = false;
                }
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            notFound.removeAll(thisTier);
            found.addAll(thisTier);
            if (notFoundSize == notFound.size()) {
                jumpstart = true;
            }

            notFoundSize = notFound.size();

            // add the current tier to the list of tiers.
            if (!thisTier.isEmpty()) {
                tiers.add(thisTier);
            }
        }

        return tiers;
    }

    /**
     * Arranges the nodes in the graph in a circle, organizing by cluster
     */
    public static void arrangeClustersInCircle(Graph graph) {
        List <Node> latents = new LinkedList <Node>();
        List <List <Node>> partition = new LinkedList <List <Node>>();
        int totalSize = getMeasurementModel(graph, latents, partition);
        boolean gaps[] = new boolean[totalSize];
        List <Node> nodes = new LinkedList <Node>();
        int count = 0;
        for (int i = latents.size() - 1; i >= 0; i--) {
            nodes.add(latents.get(i));
            gaps[count++] = (i == 0);
        }

        for (Object aPartition : partition) {
            List <Node> cluster = (List <Node>) aPartition;
            for (int i = 0; i < cluster.size(); i++) {
                nodes.add(cluster.get(i));
                gaps[count++] = (i == cluster.size() - 1);
            }
        }

        double rad = 6.28 / (nodes.size() + partition.size() + 1);
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (int i = 0; i < nodes.size(); i++) {
            Node n1 = nodes.get(i);
            int centerX = 200 + (int) (150 * Math.cos(phi));
            int centerY = 200 + (int) (150 * Math.sin(phi));

            n1.setCenterX(centerX);
            n1.setCenterY(centerY);

            if (gaps[i]) {
                phi += 2 * rad;
            } else {
                phi += rad;
            }
        }
    }

    public static void arrangeClustersInLine(Graph graph, boolean jitter) {
        List <Node> latents = new LinkedList <Node>();
        List <List <Node>> partition = new LinkedList <List <Node>>();
        getMeasurementModel(graph, latents, partition);
        List <Node> nodes = new LinkedList <Node>();
        double clusterWidth[] = new double[partition.size()];
        double indicatorWidth[][] = new double[partition.size()][];
        double latentWidth[] = new double[partition.size()];

        for (int i = 0; i < latents.size(); i++) {
            nodes.add(latents.get(i));
            latentWidth[i] = 60;
        }
        for (int k = 0; k < partition.size(); k++) {
            List <Node> cluster = partition.get(k);
            clusterWidth[k] = 0.;
            indicatorWidth[k] = new double[cluster.size()];
            for (int i = 0; i < cluster.size(); i++) {
                nodes.add(cluster.get(i));
                indicatorWidth[k][i] = 60;
                clusterWidth[k] += 60;
            }
            clusterWidth[k] += (cluster.size() - 1.) * NODE_GAP;
        }

        int currentPos = NODE_GAP;
        for (int k = 0; k < partition.size(); k++) {
            Node nl = latents.get(k);
            nl.setCenterX(currentPos + (int) (clusterWidth[k] / 2.));
            int noise = 0;
            if (jitter) {
                noise = RandomUtil.getInstance().nextInt(50) - 25;
            }
            nl.setCenterY(100 + noise);
            List <Node> cluster = partition.get(k);
            for (int i = 0; i < cluster.size(); i++) {
                Node ni = cluster.get(i);
                int centerX = currentPos + (int) (indicatorWidth[k][i] / 2.);
                ni.setCenterX(centerX);
                ni.setCenterY(200);
                currentPos += indicatorWidth[k][i] + NODE_GAP;
            }
            currentPos += 2. * NODE_GAP;
        }
    }

    /**
     * Decompose a latent variable graph into its measurement model
     */
    public static int getMeasurementModel(Graph graph, List <Node> latents,
                                          List <List <Node>> partition) {
        int totalSize = 0;

        for (Object o : graph.getNodes()) {
            Node node = (Node) o;
            if (node.getNodeType() == NodeType.LATENT) {
                Collection <Node> children = graph.getChildren(node);
                List <Node> newCluster = new LinkedList <Node>();

                for (Object aChildren : children) {
                    Node child = (Node) aChildren;
                    if (child.getNodeType() == NodeType.MEASURED) {
                        newCluster.add(child);
                    }
                }

                latents.add(node);
                partition.add(newCluster);
                totalSize += 1 + newCluster.size();
            }
        }
        return totalSize;
    }

    public static Dag randomDag(List <Node> nodes, int numLatentNodes,
                                int maxNumEdges, int maxDegree,
                                int maxIndegree, int maxOutdegree,
                                boolean connected) {
        int numNodes = nodes.size();

        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (maxNumEdges < 0 || maxNumEdges > numNodes * (numNodes - 1)) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    maxNumEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        for (Node node : nodes) {
            node.setNodeType(NodeType.MEASURED);
        }

        UniformGraphGenerator generator;

        if (connected) {
            generator = new UniformGraphGenerator(
                    UniformGraphGenerator.CONNECTED_DAG);
        } else {
            generator =
                    new UniformGraphGenerator(UniformGraphGenerator.ANY_DAG);
        }

        generator.setNumNodes(numNodes);
        generator.setMaxEdges(maxNumEdges);
        generator.setMaxDegree(maxDegree);
        generator.setMaxInDegree(maxIndegree);
        generator.setMaxOutDegree(maxOutdegree);
        generator.generate();
        Dag dag = generator.getDag(nodes);

        // Create a list of nodes. Add the nodes in the list to the
        // dag. Arrange the nodes in a circle.
        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        return dag;
    }

    /**
     * Implements the method in Melancon and Dutour, "Random Generation of
     * Directed Graphs," with optional biases added.
     */
    public static Dag randomDag(int numNodes, int numLatentNodes,
                                int maxNumEdges, int maxDegree,
                                int maxIndegree, int maxOutdegree,
                                boolean connected) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (maxNumEdges < 0 || maxNumEdges > numNodes * (numNodes - 1)) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    maxNumEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        UniformGraphGenerator generator;

        if (connected) {
            generator = new UniformGraphGenerator(
                    UniformGraphGenerator.CONNECTED_DAG);
        } else {
            generator =
                    new UniformGraphGenerator(UniformGraphGenerator.ANY_DAG);
        }

        generator.setNumNodes(numNodes);
        generator.setMaxEdges(maxNumEdges);
        generator.setMaxDegree(maxDegree);
        generator.setMaxInDegree(maxIndegree);
        generator.setMaxOutDegree(maxOutdegree);
        generator.generate();
        Dag dag = generator.getDag();

        // Create a list of nodes. Add the nodes in the list to the
        // dag. Arrange the nodes in a circle.
        List <Node> nodes = dag.getNodes();
        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        return dag;
    }

    public static Dag randomDag(int numNodes, int numEdges, boolean connected) {
        Dag dag;

        do {
            dag = GraphUtils.randomDag(numNodes, 0, numEdges, 30, 15, 15, connected);
        } while (dag.getNumEdges() < numEdges);

        return dag;
    }

    public static Dag randomDag(List <Node> nodes, int numEdges, boolean connected) {
        Dag dag;

        do {
            dag = GraphUtils.randomDag(nodes, 0, numEdges, 30, 15, 15, connected);
        } while (dag.getNumEdges() < numEdges);

        return dag;
    }

    /**
     * Creates a random DAG by choosing each edge with uniform probability from
     * available edges at each stage and adding it. This is biased in the
     * direction of slighly longer path lengths with respect to an unbiased
     * method, with the bias increasing as the number of nodes increases, but it
     * is fine for relatively low numbers of edges.
     */
    public static Dag randomDagB(int numNodes, int numLatentNodes,
                                 int numEdges, double convergenceBias, double divergenceBias,
                                 double chainingBias) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Dag graph = new Dag();
        Dag dpathGraph = new Dag();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List <Node> nodes = new ArrayList <Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        for (Object node3 : nodes) {
            Node node = (Node) node3;
            graph.addNode(node);
            dpathGraph.addNode(node);
        }

        GraphUtils.circleLayout(graph, 200, 200, 150);

        while (graph.getNumEdges() < numEdges) {
            double[] fromWeights = new double[numNodes];

            for (int j = 0; j < numNodes; j++) {
                Node from = nodes.get(j);

                for (int k = 0; k < numNodes; k++) {
                    if (j == k) {
                        continue;
                    }

                    Node to = nodes.get(k);

                    if (graph.isParentOf(from, to)) {
                        continue;
                    }

                    if (dpathGraph.isParentOf(to, from)) {
                        continue;
                    }

                    fromWeights[j] += 1.0;
                }

                int indegree = graph.getIndegree(from);
                int outdegree = graph.getOutdegree(from);

                if (outdegree > 0) {
                    fromWeights[j] *= multiplier(divergenceBias, numNodes);

                    if (fromWeights[j] == 0.0) {
                        fromWeights[j] = 1.e-10;
                    }
                }

                if (indegree == 1 && outdegree == 0) {
                    fromWeights[j] *= multiplier(chainingBias, numNodes);

                    if (fromWeights[j] == 0.0) {
                        fromWeights[j] = 1.e-10;
                    }
                }
            }

            int fromIndex = getIndex(fromWeights);

            // Make array of weights for to nodes.
            double[] toWeights = new double[numNodes];
            boolean foundPositiveWeight = false;

            for (int j = 0; j < numNodes; j++) {
                if (j == fromIndex) {
                    continue;
                }

                Node from = nodes.get(fromIndex);
                Node to = nodes.get(j);

                if (graph.isParentOf(from, to)) {
                    continue;
                }

                if (graph.isAncestorOf(to, from)) {
                    continue;
                }

                toWeights[j] = 1.0;

                int indegree = graph.getIndegree(to);
                int outdegree = graph.getOutdegree(to);

                if (indegree > 0) {
                    toWeights[j] *= multiplier(convergenceBias, numNodes);

                    if (toWeights[j] == 0.0) {
                        toWeights[j] = 1.e-10;
                    }
                }

                if (indegree == 0 && outdegree == 1) {
                    toWeights[j] *= multiplier(chainingBias, numNodes);

                    if (toWeights[j] == 0.0) {
                        toWeights[j] = 1.e-10;
                    }
                }

                foundPositiveWeight = true;
            }

            if (!foundPositiveWeight) {
                continue;
            }

            int toIndex = getIndex(toWeights);

            Node node1 = nodes.get(fromIndex);
            Node node2 = nodes.get(toIndex);

            if (dpathGraph.isParentOf(node2, node1)) {
                throw new IllegalStateException();
            }

            if (graph.isAdjacentTo(node2, node1)) {
                continue;
            }

            if (graph.isAncestorOf(node2, node1)) {
                continue;
            }

            graph.addDirectedEdge(node1, node2);

            if (!dpathGraph.isParentOf(node1, node2)) {
                dpathGraph.addDirectedEdge(node1, node2);
            }

            List <Node> parents = dpathGraph.getParents(node1);

            for (Object parent1 : parents) {
                Node parent = (Node) parent1;
                if (!dpathGraph.isParentOf(parent, node2)) {
                    dpathGraph.addDirectedEdge(parent, node2);
                }
            }
        }

        return graph;
    }

    /**
     * Creates a random DAG by selecting a random edge x-->y from a node earlier
     * in the list of nodes to a node later in the list of nodes at each state,
     * where all such nodes are weighted equally. This is biased toward
     * divergence for nodes near the beginning of the list and convergence for
     * nodes toward the end of the list.
     */
    public static Dag randomDagC(int numNodes, int numLatentNodes,
                                 int numEdges) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Dag graph = new Dag();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List <Node> nodes = new ArrayList <Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        System.out.println("Adding nodes");
        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        // Add nodes to graph.
        for (Node node3 : nodes) {
            graph.addNode(node3);
        }

        GraphUtils.circleLayout(graph, 200, 200, 150);

        // Iterate through all pairs of nodes and add a directed
        // edge between a pair if a randomly chosen number in [0,
        // 1] is < probability 'probIncludeEdge'. Flip a coin to
        // determine the direction of the arrow.
        int numPossibleEdges = numNodes * numNodes;
        int edgeCount = 0;
        int numTrials = 0;

        while (edgeCount < numEdges && numTrials < 5 * numEdges) {
            numTrials++;

            System.out.println(edgeCount);

            int edgeIndex = RandomUtil.getInstance().nextInt(
                    numPossibleEdges);
            int first = edgeIndex / numNodes;
            int second = edgeIndex % numNodes;

            if (first == second) {
                continue;
            }

            Node node1, node2;

            // Add from lower index node to higher index node to guarantee
            // acyclicity.
            if (first < second) {
                node1 = nodes.get(first);
                node2 = nodes.get(second);
            } else {
                node1 = nodes.get(second);
                node2 = nodes.get(first);
            }

//            System.out.println(node1 + "-->" + node2);

            if (graph.getEdge(node1, node2) != null) {
                continue;
            }

//            if (graph.getIndegree(node2) > maxIndegree - 1) {
//                continue;
//            }
//
//            if (graph.getOutdegree(node1) > maxOutdegree - 1) {
//                continue;
//            }
//
//            if (graph.getNumEdges(node1) > maxDegree - 1) {
//                continue;
//            }
//
//            if (graph.getNumEdges(node2) > maxDegree - 1) {
//                continue;
//            }

            graph.addDirectedEdge(node1, node2);
            edgeCount++;
        }

        return graph;
    }

    public static Graph randomDagCb(int numNodes, int numLatentNodes,
                                    int numEdges) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Graph graph = new EdgeListGraph();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List <Node> nodes = new ArrayList <Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        System.out.println("Adding nodes");
        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
            System.out.println("Adding " + i);
        }

//        int numNodesMadeLatent = 0;
//
//        while (numNodesMadeLatent < numLatentNodes) {
//            int index = RandomUtil.getInstance().nextInt(numNodes);
//            Node node = nodes.get(index);
//            if (node.getNodeType() == NodeType.LATENT) {
//                continue;
//            }
//            node.setNodeType(NodeType.LATENT);
//            numNodesMadeLatent++;
//        }

        // Add nodes to graph.
        for (Node node3 : nodes) {
            graph.addNode(node3);
        }

//        GraphUtils.circleLayout(graph, 200, 200, 150);

        // Iterate through all pairs of nodes and add a directed
        // edge between a pair if a randomly chosen number in [0,
        // 1] is < probability 'probIncludeEdge'. Flip a coin to
        // determine the direction of the arrow.
        int numPossibleEdges = numNodes * numNodes;
        int edgeCount = 0;
        int numTrials = 0;

        while (edgeCount < numEdges && numTrials < 5 * numEdges) {
            numTrials++;

            System.out.println(edgeCount);

            int edgeIndex = RandomUtil.getInstance().nextInt(
                    numPossibleEdges);
            int first = edgeIndex / numNodes;
            int second = edgeIndex % numNodes;

            if (first == second) {
                continue;
            }

            Node node1, node2;

            // Add from lower index node to higher index node to guarantee
            // acyclicity.
            if (first < second) {
                node1 = nodes.get(first);
                node2 = nodes.get(second);
            } else {
                node1 = nodes.get(second);
                node2 = nodes.get(first);
            }

//            System.out.println(node1 + "-->" + node2);

            if (graph.getEdge(node1, node2) != null) {
                continue;
            }

//            if (graph.getIndegree(node2) > maxIndegree - 1) {
//                continue;
//            }
//
//            if (graph.getOutdegree(node1) > maxOutdegree - 1) {
//                continue;
//            }
//
//            if (graph.getNumEdges(node1) > maxDegree - 1) {
//                continue;
//            }
//
//            if (graph.getNumEdges(node2) > maxDegree - 1) {
//                continue;
//            }

            graph.addDirectedEdge(node1, node2);
            edgeCount++;
        }

        return graph;
    }


    /**
     * Creates a random DAG by selecting a random edge x-->y from a node earlier
     * in the list of nodes to a node later in the list of nodes at each state,
     * where all such nodes are weighted equally. This is biased toward
     * divergence for nodes near the beginning of the list and convergence for
     * nodes toward the end of the list.
     */
    public static Dag randomDagE(int numNodes, int numLatentNodes,
                                 int numEdges, int maxDegree, int maxIndegree, int maxOutdegree) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Dag graph = new Dag();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List <Node> nodes = new ArrayList <Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        // Add nodes to graph.
        for (Node node3 : nodes) {
            graph.addNode(node3);
        }

        GraphUtils.circleLayout(graph, 200, 200, 150);

        // Iterate through all pairs of nodes and add a directed
        // edge between a pair if a randomly chosen number in [0,
        // 1] is < probability 'probIncludeEdge'. Flip a coin to
        // determine the direction of the arrow.
        int numPossibleEdges = numNodes * numNodes;
        int edgeCount = 0;
        int numTrials = 0;

        while (edgeCount < numEdges && numTrials < 5 * numEdges) {
            numTrials++;

            int edgeIndex = RandomUtil.getInstance().nextInt(
                    numPossibleEdges);
            int first = edgeIndex / numNodes;
            int second = edgeIndex % numNodes;

            if (first == second) {
                continue;
            }

            Node node1, node2;

            // Add from lower index node to higher index node to guarantee
            // acyclicity.
            if (first < second) {
                node1 = nodes.get(first);
                node2 = nodes.get(second);
            } else {
                node1 = nodes.get(second);
                node2 = nodes.get(first);
            }

//            System.out.println(node1 + "-->" + node2);

            if (graph.getEdge(node1, node2) != null) {
                continue;
            }

            if (graph.getIndegree(node2) > maxIndegree - 1) {
                continue;
            }

            if (graph.getOutdegree(node1) > maxOutdegree - 1) {
                continue;
            }

            if (graph.getNumEdges(node1) > maxDegree - 1) {
                continue;
            }

            if (graph.getNumEdges(node2) > maxDegree - 1) {
                continue;
            }

            graph.addDirectedEdge(node1, node2);
            edgeCount++;
        }

        return graph;
    }


    /**
     * Implements the method in Melancon and Dutour, "Random Generation of
     * Directed Graphs," with optional biases added.
     */
    public static Dag randomDagD(int numNodes, int numLatentNodes,
                                 int minNumEdges, int maxNumEdges) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (maxNumEdges < 0 || maxNumEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    maxNumEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        GraphGeneratorRandomNumEdges generator =
                new GraphGeneratorRandomNumEdges(UniformGraphGenerator.ANY_DAG);

        generator.setNumNodes(numNodes);
        generator.setMaxEdges(maxNumEdges);
        generator.setMinEdges(minNumEdges);
        generator.generate();
        Dag dag = generator.getDag();

        // Create a list of nodes. Add the nodes in the list to the
        // dag. Arrange the nodes in a circle.
        List <Node> nodes = dag.getNodes();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 0; i < numNodes; i++) {
            Node node = nodes.get(i);
            node.setName("X" + nf.format(i + 1));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = RandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        return dag;
    }

    /**
     * This method builds on the randomDag methods by implementing a procedure
     * for adding cycles to a graph.
     *
     * @param dag            A Dag returned from any of the randomDag methods
     * @param maxNumEdges    Algorithm will add at most this many cyclic edges to the graph
     * @param minCycleLength The smallest number of edges allowed for creating cycles
     * @return
     */

    public static Graph addCycles(Dag dag, int maxNumEdges, int minCycleLength) {

        if (maxNumEdges <= 0) {
            throw new IllegalArgumentException(
                    "maxNumEdges most be > 0: " + maxNumEdges);
        }

        if (minCycleLength <= 0) {
            throw new IllegalArgumentException(
                    "minCycleLength most be > 0: " + minCycleLength);
        }

        // convert dag to EgdeListGraph
        List <Edge> edges = dag.getEdges();
        EdgeListGraph graph = new EdgeListGraph(dag.getNodes());
        for (Edge e : edges) {
            graph.addEdge(e);
        }

        int cycles = maxNumEdges; // make up to this many cycles

        //get nodes in list
        List <Node> nodes = graph.getNodes();

        //go through list and get all possible cycles
        List <NodePair> cycleEdges = new ArrayList <NodePair>();
        for (Node i : nodes) {
            List <Node> c = findPotentialCycle(i, graph, -minCycleLength + 1);
            for (Node j : c) {
                NodePair p = new NodePair(i, j);
                if (!cycleEdges.contains(p))
                    cycleEdges.add(p);
            }
        }

        // with all edge possibilities, we pick from random and add to dag
        if (cycles > cycleEdges.size()) cycles = cycleEdges.size();
        for (int i = cycles; i > 0; i--) {
            int r = RandomUtil.getInstance().nextInt(i);
            NodePair p = cycleEdges.get(r);
            graph.addDirectedEdge(
                    graph.getNode(p.getFirst().getName()),
                    graph.getNode(p.getSecond().getName()));
            cycleEdges.remove(r);
        }

        return graph;
    }

    public static Graph addCycles2(Dag dag, int minNumCycles, int minlength) {
        if (minlength < 2) {
            throw new IllegalArgumentException("Cycle length must be at least 2.");
        }

        if (dag == null) throw new NullPointerException();

        Graph graph = new EdgeListGraph(dag);
        List <Node> nodes = graph.getNodes();

        Map <Edge, List <List <Node>>> edgePaths = new HashMap <Edge, List <List <Node>>>();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);

                List <List <Node>> _directedPaths = GraphUtils.directedPathsFromTo(graph, node1, node2);

                if (!_directedPaths.isEmpty()) {
                    Edge edge = Edges.directedEdge(node2, node1);
                    edgePaths.put(edge, _directedPaths);
                }
            }
        }

        for (Edge edge : new HashSet <Edge>(edgePaths.keySet())) {
            List <List <Node>> paths = edgePaths.get(edge);

            for (List <Node> path : new ArrayList <List <Node>>(paths)) {
                if (path.size() < minlength) {
                    edgePaths.remove(edge);
                    break;
                }
            }
        }

        int _numCycles = 0;
        int numTrials = -1;

        List <Edge> cyclicEdges = new ArrayList <Edge>(edgePaths.keySet());

        while (_numCycles < minNumCycles && ++numTrials < 4 * minNumCycles) {
            if (cyclicEdges.isEmpty()) {
                return graph;
            }

            int r = RandomUtil.getInstance().nextInt(cyclicEdges.size());
            Edge edge = cyclicEdges.get(r);

            if (graph.getAdjacentNodes(edge.getNode1()).size() > 4) {
                continue;
            }

            if (graph.getAdjacentNodes(edge.getNode2()).size() > 4) {
                continue;
            }

            cyclicEdges.remove(edge);
            graph.addEdge(edge);
            removeIdenticalPaths(edgePaths, edge, cyclicEdges);
            _numCycles += edgePaths.get(edge).size();

            System.out.println("Adding " + edgePaths.get(edge).size() + " cycles: " + edge);
        }

        graph = new EdgeListGraph(graph.getNodes());

        // kludge
//        graph = cyclicGraph3(dag.getNumNodes(), dag.getNumEdges(), 0);

        return graph;
    }

    public static Graph cyclicGraph3(int numNodes, int numEdges, int numTwoCycles) {
        List <Node> nodes = new ArrayList <Node>();

        for (int i = 0; i < numNodes; i++) {
            nodes.add(new GraphNode("X" + (i + 1)));
        }

        Graph graph = new EdgeListGraph(nodes);

        for (int r = 0; r < numEdges; r++) {
            System.out.println("r = " + r);

            int i = RandomUtil.getInstance().nextInt(numNodes);
            int j = RandomUtil.getInstance().nextInt(numNodes);

            if (i == j) {
                r--;
                continue;
            }

            if (graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                r--;
                continue;
            }

            Edge edge = Edges.directedEdge(nodes.get(i), nodes.get(j));
            graph.addEdge(edge);
        }

        List <Edge> edges = graph.getEdges();

        for (int s = 0; s < numTwoCycles; s++) {
            Edge edge = edges.get(RandomUtil.getInstance().nextInt(edges.size()));
            Edge reversed = Edges.directedEdge(edge.getNode2(), edge.getNode1());

            if (graph.containsEdge(reversed)) {
                s--;
                continue;
            }

            graph.addEdge(reversed);
        }

        GraphUtils.circleLayout(graph, 200, 200, 150);

        return graph;
    }

    /**
     * Makes a cyclic graph by repeatedly adding cycles of length of 3, 4, or 5 to the graph, then finally
     * adding two cycles.
     */
    public static Graph cyclicGraph4(int numNodes, int numEdges, int numTwoCycles) {


        List <Node> nodes = new ArrayList <Node>();

        for (int i = 0; i < numNodes; i++) {
            nodes.add(new GraphNode("X" + (i + 1)));
        }

        Graph graph = new EdgeListGraph(nodes);
        int count1 = -1;

        LOOP:
        while (graph.getEdges().size() < numEdges && ++count1 < 100) {
//            int cycleSize = RandomUtil.getInstance().nextInt(2) + 4;
            int cycleSize = RandomUtil.getInstance().nextInt(3) + 3;

            // Pick that many nodes randomly
            System.out.println("====");
            List <Node> cycleNodes = new ArrayList <Node>();
            int count2 = -1;

            for (int i = 0; i < cycleSize; i++) {
                Node node = nodes.get(RandomUtil.getInstance().nextInt(nodes.size()));

                System.out.println("Adding " + node);

                if (cycleNodes.contains(node)) {
                    i--;
                    ++count2;
                    if (count2 < 10) continue;
                }

                cycleNodes.add(node);
            }

            for (int i = 0; i < cycleSize; i++) {
                Node node = cycleNodes.get(i);

                if (graph.getAdjacentNodes(node).size() > 3) {
                    continue LOOP;
                }
            }

            Edge edge;

            // Make sure you won't created any two cycles (this will be done later, explicitly)
            for (int i = 0; i < cycleNodes.size() - 1; i++) {
                edge = Edges.directedEdge(cycleNodes.get(i + 1), cycleNodes.get(i));

                if (graph.containsEdge(edge)) {
                    continue LOOP;
                }
            }

            edge = Edges.directedEdge(cycleNodes.get(0), cycleNodes.get(cycleNodes.size() - 1));

            if (graph.containsEdge(edge)) {
                continue LOOP;
            }

            for (int i = 0; i < cycleNodes.size() - 1; i++) {
                edge = Edges.directedEdge(cycleNodes.get(i), cycleNodes.get(i + 1));

                if (!graph.containsEdge(edge)) {
                    graph.addEdge(edge);

                    if (graph.getNumEdges() == numEdges) {
                        break LOOP;
                    }
                }
            }

            edge = Edges.directedEdge(cycleNodes.get(cycleNodes.size() - 1), cycleNodes.get(0));

            if (!graph.containsEdge(edge)) {
                graph.addEdge(edge);

                if (graph.getNumEdges() == numEdges) {
                    break LOOP;
                }
            }
        }

        GraphUtils.circleLayout(graph, 200, 200, 150);

        List <Edge> edges = graph.getEdges();
        Collections.shuffle(edges);

        for (int i = 0; i < Math.min(numTwoCycles, edges.size()); i++) {
            Edge edge = edges.get(i);
            Edge reversed = Edges.directedEdge(edge.getNode2(), edge.getNode1());

            if (graph.containsEdge(reversed)) {
                i--;
                continue;
            }

            graph.addEdge(reversed);
        }


        return graph;
    }

    private static void removeIdenticalPaths(Map <Edge, List <List <Node>>> edgePaths, Edge edge,
                                             List <Edge> cyclicEdges) {
        for (Edge _edge : cyclicEdges) {
            for (List <Node> path : edgePaths.get(edge)) {
                for (List <Node> _path : new ArrayList <List <Node>>(edgePaths.get(_edge))) {
                    if (samePath(path, _path)) {
                        edgePaths.get(_edge).remove(_path);
                    }

                    if (edgePaths.get(_edge).isEmpty()) {
                        edgePaths.remove(_edge);
                    }
                }
            }
        }
    }

    /**
     * Assumes the nodes on path1 are unique and the nodes on path2 are unique.
     */
    private static boolean samePath(List <Node> path1, List <Node> path2) {
        if (path1.isEmpty() && path2.isEmpty()) {
            return true;
        }

        if (path1.size() != path2.size()) {
            return false;
        }

        int firstIndex = path2.indexOf(path1.get(0));

        if (firstIndex == -1) {
            return false;
        }

        for (int i = 0; i < path1.size(); i++) {
            int i2 = (i + firstIndex) % path2.size();
            Node node1 = path1.get(i);
            Node node2 = path2.get(i2);

            if (!(node1 == node2)) {
                return false;
            }
        }

        return true;
    }

    public static Graph addCycles3(Dag dag, int minNumCycles, int minlength) {
        if (minlength < 2) {
            throw new IllegalArgumentException("Cycle length must be at least 2.");
        }

        Graph graph = new EdgeListGraph(dag);
        List <Node> nodes = graph.getNodes();
        Map <Edge, List <List <Node>>> edgePaths = new HashMap <Edge, List <List <Node>>>();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);

                if (!graph.isAdjacentTo(node1, node2)) {
                    continue;
                }

                List <List <Node>> _directedPaths = GraphUtils.directedPathsFromTo(graph, node1, node2);

                if (!_directedPaths.isEmpty()) {
                    Edge edge = graph.getEdge(node1, node2);
                    edgePaths.put(edge, _directedPaths);
                }
            }
        }

//        for (Edge edge : new HashSet<Edge>(edgePaths.keySet())) {
//            List<List<Node>> paths = edgePaths.get(edge);
//
//            for (List<Node> path : new ArrayList<List<Node>>(paths)) {
//                if (path.size() < minlength) {
//                    edgePaths.remove(edge);
//                    System.out.println("Num edges = " + edgePaths.keySet().size());
//                    break;
//                }
//            }
//        }

        int _numCycles = 0;
        int trials = 0;

        List <Edge> cyclicEdges = new ArrayList <Edge>(edgePaths.keySet());

        while (_numCycles < minNumCycles && trials < minNumCycles) {
            if (cyclicEdges.isEmpty()) {
                return null;
            }

            int r = RandomUtil.getInstance().nextInt(cyclicEdges.size());
            Edge edge = cyclicEdges.get(r);
            cyclicEdges.remove(edge);

            for (List <Node> path : edgePaths.get(edge)) {
                for (int i = 0; i < path.size() - 2; i++) {
                    cyclicEdges.remove(graph.getEdge(path.get(i), path.get(i + 1)));
                }
            }

            graph.removeEdge(edge.getNode1(), edge.getNode2());
            graph.addEdge(Edges.directedEdge(edge.getNode2(), edge.getNode1()));
            _numCycles += edgePaths.get(edge).size();
        }

        return graph;
    }

    public static List <Node> findPotentialCycle(Node node, Graph dag, Integer depth) {

        List <Node> candidate = new ArrayList <Node>();
        List <Node> parent = dag.getParents(node);

        for (Node i : parent) {
            List <Node> c = findPotentialCycle(i, dag, depth + 1);
            for (Node n : c)
                candidate.add(n);
        }

        if (depth > 0 && parent.size() == 0) candidate.add(node);

        return candidate;

    }

    public static Graph randomMim(int numStructuralNodes,
                                  int numStructuralEdges, int numMeasurementsPerLatent,
                                  int numLatentMeasuredImpureParents,
                                  int numMeasuredMeasuredImpureParents,
                                  int numMeasuredMeasuredImpureAssociations) {

        return randomMim(numStructuralNodes, numStructuralEdges, numMeasurementsPerLatent,
                numLatentMeasuredImpureParents, numMeasuredMeasuredImpureParents, numMeasuredMeasuredImpureAssociations,
                true, false);
    }

    public static Graph randomMim(int numStructuralNodes,
                                  int numStructuralEdges, int numMeasurementsPerLatent,
                                  int numLatentMeasuredImpureParents,
                                  int numMeasuredMeasuredImpureParents,
                                  int numMeasuredMeasuredImpureAssociations, boolean arrangeGraph, boolean acyclic) {
//        Dag dag = GraphUtils.randomDagB(numStructuralNodes,
//                numStructuralNodes, numStructuralEdges, 0.0, 0.0, 0.0);

        Graph dag = GraphUtils.randomDag(numStructuralNodes, numStructuralNodes, numStructuralEdges, 4, 3, 3, false);

        Graph graph = new EdgeListGraph(dag);

        List <Node> latents = graph.getNodes();

        for (int i = 0; i < latents.size(); i++) {
            Node latent = latents.get(i);

            if (!(latent.getNodeType() == NodeType.LATENT)) {
                throw new IllegalArgumentException("Expected latent.");
            }

            latent.setName("L" + (i + 1));
        }

        int measureIndex = 0;

        for (Object latent1 : latents) {
            Node latent = (Node) latent1;

            for (int j = 0; j < numMeasurementsPerLatent; j++) {
                Node measurement = new GraphNode("X" + (++measureIndex));
                graph.addNode(measurement);
                graph.addDirectedEdge(latent, measurement);
            }
        }

        // Latent-->measured.
        int misses = 0;

        for (int i = 0; i < numLatentMeasuredImpureParents; i++) {
            if (misses > 10) {
                break;
            }

            int j = RandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List <Node> nodes = graph.getNodes();
            List <Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);
            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = RandomUtil.getInstance().nextInt(nodes.size());
            Node measure = nodes.get(k);

            if (graph.getEdge(latent, measure) != null ||
                    graph.isAncestorOf(measure, latent)) {
                i--;
                misses++;
                continue;
            }

            // These can't create cycles.
            graph.addDirectedEdge(latent, measure);

//            System.out.println("Latent to  measured: " + graph.getEdge(latent,  measure));
        }

        // Measured-->measured.
        misses = 0;

        for (int i = 0; i < numMeasuredMeasuredImpureParents; i++) {
            if (misses > 10) {
                break;
            }

            int j = RandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List <Node> nodes = graph.getNodes();
            List <Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);

            if (measures.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int m = RandomUtil.getInstance().nextInt(measures.size());
            Node measure1 = measures.get(m);

            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = RandomUtil.getInstance().nextInt(nodes.size());
            Node measure2 = nodes.get(k);

            if (graph.getEdge(measure1, measure2) != null ||
                    graph.isAncestorOf(measure2, measure1)) {
                i--;
                misses++;
                continue;
            }

            graph.addDirectedEdge(measure1, measure2);
//            System.out.println("Measure to  measure: " + graph.getEdge(measure1,  measure2));
        }

        // Measured<->measured.
        misses = 0;

        for (int i = 0; i < numMeasuredMeasuredImpureAssociations; i++) {
            if (misses > 10) {
                break;
            }

            int j = RandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List <Node> nodes = graph.getNodes();
            List <Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);

            if (measures.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int m = RandomUtil.getInstance().nextInt(measures.size());
            Node measure1 = measures.get(m);

            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = RandomUtil.getInstance().nextInt(nodes.size());
            Node measure2 = nodes.get(k);

            if (graph.getEdge(measure1, measure2) != null) {
                i--;
                misses++;
                continue;
            }

            graph.addBidirectedEdge(measure1, measure2);
//            System.out.println("Bidirected: " + graph.getEdge(measure1, measure2));
        }

        if (arrangeGraph) {
            GraphUtils.circleLayout(graph, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(graph);
        }

        return graph;
    }

    private static double multiplier(double bias, int numNodes) {
        if (bias > 0.0) {
            return numNodes * bias + 1.0;
        } else {
            return bias + 1.0;
        }
    }

    private static int getIndex(double[] weights) {
        double sum = 0.0;

        for (double weight : weights) {
            sum += weight;
        }

        double random = RandomUtil.getInstance().nextDouble() * sum;
        double partialSum = 0.0;

        for (int j = 0; j < weights.length; j++) {
            partialSum += weights[j];

            if (partialSum > random) {
                return j;
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Arranges the nodes in the result graph according to their positions in
     * the source graph.
     *
     * @param resultGraph
     * @param sourceGraph
     * @return true if all of the nodes were arranged, false if not.
     */
    public static boolean arrangeBySourceGraph(Graph resultGraph,
                                               Graph sourceGraph) {
        if (resultGraph == null) {
            throw new IllegalArgumentException("Graph must not be null.");
        }

        if (sourceGraph == null) {
            GraphUtils.circleLayout(resultGraph, 200, 200, 150);
            return true;
        }

        boolean arrangedAll = true;

        // There is a source graph. Position the nodes in the
        // result graph correspondingly.
        for (Object o : resultGraph.getNodes()) {
            Node node = (Node) o;
            String name = node.getName();
            Node sourceNode = sourceGraph.getNode(name);

            if (sourceNode == null) {
                arrangedAll = false;
                continue;
            }

            node.setCenterX(sourceNode.getCenterX());
            node.setCenterY(sourceNode.getCenterY());
        }

        return arrangedAll;
    }

    public static void arrangeByLayout(Graph graph, HashMap <String, PointXy> layout) {
        for (Node node : graph.getNodes()) {
            PointXy point = layout.get(node.getName());
            node.setCenter(point.getX(), point.getY());
        }
    }

    /**
     * Returns the node associated with a given error node. This should be the
     * only child of the error node, E --> N.
     */
    public static Node getAssociatedNode(Node errorNode, Graph graph) {
        if (errorNode.getNodeType() != NodeType.ERROR) {
            throw new IllegalArgumentException(
                    "Can only get an associated node " + "for an error node: " +
                            errorNode);
        }

        List <Node> children = graph.getChildren(errorNode);

        if (children.size() != 1) {
            System.out.println("children of " + errorNode + " = " + children);
            System.out.println(graph);

            throw new IllegalArgumentException(
                    "An error node should have only " +
                            "one child, which is its associated node: " +
                            errorNode);
        }

        return children.get(0);
    }

    /**
     * Returns true if <code>set</code> is a clique in <code>graph</code>. </p>
     * R. Silva, June 2004
     */

    public static boolean isClique(Set <Node> set, Graph graph) {
        List <Node> setv = new LinkedList <Node>(set);
        for (int i = 0; i < setv.size() - 1; i++) {
            for (int j = i + 1; j < setv.size(); j++) {
                if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculates the Markov blanket of a target in a DAG. This includes the
     * target, the parents of the target, the children of the target, the
     * parents of the children of the target, edges from parents to target,
     * target to children, parents of children to children, and parent to
     * parents of children. (Edges among children are implied by the inclusion
     * of edges from parents of children to children.) Edges among parents and
     * among parents of children not explicitly included above are not included.
     * (Joseph Ramsey 8/6/04)
     *
     * @param target a node in the given DAG.
     * @param dag    the DAG with respect to which a Markov blanket DAG is to to
     *               be calculated. All of the nodes and edges of the Markov
     *               Blanket DAG are in this DAG.
     */
    public static Dag markovBlanketDag(Node target, Graph dag) {
        if (dag.getNode(target.getName()) == null) {
            throw new NullPointerException("Target node not in graph: " + target);
        }

        Graph blanket = new EdgeListGraph();
        blanket.addNode(target);

        // Add parents of target.
        List <Node> parents = dag.getParents(target);
        for (Object parent1 : parents) {
            Node parent = (Node) parent1;
            blanket.addNode(parent);

            blanket.addDirectedEdge(parent, target);
        }

        // Add children of target and parents of children of target.
        List <Node> children = dag.getChildren(target);
        List <Node> parentsOfChildren = new LinkedList <Node>();
        for (Object aChildren : children) {
            Node child = (Node) aChildren;

            if (!blanket.containsNode(child)) {
                blanket.addNode(child);
            }

            blanket.addDirectedEdge(target, child);

            List <Node> parentsOfChild = dag.getParents(child);
            parentsOfChild.remove(target);
            for (Object aParentsOfChild : parentsOfChild) {
                Node parentOfChild = (Node) aParentsOfChild;

                if (!parentsOfChildren.contains(parentOfChild)) {
                    parentsOfChildren.add(parentOfChild);
                }

                if (!blanket.containsNode(parentOfChild)) {
                    blanket.addNode(parentOfChild);
                }

                blanket.addDirectedEdge(parentOfChild, child);
            }
        }

        // Add in edges connecting parents and parents of children.
        parentsOfChildren.removeAll(parents);

        for (Object parent2 : parents) {
            Node parent = (Node) parent2;

            for (Object aParentsOfChildren : parentsOfChildren) {
                Node parentOfChild = (Node) aParentsOfChildren;
                Edge edge1 = dag.getEdge(parent, parentOfChild);
                Edge edge2 = blanket.getEdge(parent, parentOfChild);

                if (edge1 != null && edge2 == null) {
                    Edge newEdge = new Edge(parent, parentOfChild,
                            edge1.getProximalEndpoint(parent),
                            edge1.getProximalEndpoint(parentOfChild));

                    blanket.addEdge(newEdge);
                }
            }
        }

        // Add in edges connecting children and parents of children.
        for (Object aChildren1 : children) {
            Node child = (Node) aChildren1;

            for (Object aParentsOfChildren : parentsOfChildren) {
                Node parentOfChild = (Node) aParentsOfChildren;
                Edge edge1 = dag.getEdge(child, parentOfChild);
                Edge edge2 = blanket.getEdge(child, parentOfChild);

                if (edge1 != null && edge2 == null) {
                    Edge newEdge = new Edge(child, parentOfChild,
                            edge1.getProximalEndpoint(child),
                            edge1.getProximalEndpoint(parentOfChild));

                    blanket.addEdge(newEdge);
                }
            }
        }

        return new Dag(blanket);
    }

    /**
     * Returns the connected components of the given graph, as a list of lists
     * of nodes.
     */
    public static List <List <Node>> connectedComponents(Graph graph) {
        List <List <Node>> components = new LinkedList <List <Node>>();
        List <Node> unsortedNodes = new ArrayList <Node>(graph.getNodes());

        while (!unsortedNodes.isEmpty()) {
            Node seed = unsortedNodes.get(0);
            Set <Node> component = new HashSet <Node>();
            collectComponentVisit(seed, component, graph, unsortedNodes);
            components.add(new ArrayList <Node>(component));
        }

        return components;
    }


    /**
     * Assumes node should be in component.
     */
    private static void collectComponentVisit(Node node, Set <Node> component,
                                              Graph graph, List <Node> unsortedNodes) {
        component.add(node);
        unsortedNodes.remove(node);
        List <Node> adj = graph.getAdjacentNodes(node);

        for (Object anAdj : adj) {
            Node _node = (Node) anAdj;

            if (!component.contains(_node)) {
                collectComponentVisit(_node, component, graph, unsortedNodes);
            }
        }
    }

    /**
     * Returns the first directed cycle encountered, or null if none is
     * encountered.
     *
     * @param graph The graph in which a directed cycle is sought.
     * @return the first directed cycle encountered in <code>graph</code>.
     */
    public static List <Node> directedCycle(Graph graph) {
        for (Node node : graph.getNodes()) {
            List <Node> path = directedPathFromTo(graph, node, node);

            if (path != null) {
                return path;
            }
        }

        return null;
    }

    /**
     * Returns the first directed path encountered from <code>node1</code>
     * to <code>node2</code>, or null if no such path is found.
     *
     * @param graph The graph in which a directed path is sought.
     * @param node1 The 'from' node.
     * @param node2 The 'to'node.
     * @return A path from <code>node1</code> to <code>node2</code>, or null
     * if there is no path.
     */
    public static List <Node> directedPathFromTo(Graph graph, Node node1, Node node2) {
        return directedPathVisit(graph, node1, node2, new LinkedList <Node>());
    }

    /**
     * Returns the path of the first directed path found from node1 to node2,
     * if any.
     */
    private static List <Node> directedPathVisit(Graph graph, Node node1, Node node2,
                                                 LinkedList <Node> path) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return path;
            }

            if (path.contains(child)) {
                continue;
            }

            if (directedPathVisit(graph, child, node2, path) != null) {
                return path;
            }
        }

        path.removeLast();
        return null;
    }

    //all adjancencies are directed <=> there is no uncertainty about who the parents of 'node' are.
    public static boolean allAdjacenciesAreDirected(Node node, Graph graph) {
        List <Edge> nodeEdges = graph.getEdges(node);
        for (Edge edge : nodeEdges) {
            if (!edge.isDirected())
                return false;
        }
        return true;
    }

    public static Graph removeBidirectedOrientations(Graph estPattern) {
        estPattern = new EdgeListGraph(estPattern);

        // Make bidirected edges undirected.
        for (Edge edge : estPattern.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                estPattern.removeEdge(edge);
                estPattern.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        return estPattern;
    }

    public static Graph removeBidirectedEdges(Graph estPattern) {
        estPattern = new EdgeListGraph(estPattern);

        // Remove bidirected edges altogether.
        for (Edge edge : new ArrayList <Edge>(estPattern.getEdges())) {
            if (Edges.isBidirectedEdge(edge)) {
                estPattern.removeEdge(edge);
            }
        }

        return estPattern;
    }

    public static Graph undirectedGraph(Graph graph) {
        Graph graph2 = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            if (!graph2.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                graph2.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        return graph2;
    }

    public static Graph nondirectedGraph(Graph graph) {
        Graph graph2 = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Edge nondirected = Edges.nondirectedEdge(edge.getNode1(), edge.getNode2());

            if (!graph2.containsEdge(nondirected)) {
                graph2.addEdge(nondirected);
            }
        }

        return graph2;
    }

    public static Graph completeGraph(Graph graph) {
        Graph graph2 = new EdgeListGraph(graph.getNodes());

        graph2.removeEdges(graph2.getEdges());

        List <Node> nodes = graph2.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);
                graph2.addUndirectedEdge(node1, node2);
            }
        }

        return graph2;
    }

    public static List <List <Node>> directedPathsFromTo(Graph graph, Node node1, Node node2) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        directedPathsFromToVisit(graph, node1, node2, new LinkedList <Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void directedPathsFromToVisit(Graph graph, Node node1, Node node2,
                                                LinkedList <Node> path, List <List <Node>> paths) {
        int witnessed = 0;

        for (Node node : path) {
            if (node == node1) {
                witnessed++;
            }
        }

        if (witnessed > 1) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            directedPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> semidirectedPathsFromTo(Graph graph, Node node1, Node node2) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        semidirectedPathsFromToVisit(graph, node1, node2, new LinkedList <Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void semidirectedPathsFromToVisit(Graph graph, Node node1, Node node2,
                                                    LinkedList <Node> path, List <List <Node>> paths) {
        int witnessed = 0;

        for (Node node : path) {
            if (node == node1) {
                witnessed++;
            }
        }

        if (witnessed > 1) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseSemiDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            semidirectedPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> allPathsFromTo(Graph graph, Node node1, Node node2) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        allPathsFromToVisit(graph, node1, node2, new LinkedList <Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void allPathsFromToVisit(Graph graph, Node node1, Node node2,
                                           LinkedList <Node> path, List <List <Node>> paths) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverse(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            allPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> allPathsFromToExcluding(Graph graph, Node node1, Node node2, List <Node> excludes) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        allPathsFromToExcludingVisit(graph, node1, node2, new LinkedList <Node>(), paths, excludes);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void allPathsFromToExcludingVisit(Graph graph, Node node1, Node node2,
                                                    LinkedList <Node> path, List <List <Node>> paths, List <Node> excludes) {
        if (excludes.contains(node1)) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverse(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            allPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> treks(Graph graph, Node node1, Node node2) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        treks(graph, node1, node2, new LinkedList <Node>(), paths);
        return paths;
    }

    private static void treks(Graph graph, Node node1, Node node2,
                              LinkedList <Node> path, List <List <Node>> paths) {
        if (path.contains(node1)) {
            return;
        }

        if (node1 == node2) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node next = Edges.traverse(node1, edge);

            // Must be a directed edge.
            if (!edge.isDirected()) {
                continue;
            }

            // Can't have any colliders on the path.
            if (path.size() > 1) {
                Node node0 = path.get(path.size() - 2);

                if (next == node0) {
                    continue;
                }

                if (graph.isDefCollider(node0, node1, next)) {
                    continue;
                }
            }

            // Found a path.
            if (next == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(next);
                paths.add(_path);
                continue;
            }

            // Nodes may only appear on the path once.
            if (path.contains(next)) {
                continue;
            }

            treks(graph, next, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> treksIncludingBidirected(SemGraph graph, Node node1, Node node2) {
        List <List <Node>> paths = new LinkedList <List <Node>>();
        treksIncludingBidirected(graph, node1, node2, new LinkedList <Node>(), paths);
        return paths;
    }

    private static void treksIncludingBidirected(SemGraph graph, Node node1, Node node2,
                                                 LinkedList <Node> path, List <List <Node>> paths) {
        if (!graph.isShowErrorTerms()) {
            throw new IllegalArgumentException("The SEM Graph must be showing its error terms; this method " +
                    "doesn't traverse two edges between the same nodes well.");
        }

        if (path.contains(node1)) {
            return;
        }

        if (node1 == node2) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node next = Edges.traverse(node1, edge);

            // Must be a directed edge or a bidirected edge.
            if (!(edge.isDirected() || Edges.isBidirectedEdge(edge))) {
                continue;
            }

            // Can't have any colliders on the path.
            if (path.size() > 1) {
                Node node0 = path.get(path.size() - 2);

                if (next == node0) {
                    continue;
                }

                if (graph.isDefCollider(node0, node1, next)) {
                    continue;
                }
            }

            // Found a path.
            if (next == node2) {
                LinkedList <Node> _path = new LinkedList <Node>(path);
                _path.add(next);
                paths.add(_path);
                continue;
            }

            // Nodes may only appear on the path once.
            if (path.contains(next)) {
                continue;
            }

            treksIncludingBidirected(graph, next, node2, path, paths);
        }

        path.removeLast();
    }

    public static List <List <Node>> dConnectingPaths(Graph graph, Node node1, Node node2,
                                                      List <Node> conditioningNodes) {

        List <List <Node>> paths = new LinkedList <List <Node>>();

        Set <Node> conditioningNodesClosure = new HashSet <Node>();

        for (Object conditioningNode : conditioningNodes) {
            doParentClosureVisit(graph, (Node) (conditioningNode),
                    conditioningNodesClosure);
        }

        // Calls the recursive method to discover a d-connecting path from node1
        // to node2, if one exists.  If such a path is found, true is returned;
        // otherwise, false is returned.
        Endpoint incomingEndpoint = null;
        isDConnectedToVisit(graph, node1, incomingEndpoint, node2, new LinkedList <Node>(), paths,
                conditioningNodes, conditioningNodesClosure);

        return paths;
    }

    private static void doParentClosureVisit(Graph graph, Node node, Set <Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Edge edge1 : graph.getEdges(node)) {
                Node sub = Edges.traverseReverseDirected(node, edge1);

                if (sub == null) {
                    continue;
                }

                doParentClosureVisit(graph, sub, closure);
            }
        }
    }

    private static void isDConnectedToVisit(Graph graph, Node currentNode,
                                            Endpoint inEdgeEndpoint, Node node2, LinkedList <Node> path, List <List <Node>> paths,
                                            List <Node> conditioningNodes, Set <Node> conditioningNodesClosure) {
//        System.out.println("Visiting " + currentNode);

        if (currentNode == node2) {
            LinkedList <Node> _path = new LinkedList <Node>(path);
            _path.add(currentNode);
            paths.add(_path);
            return;
        }

//        if (path.size() >= 2) {
//            return;
//        }

//        if (currentNode == node2) {
//            return true;
//        }

        if (path.contains(currentNode)) {
            return;
        }

        path.addLast(currentNode);

        for (Edge edge1 : graph.getEdges(currentNode)) {
            Endpoint outEdgeEndpoint = edge1.getProximalEndpoint(currentNode);

            // Apply the definition of d-connection to determine whether
            // we can pass through on a path from this incoming edge to
            // this outgoing edge through this node.  it all depends
            // on whether this path through the node is a collider or
            // not--that is, whether the incoming endpoint and the outgoing
            // endpoint are both arrow endpoints.
            boolean isCollider = (inEdgeEndpoint == Endpoint.ARROW) &&
                    (outEdgeEndpoint == Endpoint.ARROW);
            boolean passAsCollider = isCollider &&
                    conditioningNodesClosure.contains(currentNode);
            boolean passAsNonCollider =
                    !isCollider && !conditioningNodes.contains(currentNode);

            if (passAsCollider || passAsNonCollider) {
                Node nextNode = Edges.traverse(currentNode, edge1);
                //if (nextNode != null) {
                Endpoint previousEndpoint = edge1.getProximalEndpoint(nextNode);
                isDConnectedToVisit(graph, nextNode, previousEndpoint, node2,
                        path, paths, conditioningNodes, conditioningNodesClosure);
            }
        }

        path.removeLast();
    }

    /**
     * Returns the edges that are in <code>graph1</code> but not in <code>graph2</code>.
     *
     * @param graph1 An arbitrary graph.
     * @param graph2 Another arbitrary graph with the same number of nodes
     *               and node names.
     * @return Ibid.
     */
    public static List <Edge> edgesComplement(Graph graph1, Graph graph2) {
        List <Edge> edges = new ArrayList <Edge>();

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null || !edge1.equals(edge2)) {
                edges.add(edge1);
            }
        }

        return edges;
    }

    /**
     * Returns the edges up to endpoints that are in graph1 but not in graph2.
     *
     * @param graph1 An arbitrary graph.
     * @param graph2 Another arbitrary graph with the same number of nodes
     *               and node names.
     * @return Ibid.
     */
    public static List <Edge> edgesComplementUndirected(Graph graph1, Graph graph2) {
        List <Edge> edges = new ArrayList <Edge>();

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null) {
                edges.add(edge1);
            }
        }

        return edges;
    }

    /**
     * Returns the edges that are in <code>graph1</code> but not in
     * <code>graph2</code>, as a list of undirected edges..
     */
    public static List <Edge> adjacenciesComplement(Graph graph1, Graph graph2) {
        List <Edge> edges = new ArrayList <Edge>();

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

//            if (node21 == null) {
//                continue;
////                throw new IllegalArgumentException("There was no node by that name in the reference graph: " + name1);
//            }
//
//            if (node22 == null) {
//                continue;
////                throw new IllegalArgumentException("There was no node by that name in the reference graph: " + name2);
//            }

            if (node21 == null || node22 == null || !graph2.isAdjacentTo(node21, node22)) {
                edges.add(Edges.nondirectedEdge(edge1.getNode1(), edge1.getNode2()));
            }
        }

        return edges;
    }

    public static List <Edge> adjacenciesComplement2(Graph graph1, Graph graph2) {
        List <Edge> edges = new ArrayList <Edge>();

        List <Node> graph1Nodes = graph1.getNodes();

        for (int i = 0; i < graph1Nodes.size(); i++) {
            for (int j = i + 1; j < graph1Nodes.size(); j++) {
                Node node11 = graph1Nodes.get(i);
                Node node12 = graph1Nodes.get(j);

                if (!graph1.isAdjacentTo(node11, node12)) continue;

                String name1 = node11.getName();
                String name2 = node12.getName();

                Node node21 = graph2.getNode(name1);
                Node node22 = graph2.getNode(name2);

//            if (node21 == null) {
//                continue;
////                throw new IllegalArgumentException("There was no node by that name in the reference graph: " + name1);
//            }
//
//            if (node22 == null) {
//                continue;
////                throw new IllegalArgumentException("There was no node by that name in the reference graph: " + name2);
//            }

                if (node21 == null || node22 == null || !graph2.isAdjacentTo(node21, node22)) {
                    edges.add(Edges.nondirectedEdge(node11, node12));
                }
            }
        }

        return edges;
    }

    public static int arrowEndpointComplement(Graph graph1, Graph graph2) {
        int count = 0;

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

            Edge edge2 = graph2.getEdge(node21, node22);
//
//            if (edge1.getEndpoint1() == Endpoint.ARROW) {
//                if (edge2 == null) {
//                    count++;
//                } else if (edge2.getProximalEndpoint(node21) != Endpoint.ARROW) {
//                    count++;
//                }
//            }
//
//            if (edge1.getEndpoint2() == Endpoint.ARROW) {
//                if (edge2 == null) {
//                    count++;
//                } else if (edge2.getProximalEndpoint(node22) != Endpoint.ARROW) {
//                    count++;
//                }
//            }


            if (edge2 != null) {
                if (edge1.getEndpoint1() == Endpoint.ARROW && edge2.getProximalEndpoint(node21) != Endpoint.ARROW) {
                    count++;
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW && edge2.getProximalEndpoint(node22) != Endpoint.ARROW) {
                    count++;
                }
            } else {
                if (Edges.isBidirectedEdge(edge1)) {
                    count += 2;
                } else if (Edges.isDirectedEdge(edge1)) {
                    count++;
                }
            }
        }


        return count;
    }

    /**
     * Returns the number of directed edges in graph 1 whose orientations are different from the
     * corresponding edges in graph2, when the corresponding edges exist.
     */
    public static int numDifferentOrientationsDirected(Graph graph1, Graph graph2) {
        int errors = 0;

//        for (Edge edge : graph1.getEdges()) {
//            if (!Edges.isDirectedEdge(edge)) {
//                continue;
//            }
//
//            Node node2a = graph2.getNode(edge.getNode1().getName());
//            Node node2b = graph2.getNode(edge.getNode2().getName());
//            Edge edge2 = graph2.getEdge(node2a, node2b);
//
//            Edge edge1Translated = new Edge(node2a, node2b, edge.getEndpoint1(), edge.getEndpoint2());
//
//            if (edge2 != null && !edge2.equals(edge1Translated)) {
//                errors++;
//            }
//        }

        for (Edge edge1 : graph1.getEdges()) {
            Node node21 = graph2.getNode(edge1.getNode1().getName());
            Node node22 = graph2.getNode(edge1.getNode2().getName());

            if (edge1.isDirected() && graph2.isDirectedFromTo(node22, node21)) {
                errors++;
            }
        }

        return errors;
    }

    /**
     * Returns the total number of edges in graph 1 whose orientations are different from the
     * corresponding edges in graph2, when the corresponding edges exist.
     */
    public static int numDifferentOrientations(Graph graph1, Graph graph2) {
        int errors = 0;

        for (Edge edge : graph1.getEdges()) {
            Node node2a = graph2.getNode(edge.getNode1().getName());
            Node node2b = graph2.getNode(edge.getNode2().getName());
            Edge edge2 = graph2.getEdge(node2a, node2b);

            Edge edge1Translated = new Edge(node2a, node2b, edge.getEndpoint1(), edge.getEndpoint2());

            if (edge2 != null && !edge2.equals(edge1Translated)) {
                errors++;
            }
        }

        return errors;
    }

    /**
     * Returns a new graph in which the bidirectred edges of the given
     * graph have been changed to undirected edges.
     */
    public static Graph bidirectedToUndirected(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);

        for (Edge edge : newGraph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                newGraph.removeEdge(edge);
                newGraph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        return newGraph;
    }

    /**
     * Returns a new graph in which the undirectred edges of the given
     * graph have been changed to bidirected edges.
     */
    public static Graph undirectedToBidirected(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);

        for (Edge edge : newGraph.getEdges()) {
            if (Edges.isUndirectedEdge(edge)) {
                newGraph.removeEdge(edge);
                newGraph.addBidirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        return newGraph;
    }

    /**
     * Returns a new graph in which the bidirectred edges of the given
     * graph have been changed to bidirected edges.
     */
    public static Graph bidirectedToTwoCycle(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);

        for (Edge edge : newGraph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                newGraph.removeEdge(edge);
                newGraph.addDirectedEdge(edge.getNode1(), edge.getNode2());
                newGraph.addDirectedEdge(edge.getNode2(), edge.getNode1());
            }
        }

        return newGraph;
    }

    public static String pathString(Graph graph, List <Node> path) {
        return pathString(graph, path, new LinkedList <Node>());
    }

    public static String pathString(Graph graph, List <Node> path, List <Node> conditioningVars) {
        StringBuilder buf = new StringBuilder();

        buf.append(path.get(0).toString());

        if (conditioningVars.contains(path.get(0))) {
            buf.append("(C)");
        }

        for (int m = 1; m < path.size(); m++) {
            Node n0 = path.get(m - 1);
            Node n1 = path.get(m);

            Edge edge = graph.getEdge(n0, n1);

            if (edge == null) {
                buf.append("(-)");
            } else {
                Endpoint endpoint0 = edge.getProximalEndpoint(n0);
                Endpoint endpoint1 = edge.getProximalEndpoint(n1);

                if (endpoint0 == Endpoint.ARROW) {
                    buf.append("<");
                } else if (endpoint0 == Endpoint.TAIL) {
                    buf.append("-");
                } else if (endpoint0 == Endpoint.CIRCLE) {
                    buf.append("o");
                }

                buf.append("-");

                if (endpoint1 == Endpoint.ARROW) {
                    buf.append(">");
                } else if (endpoint1 == Endpoint.TAIL) {
                    buf.append("-");
                } else if (endpoint1 == Endpoint.CIRCLE) {
                    buf.append("o");
                }
            }

            buf.append(n1.toString());

            if (conditioningVars.contains(n1)) {
                buf.append("(C)");
            }
        }
        return buf.toString();
    }

    /**
     * Converts the given graph, <code>originalGraph</code>, to use the new
     * variables (with the same names as the old).
     *
     * @param originalGraph The graph to be converted.
     * @param newVariables  The new variables to use, with the same names as
     *                      the old ones.
     * @return A new, converted, graph.
     */
    public static Graph replaceNodes(Graph originalGraph, List <Node> newVariables) {
        Graph convertedGraph = new EdgeListGraph(newVariables);

        for (Edge edge : originalGraph.getEdges()) {
            Node node1 = convertedGraph.getNode(edge.getNode1().getName());
            Node node2 = convertedGraph.getNode(edge.getNode2().getName());

            if (node1 == null) {
                node1 = edge.getNode1();
                if (!convertedGraph.containsNode(node1)) {
                    convertedGraph.addNode(node1);
                }
            }
            if (node2 == null) {
                node2 = edge.getNode2();
                if (!convertedGraph.containsNode(node2)) {
                    convertedGraph.addNode(node2);
                }
            }

            if (node1 == null) {
                throw new IllegalArgumentException("Couldn't find a node by the name " + edge.getNode1().getName()
                        + " among the new variables for the converted graph (" + newVariables + ").");
            }

            if (node2 == null) {
                throw new IllegalArgumentException("Couldn't find a node by the name " + edge.getNode2().getName()
                        + " among the new variables for the converted graph (" + newVariables + ").");
            }

            Endpoint endpoint1 = edge.getEndpoint1();
            Endpoint endpoint2 = edge.getEndpoint2();
            Edge newEdge = new Edge(node1, node2, endpoint1, endpoint2);
            convertedGraph.addEdge(newEdge);
        }

        return convertedGraph;
    }

    /**
     * Converts the given list of nodes, <code>originalNodes</code>, to use the new
     * variables (with the same names as the old).
     *
     * @param originalNodes The list of nodes to be converted.
     * @param newNodes      A list of new nodes, containing as a subset nodes with
     *                      the same names as those in <code>originalNodes</code>.
     *                      the old ones.
     * @return The converted list of nodes.
     */
    public static List <Node> replaceNodes(List <Node> originalNodes, List <Node> newNodes) {
        List <Node> convertedNodes = new LinkedList <Node>();

        for (Node node : originalNodes) {
            for (Node _node : newNodes) {
                if (node.getName().equals(_node.getName())) {
                    convertedNodes.add(_node);
                    break;
                }
            }
        }

        return convertedNodes;
    }

    public static int countAdjCorrect(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        graph1 = GraphUtils.undirectedGraph(graph1);
        graph2 = GraphUtils.undirectedGraph(graph2);

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List <Node> graph1Nodes = graph1.getNodes();
        List <Node> graph2Nodes = graph2.getNodes();

        Comparator <Node> comparator = new Comparator <Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        List <Edge> edges1 = graph1.getEdges();

        for (Edge edge : edges1) {
            Node node1 = graph2.getNode(edge.getNode1().getName());
            Node node2 = graph2.getNode(edge.getNode2().getName());

            if (graph2.isAdjacentTo(node1, node2)) {
                ++count;
            }
        }

        return count;
    }


    /**
     * Counts the adjacencies that are in graph1 but not in graph2.
     *
     * @throws IllegalArgumentException if graph1 and graph2 are not namewise
     *                                  isomorphic.
     */
    public static int countAdjErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        graph1 = GraphUtils.undirectedGraph(graph1);
        graph2 = GraphUtils.undirectedGraph(graph2);

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List <Node> graph1Nodes = graph1.getNodes();
        List <Node> graph2Nodes = graph2.getNodes();

        Comparator <Node> comparator = new Comparator <Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        List <Edge> edges1 = graph1.getEdges();

        for (Edge edge : edges1) {
            Node node1 = graph2.getNode(edge.getNode1().getName());
            Node node2 = graph2.getNode(edge.getNode2().getName());

            if (!graph2.isAdjacentTo(node1, node2)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Counts the arrowpoints that are in graph1 but not in graph2.
     */
    public static int countArrowptErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List <Node> graph1Nodes = graph1.getNodes();
        List <Node> graph2Nodes = graph2.getNodes();

        Comparator <Node> comparator = new Comparator <Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

//        if (graph1Nodes.size() != graph2Nodes.size()) {
//            throw new IllegalArgumentException(
//                    "The graph sizes are different.");
//        }
//
//        for (int i = 0; i < graph1Nodes.size(); i++) {
//            String name1 = graph1Nodes.get(i).getName();
//            String name2 = graph2Nodes.get(i).getName();
//
//            if (!name1.equals(name2)) {
//                throw new IllegalArgumentException(
//                        "Graph names don't " + "correspond.");
//            }
//        }

        for (Edge edge1 : graph1.getEdges()) {
            Node node11 = edge1.getNode1();
            Node node12 = edge1.getNode2();

            Node node21 = graph2.getNode(node11.getName());
            Node node22 = graph2.getNode(node12.getName());

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null) {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    count++;
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    count++;
                }
            } else {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node21) != Endpoint.ARROW) {
                        count++;
                    }
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node22) != Endpoint.ARROW) {
                        count++;
                    }
                }
            }
        }

//        System.out.println("Arrowpoint errors = " + count);

        return count;
    }

    /**
     * The ratio of the number of directed edges in graph1 that are in graph2.
     */
    public static double strictOrientationPrecision(Graph graph1, Graph graph2) {
        Graph _graph2 = replaceNodes(graph2, graph1.getNodes());

        if (!new HashSet(graph1.getNodes()).equals(new HashSet <Node>(graph2.getNodes()))) {
            throw new IllegalArgumentException("Variables in the two graphs must be the same.");
        }

        int intersection = 0;
        int numGraph1 = 0;

        for (Edge edge : graph1.getEdges()) {
            if (!edge.isDirected()) continue;

            Edge oppositeEdge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(), edge.getEndpoint1());

            // Ignore cycles.
            if (graph1.containsEdge(oppositeEdge)) {
                continue;
            }

            numGraph1++;

            if (_graph2.containsEdge(edge) && !_graph2.containsEdge(oppositeEdge)) {
                intersection++;
            }
        }

        return intersection / (double) numGraph1;
    }

    public static double strictAdjacencyPrecision(Graph graph1, Graph graph2) {
        List <Node> nodes = graph1.getNodes();
        Graph _graph2 = replaceNodes(graph2, nodes);

        if (!new HashSet(nodes).equals(new HashSet <Node>(graph2.getNodes()))) {
            throw new IllegalArgumentException("Variables in the two graphs must be the same.");
        }

        int intersection = 0;
        int numGraph1 = 0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);

                if (graph1.isAdjacentTo(node1, node2)) {
                    numGraph1++;

                    if (_graph2.isAdjacentTo(node1, node2)) {
                        intersection++;
                    }
                }
            }
        }

//        for (Edge edge : graph1.getEdges()) {
//            numGraph1++;
//
//            if (_graph2.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
//                intersection++;
//            }
//        }

        return intersection / (double) numGraph1;
    }

    public static int getNumArrowpts(Graph graph) {
        List <Edge> edges = graph.getEdges();
        int numArrowpts = 0;

        for (Edge edge : edges) {
            if (edge.getEndpoint1() == Endpoint.ARROW) {
                numArrowpts++;
            }
            if (edge.getEndpoint2() == Endpoint.ARROW) {
                numArrowpts++;
            }
        }

//        System.out.println("Num arrowpoints = " + numArrowpts);

        return numArrowpts;
    }

    /**
     * Converts the given list of nodes, <code>originalNodes</code>, to use the
     * replacement nodes for them by the same name in the given <code>graph</code>.
     *
     * @param originalNodes The list of nodes to be converted.
     * @param graph         A graph to be used as a source of new nodes.
     * @return A new, converted, graph.
     */
    public static List <Node> replaceNodes(List <Node> originalNodes, Graph graph) {
        List <Node> convertedNodes = new LinkedList <Node>();

        for (Node node : originalNodes) {
            convertedNodes.add(graph.getNode(node.getName()));
        }

        return convertedNodes;
    }

    public static GraphComparison getGraphComparison(Graph graph, Graph trueGraph) {
        graph = GraphUtils.replaceNodes(graph, trueGraph.getNodes());

//        System.out.println("graph = " + graph);
//        System.out.println("true graph = " + trueGraph);

        int adjFn = GraphUtils.countAdjErrors(trueGraph, graph);
        int adjFp = GraphUtils.countAdjErrors(graph, trueGraph);
        int adjCorrect = graph.getNumEdges() - adjFp;

        int arrowptFn = GraphUtils.countArrowptErrors(trueGraph, graph);
        int arrowptFp = GraphUtils.countArrowptErrors(graph, trueGraph);
        int arrowptCorrect = GraphUtils.getNumArrowpts(graph) - arrowptFp;

        List <Edge> edgesAdded = new ArrayList <Edge>();
        List <Edge> edgesRemoved = new ArrayList <Edge>();
        List <Edge> edgesReorientedFrom = new ArrayList <Edge>();
        List <Edge> edgesReorientedTo = new ArrayList <Edge>();

        for (Edge edge : trueGraph.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                edgesRemoved.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }
//            if (!trueGraph.containsEdge(edge)) {
//                edgesAdded.add(edge);
//            }
        }

        for (Edge edge : graph.getEdges()) {
            if (!trueGraph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                edgesAdded.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }
//            if (!graph.containsEdge(edge)) {
//                edgesRemoved.add(edge);
//            }
        }

        for (Edge edge : trueGraph.getEdges()) {
            if (graph.containsEdge(edge)) {
                continue;
            }

            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            for (Edge _edge : graph.getEdges(node1, node2)) {
                if (edge.equals(_edge)) continue;

                edgesReorientedFrom.add(edge);
                edgesReorientedTo.add(_edge);

//                Edge opposite = new Edge(_edge.getNode1(), _edge.getNode2(), _edge.getEndpoint2(), _edge.getEndpoint1());
//
//                boolean b1 = _edge.equals(edge);
//                boolean b2 = trueGraph.containsEdge(opposite);
//
//                if (b1 && b2) {
//                    edgesReorientedFrom.add(edge);
//                    edgesReorientedTo.add(_edge);
//                }
            }
        }

        return new GraphComparison(
                adjFn, adjFp, adjCorrect, arrowptFn, arrowptFp, arrowptCorrect,
                edgesAdded, edgesRemoved, edgesReorientedFrom, edgesReorientedTo);
    }

    /**
     * Just counts arrowpoint errors--for cyclic edges counts an arrowpoint at each node.
     */
    public static GraphComparison getGraphComparison2(Graph graph, Graph trueGraph) {
        graph = GraphUtils.replaceNodes(graph, trueGraph.getNodes());

//        System.out.println("graph = " + graph);
//        System.out.println("true graph = " + trueGraph);

        int adjFn = GraphUtils.countAdjErrors(trueGraph, graph);
        int adjFp = GraphUtils.countAdjErrors(graph, trueGraph);

        Graph undirectedGraph = undirectedGraph(graph);
        int adjCorrect = undirectedGraph.getNumEdges() - adjFp;

        int arrowptFn = 0;
        int arrowptFp = 0;
        int arrowptCorrect = 0;

//        int arrowptFn = GraphUtils.countArrowptErrors(trueGraph, graph);
//        int arrowptFp = GraphUtils.countArrowptErrors(graph, trueGraph);
//        int arrowptCorrect = GraphUtils.getNumArrowpts(graph) - arrowptFp;

        List <Edge> edgesAdded = new ArrayList <Edge>();
        List <Edge> edgesRemoved = new ArrayList <Edge>();
        List <Edge> edgesReorientedFrom = new ArrayList <Edge>();
        List <Edge> edgesReorientedTo = new ArrayList <Edge>();

        for (Edge edge : trueGraph.getEdges()) {
            if (!graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                edgesRemoved.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }
//            if (!trueGraph.containsEdge(edge)) {
//                edgesAdded.add(edge);
//            }
        }

        for (Edge edge : graph.getEdges()) {
            if (!trueGraph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                edgesAdded.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }
//            if (!graph.containsEdge(edge)) {
//                edgesRemoved.add(edge);
//            }
        }

        List <Node> nodes = trueGraph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                Node x = nodes.get(i);
                Node y = nodes.get(j);

                Node _x = graph.getNode(x.getName());
                Node _y = graph.getNode(y.getName());

                List <Edge> edges = trueGraph.getEdges(x, y);
                List <Edge> _edges = graph.getEdges(_x, _y);

                boolean existsArrow = false;
                boolean _existsArrow = false;

                for (Edge edge : edges) {
                    if (edge.getProximalEndpoint(y) == Endpoint.ARROW) {
                        existsArrow = true;
                        break;
                    }
                }

                for (Edge _edge : _edges) {
                    if (_edge.getProximalEndpoint(y) == Endpoint.ARROW) {
                        _existsArrow = true;
                        break;
                    }
                }

                if (existsArrow && !_existsArrow) {
                    arrowptFn++;
                } else if (!existsArrow && _existsArrow) {
                    arrowptFp++;
                } else if (existsArrow && _existsArrow) {
                    arrowptCorrect++;
                }
            }
        }

        for (Edge edge : trueGraph.getEdges()) {
            if (graph.containsEdge(edge)) {
                continue;
            }

            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            for (Edge _edge : graph.getEdges(node1, node2)) {
                if (edge.equals(_edge)) continue;

                edgesReorientedFrom.add(edge);
                edgesReorientedTo.add(_edge);
            }
        }

        return new GraphComparison(
                adjFn, adjFp, adjCorrect, arrowptFn, arrowptFp, arrowptCorrect,
                edgesAdded, edgesRemoved, edgesReorientedFrom, edgesReorientedTo);
    }

    public static String graphComparisonString(String name1, Graph graph1, String name2, Graph graph2, boolean printStars) {
        StringBuilder builder = new StringBuilder();
        graph2 = replaceNodes(graph2, graph1.getNodes());

        String trueGraphAndTarget = "Comparing " + name1 + " to " + name2;
        builder.append(trueGraphAndTarget + "\n");

        GraphComparison comparison = getGraphComparison(graph1, graph2);

        List <Edge> edgesAdded = comparison.getEdgesAdded();

        builder.append("\nEdges added:");

        if (edgesAdded.isEmpty()) {
            builder.append("\n  --NONE--");
        } else {
            for (int i = 0; i < edgesAdded.size(); i++) {
                Edge edge = edgesAdded.get(i);

                Node node1 = graph1.getNode(edge.getNode1().getName());
                Node node2 = graph1.getNode(edge.getNode2().getName());


                builder.append("\n").append(i + 1).append(". ").append(edge);

                if (printStars) {
                    boolean directedInGraph2 = false;

                    if (Edges.isDirectedEdge(edge) && existsSemidirectedPathFromTo(graph2, node1, node2)) {
                        directedInGraph2 = true;
                    } else if ((Edges.isUndirectedEdge(edge) || Edges.isBidirectedEdge(edge)) &&
                            (existsSemidirectedPathFromTo(graph2, node1, node2) ||
                                    existsSemidirectedPathFromTo(graph2, node2, node1))) {
                        directedInGraph2 = true;
                    }

                    if (directedInGraph2) {
                        builder.append(" *");
                    }
                }

            }
        }

        builder.append("\n\nEdges removed:");
        List <Edge> edgesRemoved = comparison.getEdgesRemoved();

        if (edgesRemoved.isEmpty()) {
            builder.append("\n  --NONE--");
        } else {
            for (int i = 0; i < edgesRemoved.size(); i++) {
                Edge edge = edgesRemoved.get(i);

                Node node1 = graph2.getNode(edge.getNode1().getName());
                Node node2 = graph2.getNode(edge.getNode2().getName());


                builder.append("\n").append(i + 1).append(". ").append(edge);

                if (printStars) {
                    boolean directedInGraph1 = false;

                    if (Edges.isDirectedEdge(edge) && existsSemidirectedPathFromTo(graph1, node1, node2)) {
                        directedInGraph1 = true;
                    } else if ((Edges.isUndirectedEdge(edge) || Edges.isBidirectedEdge(edge)) &&
                            (existsSemidirectedPathFromTo(graph1, node1, node2) ||
                                    existsSemidirectedPathFromTo(graph1, node2, node1))) {
                        directedInGraph1 = true;
                    }

                    if (directedInGraph1) {
                        builder.append(" *");
                    }
                }
            }
        }

        builder.append("\n\n" +
                "Edges reoriented:");
        List <Edge> edgesReorientedFrom = comparison.getEdgesReorientedFrom();
        List <Edge> edgesReorientedTo = comparison.getEdgesReorientedTo();

        if (edgesReorientedFrom.isEmpty()) {
            builder.append("\n  --NONE--");
        } else {
            for (int i = 0; i < edgesReorientedFrom.size(); i++) {
                Edge from = edgesReorientedFrom.get(i);
                Edge to = edgesReorientedTo.get(i);
                builder.append("\n").append(i + 1).append(". ").append(from)
                        .append(" ====> ").append(to);
            }
        }

        return builder.toString();
    }

    /**
     * Sorts a list of edges alphabetically by name.
     */
    public static void sortEdges(List <Edge> edges) {
        Collections.sort(edges, new Comparator <Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                String name11 = o1.getNode1().getName();
                String name12 = o1.getNode2().getName();
                String name21 = o2.getNode1().getName();
                String name22 = o2.getNode2().getName();

                int major = name11.compareTo(name21);
                int minor = name12.compareTo(name22);

                if (major == 0) {
                    return minor;
                } else {
                    return major;
                }
            }
        });
    }

    /**
     * Returns an empty graph with the given number of nodes.
     */
    public static Graph emptyGraph(int numNodes) {
        List <Node> nodes = new ArrayList <Node>();

        for (int i = 0; i < numNodes; i++) {
            nodes.add(new GraphNode("X" + i));
        }

        return new EdgeListGraph(nodes);
    }

    /**
     * Converts a graph to a Graphviz .dot file
     */
    public static String graphToDot(Graph graph) {
        StringBuilder builder = new StringBuilder();

        builder.append("digraph g {\n");
        for (Edge edge : graph.getEdges()) {
            builder.append(" \"" + edge.getNode1() + "\" -> \"" + edge.getNode2() +
                    "\" [arrowtail=");
            if (edge.getEndpoint1() == Endpoint.ARROW)
                builder.append("normal");
            else if (edge.getEndpoint1() == Endpoint.TAIL)
                builder.append("none");
            else if (edge.getEndpoint1() == Endpoint.CIRCLE)
                builder.append("odot");
            builder.append(", arrowhead=");
            if (edge.getEndpoint2() == Endpoint.ARROW)
                builder.append("normal");
            else if (edge.getEndpoint2() == Endpoint.TAIL)
                builder.append("none");
            else if (edge.getEndpoint2() == Endpoint.CIRCLE)
                builder.append("odot");
            builder.append("]; \n");
        }

        builder.append("}");
        return builder.toString();
    }

    public static void graphToDot(Graph graph, File file) {
        try {
            Writer writer = new FileWriter(file);
            writer.write(graphToDot(graph));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an XML element representing the given graph. (Well, only a
     * basic graph for now...)
     */
    public static Element convertToXml(Graph graph) {
        Element element = new Element("graph");

        Element variables = new Element("variables");
        element.appendChild(variables);

        for (Node node : graph.getNodes()) {
            Element variable = new Element("variable");
            Text text = new Text(node.getName());
            variable.appendChild(text);
            variables.appendChild(variable);
        }

        Element edges = new Element("edges");
        element.appendChild(edges);

        for (Edge edge : graph.getEdges()) {
            Element _edge = new Element("edge");
            Text text = new Text(edge.toString());
            _edge.appendChild(text);
            edges.appendChild(_edge);
        }

        Set <Triple> ambiguousTriples = graph.getAmbiguousTriples();

        if (!ambiguousTriples.isEmpty()) {
            Element underlinings = new Element("ambiguities");
            element.appendChild(underlinings);

            for (Triple triple : ambiguousTriples) {
                Element underlining = new Element("ambiguities");
                Text text = new Text(niceTripleString(triple));
                underlining.appendChild(text);
                underlinings.appendChild(underlining);
            }
        }

        Set <Triple> underlineTriples = graph.getUnderLines();

        if (!underlineTriples.isEmpty()) {
            Element underlinings = new Element("underlines");
            element.appendChild(underlinings);

            for (Triple triple : underlineTriples) {
                Element underlining = new Element("underline");
                Text text = new Text(niceTripleString(triple));
                underlining.appendChild(text);
                underlinings.appendChild(underlining);
            }
        }

        Set <Triple> dottedTriples = graph.getDottedUnderlines();

        if (!dottedTriples.isEmpty()) {
            Element dottedUnderlinings = new Element("dottedUnderlines");
            element.appendChild(dottedUnderlinings);

            for (Triple triple : dottedTriples) {
                Element dottedUnderlining = new Element("dottedUnderline");
                Text text = new Text(niceTripleString(triple));
                dottedUnderlining.appendChild(text);
                dottedUnderlinings.appendChild(dottedUnderlining);
            }
        }

        return element;
    }

    private static String niceTripleString(Triple triple) {
        return triple.getX() + ", " + triple.getY() + ", " + triple.getZ();
    }

    public static String graphToXml(Graph graph) {
        Document document = new Document(convertToXml(graph));
        OutputStream out = new ByteArrayOutputStream();
        Serializer serializer = new Serializer(out);
        serializer.setLineSeparator("\n");
        serializer.setIndent(2);

        try {
            serializer.write(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out.toString();
    }

    public static Graph parseGraphXml(Element graphElement, Map <String, Node> nodes) throws ParsingException {
        if (!"graph".equals(graphElement.getLocalName())) {
            throw new IllegalArgumentException("Expecting graph element: " + graphElement.getLocalName());
        }

        Attribute noteAttribute = graphElement.getAttribute("note");

        if (!("variables".equals(graphElement.getChildElements().get(0).getLocalName()))) {
            throw new ParsingException("Expecting variables element: " +
                    graphElement.getChildElements().get(0).getLocalName());
        }

        Element variablesElement = graphElement.getChildElements().get(0);
        Elements variableElements = variablesElement.getChildElements();
        List <Node> variables = new ArrayList <Node>();

        for (int i = 0; i < variableElements.size(); i++) {
            Element variableElement = variableElements.get(i);

            if (!("variable".equals(variablesElement.getChildElements().get(i).getLocalName()))) {
                throw new ParsingException("Expecting variable element.");
            }

            String value = variableElement.getValue();

            if (nodes == null) {
                variables.add(new GraphNode(value));
            } else {
                variables.add(nodes.get(value));
            }
        }

        Graph graph = new EdgeListGraph(variables);

//        graphNotes.add(noteAttribute.getValue());

        if (!("edges".equals(graphElement.getChildElements().get(1).getLocalName()))) {
            throw new ParsingException("Expecting edges element.");
        }

        Element edgesElement = graphElement.getChildElements().get(1);
        Elements edgesElements = edgesElement.getChildElements();

        for (int i = 0; i < edgesElements.size(); i++) {
            Element edgeElement = edgesElements.get(i);

            if (!("edge".equals(edgeElement.getLocalName()))) {
                throw new ParsingException("Expecting edge element: " + edgeElement.getLocalName());
            }

            String value = edgeElement.getValue();

//            System.out.println("value = " + value);

//            String regex = "([A-Za-z0-9_-]*) ?(.)-(.) ?([A-Za-z0-9_-]*)";
            String regex = "([A-Za-z0-9_-]*:?[A-Za-z0-9_-]*) ?(.)-(.) ?([A-Za-z0-9_-]*:?[A-Za-z0-9_-]*)";
//            String regex = "([A-Za-z0-9_-]*) ?([<o])-([o>]) ?([A-Za-z0-9_-]*)";

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);

            if (!matcher.matches()) {
                throw new ParsingException("Edge doesn't match pattern.");
            }

            String var1 = matcher.group(1);
            String leftEndpoint = matcher.group(2);
            String rightEndpoint = matcher.group(3);
            String var2 = matcher.group(4);

            Node node1 = graph.getNode(var1);
            Node node2 = graph.getNode(var2);
            Endpoint endpoint1;

            if (leftEndpoint.equals("<")) {
                endpoint1 = Endpoint.ARROW;
            } else if (leftEndpoint.equals("o")) {
                endpoint1 = Endpoint.CIRCLE;
            } else if (leftEndpoint.equals("-")) {
                endpoint1 = Endpoint.TAIL;
            } else {
                throw new IllegalStateException("Expecting an endpoint: " + leftEndpoint);
            }

            Endpoint endpoint2;

            if (rightEndpoint.equals(">")) {
                endpoint2 = Endpoint.ARROW;
            } else if (rightEndpoint.equals("o")) {
                endpoint2 = Endpoint.CIRCLE;
            } else if (rightEndpoint.equals("-")) {
                endpoint2 = Endpoint.TAIL;
            } else {
                throw new IllegalStateException("Expecting an endpoint: " + rightEndpoint);
            }

            Edge edge = new Edge(node1, node2, endpoint1, endpoint2);
            graph.addEdge(edge);
        }


        int size = graphElement.getChildElements().size();
        if (2 >= size) return graph;

        int p = 2;

        if ("ambiguities".equals(graphElement.getChildElements().get(p).getLocalName())) {
            Element ambiguitiesElement = graphElement.getChildElements().get(p);
            Set <Triple> triples = parseTriples(variables, ambiguitiesElement, "ambiguity");
            graph.setAmbiguousTriples(triples);
            p++;
        }

        if (p >= size) return graph;

        if ("underlines".equals(graphElement.getChildElements().get(p).getLocalName())) {
            Element ambiguitiesElement = graphElement.getChildElements().get(p);
            Set <Triple> triples = parseTriples(variables, ambiguitiesElement, "underline");
            graph.setUnderLineTriples(triples);
            p++;
        }

        if (p >= size) return graph;

        if ("dottedunderlines".equals(graphElement.getChildElements().get(p).getLocalName())) {
            Element ambiguitiesElement = graphElement.getChildElements().get(p);
            Set <Triple> triples = parseTriples(variables, ambiguitiesElement, "dottedunderline");
            graph.setDottedUnderLineTriples(triples);
        }

        return graph;
    }

    /**
     * A triples element has a list of three (comman separated) nodes as text.
     */
    private static Set <Triple> parseTriples(List <Node> variables, Element triplesElement, String s) {
        Elements elements = triplesElement.getChildElements(s);

        Set <Triple> triples = new HashSet <Triple>();

        for (int q = 0; q < elements.size(); q++) {
            Element tripleElement = elements.get(q);
            String value = tripleElement.getValue();

            String[] tokens = value.split(",");

            if (tokens.length != 3) {
                throw new IllegalArgumentException("Expecting a triple: " + value);
            }

            String x = tokens[0].trim();
            String y = tokens[1].trim();
            String z = tokens[2].trim();

            Node _x = getNode(variables, x);
            Node _y = getNode(variables, y);
            Node _z = getNode(variables, z);

            Triple triple = new Triple(_x, _y, _z);
            triples.add(triple);
        }
        return triples;
    }

    private static Node getNode(List <Node> nodes, String x) {
        for (Node node : nodes) {
            if (x.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    public static Element getRootElement(File file) throws ParsingException, IOException {
        Builder builder = new Builder();
        Document document = builder.build(file);
        return document.getRootElement();
    }

    /**
     * @param graph The graph to be saved.
     * @param file  The file to save it in.
     * @param xml   True if to be saved in XML, false if in text.
     * @return I have no idea whey I'm returning this; it's already closed...
     */
    public static PrintWriter saveGraph(Graph graph, File file, boolean xml) {
        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
//            out.print(graph);

            if (xml) {
                out.print(graphToXml(graph));
            } else {
                out.println(graph);
            }
            out.close();
        } catch (IOException e1) {
            throw new IllegalArgumentException(
                    "Output file could not " + "be opened: " + file);
        }
        return out;
    }

    public static Graph loadGraph(File file) {
//        if (!file.getName().endsWith(".xml")) {
//            throw new IllegalArgumentException("Not an XML file.");
//        }

        Element root;
        Graph graph = null;

        try {
            root = getRootElement(file);
            graph = parseGraphXml(root, null);
        } catch (ParsingException e1) {
            throw new IllegalArgumentException("Could not parse " + file, e1);
        } catch (IOException e1) {
            throw new IllegalArgumentException("Could not read " + file, e1);
        }

        if (graph == null) {
            throw new IllegalArgumentException("Expecting a graph in " + file);
        }
        return graph;
    }

    public static Graph loadGraphTxt(File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            while (!in.readLine().trim().equals("Graph Nodes:")) ;

            String line;
            Graph graph = new EdgeListGraph();

            while (!(line = in.readLine().trim()).equals("")) {
                String[] tokens = line.split(" ");

                for (String token : tokens) {
                    graph.addNode(new GraphNode(token));
                }
            }

            while (!in.readLine().trim().equals("Graph Edges:")) ;

            while (!(line = in.readLine().trim()).equals("")) {
                String[] tokens = line.split(" ");

                String from = tokens[1];
                String to = tokens[3];
                String edge = tokens[2];

                Node _from = graph.getNode(from);
                Node _to = graph.getNode(to);

                char end1 = edge.charAt(0);
                char end2 = edge.charAt(2);

                Endpoint _end1, _end2;

                if (end1 == '<') {
                    _end1 = Endpoint.ARROW;
                } else if (end1 == 'o') {
                    _end1 = Endpoint.CIRCLE;
                } else if (end1 == '-') {
                    _end1 = Endpoint.TAIL;
                } else {
                    throw new IllegalArgumentException();
                }

                if (end2 == '>') {
                    _end2 = Endpoint.ARROW;
                } else if (end2 == 'o') {
                    _end2 = Endpoint.CIRCLE;
                } else if (end2 == '-') {
                    _end2 = Endpoint.TAIL;
                } else {
                    throw new IllegalArgumentException();
                }

                Edge _edge = new Edge(_from, _to, _end1, _end2);

                graph.addEdge(_edge);
            }

            return graph;

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalStateException();
    }

    public static HashMap <String, PointXy> grabLayout(List <Node> nodes) {
        HashMap <String, PointXy> layout = new HashMap <String, PointXy>();

        for (Node node : nodes) {
            layout.put(node.getName(), new PointXy(node.getCenterX(), node.getCenterY()));
        }

        return layout;
    }

    /**
     * @return A list of triples of the form X*->Y<-*Z.
     */
    public static List <Triple> getCollidersFromGraph(Node node, Graph graph) {
        List <Triple> colliders = new ArrayList <Triple>();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            Endpoint endpt1 = graph.getEdge(x, node).getProximalEndpoint(node);
            Endpoint endpt2 = graph.getEdge(z, node).getProximalEndpoint(node);

            if (endpt1 == Endpoint.ARROW && endpt2 == Endpoint.ARROW) {
                colliders.add(new Triple(x, node, z));
            }
        }

        return colliders;
    }

    /**
     * @return A list of triples of the form X*->Y<-*Z.
     */
    public static List <Triple> getDefiniteCollidersFromGraph(Node node, Graph graph) {
        List <Triple> defColliders = new ArrayList <Triple>();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            if (graph.isDefCollider(x, node, z)) {
                defColliders.add(new Triple(x, node, z));
            }
        }

        return defColliders;
    }

    /**
     * @return A list of triples of the form <X, Y, Z>, where <X, Y, Z> is a definite noncollider
     * in the given graph.
     */
    public static List <Triple> getNoncollidersFromGraph(Node node, Graph graph) {
        List <Triple> noncolliders = new ArrayList <Triple>();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            Endpoint endpt1 = graph.getEdge(x, node).getProximalEndpoint(node);
            Endpoint endpt2 = graph.getEdge(z, node).getProximalEndpoint(node);

            if (endpt1 == Endpoint.ARROW && endpt2 == Endpoint.TAIL
                    || endpt1 == Endpoint.TAIL && endpt2 == Endpoint.ARROW
                    || endpt1 == Endpoint.TAIL && endpt2 == Endpoint.TAIL) {
                noncolliders.add(new Triple(x, node, z));
            }
        }

        return noncolliders;
    }

    /**
     * @return A list of triples of the form <X, Y, Z>, where <X, Y, Z> is a definite noncollider
     * in the given graph.
     */
    public static List <Triple> getDefiniteNoncollidersFromGraph(Node node, Graph graph) {
        List <Triple> defNoncolliders = new ArrayList <Triple>();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            if (graph.isDefNoncollider(x, node, z)) {
                defNoncolliders.add(new Triple(x, node, z));
            }
        }

        return defNoncolliders;
    }


    /**
     * @return A list of triples of the form <X, Y, Z>, where <X, Y, Z> is a definite noncollider
     * in the given graph.
     */
    public static List <Triple> getAmbiguousTriplesFromGraph(Node node, Graph graph) {
        List <Triple> ambiguousTriples = new ArrayList <Triple>();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            if (graph.isAmbiguousTriple(x, node, z)) {
                ambiguousTriples.add(new Triple(x, node, z));
            }
        }

        return ambiguousTriples;
    }

    /**
     * @return A list of triples of the form <X, Y, Z>, where <X, Y, Z> is a definite noncollider
     * in the given graph.
     */
    public static List <Triple> getUnderlinedTriplesFromGraph(Node node, Graph graph) {
        List <Triple> underlinedTriples = new ArrayList <Triple>();
        Set <Triple> allUnderlinedTriples = graph.getUnderLines();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            if (allUnderlinedTriples.contains(new Triple(x, node, z))) {
                underlinedTriples.add(new Triple(x, node, z));
            }
        }

        return underlinedTriples;
    }

    /**
     * @return A list of triples of the form <X, Y, Z>, where <X, Y, Z> is a definite noncollider
     * in the given graph.
     */
    public static List <Triple> getDottedUnderlinedTriplesFromGraph(Node node, Graph graph) {
        List <Triple> dottedUnderlinedTriples = new ArrayList <Triple>();
        Set <Triple> allDottedUnderlinedTriples = graph.getDottedUnderlines();

        List <Node> adj = graph.getAdjacentNodes(node);
        if (adj.size() < 2) return new LinkedList <Triple>();

        ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = adj.get(choice[0]);
            Node z = adj.get(choice[1]);

            if (allDottedUnderlinedTriples.contains(new Triple(x, node, z))) {
                dottedUnderlinedTriples.add(new Triple(x, node, z));
            }
        }

        return dottedUnderlinedTriples;
    }

    public static Matrix graphToMatrix(Graph graph) {
        // initialize matrix
        int n = graph.getNumNodes();
        Matrix matrix = new DenseMatrix(n, n);
        matrix.zero();

        // map node names in order of appearance
        HashMap <Node, Integer> map = new HashMap <Node, Integer>();
        int i = 0;
        for (Node node : graph.getNodes()) {
            map.put(node, i);
            i++;
        }

        // mark edges
        for (Edge edge : graph.getEdges()) {
            // if directed find which is parent/child
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();
            if (edge.isDirected()) {
                if (edge.pointsTowards(node1)) {
                    matrix.set(map.get(node2), map.get(node1), -1);
                }
                if (edge.pointsTowards(node2)) {
                    matrix.set(map.get(node1), map.get(node2), -1);
                }
            } else {
                matrix.set(map.get(node1), map.get(node2), 1);
                matrix.set(map.get(node2), map.get(node1), 1);
            }
        }
        return matrix;
    }

    /**
     * Represents straight-out adjacencies for any graph. a[i][j] = 1 just in case there is an
     * edge from i to j in the graph.
     */
    public static int[][] adjacencyMatrix(Graph graph) {
        List <Node> nodes = graph.getNodes();
        int[][] m = new int[nodes.size()][nodes.size()];

        for (Edge edge : graph.getEdges()) {
            if (!Edges.isDirectedEdge(edge)) {
                throw new IllegalArgumentException("Incidence matrix is for directed graphs.");
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                Node x1 = nodes.get(i);
                Node x2 = nodes.get(j);
                Edge edge = graph.getEdge(x1, x2);

                if (edge == null) {
                    m[i][j] = 0;
                } else {
                    m[i][j] = 1;
                }
            }
        }

        return m;
    }

    /**
     * A standard matrix graph representation for directed graphs. a[i][j] = 1 is j-->i and -1 if i-->j.
     *
     * @throws IllegalArgumentException if <code>graph</code> is not a directed graph.
     */
    public static int[][] incidenceMatrix(Graph graph) throws IllegalArgumentException {
        List <Node> nodes = graph.getNodes();
        int[][] m = new int[nodes.size()][nodes.size()];

        for (Edge edge : graph.getEdges()) {
            if (!Edges.isDirectedEdge(edge)) {
                throw new IllegalArgumentException("Not a directed graph.");
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                Node x1 = nodes.get(i);
                Node x2 = nodes.get(j);
                Edge edge = graph.getEdge(x1, x2);

                if (edge == null) {
                    m[i][j] = 0;
                } else if (edge.getProximalEndpoint(x1) == Endpoint.ARROW) {
                    m[i][j] = 1;
                } else if (edge.getProximalEndpoint(x1) == Endpoint.TAIL) {
                    m[i][j] = -1;
                }
            }
        }

        return m;
    }

    /**
     * Returns a matrix suitable for reading into R--e.g.
     * <pre>
     *   V1 V2 V3 V4 V5
     * 1  0  1  0  0 -1
     * 2 -1  0 -1 -1 -1
     * 3  0  1  0 -1  0
     * 4  0  1  1  0  0
     * 5  1  1  0  0  0
     * </pre>
     *
     * @param graph
     * @return
     */
    public static String rMatrix(Graph graph) throws IllegalArgumentException {
        int[][] m = incidenceMatrix(graph);

        TextTable table = new TextTable(m[0].length + 1, m.length + 1);

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                table.setToken(i + 1, j + 1, m[i][j] + "");
            }
        }

        for (int i = 0; i < m.length; i++) {
            table.setToken(i + 1, 0, (i + 1) + "");
        }

        List <Node> nodes = graph.getNodes();

        for (int j = 0; j < m[0].length; j++) {
            table.setToken(0, j + 1, nodes.get(j).getName());
        }

        return table.toString();

    }

    public static boolean containsBidirectedEdge(Graph graph) {
        boolean containsBidirected = false;

        for (Edge edge : graph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                containsBidirected = true;
                break;
            }
        }
        return containsBidirected;
    }

    public static boolean existsDirectedPathFromTo(Graph graph, Node node1, Node node2) {
        return existsDirectedPathVisit(graph, node1, node2, new LinkedList <Node>());
    }

    public static boolean existsSemidirectedPathFromTo(Graph graph, Node node1, Node node2) {
        return existsSemiDirectedPathVisit(graph, node1, node2, new LinkedList <Node>());
    }

    public static boolean existsDirectedPathVisit(Graph graph, Node node1, Node node2,
                                                  LinkedList <Node> path) {
//        if (path.size() >= 3) {
//            return false;
//        }

        path.addLast(node1);
        Node previous = null;
        if (path.size() > 1) previous = path.get(path.size() - 2);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

//            if (previous != null && graph.isAmbiguousTriple(previous, node1, child)) {
//                continue;
//            }

            if (existsDirectedPathVisit(graph, child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    /**
     * @return true iff there is a semi-directed path from node1 to node2
     */
    public static boolean existsSemiDirectedPathVisit(Graph graph, Node node1, Node node2,
                                                      LinkedList <Node> path) {
//        if (path.size() >= 3) {
//            return false;
//        }

        path.addLast(node1);
        Node previous = null;
        if (path.size() > 1) previous = path.get(path.size() - 2);

        for (Edge edge : graph.getEdges(graph.getNode(node1.getName()))) {
            Node child = Edges.traverseSemiDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (previous != null && graph.isAmbiguousTriple(previous, node1, child)) {
                continue;
            }

            if (existsSemiDirectedPathVisit(graph, child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    public static LinkedList <Triple> listTriples(Graph graph) {
        LinkedList <Triple> triples = new LinkedList <Triple>();

        for (Node node : graph.getNodes()) {
            List <Node> adj = graph.getAdjacentNodes(node);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List <Node> others = asList(choice, adj);
                triples.add(new Triple(others.get(0), node, others.get(1)));
            }
        }
        return triples;
    }

    public static LinkedList <Triple> listColliderTriples(Graph graph) {
        LinkedList <Triple> colliders = new LinkedList <Triple>();

        for (Node node : graph.getNodes()) {
            List <Node> adj = graph.getAdjacentNodes(node);

            if (adj.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List <Node> others = asList(choice, adj);

                if (graph.isDefCollider(others.get(0), node, others.get(1))) {
                    colliders.add(new Triple(others.get(0), node, others.get(1)));
                }
            }
        }
        return colliders;
    }

    public static int getDegree(Graph graph) {
        int max = 0;

        for (Node node : graph.getNodes()) {
            if (graph.getAdjacentNodes(node).size() > max) {
                max = graph.getAdjacentNodes(node).size();
            }
        }

        return max;
    }

    public static Graph newNodes(Graph graph) {
        List <Node> newNodes = new ArrayList <Node>();

        for (Node node : graph.getNodes()) {
            Node _node = node.like(node.getName());
            _node.setNodeType(node.getNodeType());

            newNodes.add(_node);
        }

        return GraphUtils.replaceNodes(graph, newNodes);
    }

    /**
     * Constructs a list of nodes from the given <code>nodes</code> list at the
     * given indices in that list.
     *
     * @param indices The indices of the desired nodes in <code>nodes</code>.
     * @param nodes   The list of nodes from which we select a sublist.
     * @return the The sublist selected.
     */
    public static List <Node> asList(int[] indices, List <Node> nodes) {
        List <Node> list = new LinkedList <Node>();

        for (int i : indices) {
            list.add(nodes.get(i));
        }

        return list;
    }

    public static Set <Node> asSet(int[] indices, List <Node> nodes) {
        Set <Node> set = new HashSet <Node>();

        for (int i : indices) {
            set.add(nodes.get(i));
        }

        return set;
    }

    public static List <Edge> asListEdge(int[] indices, List <Edge> nodes) {
        List <Edge> list = new LinkedList <Edge>();

        for (int i : indices) {
            list.add(nodes.get(i));
        }

        return list;
    }

    public static int numDirectionalErrors(Graph result, Graph pattern) {
        int count = 0;

        for (Edge edge : result.getEdges()) {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            Node _node1 = pattern.getNode(node1.getName());
            Node _node2 = pattern.getNode(node2.getName());

            Edge _edge = pattern.getEdge(_node1, _node2);

            if (_edge == null) continue;

            if (Edges.isDirectedEdge(edge)) {
                if (_edge.pointsTowards(_node1)) {
                    count++;
                } else if (Edges.isUndirectedEdge(_edge)) {
                    count++;
                }
            }

//            else if (Edges.isBidirectedEdge(edge)) {
//                count++;
//            }
        }

        return count;
    }

    public static int numBidirected(Graph result) {
        int numBidirected = 0;

        for (Edge edge : result.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) numBidirected++;
        }

        return numBidirected;
    }

    public static int degree(Graph graph) {
        int maxDegree = 0;

        for (Node node : graph.getNodes()) {
            int n = graph.getAdjacentNodes(node).size();
            if (n > maxDegree) maxDegree = n;
        }

        return maxDegree;
    }

    public static class GraphComparison {
        private int adjFn;
        private int adjFp;
        private int adjCorrect;
        private int arrowptFn;
        private int arrowptFp;
        private int arrowptCorrect;

        private List <Edge> edgesAdded;
        private List <Edge> edgesRemoved;
        private List <Edge> edgesReorientedFrom;
        private List <Edge> edgesReorientedTo;

        public GraphComparison(int adjFn, int adjFp, int adjCorrect,
                               int arrowptFn, int arrowptFp, int arrowptCorrect,
                               List <Edge> edgesAdded, List <Edge> edgesRemoved,
                               List <Edge> edgesReorientedFrom,
                               List <Edge> edgesReorientedTo) {
            this.adjFn = adjFn;
            this.adjFp = adjFp;
            this.adjCorrect = adjCorrect;
            this.arrowptFn = arrowptFn;
            this.arrowptFp = arrowptFp;
            this.arrowptCorrect = arrowptCorrect;
            this.edgesAdded = edgesAdded;
            this.edgesRemoved = edgesRemoved;
            this.edgesReorientedFrom = edgesReorientedFrom;
            this.edgesReorientedTo = edgesReorientedTo;
        }

        public int getAdjFn() {
            return adjFn;
        }

        public int getAdjFp() {
            return adjFp;
        }

        public int getAdjCorrect() {
            return adjCorrect;
        }

        public int getArrowptFn() {
            return arrowptFn;
        }

        public int getArrowptFp() {
            return arrowptFp;
        }

        public int getArrowptCorrect() {
            return arrowptCorrect;
        }

        public List <Edge> getEdgesAdded() {
            return edgesAdded;
        }

        public List <Edge> getEdgesRemoved() {
            return edgesRemoved;
        }

        public List <Edge> getEdgesReorientedFrom() {
            return edgesReorientedFrom;
        }

        public List <Edge> getEdgesReorientedTo() {
            return edgesReorientedTo;
        }
    }
}


