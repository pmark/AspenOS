package org.aspenos.app.aossystemconsole.eh;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.exception.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.db.*;



public class Home extends SysConEHParent {

	public HashMap handleEvent() {

		_lw.logDebugMsg("Home: setting up cloned tag table");
		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", "HOME");
		return tagTable; 
	}
}

