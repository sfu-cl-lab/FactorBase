package ca.sfu.jbn.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.parameterLearning.ParamTet;
import ca.sfu.jbn.structureLearning.S_learning;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;

public class JoinBayesNetEstimatorWrapper extends edu.cmu.tetradapp.model.BayesEstimatorWrapper{
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


		System.out.println("Starting MLN parameter learning package on "
				+ global.schema);
		ReadXML sqlToXMLReader = new CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		Parser.initialize();
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






	public JoinBayesNetEstimatorWrapper(dbDataWrapper dbdataWrapper) throws Exception {
		super();
		Preparing();
		Processing();

		//takeStars(bayesIm);		

		//updateGraph(bayesIm.getDag());
		updateGraph(bayesIm);
	}


	private void takeStars(BayesIm FinalIm){


		//		int nodeNum = FinalIm.getNumNodes();
		//		
		//							
		//		// for each node
		//		for (int i = 0; i < nodeNum; i++) {
		//					
		//			// each row in the table contains a number of entries
		//			int rowNum = FinalIm.getNumRows(i);
		//			int colNum = FinalIm.getNumColumns(i);
		//			for (int j = 0; j < rowNum; j++) {
		//				int numValues=colNum;
		//				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals("*")) numValues--;
		//				for (int k = 0; k < colNum; k++) {
		//				
		//					Double prob = FinalIm.getProbability(i, j, k);
		//					Node node = FinalIm.getNode(i);
		//					String nodeValue = FinalIm.getBayesPm().getCategory(
		//								node, k);
		//					
		//					System.out.println(node.getName());
		//					
		//					if (node.getName().startsWith("B(")){
		//						if (nodeValue.equals("*")){
		//							FinalIm.setProbability(i, j, k, 0);
		//						} 
		//					}
		//					else {
		//						boolean flag = true;
		//						
		//						int[] parents = FinalIm.getParents(i);
		//						int[] parentValues = FinalIm.getParentValues(i, j);
		//
		//						for (int parentIndex = 0; parentIndex < parents.length; parentIndex++) {
		//							String parentValue = FinalIm
		//									.getBayesPm()
		//									.getCategory(
		//											FinalIm
		//													.getNode(parents[parentIndex]),
		//											parentValues[parentIndex]);
		//							if (parentValue.equals("false")) {
		//								
		//						
		//						if (nodeValue.equals("*")){
		//							FinalIm.setProbability(i, j, k, 0);
		//						} 
		//					}
		//
		//					
		//				}
		//			



	}

	private void updateGraph(BayesIm bayes){
		int N = bayes.getNumNodes();
		//for(Node x : bayes.getNodes()){
		for(int i = 0;i<N;i++){
			Node x = bayes.getNode(i);
			//			
			//			int colNum = bayesIm.getBayesPm().getNumCategories(x);
			//			if (bayesIm.getBayesPm().getCategory(x, colNum-1).equals("*")){
			//				bayesIm.
			//				getBayesPm().setNumCategories(x, colNum-1);
			//			}


			//for(int i =0;i<bayesIm.getBayesPm().getNumCategories(x);i++){

			//System.out.println("Node "+x);
			//System.out.println(bayesIm.getBayesPm().getCategory(x,i));
			//}

			//System.out.println("Node is "+x.getName());
			int flag =0;



			for(Object t : Parser.getInstance().getRel_att()){
				//System.out.println("t is "+t.toString());
				if (t.toString().contains(x.getName())) flag=1;
			}

			for(Object t : Parser.getInstance().getEntity_att()){
				//System.out.println("t is "+t.toString());
				if (t.toString().contains(x.getName())) flag=2;
			}

			if (x.getName().contains("B(")){
				flag=3;
			}

			if (flag==1){
				String tableName = Parser.getInstance().getTableOfField(x.getName());
				x.setName(x.getName()+"(");
				for(Object s : Parser.getInstance().getRefEntities(tableName)){
					//System.out.println("S is "+s.toString());
					x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");
				}
				x.setName(x.getName().substring(0,x.getName().length()-1)+")");
			}
			else if(flag==2){
				String tableName = Parser.getInstance().getTableOfField(x.getName());
				x.setName(x.getName()+"(");
				for(Object s : Parser.getInstance().getEntityPrimaryKey(tableName)){
					//System.out.println("S is "+s.toString());
					x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");

				}
				x.setName(x.getName().substring(0,x.getName().length()-1)+")");
			}
			else if(flag==3){
				String st = x.getName();
				String tableName = st.substring(st.indexOf("(")+1,st.indexOf(")"));
				x.setName("B_"+tableName);
				//System.out.println("relation table name:"+tableName);
				x.setName(x.getName()+"(");

				for(Object s : Parser.getInstance().getRefEntities(tableName)){

					//System.out.println("S is "+s.toString());
					if (s.toString().contains("_dummy")){
						x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+ ca.sfu.jbn.common.global.dummyReplacer +",");
					}
					else{
						x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");
					}

				}

				//elwin added for the relation attributes. But not solveing the essence
				//				for(Object s : Parser.getInstance().getRel_att(tableName)){
				//					for(Node tempNode : dag.getNodes()){
				//						if (tempNode.getName().equals(s.toString())){
				//							if (!dag.isDirectedFromTo(x, tempNode))
				//								dag.addDirectedEdge(x,tempNode);
				//						}
				//					}
				//				}

				x.setName(x.getName().substring(0,x.getName().length()-1)+")");

			}

			/* Remove "dummy" from the GUI */ 
			if(x.getName().contains("_dummy")){
				//System.out.println("Find Dummy!");
				String tempName = x.getName().replace("_dummy", "");
				tempName = tempName.substring(0,tempName.length()-1)+ ca.sfu.jbn.common.global.dummyReplacer + ")";
				x.setName(tempName);
			}

		}

	}
	/**`
	 * Generates a simple exemplar of this class to test serialization.
	 *
	 * @see edu.cmu.TestSerialization
	 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
	 */







}

