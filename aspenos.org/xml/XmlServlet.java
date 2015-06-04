package org.aspenos.xml;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*; 
import org.w3c.dom.*;
import org.xml.sax.*; 
import org.apache.xerces.*; 
import org.apache.xerces.dom.*; 
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*; 


/**
 * Given an XML request, this servlet will serialize
 * a Document object back to the requestor.  Implementations
 * of this abstract class must define the doRequest() method,
 * which takes the submitted XML Document and returns another
 * XML Document.
 */
public abstract class XmlServlet extends GenericServlet {     

	/**
	 * Subclasses must implement this method, which handles
	 * the serialized Document and respondes with another
	 * XML Document.
	 * @param req the serialized input document
	 * @return the response as an XML Document
	 */
    protected abstract Document doRequest(Document req);


	/**
	 * 
	 */
    public XmlServlet() {} 


	/**
	 * Parses an XML input stream, calls the doRequest() 
	 * implementation, then serializes the resultant
	 * Document object.
	 * @param req a servlet request
	 * @param res a servlet response
	 */
    public void service(ServletRequest req, ServletResponse res)  
			throws ServletException { 

		try {
			DOMParser parser = new DOMParser();

			parser.parse(new InputSource(
				   req.getInputStream())); 

			Document docOut = doRequest(parser.getDocument()); 

			XMLSerializer ser = 
				new XMLSerializer( res.getOutputStream(), 
					new OutputFormat("xml", "UTF-8", false) );

			ser.serialize(docOut);	    
			res.getOutputStream().close(); 

		} catch (Throwable e) {	    
			e.printStackTrace(); 
		} 
    } 

} 

