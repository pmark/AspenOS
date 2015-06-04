package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


public class SaxWebEventParser extends GenericSaxParser {

	private WebEventDefs _weDefs;
	private WebEventDef _curWebEvent;


	public SaxWebEventParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxWebEventParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	public WebEventDefs getWebEventDefs() {
		return _weDefs;
	}




	////////////////////////////////////////////////////////////////
    public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs)
    		throws SAXException {

		HashMap hash = new HashMap();	
		String id;

		// Hash all of the attributes for this element
		if(attrs != null) {
			for(int i = 0; i < attrs.getLength(); i++) {
				String attrib = attrs.getLocalName(i);
				String value = attrs.getValue(i);
				hash.put(attrib, value);
			}
		}


		if (localName.equals("WebEventDef")) {
			_curWebEvent = new WebEventDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curWebEvent.setId(id);
		} 

    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("WebEventDef")) {
			if (_weDefs == null)
				_weDefs = new WebEventDefs();
			_weDefs.add(_curWebEvent);
		}
    }


    public static void main(String args [])
    		throws IOException {

		if(args.length != 1) {
			System.err.println("Usage:  <XML filename>");
			System.exit(1);
		}

		try {

			// THIS IS CRUCIAL!!
			// You must set the XML parser class here.
			System.setProperty("sax.parser.class",
					"org.apache.xerces.parsers.SAXParser");
			System.setProperty("sax.parser.validating",
					"false");

			// Run it with a plain xml String ///////////////
			BufferedReader is = new BufferedReader(
					new FileReader(args[0]));
			StringBuffer in = new StringBuffer();
			String curLine;
			while((curLine=is.readLine()) != null) 
				in.append(curLine+"\n");
			is.close();
			System.out.println("Parsing this:\n" + in.toString());
			SaxWebEventParser parser = 
				new SaxWebEventParser(in.toString());
			/////////////////////////////////////////////////


			String xml = parser.getWebEventDefs().toXML();
			System.out.println("Ready to send WebEvents to the registry:\n\n"+
					xml);

		} catch(SAXParseException err) {
			System.out.println("** Parsing error" 
			+ ", line " + err.getLineNumber()
			+ ", uri " + err.getSystemId());
			System.out.println("   " + err.getMessage());
			
		} catch(SAXException e) {
			Exception	x = e;
			if(e.getException() != null)
			x = e.getException();
			x.printStackTrace();

		} catch(Throwable t) {
			t.printStackTrace();
		}

		System.exit(0);
    }



}
