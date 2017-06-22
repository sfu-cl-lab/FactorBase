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
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Tests Sem.
 *
 * @author Joseph Ramsey
 */
public class TestGeneralizedSem extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGeneralizedSem(String name) {
        super(name);
    }

    public void test1() {
        GeneralizedSemPm pm = makeTypicalPm();

        Node x1 = pm.getNode("X1");
        Node x2 = pm.getNode("X2");
        Node x3 = pm.getNode("X3");
        Node x4 = pm.getNode("X4");
        Node x5 = pm.getNode("X5");

        SemGraph graph = pm.getGraph();

        List<Node> variablesNodes = pm.getVariableNodes();
        System.out.println(variablesNodes);

        List<Node> errorNodes = pm.getErrorNodes();
        System.out.println(errorNodes);


        try {
            pm.setNodeExpression(x1, "cos(B1) +\n E_X1");
            System.out.println(pm);

            String b1 = "B1";

            Set<Node> nodes = pm.getReferencingNodes(b1);

            assertTrue(nodes.contains(x1));
            assertTrue(!nodes.contains(x2) && !nodes.contains(x2));

//            assertTrue(nodes.contains(x1) && nodes.contains(x3));
//            assertTrue(!(nodes.contains(x1) && nodes.contains(x2)));

            Set<String> referencedParameters = pm.getReferencedParameters(x3);

            String b2 = "B2";
            String b3 = "B3";

            System.out.println("Parameters referenced by X3 are: " + referencedParameters);

//            assertTrue(referencedParameters.contains(b1) && referencedParameters.contains(b2));
//            assertTrue(!(referencedParameters.contains(b1) && referencedParameters.contains(b3)));

//            assertTrue(referencedParameters.contains(b1) && referencedParameters.contains(b2));
//            assertTrue(!(referencedParameters.contains(b1) && referencedParameters.contains(b3)));

            Node e_x3 = pm.getNode("E_X3");

            for (Node node : pm.getNodes()) {
                Set<Node> referencingNodes = pm.getReferencingNodes(node);
                System.out.println("Nodes referencing " + node + " are: " + referencingNodes);
            }

            for (Node node : pm.getVariableNodes()) {
                Set<Node> referencingNodes = pm.getReferencedNodes(node);
                System.out.println("Nodes referenced by " + node + " are: " + referencingNodes);
            }

            Set<Node> referencingX3 = pm.getReferencingNodes(x3);
            assertTrue(referencingX3.contains(x4) && !referencingX3.contains(x5));

            Set<Node> referencedByX3 = pm.getReferencedNodes(x3);
            assertTrue(referencedByX3.contains(x1) && referencedByX3.contains(x2) && referencedByX3.contains(e_x3)
                    && !referencedByX3.contains(x4));

            pm.setNodeExpression(x5, "a * E^X2 + X4 + E_X5");

            Node e_x5 = pm.getErrorNode(x5);

            graph.setShowErrorTerms(true);
            assertTrue(e_x5.equals(graph.getExogenous(x5)));

            pm.setNodeExpression(e_x5, "Beta(3, 5)");

            System.out.println(pm);

            assertEquals("U(0, 1)", pm.getParameterExpressionString(b1));
            pm.setParameterExpression(b1, "N(0, 2)");
            assertEquals("N(0, 2)", pm.getParameterExpressionString(b1));

            GeneralizedSemIm im = new GeneralizedSemIm(pm);

            System.out.println(im);

            DataSet dataSet = im.simulateDataAvoidInfinity(10, false);

            System.out.println(dataSet);

        } catch (ParseException e) {
            System.out.println(e);
        }
    }

    public void test2() {
        RandomUtil.getInstance().setSeed(29483L);

        int sampleSize = 1000;

        List<Node> variableNodes = new ArrayList<Node>();
        ContinuousVariable x1 = new ContinuousVariable("X1");
        ContinuousVariable x2 = new ContinuousVariable("X2");
        ContinuousVariable x3 = new ContinuousVariable("X3");
        ContinuousVariable x4 = new ContinuousVariable("X4");
        ContinuousVariable x5 = new ContinuousVariable("X5");

        variableNodes.add(x1);
        variableNodes.add(x2);
        variableNodes.add(x3);
        variableNodes.add(x4);
        variableNodes.add(x5);

        Graph _graph = new EdgeListGraph(variableNodes);
        SemGraph graph = new SemGraph(_graph);
        graph.addDirectedEdge(x1, x3);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x2, x4);
        graph.addDirectedEdge(x4, x5);
        graph.addDirectedEdge(x2, x5);

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        DataSet dataSet = semIm.simulateData(sampleSize, false);

        System.out.println(semPm);

        GeneralizedSemPm _semPm = new GeneralizedSemPm(semPm);
        GeneralizedSemIm _semIm = new GeneralizedSemIm(_semPm, semIm);
        DataSet _dataSet = _semIm.simulateDataMinimizeSurface(sampleSize, false);

        System.out.println(_semPm);

//        System.out.println(_dataSet);

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            double[] col = dataSet.getDoubleData().viewColumn(j).toArray();
            double[] _col = _dataSet.getDoubleData().viewColumn(j).toArray();

            double mean = StatUtils.mean(col);
            double _mean = StatUtils.mean(_col);

            double variance = StatUtils.variance(col);
            double _variance = StatUtils.variance(_col);

            assertEquals(mean, _mean, 0.3);
            assertEquals(1.0, variance / _variance, .2);
        }
    }

    public void test3() {
        List<Node> variableNodes = new ArrayList<Node>();
        ContinuousVariable x1 = new ContinuousVariable("X1");
        ContinuousVariable x2 = new ContinuousVariable("X2");
        ContinuousVariable x3 = new ContinuousVariable("X3");
        ContinuousVariable x4 = new ContinuousVariable("X4");
        ContinuousVariable x5 = new ContinuousVariable("X5");

        variableNodes.add(x1);
        variableNodes.add(x2);
        variableNodes.add(x3);
        variableNodes.add(x4);
        variableNodes.add(x5);

        Graph _graph = new EdgeListGraph(variableNodes);
        SemGraph graph = new SemGraph(_graph);
        graph.setShowErrorTerms(true);

        Node e1 = graph.getExogenous(x1);
        Node e2 = graph.getExogenous(x2);
        Node e3 = graph.getExogenous(x3);
        Node e4 = graph.getExogenous(x4);
        Node e5 = graph.getExogenous(x5);

        graph.addDirectedEdge(x1, x3);
        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x2, x4);
        graph.addDirectedEdge(x4, x5);
        graph.addDirectedEdge(x2, x5);
        graph.addDirectedEdge(x5, x1);

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);

        List<Node> variablesNodes = pm.getVariableNodes();
        System.out.println(variablesNodes);

        List<Node> errorNodes = pm.getErrorNodes();
        System.out.println(errorNodes);


        try {
            pm.setNodeExpression(x1, "cos(b1) + a1 * X5 + E_X1");
            pm.setNodeExpression(x2, "a2 * X1 + E_X2");
            pm.setNodeExpression(x3, "tanh(a3*X2 + a4*X1) + E_X3");
            pm.setNodeExpression(x4, "0.1 * E^X2 + X3 + E_X4");
            pm.setNodeExpression(x5, "0.1 * E^X4 + a6* X2 + E_X5");
            pm.setNodeExpression(e1, "U(0, 1)");
            pm.setNodeExpression(e2, "U(0, 1)");
            pm.setNodeExpression(e3, "U(0, 1)");
            pm.setNodeExpression(e4, "U(0, 1)");
            pm.setNodeExpression(e5, "U(0, 1)");

            GeneralizedSemIm im = new GeneralizedSemIm(pm);

            System.out.println(im);

            DataSet dataSet = im.simulateDataNSteps(1000, false);

//            System.out.println(dataSet);
        } catch (ParseException e) {
            System.out.println(e);
        }
    }

    public void test7() {

        // For X3

        Map<String, String[]> templates = new HashMap<String, String[]>();

        templates.put("NEW(b) + NEW(b) + NEW(c) + NEW(c) + NEW(c)", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("NEW(X1) + NEW(b) + NEW(c) + NEW(c) + NEW(c)", new String[]{});
        templates.put("$", new String[]{});
        templates.put("TSUM($)", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("TPROD($)", new String[] {"X1", "X2", "X3", "X4", "X5"});
        templates.put("TPROD($) + X2", new String[] {"X1", "X2", "X3", "X4", "X5"});
        templates.put("TPROD($) + TSUM($)", new String[] {"X1", "X2", "X3", "X4", "X5"});
        templates.put("tanh(TSUM(NEW(a)*$))", new String[] {"X1", "X2", "X3", "X4", "X5"});
        templates.put("Normal(0, 1)", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("Normal(m, s)", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("Normal(NEW(m), s)", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("Normal(NEW(m), NEW(s)) + m1 + s6", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("TSUM($) + a", new String[]{"X1", "X2", "X3", "X4", "X5"});
        templates.put("TSUM($) + TSUM($) + TSUM($) + 1", new String[]{"X1", "X2", "X3", "X4", "X5"});

        for (String template : templates.keySet()) {
            GeneralizedSemPm semPm = makeTypicalPm();
            System.out.println(semPm.getGraph());

            Set<Node> shouldWork = new HashSet<Node>();

            for (String name : templates.get(template)) {
                shouldWork.add(semPm.getNode(name));
            }

            Set<Node> works = new HashSet<Node>();

            for (int i = 0; i < semPm.getNodes().size(); i++) {
                System.out.println("-----------");
                System.out.println(semPm.getNodes().get(i));
                System.out.println("Trying template: " + template);
                String _template = template;

                Node node = semPm.getNodes().get(i);

                try {
                    _template = TemplateExpander.getInstance().expandTemplate(_template, semPm, node);
                } catch (Exception e) {
                    System.out.println("Couldn't expand template: " + template);
                    continue;
                }

                try {
                    semPm.setNodeExpression(node, _template);
                    System.out.println("Set formula " + _template + " for " + node);

                    if (semPm.getVariableNodes().contains(node)) {
                        works.add(node);                             
                    }

                } catch (Exception e) {
                    System.out.println("Couldn't set formula " + _template + " for " + node);
                }
            }

            for (String parameter : semPm.getParameters()) {
                System.out.println("-----------");
                System.out.println(parameter);
                System.out.println("Trying template: " + template);
                String _template = template;

                try {
                    _template = TemplateExpander.getInstance().expandTemplate(_template, semPm, null);
                } catch (Exception e) {
                    System.out.println("Couldn't expand template: " + template);
                    continue;
                }

                try {
                    semPm.setParameterExpression(parameter, _template);
                    System.out.println("Set formula " + _template + " for " + parameter);
                } catch (Exception e) {
                    System.out.println("Couldn't set formula " + _template + " for " + parameter);
                }
            }

            assertEquals(shouldWork, works);
        }
    }

    public void test4() {
        Graph graph = GraphUtils.randomDag(5, 5, false);
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        semIm.simulateDataReducedForm(1000, false);

        GeneralizedSemPm pm = new GeneralizedSemPm(semPm);
        GeneralizedSemIm im = new GeneralizedSemIm(pm, semIm);

        DoubleMatrix1D e = new DenseDoubleMatrix1D(5);

        for (int i = 0; i < e.size(); i++) {
            e.set(i, RandomUtil.getInstance().nextNormal(0, 1));
        }

        DoubleMatrix1D record1 = semIm.simulateOneRecord(e);
        DoubleMatrix1D record2 = im.simulateOneRecord(e);

        System.out.println("XXX1" + e);
        System.out.println("XXX2" + record1);
        System.out.println("XXX3" + record2);

        assertEquals(record1, record2);
    }

    private GeneralizedSemPm makeTypicalPm() {
        List<Node> variableNodes = new ArrayList<Node>();
        ContinuousVariable x1 = new ContinuousVariable("X1");
        ContinuousVariable x2 = new ContinuousVariable("X2");
        ContinuousVariable x3 = new ContinuousVariable("X3");
        ContinuousVariable x4 = new ContinuousVariable("X4");
        ContinuousVariable x5 = new ContinuousVariable("X5");

        variableNodes.add(x1);
        variableNodes.add(x2);
        variableNodes.add(x3);
        variableNodes.add(x4);
        variableNodes.add(x5);

        Graph _graph = new EdgeListGraph(variableNodes);
        SemGraph graph = new SemGraph(_graph);
        graph.addDirectedEdge(x1, x3);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x2, x4);
        graph.addDirectedEdge(x4, x5);
        graph.addDirectedEdge(x2, x5);

        GeneralizedSemPm semPm = new GeneralizedSemPm(graph);
        return semPm;
    }

    private String replaceNewParameters(GeneralizedSemPm semPm, String formula, List<String> usedNames) {
        String parameterPattern = "\\$|(([a-zA-Z]{1})([a-zA-Z0-9-_/]*))";
        Pattern p = Pattern.compile("NEW\\((" + parameterPattern + ")\\)");

        while (true) {
            Matcher m = p.matcher(formula);

            if (!m.find()) {
                break;
            }

//            String group0 = m.group(0).replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
            String group0 = Pattern.quote(m.group(0));
            String group1 = m.group(1);

            String nextName = semPm.nextParameterName(group1, usedNames);
            formula = formula.replaceFirst(group0, nextName);
            usedNames.add(nextName);
//            System.out.println(formula);
        }
        return formula;
    }


    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestGeneralizedSem.class);
    }
}
