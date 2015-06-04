package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;
import org.aspenos.db.*;

/**
 *
 */
public class MenuRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public MenuRegistry() {
	}

	public MenuRegistry(DbPersistence db) {
		_db = db;
	}

	// Primary Methods =============================================

	/** 
	 * Returns a menu for a given menu ID.
	 *
	 * @param mid of the menu to get
	 * @return A MenuDef with all of this menu's properties
	 */
	public MenuDefs getAllMenus() 
			throws Exception {
		return getAllMenus(null);
	}

	public MenuDefs getAllMenus(String rgKey) 
			throws Exception {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		String regGroup;
		if (rgKey == null || rgKey.equals(""))
			regGroup = _regGroup;
		else 
			regGroup = rgKey;

		attribs		= " * ";
		from 		= new StringBuffer("menu")
			.append(_regGroupSeparator).append(regGroup);

		List l = _db.selectAsHash(attribs, from.toString(), "");

		if (l == null || l.size() == 0)
			return null;

		return new MenuDefs(l);
	}




	/** 
	 * Returns a menu for a given menu ID.
	 *
	 * @param mid of the menu to get
	 * @return A MenuDef with all of this menu's properties
	 */
	public MenuDef getMenuByName(String name) 
			throws SQLException {
		return getMenuByName(null, name);
	}

	public MenuDef getMenuByName(String rgKey, String name) 
			throws SQLException {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		String regGroup;
		if (rgKey == null || rgKey.equals(""))
			regGroup = _regGroup;
		else 
			regGroup = rgKey;

		attribs		= " * ";
		from 		= new StringBuffer("menu")
			.append(_regGroupSeparator).append(regGroup);
		where 		= new StringBuffer(" name='")
			.append(name).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		MenuDef menu = new MenuDef(hash);
		Integer id = (Integer)hash.get("menu_id");
		if (id != null)
			menu.setId(id.toString());

		return menu;
	}


	/** 
	 * Returns a menu for a given menu ID.
	 *
	 * @param mid of the menu to get
	 * @return A MenuDef with all of this menu's properties
	 */
	public MenuDef getMenuById(String mid) 
			throws SQLException {
		return getMenuById(null, mid);
	}

	public MenuDef getMenuById(String rgKey, String mid) 
			throws SQLException {

		String regGroup;
		if (rgKey == null || rgKey.equals(""))
			regGroup = _regGroup;
		else 
			regGroup = rgKey;

		StringBuffer from  = new StringBuffer("menu")
			.append(_regGroupSeparator).append(regGroup);
		StringBuffer where = new StringBuffer("menu_id=")
			.append(mid);

		// SELECT * FROM menu WHERE menu_id=mid
		HashMap hash = (HashMap)_db.selectFirstAsHash("*", from.toString(),
				where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		MenuDef menu = new MenuDef(hash);
		Integer id = (Integer)hash.get("menu_id");
		if (id != null)
			menu.setId(id.toString());

		return menu;
	}


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
	 * @param prinRoleIds with which to compare the menu's buttons' target events 
	 * @return A menu with buttons that allow only events that 
	 *  the are accessible to the given role
	 */
	public RoleBasedMenu getRoleBasedMenu(String mid, List prinRoleIds,
			Map eventRoleMap) 
			throws Exception {

		RoleBasedMenu rbm = new RoleBasedMenu();

		// Get this menu
		MenuDef menu = getMenuById(mid);

		String menuName = (String)menu.getProperty("name");

		// Get this menu's buttons
		MenuButtonDefs buttonDefs = getMenuButtons(menuName);

		// Remove any buttons that link to inaccessible events
		buttonDefs = filterButtonsByRole(buttonDefs, prinRoleIds, eventRoleMap);

		// Load the icon info for each button
		// and make RoleBasedMenuButton objects
		ArrayList rbmButtons = (ArrayList)
			getRoleBasedMenuButtons(buttonDefs);

		// Get this menu's parent ID
		IdDef parentMenuId = getParentMenuId(mid);
		String strParentMenuId = parentMenuId.getId();

		// If this menu has a parent, set it
		if (strParentMenuId != null &&
			!strParentMenuId.equals(mid)) {
			rbm.setParent(getRoleBasedMenu(
					strParentMenuId, prinRoleIds, eventRoleMap));
		}

		// Set up the RBM
		rbm.setMenu(menu);
		rbm.setButtons(rbmButtons);

		return rbm;
	}


	/** 
	 * Returns the parent menu name for a given menu ID.
	 *
	 * @param mid of the child menu
	 * @return the parent menu name
	 */
	public String getParentMenuName(String mid) 
			throws SQLException {

		String select;
		StringBuffer from;
		StringBuffer where;

		select 		= "parent_name";
		from		= new StringBuffer("menu")
			.append(_regGroupSeparator).append(_regGroup);
		where 		= new StringBuffer(" menu_id=")
			.append(mid);
			
		String name = (String)_db.selectFirstAttrib(
				select, from.toString(), where.toString());
 
		return name;
	}


	/** 
	 * Returns the parent menu ID for a given menu ID.
	 *
	 * @param mid of the child menu
	 * @return the parent menu ID
	 */
	public IdDef getParentMenuId(String mid) 
			throws SQLException {


		String parentName = getParentMenuName(mid);

		String select;
		StringBuffer from;
		StringBuffer where;

		select 		= "menu_id";
		from		= new StringBuffer("menu")
			.append(_regGroupSeparator).append(_regGroup);
		where 		= new StringBuffer(" name='")
			.append(parentName).append("'");
			
		Integer parentId = (Integer)_db.selectFirstAttrib(
				select, from.toString(), where.toString());
 
		return new IdDef(parentId.toString());
	}


	/** 
	 * Returns a set of buttons for a menu/role combination.
	 *
	 * @return the buttons for the menu
	 */
	public MenuButtonDefs getMenuButtons(String menuName) 
			throws Exception {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= " * ";
		from		= new StringBuffer("menubtn")
			.append(_regGroupSeparator).append(_regGroup);
		where 		= new StringBuffer(" menu_name='")
			.append(menuName).append("'");

		List buttons = _db.selectAsHash(attribs, from.toString(), 
				where.toString());

		return new MenuButtonDefs(buttons);
	}


	/**
	 * Returns a subset of the given buttons.  
	 * Only buttons that link to events that
	 * are accessible by the role will be returned.
	 *
	 * 3/13/01 added Multiple Role Support
	 *
	 * @param buttons a list of all buttons on the menu
	 * @param prinRoleIds a list of all roles allowed
	 */
	public MenuButtonDefs filterButtonsByRole(
			MenuButtonDefs buttons, List prinRoleIds,
			Map eventRoleMap) 
			throws SQLException {

		ArrayList origEventIds = new ArrayList();

		// Get each button's web event ID
		Iterator it = buttons.iterator();
		while (it.hasNext()) {
			MenuButtonDef mb = (MenuButtonDef)it.next();
			String weName = (String)mb.getProperty("event_name");
			WebEventRegistry webEventReg = new WebEventRegistry(_db);
			WebEventDef wed = webEventReg.getEventByName(_regGroup, weName);
			String weid = ((Integer)wed.getProperty("webevent_id"))
				.toString();
			origEventIds.add(weid);
		}


		///////////////////////////////////
		// Sort through eventRoleMap finding valid matches
		// eventRoleMap has all events IDs
		// prinRoleIds are the ones that are allowed
		List eventRoleIds;
		ArrayList remButtons = new ArrayList();
		debug("prin roles: " + prinRoleIds.toString());

		it = origEventIds.iterator();
		for (int i=0; it.hasNext(); i++) {

			// get the next button's event ID
			String origId = (String)it.next();

			// get this event's roles
			eventRoleIds = (List)eventRoleMap.get(origId);

			// try to find a match between prin and event roles
			if (!canFindOneMatch(eventRoleIds, prinRoleIds)) {
				remButtons.add(buttons.get(i));	
			}
		}

		// this has been separated so that the indexing
		// would work in the above loop.
		it = remButtons.iterator();
		while (it.hasNext()) {
			MenuButtonDef btn = (MenuButtonDef)it.next();
			buttons.remove(btn);
		}


		return buttons;
	}


	private boolean canFindOneMatch(List eventRoleIds, List prinRoleIds) {
		String prinRoleId;
		Iterator pit = prinRoleIds.iterator();
		while (pit.hasNext()) {
			prinRoleId = (String)pit.next();
			if (eventRoleIds.contains(prinRoleId))
				return true;
		}
		return false;
	}


	/**
	 * Returns a large WHERE statement for use in filterButtonsByRole.
	 *
		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE wer.resource_id=rt.resource_id 
		//  AND wer.webevent_id=1
		//  AND rt.role_id=37
		//  UNION
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE wer.resource_id=rt.resource_id 
		//  AND wer.webevent_id=2
		//  AND rt.role_id=37
		//  ...there is another UNION for each webevent ID
		/////////////////////////////////////////////////////////////
	 */
	private String buildHugeEventWhere(
			String roleid, ArrayList events, String attribs, String from) {
		
		StringBuffer finalWhere = new StringBuffer();

		StringBuffer templateWhere = new StringBuffer(
			"wer.resource_id=rt.resource_id AND rt.role_id=")
			.append(roleid)
			.append(" ");

		StringBuffer unionSelect = new StringBuffer(" UNION SELECT ")
			.append(attribs)
			.append(" FROM ")
			.append(from)
			.append(" WHERE ");


		Iterator it = events.iterator();
		while (it.hasNext()) {

			String weid = (String)it.next();

			StringBuffer eventWhere = new StringBuffer(" AND wer.webevent_id=")
				.append(weid)
				.append(" ");

			finalWhere.append(templateWhere.toString())
				.append(eventWhere.toString());
			if (it.hasNext())
				finalWhere.append(unionSelect.toString());
		}

		return finalWhere.toString();
	}

	/**
	 * Builds a list of RoleBasedMenuButton objects
	 * by getting the icon info for each button defined
	 * in 'buttonDefs'.
	 */
	public List getRoleBasedMenuButtons(MenuButtonDefs buttonDefs) 
			throws SQLException {

		ArrayList rbmbList = new ArrayList();
		IconDefs icons = getButtonIconDefs(buttonDefs);

		Iterator it = buttonDefs.iterator();
		for (int i=0; it.hasNext(); i++) {


			// Get the next button and icon in the list
			MenuButtonDef button = (MenuButtonDef)it.next();
			IconDef icon = (IconDef)icons.get(i);

			RoleBasedMenuButton rbmb = new RoleBasedMenuButton(button, icon);
			rbmbList.add(rbmb);
		}

		return rbmbList;
	}


	/**
	 * Given a set of buttons, this returns their icons.
	 */
	public IconDefs getButtonIconDefs(MenuButtonDefs buttonDefs) 
			throws SQLException {

		String iconName;
		ArrayList iconNames = new ArrayList();
		Iterator it = buttonDefs.iterator();
		while (it.hasNext()) {
			MenuButtonDef mb = (MenuButtonDef)it.next();
			iconName = (String)mb.getProperty("icon_name");
			iconNames.add(iconName);
		}

		// Prepare one query for all button icons:
		StringBuffer from = new StringBuffer("icon")
			.append(_regGroupSeparator).append(_regGroup);

		String iconWhere = DbTranslator.buildWhereStringList(
				iconNames,"name","OR", true);
		List icons = _db.selectAsHash("*", from.toString(), iconWhere);

		// Build an IconDefs object
		// Note that the icon records from the database may
		// not be in the correct order.
		HashMap unorderedIconMap = new HashMap();
		it = icons.iterator();
		while (it.hasNext()) {
			HashMap hash = (HashMap)it.next();
			iconName = (String)hash.get("name");
			unorderedIconMap.put(iconName, new IconDef(hash));
		}


		IconDefs iconDefs = new IconDefs();
		it = iconNames.iterator();
		while (it.hasNext()) {
			iconName = (String)it.next();
			iconDefs.add((IconDef)
					unorderedIconMap.get(iconName));
		}

		return iconDefs;
	}


	/**
	 * Stores a set of menus.
	 */
	public void storeMenuDefs(MenuDefs defs)
			throws SQLException {

		StringBuffer insert = new StringBuffer("INSERT INTO menu_")
				.append(_regGroup)
				.append(" ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			MenuDef def = (MenuDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			_db.insert(tmp);
		}
	}


	/**
	 * Stores a set of menu buttons.
	 */
	public void storeMenuButtonDefs(MenuButtonDefs defs)
			throws SQLException {

		StringBuffer insert = new StringBuffer("INSERT INTO menubtn_")
				.append(_regGroup)
				.append(" ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			MenuButtonDef def = (MenuButtonDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			//debug("MR: " + tmp);

			_db.insert(tmp);
		}
	}


	/**
	 * Stores a set of icons.
	 */
	public void storeIconDefs(IconDefs defs)
			throws SQLException {

		StringBuffer insert = new StringBuffer("INSERT INTO icon_")
				.append(_regGroup)
				.append(" ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			IconDef def = (IconDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			_db.insert(tmp);
		}
	}


	/**
	 * Gets all menus for this app, then gets all events
	 * for each button, then gets all the roles for each
	 * event and maps the event ID to the event roles.
	 */
	public Hashtable cacheMenuEventRoles()
			throws Exception {

		Hashtable erCache = new Hashtable();
		ArrayList origEventIds = new ArrayList();

		MenuDefs menus = getAllMenus();
		Iterator mit = menus.iterator();
		while (mit.hasNext()) {

			// get the menu
			MenuDef menu = (MenuDef)mit.next();
			String menuName = (String)menu.getProperty("name");
			MenuButtonDefs buttonDefs = getMenuButtons(menuName);

			// Get each button's web event ID
			Iterator it = buttonDefs.iterator();
			while (it.hasNext()) {
				MenuButtonDef mb = (MenuButtonDef)it.next();
				String weName = (String)mb.getProperty("event_name");
				WebEventRegistry webEventReg = new WebEventRegistry(_db);
				WebEventDef wed = webEventReg.getEventByName(_regGroup, weName);
				String weid = ((Integer)wed.getProperty("webevent_id"))
					.toString();
				origEventIds.add(weid);

				// Now I have each button's event ID for this menu
				// so it's time to get that button's roles
				List eventRoleIds = webEventReg.getEventRoles(_regGroup,weid);
				erCache.put(weid, eventRoleIds);
			}
		}
		return erCache;
	}

}


