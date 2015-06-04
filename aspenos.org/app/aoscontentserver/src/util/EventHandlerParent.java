package org.aspenos.app.aoscontentserver.util;

import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;



public class EventHandlerParent 
		implements EventHandler, ICSConstants {

	protected HashMap _tagTable = null;
	protected Properties _props = null;
	protected LoggerWrapper _lw = null;
	protected WebEventRequest _wer = null;
	protected String _appBaseURI = null;
	protected List _usedExtBundles = null;

	protected HashMap _checkedOutConnLists = 
		new HashMap();

//// CONSTRUCTORS /////////////////////////////////////////
	public EventHandlerParent() { 
		setBaseURI();
	}

	public EventHandlerParent(LoggerWrapper lw) {
		setLoggerWrapper(lw);
		setBaseURI();
	}

//// PRIMARY METHODS //////////////////////////////////////
	public void init(LoggerWrapper lw, WebEventRequest wer) { 
		setLoggerWrapper(lw); 
		_wer = wer;
		setBaseURI();
		setupTagTable();
	}

	public void init(LoggerWrapper lw, Properties p,
			WebEventRequest wer) {
		setLoggerWrapper(lw);
		setProperties(p);
		_wer = wer;
		setBaseURI();
		setupTagTable();
	}

	private void setBaseURI() {
		if (_wer != null) {

			_appBaseURI = _wer.getHttpRequest().getRequestURI();

			//_lw.logDebugMsg("Using context path + servlet path");
			//_appBaseURI = _wer.getHttpRequest().getContextPath() +
			//	_wer.getHttpRequest().getServletPath();
		}

		if (_appBaseURI == null)
			_appBaseURI = "/";
	}

	public String getServerAddress() {
		String defHost = "";
		String serverAddress = defHost;

		if (_wer != null) {
			String reqURL = HttpUtils.getRequestURL(
				_wer.getHttpRequest()).toString();

			int startPos, pos2, portPos, endPos;
			String port=null;

			// get the port, if there is one
			if (reqURL.toLowerCase().startsWith("http"))
				portPos = reqURL.indexOf(":", "https://".length());
			else
				portPos = reqURL.indexOf(":");

			if (portPos > 0) {
				endPos = reqURL.indexOf("/",portPos);
				if (endPos == -1)
					endPos = reqURL.length()-1;

				if (endPos > portPos)
					port = reqURL.substring(portPos+1,endPos);

				// clear everything to the right
				reqURL = reqURL.substring(0,portPos);
			}

			// at this point, if there was a port it's gone now
			// remove the protocol
			if (reqURL.toLowerCase().startsWith("http")) {
				startPos = reqURL.indexOf("/");
				if (startPos > 0 && startPos < reqURL.length()-1) {
					// get the second slash
					startPos = reqURL.indexOf("/",startPos+1);
					startPos++;
					
					// try to get the third slash
					endPos = reqURL.indexOf("/",startPos);
					if (endPos == -1) 
						endPos = reqURL.length();

					serverAddress = reqURL.substring(startPos,endPos);
				} else {
					serverAddress = defHost;
				}
			} else {
				serverAddress = defHost;
			}
			if (port != null && !port.equals(""))
				serverAddress += ":" + port;
		}

		return serverAddress;
	}

	protected void setupTagTable() {
		_tagTable = new HashMap();
		_tagTable.put("form_name", "main_form");
		_tagTable.put("form_action", _appBaseURI);
	}

	public HashMap handleEvent() { 
		_lw.logDebugMsg("EHP.handleEvent: THIS PROBABLY SHOULD NOT HAPPEN!");
		return _tagTable; 
	}


	public String getHeaderTemplate() 
	{ return CS_DEF_HEADER_TEMPLATE; }

	public String getFooterTemplate() 
	{ return CS_DEF_FOOTER_TEMPLATE; }


	public void setLoggerWrapper(LoggerWrapper lw) 
	{ _lw = lw; }

	public void setProperties(Properties p) 
	{ _props = p; }

	public boolean useStandardTemplate() 
	{ return false; }


	//// CONVENIENCE METHODS ///////////////////
	public HashMap getClonedTagTable() 
	{ return (HashMap)_tagTable.clone(); }

	public HashMap getRequestParams() 
	{ return (HashMap)_wer.getProperty("req_params"); }

	public TemplateLoader getTemplateLoader() 
	{ return (TemplateLoader)
		_wer.getProperty("app_template_loader"); }


	/**
	 * Returns the application's registry bundle.
	 * Use getAppRegistry() to retrieve individual 
	 * application registries INSTEAD OF using this
	 * method to get a bundle of registries (which
	 * you would then use to get registries).
	 * The reason is that retrieving registries 
	 * directly from the bundle does not use auto
	 * DB connection pooling.
	 */
	public RegistryBundle getAppRegistryBundle() 
	{ return (RegistryBundle)_wer.getProperty("app_reg_bundle"); }

	/**
	 * Returns the content server's registry bundle
	 * for this application.
	 * Use getCSRegistry() to retrieve individual 
	 * content server registries INSTEAD OF using this
	 * method to get a bundle of registries (which
	 * you would then use to get registries).
	 * The reason is that retrieving registries 
	 * directly from the bundle does not use auto
	 * DB connection pooling.
	 */
	public RegistryBundle getCSRegistryBundle() 
	{ return (RegistryBundle)_wer.getProperty("cs_reg_bundle"); }



	// ****************************************************
	// 2/6/01 additions
	// ****************************************************
	/**
	 * Gets a registry from the current application's (app_reg_bundle) 
	 * RegistryBundle.  Enables automatic DB connection pooling.
	 */
	public IRegistry getAppRegistry(String regName) {
		IRegistry reg = this.getRegistry("app_reg_bundle", regName); 
		return reg;
	}

	/**
	 * Gets a registry from the content server's (cs_reg_bundle) 
	 * RegistryBundle.  Enables automatic DB connection pooling.
	 */
	public IRegistry getCSRegistry(String regName) {
		return this.getRegistry("cs_reg_bundle", regName); 
	}

	/**
	 * Gets a registry from another application's RegistryBundle.  
	 * Use this method to easily communicate with databases
	 * from other applications.  The application's AOS system 
	 * name is used as the bundle name.  See the documentation for
	 * the application to get the system name, or just check it
	 * in the AOS System Console's DB..
	 *
	 * Enables automatic DB connection pooling.
	 */
	public IRegistry getExternalRegistry(String bundleName, String regName) {
		IRegistry reg = null;
		try {
			// get the reg bundle hash
			Map extBundles = (Map)_wer.getProperty("ext_reg_bundles");

			if (extBundles == null)
				_lw.logDebugMsg("ext_reg_bundles IS NULL!");


			// get the reg bundle
			RegistryBundle bundle = (RegistryBundle)
				extBundles.get(bundleName);

			if (bundle == null) {
				StringBuffer sb = new StringBuffer(92)
					.append("EHP: Cannot find registry bundle named '")
					.append(bundleName).append(" ' trying for registry '")
					.append(regName).append("'");

				throw new NullPointerException(sb.toString());
			}

			// get the registry by name
			reg = bundle.getRegistry(regName); 

			// check out a new DB connection
			String dbId = bundle.getDbId(regName);
			//_lw.logDebugMsg("EHP: getting DB conn for " + bundleName + " / " + regName)
			//_lw.logDebugMsg("EHP: _wer.getSid() = " + _wer.getSid());
			DbPersistence conn = bundle.getDbConn(_wer.getSid().getId(), dbId);

			// set the registry's DB connection to the checked out conn.
			reg.setDbConn(conn);
			if (_usedExtBundles == null)
				_usedExtBundles = new ArrayList();
			_usedExtBundles.add(bundle);

		} catch (SQLException sex) {
			String msg = "Unable to get registry.";
			_lw.logDebugMsg(msg, sex);
			_lw.logErr(msg, sex);
			reg = null;
		}
		return reg;
	}



	/**
	 * Event handlers that use registries MUST use this 
	 * method to enable automatic connection pooling.  
	 * NOTE that getAppRegistry() and getCSRegistry() 
	 * are provided for convenience.
	 */
	public IRegistry getRegistry(String bundleName, String regName) {

		IRegistry reg = null;
		try {
			// get the reg bundle
			RegistryBundle bundle = (RegistryBundle)
				_wer.getProperty(bundleName);

			if (bundle == null) {
				throw new NullPointerException(
						"No registry bundle named " + bundleName);
			}

			// get the registry by name
			reg = bundle.getRegistry(regName); 

			// check out a new DB connection
			String dbId = bundle.getDbId(regName);
			//_lw.logDebugMsg("EHP: getting DB conn for " + bundleName + " / " + regName)
			//_lw.logDebugMsg("EHP: _wer.getSid() = " + _wer.getSid());
			DbPersistence conn = bundle.getDbConn(_wer.getSid().getId(), dbId);

			// set the registry's DB connection to the 
			// checked out conn.
			reg.setDbConn(conn);

		} catch (SQLException sex) {
			String msg = "Unable to get registry.";
			_lw.logDebugMsg(msg, sex);
			_lw.logErr(msg, sex);
			reg = null;
		}

		return reg;
	}


	/**
	 * If the programmer is a good programmer (s)he will
	 * have used EventHandlerParent.getRegistry() 
	 * to retrieve registries so that automatic DB 
	 * connection pooling will work properly.  
	 * This method gets called when the 
	 * EventHandlerServlet is cleaning up,
	 * so it should never be called directly 
	 * from an EventHandler.  (that's why it
	 * is called *automatic* DB connection
	 * pooling.  Just use get*Registry() and 
	 * you don't ever have to worry about 
	 * the DB connections.
	 */
	public void returnDbConnections(String sid) {

		RegistryBundle bundle;

		bundle = (RegistryBundle)_wer.getProperty("app_reg_bundle");
		if (bundle != null)
			bundle.returnAllDbConnections(sid);

		bundle = (RegistryBundle)_wer.getProperty("cs_reg_bundle");
		if (bundle != null)
			bundle.returnAllDbConnections(sid);

		// return the external connections too
		if (_usedExtBundles != null) {
			Iterator bit = _usedExtBundles.iterator();
			while (bit.hasNext()) {
				bundle = (RegistryBundle)bit.next();
				bundle.returnAllDbConnections(sid);
			}
			_usedExtBundles = null;
		}
	}


	/**
	 * 
	 */
	public String getSystemTemplate(String tname, HashMap tagTable) {

		// get CS template loader and template
		TemplateLoader tl = (TemplateLoader)_wer.getProperty("cs_template_loader");
		String templ = tl.loadTemplate(tname);

		// swap!
		FieldExchanger fe = new FieldExchanger(_lw);
		return fe.doExchange(templ, tagTable);
	}

}



