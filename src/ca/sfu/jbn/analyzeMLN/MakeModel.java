package ca.sfu.jbn.analyzeMLN;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MakeModel {
	public HashMap<String, Predicate> pred = new HashMap<String, Predicate>();
	public int numOfRules = 0;
	public double totalWeight = 0;
	public double totalAbsWeight = 0;
	public double totalPredicates = 0;
	public double totalVars = 0;
	public ArrayList<String> vars = new ArrayList<String>();

	public HashMap<String, Predicate> readFile(String inputFile) {
		boolean secondPart = false;
		File file = new File(inputFile);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			String read = "";

			while (dis.available() != 0) {
				//first:{
				read = dis.readLine();
				if(read.startsWith("//")||read.startsWith("\n"))
					continue;
				if (!secondPart && checkSecond(read)){
					secondPart = true;
					//continue; 
				}
				if(!secondPart && (read.length()>1)){
					String name = read.substring(0, read.indexOf('('));
					Predicate temp = new Predicate(name);
					String t1 = read.substring(read.indexOf('(') + 1, read.indexOf(')'));
					String[] args = t1.split(",");
					int num = name.startsWith("B_")? 2: args.length - 1;
					temp.setArgu(num);
					for(int i = 0; i < num ; i++){
						temp.arguments[i] = args[i].substring(0, args[i].indexOf("_type"));
					}	
					//System.out.println(name);
					pred.put(name, temp);
				}
				if(secondPart && (read.length()>1)){
					//System.out.println("*"+read);

					int index = read.indexOf("\t");
					if (index<0) index = read.indexOf(" ");
					double weight = read.contains(" v ")? Double.parseDouble(read.substring(0, index))* -1 : Double.parseDouble(read.substring(0, index)) ;

					//TODO Get all the values
					if (weight == 0) continue;
					Rule temp = new Rule(weight);
					read = read.substring(read.indexOf(' ')).trim();
					String[] res;
					if(read.contains(" v "))
						res = read.split(" v ");
					else{
						read = read.replace('^', '%');
						res = read.split(" % ");
					}
					//if(res.length > 1)
					//	System.out.println("here you go");
					temp.setElements(res, read.contains(" v "));

					if (weight!=0) {
						numOfRules++;
						//System.out.println("add weight" + weight);
						totalWeight+=weight;
						totalAbsWeight+=Math.abs(weight);
						totalPredicates += res.length;
					}

					//search all the elements in rules res[]
					vars.clear();
					for(String i:res)
					{
						String varpart = i.substring(i.indexOf('(')+1,i.indexOf(')'));
						String temps[] = varpart.split(",");
						for (String j: temps){
							if(Character.isLowerCase(j.charAt(0)))
								if(!vars.contains(j))
									vars.add(j);
						}
					}
					totalVars += vars.size();
					// Count the weights v.s. variable numbers
//					System.out.println(vars.size() + "\t" + Math.abs(weight));
				}
			}
			fis.close();
			bis.close();
			dis.close();
			//}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pred;
	}

	public boolean checkSecond(String read){
		//System.out.println(read);
		if (read.startsWith("-")) return true;
		else if (read.length()>2){
			String s = read.substring(0,1);
			if ("0123456789".contains(s)) return true;
			//System.out.println(s);
		}
		return false;
	}

	public void report(){
		System.out.println("numOfRules="+numOfRules);
		System.out.println("avgWeight="+(totalWeight/numOfRules));
		System.out.println("avgABSWeight="+(totalAbsWeight/numOfRules));
		System.out.println("avgPredicates="+(totalPredicates/numOfRules));
		System.out.println("avgVars="+(totalVars/numOfRules));

		StringBuffer output = new StringBuffer();
		output.append("numOfRules="+numOfRules+"\n");
		output.append("avgWeight="+(totalWeight/numOfRules)+"\n");
		output.append("avgABSWeight="+(totalAbsWeight/numOfRules)+"\n");
		output.append("avgPredicates="+(totalPredicates/numOfRules)+"\n");
		output.append("avgVars="+(totalVars/numOfRules)+"\n");

		FileOutputStream outputStream;
		try {
			outputStream = new  FileOutputStream("result.txt",false);

			outputStream.write(output.toString().getBytes());
			outputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void print(){
		java.util.Iterator<String> ir = pred.keySet().iterator();
		while(ir.hasNext()){
			String r = ir.next().toString();
			//System.out.println("Predict = " + r);
			pred.get(r).print();
		}
	}

	public static void main(String[] args) {
		MakeModel m = new MakeModel();
		m.readFile("uw_Training1_VJ_mbn.mln");
		m.report();
		System.out.println("finished");
	}
}
