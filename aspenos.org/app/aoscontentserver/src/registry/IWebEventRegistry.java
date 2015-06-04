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
public interface IWebEventRegistry extends ICSRegistry {
	
	/** 
	 * Returns all info about a particular WebEvent. 
	 *
	 * @param name the WebEvent's name
	 * @return the WebEvent with this name
	 */
	public WebEventDef getEventByName(String rgKey, String name)
			throws SQLException;


	/** 
	 * Returns all info about a particular WebEvent.
	 *
	 * @param weid the WebEvent's ID
	 * @return the WebEvent with this ID
	 */
	public WebEventDef getEventById(String rgKey, String weid)
			throws SQLException;


	/** 
	 * Returns a set IDs for resources associated with a given WebEvent.
	 *
	 * @param weid the WebEvent's ID
	 * @return the IDs of all of the given WebEvent's resources
	 * or null if there are none.
	 */
	public IdDefs getAllResourceIds(String rgKey, String weid)
			throws SQLException;


	/** 
	 * Checks if a role can access a given event.
	 *
	 * @param weid the WebEvent's ID
	 * @return true if so
	 */
	public boolean roleHasAccess(String rgKey, String weid, String roleid)
			throws SQLException;


	/**
	 * Stores a set of web events.
	 */
	public void storeWebEventDefs(String rgKey, WebEventDefs defs)
			throws SQLException;


	/**
	 * Stores a set of ERTs (Event/Resource/Template combos).
	 * If the 'registry_group' attribute of an ERTDef is set,
	 * then that group is used for the insert.  Otherwise
	 * _regGroup, the one set at registry creation time,
	 * is used as the default.
	 */
	public void storeERTDefs(String rgKey, ERTDefs defs)
			throws SQLException;
}


