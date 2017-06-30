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

package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetrad.util.dist.Split;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Parametric model for the extended SEM model. This is a linear model
 * over a set of variables with arbitrary distributions over the errors. The
 * parameters of the model are the linear coefficients plus the parameters
 * of the exogenous distributions.
 * <p/>
 * The graph for this model must be acyclic.
 *
 * @author Joseph Ramsey
 * @deprecated Kept for serialization 7/12/09
 */
@Deprecated
public final class SemPm2 implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The structural model graph that this sem parametric model is based on.
     *
     * @serial Cannot be null.
     */
    private SemGraph graph;

    /**
     * The list of all nodes (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Node> nodes;

    /**
     * The list of Parameters (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Parameter> parameters;

    /**
     * The list of variable nodes (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Node> variableNodes;

    /**
     * Map from variable nodes to exogenous distributions.
     */
    private Map<Node, DistributionType> distributionTypes;

    /**
     * A map from exogenous distributions to parameters for those distributions.
     */
    private HashMap<Node, List<Parameter>> distributionParameters;

    /**
     * The set of parameter comparisons.
     *
     * @serial Cannot be null.
     */
    private Map<ParameterPair, ParamComparison> paramComparisons =
            new HashMap<ParameterPair, ParamComparison>();

    /**
     * The index of the most recent "B" parameter. (These are edge
     * coefficients.)
     *
     * @serial Range >= 0.
     */
    private int bIndex = 0;

    /**
     * The index of the most recent "D" parameter. (These are distribution
     * coefficients.)
     *
     * @serial Range >= 0.
     */
    private int dIndex = 0;

    //===========================CONSTRUCTORS==========================//

    /**
     * Constructs a BayesPm from the given Graph, which must be convertible
     * first into a ProtoSemGraph and then into a SemGraph.
     */
    public SemPm2(Graph graph) {
        this(new SemGraph(graph));
    }

    /**
     * Constructs a new SemPm from the given SemGraph.
     */
    public SemPm2(SemGraph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        if (graph.existsDirectedCycle()) {
            throw new NullPointerException("Graph must be acyclic. Only the DAG " +
                    "case is considered.");
        }

        for (Edge edge : graph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                throw new NullPointerException("Graph must not contain " +
                        "bidirected edges. Only the DAG case is considered.");
            }
        }

        this.graph = graph;
        this.graph.setShowErrorTerms(false);

        initializeNodes(graph);
        initializeVariableNodes();
        initializeParams();
    }

    /**
     * Copy constructor.
     */
    public SemPm2(SemPm2 semPm2) {
        this.graph = semPm2.graph;
        this.nodes = new LinkedList<Node>(semPm2.nodes);
        this.parameters = new LinkedList<Parameter>(semPm2.parameters);
        this.variableNodes = new LinkedList<Node>(semPm2.variableNodes);
        this.distributionTypes = new HashMap<Node, DistributionType>(semPm2.distributionTypes);
        this.distributionParameters = new HashMap<Node, List<Parameter>>(semPm2.distributionParameters);
        this.paramComparisons = new HashMap<ParameterPair, ParamComparison>(
                semPm2.paramComparisons);
        this.bIndex = semPm2.bIndex;
        this.dIndex = semPm2.dIndex;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemPm2 serializableInstance() {
        Dag dag = new Dag();
        GraphNode node1 = new GraphNode("X");
        dag.addNode(node1);
        return new SemPm2(Dag.serializableInstance());
    }

    //============================PUBLIC METHODS========================//

    /**
     * Returns the structural model graph this SEM PM is using.
     */
    public SemGraph getGraph() {
        return this.graph;
    }

    /**
     * Returns a list of all the parameters, including variance, covariance,
     * coefficient, and mean parameters.
     */
    public List<Parameter> getParameters() {
        return new ArrayList<Parameter>(this.parameters);
    }

    /**
     * Returns the list of variable nodes--that is, node that are not error
     * nodes.
     */
    public List<Node> getVariableNodes() {
        return this.variableNodes;
    }

    /**
     * Returns the list of exogenous variableNodes.
     */
    public List<Node> getErrorNodes() {
        List<Node> errorNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            if (node1.getNodeType() == NodeType.ERROR) {
                errorNodes.add(node1);
            }
        }

        return errorNodes;
    }

    /**
     * Returns the list of measured variableNodes.
     */
    public List<Node> getMeasuredNodes() {
        List<Node> measuredNodes = new ArrayList<Node>();

        for (Node variable : getVariableNodes()) {
            if (variable.getNodeType() == NodeType.MEASURED) {
                measuredNodes.add(variable);
            }
        }

        return measuredNodes;
    }

    /**
     * Returns the list of latent variableNodes.
     */
    public List<Node> getLatentNodes() {
        List<Node> latentNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            if (node1.getNodeType() == NodeType.LATENT) {
                latentNodes.add(node1);
            }
        }

        return latentNodes;
    }

    /**
     * Returns the first parameter encountered with the given name, or null if
     * there is no such parameter.
     */
    public Parameter getParameter(String name) {
        for (Parameter parameter1 : getParameters()) {
            if (name.equals(parameter1.getName())) {
                return parameter1;
            }
        }

        return null;
    }

    /**
     * Return the parameter for the edge connecting the two nodes, or null if
     * there is no such parameter. Special note--when trying to get the
     * parameter for a directed edge, it's better to use getEdgeParameter, which
     * automatically adjusts if the user has changed the endpoints of an edge X1
     * --> X2 to X1 <-- X2.
     */
    public Parameter getVarianceParameter(Node node) {
        if (!getGraph().isExogenous(node)) {
            return null;
        }

        node = getGraph().getVarNode(node);

        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (node == _nodeA && node == _nodeB && parameter.getType() == ParamType.VAR) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getCovarianceParameter(Node nodeA, Node nodeB) {
        nodeA = getGraph().getVarNode(nodeA);
        nodeB = getGraph().getVarNode(nodeB);

        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (nodeA == _nodeA && nodeB == _nodeB && parameter.getType() == ParamType.COVAR) {
                return parameter;
            } else if (nodeB == _nodeA && nodeA == _nodeB && parameter.getType() == ParamType.COVAR) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getCoefficientParameter(Node nodeA, Node nodeB) {
        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (nodeA == _nodeA && nodeB == _nodeB && parameter.getType() == ParamType.COEF) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getMeanParameter(Node node) {
        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (node == _nodeA && node == _nodeB && parameter.getType() == ParamType.MEAN) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * Returns the list of measured variable names in the order in which they
     * appear in the list of nodes. (This order is fixed.)
     */
    public String[] getMeasuredVarNames() {
        List<Node> semPmVars = getVariableNodes();
        List<String> varNamesList = new ArrayList<String>();

        for (Node semPmVar : semPmVars) {
            if (semPmVar.getNodeType() == NodeType.MEASURED) {
                varNamesList.add(semPmVar.toString());
            }
        }

        return varNamesList.toArray(new String[0]);
    }

    public void setDistributionType(Node node, DistributionType type) {
        removeDistribution(node);
        addExogenousDistribution(node, type);
    }

    public DistributionType getDistributionType(Node node) {
        return distributionTypes.get(node);
    }

    public List<Parameter> getDistributionParameters(Node node) {
        return distributionParameters.get(node);
    }

    /**
     * Returns the comparison of parmeter a to parameter b.
     */
    public ParamComparison getParamComparison(Parameter a, Parameter b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }

        ParameterPair pair1 = new ParameterPair(a, b);
        ParameterPair pair2 = new ParameterPair(b, a);

        if (paramComparisons.containsKey(pair1)) {
            return paramComparisons.get(pair1);
        } else if (paramComparisons.containsKey(pair2)) {
            return paramComparisons.get(pair2);
        } else {
            return ParamComparison.NC;
        }
    }

    /**
     * Returns the comparison of parmeter a to parameter b.
     */
    public void setParamComparison(Parameter a, Parameter b,
                                   ParamComparison comparison) {
        if (a == null || b == null || comparison == null) {
            throw new NullPointerException();
        }

        ParameterPair pair1 = new ParameterPair(a, b);
        ParameterPair pair2 = new ParameterPair(b, a);

        paramComparisons.remove(pair2);
        paramComparisons.remove(pair1);

        if (comparison != ParamComparison.NC) {
            paramComparisons.put(pair1, comparison);
        }
    }

    @Override
	public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\nSEM PM:");
        buf.append("\n\tParameters:");

        for (Parameter parameter : parameters) {
            buf.append("\n\t\t").append(parameter);
        }

        buf.append("\n\tNodes: ");
        buf.append(nodes);

        buf.append("\n\tVariable nodes: ");
        buf.append(variableNodes);

        buf.append("\n\tDistributions: ");
        buf.append(distributionTypes);

        return buf.toString();
    }

    //============================PRIVATE METHODS======================//

    private void removeDistribution(Node node) {
        distributionTypes.remove(node);
        parameters.removeAll(getDistributionParameters(node));
    }

    private void initializeNodes(SemGraph graph) {
        this.nodes = Collections.unmodifiableList(graph.getNodes());
    }

    private void initializeVariableNodes() {
        List<Node> varNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            Node node = (node1);

            if (node.getNodeType() == NodeType.MEASURED ||
                    node.getNodeType() == NodeType.LATENT) {
                varNodes.add(node);
            }
        }

        this.variableNodes = Collections.unmodifiableList(varNodes);
    }

    private void initializeParams() {
        parameters = new ArrayList<Parameter>();
        List<Edge> edges = graph.getEdges();

        // Add linear coefficient parameters for all directed edges that
        // aren't error edges.
        for (Edge edge : edges) {
            if (edge.getNode1() == edge.getNode2()) {
                throw new IllegalStateException("There should not be any" +
                        "edges from a node to itself in a SemGraph: " + edge);
            }

            if (!SemGraph.isErrorEdge(edge) &&
                    edge.getEndpoint1() == Endpoint.TAIL &&
                    edge.getEndpoint2() == Endpoint.ARROW) {
                Parameter param = new Parameter(newBName(), ParamType.COEF,
                        edge.getNode1(), edge.getNode2());

                param.setDistribution(new Split(0.5, 1.5));
//                param.setDistribution(new SplitDistributionSpecial(0.5, 1.5));
//                param.setDistribution(new UniformDistribution(-0.2, 0.2));
                parameters.add(param);
            }
        }

        distributionTypes = new HashMap<Node, DistributionType>();
        distributionParameters = new HashMap<Node, List<Parameter>>();

        for (Node node : getVariableNodes()) {
            addExogenousDistribution(node, DistributionType.NORMAL);
        }
    }

    private void addExogenousDistribution(Node node, DistributionType type) {
        distributionTypes.put(node, type);
        int numArgs = DistributionType.NORMAL.getNumArgs();

        List<Parameter> _parameters = new LinkedList<Parameter>();

        for (int i = 0; i < numArgs; i++) {
            Parameter parameter = new Parameter(newDName(), ParamType.DIST, node, node);
            _parameters.add(parameter);
            parameters.add(parameter);
        }

        distributionParameters.put(node, _parameters);
    }

    /**
     * Returns a unique (for this PM) parameter name beginning with the letter
     * "B".
     */
    private String newBName() {
        return "B" + (++this.bIndex);
    }

    /**
     * Returns a unique (for this PM) parameter name beginning with the letter
     * "D".
     */
    private String newDName() {
        return "D" + (++this.dIndex);
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

        if (graph == null) {
            throw new NullPointerException();
        }

        if (nodes == null) {
            throw new NullPointerException();
        }

        if (parameters == null) {
            throw new NullPointerException();
        }

        if (variableNodes == null) {
            throw new NullPointerException();
        }

        if (paramComparisons == null) {
            throw new NullPointerException();
        }
    }
}

