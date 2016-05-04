package ca.sfu.jbn.common;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;

//The class takes in the name of a table and returns a rectangular dataset for that table

public class GetDataset {
	public db database;

	public GetDataset() {
		database = new db();
		database.reconnect();
	}
	private static GetDataset gd = new GetDataset();
	
	public static GetDataset getInstance(){
		return gd;
	}

	public ColtDataSet getData(String tablename) throws Exception {
		DataWrapper dw = new DataWrapper();
		ArrayList result = database.describeTable(tablename);
		List<Node> resultToNodes = new ArrayList<Node>();
		for (Object s : result) {
			DiscreteVariable gn = new DiscreteVariable(s.toString());
			resultToNodes.add(gn);
		}

//		int size = database.countStar(tablename);
//		ColtDataSet dataset = new ColtDataSet(result.size(), resultToNodes);

//		dataset.ensureRows(size);
//		List<String> excludedVariableNames = new ArrayList<String>();
//		dataset.ensureColumns(result.size(), excludedVariableNames );
		// int i = 0;
		// for (Object s : result) {
//		String allData= database.getAllRowsInString(tablename);
//			for (int j = 0; j < result.size(); j++) {
//				// dataset.setInt(j, i, Integer.parseInt(data.get(j)));
//				int index = i * result.size() + j;
//				// System.out.println(index);
//				String value = data.get(index);
////				System.out.println(value);
//				dataset.setObject(i, j, value);
//				// dataset.setInt(j, i, Integer.parseInt(data.get(j)));
//			}
//			// System.out.println(i);
//		}
		// i++;
//		System.out.println("done");
		// }

//		System.out.println(newDataSet.toString());
//		return newDataSet;
		DataParser parser = new DataParser();

        parser.setDelimiter(DelimiterType.COMMA);
        parser.setVarNamesSupplied(true);
        parser.setIdsSupplied(false);
//        parser.setMaxIntegralDiscrete(getMaxDiscrete());

        String string = new String(database.getAllRowsInString(tablename));
        ArrayList<String> variableNames=database.getColumns(tablename);
        String varNames ="";
        for(String variableName:variableNames){
        	varNames+=variableName+",";
        }
        varNames=varNames.substring(0, varNames.length()-1)+"\n";

        RectangularDataSet dataSet= parser.parseTabular((varNames+string).toCharArray());
        dataSet.setName(tablename);

      //  System.out.println(dataSet);
        return (ColtDataSet) dataSet;
		
	}

	public ColtDataSet GetData(String tablename, List<Node> nodes)
			throws Exception {
		DataWrapper dw = new DataWrapper();
		ArrayList<String> result = database.describeTable(tablename);

		List<Node> resultToNodes = new ArrayList<Node>();
		ArrayList<String> nodesString = new ArrayList<String>();
		for (Node n : nodes) {
			nodesString.add(n.getName());
		}

		ArrayList<String> tempResult = (ArrayList<String>) result.clone();
		;
		for (String s : tempResult) {
			if (nodesString.contains(s)) {
				DiscreteVariable gn = new DiscreteVariable(s.toString());
				resultToNodes.add(gn);
			} else {
				result.remove(s);
			}
		}
//		 ColtDataSet dataset = new ColtDataSet(result.size(), resultToNodes);

//		int size = database.countStar(tablename);

//		ColtDataSet dataset = new ColtDataSet(size, resultToNodes);
		// int i = 0;
		// for (Object s : result) {
		DataParser parser = new DataParser();

        parser.setDelimiter(DelimiterType.COMMA);
        parser.setVarNamesSupplied(true);
        parser.setIdsSupplied(false);
//        parser.setMaxIntegralDiscrete(getMaxDiscrete());

		String string = database.getRowsInString(tablename, result);

        String varNames ="";
        for(String variableName:nodesString){
        	varNames+=variableName+",";
        }
        varNames=varNames.substring(0, varNames.length()-1)+"\n";

        RectangularDataSet dataSet= parser.parseTabular((varNames+string).toCharArray());
        
        System.out.println(dataSet);
        return (ColtDataSet) dataSet;
	}

	public static void main(String[] args) throws Exception {

		// // initialize logging to go to rolling log file
		// LogManager logManager = LogManager.getLogManager();
		// logManager.reset();
		//
		// // log file max size 10K, 3 rolling files, append-on-open
		// Handler fileHandler = new FileHandler("log", 10000, 3, true);
		// fileHandler.setFormatter(new SimpleFormatter());
		// Logger.getLogger("").addHandler(fileHandler);
		// 
		//
		//        
		// // preserve old stdout/stderr streams in case they might be useful
		// PrintStream stdout = System.out;
		// PrintStream stderr = System.err;
		//
		//                                     
		// Logger logger;
		// LoggingOutputStream los;
		//
		// logger = Logger.getLogger("stdout");
		// los = new LoggingOutputStream(logger, StdOutErrLevel.STDOUT);
		// System.setOut(new PrintStream(los, true));
		//
		// logger = Logger.getLogger("stderr");
		// los= new LoggingOutputStream(logger, StdOutErrLevel.STDERR);
		// System.setErr(new PrintStream(los, true));
		//        
		//        
		// S_learning s = new S_learning(2);
		//        
		GetDataset a = new GetDataset();

	//	 a.GetData("courseregistrationstudent");
	//	 a.GetData("MovieLens.u2base");
		 System.out.println("222");

		 a.getData("MovieLens.Useritem2u2base");
		 System.out.println("222");
	//	 a.GetData("student");
	//	 a.GetData("RA");
	//	 a.GetData("registration");
	//	a.GetData("RAprofstudent");
	//	 a.GetData("prof");
	}

}
