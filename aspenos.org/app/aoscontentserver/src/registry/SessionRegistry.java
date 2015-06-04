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
public class SessionRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public SessionRegistry() {
	}

	public SessionRegistry(DbPersistence db) {
		_db = db;
	}

	// Primary Methods =============================================

	/** 
	 *  Retrieves a the role of the Principal associated with a 
	 *  specific Session. 
	 */
	public IdDef getPrincipalRoleId(String sessionId) 
			throws SQLException {

		StringBuffer where = new StringBuffer("s.system_sid='")
			.append(sessionId).append("' AND ")
			.append("s.principal_id=p.principal_id");

		Integer rid = (Integer)_db.selectFirstAttrib("p.selected_role", 
				"session s,principal p", where.toString() );

		if (rid == null) return null;

		return new IdDef(rid.toString());
	}


	/** 
	 * Retrieves a Principal associated with a specific Session. 
	 */
	public IdDef getPrincipalId(String sessionId) 
				throws SQLException {


		StringBuffer where = new StringBuffer("system_sid='")
			.append(sessionId).append("'");

		Integer pid = (Integer)_db.selectFirstAttrib("principal_id", "session", 
				where.toString() );

		if (pid == null) return null;

		return new IdDef(pid.toString());
	}


	/** 
	 * Sets the Principal for a specific Session.
	 */
	public void setPrincipal(String sessionId, String pid) 
				throws SQLException, InvalidSessionException {

		// check for the session
		StringBuffer where = new StringBuffer("system_sid='")
			.append(sessionId).append("'");

		// search for an existing session with this sid
		if (!_db.canFind("session", where.toString())) {
			throw new InvalidSessionException("No such session: " +
					sessionId);
		}

		StringBuffer up = new StringBuffer("UPDATE session SET ")
			.append("principal_id=").append(pid)
			.append(" WHERE ").append( where.toString() );

		_db.update( up.toString() );
	}


	/** 
	 * Sets the status for a specific Session.
	 * If the system session ID does not exist, 
	 * a session gets automatically created.
	 */
	public void setStatus(String sessionId, int status) 
				throws SQLException {

		StringBuffer where = new StringBuffer("system_sid='")
			.append(sessionId).append("'");

		// search for an existing session with this sid
		if (!_db.canFind("session", where.toString())) {
			// Insert a new record (start a new session)
			StringBuffer ins = new StringBuffer("INSERT INTO session ")
				.append("(system_sid,status) VALUES ('")
				.append(sessionId)
				.append("','").append(status).append("')");

			_db.insert( ins.toString() );
			
		} else {

			// Do an update to the existing one
			StringBuffer up = new StringBuffer("UPDATE session SET ")
				.append("status='").append(status)
				.append("' WHERE ").append( where.toString() );

			_db.update( up.toString() );
		}
	}


	/** 
	 * Gets the status for a specific Session.
	 */
	public int getStatus(String sessionId)
				throws SQLException {

		if (sessionId == null)
			return -1;

		StringBuffer where = new StringBuffer("system_sid='")
			.append(sessionId).append("'");

		Object o = _db.selectFirstAttrib("status", "session", 
				where.toString());

		if (o == null)
			return -1;

		Integer i = (Integer)o;


		return i.intValue();
	}


	/** 
	 * Finds out if a session is valid for event requests.
	 */
	public boolean isValid(String sessionId)
				throws SQLException {

		if (getStatus(sessionId) == -1)
			return false;

		return true;
	}


	/**
	 *  Creates a new session that is ready for a user to login,
	 *  and uses the given SID as its session ID.
	 */
	public void createNewLogin(String newSid) 
				throws SQLException {
	
		StringBuffer where = new StringBuffer("system_sid='")
			.append(newSid).append("'");

		// Search for an existing system SID
		if (_db.canFind("session", where.toString())) {
			// If this system session ID has already been used, 
			// something important needs to happen.  I do not know
			// what that is yet.  
			// 
			// For now, just append a special character onto the
			// end of the system SID, which will make it unique.
		} 


		// Create the INSERT SQL
		StringBuffer ins = new StringBuffer("INSERT INTO session ")
			.append("(system_sid,status) VALUES ('")
			.append(newSid)
			.append("','")
			.append(Integer.toString(CS_STATUS_NEW_LOGIN))
			.append("')");

		_db.insert( ins.toString() );
	}


	/** 
	 * Sets the principal and status for a specific Session.
	 * If the system session ID does not exist, a session 
	 * gets automatically created.
	 */
	public void setPrincipalAndStatus(String sessionId, String pid, int status) 
				throws SQLException {

		StringBuffer where = new StringBuffer("system_sid='")
			.append(sessionId).append("'");

		// search for an existing session with this sid
		if (!_db.canFind("session", where.toString())) {
			// Insert a new record (start a new session)
			StringBuffer ins = new StringBuffer("INSERT INTO session ")
				.append("(system_sid,principal_id,status) VALUES ('")
				.append(sessionId)
				.append("','")
				.append(pid)
				.append("','")
				.append(status)
				.append("')");

			_db.insert( ins.toString() );
			
		} else {

			// Do an update to the existing one
			StringBuffer up = new StringBuffer("UPDATE session SET ")
				.append("principal_id=").append(pid)
				.append(", ")
				.append("status='").append(status)
				.append("' WHERE ").append( where.toString() );

			_db.update( up.toString() );
		}
	}




}


