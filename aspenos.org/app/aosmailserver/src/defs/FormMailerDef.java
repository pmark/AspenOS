package org.aspenos.app.aosmailserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class FormMailerDef extends IdDef {

	public FormMailerDef() {
		super();
		setDefName("FormMailer");
	}


	public FormMailerDef(Map m) {
		super(m, "FormMailer"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String fm_id = getId();
		if (fm_id == null || fm_id.equals("null")) {
			fm_id = (String)getProperty("fm_id");
		}

		String fm_name = (String)getProperty("fm_name");
		String fm_desc = (String)getProperty("fm_desc");
		String send_to = (String)getProperty("send_to");
		String send_from = (String)getProperty("send_from");
		String formatter = (String)getProperty("formatter");
		String param_from = (String)getProperty("param_from");
		String use_param_from = ((Boolean)getProperty("use_param_from"))
			.toString();
		String site_name = (String)getProperty("site_name");

		String fields = 
			" fm_name,fm_desc,send_to,send_from,formatter,param_from,use_param_from,site_name) VALUES (";

		sb.append(" (");

		// Skip the ID if it's null
		if (fm_id == null || fm_id.equals("null")) {
			sb.append(fields).append("'");

		} else {
			sb.append("fm_id,")
				.append(fields)
				.append(fm_id)
				.append(",'");
		}

		sb.append(fm_name)
			.append("','")
			.append(fm_desc)
			.append("','")
			.append(send_to)
			.append("','")
			.append(send_from)
			.append("','")
			.append(formatter)
			.append("','")
			.append(param_from)
			.append("','")
			.append(use_param_from)
			.append("','")
			.append(site_name)
			.append("') ");

		return sb.toString();
	}

}
