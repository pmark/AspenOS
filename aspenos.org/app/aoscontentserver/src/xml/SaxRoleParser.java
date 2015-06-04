package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


// The Role (Event-Role-Template) Def has all the
// info needs to set up the relationships between
// events, resources and templates.
public class SaxRoleParser extends GenericSaxParser {

	private RoleDefs _roleDefs;
	private RoleDef _curRole;


	public SaxRoleParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxRoleParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public RoleDefs getRoleDefs() {
		return _roleDefs;
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


		if (localName.equals("RoleDef")) {
			String resId = (String)hash.get("id");
			_curRole = new RoleDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curRole.setId(id);
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("RoleDef")) {
			if (_roleDefs == null)
				_roleDefs = new RoleDefs();
			_roleDefs.add(_curRole);
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

			// Run it with a plain xml String ///////////////
			BufferedReader is = new BufferedReader(
					new FileReader(args[0]));
			StringBuffer in = new StringBuffer();
			String curLine;
			while((curLine=is.readLine()) != null) 
				in.append(curLine+"\n");
			is.close();
			System.out.println("parsing this:\n" + in.toString());
			SaxRoleParser roleParser = new SaxRoleParser(in.toString());
			/////////////////////////////////////////////////


			String xml = roleParser.getRoleDefs().toXML();
			System.out.println("Ready to send Roles to the registry:\n\n" + xml);

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
