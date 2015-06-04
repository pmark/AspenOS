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
public class UpdateResource extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 
		String selEventName = (String)params.get("sel_event_name"); 
		String newResourceId = (String)params.get("txt_id"); 
		String newResourceName = (String)params.get("txt_name"); 
		String origRId = (String)params.get("orig_id"); 
		String ordinal = (String)params.get("txt_ordinal"); 

		if (ordinal == null || ordinal.equals(""))
			ordinal = MAX_ORDINAL;

		boolean isNewRecord = false;
		String newRecord = (String)params.get("new_record"); 
		if (newRecord != null && newRecord.equalsIgnoreCase("true"))
			isNewRecord = true;

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("sel_event_name", selEventName);
		tagTable.put("sel_resource_name", newResourceName);

		HashMap hash = new HashMap();
		hash.put("resource_id", newResourceId);
		hash.put("name", newResourceName);
		ResourceDef rdef = new ResourceDef(hash);

		ResourceRegistry resreg = (ResourceRegistry)
			getCSRegistry("resource");

		try {
			resreg.setAutoCommit(false);

			// INSERT if new, else UPDATE
			if (isNewRecord) {
				_lw.logDebugMsg("Storing resource: " + newResourceName);
				resreg.storeResourceDef(rgKey, rdef);

				if (newResourceId == null || newResourceId.equals("")) {
					rdef = resreg.getResourceByName(rgKey, newResourceName);
					_lw.logDebugMsg("got new resource ID: " + rdef.getId());
				}

				_lw.logDebugMsg("Storing event-resource: " + selEventName);
				resreg.storeEventResource(rgKey, rdef, selEventName, ordinal);

			} else {

				_lw.logDebugMsg("Updating resource: " + newResourceName);
				resreg.updateResourceDef(rgKey, new IdDef(origRId), rdef);

				// only update the event resource 
				// if the resource ID has been changed
				if (!origRId.equals(newResourceId)) {
					_lw.logDebugMsg("Updating event-resource: " + 
							selEventName);
					resreg.updateEventResource(rgKey, 
							new IdDef(origRId), 
							rdef, selEventName, ordinal);
				}
			}

			resreg.commit();
			resreg.setAutoCommit(true);
			
		} catch (Exception ex) {
			String msg = "Unable to add/update new resource";
			try {
				_lw.logDebugMsg("Rolling back");
				resreg.rollback();
			} catch (Exception rex) {
				_lw.logDebugMsg("Unable to rollback", rex);
				_lw.logErr("Unable to rollback", rex);
				msg += "\n<BR><BR>Also unable to rollback";
			}

			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
		}


		return tagTable; 
	}
}

