package org.aspenos.app.aossystemconsole.eh;

import java.util.*;
import java.sql.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.util.appinstall.*;
import org.aspenos.db.*;


/**
 * Installs an application.
 */
public class Install3 extends SysConEHParent {


	public HashMap handleEvent() {

		boolean success = true;
		File tmpLogFile = null;
		HashMap tagTable = getClonedTagTable();

		StringBuffer installResults = new StringBuffer();
		StringBuffer errMsg = new StringBuffer();

		HashMap params = (HashMap)
			_wer.getProperty("req_params");
		
		String appJarPath = (String)params.get("jar");


		// Read the config file from the jar
		PropLoader propLoader = new PropLoader();
		Properties appProps;
		try {
			appProps = propLoader.load(Install2.PROPS_FILE, appJarPath);
		} catch (Exception pex) {
			_lw.logDebugMsg("Install3: unable to get app props");
			tagTable.put("error","Unable to get app props");
			return tagTable;
		}

		// Update the properties
		appProps.put("app.jar_path", appJarPath);

		// Get the app display name
		String appDisplayName = propLoader.getString("app.display_name");

		// Specify the XML parser 
		System.setProperty("sax.parser.class",
				"org.apache.xerces.parsers.SAXParser");



		// Get the cs registry bundle
		RegistryBundle csRegBundle = getCSRegistryBundle(); 

		// Get the system console registry bundle
		RegistryBundle consoleRegBundle = getAppRegistryBundle();

		if (consoleRegBundle == null || csRegBundle == null) {
			_lw.logDebugMsg("Install3: null registry bundle");
			tagTable.put("error","Registry bundle is null");
			return tagTable;
		}

		IConnectionPool csDbPool = null;
		IConnectionPool consoleDbPool = null;

		try {

			csDbPool = csRegBundle.getDbPool(ICSConstants.AOSCS_DBID);
			consoleDbPool = consoleRegBundle.getDbPool(ICSConstants.AOSCONSOLE_DBID);

			String msg;
			if (csDbPool == null) {
				msg = "ERROR: no cs DB pool for " + ICSConstants.AOSCS_DBID;
				_lw.logDebugMsg(msg);
				_lw.logErr(msg);
			}
			if (consoleDbPool == null) {
				msg = "ERROR: no console DB pool for " + ICSConstants.AOSCONSOLE_DBID;
				_lw.logDebugMsg(msg);
				_lw.logErr(msg);
			}

			// Turn off auto commit so we can rollback on error
			DbPersistence csDb = csDbPool.getConnection();
			csDb.getConnection().setAutoCommit(false);
			DbPersistence consoleDb = consoleDbPool.getConnection();
			consoleDb.getConnection().setAutoCommit(false);

			_lw.logDebugMsg("Install3:","Creating new AppInstaller for app: " + 
					appDisplayName);

			tmpLogFile = File.createTempFile("AOS","installLOG");
			String tmpLogPath = tmpLogFile.getAbsolutePath();
			Logger logger = new Logger();
			logger.setMsgLog(tmpLogPath);
			logger.setDebugLog(tmpLogPath);
			logger.setErrLog(tmpLogPath);
			LoggerWrapper installLog = new LoggerWrapper(logger);

			_lw.logDebugMsg("Temp install log is at: " + tmpLogPath);
			AppInstaller installer = 
				new AppInstaller(appProps, consoleDb, csDb, installLog);

			/*
	_lw.logDebugMsg("testing cs conn");
	_lw.logDebugMsg(csDb.selectFirstAsHash("*","role","role_id=3").toString());
	_lw.logDebugMsg("testing console conn");
	_lw.logDebugMsg(consoleDb.selectFirstAsHash("*","vendor","vendor_id=3").toString());

	_lw.logDebugMsg("Using app log for install log");
	AppInstaller installer = 
		new AppInstaller(appProps, consoleDb, csDb, _lw);
		*/

			_lw.logDebugMsg("Install3:","\n\nINSTALLING APP\n");
			installer.install();

			// Commit the changes since no exceptions were thrown
			_lw.logDebugMsg("Install3:","\n\nCOMMITTING CHANGES TO DATABASES\n");

			csDb.getConnection().commit();
			consoleDb.getConnection().commit();

			success = true;

		} catch (Exception ex) {

			String exMsg = ex.toString();
			_lw.logDebugMsg("Install3:","Problem installing app:\n", ex);
			_lw.logDebugMsg("Install3:","\n\nROLLING BACK DB TRANSACTIONS\n\n");
			try {
				csDbPool.getConnection().getConnection().rollback();
				consoleDbPool.getConnection().getConnection().rollback();
			} catch (SQLException re) {
				_lw.logDebugMsg("Install3:","Unable to rollback:", re);
				_lw.logErr("Install3:","Unable to rollback:", re);
			}

			StringBuffer dupMsg = new StringBuffer("This occurs when ")
				.append("trying to install an application that specifies ")
				.append("IDs for its events, resources, or templates " )
				.append("that are already defined in the registry group.")
				.append("<br><br>This application may already be installed.");

			errMsg.append("<font color=\"#FF0000\">");
			errMsg.append("<b>")
				.append(exMsg)
				.append("</b><br><br>");


			// Check for the duplicate unique key error
			if (exMsg.indexOf(
					"Cannot insert a duplicate key into a unique index")
					!= -1) {
				errMsg.append(dupMsg.toString());
			}

			errMsg.append("</font>");

			success = false;
		}


		if (success) {
			installResults = new StringBuffer("<b>SUCCESS:</b>");
			installResults.append(" Installation of <i>")
				.append(appDisplayName)
				.append("</i> is complete!<br><br>");
			installResults.append("<a href=\"console\">Return</a>")
				.append(" to the System Console home.");


			// move the install log to the app dir
			try {
				String appInstallDir = (String)appProps
					.getProperty("app.install_dir");
				_lw.logDebugMsg("copying install log to " + 
						appInstallDir);

				if (!appInstallDir.endsWith(File.separator))
					appInstallDir += File.separator;

				File logDir = new File(appInstallDir + "logs");
				if (!logDir.exists())
					logDir.mkdirs();

				File newLogFile = new File(appInstallDir + "logs" +
						File.separator + "install.log");

				if (!newLogFile.exists())
					newLogFile.delete();
				
				FileCopy.copy(tmpLogFile, newLogFile);

				tmpLogFile.delete();

			} catch (IOException ioe) {
				_lw.logDebugMsg("Unable to move install log: ", ioe);
			}

		} else {
			installResults = new StringBuffer();
			installResults
				.append(errMsg.toString())
				.append("<br><br><br>")
				.append("Please refer to the <a href=")
				.append("\"/aspenos/install_help.html\">")
				.append("installation help</a> for more info.<br><br>");
			installResults.append("<a href=\"console\">Return</a>")
				.append(" to the System Console home.");
		}

		tagTable.put("install_results", installResults.toString());
		tagTable.put("sub_titles", "INSTALLING A NEW APPLICATION (step 3 of 3)");

		return tagTable; 
	}
}

