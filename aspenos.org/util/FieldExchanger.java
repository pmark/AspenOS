/**************************************************************
CLASS: FieldExchanger

DESCRIPTION
	This class takes 3 parameters: file, tag, and string. 
	It reads file, and replaces every occurence of tag 
	with string.

***************************************************************/
package org.aspenos.util;
import java.util.*;
import org.aspenos.logging.*;

/**
 *
 */
public class FieldExchanger {

	private LoggerWrapper _lw;

	//=== CONSTRUCTORS ===
	public FieldExchanger(LoggerWrapper lw) {
		_lw = lw;
	}
	
	public FieldExchanger() {
	}
	
	
	//=== doExchange ===
	public String doExchange(String inString, HashMap tagTable) {

		if (inString == null) 
			return null;
		if (tagTable == null)
			return inString;

		StringBuffer inBuffer = new StringBuffer(inString);
		
		boolean inTag = false;
		boolean inDefval = false;
		char [] token = new char[4];
		StringBuffer tag = null;
		StringBuffer defval = null;
		String tagValue = null;
		int bufferLength = inBuffer.length();
		int numberOfTags = tagTable.size();

		
		StringBuffer outBuffer = new StringBuffer(
				(bufferLength + (numberOfTags * 100)));
		
		try {
			char array[] = new char [bufferLength];
			inBuffer.getChars(0, bufferLength, array, 0);
			int i;	
			for(i=0; i < bufferLength; i++) {
				switch(array[i]) {
					case '[':
						// Make sure we don't throw an array exception
						if (bufferLength - i < 6) {
							outBuffer.append(array[i]);
							continue;
						}

						// read the next 4 chars
						for(int j = 0; j < 4; j++)
							token[j] = array[i+j+1];
							
						if(String.valueOf(token).equals("tag:") ) {
							i+=4;
							inTag = true;
							tag = new StringBuffer(25);

						} else {

							outBuffer.append(array[i]);
							continue;
						}
						break;
					case '\\':
						if(i<bufferLength-1) {
							if (array[i+1] == ':') {
								if (inTag)
									tag.append(array[++i]);
								else if (inDefval)
									defval.append(array[++i]);
								else
									outBuffer.append(array[i]);
							}
							else
								outBuffer.append(array[i]);
						}
						break;
					case ':':
						if(inTag) {
							inTag = false;
							inDefval = true;
							defval = new StringBuffer(25);
						} else {
						   outBuffer.append(array[i]);
						   continue;
						}
						break;
					case ']':
						if(inTag) {
							inTag = false;
							Object o = tagTable.get(tag.toString());
							if (o==null) tagValue=null;
							else tagValue = o.toString();
							outBuffer.append((tagValue==null) ? "" : tagValue);
						} else if (inDefval) {
							inDefval = false;
							tagValue = (String)tagTable.get(tag.toString());

							// If tag not in hash and there is a defval
							if (tagValue == null && defval != null)
								outBuffer.append(defval);
							else
								outBuffer.append(tagValue);
						} else {
						   outBuffer.append(array[i]);
						   continue;
						}
						break;
					default:
						if (inTag)
							tag.append(array[i]);
						else if (inDefval)
							defval.append(array[i]);
						else
							outBuffer.append(array[i]);
				}
			}
		} catch( Exception e ) {
			if (_lw != null) {
				_lw.logErr("FieldExchanger.doExchange: " + e.toString());
				_lw.logDebugMsg("FieldExchanger.doExchange: " + e.toString());
			} else {
				System.err.println("[" + DateTool.getDateTime() + "]  ");
				e.printStackTrace();
			}
		}
		return outBuffer.toString();
	}


	public static void main(String args[]) {
		HashMap tagTable = new HashMap();
		tagTable.put("tit:le", "This is a swapped title!");
		tagTable.put("recip", "THE RECIPIENT");
		tagTable.put("theproduct", "THE SERVICE");

		FieldExchanger exchanger = new FieldExchanger();
		TemplateLoader tl = new TemplateLoader();
		String templ = 
			tl.loadTemplate(
				"/sites/templates/emailoftheday/messages/writecall.com/" +
				"trial_signup.msg", false);
		String page = exchanger.doExchange(templ, tagTable);

		System.out.println("Final page:\n" + page);
	}
}



