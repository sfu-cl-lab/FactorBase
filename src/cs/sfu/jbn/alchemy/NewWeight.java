/**********************************************************************
 * Use newweight class to generate new weight mln file for the dataset
 * 
 * Please add the three functions to the ExportToMLN.java file
 * according to the instruction given before the functions
 *  
 * Put this file into cs.sfu.jbn.alchemy 
 *  
*********************************************************************/
package cs.sfu.jbn.alchemy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import ca.sfu.jbn.common.global;

public class NewWeight {
	private int nodeCount[] = new int[10000];
	int maxcount=0;
	int totalOutputs = 2;
	StringBuffer[] outputs = new StringBuffer[10];
	
	public NewWeight(){
		
		
	}
	
	/**********************************************************************
	 * Use newweight class to generate new weight mln file for the dataset
	 * The initial() function will initialize the file for preparation 
	 *  
	 * Please add this function to the beginning of the ExportToMLN files export()
	 * function after the following sentence:  
	 * int nodeNum = FinalIm.getNumNodes();
	 *  
	 *********************************************************************/
	public void initial(int nodeNum){
		//Elwin add the following code, for the counting tuples of Node i in the mintable:
		

		for (int i=0;i<totalOutputs;i++){
			outputs[i]=new StringBuffer();
		}
		//StringBuffer output3 = new StringBuffer();
		
		InputStream file;
		String filename = global.WorkingDirectory + "/" + global.schema
		+ "_clean_count"+".txt";
		try {
		file = new FileInputStream(filename);
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String temps = "";

		for (int i =0; i<nodeNum; i++){
			   temps = in.readLine();
			   nodeCount[i] = Integer.parseInt(temps);
			 if (nodeCount[i]>maxcount) maxcount=nodeCount[i];
			 
		}
		//End of adding code.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**********************************************************************
	 * Use newweight class to generate new weight mln file for the dataset
	 * The computeWeights() function will compute all kinds of new weights
	 * base on the given prob and weight
	 *  
	 *  Please add this function in ExportToMLN.java line 215
	 *********************************************************************/
	public void computeWeights( NumberFormat formatter,Double weight,Double prob,int i,int numValues,String sentence){
		//Added code
		
	
		String[] ss = new String[10];
		for(int ii=0;ii<totalOutputs;ii++){
			ss[ii]="";
		}
		ss[0] = formatter.format(weight/nodeCount[i]+1.0);
		
		ss[1] = formatter.format(weight*maxcount/nodeCount[i]);
		
		if ((prob!=0)&&(!prob.isNaN())) weight = Math.log(prob/nodeCount[i]);
		//if (flag) weight = Math.log(prob/nodeCount[i]);
		
		ss[2] = formatter.format(weight);
		
		
		weight = (prob - 1.0 / numValues)*numValues;
		ss[3] =  formatter.format(weight);
		
		Double tempprob =  (prob + 0.001)*1000/(1000+numValues);
		weight = tempprob;
		ss[4] =  formatter.format(weight);
		
		
		tempprob =  (prob + 0.001)*1000/(1000+numValues);
		weight = Math.log(tempprob);
		ss[5] =  formatter.format(weight);
		
		for(int ii=0;ii<totalOutputs;ii++){
			outputs[ii].append(ss[ii]+"\t");
		}
		//End add
		for(int ii=0;ii<totalOutputs;ii++){
			outputs[ii].append(sentence+"\n");
		}
	
	}
	/**********************************************************************
	 * Use newweight class to generate new weight mln file for the dataset
	 * The outputeWeight() function will output the final results in files
	 * 
	 * Please add this function to the ExportToMLN.java at the end of the 
	 *  export() function
	 *********************************************************************/
	public void outputWeight(String type){
		//Elwin Add
		try {
		FileOutputStream[] outputStreams = new FileOutputStream[10];
		for(int ii=0;ii<totalOutputs;ii++){
		
			outputStreams[ii] = new  FileOutputStream(global.WorkingDirectory + "/" + global.schema
					+"_"+type+ "_VJ_Cnt"+ii+".mln",false);
		} 
		
		for (int ii=0;ii<totalOutputs;ii++){
			outputStreams[ii].write(outputs[ii].toString().getBytes());
			outputStreams[ii].close();
		}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("MLN with modified new0 Olweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_"+type+"_VJ_Cnt0.mln");
		System.out.println("MLN with modified new1 Elweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_"+type+"_VJ_Cnt1.mln");
		//System.out.println("MLN with modified new2 Miweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_VJ_Cnt2.mln");
		//System.out.println("MLN with modified new3 Miweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_VJ_Cnt3.mln");
		//System.out.println("MLN with modified new4 Miweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_VJ_Cnt4.mln");
		//System.out.println("MLN with modified new5 Miweight (by Elwin&Mi) generated: " + global.WorkingDirectory + "/" + global.schema+ "_VJ_Cnt5.mln");
		
		//End Add
		
	}
	
}
