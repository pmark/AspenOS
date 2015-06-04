package org.aspenos.xml;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

//import org.w3c.dom.*;
//import javax.xml.parsers.SAXParserFactory;
//import javax.xml.parsers.SAXParser;


public class SaxHandler implements ContentHandler {

    private PrintWriter	out;

    public static void main(String argv [])
    		throws IOException {

		if(argv.length != 1) {
			System.err.println("Usage: SaxHandler <XML filename>");
			System.exit(1);
		}

		try {

			String uri = new File(argv[0]).getAbsolutePath();
			System.out.println("Starting to parse " + uri);

			// THE PARSER CLASS NEEDS TO GO IN A PROPS FILE
			XMLReader parser = XMLReaderFactory.createXMLReader(
					"org.apache.xerces.parsers.SAXParser");

			// OBVIOUSLY THIS NEEDS TO GO IN THE PROPS FILE TOO
			//String validation = "false";
			//if(validation.equalsIgnoreCase("true"))
			//	parser.setValidating(true);

			SaxHandler sh = new SaxHandler();
			parser.setContentHandler(sh);

			parser.setErrorHandler(new BoringErrorHandler());

			parser.parse(uri);

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



	public SaxHandler() {
		try {
			System.out.println("setting output stream...");
			out = new PrintWriter(new OutputStreamWriter(System.out, "UTF8"),
					true);
			out.println("output stream set to stdout\n");
		} catch(IOException e) {
			System.out.println("I/O error:"  + e);
		}
	}


    // here are all the SAX DocumentHandler methods

    public void startDocument() throws SAXException {
		out.println("<?xml version='1.0' encoding='UTF-8'?>\n");
    }

    public void endDocument() throws SAXException {
		out.println("\n");
		out = null;
    }

    public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs)
    		throws SAXException {
		out.print("<");
		out.print(localName);
		if(attrs != null) {
			for(int i = 0; i < attrs.getLength(); i++) {
			out.print(" ");
			out.print(attrs.getLocalName(i));
			out.print("=\"");
			// XXX this doesn't quote '&', '<', and '"' in the
			// way it should ... needs to scan the value and
			// out.print '&amp;', '&lt;', and '&quot;' respectively
			out.print(attrs.getValue(i));
			out.print("\"");
			}
		}
		out.print(">");
    }

    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {
		out.print("</");
		out.print(localName);
		out.print(">");
    }

    public void characters(char buf [], int offset, int len)
    		throws SAXException {
		// NOTE:  this doesn't escape '&' and '<', but it should
		// do so else the output isn't well formed XML.  to do this
		// right, scan the buffer and write '&amp;' and '&lt' as
		// appropriate.

		String s = new String(buf, offset, len);
		out.print(s);
    }

    public void ignorableWhitespace(char buf [], int offset, int len)
    		throws SAXException {
		// this whitespace ignorable ... so we ignore it!

		// this callback won't be used consistently by all parsers,
		// unless they read the whole DTD.  Validating parsers will
		// use it, and currently most SAX nonvalidating ones will
		// also; but nonvalidating parsers might hardly use it,
		// depending on the DTD structure.
    }

    public void processingInstruction(String target, String data)
    		throws SAXException {
		out.print("<?");
		out.print(target);
		out.print(" ");
		out.print(data);
		out.print("?>");
    }

    public void skippedEntity(String name) throws SAXException {
		out.print("skipped entity\n");
	}

    public void startPrefixMapping(String prefix, String uri) 
			throws SAXException {
		out.print("skipped entity\n");
	}

    public void endPrefixMapping(String prefix)
			throws SAXException {
		out.print("skipped entity\n");
	}

    public void setDocumentLocator(Locator l) {
	// we'd record this if we needed to resolve relative URIs
	// in content or attributes, or wanted to give diagnostics.
    }





    // helpers ... wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void emit(String s) throws SAXException {
		out.print(s);
		//out.flush();
    }

    static class BoringErrorHandler extends HandlerBase {

		// treat validation errors as fatal
		public void error(SAXParseException e)
				throws SAXParseException {
			System.out.println("BEH.error()!");
			throw e;
		}

		// dump warnings too
		public void warning(SAXParseException err)
				throws SAXParseException {
			System.out.println("** Warning" 
			+ ", line " + err.getLineNumber()
			+ ", uri " + err.getSystemId());
			System.out.println("   " + err.getMessage());
		}
    }

}
