package org.aspenos.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.text.*;

public class PropLoader {

	private Properties _props = new Properties();
	private String _prefix = "";
	
	/**
	 * Loads all properties from the property files 
	 * in the specified jar.
	 * Examples:
	 *		loadFromJar("lib/app.jar") +
	 **/
	public Properties loadFromJar(String jarPath) 
			throws IOException {
		_props = new Properties();

		ZipInputStream stream = new ZipInputStream( 
				new FileInputStream(jarPath) );
		ZipEntry entry = null;

		while(( entry=stream.getNextEntry()) != null) {

			if(entry.getName().endsWith(".properties")) {
				_props.load(stream);
			}
		}         

		return _props;
	}


	/**
	 * Loads properties from one file (not in a jar).
	 * Example:
	 *		load("p1.properties")
	 *
	 **/
	public Properties load(String filePath) 
			throws IOException {
		return load(filePath, null);
	}


	/**
	 * Loads properties from one file, whether or not it's in a jar.
	 * Examples:
	 *		load("p1.properties", "lib/app.jar") +
	 *		load("props/p1.properties", null) +
	 *
	 **/
	public Properties load(String filePath, String jarPath) 
			throws IOException {
		_props = new Properties();
		StringBuffer retval = new StringBuffer();
		String curLine = new String();
		BufferedReader is = null;

		if (jarPath != null) {
			String jar = new String( jarPath );
			ZipInputStream zipstream = new ZipInputStream(
					new FileInputStream( jar ) );
			ZipEntry entry = null;
			String entryName = null;

			// Search the jar for the right prop file
			while( ( entry=zipstream.getNextEntry() ) != null ) {
				entryName = entry.getName();               
				if( entryName.endsWith( filePath ) ) {
					_props.load(zipstream);
					break;
				}
			}
		} else {
			FileInputStream filestream = new FileInputStream(filePath);
			_props.load(filestream);
		}

		return _props;
	}



///// ACCESSOR METHODS //////////////////////////////////
	public Properties getProperties()
	{ return _props; }

	public void setProperties(Properties props)
	{ _props = props; }

	public void setPrefix(String p)
	{ _prefix = p; }

	public String getPrefix()
	{ return _prefix; }


	public String getProperty(String key) {
		if (_prefix != null && !_prefix.equals(""))
			key = _prefix + key;

		return _props.getProperty(key);
	}

	/**
	 * Same as getProperty().
	 */
	public String getString(String key) {
		return getProperty(key);
	}

	public String getString(String key, String def) {
		if (_prefix != null && !_prefix.equals(""))
			key = _prefix + key;

		return _props.getProperty(key, def);
	}

	public int getInt(String key, int def) {
		try {
			return Integer.parseInt(getString(key, "bad"));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public long getLong(String key, long def) {
		try {
			return Long.parseLong(getString(key, "bad"));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public boolean getBoolean(String key, boolean def) {
		return Boolean.valueOf(
			getString(key, def ? "true" : "false")).booleanValue();
	}

	public String[] getStringArray(String key, String sep, String def) {
		String val = getString(key, def);
		StringTokenizer st = new StringTokenizer(val, sep);
		int items = st.countTokens();
		String rv[] = new String[items];
		for (int i = 0; i < items; i++) {
			rv[i] = st.nextToken();
		}
		return rv;
	}

	public int[] getIntArray(String key, String sep, String def) {
		String val = getString(key, def);
		StringTokenizer st = new StringTokenizer(val, sep);
		int items = st.countTokens();
		int rv[] = new int[items];
		for (int i = 0; i < items; i++) {
			try {
				rv[i] = Integer.parseInt(st.nextToken());
			} catch (NumberFormatException e) {
				rv[i] = 0;
			}
		}
		return rv;
	}



///// MAIN METHOD /////////////////////////////////////////
	public static void main(String args[]) {
		try {
			PropLoader pl = new PropLoader();

			System.out.println("Loading p1.properties props from lib/app.jar\n" +
					"== Start Prop =============================\n" +
					pl.load("p1.properties", "lib/app.jar") +
					"\n== End Prop ===============================\n\n");

			System.out.println("Loading props/temp1.properties\n" +
					"== Start Prop =============================\n" +
					pl.load("props/p1.properties", null) +
					"\n== End Prop ===============================\n\n");

			System.out.println("Loading all props from lib/app.jar\n" +
					"== Start Prop =============================\n" +
					pl.loadFromJar("lib/app.jar") +
					"\n== End Prop ===============================\n");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


}
