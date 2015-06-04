package org.aspenos.app.aossystemconsole.eh;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;


/**
 * Prepares a jar for installation.
 *
 * 1. get the uploaded jar path
 * 2. load the app's install.properties
 * 3. get 5 fields
 * 4. ask user to confirm installation
 */
public class Install2 extends SysConEHParent {

	public static final String PROPS_FILE = "config/install.properties";
	private WebFileFetch _fetcher = null;

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();

		StringBuffer setupInfo = new StringBuffer();
		StringBuffer errMsg = new StringBuffer();
		StringBuffer uploadResults = 
			new StringBuffer("<b>UPLOAD:</b> ");

		String fileParam = "";
		String fileName = "";
		String filePath = "";
		String type = "";

		HashMap params = (HashMap)
			_wer.getProperty("req_params");
		
		boolean isJar = false;
		boolean success = true;

		if (params.get("btn_fetch") != null) {

			_lw.logDebugMsg("Fetching jar...");

			_fetcher = new WebFileFetch();
			String saveDir = (String)_wer.getProperty("upload_dir");
			_fetcher.setSaveDir(saveDir);

			_lw.logDebugMsg("Save dir: " + saveDir);

			String url = (String)
				params.get("jar_url");
			String webUsername = (String)
				params.get("web_username");
			String webPassword = (String)
				params.get("web_password");

			if (webUsername != null && webPassword != null)
				_fetcher.setDoAuthentication(true);

			_fetcher.setAuthInfo(webUsername, webPassword);
			try {

				_lw.logDebugMsg("URL: " + url);
				File theJar = _fetcher.saveRemoteFile(url, null, "get");

				filePath = theJar.getAbsolutePath();
				
				_lw.logDebugMsg("jar path: " + filePath);

				int pos = filePath.lastIndexOf("/");
				if (pos == -1)
					pos = 0;
				else
					pos++;

				fileName = filePath.substring(pos);
				_lw.logDebugMsg("file name: " + fileName);

				if (fileName.toLowerCase().endsWith("jar"))
					isJar = true;

			} catch (Exception ex) {
				uploadResults.append("Problem while fetching jar.");
				errMsg.append("<li><b><font color=\"#FF0000\">")
					.append(ex.toString()).append("</font></b></li>");
				success = false;
			}

		} else {

			MultipartRequest mpr = (MultipartRequest)
				_wer.getProperty("mpr");

			if (mpr == null) {
				uploadResults.append("Internal server error!<br>");
				errMsg.append("Sorry, this one is the server's fault.  ")
						.append("Please notify the admin.");
				success = false;
			} else {
				Enumeration e = mpr.getFileNames();
				if (e.hasMoreElements()) {
					fileParam = (String)e.nextElement();	
					fileName = mpr.getFilesystemName(fileParam);
					type = mpr.getContentType(fileParam);
					if (type.equals("application/x-zip-compressed"))
						isJar = true;

					File f = mpr.getFile(fileParam);
					filePath = f.getAbsolutePath();
				}
			}
		}

		if (success) {
			if (!isJar) {
				uploadResults.append("The uploaded file is not a JAR");
				errMsg.append("<li><b><font color=\"#FF0000\">")
					.append("Please upload a JAR file</font></b></li>");
				success = false;
			} else if (fileName != null) {
				uploadResults.append("Received <font color=\"#0000FF\">")
					.append(fileName).append("</font>."); 
				success = true;
			}
		}

		// Now read the config file from the jar
		PropLoader propLoader = new PropLoader();
		if (success) {

			// Display the app's install.properties file
			try {
				uploadResults.append("<br><br><b>PROPERTIES:</b>\n<blockquote>");
				Properties appProps = propLoader.load(PROPS_FILE, filePath);
				String strProps = appProps.toString();

				strProps = strProps.substring(1,strProps.length()-1);
				StringTokenizer st = new StringTokenizer(strProps, ",=");
				while (st.hasMoreTokens()) {
					uploadResults.append("\n")
						.append(st.nextToken())
						.append(" = <font color=\"#0000FF\">")
						.append(st.nextToken())
						.append("</font><br>");
				}
				uploadResults.append("</blockquote>");
			} catch (Exception ex) {
				success = false;
				errMsg.append("Unable to get properties: " + ex);
			}
		}


		// setup info
		if (success) {

			// Check for required fields
			if (propLoader.getString("app.display_name") == null) {
				errMsg.append("<li><b><font color=\"#FF0000\">")
					.append("The property <font color=\"000000\">")
					.append("app.display_name</font> must be set")
					.append("</font></b></li>");
				success = false;
			}
			if (propLoader.getString("vendor.system_name") == null) {
				errMsg.append("<li><b><font color=\"#FF0000\">")
					.append("The property <font color=\"000000\">")
					.append("vendor.system_name</font> must be set")
					.append("</font></b></li>");
				success = false;
			}
		}

		setupInfo = new StringBuffer("<b>SETUP: </b>");

		if (success) {
			setupInfo.append("Configuration is valid.<br><br>");
			setupInfo.append("<a href=\"?webevent_name=install3&jar=")
				.append(filePath)
				.append("\">Run the installer</a>");
		} else {
			setupInfo
				.append("<font color=\"#FF0000\">")
				.append("Please remedy the following errors.</font>")
				.append("<ul>")
				.append(errMsg.toString())
				.append("</ul><br>")
				.append("<b>Please refer to the <a href=")
				.append("\"/aspenos/install_help.html\">")
				.append("installation help</a> for more info.</b>");
		}

		tagTable.put("upload_results", uploadResults.toString());
		tagTable.put("setup_info", setupInfo.toString());
		tagTable.put("sub_titles", 
				"INSTALLING A NEW APPLICATION (step 2 of 3)");

		return tagTable; 
	}
}

