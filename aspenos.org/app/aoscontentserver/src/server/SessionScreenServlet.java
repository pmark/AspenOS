package org.aspenos.app.aoscontentserver.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.util.*;
import org.aspenos.mail.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.db.*;

/**
 *
 * SessionScreenServlet 
 *
 * Validates a user's session and role at every single request.
 *
 * Children of this class need to call initCSRegistries() before
 * accessing the content server registries and after getting the
 * CS registry group key.  All content server registries are 
 * instantiated here, and are accessible by children classes 
 * through the CS RegistryBundle.
 *
 * Children classes must also implement the getContent() method,
 * which takes a WebEventRequest.  This means that a child class
 * has the responsibility of handling events.
 *
 */
public abstract class SessionScreenServlet extends HttpServlet 
		implements ICSConstants {

	//===== Fields ============================================================
	public static final String CS_APP_ID = "0";

	protected static final String INIT_SID = "INIT";

	protected String __csRegGroupKey;
	protected String __uploadSaveDir;
	protected String __title = "";
	protected String __headerData = "";
	protected LoggerWrapper __lw = null;
	protected TemplateLoader __csTemplateLoader = null;
	protected boolean __checkSession;
	protected boolean __checkRoles;
	protected boolean __anonRole;
	protected boolean __headerNoCache = false;
	protected boolean __useMenu = false;
	protected int __cacheTimeout = -1;
	protected Hashtable __eventRoleCache = null;

	// static registries
	//protected static PrincipalRegistry	__principalReg;
	//protected static SessionRegistry 	__sessionReg;
	//protected static RoleRegistry 		__roleReg;

	// Registry Bundles
	protected static HashMap __appRegBundles = null;
	//protected static AppRegistry 		__appReg;  // 2/21 nix
	//protected static VendorRegistry 	__vendorReg;  // 2/21 nix
	//protected static RegistryBundle	__consoleRegBundle;  // 2/21 nix
	protected RegistryBundle __csRegBundle;

	// 

	// app-specific registries and bundle
	//protected WebEventRegistry 	__webEventReg;
	//protected TemplateRegistry 	__templateReg;
	//protected MenuRegistry 		__menuReg;
	//protected ResourceRegistry 	__resourceReg;

	protected static Hashtable __templateLoaders;
	//protected static CSRegistryFactory __csRegFactory;
	//protected static DbConnectionPool __csDbPool;
	//protected static DbConnectionPool __consoleDbPool;
	protected static String __aspenHomeDir = null;
	protected static String __csJarPath = null;
	protected static String __consoleJarPath = null;
	protected static ServerInit __serverInit = null;
	protected static int __serverType;

	// content server properties
	private Properties __csProps;
	private List __cookies;
	//private Properties __consoleProps;



	//===== Methods ============================================================
	/** Initialize the servlet */
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		try {

			ServerInit.log("SSS: starting init");
			__serverInit = ServerInit.getInstance();
			ServerInit.log("SSS: getting sys jars");
			__serverInit.fetchSystemJarPaths();
			__csJarPath = __serverInit.getContentServerJarPath();
			__consoleJarPath = __serverInit.getConsoleJarPath();
			__aspenHomeDir = __serverInit.getAspenHomeDir();
			__serverType = __serverInit.getServerType();
			ServerInit.log("SSS: got all ServerInit stuff");

			//__csRegFactory = new CSRegistryFactory();

			// Load the Content Server properties
			ServerInit.log("SSS: loading CS props from jar: " + __csJarPath);
			PropLoader csPropLoader = new PropLoader();
			__csProps = csPropLoader.loadFromJar(__csJarPath);
			ServerInit.log("SSS: got CS props");

			// Load the System Console properties
			ServerInit.log("SSS: loading CONSOLE props from jar: " + 
					__consoleJarPath);
			PropLoader consolePropLoader = new PropLoader();
			Properties consoleProps = 
				consolePropLoader.loadFromJar(__consoleJarPath);
			ServerInit.log("SSS: got CONSOLE props");

			// Get content server (cserver) specific properties
			csPropLoader.setPrefix("cserver.");

			// Set up the logging system
			ServerInit.log("SSS: initializing CS logs in dir " +
					csPropLoader.getString("log_dir"));

			if (__lw == null) {
				__lw = initLogs(
					csPropLoader.getString("log_dir"),
					csPropLoader.getString("msg_log"),
					csPropLoader.getString("err_log"),
					csPropLoader.getString("debug_log"),
					csPropLoader.getBoolean("do_debug",true));
			}

			ServerInit.log("SSS: Switching to CS logs in " +
				csPropLoader.getString("log_dir"));

			__lw.logDebugMsg("SSS.init", "===============================");
			__lw.logDebugMsg("Loading properties");


			//__lw.logDebugMsg("Initializing CS DB pool");
			//initCSDbPool(csPropLoader);
			if (__appRegBundles == null)
				__appRegBundles = new HashMap();

			__lw.logDebugMsg("Initializing SYSTEM CONSOLE registries");
			initSysConsoleRegistries(consoleProps);

			__lw.logDebugMsg("Caching content server templates");
			__csTemplateLoader = new TemplateLoader(__lw, __csJarPath);
			__csTemplateLoader.cacheTemplates();

			if (__templateLoaders == null)
				__templateLoaders = new Hashtable();
			__templateLoaders.put(CS_APP_ID, __csTemplateLoader);


			__lw.logDebugMsg("SSS.init",
					"Finished =====================\n\n");
		} catch (Exception initEx) {
			StringBuffer errMsg = 
				new StringBuffer("SSS.init:  ")
				.append("Problem while initializing SessionScreenServlet: " +
						initEx.toString());
			if (__lw == null) {
				ServerInit.log(errMsg.toString());
				initEx.printStackTrace();
			} else {
				__lw.logDebugMsg(errMsg.toString(), initEx);  
			}

			ServletException se = (ServletException)initEx.fillInStackTrace();
			throw se;
		}
	}



	/** Process HTTP GET request. */
	public final void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);
	}


	/** Process HTTP POST request. */
	public final void doPost(HttpServletRequest req,HttpServletResponse res)
		throws ServletException, IOException {

		//__lw.logDebugMsg("SSS.doPost", "Starting");

		String content = "";
		__cookies = null;

		try {
			//Look for a sid.  Will return null if DNE
			String sid = getSessionID(req, res);
			__lw.logDebugMsg("sid='" + sid + "'");

			//__lw.logDebugMsg("anon role: " + __anonRole);

			// Use the INIT_SID unless the app runs as anon_role
			boolean validSession = false;
			if (sid == null || sid.equals("") || sid.equals(CS_LOGOUT_SID)) {

				if (__anonRole) {
					HttpSession session=req.getSession(true);
					sid = session.getId();

					// write the AOS SID cookie
					Cookie sidCookie = new Cookie(CS_SESSION_COOKIE_KEY, sid);
					//__lw.logDebugMsg("Setting cookie: " + sidCookie.getValue());
					res.addCookie(sidCookie);
					__lw.logDebugMsg("New sid created for anon role");
					validSession = true;

				} else if (sid == null || sid.equals(CS_LOGOUT_SID)) {
					validSession = false;
					sid = INIT_SID;
					__lw.logDebugMsg("Setting session ID to INIT_SID");

				} else {
					validSession = true;
				}
			} else if (__anonRole) {
				validSession = true;
			}

			// Get an auto-pooled DB connection to this app's 
			// CS DB through its registry bundle.
			DbPersistence db = __csRegBundle.getDbConn(sid, AOSCS_DBID);

			// Retrieve the session reg and slap it with DB conn
			SessionRegistry sessionReg = 
				(SessionRegistry)__csRegBundle.getRegistry("session");
			sessionReg.setDbConn(db);

			// Retrieve the webevent reg and slap it with DB conn
			WebEventRegistry webEventReg = 
				(WebEventRegistry)__csRegBundle.getRegistry("webevent");
			webEventReg.setDbConn(db);

			// Retrieve the template reg and slap it with DB conn
			TemplateRegistry templateReg = 
				(TemplateRegistry)__csRegBundle.getRegistry("template");
			templateReg.setDbConn(db);

			// Retrieve the principal reg and slap it with DB conn
			PrincipalRegistry prinReg = 
				(PrincipalRegistry)__csRegBundle.getRegistry("principal");
			prinReg.setDbConn(db);

			if (sessionReg == null) {
				__lw.logDebugMsg("session reg is null!");
				throw new Exception("Session registry must not be null");
			}

			// if invalid session and not init, check status
			if (!validSession && !sid.equals(INIT_SID)) {
				validSession = sessionReg.isValid(sid);
			}

			if (__checkSession && !validSession && !__anonRole) {

				__lw.logDebugMsg("Invalid session");

				//Redirect to SessionLoginServlet
				String reqURL = ServletTool.getRequestURL(req);
				//String reqURL = (HttpUtils.getRequestURL(req)).toString();
				String redirectURL = getRedirURL(reqURL, req);

				__lw.logDebugMsg( 
						"\tInvalid sid: redirecting to login page.\n" +
						"\tRequest URL was: " + reqURL +
						"\n\tRedir URL is: " + redirectURL );
				
				__lw.logDebugMsg("no SID.  redirectURL='" + 
						redirectURL + "'");

				res.sendRedirect(redirectURL);

				__lw.logDebugMsg("Sent redirect and returned\n");
				__csRegBundle.returnAllDbConnections(sid);
				return;
			} 


			// Role ID 0 can be used as the anonymous role
			IdDef selRoleId = new IdDef("0");

			if (__checkSession) {
				// Get this session's principal's role
				selRoleId = sessionReg.getPrincipalRoleId(sid);
				if (selRoleId == null) {
					__lw.logDebugMsg("NULL principal role ID!");
					__lw.logErr("NULL principal role ID!");
				}
			}

			__lw.logDebugMsg("Session is valid (or I'm not checking it)");


			// Handle uploaded files and other multi-part requests.
			MultipartRequest mpr = 
				checkForMultipart(req);

			HashMap params = null;
			if (mpr == null) {
				params = (HashMap)ServletTool.hashParams(req);
			} else {
				params = (HashMap)ServletTool.hashParams(mpr);
			}

			boolean	hasAccess = true;
			boolean	doLogout = false;

			IdDef pidDef = null;
			List prinRoleIds = null;
			List eventRoleIds = null;

			// Get the requested event
			WebEventDef webEvent = getWebEvent(params, webEventReg);

			// Do a log out if the event is null
			if (webEvent == null) {
				if (__checkSession) {
					if (selRoleId != null && !selRoleId.getId().equals("0")) {
						sessionReg.setStatus(sid, CS_STATUS_LOGOUT);
						__lw.logDebugMsg("Setting session " + 
								sid + " to status " +
								CS_STATUS_LOGOUT);
					} else {
						__lw.logDebugMsg("Not changing session " + 
								sid + " to logout status");
					}
				}

				content = buildLogoutPage();
				doLogout = true;
			}

			if (doLogout) {
				// reset the SID cookie and kill the HttpSession
				Cookie c = new Cookie(CS_SESSION_COOKIE_KEY, CS_LOGOUT_SID);
				res.addCookie(c);
				if (req != null) {
					HttpSession sess = req.getSession(false);
					if (sess != null)
						sess.invalidate();
				}
				__lw.logDebugMsg("LOGOUT::Killing session");

			} else {

				if (__checkRoles) {

					if (selRoleId==null && !__anonRole) {
						__lw.logDebugMsg("Non-anonymous role must not be null!");
						throw new Exception("Selected role is null and not anon.  "
						+ "The role does not exist or is not in the session.");
					}

					if (webEventReg == null) {
						__lw.logDebugMsg("web event reg is null!");
						throw new Exception("Web event registry mustn't be null");
					}

					if (webEvent == null) {
						__lw.logDebugMsg("web event DEF is null!");
						throw new Exception("Problem getting the WebEventDef.");
					}

					// Can this role access this event?
					//// 3/13/01 multiple role support
					/*
					hasAccess = webEventReg.roleHasAccess(
							webEvent.getId(), selRoleId.getId());
					*/

					pidDef = sessionReg.getPrincipalId(sid);

					if (pidDef == null) {
						__lw.logDebugMsg("Principal from session is null!");
						throw new Exception("Cannot find principal in session.");
					}

					StringBuffer dbMsg = new StringBuffer(92)
						.append("Checking roles for event ")
						.append(webEvent.getId())
						.append(", prin ")
						.append(pidDef.getId())
						.append(", role ")
						.append(selRoleId.getId());
					__lw.logDebugMsg(dbMsg.toString());

					// Get the prin roles
					prinRoleIds = prinReg.getAllRoleIds(pidDef.getId());

					// Get or update roles from the event role cache
					String weid = webEvent.getId();
					eventRoleIds = (List)__eventRoleCache.get(weid);
					if (eventRoleIds == null) { 
						eventRoleIds = webEventReg.getEventRoles(__csRegGroupKey, weid);
						__eventRoleCache.put(weid, eventRoleIds);
					}
						

					// FOR ACCESS:
					// Match at least one role in the 
					// prin and event role lists.
					IdDef newSelRoleId = selectRoleAccess(
							prinRoleIds, eventRoleIds, selRoleId);

					if (newSelRoleId == null) {
						hasAccess = false;
					} else {
						hasAccess = true;

						// FOR CONTENT SELECTION:
						// If selected role is not in event roles,
						// choose arbitrary role in both lists
						if (!newSelRoleId.equals(selRoleId)) {
							prinReg.setSelectedRole(pidDef.getId(), newSelRoleId.getId());
							selRoleId = newSelRoleId;
						}
						__lw.logDebugMsg("Selected role: " + 
								newSelRoleId.getId());
					}
					/////////////////////////////////
				}


				if (hasAccess) {

					// ACCESS GRANTED!
					// Call getContent() for this web event request
					// __lw.logDebugMsg("Access granted!");

					IdDef sidDef = new IdDef(sid);

					// Build the WebEventRequest
					WebEventRequest webEventRequest = 
						new WebEventRequest(req, res, sidDef, pidDef, 
								selRoleId, webEvent);

					// Set the list of this user's roles
					webEventRequest.setProperty("prin_roles",prinRoleIds);

					// Put the request params in the event
					if (mpr == null) {
						webEventRequest.setProperty("req_params", params);
					} else {
						webEventRequest.setProperty("mpr", mpr);
						webEventRequest.setProperty("mpr_params", params);
						webEventRequest.setProperty("req_params", params);
					}

					// MUST HAVE A VALID WEB EVENT FOR THIS STUFF.
					//  Get all of this event's templates, based on role.
					//  The WebEventRequest uses these later
					if (webEvent != null) {
						getAllTemplates(webEventRequest, templateReg);
					}

					__lw.logDebugMsg("ACCESS ALLOWED -- getting content.");

					webEventRequest.setProperty("cs_reg_bundle", __csRegBundle);
					webEventRequest.setProperty("upload_dir", __uploadSaveDir);

					// make sure we're using the most recent TL
					TemplateLoader tl = (TemplateLoader)
						__templateLoaders.get(CS_APP_ID);
					if (tl != null)
						__csTemplateLoader = tl;

					content = getContent(webEventRequest);
				} else {

					// ACCESS DENIED
					// Display the proper 'access denied' page
					__lw.logDebugMsg("ACCESS DENIED");

					// Load the proper "access denied" template...

					StringBuffer tsb = new StringBuffer()
						.append("<FONT face='sans-serif'>")
						.append("<B>\nAccess Denied!</B><br><br>")
						.append("You do not have a role that allows ")
						.append("access to this page.\n")
						.append("<br><br><a href=\"")
						.append(getRedirURL(
									ServletTool.getRequestURL(req),
									req))
						.append("\">Go to login</a>")
						.append("</FONT>");

					content = tsb.toString();
				}
			}

		} catch (Exception e) {
			__lw.logDebugMsg("Caught exception:", e);
			__lw.logErr("Caught exception:", e);
			content += getErrorHTML();
		} 


		addCookiesToResponse(res);

		//Display Response
		res.setContentType("text/html");

		if (__headerNoCache) {
			__lw.logDebugMsg("Turning caching off");
			//res.setHeader("Pragma", "no-cache");
			res.setHeader("Cache-Control", "no-store");
		} else {
			__lw.logDebugMsg("Trying to turn caching on");
			//res.setHeader("Pragma", null);
			res.setHeader("Cache-Control", "max-age=" + __cacheTimeout);
		}


		PrintWriter out = new PrintWriter(res.getOutputStream());

		//Content
		__lw.logDebugMsg("SSS.doPost:","SENDING CONTENT TO BROWSER\n\n");
		out.println(content);

		//Done!
		res.flushBuffer();
		out.close();
	}
 

	/** 
	 * Retrieves all of this event's templates.
	 */
	private void getAllTemplates(WebEventRequest webEventRequest, 
			TemplateRegistry templateReg) 
			throws SQLException, Exception {

		TemplateDefs templates = 
			templateReg.getTemplatesByEvent(
					webEventRequest.getWebEventId().getId(),
					webEventRequest.getRoleId().getId());
		
		webEventRequest.setProperty("template_defs", templates);
	}


	/** 
	 * Handles multipart requests (like file uploads) by
	 * putting a MultipartRequest in the WebEventRequest.
	 */
	private MultipartRequest checkForMultipart(HttpServletRequest req) 
			throws IOException {

		String requestType = req.getContentType();
		//__lw.logDebugMsg("cfMulti(): request type: " + requestType);

		// Check for multipart/form-data
		if (requestType != null && 
				requestType.toLowerCase()
				.startsWith("multipart/")) {

			//__lw.logDebugMsg("cfMulti(): processing multi-part request");
			__lw.logDebugMsg("cfMulti(): saving uploads to " +
					new File(__uploadSaveDir).getAbsolutePath());

			MultipartRequest mpr = new MultipartRequest(
					req, __uploadSaveDir, __lw);

			__lw.logDebugMsg("cfMulti(): UPLOAD SUCCESSFUL");

			return mpr;
		} else {
			return null;
		}
	}


	/** 
	 * Get the WebEvent for this request.
	 //* Handles multipart requests (like file uploads) by
	 //* putting a MultipartRequest in the WebEventRequest.
	 */
	protected WebEventDef getWebEvent(HashMap params, 
			WebEventRegistry webEventReg) 
			throws SQLException {

		WebEventDef event = null;
		boolean doLogout = false;

		String val = (String)params.get(CS_WEBEVENT_ID_KEY);

		// Check for the event ID, then the event name
		if (val != null && !val.equals("")) {
			__lw.logDebugMsg("web event ID: " + val);
			event = webEventReg.getEventById(val);
		} else {
			val = (String)params.get(CS_WEBEVENT_NAME_KEY);
			__lw.logDebugMsg("web event name: " + val);
			if (val != null && !val.equals("")) {
				if (val.equals("logout")) 
					doLogout = true;
				else
					event = webEventReg.getEventByName(val);
			}
		}

		if (doLogout) {
			// get the app's custom logout event, if there is one
			//event = webEventReg.getEventById(CS_LOGOUT_EVENT_ID);
			event = null;

		} else if (event == null) {
			// If neither the ID nor the name is found,
			// load up the default event.
			event = webEventReg.getEventById(CS_DEF_EVENT_ID1);
			if (event == null) {
				event = webEventReg.getEventById(CS_DEF_EVENT_ID2);
			}
		}

		return event;
	}




	/** 
	 * Get the Session ID for this request.
	 */
	protected String getSessionID(HttpServletRequest req, HttpServletResponse res) {

		String sidKey = CS_SESSION_COOKIE_KEY;

		/* Get request cookies */
		Cookie[] cookies = req.getCookies();

		if (cookies == null) 
			return null;

		Cookie cookie = null;
		for (int i=0; i < cookies.length && cookie == null; i++) {
			String name = cookies[i].getName();
			if (name.equals(sidKey)) {
				cookie = cookies[i];
				break;
			}
		}

		// if no sid cookie, check the params
		if (cookie == null) {
			String sid = (String)req.getParameter(sidKey);

			if (sid == null)
				return null;

			 //Set the sid in the cookie
			__lw.logDebugMsg("Setting SID from param in cookie: " + sid);
			Cookie sidCookie = new Cookie(CS_SESSION_COOKIE_KEY, sid);
			res.addCookie(sidCookie);

			return sid;
		} else {
			return cookie.getValue();
		}
	}



	/**
	 * Builds a URL to the login servlet with the goto 
	 * parameter set to the current URL.  NOTE that the
	 * URL depends upon the login servlet to be addressed
	 * like this:
	 *   http://servername/<servlet_dir>/login
	 * That is, there must be one directory after the
	 * server name and between the "login" alias.
	 *
	 */
	private String getRedirURL(String reqURL, HttpServletRequest request) {

		if (reqURL == null)
			return "login";

		StringTokenizer st = new StringTokenizer(reqURL,"/");
		StringBuffer sb = new StringBuffer();

		if (st.countTokens() == 3) {
			sb.append(st.nextElement());
			sb.append("//");
			sb.append(st.nextElement());
			sb.append("/");
			sb.append(st.nextElement());
			sb.append("/");
		} else {
			String absPath = ServletTool.getRequestURL(request);

			// remove the requested resource name
			if (absPath.endsWith("/"))
				absPath = absPath.substring(0,absPath.length()-1);

			int pos = absPath.lastIndexOf("/");

			if (pos > 0)
				absPath = absPath.substring(0,pos);

			sb.append(absPath).append("/");

		}

		sb.append("login");
		sb.append("?");
		sb.append(CS_GOTO);
		sb.append("=");
		sb.append(URLEncoder.encode(reqURL));

		return sb.toString();
	}



	public static LoggerWrapper initLogs(
			String dir, String msg, String err, 
			String debug, boolean doDebug) {

		if (dir == null)
			dir = "";
		if (msg == null)
			msg = "default.msg";
		if (err == null)
			err = "default.err";
		if (debug == null)
			debug = "default.debug";


		dir = ServerInit.fixFilePath(dir);
		msg = ServerInit.fixFilePath(msg);
		err = ServerInit.fixFilePath(err);
		debug = ServerInit.fixFilePath(debug);

		if (!dir.equals("") && !dir.endsWith(File.separator))
			dir += File.separator;

		// Check for absolute or relative path.
		// Relative path root depends upon whether
		// Aspen is running in JAR or DIR mode.
		if (!dir.startsWith(File.separator)) {
			StringBuffer tmp = new StringBuffer()
				.append(__aspenHomeDir)
				.append(APP_DIR)
				.append(File.separator)
				.append(dir);
			dir = tmp.toString();
		}

		Logger logger = new Logger();
		logger.setMsgLog(dir+msg);
		logger.setErrLog(dir+err);
		logger.setDebugLog(dir+debug);
		logger.setDoDebug(doDebug);

		LoggerWrapper lw = new LoggerWrapper(logger);

		if (lw == null) {
			ServerInit.log("initLogs: Unable to set log dir: " + dir);
		} else {
			ServerInit.log("initLogs: Setting log dir: " + dir);
			lw.logDebugMsg("initLogs: Setting log dir: " + dir);
		}

		return lw;
	}


	/**
	 *
	 */
	private void reportError(String msg, HttpServletRequest request) {
		if (request != null) {
			HashMap params = (HashMap)ServletTool.hashParams(request);
			msg += "\n\nPARAMS:\n" + params.toString();
		}

		__lw.logErr(msg);
		__lw.logDebugMsg(msg);
		sendMailMsg(CS_ERROR_MAIL_RECIP, null, "SSServlet error",
				"SSServlet Error: " + msg, "error@aspenos.org");
	}


	/**
	 *
	 */
	public void sendMailMsg(String to, String bcc, 
			String subject, String body, String from) {
		try {
			Mailer m = new Mailer();
			m.sendPlain(body, subject, to, null, bcc, from);
		} catch (Exception ex) { 
			__lw.logDebugMsg("SSS.smm:  Unable to send message to '" + to + "':\n\t" + ex + 
				"\n\tFAILED MESSAGE:\n" + body + "\n\n"); 
		}
	}


	/**
	 *
	 */
	protected String getErrorHTML() {
		return __csTemplateLoader.loadTemplate("aos_error");
		/*
		return	"<PRE>\n" +
				"An error has occurred and your page cannot be served at " +
				"this time.\n\n" + 
				"</PRE>\n";
		*/
	}

	/**
	 *
	 */
/* 2/23 nix
	public static void initStandardRegistries()
			throws SQLException, ClassNotFoundException {

		//if (__csDbPool == null)
		//	return;

		//DbPersistence db = __csDbPool.getConnection(); // 2/23 nix

		// Get the registries
		//boolean returnConn = true;
		if (__principalReg == null) {
			//returnConn = false;
			__principalReg =
				__csRegFactory.getPrincipalRegistry();
		}

		if (__sessionReg == null) {
			//returnConn = false;
			__sessionReg =
				__csRegFactory.getSessionRegistry();
		}

		if (__roleReg == null) {
			//returnConn = false;
			__roleReg =
				__csRegFactory.getRoleRegistry();
		}

		// 2/23 nix
		//if (returnConn) {
		//	__csDbPool.returnConnection();
		}
	}
*/


	/**
	 * CS registries are special because the DB tables
	 * include the application's registry group.  
	 */
	protected void initCSRegistries(String regGroup)
			throws SQLException, ClassNotFoundException,
			IllegalAccessException, InstantiationException {

		// __csRegBundle is NOT static;
		// each app has its own instance
		if (__csRegBundle != null) {
			__lw.logDebugMsg("Using existing CS reg bundle");
			return;
		}

		// 2/23 nix
		//initStandardRegistries();

		// Get the DB connections
		//DbPersistence dbConn1 = __csDbPool.getConnection();
		//__csRegBundle.setDbConn("content_server", dbConn1);	// 2/6 nix


		// Go ahead and init the AOS Content Server DB
		CSRegistryFactory csRegFactory = new CSRegistryFactory(regGroup);
		RegistryInit ri = new RegistryInit(__lw);
		ri.setRegistryFactory(csRegFactory);

		__csRegBundle = new RegistryBundle();
		__csRegBundle = ri.initRegistryBundle(__csProps);


/////////////////////////////////////////////////////

		/*
		__csRegBundle.setRegistry("principal", 
				csRegFactory.getPrincipalRegistry());
		__csRegBundle.setRegistry("session", 
				csRegFactory.getSessionRegistry());
		__csRegBundle.setRegistry("role", 
				csRegFactory.getRoleRegistry());
		__csRegBundle.setRegistry("menu", 
				csRegFactory.getMenuRegistry(regGroup));
		__csRegBundle.setRegistry("resource", 
				csRegFactory.getResourceRegistry(regGroup));
		__csRegBundle.setRegistry("template", 
				csRegFactory.getTemplateRegistry(regGroup));
		__csRegBundle.setRegistry("webevent", 
				csRegFactory.getWebEventRegistry(regGroup));
		*/

/////////////////////////////////////////////////////
	}
			

	/**
	 *
	 */
	protected void initSysConsoleRegistries(Properties conProps)
			throws SQLException, ClassNotFoundException,
			IllegalAccessException, InstantiationException {

		RegistryBundle	consoleRegBundle = 
			(RegistryBundle)__appRegBundles.get(AOSCONSOLE_SYS_NAME);

		if (consoleRegBundle != null) {
			__lw.logDebugMsg("Using existing console reg. bundle");
			return;
		}

		// Go ahead and init the AOS System Console DB
		RegistryInit ri = new RegistryInit(__lw);
		consoleRegBundle = ri.initRegistryBundle(conProps);

		__appRegBundles.put(AOSCONSOLE_SYS_NAME, consoleRegBundle);
	}


	/** 
	 * This should be overridden to produce 
	 * a cool logout page.  If not, the boring 
	 * content server logout page will be used.
	 */
	public String buildLogoutPage() {
		return __csTemplateLoader.loadTemplate("logout.template");
	}


	/**
	 * Uses the AOS System Console's app registry
	 * to find the app's jar path, then uses the
	 * app's TemplateLoader to cache (or recache)
	 * the templates.
	 *
	 * If this static method is called outside of 
	 * the normal EventHandler framework, the
	 * DB connection to the System Console DB will
	 * not be auto-pooled.
	 *
	 * @param appId AOS System Console application ID
	 * @param sid AOS session ID for DB access
	 * @param loadIfNotLoaded true if the app's templates 
	 *   loaded even if the app is not running. 
	 *    
	 */
	public static void cacheTemplates(IdDef appId, 
				IdDef sid,
				boolean loadIfNotLoaded) 
				throws SQLException {

		// Get an auto-pooled DB connection to this app's 
		// console DB through the console registry bundle.
		RegistryBundle consoleRegBundle = (RegistryBundle)
			__appRegBundles.get(AOSCONSOLE_SYS_NAME);
		DbPersistence db = 
			consoleRegBundle.getDbConn(sid.getId(), AOSCONSOLE_DBID);

		// Retrieve the session reg and slap it with DB conn
		AppRegistry appReg = 
			(AppRegistry)consoleRegBundle.getRegistry("app");
		appReg.setDbConn(db);

		// Get this app's template loader
		if (__templateLoaders == null)
			__templateLoaders = new Hashtable();

		String appIdStr = appId.getId();
		TemplateLoader tl = (TemplateLoader)
			__templateLoaders.get(appIdStr);


		if (loadIfNotLoaded || tl != null) {

			AppDef app = appReg.getAppById(appId);
			String jarPath = (String)app.getProperty("jar_path");

			tl = new TemplateLoader(jarPath);
			tl.cacheTemplates();

			__templateLoaders.put(appIdStr, tl);
		}
	}


	/** 
	 * Determines if a list of principal's roles
	 * contain a role in a list of event's roles.
	 * If not, null is returned.
	 * If so, and the selected role is contained
	 * within the event's roles, return the selected
	 * role.  Else, return an arbitrary role found
	 * in both lists to be used as the selected role.
	 *
	 * The lists are expected to contain String values
	 * so that comparison is easy.
	 *
	 * @return null if access is denied or the proper
	 *   role to be selected.
	 */
	private IdDef selectRoleAccess(List prinRoleIds, 
			List eventRoleIds, IdDef selRoleId) {

		String prinRoleId;
		boolean chooseNewRole = !eventRoleIds.contains(
				selRoleId.getId());

		// add the default role
		prinRoleIds.add("0");

		Iterator pit = prinRoleIds.iterator();
		while (pit.hasNext()) {

			// get the next prin role
			prinRoleId = (String)pit.next();

			// check the role for access
			if (eventRoleIds.contains(prinRoleId)) {

				// access granted!
				if (chooseNewRole) {
					// return the default role
					// if the event has one,
					// else an arbitrary role
					if (eventRoleIds.contains("0"))
						return new IdDef("0");
					else
						return new IdDef(prinRoleId);
				} else {
					return selRoleId;
				}

			}  // end if
		}  // end while

		return null;
	}


	/** 
	 * 
	 */
	protected void addCookie(String key, String value) {
		__lw.logDebugMsg("SSS: adding cookie: " + key + "=" + value);
		if (key == null)
			return;
		if (__cookies == null)
			__cookies = new ArrayList();
		Cookie c = new Cookie(key, value);
		__cookies.add(c);
	}


	/** 
	 * 
	 */
	protected void addCookiesToResponse(HttpServletResponse res) {
		if (__cookies != null) {
			Iterator cit = __cookies.iterator();
			while (cit.hasNext()) {
				Cookie c = (Cookie)cit.next();
				res.addCookie(c);
			}
		}
	}

/*
/////////////////////////////////////
	public static PrincipalRegistry getPrincipalRegistry() {
		return (PrincipalRegistry)__csRegBundle.getRegistry("principal");
	}

	public static SessionRegistry getSessionRegistry() {
		return (SessionRegistry)__csRegBundle.getRegistry("session");
	}

	public static RoleRegistry getRoleRegistry() {
		return (RoleRegistry)__csRegBundle.getRegistry("role");
	}
/////////////////////////////////////
*/

	/** 
	 * 
	 */
	public abstract String getContent(WebEventRequest webEventRequest);


}


