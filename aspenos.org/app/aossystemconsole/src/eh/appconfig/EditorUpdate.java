package org.aspenos.app.aossystemconsole.eh.appconfig;

import java.util.*;
import java.io.*;
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



/**
 * Template Configuration.
 */
public class EditorUpdate extends SysConEHParent {

	private static final String TEMPLATE_DIR =
		"templates/";

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"TEMPLATE CONFIGURATION");

		String updatedHtml = null;
		File uploadedFile = null;
		HashMap params = null;
		boolean upload = false;
		
		// get the request params
		params = (HashMap)_wer.getProperty("mpr_params");

		if (params == null) {
			// get the updated html string
			upload = false;
			params = (HashMap)_wer.getProperty("req_params");
			updatedHtml = (String)params.get("html"); 

		} else {
			// get the uploaded file
			upload = true;
			MultipartRequest mpr = (MultipartRequest)
				_wer.getProperty("mpr");
			_lw.logDebugMsg("retrieving uploaded file");
			uploadedFile = mpr.getFile("file_new_template"); 
		}


		String entryName = (String)params.get("file_path"); 
		entryName = TEMPLATE_DIR + entryName;
		String jarPath = (String)params.get("jar_path"); 

		_lw.logDebugMsg("about to change jar: " + jarPath);

		try {

			JarUpdater ju = new JarUpdater(jarPath);

			if (upload) {
				_lw.logDebugMsg("replacing template with upload");
				ju.updateEntry(entryName, uploadedFile);
			} else {
				_lw.logDebugMsg("saving html");
				ju.updateEntry(entryName, updatedHtml);
			}

			_lw.logDebugMsg("entry was updated: " + entryName);


		} catch (Exception ex) {
			String msg = "Unable to update the template: " + 
				ex.toString();
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return tagTable;
		}


		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
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

		return tagTable; 
	}
}

