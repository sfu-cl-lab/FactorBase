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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ProbUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Apr 19, 2009 Time: 7:28:45 PM To change this template use File |
 * Settings | File Templates.
 */

/**
 * @author diljot,chris
 * We wont be using the localScoreCache anymore as it just runs along with the 
 * scoreHash and increases memory consumption while not providing any extra functionality
 * as noted by Dr. Oliver Schulte. The code here uses the globalScoreHash which is initialized
 * in BayesBase.h and does not lose its contents in multiple calls. It doesnt work
 * properly as the scores keep on changing between different calls, thus using the scores from old 
 * runs changes the structure of the bayesnet.
 * Instead of adding to the localScoreCache, we write the nodes to hash.
 * A simple way to fix the program would be to clear the cache in Ges3.java. Please see line 262
 * fix it. Although that will clear up cache and we wont get any hits,defeating the whole purpose and increasing the runtime.
 */
public class BDeuScore implements LocalDiscreteScore {
	//Diljot,Chris : We dont need this anymore.
   // private final LocalScoreCache localScoreCache = new LocalScoreCache();
    private DataSet dataSet;

    private double samplePrior = 10;
    private double structurePrior = 1.0;

    public BDeuScore(DataSet dataSet, double samplePrior, double structurePrior) {
        if (dataSet == null) {
            throw new NullPointerException();
        }

        this.dataSet = dataSet;
        this.samplePrior = samplePrior;
        this.structurePrior = structurePrior;
    }

    public double localScore(int i, int parents[], Node y, Set<Node> parentNodes, Map<Node, Map<Set<Node>, Double>> globalScoreHash) {
    	
    	
    	Double oldscore = null;
    	if(globalScoreHash.containsKey(y)){
    	if(globalScoreHash.get(y).containsKey(parentNodes)){
    		oldscore = globalScoreHash.get(y).get(parentNodes);
    		}
    	}
    		
    		if(oldscore != null && !Double.isNaN(oldscore)){
    			
    			//=================Writing to the file.=======================
    			//We just use this file to compare the scores later after execution is complete.
    			//The following code will write the contents of the hit to file
    			try{
    	        	   File file =new File("Hash-Hits");
    	           	
    	       		if(!file.exists()){
    	       			file.createNewFile();
    	       		}
    	       		
    	       		FileWriter fileWriter = new FileWriter(file.getName(),true);
    	       		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
    	       		bufferWriter.write("The Node:" +y +"\n The Parents: "+ parentNodes+ "\n The Score:" +oldscore);
    	       		bufferWriter.write("\n_______________________________________________________________________________\n");
    	       		bufferWriter.close();
    	           }
    	           catch(Exception e)
    	           {System.out.println("Error Writing");}
    			//===========================Writing to file complete===============
    	           
    		return oldscore;
    		}
    	
       /* double oldScore = localScoreCache.get(i, parents);

        if (!Double.isNaN(oldScore)) {
            return oldScore;
        }*/

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
        long n_ijk1[][] = new long[q][r];
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

       //     n_ijk[getRowIndex(dims, values)][childValue]++;
            for (int m = 0; m < dataSet().getMultiplier(n); m++){ // case expander May 1st, @zqian
             	n_ijk[getRowIndex(dims, values)][childValue]++;
           //  	LoopingCounter++; // LoopingCounter
             	}
       //      System.out.println(" dataSet().getMultiplier(n) " +dataSet().getMultiplier(n));
      //       System.out.println(" after looping, n_ijk  :" +getRowIndex(dims, values)+", " +childValue+", "+(n_ijk[getRowIndex(dims, values)][childValue])); //@zqian
             
             // case expander Jun 13rd, @zqian
             n_ijk1[getRowIndex(dims, values)][childValue] = n_ijk[getRowIndex(dims, values)][childValue]+dataSet().getMultiplier(n);
        }

        // Row sums.
        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                n_ij[j] += n_ijk[j][k];
            }
        }

        //Finally, compute the score
        double score = (r - 1) * q * Math.log(getStructurePrior());

        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                score += ProbUtils.lngamma(
                        getSamplePrior() / (r * q) + n_ijk[j][k]);
            }

            score -= ProbUtils.lngamma(getSamplePrior() / q + n_ij[j]);
        }

        score += q * ProbUtils.lngamma(getSamplePrior() / q);
        score -= (r * q) * ProbUtils.lngamma(getSamplePrior() / (r * q));
//        score -= r * ProbUtils.lngamma(getSamplePrior() / (r * q));
        /**
         * We don't need the localScoreCache anymore, so instead of hashing into the localscorecache, We 
         * would add the scores to the globalScoreHash which keeps the globalHashScores,
         * instead we check if we have the parent node in the hash map and put the child and
         * the score for that entry, else if the parent node doesn't exist in the hash, then we can 
         * add it to the hash and then add the children and scores.
         */
       // localScoreCache.add(i, parents, score);
        if(globalScoreHash.containsKey(y)){
        	globalScoreHash.get(y).put(parentNodes, score);	
        }
        else{
        	globalScoreHash.put(y, new HashMap<Set<Node>, Double>());
        	globalScoreHash.get(y).put(parentNodes, score);
        }
        
        //===============File =================
        ///Another file keeps track of the times when we don't get a hit in the cache.
        try{
     	   File file =new File("NoHit");
        	
    		if(!file.exists())
    			file.createNewFile();
    		
    		FileWriter fileWriter = new FileWriter(file.getName(),true);
    		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
    		bufferWriter.write("The Node:" +y +"\n The Parents: "+ parentNodes+ "\n The Score:" +score);
    		bufferWriter.write("\n_______________________________________________________________________________\n");
    		bufferWriter.close();
        }
        catch(Exception e)
        {System.out.println("Error Writing");}
        //======================================End writing===============
        
        return score;
    }

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
        return structurePrior;
    }

    public double getSamplePrior() {
        return samplePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public void setSamplePrior(double samplePrior) {
        this.samplePrior = samplePrior;
    }
}

