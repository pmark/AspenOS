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
public class PrinRoleDef extends IdDef {

	public PrinRoleDef() {
		super("PrinRole");
	}


	public PrinRoleDef(Map m) {
		super(m, "PrinRole"); 
	}

	/**
	 * prinroles
	 */
	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String prinId = (String)getProperty("principal_id");
		String roleId = (String)getProperty("role_id");

		if (prinId==null || prinId.equals("null")) {
			prinId = (String)getProperty("principal_id");

			if (prinId==null || prinId.equals("null")) {
				return null;
			}
		}
		
		if (roleId==null)
			return null;

		sb.append(" (principal_id,role_id) VALUES ('")
				.append(prinId)
				.append("','")
				.append(roleId)
				.append("')");

		return sb.toString();
	}


}
