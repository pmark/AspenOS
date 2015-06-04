package org.aspenos.app.aossystemconsole.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aossystemconsole.defs.*;

/**
 *
 */
public class AppRegistry extends SysConsoleRegistry implements IAppRegistry {

	public AppRegistry() {
	}

	public AppRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Get all vendors that have apps installed in the system.
	 */
	public VendorDefs getInstalledAppVendors()
			throws SQLException, Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM vendor v
		//  WHERE EXISTS 
		//     (SELECT * FROM app a 
		//     WHERE a.vendor_id=v.vendor_id) 
		/////////////////////////////////////////////////////////////

		String firstAttribs = "*";
		String firstFrom = "vendor v";
		String where = 
			"EXISTS (SELECT * FROM app WHERE app.vendor_id=v.vendor_id)";

		List list = _db.selectAsHash(firstAttribs, firstFrom, where);

		return new VendorDefs(list);
	}


	/** 
	 * Get all of a vendor's installed apps.
	 */
	public AppDefs getInstalledApps(String vid)
			throws SQLException, Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM app,vendor v
		//  WHERE v.vendor_id=app.vendor_id
		//  AND v.vendor_id=101
		/////////////////////////////////////////////////////////////

		String attribs = "app.*";
		String from = "app,vendor v";
		StringBuffer where = new StringBuffer("v.vendor_id=")
			.append(vid)
			.append(" AND v.vendor_id=app.vendor_id");

		List list = _db.selectAsHash(attribs, from, where.toString());

		return new AppDefs(list);
	}


	/** 
	 * Get an app.
	 */
	public AppDef getAppBySysName(String sysName)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM app
		//  WHERE system_name=THE GIVEN NAME
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "app";
		StringBuffer where = new StringBuffer("system_name='")
			.append(sysName).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		return new AppDef(hash);
	}


	/** 
	 * Get an app.
	 */
	public AppDef getAppById(IdDef appId)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM app
		//  WHERE app_id=THE GIVEN ID
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "app";
		StringBuffer where = new StringBuffer("app_id=")
			.append(appId.getId());

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		return new AppDef(hash);
	}


	/** 
	 * Get an app's display name given its ID.
	 */
	public String getAppDisplayName(IdDef appId)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT display_name FROM app
		//  WHERE app_id=THE GIVEN ID
		/////////////////////////////////////////////////////////////

		String attribs = "display_name";
		String from = "app";
		StringBuffer where = new StringBuffer("app_id=")
			.append(appId.getId());

		String displayName = (String)
			_db.selectFirstAttrib(attribs, from, where.toString());

		return displayName;
	}


	/** 
	 * Get an app's registry group key given an app ID.
	 */
	public RegGroupDef getAppRG(IdDef appId)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT rg.* FROM app,reggrp rg
		//  WHERE app_id=THE GIVEN ID
		//  AND app.reggrp_id=rg.reggrp_id
		/////////////////////////////////////////////////////////////

		String attribs = "rg.*";
		String from = "app,reggrp rg";
		StringBuffer where = new StringBuffer("app_id=")
			.append(appId.getId())
			.append(" AND app.reggrp_id=rg.reggrp_id");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		return new RegGroupDef(hash);
	}


}


