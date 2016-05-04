package ca.sfu.infer.Analyzer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;

public class AccuracyAnalyzer {
	double loglikelihood = 0.0;

	class Entry {
		public String predicate;
		public String att;
		public String key;
		double weight;

		Entry(String pre, String att, String key, double weight) {
			this.predicate = pre;
			this.att = att;
			this.key = key;
			this.weight = weight;
		}

		Entry(String strLine,Boolean isDBentry) {
			key="";
			if (strLine == null || strLine.length() <= 0)
				return;
			strLine = strLine.replace(" ", "");
			String s[] = strLine.split("[(,)]");
			if (s.length < 3) {
				return;
			}
//			if (s.length > 4) {
			if(isDBentry){
				int length=s.length;
				predicate=s[0];
				for(int i=1;i<length-1;i++){
					key+=s[i];
				}
				att=s[length-1];
			}
			else{
				int length=s.length;
				weight=Double.parseDouble(s[length-1]);
				predicate=s[0];
				for(int i=1;i<length-2;i++){
					key+=s[i];
				}
				att=s[length-2];
//			}
//			else {
//				predicate = s[0];
//				key = s[1];
//				att = s[2];

//				if (s.length >= 4)
//					weight = Double.parseDouble(s[3]);
			}

		}

		boolean isCorrect(String pre2, String att2, String key2) {
			if (predicate.equals(pre2) && att.equals(att2) && key.equals(key2))
				return true;
			else
				return false;
		}
	}

	class AUC {
		public final int NUM = 11;
		public final String positive;
		public double threshhold[];
		public double TPR[];
		public double FPR[];
		public int TP[];
		public int FP[];
		public int TN[];
		public int FN[];
		public double area;
		DecimalFormat formatter = new DecimalFormat("0.00000");

		AUC(String pos) {
			positive = pos;
			threshhold = new double[NUM];
			TP = new int[NUM];
			FP = new int[NUM];
			TN = new int[NUM];
			FN = new int[NUM];
			TPR = new double[NUM];
			FPR = new double[NUM];
			double t = 1.0 / (NUM - 1);
			for (int i = 0; i < NUM; i++) {
				threshhold[i] = t * i;
				TP[i] = 0;
				FP[i] = 0;
				TN[i] = 0;
				FN[i] = 0;
			}
			area = 0.0;
		}

		boolean Add(Entry envident, Entry infer) {
			if (!envident.predicate.equals(infer.predicate))
				return false;
			if (!envident.key.equals(infer.key))
				return false;

			double probability;

			for (int i = 0; i < NUM; i++) {

				if (positive.equals(infer.att))
					probability = infer.weight;
				else
					probability = 1 - infer.weight;

				if (probability > threshhold[i])
					if (positive.equals(envident.att))
						TP[i]++;
					else
						FP[i]++;
				else if (positive.equals(envident.att))
					FN[i]++;
				else
					TN[i]++;
			}
			return true;
		}

		void result() {

			for (int i = 0; i < NUM; i++) {
				TPR[i] = (double) TP[i] / (TP[i] + FN[i]);
				FPR[i] = (double) FP[i] / (FP[i] + TN[i]);
			}

			area = 0.0;
			for (int i = 1; i < NUM; i++) {
				double h = FPR[i - 1] - FPR[i];
				double w = TPR[i - 1] + TPR[i];
				area = area + h * w / 2;
			}
		}

		String resultString() {
			result();
			String output = new String("Positive= " + positive + "\n");
			for (int i = 0; i < NUM; i++) {
				output = output.concat("With threshhold "
						+ formatter.format(threshhold[i]) + " TP=" + TP[i]
						+ " TN=" + TN[i] + " FP=" + FP[i] + " FN=" + FN[i]
						+ "\n" + " TPR=" + formatter.format(TPR[i])
						+ " and FPR=" + formatter.format(FPR[i]) + "\n");
			}
			output = output.concat("AUC= " + area + "\n");
			return output;
		}
	}

	String currentPredicate;
	String maxAtt;
	double maxWeight;
	double sumWeight;
	double entryweight;
	String currentKey;

	int correct;
	int incorrect;

	DecimalFormat formatter = new DecimalFormat("0.00000");

	AccuracyAnalyzer() {
		newPredicate();
	}

	void newEntry() {
		maxAtt = "";
		maxWeight = 0;
		sumWeight = 0;
		currentKey = "";
	}

	void newPredicate() {
		currentPredicate = "";
		correct = 0;
		incorrect = 0;
		newEntry();
	};

	void CleanSetting() {
		currentPredicate = "";
		maxAtt = "";
		maxWeight = 0;
		sumWeight = 0;
		currentKey = "";
		correct = 0;
		incorrect = 0;

	};

	void Analyze(String dbfile, String mlnfile, PrintStream output) {
		try {
			FileInputStream fstream = new FileInputStream(mlnfile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			FileInputStream fstream2 = new FileInputStream(dbfile);
			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader dbReader = new BufferedReader(new InputStreamReader(
					in2));

			String strLine;
			String bdstrLine = null;
			strLine = br.readLine();
			Entry entry = new Entry(strLine,false);
			AUC aucAnalyzer = new AUC(entry.att);
			while ((bdstrLine = dbReader.readLine()) != null
					&& bdstrLine.length() > 0) {
				Entry dbentry = new Entry(bdstrLine,true);

				currentPredicate = "";
				// newPredicate();

				while (entry.key != null && entry.key.equals(dbentry.key)) {

					currentPredicate = entry.predicate;
					currentKey = entry.key;
					sumWeight += entry.weight;
					if (maxWeight < entry.weight) {
						maxWeight = entry.weight;
						maxAtt = entry.att;
					}
					if (entry.att.equals(dbentry.att)) {
						 entryweight = entry.weight;
					}
					strLine = br.readLine();
					if (strLine == null || strLine.length() <= 0)
						break;
					entry = new Entry(strLine,false);
				}
				if (dbentry.isCorrect(dbentry.predicate, maxAtt, currentKey))
					correct++;
				else
					incorrect++;
				loglikelihood+= Math.log(entryweight /sumWeight);
				double probability = maxWeight / sumWeight;
				output.append(currentKey + " " + maxAtt + " "
						+ formatter.format(maxWeight) + "\t"
						+ formatter.format(probability) + "\n");

				Entry result = new Entry(currentPredicate, maxAtt, currentKey,
						probability);

				aucAnalyzer.Add(dbentry, result);

				newEntry();
			}
			output.append("Accuracy is " + correct + "/"
					+ (correct + incorrect) + " = " + (double) correct
					/ (correct + incorrect) + "\n");
			output.append("loglikelihood is" + loglikelihood
					/ (correct + incorrect) + "\n");
			output.append(aucAnalyzer.resultString());
			in.close();

			output.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {

		String dbName =  args[0];
		String inputName =  args[1];

		PrintStream out = new PrintStream(System.out);

		AccuracyAnalyzer analyzer = new AccuracyAnalyzer();
		analyzer.Analyze(dbName, inputName, out);
	}
}