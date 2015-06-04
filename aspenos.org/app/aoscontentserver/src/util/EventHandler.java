package org.aspenos.app.aoscontentserver.util;

import java.sql.*;
import java.util.*;
import javax.servlet.http.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;

public interface EventHandler {

	public void init(LoggerWrapper lw, WebEventRequest wer);
	public void init(LoggerWrapper lw, Properties p, 
			WebEventRequest wer);
	public HashMap handleEvent();

	public void setLoggerWrapper(LoggerWrapper lw);
	public void setProperties(Properties p);
	public String getHeaderTemplate();
	public String getFooterTemplate();

	public boolean useStandardTemplate();

	public HashMap getClonedTagTable();
	public HashMap getRequestParams();
	public TemplateLoader getTemplateLoader();

	// ****************************************************
	// 2/6/01 additions
	// ****************************************************
	/**
	 * Gets a registry from the application's (app_reg_bundle) 
	 * RegistryBundle.
	 */
	public IRegistry getAppRegistry(String regName) 
		throws SQLException;

	/**
	 * Gets a registry from the content server's (cs_reg_bundle) 
	 * RegistryBundle.
	 */
	public IRegistry getCSRegistry(String regName) 
		throws SQLException;

	/**
	 * Event handlers that use registries MUST use this 
	 * method to enable automatic connection pooling.  
	 * NOTE that getAppRegistry and getCSRegistry are provided
	 * for convenience.
	 */
	public IRegistry getRegistry(String bundleName, String regName)
		throws SQLException;

	/**
	 * If the programmer is a good programmer (s)he will
	 * have used EventHandlerParent.getRegistry() 
	 * to retrieve registries so that automatic DB 
	 * connection pooling will work properly.  
	 * This method gets called right after the 
	 * event's handleEvent() method is called
	 * in the EventHandlerServlet.
	 */
	public void returnDbConnections(String sid);
}

