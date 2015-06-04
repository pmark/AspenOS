package org.aspenos.app.aosstorypublisher.util;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.mail.*;
import org.aspenos.logging.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class StoryUtils {

	public static String limitLine(String origLine, 
			int maxLength, boolean useDots) {

		int origLength = origLine.length();

		if (maxLength < 4)
			maxLength = 4;

		// check for no action
		if (origLength <= maxLength)
			return origLine;


		// THE LINE IS TOO LONG ////////////////
		int lastSpacePos = origLine.lastIndexOf(' ', maxLength);
		if (lastSpacePos == -1)
			lastSpacePos = maxLength;

		if (!useDots)
			return origLine.substring(0,lastSpacePos);

		int cutPos = lastSpacePos;

		// use the next space to the left if the
		// ellipses would go past the max length
		if (cutPos+3 > maxLength) {
			if (lastSpacePos > 1);
				cutPos = origLine.lastIndexOf(' ', lastSpacePos-1);
		}

		if (cutPos == -1)
			cutPos = maxLength-3;
		String tmp = origLine.substring(0,cutPos);
		tmp += "...";

		return tmp;
	}


	/**
	 * Capitalizes only the first character of each word.
	 */
	public static String formatTitle(String title) {
		StringTokenizer st = new StringTokenizer(
				title.trim().toLowerCase());

		String tmpStr;
		StringBuffer finalStr = new StringBuffer();

		char[] b;
		boolean firstWord=true;
		while (st.hasMoreTokens()) {
			tmpStr = st.nextToken();

			if (firstWord || (
					!tmpStr.equals("a") &&
					!tmpStr.equals("the") &&
					!tmpStr.equals("or") &&
					!tmpStr.equals("but") &&
					!tmpStr.equals("and"))) {
				b = new char[tmpStr.length()];
				tmpStr.getChars(0,tmpStr.length(),b,0);
				b[0] = Character.toUpperCase(b[0]);
				tmpStr = new String(b);
			}

			finalStr.append(tmpStr).append(" ");
			firstWord=false;
		}
		return finalStr.toString();
	}

}
