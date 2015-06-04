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
public class TemplateDef extends IdDef {

	public TemplateDef() {
		super();
		setDefName("Template");
	}


	public TemplateDef(Map m) {
		super(m, "Template"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String id = getId();
		if (id == null)
			id = (String)getProperty("template_id");

		String name = (String)getProperty("name");
		String filePath = (String)getProperty("file_path");

		String fields = " name,file_path) VALUES (";

		sb.append(" (");

		// Skip the ID if it's null
		if (id == null || id.equals("null") || id.equals("")) {
			sb.append(fields);

		} else {
			sb.append("template_id,")
				.append(fields)
				.append(id)
				.append(",");
		}

		sb.append("'").append(name).append("','")
			.append(filePath).append("') ");

		return sb.toString();
	}


	/**
	 *
	 */
	public String getUpdateFieldsAndValues() {

		String id = getId();
		if (id == null)
			id = (String)getProperty("template_id");

		String name = (String)getProperty("name");
		String filePath = (String)getProperty("file_path");

		boolean useTemplateId = false;

		if (name==null || name.equals("")) 
			name = "''";
		if (filePath==null || filePath.equals("")) 
			filePath = "''";

		// Skip the ID if it's null
		if (id != null && !id.equals("null") && !id.equals("")) {
			useTemplateId = true;
		} 

		StringBuffer sb = new StringBuffer();

		if (useTemplateId)
			sb.append(" template_id=").append(id).append(", ");

		// The name and classname fields better be there
		sb.append(" name='")
			.append(name)
			.append("', file_path='")
			.append(filePath)
			.append("'");

		return sb.toString();
	}
}
