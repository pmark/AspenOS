package org.aspenos.util.appinstall;

import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.xml.sax.*; // just for the exceptions

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.app.aoscontentserver.xml.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class XMLFileHandler {

	private static final String XML_DIR = "xml" + File.separator;

	private HashMap _files;
	private LoggerWrapper _lw;
	private Properties _props;
	private String _appXmlDir;
	private String _appName;

	private String _xmlErts;
	private String _xmlWebEvents;
	private String _xmlResources;
	private String _xmlTemplates;
	private String _xmlMenus;
	private String _xmlMenuBtns;
	private String _xmlIcons;
	private String _xmlRoles;
	private String _xmlPrincipals;


	/**
	 * 
	 */
	public XMLFileHandler(HashMap files, 
			Properties props, LoggerWrapper lw) {

		_props = props;
		_files = files;
		_lw = lw;

		_appName = (String)_props.getProperty("app.name");

		String appInstallDir = (String)_props.getProperty("app.install_dir");
		if (!appInstallDir.endsWith(File.separator))
			appInstallDir += File.separator;
		_appXmlDir = appInstallDir + XML_DIR;

		AspenUtils.makeDirDirs(_appXmlDir);
		loadFiles();
	}


	/**
	 * Uses parsers to install registry stuff, then just
	 * copies the XML files to the app's /xml directory.
	 */
	public void install(RegistryBundle regBundle) 
			throws SAXException, RegistryException, IOException, SQLException {

		// parse the standard XML files for this app
		parseStandardXmlFiles(regBundle);

		// copy additional xml files to app xml dir
		copyXMLDir();
	}


	/**
	 *
	 */
	private void parseStandardXmlFiles(RegistryBundle regBundle) 
			throws SAXException, IOException, RegistryException, SQLException {
		

		SaxWebEventParser parWebEvent 	= null;
		SaxResourceParser parResource 	= null;
		SaxTemplateParser parTemplate 	= null;
		SaxERTParser parErt 			= null;
		SaxMenuParser parMenu 			= null;
		SaxMenuBtnParser parMenuBtn 	= null;
		SaxIconParser parIcon 			= null;
		SaxRoleParser parRole 			= null;
		SaxPrincipalParser parPrincipal = null;

		_lw.logMsg("XFH:","Parsing standard XML files");

		// parse webevents.xml
		if (_xmlWebEvents != null) {
			_lw.logMsg("XFH:","webevents");
			parWebEvent = new SaxWebEventParser(_xmlWebEvents);
		}

		// parse resources.xml
		if (_xmlResources != null) {
			_lw.logMsg("XFH:","resources");
			parResource = new SaxResourceParser(_xmlResources);
		}

		// parse templates.xml
		if (_xmlTemplates != null) {
			_lw.logMsg("XFH:","templates");
			parTemplate = new SaxTemplateParser(_xmlTemplates);
		}

		// parse erts.xml
		if (_xmlErts != null) {
			_lw.logMsg("XFH:","erts");
			parErt = new SaxERTParser(_xmlErts);
		}

		// parse menus.xml
		if (_xmlMenus != null) {
			_lw.logMsg("XFH:","menus");
			parMenu = new SaxMenuParser(_xmlMenus);
		}

		// parse menubuttons.xml
		if (_xmlMenuBtns != null) {
			_lw.logMsg("XFH:","menubuttons");
			parMenuBtn = new SaxMenuBtnParser(_xmlMenuBtns);
		}

		// parse icons.xml
		if (_xmlIcons != null) {
			_lw.logMsg("XFH:","icons");
			parIcon = new SaxIconParser(_xmlIcons);
		}


		// parse roles.xml
		if (_xmlRoles != null) {
			_lw.logMsg("XFH:","roles");
			parRole	= new SaxRoleParser(_xmlRoles);
		}

		// parse principals.xml
		if (_xmlPrincipals != null) {
			_lw.logMsg("XFH:","principals");
			parPrincipal = new SaxPrincipalParser(_xmlPrincipals);
		}

		_lw.logMsg("XFH:","Done parsing XML");

		if (regBundle == null)
			_lw.logMsg("XFH:","Registry bundle is null!!");

		String regGroup = (String)_props.getProperty("app.registry_group");
		_lw.logMsg("XFH:","app.registry_group is: " + regGroup);

		// get registry handles
		WebEventRegistry weReg = (WebEventRegistry)
			regBundle.getRegistry("webevent");
		weReg.setRegistryGroupName(regGroup);
		
		ResourceRegistry resReg = (ResourceRegistry)
			regBundle.getRegistry("resource");
		resReg.setRegistryGroupName(regGroup);
		
		TemplateRegistry tmpReg = (TemplateRegistry)
			regBundle.getRegistry("template");
		tmpReg.setRegistryGroupName(regGroup);
		
		MenuRegistry menuReg = (MenuRegistry)
			regBundle.getRegistry("menu");
		menuReg.setRegistryGroupName(regGroup);

		PrincipalRegistry prinReg = (PrincipalRegistry)
			regBundle.getRegistry("principal");
		
		RoleRegistry roleReg = (RoleRegistry)
			regBundle.getRegistry("role");
		

		_lw.logMsg("XFH:","Retrieved registries, storing data...");

		// store defs from XML in registry 
		if (parRole != null) {
			RoleDefs roledefs = parRole.getRoleDefs();
			if (roledefs != null)
				roleReg.storeRoleDefs(roledefs);
		}

		if (parPrincipal != null) {
			PrincipalDefs prindefs = parPrincipal.getPrincipalDefs();
			if (prindefs != null)
				prinReg.storePrincipalDefs(prindefs);

			PrinRoleDefs prinroledefs = parPrincipal.getPrinRoleDefs();
			if (prinroledefs != null)
				prinReg.storePrinRoleDefs(prinroledefs);
		}

		
		if (parWebEvent != null) {
			WebEventDefs weds = parWebEvent.getWebEventDefs();
			if (weds != null)
				weReg.storeWebEventDefs(weds);
		}

		if (parResource != null) {
			ResourceDefs resdefs = parResource.getResourceDefs();
			if (resdefs != null)
				resReg.storeResourceDefs(resdefs);
		}

		if (parTemplate != null) {
			TemplateDefs tdefs = parTemplate.getTemplateDefs();
			if (tdefs != null)
				tmpReg.storeTemplateDefs(tdefs);
		}

		if (parErt != null) {
			ERTDefs erts = parErt.getERTDefs();
			_lw.logMsg("XFH:","erts: " + erts.toXML());
			if (erts != null)
				weReg.storeERTDefs(erts);
		}

		if (parMenu != null) {
			MenuDefs menus = parMenu.getMenuDefs();
			if (menus != null)
				menuReg.storeMenuDefs(menus);
		}

		if (parMenuBtn != null) {
			MenuButtonDefs buttons = parMenuBtn.getMenuButtonDefs();
			if (buttons != null)
				menuReg.storeMenuButtonDefs(buttons);
		}

		if (parIcon != null) {
			IconDefs icons = parIcon.getIconDefs();
			if (icons != null)
				menuReg.storeIconDefs(icons);
		}

		_lw.logMsg("XFH:","Done storing XML data");
	}


	/**
	 *
	 */
	private void copyXMLDir() throws IOException {

		_lw.logMsg("XFH:", "Copying the XML directory to " +
				_appXmlDir);

		Iterator keys = _files.keySet().iterator();
		while (keys.hasNext()) {
			String fileName = (String)keys.next();
			String contents = (String)_files.get(fileName);

			_lw.logMsg("XFH", "- " + fileName);

			// Write the file out
			String fullFilePath = _appXmlDir + fileName;

			AspenUtils.makeFileDirs(fullFilePath);

			FileWriter writer = new FileWriter(fullFilePath);
			writer.write(contents);
			writer.flush();
			writer.close();
		}
	}


	/**
	 * All XML files for installation will be keyed by
	 * file path (could be just file name) 
	 * in the _files hash.
	 */
	private void loadFiles() {

		_xmlErts 		= (String)_files.get("erts.xml"); 
		_xmlWebEvents 	= (String)_files.get("webevents.xml"); 
		_xmlTemplates 	= (String)_files.get("templates.xml"); 
		_xmlResources 	= (String)_files.get("resources.xml"); 
		_xmlMenus 		= (String)_files.get("menus.xml"); 
		_xmlMenuBtns 	= (String)_files.get("menubuttons.xml"); 
		_xmlIcons 		= (String)_files.get("icons.xml"); 
		_xmlRoles 		= (String)_files.get("roles.xml"); 
		_xmlPrincipals 	= (String)_files.get("principals.xml"); 

		// Now remove those standard XML files
		/*
		_files.remove("erts.xml"); 
		_files.remove("webevents.xml"); 
		_files.remove("templates.xml"); 
		_files.remove("resources.xml"); 
		_files.remove("principals.xml"); 
		_files.remove("roles.xml"); 
		*/
	}

}

