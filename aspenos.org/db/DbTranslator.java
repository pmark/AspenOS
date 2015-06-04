/** -------------------------------------
	FILE: DbTranslator.java
	AUTHOR: P. Mark Anderson
	DATE: 8/2000
    ------------------------------------- */

package org.aspenos.db;

import java.util.*;
import java.sql.*;

import org.aspenos.logging.*;
//import org.aspenos.app.aoscontentserver.util.*;

/**
 * DbTranslator is an implementation of DbPersistence
 * that provides some additional logging methods.
 *
 * @author P. Mark Anderson
 * @version 1.0
 */
public class DbTranslator implements DbPersistence {

	//// FIELDS ////////////////////////////////////
	protected Connection _conn			= null;

	private String _username 			= null;
	private String _password 			= null;
	private String _dsn 				= null;
	private SqlLog _sqlUpdateLog 		= null;
	private Properties _props 			= null;
	private boolean _doSqlUpdateLogging = false; 


	//// CONSTRUCTORS ///////////////////////////////
	public DbTranslator() { 
	}


	public DbTranslator(String dsn, String username, String password) 
			throws SQLException {
		open(dsn, username, password);
	}


	public DbTranslator(String dsn, Properties props) 
			throws SQLException {
		open(dsn, props);
	}



	//// CONNECTION /////////////////////////////////
	/**
	 * Open a connection to a database.
	 */
	public void open() throws SQLException {
		if (!isClosed())
			close();

		if (_props == null)
			_conn = DriverManager.getConnection(_dsn, _username, _password);
		else
			_conn = DriverManager.getConnection(_dsn, _props);
	}

	
	/**
	 * Open a connection to a database.
	 */
	public void open(String dsn, String username, String password) 
			throws SQLException {
		if (!isClosed())
			close();

		_dsn		= dsn;
		_username	= username;
		_password	= password;
		_props		= null;

		_conn = DriverManager.getConnection(dsn, username, password);
	}


	/**
	 * Open a connection to a database.
	 */
	public void open(String dsn, Properties props) 
			throws SQLException {
		if (!isClosed())
			close();

		_dsn		= dsn;
		_username	= null;
		_password	= null;
		_props		= props;

		_conn = DriverManager.getConnection(dsn, props);
	}


	/**
	 * Closes the connection to the database.
	 */
	public void close() throws SQLException {
		if (_conn != null)
			_conn.close();
	}




	//// SELECTION ///////////////////////////////////
	/**
	 * Returns a matrix of results of a SQL SELECT statement.
	 * The matrix is represented as a List of nested 
	 * Lists.  The nested Lists are individual records.
	 * The type of the selected attributes in the List will be 
	 * the default Java object type corresponding to the 
	 * column's SQL type, following the mapping for built-in 
	 * types specified in the JDBC spec.  See the documentation
	 * for java.sql.ResultSet's getObject(int columnIndex) for
	 * more details about the data types.
	 * 
	 */
	public List select(String attrib, String table, String where) 
			throws SQLException { 
		String query = buildSelect(attrib, table, where);
		return query( query );
	}


	/**
	 * Retrieves the first matching record as a List of attributes. 
	 * The attribute data types are just like those in select().
	 * @see select()
	 * @return empty list if there is no matching record; 
	 * else returns the record as a List of attributes
	 */
	public List selectFirst(String attrib, String table, String where) 
			throws SQLException {
		String query = buildSelect(attrib, table, where);

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet find_rs = stmt.executeQuery(query);
		ResultSetMetaData rsmd = find_rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		ArrayList row = new ArrayList();

		if (find_rs.next()) {
			row = new ArrayList();

			for(int i=0; i < columnCount; i++) {
				Object value = find_rs.getObject(1+i);
				row.add(value);
			}
		}

		find_rs.close();
		stmt.close();

		return row;
	}


	/**
	 * Returns a matrix of results of a SELECT statement.
	 * The matrix is represented as a List of HashMaps,
	 * and each HashMap is a record returned from the query.
	 * The keys of the map are the attribute names.
	 * @return empty List is no match, else a list of Maps
	 */
	public List selectAsHash(String attrib, String table, String where) 
			throws SQLException { 

		ArrayList results = new ArrayList();      
		String query = buildSelect(attrib, table, where);

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet find_rs = stmt.executeQuery(query);
		ResultSetMetaData rsmd = find_rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		while(find_rs.next()) {
			HashMap row = new HashMap();

			for(int i=0; i < columnCount; i++) {
				String col_name = rsmd.getColumnLabel(1+i);
				Object value = find_rs.getObject(1+i);

				if (col_name != null) {
					if (value == null) {
						row.put(col_name, "null");
					} else {
						row.put(col_name, value);
					}
				}
			}

			results.add(row);
		}

		find_rs.close();
		stmt.close();

		return results;
	}


	/**
	 * Retrieves the first matching record as a HashMap of the 
	 * column-value pairs.
	 * @return empty Map if there is no matching record; 
	 * else a column-value mapping
	 */
	public Map selectFirstAsHash(String attrib, String table, String where) 
			throws SQLException {
		String query = buildSelect(attrib, table, where);

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet find_rs = stmt.executeQuery(query);
		ResultSetMetaData rsmd = find_rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		HashMap row = new HashMap();

		if (find_rs.next()) {
			row = new HashMap();

			for(int i=0; i < columnCount; i++) {
				String col_name = rsmd.getColumnLabel(1+i);
				Object value = find_rs.getObject(1+i);
				row.put(col_name, value);
			}
		}

		find_rs.close();
		stmt.close();

		return row;
	}


	/** 
	 * Executes a query without a WHERE clause on a table.
	 * Returns the results just like select() does. 
	 * @see select()
	 */
	public List selectAll(String table) throws SQLException {
		String query = "SELECT * FROM " + table;
		return query( query );
	}


	/**
	 * Returns a List of the first (or only) selected attribute 
	 * of all matching records.  Useful for retrieving a list 
	 * of values for one single attribute.
	 *
	 * @return a list of this attribute's matching values for the query,
	 * or an empty (not null) List if there are none.
	 */
	public List selectAttrib(String attrib, String table, String where) 
			throws SQLException {
		String query = buildSelect(attrib, table, where);

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet find_rs = stmt.executeQuery(query);

		ArrayList list = new ArrayList();

		while (find_rs.next()) {
			Object value = find_rs.getObject(1);
			list.add(value);
		}

		find_rs.close();
		stmt.close();

		return list;
	}


	/**
	 * Returns the first (or only) selected attribute of the first 
	 * record retrieved.  Useful for retrieving just one value
	 * at a time.
	 * @return the value retrieved, same data type as in select(),
	 * or null if there is no match.
	 * @see select()
	 */
	public Object selectFirstAttrib(String attrib, String table, String where) 
			throws SQLException {

		String query = buildSelect(attrib, table, where);

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();

		ResultSet find_rs = stmt.executeQuery(query);

		Object value = null;
		if (find_rs.next()) {
			value = find_rs.getObject(1);
		}

		find_rs.close();
		stmt.close();

		return value;
	}


	/**
	 * Calls Statement.executeQuery on the given query and returns 
	 * the results just like select() does. 
	 * @see select()
	 */
	public List query(String query) throws SQLException { 
		ArrayList column = new ArrayList();      

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		while(rs.next()) {
			ArrayList row = new ArrayList();

			// populate a tuple in the List
			for(int i=0; i < columnCount; i++) {

				Object o = rs.getObject(1+i);
				if (o == null)
					row.add("null");
				else
					row.add(o);
			}

			// add the record (row) to the set of records (column)
			column.add(row);
		}

		rs.close();
		stmt.close();

		return column;
	}


	/**
	 * Returns number of matching records
	 */
	public int howMany(String table, String where) 
		throws SQLException { 

		StringBuffer query = 
			new StringBuffer("SELECT * FROM ").append(table);

		if (where != null && where.length() > 0) {
			query.append(" WHERE ");
			query.append( where );      
		}

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet rs = stmt.executeQuery(query.toString());

		int count=0;
		while(rs.next()) { 
			count++; 
		}

		return count;
	}


	/**
	 * Returns true if at least one matching record is found.
	 */
	public boolean canFind(String table, String where) 
		throws SQLException { 

		StringBuffer query =
			new StringBuffer("SELECT * FROM ").append(table);

		if (where != null && where.length() > 0) {
			query.append(" WHERE ");
			query.append( where );      
		}

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		ResultSet rs = stmt.executeQuery(query.toString());

		return rs.next();
	}




	//// UPDATE & INSERT ////////////////////////////////////////
	/**
	 *
	 */
	public int update(String updateSql) throws SQLException { 

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		int rv = stmt.executeUpdate(updateSql);

		if (_doSqlUpdateLogging) {
			_sqlUpdateLog.addEntry(updateSql);
		}

		stmt.close();

		return rv;
	}


	/**
	 *
	 */
	public void insert(String insertSql) throws SQLException { 
		update(insertSql);
	}


	/**
	 * Given a table and a List of Maps, this method will
	 * insert key-value pairs into the DB.  MAKE SURE THAT
	 * THE KEY SET IS THE SAME IN EACH MAP!  The key set
	 * of the first Map in the list is used to build the SQL
	 * INSERT statement's column phrase.
	 *
	 * @param table the table in which to insert the records
	 * @param records Must be a List of Maps
	 */
	public void insert(String table, List records) 
			throws SQLException { 

		StringBuffer insert = new StringBuffer("INSERT INTO ")
			.append(table).append(" (");

		HashMap hash = (HashMap)records.get(0);

		boolean first = true;
		Iterator it = hash.keySet().iterator();

		// Get the column names first
		while (it.hasNext()) {
			if (first)
				first = false;
			else
				insert.append(",");

			insert.append((String)it.next());
		}

		insert.append(") VALUES ('");


		// Now set the values and insert them in the DB
		for (int i=0; i<records.size(); i++) {
			first = true;
			hash = (HashMap)records.get(i);
			StringBuffer values = new StringBuffer();

			it = hash.values().iterator();
			while (it.hasNext()) {
				if (first)
					first = false;
				else
					values.append("','");

				values.append((String)it.next());
			}

			StringBuffer finalInsert = new StringBuffer(insert.toString())
				.append(values.toString()).append("')");

			update(finalInsert.toString());
		}
	}






	//// UTILITY //////////////////////////////////////
	/**
	 * Returns the attributes (column names) of a table.
	 */
	public List getColumnNames(String table) 
			throws SQLException {
		ArrayList col = new ArrayList();                            

		if (isClosed())
			open();   

		DatabaseMetaData dbmd = _conn.getMetaData();
		ResultSet rs = dbmd.getColumns(null,null,table,"%");

		while(rs.next())
			col.add(rs.getString("COLUMN_NAME"));         

		rs.close();

		return col;
	}   


	/**
	 *
	 */
	public Connection getConnection() {
		return _conn;
	}


	/**
	 * Checks the state of the DB connection.
	 */
	public boolean isClosed() throws SQLException {
		if (_conn == null)
			return true;

		return _conn.isClosed();
	}



	//// EXTRA //////////////////////////////////////////
	/**
	 * 
	 */
	public void doSqlUpdateLogging(boolean b) {
		_doSqlUpdateLogging = b;
	}


	/**
	 * 
	 */
	public void doSqlUpdateLogging(boolean b, String path) {
		_doSqlUpdateLogging = b;
		setSqlUpdateLogPath(path);
	}


	/**
	 * 
	 */
	public void setSqlUpdateLogPath(String path) {
		if (path != null) {
			_sqlUpdateLog = new SqlLog(path);
		}
	}


	//// STATIC ////////////////////////////////////////

	/**
	 * Given a list of an attribute's values, this method
	 * will build a list to be used in the WHERE clause
	 * of a query.  For example, if the 'values' List 
	 * contains {5, 6, 7}, 'attrib' is "record_id",
	 * and 'op' is "OR", the returned string will look 
	 * exactly like this:
	 * <br><br><pre>
	 * record_id='5' OR record_id='6' OR record_id='7'
	 * </pre><br><br>
	 *
	 * @return a string with all of the attrib values
	 * in the given list separated by attrib name-value
	 * pairs and ops.
	 */
	public static String buildWhereStringList(
			List values, String attrib, String op, boolean useQuotes) {
			
		boolean first = true;
		Iterator it = values.iterator();
		StringBuffer tids = new StringBuffer();
		while (it.hasNext()) {

			if (first)	first = false;
			else 		tids.append(" ").append(op).append(" ");

			Object val = it.next();

			tids.append(" ")
				.append(attrib)
				.append("=");

			if (useQuotes)
				tids.append("'");

			tids.append(val.toString());

			if (useQuotes)
				tids.append("' ");

		}
		
		return tids.toString();
	}


	/*
	 * 
	 */
	public boolean execute(String sql)
			throws SQLException { 

		if (isClosed())
			open();   

		Statement stmt = _conn.createStatement();
		boolean b = stmt.execute(sql);
		stmt.close();

		return b;
	}



	/**
	 * Convenience version of makeSQLSafe.
	 * recursiveFix = true
	 * convertDblQuotes = false
	 */
	public static String makeSQLSafe(String orig) {
		return DbTranslator.makeSQLSafe(orig, true, false);
	}


	/**
	 * A recursive fix will delimit delimiters.
	 * convertDblQuotes just converts
	 * a double quote (") to a single quote (').
	 */
	public static String makeSQLSafe(String orig, 
			boolean recursiveFix, boolean convertDblQuotes) {

		if (convertDblQuotes)
			orig = orig.replace('\"', '\''); 

		String delims = "'\\\"";
		StringBuffer ret = new StringBuffer();
		int curPos=0;

		if (orig != null) {
			StringTokenizer st = new StringTokenizer(orig, delims, true);
			//if (st.countTokens() > 1) {
				while (st.hasMoreTokens()) {
					String tmp = st.nextToken();
					curPos += tmp.length()-1;

					if (stringContains(tmp, delims)) { 

						if (recursiveFix) {
							ret.append('\\');

						} else {
							if (orig.length() >= curPos+1) {
								// get the next char after the delim
								char ch = orig.charAt(curPos+1);

								// find out if it's a backslash
								if (ch != '\\')
									ret.append('\\');
							}
						}
					} 
					ret.append(tmp);
					//System.out.println("Appending: " + tmp);
					
					/*
					if (st.hasMoreTokens()) {
						String delim = st.nextToken();
						ret.append('\\').append(delim);
					}
					*/
				}
				/*
			} else {
				ret = new StringBuffer(orig);
			}
			*/
		}

		return ret.toString();
	}


	/**
	 * Removes special formatting that would make a string 
	 * SQL safe.  
	 * @param orig One field that may not be
	 *  safe to insert into a SQL table because of 
	 *  characters that SQL like the apostrophe (').
	 */
	public static String makeSQLUnsafe(String orig) {

		int pos=0;
		int delPos;
		int numDeletions=0;
		char nextChar;
		String delims = "'\\\"";
		StringBuffer ret = new StringBuffer(orig);

		// find a backslash
		while ((pos=orig.indexOf('\\',pos)) != -1) {
			// get the next char
			nextChar = orig.charAt(pos+1);
			//System.out.println("Found \\ at " + pos + ". nextChar is " + nextChar);

			// if next char is a delimiter
			if (delims.indexOf(nextChar) != -1) {
				delPos = pos - numDeletions;
				//System.out.println("deleting " + ret.charAt(delPos) + 
				//		" at editted pos " + delPos);
				ret.deleteCharAt(delPos);
				numDeletions++;
			}

			// make sure that the string is 
			// long enough for another search
			if (pos > orig.length()-2)
				break;
			else
				pos+=2;
		}

		return ret.toString();
	}

	/**
	 * Returns true if 'src' contains any of the characters
	 * in the string 'chars'.
	 */
	public static boolean stringContains(String src, String chars) {
		for (int i=0; i<chars.length(); i++) {
			char achar = chars.charAt(i);
			if (src.indexOf(achar) != -1)
				return true;
		}
		return false;
	}	



	//// PRIVATE ///////////////////////////////////////
	/**
	 * Since this is used all over the place, might as well
	 * make it its own method.
	 */
	private String buildSelect(String attrib, String table, String where) {

		StringBuffer query = new StringBuffer("SELECT ")
			.append(attrib).append(" FROM ").append(table);

		if (where != null && where.length() > 0) {
			query.append(" WHERE ").append(where);      
		}

		return query.toString();
	}

}


