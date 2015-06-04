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



/**
 * Template Configuration.
 */
public class UpdateTemplate extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"TEMPLATE CONFIGURATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 
		String redirEvent = (String)params.get("redir_event"); 
		String selEventName = (String)params.get("sel_event_name"); 
		String selResourceName = (String)params.get("sel_resource_name"); 
		String selTemplateName = (String)params.get("sel_template_name"); 

		String newTemplateId = (String)params.get("txt_id"); 
		String newTemplateName = (String)params.get("txt_name"); 
		String newFilePath = (String)params.get("txt_file_path"); 

		String newEventMenuId = (String)params.get("txt_menu_id"); 
		String newEventMenuSel = (String)params.get("txt_menu_index"); 

		String origEventId = (String)params.get("orig_id"); 
		String newRecord = (String)params.get("new_record"); 
		boolean isNewRecord = false;
		if (newRecord != null && newRecord.equalsIgnoreCase("true"))
			isNewRecord = true;

		HashMap paramTags = new HashMap();
		paramTags.put("app_display_name", appDisplayName);
		paramTags.put("app_id", appIdStr);
		paramTags.put("reggrp_key", rgKey);
		paramTags.put("reggrp_name", rgName);
		paramTags.put("sel_template_name", selTemplateName);
		paramTags.put("sel_event_name", selEventName);
		paramTags.put("sel_resource_name", selResourceName);
		paramTags.put("webevent_name", redirEvent);

		HashMap hash = new HashMap();
		hash.put("template_id", newTemplateId);
		hash.put("name", newTemplateName);
		hash.put("file_path", newFilePath);
		TemplateDef def = new TemplateDef(hash);

		TemplateRegistry reg = (TemplateRegistry)
			getCSRegistry("template");



		try {
			if (isNewRecord) {
				_lw.logDebugMsg("Storing new record");
				reg.storeTemplateDef(rgKey, def);
			} else {
				_lw.logDebugMsg("Updating record");
				reg.updateTemplateDef(rgKey, new IdDef(origEventId), def);
			}
			
			_lw.logDebugMsg("Record updated!");

		} catch (Exception ex) {
			String msg = "Unable to add/create new template.";
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return tagTable;
		}

		String paramString = ServletTool.getParamString(paramTags);

		StringBuffer redirURL = new StringBuffer(_appBaseURI)
			.append("?")
			.append(paramString);

		_lw.logDebugMsg("redirecting to " + redirURL.toString());
		tagTable.put("REDIR_URL", redirURL.toString());

		tagTable.putAll(paramTags);

		return tagTable; 
	}

}

