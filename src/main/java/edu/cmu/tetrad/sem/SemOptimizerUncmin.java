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

import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.util.TetradLogger;
import optimization.Uncmin_methods;

/**
 * Minimizes using the Uncmin_f77 nonlinear optimization method.
 *
 * @author Ricardo Silva
 */
public class SemOptimizerUncmin implements SemOptimizer {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS=========================//

    /**
     * Blank constructor.
     */
    public SemOptimizerUncmin() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerUncmin serializableInstance() {
        return new SemOptimizerUncmin();
    }

    //============================PUBLIC METHODS=========================//

    @Override
    public void optimize(SemIm semIm) {
        if (semIm == null) {
            throw new NullPointerException("SemIm must not be null.");
        }

        if (DataUtils.containsMissingValue(semIm.getSampleCovar())) {
            throw new IllegalArgumentException("Please remove or impute missing values.");
        }

        new SemOptimizerEm().optimize(semIm);

        UncminFittingFunction fittingFunction =
                new UncminFittingFunction(semIm);
        int numParams = semIm.getNumFreeParams();
        double[] init0Ind = semIm.getFreeParamValues();
        double[] init1Ind = new double[numParams + 1];
        double[] finalEst = new double[numParams + 1];
        double[] g = new double[numParams + 1];
        double[][] hessian = new double[numParams + 1][numParams + 1];
        double[] hessianDiag = new double[numParams + 1];

        for (int i = 1; i <= numParams; i++) {
            init1Ind[i] = init0Ind[i - 1];
        }

        init1Ind[0] = 1.0;

        double[] f = new double[2];
        int[] info = {0, 1};
        double[] typsiz = new double[numParams + 1];

        //for (int i = 0; i <= init0Ind.numParams; i++)
        //    typsiz[i] = 1.;
        double[] fscale = new double[2];    //{0., 10000.};
        int[] method = {0, 3};
        int[] iexp = {0, 0};
        int[] msg = new int[2];
        int[] ndigit = {0, 20};
        int[] itnlim = {0, 1500};
        int[] iagflg = {0, 0};
        int[] iahflg = {0, 0};
        double[] dlt = new double[2];
        double[] gradtl = {0., 0.000001};
        double[] stepmx = new double[2];
        double[] steptl = new double[2];

        //optimization.Uncmin_f77.optif0_f77(init0Ind.numParams, init1Ind, new OldSemEstimator2Aux(this),
        //                                   finalEst, f, g, info, hessian, hessianDiag);
        optimization.Uncmin_f77.optif9_f77(numParams, init1Ind, fittingFunction,
                typsiz, fscale, method, iexp, msg, ndigit, itnlim, iagflg,
                iahflg, dlt, gradtl, stepmx, steptl, finalEst, f, g, info,
                hessian, hessianDiag);
    }

    static class UncminFittingFunction implements Uncmin_methods {
        private SemIm sem;
        private double[] params0Ind;

        public UncminFittingFunction(SemIm sem) {
            this.sem = sem;
            this.params0Ind = new double[sem.getNumFreeParams()];
        }

        @Override
        public double f_to_minimize(double params1Ind[]) {
            System.arraycopy(params1Ind, 1, params0Ind(), 0,
                    sem().getNumFreeParams());
            sem().setFreeParamValues(params0Ind());
            double fml = sem().getFml();

            TetradLogger.getInstance().log("optimization", "FML = " + fml);

            if (Double.isNaN(fml)) {
                return 10000;
            }

            return fml;
        }

        @Override
        public void gradient(double x[], double g[]) {
        }

        @Override
        public void hessian(double x[], double h[][]) {
        }

        private double[] params0Ind() {
            return params0Ind;
        }

        private SemIm sem() {
            return sem;
        }
    }

}



