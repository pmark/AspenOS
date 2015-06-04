package org.aspenos.util.appinstall;

import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import org.xml.sax.*;

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.exception.*;
import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.registry.*;

/**
 * Installs an Aspen application from its JAR file.
 * <BR><BR>
 * Some properties are required and others are optional.
 * <BR><BR>
 * REQUIRED:<BR>
 * app.display_name<BR>
 * vendor.system_name<BR>
 * <BR><BR>
 * OPTIONAL:<BR>
 * aspen.install_dir (/opt/aspenos/)<BR>
 * aspen.app_dir (apps/)<BR>
 * aspen.class_dir (WEB-INF/classes/)<BR>
 * app.system_name<BR>
 * app.install_dir<BR>
 *
 *
 * @author P. Mark Anderson
 **/
public class AppInstaller {

	public static final String BIN_HANDLER = "bin_handler";
	public static final String CONFIG_HANDLER = "config_handler";
	public static final String DATA_HANDLER = "data_handler";
	public static final String DOC_HANDLER = "doc_handler";
	public static final String LIB_HANDLER = "lib_handler";
	public static final String SQL_HANDLER = "sql_handler";
	public static final String TEMPLATE_HANDLER = "template_handler";
	public static final String XML_HANDLER = "xml_handler";
	public static final String CLASS_HANDLER = "class_handler";

	public static final String CONSOLE_DBID = "system_console";
	public static final String CS_DBID = "content_server";

	public static final int RG_RADIX = 36;

	private static final String ASPEN_APP_DIR = 
		"apps" + File.separator;
	private static final String ASPEN_CLASS_DIR = 
		"classes" + File.separator;
	private static final String ASPEN_DIR = 
		File.separator + "opt" + File.separator + 
		"aspenos" + File.separator;

	private static final String CS_LIB_PATH = 
		ASPEN_APP_DIR + "AOSContentServer" +
		File.separator + "lib" + File.separator;
	private static final String CS_JAR_PATH = 
		CS_LIB_PATH + "contentserver.jar";

	private RegistryBundle _regBundle;
	//private IConnectionPool _consoleDbPool;
	//private IConnectionPool _csDbPool;
	private DbPersistence _consoleDb;
	private DbPersistence _csDb;
	private JarFileHandler _jfh;
	private LoggerWrapper _lw;
	private Properties _props;
	private TemplateLoader _csTemplateLoader;
	private String _regGroup;
	private String _rgId;



	/**
	 * 
	 */
	public AppInstaller(Properties props, 
			DbPersistence consoleDb,
			DbPersistence csDb,
			LoggerWrapper lw) throws Exception {

		_consoleDb = consoleDb;
		_csDb = csDb;

		_lw = lw;
		_props = props;


		/////// ASPEN PROPERTIES ////////////////////////////////////////
		// Get the Aspen install dir
		String aspenInstallDir = (String)
			_props.getProperty("aspen.install_dir");
		if (aspenInstallDir == null)
			aspenInstallDir = ASPEN_DIR;
		aspenInstallDir = new File(aspenInstallDir)
				.getAbsolutePath();
		if (!aspenInstallDir.endsWith(File.separator))
			aspenInstallDir += File.separator;
		_props.setProperty("aspen.install_dir", aspenInstallDir);

		_lw.logMsg("AI:","setting aspen install dir: " + aspenInstallDir);

		// Get the aspen class dir
		String aspenClassDir = (String)
			_props.getProperty("aspen.class_dir");
		if (aspenClassDir == null)
			aspenClassDir = aspenInstallDir + ASPEN_CLASS_DIR;
		if (!aspenClassDir.endsWith(File.separator))
			aspenClassDir += File.separator;
		_props.put("aspen.class_dir", aspenClassDir);

		// Get the aspen app dir
		String aspenAppDir = (String)
			_props.getProperty("aspen.app_dir");
		if (aspenAppDir == null)
			aspenAppDir = aspenInstallDir + ASPEN_APP_DIR;
		if (!aspenAppDir.endsWith(File.separator))
			aspenAppDir += File.separator;
		_props.put("aspen.app_dir", aspenAppDir);


		String csJar = aspenInstallDir + CS_JAR_PATH;
		_props.put("aspen.cs_jar_path", csJar);



		/////// APP PROPERTIES ///////////////////////////////////////////
		// Get the app display and system names
		String appDisplayName = (String)
			_props.getProperty("app.display_name");
		String appSysName = (String)
			_props.getProperty("app.system_name");

		// The app system name is a modified app display 
		// name by default
		if (appSysName == null) {
			appSysName = appDisplayName
				.replace(' ','_')
				.replace('\'','_')
				.replace(';','_')
				.replace('$','_')
				.replace('!','_')
				.replace('*','_')
				.replace(':','_');

			_props.setProperty("app.system_name", appSysName);
		}

		// Create an app install dir name if one does not exist
		String appInstallDir = (String)
			_props.getProperty("app.install_dir");

		if (appInstallDir == null)
			appInstallDir = aspenAppDir + appSysName;
		if (!appInstallDir.endsWith(File.separator))
			appInstallDir += File.separator;
		_props.setProperty("app.install_dir", appInstallDir);

		_lw.logMsg("setting app install dir: " + appInstallDir);

		// Get the registry group
		_regGroup = (String)_props.getProperty("app.registry_group");
		if (_regGroup != null) {
			_regGroup = _regGroup.toLowerCase();
			_props.setProperty("app.registry_group", _regGroup);
		}

		_lw.logMsg("app.registry_group:  " + _regGroup);


		// Get the Content Server template loader
		_csTemplateLoader = new TemplateLoader(_lw, csJar);
		_csTemplateLoader.cacheTemplates();

		setupRegistryBundle();

		_lw.logMsg("Loading the jar file handler");

		// Now handle load up the app's JAR file
		_jfh = new JarFileHandler(_props, _lw);

	}


	/**
	 *
	 */
	public void install() 
		throws SQLException, IOException, SAXException, RegistryException  {

		// First the content server database must be set up 
		_lw.logMsg("AI:","Setting up CS database");
		setupCSDatabase();

		// This is what actually loads the files from the Jar
		_jfh.handleFiles();

		// Now we're ready to install the app
		AppJarDef appJar = _jfh.getAppJarDef();

		DirectoryHandler bfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.BIN_HANDLER);

		DirectoryHandler conffh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.CONFIG_HANDLER);

		DirectoryHandler dfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.DATA_HANDLER);

		DirectoryHandler docfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.DOC_HANDLER);

		DirectoryHandler lfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.LIB_HANDLER);

		DirectoryHandler sfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.SQL_HANDLER);

		DirectoryHandler tfh = (DirectoryHandler)
			appJar.getProperty(AppInstaller.TEMPLATE_HANDLER);

		XMLFileHandler xfh = (XMLFileHandler)
			appJar.getProperty(AppInstaller.XML_HANDLER);

		ClassFileHandler cfh = (ClassFileHandler)
			appJar.getProperty(AppInstaller.CLASS_HANDLER);


		_lw.logMsg("AI:","Installing XML files...");
		xfh.install(_regBundle);
		_lw.logMsg("AI:","Installing class files...");
		cfh.install();
		_lw.logMsg("AI:","Installing bin files...");
		bfh.install();
		_lw.logMsg("AI:","Installing config files...");
		conffh.install();
		_lw.logMsg("AI:","Installing data files...");
		dfh.install();
		_lw.logMsg("AI:","Installing doc files...");
		docfh.install();
		_lw.logMsg("AI:","Installing lib files...");
		lfh.install();
		_lw.logMsg("AI:","Installing sql files...");
		sfh.install();
		_lw.logMsg("AI:","Installing template files...");
		tfh.install();

		// Add the app and vendor to their registries
		_lw.logMsg("AI:","UPDATING APP REGISTRY\n");
		updateAppRegistry();

		_lw.logMsg("AI:","Install successful!\n\n");
	}


	/**
	 *
	 */
	private void updateAppRegistry() throws SQLException {

		//AppRegistry appReg = (AppRegistry)
		//	_regBundle.getRegistry("app");

		AppDef appDef = new AppDef();

		// Get the app properties
		String appSystemName = (String)_props.getProperty(
				"app.system_name");
		String appDisplayName = (String)_props.getProperty(
				"app.display_name");
		String appJarPath = (String)_props.getProperty(
				"app.install_dir");

		// Get the jar name
		String jarName = (String)_props.getProperty(
				"app.jar_path");
		int pos = jarName.lastIndexOf(File.separator);
		if (pos != -1) {
			if ((pos+1) >= jarName.length())
				pos = jarName.length()-1;
			jarName = jarName.substring(pos+1);
		} 

		// Get the jar path
		if (!appJarPath.endsWith(File.separator))
			appJarPath += File.separator;
		appJarPath += "lib" + File.separator + jarName;

		// Get this app's vendor's properties
		String vendorSystemName = (String)_props.getProperty(
				"vendor.system_name");
		String vendorDisplayName = (String)_props.getProperty(
				"vendor.display_name");

		// Query for this vendor's system_name
		StringBuffer where = new StringBuffer("system_name='")
			.append(vendorSystemName).append("'");
		Integer vid = (Integer)_consoleDb.selectFirstAttrib(
				"vendor_id","vendor",where.toString());
		StringBuffer sqlInsert;

		// If there is no vendor by the given system name,
		// insert a new vendor record.
		if (vid == null) {
			sqlInsert = 
				new StringBuffer("INSERT INTO vendor ")
				.append("(system_name,display_name) VALUES ('")
				.append(vendorSystemName)
				.append("','")
				.append(vendorDisplayName)
				.append("')");

			_lw.logMsg("AI:","ADDING NEW VENDOR\n");
			_consoleDb.insert(sqlInsert.toString());

			// Now get the vendor id of the new record
			vid = (Integer)_consoleDb.selectFirstAttrib(
				"vendor_id","vendor",where.toString());
		}

		String appVendorId = vid.toString();

		appDef.setProperty("vendor_id", appVendorId);
		appDef.setProperty("system_name", appSystemName);
		appDef.setProperty("display_name", appDisplayName);
		appDef.setProperty("jar_path", appJarPath);
		appDef.setProperty("reggrp_id", _rgId);

		String fandv = appDef.getSqlFieldsAndValues();
		sqlInsert = new StringBuffer("INSERT INTO app ")
			.append(fandv);

		_consoleDb.insert(sqlInsert.toString());
	}


	/**
	 * Create tables necessary for an app to live.
	 * FUTURE: Use a SQLFileHandler to create app specific tables.
	 * The SQLFileHandler would not go in this method, of course.
	 */
	private void setupCSDatabase() throws SQLException {
	
		// Get the two DB connections
		//DbPersistence dbCs = _regBundle.getDbConn(CS_DBID);
		//DbPersistence dbConsole = _regBundle.getDbConn(CONSOLE_DBID);

		if (_csDb == null) {
			_lw.logErr("AI: null CS connection!");
			_lw.logDebugMsg("AI: null CS connection!");
		}

		// Get the list of all tables in the content server DB
		String[] types = new String[1];
		types[0] = "TABLE";

		Connection conn = _csDb.getConnection();
		DatabaseMetaData md = conn.getMetaData();
		ResultSet rsTableNames = md.getTables(null, null, "%", types);
		ArrayList alreadyThere = new ArrayList();


		// Get the RegGroupDef for this registry group
		RegGroupDef rg = getRegGroupDef();
		//_lw.logMsg("AI:","Got registry group definition");

		Object o = rg.getProperty("reggrp_id");
		//_lw.logMsg("AI:","reggrp_id is a " + o.getClass());

		if (o != null) {
			if (o.getClass().toString().endsWith("Integer"))
				_rgId = ((Integer)o).toString();
			else
				_rgId = (String)o;
		}

		String rgKey = (String)rg.getProperty("reggrp_key");
		_lw.logMsg("AI:","Setting app.registry_group to: " + rgKey);
		_props.setProperty("app.registry_group",rgKey);

		String tableExt = "_" + rgKey;
		String table;
		Iterator it;

		_lw.logMsg("AI.setupDB:","using table extension: " + tableExt);

		// Store all of the table names in a List
		while (rsTableNames.next()) {
			table = rsTableNames.getString(3);
			//_lw.logMsg("AI.setupDB:","found table:" + table);

			// Find tables that are already there
			if (table != null)  {
				if (table.endsWith(tableExt)) {
					String tmp = table.substring(
							0, table.length()-tableExt.length());

					//_lw.logMsg("AI.setupDB","Table " + tmp + " already exists");
					alreadyThere.add(tmp);
				}
			}
		}

		rsTableNames.close();


		// Create any tables that need to be there but aren't there yet.
		// FUTURE: This list should be filled up from a props file
		ArrayList newTables = new ArrayList();
		newTables.add("webevent");
		newTables.add("resource");
		newTables.add("template");
		newTables.add("resourcetemplates");
		newTables.add("webeventresources");
		newTables.add("menu");
		newTables.add("menubtn");
		newTables.add("icon");
		newTables.add("texttmpl");

		// For each table in the list, load its SQL template
		// and execute full the statement.

		for (it=newTables.iterator(); it.hasNext();) {

			table = (String)it.next();

			if (alreadyThere.contains(table)) {
				continue;
			}

			// Create the table since it ain't there yet.
			// This template loader has cached templates in
			// the Aspen jar since these are standard tables.
			String tableDef = 
				_csTemplateLoader.loadTemplate(
						"templates/sql/" + table + ".template");

			StringBuffer create = new StringBuffer(
				"CREATE TABLE ")
				.append(table)
				.append(tableExt)
				.append(" ")
				.append(tableDef);

			_lw.logMsg("AI:","Creating table '" + 
					table + tableExt + "'");
			_lw.logMsg("AI:","SQL: " + create.toString());

			// Execute the creation statement
			_csDb.execute(create.toString());

		}

		_lw.logMsg("AI:","Done setting up database");
	}



	/**
	 * Retrieves the registry group using the given registry 
	 * group name and vendor system name.  If there is no
	 * registry group defined, it creates one automatically.
	 */
	private RegGroupDef getRegGroupDef() throws SQLException {

		// Get a vendor registry
		VendorRegistry vReg = (VendorRegistry)
			_regBundle.getRegistry("vendor");

		// Get the vendor system name
		String vendorSysName = (String)_props.getProperty(
				"vendor.system_name");

		// Get the app display name
		String appDisplayName = (String)_props.getProperty(
				"app.display_name");


		RegGroupDef rg = null;

		if (_regGroup == null)
			_regGroup = appDisplayName;
		if (_regGroup == null)
			_regGroup = "default";;
		if (vendorSysName == null)
			vendorSysName = "default";

		_lw.logMsg("getRGD: Trying to getting RG by name: " + _regGroup);
		rg = vReg.getRGByName(_regGroup, vendorSysName);

		// Does the registry group exist?
		if (rg == null) {

			// Since another thread could try to create the
			// next registry group at the same time, this
			// block must be synchronized.
			synchronized (this) {
				// Create a new registry group
				rg = new RegGroupDef();

				// get the next registry group id
				long id = vReg.getNextRGId().longValue();
				_lw.logMsg("getRGD: next RG ID: " + id);

				// get the next key
				String key = Long.toString(id, RG_RADIX);
				
				// set the next registry group id
				String strId = Long.toString(id);
				rg.setId(strId);
				rg.setProperty("reggrp_id", strId);

				// set the next key
				rg.setProperty("reggrp_key", key);
				// set the registry group name
				if (_regGroup != null)
					rg.setProperty("reggrp_name", _regGroup);
				else
					rg.setProperty("reggrp_name", appDisplayName);

				// set the vendor system name
				rg.setProperty("vendor_name", vendorSysName);

				_lw.logMsg("getRGD: Creating registry group " + _regGroup);
				_lw.logMsg("INSERT INTO reggrp " + rg.getSqlFieldsAndValues());
				vReg.storeRG(rg);
			}
		} 

		_lw.logMsg("getRGD: returning RG: " + rg.toXML());

		return rg;
	}

	/**
	 * None of the schnazzy auto DB pooling stuff is
	 * being used here.
	 */
	private void setupRegistryBundle() {
		try {
			// Get the registries
			RegistryFactory regFactory = new RegistryFactory();
			CSRegistryFactory csRegFactory = new CSRegistryFactory(_regGroup);
			_regBundle = new RegistryBundle();


			//// AOS CS registries
			String roleClass =
				"org.aspenos.app.aoscontentserver.registry.RoleRegistry";
			String prinClass =
				"org.aspenos.app.aoscontentserver.registry.PrincipalRegistry";
			String resClass =
				"org.aspenos.app.aoscontentserver.registry.ResourceRegistry";
			String temClass =
				"org.aspenos.app.aoscontentserver.registry.TemplateRegistry";
			String evClass =
				"org.aspenos.app.aoscontentserver.registry.WebEventRegistry";
			String menuClass =
				"org.aspenos.app.aoscontentserver.registry.MenuRegistry";

			IRegistry roleReg		= csRegFactory.createRegistry(roleClass);
			IRegistry principalReg 	= csRegFactory.createRegistry(prinClass);
			IRegistry resourceReg 	= csRegFactory.createRegistry(resClass);
			IRegistry templateReg 	= csRegFactory.createRegistry(temClass);
			IRegistry webEventReg 	= csRegFactory.createRegistry(evClass);
			IRegistry menuReg 		= csRegFactory.createRegistry(menuClass);

			roleReg.setDbConn(_csDb);
			principalReg.setDbConn(_csDb);
			resourceReg.setDbConn(_csDb);
			templateReg.setDbConn(_csDb);
			webEventReg.setDbConn(_csDb);
			menuReg.setDbConn(_csDb);

			_regBundle.setRegistry("role", roleReg, CS_DBID);
			_regBundle.setRegistry("principal", principalReg, CS_DBID);
			_regBundle.setRegistry("resource", resourceReg, CS_DBID);
			_regBundle.setRegistry("template", templateReg, CS_DBID);
			_regBundle.setRegistry("webevent", webEventReg, CS_DBID);
			_regBundle.setRegistry("menu", menuReg, CS_DBID);


			//// AOS System Console registries
			// classes
			String appClass =
				"org.aspenos.app.aossystemconsole.registry.AppRegistry";
			String vendorClass =
				"org.aspenos.app.aossystemconsole.registry.VendorRegistry";

			IRegistry appReg = (AppRegistry)regFactory.createRegistry(appClass);
			IRegistry vendorReg = (VendorRegistry)regFactory.createRegistry(vendorClass);

			appReg.setDbConn(_consoleDb);
			vendorReg.setDbConn(_consoleDb);

			_regBundle.setRegistry("app", appReg, CONSOLE_DBID);
			_regBundle.setRegistry("vendor", vendorReg, CONSOLE_DBID);

		} catch (Exception ex) {
			_lw.logErr("AI:", "Problem while setting up registries", ex);
			_lw.logMsg("AI:", "Problem while setting up registries: " + ex.toString());
		}
	}

}







