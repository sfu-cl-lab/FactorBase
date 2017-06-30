package ca.sfu.jbn.frequency;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;

/***********************************
 * BayesProbCounter will learn the bayesNet from given
 * database (in global class), and it can count the 
 * joint probability from the specified queries 
 */



public class BayesProbCounter {

	private BayesIm bayesIm;

	// BayesIM will be learned from database, with all 
	// true and * values correctly filled

	public void load() throws IOException,
	ClassNotFoundException {

		String filename = global.schema+"/"+global.schema + ".bin";
		InputStream file;
		try {
			file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput object = new ObjectInputStream(buffer);
			bayesIm = (MlBayesIm) object.readObject();
			// bayesPm = (BayesPm) object.readObject();
			// System.out.println(FinalIm);
			object.close();
			// System.out.println(FinalIm.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Not Exist! Please learn first!");
			e.printStackTrace();
		}
	}


	public BayesIm loadBayes(){
		//System.out.println("loading "+global.schema);
		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		try {
			load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bayesIm;
	}

	public BayesIm learnBayes(){
		// initialize xml converter to stage the xml file
		System.out.println("Learning "+global.schema);
		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		// structure Learning
		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out
		.print("SLtime(ms):  ");
		System.out.println(l2 - l);

		// Parameter Learning
		long l3 = System.currentTimeMillis();
		ParamTet t = new ParamTet(bayes);
		try {
			bayesIm = t.paramterlearning();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		long l4 = System.currentTimeMillis();

		System.out
		.print("PLtime(ms):  ");
		System.out.println(l4 - l3);

		return bayesIm;
	}


	public BayesIm learnBayesModel(){
		// initialize xml converter to stage the xml file
		System.out.println("Learning " + global.schema);
		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		// structure Learning
		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out
		.print("SLtime(ms):  ");
		System.out.println(l2 - l);

		bayesIm = new MlBayesIm(bayes);

		return bayesIm;
	}

	public double getJoinProb(QueryGenerator queryGen){
		double prob = 0;

		// Get the join prob in the bayesIm from the 
		// given query generator

		// probably you can try to use updater to help

		return prob;		
	}
	public static void main(String args[]){
		global.schema="hepelwin";
		global.initialize();
		new BayesProbCounter().learnBayes();
	}

	/*****************************************************************
	ALL PRIVATE METHODS WRITE AFTER THIS:	
	 *****************************************************************/


}
