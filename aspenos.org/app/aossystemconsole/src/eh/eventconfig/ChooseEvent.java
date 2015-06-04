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
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;



/**
 * App Event Configuration.
 */
public class ChooseEvent extends SysConEHParent {

	public HashMap handleEvent() {

		String pageContent = "";
		HashMap tagTable = getClonedTagTable();
		HashMap params = (HashMap)_wer.getProperty("req_params");

		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		WebEventDefs eventList = null;
		StringBuffer listItems = new StringBuffer();

		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		try {

			WebEventRegistry evreg = (WebEventRegistry)
				getCSRegistry("webevent");

			// get all of this app's web events from the CS reg
			eventList = evreg.getAllEventsForRGKey(rgKey);
			Iterator eventIt = eventList.iterator();

			while (eventIt.hasNext()) {
				WebEventDef eventDef = (WebEventDef)
					eventIt.next();
				String eventId = ((Integer)
						eventDef.getProperty("webevent_id")).toString();
				String eventName = (String)eventDef.getProperty("name");
				listItems.append("<OPTION VALUE=\"")
					.append(eventId)
					.append("\">")
					.append(eventName)
					.append("</OPTION>\n");
			}
		} catch (Exception regEx) {
			tagTable.put("ERROR", "Problem while accessing app's event info");
			_lw.logDebugMsg("ChooseEvent: ERROR!", regEx);
			_lw.logErr("ChooseEvent: ERROR!", regEx);
			return tagTable;
		}

		int numEvents = eventList.size();

		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		StringBuffer tmpPage = new StringBuffer();
		FieldExchanger fe = new FieldExchanger(_lw);

		tagTable.put("app_display_name", appDisplayName);
		if (numEvents > 0) {

			// Get the CS template loader
			TemplateLoader csTl = (TemplateLoader)_wer
				.getProperty("cs_template_loader");


			//// Main list
			// load main_list.template from the CS jar
			String mainListTemplate = 
				csTl.loadTemplate("main_list.template");
			String extraStuff = appTl.loadTemplate(
					"app_config/event_extra_stuff.template");
			tagTable.put("extra_stuff", extraStuff);

			StringBuffer listLabel = new StringBuffer("Group <b>")
				.append(rgName)
				.append("</b> has <b>")
				.append(numEvents)
				.append("</b> web events");
			tagTable.put("list_items", listItems.toString());
			tagTable.put("list_label", listLabel.toString() );

			// buttons
			tagTable.put("primary_cmd_name", "edit");
			tagTable.put("primary_cmd_label", "edit");
			tagTable.put("next_name_add", "aec_add_event");
			tagTable.put("next_name_primary", "aec_edit_event");
			tagTable.put("next_name_remove", "aec_remove_event");

			String swappedMainList = 
					fe.doExchange(mainListTemplate, tagTable);

			// load event_list.template from the app jar
			String eventListTemplate = 
				appTl.loadTemplate("app_config/event_list.template");
			tagTable.put("main_list", swappedMainList);
			tmpPage.append(
					fe.doExchange(eventListTemplate, tagTable));


		} else {
			// load no_events.template
			tmpPage.append(appTl.loadTemplate("app_config/no_events.template"));
			tmpPage = new StringBuffer(	
					fe.doExchange(tmpPage.toString(), tagTable));
		}

		pageContent = tmpPage.toString();
		tagTable.put("choose_event_content", pageContent);

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}
}

