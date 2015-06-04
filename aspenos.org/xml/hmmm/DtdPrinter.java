package org.aspenos.xml;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.ParserFactory;
import com.sun.xml.parser.*;

import java.util.*;

/**
 *
 */
public class DtdPrinter implements DtdEventListener {
    
    
    private Hashtable elements = new Hashtable();


	/**
	 *
	 */
    public DtdPrinter(String file) throws IOException {
	
		InputSource     input;
	
        try {

			// Turn the filename into an XML input source
            input = Resolver.createInputSource (new File (file));

            // Get an instance of the parser.
            com.sun.xml.parser.Parser parser;

	    	// Choose between the non/validating parser...
            // parser = (com.sun.xml.parser.Parser)ParserFactory.makeParser ();
	        // Or the validating parser
			parser = (com.sun.xml.parser.ValidatingParser)
				new com.sun.xml.parser.ValidatingParser(true);
			parser.setDTDHandler(this);

            // Parse the input
            parser.parse (input);

		} catch(SAXParseException e) {
			e.printStackTrace();
				System.out.println("column: " + e.getColumnNumber());
				System.out.println("line: " + e.getLineNumber());
				System.out.println("sys: " + e.getSystemId());
		} catch (Exception ex) {
				ex.printStackTrace();
		}
    }

//////////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
    public void attributeDecl(
			java.lang.String elementName, 
			java.lang.String attributeName,
			java.lang.String attributeType, 
			java.lang.String[] options,
			java.lang.String defaultValue, 
			boolean isFixed, 
			boolean isRequired) {

		System.out.println("attibuteDecl:"+elementName+" "+attributeName+" "+attributeType+
			   " "+defaultValue+" "+isFixed+" "+isRequired);

		if(elements.containsKey(elementName)) {
			((Element)elements.get(elementName)).setAttribute(
					attributeName,
					attributeType,
					options,
					defaultValue,
					isFixed,
					isRequired);
		} else {
			System.out.println("Attribute declared without element!");
		}
    }


	/**
	 *
	 */
    public void elementDecl(
			java.lang.String elementName, 
			java.lang.String contentModel) {
		System.out.println("elementDecl:"+elementName+" model="+contentModel);
		elements.put(elementName, new Element(elementName ,contentModel));
    }


	/**
	 *
	 */
    public void endDtd() {
		System.out.println("endDTD()");
    }


	/**
	 *
	 */
    public void externalDtdDecl(
			java.lang.String publicId,
			java.lang.String systemId) {
		System.out.println("externalDtdDecl:"+publicId+" "+systemId);
    }


	/**
	 *
	 */
    public void externalEntityDecl(
			java.lang.String name, 
			java.lang.String publicId, 
			java.lang.String systemId) {
		System.out.println("externalDtdDecl:"+name+" "+publicId+" "+systemId);
    }


	/**
	 *
	 */
    public void internalDtdDecl(java.lang.String internalSubset) {
		System.out.println("internalDtdDecl:"+internalSubset);
    }


	/**
	 *
	 */
    public void internalEntityDecl(java.lang.String name, java.lang.String value) {
		System.out.println("internalEntityDecl:"+name+" "+value);
    }


	/**
	 *
	 */
    public void startDtd(String root) {
		System.out.println("startDTD: "+root);
    }


	/**
	 *
	 */
    public void  notationDecl(String name,
			      String publicId,
			      String systemId)
				  throws SAXException {
		System.out.println("notationDecl("+name+","+publicId+","+systemId+")");
    }

    
	/**
	 *
	 */
    public void unparsedEntityDecl(String name, 
				   String publicId,
				   String systemId, 
				   String notationName) 
				   throws SAXException {
		System.out.println("unparsedEntityDecl("+name+","+publicId+","+systemId+
			       ","+notationName+")");
    }
    

	/**
	 *
	 */
    public Enumeration getElements() {
		return elements.elements();
    }

    
	/**
	 *
	 */
    public static void main (String argv []) {

        try {

			if (argv.length != 1) {
				System.err.println ("Usage: cmd filename");
				System.exit (1);
			}

			DtdPrinter dp = new DtdPrinter(argv[0]);

			for(Enumeration e=dp.getElements(); e.hasMoreElements();) 
				System.out.println( ((Element)e.nextElement()).toString());

		} catch (Throwable t) {
			t.printStackTrace ();
		}

		System.exit (0);
    }
    

}




