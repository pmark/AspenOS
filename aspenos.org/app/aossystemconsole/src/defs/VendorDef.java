package org.aspenos.app.aossystemconsole.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class VendorDef extends IdDef {

	public VendorDef() {
		super();
		setDefName("Vendor");
	}


	public VendorDef(Map m) {
		super(m, "Vendor"); 
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String vendorId = getId();
		String sysName = (String)getProperty("system_name");
		String displayName = (String)getProperty("display_name");

		String fields = " system_name,display_name) VALUES ('";

		sb.append(" (");

		// Skip the ID if it's null
		if (vendorId == null || vendorId.equals("null")) {
			sb.append(fields);

		} else {
			sb.append("vendor_id,")
				.append(fields)
				.append(vendorId)
				.append("','");
		}

		sb.append(sysName)
			.append("','")
			.append(displayName)
			.append("') ");

		return sb.toString();
	}

}
