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
public class ERTDefs extends IdDefs {

	public ERTDefs() {
	}

	public ERTDefs(List l) throws Exception {
		super(l, "org.aspenos.app.aoscontentserver.defs.ERTDef");
	}

	public String toXML() {

		StringBuffer sb = new StringBuffer();
		Iterator it = this.iterator();
		while (it.hasNext()) {
			ERTDef next = (ERTDef)it.next();
			sb.append(next.toXML());
		}
		return sb.toString();
	}

}
