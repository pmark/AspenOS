package org.aspenos.app.aoscontentserver.util;

import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.defs.*;

public class WebEventRequest {

	public static final String SID 		= "sid";
	public static final String PID 		= "pid";
	public static final String ROLEID 	= "roleid";
	public static final String WEBEVENT = "webevent";
	public static final String HTTPREQ 	= "http_request";
	public static final String HTTPRES 	= "http_response";

	private HashMap _props;
	private String _templateDir = "templates/";


	//// CONSTRUCTOR //////////////////////////////////
	public WebEventRequest(HttpServletRequest httpReq, 
			HttpServletResponse httpRes, IdDef sid, 
			IdDef pid, IdDef roleid, WebEventDef event) {

		_props = new HashMap();

		if (sid != null) _props.put(SID, sid);
		if (pid != null) _props.put(PID, pid);
		if (roleid != null) _props.put(ROLEID, roleid);
		if (httpReq != null) _props.put(HTTPREQ, httpReq);
		if (httpRes != null) _props.put(HTTPRES, httpRes);
		if (event != null) _props.put(WEBEVENT, event);
	}


	//// ACCESS ///////////////////////////////////////
	public IdDef getSid() {
		return (IdDef)_props.get(SID);
	}

	public IdDef getPid() {
		return (IdDef)_props.get(PID);
	}

	public IdDef getRoleId() {
		return (IdDef)_props.get(ROLEID);
	}

	public WebEventDef getWebEvent() {
		return (WebEventDef)_props.get(WEBEVENT);
	}

	public HttpServletRequest getHttpRequest() {
		return (HttpServletRequest)_props.get(HTTPREQ);
	}

	public HttpServletResponse getHttpResponse() {
		return (HttpServletResponse)_props.get(HTTPRES);
	}


	public String getWebEventName() {
		WebEventDef event = getWebEvent();
		if (event == null)
			return null;
		return (String)event.getProperty("name");
	}

	public IdDef getWebEventId() {
		WebEventDef event = getWebEvent();
		if (event == null)
			return null;
		return new IdDef(((Integer)event.getProperty("webevent_id"))
				.toString());
	}



	public Cookie[] getAllCookies() {
		return getHttpRequest().getCookies();
	}

	public String getCookieValue(String key) {
		Cookie[] cookies = getAllCookies();
		if (cookies == null) 
			return null;

		Cookie cookie = null;
		for (int i=0; i < cookies.length && cookie == null; i++) {
			String name = cookies[i].getName();
			if (name.equals(key)) {
				cookie = cookies[i];
				break;
			}
		}

		if (cookie == null)
			return null;

		return cookie.getValue();
	}



	public Object getProperty(String key) {
		return _props.get(key);
	}

	public void setProperty(String key, Object value) {
		_props.put(key, value);
	}


	/**
	 * Appends all of the templates in order into one huge template.
	 */
	public String buildHugeTemplate(TemplateLoader loader) {

		StringBuffer hugeTemplate = new StringBuffer();

		TemplateDefs templateDefs = (TemplateDefs)
			getProperty("template_defs");

		if (templateDefs == null)
			return "";
		Iterator it = templateDefs.iterator();
		while (it.hasNext()) {
			TemplateDef tmp = (TemplateDef)it.next();
			String fileName = (String)tmp.getProperty("file_path");

			String t = loader.loadTemplate(fileName);

			if (t != null) {
				// Tack this template onto the end of the huge one
				hugeTemplate.append(t);
			} 

		}

		return hugeTemplate.toString();
	}


	public String toXML() {
		StringBuffer sb = new StringBuffer();

		Object tmp = getSid();
		if (tmp != null)
			sb.append(getSid().toXML());

		tmp = getPid();
		if (tmp != null)
			sb.append(getPid().toXML());

		tmp = getRoleId();
		if (tmp != null)
			sb.append(getRoleId().toXML());

		sb.append("<br><br>PLUS THE PROPS!");

		return sb.toString();
	}


	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("SID: " + getSid())
			.append("PID: " + getPid())
			.append("ROLEID: " + getRoleId())
			.append("PROPS: " + _props.toString());

		return sb.toString();
	}


}

