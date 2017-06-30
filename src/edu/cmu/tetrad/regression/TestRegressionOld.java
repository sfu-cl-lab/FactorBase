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

package edu.cmu.tetrad.regression;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestRegressionOld extends TestCase {
    public TestRegressionOld(String name) {
        super(name);
    }

    public void testSimpleRegression() {
        /*
        //A Simple regression of 1 variable on another by Joe Ramsey probably
        double[] y = {1.0, 2.5, 2.3, 1.7, 6.5};
        double[] x = {5.1, 3.7, 2.4, 3.2, 3.2};
        double[][] regressors = {x};
        String[] names = {"x"};

        Regression regresion = new Regression();
        regresion.setRegressors(regressors);
        regresion.setVarNames(names);
        RegressionResult result = regresion.regress(y);
        System.out.println(result);
        */

        //A Test of multiple regression by Frank Wimberly
        //Zero to 60 acceleration times of various high-performance cars are regressed
        //on horsepower, torque, curb weight, final drive ratio, and displacement (in cc).
        double[] accel = {7.1, 6.1, 5.8, 4.0, 3.6, 5.5, 4.4, 6.7, 7.8};
        double[] hp =
                {250.0, 227.0, 340.0, 180.0, 348.0, 240.0, 190.0, 178.0, 200.0};
        double[] torque =
                {250.0, 217.0, 302.0, 145.0, 328.0, 162.0, 138.0, 166.0, 220.0};
        double[] weight = {3640.0, 3256.0, 4173.0, 1257.0, 2062.0, 2866.0,
                1930.0, 2596.0, 3460.0};
        double[] ratio = {3.6, 3.9, 3.89, 3.62, 3.55, 4.1, 4.53, 4.1, 3.29};
        double[] disp = {2457, 1994, 4163, 1988, 4942, 2157, 1796, 1840, 3498};

        double[][] regressors = {hp, torque, weight, ratio, disp};

        System.out.println("regressors " + regressors[0][0] + " " +
                regressors[0][1] + " " + regressors[0][2]);
        System.out.println("regressors " + regressors[1][0] + " " +
                regressors[1][1] + " " + regressors[1][2]);

        String[] names = {"HP", "Torque", "Weight", "RAR", "Disp"};

        System.out.println(
                "target " + accel[0] + " " + accel[1] + " " + accel[2]);

        RegressionOld regression = new RegressionOld();
        regression.setRegressors(regressors);
        regression.setRegressorNames(names);
        RegressionResult result = regression.regress(accel, "Accel");
        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertEquals(6.5, coeffs[0], 0.05);
        assertEquals(-0.02, coeffs[1], 0.005);
        assertEquals(0.007, coeffs[2], 0.001);
        assertEquals(0.0014, coeffs[3], 0.0002);
        assertEquals(-0.318, coeffs[4], 0.001);
        assertEquals(0.0002, coeffs[5], 0.00005);
    }

    public static Test suite() {
        return new TestSuite(TestRegressionOld.class);
    }
}



