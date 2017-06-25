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

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.*;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Performs a test of conditional independence X _||_ Y | Z1...Zn where all variables are either continuous or discrete.
 * This test is valid for both ordinal and non-ordinal discrete variables.
 * <p/>
 * This regression makes multiple assumptions: 1. IIA 2. Large sample size (multiple regressions needed on subsets of
 * sample)
 *
 * @author Joseph Ramsey
 * @author Augustus Mayo.
 */
public class IndTestMultinomialLogisticRegression implements IndependenceTest {
    private DataSet dataSet;
    private double alpha;
    private double lastP;

    public IndTestMultinomialLogisticRegression(DataSet dataSet, double alpha) {
        this.dataSet = dataSet;
        this.alpha = alpha;
    }

    /**
     * Returns an Independence test for a subset of the variables.
     */
    @Override
    public IndependenceTest indTestSubset(List <Node> vars) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the given independence question is judged true, false if not. The independence question is of the
     * form x _||_ y | z, z = <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    @Override
    public boolean isIndependent(Node x, Node y, List <Node> z) {

        if (x instanceof DiscreteVariable) {
            if (((DiscreteVariable) x).getNumCategories() > 2) {
                if (y instanceof DiscreteVariable) {
                    if (((DiscreteVariable) y).getNumCategories() > 2) {
                        // Consider a better test here X (discrete) and y (discrete)
                        return isIndependentMultinomialLogisticRegression(x, y, z); // X: D, Y: D
                    } else {
                        return isIndependentLogisticRegression(x, y, z); // X: D, Y: B
                    }
                } else {
                    return isIndependentMultinomialLogisticRegression(x, y, z); // X: D, Y: C
                }
            } else {
                if (y instanceof DiscreteVariable) {
                    if (((DiscreteVariable) y).getNumCategories() > 2) {
                        return isIndependentLogisticRegression(x, y, z); // X: B, Y: D
                    } else {
                        return isIndependentLogisticRegression(x, y, z); // X: B, Y: B
                    }
                } else {
                    return isIndependentLogisticRegression(x, y, z); // X: B, Y: C
                }
            }
        } else {
            if (y instanceof DiscreteVariable) {
                if (((DiscreteVariable) y).getNumCategories() > 2) {
                    return isIndependentMultinomialLogisticRegression(y, x, z); // X: C, Y: D
                } else {
                    return isIndependentLogisticRegression(y, x, z); // X: C, Y: B
                }
            } else {
                return isIndependentRegression(x, y, z); // X: C, Y: C
            }
        }
    }

    private boolean isIndependentMultinomialLogisticRegression(Node x, Node y, List <Node> z) {

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        // Copy data to be able to modify
        DataSet regressors = new ColtDataSet((ColtDataSet) dataSet);
        List <String> regressorList = new ArrayList <String>();
        List <Node> dataSetVariables = regressors.getVariables();
        int targetIndex = dataSet.getVariables().indexOf(x);

        // Set of regressors = {y} U z
        regressorList.add(dataSet.getVariable(y.getName()).getName());

        int returnPIndex = 0; // Keep track of the y index for the array of regressors

        for (Node zVar : z) {
            regressorList.add(dataSet.getVariable(zVar.getName()).getName());
        }

        // Remove from the data set any variables not in the regressor list (also retain target)
        for (Node var : dataSetVariables) {
            if (!regressorList.contains(var.getName()) && !(var.getName().equals(x.getName())))
                regressors.removeColumn(var);
        }

        List <Node> yVars = new ArrayList();

        dataSetVariables = regressors.getVariables();   // Update the list of variables to cover dummy vars created

        for (Node dsVar : dataSetVariables) {           // Create dummy variables for any other discrete with numCats >2

            if (dsVar instanceof DiscreteVariable && ((DiscreteVariable) dsVar).getNumCategories() > 2 && (!dsVar.getName().equals(x.getName()))) {
                List <String> varCats = ((DiscreteVariable) dsVar).getCategories();
                varCats.remove(0);

                for (String cat : varCats) {
                    String newVarName = dsVar.getName() + "MULTINOM" + cat;
                    Node newVar = new DiscreteVariable(newVarName, 2);

                    regressors.addVariable(newVar);
                    int newVarIndex = regressors.getColumn(newVar);

                    int numCases = regressors.getNumRows();
                    for (int i = 0; i < numCases; i++) {
                        Object dataCell = regressors.getObject(i, regressors.getColumn(dsVar));
                        int dataCellIndex = ((DiscreteVariable) dsVar).getIndex(dataCell.toString());

                        if (dataCellIndex == ((DiscreteVariable) dsVar).getIndex(cat))
                            regressors.setInt(i, newVarIndex, 1);
                        else
                            regressors.setInt(i, newVarIndex, 0);
                    }

                    regressorList.add(newVarName);
                    if (dsVar.equals(y))
                        yVars.add(y);
                }

                regressorList.remove(dsVar.getName());
                regressors.removeColumn(dsVar);
            }
        }

        dataSetVariables = regressors.getVariables(); // Update again for any more changes

        // Logistic Regression requires a string array of the names
        String[] regressorNames = new String[regressorList.size()];

        int k = 0;

        for (Node var : dataSetVariables) {
            if (yVars.contains(var) || y.getName().equals(var.getName()))
                returnPIndex = k;

            if (regressorList.contains(var.getName())) {
                regressorNames[k] = var.getName();
                k++;
            }
        }

        List <String> targetCats = ((DiscreteVariable) x).getCategories();

        int targetBaseCat = ((DiscreteVariable) x).getIndex(targetCats.remove(0)); // Category to test other categories against
        List <Double> pValuesForTarget = new ArrayList(); // List of p-values for the target variables (numCats - 1)

        for (String cat : targetCats) {

            DataSet tempSet = new ColtDataSet((ColtDataSet) regressors); // Need new copy to select proper rows from
            targetIndex = tempSet.getColumn(x);                         // Update targetIndex since data set has changed
            List <Integer> removeList = new ArrayList();                 // These rows are do not have targetIndex nor current cat index

            // Remove rows where target is not one of the two categories for the current regression
            // Set to 0 or 1 otherwise
            for (int i = 0; i < tempSet.getNumRows(); i++) {
                Object cell = tempSet.getObject(i, targetIndex);
                int cellCat = ((DiscreteVariable) x).getIndex(cell.toString());

                if (cellCat == targetBaseCat) {
                    tempSet.setInt(i, targetIndex, 0);
                } else if (cellCat == ((DiscreteVariable) x).getIndex(cat)) {
                    tempSet.setInt(i, targetIndex, 1);
                } else {
                    removeList.add(i);
                }
            }

            // Convert rows to remove to an array
            int[] removeArray = new int[removeList.size()];
            int r = 0;
            for (Integer rmv : removeList) {
                removeArray[r++] = rmv.intValue();
            }

            tempSet.removeRows(removeArray);

            int numcases = tempSet.getNumRows();

            // Extract values for the target
            int[] target = new int[numcases];
            for (int j = 0; j < numcases; j++) {
                target[j] = tempSet.getInt(j, targetIndex);
            }

            tempSet.removeColumn(x); // Remove once extracted

            int numvars = tempSet.getNumColumns();

            // Copy modified data set into new array to be passed
            double[][] regressorData = new double[numvars][numcases];

            for (int i = 0; i < numcases; i++) {
                for (int j = 0; j < numvars; j++) {
                    regressorData[j][i] = tempSet.getDouble(i, j);
                }
            }

            LogisticRegression regression = new LogisticRegression();
            regression.setRegressors(regressorData);
            regression.setVariableNames(regressorNames);
            regression.setAlpha(this.alpha);

            LogisticRegressionResult result = null;
            String report = "";

            try {
                report = regression.regress(target, x.getName());
                result = regression.getResult();
            } catch (Exception e) {
                return false;
            }

            // Only accept if all are significant so take the lowest
            //double combP = 1.0;
            //for (Integer index : returnPIndices) {
            //    if (combP > result.getProbs()[(index + 1)])
            //        combP = result.getProbs()[(index + 1)];
            //}

            // If Y has > 2 categories, test this model versus the null model

            //pValuesForTarget.add(combP);

            pValuesForTarget.add(result.getProbs()[returnPIndex + 1]);

            // If y had > 2 categories perform a liklihood ratio test to determine the p value
            if (y instanceof DiscreteVariable && ((DiscreteVariable) y).getNumCategories() > 2 && z.size() > 0) {
                // Record the -2Loglikelihood for the alternative model
                double testLL = result.getLogLikelihood();

                // Run a logistic regression on the regressors excluding dummy variables for y (Null Model)
                for (Node yRg : yVars) {
                    tempSet.removeColumn(yRg);
                    regressorList.remove(yRg.getName());
                }

                dataSetVariables = tempSet.getVariables();
                regressorNames = new String[regressorList.size()];
                k = 0;

                for (Node var : dataSetVariables) {
                    if (regressorList.contains(var.getName())) {
                        regressorNames[k] = var.getName();
                        k++;
                    }
                }

                numcases = tempSet.getNumRows();
                numvars = tempSet.getNumColumns();

                regressorData = new double[numvars][numcases];

                for (int i = 0; i < numvars; i++) {
                    for (int j = 0; j < numcases; j++) {
                        regressorData[i][j] = tempSet.getDouble(j, i);
                    }
                }

                regression = new LogisticRegression();
                regression.setRegressors(regressorData);
                regression.setVariableNames(regressorNames);
                regression.setAlpha(this.alpha);

                result = null;
                report = "";

                try {
                    report = regression.regress(target, x.getName());
                    result = regression.getResult();
                } catch (Exception e) {
                    return false;
                }

                /**
                 * LogisticRegressionResult returns the -2Logliklihoods
                 * so equivalent to ratio test is taking the difference
                 */
                double LRm = result.getLogLikelihood() - testLL;

                // Current size of y list is equal to the difference between regressors in alt - null
                pValuesForTarget.add(1 - ProbUtils.chisqCdf(LRm, yVars.size()));
            }

        }

        double p = 1.0;
        // Choose the minimum of the p-values
        // This is only one method that can be used, this requires every coefficient to be significant
        for (Double val : pValuesForTarget) {
            if (p > val)
                p = val;
        }

        boolean indep = p > alpha;

        this.lastP = p;

        if (indep) {
            TetradLogger.getInstance().log("independencies", SearchLogUtils.independenceFactMsg(x, y, z, p));
        } else {
            TetradLogger.getInstance().log("dependencies", SearchLogUtils.dependenceFactMsg(x, y, z, p));
        }

        return indep;
    }

    private boolean isIndependentLogisticRegression(Node x, Node y, List <Node> z) {

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        DataSet regressors = new ColtDataSet((ColtDataSet) dataSet);

        List <String> regressorList = new ArrayList <String>();
        int targetIndex = dataSet.getVariables().indexOf(x);
        regressors.removeColumn(x);

        List <Node> dataSetVariables = regressors.getVariables();
        List <Node> yVars = new ArrayList <Node>();

        regressorList.add(dataSet.getVariable(y.getName()).getName());

        for (Node zVar : z) {
            regressorList.add(dataSet.getVariable(zVar.getName()).getName());
        }

        for (Node dsVar : dataSetVariables) { // Create dummy variables for any other discrete with numCats >2
            if (regressorList.contains(dsVar.getName()) && dsVar instanceof DiscreteVariable && ((DiscreteVariable) dsVar).getNumCategories() > 2) {
                List <String> varCats = ((DiscreteVariable) dsVar).getCategories();
                varCats.remove(0);

                for (String cat : varCats) {
                    String newVarName = dsVar.getName() + "MULTINOM" + cat;
                    Node newVar = new DiscreteVariable(newVarName, 2);

                    regressors.addVariable(newVar);
                    int newVarIndex = regressors.getColumn(newVar);

                    int numCases = regressors.getNumRows();
                    for (int i = 0; i < numCases; i++) {
                        Object dataCell = regressors.getObject(i, regressors.getColumn(dsVar));
                        int dataCellIndex = ((DiscreteVariable) dsVar).getIndex(dataCell.toString());

                        if (dataCellIndex == ((DiscreteVariable) dsVar).getIndex(cat))
                            regressors.setInt(i, newVarIndex, 1);
                        else
                            regressors.setInt(i, newVarIndex, 0);
                    }

                    regressorList.add(newVarName);
                    if (dsVar.equals(y))
                        yVars.add(newVar);
                }

                regressorList.remove(dsVar.getName());
                regressors.removeColumn(dsVar);
            }
        }

        dataSetVariables = regressors.getVariables();

        String[] regressorNames = new String[regressorList.size()];
        int returnPIndex = 0;
        int k = 0;

        for (Node var : dataSetVariables) {
            if (!regressorList.contains(var.getName())) {
                regressors.removeColumn(var);
            } else {
                if (var.getName().equals(y.getName()) || yVars.contains(var))
                    returnPIndex = k;
                regressorNames[k] = var.getName();
                k++;
            }
        }

        int numcases = regressors.getNumRows();
        int numvars = regressors.getNumColumns();

        double[][] regressorData = new double[numvars][numcases];

        for (int i = 0; i < numvars; i++) {
            for (int j = 0; j < numcases; j++) {
                regressorData[i][j] = regressors.getDouble(j, i);
            }
        }

        int[] target = new int[numcases];
        for (int j = 0; j < numcases; j++) {
            target[j] = dataSet.getInt(j, targetIndex);
        }

        LogisticRegression regression = new LogisticRegression();
        regression.setRegressors(regressorData);
        regression.setVariableNames(regressorNames);
        regression.setAlpha(this.alpha);

        LogisticRegressionResult result = null;
        String report = "";

        try {
            report = regression.regress(target, x.getName());
            result = regression.getResult();
        } catch (Exception e) {
            return false;
        }

        double p;
        p = result.getProbs()[returnPIndex + 1]; // p-value if y was binary

        // If y had > 2 categories perform a liklihood ratio test to determine the p value
        if (y instanceof DiscreteVariable && ((DiscreteVariable) y).getNumCategories() > 2 && z.size() > 0) {
            // Record the -2Loglikelihood for the alternative model
            double testLL = result.getLogLikelihood();

            // Run a logistic regression on the regressors excluding dummy variables for y (Null Model)
            for (Node yRg : yVars) {
                regressors.removeColumn(yRg);
                regressorList.remove(yRg.getName());
            }

            dataSetVariables = regressors.getVariables();
            regressorNames = new String[regressorList.size()];
            k = 0;

            for (Node var : dataSetVariables) {
                if (regressorList.contains(var.getName())) {
                    regressorNames[k] = var.getName();
                    k++;
                }
            }

            numcases = regressors.getNumRows();
            numvars = regressors.getNumColumns();

            regressorData = new double[numvars][numcases];

            for (int i = 0; i < numvars; i++) {
                for (int j = 0; j < numcases; j++) {
                    regressorData[i][j] = regressors.getDouble(j, i);
                }
            }

            regression = new LogisticRegression();
            regression.setRegressors(regressorData);
            regression.setVariableNames(regressorNames);
            regression.setAlpha(this.alpha);

            result = null;
            report = "";

            try {
                report = regression.regress(target, x.getName());
                result = regression.getResult();
            } catch (Exception e) {
                return false;
            }

            /**
             * LogisticRegressionResult returns the -2Logliklihoods
             * so equivalent to ratio test is taking the difference
             */
            double LRm = result.getLogLikelihood() - testLL;

            // Current size of y list is equal to the difference between regressors in alt - null
            p = 1 - ProbUtils.chisqCdf(LRm, yVars.size());
        }

        this.lastP = p;

        boolean indep = p > alpha;

        if (indep) {
            TetradLogger.getInstance().log("independencies", SearchLogUtils.independenceFactMsg(x, y, z, p));
        } else {
            TetradLogger.getInstance().log("dependencies", SearchLogUtils.dependenceFactMsg(x, y, z, p));
        }

        return indep;
    }

    private boolean isIndependentRegression(Node x, Node y, List <Node> z) {

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        DataSet regressorData = new ColtDataSet((ColtDataSet) dataSet);

        List <Node> regressors = new ArrayList <Node>();
        regressors.add(dataSet.getVariable(y.getName()));

        for (Node zVar : z) {
            regressors.add(dataSet.getVariable(zVar.getName()));
        }

        List <Node> initRegressors = new ArrayList(regressors);
        for (Node dsVar : initRegressors) { // Create dummy variables for any other discrete with numCats >2
            if (dsVar instanceof DiscreteVariable && ((DiscreteVariable) dsVar).getNumCategories() > 2) {
                List <String> varCats = ((DiscreteVariable) dsVar).getCategories();
                varCats.remove(0);

                for (String cat : varCats) {
                    String newVarName = dsVar.getName() + "MULTINOM" + cat;
                    Node newVar = new DiscreteVariable(newVarName, 2);

                    regressorData.addVariable(newVar);
                    int newVarIndex = regressorData.getColumn(newVar);

                    int numCases = regressorData.getNumRows();
                    for (int i = 0; i < numCases; i++) {
                        Object dataCell = regressorData.getObject(i, regressorData.getColumn(dsVar));
                        int dataCellIndex = ((DiscreteVariable) dsVar).getIndex(dataCell.toString());

                        if (dataCellIndex == ((DiscreteVariable) dsVar).getIndex(cat))
                            regressorData.setInt(i, newVarIndex, 1);
                        else
                            regressorData.setInt(i, newVarIndex, 0);
                    }

                    regressors.add(newVar);
                }

                regressors.remove(dsVar);
                regressorData.removeColumn(dsVar);
            }
        }

        Regression regression = new RegressionDataset(regressorData);
        RegressionResult result = null;

        try {
            result = regression.regress(x, regressors);
        } catch (Exception e) {
            return false;
        }

        double p = result.getP()[1];
        this.lastP = p;

        boolean indep = p > alpha;

        if (indep) {
            TetradLogger.getInstance().log("independencies", SearchLogUtils.independenceFactMsg(x, y, z, p));
        } else {
            TetradLogger.getInstance().log("dependencies", SearchLogUtils.dependenceFactMsg(x, y, z, p));
        }

        return indep;
    }


    @Override
    public boolean isIndependent(Node x, Node y, Node... z) {
        List <Node> zList = Arrays.asList(z);
        return isIndependent(x, y, zList);
    }

    /**
     * Returns true if the given independence question is judged false, true if not. The independence question is of the
     * form x _||_ y | z, z = <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    @Override
    public boolean isDependent(Node x, Node y, List <Node> z) {
        return !this.isIndependent(x, y, z);
    }

    @Override
    public boolean isDependent(Node x, Node y, Node... z) {
        List <Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);
    }

    /**
     * Returns the probability associated with the most recently executed independence test, of Double.NaN if p value is
     * not meaningful for tis test.
     */
    @Override
    public double getPValue() {
        return this.lastP; //STUB
    }

    /**
     * Returns the list of variables over which this independence checker is capable of determinining independence
     * relations.
     */
    @Override
    public List <Node> getVariables() {
        return dataSet.getVariables(); //STUB
    }

    /**
     * Returns the list of variable varNames.
     */
    @Override
    public List <String> getVariableNames() {
        List <Node> variables = getVariables();
        List <String> variableNames = new ArrayList <String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    @Override
    public Node getVariable(String name) {
        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    /**
     * Returns true if y is determined the variable in z.
     */
    @Override
    public boolean determines(List <Node> z, Node y) {
        return false; //stub
    }

    /**
     * Returns the significance level of the independence test.
     *
     * @throws UnsupportedOperationException if there is no significance level.
     */
    @Override
    public double getAlpha() {
        return this.alpha; //STUB
    }

    /**
     * Sets the significance level.
     */
    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public DataSet getData() {
        return this.dataSet;
    }
}
