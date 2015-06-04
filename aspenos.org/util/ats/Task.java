package org.aspenos.util.ats;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;

/**
 * 
 */
public abstract class Task {


	public static final String DB_POOL_TASK_ID = 
		"TASK_HANDLE";

	private Map _props=null;
	private LoggerWrapper _lw=null;


	public abstract void handleTask();

	public void setProperties(Map props) {
		if (_props != null)
			logDebugMsg("Task.setProps: overwriting original props");

		_props = props;
	}

	public Map getProperties() {
		return _props;
	}

	public Object getProperty(String key) {
		if (_props == null) return null;
		return _props.get(key);
	}

	public String getString(String key) {
		return (String)getProperty(key);
	}

	public void setLogger(LoggerWrapper lw) {
		_lw = lw;
	}


	/**
	 *  Tasks need to use this method to get registries
	 *  so that automatic DB connection pooling will 
	 *  take place.
	 */
	public IRegistry getRegistry(String bundleName, String regName) {

		IRegistry reg = null;
		try {
			// get the reg bundle
			Map regBundles = (Map)getProperty("reg_bundles");
			RegistryBundle bundle = (RegistryBundle)
				regBundles.get(bundleName);

			if (bundle == null) {
				throw new NullPointerException(
						"No registry bundle named " + bundleName);
			}

			// get the registry by name
			reg = bundle.getRegistry(regName); 

			// check out a new DB connection
			String dbId = bundle.getDbId(regName);
			DbPersistence conn = bundle.getDbConn(getThreadKey(), dbId);

			/*
			logDebugMsg("Task.getRegistry: Checked out DB conn\n\tREGISTRY: " + 
					regName +
					"\n\tBUNDLE: " + bundleName + 
					"\n\tDB ID: " + dbId);
			*/

			// set the registry's DB connection to the 
			// checked out conn.
			reg.setDbConn(conn);

		} catch (SQLException sex) {
			String msg = "Unable to get registry.";
			reg = null;
		}

		return reg;
	}

	/**
	 *
	 */
	public void returnDbConnections() {

		if (_props == null) {
			logDebugMsg("Task.returnDbConnections: no props, no DBs");
			return;
		}

		RegistryBundle bundle;
		Map regBundles = (Map)getProperty("reg_bundles");

		if (regBundles == null) {
			logDebugMsg("Task.returnDbConnections: no regBundles");
			return;
		}

		Iterator bit = regBundles.values().iterator();
		while (bit.hasNext()) {
			bundle = (RegistryBundle)bit.next();
			bundle.returnAllDbConnections(getThreadKey());
		}
	}


	/**
	 * Returns a unique identifier for the thread
	 * that executes this Task.
	 */
	public String getThreadKey() {
		return Integer.toString(this.hashCode());
	}


	/**
	 *
	 */
	public TemplateLoader getAppTemplateLoader(String app) {
		Map tls = (Map)getProperty("template_loaders");
		if (tls == null) {
			logDebugMsg("ATS.Task: no template loaders!");
			logErr("ATS.Task: no template loaders!");
		}
		TemplateLoader tl = (TemplateLoader)tls.get(app);
		return tl;
	}

	protected void loadProps(String propsPath) throws IOException {
		PropLoader pl = new PropLoader();
		pl.load(propsPath);
		Map m = pl.getProperties();
		if (m != null)  _props.putAll(m);
	}

	
	//////////// LOGGING ////////////////////////////
	public LoggerWrapper getLogger() {
		return _lw;
	}

	public void logErr(String msg) {
		msg = getThreadKey() + "::  " + msg;
		LoggerWrapper lw = getLogger();
		if (lw != null)  lw.logErr(msg);
	}

	public void logErr(String msg, Exception ex) {
		msg = getThreadKey() + "::  " + msg;
		LoggerWrapper lw = getLogger();
		if (lw != null)  lw.logErr(msg, ex);
	}

	public void logMsg(String msg) {
		msg = getThreadKey() + "::  " + msg;
		LoggerWrapper lw = getLogger();
		if (lw != null)  lw.logMsg(msg);
	}

	public void logDebugMsg(String msg, Exception ex) {
		msg = getThreadKey() + "::  " + msg;
		LoggerWrapper lw = getLogger();
		if (lw != null)  lw.logDebugMsg(msg, ex);
	}

	public void logDebugMsg(String msg) {
		msg = getThreadKey() + "::  " + msg;
		LoggerWrapper lw = getLogger();
		if (lw != null)  lw.logDebugMsg(msg);
	}


}
