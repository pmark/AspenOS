package org.aspenos.app.aosmailserver.util;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.mail.*;
import org.aspenos.logging.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class MailUtils {

	public static String formatMap(Map params) {
		String str = params.toString();
		str = str.substring(1,str.length()-1);
		StringTokenizer st = new StringTokenizer(str, ",=");
		StringBuffer sb = new StringBuffer();

		while (st.hasMoreTokens()) {
			sb.append("\n")
				.append(st.nextToken())
				.append(" = ")
				.append(st.nextToken());
		}

		return sb.toString();
	}
}
