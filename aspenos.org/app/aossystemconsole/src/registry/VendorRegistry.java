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
public class VendorRegistry extends SysConsoleRegistry 
		implements IVendorRegistry {

	public VendorRegistry() {
	}

	public VendorRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Get a vendor by its display name.
	 */
	public VendorDef getVendorByDisplayName(String name)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM vendor 
		//  WHERE  display_name='name'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "vendor";
		StringBuffer where = new StringBuffer("display_name='")
			.append(name).append("'");

		HashMap hash = (HashMap)_db.selectFirstAsHash(attribs, from, where.toString());

		VendorDef vendor = new VendorDef(hash);
		Integer id = (Integer)hash.get("vendor_id");
		vendor.setId(id.toString());

		return vendor;
	}


	/** 
	 * Get a vendor by its system name.
	 */
	public VendorDef getVendorBySystemName(String name)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM vendor 
		//  WHERE  system_name='name'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "vendor";
		StringBuffer where = new StringBuffer("system_name='")
			.append(name).append("'");

		HashMap hash = (HashMap)_db.selectFirstAsHash(attribs, from, where.toString());

		VendorDef vendor = new VendorDef(hash);
		Integer id = (Integer)hash.get("vendor_id");
		vendor.setId(id.toString());

		return vendor;
	}


	/** 
	 * Get a vendor by its display name.
	 */
	public VendorDef getVendorById(String vid)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM vendor 
		//  WHERE  vendor_id=vid
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "vendor";
		StringBuffer where = new StringBuffer("vendor_id=")
			.append(vid);

		HashMap hash = (HashMap)_db.selectFirstAsHash(attribs, from, where.toString());

		VendorDef vendor = new VendorDef(hash);
		Integer id = (Integer)hash.get("vendor_id");
		vendor.setId(id.toString());

		return vendor;
	}



	/** 
	 * Get a registry group by its key.
	 */
	public RegGroupDef getRGByKey(String key)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM reggrp 
		//  WHERE key='key'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "reggrp";
		StringBuffer where = new StringBuffer("key='")
			.append(key).append("'");

		HashMap hash = (HashMap)_db.selectFirstAsHash(attribs, from, where.toString());

		RegGroupDef rg = null;
		if (hash.size() > 0) { 
			rg = new RegGroupDef(hash);
			Integer id = (Integer)hash.get("reggrp_id");
			rg.setId(id.toString());
		}

		return rg;
	}


	/** 
	 * Get a registry group by its key.
	 */
	public RegGroupDef getRGById(String rgid)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM reggrp 
		//  WHERE reggrp_id='rgid'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "reggrp";
		StringBuffer where = new StringBuffer("reggrp_id=")
			.append(rgid);

		HashMap hash = (HashMap)_db
			.selectFirstAsHash(attribs, from, where.toString());

		RegGroupDef rg = null;
		if (hash.size() > 0) { 
			rg = new RegGroupDef(hash);
			Integer id = (Integer)hash.get("reggrp_id");
			rg.setId(id.toString());
		}

		return rg;
	}


	/** 
	 * Get a registry group by its name and a vendor system name.
	 */
	public RegGroupDef getRGByName(String reggrpName, String vendorSysName)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM reggrp 
		//  WHERE reggrp_name='reggrpName' 
		//  AND vendor_name='vendorSysName'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "reggrp";
		StringBuffer where = new StringBuffer("vendor_name='")
			.append(vendorSysName).append("' AND reggrp_name='")
			.append(reggrpName).append("'");

		HashMap hash = (HashMap)_db.selectFirstAsHash(attribs, from, where.toString());

		RegGroupDef rg = null;
		if (hash.size() > 0) { 
			rg = new RegGroupDef(hash);
			Integer id = (Integer)hash.get("reggrp_id");
			rg.setId(id.toString());
		}

		return rg;
	}


	/** 
	 * Get all registry groups by their vendor ID.
	 */
	public RegGroupDefs getAllRGByVendor(String vendorSysName)
			throws SQLException, Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM reggrp 
		//  WHERE vendor_name='vendorSysName'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "reggrp";
		StringBuffer where = new StringBuffer("vendor_name='")
			.append(vendorSysName).append("'");

		List l = _db.selectAsHash(attribs, from, where.toString());

		return new RegGroupDefs(l);
	}




	/** 
	 * Get the next available registry group ID.
	 */
	public Long getNextRGId() throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT MAX(reggrp_id) FROM reggrp; 
		/////////////////////////////////////////////////////////////

		String attribs = "MAX(reggrp_id)";
		String from = "reggrp";

		Integer intv = (Integer)_db.selectFirstAttrib(attribs, from, "");
		return new Long(intv.intValue() + 1);
	}
	

	/** 
	 * Store a registry group.
	 */
	public void storeRG(RegGroupDef rg) throws SQLException {

		String insert = "INSERT INTO reggrp ";
		String fandv = rg.getSqlFieldsAndValues();
		String tmp = insert + fandv;
		_db.insert(tmp);
	}

}
