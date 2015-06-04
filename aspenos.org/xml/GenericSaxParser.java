package org.aspenos.xml;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * Provides an easy way to use SAX to parse an XML file.
 *
 * Works with any SAX parser.  Just set the class name of
 * the specific SAX parser you are using in the System
 * Properties before constructing your parser, like this:
 *
	System.setProperty("sax.parser.class",
			"org.apache.xerces.parsers.SAXParser");
 *
 * This example is using Apache's Xerces parser, but Sun and
 * IBM both make good parsers too.
 *
 * You can also specify whether or not you want your parser to
 * validate the XML.  By default parsers do NOT validate, but
 * to turn validation on, set the property like this:
 *
	System.setProperty("sax.parser.validation", "true");
 *
 * These properties are used to instantiate whichever kind
 * of parser you need for your application. Just extend this
 * class and override any org.xml.sax.ContentHandler methods 
 * you want.  Typically you only need to override startElement()
 * and endElement() to make a functional parser.
 *
 * @author P. Mark Anderson
 */
public abstract class GenericSaxParser extends DefaultHandler {

    protected PrintWriter _out;
	protected XMLReader _parser;
	protected boolean _useSameParser = true;


	/**
	 * Default constructor. 
	 * Lets the app decide when to parse the XML.
	 */
	public GenericSaxParser() {
	}


	/**
	 * Constructs a parser and parses the given File.
	 */
	public GenericSaxParser(File f) throws SAXException, IOException {
		if (f == null)
			return;
		parseFile(f);
	}


	/**
	 * Constructs a parser and parses the given String.
	 */
	public GenericSaxParser(String xml) throws SAXException, IOException {
		if (xml == null)
			return;
		parseString(xml);
	}


	/**
	 * Runs the parser on the given XML String.
	 */
	public void parseString(String xml) throws SAXException, IOException {

		
		if (!_useSameParser || _parser == null)
			_parser = getParser();

		_parser.setContentHandler(this);
		StringReader reader = new StringReader(xml);
		InputSource inputSource = new InputSource(reader);
		_parser.parse(inputSource);
	}

	/**
	 * Runs the parser on the given XML File.
	 */
	public void parseFile(File f) throws SAXException, IOException {

		if (!_useSameParser || _parser == null)
			_parser = getParser();

		_parser.setContentHandler(this);
		String uri = f.getAbsolutePath();
		_parser.parse(uri);
	}




	/**
	 * Creates a new parser using the system properties
	 * sax.parser.class and sax.parser.validation.
	 */
	public XMLReader getParser() throws SAXException {

		String parserClass = (String)
			System.getProperty("sax.parser.class");

		XMLReader parser = 
			XMLReaderFactory.createXMLReader(parserClass);

		// The property can be 'sax.parser.validating' or
		// 'sax.parser.validation', or even 'sax.parser.validate'
		String validation = (String)System.getProperty("sax.parser.validating");
		if(validation == null)
			validation = (String)System.getProperty("sax.parser.validation");
		if(validation == null)
			validation = (String)System.getProperty("sax.parser.validate");
		if(validation == null)
			validation = "false";

		// Turn on or off validation in the parser
		parser.setFeature("http://xml.org/sax/features/validation",
				Boolean.valueOf(validation.toLowerCase()).booleanValue());

		return parser;
	}


	/**
	 * If set to false, makes it so that a new parser 
	 * is created every time a parse method is called
	 * so that parser properties can be changed between
	 * parsings.
	 */
	public void useSameParser(boolean b) {
		_useSameParser = b;
	}


	/**
	 * If subclasses use _out to print data out, 
	 * this method allows an application to set
	 * the value of _out.  For example, to set 
	 * your parser's output to System.out:
	 *
	 *     setOutputStream( System.out );
	 */
	public void setOutputStream(OutputStream out) {
		try {
			_out = new PrintWriter(new OutputStreamWriter(out, "UTF8"),
					true);
		} catch(IOException e) {
			System.out.println("I/O error:"  + e);
		}
	}


}
