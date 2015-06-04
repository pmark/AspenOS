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
public class IconDef extends IdDef {

	public IconDef() {
		super();
		setDefName("Icon");
	}


	public IconDef(Map m) {
		super(m, "Icon"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String name = (String)getProperty("name");
		String alt = (String)getProperty("alt");
		String type = (String)getProperty("type");
		String defImg = (String)getProperty("default_image");
		String moImg = (String)getProperty("mouseover_image");
		String selImg = (String)getProperty("select_image");
		String style = (String)getProperty("style_class");
		String label = (String)getProperty("label");

		sb.append("(name,alt,type,default_image,mouseover_image,")
			.append("select_image,style_class,label) VALUES ('")
			.append(name).append("','")
			.append(alt).append("','")
			.append(type).append("','")
			.append(defImg).append("','")
			.append(moImg).append("','")
			.append(selImg).append("','")
			.append(style).append("','")
			.append(label).append("')");

		return sb.toString();
	}


}
