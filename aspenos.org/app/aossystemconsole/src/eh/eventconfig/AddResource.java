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
public class AddResource extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		// initial values
		HashMap ivTags = new HashMap();
		ivTags.put("iv_id", "");
		ivTags.put("iv_name", "");
		ivTags.put("iv_ordinal", "");
		ivTags.put("form_name", FORM_NAME);

		String selEventName = (String)params.get("sel_event_name");
		ivTags.put("sel_event_name", selEventName);

		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String resourceFieldTemplate = 
			appTl.loadTemplate("app_config/resource_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String resourceFields = fe.doExchange(resourceFieldTemplate, ivTags);

		tagTable.put("function_name", "Add");
		tagTable.put("entity_name", "Resource");
		tagTable.put("fields", resourceFields);

		tagTable.put("webevent_name", "aec_update_resource");
		tagTable.put("button_label", "add resource");
		tagTable.put("new_record", "true");

		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		tagTable.put("sel_event_name", selEventName);
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);


		return tagTable; 
	}
}

