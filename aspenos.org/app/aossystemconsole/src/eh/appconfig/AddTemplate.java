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
 * App Template Configuration.
 */
public class AddTemplate extends SysConEHParent {

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

		// initial values
		HashMap ivTags = new HashMap();
		ivTags.put("iv_id", "");
		ivTags.put("iv_name", "");
		ivTags.put("iv_class_name", "");
		ivTags.put("iv_menu_id", "");
		ivTags.put("iv_menu_selection", "");
		ivTags.put("form_name", FORM_NAME);
		ivTags.put("redir_event", redirEvent);
		ivTags.put("sel_event_name", selEventName);
		ivTags.put("sel_resource_name", selResourceName);

		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String templateFieldTemplate = 
			appTl.loadTemplate("app_config/template_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String templateFields = fe.doExchange(templateFieldTemplate, ivTags);

		tagTable.put("fields", templateFields);
		tagTable.put("function_name", "Add");
		tagTable.put("entity_name", "Template");

		tagTable.put("sel_event_name", selEventName);
		tagTable.put("sel_resource_name", selResourceName);
		tagTable.put("template_name", "update_template");
		tagTable.put("button_label", "add template");
		tagTable.put("new_record", "true");

		tagTable.put("webevent_name", "update_template");
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);


		return tagTable; 
	}
}

