package org.aspenos.util.appinstall;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.exception.*;

/**
 * 
 *
 * 
 * @author P. Mark Anderson
 */
public class DirectoryHandler {

	private HashMap _files;
	private LoggerWrapper _lw;
	private String _appSectionDir;
	private String _destDir;

	/**
	 * 
	 */
	public DirectoryHandler(HashMap files, String appInstallDir, 
			String destDir, LoggerWrapper lw) {

		if (!destDir.endsWith(File.separator))
			destDir += File.separator;
		_destDir = destDir;

		if (!appInstallDir.endsWith(File.separator))
			appInstallDir += File.separator;
		_appSectionDir = appInstallDir + _destDir;

		_lw = lw;
		_files = files;

		AspenUtils.makeDirDirs(_appSectionDir);
	}


	/**
	 * Simply copy each file in _files
	 * the _appSectionDir.
	 */
	public void install() throws IOException {

		Iterator keys = _files.keySet().iterator();
		while (keys.hasNext()) {
			String fileName = (String)keys.next();
			String contents = (String)_files.get(fileName);

			// Remove the directory prefix
			if (fileName.startsWith(_destDir)) {
				fileName = fileName.substring(_destDir.length());
			}

			_lw.logMsg("DH", "- " + fileName);

			// Write the file out
			String fullFilePath = _appSectionDir + fileName;
			AspenUtils.makeFileDirs(fullFilePath);

			FileWriter writer = new FileWriter(fullFilePath);
			writer.write(contents);
			writer.flush();
			writer.close();
		}
	}


}
