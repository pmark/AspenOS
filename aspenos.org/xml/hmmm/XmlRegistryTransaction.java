package org.aspenos.xml;

import java.util.*;
import java.io.*;


import org.w3c.dom.*; 
import org.xml.sax.*;
import org.xml.sax.helpers.ParserFactory;
import com.sun.xml.parser.*;
import com.sun.xml.tree.*;

//import org.aspenos.app.aoscontentserver.registry.*;
//import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;


/**
 * The XmlRegistryTransaction translates XML directly into Registry 
 * method calls.
 **/
public class XmlRegistryTransaction {

	private LoggerWrapper _lw;


	//===== constructor ==================================
	/**
	 *
	 **/
	public XmlRegistryTransaction() {
		_lw = null;
	}

	/**
	 *
	 **/
	public XmlRegistryTransaction(String xmlRequest) {
		handleRequest(xmlRequest);
	}

	/**
	 *
	 **/
	public XmlRegistryTransaction(LoggerWrapper lw) {
		_lw = lw;
	}

	/**
	 *
	 **/
	public XmlRegistryTransaction(String xmlRequest, LoggerWrapper lw) {
		_lw = lw;
		handleRequest(xmlRequest);
	}




	//===== primary methods ===============================
	/**
	 * This is the method that actually parses the XML and handles
	 * its data appropriately.
	 * @param xmlRequest String that represents the actual XML request.
	 **/
	public String handleRequest(String xmlRequest) {
		StringBuffer response = new StringBuffer();

		try {
			StringReader sr = new StringReader(xmlRequest.toString());

			//InputSource input = Resolver.createInputSource(f);
/************************* USE THIS *************************
			InputSource input = Resolver.createInputSource(sr);
*************************************************************/
			//InputSource input = new InputSource(sr);

			_lw.logMsg("XmlRegistryTransaction", 
				"created input" + input.getSystemId());

/************************* USE THIS *************************
			XmlDocument xmlDoc = XmlDocument.createXmlDocument(input, false);
*************************************************************/
			_lw.logMsg("XmlRegistryTransaction", "created xmldoc");

			//com.sun.xml.parser.Parser parser = 
			//	(com.sun.xml.parser.Parser)new ValidatingParser(true);
			//parser.setDTDHandler(this);
			//parser.parse(input);

/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
///// PUT XML PARSING STUFF IN HERE /////////////////////////////
/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////


		} catch (Exception e) {
			_lw.logErr("XmlRegistryTransaction", "Caught exception while handling" +
				"request: ", e);
		}

//////
response=new StringBuffer(xmlRequest);
//////
		return response.toString();
	}


	/**
	 * A versatile method that takes a list of objects, each
	 * representing an XML request.  The objects in the Enum
	 * can be of type File or String.
	 **/
	public String handleRequest(Enumeration xmlFiles) {

		StringBuffer response = new StringBuffer();
		String text;
	
		while (xmlFiles.hasMoreElements()) {
			Object o = xmlFiles.nextElement();

			if (o instanceof File) {
				text = getFileText( (File)o );	
			} else if (o instanceof String) {
				text = new StringBuffer( (String)o );
			}

			response.append( handleRequest(text) );	
		}

		return response.toString();
	}


	/**
	 * Same as the others, but this one takes a File
	 * instead of a String or Enum
	 **/
	public String handleRequest(File xmlRequestFile)
	{
		String text = null; 

		try {
			text = getFileText( xmlRequestFile );	
		} catch (IOException e) {

			_lw.logErr("XmlRegistryTransaction", 
					"Caught exception while reading file: ", e);
		}

		return handleRequest(text);
	}


	//===== private methods ==================================
	private String getFileText(File f)	{

		BufferedReader br = new BufferedReader(
			new InputStreamReader(new FileInputStream(f)));

		StringBuffer text = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			text.append(line);
			text.append("\n");
		}
		return text.toString();
	}
	
	//===== main method ======================================
	/**
	 *
	 **/
	public static void main(String args[]) {
		Logger logger = new Logger();
		logger.setMsgLog("/tmp/HLD/XmlRegistryTransaction.out");	
		logger.setErrLog("/tmp/HLD/XmlRegistryTransaction.err");	
		logger.setDebugLog("/tmp/HLD/XmlRegistryTransaction.debug");	

		LoggerWrapper lw = new LoggerWrapper(logger);


		// ============================================
		// Create the XML request
		String fileName;
		Vector files = new Vector();

		for (int i=0; i < args.length; i++) {
			File f = new File(args[i]);
			files.add(f);
		}

		
		// ============================================
		// Handle the XML request by submitting it to
		// the registries.
		XmlRegistryTransaction trans = new XmlRegistryTransaction(lw);
		String response = trans.handleRequest(files);

		System.out.println("\n\nSee XmlRegistryTransaction log files in /tmp/HLD\n\n");

		lw.logMsg("XmlRegistryTransaction.main()", response);
		System.out.println("Response was:\n" + response + "\n\n");
		

	}

	
}


