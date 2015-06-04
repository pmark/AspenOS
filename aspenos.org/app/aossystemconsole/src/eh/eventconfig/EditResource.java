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
public class EditResource extends SysConEHParent {

	public HashMap handleEvent() {

		// get the resource ID from the params
		HashMap params = (HashMap)_wer.getProperty("req_params");
		String resourceId = (String)params.get("lst_main");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		String selEventName = (String)params.get("sel_event_name"); 

		// load the resource
		ResourceRegistry resreg = (ResourceRegistry)
			getCSRegistry("resource");

		HashMap ivTags = new HashMap();
		try {
			ResourceDef rdef = resreg.getResourceById(rgKey, resourceId);

			String name = (String)rdef.getProperty("name");

			String ordinal;
			Object o = rdef.getProperty("ordinal");
			if (o == null)
				ordinal = "";
			else 

			ordinal = ((Integer)o).toString();
			if (ordinal.equals(MAX_ORDINAL))
				ordinal = "";

			// fill up tag table fields with resource data
			ivTags.put("iv_id", resourceId);
			ivTags.put("iv_name", name);
			ivTags.put("iv_ordinal", ordinal);
			ivTags.put("form_name", FORM_NAME);
			ivTags.put("sel_event_name", selEventName);

		} catch (Exception ex) {
			String msg = "Unable to get resource #" + resourceId;
			HashMap errTags = new HashMap();
			errTags.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return errTags;
		}


		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String resourceFieldTemplate = 
			appTl.loadTemplate("app_config/resource_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String resourceFields = fe.doExchange(resourceFieldTemplate, ivTags);

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		tagTable.put("fields", resourceFields);
		tagTable.put("function_name", "Edit");
		tagTable.put("entity_name", "Web Event");
		tagTable.put("webevent_name", "aec_update_resource");
		tagTable.put("button_label", "update resource");

		tagTable.put("sel_event_name", selEventName);

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}
}

