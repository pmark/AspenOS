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
public class ResourceDef extends IdDef {

	public ResourceDef() {
		super();
		setDefName("Resource");
	}


	public ResourceDef(Map m) {
		super(m, "Resource"); 
	}


	/**
	 *
	 */
	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String id = getId();
		if (id == null)
			id = (String)getProperty("resource_id");

		String name = (String)getProperty("name");

		sb.append(" (");

		if (name==null) name = "CAN'T INSERT NULL NAME FOR RESOURCE!";

		// Skip the ID if it's null
		if (id == null || id.equals("null")
			|| id.equals("")) {
			sb.append("name) VALUES ('");

		} else {
			sb.append("resource_id,name) VALUES (")
				.append(id)
				.append(",'");

		}

		sb.append(name).append("')");

		return sb.toString();
	}


	/**
	 *
	 */
	public String getUpdateFieldsAndValues() {

		String resid = getId();
		if (resid == null)
			resid = (String)getProperty("resource_id");

		String name = (String)getProperty("name");
		boolean useResourceId = false;

		if (name==null || name.equals("")) 
			name = "''";


		// Skip the ID if it's null
		if (resid != null && !resid.equals("null") && !resid.equals("")) {
			useResourceId = true;
		} 

		StringBuffer sb = new StringBuffer();

		if (useResourceId)
			sb.append(" resource_id=").append(resid).append(", ");

		// The name and classname fields better be there
		sb.append(" name='").append(name)
			.append("'");

		return sb.toString();
	}

}
