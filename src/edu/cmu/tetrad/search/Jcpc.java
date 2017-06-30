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

import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * Implements the ICPC algorithm.
 *
 * @author Joseph Ramsey (this version).
 */
public class Jcpc implements GraphSearch {
    private int numAdded;
    private int numRemoved;

    public enum PathBlockingSet {
        LARGE, SMALL
    }

    private PathBlockingSet pathBlockingSet = PathBlockingSet.LARGE;

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private IKnowledge knowledge = new Knowledge();

    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * The maximum number of adjacencies that may ever be added to any node. (Note, the initial search may already have
     * greater degree.)
     */
    private int maxAdjacencies = 8;

    /**
     * The maximum number of iterations of the algorithm, in the major loop.
     */
    private int maxIterations = 20;

    /**
     * True if the algorithm should be started from an empty graph.
     */
    private boolean startFromEmptyGraph = false;

    /**
     * The maximum length of a descendant path. Descendant paths must be checked in the common collider search.
     */
    private int maxDescendantPath = 20;

    /**
     * An initial graph, if there is one.
     */
    private Graph initialGraph;

    /**
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();


    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;

    private int pcDepth = -1;

    private int orientationDepth = 3;

    //=============================CONSTRUCTORS==========================//

    /**
     * Constructs a JPC search with the given independence oracle.
     */
    public Jcpc(IndependenceTest independenceTest) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
    }

    //==============================PUBLIC METHODS========================//

    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public IKnowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(IKnowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    @Override
	public long getElapsedTime() {
        return elapsedTime;
    }

    public int getMaxAdjacencies() {
        return maxAdjacencies;
    }

    /**
     * Sets the maximum number of adjacencies.
     *
     * @param maxAdjacencies
     */
    public void setMaxAdjacencies(int maxAdjacencies) {
        if (maxAdjacencies < 1) {
            throw new IllegalArgumentException("Max adjacencies needs to be at " +
                    "least one, preferably at least 3");
        }

        this.maxAdjacencies = maxAdjacencies;
    }

    public List<Node> getSemidirectedDescendants(Graph graph, List<Node> nodes) {
        HashSet<Node> descendants = new HashSet<Node>();

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            collectSemidirectedDescendantsVisit(graph, node, descendants);
        }

        return new LinkedList<Node>(descendants);
    }

    public void setStartFromEmptyGraph(boolean startFromEmptyGraph) {
        this.startFromEmptyGraph = startFromEmptyGraph;
    }

    public int getMaxDescendantPath() {
        return maxDescendantPath;
    }

    /**
     * Set to 0 to turn off cycle checking.
     */
    public void setMaxDescendantPath(int maxDescendantPath) {
        this.maxDescendantPath = maxDescendantPath;
    }

    public PathBlockingSet getPathBlockingSet() {
        return pathBlockingSet;
    }

    public void setPathBlockingSet(PathBlockingSet pathBlockingSet) {
        if (pathBlockingSet == null) throw new NullPointerException();
        this.pathBlockingSet = pathBlockingSet;
    }


    /**
     * Runs PC starting with a fully connected graph over all of the variables in the domain of the independence test.
     */
    @Override
	public Graph search() {
        long time1 = System.currentTimeMillis();

        List<Graph> graphs = new ArrayList<Graph>();
        IndependenceTest test = getIndependenceTest();

        Graph graph;

        if (startFromEmptyGraph) {
            graph = new EdgeListGraph(test.getVariables());
        } else {
            if (initialGraph != null) {
                graph = initialGraph;
            } else {
                Cpc2 search = new Cpc2(test);
                search.setKnowledge(getKnowledge());
                search.setDepth(getPcDepth());
                search.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
                graph = search.search();
            }
        }

        // This makes a list of all possible edges.
        List<Node> _changedNodes = graph.getNodes();
        Set<Node> changedNodes = new HashSet<Node>();

        boolean changed = true;
        int count = -1;

        int storedMaxAdjacencies = getMaxAdjacencies();

        LOOP:
        while (changed && ++count < getMaxIterations()) {
//            System.out.println("\nRound = " + (count + 1));

            if (this.startFromEmptyGraph && count < 2) {
                setMaxAdjacencies(4);
            } else {
                setMaxAdjacencies(storedMaxAdjacencies);
            }

            TetradLogger.getInstance().log("info", "Round = " + (count + 1));
            System.out.println("Round = " + (count + 1));
            numAdded = 0;
            numRemoved = 0;
            int index = 0;

//            Graph oldGraph = new EdgeListGraph(graph);
            Graph oldGraph = graph;
            int indexBackwards = 0;
            int numEdgesBackwards = graph.getNumEdges();

            for (Edge edge : graph.getEdges()) {
                if (++indexBackwards % 10000 == 0) {
                    TetradLogger.getInstance().log("info", index + " of " + numEdgesBackwards);
                    System.out.println(index + " of " + numEdgesBackwards);
                }

                Node x = edge.getNode1();
                Node y = edge.getNode2();

                List<Node> sepsetX, sepsetY;
                boolean existsSepset = false;

                if (getPathBlockingSet() == PathBlockingSet.LARGE) {
                    sepsetX = pathBlockingSet(test, oldGraph, x, y);

                    if (sepsetX != null) {
                        existsSepset = true;
                    } else {
                        sepsetY = pathBlockingSet(test, oldGraph, y, x);

                        if (sepsetY != null) {
                            existsSepset = true;
                        }
                    }
                } else if (getPathBlockingSet() == PathBlockingSet.SMALL) {
                    sepsetX = pathBlockingSetSmall(test, oldGraph, x, y);
                    sepsetY = pathBlockingSetSmall(test, oldGraph, y, x);
                    existsSepset = sepsetX != null || sepsetY != null;
                } else {
                    throw new IllegalStateException("Unrecognized sepset type.");
                }

                if (existsSepset) {
                    if (!getKnowledge().noEdgeRequired(x.getName(), y.getName())) {
                        continue;
                    }

                    appendChangedNodes(graph, changedNodes, x, y);
                    graph.removeEdge(edge);
                    numRemoved++;
                }
            }

            oldGraph = new EdgeListGraph(graph);

            int numEdges = _changedNodes.size() * (_changedNodes.size() - 1) / 2;

            for (int i = 0; i < _changedNodes.size(); i++) {
                for (int j = i + 1; j < _changedNodes.size(); j++) {
                    index++;

                    if (index % 10000 == 0) {
                        TetradLogger.getInstance().log("info", index + " of " + numEdges);
                        System.out.println(index + " of " + numEdges);
                    }

                    Node x = _changedNodes.get(i);
                    Node y = _changedNodes.get(j);

                    if (graph.isAdjacentTo(x, y)) {
                        continue;
                    }

                    if (oldGraph.getAdjacentNodes(x).isEmpty() && oldGraph.getAdjacentNodes(y).isEmpty()) {
                        continue;
                    }

                    List<Node> sepsetX, sepsetY;
                    boolean existsSepset = false;

                    if (getPathBlockingSet() == PathBlockingSet.LARGE) {
                        sepsetX = pathBlockingSet(test, oldGraph, x, y);

                        if (sepsetX != null) {
                            existsSepset = true;
                        } else {
                            sepsetY = pathBlockingSet(test, oldGraph, y, x);

                            if (sepsetY != null) {
                                existsSepset = true;
                            }
                        }
                    } else if (getPathBlockingSet() == PathBlockingSet.SMALL) {
                        sepsetX = pathBlockingSetSmall(test, oldGraph, x, y);
                        sepsetY = pathBlockingSetSmall(test, oldGraph, y, x);
                        existsSepset = sepsetX != null || sepsetY != null;
                    } else {
                        throw new IllegalStateException("Unrecognized sepset type.");
                    }


                    if (!existsSepset) {
                        if (graph.getAdjacentNodes(x).size() >= getMaxAdjacencies()) {
                            continue;
                        }

                        if (graph.getAdjacentNodes(y).size() >= getMaxAdjacencies()) {
                            continue;
                        }

                        if (getKnowledge().edgeForbidden(x.getName(), y.getName()) && getKnowledge().edgeForbidden(y.getName(), x.getName())) {
                            continue;
                        }

                        graph.addUndirectedEdge(x, y);
                        appendChangedNodes(graph, changedNodes, x, y);
                        numAdded++;
                    }
                }
            }

            System.out.println("Num added = " + numAdded);
            System.out.println("Num removed = " + numRemoved);
            TetradLogger.getInstance().log("info", "Num added = " + numAdded);
            TetradLogger.getInstance().log("info", "Num removed = " + numRemoved);

            if (numAdded == 0 && numRemoved == 0) {
                graphs.add(new EdgeListGraph(graph));
                changed = false;
                continue LOOP;
            }

            System.out.println("(Reorienting...)");
            graph = orientCpc(graph, getKnowledge(), getOrientationDepth(), test);

            graphs.add(new EdgeListGraph(graph));

            for (int i = graphs.size() - 2; i >= 0; i--) {
                if (graphs.get(graphs.size() - 1).equals(graphs.get(i))) {
                    changed = false;
                    continue LOOP;
                }
            }

            _changedNodes = new ArrayList<Node>(changedNodes);
            changedNodes.clear();

            changed = true;
        }

        this.logger.log("graph", "\nReturning this graph: " + graph);

        long time2 = System.currentTimeMillis();
        this.elapsedTime = time2 - time1;

        return graph;
    }

    private void appendChangedNodes(Graph graph, Set<Node> changedNodes, Node x, Node y) {
        changedNodes.add(x);
        changedNodes.add(y);

        for (Node _x : graph.getAdjacentNodes(x)) {
            if (graph.getAdjacentNodes(_x).size() > 1) {
                changedNodes.add(_x);
            }
        }

        for (Node _y : graph.getAdjacentNodes(y)) {
            if (graph.getAdjacentNodes(_y).size() > 1) {
                changedNodes.add(_y);
            }
        }

//        changedNodes.addAll(graph.getAdjacentNodes(x));
//        changedNodes.addAll(graph.getAdjacentNodes(y));
    }

    //================================PRIVATE METHODS=======================//

    private List<Node> pathBlockingSet(IndependenceTest test, Graph graph, Node x, Node y) {

        List<Node> fullSet = new ArrayList<Node>();

        if (getMaxDescendantPath() == 0) {
            fullSet = pathBlockingSetExcluding(graph, x, y, new HashSet<Node>());
        }

        List<Node> commonAdjacents = graph.getAdjacentNodes(x);
        commonAdjacents.retainAll(graph.getAdjacentNodes(y));

        DepthChoiceGenerator generator = new DepthChoiceGenerator(commonAdjacents.size(), commonAdjacents.size());
        int[] choice;

        while ((choice = generator.next()) != null) {
            Set<Node> definitelyExcluded = new HashSet<Node>(GraphUtils.asList(choice, commonAdjacents));
//            Set<Node> perhapsExcluded = new HashSet<Node>();

            if (getMaxDescendantPath() == 0) {
                for (Node node1 : new ArrayList<Node>(definitelyExcluded)) {
                    //                if (node1 == x || node1 == y) continue;

                    for (Node node2 : fullSet) {
                        if (graph.isParentOf(node2, x)) continue;
                        if (graph.isParentOf(node2, y)) continue;
                        if (node1 == node2) continue;


                        // These calls to proper descendant and semidiriected path are hanging for large searches. jdramsey 5/5/10
                        if (existsDirectedPathFromTo(graph, node1, node2)) {
                            definitelyExcluded.add(node2);
                        }
                        //                    else if (existsSemidirectedPathFromTo(graph, node1, node2)) {
                        //                        if (!definitelyExcluded.contains(node2)) {
                        //                            perhapsExcluded.add(node2);
                        //                        }
                        //                    }
                    }
                }
            }

            List<Node> sepset = pathBlockingSetExcluding(graph, x, y, definitelyExcluded);

            if (test.isIndependent(x, y, sepset)) {
                return sepset;
            }

//            List<Node> _perhapsExcluded = new ArrayList<Node>(perhapsExcluded);
//            DepthChoiceGenerator generator2 = new DepthChoiceGenerator(_perhapsExcluded.size(), _perhapsExcluded.size());
//            int[] choice2;
//
//            while ((choice2 = generator2.next()) != null) {
//                List<Node> perhapsExcludedSubset = GraphUtils.asList(choice2, _perhapsExcluded);
//                Set<Node> excluded = new HashSet<Node>(definitelyExcluded);
//                excluded.addAll(perhapsExcludedSubset);
//
//                List<Node> sepset = pathBlockingSetExcluding(graph, x, y, excluded);
//
//                if (test.isIndependent(x, y, sepset)) {
//                    return sepset;
//                }
//            }
        }

        return null;
    }

    private List<Node> pathBlockingSetSmall(IndependenceTest test, Graph graph, Node x, Node y) {
        List<Node> adjX = graph.getAdjacentNodes(x);
        adjX.removeAll(graph.getParents(x));
        adjX.removeAll(graph.getChildren(x));

        DepthChoiceGenerator gen = new DepthChoiceGenerator(adjX.size(), -1);
        int[] choice;

        while ((choice = gen.next()) != null) {
            List<Node> selection = GraphUtils.asList(choice, adjX);
            Set<Node> sepset = new HashSet<Node>(selection);
            sepset.addAll(graph.getParents(x));

            sepset.remove(x);
            sepset.remove(y);

            ArrayList<Node> sepsetList = new ArrayList<Node>(sepset);

            if (test.isIndependent(x, y, sepsetList)) {
                return sepsetList;
            }
        }

        return null;
    }

    private List<Node> pathBlockingSetExcluding(Graph graph, Node x, Node y, Set<Node> excludedNodes) {
        List<Node> condSet = new LinkedList<Node>();

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

    private Graph orientCpc(Graph graph, IKnowledge knowledge, int depth,
                            IndependenceTest test) {
//        SearchGraphUtils.basicPattern(graph);
        graph = GraphUtils.undirectedGraph(graph);
        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());

        System.out.println("Colliders");
//        orientUnshieldedTriplesNew(graph, knowledge, test, depth, changedNodes,
//                colliderNodes, colliders);

        Set<Node> colliderNodes = orientUnshieldedTriples(graph, test, depth, knowledge);

        System.out.println("Meek rules");
        MeekRules meekRules = new MeekRules();
        meekRules.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
        meekRules.setKnowledge(knowledge);
        meekRules.orientImplied(graph);

        return graph;
    }

    /**
     * Assumes a graph with only required knowledge orientations.
     */
    private Set<Node> orientUnshieldedTriples(Graph graph, IndependenceTest test, int depth, IKnowledge knowledge) {
        TetradLogger.getInstance().log("info", "Starting Collider Orientation:");

        List<Node> nodes = graph.getNodes();
        Set<Node> colliderNodes = new HashSet<Node>();


        for (Node y : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(y);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node x = adjacentNodes.get(combination[0]);
                Node z = adjacentNodes.get(combination[1]);

                if (graph.isAdjacentTo(x, z)) {
                    continue;
                }

                SearchGraphUtils.CpcTripleType type = SearchGraphUtils.getCpcTripleType2(x, y, z, test, depth, graph);

                if (type == SearchGraphUtils.CpcTripleType.COLLIDER &&
                        isArrowpointAllowed(x, y, knowledge) &&
                        isArrowpointAllowed(z, y, knowledge)) {
                    graph.setEndpoint(x, y, Endpoint.ARROW);
                    graph.setEndpoint(z, y, Endpoint.ARROW);

                    colliderNodes.add(y);
                    TetradLogger.getInstance().log("colliderOrientations", SearchLogUtils.colliderOrientedMsg(x, y, z));
//                    System.out.println(SearchLogUtils.colliderOrientedMsg(x, y, z));
                } else if (type == SearchGraphUtils.CpcTripleType.AMBIGUOUS) {
                    Triple triple = new Triple(x, y, z);
                    graph.addAmbiguousTriple(triple.getX(), triple.getY(), triple.getZ());
                }
            }
        }

        TetradLogger.getInstance().log("info", "Finishing Collider Orientation.");

        return colliderNodes;
    }

//    private void orientUnshieldedTriplesNew(Graph graph, IKnowledge knowledge,
//                                             IndependenceTest test, int depth, Set<Node> changedNodes,
//                                             Set<Node> colliderNodes, Set<Triple> colliders) {
//        TetradLogger.getInstance().log("details", "Starting Collider Orientation:");
//
//        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
//
//        graph = orientUnshieldedColliders(graph, colliders, changedNodes);
////        SearchGraphUtils.basicPattern(graph);
//
//        for (Node x : changedNodes) {
//            List<Node> adj = graph.getAdjacentNodes(x);
//
////            for (Node y : adj) {
////                graph.setEndpoint(y, x, Endpoint.TAIL);
////            }
//
//            colliderNodes.remove(adj);
//
//            for (Triple triple : colliders) {
//                if (triple.getY() == adj) {
//                    colliders.remove(triple);
//                }
//            }
//        }
//
//        for (Triple triple : graph.getAmbiguousTriples()) {
//            if (changedNodes.contains(triple.getY())) {
//                graph.removeAmbiguousTriple(triple.getX(), triple.getY(), triple.getZ());
//            }
//        }
//
//        for (Node y : changedNodes) {
//            List<Node> adjacentNodes = graph.getAdjacentNodes(y);
//
//            if (adjacentNodes.size() < 2) {
//                continue;
//            }
//
//            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
//            int[] combination;
//
//            while ((combination = cg.next()) != null) {
//                Node x = adjacentNodes.get(combination[0]);
//                Node z = adjacentNodes.get(combination[1]);
//
//                if (graph.isAdjacentTo(x, z)) {
//                    continue;
//                }
//
//                SearchGraphUtils.CpcTripleType type = getCpcTripleType(x, y, z, test, depth, graph);
//
//                if (type == SearchGraphUtils.CpcTripleType.COLLIDER) {
//                    if (colliderAllowed(x, y, z, knowledge)) {
//                        graph.setEndpoint(y, x, Endpoint.TAIL);
//                        graph.setEndpoint(y, z, Endpoint.TAIL);
//                        graph.setEndpoint(x, y, Endpoint.ARROW);
//                        graph.setEndpoint(z, y, Endpoint.ARROW);
//
//                        colliderNodes.add(y);
//                        colliders.add(new Triple(x, y, z));
//
//                        TetradLogger.getInstance().log("colliderOrientations", SearchLogUtils.colliderOrientedMsg(x, y, z));
//                        System.out.println(SearchLogUtils.colliderOrientedMsg(x, y, z));
//                    }
//                } else if (type == SearchGraphUtils.CpcTripleType.AMBIGUOUS) {
//                    graph.addAmbiguousTriple(x, y, z);
//                }
//            }
//        }
//
//        TetradLogger.getInstance().log("details", "Finishing Collider Orientation.");
//    }

    private Graph orientUnshieldedColliders(Graph graph, Set<Triple> colliders, Set<Node> changedNodes) {
        graph = GraphUtils.undirectedGraph(graph);

        for (Triple triple : colliders) {
            if (!changedNodes.contains(triple.getY())) {
                graph.setEndpoint(triple.getX(), triple.getY(), Endpoint.ARROW);
                graph.setEndpoint(triple.getZ(), triple.getX(), Endpoint.ARROW);
            }
        }

        return graph;
    }

    public static SearchGraphUtils.CpcTripleType getCpcTripleType(Node x, Node y, Node z,
                                                                  IndependenceTest test, int depth,
                                                                  Graph graph) {
        boolean existsSepsetContainingY = false;
        boolean existsSepsetNotContainingY = false;

        List<Node> adjX = graph.getAdjacentNodes(x);
        List<Node> adjZ = graph.getAdjacentNodes(z);

        Set<Node> adjXMinusZ = new HashSet<Node>(adjX);
        adjXMinusZ.remove(z);

        List<Node> _nodes = new LinkedList<Node>(adjXMinusZ);
        TetradLogger.getInstance().log("adjacencies", "Adjacents for " + x + "--" + y + "--" + z + " = " + _nodes);

        int _depth = depth;
        if (_depth == -1) {
            _depth = 1000;
        }
        _depth = Math.min(_depth, _nodes.size());

        for (int d = 0; d <= _depth; d++) {
            ChoiceGenerator cg = new ChoiceGenerator(_nodes.size(), d);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = GraphUtils.asList(choice, _nodes);

                if (test.isIndependent(x, z, condSet)) {
                    if (condSet.contains(y)) {
                        existsSepsetContainingY = true;
                    } else {
                        existsSepsetNotContainingY = true;
                    }
                }

                if (existsSepsetContainingY && existsSepsetNotContainingY) {
                    return SearchGraphUtils.CpcTripleType.AMBIGUOUS;
                }
            }
        }

        Set<Node> adjZMinuxX = new HashSet<Node>(adjZ);
        adjZMinuxX.remove(x);

        _nodes = new LinkedList<Node>(adjZMinuxX);
        TetradLogger.getInstance().log("adjacencies", "Adjacents for " + x + "--" + y + "--" + z + " = " + _nodes);

        _depth = depth;
        if (_depth == -1) {
            _depth = 1000;
        }
        _depth = Math.min(_depth, _nodes.size());

        for (int d = 0; d <= _depth; d++) {
            ChoiceGenerator cg = new ChoiceGenerator(_nodes.size(), d);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = GraphUtils.asList(choice, _nodes);

                if (test.isIndependent(x, z, condSet)) {
                    if (condSet.contains(y)) {
                        existsSepsetContainingY = true;
                    } else {
                        existsSepsetNotContainingY = true;
                    }
                }

                if (existsSepsetContainingY == true && existsSepsetNotContainingY == true) {
                    return SearchGraphUtils.CpcTripleType.AMBIGUOUS;
                }

                if (existsSepsetContainingY && existsSepsetNotContainingY) {
                    return SearchGraphUtils.CpcTripleType.AMBIGUOUS;
                }
            }
        }

        if (existsSepsetContainingY && !existsSepsetNotContainingY) {
            return SearchGraphUtils.CpcTripleType.NONCOLLIDER;
        } else if (!existsSepsetContainingY && existsSepsetNotContainingY) {
            return SearchGraphUtils.CpcTripleType.COLLIDER;
        } else {
            return SearchGraphUtils.CpcTripleType.AMBIGUOUS;
        }
    }

    private boolean colliderAllowed(Node x, Node y, Node z, IKnowledge knowledge) {
        return isArrowpointAllowed1(x, y, knowledge) &&
                isArrowpointAllowed1(z, y, knowledge);
    }

    private static boolean isArrowpointAllowed1(Node from, Node to,
                                                IKnowledge knowledge) {
        if (knowledge == null) {
            return true;
        }

        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }

    private void collectSemidirectedDescendantsVisit(Graph graph, Node node, Set<Node> descendants) {
        descendants.add(node);
        List<Node> children = graph.getChildren(node);

        if (!children.isEmpty()) {
            for (Object aChildren : children) {
                Node child = (Node) aChildren;
                doSemidirectedChildClosureVisit(graph, child, descendants);
            }
        }
    }

    /**
     * closure under the child relation
     */
    private void doSemidirectedChildClosureVisit(Graph graph, Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Edge edge1 : graph.getEdges(node)) {
                Node sub = Edges.traverseUndirected(node, edge1);

                if (sub != null && (edge1.pointsTowards(sub) || Edges.isUndirectedEdge(edge1))) {
                    doSemidirectedChildClosureVisit(graph, sub, closure);
                }
            }
        }
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            throw new IllegalArgumentException("Number of graph correction iterations must be >= 0: " + maxIterations);
        }

        this.maxIterations = maxIterations;
    }

    public void setInitialGraph(Graph initialGraph) {
        this.initialGraph = initialGraph;
    }

    public Set<Triple> getColliderTriples(Graph graph) {
        Set<Triple> triples = new HashSet<Triple>();

        for (Node node : graph.getNodes()) {
            List<Node> nodesInto = graph.getNodesInTo(node, Endpoint.ARROW);

            if (nodesInto.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(nodesInto.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                triples.add(new Triple(nodesInto.get(choice[0]), node, nodesInto.get(choice[1])));
            }
        }

        return triples;
    }


    public boolean existsDirectedPathFromTo(Graph graph, Node node1, Node node2) {
        return existsDirectedPathVisit(graph, node1, node2, new LinkedList<Node>());
    }

//    public boolean existsSemidirectedPathFromTo(Graph graph, Node node1, Node node2) {
//        return existsSemiDirectedPathVisit(graph, node1, node2, new LinkedList<Node>());
//    }


    private boolean existsDirectedPathVisit(Graph graph, Node node1, Node node2,
                                            LinkedList<Node> path) {
        if (graph.getAdjacentNodes(node1).size() <= 6 && path.size() > getMaxDescendantPath()) {
            return false;
        } else if (graph.getAdjacentNodes(node1).size() > 6) {
            return false;
        }

        path.addLast(node1);

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
    private boolean existsSemiDirectedPathVisit(Graph graph, Node node1, Node node2,
                                                LinkedList<Node> path) {
        if (graph.getAdjacentNodes(node1).size() <= 6 && path.size() > getMaxDescendantPath()) {
            return false;
        } else if (graph.getAdjacentNodes(node1).size() > 6) {
            return false;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
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

            if (existsSemiDirectedPathVisit(graph, child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }


    public void setPcDepth(int pcDepth) {
        if (pcDepth < -1) {
            throw new IllegalArgumentException();
        }

        this.pcDepth = pcDepth;
    }

    public int getPcDepth() {
        return pcDepth;
    }

    public int getOrientationDepth() {
        return orientationDepth;
    }

    public void setOrientationDepth(int orientationDepth) {
        if (orientationDepth < -1) {
            throw new IllegalArgumentException("Depth must be -1 or >= 0.");
        }

        if (orientationDepth == -1) {
            orientationDepth = Integer.MAX_VALUE;
        }

        this.orientationDepth = orientationDepth;
    }

    /**
     * Checks if an arrowpoint is allowed by background knowledge.
     */
    public static boolean isArrowpointAllowed(Object from, Object to,
                                              IKnowledge knowledge) {
        if (knowledge == null) {
            return true;
        }
        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }
}
