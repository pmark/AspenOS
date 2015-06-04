package org.aspenos.util;

import org.xml.sax.*;
import java.sql.*;
import java.util.*;
import java.io.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;

/**
 * Since every registry must implement IRegistry
 * or another interface that extends IRegistry, 
 * any registry can be used with powerful modules 
 * like org.aspenos.xml.XmlRegistryTransaction.
 *
 */
public interface IRegistry {

	public static final int XML_ADD = 0;
	public static final int XML_UPDATE = 1;
	public static final int XML_DELETE = 2;

	///////////////////////////////////////////////
	public void setDbConn(DbPersistence db);
	public DbPersistence getDbConn();

	///////////////////////////////////////////////
	public void rollback() throws SQLException;
	public void commit() throws SQLException;
	public void setAutoCommit(boolean b) throws SQLException;
	public boolean getAutoCommit() throws SQLException;

	public Object getProperty(String key);
	public HashMap getProperties();
	public void setProperty(String key, Object o);
	public void setLogger(LoggerWrapper lw);
	public LoggerWrapper getLogger();

	public void importXML(String xml, int operation)
				throws IOException, SAXException, SQLException;
	public void importXML(File xmlFile, int operation)
				throws IOException, SAXException, SQLException;

}
