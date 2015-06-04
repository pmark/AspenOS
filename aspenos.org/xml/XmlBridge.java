package org.aspenos.xml;

import org.w3c.dom.*; 
import org.apache.xerces.*; 
import org.apache.xerces.dom.*; 
import org.apache.xerces.parsers.*; 
import org.apache.xml.serialize.*; 
import org.xml.sax.*; 
import java.net.*; 
import java.io.*; 


/**
 * Creates an HTTP connection to a specified server
 * and serializes a specified XML Document to it. 
 * The server's response is turned into an XML 
 * Document and returned to the request sender.
 **/
class XmlBridge {   

    private String sURI; 


	/**
	 * Construct the bridge with the server's address. 
	 * Do not prepend with http://, just use the
	 * server's URI.
	 * @param serverURI the URI (no protocol specified) of
	 *     the server to which the request will be sent.
	 **/
    public XmlBridge(String serverURI) { 	
		sURI = serverURI; 
    } 


	/**
	 * Serialize an XML document to the specified server.
	 * @param doc the XML Document
	 * @return the server's response as an XML Document
	 **/
    public Document sendRequest(Document doc) { 

		Document docOut = null; 	

		try { 

			URL url = new URL("http://" + sURI); 
			HttpURLConnection conn = (HttpURLConnection) 
						   url.openConnection(); 
			conn.setDoInput(true); 
			conn.setDoOutput(true); 
			OutputStream out = conn.getOutputStream(); 

			XMLSerializer ser = new XMLSerializer(
					out, new OutputFormat("xml", "UTF-8", false)); 

			ser.serialize(doc); 
			out.close(); 


			DOMParser parser = new DOMParser(); 
			parser.parse(new InputSource(
						conn.getInputStream())); 
			docOut = parser.getDocument(); 

		} catch (Throwable e) { 
			e.printStackTrace(); 
		} 

		return docOut; 
    }     


	/**
	 *
	 **/
    public static void main(String[] args) { 

		try {  
			// Build up an XML Document 
			Document docIn = new DocumentImpl(); 
			Element e = docIn.createElement("Order"); 
			e.appendChild(docIn.createElement("Type")); 
			e.appendChild(docIn.createElement("Amount")); 
			docIn.appendChild(e); 

			// Send it to the Servlet 
			XmlBridge a = new XmlBridge(args[0]); 
			Document docOut = a.sendRequest(docIn); 

			// Debug - write the sent Document to stdout 
			XMLSerializer ser = 
				new XMLSerializer(System.out, null); 

			ser.serialize(docOut); 

		} catch (Throwable e) { 
			e.printStackTrace(); 
		} 
    } 

} 
