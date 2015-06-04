package org.aspenos.logging;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

public class LogFile {

	protected String _fileName = null;
	protected List _entries = null;
	protected RandomAccessFile _logFile = null;
	protected boolean _changed;


	public LogFile(String fileName) {
		_fileName = fileName;
		AspenUtils.makeFileDirs(_fileName);
	}

	public void log(String s) {
		writeToFile(s);
	}

	public void logOnSameLine(String s) {
		writeToFile(s, false);
	}

	public void log(String s, Exception ex) {
		if (ex != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			writeToFile(s + "\n" + sw.toString());
		} else {
			writeToFile(s);
		}
	}

	public void writeToFile(String s) {
		writeToFile(s, true);
	}

	public synchronized void writeToFile(String s, boolean endline) {
		try {
			StringBuffer sb = new StringBuffer();
			if (endline) {
				sb.append("[");
				sb.append(DateTool.getDateTime());
				sb.append("]  ");
				sb.append(s);
				sb.append("\n");
			} else {
				sb.append(s);
			}

			_logFile = new RandomAccessFile(_fileName, "rw");
			_logFile.seek(_logFile.length());
			_logFile.writeBytes( sb.toString() );
			_changed = true;

			//important
			_logFile.close();
		} catch(IOException e) {
			System.err.println("[" + DateTool.getDateTime() + 
				"]  Exception while logging this '" + s + "':\n\t" + e);
		}
	}


	public void parse() {
	}

	public int getNumEntries() {
		if (_entries != null)
			return _entries.size();

		return 0;
	}

	public List getEntries() {
		return _entries;
	}

///// statics //////////////
	public static String exToString(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.toString();
	}
}




