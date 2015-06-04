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
public class UpdateEvent extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 
		String newEventId = (String)params.get("txt_id"); 
		String newEventName = (String)params.get("txt_name"); 
		String newEventClassName = (String)params.get("txt_class_name"); 
		String newEventMenuName = (String)params.get("txt_menu_name"); 
		String newEventMenuSel = (String)params.get("txt_menu_sel_name"); 
		String origEventId = (String)params.get("orig_id"); 
		String newRecord = (String)params.get("new_record"); 
		boolean isNewRecord = false;
		if (newRecord != null && newRecord.equalsIgnoreCase("true"))
			isNewRecord = true;

		tagTable.put("sel_event_name", newEventName);
		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("sel_event_name", newEventName);

		HashMap hash = new HashMap();
		hash.put("webevent_id", newEventId);
		hash.put("name", newEventName);
		hash.put("classname", newEventClassName);
		hash.put("menu_name", newEventMenuName);
		hash.put("menu_sel_name", newEventMenuSel);
		WebEventDef wed = new WebEventDef(hash);

		WebEventRegistry evreg = (WebEventRegistry)
			getCSRegistry("webevent");



		try {
			if (isNewRecord)
				evreg.storeWebEventDef(rgKey, wed);
			else
				evreg.updateWebEventDef(rgKey, new IdDef(origEventId), wed);

		} catch (Exception ex) {
			String msg = "Unable to insert new event: " + ex.toString();
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
		}

		return tagTable; 
	}
}

