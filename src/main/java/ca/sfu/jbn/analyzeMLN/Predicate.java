package ca.sfu.jbn.analyzeMLN;


import java.util.ArrayList;

public class Predicate {
	public Predicate(String name) {
		Name = name;
	}
	
	public Predicate() {
		
	}
	public void setArgu(int num) {
		numOfArguments = num;
		arguments = new String[numOfArguments];
	}
	public String Name;
	public int numOfArguments;
	public String[] arguments;
	public ArrayList<String> values = new ArrayList<String>();
	public ArrayList<Rule> rules= new ArrayList<Rule>();
	public void print(){
		System.out.println("Name=" + Name + "; Arguments = ");
		for (int i = 1; i<=numOfArguments; i++){
			System.out.print(arguments[i-1] + " ");
		}
		System.out.println();
		System.out.print("rules:");
		for (Rule r : rules){
			r.print();
		}
		System.out.print("Values:");
		for (String s : values){
			System.out.print(s+",");
		}
		System.out.println();
	}

}
