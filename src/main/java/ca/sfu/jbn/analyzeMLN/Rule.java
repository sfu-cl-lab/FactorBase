package ca.sfu.jbn.analyzeMLN;


import java.util.ArrayList;

public class Rule {
	public Rule(double weight) {
		this.weight = weight;
	}
	public void setElements(String[] inp, boolean negate){
		for(String i : inp){
			if(negate && i.contains("!"))
				elements.add(i.substring(i.indexOf("!")+1));
			else if(negate && !i.contains("!"))
				elements.add("!"+i);
			else
				elements.add(i);
		}
	}
	public double weight;
	public ArrayList<String> elements = new ArrayList<String>();
	
	public void print(){
		System.out.print(weight+" ");
		for (String s : elements){
			System.out.print(s+",");
		}
		System.out.println();
		System.out.println("Element# = " + elements.size());
	}
	
}
