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

import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * The class edu.cmu.tetrad.graph.UnderLine is used to store ordered triples of
 * variables. In the search method of, say, CcdSearh instances of this class are
 * stored in the two Lists underLineTriples and dottedUnderLineTriples.
 *
 * @author Frank Wimberly
 * @deprecated Use Triple instead. This class cannot be deleted because of
 * serialization constraints.
 */
@Deprecated
public final class Underline implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private final Node first;

    /**
     * @serial
     */
    private final Node second;

    /**
     * @serial
     */
    private final Node third;

    //==============================CONSTRUCTORS=========================//

    public Underline(Node u1, Node u2, Node u3) {

        if (u1 == null || u2 == null || u3 == null) {
            throw new IllegalArgumentException(
                    "An underline is determined by three non-null nodes");
        }

        this.first = u1;
        this.second = u2;
        this.third = u3;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Underline serializableInstance() {
        return new Underline(new GraphNode("X"), new GraphNode("Y"),
                new GraphNode("Z"));
    }

    //============================PUBLIC METHODS========================//

    public Node getFirst() {
        return first;
    }

    public Node getSecond() {
        return second;
    }

    public Node getThird() {
        return third;
    }

    public void print() {
        //System.out.println("  " + first.getName() + " " + second.getName() +
        //                    " " + third.getName());
        System.out.println("  " + first + " " + second + " " + third);
    }

    @Override
	public String toString() {
        return "  " + first + " " + second + " " + third;
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

        if (first == null) {
            throw new NullPointerException();
        }

        if (second == null) {
            throw new NullPointerException();
        }

        if (third == null) {
            throw new NullPointerException();
        }
    }

}




