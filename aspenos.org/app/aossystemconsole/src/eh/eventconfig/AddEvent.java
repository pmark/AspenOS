package org.aspenos.app.aossystemconsole.eh.eventconfig;

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
public class AddEvent extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		// initial values
		HashMap ivTags = new HashMap();
		ivTags.put("iv_id", "");
		ivTags.put("iv_name", "");
		ivTags.put("iv_class_name", "");
		ivTags.put("iv_menu_name", "");
		ivTags.put("iv_menu_sel_name", "");
		ivTags.put("form_name", FORM_NAME);

		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String eventFieldTemplate = 
			appTl.loadTemplate("app_config/event_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String eventFields = fe.doExchange(eventFieldTemplate, ivTags);

		tagTable.put("fields", eventFields);
		tagTable.put("function_name", "Add");
		tagTable.put("entity_name", "Web Event");

		tagTable.put("webevent_name", "aec_update_event");
		tagTable.put("button_label", "add event");
		tagTable.put("new_record", "true");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);


		return tagTable; 
	}
}

