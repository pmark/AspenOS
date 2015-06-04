package org.aspenos.app.aosstorypublisher.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class StoryDef extends IdDef {

	private StoryHeaderDef _header = null;

	public StoryDef() {
		super();
		setDefName("Story");
	}


	public StoryDef(Map m) {
		super(m, "Story"); 
	}

	public void setHeader(StoryHeaderDef header) {
		_properties.putAll(header.getProperties());
		_header = header;
	}

	public StoryHeaderDef getHeader() {
		return _header;
	}


	public String getSqlFieldsAndValues() {
		return _header.getSqlFieldsAndValues();
	}

	public String getSqlUpdateFandV() {
		return _header.getSqlUpdateFandV();
	}


}
