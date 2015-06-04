package org.aspenos.app.aossystemconsole.server;

import java.io.*;
import java.util.*;
import javax.servlet.*;

import org.aspenos.app.aoscontentserver.server.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;


public class SystemConsole extends EventHandlerServlet {


	/**
	 * This init is called just so that the jar path 
	 * can be set before calling the super.init().
	 **/
	public void init(ServletConfig sc) throws ServletException {
		try {
			// Let EHS set all of the props with this jar
			System.setProperty("sax.parser.class",
					"org.apache.xerces.parsers.SAXParser");
			System.setProperty("sax.parser.validating",
					"false");

			setAppDir("AOSSystemConsole");
			setAppJar("systemconsole.jar");
			super.init(sc);

		} catch (Exception e) {

			if (__lw != null) {
				__lw.logErr("SystemConsole.init()", 
						"throwing ServletException: " + e);
				__lw.logDebugMsg("SystemConsole.init()", 
						"throwing ServletException: " + e);
			} else {
				System.err.println("SystemConsole.init()" +
						"\nthrowing ServletException: " + e);
			}

			ServletException se = (ServletException)e.fillInStackTrace();
			throw se;
		}
	} 

}
