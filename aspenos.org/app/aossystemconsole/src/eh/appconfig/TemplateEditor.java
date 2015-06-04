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
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.app.aossystemconsole.registry.*;



/**
 * Template Configuration.
 */
public class TemplateEditor extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "TEMPLATE EDITOR");

		TemplateRegistry treg = (TemplateRegistry)
			getCSRegistry("template");

		// get the app registry
		 AppRegistry appreg = (AppRegistry)
			 getAppRegistry("app");

		if (appreg == null)
			_lw.logDebugMsg("APP REGISTRY IS NULL!");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String rgKey = (String)params.get("reggrp_key");


		TemplateDef def = null;
		String filePath = null;
		String selTemplateId = null;
		String selTemplateName = (String)params.get("sel_template_name"); 
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgName = (String)params.get("reggrp_name"); 
		String selEventName = (String)params.get("sel_event_name"); 
		String selResourceName = (String)params.get("sel_resource_name"); 
		String redirEvent = (String)params.get("redir_event"); 


		try {
			if (selTemplateName==null || selTemplateName.equals("")) {
				// get def by ID
				selTemplateId = (String)params.get("lst_main"); 
				def = treg.getTemplateById(
						rgKey, selTemplateId);
				selTemplateName = (String)def.getProperty("name");

			} else {
				// get def by name
				def = treg.getTemplateById(
						rgKey, selTemplateName);

			}

			filePath = (String)def.getProperty("file_path");

			/////////////////////////////////////////
			// get the app's jar path
			AppDef app = appreg.getAppById(new IdDef(appIdStr));
			String jarPath = (String)app.getProperty("jar_path");

			// load the template from the app's jar
			TemplateLoader tloader = new TemplateLoader(jarPath);
			String html = tloader.loadTemplate(filePath);

			tagTable.put("html", html);
			tagTable.put("jar_path", jarPath);
			/////////////////////////////////////////

		} catch (Exception ex) {
			String msg = "Unable to get template info.";
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return tagTable;
		}



		tagTable.put("file_path", filePath);
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("sel_event_name", selEventName);
		tagTable.put("sel_resource_name", selResourceName);
		tagTable.put("sel_template_name", selTemplateName);
		tagTable.put("redir_event", redirEvent);


		return tagTable; 
	}
}

