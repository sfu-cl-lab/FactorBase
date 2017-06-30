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

package edu.cmu.tetrad.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.RandomUtil;

import cern.colt.matrix.DoubleMatrix2D;


/**
 * Tests basic functionality of the statistical utilities.
 *
 * @author Joseph Ramsey
 */
public class TestStatUtils extends TestCase {
    public TestStatUtils(String name) {
        super(name);
    }

    /**
     * Tests that the unconditional correlations and covariances are correct,
     * at least for the unconditional tests.
     */
    public void testConditionalCorrelation() {

        RandomUtil.getInstance().setSeed(302995833L);

        // Make sure the unconditional correlations and covariances are OK.
        Graph graph = GraphUtils.randomDag(5, 0, 5, 3, 3, 3, false);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(1000, false);
        double[] x = dataSet.getDoubleData().viewColumn(0).toArray();
        double[] y = dataSet.getDoubleData().viewColumn(1).toArray();

        double r1 = StatUtils.correlation(x, y);
        double s1 = StatUtils.covariance(x, y);
        double v1 = StatUtils.variance(x);
        double sd1 = StatUtils.standardDeviation(x);

        ICovarianceMatrix cov = new CovarianceMatrix(dataSet);
        DoubleMatrix2D _cov = cov.getMatrix();

        System.out.println(cov);

        double r2 = StatUtils.partialCorrelation(_cov, 0, 1);
        double s2 = StatUtils.partialCovariance(_cov, 0, 1);
        double v2 = StatUtils.partialVariance(_cov, 0);
        double sd2 = StatUtils.partialStandardDeviation(_cov, 0);

        assertEquals(r1, r2, .01);
        assertEquals(s1, s2, .01);
        assertEquals(v1, v2, .01);
        assertEquals(sd1, sd2, 0.01);
    }

    public void testRankCorr() {
        double[] a1 = new double[] {2, 2, 3};
        double[] a2 = new double[] {2, 3, 4};

        double r = StatUtils.rankCorrelation(a1, a2);
        System.out.println("rank corr = " + r);
    }

    public static Test suite() {
        return new TestSuite(TestStatUtils.class);
    }
}
