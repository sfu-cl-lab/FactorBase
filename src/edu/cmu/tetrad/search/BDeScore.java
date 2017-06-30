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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Apr 19, 2009 Time: 7:28:45 PM To change this template use File |
 * Settings | File Templates.
 */
public class BDeScore implements LocalDiscreteScore {
    private final LocalScoreCache localScoreCache = new LocalScoreCache();
    private DataSet dataSet;

    public BDeScore(DataSet dataSet) {
        if (dataSet == null) {
            throw new NullPointerException();
        }

        this.dataSet = dataSet;
    }

    @Override
	public double localScore(int i, int parents[]) {
        double oldScore = localScoreCache.get(i, parents);

        if (!Double.isNaN(oldScore)) {
            return oldScore;
        }

        // Number of categories for i.
        int r = numCategories(i);

        // Numbers of categories of parents.
        int dims[] = new int[parents.length];

        for (int p = 0; p < parents.length; p++) {
            dims[p] = numCategories(parents[p]);
        }

        // Number of parent states.
        int q = 1;
        for (int p = 0; p < parents.length; p++) {
            q *= dims[p];
        }

        // Conditional cell counts of data for i given parents(i).
        int n_ijk[][] = new int[q][r];
        int n_ij[] = new int[q];

        int values[] = new int[parents.length];

        for (int n = 0; n < sampleSize(); n++) {
            for (int p = 0; p < parents.length; p++) {
                int parentValue = dataSet().getInt(n, parents[p]);

                if (parentValue == -99) {
                    throw new IllegalStateException("Please remove or impute " +
                            "missing values.");
                }

                values[p] = parentValue;
            }

            int childValue = dataSet().getInt(n, i);

            if (childValue == -99) {
                throw new IllegalStateException("Please remove or impute missing " +
                        "values (record " + n + " column " + i + ")");

            }

            n_ijk[getRowIndex(dims, values)][childValue]++;
        }

        // Row sums.
        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                n_ij[j] += n_ijk[j][k];
            }
        }

        //Finally, compute the score
        double score = 0;

        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                double nPrimeijk = 1. / (r * q);
                score += ProbUtils.lngamma(n_ijk[j][k] + nPrimeijk);
                score -= ProbUtils.lngamma(nPrimeijk);
            }

            double nPrimeij = 1. / q;

            score += ProbUtils.lngamma(nPrimeij);
            score -= ProbUtils.lngamma(n_ij[j] + nPrimeij);
        }

        localScoreCache.add(i, parents, score);

        return score;
    }

    @Override
	public DataSet getDataSet() {
        return dataSet;
    }

    private int getRowIndex(int[] dim, int[] values) {
        int rowIndex = 0;
        for (int i = 0; i < dim.length; i++) {
            rowIndex *= dim[i];
            rowIndex += values[i];
        }
        return rowIndex;
    }

    private int sampleSize() {
        return dataSet().getNumRows();
    }

    private int numCategories(int i) {
        return ((DiscreteVariable) dataSet().getVariable(i)).getNumCategories();
    }

    private DataSet dataSet() {
        return dataSet;
    }

    public double getStructurePrior() {
        return Double.NaN;
    }

    public double getSamplePrior() {
        return Double.NaN;
    }

    @Override
	public void setStructurePrior(double structurePrior) {
    }

    @Override
	public void setSamplePrior(double samplePrior) {
    }
}
