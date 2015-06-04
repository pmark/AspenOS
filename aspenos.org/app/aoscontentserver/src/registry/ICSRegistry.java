package org.aspenos.app.aoscontentserver.registry;

import org.aspenos.util.*;

/**
 * The root of all AOSContentServer (CS) registry interfaces.
 */
public interface ICSRegistry extends IRegistry {

	/**
	 * Sets the name of the group used by a CS Registry.  
	 * The group is used to identify the proper set of 
	 * database tables for an app or group of apps.
	 *
	 * For example, a web event called "home" by one
	 * app cannot be confused with one named "home" by
	 * a different app.  If those two apps were in 
	 * different registry groups, the "home" identifier 
	 * could be used by both apps.
	 * 
	 */
	public void setRegistryGroupName(String regGroupName);


	/**
	 * Retrieves the registry group name.
	 */
	public String getRegistryGroupName();

}


