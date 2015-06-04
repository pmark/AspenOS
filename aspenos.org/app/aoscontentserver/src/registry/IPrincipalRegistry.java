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
public interface IPrincipalRegistry extends ICSRegistry {

	
	/** 
	 * Username/password authentication.
	 *
	 * @param username
	 * @param password
	 * @return A principal validated with the username and password,
	 *         or null if 
	 */
	public IdDef validatePrincipal(
			String username, String password)
			throws WrongPasswordException, NoSuchUserException,
			SQLException;

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
			SQLException;

	/** 
	 * Retrieves the selected role for a given principal.
	 *
	 * @param pid of the principal to get the selected role for
	 * @return The pid's selected role
	 */
	public IdDef getSelectedRole(String pid)
			throws SQLException;

	/** 
	 * Retrieves profile for the given principal.
	 *
	 * @param pid of the principal to get the profile for
	 * @return The pid's profile as a Map of key=value pairs
	 */
	public PrincipalDef getProfile(String pid)
			throws SQLException;

	/** 
	 * Retrieves a set of roles associated with a specific principal.
	 *
	 * @return All roles defined for the given principal
	 */
	public List getAllRoleIds(String pid)
			throws SQLException;

	/**
	 * Stores a principal.
	 */
	public long storePrincipalDef(PrincipalDef def)
			throws SQLException, UserAlreadyExistsException;


	/**
	 * Stores a set of principals.
	 */
	public void storePrincipalDefs(PrincipalDefs defs)
			throws SQLException, UserAlreadyExistsException;


	/**
	 * Stores a set of prin-roles.
	 */
	public void storePrinRoleDefs(PrinRoleDefs defs)
			throws SQLException;
}


