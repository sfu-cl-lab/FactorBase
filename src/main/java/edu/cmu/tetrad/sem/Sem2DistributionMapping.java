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

import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetrad.util.dist.Distribution;
import edu.cmu.tetrad.util.dist.Normal;

/**
 * A mapping allowing the value of a distribution parameter to be retrieved and
 * modified.
 *
 * @author Joseph Ramsey
 */
public class Sem2DistributionMapping implements Sem2Mapping, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The distribution whose parameter is to be modified.
     */
    private Distribution distribution;

    /**
     * The index (0, 1, ...) of the parameter to be modified.
     */
    private int index;

    /**
     * The parameter itself (from the PM).
     */
    private Parameter parameter;

    /**
     * Constructs the mapping.
     *
     * @param distribution The underlying distribution.
     * @param index        The index (0, 1, ...) of the parameter.
     * @param parameter    The parameter itself from the PM.
     */
    public Sem2DistributionMapping(Distribution distribution, int index,
                                   Parameter parameter) {
        if (index > distribution.getNumParameters()) {
            throw new IllegalArgumentException();
        }

        this.distribution = distribution;
        this.index = index;
        this.parameter = parameter;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Sem2DistributionMapping serializableInstance() {
        return new Sem2DistributionMapping(Normal.serializableInstance(),
                0, new Parameter("P1", ParamType.DIST, new GraphNode("X1"),
                new GraphNode("X2")));
    }

    /**
     * Returns the value of the parameter.
     *
     * @return the value of the parameter.
     */
    @Override
    public double getValue() {
        return distribution.getParameter(index);
    }

    /**
     * Sets the value of the parameter.
     *
     * @param x The value to be set.
     * @throws IllegalArgumentException if the parameter cannot be set to that
     *                                  value.
     */
    @Override
    public void setValue(double x) {
        distribution.setParameter(index, x);
    }

    /**
     * Returns the parameter from the PM.
     *
     * @return the parameter from the PM.
     */
    @Override
    public Parameter getParameter() {
        return parameter;
    }
}

