package org.aspenos.app.aoscontentserver.server;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.db.*;
import org.aspenos.mail.*;


/**
 * This servlet initiates a user's session.
 * Looks for a param called 'goto' set to a 
 * fully qualified redirect URL.
 */
public class LoginServlet extends HttpServlet 
		implements ICSConstants { 
	
	private static final String ERROR_MAIL_RECIP = 
		"error@aspenos.com";

	private static LoggerWrapper _lw = null;
	private static DbConnectionPool _dbPool = null;
	private static TemplateLoader _templateLoader = null;
	private static FieldExchanger _fieldExchanger = null;
	private static Properties _csProps = null;
	private static String _csJarPath = null;

	private static PrincipalRegistry _prinReg = null;
	private static SessionRegistry _sessionReg = null;

	protected PrintWriter _out = null;
	protected static ServerInit _serverInit = null;
	
	


	/**
	 * Initializes:
	 *   1) logs
	 *   2) registry factory
	 *   3) DB connection pool
	 **/
	public void init(ServletConfig config) throws ServletException {

		PropLoader propLoader = null;

		try {
			_serverInit = ServerInit.getInstance();
			_serverInit.fetchSystemJarPaths();
			_csJarPath = _serverInit.getContentServerJarPath();
			propLoader = new PropLoader();
			_csProps = propLoader.loadFromJar(_csJarPath);
		} catch (Exception ex) {
			throw new ServletException("Unable to fetch system jars: " + ex);
		}

		ServerInit.log("Login: Initializing logs");
		if (_lw == null) {
			_lw = SessionScreenServlet.initLogs(
				propLoader.getString("login.log_dir"),
				propLoader.getString("login.msg_log"),
				propLoader.getString("login.err_log"),
				propLoader.getString("login.debug_log"),
				propLoader.getBoolean("login.do_debug",true));
		}

		_lw.logDebugMsg("Starting to initialize LoginServlet");
		_lw.logDebugMsg("Got CS JAR path:\n\t" + _csJarPath);

		// Get content server DB-specific properties
		propLoader.setPrefix("app.");

		String dbURL = propLoader.getString("db.url." + AOSCS_DBID);

		// only build the URL if it is not given
		if (dbURL == null) {
			StringBuffer url = new StringBuffer();
			url.append(propLoader.getString("db.protocol." + AOSCS_DBID));
			if (!url.toString().endsWith(":"))
				url.append(":");
			url.append("//")
				.append(propLoader.getString("db.host." + AOSCS_DBID))
				.append("/")
				.append(propLoader.getString("db.dsn." + AOSCS_DBID));

			dbURL = url.toString();
		}

		String dbUsername 	= propLoader.getString("db.user." + AOSCS_DBID);
		String dbPassword 	= propLoader.getString("db.pwd." + AOSCS_DBID);
		String dbDriver 	= propLoader.getString("db.driver." + AOSCS_DBID);
		int dbInitialConn 	= propLoader.getInt("db.initialconns." + AOSCS_DBID,1);
		int dbMaxConn 		= propLoader.getInt("db.maxconns." + AOSCS_DBID,10);

		try {

			//_lw.logDebugMsg("Registering driver: " + dbDriver);
			//Class.forName (dbDriver); 
			//_lw.logDebugMsg("DB driver registered successfully!");
	
			_lw.logDebugMsg("Creating DB pool: " +
					"\n\tURL: " + dbURL +
					"\n\tUsername: " + dbUsername +
					"\n\tDriver: " + dbDriver);

			_dbPool = new DbConnectionPool(_lw,
					dbURL,
					dbUsername, dbPassword,
					dbDriver,
					dbInitialConn, dbMaxConn);

			_lw.logDebugMsg("DB pool init complete: " + dbURL);

			_prinReg = new PrincipalRegistry();
			_sessionReg = new SessionRegistry();

			_lw.logDebugMsg("Got registries");

			ServerInit.log("Login: DB pool init complete");

		} catch (Exception e) {
			_lw.logDebugMsg("DbConnectionPool init failed:  ", e);
		}

		_templateLoader = new TemplateLoader(_lw, _csJarPath);
		_fieldExchanger = new FieldExchanger(_lw);

		_lw.logDebugMsg("Done initializing LoginServlet");
	}



	/**
	 *
	 **/
    public void doGet (HttpServletRequest request, 
			HttpServletResponse response) 
		throws ServletException, IOException {
		doPost(request, response);
	}
	
	
    /**
	 *
     */
    public void doPost (HttpServletRequest request, 
			HttpServletResponse response) 
			throws ServletException, IOException {

		String pageTemplate = null;
		try {
			_lw.logDebugMsg("doPost: Starting new login");
			pageTemplate = buildLoginPage(request,response);
		} catch (Exception ex) {
			_lw.logDebugMsg("doPost: Caught: ", ex);
			pageTemplate = ex.toString();
		}

		response.setContentType("text/html");

		// Make sure the page doesn't get cached
		response.setHeader("Pragma", "no-cache");
		//response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Cache-Control", "no-store");

		_out = response.getWriter();
		_out.println( pageTemplate );
		_out.close();
	}




///// Helper Methods ///////////////////////////////////////////////////
	/**
	 * Returns the appropriate HTML page (or template) based
	 * on the status of the login request.
	 */
	private String buildLoginPage(HttpServletRequest request,
			HttpServletResponse response) 
			throws SQLException, IOException {

		HttpSession session=request.getSession(false);
		HashMap params=null;
		String loginTemplateName=null;
		String username=null, password=null, host_site=null;
		String pageTemplate=null;
		String gotoURL=null;


		// Store the request params in the HashMap
		params = (HashMap)ServletTool.hashParams(request);

		//_lw.logDebugMsg("Login", "\nparams: " + params + "\n");

		username = (String)params.get("username");
		password = (String)params.get("password");
		host_site = (String)params.get("host_site");
		gotoURL = (String)params.get(CS_GOTO);

		// Display the login form if there was no session
		if (session == null || username == null) {

			if (session == null)
				_lw.logDebugMsg("Login", "Creating a new session");

			// Create a new session
			session = request.getSession(true);
			_lw.logDebugMsg("Login", "Session ID: " + session.getId());

			// Store the goto URL in the session
			if (gotoURL == null) {
				session.setAttribute(CS_GOTO, "null");
			} else {
				session.setAttribute(CS_GOTO, gotoURL);
			}

			_lw.logDebugMsg("Null session; Getting login template");
			loginTemplateName = getLoginTemplateName(session, params);
			pageTemplate = getLoginTemplate(loginTemplateName);
			HashMap tagTable = new HashMap();
			StringBuffer sb = new StringBuffer()
				.append(request.getContextPath())
				.append(request.getServletPath());
			tagTable.put("form_action", sb.toString()); 
			pageTemplate = _fieldExchanger.doExchange(pageTemplate, tagTable);

		} else {

			DbPersistence dbConn = null;
			try {
				dbConn = _dbPool.getConnection();
			} catch (Exception dbe) {
				return getServletErrorTemplate(
						"Failed to get DB conn from pool: " + 
						dbe);
			}

			try {

				_sessionReg.setDbConn(dbConn);
				_prinReg.setDbConn(dbConn);

				if (_sessionReg == null || _prinReg == null) {
					_lw.logErr("Login", "Null prin or session registry");
					_lw.logDebugMsg("Login", "Null prin or session registry");
					//_lw.logDebugMsg("Returning DB connection");
					_dbPool.returnConnection(dbConn);
					return getServletErrorTemplate(
							"Problem initializing registry service.");
				}

				IdDef prin = null;
				String sid = session.getId();

				_lw.logDebugMsg("Login", "Validating login for session: "+sid);

				// Check for invalid form entries and show error if bad
				if ((username == null || password == null) ||
						(username.equals("") || password.equals(""))) { 
					_lw.logDebugMsg("Login", "Invalid login");
					//_lw.logDebugMsg("Returning DB connection");
					_dbPool.returnConnection(dbConn);
					return getServletErrorTemplate(
							"Invalid username or password.");
				}


				_lw.logDebugMsg("Login", "Registering a new session...");
				//_sessionReg.createNewLogin(sid);
				try {
					prin = _prinReg.validatePrincipal(
							username, password, host_site);
				} catch (NoSuchUserException nsue) {
					_lw.logDebugMsg("Login", "No such user:  " + nsue);
				} catch (WrongPasswordException wpe) {
					_lw.logDebugMsg("Login", "wrong password:  " + wpe);
				}


				_lw.logDebugMsg("Login", "Authentication test complete");

				// Check for an invalid login
				if (prin == null) {

					_lw.logDebugMsg("Login", "Login is invalid!");
					//_sessionReg.setStatus(sid, CS_STATUS_INVALID_LOGIN);

					//// Load the invalid login template that 
					//// correspondes to loginTemplateName
					_lw.logDebugMsg("Null principal; Getting login template");
					loginTemplateName = getLoginTemplateName(session, params);
					pageTemplate = getInvalidLoginTemplate(loginTemplateName);

				} else {

					// User is authorized
					_lw.logDebugMsg("Login", "Principal #" +
							prin.getId() + " has been authenticated");
					_sessionReg.setPrincipalAndStatus(sid, 
							prin.getId(), CS_STATUS_ACTIVE_AUTHENTICATED);


					// Set the sid in the cookie
					Cookie sidCookie = new Cookie(CS_SESSION_COOKIE_KEY, sid);
					_lw.logDebugMsg("Login", "Setting cookie: " + sidCookie.getValue());
					response.addCookie( sidCookie );

					// Get the goto URL
					String redirURL = getRedirURL(session, request, params);

					//response.setHeader("Pragma", "no-cache");
					//response.setHeader("Cache-Control", "no-cache");
					response.setHeader("Cache-Control", "no-store");

					_lw.logDebugMsg("Login", "Sending redirect to: " + redirURL + "\n\n");
					response.sendRedirect( redirURL );

				} // end else - validated principal


			} finally {
				// Free the DB conn no matter what!
				//_lw.logDebugMsg("Returning DB connection");
				_dbPool.returnConnection(dbConn);
			}

		} // end else - valid session exists - login submission

		return pageTemplate;
	}


	/**
	 * Extracts 'goto' from the request parameters and
	 * sets that URL so that the user's browser will be
	 * redirected there after a successful login.
	 * If 'goto' is not found as a parameter, this method
	 * looks in the HttpSession object to see if it was 
	 * stored there originally.
	 */
	private String getRedirURL(HttpSession session, 
			HttpServletRequest request, Map params) {

		// Check the params first
		String redirURL = (String)params.get(CS_GOTO);

		if (redirURL == null) {
			// Wasn't in the params, so check the session
			redirURL = (String)session.getAttribute(CS_GOTO);
		}

		if (redirURL == null || redirURL.equals("null")) {
			// No goto URL, so just go to the default goto URL
			_lw.logDebugMsg("Login", "No goto URL; redir to default: " +
					CS_DEFAULT_GOTO_URL);
				redirURL = CS_DEFAULT_GOTO_URL;
		} else {
			// Found a goto URL 
			_lw.logDebugMsg("Login", "Found goto URL: " + redirURL);
					//"putting this in the session: " + redirURL);
		}
		
		return redirURL;

/* ///////////////// This was the original getRedirURL code /////////
		if (redirURL == null || redirURL.equals("")) {
			redirURL = (String)params.get(CS_GOTO);

			// Try 'goto_url' if 'goto' isn't there
			if (redirURL == null)
				redirURL = (String)params.get("goto_url");

			// If both 'goto_url' and 'goto' are not there
			// set the redirect to CS_DEFAULT_GOTO_URL
			if (redirURL == null) {
				// No goto, so just go to the default URL
				_lw.logDebugMsg("No goto_url; redir to default: " +
						CS_DEFAULT_GOTO_URL);
				redirURL = CS_DEFAULT_GOTO_URL;
			} else {
				// Found a goto URL 
				_lw.logDebugMsg("Found goto URL; " +
						"putting this in the session: " + redirURL);
			}

			// Put the goto_url in the session to redirect later
			session.putValue(CS_GOTO, redirURL);
		}
*/

/* ////////////////// This used to be up above ////////////////
		if (redirURL == null) {
			_lw.logDebugMsg("There should have been a goto_url in the " +
					"session.  Redirecting to default goto...");
			redirURL = CS_DEFAULT_GOTO_URL;
		} else {
			// Only get the old goto url if the latest one 
			// was cleared out.  This enables the user to
			// go back (in the browser history) to the login 
			// page after logging in.
			if (redirURL.equals(""))
				redirURL = (String)session.getAttribute("old_goto_url");
			else
				session.putValue("old_goto_url", redirURL);

			// Clear out the goto_url
			session.putValue("goto_url", "");
		}
*/

	}	


	/**
	 * Loads the given login template.  If no template is specified
	 * the default login template is displayed.  On login failure, the
	 * failure template associated with the given (or default) login
	 * template is displayed.
	 */
	private String getLoginTemplate(String loginTemplateName) {

		String page = "";

		if (loginTemplateName == null) {
			loginTemplateName = CS_DEF_LOGIN_TEMPL_NAME;
			_templateLoader.useJar(true);
			page = _templateLoader.loadTemplate(loginTemplateName);
		} else {
			// go to /opt/aspenos/login to get the template
			String path = ServerInit.getAspenHomeDir() + "login" +
				File.separator + loginTemplateName;
			_templateLoader.useJar(false);
			page = _templateLoader.loadTemplate(path, false);
		}

		return page;
	}


	/**
	 * Gets the "invalid." version of the specified or default
	 * login template name.  Use an HTTP param called login_tname
	 * to specify a login template name.
	 */
	private String getInvalidLoginTemplate(String loginTemplateName) {
		
		String iLoginTName = "";
		String page = "";

		if (loginTemplateName == null) {
			iLoginTName = "invalid." + CS_DEF_LOGIN_TEMPL_NAME;
			_templateLoader.useJar(true);
			page = _templateLoader.loadTemplate(iLoginTName);

		} else {
			// go to /opt/aspenos/login to get the template
			iLoginTName = "invalid." + loginTemplateName;
			String path = ServerInit.getAspenHomeDir() + "login" +
				File.separator + iLoginTName;
			_templateLoader.useJar(false);
			page = _templateLoader.loadTemplate(path, false);
		}


		return page;
	}


	/**
	 * Tries the HttpSession for login_tname, then the 'goto' param.
	 * If login_tname is found, sets it in HttpSession.
	 */
	private String getLoginTemplateName(HttpSession session, Map params) {

		_lw.logDebugMsg("getLoginTemplateName: params: " + params.toString());

		// check for login_tname in session
		String rhs = (String)session.getAttribute("login_tname");
		if (rhs != null && !rhs.equals("")) {
			_lw.logDebugMsg("found login_tname in session: " + rhs);
			return rhs;
		}

		// check the goto param for login_tname
		String gotoParam = (String)params.get(CS_GOTO);
		if (gotoParam == null) {
			session.setAttribute("login_tname", null);
			_lw.logDebugMsg("login_tname not found in 'goto' param");
			return null;
		}


		// take params after ?
		int pos = gotoParam.indexOf("?");
		if (pos != -1 && pos < gotoParam.length()-1) {
			rhs = gotoParam.substring(pos+1);
			StringTokenizer st = new StringTokenizer(rhs, "=&");
			String param, value;
			while (st.hasMoreTokens()) {
				param = st.nextToken();
				if (!st.hasMoreTokens()) 
					break;
				value = st.nextToken();
				if (param.equals("login_tname")) {
					_lw.logDebugMsg("found login_tname in params: " + value);
					session.setAttribute("login_tname", value);
					return value;
				}
			}
		}

		session.setAttribute("login_tname", null);
		_lw.logDebugMsg("login_tname not found: using default");
		return null; 
	}

	/**
	 *
	 */
	private String getServletErrorTemplate(String msg) {

		_lw.logDebugMsg("Returning ERROR template:\n\t" + msg);
		HashMap tags = new HashMap();
		tags.put("error", msg);

		String errPage = 
			_templateLoader.loadTemplate(CS_DEF_SERVLET_ERROR_TEMPL_NAME);

		if (_fieldExchanger == null) {
			_lw.logDebugMsg("_fieldExchanger is null");
			return "error!";
		} else {
			return _fieldExchanger.doExchange(errPage, tags);
		}
	}


	/**
	 *
	 **/
	private void reportError(String msg, HttpServletRequest request) {
		if (request != null) {
			HashMap params = (HashMap)ServletTool.hashParams(request);
			msg += "\n\nPARAMS:\n" + params.toString();
		}

		_lw.logErr(msg);
		sendMailMsg(ERROR_MAIL_RECIP, null, "LoginServlet error",
				"LoginServlet Error: " + msg, "error@aspenos.org");
	}


	public void sendMailMsg(String to, String bcc, 
			String subject, String body, String from) {
		try {
			Mailer m = new Mailer();
			m.sendPlain(body, subject, to, null, bcc, from);
		} catch (Exception ex) { 
			_lw.logMsg("Unable to send message to '" + to + "':\n\t" + ex + 
				"\n\tFAILED MESSAGE:\n" + body + "\n\n"); 
		}
	}

}

 
