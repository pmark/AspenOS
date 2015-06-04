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
public class MenuDef extends IdDef {

	public MenuDef() {
		super();
		setDefName("Menu");
	}


	public MenuDef(Map m) {
		super(m, "Menu"); 
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String name = (String)getProperty("name");
		String parentName = (String)getProperty("parent_name");
		String type = (String)getProperty("type");

		sb.append("(name,parent_name,type) VALUES ('")
			.append(name).append("','")
			.append(parentName).append("','")
			.append(type).append("') ");

		return sb.toString();
	}

}
