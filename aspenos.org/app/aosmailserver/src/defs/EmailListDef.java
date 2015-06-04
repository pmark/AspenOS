package org.aspenos.app.aosmailserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class EmailListDef extends IdDef {

	public EmailListDef() {
		super();
		setDefName("EmailList");
	}


	public EmailListDef(Map m) {
		super(m, "EmailList"); 
	}


	public String getInsertSqlFandV() {
		return getSqlFieldsAndValues();
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String list_id = getId();
		if (list_id == null || list_id.equals("null")) {
			list_id = (String)getProperty("list_id");
		}

		String list_name = (String)getProperty("list_name");
		String list_sys_name = (String)getProperty("list_sys_name");
		String list_desc = (String)getProperty("list_desc");
		String site_name = (String)getProperty("site_name");

		String fields = 
			" list_name,list_sys_name,list_desc,site_name) VALUES (";

		sb.append(" (");

		// Skip the ID if it's null
		if (list_id == null || list_id.equals("null")) {
			sb.append(fields).append("'");

		} else {
			sb.append("list_id,")
				.append(fields)
				.append(list_id)
				.append(",'");
		}

		sb.append(list_name)
			.append("','")
			.append(list_sys_name)
			.append("','")
			.append(list_desc)
			.append("','")
			.append(site_name)
			.append("') ");

		return sb.toString();
	}

}
