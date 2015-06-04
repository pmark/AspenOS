package org.aspenos.app.aoscontentserver.util;

import java.io.File;

/**
 *
 */
public interface ICSConstants {

	// Config ------------------------------------------

	public static final String SERVER_HOST = "localhost";
	public static final String SERVER_PROTOCOL = "http";
	public static final String SERVER_PORT = "";
	public static final String CONFIG_USER = "mktg";
	public static final String CONFIG_PASSWORD = "snow99";
	public static final String AOS_CONFIG_WEB_DIR = "aos";

	public static final String DEF_JAR_MODE_HOME = 
		File.separator + "opt" + File.separator + "aspenos";
	public static final String DEF_DIR_MODE_HOME = 
		File.separator + "opt" + File.separator + "aspenos";
	public static final String AOS_PROPS_URI = "aspenos.properties";



	// General ----------------------------------------
	public static final String FSEP = File.separator;
	public static final String APP_DIR = "apps";

	public static final String CS_DEFAULT_GOTO_URL = 
		"http://"+SERVER_HOST+"/";
	public static final String CS_ERROR_MAIL_RECIP = 
		"root@ix";
	public static final String CS_SID_KEY =
		"aspen_sid";
	public static final String ANON_PRIN_ID =
		"-1";

	public static final String CONTENT_SERVER_JAR_NAME = 
		"contentserver.jar";
	public static final String SYSTEM_CONSOLE_JAR_NAME = 
		"systemconsole.jar";
	public static final String CONTENT_SERVER_JAR_DIR = 
		APP_DIR+FSEP+"AOSContentServer"+FSEP+"lib"+FSEP;
	public static final String SYSTEM_CONSOLE_JAR_DIR = 
		APP_DIR+FSEP+"AOSSystemConsole"+FSEP+"lib"+FSEP;
	public static final String CONTENT_SERVER_JAR_PATH = 
		CONTENT_SERVER_JAR_DIR + CONTENT_SERVER_JAR_NAME;
	public static final String SYSTEM_CONSOLE_JAR_PATH = 
		SYSTEM_CONSOLE_JAR_DIR + SYSTEM_CONSOLE_JAR_NAME;

	public static final String AOSCS_DBID = 
		"cserver_db";

	//// Telski.com still doesn't have the integrated
	//// aosmain DB, so it needs to use 'console_db'
	//// as its AOSCONSOLE_DBID
	public static final String AOSCONSOLE_DBID = "cserver_db";
	//public static final String AOSCONSOLE_DBID = "console_db";


	public static final String AOSCS_RG_KEY = 
		"reggrp";
	public static final String AOSCONSOLE_SYS_NAME = 
		"AOSSystemConsole";
	public static final int JAR_SERVER_TYPE = 1;
	public static final int DIR_SERVER_TYPE = 2;

	// Login Servlet ----------------------------------
	public static final String CS_GOTO =
		"goto"; // used with login servlet 


	// Event Handling ---------------------------------
	public static final String CS_WEBEVENT_NAME_KEY = 
		"webevent_name";
	public static final String CS_WEBEVENT_ID_KEY = 
		"webevent_id";


	// Logging ----------------------------------------
	public static final boolean CS_DO_INIT_LOG =
		true;
	public static final boolean CS_DO_DEBUG =
		true;

	public static final String CS_DEF_HEADER_TEMPLATE =
		"def_header.template";
	public static final String CS_DEF_FOOTER_TEMPLATE =
		"def_footer.template";



	// What2do ================================================================

	// Default template IDs
	public static final String CS_DEF_SERVLET_ERROR_TEMPL_NAME =
		"error.template";
	public static final String CS_DEF_LOGIN_TEMPL_NAME = 
		"default_login.template";


	// Stuff
	public static final String CS_SESSION_COOKIE_KEY = 
		"aspenos_sid";
	public static final String CS_LOGOUT_SID = 
		"LOGOUT";
	public static final String CS_DEF_EVENT_ID1 = 
		"0";
	public static final String CS_DEF_EVENT_ID2 = 
		"1";


	// Session status 
	public static final int CS_STATUS_NEW_LOGIN =
		1000;
	public static final int CS_STATUS_INVALID_LOGIN =
		1001;
	public static final int CS_STATUS_ACTIVE_AUTHENTICATED =
		1002;
	public static final int CS_STATUS_LOGOUT =
		-1;

}


