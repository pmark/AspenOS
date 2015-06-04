package org.aspenos.app.aoscontentserver.defs;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.util.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class RoleDefs extends IdDefs {

	public RoleDefs() {
	}

	public RoleDefs(List l) throws Exception {
		super(l, "org.aspenos.app.aoscontentserver.defs.RoleDef");
	}

	public String toXML() {

		StringBuffer sb = new StringBuffer();
		Iterator it = this.iterator();
		while (it.hasNext()) {
			RoleDef next = (RoleDef)it.next();
			sb.append(next.toXML());
		}
		return sb.toString();
	}

}
