package ca.sfu.jbn.analyzeMLN;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class analyze {

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

	public void eliminateZero(String inputFile,String outputFile){
		File file = new File(inputFile);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);
		
		bis = new BufferedInputStream(fis);
		dis = new DataInputStream(bis);
		String read = "";
		StringBuffer output = new StringBuffer();
		while (dis.available() != 0) {
			read = dis.readLine();
			if ((read.length()>2)&&checkSecond(read)){
				int index = read.indexOf("\t");
				if (index<0) index = read.indexOf(" ");
				double weight = read.contains(" v ")? Double.parseDouble(read.substring(0, index))* -1 : Double.parseDouble(read.substring(0, index)) ;
				if (weight==0) continue;
			}
			output.append(read+"\n");
		}
		FileOutputStream outputStream= new  FileOutputStream(outputFile,true);
		outputStream.write(output.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void process(String input,String output){
		eliminateZero(input, output);
		MakeModel m = new MakeModel();
		m.readFile(input);
		m.report();
		System.out.println("finished");
	}
	
	public void process(String input){
		eliminateZero(input, "temp");
		MakeModel m = new MakeModel();
		m.readFile(input);
		m.report();
		System.out.println("finished");
	}
	
	public static void main(String args[]){
		if(args.length < 2){
			System.err.println("correct argument: <input mln> <output mln>");
			System.exit(0);
		}
		analyze a = new analyze();
		a.eliminateZero(args[0], args[1]);
		MakeModel m = new MakeModel();
		m.readFile(args[0]);
		m.report();
		System.out.println("finished");
	}
}


