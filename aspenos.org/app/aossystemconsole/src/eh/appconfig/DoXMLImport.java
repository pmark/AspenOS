package org.aspenos.app.aossystemconsole.eh.appconfig;

import java.util.*;
import java.io.*;
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
 * DoXMLImport.
 */
public class DoXMLImport extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "IMPORTING XML");

		String updateResult = "";
		String savedXML = null;
		File uploadedFile = null;
		HashMap params = null;
		boolean upload = false;
		
		// get the request params
		params = (HashMap)_wer.getProperty("mpr_params");

		if (params == null) {
			// get the updated html string
			upload = false;
			params = (HashMap)_wer.getProperty("req_params");
			savedXML = (String)params.get("xml"); 

		} else {
			// get the uploaded file
			upload = true;
			MultipartRequest mpr = (MultipartRequest)
				_wer.getProperty("mpr");
			_lw.logDebugMsg("retrieving uploaded file");
			uploadedFile = mpr.getFile("uploaded_file"); 
		}

		// params
		String appDisplayName = (String)params.get("app_display_name");
		String appSysName = (String)params.get("app_system_name");
		String appIdStr = (String)params.get("app_id");
		String rgKey = (String)params.get("reggrp_key");
		String rgName = (String)params.get("reggrp_name"); 
		String redirEvent = (String)params.get("redir_event"); 
		String selElement = (String)params.get("sel_element"); 


		try {
			CSRegistry reg = (CSRegistry)getCSRegistry(selElement);

			synchronized (reg) {
				String origKey = reg.getRegistryGroupName();

				reg.setLogger(_lw);
				reg.setRegistryGroupName(rgKey);
				updateResult = "got " + selElement + " registry with rgKey=" + rgKey;
				updateResult += "<BR>Now just implement importXML in that registry";
				_lw.logDebugMsg(updateResult);

				if (upload) {
					_lw.logDebugMsg("importing uploaded XML...");
					reg.importXML(uploadedFile, IRegistry.XML_ADD);
				} else {
					_lw.logDebugMsg("importing XML...");
					reg.importXML(savedXML, IRegistry.XML_ADD);
				}

				// put the registry back to the way it was before
				reg.setRegistryGroupName(origKey);
			}

		} catch (Exception ex) {
			String msg = "Unable to import XML: " + 
				ex.toString();
			tagTable.put("ERROR", ex);
			tagTable.put("ERROR_MSG", msg);
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			return tagTable;
		}


		tagTable.put("app_display_name", appDisplayName);
		tagTable.put("app_id", appIdStr);
		tagTable.put("reggrp_key", rgKey);
		tagTable.put("reggrp_name", rgName);
		tagTable.put("webevent_name", redirEvent);

		tagTable.put("update_result", updateResult);

		return tagTable; 
	}
}

