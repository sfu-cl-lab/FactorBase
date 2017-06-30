package ca.sfu.autocorrelation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.SQLtoXML.TableXML;
import ca.sfu.jbn.common.db;
import ca.sfu.jbn.common.global;

public class CorrelatedSQLToXML extends ReadXML {
	// start with the generated XML file
	// if there is a relationship dealing with the same entity
	// duplicate that entity, change names or its attributes
	// make the modification in the db, create a view for the entity, change
	// names for its attributes
	// also change the foreign key constraints in the relationship table, and
	// the name of the entity
	// finally, output the revised xml file



	private  List<String> correlatedEntities = new ArrayList<String>();




	@Override
	public void initialize() throws SQLException {
		global.WorkingDirectory = global.schema;
		global.XMLFile = global.schema + "/relation.xml";
		db.getInstance().reconnect();
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
	public void modifyDB() {
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
			}
		}

		// second iteration, deal with relationship tables
		for (Iterator<TableXML> i = tables.iterator(); i.hasNext();) {
			TableXML oneTable = i.next();
			ArrayList<String> foreignKeys = oneTable.getForeignKeys();
			ArrayList<String> otherKeys = oneTable.getOtherKeys();

			// table with foreign keys are treated as relationship tables
			if (!foreignKeys.isEmpty()) {
				String correlatedEntity = null;
				String correlatedEntry2 = null;
				String correlatedEntityTable = null;
				String lastEntity = "";
				for (int j = 0; j < foreignKeys.size(); j++) {
					String foreighKey = foreignKeys.get(j);
					String currentEntity = foreighKey;

					if (currentEntity.equals(lastEntity + "_dummy")) {
						correlatedEntry2 = currentEntity;
						correlatedEntity = lastEntity;
						correlatedEntityTable = entities.get(correlatedEntity);
					}

					if (lastEntity.equals(currentEntity + "_dummy")) {
						correlatedEntity = currentEntity;
						correlatedEntry2 = lastEntity;
						correlatedEntityTable = entities.get(correlatedEntity);
					}
					lastEntity = currentEntity;
				}
				// if this is autocorrelation
				if (correlatedEntity != null) {
					correlatedEntities.add(correlatedEntityTable);
					// create a view from entity table
					// and in the view, names of attributes should also be
					// changed
					// find names of attributes from the entity table
					TableXML targetEntityTable = null;
					for (Iterator<TableXML> entityTables = tables.iterator(); entityTables
					.hasNext();) {
						TableXML oneEntity = entityTables.next();
						if (oneEntity.getName().equals(correlatedEntityTable)) {
							targetEntityTable = oneEntity;
							break;
						}
					}
					String createViewSQL = "create or replace view "
						+ targetEntityTable.getName() + "_dummy as ";
					createViewSQL += "(select ";
					String primaryKeyName = targetEntityTable.getPrimaryKeys()
					.get(0);
					createViewSQL += primaryKeyName + " as " + primaryKeyName
					+ "_dummy";
					for (String other : targetEntityTable.getOtherKeys()) {
						createViewSQL += ", " + other + " as " + other
						+ "_dummy";
					}
					createViewSQL += " from " + targetEntityTable.getName();
					createViewSQL += ") ";
					// create the view
					try {
						cmd.execute(createViewSQL);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// no need to change foreign key constraint

				}

			}
		}

	}

	@Override
	public void ConvertToXML(PrintStream output) throws SQLException,
	IOException {
		// do two rounds of parsing
		// first just find the entity name, and the corresponding entity id
		// second round write the real xml
		// clear the output stream
		modifyDB();
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

				// duplicate the entity definition if it is correlated
				if (correlatedEntities.contains(oneTable.getName())) {
					entities.put(primaryKey+"_dummy", oneTable.getName()+"_dummy");

					// compose entity part of xml
					otherKeys = oneTable.getOtherKeys();
					output.println("<entity>");
					output.print("<entity_name>");
					output.print(oneTable.getName()+"_dummy");
					output.println("</entity_name>");
					output.print("<entity_id>");
					output.print(primaryKey+"_dummy");
					output.println("</entity_id>");

					if (!otherKeys.isEmpty()) {
						for (int j = 0; j < otherKeys.size(); j++) {
							String otherKey = otherKeys.get(j);
							output.print("<entity_att>");
							output.print(otherKey+"_dummy");
							output.println("</entity_att>");
						}
					}

					output.println("</entity>");
				}
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String schema = "";
		String dbURL = "jdbc:mysql://kripke.cs.sfu.ca/";
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
		global.dbURL = dbURL;
		global.dbUser = dbUser;
		global.schema = schema;
		global.dbPassword = dbPassword;
		CorrelatedSQLToXML correlatedSQLToXML = new CorrelatedSQLToXML();
		try {
			correlatedSQLToXML.initialize();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}
