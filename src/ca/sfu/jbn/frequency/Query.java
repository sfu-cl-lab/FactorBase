package ca.sfu.jbn.frequency;
import java.util.ArrayList;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.Evidence;
public class Query {
	
	public String queryName;
	public int queryValNum;
	public ArrayList<String> queryValue = new ArrayList<String>();
	
	public int evidenceNum;
	public ArrayList<String> evidenceName = new ArrayList<String>();;
	public ArrayList<String> evidenceValue = new ArrayList<String>();;
		
	public String getString(int x){
		String sentence = "P(";
		sentence += queryName+"="+queryValue.get(x)+"|";
		for(int i = 0 ; i < evidenceNum;i++) sentence+=evidenceName.get(i)+"="+evidenceValue.get(i)+",";
		sentence=sentence.substring(0,sentence.length()-1);
		sentence+=")";
		return sentence;
	}

	public void printAll(){
		for(int i = 0; i < queryValNum; i++) System.out.println(this.getString(i));
	}
	
	public void print(){
		System.out.println("Query: "+queryName);
		System.out.println("Evidence: "+evidenceName);
		System.out.println("Evidence: "+evidenceValue);
	}
	
	public Evidence getEvidence(BayesIm im){
		Evidence e = Evidence.tautology(im);
		
		for(int i = 0; i < evidenceName.size(); i++){
			int name, value;
			name = e.proposition.getNodeIndex(evidenceName.get(i));
			value = e.proposition.getCategoryIndex(evidenceName.get(i), evidenceValue.get(i));
			e.proposition.setCategory(name, value);
		}
		
		return e;
	}
	
	public void addE(String name, String val){
		evidenceName.add(name);
		evidenceValue.add(val);
	}
}
