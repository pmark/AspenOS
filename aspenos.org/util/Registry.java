package org.aspenos.util;

import org.aspenos.db.*;
import org.aspenos.logging.*;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;

/**
 *
 */
public class Registry implements IRegistry {

	protected DbPersistence _db = null;
	protected LoggerWrapper _lw = null;
	protected boolean _prevAutoCommit = true;
	protected HashMap _properties = null;

	public Registry() {
	}

	public Registry(DbPersistence db) {
		_db = db;
	}

	public void setDbConn(DbPersistence db) {
		_db = db;
	}

	public DbPersistence getDbConn() {
		return _db;
	}

	public void setLogger(LoggerWrapper lw) {
		_lw = lw;
	}

	public LoggerWrapper getLogger() {
		return _lw;
	}

	public void debug(String msg) {
		if (_lw == null) {
			StringBuffer sb = new StringBuffer("[")
				.append(DateTool.getDateTime())
				.append("]  ")
				.append(msg);
			//System.err.println(sb.toString());
			System.out.println(sb.toString());
		} else {
			_lw.logDebugMsg(msg);
		}
	}


	///////////////////////////////////////////////
	public void rollback() throws SQLException {
		_db.getConnection().rollback();
	}

	public void commit() throws SQLException {
		_db.getConnection().commit();
	}

	public boolean getAutoCommit() throws SQLException {
		return _db.getConnection().getAutoCommit();
	}

	public void setAutoCommit(boolean b) throws SQLException {
		// save the current AC value
		_prevAutoCommit = getAutoCommit();

		_db.getConnection().setAutoCommit(b);
	}

	public void resetAutoCommit() throws SQLException {
		_db.getConnection().setAutoCommit(_prevAutoCommit);
	}

	///////////////////////////////////////////////
	public Object getProperty(String key) {
		return _properties.get(key);
	}

	public HashMap getProperties() {
		return _properties;
	}

	public void setProperty(String key, Object o) {
		if (_properties == null)
			_properties = new HashMap();
		_properties.put(key, o);
	}


	/**
	 * This is a dummy (not abstract) method that does nothing.
	 * Children registries must override this method in order
	 * to import custom XML.
	 */
	public void importXML(String xml, int operation)
				throws IOException, SAXException, SQLException {
		if (_lw != null)
			_lw.logDebugMsg("Registry.importXML(): not implemented!");
	}


	/**
	 * Reads the file and passes its contents
	 * as a String to the other Registry.importXML().
	 */
	public void importXML(File xmlFile, int operation) 
			throws IOException, SAXException, SQLException {

		BufferedReader br = new BufferedReader(
				new FileReader(xmlFile));
		StringBuffer xmlString = new StringBuffer(512);
		String line;

		while ((line=br.readLine()) != null) {
			xmlString.append(line);
		}

		importXML(xmlString.toString(), operation);
	}

}





