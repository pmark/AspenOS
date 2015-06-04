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
public class AssignTemplateRoles extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 
		String redirEvent = (String)params.get("redir_event"); 

		HashMap paramTags = new HashMap();

		paramTags.put("app_display_name", appDisplayName);
		paramTags.put("app_id", appIdStr);
		paramTags.put("reggrp_key", rgKey);
		paramTags.put("reggrp_name", rgName);

		String selEventName = (String)params.get("sel_event_name"); 
		String selResourceName = (String)params.get("sel_resource_name"); 

		paramTags.put("sel_event_name", selEventName);
		paramTags.put("sel_resource_name", selResourceName);
		

		_lw.logDebugMsg("Assigning roles to template");


		// Get the template_id from the list
		// Get the resource name from sel_resource_name
		// Get the role IDs from txt_role_id
		// Put each role_id to assign in a list
		ArrayList ridList = new ArrayList();
		String tid = (String)params.get("lst_main");
		String rids = (String)params.get("txt_role_id");
		StringTokenizer st = new StringTokenizer(rids, ",");
		while (st.hasMoreTokens()) {
			String tmpRoleId = st.nextToken().trim();
			ridList.add(tmpRoleId);
		}

		try {
			TemplateRegistry treg = (TemplateRegistry)
				getCSRegistry("template");

			_lw.logDebugMsg("Assigning " + ridList.toString() + 
					" to template #" + tid);
			treg.assignTemplateRoles(rgKey,
					selResourceName, new IdDef(tid), ridList);
		} catch (Exception ex) {
			String msg = "Unable to assign roles to template: " + 
				ex.toString();
			HashMap errTags = new HashMap();
			errTags.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return errTags;
		}

		String paramString = ServletTool.getParamString(paramTags);

		StringBuffer redirURL = new StringBuffer(_appBaseURI)
			.append("?webevent_name=")
			.append(redirEvent)
			.append("&")
			.append(paramString);

		_lw.logDebugMsg("redirecting to " + redirURL.toString());
		paramTags.put("REDIR_URL", redirURL.toString());

		paramTags.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		return paramTags; 

	}
}

