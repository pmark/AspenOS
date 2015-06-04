package org.aspenos.app.aoscontentserver.util;

import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;

public class ServletTool {

	/**
	 *
	 **/
	public static String getRequestURL(HttpServletRequest request) {

		StringBuffer sb = new StringBuffer();
		String path;

		path = request.getRequestURI();
		if (path != null)
			sb.append(path);

		path = request.getQueryString();
		if (path != null)
			sb.append("?")
				.append(path);

		path = sb.toString();
/////////////////////////////////
  //private String getAbsolutePath(String path) 
  //{ 
    int colon = path.indexOf(':'); 
    int slash = path.indexOf('/'); 

    if (slash == 0 || colon > 0 && (colon < slash || slash < 0))  {
      return path; 
	}

    String contextPath = request.getContextPath(); 
    String servletPath = request.getServletPath(); 

    int p = servletPath.lastIndexOf('/'); 

    if (p < 0) 
      path = contextPath + "/" + path; 
    else 
      path = contextPath + servletPath.substring(0, p + 1) + path; 

    //return Invocation.normalizeUri(path); 
  //} 
/////////////////////////////////

		return path;
	}


	/**
	 *
	 **/
	public static Map hashParams(HttpServletRequest request) {

		String name, value;
		HashMap hash = new HashMap();

		if (request == null)
			return hash;

		Enumeration e = request.getParameterNames();

		while (e.hasMoreElements()) {
			name = (String)e.nextElement();
			value = request.getParameter(name);
			hash.put(name, value);
		}

		return hash;
	}


	/**
	 *
	 **/
	public static Map hashParams(MultipartRequest request) {

		String name, value;
		HashMap hash = new HashMap();

		if (request == null)
			return hash;

		Enumeration e = request.getParameterNames();

		while (e.hasMoreElements()) {
			name = (String)e.nextElement();
			value = request.getParameter(name);
			hash.put(name, value);
		}

		return hash;
	}


	/**
	 * URL encodes all of the values in the map.
	 */
	public static String getParamString(Map params) {

		String paramString = params.toString();

		// remove the braces
		paramString = paramString.substring(1,paramString.length()-1);

		StringBuffer tmpParams = new StringBuffer();
		boolean first = true;
		StringTokenizer st = new StringTokenizer(paramString, ",");

		String key, value;
		while (st.hasMoreTokens()) {

			key = st.nextToken().trim();

			int pos = key.indexOf("=");
			if (pos == -1)
				continue;
			key = key.substring(0,pos);
			value = (String)params.get(key);

			if (value==null)
				value = "";

			if (first) {
				first = false;
			} else {
				tmpParams.append("&");
			}
			tmpParams.append(key)
				.append("=")
				.append(java.net.URLEncoder.encode(value));
		}

		paramString = tmpParams.toString();

		return paramString;
	}
}

