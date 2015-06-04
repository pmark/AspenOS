package org.aspenos.app.aossystemconsole.eh.eventconfig;

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
import org.aspenos.app.aossystemconsole.eh.*;



/**
 * App Event Configuration.
 */
public class RemoveEvent extends SysConEHParent {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		tagTable.put("sub_titles", 
				"WEB EVENT CONFIGURATION");

		// Get the event id to remove

		// Remove this event from the registry

		// Remove all event-resource records for this event

		// Set up a redirect
		tagTable.put("REDIR_URL", _appBaseURI + 
				"?webevent_name=choose_event");

		return tagTable; 
	}
}

