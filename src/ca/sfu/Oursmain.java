package ca.sfu;

import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesPm;

public class Oursmain {
	public static void main(String[] args) throws Exception {
		long start2=System.currentTimeMillis();
		S_learning a = new S_learning(2);
		BayesPm bayesPm =a.major();
		System.out.println("Structure Learning run time "+(System.currentTimeMillis() - start2));
		System.out.println("parameter Learning");
		long start= System.currentTimeMillis();
		ParamTet n = new ParamTet(bayesPm);
		try {
			n.paramterlearning();

			System.out.println("parameter Learning run time  "+ (System.currentTimeMillis() - start));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
