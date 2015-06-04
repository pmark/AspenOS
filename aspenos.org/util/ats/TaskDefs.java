package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class TaskDefs extends IdDefs {

	private Map _appRegBundles = null;
	private Map _templateLoaders = null;

	public TaskDefs() {
	}

	public TaskDefs(List l) throws Exception {
		super(l, "org.aspenos.util.ats.TaskDef");
	}

	public void setRegBundles(Map rb) {
		_appRegBundles = rb;
	}

	public void setTemplateLoaders(Map tl) {
		_templateLoaders = tl;
	}

	public Map getRegBundles() {
		return _appRegBundles;
	}

	public Map getTemplateLoaders() {
		return _templateLoaders;
	}
}
