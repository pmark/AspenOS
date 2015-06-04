package org.aspenos.logging;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 */
public class SqlLog extends LogFile {


	public SqlLog(String filename) {
		super(filename);
		_changed = true;
	}

	public List getEntries() {
		if (_changed) {
			_entries = new ArrayList();
			parse();
		}

		return _entries;
	}


	/**
	 * Write a SQL statement to the log file.
	 **/
	public void addEntry(String stmt) {
		_changed = true;
		if (!stmt.endsWith(";"))
			stmt += ";";
		log(stmt);
	}


	/**
	 * 
	 **/
	public void parse() {
		String line;
		String tmp = new String();

		try {
			BufferedReader logIn = new BufferedReader(
				new FileReader(_fileName));
			if (logIn == null) {
				System.err.println("No SQL log is open.");
				return;
			}

			while ((line = logIn.readLine()) != null) {
				if (line.length() < 1)
					continue;

				// If the line does not end with a semicolon
				if (line.charAt(line.length()-1) != ';') {
					tmp += line;
					continue;
				} else {
					if (!line.equals(";"))
						tmp += line.substring(0, line.length()-1);

					_entries.add(tmp);
					tmp = new String();
				}
			}
		} catch (IOException ioe) { 
			System.err.println("[" + DateTool.getDateTime() + "]  " +
				"SqlLog: IOException while parsing SQL log file:\n\t" + ioe); 
		} catch (Exception e) { 
			System.err.println("[" + DateTool.getDateTime() + "]  " +
				"SqlLog: Exception while parsing SQL log file:\n\t" + e); 
		}
	}



	public static void main(String args[]) {
		List v;
		SqlLog sql = 
			new SqlLog("test/sql1.log");

		System.out.println("\n\nInserting...");
		sql.addEntry("INSERT INTO inbox (data_id) values (800);");
		sql.addEntry("INSERT INTO inbox (data_id) values (801)");
		
		System.out.println("\nSQL log file:");
		v = sql.getEntries();
		System.out.println("\n");
		
		if (v == null)
			System.out.println("No entries.");
		else
			for (int i=0; i<v.size(); i++)
				System.out.println((String)v.get(i));

	}
}


