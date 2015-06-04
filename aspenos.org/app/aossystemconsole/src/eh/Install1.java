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



public class Install1 extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"INSTALLING A NEW APPLICATION (step 1 of 3)");
		return tagTable; 
	}
}

