package org.aspenos.app.aossystemconsole.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.util.*;
import org.aspenos.app.aossystemconsole.defs.*;

/**
 *
 */
public interface IAppRegistry extends ISysConsoleRegistry {
	
	/** 
	 * Get all vendors that have apps installed in the system.
	 */
	public VendorDefs getInstalledAppVendors()
			throws SQLException, Exception;

	public AppDefs getInstalledApps(String vid)
			throws SQLException, Exception;

}
