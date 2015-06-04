package org.aspenos.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.logging.*;

/**
 * 
 * 
 * @author P. Mark Anderson
 **/
public class JarLoader {

	protected String _jarPath		= "";
	protected LoggerWrapper _lw		= null;
	protected HashMap _cache		= null;
	protected String _cacheDir		= "";
	protected String _cacheExt		= "";
	protected boolean _useDir;
	protected boolean _useExt;


	public JarLoader() {
		_lw = new LoggerWrapper();
		_lw.setDefaultLogs();
		cacheByDirectory();
	}

	public JarLoader(LoggerWrapper lw) {
		_lw = lw;
		cacheByDirectory();
	}


	public JarLoader(String jarPath) {
		_jarPath = jarPath;
		cacheByDirectory();
	}

	public JarLoader(LoggerWrapper lw, String jarPath) {
		_jarPath = jarPath;
		_lw = lw;
		cacheByDirectory();
	}



// CACHING MECHANISM ////////////////////////////////////////////

	public String getCachedFile(String fileName) {
		String file = null;

		try {
			if (_cache != null) {
				file = (String)_cache.get(fileName);
			}
		} catch (Exception e) {
			_lw.logErr("getCachedFile(): ", e);
			_lw.logDebugMsg("getCachedFile(): ", e);
			return null;
		}

		return file;
	}


	public void cacheFiles(String jarPath) {
		setJarPath(jarPath);
		cacheFiles();
	}


	/**
	 * Clears the old cache and caches files from the jar.
	 */
	public void cacheFiles() {
		try {
			_cache = new HashMap();

			if (_jarPath == null || _jarPath.equals("")) {
				_lw.logErr("cacheFiles():  need jar path");
				_lw.logDebugMsg("cacheFiles():  need jar path");
				return;
			}

			String fullFileName = new String(_jarPath);
			ZipInputStream stream = new ZipInputStream(
					new FileInputStream(fullFileName));

			String curLine = new String();
			ZipEntry entry = null;

			while((entry=stream.getNextEntry()) != null) {

				String entryName = entry.getName();

				if(!entryName.endsWith("/")) {

					// certain files will be cached.
					// check if the entry name starts or ends with
					// the specified string.
					if ((_useDir && entryName.startsWith(_cacheDir)) ||
						(_useExt && entryName.endsWith(_cacheExt))) {

						BufferedReader is = new BufferedReader(
								new InputStreamReader(stream));
						StringBuffer theDataFile = new StringBuffer();

						//loop through the file reading in a line at a time
						while((curLine = is.readLine()) != null) 
							theDataFile.append(curLine+"\n");

						//is.close();
						curLine = null;

						// Remove the cache dir, if it was used
						if (_useDir) {
							entryName = 
								entryName.substring(_cacheDir.length());
						}

						cacheFile(entryName, theDataFile.toString());
					}  // end if 
				}  // end if 
			}  // end of jar-searching while

		} catch(Exception e) {
			_lw.logErr("cacheFiles(): ", e);
		}

		return;
	}


	/**
	 * Uses the full fileName (path and extension as stored in
	 * the cache) as the key to the file's contents.
	 */
	public void cacheFile(String fileName, String retval) {

		if (_cache == null)
			_cache = new HashMap();

		_cache.put(fileName, retval);
	}


////////////////////////////////////////////////////////////////


	

	public BufferedReader getReader(String fileName) {

		BufferedReader is = null;

		try {
			//_lw.logDebugMsg("JL","getReader(): Using jar...");

			ZipInputStream zipstream = 
				new ZipInputStream(new FileInputStream( _jarPath ) );
			ZipEntry entry = null;
			String entryName = null;

			// Search the jar for the right file
			while( ( entry=zipstream.getNextEntry() ) != null ) {
				entryName = entry.getName();               
				if( entryName.endsWith( fileName ) ) {
					is = new BufferedReader( 
						new InputStreamReader( zipstream ) );
					break;
				}
			}
		} catch(FileNotFoundException fnf) { 
			_lw.logErr("getReader() FNF!:\n", fnf);
			_lw.logDebugMsg("getReader() FNF!:\n" + fnf);
		} catch(Exception ex) { 
			_lw.logErr("getReader() error:\n", ex);
			_lw.logDebugMsg("getReader() error:\n" + ex);
		}

		return is;
	}

	// ======= ACCESS METHODS ========================================= //
	public String getJarPath() { 
		return _jarPath; 
	}

	public void setJarPath(String path) {
		if (path == null) {
			_lw.logErr("setJarPath(): null path to set");
			_lw.logDebugMsg("setJarPath(): null path to set");
		}
		_jarPath = path; 
	}

	public String getCacheDirectory() { 
		return _cacheDir; 
	}

	public void setCacheDirectory(String dir) {
		_cacheDir = dir; 
		if (_cacheDir == null) {
			_cacheDir = "";
		} else if (!_cacheDir.endsWith("/")) {
			_cacheDir += "/";
		}
	}

	public String getCacheExtension() { 
		return _cacheExt; 
	}

	public void setCacheExtension(String ext) {
		_cacheExt = ext; 
		if (_cacheExt == null) {
			_cacheExt = "";
		} 
	}

	public HashMap getCache() { 
		return _cache; 
	}

	public void setCache(HashMap cache) { 
		_cache = cache; 
	}




	public void cacheByExtension() { 
		_useDir = false; 
		_useExt = true; 
	}

	public void cacheByDirectory() { 
		_useDir = true; 
		_useExt = false; 
	}


}
