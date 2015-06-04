package org.aspenos.util;

import java.util.*;
import java.io.*;

public class HtmlTool
{
	/**
	 *
	 **/
	public static String getSpaces(int n)
	{
		String str = new String();

		for (int i=0; i<n; i++)
			str += "&nbsp;";

		return str;
	}

	/**
	 * Make sure that the column definitions 
	 * don't have spaces after the commas. 
	 *
	 * Sample call:
	  	    String myList = createSelectionList(
				some_db.hashedFind("*", "some_table", "num=4"),
				"some_table_id",
				"dbCol1,40,dbCol2,65",
				"Display Column 1,40,Display Column 2,65"
				"the_select", 12);
	 **/
	public static String createSelectionList(
			Vector data,
			String dbColumns, 
			String dispColumns,
			String selectName,
			String selectValue,
			int selectSize)
	{
		String html = new String(); 
		StringTokenizer dbSt = new StringTokenizer(dbColumns, ",");
		StringTokenizer dispSt = new StringTokenizer(dispColumns, ",");
		String colName, header = new String();
		String tmp;
		int colWidth;
		int i, j;
		int numDispCols = dispSt.countTokens() / 2;
		int numDbCols = dbSt.countTokens() / 2;
		int[] dbColWidths = new int[numDbCols];
		String[] dbColNames = new String[numDbCols];
		Hashtable hash;


		// Create the header for the table
		for (j=0; j < numDispCols; j++)
		{
			colName = dispSt.nextToken();
			colWidth = Integer.parseInt(dispSt.nextToken());
			i = colName.length();


			if (j == 0 || j < numDispCols - 1)
			{
				if (colWidth < i)
					colWidth = i;
				// Add the column name BEFORE the trailing spaces
				header += colName + getSpaces(colWidth - i);
			}
			else
				header += getSpaces(colWidth - i) + colName;
		}

		// Now do a similar task for the selections
		for (j=0; j < numDbCols; j++)
		{
			colName = dbSt.nextToken();
			colWidth = Integer.parseInt(dbSt.nextToken());
			i = colName.length();

			if ((j == 0 || j < numDispCols - 1) && colWidth < i)
				colWidth = i;

			dbColNames[j] = colName;
			dbColWidths[j] = colWidth;
		}

		html += header + "\n<BR>\n";
		html += "<select class=sty1 name=" + selectName + 
			" size=" + selectSize + ">";

		// Add the data to a SELECT box
		if (data != null)
		{
			for (i=0; i < data.size(); i++)
			{
				// Associate the specified value with each
				// selection option
				hash = (Hashtable)data.elementAt(i);
				html += "<option value='" + 
					(String)hash.get(selectValue) + "'>";

				for (j=0; j < numDbCols; j++)
				{
					colName = dbColNames[j];
					colWidth = dbColWidths[j]; 
					
					tmp = (String)hash.get(colName);

					if (j == 0 || j < numDbCols - 1)
						html += tmp + getSpaces(colWidth - tmp.length());
					else
						html += getSpaces(colWidth - tmp.length()) + tmp;
							
							
				}
				html += "\n";
			}
		}

		return html;
	}


	/**
	 * Parse the HTML content for references to URLs that need to 
	 * be loaded along with this URL.  This includes images, 
	 * applets, frames, etc.
	 *
	 * @returns a Vector of Strings listing the URLs
	 * usage: parseHTMLForRefs(page, "href")
	 */
    public static Vector parseHTMLForRefs(String html, String tags) 
	{
		Hashtable tagHash = new Hashtable();
		StringTokenizer tst = new StringTokenizer(tags, "|");
		while  (tst.hasMoreTokens()) {
			String s = tst.nextToken();
			tagHash.put(s, s);
		}

		Vector reflist = new Vector();
		StreamTokenizer st = new StreamTokenizer(new StringReader(html));
    
		st.resetSyntax();
		st.whitespaceChars('<','<');
		st.wordChars(0,59);
		st.wordChars(63,255);
		st.wordChars(61,61);
		st.eolIsSignificant(false);
    
		boolean inTag=false;
		boolean inComment=false;
		boolean isNotTag = true;
		boolean isPrevNotTag = true;
		String prev = null;

		try {
			int res;
			while ((res=st.nextToken())!=st.TT_EOF) 
			{
								// Are we in a mulitline comment?
				if (inComment) 
				{
					// if this ends with a -- it means we wound the end of the 
					// mulitiline comment. We need to toggle the state of inTag also.
					if (st.ttype==st.TT_WORD && st.sval.endsWith("--")) 
					{
						inComment=false;	      
						inTag=!inTag;
						st.resetSyntax();
						st.whitespaceChars('<','<');
						st.wordChars(0,59);
						st.wordChars(63,255);
						st.wordChars(61,61);
						st.eolIsSignificant(false);
					}
				} 
				else 
				{
					inTag=!inTag;
					if (!inTag) 
					{
						st.resetSyntax();
						st.whitespaceChars('<','<');
						st.wordChars(0,59);
						st.wordChars(63,255);
						st.wordChars(61,61);
						st.eolIsSignificant(false);
		    
						isPrevNotTag = isNotTag;
						isNotTag = true;
					}
					else 
					{
						st.resetSyntax();
						st.whitespaceChars('>','>');
						st.wordChars(0,59);
						st.wordChars(63,255);
						st.wordChars(61,61);
						st.eolIsSignificant(false);
						isPrevNotTag = isNotTag;
						isNotTag = false;
		    
						//Is this a multiline comment?
						if (st.sval != null && st.sval.startsWith("!--") && 
							!st.sval.endsWith("--")) 
						{
							inComment=true;
						}
					}
		
					prev = st.sval;
					if (prev != null && prev.trim().length() != 0 && 
						!isNotTag && !inComment && 
						!prev.startsWith("!--"))
					{
						parseTag(prev, reflist, tagHash);
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("HtmlTool: " + ex);	
		}
    
		return reflist;
    }

    private static void parseTag(String tagstmt, 
			Vector reflist, Hashtable tagHash) 
	{
		tagstmt = tagstmt.trim();
		if (tagstmt.indexOf("=") == -1) return;

		String ltag = tagstmt.toLowerCase();
		int stmtlen = ltag.length();
		int start = 0, holder = 0, prevStart = 0;
		int startLink = 0, endLink = 0;
		int i;
		boolean inSQuote = false, inDQuote = false;
		boolean foundRef = false;
		char c;

		for (i = 0; i < stmtlen; i++) 
		{
			c = ltag.charAt(i);
			if (Character.isWhitespace(c)) 
			{
				if (!inSQuote && !inDQuote) 
				{
					while ((i + 1) < stmtlen && Character.isWhitespace(ltag.charAt(i+1))) 
					{
						++i;
					}
					if (!((i + 1) < stmtlen && 
						  ltag.charAt(i + 1) == '=')) {
						start = i + 1;
					}

					if (foundRef) {
						if (startLink > endLink) endLink = i;
						reflist.addElement(tagstmt.substring(startLink, endLink));
						foundRef = false;
					}
					endLink = i;
					prevStart = i;
				}
			} else if (c == '\'') {
				if (!inDQuote) {
					inSQuote = !inSQuote;
					if (inSQuote) {
						startLink = i + 1;
					} else {
						endLink = i;
					}
				}
			} else if (c == '\"') {
				if (!inSQuote) {
					inDQuote = !inDQuote;
					if (inDQuote) {
						startLink = i + 1;
					} else {
						endLink = i;
					}
				}
			} else if (c == '=') {
				if (!inDQuote && !inSQuote) {
					startLink = i + 1;
					holder = i;
					while(Character.isWhitespace(ltag.charAt(startLink)) ) {
						++startLink;
						++holder;
					}
					foundRef = tagHash.contains(ltag.substring(start, i).trim());
					i = holder;
				}
			}
		}

		if (startLink > endLink) endLink = i;

		if (foundRef) 
		{
			reflist.addElement(tagstmt.substring(startLink, endLink));
		}
    }


	public static void main(String args[])
	{

	}
}

