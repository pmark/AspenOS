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
public class MenuButtonDef extends IdDef {

	public MenuButtonDef() {
		super();
		setDefName("MenuButton");
	}


	public MenuButtonDef(Map m) {
		super(m, "MenuButton");
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String name = (String)getProperty("menu_name");
		String iconName = (String)getProperty("icon_name");
		String eventName = (String)getProperty("event_name");
		String ordinal = (String)getProperty("ordinal");

		sb.append("(menu_name,icon_name,event_name,ordinal) VALUES ('")
			.append(name).append("','")
			.append(iconName).append("','")
			.append(eventName).append("','")
			.append(ordinal).append("') ");

		return sb.toString();
	}

}
