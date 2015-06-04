package org.aspenos.app.aossystemconsole.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class RegGroupDef extends IdDef {

	public RegGroupDef() {
		super();
		setDefName("RegGroup");
	}


	public RegGroupDef(Map m) {
		super(m, "RegGroup"); 
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String reggrpId = (String)getProperty("reggrp_id");
		String reggrpName = (String)getProperty("reggrp_name");
		String reggrpKey = (String)getProperty("reggrp_key");
		String vendorName = (String)getProperty("vendor_name");

		String fields = " reggrp_name,reggrp_key,vendor_name) VALUES ('";

		sb.append(" (");

		// Skip the ID if it's null
		if (reggrpId == null || reggrpId.equals("null")) {
			sb.append(fields);

		} else {
			sb.append("reggrp_id,")
				.append(fields)
				.append(reggrpId)
				.append("','");
		}

		sb.append(reggrpName)
			.append("','")
			.append(reggrpKey)
			.append("','")
			.append(vendorName)
			.append("') ");

		return sb.toString();
	}

}
