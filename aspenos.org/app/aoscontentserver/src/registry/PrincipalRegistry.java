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
public class PrincipalRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public PrincipalRegistry() {
	}

	public PrincipalRegistry(DbPersistence db) {
		_db = db;
	}

	// Primary Methods =============================================

	/** 
	 * Username/password authentication.
	 *
	 * @param username
	 * @param password
	 * @return The authenticated principal ID or null.
	 */
	public IdDef validatePrincipal(String username, String password)
			throws WrongPasswordException, NoSuchUserException,
			SQLException {
		return validatePrincipal(username, password, null);
	}


	/** 
	 * Username/password authentication.
	 *
	 * @param username
	 * @param password
	 * @param host_site
	 * @return A principal validated with the username and password,
	 *         or null if 
	 */
	public IdDef validatePrincipal(
			String username, String password, String host_site)
			throws WrongPasswordException, NoSuchUserException,
			SQLException {

		StringBuffer where_user = new StringBuffer("username='")
			.append(username).append("'");


		boolean exists = _db.canFind("principal", where_user.toString());
		if (!exists) {
			throw new NoSuchUserException("User " + username + 
					" does not exist.");
		}

		
		StringBuffer where_both = new StringBuffer(where_user.toString())
			.append(" AND password='")
			.append(password).append("'");

		if (host_site != null) {
			where_both.append(" AND host_site='")
				.append(host_site).append("'");
		}
	
		// Select the principal ID
		Integer intPid = (Integer)_db.selectFirstAttrib("principal_id", 
				"principal", where_both.toString());

		String pid = null;
		if (intPid != null)
			pid = intPid.toString();

		IdDef prin=null;
		if (pid != null && pid.length() > 0) {
			prin = new IdDef(pid);
		} else {
			throw new WrongPasswordException(username + 
					"'s password was incorrect.");
		}

		return prin;
	}


	/**
	 * Retrieves the selected role for a given principal.
	 *
	 * @param pid of the principal to get the selected role for
	 * @return The ID of this principal's selected role 
	 */
	public IdDef getSelectedRole(String pid) 
			throws SQLException {

		StringBuffer where = new StringBuffer("principal_id=")
			.append(pid);

		String rid = (String)_db.selectFirstAttrib(
				"selected_role", "principal", where.toString() );

		if (rid == null)
			return null;

		return new IdDef(rid);
	}


	/** 
	 * Retrieves profile for the given principal.
	 *
	 * @param pid of the principal to get
	 * @return The pid's profile
	 */
	public PrincipalDef getPrincipalById(String pid) 
			throws SQLException {
		return getProfile(pid);
	}


	/** 
	 * Retrieves profile for the given principal.
	 *
	 * @param pid of the principal to get
	 * @return The pid's profile
	 */
	public PrincipalDef getPrincipalByName(String username) 
			throws SQLException {
		return getPrincipalByName(username, null);
	}

	public PrincipalDef getPrincipalByName(String username, String hostSite) 
			throws SQLException {

		StringBuffer where = new StringBuffer(48)
			.append("username='")
			.append(username).append("' AND host_site=");

		if (hostSite == null)
			where.append("null");
		else
			where.append("'").append(hostSite).append("'");

		HashMap hash = (HashMap)_db.selectFirstAsHash(
				"*", "principal", where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		return new PrincipalDef(hash);
	}


	/** 
	 * Retrieves profile for the given principal.
	 * Exactly the same as getPrincipalById().
	 *
	 * @param pid of the principal to get
	 * @return The pid's profile
	 */
	public PrincipalDef getProfile(String pid) 
			throws SQLException {

		StringBuffer where = new StringBuffer("principal_id=")
			.append(pid);

		HashMap hash = (HashMap)_db.selectFirstAsHash(
				"*", "principal", where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		return new PrincipalDef(hash);
	}


	/**
	 * Retrieves a set of roles associated with a specific principal.
	 *
	 * @return All roles defined for the given principal
	 */
	public List getAllRoleIds(String pid) 
			throws SQLException {

		StringBuffer where = new StringBuffer(20)
			.append("principal_id=").append(pid);

		List list = _db.selectAttrib(
				"role_id", "prinroles", where.toString());

		if (list == null || list.size() == 0)
			return null;

		// convert the Integer role IDs to Strings
		String rid;
		Iterator lit = list.iterator();
		ArrayList allIds = new ArrayList();
		while (lit.hasNext()) {
			rid = ((Integer)lit.next()).toString();
			allIds.add(rid);
		}

		return allIds;
	}


	/**
	 * Stores a principal.  Returns the new principal's
	 * AOS principal ID.  Does not update the 
	 * 'prinroles' or 'role' table.
	 *
	 * @return The new principal's AOS principal ID.
	 */
	public long storePrincipalDef(PrincipalDef def)
			throws SQLException, UserAlreadyExistsException {

		// make sure the name does not already exist
		String username = (String)def.getProperty("username");
		String hostSite = (String)def.getProperty("host_site");

		PrincipalDef testDef = getPrincipalByName(username, hostSite);
		if (testDef != null)
			throw new UserAlreadyExistsException(
					"User " + username + " already exists.");


		String tmp = "INSERT INTO principal " + 
			def.getSqlFieldsAndValues();

		_db.insert(tmp);

		// Use the new user's username to get its
		// principal ID.
		StringBuffer where = new StringBuffer("username='")
			.append(username);
		if (hostSite != null && !hostSite.equals(""))
			where.append(" AND host_site='")
				.append(hostSite).append("'");
		else
			where.append("'"); 

		Integer pid = (Integer)_db.selectFirstAttrib("principal_id", 
				"principal", where.toString());

		if (pid == null)
			return -1;
		else 
			return pid.longValue();
	}


	/**
	 * Stores a set of principals.  Does not update
	 * the 'prinroles' or 'role' table.
	 */
	public void storePrincipalDefs(PrincipalDefs defs)
			throws SQLException, UserAlreadyExistsException {

		String insert = "INSERT INTO principal ";

		for (Iterator it=defs.iterator(); it.hasNext();) {
			PrincipalDef def = (PrincipalDef)it.next();
			storePrincipalDef(def);
		}
	}


	/**
	 * Stores one prin-role record.
	 */
	public void storePrinRoleDef(PrinRoleDef def)
			throws SQLException {

		String insert = "INSERT INTO prinroles ";
		String fandv = def.getSqlFieldsAndValues();
		_db.insert(insert + fandv);
	}

	/**
	 * Stores a set of prin-roles.
	 */
	public void storePrinRoleDefs(PrinRoleDefs defs)
			throws SQLException {

		String insert = "INSERT INTO prinroles ";

		for (Iterator it=defs.iterator(); it.hasNext();) {
			PrinRoleDef def = (PrinRoleDef)it.next();

			String fandv = def.getSqlFieldsAndValues();
			if (fandv == null)
				continue;

			_db.insert(insert + fandv);
		}
	}


	/** 
	 * Delete a principal.  Deletes all prinrole records for the 
	 * principal too.
	 */
	public void deletePrincipalById(String pid) throws SQLException {

		if (pid == null || pid.equals("") || pid.equals("null"))
			throw new NullPointerException("null principal_id for deletion");

		StringBuffer where = new StringBuffer(" WHERE principal_id=")
			.append(pid);

		// erase this principal and the prinroles
		_db.update("DELETE FROM principal " + where.toString());
		_db.update("DELETE FROM prinroles " + where.toString());
	}


	/** 
	 * Sets a principal's selected role.
	 */
	public void setSelectedRole(String pid, String selRole) 
				throws SQLException {

		if (pid == null || pid.equals("") || pid.equals("null"))
			throw new NullPointerException("null principal_id for update");

		StringBuffer where = new StringBuffer(" WHERE principal_id=")
			.append(pid);

		// erase this principal
		_db.update("UPDATE principal SET selected_role=" + 
				selRole + where.toString());
	}


	/** 
	 * Sets a principal's password.
	 */
	public void setPassword(String pid, String newPassword) 
				throws SQLException {

		if (pid == null || pid.equals("") || pid.equals("null"))
			throw new NullPointerException("null principal_id for password update");

		StringBuffer update = new StringBuffer(48)
			.append("UPDATE principal SET password='")
			.append(newPassword)
			.append("' WHERE principal_id=")
			.append(pid);

		_db.update(update.toString());
	}


	/** 
	 * Sets a principal's password.
	 */
	public void setPassword(String username, String hostSite, String newPassword) 
				throws SQLException {

		if (username == null || username.equals(""))
			throw new NullPointerException("null username for password update");

		StringBuffer update = new StringBuffer(48)
			.append("UPDATE principal SET password='")
			.append(newPassword)
			.append("' WHERE username='")
			.append(username)
			.append("' AND host_site='")
			.append(hostSite)
			.append("'");

		_db.update(update.toString());
	}


}


