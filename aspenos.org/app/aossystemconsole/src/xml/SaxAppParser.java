package org.aspenos.app.aossystemconsole.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.xml.*;


public class SaxAppParser extends GenericSaxParser {

	private AppDefs _appDefs;
	private AppDef _curApp;


	public SaxAppParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxAppParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public AppDefs getAppDefs() {
		return _appDefs;
	}






	////////////////////////////////////////////////////////////////
    public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs)
    		throws SAXException {

		HashMap hash = new HashMap();	
		String id;

		if(attrs != null) {
			for(int i = 0; i < attrs.getLength(); i++) {
				String attrib = attrs.getLocalName(i);
				String value = attrs.getValue(i);
				hash.put(attrib, value);
			}
		}


		if (localName.equals("AppDef")) {
			String resId = (String)hash.get("id");
			_curApp = new AppDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curApp.setId(id);
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("AppDef")) {
			if (_appDefs == null)
				_appDefs = new AppDefs();
			_appDefs.add(_curApp);
		}
    }





	////////////////////////////////////////////////////////////////
    public static void main(String args [])
    		throws IOException {

		if(args.length != 1) {
			System.err.println("Usage:  <XML filename>");
			System.exit(1);
		}

		try {

			// specify the XML parser 
			System.setProperty("sax.parser.class",
					"org.apache.xerces.parsers.SAXParser");

			// Run it with a plain xml String ///////////////
			BufferedReader is = new BufferedReader(
					new FileReader(args[0]));
			StringBuffer in = new StringBuffer();
			String curLine;
			while((curLine=is.readLine()) != null) 
				in.append(curLine+"\n");
			is.close();
			System.out.println("parsing this:\n" + in.toString());
			SaxAppParser appParser = new SaxAppParser(in.toString());
			/////////////////////////////////////////////////


			String xml = appParser.getAppDefs().toXML();
			System.out.println("Ready to send Apps to the registry:\n\n" + xml);

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
