package ca.sfu.jbn.common;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

//import ca.sfu.jbn.classification.global;
//import com.microsoft.jdbc.*;

/**
 * 
 * User: bahare bina Date: Jun 8, 2008 Time: 9:24:32 AM To change this template
 * use File | Settings | File Templates.
 */
public class db {
    public static Connection conn;

    public static db database = new db();

    public static db getInstance() {
        return database;
    }

    public db() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            // com.microsoft.jdbc.sqlserver.SQLServerDriver ());
            // \\com.sql.jdbc.Driver
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
        	//look for config.xml and read in connection info
        	
        	
    			FileInputStream f = new FileInputStream("config.xml");

    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document document = builder.parse(new InputSource((new InputStreamReader(f, "utf-8"))));

    			Node root = document.getDocumentElement();
    			
    			
    			NodeList rootChildNodes = root.getChildNodes();
    			Node currentNode;
    			

    			for (int i = 0; i < rootChildNodes.getLength(); i++) {
    				currentNode = rootChildNodes.item(i);
    				if (currentNode.getNodeName().equals("dbURL")) {
    					global.dbURL=currentNode.getTextContent().toString();
    				}
    				else if (currentNode.getNodeName().equals("user")) {
    					global.dbUser=currentNode.getTextContent().toString();
    				}
    				else if (currentNode.getNodeName().equals("password")) {
    					global.dbPassword=currentNode.getTextContent().toString();
    				}
    			}

    		
    	
        	
            conn = DriverManager.getConnection(
                    global.dbURL , global.dbUser, global.dbPassword);

             //conn = (Connection)
            // DriverManager.getConnection("jdbc:mysql://kripke/Financial DRed",
            // "hassan", "");
            // conn = (Connection)
          //  DriverManager.getConnection("jdbc:mysql://kripke/"+schema,
           //  "hassan", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ////////////////////////Select number of distinct values
    public int count(String tableName, String where) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            
            java.sql.ResultSet res3 = myst.executeQuery("SELECT COUNT(*) FROM "
                    + tableName + " where " + where + ";");
            
            res3.next();
            return res3.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
            System.out.println("SELECT COUNT(*) FROM " + tableName +
                    " where " + where + ";");
        }
        return 0;
    }

    public ArrayList describeTable(String tableName) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("describe " + tableName + ";");

            while (res3.next()) {
                result.add(res3.getString("Field"));
            }
        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;
    }

    // public java.sql.ResultSet selectAll(String tableName){
    // java.sql.Statement myst = null;
    // java.sql.ResultSet res3 = null;
    // java.sql.ResultSet result = null;
    //
    // try {
    // myst = conn.createStatement();
    //
    // res3 = myst.executeQuery("SELECT * FROM "+ tableName + ";");
    // result = res3;
    //          
    // } catch (SQLException e1) {
    // e1.printStackTrace(); // To change body of catch statement use
    // // File | Settings | File Templates.
    // }
    // return result;
    // }

    // //////////////////////////////////////////////Select all values
    public ArrayList Values(String tableName, String fieldName) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT distinct " + fieldName + " FROM "
                    + tableName + ";");

            while (res3.next()) {
                result.add(res3.getString(fieldName));
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;

    }
    
    // for specific whereClause, count the number of record for each node value, return as Arraylist<int>
    public ArrayList<Integer> count(String tableName, String fieldName, ArrayList nodeValues, String whereClause) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            for (int i = 0; i < nodeValues.size(); i++) {
            	if (i != 0) sb.append(", ");
            	sb.append("SUM(IF("+fieldName+"=\""+nodeValues.get(i)+"\",1,0)) AS `"+fieldName+"_"+nodeValues.get(i)+"`");
            }
            sb.append(" FROM " + tableName);
            if (whereClause.length() > 0) {
            	sb.append(" WHERE " + whereClause + ";");
            }

            //System.out.println(sb.toString());
            res3 = myst.executeQuery(sb.toString());

            while (res3.next()) {
                for (int i = 1; i <= nodeValues.size(); i++) {
                	result.add(res3.getInt(i));
                }
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;

    }

    // ////////////////////////////////////////////////
    public int countStar(String tableName) {
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            java.sql.ResultSet res3 = myst.executeQuery("SELECT count(*) FROM "
                    + tableName + " ;");

            res3.next();
            return res3.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return 0;

    }

    // ////////////////////////////////////////////////////
    public int count2(String tableName, String fieldName) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            java.sql.ResultSet res3 = myst
                    .executeQuery("SELECT COUNT(DISTINCT " + fieldName
                            + ") FROM " + tableName);

            res3.next();

            return (res3).getInt(1);
        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return 0;

    }

    // returns the number count for sql in "query"
    public double countsql(String query) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            java.sql.ResultSet res3 = myst.executeQuery(query);
            res3.next();
            return (res3).getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }

        return 0;
    }

    public String makeTableName(String table1, List tables) {
        String tableName = new String();
        ArrayList viewName = new ArrayList();
        // if (table1.contains(",")) {
        String[] temp = table1.split(",");
        viewName.add(temp[0]);
        for (int i = 1; i < temp.length; i++) {
            viewName.add(temp[i]);
        }
        // }
        for (int i = 0; i < tables.size(); i++) {
            viewName.add(tables.get(i).toString());
        }
        Object[] temp2 = viewName.toArray();
        Arrays.sort(temp2);
        for (int j = 0; j < temp2.length; j++)
            tableName += temp2[j];
        return tableName;
    }

    public String makeSQL(String table1, List tables) {
        ArrayList viewName = new ArrayList();
        String tempsql = "SELECT distinct * FROM ";
        if (table1.contains(",")) {
            String[] temp = table1.split(",");

            tempsql += temp[0];
            for (int i = 1; i < temp.length; i++) {
                tempsql += " natural JOIN " + temp[i];
            }
            for (int k = 0; k < tables.size(); k++) {
                tempsql += " natural JOIN " + tables.get(k);
            }

        }
       
        else  {
            if (tables.size() != 0) {
                tempsql += table1;
                for (int i = 0; i < tables.size(); i++) {
                    tempsql += " natural JOIN " + tables.get(i);
                }
            }
        }
        //System.out.println(tempsql);
        return tempsql;
    }

    // //////////////////////////

    // /////////////////////////////////////////
    public ArrayList getAllRows(String tableName) {
        ArrayList result = new ArrayList();

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT * FROM " + tableName + ";");

            java.sql.ResultSetMetaData rsMetaData = res3.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();

            while (res3.next()) {
                for (int i = 0; i < numberOfColumns; i++) {
                    result.add(res3.getString(i + 1));
                }
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;

    }

    public String getAllRowsInString(String tableName) {
        StringBuffer result = new StringBuffer("");

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT * FROM " + tableName + ";");

            java.sql.ResultSetMetaData rsMetaData = res3.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();

            while (res3.next()) {
                for (int i = 0; i < numberOfColumns; i++) {
                    result.append(res3.getString(i + 1) + ",");
                }
                result.deleteCharAt(result.length() - 1);
                result.append("\n");
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result.toString();

    }

    public ArrayList getAllRows(String tableName, String columnName) {
        ArrayList result = new ArrayList();

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT * FROM " + tableName + ";");

            while (res3.next()) {
                result.add(res3.getString(columnName));
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;

    }

    public ArrayList<String> getRows(String tableName,
            ArrayList<String> columnNames) {

        ArrayList<String> result = new ArrayList<String>();

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT * FROM " + tableName + ";");

            while (res3.next()) {
                for (String columnName : columnNames) {
                    result.add(res3.getString(columnName));
                }
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result;

    }

    public String getRowsInString(String tableName,
            ArrayList<String> columnNames) {
        StringBuffer result = new StringBuffer("");

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();

            res3 = myst.executeQuery("SELECT * FROM " + tableName + ";");

            java.sql.ResultSetMetaData rsMetaData = res3.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();

            while (res3.next()) {
                for (String columnName : columnNames) {
                    result.append(res3.getString(columnName) + ",");
                }
                result.deleteCharAt(result.length() - 1);
                result.append("\n");
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        return result.toString();

    }

    // ////////////////////////////////////////////////////////////////////
    public void joinForClassification(String tableName, String nameWithComma) {
        java.sql.Statement myst = null;

        String tempsql = makeSQL(nameWithComma, new ArrayList());

        try {
            myst = conn.createStatement();
            myst.executeUpdate("create view  " + tableName + " as (" + tempsql
                    + ")");

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    public ArrayList joinNatural(String table1, ArrayList tables) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        String tableName = new String();
        java.sql.ResultSet res3;
        tableName = makeTableName(table1, tables);
        String tempsql = makeSQL(table1, tables);

        try {
            myst = conn.createStatement();
            myst.executeUpdate("drop view if exists " + tableName);
            int i = myst.executeUpdate("create  view  " + tableName + " as "
                    + tempsql);

            res3 = myst.executeQuery("select * from " + tableName);
            int cols = res3.getMetaData().getColumnCount();
            Vector headers = new Vector(15);
            for (int k = 1; k <= cols; k++) {
                String tempString = res3.getMetaData().getColumnLabel(
                        k);
                if (!headers.contains(tempString)) {
                    headers.add(tempString);
                    result.add(tempString);
                }
            }
            result.add("\r\n");
            while (res3.next()) {
                for (int k = 0; k < headers.size(); k++) {
                    result.add(res3.getString(headers.get(k).toString()));
                }
                result.add("\r\n");
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    // //not doing anything at the moment
    public ArrayList joinTabel(String table1, ArrayList tables, int shows) {
        ArrayList result = new ArrayList();
        java.sql.Statement myst = null;
        String tableName = new String(table1);
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++)
                tableName += "," + tables.get(i);
        } else
            tableName = table1;
        int i = 0;
        String where = findJoinCond(tableName).substring(4);
        try {
            myst = conn.createStatement();
            java.sql.ResultSet res3;
            if (shows == 0) {
                res3 = myst.executeQuery("SELECT count(*) FROM " + tableName
                        + " where " + where + ";");

                res3.next();
                ArrayList a = new ArrayList();
                a.add((res3.getString(1)));
                return a;
            } else {
                res3 = myst.executeQuery("SELECT * FROM " + tableName
                        + " where " + where + ";");

                int cols = res3.getMetaData().getColumnCount();
                Vector headers = new Vector(15);
                for (int k = 1; k <= cols; k++) {
                    String tempString = res3.getMetaData()
                            .getColumnLabel(k);
                    if (!headers.contains(tempString)) {
                        headers.add(tempString);
                        result.add(tempString);
                    }
                }
                result.add("\r\n");
                while (res3.next()) {
                    for (int k = 0; k < headers.size(); k++) {
                        result.add(res3.getString(headers.get(k).toString()));
                    }
                    result.add("\r\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }

        return result;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    public double probOFJoin(ArrayList table) {

        java.sql.Statement myst = null;
        String tableName = new String();
        java.sql.ResultSet res3;

        Object[] temp2 = table.toArray();
        Arrays.sort(temp2);
        for (int j = 0; j < temp2.length; j++)
            tableName += temp2[j];

        try {
            myst = conn.createStatement();
            res3 = myst.executeQuery("SELECT count(*) FROM " + tableName + ";");

            res3.next();

            return res3.getDouble(1);

        } catch (Exception e) {
        }
        return 0;
    }

    // ///////////////////
    public String findJoinCond(String joinTableName) {
        Parser parser = new Parser();
        String[] tables = joinTableName.split(",");
        String result = new String();
        ArrayList relationships = new ArrayList();
        ArrayList table = new ArrayList(Arrays.asList(tables));
        int ind = 0;
        for (int i = 0; i < tables.length; i++) {
            //  
            if (parser.getRelations().contains(tables[i]))
                // if(relation.containsKey(tables[i]))
                relationships.add(tables[i]);
        }
        for (int j = 0; j < relationships.size(); j++) {
            ArrayList ent = parser.getEntities(relationships.get(j).toString());
            for (int k = 0; k < ent.size(); k++) {
                if (table.contains(ent.get(k))) {
                    ind = parser.getEntityIndex(ent.get(k).toString());
                    ArrayList entityID = parser.getEntityId(ind);
                    for (int y = 0; y < entityID.size(); y++) {
                        result += " and ";
                        result += ent.get(k).toString() + '.' + entityID.get(y)
                                + " = " + relationships.get(j).toString() + '.'
                                + entityID.get(y);
                    }
                }

            }
            for (int y = j + 1; y < relationships.size(); y++) {
                ArrayList ent2 = parser.getEntities(relationships.get(y)
                        .toString());

                for (int k1 = 0; k1 < ent2.size(); k1++) {
                    for (int k = 0; k < ent.size(); k++) {
                        if (ent2.get(k1).toString().equals(ent.get(k))) {
                            int in = parser.getEntityIndex(ent.get(k)
                                    .toString());
                            ArrayList entityID = parser.getEntityId(in);
                            for (int y1 = 0; y1 < entityID.size(); y1++) {
                                result += " and ";
                                result += relationships.get(j).toString() + '.'
                                        + entityID.get(y1) + " = "
                                        + relationships.get(y).toString() + '.'
                                        + entityID.get(y1);
                            }
                        }
                    }

                }
            }
        }

        return result;

    }

    // ///////////////////////

    public void create(String tableName, String where) {
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            myst.executeUpdate("drop view if exists New" + tableName);
            if (where.length() == 0)
                myst.executeUpdate("create view New" + tableName
                        + "  as (select distinct * from " + tableName + ")");

            else
                myst.executeUpdate("create view New" + tableName
                        + "  as (select distinct * from " + tableName
                        + " where " + where + ")");
        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
    }

    // ///////////////////////////////////////////////////////////
    public void createSchema(String name) {
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            int res3 = myst.executeUpdate("drop schema if exists " + name);
            
            res3 = myst.executeUpdate("create schema " + name);
            
        } catch (SQLException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
    }

    // ////////////////////////////
    public static void closeDB() {
        try {
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // get the table name according to the given list of column names
    public ArrayList<String> getTableNames() throws SQLException {
        java.sql.ResultSet res3;
        java.sql.Statement myst = null;
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result2 = new ArrayList<String>();
        reconnect();
       myst = conn.createStatement();
   
       
       res3 = myst.executeQuery("show full tables");
        while (res3.next()) {
            // store base tables and views seperately
            if ((res3.getString("Table_type")).equals("BASE TABLE"))
                // equals())
                result.add(res3.getString("Tables_in_" + global.schema));
            else {
                result2.add(res3.getString("Tables_in_" + global.schema));
            }
        }
        // append views to the end of table names
        result.addAll(result2);
        return result;
    }

  
    public ArrayList<String> getColumns(String tableName) throws SQLException {
        java.sql.ResultSet res3;
        java.sql.Statement myst = null;
        ArrayList<String> result = new ArrayList<String>();
        //elwin
        //reconnect();
        myst = conn.createStatement();
        res3 = myst.executeQuery("describe " + tableName);
        while (res3.next()) {
            result.add(res3.getString("FIELD"));

        }
        return result;
    }
    public void DoQuery(String query) {
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            myst.executeUpdate(query);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
 public void reconnect(){
    	
    	try {
    		//conn.close();
            //conn = (Connection) DriverManager.getConnection( global.dbURL + global.schema, global.dbUser, global.dbPassword);
            conn = DriverManager.getConnection( global.dbURL + global.schema, global.dbUser, global.dbPassword);

            // conn = (Connection)
            // DriverManager.getConnection("jdbc:mysql://kripke/Financial DRed",
            // "hassan", "");
            // conn = (Connection)
            // DriverManager.getConnection("jdbc:mysql://kripke/"+schema,
            // "hassan", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reconnect(String schema){
    	
    	try {
//    		conn.close();
            conn = DriverManager.getConnection(
                    global.dbURL + schema, global.dbUser, global.dbPassword);
            // conn = (Connection)
            // DriverManager.getConnection("jdbc:mysql://kripke/Financial DRed",
            // "hassan", "");
            // conn = (Connection)
            // DriverManager.getConnection("jdbc:mysql://kripke/"+schema,
            // "hassan", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void DoQueryUnsafe(String query) {
        java.sql.Statement myst = null;
        try {
            myst = conn.createStatement();
            myst.executeUpdate(query);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
        }

    }

    // returns the name of the table created as view
    public String joinNatural1(String table1, List<String> possibleComb) {
        String tableName = makeTableName(table1, possibleComb);

        
        java.sql.Statement myst = null;
        java.sql.ResultSet res3;
        String tempsql = makeSQL(table1, possibleComb);
    try {
    	//reconnect(global.schema);
            myst = conn.createStatement();
            myst.executeUpdate("drop view if exists " +tableName);
            int i = myst.executeUpdate("create  view  " + tableName + " as " + tempsql);
            
        }
catch (Exception e) {
                e.printStackTrace();

            }
return tableName;

    }

    public List<String> Select(String tableName, List<String> what,String where) {
        ArrayList result = new ArrayList();

        java.sql.Statement myst = null;
        java.sql.ResultSet res3 = null;

        try {
            myst = conn.createStatement();
            
            String query="SELECT ";
            for(String one:what){
            	query+=one+", ";
            }
            query=query.substring(0, query.lastIndexOf(","));
            
            query+=" FROM " + tableName + " WHERE "+where+" order by rand();";
            
//            System.out.println(query);
            res3 = myst.executeQuery(query);
            
            while (res3.next()) {
            	for (String columnName : what) {
                    result.add(res3.getString(columnName));
                }
               
            }

        } catch (SQLException e1) {
            e1.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        
        return result;

    }

}
