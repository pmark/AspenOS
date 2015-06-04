package org.aspenos.util;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.logging.*;
import org.aspenos.db.*;

/**
 *
 */
public class RegistryInit {

	private DbConnectionPool _dbPool;
	private static HashMap _dbPools;
	private LoggerWrapper _lw;
	private RegistryFactory _regFactory;
	//private DbPersistence _dbConn;


	// Init Methods ==========================================================
	public RegistryInit(LoggerWrapper lw) {
		_dbPool = null;
		_regFactory = null;
		_dbPools = new HashMap();
		_lw = lw;
	}


	// Accessor Methods ======================================================
	/*
	public DbPersistence getPooledConn() {
		return _dbConn;
	}
	*/

	public DbConnectionPool getDbPool() {
		return _dbPool;
	}


	// Primary Methods =======================================================
	/**
	 * If pool of connections to the same DB has already
 	 * been initialized, use that pool.  
	 * Else create a new one.
	 */
	public void setupDbPool(String url, String user, String pwd, 
			String driver, int initial, int max)
			throws SQLException, ClassNotFoundException {

		if (_dbPools.containsKey(url)) {
			_dbPool = (DbConnectionPool)_dbPools.get(url);
			_lw.logDebugMsg("Reusing pool for " + url);
		} else {
			_dbPool = new DbConnectionPool(_lw, url, user, 
					pwd, driver, initial, max);
			_dbPools.put(url, _dbPool);
			_lw.logDebugMsg("Creating pool for " + url);
		}

		// this needs to go away SOON
		//_dbConn = _dbPool.getConnection();
	}


	/**
	 *
	 */
	public RegistryBundle updateRegBundle(HashMap regMap, RegistryBundle rb,
			String dbId)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		if (rb == null) {
			_lw.logDebugMsg("RI:  Creating registry bundle");
			rb = new RegistryBundle();
		}

		_lw.logDebugMsg("RI:  Updating registry bundle");

		// If no RegistryFactory has been set, use the default.
		if (_regFactory == null)
			_regFactory = new RegistryFactory();

		for (Iterator it=regMap.keySet().iterator(); it.hasNext();) {

			String regName = (String)it.next();
			String regClass = (String)regMap.get(regName);
			IRegistry reg = _regFactory.createRegistry(regClass);

			// 2/6/01  Registries should not get DB connections until
			// they need them!  Unfortunately, there is not yet a way
			// to automatically return a used connection to the pool
			// when a registry is done with it.
			//
			// Registries are put into a bundle at APP init time 
			// and are retrieved through the bundle upon demand.
			// They are retrieved all over the place, including 
			// during event handling and app init.  I would like to
			// call Registry.setDbConn() when RegistryBundle.getRegistry()
			// is called so that I could give a pooled connection to the 
			// returned registry.
			// 
			// The problem with giving registries a newly checked out
			// connection at registry retrieval time is that there is
			// no guarantee that the connection will be returned to the
			// pool.  It should not be the registry's responsibility to
			// put back the connection either.
			//
			// What is the solution?  Putting the connection back must 
			// be the app's responsibility, unless the EHS can somehow
			// do it.
			//
			// Hmmmm.  All conns used by registries used by event handlers
			// should be put back in the EHS by calling EventHandlerParent's
			// returnDbConnections() method.
			//
			// 2/21/01 The problem documented above on 2/6 was fixed on 2/6.
			// Today I added functionality for the sharing of DB pools
			// between apps that use the same DB.
			//
			//reg.setDbConn(_dbConn); 	// 2/6 - wish I could nix it

			_lw.logDebugMsg("RI:  Setting registry:\n\t" + 
					regName + " = " + regClass);

			rb.setRegistry(regName, reg, dbId);
		}

		return rb;
	}


	/**
	 * Gets the registry name and class name for any and 
	 * all app registries used by this application.  
	 * An app registry definition in the properties 
	 * file must look this:
	 * <br><br>
	 *    app.registry.name.NAME_OF_APP_REGISTRY=REGISTRY_CLASS
	 * <br><br>
	 * ...where the words in caps are the actual registry info.
	 */
	public HashMap getAppRegClassMap(Properties props, String dbId) {

		HashMap classMap = new HashMap();
		List regNameList = new ArrayList();

		// Get all of the registry names that use this dbId
		Enumeration keys = props.propertyNames();
		String regName, regClass;
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith("app.registry." + dbId)) {
				regName = key.substring(("app.registry." + dbId).length() + 1);
				regNameList.add(regName);
				_lw.logDebugMsg("Found registry for DB '" + dbId + "':  " +
						regName);
			}
		}

		// Get all of this app's registries
		String tmp;
		Iterator nameIt = regNameList.iterator();
		while (nameIt.hasNext()) {
			regName = (String)nameIt.next();
			regClass = props.getProperty("app.registry.name." + regName);
			classMap.put(regName, regClass);
		}

		return classMap;
	}






	/**
	 * DB and registry info is specified in the
	 * given properties file.
	 */
	public RegistryBundle initRegistryBundle(Properties props) 
			throws ClassNotFoundException, SQLException, 
			InstantiationException, IllegalAccessException {

		RegistryBundle regBundle = null;

		ArrayList dbIdList = getDbIdList(props);
		Iterator idIt = dbIdList.iterator();

		while (idIt.hasNext()) {

			String dbId = (String)idIt.next();

			_lw.logDebugMsg("Setting up the app database:  " + dbId);

			String regHost, regDsn, regURL, regUser, regPwd;
			String regIC, regMC, regDriver, regProtocol;
			
			// get the properties
			regUser = props.getProperty("app.db.user." + dbId); 
			regPwd = props.getProperty("app.db.pwd." + dbId); 
			regIC = props.getProperty("app.db.initialconns." + dbId);
			regMC = props.getProperty("app.db.maxconns." + dbId); 
			regDriver = props.getProperty("app.db.driver." + dbId); 
			regURL = props.getProperty("app.db.url." + dbId); 

			// Check for the alternate DB URL form
			// with the host, dsn and protocol separated.
			if (regURL == null) {
				regHost = props.getProperty("app.db.host." + dbId); 
				regDsn = props.getProperty("app.db.dsn." + dbId); 
				regProtocol = props.getProperty("app.db.protocol." + dbId); 

				StringBuffer dbURL = new StringBuffer();
				dbURL.append(regProtocol);
				if (!dbURL.toString().endsWith(":"))
					dbURL.append(":");
				dbURL.append("//")
					.append(regHost)
					.append("/")
					.append(regDsn);

				regURL = dbURL.toString();
			}


			// Get this app's DB pool.  If another app has already 
			// initialized a pool of connections to the same DB,
			// use that pool.  RegistryInit takes care of that.
			setupDbPool(
					regURL, 
					regUser, 
					regPwd, 
					regDriver, 
					Integer.parseInt(regIC), 
					Integer.parseInt(regMC));

			HashMap classMap = getAppRegClassMap(props, dbId);
			regBundle = updateRegBundle(classMap, regBundle, dbId);

			// 2/6/01  to enable pooling, the app reg bundle should have
			// its DbConnectionPool set here instead of one connection.
			// That way, when the app reg's DB conn is asked for later
			// it can call dbPool.getConnection() instead of just
			// returning the one and only connection for the app reg.
			/////////////////////////////////////////////////////////////
			// This is the old way:
			// Every reg was given only one DB connection, which
			// did not use DB pooling at all!
			//regBundle.setDbConn(dbId, getPooledConn());
			/////////////////////////////////////////////////////////////
			// This is the new way:
			// Every reg in the app reg bundle has its own conn pool.
			regBundle.setDbPool(dbId, _dbPool);
			/////////////////////////////////////////////////////////////

		} // end while list of DB IDs

		return regBundle;
	}


	/**
	 * Gets the property file's representation of a
	 * a database ID for all databases used by this
	 * application.  The DB IDs are just used to 
	 * associate other database properties in the 
	 * props file with a specific database.
	 */
	private ArrayList getDbIdList(Properties props) {

		ArrayList idList = new ArrayList();

		Enumeration keys = props.propertyNames();
		String dbId;
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith("app.db.id.")) {
				dbId = key.substring("app.db.id.".length());
				idList.add(dbId);
			}
		}
		return idList;
	}


	public void setRegistryFactory(RegistryFactory regFactory) {
		_regFactory = regFactory;
	}
}


