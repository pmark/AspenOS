package org.aspenos.app.aossystemconsole.eh.appconfig;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.server.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aossystemconsole.eh.*;



/**
 * Template Configuration.
 */
public class RecacheTemplates extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"TEMPLATE CONFIGURATION");

		// get the request params
		HashMap params = (HashMap)_wer.getProperty("req_params");

		String appIdStr = (String)params.get("app_id");
		String appDisplayName = (String)params.get("app_display_name");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		String selEventName = (String)params.get("sel_event_name"); 
		String selResourceName = (String)params.get("sel_resource_name"); 
		String selTemplateName = (String)params.get("sel_template_name"); 
		String redirEvent = (String)params.get("redir_event"); 

		tagTable.put("sel_event_name", selEventName);
		tagTable.put("sel_resource_name", selResourceName);
		tagTable.put("sel_template_name", selTemplateName);
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("webevent_name", redirEvent);

		try {
			_lw.logDebugMsg("about to recache jar for app ID #" + appIdStr);
			SessionScreenServlet.cacheTemplates(new IdDef(appIdStr), 
					_wer.getSid(), false);
		} catch (Exception ex) {
			String msg = "Problem while recaching templates";
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			tagTable.put("ERROR", msg);
		}

		return tagTable; 
	}
}

