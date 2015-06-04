package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;
import org.aspenos.db.*;

/**
 *
 */
public class CSRegistry extends Registry 
		implements ICSRegistry, ICSConstants {

	protected String _regGroup = "";
	protected String _regGroupSeparator = "_";

	/**
	 * Sets the registry group name.
	 */
	public void setRegistryGroupName(String regGroupName) {
		_regGroup = regGroupName;
	}


	/**
	 * Retrieves the registry group name.
	 */
	public String getRegistryGroupName() {
		return _regGroup;
	}

	public void setDbConn(DbPersistence db) {
		_db = db;
	}

	public DbPersistence getDbConn() {
		return _db;
	}

}


