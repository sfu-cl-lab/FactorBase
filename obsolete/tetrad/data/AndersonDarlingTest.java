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

import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements the Anderson-Darling test for normality, with P values calculated
 * as in R's ad.test method (in package nortest).
 * <p/>
 * Note that in the calculation, points x such that log(1 - normal_cdf(x))
 * is infinite are ignored.
 *
 * @author Joseph Ramsey
 */
public class AndersonDarlingTest {

    /**
     * The column of data being analyzed.
     */
    private double[] data;

    /**
     * The A^2 statistic for <code>data</code>
     */
    private double aSquared;

    /**
     * The A^2 statistic adjusted for sample size.
     */
    private double aSquaredStar;

    /**
     * The interpolated p value for the adjusted a squared.
     */
    private double p;
    private double sMean;
    private double sVariance;
    private int sN;
    private double[] sColumn;

    //============================CONSTRUCTOR===========================//

    /**
     * Constructs an Anderson-Darling test for the given column of data.
     */
    public AndersonDarlingTest(double[] data) {
        this.data = data;
        runTest();
    }

    //============================PUBLIC METHODS=========================//

    /**
     * Returns a copy of the data being analyzed.
     */
    public double[] getData() {
        double[] data2 = new double[data.length];
        System.arraycopy(data, 0, data2, 0, data.length);
        return data2;
    }

    /**
     * Returns the A^2 statistic.
     */
    public double getASquared() {
        return aSquared;
    }

    /**
     * Returns the A^2* statistic, which is the A^2 statistic adjusted
     * heuristically for sample size.
     */
    public double getASquaredStar() {                      
        return aSquaredStar;
    }

    /**
     * Returns the p value of the A^2* statistic, which is interpolated using
     * exponential functions.
     */
    public double getP() {
        return p;
    }

    //============================PRIVATE METHODS========================//

    private void runTest() {
        int n = data.length;

        double[] x = new double[data.length];
        System.arraycopy(data, 0, x, 0, data.length);
        x = leaveOutNaN(x);
        n = x.length;
        Arrays.sort(x);

        double[] _x = x; //leaveOutNaN(x);

        double mean = StatUtils.mean(_x);
        double sd = StatUtils.standardDeviation(_x);
        double[] y = new double[n];

//        Normal phi = new Normal(0, 1, RandomUtil.getInstance().getEngine());

        for (int i = 0; i < n; i++) {
            y[i] = (x[i] - mean) / sd;
        }

        double h = 0.0;
        double[] sColumn = new double[n];

        int numSummed = 0;

        for (int i = 0; i < n; i++) {
            double y1 = y[i];
            double a1 = Math.log(RandomUtil.getInstance().normalCdf(0, 1, y1));

            double y2 = y[n - i - 1];
            double a2 = Math.log(1.0 - RandomUtil.getInstance().normalCdf(0, 1, y2));

            double k = (2 * (i + 1) - 1) * (a1 + a2);
            sColumn[i] = k;

            if (!(Double.isNaN(a1) || Double.isNaN(a2) || Double.isInfinite(a1) || Double.isInfinite(a2))) {
                h += k;
                numSummed++;
            }
        }

        double a = -numSummed - (1.0 / numSummed) * h;
        double aa = (1 + 0.75 / numSummed + 2.25 / Math.pow(numSummed, 2)) * a;
        double p;

        if (aa < 0.2) {
            p = 1 - Math.exp(-13.436 + 101.14 * aa - 223.73 * aa * aa);
        } else if (aa < 0.34) {
            p = 1 - Math.exp(-8.318 + 42.796 * aa - 59.938 * aa * aa);
        } else if (aa < 0.6) {                                  
            p = Math.exp(0.9177 - 4.279 * aa - 1.38 * aa * aa);
        } else {
            p = Math.exp(1.2937 - 5.709 * aa + 0.0186 * aa * aa);
        }

        this.aSquared = a;
        this.aSquaredStar = aa;
        this.p = p;

        this.sColumn = sColumn;
        this.sMean = StatUtils.mean(sColumn);
        this.sVariance = StatUtils.variance(sColumn);
        this.sN = sColumn.length;
        this.sColumn = sColumn;
    }

    private double[] leaveOutNaN(double[] data) {
        List<Double> _leaveOutMissing = new ArrayList<Double>();

        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                _leaveOutMissing.add(data[i]);
            }
        }

        double[] _data = new double[_leaveOutMissing.size()];

        for (int i = 0; i < _leaveOutMissing.size(); i++) _data[i] = _leaveOutMissing.get(i);

        return _data;
    }

    public double getsMean() {
        return sMean;
    }

    public double getsVariance() {
        return sVariance;
    }

    public int getsN() {
        return sN;
    }

    public double[] getSColumn() {
        return sColumn;
    }
}

