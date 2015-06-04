package org.aspenos.app.aossystemconsole.eh.appconfig;

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
import org.aspenos.app.aossystemconsole.eh.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.app.aossystemconsole.defs.*;



/**
 */
public class DownloadAppJar extends SysConEHParent {

	public static String DEST_DIR = "doc/aos/temp/";
	public static String LINK_DIR = "/aos/temp/";

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		HashMap params = (HashMap)_wer.getProperty("req_params");

		tagTable.put("sub_titles", 
				"DOWNLOAD APP JAR");

		AppRegistry appreg = (AppRegistry)getAppRegistry("app");

		String appIdStr = (String)params.get("app_id");

		String jarPath = "";
		String fileName = "";
		try {
			// get the app's jar path
			AppDef app = appreg.getAppById(new IdDef(appIdStr));
			jarPath = (String)app.getProperty("jar_path");

			File src = new File(jarPath);
			fileName = src.getName();
			if (!DEST_DIR.endsWith(File.separator))
				DEST_DIR += File.separator;
			File destDir = new File(DEST_DIR);
			if (!destDir.exists())
				destDir.mkdirs();
			File destFile = new File(DEST_DIR + fileName);

			// copy the jar to the web dir
			FileCopy.copy(src, destFile);

		} catch (Exception ex) {
			String msg = "Unable to copy app jar.";
			_lw.logDebugMsg(msg, ex);
			_lw.logErr(msg, ex);
			tagTable.put("ERROR", msg);
			return tagTable;
		}


		// prepare the link tag
		String file_link = LINK_DIR + fileName;
		tagTable.put("file_link", file_link);

		return tagTable;
	}

}

