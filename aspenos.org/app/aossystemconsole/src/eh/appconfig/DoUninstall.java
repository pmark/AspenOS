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



/**
 * Carries out an application uninstall.
 */
public class DoUninstall extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"APPLICATION UNINSTALLATION");

		HashMap params = (HashMap)_wer.getProperty("req_params");
		String appDisplayName = (String)params.get("app_display_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 

		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);

		String resultMsg = "let's go!";

		try {


		} catch (Exception ex) {
			String msg = "Unable to uninstall application.";
			tagTable.put("ERROR", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return tagTable;
		}

		tagTable.put("uninstall_result", resultMsg);

		return tagTable; 
	}

}

