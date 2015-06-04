package org.aspenos.app.aossystemconsole.eh;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.util.*;


/**
 * The default behavior for a System Console event 
 * handler is to load the content server's default
 * header and footer templates, which is taken care
 * of by EventHandlerParent.
 */
public class SysConEHParent extends EventHandlerParent {

	protected static final String FORM_NAME = "choose_form";
	protected static final String MAX_ORDINAL = "9999";
	protected static final String EMPTY_INT_SEL = "-1";

	public void setupTagTable() {

		if (_tagTable == null)
			_tagTable = new HashMap();

		//_appBaseURI += "console";

		StringBuffer headerData = new StringBuffer()
			.append("<style>")
			.append(".list_style {font-family: Courier, ")
			.append("Courier New, Lucida Console}</style>");

		_lw.logDebugMsg("SysConEHParent: setting up default tags");
		_tagTable.put("main_title", "AOS System Console");
		_tagTable.put("main_logo_image", "/aspenos/images/sys_console.gif");
		_tagTable.put("main_logo_width", "80");
		_tagTable.put("main_logo_height", "80");
		_tagTable.put("header_data", headerData.toString());
		_tagTable.put("header_sep_color", "#000000");
		_tagTable.put("footer_sep_color", "#000000");
		_tagTable.put("footer_logo_image", "dot_clear.gif");
		_tagTable.put("form_action", _appBaseURI);
		_tagTable.put("form_name", FORM_NAME);
	}
}

