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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a time series graph--that is, a graph with a fixed number S of lags, with edges into initial lags
 * only--that is, into nodes in the first R lags, for some R. Edge structure repeats every R nodes.
 *
 * @author Joseph Ramsey
 */
public class TimeLagGraph implements Graph {
    static final long serialVersionUID = 23L;

    /**
     * Fires property change events.
     */
    private transient PropertyChangeSupport pcs;


    private EdgeListGraph edgeListGraph = new EdgeListGraph();
    private int maxLag = 1;
    private int numInitialLags = 1;
    private List<Node> lag0Nodes = new ArrayList<Node>();

    public TimeLagGraph() {
    }

    public TimeLagGraph(TimeLagGraph graph) {
        this.edgeListGraph = new EdgeListGraph(graph.getEdgeListGraph());
        this.maxLag = graph.getMaxLag();
        this.numInitialLags = graph.getNumInitialLags();
        this.lag0Nodes = graph.getLag0Nodes();

        this.edgeListGraph.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                getPcs().firePropertyChange(evt);
            }
        });
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static TimeLagGraph serializableInstance() {
        return new TimeLagGraph();
    }


    /**
     * Nodes may be added into the current time step only. That is, node.getLag() must be 0.
     */
    @Override
	public boolean addNode(Node node) {
        
        NodeId id = getNodeId(node);

        if (id.getLag() != 0) {
            node = node.like(id.getName() + ":0");
//            throw new IllegalArgumentException("Nodes may be added into the current time step only.");
        }

        boolean added = getEdgeListGraph().addNode(node);

        if (!lag0Nodes.contains(node)) {
            lag0Nodes.add(node);
        }

        if (node.getNodeType() == NodeType.ERROR) {
            for (int i = 1; i <= getMaxLag(); i++) {
                Node node1 = node.like(id.getName() + ":" + i);

                if (i < getNumInitialLags()) {
                    getEdgeListGraph().addNode(node1);
                }
            }
        }
        else {
            for (int i = 1; i <= getMaxLag(); i++) {
                Node node1 = node.like(id.getName() + ":" + i);
                getEdgeListGraph().addNode(node1);
            }
        }

        getPcs().firePropertyChange("editingFinished", null, null);

        return added;
    }

    @Override
	public boolean removeNode(Node node) {
        if (!containsNode(node)) {
            throw new IllegalArgumentException("That is not a node in this graph: " + node);
        }

        NodeId id = getNodeId(node);

        for (int lag = 0; lag < maxLag; lag++) {
            Node _node = getNode(id.getName(), lag);
            if (_node != null) {
                getEdgeListGraph().removeNode(_node);
            }
            if (_node != null && lag == 0) {
                lag0Nodes.remove(_node);
            }
        }

        getPcs().firePropertyChange("editingFinished", null, null);

        if (getEdgeListGraph().containsNode(node)) {
            return getEdgeListGraph().removeNode(node);
        }
        else {
            return false;
        }
    }

    @Override
	public boolean addEdge(Edge edge) {
        if (!Edges.isDirectedEdge(edge)) {
            throw new IllegalArgumentException("Only directed edges supported: " + edge);
        }

        Node node1 = Edges.getDirectedEdgeTail(edge);
        Node node2 = Edges.getDirectedEdgeHead(edge);

        NodeId id1 = getNodeId(node1);
        NodeId id2 = getNodeId(node2);
        int lag = id1.getLag() - id2.getLag();

        if (lag < 0) {
            throw new IllegalArgumentException("Backward edges not permitted: " + edge);
        }

        for (int _lag = getNodeId(node2).getLag() % getNumInitialLags(); _lag <= getMaxLag() - lag; _lag += getNumInitialLags()) {
            Node from = getNode(id1.getName(), _lag + lag);
            Node to = getNode(id2.getName(), _lag);

            if (from == null || to == null) {
                continue;
            }

            Edge _edge = Edges.directedEdge(from, to);

            if (!getEdgeListGraph().containsEdge(_edge)) {
                getEdgeListGraph().addDirectedEdge(from, to);
            }
        }

        return true;
    }

    @Override
	public boolean removeEdge(Edge edge) {
        if (!Edges.isDirectedEdge(edge)) throw new IllegalArgumentException("Only directed edges are expected in the model.");

        Node node1 = Edges.getDirectedEdgeTail(edge);
        Node node2 = Edges.getDirectedEdgeHead(edge);

        NodeId id1 = getNodeId(node1);
        NodeId id2 = getNodeId(node2);
        int lag = id1.getLag() - id2.getLag();

        boolean removed = false;

        for (int _lag = 0; _lag <= getMaxLag(); _lag++) {
            Node from = getNode(id1.getName(), _lag + lag);
            Node to = getNode(id2.getName(), _lag);

            if (from != null && to != null) {
                Edge _edge = getEdgeListGraph().getEdge(from, to);

                if (_edge != null) {
                    boolean b = getEdgeListGraph().removeEdge(_edge);
                    removed = removed || b;
                }
            }
        }

        return removed;
    }

    public boolean setMaxLag(int maxLag) {
        if (maxLag < 0) {
            throw new IllegalArgumentException("Max lag must be at least 0: " + maxLag);
        }

        List<Node> lag0Nodes = getLag0Nodes();

        boolean changed = false;

        if (maxLag > this.getMaxLag()) {
            this.maxLag = maxLag;
            for (Node node : lag0Nodes) {
                addNode(node);
            }

            for (Node node : lag0Nodes) {
                List<Edge> edges = getEdgeListGraph().getEdges(node);

                for (Edge edge : edges) {
                    boolean b = addEdge(edge);
                    changed = changed || b;
                }
            }
        } else if (maxLag < this.getMaxLag()) {
            for (Node node : lag0Nodes) {
                List<Edge> edges = getEdgeListGraph().getEdges(node);

                for (Edge edge : edges) {
                    Node tail = Edges.getDirectedEdgeTail(edge);

                    if (getNodeId(tail).getLag() > maxLag) {
                        throw new IllegalArgumentException("This edge has lag greater than the new maxLag: " + edge +
                                " Please remove first.");
                    }
                }
            }

            for (Node _node : getNodes()) {
                if (getNodeId(_node).getLag() > maxLag) {
                    boolean b = getEdgeListGraph().removeNode(_node);
                    changed = changed || b;
                }
            }

            this.maxLag = maxLag;
        }

        getPcs().firePropertyChange("editingFinished", null, null);

        return changed;
    }

    public boolean removeHighLagEdges(int maxLag) {
        List<Node> lag0Nodes = getLag0Nodes();
        boolean changed = false;

        for (Node node : lag0Nodes) {
            List<Edge> edges = getEdgeListGraph().getEdges(node);

            for (Edge edge : new ArrayList<Edge>(edges)) {
                Node tail = Edges.getDirectedEdgeTail(edge);

                if (getNodeId(tail).getLag() > maxLag) {
                    boolean b = getEdgeListGraph().removeEdge(edge);
                    changed = changed || b;
                }
            }
        }

        return changed;
    }

    public boolean setNumInitialLags(int numInitialLags) {
        if (numInitialLags < 1) {
            throw new IllegalArgumentException("The number of initial lags must be at least 1: " + numInitialLags);
        }

        if (numInitialLags == this.numInitialLags) return false;

        List<Node> lag0Nodes = getLag0Nodes();
        boolean changed = false;

        for (Node node : lag0Nodes) {
            NodeId id = getNodeId(node);

            for (int lag = 1; lag <= getMaxLag(); lag++) {
                Node _node = getNode(id.getName(), lag);
                List<Node> nodesInto = getEdgeListGraph().getNodesInTo(_node, Endpoint.ARROW);

                for (Node _node2 : nodesInto) {
                    Edge edge = Edges.directedEdge(_node2, _node);
                    boolean b = getEdgeListGraph().removeEdge(edge);
                    changed = changed || b;
                }
            }
        }

        this.numInitialLags = numInitialLags;

        for (Node node : lag0Nodes) {
            for (int lag = 0; lag < numInitialLags; lag++) {
                List<Edge> edges = getEdgeListGraph().getEdges(node);

                for (Edge edge : edges) {
                    boolean b = addEdge(edge);
                    changed = changed || b;
                }
            }
        }

        getPcs().firePropertyChange("editingFinished", null, null);

        return changed;
    }

    public NodeId getNodeId(Node node) {
        String _name = node.getName();
        String[] tokens = _name.split(":");
        if (tokens.length < 2) throw new IllegalArgumentException("Expecting a time lag variable name: " + _name);
        if (tokens.length > 2) throw new IllegalArgumentException("Name may contain only one colon: " + _name);
        if (tokens[0].length() == 0) throw new IllegalArgumentException("Part to the left of the colon may " +
                "not be empty; that's the name of the variable: " + _name);
        String name = tokens[0];

        int lag = Integer.parseInt(tokens[1]);
        if (lag < 0) throw new IllegalArgumentException("Lag is less than 0: " + lag);
        if (lag > getMaxLag()) throw new IllegalArgumentException("Lag is greater than the maximum lag: " + lag);

        return new NodeId(name, lag);
    }

    public Node getNode(String name, int lag) {
        if (name.length() == 0) throw new IllegalArgumentException("Empty node name: " + name);
        if (lag < 0) throw new IllegalArgumentException("Negative lag: " + lag);
        String _name = name + ":" + lag;
        return getNode(_name);
    }

    public List<Node> getLag0Nodes() {
        return new ArrayList<Node>(lag0Nodes);
    }

    public EdgeListGraph getEdgeListGraph() {
        return edgeListGraph;
    }

    public int getMaxLag() {
        return maxLag;
    }

    public int getNumInitialLags() {
        return numInitialLags;
    }

    public static class NodeId {
        private String name;
        private int lag;

        public NodeId(String name, int lag) {
            this.name = name;
            this.lag = lag;
        }

        public String getName() {
            return name;
        }

        public int getLag() {
            return lag;
        }
    }

    @Override
	public String toString() {
        return getEdgeListGraph().toString() + "\n" + lag0Nodes;
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        return getEdgeListGraph().addGraphConstraint(gc);
    }

    @Override
	public boolean addDirectedEdge(Node node1, Node node2) {
        return addEdge(Edges.directedEdge(node1, node2));
//        return edgeListGraph.addDirectedEdge(node1, node2);
    }

    @Override
	public boolean addUndirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException("Undirected edges not currently supported.");
//        return edgeListGraph.addUndirectedEdge(node1, node2);
    }

    @Override
	public boolean addNondirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException("Nondireced edges not supported.");
//        return edgeListGraph.addNondirectedEdge(node1, node2);
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException("Partially oriented edges not supported.");
//        return edgeListGraph.addPartiallyOrientedEdge(node1, node2);
    }

    @Override
	public boolean addBidirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException("Bidireced edges not currently supported.");
//        return edgeListGraph.addBidirectedEdge(node1, node2);
    }

    @Override
	public boolean existsDirectedCycle() {
        return getEdgeListGraph().existsDirectedCycle();
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return getEdgeListGraph().isDirectedFromTo(node1, node2);
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return getEdgeListGraph().isUndirectedFromTo(node1, node2);
    }

    @Override
	public boolean defVisible(Edge edge) {
        return getEdgeListGraph().defVisible(edge);
    }

    @Override
	public boolean isDefNoncollider(Node node1, Node node2, Node node3) {
        return getEdgeListGraph().isDefNoncollider(node1, node2, node3);
    }

    @Override
	public boolean isDefCollider(Node node1, Node node2, Node node3) {
        return getEdgeListGraph().isDefCollider(node1, node2, node3);
    }

    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        return getEdgeListGraph().existsDirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return getEdgeListGraph().existsUndirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes) {
        return getEdgeListGraph().existsSemiDirectedPathFromTo(node1, nodes);
    }

    @Override
	public boolean existsTrek(Node node1, Node node2) {
        return getEdgeListGraph().existsTrek(node1, node2);
    }

    @Override
	public List<Node> getChildren(Node node) {
        return getEdgeListGraph().getChildren(node);
    }

    @Override
	public int getConnectivity() {
        return getEdgeListGraph().getConnectivity();
    }

    @Override
	public List<Node> getDescendants(List<Node> nodes) {
        return getEdgeListGraph().getDescendants(nodes);
    }

    @Override
	public Edge getEdge(Node node1, Node node2) {
        return getEdgeListGraph().getEdge(node1, node2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        return getEdgeListGraph().getDirectedEdge(node1, node2);
    }

    @Override
	public List<Node> getParents(Node node) {
        return getEdgeListGraph().getParents(node);
    }

    @Override
	public int getIndegree(Node node) {
        return getEdgeListGraph().getIndegree(node);
    }

    @Override
	public int getOutdegree(Node node) {
        return getEdgeListGraph().getOutdegree(node);
    }

    @Override
	public boolean isAdjacentTo(Node node1, Node node2) {
        return getEdgeListGraph().isAdjacentTo(node1, node2);
    }

    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return getEdgeListGraph().isAncestorOf(node1, node2);
    }

    @Override
	public boolean possibleAncestor(Node node1, Node node2) {
        return getEdgeListGraph().possibleAncestor(node1, node2);
    }

    public boolean possibleAncestorSet(Node node1, List<Node> nodes2) {
        return getEdgeListGraph().possibleAncestorSet(node1, nodes2);
    }

    @Override
	public List<Node> getAncestors(List<Node> nodes) {
        return getEdgeListGraph().getAncestors(nodes);
    }

    @Override
	public boolean isChildOf(Node node1, Node node2) {
        return getEdgeListGraph().isChildOf(node1, node2);
    }

    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return getEdgeListGraph().isDescendentOf(node1, node2);
    }

    @Override
	public boolean defNonDescendent(Node node1, Node node2) {
        return getEdgeListGraph().defNonDescendent(node1, node2);
    }

    @Override
	public boolean isDConnectedTo(Node node1, Node node2, List<Node> conditioningNodes) {
        return getEdgeListGraph().isDConnectedTo(node1, node2, conditioningNodes);
    }

    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return getEdgeListGraph().isDSeparatedFrom(node1, node2, z);
    }

    @Override
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> condNodes) {
        return getEdgeListGraph().possDConnectedTo(node1, node2, condNodes);
    }

    @Override
	public boolean existsInducingPath(Node node1, Node node2, Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return getEdgeListGraph().existsInducingPath(node1, node2, observedNodes, conditioningNodes);
    }

    @Override
	public boolean isParentOf(Node node1, Node node2) {
        return getEdgeListGraph().isParentOf(node1, node2);
    }

    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return getEdgeListGraph().isProperAncestorOf(node1, node2);
    }

    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return getEdgeListGraph().isProperDescendentOf(node1, node2);
    }

    @Override
	public void transferNodesAndEdges(Graph graph) throws IllegalArgumentException {
        getEdgeListGraph().transferNodesAndEdges(graph);
    }

    @Override
	public Set<Triple> getAmbiguousTriples() {
        return getEdgeListGraph().getAmbiguousTriples();
    }

    @Override
	public Set<Triple> getUnderLines() {
        return getEdgeListGraph().getUnderLines();
    }

    @Override
	public Set<Triple> getDottedUnderlines() {
        return getEdgeListGraph().getDottedUnderlines();
    }

    @Override
	public boolean isAmbiguousTriple(Node x, Node y, Node z) {
        return getEdgeListGraph().isAmbiguousTriple(x, y, z);
    }

    @Override
	public boolean isUnderlineTriple(Node x, Node y, Node z) {
        return getEdgeListGraph().isUnderlineTriple(x, y, z);
    }

    @Override
	public boolean isDottedUnderlineTriple(Node x, Node y, Node z) {
        return getEdgeListGraph().isDottedUnderlineTriple(x, y, z);
    }

    @Override
	public void addAmbiguousTriple(Node x, Node y, Node z) {
        getEdgeListGraph().addAmbiguousTriple(x, y, z);
    }

    @Override
	public void addUnderlineTriple(Node x, Node y, Node z) {
        getEdgeListGraph().addUnderlineTriple(x, y, z);
    }

    @Override
	public void addDottedUnderlineTriple(Node x, Node y, Node z) {
        getEdgeListGraph().addDottedUnderlineTriple(x, y, z);
    }

    @Override
	public void removeAmbiguousTriple(Node x, Node y, Node z) {
        getEdgeListGraph().removeAmbiguousTriple(x, y, z);
    }

    @Override
	public void removeUnderlineTriple(Node x, Node y, Node z) {
        getEdgeListGraph().removeUnderlineTriple(x, y, z);
    }

    @Override
	public void removeDottedUnderlineTriple(Node x, Node y, Node z) {
        getEdgeListGraph().removeDottedUnderlineTriple(x, y, z);
    }

    @Override
	public void setAmbiguousTriples(Set<Triple> triples) {
        getEdgeListGraph().setAmbiguousTriples(triples);
    }

    @Override
	public void setUnderLineTriples(Set<Triple> triples) {
        getEdgeListGraph().setUnderLineTriples(triples);
    }

    @Override
	public void setDottedUnderLineTriples(Set<Triple> triples) {
        getEdgeListGraph().setDottedUnderLineTriples(triples);
    }

    @Override
	public List<Node> getTierOrdering() {
        return getEdgeListGraph().getTierOrdering();
    }

    @Override
	public void setHighlighted(Edge edge, boolean highlighted) {
        getEdgeListGraph().setHighlighted(edge, highlighted);
    }

    @Override
	public boolean isHighlighted(Edge edge) {
        return getEdgeListGraph().isHighlighted(edge);
    }

    @Override
	public boolean isParameterizable(Node node) {
        return getNodeId(node).getLag() < getNumInitialLags();
    }

    @Override
	public boolean isTimeLagModel() {
        return true;
    }

    @Override
	public TimeLagGraph getTimeLagGraph() {
        return this;
    }

    @Override
	public boolean isExogenous(Node node) {
        return getEdgeListGraph().isExogenous(node);
    }

    @Override
	public List<Node> getAdjacentNodes(Node node) {
        return getEdgeListGraph().getAdjacentNodes(node);
    }


    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return getEdgeListGraph().getEndpoint(node1, node2);
    }

    @Override
	public boolean setEndpoint(Node from, Node to, Endpoint endPoint) throws IllegalArgumentException {
        return getEdgeListGraph().setEndpoint(from, to, endPoint);
    }

    @Override
	public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        return getEdgeListGraph().getNodesInTo(node, endpoint);
    }

    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint endpoint) {
        return getEdgeListGraph().getNodesOutTo(node, endpoint);
    }

    @Override
	public Endpoint[][] getEndpointMatrix() {
        return getEdgeListGraph().getEndpointMatrix();
    }


    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        getPcs().addPropertyChangeListener(l);
        getEdgeListGraph().addPropertyChangeListener(l);
    }

    @Override
	public List<Edge> getEdges() {
        return getEdgeListGraph().getEdges();
    }

    @Override
	public boolean containsEdge(Edge edge) {
        return getEdgeListGraph().containsEdge(edge);
    }

    @Override
	public boolean containsNode(Node node) {
        return getEdgeListGraph().containsNode(node);
    }

    @Override
	public List<Edge> getEdges(Node node) {
        if (getEdgeListGraph().containsNode(node)) {
            return getEdgeListGraph().getEdges(node);
        }
        else {
            return null;
        }
    }

    @Override
	public List<Edge> getEdges(Node node1, Node node2) {
        return getEdgeListGraph().getEdges(node1, node2);
    }

    @Override
	public int hashCode() {
        return getEdgeListGraph().hashCode();
    }

    @Override
	public boolean equals(Object o) {
        return getEdgeListGraph().equals(o);
    }

    @Override
	public void fullyConnect(Endpoint endpoint) {
        getEdgeListGraph().fullyConnect(endpoint);
    }

    @Override
	public void reorientAllWith(Endpoint endpoint) {
        getEdgeListGraph().reorientAllWith(endpoint);
    }

    @Override
	public Node getNode(String name) {
        return getEdgeListGraph().getNode(name);
    }

    @Override
	public int getNumNodes() {
        return getEdgeListGraph().getNumNodes();
    }

    @Override
	public int getNumEdges() {
        return getEdgeListGraph().getNumEdges();
    }

    @Override
	public int getNumEdges(Node node) {
        return getEdgeListGraph().getNumEdges(node);
    }

    @Override
	public List<GraphConstraint> getGraphConstraints() {
        return getEdgeListGraph().getGraphConstraints();
    }

    @Override
	public boolean isGraphConstraintsChecked() {
        return getEdgeListGraph().isGraphConstraintsChecked();
    }

    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        getEdgeListGraph().setGraphConstraintsChecked(checked);
    }

    @Override
	public Graph subgraph(List<Node> nodes) {
        return getEdgeListGraph().subgraph(nodes);
    }

    @Override
	public List<Node> getNodes() {
        return getEdgeListGraph().getNodes();
    }

    @Override
	public List<String> getNodeNames() {
        return getEdgeListGraph().getNodeNames();
    }

    @Override
	public void clear() {
        getEdgeListGraph().clear();
    }

    @Override
	public boolean removeEdge(Node node1, Node node2) {
        return removeEdge(getEdge(node1, node2));
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        boolean change = false;

        for (Edge edge : edges) {
            boolean _change = removeEdge(edge);
            change = change || _change;
        }

        return change;
    }

    @Override
	public boolean removeNodes(List<Node> nodes) {
        return getEdgeListGraph().removeNodes(nodes);
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return removeEdges(getEdges(node1, node2));
    }

    /**
     * Returns the existing property change support object for this class, if
     * there is one, or else creates a new one and returns that.
     *
     * @return this object.
     */
    private PropertyChangeSupport getPcs() {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        return pcs;
    }


}

