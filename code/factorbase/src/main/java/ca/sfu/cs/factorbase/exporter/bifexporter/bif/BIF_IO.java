package ca.sfu.cs.factorbase.exporter.bifexporter.bif;

import java.util.ArrayList;

public class BIF_IO {
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