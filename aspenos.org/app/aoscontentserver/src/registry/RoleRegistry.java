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
public class RoleRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public RoleRegistry() {
	}

	public RoleRegistry(DbPersistence db) {
		_db = db;
	}

	// Primary Methods =============================================

	/**
	 * Stores a set of roles.
	 */
	public void storeRoleDefs(RoleDefs defs)
			throws SQLException {

		StringBuffer insert = new StringBuffer("INSERT INTO role ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			RoleDef def = (RoleDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			_db.insert(tmp);
		}
	}

	/** 
	 * Returns all info about a particular Role.
	 *
	 * @param name the Role's name 
	 * @return the Role with this name 
	 */
	public RoleDef getRoleByName(String name) 
			throws SQLException {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= " * ";
		from 		= new StringBuffer("role");
		where 		= new StringBuffer(" name='")
			.append(name).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		RoleDef res = new RoleDef(hash);
		Integer id = (Integer)hash.get("role_id");
		if (id != null)
			res.setId(id.toString());

		return res;
	}


	/** 
	 * Returns all info about a particular Role.
	 *
	 * @param resid the Role's ID
	 * @return the Role with this ID
	 */
	public RoleDef getRoleById(String resid) 
			throws SQLException {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= " * ";
		from 		= new StringBuffer("role");
		where 		= new StringBuffer(" role_id=")
			.append(resid);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		RoleDef res = new RoleDef(hash);
		Integer id = (Integer)hash.get("role_id");
		if (id != null)
			res.setId(id.toString());

		return res;
	}


}


