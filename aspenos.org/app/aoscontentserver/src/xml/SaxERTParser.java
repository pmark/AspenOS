package org.aspenos.app.aoscontentserver.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.xml.*;


// The ERT (Event-Resource-Template) Def has all the
// info needs to set up the relationships between
// events, resources and templates.
public class SaxERTParser extends GenericSaxParser {

	private String _curRegistryGroup;

	private WebEventDefs _wedefs;
	private WebEventDef _curEvent;
	private ERTDefs _ertDefs;
	private ERTDef _curERT;
	private ResourceDefs _rdefs;
	private ResourceDef _curResource;
	private TemplateDefs _tdefs;
	private TemplateDef _curTemplate;
	private RoleDefs _roledefs;
	private RoleDef _curRole;
	private int _curOrdinal;


	public SaxERTParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxERTParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public ERTDefs getERTDefs() {
		return _ertDefs;
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

		if (localName.equals("ERTDef")) {
			_curRegistryGroup = (String)hash.get("registry_group");
			_curERT = new ERTDef(hash);

			// Create a new webevent defs for this ert's webevents
			_wedefs = new WebEventDefs();

		} else if (localName.equals("WebEventDef")) {
			_curEvent = new WebEventDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curEvent.setId(id);
			_wedefs.add(_curEvent);

			// Reset the ordinal counter
			_curOrdinal = 1;

			// Reset the parent's defs
			_curERT.setProperty("webevent_defs", _wedefs);

			// Create a new resource defs for this event's resources
			_rdefs = new ResourceDefs();

		} else if (localName.equals("ResourceDef")) {
			_curResource = new ResourceDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curResource.setId(id);

			// count the ordinal
			_curResource.setProperty("ordinal", 
					Integer.toString(_curOrdinal));
			_curOrdinal++;


			_rdefs.add(_curResource);

			// Reset the parent's defs
			_curEvent.setProperty("resource_defs", _rdefs);


			// Create a new template defs for this resource's templates
			_tdefs = new TemplateDefs();

		} else if (localName.equals("TemplateDef")) {
			_curTemplate = new TemplateDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curTemplate.setId(id);
			_tdefs.add(_curTemplate);

			// Reset the parent's defs
			_curResource.setProperty("template_defs", _tdefs);

			// Create a new role defs for this template's roles
			_roledefs = new RoleDefs();

		} else if (localName.equals("RoleDef")) {
			_curRole = new RoleDef(hash);
			id = (String)hash.get("id");
			if (id != null)
				_curRole.setId(id);
			_roledefs.add(_curRole);

			// Reset the parent's defs
			_curTemplate.setProperty("role_defs", _roledefs);

		}

    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("ERTDef")) {
			if (_ertDefs == null)
				_ertDefs = new ERTDefs();
			_ertDefs.add(_curERT);
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

			// Run it with a File ///////////////////////////
			//SaxERTParser ertParser = new SaxERTParser(new File(args[0]));
			/////////////////////////////////////////////////

			// Run it with a plain xml String ///////////////
			BufferedReader is = new BufferedReader(
					new FileReader(args[0]));
			StringBuffer in = new StringBuffer();
			String curLine;
			while((curLine=is.readLine()) != null) 
				in.append(curLine+"\n");
			is.close();
			System.out.println("parsing this: " + in.toString());
			SaxERTParser ertParser = new SaxERTParser(in.toString());
			/////////////////////////////////////////////////


			String xml = ertParser.getERTDefs().toXML();
			System.out.println("Ready to send ERTs to the registry:\n\n" + xml);

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
