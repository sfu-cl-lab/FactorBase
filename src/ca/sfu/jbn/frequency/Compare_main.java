package ca.sfu.jbn.frequency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.jbn.common.global;

import edu.cmu.tetrad.bayes.BayesIm;

public class Compare_main {

	public static void compare(String fileName, String outname) {


		String outputFile = outname;

		BayesIm bayes = new BayesProbCounter().learnBayes();
		new QueryGenerator(bayes);
		ArrayList<Query> qList = QueryGenerator.QueryReader(fileName);
		BayesStat stat = new BayesStat(bayes);

		StringBuffer output = new StringBuffer();


		ArrayList<String> sentence1 = new ArrayList<String>();

		// 1. Fill queries
		int total = 0;
		for (Query query : qList){
			for(int i = 0 ; i<query.queryValNum;i++){
				sentence1.add(query.getString(i));
				total++;
			}
		}

		int count = 0;
		NumberFormat formatter = new DecimalFormat(
		"0.0000");



		// 2. Fill Updaters
		ArrayList<String> sentence2 = new ArrayList<String>();
		System.out.println("Doing By Updater:");

		long l1 = System.currentTimeMillis();
		for (Query query : qList){

			double prob[] = new Updater(bayes,query).getProbability();
			//			query.print();
			for(int i =0;i<prob.length;i++){
				Double weight = prob[i];
				if (weight.isNaN()) weight=0.0;
				sentence2.add(sentence1.get(count)+" "+formatter.format(weight));
				count++;
				System.out.println(count+"/"+total);
			}
		}
		long l2 = System.currentTimeMillis();
		System.out.println("Bayes:"+(l2-l1)+"ms");


		// 3. Fill SQL
		ArrayList<String> sentence3 = new ArrayList<String>();
		System.out.println("Doing By SQL:");
		count=0;
		long l3 = System.currentTimeMillis();

		for (Query query : qList){

			List<Double> prob = stat.getRowProbabilities(query);
			//			query.print();
			for(int i =0;i<prob.size();i++){
				Double weight = prob.get(i);
				if (weight.isNaN()) weight=0.0;
				sentence3.add(sentence2.get(count)+" "+formatter.format(weight));
				count++;
				System.out.println(count+"/"+total);
			}

		}
		long l4 = System.currentTimeMillis();


		System.out.println("SQL:"+(l4-l3)+"ms");

		
		
		String timeinfo = "Time cost: "+"Bayes:"+(l2-l1)+"ms  "+ "SQL:"+(l4-l3)+"ms";
		output.append("Query Updater SQL Correlation "+ timeinfo +"\n");
		for(int i = 0 ;i < count;i++){
			output.append(sentence3.get(i)+"\n");

		}


		File file = new File(outputFile);
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(output.toString().getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void generate(String Filename){

		int QueryNum = 100;

		QueryGenerator.NumWriter(Filename, QueryNum);

		QueryGenerator qGen = new QueryGenerator();

		for (int i = 0; i< QueryNum;i++){
			int EviNodeNum=(int)(Math.random()*3)+1;

			Query query=qGen.generate(EviNodeNum);
			QueryGenerator.QueryWriter(Filename,query);
		}

		ArrayList<Query> qList=QueryGenerator.QueryReader(Filename);
		for(int j=0;j<qList.size();j++){
			qList.get(j).printAll();
		}

	}

	public static void main(String args[]){
		//		String database = "MovieLens";
		
		global.schema="unielwin";
		global.initialize();

		String filename = global.schema+"_0.txt";
		//String outname = "result_unielwin_0.txt";
		
		generate(filename);
		
		//compare(filename,outname);


	}
	

	
}
