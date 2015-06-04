package org.aspenos.app.aosmailserver.util;

import java.util.*;
import java.io.*;
import org.aspenos.util.*;
import org.aspenos.util.ats.*;


/**
 *
 */
public interface MailConstants {

	public static final String DEF_LANG = "en";
	public static final String DEF_VARIANT = "us";
	public static final String DEF_LOCALE = DEF_LANG + "_" + DEF_VARIANT;
	public static final String AOS_HOME = TaskScheduler._aosHome;

	// You can change these the fully qualified URLs
	// for redirects in the form mailer
	public static final String DEF_ERROR_REDIR = null;
	public static final String DEF_REDIR = null;


	public static final String AOSMAIL_HOME = AOS_HOME + 
		"apps" + File.separator + "AOSMailServer" + File.separator;

	public static final String AOSMAIL_DATA = AOSMAIL_HOME +
		"data" + File.separator;

	public static final String AOSMAIL_TEMPLATE_PATH = AOSMAIL_DATA +
		"mail_templates" + File.separator;

	public static final String AOSMAIL_PROPS_PATH = AOSMAIL_DATA +
		"aosmail.properties";

}


