package bif;

import nu.xom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BIF_IO {

	/*
	 * Get the list of links from a given file
	 */
	public static ArrayList<String[]> getLinksFromFileOld(String filePath) throws IOException {
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
				if (givens.size() > 0) {
					for (String given : givens) {
						String[] val = {given, forNode};
						toReturn.add(val);
					}
				}
				else {
					String[] val = {"", forNode};
					toReturn.add(val);
				}
			}
		}
		in.close();
		file.close();
		return toReturn;		
	}
	
	public static ArrayList<String[]> getLinksFromFile(String filePath) throws ValidityException, ParsingException, IOException {
		Builder xmlParser = new Builder();
		//Document doc = xmlParser.build(filePath);
        Document doc = xmlParser.build(new File(filePath));
		Element root = doc.getRootElement();
		root = root.getFirstChildElement("NETWORK");
		
		//first, get a list of all variables
		ArrayList<String> variables = new ArrayList<String>();
		Elements varElements = root.getChildElements("VARIABLE");
		for (int i = 0; i < varElements.size(); i++) {
			Element curVar = varElements.get(i);
			variables.add(curVar.getChildElements("NAME").get(0).getValue());
		}

		//next, get a list of links
		ArrayList<String[]> links = new ArrayList<String[]>();
		Elements defElements = root.getChildElements("DEFINITION");
		for (int i = 0; i < defElements.size(); i++) {
			//first, get all the given values from this definition
			ArrayList<String> givenVals = new ArrayList<String>();
			Elements givenElements = defElements.get(i).getChildElements("GIVEN");
			//System.out.print("\n given\n");
			for (int j = 0; j < givenElements.size(); j++) {
				givenVals.add(givenElements.get(j).getValue());
			//	System.out.print(givenElements.get(j).getValue());//testing
			}
			
			//next, get the for value from this definition
			Elements forElements = defElements.get(i).getChildElements("FOR");
			String forVal = forElements.get(0).getValue();
			//System.out.print("\n for "+ forElements.get(0).getValue() +"\n"); //testing
			//if the file contains for's with no given's, then record the for as a child with no parents
			if (givenVals.size() == 0) {
				String[] edge = {"", forVal};
				links.add(edge);
			}
			else {
				//for each given value, add an edge from it to the for value
				for (String givenVal : givenVals) {
					String[] edge = {givenVal, forVal};
					links.add(edge);
					
				}
			}
			
			//finally, remove the forVal from the list of variables
			if (variables.contains(forVal)) {
				variables.remove(forVal);
			}
			
		}
		
		//for each remaining variable which was never used as a for value, we know that it is never a child
		//so add it as a child with no parents
		for (String var : variables) {
			String[] edge = {"", var};
			links.add(edge);
		}
		
		return links;
	}
	
	/*
	 * Retrieve the name from a .BIF file
	 */
	public static String getNameFromFile(String filePath) throws IOException, ValidityException, ParsingException {
		Builder xmlParser = new Builder();
		Document doc = xmlParser.build(filePath);
		Element root = doc.getRootElement();
		root = root.getFirstChildElement("NETWORK");
		Elements nameElement = root.getChildElements("NAME");
		String name = nameElement.get(0).getValue();
		return name;
	}
	
	public static String writeBifHeader() {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\"?>\n");
		builder.append("<!-- DTD for the XMLBIF 0.3 format -->\n");
		builder.append("<!DOCTYPE BIF [\n");
		builder.append("\t<!ELEMENT BIF ( NETWORK )*>\n");
		builder.append("\t\t<!ATTLIST BIF VERSION CDATA #REQUIRED>\n");
		builder.append("\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n");
		builder.append("\t<!ELEMENT NAME (#PCDATA)>\n");
		builder.append("\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n");
		builder.append("\t\t<!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n");
		builder.append("\t<!ELEMENT OUTCOME (#PCDATA)>\n");
		builder.append("\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n");
		builder.append("\t<!ELEMENT FOR (#PCDATA)>\n");
		builder.append("\t<!ELEMENT GIVEN (#PCDATA)>\n");
		builder.append("\t<!ELEMENT TABLE (#PCDATA)>\n");
		builder.append("\t<!ELEMENT PROPERTY (#PCDATA)>\n");
		builder.append("]>\n\n");
		return builder.toString();
	}
	
	public static String writeNetworkBegin(String name) {
		return "<BIF VERSION=\"0.3\">\n<NETWORK>\n<NAME>" + name + "</NAME>\n";
	}
	
	public static String writeNetworkEnd() {
		return "</NETWORK>\n</BIF>\n";
	}
	
	public static String writeVariable(String variable, ArrayList<String> outcomes) {
		StringBuilder builder = new StringBuilder("<VARIABLE TYPE=\"nature\">\n");
		builder.append("\t<NAME>");
		builder.append(variable);
		builder.append("</NAME>\n");
		for (String outcome : outcomes) {
			builder.append("\t<OUTCOME>");
			builder.append(outcome);
			builder.append("</OUTCOME>\n");
		}
		builder.append("</VARIABLE>\n");
		return builder.toString();
	}
	
	public static String writeDefinition(String forVariable, ArrayList<String> givenVariables) {
		StringBuilder builder = new StringBuilder("<DEFINITION>\n");
		builder.append("\t<FOR>");
		builder.append(forVariable); 
		builder.append("</FOR>\n");
		for (String given : givenVariables) {
			builder.append("\t<GIVEN>");
			builder.append(given); 
			builder.append("</GIVEN>\n");
		}
		builder.append("</DEFINITION>\n");
		return builder.toString();
	}
	
}
