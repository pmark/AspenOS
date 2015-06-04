package org.aspenos.util.appinstall;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.exception.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class AppJarDef extends IdDef {

	public AppJarDef() {
		super();
		setDefName("AppJar");
	}


	public AppJarDef(Map m) {
		super(m, "AppJar"); 
	}

}
