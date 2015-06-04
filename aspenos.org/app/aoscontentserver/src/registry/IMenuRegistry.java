package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;

/**
 *
 */
public interface IMenuRegistry extends ICSRegistry {

	
	/** 
	 * Returns a menu for a given menu ID, based on a given role ID.  
	 * A MenuDef should contain all available ButtonDefs for that menu,
	 * and availability of a button depends upon whether or the given
	 * role has access to that button's target event.
	 *
	 * @param mid of the menu to get
	 * @param roleid with which to compare the menu's buttons' target events 
	 * @return A menu with buttons that allow only events that 
	 *  the roleid allows
	 */
	public MenuDef getMenuById(String mid)
			throws SQLException;


	/** 
	 * Returns a menu for a given menu ID, based on a given role ID.  
	 * A RoleBasedMenu contains all buttons and a parent menu, if
	 * there is one.  The availability of a button depends upon 
	 * whether the given role has access to that button's target event.
	 * <br>
	 * Currently, each menu (considering parent menus as separate menus)
	 * takes a total of five database queries.  A menu cache is necessary.
	 * <br>
	 * @param mid of the menu to get
	 * @param roleid with which to compare the menu's buttons' target events 
	 * @return A menu with buttons that allow only events that 
	 *  the are accessible to the given role
	 */
	public RoleBasedMenu getRoleBasedMenu(String mid, String roleid) 
			throws Exception;


	/** 
	 * Returns the menu ID for a given menu ID.
	 *
	 * @param mid of the child menu
	 * @return the parent menu ID
	 */
	public IdDef getParentMenuId(String mid)
			throws SQLException;


	/** 
	 * Returns the menu name for a given menu ID.
	 *
	 * @param mid of the child menu
	 * @return the parent menu name
	 */
	public String getParentMenuName(String mid)
			throws SQLException;


	/** 
	 * Returns a set of buttons for a menu/role combination.
	 *
	 * @param mid that contains the buttons
	 * @param roleid with which to compare the menu's buttons' target events 
	 * @return the buttons for the menu
	 */
	public MenuButtonDefs getMenuButtons(String mid) 
			throws Exception;


	/**
	 * Returns a subset of the given buttons.  
	 * Only buttons that link to events that
	 * are accessible by the role will be returned.
	 */
	public MenuButtonDefs filterButtonsByRole(
			MenuButtonDefs buttons, String roleid) 
			throws SQLException;


	/**
	 * Builds a list of RoleBasedMenuButton objects
	 * by getting the icon info for each button defined
	 * in 'buttonDefs'.
	 */
	public List getRoleBasedMenuButtons(MenuButtonDefs buttonDefs) 
			throws SQLException;


	/**
	 * Given a set of buttons, this returns their icons.
	 */
	public IconDefs getButtonIconDefs(MenuButtonDefs buttonDefs) 
			throws SQLException;


	/**
	 * Stores a set of resources.
	 */
	public void storeMenuDefs(MenuDefs defs)
			throws SQLException;

}


