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
public interface IRoleRegistry extends ICSRegistry {
	
	/**
	 * Stores a set of resources.
	 */
	public void storeRoleDefs(RoleDefs defs)
			throws SQLException;

}
