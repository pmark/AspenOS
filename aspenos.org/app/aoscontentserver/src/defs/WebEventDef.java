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
public class WebEventDef extends IdDef {

	public WebEventDef() {
		super();
		setDefName("WebEvent");
	}


	public WebEventDef(Map m) {
		super(m, "WebEvent"); 
	}


	/**
	 *
	 */
	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String weid = getId();
		if (weid == null)
			weid = (String)getProperty("webevent_id");

		String name = (String)getProperty("name");
		String className = (String)getProperty("classname");
		String menuName = (String)getProperty("menu_name");
		String menuSel = (String)getProperty("menu_sel_name");

		sb.append(" (");

		String fields = "name,classname,menu_name,menu_sel_name";
		if (name==null || name.equals("")) 
			name = "null";
		if (className==null || className.equals("")) 
			className = "null";


		// Check for an empty menu name
		if (menuName == null)
			menuName = "";
		if (menuSel == null)
			menuSel = "";

		// This thing goes on no matter what
		fields += ") VALUES (";

		// Skip the ID if it's null
		if (weid == null || weid.equals("null") || weid.equals("")) {
			sb.append(fields);

		} else {
			sb.append("webevent_id," + fields)
				.append(weid).append(",");
		}

		sb.append("'")
			.append(name).append("','")
			.append(className).append("','")
			.append(menuName).append("','")
			.append(menuSel).append("')");

		return sb.toString();
	}

	/**
	 *
	 */
	public String getUpdateFieldsAndValues() {

		String weid = getId();
		if (weid == null)
			weid = (String)getProperty("webevent_id");

		String name = (String)getProperty("name");
		String className = (String)getProperty("classname");
		String menuName = (String)getProperty("menu_name");
		String menuSel = (String)getProperty("menu_sel_name");
		boolean useEventId = false;

		if (name==null || name.equals("")) 
			name = "null";
		if (className==null || className.equals("")) 
			className = "";
		if (menuName==null)
			menuName = "";
		if (menuSel==null)
			menuSel = "";


		// Skip the ID if it's null
		if (weid != null && !weid.equals("null") && !weid.equals("")) {
			useEventId = true;
		} 

		StringBuffer sb = new StringBuffer();

		if (useEventId)
			sb.append(" webevent_id=").append(weid).append(", ");

		// The name and classname fields better be there
		sb.append(" name='").append(name)
			.append("', classname='").append(className)
			.append("', menu_name='").append(menuName)
			.append("', menu_sel_name='").append(menuSel).append("'");

		return sb.toString();
	}

}
