package ca.sfu.jbn.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;





import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

public class CopyOfJoinBayesNetEstimatorWrapper extends BayesEstimatorWrapper{
	static final long serialVersionUID = 23L;

	/**
	 * @serial Can be null.
	 */
	//  private String name;

	/**
	 * @throws Exception 
	 * @serial Cannot be null.
	 */

	//private BayesIm bayesIm;

	//============================CONSTRUCTORS============================//


	public void Preparing(){
		global.initialize();
		Parser.initialize();
		
		System.out.println("Starting MLN parameter learning package on "
				+ global.schema);
		ReadXML sqlToXMLReader = new ReadXML();
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
					+ "_VJ_.mln"));
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
	}

	public void Processing(){
		System.out.println("Stucture learning begins");
		// it constructs the Structure Learning phase.
		long l= System.currentTimeMillis();
		// MLN_ParameterLearning param = MLN_ParameterLearning();
		S_learning sLearn = new S_learning(2);
		BayesPm bayes = sLearn.major();
		long l2= System.currentTimeMillis();
		System.out.println("Running time of Structure Learning(ms):    ");
		System.out.println( l2 - l);
		// start Paramter Learning
		long l3 = System.currentTimeMillis();
		System.out.println("Parameter learning using Virtual joins begins");
		ParamTet t = new ParamTet(bayes);	
		try {
			bayesIm =  t.paramterlearning();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long l4 = System.currentTimeMillis();
		System.out
		.print("Parameter learning using Virtual joins ends. Running time of learning(ms):  ");
		System.out.println(l4 - l3);

		db.closeDB();

	}




	public CopyOfJoinBayesNetEstimatorWrapper(dbDataWrapper dataWrapper) throws Exception {
		super(dataWrapper);
		Preparing();
		Processing();



		//    	RectangularDataSet dataSet =
		//                (RectangularDataSet) dataWrapper.getSelectedDataModel();
		//
		//        BayesPm bayesPm = new BayesPm(bayesPmWrapper.getBayesPm());
		////        ParamTet param = new ParamTet(dataSet,bayesPm);
		//        BayesIm estimatedIm = new MlBayesIm(bayesPm);
		////        estimatedIm = param.paramterlearning(bayesPm);
		//        
		//        
		//        
		//       // bayesPm =  bayesPmWrapper.getBayesPm();
		//        S_learning sLearn = new S_learning(2);
		//                             // BayesPm bayesPm = sLearn.major();
		//        //CPTInitializer cpt = new CPTInitializer(bayesPm);
		//        CPTInitializer cpt = new CPTInitializer();
		//    
		//       
		//       //	jointProbLearner cpt = new jointProbLearner(bayesPm);
		//        try {
		//        	
		//          //  dirichletBayesIm = cpt.computeCPT();
		//       	dirichletBayesIm = cpt.computeCPT();
		//            dirichletBayesIm = cpt.copytoNewPM();
		//            this.dirichletBayesIm = super.getEstimatedBayesIm();
		//          
		//        }
		//        catch (IllegalArgumentException e) {
		//            throw new RuntimeException(
		//                    "Please fully specify the Dirichlet prior first.");
		//        }
		//        log(dirichletBayesIm);              
		//    
	}
	private void log(DirichletBayesIm im){
		TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());
		if(config != null){
			TetradLogger.getInstance().setTetradLoggerConfig(config);
			TetradLogger.getInstance().info("Estimated Dirichlet Bayes IM:");
			TetradLogger.getInstance().log("im", "" + im);
			TetradLogger.getInstance().reset();
		}
	}

	/**`
	 * Generates a simple exemplar of this class to test serialization.
	 *
	 * @see edu.cmu.TestSerialization
	 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
	 */







}

