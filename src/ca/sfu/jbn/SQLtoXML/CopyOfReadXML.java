package ca.sfu.jbn.SQLtoXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import ca.sfu.jbn.common.global;

public class CopyOfReadXML {

	class Node {
		String name;
		ArrayList<String> attribute;
		ArrayList<String> parents;
		boolean isParent;

		Node(String s) {
			name = s;
			attribute = new ArrayList<String>();
			parents = new ArrayList<String>();
			isParent = false;
		}

		public void setIsParent(boolean b) {
			isParent = b;
		}

		public boolean isParent() {
			return isParent;
		}

		public String getName() {
			return name;
		}

		public void addAttribute(String a) {
			if (!a.contains("_id"))
				a = "+" + a.toLowerCase();
			attribute.add(a);
		}

		public ArrayList<String> getAttList() {
			return attribute;
		}

		public void addParent(String a) {
			parents.add(a);
		}

		public ArrayList<String> getParents() {
			return parents;
		}

		public String print() {
			String result = this.getName() + "(";
			int i = 0;

			for (; i < attribute.size() - 1; i++) {
				result = result
						.concat(attribute.get(i).substring(0, 3) + "1, ");
			}
			result = result.concat(attribute.get(i).substring(0, 3) + "1)");
			return result;
		}
	}

	private Connection DBConnection;
	private ArrayList<TableXML> tables;
	private HashMap<String, Node> nodes;
	private Statement cmd;

	public CopyOfReadXML() {
		tables = new ArrayList<TableXML>();
		nodes = new HashMap<String, Node>();
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setConnection(String url, String userID, String passWord) {
		try {
			DBConnection = DriverManager.getConnection(url,
					userID, passWord);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize() throws SQLException {
		global.WorkingDirectory = global.schema;
		global.XMLFile = global.schema + "/relation.xml";
		setConnection(global.dbURL + global.schema, global.dbUser,
				global.dbPassword);
		cmd = DBConnection.createStatement();
		ResultSet queriedTables = cmd.executeQuery("show tables");
		// get the table names
		while (queriedTables.next()) {
			TableXML tempTable = new TableXML(queriedTables.getString(1));
			tables.add(tempTable);
		}
		queriedTables.close();

		// get detailed table info
		for (Iterator<TableXML> i = tables.iterator(); i.hasNext();) {
			TableXML oneTable = i.next();
			ResultSet tableQuery = cmd.executeQuery("show columns from "
					+ oneTable.getName());
			while (tableQuery.next()) {
				TableXML.KeyType keyType;
				if (tableQuery.getString("KEY").equals("PRI")) {
					keyType = TableXML.KeyType.PRIMARY;
				} else if (tableQuery.getString("KEY").equals("MUL")) {
					keyType = TableXML.KeyType.FOREIGH;
				} else {
					keyType = TableXML.KeyType.NONE;
				}
				oneTable.addKey(tableQuery.getString("Field"), keyType);
			}
			tableQuery.close();
			// delete the xml file, if exists
			
			}
		try {
			File f = new File(global.WorkingDirectory);
			f.mkdir();
			File fi = new File(global.XMLFile);
			if (fi.exists()) {
				boolean success = f.delete();
				if (!success){
					throw new IllegalArgumentException(
						"Delete: deletion failed");}
			}
				PrintStream out = new PrintStream(new FileOutputStream(
						global.XMLFile));
				ConvertToXML(out);
			

		} catch (Exception e) {

		}
	}

	public void ConvertToXML(PrintStream output) throws SQLException,
			IOException {
		// do two rounds of parsing
		// first just find the entity name, and the corresponding entity id
		// second round write the real xml
		// clear the output stream

		// output xml header
		output.println("<relationships>");

		HashMap<String, String> entities = new HashMap<String, String>();
		// first round, store entity id name and table name
		// in the meantime,output the entity part of the xml
		for (Iterator<TableXML> i = tables.iterator(); i.hasNext();) {
			TableXML oneTable = i.next();
			ArrayList<String> primarykeys = oneTable.getPrimaryKeys();
			if (!primarykeys.isEmpty()) {
				String primaryKey = primarykeys.get(0);
				entities.put(primaryKey, oneTable.getName());

				// compose entity part of xml
				ArrayList<String> otherKeys = oneTable.getOtherKeys();
				output.println("<entity>");
				output.print("<entity_name>");
				output.print(oneTable.getName());
				output.println("</entity_name>");
				output.print("<entity_id>");
				output.print(primaryKey);
				output.println("</entity_id>");

				if (!otherKeys.isEmpty()) {
					for (int j = 0; j < otherKeys.size(); j++) {
						String otherKey = otherKeys.get(j);
						output.print("<entity_att>");
						output.print(otherKey);
						output.println("</entity_att>");
					}
				}

				output.println("</entity>");
			}
		}

		// second iteration, deal with relationship tables
		for (Iterator<TableXML> i = tables.iterator(); i.hasNext();) {
			TableXML oneTable = i.next();
			ArrayList<String> foreignKeys = oneTable.getForeignKeys();
			ArrayList<String> otherKeys = oneTable.getOtherKeys();

			// table with foreign keys are treated as relationship tables
			if (!foreignKeys.isEmpty()) {
				output.println("<relation>");
				output.print("<name>");
				output.print(oneTable.getName());
				output.println("</name>");

				for (int j = 0; j < foreignKeys.size(); j++) {
					String foreighKey = foreignKeys.get(j);
					output.print("<ref_entity>");
					output.print(entities.get(foreighKey));
					output.println("</ref_entity>");
				}

				if (!otherKeys.isEmpty()) {
					for (int j = 0; j < otherKeys.size(); j++) {
						String otherKey = otherKeys.get(j);
						output.print("<rel_att>");
						output.print(otherKey);
						output.println("</rel_att>");
					}
				}

				output.println("</relation>");
			}
		}
		// output xml footer
		output.print("</relationships>");
		output.flush();
		output.close();
	}

	public static void main(String[] args) throws Exception {
		CopyOfReadXML read = new CopyOfReadXML();

		String schema = "";
		String dbURL = "jdbc:mysql://kripke.cs.sfu.ca/:3306/";
		String dbUser = "sfu";
		String dbPassword = "";
		if (args.length == 1) {
			schema = args[0];

		} else if (args.length == 4) {
			schema = args[0];
			dbURL = args[1];
			dbUser = args[2];
			dbPassword = args[3];
		} else {
			System.out
					.println("argument: dataset name <database connection><databse user><database password>");
			System.exit(1);
		}

		read.setConnection(dbURL + schema, dbUser, dbPassword);
		read.initialize();

		PrintStream out = new PrintStream(new FileOutputStream("relation.xml"));

		read.ConvertToXML(out);
		out.close();
		// out2.println("\n// rules after moralization\n");
		// read.moralization(out2);
	}
}
