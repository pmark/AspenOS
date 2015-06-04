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
public class EditEvent extends SysConEHParent {

	public HashMap handleEvent() {

		// get the event ID from the params
		HashMap params = (HashMap)_wer.getProperty("req_params");
		String eventId = (String)params.get("lst_main");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		// load the event
		WebEventRegistry evreg = (WebEventRegistry)
			getCSRegistry("webevent");

		HashMap ivTags = new HashMap();
		try {
			WebEventDef wed = evreg.getEventById(rgKey, eventId);
			String name = (String)wed.getProperty("name");
			String className = (String)wed.getProperty("classname");
			String menuName = (String)wed.getProperty("menu_name");
			String menuSel = (String)wed.getProperty("menu_sel_name");

_lw.logDebugMsg("wed XML: " + wed.toXML());

			// fill up tag table fields with event data
			ivTags.put("iv_id", eventId);
			ivTags.put("iv_name", name);
			ivTags.put("iv_class_name", className);
			ivTags.put("iv_menu_name", menuName);
			ivTags.put("iv_menu_sel_name", menuSel);
			ivTags.put("form_name", FORM_NAME);

		} catch (Exception ex) {
			String msg = "Unable to get event #" + eventId +
				"\n\t" + ex.toString();
			HashMap errTags = new HashMap();
			errTags.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return errTags;
		}


		// load the template
		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		String eventFieldTemplate = 
			appTl.loadTemplate("app_config/event_fields.template");

		// swap the tags
		FieldExchanger fe = new FieldExchanger(_lw);
		String eventFields = fe.doExchange(eventFieldTemplate, ivTags);

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		tagTable.put("fields", eventFields);
		tagTable.put("function_name", "Edit");
		tagTable.put("entity_name", "Web Event");
		tagTable.put("webevent_name", "aec_update_event");
		tagTable.put("button_label", "update event");

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}
}

