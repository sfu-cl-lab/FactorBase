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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.dist.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores a SEM model, pared down, for purposes of simulating data sets with
 * large numbers of variables and sample sizes. Assumes acyclicity.
 *
 * @author Joseph Ramsey
 */
public final class LargeSemSimulator {
    static final long serialVersionUID = 23L;

    private DoubleMatrix2D edgeCoef;
    private DoubleMatrix2D errCovar;
    private double[] variableMeans;

    /**
     * Used for some linear algebra calculations.
     */
    private transient Algebra algebra;
    private List<Node> variableNodes;
    private Graph graph;

    //=============================CONSTRUCTORS============================//

    public LargeSemSimulator(Graph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        this.graph = graph;

        this.variableNodes = graph.getNodes();
        int size = variableNodes.size();

        this.edgeCoef = new SparseDoubleMatrix2D(size, size);
        this.errCovar = new SparseDoubleMatrix2D(size, size);
        this.variableMeans = new double[size];

        Distribution edgeCoefDist = new Split(.5, 1.5);
        Distribution errorCovarDist = new Uniform(1.0, 3.0);
        Distribution meanDist = new Uniform(-1.0, 1.0);

        for (Edge edge : graph.getEdges()) {
            if (edge.getNode1().getNodeType() == NodeType.ERROR ||
                    edge.getNode2().getNodeType() == NodeType.ERROR) {
                continue;
            }

            Node tail = Edges.getDirectedEdgeTail(edge);
            Node head = Edges.getDirectedEdgeHead(edge);

            int _tail = variableNodes.indexOf(tail);
            int _head = variableNodes.indexOf(head);

            this.edgeCoef.set(_tail, _head, edgeCoefDist.nextRandom());
        }

        for (int i = 0; i < size; i++) {
            this.errCovar.set(i, i, errorCovarDist.nextRandom());
            this.variableMeans[i] = meanDist.nextRandom();
        }

//        System.out.println("Edge coefs: " + this.edgeCoef);
//        System.out.println("Error covars: " + this.errCovar);
//        System.out.println("Means: ");
//
//        for (int i = 0; i < size; i++) {
//            System.out.print(variableMeans[i] + "\t");
//        }
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Works, but will hang for cyclic models, and is very slow for
     * large numbers of variables (probably due to the heavyweight lookups of
     * various values--could be improved).
     */
    public DataSet simulateDataAcyclic(int sampleSize) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = getVariableNodes();

        // Make an empty data set.
        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            var.setNodeType(node.getNodeType());
            variables.add(var);
        }

//        System.out.println("Creating data set.");

        DataSet dataSet = new ColtDataSet(sampleSize, variables);
        constructSimulation(variableNodes, variables, sampleSize, dataSet);
        return dataSet;
    }

    public DataSet simulateDataAcyclic(DataSet dataSet) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = getVariableNodes();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            ContinuousVariable var = (ContinuousVariable) dataSet.getVariable(i);
            variables.add(var);

        }

        constructSimulation(variableNodes, variables, dataSet.getNumRows(), dataSet);
        return dataSet;
    }

    private void constructSimulation(List<Node> variableNodes,
                                     List<Node> variables, int sampleSize,
                                     DataSet dataSet) {
        // Create some index arrays to hopefully speed up the simulation.
        Graph graph = getGraph();
        List<Node> tierOrdering = graph.getTierOrdering();

        int[] tierIndices = new int[variableNodes.size()];
                                                                             
        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = variableNodes.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variableNodes.size(); i++) {
            Node node = variableNodes.get(i);
            List parents = graph.getParents(node);

            for (Iterator j = parents.iterator(); j.hasNext();) {
                Node _node = (Node) j.next();

                if (_node.getNodeType() == NodeType.ERROR) {
                    j.remove();
                }
            }

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = (Node) parents.get(j);
                _parents[i][j] = variableNodes.indexOf(_parent);
            }
        }

//        System.out.println("Starting simulation.");

        DoubleMatrix2D _data = ((ColtDataSet) dataSet).getDoubleDataNoCopy();

        // Do the simulation.
        for (int row = 0; row < sampleSize; row++) {
            if (row % 100 == 0) System.out.println("Row " + row);

            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
                double value = RandomUtil.getInstance().nextNormal(0, 1) *
                                errCovar.get(col, col);

                for (int j = 0; j < _parents[col].length; j++) {
                    int parent = _parents[col][j];
//                    value += dataSet.getDouble(row, parent) *
//                            edgeCoef.get(parent, col);

                    value += _data.get(row, parent) *
                            edgeCoef.get(parent, col);
                }

                value += variableMeans[col];
//                dataSet.setDouble(row, col, value);

                _data.set(row, col, value);
            }
        }
    }

    public Algebra getAlgebra() {
        if (algebra == null) {
            algebra = new Algebra();
        }

        return algebra;
    }

    public double getCoefficient(Node from, Node to) {
        int node1Index = variableNodes.indexOf(getVariable(from.getName()));
        int node2Index = variableNodes.indexOf(getVariable(to.getName()));

        return edgeCoef.get(node1Index, node2Index);
    }

    private Node getVariable(String name) {
        for (Node node : variableNodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }

        return null;
    }

    public double getErrorCovariance(Node node) {
        int index = variableNodes.indexOf(getVariable(node.getName()));
        return errCovar.get(index, index);
    }

    private List<Node> getVariableNodes() {
        return variableNodes;
    }

    public Graph getGraph() {
        return graph;
    }
}


