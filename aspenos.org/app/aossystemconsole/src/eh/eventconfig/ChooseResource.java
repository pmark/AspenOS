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
public class ChooseResource extends SysConEHParent {

	public HashMap handleEvent() {

		String pageContent = "";
		HashMap tagTable = getClonedTagTable();
		HashMap params = (HashMap)_wer.getProperty("req_params");

		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		ResourceDefs resList = null;
		StringBuffer listItems = new StringBuffer();

		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		String selEventId = null;
		String selEventName = (String)params.get("sel_event_name"); 
		if (selEventName == null || selEventName.equals("")) {
			selEventName = null;
			selEventId = (String)params.get("lst_main"); 
		}

		String selResourceName = "";

		// Try to get the event's name
		try {
			WebEventRegistry evreg = (WebEventRegistry)
				getCSRegistry("webevent");

			_lw.logDebugMsg("selected event ID: " + selEventId);
			_lw.logDebugMsg("selected event NAME: " + selEventName);

			if (selEventId == null) {
				WebEventDef wed = evreg.getEventByName(rgKey, selEventName);
				selEventId = ((Integer)wed.getProperty("webevent_id"))
					.toString();
			} else {
				WebEventDef wed = evreg.getEventById(rgKey, selEventId);
				selEventName = (String)wed.getProperty("name");	
			}


		} catch (Exception regEx) {
			String msg = "Unable to get selected event's name: " +
				regEx;
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg("ChooseEvent: " + msg, regEx);
			_lw.logErr("ChooseEvent: " + msg, regEx);
			return tagTable;
		}

		// Try to get all resources for this event
		try {
			ResourceRegistry resreg = (ResourceRegistry)
				getCSRegistry("resource");

			// get all of this app's web resources from the CS reg
			resList = resreg.getEventResources(rgKey,
					new IdDef(selEventId));
			Iterator resIt = resList.iterator();

			while (resIt.hasNext()) {
				ResourceDef resDef = (ResourceDef)
					resIt.next();
				String resId = ((Integer)
						resDef.getProperty("resource_id")).toString();
				String resName = (String)resDef.getProperty("name");
				listItems.append("<OPTION VALUE=\"")
					.append(resId)
					.append("\">")
					.append(resName)
					.append("</OPTION>\n");

	_lw.logDebugMsg(resName);
			}
		} catch (Exception regEx) {
			String msg = "Problem while accessing app's resource info" +
				regEx;
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg("ChooseEvent: " + msg, regEx);
			_lw.logErr("ChooseEvent: " + msg, regEx);
			return tagTable;
		}

		tagTable.put("sel_event_name", selEventName);

		int numEvents = resList.size();

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
					"app_config/resource_extra_stuff.template");
			tagTable.put("extra_stuff", extraStuff);

			StringBuffer listLabel = new StringBuffer("Group <b>")
				.append(rgName)
				.append("</b> has <b>")
				.append(numEvents)
				.append("</b> resources");
			tagTable.put("list_items", listItems.toString());
			tagTable.put("list_label", listLabel.toString() );

			// buttons
			tagTable.put("primary_cmd_name", "edit");
			tagTable.put("primary_cmd_label", "edit");
			tagTable.put("next_name_add", "aec_add_resource");
			tagTable.put("next_name_primary", "aec_edit_resource");
			tagTable.put("next_name_remove", "aec_remove_resource");

			String swappedMainList = 
					fe.doExchange(mainListTemplate, tagTable);

			// load resource_list.template from the app jar
			String resListTemplate = 
				appTl.loadTemplate("app_config/resource_list.template");
			tagTable.put("main_list", swappedMainList);
			tmpPage.append(
					fe.doExchange(resListTemplate, tagTable));


		} else {
			// load no_resources.template
			tmpPage.append(appTl.loadTemplate(
						"app_config/no_resources.template"));

			tmpPage = new StringBuffer(	
					fe.doExchange(tmpPage.toString(), tagTable));
		}

		pageContent = tmpPage.toString();

		tagTable.put("choose_resource_content", pageContent);

		tagTable.put("sel_event_name", selEventName);
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}

}

