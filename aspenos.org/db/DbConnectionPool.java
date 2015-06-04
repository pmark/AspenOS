package org.aspenos.db;

import java.sql.*;
import java.util.*;

import org.aspenos.logging.*;
import org.aspenos.exception.*;
//import org.aspenos.app.aoscontentserver.util.*;


public class DbConnectionPool implements IConnectionPool {

	private String _dbURL, _user, _pwd;
	private int _initialConnections=0;
	private int _maxConnections=5;
	private Hashtable _connections;
	private LoggerWrapper _lw;


	public DbConnectionPool(
			LoggerWrapper lw,
			String dbURL,
			String user,
			String pwd,
			String driverClassName,
			int initialConnections,
			int maxConnections)
			throws SQLException, ClassNotFoundException {

		_lw = lw;

		init(dbURL, user, pwd, 
				driverClassName,
				initialConnections, maxConnections);
	}


	public DbConnectionPool(
			String dbURL,
			String user,
			String pwd,
			String driverClassName,
			int initialConnections,
			int maxConnections)
			throws SQLException, ClassNotFoundException {

		init(dbURL, user, pwd, 
				driverClassName,
				initialConnections, maxConnections);

	}

	private void init(
			String dbURL,
			String user,
			String pwd,
			String driverClassName,
			int initialConnections,
			int maxConnections) 
			throws SQLException, ClassNotFoundException {

		_dbURL = dbURL;
		_user = user;
		_pwd = pwd;
		_maxConnections = maxConnections;
		_initialConnections = initialConnections;

		_connections = new Hashtable();

		if (_maxConnections < 1)
			_maxConnections = 5;

		//ServerInit.log("DBCP: Registering driver: " + driverClassName);
		if (_lw != null)
			_lw.logDebugMsg("DBCP: Registering driver: " + driverClassName);

		Class.forName(driverClassName);
		Properties p = System.getProperties();
		p.put("jdbc.drivers", driverClassName);

		//ServerInit.log("DBCP: Making initial connections");
		for (int i=0; i < _initialConnections; i++) {
			_connections.put(new DbTranslator(_dbURL, _user, _pwd), 
				Boolean.FALSE);
		}

		//ServerInit.log("DBCP: DONE creating DB connections to:" +
		//		"\n\tDSN =   " + _dbURL);
		if (_lw != null)
			_lw.logDebugMsg("DBCP: DONE creating DB connections to:" +
				"\n\tDSN =   " + _dbURL);
	}


	public DbPersistence getConnection() throws SQLException {
		DbPersistence con = null;
		Enumeration cons = _connections.keys();

		try {
			synchronized (_connections) {
				while (cons.hasMoreElements()) {
					con = (DbPersistence)cons.nextElement();

					Boolean b = (Boolean)_connections.get(con);
					if (b == Boolean.FALSE) {
						try { 
							// test connection
							con.getConnection().setAutoCommit(true);  
						} catch (SQLException e) {
							con = new DbTranslator(_dbURL, _user, _pwd);
						}

						_connections.put(con, Boolean.TRUE);
						_lw.logDebugMsg("DBCP: returning pooled conn");
						return con;
					} // End if
				}
			}

			// No free connections; make new one
			if (_connections.size() < _maxConnections) {
				_lw.logDebugMsg("DBCP: creating a new connection");
				con = new DbTranslator(_dbURL, _user, _pwd);
				_connections.put(con, Boolean.FALSE);
			} else {
				_lw.logDebugMsg("DBCP: no more connections available");
				return null;
			}

		} catch (Exception ex) {
			SQLException sex = new 
				SQLException(ex.toString());
			sex.fillInStackTrace();
			throw sex;
		}

		return getConnection();
	}


	public void returnConnection(DbPersistence returned) {
		DbPersistence con;
		Enumeration cons = _connections.keys();
		while (cons.hasMoreElements()) {
			con = (DbPersistence)cons.nextElement();
			if (con == returned) {
				_connections.put(con, Boolean.FALSE);
				break;
			}
		}
	}

}
