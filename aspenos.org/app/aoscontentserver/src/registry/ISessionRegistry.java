package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;

/**
 *
 */
public interface ISessionRegistry extends ICSRegistry {

	/**
	 *  Creates a new session that is ready for a user to login,
	 *  and use the given SID as its session ID.
	 */
	public void createNewLogin(String newSid)
			throws SQLException;

	/** 
	 *  Retrieves a the role of the Principal associated with a 
	 *  specific Session 
	 */
	public IdDef getPrincipalRoleId(String sessionId)
			throws SQLException;

	/** Retrieves a Principal associated with a specific Session */
	public IdDef getPrincipalId(String sessionId)
			throws SQLException;

	/** Sets the Principal for a specific Session */
	public void setPrincipal(String sessionId, String pid)
			throws SQLException, InvalidSessionException;

	/** Sets the status for a specific Session */
	public void setStatus(String sessionId, int status)
			throws SQLException;

	/** 
	 * Finds out if a session is valid for event requests.
	 */
	public boolean isValid(String sessionId)
				throws SQLException;

	/** 
	 * Gets the status for a specific Session.
	 */
	public int getStatus(String sessionId)
				throws SQLException;

	/** 
	 * Sets the principal and status for a specific Session.
	 * If the system session ID does not exist, a session 
	 * gets automatically created.
	 */
	public void setPrincipalAndStatus(String sessionId, String pid, int status) 
			throws SQLException; 
}


