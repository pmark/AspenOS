package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


public class SaxMenuBtnParser extends GenericSaxParser {

	private MenuButtonDefs _defs;
	private MenuButtonDef _curMenuBtn;


	public SaxMenuBtnParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxMenuBtnParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public MenuButtonDefs getMenuButtonDefs() {
		return _defs;
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


		if (localName.equals("MenuBtnDef")) {
			_curMenuBtn = new MenuButtonDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curMenuBtn.setId(id);
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("MenuBtnDef")) {
			if (_defs == null)
				_defs = new MenuButtonDefs();
			_defs.add(_curMenuBtn);
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
			System.out.println("parsing this:\n" + in.toString());
			SaxMenuBtnParser parser = new SaxMenuBtnParser(in.toString());
			/////////////////////////////////////////////////

			System.out.println("Getting resource defs");

			String xml = parser.getMenuButtonDefs().toXML();
			System.out.println("Ready to send MenuBtns to the registry:\n\n" + xml);

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
