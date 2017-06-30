/*
 * MySQLDatabaseConnector
 * 
 * Database connector which connects to a MySQL database
 */

package database;

import java.sql.*;
import java.util.Properties;

public class MySqlDatabaseConnector extends DatabaseConnector{

	Connection _conn = null;
	
	/*
	 * Connects the connector to the database
	 */
	@Override
	public void connect(String username, String password, String serverUrl, String databaseName, String port) throws SQLException {
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", username);
	    connectionProps.put("password", password);
        _conn = DriverManager.getConnection("jdbc:mysql://" + serverUrl + ":" + port + "/" + databaseName ,connectionProps);
	}
	
	/*
	 * queries the database
	 */
	@Override
	public ResultSet query(String query) throws SQLException {
		Statement stmt = _conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		return rs;
	}
	
	/*
	 * Updates the database
	 */
	@Override
	public void update(String query) throws SQLException {
		Statement stmt = _conn.createStatement();
		stmt.executeUpdate(query);
	}
	
	/*
	 * closes the database
	 */
	@Override
	public void close() throws SQLException {
		_conn.close();
	}
}
