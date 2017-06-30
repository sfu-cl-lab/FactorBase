package ca.sfu.jbn;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.SQLException;


import cs.sfu.jbn.alchemy.ExportToMLN;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;

public class MBN_learning {



	public static void main(String[] args) throws IOException {

		if (args.length == 1) {
			global.schema = args[0];

		}  else {
			System.out
					.println("argument: dataset");
			System.exit(1);
		}
		//read the config.xml here and put db connectio info into global

		
		// initialize xml converter to stage the xml file
		System.out.println("Starting MBN learning package on "
				+ global.schema);
		ReadXML sqlToXMLReader = new CorrelatedSQLToXML();
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
					+ "_MBN_.mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate_temp.mln"));
			
			r.read(out1, out2,out3);
			
			System.out.println("DB file and MLN predicate file created");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stucture learning begins");

		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out
				.print("Structure learning ends. Running time of Structure Learning(ms):   ");
		System.out.println(l2 - l);
		
		
	BayesIm FinalIm = new MlBayesIm(bayes);
		ParamTet t = new ParamTet(bayes);
	t.writeToBinFile(FinalIm);
		ExportToMLN export = new ExportToMLN();
		StringBuffer rules = export.export("mbn");
		Writer output = null;
//		File file = new File(global.WorkingDirectory + "/" + global.schema
//				+ "_VJ_.mln");
		try {
		
		FileOutputStream outputStream= new  FileOutputStream(global.WorkingDirectory + "/" + global.schema
				+ "_MBN_.mln",true);
		//	output=new BufferedWriter(new FileOoutputStream); 
		outputStream.write(rules.toString().getBytes());
//		output.append(rules.toString());
			
			System.out.println("MLN ready for use " + global.WorkingDirectory
					+ "/" + global.schema + "_MBN_.mln");
		} catch (Exception e) {

		}

		db.closeDB();

	}
}
