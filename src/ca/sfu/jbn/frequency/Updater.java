package ca.sfu.jbn.frequency;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tetrad.bayes.*;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.Evidence;

public class Updater {
	ApproximateUpdater approixUpdater;
//	RowSummingExactUpdater approixUpdater;
//	CptInvariantUpdater approixUpdater;
	
	Evidence e;
	Query query;
	public Updater(BayesIm bayesIm, Query q){
		query = q;
		e = q.getEvidence(bayesIm);
		
		// approixUpdater = new RowSummingExactUpdater(bayesIm, e);
		 approixUpdater = new ApproximateUpdater(bayesIm, e);
		// approixUpdater = new CptInvariantUpdater(bayesIm, e);
	}
	public double[] getProbability(){
		int val = e.proposition.getNodeIndex(query.queryName);
		double[] res = new double[query.queryValue.size()];
		for(int i = 0; i < query.queryValue.size(); i++)
			res[i] = approixUpdater.getMarginal(val, i);
		return res;
	}

	public static void main(String args[]){
		BayesIm bayes = new BayesProbCounter().learnBayes();
		new QueryGenerator(bayes);
		ArrayList<Query> qList = QueryGenerator.QueryReader("movieLens.txt");
		BayesStat stat = new BayesStat(bayes);
		
		
		System.out.println("Updater:");
		long l1 = System.currentTimeMillis();
		for (Query query : qList){
		
			double prob[] = new Updater(bayes,query).getProbability();
//			query.print();
			for(int i =0;i<prob.length;i++){
				System.out.println(prob[i]);
			}
			
		}
		long l2 = System.currentTimeMillis();
		
		
		
		long l3 = System.currentTimeMillis();
		System.out.println("SQL:");
		for (Query query : qList){
		
			List<Double> prob = stat.getRowProbabilities(query);
//			query.print();
			for(int i =0;i<prob.size();i++){
				System.out.println(prob.get(i));
			}
			
		}
		long l4 = System.currentTimeMillis();
		
		System.out.println("Bayes:"+(l2-l1)+"ms");
		System.out.println("SQL:"+(l4-l3)+"ms");
				
	}
}
