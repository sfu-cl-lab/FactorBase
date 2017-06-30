package ca.sfu.jbn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.SQLException;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.frequency.BayesStat;
import ca.sfu.jbn.parameterLearning.Decisiontree;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;

//method: dtmbn, dtcpt, dtls, dtlsn, dtlog
public class MLN_DecisionTreeLearner_main {
	//private db db = new db();
	public static void main(String[] args) {
		// method = dtlog or dtmbn
		String method = "dtmbn";
		global.schema = "unielwin";
		
		if (args.length == 4) {
			global.schema = args[0];
			global.dbURL = args[1];
			global.dbUser = args[2];
			global.dbPassword = args[3];
		} else if (args.length >= 2) {
			global.schema = args[0];
			method = args[1];
		} else {
			System.out
			.println("argument: dataset name <database connection><databse user><database password>");
			System.exit(1);
		}

		// initialize xml converter to stage the xml file

		System.out.println(global.schema+" "+method);
		
		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		ReadSQL_MLN_Files r = new ReadSQL_MLN_Files();
		try {
			r.initialize();
			PrintStream out1 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + ".db"));
			PrintStream out2 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate.mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate_temp.mln"));
			
			r.read(out1, out2,out3);
			//System.out.println("DB file and MLN predicate file created");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		//System.out.println("Stucture learning begins");
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out
				.print("SLtime(ms):   ");
		System.out.println(l2 - l);

		String unitClauses = null;
		try 
		{
			ParamTet t = new ParamTet(bayes);
			BayesIm a = t.paramterlearning();
			BayesStat stat = new BayesStat(a);
			unitClauses = stat.getUnitClauseString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		/* yuke added for unit clauses */
		if (unitClauses != null)
		{
			System.out
			.println("Create unit clause file...");
			try {
				FileOutputStream outputStream= new FileOutputStream(global.WorkingDirectory + "/" + global.schema
						+ "_unit_clauses" + ".mln", false);
				outputStream.write(unitClauses.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			//System.out.println("Parameter learning using Decision Trees begins");
			long l3 = System.currentTimeMillis();
		
			String rules = "";

			if (method.equals("dtmbn")){
				Decisiontree tree = new Decisiontree(bayes);
				rules = tree.decisionTreeLearner(method);
//				DecisiontreeStruct tree = new DecisiontreeStruct(bayes);
//				rules = tree.decisionTreeLearner();
			}
			else {
				Decisiontree tree = new Decisiontree(bayes);
				rules = tree.decisionTreeLearner(method);
				
				if (method.equals("dtlsn")){
//					append output
					System.out.println("Append unit clauses to mln file...");
					
					String newoutput = tree.stat.getUnitClauseString();
					rules=rules+newoutput;
				}
			}
			 
			long l4 = System.currentTimeMillis();
			System.out.print("PLtime(ms):  ");
			System.out.println(l4 - l3);
			Writer output = null;
			
			File file = null;
			
			if (method.equals("dtmbn")){ 
				file = new File(global.WorkingDirectory + "/" + global.schema
					+ "_VJ_dtstruct.mln");
			}
			else{
				file = new File(global.WorkingDirectory + "/" + global.schema
						+ "_VJ_"+method+".mln");
			}
			
			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write(rules.toString());
				output.close();
			} catch (Exception e) {

			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		
		
		//db.closeDB();
	}
}
