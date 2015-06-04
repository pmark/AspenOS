package org.aspenos.app.aoscontentserver.util;

import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;



			

public class ServerInit implements ICSConstants {

	private static Logger _logger = null;
	private static Properties _aspenProps = null;
	private static String _configBaseURL = null;

	private static String _csJarPath = null;
	private static String _consoleJarPath = null;
	private static String _aspenHomeDir = null;
	private static int _serverType;
	private static ServerInit _instance = null;

	private WebFileFetch _fetcher = null;


	public static ServerInit getInstance() {
		if (_instance == null)
			_instance = new ServerInit();

		ServerInit.log("ServerInit: getting instance");

		return _instance;
	}

	private ServerInit() {
		log("ServerInit: Creating new instance");
	}


	/**
	 *
	 */
	public void loadAspenProps() throws Exception {

		log("ServerInit: LOADING PROPS");

		if (_fetcher == null)
			_fetcher = new WebFileFetch();

		StringBuffer aosPropsURL = new StringBuffer();

		// Begin building the URL to the AOS system properties
		if (SERVER_PROTOCOL != null && SERVER_PROTOCOL.length() > 0) {
			aosPropsURL.append(SERVER_PROTOCOL);
			if (!SERVER_PROTOCOL.endsWith("://"))
				aosPropsURL.append("://");
		}

		// http://
		aosPropsURL.append(SERVER_HOST);

		// http://hostname
		if (SERVER_PORT != null && SERVER_PORT.length() > 0) {
			if (!SERVER_HOST.endsWith(":"))
				aosPropsURL.append(":");
			aosPropsURL.append(SERVER_PORT);
		}

		// http://hostname:8080
		aosPropsURL.append("/");
		aosPropsURL.append(AOS_CONFIG_WEB_DIR);
		if (!AOS_CONFIG_WEB_DIR.endsWith("/"))
			aosPropsURL.append("/");

		// http://hostname:8080/aos/
		_configBaseURL = aosPropsURL.toString();

		// http://hostname:8080/
		aosPropsURL.append(AOS_PROPS_URI);

		// http://hostname:8080/aos/uri/to/aspenos.properties
		log("ServerInit: built URL: " + aosPropsURL.toString());

	/*
	log("ServerInit: Using 'standard' properties");
	_aspenProps = new Properties();
	_aspenProps.put("aspen.app_load.type", "jar");
	_aspenProps.put("aspen.cserver.uri", "AOSContentServer/lib/contentserver.jar");
	_aspenProps.put("aspen.console.uri", "AOSSystemConsole/lib/systemconsole.jar");
	*/


		log("ServerInit: Setting save dir...");

		//_fetcher.setSaveDir(DEF_JAR_MODE_HOME);
		//log("ServerInit: Save dir: " + 
		//		(new File(DEF_JAR_MODE_HOME)).getAbsolutePath());

		_fetcher.setDoAuthentication(true);
		_fetcher.setAuthInfo(CONFIG_USER, CONFIG_PASSWORD);
		log("ServerInit: Fetching props file...");
		_aspenProps = _fetcher.loadProperties(
				aosPropsURL.toString(), null, "get");


		// Get the directory where Aspen is installed
		_aspenHomeDir = (String)_aspenProps.getProperty(
					"aspen.home_dir");
		_aspenHomeDir = ServerInit.fixFilePath(_aspenHomeDir);
		if (!_aspenHomeDir.endsWith(File.separator))
			_aspenHomeDir += File.separator;
		log("ServerInit: Aspen home dir: " + 
				(new File(_aspenHomeDir)).getAbsolutePath());

		
		log("ServerInit: Done getting props: ");
	}


	/**
	 *
	 */
	public void fetchSystemJarPaths() throws Exception {

		// Return if both paths have already been retrieved
		if (_csJarPath != null && _consoleJarPath != null)
			return;

		log("ServerInit: Getting system jar paths");
		if (_aspenProps == null)
			loadAspenProps();

		String appLoadType = (String)_aspenProps.getProperty(
				"aspen.app_load.type");

		// default type is dir
		if (appLoadType == null)
			appLoadType = "dir";



		// The server config is extremely different based
		// upon either JAR or DIR app load types.
		if (appLoadType.equals("jar")) {

			//// JAR app load type
			log("ServerInit: Preparing for JAR type init");
			_serverType = JAR_SERVER_TYPE;

			String url;
			File tmpFile=null;

			String csURI = (String)_aspenProps.getProperty(
					"aspen.cserver.uri");
			String consoleURI = (String)_aspenProps.getProperty(
					"aspen.console.uri");


			// The AOS temp (save) dir is where files used
			// by Aspen are copied from a web server and
			// and saved.  The save dir is relative to the 
			// servlet engine's root directory if it does
			// not start with File.separator.
			if (_aspenHomeDir == null) {
				_aspenHomeDir = DEF_JAR_MODE_HOME;
			}
			_aspenHomeDir = ServerInit.fixFilePath(_aspenHomeDir);
			File tmpDir = null;
			tmpDir = new File(_aspenHomeDir);
			if (!tmpDir.exists())
				tmpDir.mkdirs();


			// Make sure the auth config is correct,
			// even if we just went to the web dir for stuff.
			_fetcher.setDoAuthentication(true);
			_fetcher.setAuthInfo(CONFIG_USER, CONFIG_PASSWORD);


			// Get content server jar
			url = _configBaseURL + csURI;
			String saveDir = _aspenHomeDir + CONTENT_SERVER_JAR_DIR;
			_fetcher.setSaveDir(saveDir);
			log("ServerInit: Set CONTENT SERVER save dir: " + saveDir);
			log("ServerInit: Getting CONTENT SERVER jar...");
			tmpFile = _fetcher.getExistingFile(url);
			if (tmpFile == null) {
				log("ServerInit: Downloading remote JAR:\n\t" + url);
				tmpFile = _fetcher.saveRemoteFile(url, null, "get");
			} else {
				log("ServerInit: ((( Using existing JAR )))");
			}
			_csJarPath = tmpFile.getAbsolutePath();

			 
			// Get system console jar
			url = _configBaseURL + consoleURI;
			saveDir = _aspenHomeDir + SYSTEM_CONSOLE_JAR_DIR;
			_fetcher.setSaveDir(saveDir);
			log("ServerInit: Set SYSTEM CONSOLE save dir: " + saveDir);
			log("ServerInit: Getting SYSTEM CONSOLE jar...");
			tmpFile = _fetcher.getExistingFile(url);
			if (tmpFile == null) {
				log("ServerInit: Downloading remote JAR:\n\t" + url);
				tmpFile = _fetcher.saveRemoteFile(url, null, "get");
			} else {
				log("ServerInit: ((( Using existing JAR )))");
			}
			_consoleJarPath = tmpFile.getAbsolutePath();

			 
		} else {

			//// DIRECTORY app load type
			log("ServerInit: Preparing for DIR type init");
			_serverType = DIR_SERVER_TYPE;

			 _csJarPath = _aspenHomeDir + CONTENT_SERVER_JAR_PATH;
			 _consoleJarPath = _aspenHomeDir + SYSTEM_CONSOLE_JAR_PATH;
		}

		log("ServerInit: Done retrieving system jar paths");
	}


	/**
	 *
	 */
	public String fetchJar(String jarURI) throws Exception {

		if (_aspenProps == null)
			loadAspenProps();

		String url = _configBaseURL + jarURI;
		log("ServerInit: Fetching jar: " + url);
		File theJar = _fetcher.getExistingFile(url);
		if (theJar == null)
			theJar = _fetcher.saveRemoteFile(url, null, "get");

		return theJar.getAbsolutePath();
	}


	/**
	 *
	 */
	public String getContentServerJarPath() {
		return _csJarPath;
	}


	/**
	 *
	 */
	public String getConsoleJarPath() {
		return _consoleJarPath;
	}


	/**
	 *
	 */
	public int getServerType() {
		return _serverType;
	}


	/**
	 *
	 */
	public static String getAspenHomeDir() {
		if (!_aspenHomeDir.endsWith(File.separator))
			_aspenHomeDir += File.separator;

		return _aspenHomeDir;
	}


	/**
	 *
	 */
	public void setSaveDir(String sd) {
		_fetcher.setSaveDir(sd);
	}


	/**
	 *
	 */
	public String getSaveDir() {
		return _fetcher.getSaveDir();
	}


	/**
	 *
	 */
	public static void log(String msg) {
		if (!CS_DO_INIT_LOG)
			return;

		if (_logger == null)
			_logger = new Logger();
		_logger.logMsg(msg);
	}



	/**
	 * Changes all forward or backslash characters into
	 * File.separator characters.
	 */
	public static String fixFilePath(String origPath) {
		String fixedPath;

		// bs to fs
		fixedPath = origPath.replace('\\', '/');

		// fs to File.separator
		char sepChar;
		if (File.separator.startsWith("/"))
			sepChar = '/';
		else
			sepChar = '\\';

		fixedPath = fixedPath.replace('/', sepChar);

		return fixedPath;
	}


	/**
	 * Changes all backslash characters into
	 * forward slashes for use as a URL.
	 */
	public static String fixURI(String origURI) {
		return origURI.replace('\\', '/');
	}
}

