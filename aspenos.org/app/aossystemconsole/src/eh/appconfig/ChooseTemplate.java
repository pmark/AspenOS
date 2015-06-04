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
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;



/**
 * App Template Configuration.
 */
public class ChooseTemplate extends SysConEHParent {

	public HashMap handleEvent() {

		String pageContent = "";
		HashMap tagTable = getClonedTagTable();
		HashMap params = (HashMap)_wer.getProperty("req_params");

		tagTable.put("sub_titles", 
				"TEMPLATE CONFIGURATION");

		TemplateDefs templateList = null;
		StringBuffer listItems = new StringBuffer();

		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		try {

			TemplateRegistry treg = (TemplateRegistry)
				getCSRegistry("template");

			// get all of this app's templates from the CS reg
			templateList = treg.getAllTemplatesForRGKey(rgKey);
			Iterator templateIt = templateList.iterator();

			while (templateIt.hasNext()) {
				TemplateDef templateDef = (TemplateDef)
					templateIt.next();
				String templateId = ((Integer)
						templateDef.getProperty("template_id")).toString();
				String templateName = (String)templateDef.getProperty("name");
				listItems.append("<OPTION VALUE=\"")
					.append(templateId)
					.append("\">")
					.append(templateName)
					.append("</OPTION>\n");
			}
		} catch (Exception regEx) {
			tagTable.put("ERROR", 
					"Problem while accessing app's template info");
			_lw.logDebugMsg("ChooseEvent: ERROR!", regEx);
			_lw.logErr("ChooseEvent: ERROR!", regEx);
			return tagTable;
		}

		int numTemplates = templateList.size();

		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		StringBuffer tmpPage = new StringBuffer();
		FieldExchanger fe = new FieldExchanger(_lw);

		tagTable.put("app_display_name", appDisplayName);
		if (numTemplates > 0) {

			// Get the CS template loader
			TemplateLoader csTl = (TemplateLoader)_wer
				.getProperty("cs_template_loader");


			//// Main list
			// load main_list.template from the CS jar
			String mainListTemplate = 
				csTl.loadTemplate("main_list.template");
			String extraStuff = appTl.loadTemplate(
					"app_config/template_extra_stuff.template");
			tagTable.put("extra_stuff", extraStuff);

			StringBuffer listLabel = new StringBuffer("Group <b>")
				.append(rgName)
				.append("</b> has <b>")
				.append(numTemplates)
				.append("</b> templates");
			tagTable.put("list_items", listItems.toString());
			tagTable.put("list_label", listLabel.toString() );

			// buttons
			tagTable.put("primary_cmd_name", "edit");
			tagTable.put("primary_cmd_label", "edit record");
			tagTable.put("next_name_add", "add_template");
			tagTable.put("next_name_primary", "edit_template");
			tagTable.put("next_name_remove", "remove_template");

			String swappedMainList = 
					fe.doExchange(mainListTemplate, tagTable);

			// load template_list.template from the app jar
			String templateListTemplate = 
				appTl.loadTemplate("app_config/template_list.template");
			tagTable.put("main_list", swappedMainList);
			tmpPage.append(
					fe.doExchange(templateListTemplate, tagTable));


		} else {
			// load no_templates.template
			tmpPage.append(appTl.loadTemplate("app_config/no_templates.template"));
			tmpPage = new StringBuffer(	
					fe.doExchange(tmpPage.toString(), tagTable));
		}

		pageContent = tmpPage.toString();
		tagTable.put("choose_template_content", pageContent);

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		tagTable.put("redir_event", "choose_template");

		return tagTable; 
	}
}

