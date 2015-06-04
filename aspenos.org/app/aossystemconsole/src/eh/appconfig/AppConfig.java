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
 * Displays a page with configuration options
 * for a specific application.  The preceding 
 * page needs to send the following information.
 *
 * vendor_sys_name = System name of the application
 * rb_<value of vendor_sys_name> = Application ID
 * 
 */
public class AppConfig extends SysConEHParent {

	public HashMap handleEvent() {

		_lw.logDebugMsg("AppConfig: setting up cloned tag table");
		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "APPLICATION CONFIGURATION");

		// get app ID from params
		HashMap params = (HashMap)_wer.getProperty("req_params");

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
		String appSystemName = null;
		String rgKey = null, rgName = null;
		AppDef appDef = null;
		try {
			// get app name from app registry
			appDef = areg.getAppById(appIdDef);
			appDisplayName = (String)appDef.getProperty("display_name");
			appSystemName = (String)appDef.getProperty("system_name");

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

		HashMap paramMap = new HashMap();
		paramMap.put("app_display_name", appDisplayName);
		paramMap.put("app_system_name", appSystemName);
		paramMap.put("app_id", appIdStr);
		paramMap.put("reggrp_key", rgKey);
		paramMap.put("reggrp_name", rgName);

		String paramString = ServletTool.getParamString(paramMap);
		tagTable.put("param_string", paramString);
		tagTable.putAll(paramMap);


		return tagTable; 
	}
}

