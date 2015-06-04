package org.aspenos.app.aoscontentserver.util;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aossystemconsole.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;

/**
 *
 */
public class CSRegistryFactory extends RegistryFactory 
		implements ICSConstants {

	private String _groupName;

	// Init Methods ==========================================================
	public CSRegistryFactory() {
		_groupName = null;
	}

	public CSRegistryFactory(String groupName) {
		_groupName = groupName;
	}


	/**
	 * This is different than the RegistryFactory in that
	 * it sets a registry's group name.
	 */
	public IRegistry createRegistry(String regClass) 
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		Class c = Class.forName(regClass);
		CSRegistry reg = (CSRegistry)c.newInstance();

		if (_groupName != null)
			reg.setRegistryGroupName(_groupName);
			//reg.setProperty(AOSCS_RG_KEY, _groupName);

		return (IRegistry)reg;
	}

/*
	// Non app-specific registries ========================================
	public PrincipalRegistry getPrincipalRegistry() {
		return new PrincipalRegistry( );
	}

	public SessionRegistry getSessionRegistry() {
		return new SessionRegistry( );
	}

	public RoleRegistry getRoleRegistry() {
		return new RoleRegistry( );
	}

	public AppRegistry getAppRegistry() {
		return new AppRegistry( );
	}

	public VendorRegistry getVendorRegistry() {
		return new VendorRegistry( );
	}



	// App-specific registries ===========================================
	public MenuRegistry getMenuRegistry(String groupName) {
		MenuRegistry r = new MenuRegistry( );
		r.setRegistryGroupName(groupName);
		return r;
	}

	public ResourceRegistry getResourceRegistry(String groupName) {
		ResourceRegistry r = new ResourceRegistry(  );
		r.setRegistryGroupName(groupName);
		return r;
	}

	public TemplateRegistry getTemplateRegistry(String groupName) {
		TemplateRegistry r = new TemplateRegistry( );
		r.setRegistryGroupName(groupName);
		return r;
	}

	public WebEventRegistry getWebEventRegistry(String groupName) {
		WebEventRegistry r = new WebEventRegistry( );
		r.setRegistryGroupName(groupName);
		return r;
	}
*/
}


