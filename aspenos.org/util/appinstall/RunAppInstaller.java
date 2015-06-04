package org.aspenos.util.appinstall;

import java.util.*;
import java.sql.*;
import java.util.zip.*;
import java.io.*;

import org.xml.sax.*;

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aoscontentserver.util.*;

/**
 * Installs an Aspen application from its JAR file.
 * For use on the command line only.
 *
 * @author P. Mark Anderson
 **/
public class RunAppInstaller {

	public static void main(String[] args) {

		if(args.length != 1) {
			System.err.println("\n\nUsage:  RunAppInstaller <properties file>");
			System.err.println("\nExample properties file:\n" +
				"aspen.install_dir=/opt/aspenos\n" + 
				"app.jar_path=lib/myapp.jar\n" + 
				"app.display_name=My App That Rocks\n" + 
				"app.system_name=MyApp\n" + 
				"app.registry_group=mygroup\n\n" + 
				"vendor.system_name=my_vendor\n" + 
				"vendor.display_name=My Vendor\n\n" + 
				"aspen.db.protocol=jdbc:postgresql\n" + 
				"aspen.db.host=ix\n" + 
				"aspen.db.cs_dsn=aos_contentserver\n" + 
				"aspen.db.console_dsn=aos_systemconsole\n" + 
				"aspen.db.driver=postgresql.Driver\n");

			System.exit(1);
		}


		final String CS_JAR_NAME = "contentserver.jar";
		final String CS_LIB_PATH = "apps" + File.separator + 
			"AOSContentServer" + File.separator + "lib" +
			File.separator;
		final String ASPEN_CLASS_DIR = "classes";

		LoggerWrapper lw = new LoggerWrapper();
		boolean success = false;

		// Set the logging so that it goes to the current directory
		lw.setDefaultLogs();

		PropLoader pl = new PropLoader();
		Properties props = null;
		try {
			props = pl.load(args[0]);
		} catch (Exception pex) {
			pex.printStackTrace();
			System.out.println("Unable to load props: " +
					pex.getMessage());
		}

		// Get the command line params
		String aspenInstallDir = pl.getString("aspen.install_dir");

		aspenInstallDir = new File(aspenInstallDir)
				.getAbsolutePath();

		// add a slash, if necessary
		if (!aspenInstallDir.endsWith(File.separator))
			aspenInstallDir += File.separator;

		// specify the XML parser 
		System.setProperty("sax.parser.class",
				"org.apache.xerces.parsers.SAXParser");

		// Load properties that are used and/or transformed here
		String appSysName = pl.getString("app.system_name");
		String appInstallDir = aspenInstallDir + "apps" + 
			File.separator + appSysName;
		//String regGroup = pl.getString("app.registry_group").toLowerCase();
		String appDisplayName = pl.getString("app.display_name");
		String appJarPath = new File(pl.getString("app.jar_path"))
				.getAbsolutePath();

		String csJar = aspenInstallDir + CS_LIB_PATH + CS_JAR_NAME;
		String aspenClassDir = aspenInstallDir + ASPEN_CLASS_DIR;

		// Update the properties
		props.put("app.jar_path", appJarPath);
		props.put("app.install_dir", appInstallDir);
		//props.put("app.registry_group", regGroup);

		props.put("aspen.class_dir", aspenClassDir);
		props.put("aspen.install_dir", aspenInstallDir);
		props.put("aspen.cs_jar_path", csJar);


		lw.logMsg("=== START: " + appDisplayName + " ========================");

		lw.logMsg("Run:","\n\nLOADING PROPERTIES\n");
		lw.logMsg("Run:","App info: " + props.toString());


		// Create a registry bundle
		RegistryBundle rb = new RegistryBundle();
		DbConnectionPool csDbPool = null;
		DbConnectionPool consoleDbPool = null;
		DbPersistence csDb = null;
		DbPersistence consoleDb = null;

		String driver = props.getProperty("aspen.db.driver");
		try { 
			if (driver != "") {
				System.out.println("Registering driver: " + driver);
				Class.forName (driver); 
				Properties sysProps = System.getProperties();
				sysProps.put("jdbc.drivers", driver);
			}

			String protocol = props.getProperty("aspen.db.protocol");
			String host = props.getProperty("aspen.db.host");
			String user = props.getProperty("aspen.db.username");
			String pwd = props.getProperty("aspen.db.password");

			StringBuffer dbURL = new StringBuffer();
			dbURL.append(protocol);
			if (!dbURL.toString().endsWith(":"))
				dbURL.append(":");
			dbURL.append("//")
				.append(host)
				.append("/");

			// Make the database connections
			String consoleDsn = props.getProperty("aspen.db.console_dsn");
			String consoleURL = dbURL.toString() + consoleDsn;
			consoleDbPool = new DbConnectionPool(lw, consoleURL, user, pwd,
					driver, 1, 1);

			String csDsn = props.getProperty("aspen.db.cs_dsn");
			String csURL = dbURL.toString() + csDsn;
			csDbPool = new DbConnectionPool(lw, csURL, user, pwd, driver, 1, 1);

			consoleDb = consoleDbPool.getConnection();
			csDb = csDbPool.getConnection();

			// Turn off auto-commit so we can roll back if necessary
			consoleDb.getConnection().setAutoCommit(false);
			csDb.getConnection().setAutoCommit(false);

			success = true;

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error connecting to databases: " +
					e.getMessage());
			lw.logMsg("Run","Error connecting to databases: " + 
					e.getMessage());
			success = false;
		}


		
		if (success) {
			try {
				lw.logMsg("Run:","\n\nLOADING APP JAR\n");

				lw.logMsg("Run:","Creating new AppInstaller for app: " + 
						appDisplayName);
				AppInstaller ai = new AppInstaller(props, consoleDb, csDb, lw);

				lw.logMsg("Run:","\n\nINSTALLING APP\n");
				ai.install();

				lw.logMsg("Run:","\n\nCOMMITTING CHANGES TO DATABASES\n");
				csDb.getConnection().commit();
				consoleDb.getConnection().commit();

				csDbPool.returnConnection(csDb);
				consoleDbPool.returnConnection(consoleDb);

			} catch (Exception ex) {

				lw.logErr("Run:","Problem installing app:\n", ex);
				lw.logMsg("Run:","\n\nROLLING BACK DB TRANSACTIONS\n\n");
				try {
					csDb.getConnection().rollback();
					consoleDb.getConnection().rollback();
				} catch (SQLException re) {
					lw.logErr("Run:","Unable to rollback:", re);
				}
			}

		} else {
			System.out.println("Problem while installing the application");
			lw.logMsg("Run:","Problem while installing the application");
		}

		lw.logMsg(
				"", "=== DONE: " + appDisplayName + " ========================\n\n");
	}

}
