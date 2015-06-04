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
public class RoleBasedMenuButton {

	private IconDef _iconDef = null;
	private MenuButtonDef _buttonDef = null;


	public RoleBasedMenuButton() {
	}

	public RoleBasedMenuButton(
			MenuButtonDef button, IconDef icon) {

		setButton(button);
		setIcon(icon);
	}



	public void setIcon(IconDef icon) {
		_iconDef = icon;
	}

	public void setButton(MenuButtonDef button) {
		_buttonDef = button;
	}



	public IconDef getIcon() {
		return _iconDef;
	}

	public MenuButtonDef getButton() {
		return _buttonDef;
	}
}
