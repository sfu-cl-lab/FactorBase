package ca.sfu.cs.factorbase.exporter.bifexporter.bif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class BIF_IO {
    public static ArrayList<String[]> getLinksFromFile(String filePath) throws ValidityException, ParsingException, IOException {
        Builder xmlParser = new Builder();
        Document doc = xmlParser.build(new File(filePath));
        Element root = doc.getRootElement();
        root = root.getFirstChildElement("NETWORK");

        // First, get a list of all variables.
        ArrayList<String> variables = new ArrayList<String>();
        Elements varElements = root.getChildElements("VARIABLE");
        for (int i = 0; i < varElements.size(); i++) {
            Element curVar = varElements.get(i);
            variables.add(curVar.getChildElements("NAME").get(0).getValue());
        }

        // Next, get a list of links.
        ArrayList<String[]> links = new ArrayList<String[]>();
        Elements defElements = root.getChildElements("DEFINITION");
        for (int i = 0; i < defElements.size(); i++) {
            // First, get all the GIVEN values from this definition.
            ArrayList<String> givenVals = new ArrayList<String>();
            Elements givenElements = defElements.get(i).getChildElements("GIVEN");

            for (int j = 0; j < givenElements.size(); j++) {
                givenVals.add(givenElements.get(j).getValue());
            }

            // Next, get the FOR value from this definition.
            Elements forElements = defElements.get(i).getChildElements("FOR");
            String forVal = forElements.get(0).getValue();

            // If the file contains FOR's with no GIVEN's, then record the FOR as a child with no parents.
            if (givenVals.size() == 0) {
                String[] edge = {"", forVal};
                links.add(edge);
            } else {
                // For each GIVEN value, add an edge from it to the FOR value.
                for (String givenVal : givenVals) {
                    String[] edge = {givenVal, forVal};
                    links.add(edge);
                }
            }

            // Finally, remove the forVal from the list of variables.
            if (variables.contains(forVal)) {
                variables.remove(forVal);
            }
        }

        // For each remaining variable which was never used as a FOR value, we know that it is never a child,
        // so add it as a child with no parents.
        for (String var : variables) {
            String[] edge = {"", var};
            links.add(edge);
        }

        return links;
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