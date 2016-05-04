package ca.sfu.jbn.frequency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.frequency.BayesProbCounter;
import ca.sfu.jbn.frequency.BayesStat;
import ca.sfu.jbn.frequency.Query;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.graph.Node;

public class SubsampleLearner {

	int count[][][] = new int[50][5000][50];

	public void learn(String args[]) throws IOException{
		
		if (args.length<3){
			System.out.println("arguments: database name, percentage, subsample number");
			System.exit(0);
		}
		String original = args[0];
		String percentage = args[1];
		String sub = args[2];
		
//		String original = "fin";
//		String percentage = "50";
//		String sub = "1";


		
		String subsample = original+"_"+percentage+"_sub"+sub;
//		String subsample = original;

		global.schema = original;
		
		BayesIm finalIm = new BayesProbCounter().loadBayes();
		DecimalFormat decimal = new DecimalFormat("0.0000");

		global.schema = subsample;
		global.initialize();

		System.out.println("Learning CP-Tables for "+global.schema);
		ReadXML sqlToXMLReader = new  CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		
		for (int i = 0; i < finalIm.getNumNodes(); i++) 
		{
			int rowNum = finalIm.getNumRows(i);
			int colNum = finalIm.getNumColumns(i);
			for (int j = 0; j < rowNum; j++)
				for (int k = 0; k < colNum; k++)
					finalIm.setProbability(i, j, k, Double.NaN);
		}

		finalIm = fillAll_Static(finalIm);
		finalIm = fillNaN_Static(finalIm);

		File f = new File(original+"/" + subsample + ".txt");
		f.createNewFile();
		BufferedWriter output = new BufferedWriter(new FileWriter(f));

		File fcount = new File(original+"/" + subsample + "_count.txt");
		fcount.createNewFile();
		BufferedWriter outputcount = new BufferedWriter(new FileWriter(fcount));


		String data = "";
		String datacount = "";

		for (int i = 0; i < finalIm.getNumNodes(); i++) 
		{
			int rowNum = finalIm.getNumRows(i);
			int colNum = finalIm.getNumColumns(i);
			for (int j = 0; j < rowNum; j++) 
			{
				int idx;
				boolean flag = true;
				for (int k = 0; k < finalIm.getNumParents(i); k++)
				{
					idx = finalIm.getParentValue(i, j, k);
					String s = finalIm.getBayesPm().getCategory(finalIm.getNode(finalIm.getParent(i, k)), idx);
					if(s.equals(global.theChar)) flag = false;
				}
				if(!flag) continue;
				for (int k = 0; k < colNum; k++)
				{
					data = data + decimal.format(finalIm.getProbability(i, j, k)) + "\n";
					datacount = datacount + count[i][j][k]+"\n";
				}
			}
		}
		output.write(data);
		output.close();

		outputcount.write(datacount);
		outputcount.close();
		System.out.println("Done!");
	}


	public static void main(String[] args) throws IOException {
		new SubsampleLearner().learn(args);

	}

	/**
	 * Static fill all
	 */
	public BayesIm fillAll_Static(BayesIm FinalIm) throws IOException{
		int nodeNum = FinalIm.getNumNodes();
		Parser parser = new Parser();
		db database = db.getInstance();
		Parser.initialize();
		database.reconnect(global.schema);
		BayesStat stat = new BayesStat(FinalIm, parser, database);

		// for each node
		System.out.print("Total 14: ");
		for (int i = 0; i < nodeNum; i++) {
			System.out.print(i+"..");
			// each row in the table contains a number of entries
			Node node = FinalIm.getNode(i);
			String nodeName = node.getName(); 

			if (nodeName.startsWith("B(")) continue;

			int rowNum = FinalIm.getNumRows(i);
			int colNum = FinalIm.getNumColumns(i);

			for (int j = 0; j < rowNum; j++) {

				int numValues=colNum;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals(global.theChar)) numValues--;

				for (int k = 0; k < colNum; k++) {

					boolean flag = true;

					Query query = new Query();
					query.evidenceName = new ArrayList<String>();
					query.evidenceValue = new ArrayList<String>();

					query.queryName = nodeName;
					query.queryValNum = numValues;
					query.queryValue = new ArrayList<String>();

					String nodeValue = FinalIm.getBayesPm().getCategory(
							node, k);
					query.queryValue.add(nodeValue);

					int[] parents = FinalIm.getParents(i);
					int[] parentValues = FinalIm.getParentValues(i, j);

					query.evidenceNum = FinalIm.getNumParents(i);

					for (int parentIndex = 0; parentIndex < parents.length; parentIndex++) {
						String parentValue = FinalIm
						.getBayesPm()
						.getCategory(
								FinalIm
								.getNode(parents[parentIndex]),
								parentValues[parentIndex]);

						// for each parentIndex
						int pIndex = parents[parentIndex];

						String parentName = FinalIm.getNode(pIndex).getName();

						if (parentName.startsWith("B(")){
							parentName=parentName.substring(2, parentName.length()-1);
						}


						if (parentValue.equals("*")){
							flag = false;
							break;
						}

						if (!parentValue.equals(global.theChar))
						{
							query.evidenceName.add(parentName);
							query.evidenceValue.add(parentValue);
						}					
					}
					if (!flag) continue;		
					double prob = stat.getRowProbabilities(query).get(0);
					FinalIm.setProbability(i, j, k, prob);
					//System.out.println(i+","+j+","+k+" "+"("+nodeNum+","+rowNum+","+colNum+")  "+query.getString(0));
					
					
					count[i][j][k] = stat.getBigJoinCount(query.evidenceName, query.evidenceValue);



				}
			}
		}
		return FinalIm;
	}

	public BayesIm fillNaN_Static(BayesIm FinalIm){
		boolean changeZero = false; 
		double zeroFillNum = 0.01;
		//System.out.println(global.XMLFile);
		Parser parser = new Parser();
		int nodeNum = FinalIm.getNumNodes();

		// for each node
		for (int i = 0; i < nodeNum; i++) {


			Node node = FinalIm.getNode(i);
			boolean flag= false;
			boolean flag2=false;
			int parentindex = 0;
			int categoryNum = 0;
			if (FinalIm.getParents(i).length>0){
				String parentName = FinalIm.getNode(FinalIm.getParent(i, 0)).getName();

				//find table-relation attribute constraint
				if (parentName.startsWith("B(")){
					String sourceTable = parser.getTableofField(node);

					// flag means the child node is a relation attribute, and its parent is its mother table
					String thisname = parentName.substring(2,parentName.length()-1);
					if (thisname.equals(sourceTable)) flag = true;


					//System.out.println("1: "+node.getName()+" "+sourceTable+" "+parentName.toString());
				}

				//find same table-relation constraint
				for(int j=1;j<FinalIm.getNumParents(i);j++){
					Node node2 = FinalIm.getNode(FinalIm.getParent(i, j));
					String sourceTable2 = parser.getTableofField(node2);
					String sourceTable = parser.getTableofField(node);
					if (sourceTable.equals(sourceTable2)){
						if (flag){

							categoryNum = FinalIm.getParentDim(i, j);
							//System.out.println("2: " + node2.getName()+" "+sourceTable+" "+sourceTable2+" "+categoryNum);
							flag2=true;
							parentindex=j;

						}
					}
				}
			}

			// each row in the table contains a number of entries
			int rowNum = FinalIm.getNumRows(i);
			int colNum = FinalIm.getNumColumns(i);
			for (int j = 0; j < rowNum; j++) {



				int numValues=colNum;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals("*")) numValues--;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals(""+global.theChar)) numValues--;
				for (int k = 0; k < colNum; k++) {

					if(flag2&&((j+1)%categoryNum==0)){//check the same table constraint

						FinalIm.setProbability(i, j, k, 0.000);
						if (k==colNum-1) FinalIm.setProbability(i, j, k, 1.000);
					}
					else{


						//if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), k).equals("*")) continue;

						// first output the weight
						Double prob = FinalIm.getProbability(i, j, k);
						//if (prob.isNaN()) System.out.println(prob);

						//prob = (prob + 0.001)*1000/(1000+numValues);

						if (prob.isNaN()) prob = 1.0/numValues;

						if (k>=numValues) prob=0.0;

						FinalIm.setProbability(i, j, k, prob);
						if (flag){
							if (j>=rowNum*2/3){
								FinalIm.setProbability(i, j, k, 0.000);
								if (k==colNum-1){
									FinalIm.setProbability(i, j, k, 1.000);
								}
							}
						}//endif
					}

					if (changeZero) {

						if (FinalIm.getProbability(i,j, k)==0)
							FinalIm.setProbability(i, j, k, zeroFillNum);
					}
				}//endcolum for (most inside)

			}//end row for
		}//end node for
		return FinalIm;
	}
}

class CT implements Comparator<String>{

	@Override
	public int compare(String arg0, String arg1) {
		int l1 = arg0.length();
		int l2 = arg1.length();
		if (l1<l2) return -1;
		if (l1>l2) return 1;

		// TODO Auto-generated method stub
		return 0;
	}
}