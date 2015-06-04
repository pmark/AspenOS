package org.aspenos.app.aossystemconsole.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class AppDef extends IdDef {

	public AppDef() {
		super();
		setDefName("App");
	}


	public AppDef(Map m) {
		super(m, "App"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String appId = getId();
		String vendorId = (String)getProperty("vendor_id");
		String systemName = (String)getProperty("system_name");
		String displayName = (String)getProperty("display_name");
		String jarPath = (String)getProperty("jar_path");
		String regGrpId = (String)getProperty("reggrp_id");

		String fields = 
			" vendor_id,system_name,display_name,jar_path,reggrp_id) VALUES ('";

		sb.append(" (");

		// Skip the ID if it's null
		if (appId == null || appId.equals("null")) {
			sb.append(fields);

		} else {
			sb.append("app_id,")
				.append(fields)
				.append(appId)
				.append("','");
		}

		sb.append(vendorId)
			.append("','")
			.append(systemName)
			.append("','")
			.append(displayName)
			.append("','")
			.append(jarPath)
			.append("','")
			.append(regGrpId)
			.append("') ");

		return sb.toString();
	}

}
