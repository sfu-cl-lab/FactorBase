package cs.sfu.jbn.alchemy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.jbn.common.global;

import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Node;

public class CopyOfExportToMLN {
	//withFalse is the switch whether to keep the false statement
	Boolean withFalse = false;// false is always removed
	Boolean takeLog = false;
	Boolean takeLog1 = false;
	Boolean structureOnly = true;
	Boolean logistic = false;
	Boolean linearShift = false;
	MlBayesIm FinalIm = null;
	// BayesPm bayesPm;
	List<String> predicates = new ArrayList<String>();

	public void readPredicates(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s = in.readLine();
			while (s != null) {
				if (s.startsWith("//")) {
					continue;
				}
				predicates.add(s);
				s = in.readLine();

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String filename) throws IOException,
			ClassNotFoundException {
		InputStream file;
		try {
			file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput object = new ObjectInputStream(buffer);
			FinalIm = (MlBayesIm) object.readObject();
			// bayesPm = (BayesPm) object.readObject();
			// System.out.println(FinalIm);
			object.close();
			// System.out.println(FinalIm.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public StringBuffer export(String type) throws IOException {
		StringBuffer output = new StringBuffer();
		output.append("\n");
		initial(type);
		
		int nodeNum = FinalIm.getNumNodes();
		
		
		
		/**********************************************************************
		 * Use newweight class to generate new weight mln file for the dataset
		 * The initial() function will initialize the file for preparation 
		 *  
		 *  
		 *********************************************************************/
		//NewWeight newweight = new NewWeight();
		//newweight.initial(nodeNum);
		
		
		
		// for each node
		for (int i = 0; i < nodeNum; i++) {
			
		
			// each row in the table contains a number of entries
			int rowNum = FinalIm.getNumRows(i);
			int colNum = FinalIm.getNumColumns(i);
			for (int j = 0; j < rowNum; j++) {
				int numValues=colNum;
				if (FinalIm.getBayesPm().getCategory(FinalIm.getNode(i), colNum-1).equals("*")) numValues--;
				for (int k = 0; k < colNum; k++) {
					// first output the weight
					Double prob = FinalIm.getProbability(i, j, k);
					
					Boolean flag = true;
				
					if (takeLog) {
						
					}
					else if(structureOnly){
						flag=true;
					}
					else {
						if (!prob.isNaN()) {
							flag = true;
						}
					}
				
					if (flag) {
						boolean outPut = true;
						//test here
						double weight = prob;
						if (takeLog) {
							weight = Math.log(prob);
						}
						else if (takeLog1) {
							weight = Math.log(prob)+1.0;
						}
						else if (logistic) {
							weight = Math.log(prob / (1 - prob));
						} else if (linearShift) {
							weight = prob - 1.0 / numValues;
						}
						Node node = FinalIm.getNode(i);
						String nodeValue = FinalIm.getBayesPm().getCategory(
								node, k);
						String sentence = "";
						if (nodeValue.equals("false")) {
							sentence = "!" + findSentence(i);
						}
						else if(nodeValue.equals("*")) {
							outPut=false;
						}
						else  {
							sentence = findSentence(i);
						}
				
						int[] parents = FinalIm.getParents(i);
						int[] parentValues = FinalIm.getParentValues(i, j);

						for (int parentIndex = 0; parentIndex < parents.length; parentIndex++) {
							String parentValue = FinalIm
									.getBayesPm()
									.getCategory(
											FinalIm
													.getNode(parents[parentIndex]),
											parentValues[parentIndex]);
							if (parentValue.equals("false")) {
								if (withFalse) {
									outPut = true;
									
									// here is the adding of whether to do with !relationship
									
									
									sentence = sentence
											+ " ^ !"
											+ findSentence(parents[parentIndex]);
								} else {
									outPut = false;
								}
							} else if (parentValue.equals("*")) {
								outPut = false;
							} else {
								sentence = sentence + " ^ "
										+ findSentence(parents[parentIndex]);
							}
							// parentIndexList.add(parentIndex);
						}

						// System.out.println(i);
						sentence = sentence.replace(getNodeName(i) + "_inst",
								node.getName().toUpperCase() + "_" + nodeValue);
						for (int l = 0; l < parents.length; l++) {
							String parentValue = FinalIm.getNode(parents[l])
									.getName().toUpperCase()
									+ "_"
									+ FinalIm.getBayesPm().getCategory(
											FinalIm.getNode(parents[l]),
											parentValues[l]);
							sentence = sentence.replace(getNodeName(parents[l])
									+ "_inst", parentValue);
						}

						if (outPut) {
							if (!structureOnly) {
								NumberFormat formatter = new DecimalFormat(
										"0.0000000000");
								String s = formatter.format(weight);
								
				
								/**********************************************************************
								 * Use newweight class to generate new weight mln file for the dataset
								 * The computeWeights() function will compute all kinds of new weights
								 * base on the given prob and weight
								 *  
								 *********************************************************************/
								//newweight.computeWeights(formatter, weight, prob, i, numValues,sentence);
				
								 
								//if the modified weight is to be incorporated into the MLN export, change s below to s1 and
								//remove the bottom block for outputing file
								output.append(s + " ");
							}
							output.append(sentence + "\n");
				
						}
						outPut = true;
					}

				}

			}
		}
	//	System.out.println(output.toString());
		
		/**********************************************************************
		 * Use newweight class to generate new weight mln file for the dataset
		 * The outputeWeight() function will output the final results in files
		 *  
		 *********************************************************************/
		//newweight.outputWeight(type);
		
		return output;

	}

	private void initial(String type) {

		readPredicates(global.WorkingDirectory + "/" + global.schema
				+ "predicate_temp.mln");
		try {
			load(global.WorkingDirectory + "/" + global.schema + ".bin");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (type.equals("cpt")) {
			structureOnly = false;
		} else if (type.equals("log")) {
			structureOnly = false;
			takeLog = true;
		}else if (type.equals("log1")) {
				structureOnly = false;
				takeLog1 = true;
		}  
		else if (type.equals("mbn")) {
			structureOnly = true;
		}

		else if (type.equals("logistic")) {
			structureOnly = false;
			logistic = true;
		} else if (type.equals("ls")) {
			structureOnly = false;
			linearShift = true;
		}

		else {
			System.out.println("argument: dataset name <method>");
			System.out.println("cpt: natural cpt");
			System.out.println("log: log of cpt");
			System.out.println("log1: log of cpt plus 1");
			System.out.println("mbn: mbn");
			System.out.println("logistic: the log(prob/1-prob)");
			System.out.println("ls: linear shift");

			System.exit(1);
		}

	}

	// return the node name for the given node
	// do the following modifications to the normal name
	// B(X) changed to B_X
	public String getNodeName(int nodeIndex) {
		String name = FinalIm.getNode(nodeIndex).getName();
		if (name.startsWith("B(")) {
			name = name.replace("(", "_");
			name = name.replace(")", "");
		}
		return name;
	}

	public String findSentence(int nodeIndex) {
		String sentence = null;
		String name = getNodeName(nodeIndex);
		name = name.charAt(0)
				+ name.subSequence(1, name.length()).toString();
		for (String s : predicates) {
			if (s.contains(name + "(")) {
				return s;
			}
		}
		return sentence;

	}

	public static void main(String[] args) {
		CopyOfExportToMLN export = new CopyOfExportToMLN();
		if (args.length == 1) {
			global.schema = args[0];
		} else if (args.length == 2) {
			global.schema = args[0];
			if (args[1].equals("cpt")) {
				export.structureOnly = false;
			} else if (args[1].equals("log")) {
				export.structureOnly = false;
				export.takeLog = true;
			} else if (args[1].equals("mbn")) {
				export.structureOnly = true;
			}

			else if (args[1].equals("logistic")) {
				export.structureOnly = false;
				export.logistic = true;
			} else if (args[1].equals("ls")) {
				export.structureOnly = false;
				export.linearShift = true;
			}
		} else {
			System.out.println("argument: dataset name <method>");
			System.out.println("cpt: natural cpt");
			System.out.println("log: log of cpt");
			System.out.println("mbn: mbn");
			System.out.println("logistic: the log(prob/1-prob)");
			System.out.println("ls: linear shift");

			System.exit(1);
		}

		try {
			export.readPredicates(global.schema + "predicate.mln");
			export.load(global.schema + ".bin");
			export.export("ls");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
