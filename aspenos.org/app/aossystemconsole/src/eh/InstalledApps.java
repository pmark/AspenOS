package org.aspenos.app.aossystemconsole.eh;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;



public class InstalledApps extends SysConEHParent {


	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();

		tagTable.put("sub_titles", "INSTALLED APPLICATIONS");

		// Get the logger
		LoggerWrapper lw = (LoggerWrapper)_wer
			.getProperty("logger");

		AppRegistry areg = (AppRegistry)getAppRegistry("app");

		lw.logDebugMsg("InstalledApps: got AppRegistry");

		TemplateLoader appTemplateLoader = (TemplateLoader)
			_wer.getProperty("app_template_loader");

		StringBuffer installedAppList = new StringBuffer();
		StringBuffer vendorList = new StringBuffer();

		HashMap oneAppTagTable, vendorAppTagTable;
		FieldExchanger fe = new FieldExchanger(lw);


		// Load the four special templates
		String templateDir = "app_list/";
		String vendor_installed_apps_wrapper = 
			appTemplateLoader.loadTemplate(templateDir + 
					"vendor_installed_apps_wrapper.template"); 
		String vendor_installed_apps = 
			appTemplateLoader.loadTemplate(templateDir + 
					"vendor_installed_apps.template"); 
		String one_installed_app =
			appTemplateLoader.loadTemplate(templateDir + 
					"one_installed_app.template"); 
		String installed_app_sep = 
			appTemplateLoader.loadTemplate(templateDir + 
					"installed_app_sep.template"); 

		boolean firstVendor = true;

		try {
			// Get all vendors that have an app installed
			VendorDefs vendorDefs = areg.getInstalledAppVendors();

			// Get each installed app for each vendor
			Iterator vit = vendorDefs.iterator();
			while (vit.hasNext()) {
				VendorDef vendor = (VendorDef)vit.next();
				Integer vid = (Integer)vendor.getProperty("vendor_id");

				String vendorSysName = (String)
					vendor.getProperty("system_name");
				String vendorDisplayName = (String)
					vendor.getProperty("display_name");


				// Build the installed app list for this vendor
				installedAppList = new StringBuffer();
				AppDefs appDefs = areg.getInstalledApps(vid.toString());
				Iterator ait = appDefs.iterator();
				while (ait.hasNext()) {
					AppDef app = (AppDef)ait.next();

					String appId = ((Integer)
						app.getProperty("app_id")).toString();
					String appSysName = (String)
						app.getProperty("system_name");
					String appDisplayName = (String)
						app.getProperty("display_name");

					oneAppTagTable = new HashMap();
					oneAppTagTable.put("vendor_sys_name", vendorSysName);
					oneAppTagTable.put("app_sys_name", appSysName);
					oneAppTagTable.put("app_id", appId);
					oneAppTagTable.put("app_display_name", appDisplayName);

					installedAppList.append(
							fe.doExchange(one_installed_app, oneAppTagTable));
				}
				
				// Build this vendor's table
				vendorAppTagTable = new HashMap();
				vendorAppTagTable.put("installed_app_list", installedAppList.toString());
				vendorAppTagTable.put("vendor_sys_name", vendorSysName);
				vendorAppTagTable.put("vendor_display_name", vendorDisplayName);

				// Add a separator if this is not the first vendor
				if (!firstVendor)
					vendorList.append(installed_app_sep);
				else
					firstVendor = false;

				vendorAppTagTable.put("form_action", _appBaseURI);
				vendorList.append(
						fe.doExchange(vendor_installed_apps, vendorAppTagTable));
			}

			// Swap the vendor list into the wrapper template
			HashMap wrapperTagTable = new HashMap();
			wrapperTagTable.put("vendor_installed_apps", vendorList.toString());
			String bigList = 
				fe.doExchange(vendor_installed_apps_wrapper, wrapperTagTable);


			// This is the main tag table that gets swapped back
			// in the EHS.
			tagTable.put("vendor_installed_app_table", bigList);

		} catch (Exception ex) {
			lw.logDebugMsg("IntalledApps (eh) problem: ", ex);
		}

		return tagTable; 
	}
}

