package org.aspenos.util.ats;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.util.*;
import org.aspenos.xml.*;


public class SaxTaskSetParser extends GenericSaxParser {

	private TaskSetDefs _taskSetDefs;
	private TaskSetDef _curTaskSet;
	private TaskDefs _taskDefs;
	private TaskDef _curTask;
	private int _curOrdinal;



	public SaxTaskSetParser(File f) throws SAXException, IOException {
		super(f);
	}


	public SaxTaskSetParser(String xml) throws SAXException, IOException {
		super(xml);
	}


	////////////////////////////////////////////////////////////////
	public TaskSetDefs getTaskSetDefs() {
		return _taskSetDefs;
	}






	////////////////////////////////////////////////////////////////
    public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs)
    		throws SAXException {

		HashMap hash = new HashMap();	

		// get the tag's params
		if(attrs != null) {
			for(int i = 0; i < attrs.getLength(); i++) {
				String attrib = attrs.getLocalName(i);
				String value = attrs.getValue(i);
				hash.put(attrib, value);
			}
		}

		// handle the specific tag
		if (localName.equals("TaskSet")) {
			_curTaskSet = new TaskSetDef(hash);

			// Create a new set of tasks for this TaskSet 
			_taskDefs = new TaskDefs();

			// Reset the ordinal counter
			_curOrdinal = 1;

		} else if (localName.equals("Task")) {
			_curTask = new TaskDef(hash);

			// count the ordinal
			_curTask.setProperty("ordinal", 
					Integer.toString(_curOrdinal));
			_curOrdinal++;

			// Reset the parent's defs
			_taskDefs.add(_curTask);
			_curTaskSet.setTaskDefs(_taskDefs);
		} else {
			String tmp = (String)hash.get("run_once");
			if (tmp != null && tmp.equalsIgnoreCase("true")) {
				if (_taskSetDefs == null)
					_taskSetDefs = new TaskSetDefs();
				_taskSetDefs.setRunOnce(true);
			}
		}
    }


    public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {

		if (localName.equals("TaskSet")) {
			if (_taskSetDefs == null)
				_taskSetDefs = new TaskSetDefs();
			_taskSetDefs.add(_curTaskSet);
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
			//SaxTaskSetParser ertParser = new SaxTaskSetParser(new File(args[0]));
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
			SaxTaskSetParser taskSetParser = new SaxTaskSetParser(in.toString());
			/////////////////////////////////////////////////


			String xml = taskSetParser.getTaskSetDefs().toXML();
			System.out.println("Objects in memory:\n\n" + xml);

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
