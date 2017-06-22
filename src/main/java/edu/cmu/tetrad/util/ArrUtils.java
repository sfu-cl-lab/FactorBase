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

package edu.cmu.tetrad.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Some utilities for handling arrays.
 *
 * @author Joseph Ramsey
 */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public final class ArrUtils {

    //=========================PUBLIC METHODS===========================//

    /**
     * Copies a 2D double arr.
     *
     * @param arr the array to copy.
     * @return the copied array.
     */
    public static double[] copy(double[] arr) {
        if (arr == null) {
            return null;
        }

        double[] copy = new double[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);

        return copy;
    }

    /**
     * Copies a 2D double arr.
     *
     * @param arr the array to copy.
     * @return the copied array.
     */
    public static int[] copy(int[] arr) {
        if (arr == null) {
            return null;
        }

        int[] copy = new int[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);

        return copy;
    }

    /**
     * Tests two vectors for equality.
     *
     * @param va the first vector to be tested for equality.
     * @param vb the second vector to be tested for equality. Same length as the first.
     * @return true if the vectors are the same length and va[i] == vb[i] for each i.
     */
    public static boolean equals(double[] va, double[] vb) {
        if (va.length != vb.length) {
            throw new IllegalArgumentException(
                    "Incompatible matrix dimensions.");
        }

        for (int i = 0; i < va.length; i++) {
            if (Math.abs(va[i] - vb[i]) != 0.0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Concatenates the vectors rows[i], i = 0...rows.length, into a single
     * vector.
     *
     * @param arr1 The first vector to concatenate.
     * @param arr2 The second vector to concatenate.
     * @return The concatenated vector.
     */
    public static double[] concatenate(double[] arr1, double[] arr2) {
        double[] concat = new double[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, concat, 0, arr1.length);
        System.arraycopy(arr2, 0, concat, arr1.length, arr2.length);
        return concat;
    }

    /**
     * Returns the vector as a 1 x n row matrix.
     *
     * @param v The 1D vector.
     * @return The 2D row vector.
     */
    public static double[][] asRow(double[] v) {
        double[][] arr = new double[1][v.length];
        System.arraycopy(v, 0, arr[0], 0, v.length);
        return arr;
    }

    /**
     * Returns the vector as an n x 1 column matrix.
     *
     * @param v The 1D vector.
     * @return The 2D column vector.
     */
    public static double[][] asCol(double[] v) {
        double[][] arr = new double[v.length][1];
        for (int i = 0; i < v.length; i++) {
            arr[i][0] = v[i];
        }
        return arr;
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     *
     * @param arr The double array to turn into a String.
     * @return The formatted array.
     */
    public static String toString(double[] arr) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toString(arr, nf);
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     *
     * @param arr The int array to turn into a string.
     * @return The formatted array.
     */
    public static String toString(int[] arr) {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        for (int anArr : arr) {
            buf.append(anArr).append("\t");
        }
        return buf.toString();
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with the given lineInit. The number
     * format is DecimalFormat(" 0.0000;-0.0000").
     *
     * @param arr The double array to turn into a string.
     * @param nf  The number format to use.
     * @return The formatted string.
     */
    public static String toString(double[] arr, NumberFormat nf) {
        String result;
        if (nf == null) {
            throw new NullPointerException("NumberFormat must not be null.");
        }
        if (arr == null) {
            result = nullMessage();
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("\n");
            buf.append("\t");

            for (double anArr : arr) {
                buf.append(nf.format(anArr)).append("\t");
            }
            result = buf.toString();
        }
        return result;
    }

    //=========================PRIVATE METHODS===========================//


    private static String nullMessage() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("\t");
        buf.append("<Matrix is null>");
        return buf.toString();
    }
}



