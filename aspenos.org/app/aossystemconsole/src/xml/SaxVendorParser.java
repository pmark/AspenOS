package org.aspenos.app.aossystemconsole.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aossystemconsole.defs.*;
import org.aspenos.xml.*;


public class SaxVendorParser extends GenericSaxParser {

	private VendorDefs _vendorDefs;
	private VendorDef _curVendor;


	public SaxVendorParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxVendorParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public VendorDefs getVendorDefs() {
		return _vendorDefs;
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


		if (localName.equals("VendorDef")) {
			String resId = (String)hash.get("id");
			_curVendor = new VendorDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curVendor.setId(id);
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("VendorDef")) {
			if (_vendorDefs == null)
				_vendorDefs = new VendorDefs();
			_vendorDefs.add(_curVendor);
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
			SaxVendorParser vendorParser = new SaxVendorParser(in.toString());
			/////////////////////////////////////////////////


			String xml = vendorParser.getVendorDefs().toXML();
			System.out.println("Ready to send Vendors to the registry:\n\n" + xml);

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
