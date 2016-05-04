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

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;

public class ExploreSemEstimatorGibbs {

    public static void main(String[] args) {

        int sampleSize = 200;
        Graph leadGraph = GraphUtils.randomDag(5, 2, 5, 3, 3, 3, false);
        SemPm semPm = new SemPm(leadGraph);
        SemIm semIm = new SemIm(semPm);
        DataSet dat = semIm.simulateData(sampleSize, false);
        ICovarianceMatrix covMatrix = new CovarianceMatrix(dat);
        double[][] sampleCovars1 = covMatrix.getMatrix().toArray();

//		System.out.println("data = "+dat);

		System.out.println("Graph as retrieved from SemPm");
		System.out.println(semPm.getGraph());
		System.out.println(semIm);

        semIm.setCovMatrix(covMatrix);

        System.out.println(semIm.getEdgeCoef());

        System.out.println(covMatrix);

		//Parameters
        boolean flatPrior = true;
        double stretch = 2.0;
        int numIterations = 2500;

        SemEstimatorGibbs gibbsEstimator = new SemEstimatorGibbs(semPm, semIm, sampleCovars1, flatPrior, stretch, numIterations);

        gibbsEstimator.estimate();

        SemIm gibbsSemIm = gibbsEstimator.getEstimatedSem();

		System.out.println(gibbsSemIm);

		DoubleMatrix2D data = gibbsEstimator.getDataSet();

        double[] means = new double[data.rows()];

        // print means
        for (int i = 0; i < data.rows(); i++){

            double v = 0.0;
            for (int j = 0; j < data.columns(); j++){
                if (Double.isInfinite(data.get(i, j))) continue;

                v += data.get(i,j);
            }
            v = v/data.columns();
            means[i] = v;

            Parameter parameter = gibbsSemIm.getSemPm().getParameters().get(i);
            gibbsSemIm.setParamValue(parameter, means[i]);

            System.out.println("mean(" + parameter + ") = " + v);
        }

//        System.out.println(gibbsSemIm);

        SemStdErrorEstimator errors = new SemStdErrorEstimator();

        errors.computeStdErrors(gibbsSemIm);

        double[] eArray = errors.getStdErrors();//new double[gibbsSemIm.getFreeParameters().size()];

        for (int i=0; i<eArray.length; i++){
            Parameter parameter = gibbsSemIm.getSemPm().getParameters().get(i);
            System.out.println("SE(" + parameter + ") = " + eArray[i]);
        }

        System.out.println("\n\nTest using standard sem estimation.");
//        Graph graph = constructGraph1();
//        SemPm semPm2 = new SemPm(leadGraph);
//       CovarianceMatrix covMatrix = constructCovMatrix1();
        SemEstimator estimator = new SemEstimator(covMatrix, new SemPm(leadGraph));
//        System.out.println();
//        System.out.println("... Before:");
//        System.out.println(estimator);
        estimator.estimate();
//		System.out.println();
//        System.out.println("... After:");
//        System.out.println(estimator);
//

        SemStdErrorEstimator errors2 = new SemStdErrorEstimator();

        errors2.computeStdErrors(estimator.getEstimatedSem());

        double[] eArray2 = errors2.getStdErrors();//new double[gibbsSemIm.getFreeParameters().size()];

        for (int i=0; i<eArray2.length; i++){
            Parameter parameter = gibbsSemIm.getSemPm().getParameters().get(i);
            System.out.println("SE(" + parameter.getName() + ") = " + eArray2[i]);
        }

//		System.out.println("Calculating Richard's Score");
//		System.out.println();
//
//		DoubleMatrix2D coef1 = estimator.getEstimatedSem().getEdgeCoef();
//		DoubleMatrix2D coef2 = gibbsEstimator.getEstimatedSem().getEdgeCoef();
//        System.out.println(coef1);
//        System.out.println(coef2);
//		double score = 0.0;
//		for (int i = 0; i < coef1.size(); i++){
//			double c1 = coef1.get(i%coef1.rows(), (int) i/coef1.rows());
//			double c2 = coef2.get(i%coef2.rows(), (int) i/coef2.rows());
//			double val = (c1-c2);
//			score += val*val;
//		}
//
//		System.out.println(coef1);
//		System.out.println();
//		System.out.println(coef2);
//		System.out.println();
//		System.out.println("score: "+score);

	}

}

