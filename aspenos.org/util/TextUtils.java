package org.aspenos.util;

import java.io.*;
import java.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class TextUtils {

	public static final int MAX_CHARS_PER_LINE = 69;


	public static String formatBody(String body, boolean useHTML)
			throws IOException {

		StringBuffer formatted = new StringBuffer();

		BufferedReader br = new BufferedReader(
				new StringReader(body));

		String line, lhs, rhs, newLine;
		int pos;
		while ((line=br.readLine()) != null) {

			rhs = line;
			while (rhs.length() > MAX_CHARS_PER_LINE) {

				// find the new end of the line
				pos = rhs.lastIndexOf(" ", MAX_CHARS_PER_LINE);

				if (pos == -1) {
					// This long line has no spaces at all
					pos = MAX_CHARS_PER_LINE;
				}

				// get the new LHS
				lhs = rhs.substring(0,pos);

				// get the new RHS
				if (rhs.length() > pos) {
					rhs = rhs.substring(pos+1);
				} 

				// write the current line
				formatted.append(lhs);
				formatted.append("\n");
				if (useHTML)
					formatted.append("<BR>");
			} 

			// write the current line
			formatted.append(rhs);
			formatted.append("\n");
			if (useHTML)
				formatted.append("<BR>");
		}

		return formatted.toString().trim();
	}


	/**
	 * Comma Separated Values.
	 */
	public static String makeListCSV(List strings) {
		StringBuffer sb = new StringBuffer();
		Iterator it = strings.iterator();
		String value;
		boolean first = true;
		while (it.hasNext()) {
			if (first) first = false;
			else sb.append(", ");

			value = (String)it.next();
			sb.append(value);
		}

		return sb.toString();
	}


	/**
	 * Line Separated Values.
	 */
	public static String makeListLSV(List strings) {
		StringBuffer sb = new StringBuffer(1024);
		Iterator it = strings.iterator();
		String value;
		while (it.hasNext()) {
			value = (String)it.next();
			sb.append(value).append("\n");
		}

		return sb.toString();
	}


	/**
	 * xxx yyy zzz becomes xxxyyyzzz.
	 */
	public static String removeSpaces(String orig) {
		StringTokenizer st = new StringTokenizer(orig, " ");
		StringBuffer sb = new StringBuffer(orig.length());
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());		
		}
		if (sb.toString().length() == 0)
			return orig;
		return sb.toString();
	}


	public static void main(String[] args) {
		System.out.println(TextUtils.removeSpaces("a b  c   d"));
	}
}
