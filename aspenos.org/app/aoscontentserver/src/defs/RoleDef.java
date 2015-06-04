package org.aspenos.app.aoscontentserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class RoleDef extends IdDef {

	public RoleDef() {
		super();
		setDefName("Role");
	}


	public RoleDef(Map m) {
		super(m, "Role"); 
	}


	/**
	 *
	 */
	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String name = (String)getProperty("name");
		String group = (String)getProperty("role_group");
		String vendor = (String)getProperty("vendor");

		String fields = " (name,role_group,vendor) VALUES ('";

		sb.append(fields)
			.append(name).append("','")
			.append(group).append("','")
			.append(vendor).append("') ");

		return sb.toString();
	}


}
