package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


// The Resource (Event-Resource-Template) Def has all the
// info needs to set up the relationships between
// events, resources and templates.
public class SaxResourceParser extends GenericSaxParser {

	private ResourceDefs _resDefs;
	private ResourceDef _curResource;


	public SaxResourceParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxResourceParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public ResourceDefs getResourceDefs() {
		return _resDefs;
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


		if (localName.equals("ResourceDef")) {
			_curResource = new ResourceDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curResource.setId(id);
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("ResourceDef")) {
			if (_resDefs == null)
				_resDefs = new ResourceDefs();
			_resDefs.add(_curResource);
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
			SaxResourceParser resParser = new SaxResourceParser(in.toString());
			/////////////////////////////////////////////////

			System.out.println("Getting resource defs");

			String xml = resParser.getResourceDefs().toXML();
			System.out.println("Ready to send Resources to the registry:\n\n" + xml);

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
