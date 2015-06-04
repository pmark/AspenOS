/** -------------------------------------
	FILE: Persistence.java
	@author P. Mark Anderson
	DATE: 7/2000
    ------------------------------------- */

package org.aspenos.db;

import java.sql.*;
import java.util.*;

/**
 * The DbPersistence interface defines a set of methods that 
 * provide a layer between an application and JDBC calls.  
 * Since most applications will never need methods that
 * apply the more advanced features of JDBC, DbPersistence
 * defines the getConnection method, which returns a
 * java.sql.Connection.
 *
 * @author P. Mark Anderson
 * @version 1.0
 */
public interface DbPersistence {

	/**
	 * Opens a new database connection.
	 * @param dsn the URL to a database in this format:
	 *    jdbc:postgresql://www.internalrecords.com:5432/some_db
	 * @param user a username for the resource
	 * @param password the password to authenticate the user
	 */
	public void open(String dsn, String user, String password)
			throws SQLException;


	/**
	 * Opens a new database connection using a Properties object.
	 * @param dsn the URL to a database in this format:
	 *    jdbc:postgresql://www.internalrecords.com:5432/some_db
	 * @param props a list of properties with at least 
	 *    "user" and "password" fields defined.
	 */
	public void open(String dsn, Properties props)
			throws SQLException;


	/**
	 * Reopens a database connection using the 
	 * original database info.
	 */
	public void open() throws SQLException;


	/**
	 * Closes all open resources associated with the current
	 * database connection.
	 */
	public void close() throws SQLException;



 // SELECTION METHODS ////////////////////////////////////////

	/**
	 * Returns a matrix of results of a SELECT statement
	 */
	public List select(String attrib, String table, String where) 
			throws SQLException;


	/**
	 * Returns the first record that matches this query.
	 */
	public List selectFirst(String attrib, String table, String where) 
			throws SQLException;


	/**
	 * Returns a matrix of results of a SELECT statement
	 */
	public List selectAsHash(String attrib, String table, String where)
			throws SQLException;


	/**
	 * Returns the first record that matches this query.
	 */
	public Map selectFirstAsHash(String attrib, String table, String where)
			throws SQLException;


	/**
	 * Executes a query without a WHERE clause on a table.
	 */
	public List selectAll(String table)
			throws SQLException;


	/**
	 * Returns the first (or only) selected attribute of all matching
	 * records.  Useful for retrieving a list of values for one
	 * attribute.
	 * @return a list of this attribute's matching values for the query
	 */
	public List selectAttrib(String attrib, String table, String where)
			throws SQLException;


	/**
	 * Returns the first (or only) selected attribute of the first 
	 * record retrieved.  Useful for retrieving just one value
	 * at a time.
	 * @return the value retrieved, same data type as in select()
	 * @see select()
	 */
	public Object selectFirstAttrib(String attrib, String table, String where)
			throws SQLException;


	/**
	 * Calls Statement.executeQuery on the given query and returns 
	 * the results just like select() does. 
	 */
	public List query(String query)
			throws SQLException;


	/**
	 * Returns the number of records that match this query.
	 */
	public int howMany(String table, String where)
			throws SQLException;


	/**
	 * Returns true if a matching record can be found.
	 */
	public boolean canFind(String table, String where)
			throws SQLException;


 // UPDATE METHODS ////////////////////////////////////////////////

	/**
	 * Makes an insertion using an SQL string.
	 * @param insertSql the SQL insert string
	 */
	public void insert(String insertSql)
			throws SQLException;


	/**
	 * Inserts multiple records into a given table.
	 * @param table the table in which to insert
	 * @param records Must be a List of Maps (hashes).
	 */
	public void insert(String table, List records)
			throws SQLException;


	/**
	 * Makes an update using an SQL string.
	 * @return the number of updated records
	 */
	public int update(String updateSql)
			throws SQLException;




 // UTILITY METHODS //////////////////////////////////////////////

	/**
	 *
	 */
///** 2/7/01.  I don't think this fits anymore
	public boolean execute(String sql)
			throws SQLException;
//*/


	/**
	 * Returns the attribute (column) names of a table.
	 * @param table the table 
	 * @return a list of column names
	 */
	public List getColumnNames(String table)
			throws SQLException;


	/**
	 * Returns the current database connection.
	 * @return the java.sql.Connection 
	 */
///** 2/7/01.  I don't think this fits anymore
	public Connection getConnection();
//*/

	/**
	 * Return connection state.
	 */
	public boolean isClosed() throws SQLException;

}
