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

package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Represents an exponential power distribution for purposes of sampling.
 *
 * @author Joseph Ramsey
 */
public class ExponentialPower implements Distribution {
    static final long serialVersionUID = 23L;

    private double tau;

    public ExponentialPower(double tau) {
        this.tau = tau;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static ExponentialPower serializableInstance() {
        return new ExponentialPower(2);
    }

    @Override
    public int getNumParameters() {
        return 1;
    }

    @Override
    public String getName() {
        return "Exponential Power";
    }

    @Override
    public void setParameter(int index, double value) {
        if (index == 0) {
            tau = value;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public double getParameter(int index) {
        if (index == 0) {
            return tau;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public String getParameterName(int index) {
        if (index == 0) {
            return "Tau";
        }

        throw new IllegalArgumentException();
    }

    @Override
    public double nextRandom() {
        return RandomUtil.getInstance().nextExponentialPower(tau);
    }

    @Override
    public String toString() {
        return "ExponentialPower(" + tau + ")";
    }
}
