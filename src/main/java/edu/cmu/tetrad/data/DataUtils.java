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

package edu.cmu.tetrad.data;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;

import java.rmi.MarshalledObject;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Some static utility methods for dealing with data sets.
 *
 * @author Various folks.
 */
public final class DataUtils {


    public static void copyColumn(Node node, DataSet source, DataSet dest) {
        int sourceColumn = source.getColumn(node);
        int destColumn = dest.getColumn(node);
        if (sourceColumn < 0) {
            throw new NullPointerException("The given node was not in the source dataset");
        }
        if (destColumn < 0) {
            throw new NullPointerException("The given node was not in the destination dataset");
        }
        int sourceRows = source.getNumRows();
        int destRows = dest.getNumRows();
        if (node instanceof ContinuousVariable) {
            for (int i = 0; i < destRows && i < sourceRows; i++) {
                dest.setDouble(i, destColumn, source.getDouble(i, sourceColumn));
            }
        } else if (node instanceof DiscreteVariable) {
            for (int i = 0; i < destRows && i < sourceRows; i++) {
                dest.setInt(i, destColumn, source.getInt(i, sourceColumn));
            }
        } else {
            throw new IllegalArgumentException("The given variable most be discrete or continuous");
        }
    }


    /**
     * States whether the given column of the given data set is binary.
     *
     * @param data   Ibid.
     * @param column Ibid.
     * @return true iff the column is binary.
     */
    public static boolean isBinary(DataSet data, int column) {
        Node node = data.getVariable(column);
        int size = data.getNumRows();
        if (node instanceof DiscreteVariable) {
            for (int i = 0; i < size; i++) {
                int value = data.getInt(i, column);
                if (value != 1 && value != 0) {
                    return false;
                }
            }
        } else if (node instanceof ContinuousVariable) {
            for (int i = 0; i < size; i++) {
                double value = data.getDouble(i, column);
                if (value != 1.0 && value != 0.0) {
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("The given column is not discrete or continuous");
        }
        return true;
    }


    /**
     * Throws an exception just in case not all of the variables from
     * <code>source1</code> are found in <code>source2</code>. A variable from
     * <code>source1</code> is found in <code>source2</code> if it is equal to a
     * variable in <code>source2</code>.
     *
     * @param source1 The first variable source.
     * @param source2 The second variable source. (See the interface.)
     */
    public static void ensureVariablesExist(VariableSource source1,
                                            VariableSource source2) {
        List <Node> variablesNotFound = source1.getVariables();
        variablesNotFound.removeAll(source2.getVariables());

        if (!variablesNotFound.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expected to find these variables from the given Bayes PM " +
                            "\nin the given discrete data set, but didn't (note: " +
                            "\ncategories might be different or in the wrong order): " +
                            "\n" + variablesNotFound);
        }
    }

    /**
     * Returns the default category for index i. (The default category should
     * ALWAYS be obtained by calling this method.)
     *
     * @param index Ond plus the given index.
     */
    public static String defaultCategory(int index) {
        return Integer.toString(index);
    }

    /**
     * Adds missing data values to cases in accordance with probabilities
     * specified in a double array which has as many elements as there are
     * columns in the input dataset.  Hence if the first element of the array of
     * probabilities is alpha, then the first column will contain a -99 (or
     * other missing value code) in a given case with probability alpha. </p>
     * This method will be useful in generating datasets which can be used to
     * test algorithms that handle missing data and/or latent variables. </p>
     * Author:  Frank Wimberly
     *
     * @param inData The data to which random missing data is to be added.
     * @param probs  The probability of adding missing data to each column.
     * @return The new data sets with missing data added.
     */
    public static DataSet addMissingData(
            DataSet inData, double[] probs) {
        DataSet outData;

        try {
            outData = (DataSet) new MarshalledObject(inData).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (probs.length != outData.getNumColumns()) {
            throw new IllegalArgumentException(
                    "Wrong number of elements in prob array");
        }

        for (double prob : probs) {
            if (prob < 0.0 || prob > 1.0) {
                throw new IllegalArgumentException("Probability out of range");
            }
        }

        for (int j = 0; j < outData.getNumColumns(); j++) {
            Node variable = outData.getVariable(j);

            for (int i = 0; i < outData.getNumRows(); i++) {
                double test = RandomUtil.getInstance().nextDouble();

                if (test < probs[j]) {
                    outData.setObject(i, j,
                            ((Variable) variable).getMissingValueMarker());
                }
            }
        }

        return outData;
    }

    /**
     * A continuous data set used to construct some other serializable
     * instances.
     */
    public static DataSet continuousSerializableInstance() {
        List <Node> variables = new LinkedList <Node>();
        variables.add(new ContinuousVariable("X"));
        ColtDataSet dataSet = new ColtDataSet(10, variables);

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                dataSet.setDouble(i, j, RandomUtil.getInstance().nextDouble());
            }
        }

        return dataSet;
    }

    /**
     * A discrete data set used to construct some other serializable instances.
     */
    public static DataSet discreteSerializableInstance() {
        List <Node> variables = new LinkedList <Node>();
        variables.add(new DiscreteVariable("X", 2));
        DataSet dataSet = new ColtDataSet(2, variables);
        dataSet.setInt(0, 0, 0);
        dataSet.setInt(1, 0, 1);
        return dataSet;
    }

    /**
     * Returns true iff the data sets contains a missing value.
     */
    public static boolean containsMissingValue(DoubleMatrix2D data) {
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                if (Double.isNaN(data.getQuick(i, j))) {
                    return true;
                }
            }
        }

        return false;
    }


    public static boolean containsMissingValue(DataSet data) {
        for (int j = 0; j < data.getNumColumns(); j++) {
            Node node = data.getVariable(j);

            if (node instanceof ContinuousVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (Double.isNaN(data.getDouble(i, j))) {
                        return true;
                    }
                }
            }

            if (node instanceof DiscreteVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (data.getInt(i, j) == DiscreteVariable.MISSING_VALUE) {
                        System.out.println("data.getInt(i, j) + :" + i + ", " + j + "," + data.getInt(i, j));//zqian Nov 19 2013
                        System.out.println("data.getNumRows() + :" + data.getNumRows());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static DoubleMatrix2D standardizeData(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.copy();

        for (int j = 0; j < data2.columns(); j++) {
            double sum = 0.0;

            for (int i = 0; i < data2.rows(); i++) {
                sum += data2.get(i, j);
            }

            double mean = sum / data.rows();

            for (int i = 0; i < data.rows(); i++) {
                data2.set(i, j, data.get(i, j) - mean);
            }

            double norm = 0.0;

            for (int i = 0; i < data.rows(); i++) {
                double v = data2.get(i, j);
                norm += v * v;
            }

            norm = Math.sqrt(norm / (data.rows() - 1));

            for (int i = 0; i < data.rows(); i++) {
                data2.set(i, j, data2.get(i, j) / norm);
            }
        }

        return data2;
    }

    public static double[] standardizeData(double[] data) {
        double[] data2 = new double[data.length];

        double sum = 0.0;

        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }

        double mean = sum / data.length;

        for (int i = 0; i < data.length; i++) {
            data2[i] = data[i] - mean;
        }

        double norm = 0.0;

        for (int i = 0; i < data2.length; i++) {
            double v = data2[i];
            norm += v * v;
        }

        norm = Math.sqrt(norm / (data2.length - 1));

        for (int i = 0; i < data2.length; i++) {
            data2[i] = data2[i] / norm;
        }

        return data2;
    }

    public static DoubleArrayList standardizeData(DoubleArrayList data) {
        DoubleArrayList data2 = new DoubleArrayList(data.size());

        double sum = 0.0;

        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i);
        }

        double mean = sum / data.size();

        for (int i = 0; i < data.size(); i++) {
            data2.add(data.get(i) - mean);
        }

        double norm = 0.0;

        for (int i = 0; i < data2.size(); i++) {
            double v = data2.get(i);
            norm += v * v;
        }

        norm = Math.sqrt(norm / (data2.size() - 1));

        for (int i = 0; i < data2.size(); i++) {
            data2.set(i, data2.get(i) / norm);
        }

        return data2;
    }

    public static List <DataSet> standardizeData(List <DataSet> dataList) {
        List <DataSet> dataSets = new ArrayList <DataSet>();

        for (DataSet dataSet : dataList) {
            dataSets.add(dataSet);
        }

        List <DataSet> inList = dataSets;
        List <DataSet> outList = new ArrayList <DataSet>();

        for (DataModel model : inList) {
            if (!(model instanceof DataSet)) {
                throw new IllegalArgumentException("Not a data set: " + model.getName());
            }

            DataSet dataSet = (DataSet) model;

            if (!(dataSet.isContinuous())) {
                throw new IllegalArgumentException("Not a continuous data set: " + dataSet.getName());
            }

            DoubleMatrix2D data2 = DataUtils.standardizeData(dataSet.getDoubleData());
            List <Node> list = dataSet.getVariables();
            List <Node> list2 = new ArrayList <Node>();

            for (Node node : list) {
                list2.add(node);
            }

            ColtDataSet dataSet2 = ColtDataSet.makeContinuousData(list2, data2);
            outList.add(dataSet2);
        }

        return outList;
    }

    public static DataSet standardizeData(DataSet dataSet) {
        List <DataSet> dataSets = Collections.singletonList(dataSet);
        List <DataSet> outList = standardizeData(dataSets);
        return outList.get(0);
    }

    public static DoubleMatrix2D centerData(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.copy();

        for (int j = 0; j < data2.columns(); j++) {
            double sum = 0.0;

            for (int i = 0; i < data2.rows(); i++) {
                sum += data2.get(i, j);
            }

            double mean = sum / data.rows();

            for (int i = 0; i < data.rows(); i++) {
                data2.set(i, j, data.get(i, j) - mean);
            }
        }

        return data2;
    }

    public static List <DataSet> centerData(List <DataSet> dataList) {
        List <DataSet> dataSets = new ArrayList <DataSet>();

        for (DataSet dataSet : dataList) {
            dataSets.add(dataSet);
        }

        List <DataSet> inList = dataSets;
        List <DataSet> outList = new ArrayList <DataSet>();

        for (DataModel model : inList) {
            if (!(model instanceof DataSet)) {
                throw new IllegalArgumentException("Not a data set: " + model.getName());
            }

            DataSet dataSet = (DataSet) model;

            if (!(dataSet.isContinuous())) {
                throw new IllegalArgumentException("Not a continuous data set: " + dataSet.getName());
            }

            DoubleMatrix2D data2 = DataUtils.centerData(dataSet.getDoubleData());
            List <Node> list = dataSet.getVariables();
            List <Node> list2 = new ArrayList <Node>();

            for (Node node : list) {
                list2.add(node);
            }

            ColtDataSet dataSet2 = ColtDataSet.makeContinuousData(list2, data2);
            outList.add(dataSet2);
        }

        return outList;
    }


    public static DataSet discretize(DataSet dataSet, int numCategories, boolean variablesCopied) {
        Discretizer discretizer = new Discretizer(dataSet);
        discretizer.setVariablesCopied(variablesCopied);

        for (Node node : dataSet.getVariables()) {
            discretizer.equalCounts(node, numCategories);
        }

        return discretizer.discretize();
    }

    /**
     * Calculates the equal freq discretization spec
     */
    public static DiscretizationSpec getEqualFreqDiscretizationSpec(int numCategories, double[] data) {
        double[] breakpoints = Discretizer.getEqualFrequencyBreakPoints(data, numCategories);
        List <String> cats = defaultCategories(numCategories);
        return new ContinuousDiscretizationSpec(breakpoints, cats);
    }

    public static List <String> defaultCategories(int numCategories) {
        List <String> categories = new LinkedList <String>();
        for (int i = 0; i < numCategories; i++) {
            categories.add(defaultCategory(i));
        }
        return categories;
    }

    public static List <Node> createContinuousVariables(String[] varNames) {
        List <Node> variables = new LinkedList <Node>();

        for (String varName : varNames) {
            variables.add(new ContinuousVariable(varName));
        }

        return variables;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static DoubleMatrix2D subMatrix(ICovarianceMatrix m, Node x, Node y, List <Node> z) {
        if (x == null) {
            throw new NullPointerException();
        }

        if (y == null) {
            throw new NullPointerException();
        }

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        List <Node> variables = m.getVariables();
        DoubleMatrix2D _covMatrix = m.getMatrix();

        // Create index array for the given variables.
        int[] indices = new int[2 + z.size()];

        indices[0] = variables.indexOf(x);
        indices[1] = variables.indexOf(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = variables.indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix = _covMatrix.viewSelection(indices, indices);

        if (containsMissingValue(submatrix)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        return submatrix;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static DoubleMatrix2D subMatrix(DoubleMatrix2D m, List <Node> variables, Node x, Node y, List <Node> z) {
        if (x == null) {
            throw new NullPointerException();
        }

        if (y == null) {
            throw new NullPointerException();
        }

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        DoubleMatrix2D _covMatrix = m;

        // Create index array for the given variables.
        int[] indices = new int[2 + z.size()];

        indices[0] = variables.indexOf(x);
        indices[1] = variables.indexOf(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = variables.indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix = _covMatrix.viewSelection(indices, indices);

//        if (containsMissingValue(submatrix)) {
//            throw new IllegalArgumentException(
//                    "Please remove or impute missing values first.");
//        }

        return submatrix;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static DoubleMatrix2D subMatrix(DoubleMatrix2D m, Map <Node, Integer> indexMap, Node x, Node y, List <Node> z) {
        if (x == null) {
            throw new NullPointerException();
        }

        if (y == null) {
            throw new NullPointerException();
        }

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        DoubleMatrix2D _covMatrix = m;

        // Create index array for the given variables.
        int[] indices = new int[2 + z.size()];

        indices[0] = indexMap.get(x);
        indices[1] = indexMap.get(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = indexMap.get(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix = _covMatrix.viewSelection(indices, indices);

//        if (containsMissingValue(submatrix)) {
//            throw new IllegalArgumentException(
//                    "Please remove or impute missing values first.");
//        }

        return submatrix;
    }


    /**
     * Returns a new data sets, copying the given on but with the columns shuffled.
     *
     * @param dataSet The data set to shuffle.
     * @return Ibid.
     */
    public static DataSet shuffleColumns(DataSet dataSet) {
        int numVariables;

        numVariables = dataSet.getNumColumns();

        List <Integer> indicesList = new ArrayList <Integer>();
        for (int i = 0; i < numVariables; i++) indicesList.add(i);
        Collections.shuffle(indicesList);

        int[] indices = new int[numVariables];

        for (int i = 0; i < numVariables; i++) {
            indices[i] = indicesList.get(i);
        }

        return dataSet.subsetColumns(indices);
    }

    public static DataSet convertNumericalDiscreteToContinuous(
            DataSet dataSet) throws NumberFormatException {
        List <Node> variables = new ArrayList <Node>();

        for (Node variable : dataSet.getVariables()) {
            if (variable instanceof ContinuousVariable) {
                variables.add(variable);
            } else {
                variables.add(new ContinuousVariable(variable.getName()));
            }
        }

        DataSet continuousData = new ColtDataSet(dataSet.getNumRows(),
                variables);

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            Node variable = dataSet.getVariable(j);

            if (variable instanceof ContinuousVariable) {
                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    continuousData.setDouble(i, j, dataSet.getDouble(i, j));
                }
            } else {
                DiscreteVariable discreteVariable = (DiscreteVariable) variable;

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    int index = dataSet.getInt(i, j);
                    String catName = discreteVariable.getCategory(index);
                    double value;

                    if (catName.equals("*")) {
                        value = Double.NaN;
                    } else {
                        value = Double.parseDouble(catName);
                    }

                    continuousData.setDouble(i, j, value);
                }
            }
        }

        return continuousData;
    }

    public static DataSet concatenateData(DataSet dataSet1, DataSet dataSet2) {
        List <Node> vars1 = dataSet1.getVariables();
        List <Node> vars2 = dataSet2.getVariables();
        Map <String, Integer> varMap2 = new HashMap <String, Integer>();
        for (int i = 0; i < vars2.size(); i++) {
            varMap2.put(vars2.get(i).getName(), i);
        }
        int rows1 = dataSet1.getNumRows();
        int rows2 = dataSet2.getNumRows();
        int cols1 = dataSet1.getNumColumns();

        DoubleMatrix2D concatMatrix = new DenseDoubleMatrix2D(rows1 + rows2, cols1);
        DoubleMatrix2D matrix1 = dataSet1.getDoubleData();
        DoubleMatrix2D matrix2 = dataSet2.getDoubleData();

        for (int i = 0; i < vars1.size(); i++) {
            int var2 = varMap2.get(vars1.get(i).getName());
            for (int j = 0; j < rows1; j++) {
                concatMatrix.set(j, i, matrix1.get(j, i));
            }
            for (int j = 0; j < rows2; j++) {
                concatMatrix.set(j + rows1, i, matrix2.get(j, var2));
            }
        }

        DataSet concatData = ColtDataSet.makeData(vars1, concatMatrix);

        return concatData;
    }

    public static DataSet concatenateDataSets(List <DataSet> dataSets) {
        DataSet concatenated = null;

        for (DataSet dataSet : dataSets) {
            if (concatenated == null) {
                concatenated = dataSet;
            } else {
                concatenated = concatenateData(concatenated, dataSet);
            }
        }

        return concatenated;
    }

    public static DataSet concatenateDataNoChecks(List <DataSet> datasets) {
        List <Node> vars1 = datasets.get(0).getVariables();
        int cols = vars1.size();
        int rows = 0;
        for (DataSet dataset : datasets) {
            rows += dataset.getNumRows();
        }

        DoubleMatrix2D concatMatrix = new DenseDoubleMatrix2D(rows, vars1.size());

        int index = 0;

        for (DataSet dataset : datasets) {
            for (int i = 0; i < dataset.getNumRows(); i++) {
                for (int j = 0; j < cols; j++) {
                    concatMatrix.set(index, j, dataset.getDouble(i, j));
                }
                index++;
            }
        }

        DataSet concatData = ColtDataSet.makeData(vars1, concatMatrix);

        return concatData;
    }


    public static DataSet concatenateDiscreteData(DataSet dataSet1, DataSet dataSet2) {
        List <Node> vars = dataSet1.getVariables();
        int rows1 = dataSet1.getNumRows();
        int rows2 = dataSet2.getNumRows();
        DataSet concatData = new ColtDataSet(rows1 + rows2, vars);

        for (Node var : vars) {
            int var1 = dataSet1.getColumn(dataSet1.getVariable(var.toString()));
            int varc = concatData.getColumn(concatData.getVariable(var.toString()));
            for (int i = 0; i < rows1; i++) {
                concatData.setInt(i, varc, dataSet1.getInt(i, var1));
            }
            int var2 = dataSet2.getColumn(dataSet2.getVariable(var.toString()));
            for (int i = 0; i < rows2; i++) {
                concatData.setInt(i + rows1, varc, dataSet2.getInt(i, var2));
            }
        }

        return concatData;
    }

    public static DataSet noisyZeroes(DataSet dataSet) {
        dataSet = new ColtDataSet((ColtDataSet) dataSet);

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            boolean allZeroes = true;

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                if (dataSet.getDouble(i, j) != 0) {
                    allZeroes = false;
                    break;
                }
            }

            if (allZeroes) {
                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    dataSet.setDouble(i, j, RandomUtil.getInstance().nextNormal(0, 1));
                }
            }
        }

        return dataSet;
    }

    public static void printAndersonDarlingPs(DataSet dataSet) {
        System.out.println("Anderson Darling P value for Variables\n");

        NumberFormat nf = new DecimalFormat("0.0000");
        DoubleMatrix2D m = dataSet.getDoubleData();

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            double[] x = m.viewColumn(j).toArray();
            double p = new AndersonDarlingTest(x).getP();
            System.out.println("For " + dataSet.getVariable(j) +
                    ", Anderson-Darling p = " + nf.format(p)
                    + (p > 0.05 ? " = Gaussian" : " = Nongaussian"));
        }

    }

    public static DataSet restrictToMeasured(DataSet fullDataSet) {
        List <Node> measuredVars = new ArrayList <Node>();

        for (Node node : fullDataSet.getVariables()) {
            if (node.getNodeType() == NodeType.MEASURED) {
                measuredVars.add(node);
            }
        }

        return fullDataSet.subsetColumns(measuredVars);
    }

    public static DoubleMatrix2D cov(DoubleMatrix2D data) {
        DoubleMatrix2D cov = new DenseDoubleMatrix2D(data.columns(), data.columns());

        for (int i = 0; i < data.columns(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                cov.set(i, j, StatUtils.covariance(data.viewColumn(i).toArray(), data.viewColumn(j).toArray()));
            }
        }

        return cov;

    }

    public static DoubleMatrix2D corr(DoubleMatrix2D data) {
        DoubleMatrix2D corr = new DenseDoubleMatrix2D(data.columns(), data.columns());

        for (int i = 0; i < data.columns(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                corr.set(i, j, StatUtils.correlation(data.viewColumn(i).toArray(), data.viewColumn(j).toArray()));
            }
        }

        return corr;

    }

    public static DoubleMatrix1D mean(DoubleMatrix2D data) {
        DoubleMatrix1D mean = new DenseDoubleMatrix1D(data.columns());

        for (int i = 0; i < data.columns(); i++) {
            mean.set(i, StatUtils.mean(data.viewColumn(i).toArray()));
        }

        return mean;

    }

    /**
     * Returns a simulation from the given covariance matrix, zero means.
     *
     * @param cov The variables and covariance matrix over the variables.
     * @return The simulated data.
     */
    public static DataSet choleskySimulation(CovarianceMatrix cov) {
        System.out.println(cov);
        int sampleSize = cov.getSampleSize();

        List <Node> variables = cov.getVariables();
        DataSet dataSet = new ColtDataSet(sampleSize, variables);
        DoubleMatrix2D _cov = cov.getMatrix().copy();

        DoubleMatrix2D cholesky = MatrixUtils.choleskyC(_cov);

        System.out.println("Cholesky decomposition" + cholesky);

        // Simulate the data by repeatedly calling the Cholesky.exogenousData
        // method. Store only the data for the measured variables.
        for (int row = 0; row < sampleSize; row++) {

            // Step 1. Generate normal samples.
            double exoData[] = new double[cholesky.rows()];

            for (int i = 0; i < exoData.length; i++) {
                exoData[i] = RandomUtil.getInstance().nextNormal(0, 1);
            }

            // Step 2. Multiply by cholesky to get correct covariance.
            double point[] = new double[exoData.length];

            for (int i = 0; i < exoData.length; i++) {
                double sum = 0.0;

                for (int j = 0; j <= i; j++) {
                    sum += cholesky.get(i, j) * exoData[j];
                }

                point[i] = sum;
            }

            double rowData[] = point;

            for (int col = 0; col < variables.size(); col++) {
                int index = variables.indexOf(variables.get(col));
                double value = rowData[index];

                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("Value out of range: " + value);
                }

                dataSet.setDouble(row, col, value);
            }
        }

        return dataSet;
    }

    public static ColtDataSet makeDataSet(DoubleMatrix2D inVectors, List <Node> nodes) {

        if (nodes == null) { //make up variable names
        }

        if (inVectors.columns() != nodes.size()) {
            System.out.println("inVectors.columns() = " + inVectors.columns());
            System.out.println("nodes.size() = " + nodes.size());
            new Exception("dimensions don't match!").printStackTrace();
        }
        //create new Continuous variables passing the node to the constructor
        Vector <Node> variables = new Vector <Node>();
        for (Node node : nodes)
            variables.add(new ContinuousVariable(node.getName()));
        ColtDataSet data = ColtDataSet.makeContinuousData(variables, inVectors);
        return data;
    }

    /**
     * Returns a sample with replacement with the given sample size from the
     * given dataset.
     */
    public static DoubleMatrix2D getBootstrapSample(DoubleMatrix2D data, int sampleSize) {
        int actualSampleSize = data.rows();

        int[] rows = new int[sampleSize];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = RandomUtil.getInstance().nextInt(actualSampleSize);
        }

        int[] cols = new int[data.columns()];
        for (int i = 0; i < cols.length; i++) cols[i] = i;

        return data.viewSelection(rows, cols);
    }


    /**
     * Returns a sample with replacement with the given sample size from the
     * given dataset.
     */
    public static DataSet getBootstrapSample(DataSet data, int sampleSize) {
        int actualSampleSize = data.getNumRows();

        int[] rows = new int[sampleSize];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = RandomUtil.getInstance().nextInt(actualSampleSize);
        }

        int[] cols = new int[data.getNumColumns()];
        for (int i = 0; i < cols.length; i++) cols[i] = i;

        return ColtDataSet.makeData(data.getVariables(), data.getDoubleData().viewSelection(rows, cols));
    }

    /**
     * Subtracts the mean of each column from each datum that column.
     */
    public static void zeroMean(DataSet data) {
        for (int j = 0; j < data.getNumColumns(); j++) {
            double sum = 0.0;

            for (int i = 0; i < data.getNumRows(); i++) {
                sum += data.getDouble(i, j);
            }

            int n = data.getNumRows();
            double avg = sum / n;

            for (int i = 0; i < data.getNumRows(); i++) {
                data.setDouble(i, j, data.getDouble(i, j) - avg);
            }
        }
    }
}



