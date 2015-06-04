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
 * App Event Configuration.
 */
public class EditTemplate extends SysConEHParent {

	public HashMap handleEvent() {

		// get the template ID from the params
		HashMap params = (HashMap)_wer.getProperty("req_params");
		String templateId = (String)params.get("lst_main");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		String selEventName = (String)params.get("sel_event_name"); 
		String selResourceName = (String)params.get("sel_resource_name"); 
		String redirEvent = (String)params.get("redir_event"); 

		// load the template
		TemplateRegistry treg = (TemplateRegistry)
			getCSRegistry("template");

		HashMap ivTags = new HashMap();
		try {
			TemplateDef def = treg.getTemplateById(rgKey, templateId);

			String name = (String)def.getProperty("name");
			String filePath = (String)def.getProperty("file_path");

			// fill up tag table fields with template data
			ivTags.put("iv_id", templateId);
			ivTags.put("iv_name", name);
			ivTags.put("iv_file_path", filePath);
			ivTags.put("form_name", FORM_NAME);
			ivTags.put("sel_event_name", selEventName);
			ivTags.put("sel_resource_name", selResourceName);
			ivTags.put("redir_event", redirEvent);

		} catch (Exception ex) {
			String msg = "Unable to get template #" + templateId;
			HashMap errTags = new HashMap();
			errTags.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return errTags;
		}


		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String templateFieldTemplate = 
			appTl.loadTemplate("app_config/template_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String templateFields = fe.doExchange(templateFieldTemplate, ivTags);

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		tagTable.put("fields", templateFields);
		tagTable.put("function_name", "Edit");
		tagTable.put("entity_name", "Template");
		tagTable.put("webevent_name", "update_template");
		tagTable.put("button_label", "update template");

		//tagTable.put("sel_event_name", selEventName);
		//tagTable.put("sel_resource_name", selResourceName);

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}
}

