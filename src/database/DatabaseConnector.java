/*
 * DatabaseConnector.java
 * 
 * An abstract class for implementing objects which can be used to connect to databases
 * -For instance, an object which connects to an MySQL database would implement this interface
 */

package database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class DatabaseConnector {

	/*
	 * Method for connecting to the database
	 */
	public abstract void connect(String username, String password, String serverUrl, String databaseName, String port) throws SQLException;
	
	/*
	 * Method for querying the database
	 * -Use this method for queries which have a return value (ie: SELECT)
	 * -Use the update method for queries which have no return value (ie: DELETE)
	 */
	public abstract ResultSet query(String query) throws SQLException;
	
	/*
	 * Method for closing the connection to the database
	 */
	public abstract void close() throws SQLException;
	
	/*
	 * Method for updating the database
	 * -An update is any query which has no return value (ie: UPDATE, DELETE, INSERT)
	 */
	public abstract void update(String query) throws SQLException;
	
	/*
	 * Returns a list of column names given a result set
	 * -The column names are the columns of the table the result set was taken from
	 */
	public static ArrayList<String> getColumnsFromResultSet(ResultSet rs) {
		try {
			//From: http://www.exampledepot.com/egs/java.sql/GetRsColCount.html
			ArrayList<String> toReturn = new ArrayList<String>();
			
		    // Get result set meta data
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int numColumns = rsmd.getColumnCount();

		    // Get the column names; column indices start from 1
		    for (int i=1; i<numColumns+1; i++) {
		        toReturn.add(rsmd.getColumnName(i));
		    }
			return toReturn;
		} catch (SQLException e) {
			System.out.println(e);
			return null;
		}
	}
	
}
