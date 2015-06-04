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
 *
 * Shows a list of ALL application templates.
 * Uses Javascript to populate a field with
 * the role IDs for a chosen template if that
 * template has roles assigned to it.
 */
public class AddResourceTemplate extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		TemplateLoader appTl = (TemplateLoader)_wer
			.getProperty("app_template_loader");
		StringBuffer tmpPage = new StringBuffer();
		FieldExchanger fe = new FieldExchanger(_lw);

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		String selEventName = (String)params.get("sel_event_name"); 

		// The resource ID will be available only if the resource
		// was selected from a list.
		String selResourceName = (String)params.get("sel_resource_name"); 
		String selResourceId = (String)params.get("lst_main"); 

		HashMap allTemplateHash = new HashMap();

		StringBuffer listItems = new StringBuffer();

		ResourceRegistry resreg = (ResourceRegistry)
			getCSRegistry("resource");
		TemplateRegistry treg = (TemplateRegistry)
			getCSRegistry("template");

		StringBuffer jsRoleIdArray = new StringBuffer();
		ArrayList skipList = new ArrayList();


		////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////
		// get all of the app's templates from the CS reg
		/*
		TemplateDefs templateList = null;
		try {
			templateList = treg.getAllTemplatesForRGKey(rgKey);
		} catch (Exception regEx) {
			tagTable.put("ERROR", "Problem while accessing app's template info");
			_lw.logDebugMsg("AddResourceTemplate: ERROR!", regEx);
			_lw.logErr("AddResourceTemplate: ERROR!", regEx);
			return tagTable;
		}
		*/

		////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////
		// get the role IDs for each resource-template pair
		try {

			_lw.logDebugMsg("ART: resource name: " + selResourceName);
			_lw.logDebugMsg("ART: resource ID: " + selResourceId);

			// use the resource ID to get the name 
			// if we don't have it yet
			if (selResourceName == null || selResourceName.equals("")) {
				ResourceDef rdef = 
					resreg.getResourceById(rgKey, 
							selResourceId);
				selResourceName = (String)rdef.getProperty("name");
				_lw.logDebugMsg("ART: got resource name: " + selResourceName);
			}


			boolean firstEntry=true;
			boolean firstRole=true;
			boolean foundOne=true;
			String tid, name, roleId;
			List roleList;
			HashMap rtMap = new HashMap();

			////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////
			// Prepare the rtMap for later use.  
			//    rtMap will contain lists of role IDs keyed by the
			//    the template ID to which the roles are assigned.

			// Get all the resource-template-role records
			List rtList = treg.getRTsByResourceName(
					rgKey, selResourceName);

			// Get all the template IDs for this resource
			allTemplateHash = (HashMap)treg.getIDToNameMappings(rgKey);
			Iterator allIt = null;


			// If there are no roles assigned to any templates 
			// for the selected resource, display a message saying that.
			if (rtList == null) {
				tmpPage.append(
					appTl.loadTemplate("app_config/no_resource_templates.template"));
				tagTable.put("art_msg", fe.doExchange(tmpPage.toString(), tagTable));

			} else {
				Iterator rtIt = rtList.iterator();
				while (rtIt.hasNext()) {

					HashMap rtHash = (HashMap)rtIt.next();
					tid = ((Integer)rtHash.get("template_id")).toString();
					roleId = ((Integer)rtHash.get("role_id")).toString();

					// get the old role list
					roleList = (List)rtMap.get(tid);

					// create a new role list if there wasn't one before
					if (roleList == null)
						roleList = new ArrayList();

					// add this role to the old role list
					roleList.add(roleId);

					// update the rtMap with the new list
					rtMap.put(tid, roleList);
				}

				////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////
				// Put the templates with assigned roles at the top.
				//    The allTemplateHash contains mappings of
				//    template ID to template name for every
				//    template record in the registry group.

				firstEntry = true;
				allIt = allTemplateHash.keySet().iterator();
				while (allIt.hasNext()) {

					tid = (String)allIt.next();

					// see if this template ID is mapped to
					// a role list
					roleList = (List)rtMap.get(tid);
					if (roleList != null) {

						name = (String)allTemplateHash.get(tid);

						// make a new list item
						listItems.append("\n\t<OPTION VALUE=\"")
							.append(tid)
							.append("\">")
							.append(name)
							.append("</OPTION>");

						// mark this template record for a skip
						skipList.add(tid);

						// the current template has assigned roles,
						// so add the roles to the javascript array
						// and remove the current template from the
						// allTemplateHash.
						Iterator roleIt = roleList.iterator();
						firstRole = true;
						StringBuffer jsRoleEntry = new StringBuffer();
						while (roleIt.hasNext()) {

							roleId = (String)roleIt.next();

							if (firstRole) {
								firstRole = false;
								jsRoleEntry.append("\"");
							} else {
								jsRoleEntry.append(",");
							}

							jsRoleEntry.append(roleId);
						}

						jsRoleEntry.append("\"");

						if (firstEntry) {
							firstEntry = false;
						} else {
							// add a comma before adding the role list
							jsRoleIdArray.append(",");
						}

						// add the current role list entry
						jsRoleIdArray.append(jsRoleEntry.toString());

					}  // end if roles are assigned

				}  // end while looping through all templates
			}


			// now add a separator between the templates
			listItems.append("<OPTION VALUE=\"\">")
				.append("======================")
				.append("</OPTION>\n");
			if (!firstEntry) {
				jsRoleIdArray.append(",");
			}
			jsRoleIdArray.append("\"\"");



			// Now add the rest of the templates...
			// 
			// HOW TO SORT BY NAME:
			// Add all of the template names
			// to a list, then sort the list, then make the HTML
			// <OPTION> tags with the sorted list.  Put the IDs
			// into a name / ID hash, then when digging out the 
			// names from the sorted list just get the ID from
			// the hash.
			List namesToSort = new ArrayList();
			HashMap nameToIdHash = new HashMap();

			// get the name list
			allIt = allTemplateHash.keySet().iterator();
			while (allIt.hasNext()) {

				tid = (String)allIt.next();
				if (skipList.contains(tid))
						continue;

				name = (String)allTemplateHash.get(tid);
				namesToSort.add(name);
				nameToIdHash.put(name, tid);
			}

			// sort the name list
			Object[] array = namesToSort.toArray();
			Arrays.sort(array);
			List sortedNames = new ArrayList();
			sortedNames.addAll(Arrays.asList(array));

			// build the HTML
			allIt = sortedNames.iterator();
			while (allIt.hasNext()) {

				name = (String)allIt.next();
				tid = (String)nameToIdHash.get(name);

				// make a new list item
				listItems.append("\n\t<OPTION VALUE=\"")
					.append(tid)
					.append("\">")
					.append(name)
					.append("</OPTION>");

				jsRoleIdArray.append(",\"\"");
			}


			// the main list has that spacer row at the bottom,
			// so update the javascript array to account for it.
			jsRoleIdArray.append(",\"\"");

		} catch (Exception regEx) {
			tagTable.put("ERROR", "Problem while getting template roles");
			_lw.logDebugMsg("AddResourceTemplate: ERROR!", regEx);
			_lw.logErr("AddResourceTemplate: ERROR!", regEx);
			return tagTable;
		}

		int numTemplates = allTemplateHash.size();

		tagTable.put("app_display_name", appDisplayName);
		if (numTemplates > 0) {

			// Get the CS template loader
			TemplateLoader csTl = (TemplateLoader)_wer
				.getProperty("cs_template_loader");


			//// Main list
			// load main_list.template from the CS jar
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

			String extraStuff = appTl.loadTemplate(
					"app_config/resource_template_extra_stuff.template");
			tagTable.put("extra_stuff", extraStuff);

			// build the main_list
			String mainListTemplate = 
				csTl.loadTemplate("main_list.template");
			tagTable.put("more_select_attribs", 
					"onChange=\"getTemplateData()\"");
			String swappedMainList = 
					fe.doExchange(mainListTemplate, tagTable);
			tagTable.put("main_list", swappedMainList);



		} else {
			// load no_templates.template
			tmpPage = new StringBuffer();
			tmpPage.append(
					appTl.loadTemplate("app_config/no_templates.template"));
			tagTable.put("main_list", fe.doExchange(tmpPage.toString(), tagTable));
		}


		///// original is below

		//tagTable.put("webevent_name", "aec_update_resource_template");
		//tagTable.put("button_label", "upload and add");
		tagTable.put("new_record", "true");

		// ////////////////////
		// get the redir URL from the params
		// so that the correct redir will 
		// happen after the template is added
		//String redirEvent = "choose_template";
		//tagTable.put("redir_event", redirEvent);

		tagTable.put("role_ids", jsRoleIdArray.toString());

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		tagTable.put("sel_event_name", selEventName);
		tagTable.put("sel_resource_name", selResourceName);

		tagTable.put("redir_event", "aec_add_resource_template");

		return tagTable; 
	}
}

