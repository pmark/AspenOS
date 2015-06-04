package org.aspenos.app.aosmailserver.server;

import java.io.*;
import java.util.*;
import javax.servlet.*;

import org.aspenos.app.aoscontentserver.server.*;
import org.aspenos.logging.*;


public class AOSMail extends EventHandlerServlet {


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

			setAppDir("AOSMailServer");
			setAppJar("aosmailserver.jar");
			super.init(sc);

			__anonRole = true;
			__checkSession = false;
			__checkRoles = false;

		} catch (Exception e) {

			if (__lw != null) {
				__lw.logErr("AOSMail.init()", 
						"throwing ServletException: " + e);
				__lw.logDebugMsg("AOSMail.init()", 
						"throwing ServletException: " + e);
			} else {
				System.err.println("AOSMail.init()" +
						"\nthrowing ServletException: " + e);
			}

			ServletException se = (ServletException)e.fillInStackTrace();
			throw se;
		}
	} 

}
