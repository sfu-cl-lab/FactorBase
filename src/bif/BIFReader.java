package bif;

import java.util.ArrayList;
import java.io.*;

public class BIFReader {

	public static ArrayList<String[]> getLinks(String filePath) throws IOException {
		FileReader file = new FileReader(filePath);
		BufferedReader in = new BufferedReader(file);
		String curLine = "";
		Boolean inDef = false;
		ArrayList<String> givens = new ArrayList<String>();
		String forNode = "";
		ArrayList<String[]> toReturn = new ArrayList<String[]>();

		while((curLine = in.readLine()) != null){
			if (inDef) {
				if (curLine.contains("<FOR>")) {
					curLine = curLine.trim();
					forNode = curLine.substring(5, curLine.length() - 6);
				}
				if (curLine.contains("<GIVEN>")) {
					curLine = curLine.trim();
					givens.add(curLine.substring(7, curLine.length() - 8));
				}
			}
			if (curLine.equals("<DEFINITION>")) {
				inDef = true;
				givens = new ArrayList<String>();
				forNode = "";
			}
			if (curLine.equals("</DEFINITION>")) {
				inDef = false;
				for (String given : givens) {
					String[] val = {given, forNode};
					toReturn.add(val);
				}
			}
		}
		in.close();
		file.close();
		return toReturn;		
	}
	
}
