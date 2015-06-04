package org.aspenos.app.aosmailserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class MessageTemplateDef extends IdDef {

	public MessageTemplateDef() {
		super();
		setDefName("MessageTemplate");
	}


	public MessageTemplateDef(Map m) {
		super(m, "MessageTemplate"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String mt_id = getId();
		if (mt_id == null || mt_id.equals("null")) {
			mt_id = (String)getProperty("mt_id");
		}

		String mt_body = (String)getProperty("mt_body");
		String mt_name = (String)getProperty("mt_name");
		String mt_type = (String)getProperty("mt_type");

		String fields = 
			"mt_body,mt_name,mt_type) VALUES (";

		sb.append(" (");

		// Skip the ID if it's null
		if (mt_id == null || mt_id.equals("null")) {
			sb.append(fields).append("'");

		} else {
			sb.append("mt_id,")
				.append(fields)
				.append(mt_id)
				.append(",'");
		}

		sb.append(mt_body)
			.append("','")
			.append(mt_name)
			.append("','")
			.append(mt_type)
			.append("') ");

		return sb.toString();
	}

}
