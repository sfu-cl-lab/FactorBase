package ca.sfu.jbn;

import java.io.IOException;
import java.sql.SQLException;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;

public class ParameterLearning_main {
	 
	public static void main(String[] args) {

//		global.schema = "Hepatitis_std";
		
		if (args.length == 1) {
			global.schema = args[0];
		} else if (args.length == 4) {
			global.schema = args[0];
			global.dbURL = args[1];
			global.dbUser = args[2];
			global.dbPassword = args[3];
		} else {
			System.out
					.println("argument: dataset name <database connection><databse user><database password>");
			System.exit(1);
		}
		// initialize xml converter to stage the xml file
		db database  = new db();
		ReadXML sqlToXMLReader = new ReadXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		
		// xml file is ready

		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out.println("Running time of Structure Learning(ms):    ");
		System.out.println(l2 - l);
		
		// start Paramter Learning
		long l3 = System.currentTimeMillis();
		ParamTet t = new ParamTet(bayes);
		try {
			BayesIm a = t.paramterlearning();
//			System.out.println(a);
//			System.out.println();
			ParameterLearning_main.BIFExportHelper(a);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long l4 = System.currentTimeMillis();
		System.out.println("Running time of parameter Learning(ms):    ");
		System.out.println(l4 - l3);

		
		db.closeDB();
	}
	
	
	/* 
	 * Print out a BIF format (partial) in the standard output
	 * Used for UBC AI Space
	 */
	private static void BIFExportHelper(BayesIm a)
	{
		double x = 7000.0, y = 4000.0;
		for (int i=0; i<a.getBayesPm().getNumNodes(); i++)
		{
			Node node = a.getNode(i);
			
			System.out.println("<VARIABLE TYPE=\"nature\">");
			System.out.println("\t<NAME>" + node.getName() + "</NAME>");

			for (int j=0; j<a.getBayesPm().getNumCategories(node); j++)
			{
				System.out.println("\t<OUTCOME>" + a.getBayesPm().getCategory(node, j) + "</OUTCOME>");
			}
			System.out.println("\t<PROPERTY>position = (" + x + "," + y + ")</PROPERTY>");
			System.out.println("</VARIABLE>");
			System.out.println();
			x += 100.0;
		}
		
		for (int i=0; i<a.getBayesPm().getNumNodes(); i++)
		{
			Node node = a.getNode(i);
			System.out.println("<DEFINITION>");
			System.out.println("\t<FOR>" + node.getName() + "</FOR>");
			for (int pid : a.getParents(i))
			{
				System.out.println("\t<GIVEN>" + a.getNode(pid).getName() + "</GIVEN>");	
			}
			System.out.print("\t<TABLE>");
			
			int numRow = a.getNumRows(i);
			int numCol = a.getNumColumns(i);
			
			/* Normalize the probabilities to exactly 1.0 */
			for (int p=0; p<numRow; p++)
			{
				double sum = 0;
				for (int q=0; q<numCol; q++)
				{
					sum += a.getProbability(i, p, q);
				}
				double rem = 0.0;
				for (int q=0; q<numCol; q++)
				{
					if (p + q > 0) System.out.print(" ");
					double prob = a.getProbability(i, p, q);
					if (sum == 0.0) prob = 1.0 / numCol;
					else prob = prob / sum;
					int pp = (int) (prob * 1000);
					prob = pp / 1000.0;
					if (q != numCol - 1)
						System.out.print(prob);
					else
						System.out.printf("%.3f", 1 - rem);
					rem += prob;
				}
			}
			
			System.out.println("</TABLE>");
			System.out.println("</DEFINITION>");
			System.out.println();
		}
	}

}
