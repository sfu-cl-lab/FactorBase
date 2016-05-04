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

import edu.cmu.tetrad.util.TetradSerializable;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * A graph that permits only directed edges, with no loops, at most one edge per
 * node pair, and with measured or latent node only.
 *
 * @author Joseph Ramsey
 * @see MeasuredLatentOnly
 * @see DirectedEdgesOnly
 */
public final class DirectedGraph implements TetradSerializable, Graph {
    static final long serialVersionUID = 23L;

    private static final GraphConstraint[] constraints = {
            new MeasuredLatentOnly(), new AtMostOneEdgePerPair(),
            new NoEdgesToSelf(), new DirectedEdgesOnly()};

    /**
     * @serial
     */
    private final Graph graph = new EdgeListGraph();

    //=========================CONSTRUCTORS==========================//

    /**
     * Constructs a new blank DG.
     */
    public DirectedGraph() {
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (Object aConstraints1 : constraints1) {
            addGraphConstraint((GraphConstraint) aConstraints1);
        }
    }

    /**
     * Constructs a new DG based on the given graph.
     *
     * @param graph the graph to base the new DG on.
     * @throws IllegalArgumentException if the given graph cannot be converted
     *                                  into a DG for some reason.
     */
    public DirectedGraph(Graph graph) throws IllegalArgumentException {
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (Object aConstraints1 : constraints1) {
            addGraphConstraint((GraphConstraint) aConstraints1);
        }

        transferNodesAndEdges(graph);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DirectedGraph serializableInstance() {
        return new DirectedGraph();
    }

    //==========================PUBLIC METHODS=======================//

    @Override
	public final void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException {
        this.getGraph().transferNodesAndEdges(graph);
    }

    @Override
	public Set<Triple> getAmbiguousTriples() {
        return getGraph().getAmbiguousTriples();
    }

    @Override
	public Set<Triple> getUnderLines() {
        return getGraph().getUnderLines();
    }

    @Override
	public Set<Triple> getDottedUnderlines() {
        return getGraph().getDottedUnderlines();
    }


    /**
     * States whether x-y-x is an underline triple or not.
     */
    @Override
	public boolean isAmbiguousTriple(Node x, Node y, Node z) {
        return getGraph().isAmbiguousTriple(x, y, z);
    }

    /**
     * States whether x-y-x is an underline triple or not.
     */
    @Override
	public boolean isUnderlineTriple(Node x, Node y, Node z) {
        return getGraph().isUnderlineTriple(x, y, z);
    }

    /**
     * States whether x-y-x is an underline triple or not.
     */
    @Override
	public boolean isDottedUnderlineTriple(Node x, Node y, Node z) {
        return getGraph().isDottedUnderlineTriple(x, y, z);
    }

    @Override
	public void addAmbiguousTriple(Node x, Node y, Node z) {
        getGraph().addAmbiguousTriple(x, y, z);
    }

    @Override
	public void addUnderlineTriple(Node x, Node y, Node z) {
        getGraph().addUnderlineTriple(x, y, z);
    }

    @Override
	public void addDottedUnderlineTriple(Node x, Node y, Node z) {
        getGraph().addDottedUnderlineTriple(x, y, z);
    }

    @Override
	public void removeAmbiguousTriple(Node x, Node y, Node z) {
        getGraph().removeAmbiguousTriple(x, y, z);
    }

    @Override
	public void removeUnderlineTriple(Node x, Node y, Node z) {
        getGraph().removeUnderlineTriple(x, y, z);
    }

    @Override
	public void removeDottedUnderlineTriple(Node x, Node y, Node z) {
        getGraph().removeDottedUnderlineTriple(x, y, z);
    }


    @Override
	public void setAmbiguousTriples(Set<Triple> triples) {
        getGraph().setAmbiguousTriples(triples);
    }

    @Override
	public void setUnderLineTriples(Set<Triple> triples) {
        getGraph().setUnderLineTriples(triples);
    }


    @Override
	public void setDottedUnderLineTriples(Set<Triple> triples) {
        getGraph().setDottedUnderLineTriples(triples);
    }

    @Override
	public List<Node> getTierOrdering() {
        return getGraph().getTierOrdering();
    }

    @Override
	public void setHighlighted(Edge edge, boolean highlighted) {
        getGraph().setHighlighted(edge, highlighted);
    }

    @Override
	public boolean isHighlighted(Edge edge) {
        return getGraph().isHighlighted(edge);
    }

    @Override
	public boolean isParameterizable(Node node) {
        return getGraph().isParameterizable(node);
    }

    @Override
	public boolean isTimeLagModel() {
        return getGraph().isTimeLagModel();
    }

    @Override
	public TimeLagGraph getTimeLagGraph() {
        return getGraph().getTimeLagGraph();
    }

    @Override
	public List<String> getNodeNames() {
        return getGraph().getNodeNames();
    }


    @Override
	public void fullyConnect(Endpoint endpoint) {
        getGraph().fullyConnect(endpoint);
    }

    @Override
	public void reorientAllWith(Endpoint endpoint) {
        getGraph().reorientAllWith(endpoint);
    }

    @Override
	public Endpoint[][] getEndpointMatrix() {
        return getGraph().getEndpointMatrix();
    }

    @Override
	public List<Node> getAdjacentNodes(Node node) {
        return getGraph().getAdjacentNodes(node);
    }

    @Override
	public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        return getGraph().getNodesInTo(node, endpoint);
    }

    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint n) {
        return getGraph().getNodesOutTo(node, n);
    }

    @Override
	public List<Node> getNodes() {
        return getGraph().getNodes();
    }

    @Override
	public boolean removeEdge(Node node1, Node node2) {
        return getGraph().removeEdge(node1, node2);
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return getGraph().removeEdges(node1, node2);
    }

    @Override
	public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return getGraph().isAdjacentTo(nodeX, nodeY);
    }

    @Override
	public boolean setEndpoint(Node node1, Node node2, Endpoint endpoint) {
        return getGraph().setEndpoint(node1, node2, endpoint);
    }

    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return getGraph().getEndpoint(node1, node2);
    }

    @Override
	public boolean equals(Object o) {
        return getGraph().equals(o);
    }

    @Override
	public Graph subgraph(List<Node> nodes) {
        return getGraph().subgraph(nodes);
    }

    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        return getGraph().existsDirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return getGraph().existsUndirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes) {
        return getGraph().existsSemiDirectedPathFromTo(node1, nodes);
    }

    @Override
	public boolean addDirectedEdge(Node nodeA, Node nodeB) {
        return getGraph().addDirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addUndirectedEdge(Node nodeA, Node nodeB) {
        return getGraph().addUndirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addNondirectedEdge(Node nodeA, Node nodeB) {
        return getGraph().addNondirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node nodeA, Node nodeB) {
        return getGraph().addPartiallyOrientedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addBidirectedEdge(Node nodeA, Node nodeB) {
        return getGraph().addBidirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addEdge(Edge edge) {
        return getGraph().addEdge(edge);
    }

    @Override
	public boolean addNode(Node node) {
        return getGraph().addNode(node);
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        getGraph().addPropertyChangeListener(l);
    }

    @Override
	public boolean containsEdge(Edge edge) {
        return getGraph().containsEdge(edge);
    }

    @Override
	public boolean containsNode(Node node) {
        return getGraph().containsNode(node);
    }

    @Override
	public List<Edge> getEdges() {
        return getGraph().getEdges();
    }

    @Override
	public List<Edge> getEdges(Node node) {
        return getGraph().getEdges(node);
    }

    @Override
	public List<Edge> getEdges(Node node1, Node node2) {
        return getGraph().getEdges(node1, node2);
    }

    @Override
	public Node getNode(String name) {
        return getGraph().getNode(name);
    }

    @Override
	public int getNumEdges() {
        return getGraph().getNumEdges();
    }

    @Override
	public int getNumNodes() {
        return getGraph().getNumNodes();
    }

    @Override
	public int getNumEdges(Node node) {
        return getGraph().getNumEdges(node);
    }

    @Override
	public List<GraphConstraint> getGraphConstraints() {
        return getGraph().getGraphConstraints();
    }

    @Override
	public boolean isGraphConstraintsChecked() {
        return getGraph().isGraphConstraintsChecked();
    }

    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        getGraph().setGraphConstraintsChecked(checked);
    }

    @Override
	public boolean removeEdge(Edge edge) {
        return getGraph().removeEdge(edge);
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        return getGraph().removeEdges(edges);
    }

    @Override
	public boolean removeNode(Node node) {
        return getGraph().removeNode(node);
    }

    @Override
	public void clear() {
        getGraph().clear();
    }

    @Override
	public boolean removeNodes(List<Node> nodes) {
        return getGraph().removeNodes(nodes);
    }

    @Override
	public boolean existsDirectedCycle() {
        return getGraph().existsDirectedCycle();
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return getGraph().isDirectedFromTo(node1, node2);
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return getGraph().isUndirectedFromTo(node1, node2);
    }

    @Override
	public boolean defVisible(Edge edge) {
        return getGraph().defVisible(edge);
    }

    @Override
	public boolean isDefNoncollider(Node node1, Node node2, Node node3) {
        return getGraph().isDefNoncollider(node1, node2, node3);
    }

    @Override
	public boolean isDefCollider(Node node1, Node node2, Node node3) {
        return getGraph().isDefCollider(node1, node2, node3);
    }

    @Override
	public boolean existsTrek(Node node1, Node node2) {
        return getGraph().existsTrek(node1, node2);
    }

    @Override
	public List<Node> getChildren(Node node) {
        return getGraph().getChildren(node);
    }

    @Override
	public int getConnectivity() {
        return getGraph().getConnectivity();
    }

    @Override
	public List<Node> getDescendants(List<Node> nodes) {
        return getGraph().getDescendants(nodes);
    }

    @Override
	public Edge getEdge(Node node1, Node node2) {
        return getGraph().getEdge(node1, node2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        return getGraph().getDirectedEdge(node1, node2);
    }

    @Override
	public List<Node> getParents(Node node) {
        return getGraph().getParents(node);
    }

    @Override
	public int getIndegree(Node node) {
        return getGraph().getIndegree(node);
    }

    @Override
	public int getOutdegree(Node node) {
        return getGraph().getOutdegree(node);
    }

    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return getGraph().isAncestorOf(node1, node2);
    }

    @Override
	public boolean possibleAncestor(Node node1, Node node2) {
        return getGraph().possibleAncestor(node1, node2);
    }

    @Override
	public List<Node> getAncestors(List<Node> nodes) {
        return getGraph().getAncestors(nodes);
    }

    @Override
	public boolean isChildOf(Node node1, Node node2) {
        return getGraph().isChildOf(node1, node2);
    }

    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return getGraph().isDescendentOf(node1, node2);
    }

    @Override
	public boolean defNonDescendent(Node node1, Node node2) {
        return getGraph().defNonDescendent(node1, node2);
    }

    @Override
	public boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes) {
        return getGraph().isDConnectedTo(node1, node2, conditioningNodes);
    }

    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return getGraph().isDSeparatedFrom(node1, node2, z);
    }

    @Override
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return getGraph().possDConnectedTo(node1, node2, z);
    }

    @Override
	public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return getGraph().existsInducingPath(node1, node2, observedNodes,
                conditioningNodes);
    }

    @Override
	public boolean isParentOf(Node node1, Node node2) {
        return getGraph().isParentOf(node1, node2);
    }

    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return getGraph().isProperAncestorOf(node1, node2);
    }

    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return getGraph().isProperDescendentOf(node1, node2);
    }

    @Override
	public boolean isExogenous(Node node) {
        return getGraph().isExogenous(node);
    }

    @Override
	public String toString() {
        return getGraph().toString();
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        return getGraph().addGraphConstraint(gc);
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (getGraph() == null) {
            throw new NullPointerException();
        }

    }

    private Graph getGraph() {
        return graph;
    }
}



