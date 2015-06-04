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
public interface ITemplateRegistry extends ICSRegistry {

	
	/** Retrieves the template associated with a WebEvent ID and
	 * a role ID.
	 * @param  of the template to get
	 * @return The pid's selected role
	 */
	public TemplateDefs getTemplatesByEvent(
			String rgKey, String weid, String roleid)
			throws Exception;


	/** Retrieves the template specified by the 'name' param.
	 * @param name of the template to get
	 * @return The template
	 */
	public TemplateDef getTemplateByName(String rgKey, String name)
			throws SQLException;


	/** Retrieves the template specified by the 'tid' param.
	 * @param tid of the template to get
	 * @return The pid's selected role
	 */
	public TemplateDef getTemplateById(String rgKey, String tid)
			throws SQLException;


	/**
	 * Stores a set of resources.
	 */
	public void storeTemplateDefs(String rgKey, TemplateDefs defs)
			throws SQLException;

}


