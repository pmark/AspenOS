package org.aspenos.app.aoscontentserver.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.defs.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class RoleBasedMenu {

	private MenuDef _menuDef = null;
	private List _buttons = null;
	private RoleBasedMenu _parent = null;


	public RoleBasedMenu() {
	}


	public void setParent(RoleBasedMenu parent) {
		_parent = parent;
	}

	public void setMenu(MenuDef menu) {
		_menuDef = menu;
	}

	public void setButtons(List buttons) {
		_buttons = buttons;
	}


	public RoleBasedMenu getParent() {
		return _parent;
	}

	public MenuDef getMenu() {
		return _menuDef;
	}

	public List getButtons() {
		return _buttons;
	}
}
