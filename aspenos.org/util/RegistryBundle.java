package org.aspenos.util;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.db.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;

/**
 * RegistryBundle objects organize a collection of
 * IRegistry implementations.  Each registry is
 * associated with a database ID, as specified in
 * an application's config file.  The registry
 * bundle knows which DB connection pool to use
 * for the individual registries, so the best way
 * to get DbPersistence object for a registry is
 * to call this class' getDbConn() method.
 *
 * Any use of a session ID (sid) is so that automatic
 * DB connection pooling can occur.
 *
 */
public class RegistryBundle {

	private HashMap _regToClassMap;
	private HashMap _regToDbIdMap; 		// 2/6 addition
	private HashMap _dbPools; 			// 2/6 addition
	//private HashMap _dbConns;			// 2/6 nix

	// A map of maps used in auto conn. pooling
	private HashMap _dbIdMaps = new HashMap();	// 2/6 addition


	public RegistryBundle() {
		_regToClassMap = new HashMap();
		_regToDbIdMap = new HashMap();
		_dbPools = new HashMap();
	}

	public void setRegistry(String regName, IRegistry reg, String dbId) {
		_regToClassMap.put(regName, reg);
		_regToDbIdMap.put(regName, dbId); 
	}

	public IRegistry getRegistry(String regName) {
		return (IRegistry)_regToClassMap.get(regName);
	}

	public String getDbId(String regName) {
		return (String)_regToDbIdMap.get(regName);
	}

	/**
	 * 2/6/01 change.  
	 * This is obsolete.
	 */
	//public void setDbConn(String dbId, DbPersistence dbConn) {
	//	_dbConns.put(dbId, dbConn);
	//}

	/**
	 * 2/6/01 change.
	 * The sid was added for auto connection pooling.
	 *
	 */
	public DbPersistence getDbConn(String sid, String dbId) 
			throws SQLException {

//System.out.println("\nRB.getDbConn: #mappings: " + _dbIdMaps.size());
//System.out.println("RB.getDbConn: sid/dbid: " + sid + "/" + dbId);

		DbPersistence conn = null;

		// _dbIdMaps is a Map of Maps
		Map idToConnListMap = (Map)_dbIdMaps.get(sid);

		if (idToConnListMap == null)
			idToConnListMap = new HashMap();

		// idToConnListMap is a Map of Lists
		List connList = (List)idToConnListMap.get(dbId);

		if (connList == null)
			connList = new ArrayList();

		if (connList.isEmpty()) {
			connList = new ArrayList();

			// get a new connection from the pool
			DbConnectionPool pool = (DbConnectionPool)
				_dbPools.get(dbId);
			conn = (DbPersistence)pool.getConnection();

			// add it to this DB's conn list for this session
			connList.add(conn);
			idToConnListMap.put(dbId, connList);
			_dbIdMaps.put(sid, idToConnListMap);

		} else {

//System.out.println("RB.getDbConn: Using existing conn");

			// Get the existing connection to this DB
			// for this session.  This means that 
			// multiple connections will not be checked
			// out for the same DB for the same session.
			conn = (DbPersistence)connList.get(0);
		}

//System.out.println("RB.getDbConn: ending with " + _dbIdMaps.size());
		return conn;
	}

	/**
	 * New as of 2/6/01.
	 * 
	 */
	public void setDbPool(String dbId, IConnectionPool pool) {
		_dbPools.put(dbId, pool);
	}

	/**
	 * New as of 2/6/01.
	 * 
	 */
	public IConnectionPool getDbPool(String dbId) {
		return (IConnectionPool )_dbPools.get(dbId);
	}


	/**
	 * New as of 2/6/01.
	 * Returns all of this session's connections 
	 * to the correct pools for each DB.
	 */
	public void returnAllDbConnections(String sid) {

		//System.out.println("\nRB.returnAll: #mappings: " + _dbIdMaps.size());
		//System.out.println("RB.returnAll: sid: " + sid);

		// _dbIdMaps is a Map of Maps
		Map idToConnListMap = (Map)_dbIdMaps.get(sid);
		if (idToConnListMap == null) {
			//System.out.println("RB.returnAll: no sessions");
			return;
		}

		// idToConnListMap is a Map of Lists
		String dbId;
		Iterator dbit = idToConnListMap.keySet().iterator();
		while (dbit.hasNext()) {
			dbId = (String)dbit.next();
			//System.out.println("RB.returnAll: dbId: " + dbId);

			List connList = (List)idToConnListMap.get(dbId);
			if (connList == null)  continue;

			//System.out.println("RB.returnAll: #conns: " + connList.size());

			DbPersistence conn = null;
			Iterator cit = connList.iterator();
			while (cit.hasNext()) {
				conn = (DbPersistence)cit.next();
				this.getDbPool(dbId).returnConnection(conn);
				//System.out.println("RB.returnAll: returned conn");
			}

			//idToConnListMap.remove(dbId);
		}

		idToConnListMap = null;
		_dbIdMaps.remove(sid);
	}


	/**
	 * New as of 2/6/01.
	 * Returns all of this session's connections to the pool
	 * for the given database ID.
	 */
	public void returnDbConnections(String sid, String dbId) {

		// _dbIdMaps is a Map of Maps
		Map idToConnListMap = (Map)_dbIdMaps.get(sid);
		if (idToConnListMap == null)  return;

		// idToConnListMap is a Map of Lists
		List connList = (List)idToConnListMap.get(dbId);
		if (connList == null)  return;

		DbPersistence conn = null;
		Iterator cit = connList.iterator();
		while (cit.hasNext()) {
			conn = (DbPersistence)cit.next();
			this.getDbPool(dbId).returnConnection(conn);
		}

		idToConnListMap.remove(dbId);

		// Only put back the updated connection list 
		// if there are connections out from other
		// databases.
		if (idToConnListMap.size() == 0)
			_dbIdMaps.remove(sid);
		else
			_dbIdMaps.put(sid, idToConnListMap);
	}

}


