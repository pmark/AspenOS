package org.aspenos.app.aossystemconsole.eh.appconfig;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aossystemconsole.eh.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.app.aossystemconsole.registry.*;



/**
 * XML Import.
 */
public class XMLImport extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "XML IMPORT");

		HashMap params = (HashMap)_wer.getProperty("req_params");

		String appDisplayName = (String)params.get("app_display_name");
		String appSystemName = (String)params.get("app_system_name");
		String appIdStr = (String)params.get("app_id");
		String rgName = (String)params.get("reggrp_name"); 
		String rgKey = (String)params.get("reggrp_key");
		String redirEvent = (String)params.get("redir_event"); 


		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_system_name", appSystemName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("redir_event", redirEvent);

		return tagTable; 
	}
}

