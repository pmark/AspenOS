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
public interface IResourceRegistry extends ICSRegistry {
	
	/** 
	 * Checks if a role is contained by a resource.
	 * Returns true if there is a template defined for
	 * the given role ID.
	 *
	 * Also returns true if the resource has a default role
	 * template defined (where role ID is 0).
	 *
	 * @param resourceid in which to check
	 * @param roleid of which to check
	 * @return true if the resource contains the role
	 */
	public boolean roleHasAccess(String resourceid, String roleid)
			throws SQLException;

	/**
	 * Stores a set of resources.
	 */
	public void storeResourceDefs(String rgKey, ResourceDefs defs)
			throws SQLException;

}
