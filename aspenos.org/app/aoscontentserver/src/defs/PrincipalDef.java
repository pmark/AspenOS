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
public class PrincipalDef extends IdDef {

	public PrincipalDef() {
		super("Principal");
	}


	public PrincipalDef(Map m) {
		super(m, "Principal"); 
	}


	/**
	 * principal
	 */
	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String username = (String)getProperty("username");
		String password = (String)getProperty("password");
		String host_site = (String)getProperty("host_site");
		String selected_role = (String)getProperty("selected_role");

		if (username==null) username = "";
		if (password==null) password = "";
		if (host_site==null) host_site = "";
		if (selected_role==null) selected_role = "";

		sb.append(" (username,password,host_site,selected_role) VALUES ('")
				.append(username)
				.append("','")
				.append(password)
				.append("','")
				.append(host_site)
				.append("','")
				.append(selected_role)
				.append("')");

		return sb.toString();
	}



}
