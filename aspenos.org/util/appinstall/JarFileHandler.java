package org.aspenos.util.appinstall;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.exception.*;

/**
 * Prepares an AppJarDef for installation. 
 * These directories get copied to the app dir:
 *
 * /bin
 * /config
 * /data
 * /doc
 * /lib
 * /sql
 * /templates
 * /xml
 * *.class   get copied to Aspen class directory
 * 
 * @author P. Mark Anderson
 **/
public class JarFileHandler {

	private static final String LIB_DIR = "lib";

	private HashMap _binFiles;
	private HashMap _configFiles;
	private HashMap _dataFiles;
	private HashMap _docFiles;
	private HashMap _libFiles;
	private HashMap _sqlFiles;
	private HashMap _templateFiles;
	private HashMap _xmlFiles;
	private HashMap _classFiles;

	private AppJarDef _appJar;
	private Properties _props;
	private LoggerWrapper _lw;
	private String _jarPath;
	private String _aspenClassDir;
	private String _appInstallDir;
	private String _appLibDir;



	/**
	 * 
	 */
	public JarFileHandler(Properties props, LoggerWrapper lw) 
			throws Exception  {

		_lw = lw;
		_props = props;
		_appJar = new AppJarDef();

		_binFiles = new HashMap();
		_configFiles = new HashMap();
		_dataFiles = new HashMap();
		_docFiles = new HashMap();
		_libFiles = new HashMap();
		_sqlFiles = new HashMap();
		_templateFiles = new HashMap();
		_xmlFiles = new HashMap();
		_classFiles = new HashMap();

		_jarPath = (String)props.getProperty("app.jar_path");
		_appInstallDir = (String)props.getProperty("app.install_dir");
		_aspenClassDir = (String)props.getProperty("aspen.class_dir");

		if (!_appInstallDir.endsWith(File.separator))
			_appInstallDir += File.separator;
		_appLibDir = _appInstallDir + LIB_DIR;

		createDirs(_appLibDir);
		loadFiles();
	}


	/**
	 *
	 */
	public void handleFiles() {

		DirectoryHandler bfh = new DirectoryHandler(_binFiles,
				_appInstallDir, "bin", _lw);
		DirectoryHandler conffh = new DirectoryHandler(_configFiles,
				_appInstallDir, "config", _lw);
		DirectoryHandler dfh = new DirectoryHandler(_dataFiles,
				_appInstallDir, "data", _lw);
		DirectoryHandler docfh = new DirectoryHandler(_docFiles,
				_appInstallDir, "doc", _lw);
		DirectoryHandler lfh = new DirectoryHandler(_libFiles,
				_appInstallDir, "lib", _lw);
		DirectoryHandler sfh = new DirectoryHandler(_sqlFiles,
				_appInstallDir, "sql", _lw);
		DirectoryHandler tfh = new DirectoryHandler(_templateFiles,
				_appInstallDir, "templates", _lw);
		XMLFileHandler xfh = new XMLFileHandler(_xmlFiles,
				_props, _lw);
		ClassFileHandler cfh = new ClassFileHandler(_classFiles,
				_aspenClassDir, _lw);

		_appJar.setProperty(AppInstaller.BIN_HANDLER, bfh);
		_appJar.setProperty(AppInstaller.CONFIG_HANDLER, conffh);
		_appJar.setProperty(AppInstaller.DATA_HANDLER, dfh);
		_appJar.setProperty(AppInstaller.DOC_HANDLER, docfh);
		_appJar.setProperty(AppInstaller.LIB_HANDLER, lfh);
		_appJar.setProperty(AppInstaller.SQL_HANDLER, sfh);
		_appJar.setProperty(AppInstaller.TEMPLATE_HANDLER, tfh);
		_appJar.setProperty(AppInstaller.XML_HANDLER, xfh);
		_appJar.setProperty(AppInstaller.CLASS_HANDLER, cfh);

	}


	public AppJarDef getAppJarDef() {
		return _appJar;
	}



	/**
	 * Create directories if they are not already there.
	 */
	private void createDirs(String dir) {
		File f = new File(dir);
		if (!f.exists())
			f.mkdirs();
	}


	/**
	 * Loads app files into separate Maps 
	 * for later processing.
	 */
	private void loadFiles() throws Exception {

		if (_jarPath == null || _jarPath.equals("")) {
			_lw.logMsg("JFH", "Jar path is null!");
			_lw.logErr("JFH", "Jar path is null!");
			throw new Exception("App jar path must not be null");
			//return;
		} else {
			_jarPath = new File(_jarPath).getAbsolutePath();
			//_lw.logMsg("JFH", "Absolute jar path: " + _jarPath);
		}

		JarLoader jarLoader = new JarLoader(_lw, _jarPath);
		jarLoader.cacheByDirectory();

		jarLoader.setCacheDirectory("bin/");
		jarLoader.cacheFiles();
		_binFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("config/");
		jarLoader.cacheFiles();
		_configFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("data/");
		jarLoader.cacheFiles();
		_dataFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("doc/");
		jarLoader.cacheFiles();
		_docFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("lib/");
		jarLoader.cacheFiles();
		_libFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("sql/");
		jarLoader.cacheFiles();
		_sqlFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("templates/");
		jarLoader.cacheFiles();
		_templateFiles = jarLoader.getCache();

		jarLoader.setCacheDirectory("xml/");
		jarLoader.cacheFiles();
		_xmlFiles = jarLoader.getCache();

		jarLoader.cacheByExtension();
		jarLoader.setCacheExtension(".class");
		jarLoader.cacheFiles();
		_classFiles = jarLoader.getCache();

	}


}
