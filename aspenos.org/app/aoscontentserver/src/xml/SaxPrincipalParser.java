package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


public class SaxPrincipalParser extends GenericSaxParser {

	private PrincipalDefs _prinDefs;
	private PrincipalDef _curPrincipal;
	private PrinRoleDefs _prinroleDefs;
	private PrinRoleDef _curPrinRole;


	public SaxPrincipalParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxPrincipalParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	public PrincipalDefs getPrincipalDefs() {
		return _prinDefs;
	}

	public PrinRoleDefs getPrinRoleDefs() {
		return _prinroleDefs;
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


		if (localName.equals("PrincipalDef")) {
			_curPrincipal = new PrincipalDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curPrincipal.setId(id);

		} else if (localName.equals("RoleDef")) {

			// This one's a little bit different.
			// Since a prinrole record can exist on its own,
			// just create a PrinRoleDefs object.
			_curPrinRole = new PrinRoleDef();

			_curPrinRole.setProperty("principal_id", 
					_curPrincipal.getId());
			_curPrinRole.setProperty("role_id", 
					(String)hash.get("id"));

			if (_prinroleDefs == null)
				_prinroleDefs = new PrinRoleDefs();

			_prinroleDefs.add(_curPrinRole);
		} 

    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("PrincipalDef")) {
			if (_prinDefs == null)
				_prinDefs = new PrincipalDefs();
			_prinDefs.add(_curPrincipal);

		}
    }


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
			System.out.println("Parsing this:\n" + in.toString());
			SaxPrincipalParser prinParser = 
				new SaxPrincipalParser(in.toString());
			/////////////////////////////////////////////////


			String xml = prinParser.getPrincipalDefs().toXML();
			System.out.println("Ready to send Principals to the registry:\n\n"+
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
