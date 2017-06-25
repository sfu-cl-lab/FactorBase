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

import edu.cmu.tetrad.graph.Node;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies a connectionn function for use with nonlinear SEMs. The order
 * of the input nodes is given by <code>getInputNodes()</code> (and specified
 * in the constructor), and the function value (with inputs in that order) is
 * given by f, as calculated from the supplied function in String form.
 * The names of the variables in the function must be the variable.getName()'s.
 *
 * @author Joseph Ramsey
 */
public class StringFunction implements ConnectionFunction {
    private String formula;
    private Node[] inputs;
    private Expression expression;

    public StringFunction(String formula, Node... parents) throws ParseException {
        this.formula = formula;

        List <String> varNames = new ArrayList <String>();

        for (Node node : parents) {
            varNames.add(node.getName());
        }

        ExpressionParser parser = new ExpressionParser(varNames, ExpressionParser.RestrictionType.MAY_ONLY_CONTAIN);
        this.expression = parser.parseExpression(formula);
        this.inputs = parents;
    }

    @Override
    public Node[] getInputNodes() {
        return inputs;
    }

    public String getFormula() {
        return formula;
    }

    /**
     * Returns the function value. The order of the input values must be the
     * same as the order of the input nodes.
     *
     * @return Double.NaN, if not overridden.
     */
    @Override
    public double valueAt(final double... inputValues) {
        if (inputValues == null) {
            throw new NullPointerException();
        }

        if (inputValues.length != inputs.length) {
            throw new NullPointerException();
        }

        return expression.evaluate(new Context() {
            public Double getValue(String var) {
                for (int i = 0; i < inputs.length; i++) {
                    Node node = inputs[i];

                    if (var.equals(node.getName())) {
                        return inputValues[i];
                    }
                }

                return null;
            }
        });
    }
}

