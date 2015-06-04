package org.aspenos.app.aoscontentserver.server;

import java.util.*;
import java.sql.*;
import java.util.zip.*;
import java.io.*;
import java.lang.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;


/**
 * @class EventHandlerServlet
 * @see EventHandlerImpl
 *
 * This class is the parent of all application servlets that use Event Handlers
 * and session screening with roles.
 *<P>
 * In order to properly use this class, each app servlet's init() must first 
 * call the setAppJar(jarFile) method and then super.init(). They must 
 * also implement the getInitialTemplate() and 
 * getInitialTagTable() methods.
 *<P>
 * At that point, all of the processing will be handled by this servlet. 
 * each of their event handler classes.
 *<P>
 * Subclasses can also override some methods to customize the look and feel
 * of the application.  Here are those methods:
 *
 * getErrorHTML()
 * 
 *
 */
public abstract class EventHandlerServlet 
		extends SessionScreenServlet implements ICSConstants {

	protected static Map _extRegBundles = null;
	protected static List _usedExtBundles = null;

	protected Properties _appProps;
	protected String _appJarPath = "";
	protected String _appDir = "";
	protected String _appDirPath = "";
	protected String _appSysName = "";
	protected TemplateLoader _appTemplateLoader = null;
	protected RegistryBundle _appRegBundle = null;
	protected String _logLabel = "EventHandlerServlet";

	private boolean _useStandardPage;
	private String _headerTemplate = null;
	private String _footerTemplate = null;
	private String _appJarURI = null;
	private String _appIdStr = null;


	/**
	 *
	 */
	public void init(ServletConfig sc) throws ServletException {

		super.init(sc);

		RegistryBundle consoleRegBundle = null;
		try {

			__lw.logDebugMsg("EHS.init", "Server type = " + __serverType);
			getAppJar();

			__lw.logDebugMsg("EHS.init", "App dir: " + _appDir);
			__lw.logDebugMsg("EHS.init", "App path: " + _appDirPath);
			__lw.logDebugMsg("EHS.init", "Upload dir: " + __uploadSaveDir);
			__lw.logDebugMsg("EHS.init", "Getting app jar from: " +
				_appJarPath);


			PropLoader appPropLoader = new PropLoader();
			_appProps = appPropLoader.loadFromJar(_appJarPath);
			if (_appProps == null) {
				__lw.logErr("EHS.init",
					"unable to load app properties from jar at\n" + 
					_appJarPath);
				__lw.logDebugMsg("EHS.init",
					"unable to load app properties from jar at\n" + 
					_appJarPath);
			}

			//__lw.logDebugMsg("EHS.init", "props: " + _appProps.toString());


			// ==== INIT WITH PROPERTY FILE SETTINGS ====================

			// Start the logging system for this app
			__lw.logDebugMsg("EHS.init", "Setting app logs");
			__lw = initLogs( 
				(String)_appProps.getProperty("app.log_dir"), 
				(String)_appProps.getProperty("app.msg_log"), 
				(String)_appProps.getProperty("app.err_log"), 
				(String)_appProps.getProperty("app.debug_log"), 
				(new Boolean(_appProps.getProperty("app.do_debug")))
					.booleanValue());

			__lw.logDebugMsg("EHS.init","starting ===================");


			__anonRole 		= appPropLoader.getBoolean("app.anon_role",true);
			__checkRoles 	= appPropLoader.getBoolean("app.check_roles",false);
			__checkSession 	= appPropLoader.getBoolean("app.check_session",false);

			// __useMenu should be a property!


			// Get the vendor registry 
			consoleRegBundle = (RegistryBundle)
				__appRegBundles.get(AOSCONSOLE_SYS_NAME);

			// Use the INIT SID
			DbPersistence condb = 
				consoleRegBundle.getDbConn(INIT_SID, AOSCONSOLE_DBID);
			__lw.logDebugMsg("AOS Console DB ID: " + AOSCONSOLE_DBID);

			// Get the vendor registry
			VendorRegistry vendorReg = (VendorRegistry)
				consoleRegBundle.getRegistry("vendor");
			vendorReg.setDbConn(condb);

			// Get the app's registry group (RG) name,
			// and the app vendor's system name,
			// then get the system's representation
			// of the RG, which is the RG key.
			String csRegGroupKey = getRGKeyForName(vendorReg);
			__csRegGroupKey = csRegGroupKey;

			if (csRegGroupKey == null) {
				__lw.logDebugMsg("EHS.init", "NO RG KEY FOR THIS APP!");
				__lw.logErr("NO RG KEY FOR THIS APP!");
				throw new ServletException();
			} else {
				__lw.logDebugMsg("Init CS RG with key: " + csRegGroupKey); 
				initCSRegistries(csRegGroupKey);
			}

			__title = _appProps.getProperty("app.title");
			if (__title == null)
				__title = "Aspen Application";
			__lw.logDebugMsg("App title is: " + __title);

			__lw.logDebugMsg("EHS.init","Caching app templates");
			_appTemplateLoader = new TemplateLoader(__lw, _appJarPath);
			_appTemplateLoader.cacheTemplates();

			// store the app's template loader
			_appSysName = (String)_appProps.getProperty("app.system_name");
			if (_appSysName == null) {
				String msg = "APP INIT ERROR: app's config file does not " +
					"contain app.system_name, so some functionality will " +
					"not be supported.";
				__lw.logErr(msg);
				__lw.logDebugMsg(msg);
			} else {
				// Get the app registry 
				AppRegistry appReg = (AppRegistry)
					consoleRegBundle.getRegistry("app");
				appReg.setDbConn(condb);

				// Now get this app by name
				AppDef app = appReg.getAppBySysName(_appSysName);
				_appIdStr = ((Integer)
						app.getProperty("app_id")).toString();

				/** 6/12/01  Switching key from _appIdStr to _appSysName */
				__templateLoaders.put(_appSysName, _appTemplateLoader);

				__lw.logDebugMsg("EHS.init","Stored template loader for " +
						_appSysName + ", app ID = " + _appIdStr);
			}

			// Initialize this app's registries.
			// Make sure this happens AFTER the _appSysName is set
			initAppRegistryBundle();


			////////////////////////////////////////
			// Get the menu registry
			DbPersistence csdb = 
				__csRegBundle.getDbConn(INIT_SID, AOSCS_DBID);
			MenuRegistry menuReg = (MenuRegistry)
				__csRegBundle.getRegistry("menu");
			menuReg.setDbConn(csdb);

			// Cache the role IDs of each menu event
			if (__useMenu) {
				__eventRoleCache = menuReg.cacheMenuEventRoles();
			}

			// Update the main event role cache
			if (__eventRoleCache == null)
				__eventRoleCache = new Hashtable();

			////////////////////////////////////////
			__lw.logDebugMsg("EHS.init","done ====================");

		} catch (Exception e) {
			__lw.logErr("EHS.init","caught exception: ", e);
			__lw.logDebugMsg("EHS.init","caught exception: ", e);
			ServletException se = (ServletException)e.fillInStackTrace();
			throw se;
		} finally {
			if (_appRegBundle != null)
				_appRegBundle.returnAllDbConnections(INIT_SID);
			if (__csRegBundle != null)
				__csRegBundle.returnAllDbConnections(INIT_SID);
			if (consoleRegBundle != null)
				consoleRegBundle.returnAllDbConnections(INIT_SID);
		}
			
	} 



	/**
	 * getContent
	 *
	 */
	public String getContent(WebEventRequest webEventRequest) {

		//__lw.logDebugMsg("EHS.getContent","starting");

		String content = "";
		String redirectURL = "";
		String templateBeforeSwap = "";
		boolean doRedirect = false;
		HashMap tagTable = new HashMap();

		HttpServletResponse response = webEventRequest.getHttpResponse();

		// make sure we're using the most recent TL
		//__lw.logDebugMsg("checking for app ID: " + _appIdStr);
		if (_appIdStr != null) {
			TemplateLoader tl = (TemplateLoader)
				__templateLoaders.get(_appIdStr);

			if (tl != null) {
				_appTemplateLoader = tl;
				//__lw.logDebugMsg("got latest app TL");
			}
		}
		//__lw.logDebugMsg("should have good TL");


		// Event handlers will have access to all of the app's 
		// registries through its RegistryBundle.  If the user's
		// role is allowed access to AOS system registries, then
		// put them in too.
		webEventRequest.setProperty("ext_reg_bundles", _extRegBundles);
		webEventRequest.setProperty("app_reg_bundle", _appRegBundle);
		webEventRequest.setProperty("cs_reg_bundle", __csRegBundle);
		webEventRequest.setProperty("logger", __lw);
		webEventRequest.setProperty("app_template_loader", _appTemplateLoader);
		webEventRequest.setProperty("cs_template_loader", __csTemplateLoader);

		try {
			// GET THE CONTENT FROM THE EVENT HANDLER
			EventHandler handler = null;
			WebEventDef event = null;
			try {
				event = webEventRequest.getWebEvent();
				if (event != null) {

					// Get the event handler class name
					String ehClassName = (String)event.getProperty("classname");
					__lw.logDebugMsg("eh class: " + ehClassName);

					// Instantiate the event handler class
					handler = (EventHandler)
						Class.forName(ehClassName).newInstance();

					__lw.logDebugMsg("EHS.getContent","init event handler");
					handler.init(__lw, _appProps, webEventRequest);

					__lw.logDebugMsg("EHS.getContent",
							"== HANDLING EVENT #" + event.getId() +
							" ===========");
					tagTable = handler.handleEvent();
					__lw.logDebugMsg("EHS.getContent",
							"=============================");

					// The standard page needs to have a tag value for
					// the tag 'body' in order to work right.
					_useStandardPage = handler.useStandardTemplate();

					if (!_useStandardPage) {
						_headerTemplate = handler.getHeaderTemplate();
						_footerTemplate = handler.getFooterTemplate();
					}

					////////////////////////////////////////////////////
					//// TAG TABLE MESSAGES ////////////////////////////
					////////////////////////////////////////////////////
					// Check for a cookie to set
					if (tagTable.containsKey("SET_COOKIE")) {
						String key, ckey, cval, cookieNum;
						Iterator kit = tagTable.keySet().iterator();
						while (kit.hasNext()) {
							key = (String)kit.next();
							if (key.startsWith("COOKIE_KEY")) {
								cookieNum = key.substring("COOKIE_KEY".length());

								ckey = (String)tagTable.get(key);
								cval = (String)tagTable.get("COOKIE_VALUE"+
										cookieNum);
								addCookie(ckey, cval);
							}
						}
					}

					// Check for an event handler error
					if (tagTable.containsKey("ERROR")) {

						String msg = (String)tagTable.get("ERROR_MSG"); 
						if (msg == null) msg = (String)tagTable.get("ERROR"); 

						__lw.logErr("ERROR while handling event: "+msg);

						tagTable.put("error",msg);
						tagTable.put("error_msg",msg);
						tagTable.put("error_remedy",tagTable.get("ERROR_REMEDY"));

						templateBeforeSwap = _appTemplateLoader
							.loadTemplate("error");

						// If there is no app ERROR template,
						// use the content server default one.
						if (templateBeforeSwap == null 
								|| templateBeforeSwap.equals("")) {
							templateBeforeSwap = __csTemplateLoader
								.loadTemplate("error");
						}

					} else if (tagTable.containsKey("FAIL")) {

						__lw.logDebugMsg("User FAILURE while handling event: " +
								(String)tagTable.get("ERROR_MSG"));

						tagTable.put("error",tagTable.get("ERROR"));
						tagTable.put("error_msg",tagTable.get("ERROR_MSG"));
						tagTable.put("error_remedy",tagTable.get("ERROR_REMEDY"));

						templateBeforeSwap = _appTemplateLoader
							.loadTemplate("fail");

						// If there is no app ERROR template,
						// use the content server default one.
						if (templateBeforeSwap == null 
								|| templateBeforeSwap.equals("")) {
							templateBeforeSwap = __csTemplateLoader
								.loadTemplate("fail");
						}

					} else if (tagTable.containsKey("REDIR_URL")) {
						doRedirect = true;
						redirectURL = (String)tagTable.get("REDIR_URL");

					} else {


						// No error, continue 
						if (_useStandardPage) {
							// Use the standard Aspen template
							templateBeforeSwap = 
								_appTemplateLoader.loadTemplate("standard_aspen_page"); 
						} else {
							// Use the content server's choice of a template
							// which is based on the user's role
							templateBeforeSwap = 
								webEventRequest.buildHugeTemplate(_appTemplateLoader);
						}
					}  // end else
					////////////////////////////////////////////////////


					///////////////////////////////////////////////
					// Put the role based menu on, if there is one
					if (__useMenu) {
						List prinRoleIds = (List)
							webEventRequest.getProperty("prin_roles");
						

						String menuName = (String)event.getProperty("menu_name");
						__lw.logDebugMsg("getting menu: " + menuName);

						////////////////
						// Get an auto-pooled DB connection to this app's 
						// CS DB through its registry bundle.
						DbPersistence csdb = __csRegBundle.getDbConn(
							webEventRequest.getSid().getId(), AOSCS_DBID);

						// Retrieve the menu reg and slap it with DB conn
						MenuRegistry menuReg =
							(MenuRegistry)__csRegBundle.getRegistry("menu");
						menuReg.setDbConn(csdb);
						////////////////

						MenuDef menu = menuReg.getMenuByName(menuName);
						if (menu != null) {
							String mid = menu.getId();
							if (mid == null || mid.equals(""))
								mid = (String)menu.getProperty("menu_id");
							RoleBasedMenu rbm = 
								menuReg.getRoleBasedMenu(mid, prinRoleIds,
										__eventRoleCache);

							// the MENU_SEL param overrides the event's setting.
							String menuSel = (String)tagTable.get("MENU_SEL");
							if (menuSel == null || menuSel.equals("")) {
								menuSel = (String)event.getProperty("menu_sel_name");
							}

							//__lw.logDebugMsg("selected menu item: " + menuSel);
							String menuStr = buildMenu(rbm, menuSel);
							FieldExchanger menuSwapper = 
								new FieldExchanger(__lw);
							menuStr = menuSwapper.doExchange(menuStr, tagTable);
							tagTable.put("main_menu", menuStr);
						}
					}
					///////////////////////////////////////////////


					// Redirect if necessary
					if (doRedirect) {
						content = "Sending redirect...";
						__lw.logDebugMsg("EHS: Redirecting to " + redirectURL);
						addCookiesToResponse(response);
						response.sendRedirect(redirectURL);
					} else {
						content = buildPage(templateBeforeSwap, tagTable);
					}

				} else {
					throw new Exception("NULL WebEventDef is invalid");
				}  

			} catch (Exception eh_ex) {

				// An error occured while creating the event handler
				__lw.logDebugMsg(
					"getContent","Caught exception while " +
					"creating event handler.  Exception: ", 
					eh_ex);
				content = getErrorHTML();
			} finally {
				// Auto DB pool
				if (handler != null && webEventRequest != null) {
					__lw.logDebugMsg("returning event's DB conns");
					handler.returnDbConnections(webEventRequest.getSid().getId());
				}
			}

			//__lw.logDebugMsg("getContent","content successfully built");

		} catch (Exception e) {
			__lw.logErr("getContent", "Caught exception: ", e);
			content += getErrorHTML();
		}

		return content;
	}


	/**
	 * setAppDir
	 *
	 * @param String app directory
	 *
	 * This method must be called by any subclass of this class. <BR>
	 * The _appDir must be set prior to calling init()
	 */
	protected void setAppDir(String dir) {

		_appDir = dir;

		StringBuffer sb = new StringBuffer();

		// Make sure that the Aspen home dir is set
		if (__serverType == DIR_SERVER_TYPE) {
			if (__aspenHomeDir == null)
				__aspenHomeDir = DEF_DIR_MODE_HOME;
		} else {
			if (__aspenHomeDir == null)
				__aspenHomeDir = DEF_JAR_MODE_HOME;
		}

		// Check for trailing slashes
		if (!_appDir.endsWith(File.separator))
			_appDir += File.separator;

		if (!__aspenHomeDir.endsWith(File.separator))
			__aspenHomeDir += File.separator;


		_appDirPath = _appDir;


		// Check for a relative path to app dir
		if (!_appDir.startsWith(File.separator)) {
			sb = new StringBuffer(__aspenHomeDir)
				.append(APP_DIR)
				.append(File.separator);

			_appDirPath = sb.toString() + _appDir;
		}

		// Append the upload dir
		sb = new StringBuffer(_appDirPath)
			.append("data")
			.append(File.separator)
			.append("upload")
			.append(File.separator);

		__uploadSaveDir = sb.toString();

		File saveDir = new File(__uploadSaveDir);
		if (!saveDir.exists())
			saveDir.mkdirs();
	}


	/**
	 * setAppJar
	 *
	 * @param String jar name
	 *
	 * This method must be called by any subclass of this class. <BR>
	 * The _appJarPath must be set prior to calling init()
	 */
	protected void setAppJar(String jar) {
		_appJarURI = jar;
	}



	/**
	 * After this method is executed, _appJarPath will contain
	 * the path to this app's jar.  If Aspen is running in
	 * JAR mode (as opposed to DIR mode), the app's jar will
	 * be downloaded from a protected URL and stored somewhere
	 * on this web server's file system.
	 *
	 * When downloading jars, the path to the jar on the remote
	 * system relative to the AspenOS config directory will be 
	 * identical the path where the jar is saved on the local
	 * web server. For example, the remote jar file
	 * http://remote.server/aos/usr/local/lib/myapp.jar 
	 * would be downloaded and saved to /usr/local/lib/myapp.jar
	 * if setAppJar() was called as 
	 * setAppJar("/usr/local/lib/myapp.jar").  Note the slash
	 * before the path.  That makes it an absolute path on the
	 * local web server's file system.
	 */
	private void getAppJar() throws Exception {

		StringBuffer sb;
		__lw.logDebugMsg("App jar URI: " + _appJarURI);
		__lw.logDebugMsg("App dir path: " + _appDirPath);

		if (__serverType == DIR_SERVER_TYPE) {

			// Check if the given jar path is absolute or relative
			if (_appJarURI.startsWith(File.separator)) {
				// an absolute path
				_appJarPath = _appJarURI;
			} else {
				// a relative path
				sb = new StringBuffer()
					.append(_appDirPath)
					.append("lib")
					.append(File.separator)
					.append(_appJarURI);
				_appJarPath = sb.toString();
			}

		} else if (__serverType == JAR_SERVER_TYPE) {

			// The jar needs to be downloaded and stored
			// on this server's file system.

			if (__serverInit == null)
				__serverInit = ServerInit.getInstance();

			String saveDir, fetchURI;

			// Check if the given jar path is absolute or relative
			if (_appJarURI.startsWith(File.separator)) {
				// ABSOLUTE
				// Remove the jar name for the local save dir
				int pos = _appJarURI.lastIndexOf(File.separator);
				saveDir = _appJarURI.substring(0,pos);

				// Remove the first slash for the remote URI
				fetchURI = _appJarURI.substring(1);

			} else {

				// RELATIVE
				// The save dir is just the app dir path
				saveDir = _appDirPath;

				// The fetch URI uses the app dir name (not path)
				sb = new StringBuffer(_appDir)
					.append("lib/")
					.append(_appJarURI);
				fetchURI = sb.toString();
			}

			if (!saveDir.endsWith(File.separator))
				saveDir += File.separator;

			saveDir += "lib"+File.separator;
			saveDir = ServerInit.fixFilePath(saveDir);
			fetchURI = ServerInit.fixURI(fetchURI);

			__lw.logDebugMsg("Setting app save dir to: " + saveDir);
			__serverInit.setSaveDir(saveDir);

			__lw.logDebugMsg("Fetching app jar from: " + fetchURI);
			_appJarPath = __serverInit.fetchJar(fetchURI);
		}

	}







	//===== HTML Template Methods ==============================================
	/**
	 * Tries to load the template named by _headerTemplate from
	 * the app's templates, then by the content server's if not
	 * found. If _headerTemplate is null, "" is returned.
	 */
	protected String getHeaderHTML() {
		String str = null;
		if (_headerTemplate != null) {
			//__lw.logDebugMsg("Loading header: " + _headerTemplate);
			str = _appTemplateLoader.loadTemplate(_headerTemplate);
			if (str == null || str.equals("")) {
				str = __csTemplateLoader.loadTemplate(_headerTemplate);
			}
		}
		if (str == null) 
			str = "";
		return str;
	}


	/**
	 * Tries to load the template named by _footerTemplate from
	 * the app's templates, then by the content server's if not
	 * found. If _footerTemplate is null, "" is returned.
	 */
	protected String getFooterHTML() {
		String str = null;
		if (_footerTemplate != null) {
			//__lw.logDebugMsg("Loading footer: " + _footerTemplate);
			str = _appTemplateLoader.loadTemplate(_footerTemplate);
			if (str == null || str.equals("")) {
				str = __csTemplateLoader.loadTemplate(_footerTemplate);
			}
		}
		if (str == null) 
			str = "";
		return str;
	}


////////////////////////////////////////////////////////////////
	/**
	 *
	 */
	private String buildPage(String body, HashMap tags) {

		StringBuffer page;
		String header, footer;

		FieldExchanger theSwapper = 
			new FieldExchanger(__lw);

		if (_useStandardPage) {
			page = new StringBuffer(body);

		} else {

			page = new StringBuffer();
			header = getHeaderHTML();
			footer = getFooterHTML();

			// Piece together all of the templates
			if (header != null && header.length() > 0)
				page.append(header);

			if (body != null)
				page.append(body);

			if (footer != null && footer.length() > 0)
				page.append(footer);
		}

		// Add extra tags
		if (tags.get("title") == null)
			tags.put("title", __title);
		if (tags.get("extra_header_data") == null)
			tags.put("header_data", __headerData);

		// Swap 'em out!
		return theSwapper.doExchange(page.toString(), tags);
	}


	/**
	 *
	 */
	private boolean isValidRegistryGroup(String regGroupName) {
		boolean valid = true;

		// There better not be a space in the name
		if (regGroupName.indexOf(" ") != -1)
			valid = false;

		return valid;
		
	}


	/**
	 * Connects to all of this app's specified
	 * databases and loads the proper registry
	 * classes for those databases.  All DB
	 * and registry info is specified in the
	 * application's properties file.
	 */
	private void initAppRegistryBundle() {
		try {
			__lw.logDebugMsg("EHS.iar","Initializing app registries");
			RegistryInit ri = new RegistryInit(__lw);
			RegistryBundle rb = ri.initRegistryBundle(_appProps);

			// init this no matter what
			if (_extRegBundles == null) {
				__lw.logDebugMsg("making a new extRegBundles");
				_extRegBundles = new Hashtable();
			} else {
				__lw.logDebugMsg("extRegBundles: " + _extRegBundles.toString());
			}

			if (rb != null) {
				__appRegBundles.put(_appSysName, rb);
				_appRegBundle = rb;

				_extRegBundles.put(_appSysName, rb);
				__lw.logDebugMsg("putting RB for " + _appSysName +
						" into extRegBundles");
			}
		} catch (Exception e1) {

			String eMsg = "Unable to init app registries.";

			__lw.logErr("EHS.iar", eMsg.toString(), e1);
			__lw.logDebugMsg("EHS.iar", eMsg.toString(), e1);
		}
	}


	/**
	 * Gets the property file's representation of a
	 * a database ID for all databases used by this
	 * application.  The DB IDs are just used to 
	 * associate other database properties in the 
	 * props file with a specific database.
	 */
	private List getDbIdList() {

		List idList = new ArrayList();

		Enumeration keys = _appProps.propertyNames();
		String dbId;
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith("app.db.id.")) {
				dbId = key.substring("app.db.id.".length());
				idList.add(dbId);
			}
		}
		return idList;
	}

	
	/**
	 * Retrieves the registry group key from the app properties
	 * using the given registry group name and vendor system name.
	 */
	private String getRGKeyForName(VendorRegistry vendorReg) throws SQLException {

		// Get the RG and vendor from the app props
		String csRegGroupName = (String)
			_appProps.getProperty("aspen.registry.group"); 

		String vendorSysName  = (String)
			_appProps.getProperty("vendor.system_name");
		if (vendorSysName == null)
			vendorSysName = "default";

		__lw.logDebugMsg("Getting RG key with \n\tgroup name: " + 
				csRegGroupName + "\n\tvendor name: " + vendorSysName);

		// Retrieve the registry group
		__lw.logDebugMsg("Getting RG from DB");
		RegGroupDef rg = vendorReg.getRGByName(csRegGroupName, vendorSysName);
		__lw.logDebugMsg("DB call returned");

		if (rg == null) {
			__lw.logDebugMsg("INVALID REGISTRY GROUP NAME: " + csRegGroupName); 
			__lw.logErr("INVALID REGISTRY GROUP: " + csRegGroupName); 
			return null;
		} 

		String csRegGroupKey = (String)
			rg.getProperty("reggrp_key");

		return csRegGroupKey;
	}


	/** 
	 * 
	 */
	public String buildLogoutPage() {

		// Look for an app template
		String logoutTemplate = 
			_appTemplateLoader.loadTemplate("logout");

		// Use the CS template if there is no app template
		if (logoutTemplate == null || logoutTemplate.equals(""))
			logoutTemplate = __csTemplateLoader.loadTemplate("logout");

		return logoutTemplate;
	}


	/** 
	 * 
	 */
	public String buildMenu(RoleBasedMenu rbm, String selection) {

		StringBuffer menuStr = new StringBuffer();
		StringBuffer tmpMenu = new StringBuffer();
		StringBuffer finalMenu = new StringBuffer();
		String HREF_EVENT = "<A HREF=\"[tag:form_action]?webevent_name=";
		String HREF_URL = "<A HREF=\"";
		String IMG = 
			"\"><IMG BORDER=\"0\" align=\"absmiddle\" SRC=\"/aspenos/images/";
		String END = "\"></A>";
		String MENU_SEP1 = "\n<TR><TD align=\"center\">\n";
		String MENU_SEP2 = "</TD></TR>";


		RoleBasedMenu curMenu = rbm;

		while (curMenu != null) {
			menuStr = new StringBuffer();
			
			List btnList = curMenu.getButtons();
			Iterator bit = btnList.iterator();
			while (bit.hasNext()) {
				RoleBasedMenuButton rbmb = (RoleBasedMenuButton)
					bit.next();

				MenuButtonDef btn = rbmb.getButton();
				String eventName = (String)btn.getProperty("event_name");

				// now get the actual info from the icon
				IconDef icon = rbmb.getIcon();
				String alt = (String)icon.getProperty("alt");
				String type = (String)icon.getProperty("type");
				String defImg = (String)icon.getProperty("default_image");
				String moImg = (String)icon.getProperty("mouseover_image");
				String selImg = (String)icon.getProperty("select_image");
				String style = (String)icon.getProperty("style_class");
				String label = (String)icon.getProperty("label");

				//String link = (String)icon.getProperty("link");

				// if link == null
					menuStr.append(HREF_EVENT).append(eventName);
				// else if link.startsWith("?")
					//menuStr.append(HREF_EVENT).append(eventName)
					//	.append(link.substring(1));
				// else 
					//menuStr.append(HREF_URL).append(link);

				if (defImg != null) {
					menuStr.append(IMG);

					// See if this button is selected
					if (selImg != null && (selection != null && !selection.equals("")
								&& (selection.indexOf(eventName) != -1))) {
							menuStr.append(selImg);
					} else {
						menuStr.append(defImg);
					}

					// finish off the IMG tag
					menuStr.append("\" ALT=\"")
						.append(alt).append(END);
				} else { 
					menuStr.append(label).append("&nbsp;");
				}
			} // end while this menu has buttons


			tmpMenu = new StringBuffer();
			// get the parent menu, if there is one
			curMenu = curMenu.getParent();
			tmpMenu.append(MENU_SEP1);
			// prepend the current (top most) menu
			tmpMenu.append(menuStr.toString());
			tmpMenu.append(MENU_SEP2).append("\n\n");
			tmpMenu.append(finalMenu.toString());
			finalMenu = tmpMenu;

		} // end while there is a parent menu

		tmpMenu = new StringBuffer("\n\n<TABLE CELLPADDING=\"0\" CELLSPACING=\"0\">")
			.append(finalMenu.toString())
			.append("\n</TABLE>\n\n");

		return tmpMenu.toString();
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
/* *********************** I don't think this will ever work  7/13/01
	public static IRegistry getExternalRegistry(String bundleName, 
			String regName, String sid) throws SQLException {

		IRegistry reg = null;

		// get the reg bundle
		if (_extRegBundles == null) return null;
		RegistryBundle bundle = (RegistryBundle)
			_extRegBundles.get(bundleName);

		if (bundle == null) {
			StringBuffer sb = new StringBuffer(92)
				.append("EHS: Cannot find registry bundle named '")
				.append(bundleName).append(" ' trying for registry '")
				.append(regName).append("'");

			throw new NullPointerException(sb.toString());
		}

		// get the registry by name
		reg = bundle.getRegistry(regName); 

		// check out a new DB connection
		String dbId = bundle.getDbId(regName);
		DbPersistence conn = bundle.getDbConn(sid, dbId);

		// set the registry's DB connection to the checked out conn.
		reg.setDbConn(conn);
		if (_usedExtBundles == null)
			_usedExtBundles = new ArrayList();
		_usedExtBundles.add(bundle);

		return reg;
	}



	public static void returnDbConnections(String sid) {
		RegistryBundle bundle;
		if (_usedExtBundles != null) {
			Iterator bit = _usedExtBundles.iterator();
			while (bit.hasNext()) {
				bundle = (RegistryBundle)bit.next();
				bundle.returnAllDbConnections(sid);
			}
			_usedExtBundles = null;
		}
	}
***************************************************************** /

}
