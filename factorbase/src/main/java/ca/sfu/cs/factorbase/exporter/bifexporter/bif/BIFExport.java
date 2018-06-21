package bif;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class BIFExport {
	
	//import connection con2 (e.g. unielwin_BN), get rid of config.java. Yan Sept 10th
	public static void Export(String filePath, String idName, String tableName, String pvid, Connection conn) throws IOException, SQLException {
		//create the file to write the exported file to
		/*String filename = "";
		if (!fileLoc.equals("")) {
			filename = fileLoc + "\\" + pvid + ".xml";
		}
		else {
			filename = pvid + ".xml";
		}
		FileWriter file = new FileWriter(filename);*/
		
        FileWriter file = new FileWriter(filePath);
		BufferedWriter out = new BufferedWriter(file);
		
		//write out the header information for the file
		out.write(BIF_IO.writeBifHeader());
		out.write(BIF_IO.writeNetworkBegin(pvid));
		
		//Get a list of variables and a list of links
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<String[]> connections = new ArrayList<String[]>();
		
		Statement st = conn.createStatement();
        //System.out.println("SELECT * FROM " + tableName + " WHERE " + idName + " = \"" + pvid + "\"");
		ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE " + idName + " = \"" + pvid + "\"");
		
		//System.out.println("\n### the Dag Begin:\n");//zqian
		FileWriter fstream = new FileWriter("dag_.txt"); 
		BufferedWriter DagOut = new BufferedWriter(fstream);
		
		while (rs.next()) {
			//get the child and parent for this row
			String childVar = rs.getString("child");
			childVar= childVar.replace("`","") ;
			// @zqian replace the "`" with during export the data to .csv file since tetrad does not support this  Apr.3rd, 2013
			
			String parentVar = rs.getString("parent");
			parentVar =parentVar.replace("`",""); // @zqian replace the "`" with during export the data to .csv file since tetrad does not support this
			
			//if we haven't see this child yet, write it out as a variable to the bif file
			if (!variables.contains(childVar)) {
				variables.add(childVar);
				out.write(BIF_IO.writeVariable(childVar, new ArrayList<String>()));
			}
			
			//store the link
			String[] link = {childVar, parentVar};
			connections.add(link);
		}

		

		//For each variable, find all its links and write them out
		for (String variable : variables) {
			ArrayList<String> givenVals = new ArrayList<String>();
			for (String[] link : connections) {
				if (link[0].equals(variable)) {
					if (!link[1].equals("")) {
						givenVals.add(link[1]);
					}
				}
			}
			
			if (givenVals.size() > 0) {
				out.write(BIF_IO.writeDefinition(variable, givenVals));
				/* redirect the dag into a .txt file:begin*/
				int a =0;	
				while(!givenVals.isEmpty())
				{  
					//System.out.println(variable+"\t"+givenVals.get(a));
					//agOut.write(variable+"\t"+givenVals.get(a)+"\n");
				//	System.out.println(givenVals.get(a)+"\t"+variable);
					DagOut.write(givenVals.get(a)+"\t"+variable+"\n");
					givenVals.remove(a);
				}
				/* redirect the dag into a .txt file :end*/
				
			}
		}
		
		
		//write out the end of the file
		out.write(BIF_IO.writeNetworkEnd());
		
		DagOut.close(); //zqian
		fstream.close(); //kdr4
		//System.out.println("\n### the  Dag End\n"); //zqian

		//close the created file
		out.flush();
		out.close();
		file.close();
		st.close();
	}
	
	
}
