package ca.sfu.jbn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.frequency.BayesStat;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import cs.sfu.jbn.alchemy.ExportToMLN;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;

public class MLN_ParameterLearning_main {

	//method: cpt,log,ls,lsn,  mbn
	public static void main(String[] args) {
		String method="lsn";
		global.schema="imdb_alchemy";
		
//		String method = null;
//		if (args.length == 2) {
//			global.schema = args[0];
//			method = args[1];
//		} 		
//		else {
//			System.out.println("argument: dataset name, method");
//			System.exit(1);
//		}
		
		// initialize xml converter to stage the xml file

		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready


		String temp = method;
		if (method.equals("mbn")) method="mbns";
		
		ReadSQL_MLN_Files r = new ReadSQL_MLN_Files();
		try {
			r.initialize();
			PrintStream out1 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + ".db"));
			PrintStream out2 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
					+ "_VJ_"+method+".mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
					+ "predicate_temp.mln"));

			method = temp;
			r.read(out1, out2,out3);
			out1.close();
			out2.close();
			out3.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		
		long l2 = System.currentTimeMillis();
		System.out
		.print("SLtime(ms):  ");
		System.out.println(l2 - l);
		
		// start Paramter Learning
		long l3 = System.currentTimeMillis();
		ParamTet t = new ParamTet(bayes);
		
		String unitClauses = null;
		try {
			BayesIm a = t.paramterlearning();
			
			BayesStat stat = new BayesStat(a);
			unitClauses = stat.getUnitClauseString();
//			System.out.println(a);
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
		ExportToMLN export = new ExportToMLN();

		/* yuke added for unit clauses */
		if (unitClauses != null)
		{
			System.out
			.println("\nCreate unit clause file...\n");
			try {
				FileOutputStream outputStream= new FileOutputStream(global.WorkingDirectory + "/" + global.schema
						+ "_unit_clauses" + ".mln", true);
				outputStream.write(unitClauses.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		StringBuffer rules;
		try {
			rules = export.export(method);


			if (method.equals("mbn")) method="mbns";
			
			FileOutputStream outputStream= new  FileOutputStream(global.WorkingDirectory + "/" + global.schema
					+ "_VJ_"+method+".mln",true);
			
			method=temp;
			outputStream.write(rules.toString().getBytes());
			outputStream.close();
		} catch (Exception e) {

		}
		
		db.closeDB();

	}
}
