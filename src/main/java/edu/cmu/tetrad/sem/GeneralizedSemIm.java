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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradSerializable;
import pal.math.ConjugateDirectionSearch;
import pal.math.MultivariateFunction;

import java.text.NumberFormat;
import java.util.*;

/**
 * Represents a generalized SEM instantiated model. The parameteric form of this model allows arbitrary
 */
public class GeneralizedSemIm implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The wrapped PM, that holds all of the expressions and structure for the model.
     */
    private GeneralizedSemPm pm;

    /**
     * A map from parameters names to their values--these form the context for evaluating expressions.
     * Variables do not appear in this list. All parameters are double-valued.
     */
    private Map<String, Double> parameterValues;

    /**
     * True iff only positive data should be simulated.
     */
    private boolean simulatePositiveDataOnly = false;


    /**
     * Constructs a new GeneralizedSemIm from the given GeneralizedSemPm by picking values for each of
     * the parameters from their initial distributions.
     *
     * @param pm the GeneralizedSemPm. Includes all of the equations and distributions of the model.
     */
    public GeneralizedSemIm(GeneralizedSemPm pm) {
        this.pm = new GeneralizedSemPm(pm);

        this.parameterValues = new HashMap<String, Double>();

        Set<String> parameters = pm.getParameters();

        for (String parameter : parameters) {
            Expression expression = pm.setParameterExpression(parameter);

            Context context = new Context() {
                public Double getValue(String var) {
                    return parameterValues.get(var);
                }
            };

            double initialValue = expression.evaluate(context);
            parameterValues.put(parameter, initialValue);
        }
    }

    public GeneralizedSemIm(GeneralizedSemPm pm, SemIm semIm) {
        this(pm);
        SemPm semPm = semIm.getSemPm();

        Set<String> parameters = pm.getParameters();

        // If there are any missing parameters, just ignore the sem IM.
        for (String parameter : parameters) {
            Parameter paramObject = semPm.getParameter(parameter);

            if (paramObject == null) {
                return;
            }
        }

        for (String parameter : parameters) {
            Parameter paramObject = semPm.getParameter(parameter);

            if (paramObject == null) {
                throw new IllegalArgumentException("Parameter missing from Gaussian SEM IM: " + parameter);
            }

            double value = semIm.getParamValue(paramObject);

            if (paramObject.getType() == ParamType.VAR) {
                value = Math.sqrt(value);
            }

            setParameterValue(parameter, value);
        }
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GeneralizedSemIm serializableInstance() {
        return new GeneralizedSemIm(GeneralizedSemPm.serializableInstance());
    }

    /**
     * @return a copy of the stored GeneralizedSemPm.
     */
    public GeneralizedSemPm getGeneralizedSemPm() {
        return new GeneralizedSemPm(pm);
    }

    /**
     * @param parameter The parameter whose values is to be set.
     * @param value     The double value that <code>param</code> is to be set to.
     */
    public void setParameterValue(String parameter, double value) {
        if (parameter == null) {
            throw new NullPointerException("Parameter not specified.");
        }

        if (!parameterValues.keySet().contains(parameter)) {
            throw new IllegalArgumentException("Not a parameter in this model: " + parameter);
        }

        parameterValues.put(parameter, value);
    }

    /**
     * @param parameter The parameter whose value is to be retrieved.
     * @return The retrieved value.
     */
    public double getParameterValue(String parameter) {
        if (parameter == null) {
            throw new NullPointerException("Parameter not specified.");
        }

        if (!parameterValues.keySet().contains(parameter)) {
            throw new IllegalArgumentException("Not a parameter in this model: " + parameter);
        }

        return parameterValues.get(parameter);
    }

    /**
     * @return the user's String formula with numbers substituted for parameters, where substitutions exist.
     */
    public String getNodeSubstitutedString(Node node) {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        String expressionString = pm.getNodeExpressionString(node);

        if (expressionString == null) return null;

        ExpressionLexer lexer = new ExpressionLexer(expressionString);
        StringBuilder buf = new StringBuilder();
        Token token;

        while ((token = lexer.nextTokenIncludingWhitespace()) != Token.EOF) {
            String tokenString = lexer.getTokenString();

            if (token == Token.PARAMETER) {
                Double value = parameterValues.get(tokenString);

                if (value != null) {
                    buf.append(nf.format(value));
                    continue;
                }
            }

            buf.append(tokenString);
        }

        return buf.toString();
    }

    /**
     * @param node              The node whose expression is being evaluated.
     * @param substitutedValues A mapping from Strings parameter names to Double values; these values will be
     *                          substituted for the stored values where applicable.
     * @return the expression string with values substituted for parameters.
     */
    public String getNodeSubstitutedString(Node node, Map<String, Double> substitutedValues) {
        if (node == null) {
            throw new NullPointerException();
        }

        if (substitutedValues == null) {
            throw new NullPointerException();
        }

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        String expressionString = pm.getNodeExpressionString(node);

        ExpressionLexer lexer = new ExpressionLexer(expressionString);
        StringBuilder buf = new StringBuilder();
        Token token;

        while ((token = lexer.nextTokenIncludingWhitespace()) != Token.EOF) {
            String tokenString = lexer.getTokenString();

            if (token == Token.PARAMETER) {
                Double value = substitutedValues.get(tokenString);

                if (value == null) {
                    value = parameterValues.get(tokenString);
                }

                if (value != null) {
                    buf.append(nf.format(value));
                    continue;
                }
            }

            buf.append(tokenString);
        }

        return buf.toString();
    }

    /**
     * Returns a String representation of the IM, in this case a lsit of parameters and their values.
     */
    @Override
	public String toString() {
        List<String> parameters = new ArrayList<String>(pm.getParameters());
        Collections.sort(parameters);
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        StringBuilder buf = new StringBuilder();
        GeneralizedSemPm pm = getGeneralizedSemPm();
        buf.append("\nVariable nodes:\n");

        for (Node node : pm.getVariableNodes()) {
            String string = getNodeSubstitutedString(node);
            buf.append("\n" + node + " = " + string);
        }

        buf.append("\n\nErrors:\n");

        for (Node node : pm.getErrorNodes()) {
            String string = getNodeSubstitutedString(node);
            buf.append("\n" + node + " ~ " + string);
        }

        buf.append("\n\nParameter values:\n");
        for (String parameter : parameters) {
            double value = getParameterValue(parameter);
            buf.append("\n" + parameter + " = " + nf.format(value));
        }

        return buf.toString();
    }

    public DataSet simulateData(int sampleSize, boolean latentDataSaved) {
        if (pm.getGraph().isTimeLagModel()) {
            return simulateTimeSeries(sampleSize);
        }

//        return simulateDataRecursive1(sampleSize, latentDataSaved);
//        return simulateDataMinimizeSurface(sampleSize, latentDataSaved);
        return simulateDataAvoidInfinity(sampleSize, latentDataSaved);
//        return simulateDataNSteps(sampleSize, latentDataSaved);
    }

    private DataSet simulateTimeSeries(int sampleSize) {
        SemGraph semGraph = new SemGraph(getSemPm().getGraph());
        semGraph.setShowErrorTerms(true);
        TimeLagGraph timeLagGraph = getSemPm().getGraph().getTimeLagGraph();

        List<Node> variables = new ArrayList<Node>();

        for (Node node : timeLagGraph.getLag0Nodes()) {
            if (node.getNodeType() == NodeType.ERROR) continue;
            variables.add(new ContinuousVariable(timeLagGraph.getNodeId(node).getName()));
        }

        List<Node> lag0Nodes = timeLagGraph.getLag0Nodes();

        for (Node node : new ArrayList<Node>(lag0Nodes)) {
            if (node.getNodeType() == NodeType.ERROR) {
                lag0Nodes.remove(node);
            }
        }

        DataSet fullData = new ColtDataSet(sampleSize, variables);

        Map<Node, Integer> nodeIndices = new HashMap<Node, Integer>();

        for (int i = 0; i < lag0Nodes.size(); i++) {
            nodeIndices.put(lag0Nodes.get(i), i);
        }

        Graph contemporaneousDag = timeLagGraph.subgraph(timeLagGraph.getLag0Nodes());

        List<Node> tierOrdering = contemporaneousDag.getTierOrdering();

        for (Node node : new ArrayList<Node>(tierOrdering)) {
            if (node.getNodeType() == NodeType.ERROR) {
                tierOrdering.remove(node);
            }
        }

        final Map<String, Double> variableValues = new HashMap<String, Double>();

        Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                } else {
                    return RandomUtil.getInstance().nextNormal(0, 1);
                }
            }
        };

        ROW:
        for (int currentStep = 0; currentStep < sampleSize; currentStep++) {
            for (Node node : tierOrdering) {
                Expression expression = pm.getNodeExpression(node);
                double value = expression.evaluate(context);

                if (isSimulatePositiveDataOnly() && value < 0) {
                    currentStep--;
                    continue ROW;
                }

                int col = nodeIndices.get(node);
                fullData.setDouble(currentStep, col, value);
                variableValues.put(node.getName(), value);
                System.out.println("Putting lag 0 " + node.getName() + " " + value);
            }

            System.out.println("lag 0 nodes " + lag0Nodes);

            for (Node node : lag0Nodes) {
                TimeLagGraph.NodeId _id = timeLagGraph.getNodeId(node);

                for (int lag = 1; lag <= timeLagGraph.getMaxLag(); lag++) {
                    Node _node = timeLagGraph.getNode(_id.getName(), lag);
                    int col = lag0Nodes.indexOf(node);

                    if (_node == null) {
                        continue;
                    }

                    if (currentStep - lag + 1 >= 0) {
                        double _value = fullData.getDouble((currentStep - lag + 1), col);
                        variableValues.put(_node.getName(), _value);
                        System.out.println("Putting " + _node.getName() + " " + _value);
                    }
                }
            }
        }

        return fullData;
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Fast for large simulations but hangs for cyclic models.
     *
     * @param sampleSize > 0.
     * @return the simulated data set.
     */
    public DataSet simulateDataRecursive1(int sampleSize, boolean latentDataSaved) {
        final Map<String, Double> variableValues = new HashMap<String, Double>();

        Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                }

                throw new IllegalArgumentException("No value recorded for '" + term + "'");
            }
        };

        List<Node> variables = pm.getNodes();
        List<Node> continuousVariables = new LinkedList<Node>();
        List<Node> nonErrorVariables = pm.getVariableNodes();

        // Work with a copy of the variables, because their type can be set externally.
        for (Node node : nonErrorVariables) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            var.setNodeType(node.getNodeType());

            if (var.getNodeType() != NodeType.ERROR) {
                continuousVariables.add(var);
            }
        }

        DataSet fullDataSet = new ColtDataSet(sampleSize, continuousVariables);

        // Create some index arrays to hopefully speed up the simulation.
        SemGraph graph = pm.getGraph();
        List<Node> tierOrdering = graph.getFullTierOrdering();

        int[] tierIndices = new int[variables.size()];

        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = nonErrorVariables.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variables.size(); i++) {
            Node node = variables.get(i);
            List<Node> parents = graph.getParents(node);

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = parents.get(j);
                _parents[i][j] = variables.indexOf(_parent);
            }
        }

        // Do the simulation.
        ROW:
        for (int row = 0; row < sampleSize; row++) {
            variableValues.clear();

            for (int tier = 0; tier < tierOrdering.size(); tier++) {
                Node node = tierOrdering.get(tier);
                Expression expression = pm.getNodeExpression(node);
                double value = expression.evaluate(context);
                variableValues.put(node.getName(), value);

                int col = tierIndices[tier];

                if (col == -1) {
                    continue;
                }

                if (isSimulatePositiveDataOnly() && value < 0) {
                    row--;
                    continue ROW;
                }

                fullDataSet.setDouble(row, col, value);
            }
        }

        if (latentDataSaved) {
            return fullDataSet;
        } else {
            return DataUtils.restrictToMeasured(fullDataSet);
        }
    }

    public DataSet simulateDataMinimizeSurface(int sampleSize, boolean latentDataSaved) {
        final Map<String, Double> variableValues = new HashMap<String, Double>();

        final double func_tolerance = 1.0e-4;
        final double param_tolerance = 1.0e-3;

        List<Node> continuousVariables = new LinkedList<Node>();
        final List<Node> variableNodes = pm.getVariableNodes();

        // Work with a copy of the variables, because their type can be set externally.
        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            var.setNodeType(node.getNodeType());

            if (var.getNodeType() != NodeType.ERROR) {
                continuousVariables.add(var);
            }
        }

        DataSet fullDataSet = new ColtDataSet(sampleSize, continuousVariables);

        final Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                }

                throw new IllegalArgumentException("No value recorded for '" + term + "'");
            }
        };

        final double[] _metric = new double[1];

        MultivariateFunction function = new MultivariateFunction() {
            double metric;

            @Override
			public double evaluate(double[] doubles) {
                for (int i = 0; i < variableNodes.size(); i++) {
                    variableValues.put(variableNodes.get(i).getName(), doubles[i]);
                }

                double[] image = new double[doubles.length];

                for (int i = 0; i < variableNodes.size(); i++) {
                    Node node = variableNodes.get(i);
                    Expression expression = pm.getNodeExpression(node);
                    image[i] = expression.evaluate(context);

                    if (Double.isNaN(image[i])) {
                        throw new IllegalArgumentException("Undefined value for expression " + expression);
                    }
                }

                metric = 0.0;

                for (int i = 0; i < variableNodes.size(); i++) {
                    double diff = doubles[i] - image[i];
                    metric += diff * diff;
                }

                for (int i = 0; i < variableNodes.size(); i++) {
                    variableValues.put(variableNodes.get(i).getName(), image[i]);
                }

                _metric[0] = metric;

                return metric;
            }

            @Override
			public int getNumArguments() {
                return variableNodes.size();
            }

            @Override
			public double getLowerBound(int i) {
                return -10000;
            }

            @Override
			public double getUpperBound(int i) {
                return 10000;
            }

            public double getMetric() {
                return -metric;
            }

            public OrthogonalHints getOrthogonalHints() {
                return null; 
            }
        };

        ConjugateDirectionSearch search = new ConjugateDirectionSearch();
        search.step = 10.0;

        // Do the simulation.
        ROW:
        for (int row = 0; row < sampleSize; row++) {

            // Take random draws from error distributions.
            for (int i = 0; i < variableNodes.size(); i++) {
                Node variable = variableNodes.get(i);
                Node error = pm.getErrorNode(variable);

                Expression expression = pm.getNodeExpression(error);
                double value = expression.evaluate(context);

                if (Double.isNaN(value)) {
                    throw new IllegalArgumentException("Undefined value for expression: " + expression);
                }

                variableValues.put(error.getName(), value);
            }

            for (int i = 0; i < variableNodes.size(); i++) {
                Node variable = variableNodes.get(i);
                variableValues.put(variable.getName(), 0.0);// RandomUtil.getInstance().nextUniform(-5, 5));
            }

            while (true) {

                double[] values = new double[variableNodes.size()];

                for (int i = 0; i < values.length; i++) {
                    values[i] = variableValues.get(variableNodes.get(i).getName());
                }

                search.optimize(function, values, func_tolerance, param_tolerance);

                for (int i = 0; i < variableNodes.size(); i++) {
                    if (isSimulatePositiveDataOnly() && values[i] < 0) {
                        row--;
                        continue ROW;
                    }

                    variableValues.put(variableNodes.get(i).getName(), values[i]);
                    fullDataSet.setDouble(row, i, values[i]);
                }

                if (_metric[0] < 0.01) {
                    break; // while
                }
            }
        }

        if (latentDataSaved) {
            return fullDataSet;
        } else {
            return DataUtils.restrictToMeasured(fullDataSet);
        }
    }

    public DataSet simulateDataAvoidInfinity(int sampleSize, boolean latentDataSaved) {
        final Map<String, Double> variableValues = new HashMap<String, Double>();

        List<Node> continuousVariables = new LinkedList<Node>();
        final List<Node> variableNodes = pm.getVariableNodes();
//        System.out.println("AAA" + variableNodes);

        // Work with a copy of the variables, because their type can be set externally.
        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            var.setNodeType(node.getNodeType());

            if (var.getNodeType() != NodeType.ERROR) {
                continuousVariables.add(var);
            }
        }

        DataSet fullDataSet = new ColtDataSet(sampleSize, continuousVariables);

        final Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                }

                throw new IllegalArgumentException("No value recorded for '" + term + "'");
            }
        };

        boolean allInRange = true;
        int _count = -1;

        // Do the simulation.
        ROW:
        for (int row = 0; row < sampleSize; row++) {

            // Take random draws from error distributions.
            for (Node variable : variableNodes) {
                Node error = pm.getErrorNode(variable);

                Expression expression = pm.getNodeExpression(error);
                double value = expression.evaluate(context);

                if (Double.isNaN(value)) {
                    throw new IllegalArgumentException("Undefined value for expression: " + expression);
                }

                variableValues.put(error.getName(), value);
            }

            // Set the variable nodes to zero.
            for (Node variable : variableNodes) {
                Node error = pm.getErrorNode(variable);

                Expression expression = pm.getNodeExpression(error);
                double value = expression.evaluate(context);

                if (Double.isNaN(value)) {
                    throw new IllegalArgumentException("Undefined value for expression: " + expression);
                }

                variableValues.put(variable.getName(), value); //0.0; //RandomUtil.getInstance().nextUniform(-1, 1));
            }

            // Repeatedly update variable values until one of them hits infinity or negative infinity or
            // convergence within delta.

            double delta = 1e-6;
            int count = -1;

            while (++count < 1000) {
                double[] values = new double[variableNodes.size()];

                for (int i = 0; i < values.length; i++) {
                    Node node = variableNodes.get(i);
                    Expression expression = pm.getNodeExpression(node);
                    double value = expression.evaluate(context);
                    values[i] = value;
                }


//                    for (int i = 0; i < values.length; i++) {
//                        System.out.print(values[i] + "\t");
//                    }
//
//                    System.out.println();

                // We can allow NaN values as well...   jdramsey 2009/8/31
//                    for (double value : values) {
////
////                        // Infinite values need to be allowed here to accomodate ln functions, since the starting
////                        // point is 0 for each variable.
//                        if (/*value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY ||*/ Double.isNaN(value)) {
//                            System.out.println(value);
//                            continue A;
//                        }
//                    }
//
                allInRange = true;

                for (int i = 0; i < values.length; i++) {
                    Node node = variableNodes.get(i);

                    if (!(Math.abs(variableValues.get(node.getName()) - values[i]) < delta)) {
                        allInRange = false;
                        break;
                    }
                }


                for (int i = 0; i < variableNodes.size(); i++) {
                    variableValues.put(variableNodes.get(i).getName(), values[i]);
                }

                if (allInRange) {
                    break;
                }
            }

            if (!allInRange && ++_count < 20) {
                row--;
                System.out.println("Trying another starting point...");
                continue ROW;
            }
            else if (_count >= 100) {
                System.out.println("Couldn't converge in simulation.");

                for (int i = 0; i < variableNodes.size(); i++) {
                    fullDataSet.setDouble(row, i, Double.NaN);
//                    continue ROW;
                    return fullDataSet;
                }
            }

            for (int i = 0; i < variableNodes.size(); i++) {
                double value = variableValues.get(variableNodes.get(i).getName());

                if (isSimulatePositiveDataOnly() && value < 0) {
                    row--;
                    continue ROW;
                }

                fullDataSet.setDouble(row, i, value);
            }
        }

        if (latentDataSaved) {
            return fullDataSet;
        } else {
            return DataUtils.restrictToMeasured(fullDataSet);
        }

    }

    DoubleMatrix1D simulateOneRecord(DoubleMatrix1D e) {
        final Map<String, Double> variableValues = new HashMap<String, Double>();

        final List<Node> variableNodes = pm.getVariableNodes();

        final Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                }

                throw new IllegalArgumentException("No value recorded for '" + term + "'");
            }
        };

        // Take random draws from error distributions.
        for (int i = 0; i < variableNodes.size(); i++) {
            Node error = pm.getErrorNode(variableNodes.get(i));
            variableValues.put(error.getName(), e.get(i));
        }

        // Set the variable nodes to zero.
        for (Node variable : variableNodes) {
            variableValues.put(variable.getName(), 0.0);// RandomUtil.getInstance().nextUniform(-5, 5));
        }

        // Repeatedly update variable values until one of them hits infinity or negative infinity or
        // convergence within delta.

        double delta = 1e-6;
        int count = -1;

        while (true && ++count < 10000) {
            double[] values = new double[variableNodes.size()];

            for (int i = 0; i < values.length; i++) {
                Node node = variableNodes.get(i);
                Expression expression = pm.getNodeExpression(node);
                double value = expression.evaluate(context);
                values[i] = value;
            }


//                    for (int i = 0; i < values.length; i++) {
//                        System.out.print(values[i] + "\t");
//                    }
//
//                    System.out.println();

            // We can allow NaN values as well...   jdramsey 2009/8/31
//                    for (double value : values) {
////
////                        // Infinite values need to be allowed here to accomodate ln functions, since the starting
////                        // point is 0 for each variable.
//                        if (/*value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY ||*/ Double.isNaN(value)) {
//                            System.out.println(value);
//                            continue A;
//                        }
//                    }
//
            boolean allInRange = true;

            for (int i = 0; i < values.length; i++) {
                Node node = variableNodes.get(i);

                if (!(Math.abs(variableValues.get(node.getName()) - values[i]) < delta)) {
                    allInRange = false;
                    break;
                }
            }


            for (int i = 0; i < variableNodes.size(); i++) {
                variableValues.put(variableNodes.get(i).getName(), values[i]);
            }

            if (allInRange) {
                break;
            }
        }

        DoubleMatrix1D _case = new DenseDoubleMatrix1D(e.size());

        for (int i = 0; i < variableNodes.size(); i++) {
            double value = variableValues.get(variableNodes.get(i).getName());
            _case.set(i, value);
        }

        return _case;
    }


    // If you use this remember to put the clause in to generate only positive data.
//    public DataSet simulateDataAvoidInfinity2(int sampleSize, boolean latentDataSaved) {
//        Graph graph = pm.getGraph();
//        final Map<String, Double> remembered = new HashMap<String, Double>();
//        List<Node> continuousVariables = new LinkedList<Node>();
//        final List<Node> variableNodes = pm.getVariableNodes();
//        double delta = 1e-6;
//
//
//        // Work with a copy of the variables, because their type can be set externally.
//        for (Node node : variableNodes) {
//            ContinuousVariable ar = new ContinuousVariable(node.getName());
//            ar.setNodeType(node.getNodeType());
//
//            if (ar.getNodeType() != NodeType.ERROR) {
//                continuousVariables.add(ar);
//            }
//        }
//
//        // Create the data set.
//        DataSet fullDataSet = new ColtDataSet(sampleSize, continuousVariables);
//
//        // Create the context, for evaluating formulas.
//        final Context context = new Context() {
//            public Double getValue(String term) {
//                Double value = parameterValues.get(term);
//
//                if (value != null) {
//                    return value;
//                }
//
//                value = remembered.get(term);
//
//                if (value != null) {
//                    return value;
//                }
//
//                throw new IllegalArgumentException("No value recorded for '" + term + "'");
//            }
//        };
//
//        // Do the simulation.
//        for (int row = 0; row < sampleSize; row++) {
//            Set<Node> changed = new HashSet<Node>(variableNodes);
//
//            A:
//            while (true) {
//
//                // Take random draws from error distributions.
//                for (Node variable : variableNodes) {
//                    Node error = pm.getErrorNode(variable);
//
//                    Expression expression = pm.getNodeExpression(error);
//                    double value = expression.evaluate(context);
//
//                    if (Double.isNaN(value)) {
//                        throw new IllegalArgumentException("Undefined value for expression: " + expression);
//                    }
//
//                    remembered.put(error.getName(), value);
//                }
//
//                // Set the remembered values to zero.
//                for (Node variable : variableNodes) {
//                    remembered.put(variable.getName(), 0.0);
//                }
//
//                // Repeatedly update variable values until one of them hits infinity or negative infinity or
//                // convergence within delta.
//
//                while (true) {
//                    double[] current = new double[variableNodes.size()];
//
//                    for (int i = 0; i < current.length; i++) {
//                        Node node = variableNodes.get(i);
//
//                        // If no parent has chnaged, don't update.
//                        List<Node> parents = graph.getParents(node);
//
//                        boolean _changed = false;
//
//                        for (Node parent : parents) {
//                            if (changed.contains(parent)) {
//                                _changed = true;
//                                break;
//                            }
//                        }
//
//                        if (!_changed) {
//                            continue;
//                        }
//
//                        Expression expression = pm.getNodeExpression(node);
//                        double value = expression.evaluate(context);
//                        current[i] = value;
////                        System.out.println("values[" + i + "] = " + values[i]);
//                    }
//
//                    for (double value : current) {
//                        if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY || Double.isNaN(value)) {
//                            continue A;
//                        }
//                    }
//
//                    changed.clear();
//
//                    // If current values are all within delta of remembered values, don't update.
//                    boolean allInRange = true;
//
//                    for (int i = 0; i < current.length; i++) {
//                        double delta2 = Math.abs(remembered.get(variableNodes.get(i).getName()) - current[i]);
//
//                        if (Math.abs(delta2) > delta) {
//                            changed.add(variableNodes.get(i));
//                            allInRange = false;
//                        }
//                    }
//
//                    if (allInRange) {
//                        break A;
//                    }
//
//                    // Remember the current values.
//                    for (int i = 0; i < variableNodes.size(); i++) {
//                        remembered.put(variableNodes.get(i).getName(), current[i]);
//                    }
//                }
//            }
//
//            // Write the remembered values to the data set.
//            for (int i = 0; i < variableNodes.size(); i++) {
//                double value = remembered.get(variableNodes.get(i).getName());
//                fullDataSet.setDouble(row, i, value);
//            }
//        }
//
//        if (latentDataSaved) {
//            return fullDataSet;
//        } else {
//            return DataUtils.restrictToMeasured(fullDataSet);
//        }
//
//    }

    public DataSet simulateDataNSteps(int sampleSize, boolean latentDataSaved) {
        final Map<String, Double> variableValues = new HashMap<String, Double>();

        List<Node> continuousVariables = new LinkedList<Node>();
        final List<Node> variableNodes = pm.getVariableNodes();

        // Work with a copy of the variables, because their type can be set externally.
        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            var.setNodeType(node.getNodeType());

            if (var.getNodeType() != NodeType.ERROR) {
                continuousVariables.add(var);
            }
        }

        DataSet fullDataSet = new ColtDataSet(sampleSize, continuousVariables);

        final Context context = new Context() {
            public Double getValue(String term) {
                Double value = parameterValues.get(term);

                if (value != null) {
                    return value;
                }

                value = variableValues.get(term);

                if (value != null) {
                    return value;
                }

                throw new IllegalArgumentException("No value recorded for '" + term + "'");
            }
        };

        // Do the simulation.
        ROW:
        for (int row = 0; row < sampleSize; row++) {

            // Take random draws from error distributions.
            for (Node variable : variableNodes) {
                Node error = pm.getErrorNode(variable);

                Expression expression = pm.getNodeExpression(error);
                double value = expression.evaluate(context);

                if (Double.isNaN(value)) {
                    throw new IllegalArgumentException("Undefined value for expression: " + expression);
                }

                variableValues.put(error.getName(), value);
            }

            // Set the variable nodes to zero.
            for (Node variable : variableNodes) {
                variableValues.put(variable.getName(), 0.0);// RandomUtil.getInstance().nextUniform(-5, 5));
            }

            // Repeatedly update variable values until one of them hits infinity or negative infinity or
            // convergence within delta.

            for (int m = 0; m < 1; m++) {
                double[] values = new double[variableNodes.size()];

                for (int i = 0; i < values.length; i++) {
                    Node node = variableNodes.get(i);
                    Expression expression = pm.getNodeExpression(node);
                    double value = expression.evaluate(context);

                    if (Double.isNaN(value)) {
                        throw new IllegalArgumentException("Undefined value for expression: " + expression);
                    }

                    values[i] = value;
//                        System.out.println("values[" + i + "] = " + values[i]);
                }

                for (double value : values) {
                    if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
                        row--;
                        continue ROW;
                    }
                }

                for (int i = 0; i < variableNodes.size(); i++) {
                    variableValues.put(variableNodes.get(i).getName(), values[i]);
                }

            }

            for (int i = 0; i < variableNodes.size(); i++) {
                double value = variableValues.get(variableNodes.get(i).getName());
                fullDataSet.setDouble(row, i, value);
            }
        }

        if (latentDataSaved) {
            return fullDataSet;
        } else {
            return DataUtils.restrictToMeasured(fullDataSet);
        }

    }


    public GeneralizedSemPm getSemPm() {
        return new GeneralizedSemPm(pm);
    }

    public void setSubstitutions(Map<String, Double> parameterValues) {
        for (String parameter : parameterValues.keySet()) {
            if (this.parameterValues.keySet().contains(parameter)) {
                this.parameterValues.put(parameter, parameterValues.get(parameter));
            }
        }
    }

    public boolean isSimulatePositiveDataOnly() {
        return simulatePositiveDataOnly;
    }

    public void setSimulatePositiveDataOnly(boolean simulatedPositiveDataOnly) {
        this.simulatePositiveDataOnly = simulatedPositiveDataOnly;
    }
}

