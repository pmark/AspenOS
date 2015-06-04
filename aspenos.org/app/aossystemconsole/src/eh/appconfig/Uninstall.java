package org.aspenos.app.aossystemconsole.eh.appconfig;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aossystemconsole.eh.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;



/**
 * Uninstalls an application.
 * 
 */
public class Uninstall extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "APPLICATION UNINSTALLATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");

		// get app ID from params
		String appIdStr = (String)params.get("app_id");
		String vendorSysName = null;
		if (appIdStr == null) {
			vendorSysName = (String)params.get("vendor_sys_name");
			appIdStr = (String)params.get("rb_" + vendorSysName);
		}

		AppRegistry areg = (AppRegistry)getAppRegistry("app");


		// The vendor system name and the app id
		// have been retrieved, so 
		IdDef appIdDef = new IdDef(appIdStr);
		String appDisplayName = null;
		String rgKey = null, rgName = null;
		try {
			// get app display name from app registry
			appDisplayName = areg.getAppDisplayName(appIdDef);

			// get app registry group key from app registry
			RegGroupDef rgDef = areg.getAppRG(appIdDef);
			rgKey = (String)rgDef.getProperty("reggrp_key");
			rgName = (String)rgDef.getProperty("reggrp_name");

		} catch (Exception regEx) {
			tagTable.put("error", "Problem while accessing app's event info");
			_lw.logDebugMsg("ChooseEvent: ERROR!", regEx);
			_lw.logErr("ChooseEvent: ERROR!", regEx);
			return tagTable;
		}

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		return tagTable; 
	}
}

