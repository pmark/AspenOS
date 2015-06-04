package org.aspenos.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.db.*;
import org.aspenos.logging.*;

/**
 * The TemplateLoader caches templates when it loads them.  
 * Every template (file that is in the "templates/" dir) in a 
 * jar file can be cached, or single files may be read from
 * the jar individually.  
 *
 * Single files may also be read from a specified path without
 * using a jar.
 * 
 * @author P. Mark Anderson
 **/
public class TemplateLoader extends JarLoader {

	public final static String TEMPLATE_EXT	= ".template";
	private String _templatePath	= "";
	private boolean _useJar			= false;


	public TemplateLoader() {
		_lw = new LoggerWrapper();
		_lw.setDefaultLogs();
		setCacheDirectory("templates/");
		cacheByDirectory();
	}

	public TemplateLoader(String jarPath) {
		_jarPath = jarPath;
		setCacheDirectory("templates/");
		cacheByDirectory();
		_useJar = true;
	}

	public TemplateLoader(LoggerWrapper lw) {
		_lw = lw;
		setCacheDirectory("templates/");
		cacheByDirectory();
	}

	public TemplateLoader(LoggerWrapper lw, String jarPath) {
		_lw = lw;
		_jarPath = jarPath;
		setCacheDirectory("templates/");
		cacheByDirectory();
		_useJar = true;
	}



// LOADING ////////////////////////////////////////////////////

	/**
	 * Can be called as:
	 *	loadTemplate("file");   // uses cache
	 *	loadTemplate("file", {false|true});
	 *	loadTemplate("type", "locale", "file");
	 *
	 * @param fileName - name of template minus file extension
	 * @return the template as a String
	 */
	public String loadTemplate(String fileName) {
		return loadTemplate(fileName, true);
	}


	/**
	 * @param fileName - name of template minus file extension
	 * @param useCache - gets from cache and puts in cache if true
	 * @return - the template as a String
	 */
	public String loadTemplate(
			String fileName, boolean useCache) {

		if (!fileName.endsWith(TEMPLATE_EXT))
			fileName += TEMPLATE_EXT;

		if (useCache) {
			String ct = getCachedTemplate(fileName);
			if (ct != null) {
				return ct;
			}
		}

		// Read the file from the jar or disk
		String curLine = new String();
		StringBuffer retval = new StringBuffer();
		BufferedReader is = getReader(fileName);
		if (is == null) return new String("");

		try {
			// loop through the file reading in a line at a time
			while((curLine = is.readLine()) != null) {
				retval.append(curLine);
 				retval.append("\n");	
			}

			// Cache the template
			//_lw.logDebugMsg("TL","Putting '" + fileName + "' into cache");
			cacheFile(fileName, retval.toString());
	
		} catch(Exception ex) { 
			_lw.logErr("TL","loadTemplate " +
				"Exception:\n", ex);
			retval.append("loadTemplate():  " + 
				ex.toString() + "\n");
		}

		return retval.toString();
	}


	/**
	 * @param type - the type of template
	 * @param locale - the template's locale
	 * @param fileName - name of template minus file extension
	 * @return - the template as a String
	 */
	public String loadTemplate(String type, String locale, String fileName) {
		StringBuffer path = new StringBuffer()
			.append(type)
			.append(File.separator)
			.append(locale)
			.append(File.separator)
			.append(fileName);

		return loadTemplate(path.toString(), true);
	}






// CACHING MECHANISM ////////////////////////////////////////////

	public void cacheTemplates(String jarPath) {
		setJarPath(jarPath);
		cacheFiles();
	}

	public void cacheTemplates() {
		cacheFiles();
	}

	public String getCachedTemplate(String fileName) {
		return getCachedFile(fileName);
	}

////////////////////////////////////////////////////////////////


	

	/**
	 *
	 */
	public BufferedReader getReader(String fileName) {
		BufferedReader is = null;

		try {

			if (_useJar) {
				is = super.getReader(fileName);

			} else {
				FileInputStream filestream = 
					new FileInputStream(_templatePath + fileName);
				is = new BufferedReader( new InputStreamReader( filestream ) );
			}

		} catch(FileNotFoundException fnf) { 
			_lw.logErr("TL","getReader() FNF!:\n", fnf);
			_lw.logDebugMsg("TL","getReader() FNF!:\n" + fnf);
		} catch(Exception ex) { 
			_lw.logErr("TL","getReader() error:\n", ex);
			_lw.logDebugMsg("TL","getReader() error:\n" + ex);
		}

		return is;
	}

	// ======= ACCESS METHODS ========================================= //
	public String getJarPath()
	{ return _jarPath; }

	public String getTemplatePath()
	{ return _templatePath; }

	public void setJarPath(String path) {
		if (path == null) {
			_lw.logErr("TL","setJarPath(): null path to set", null);
			return;
		}
		_jarPath = path; 
	}

	public void setTemplatePath(String path) { 
		if (!path.endsWith("/"))
			path += "/";
		_templatePath = path; 
	}

	public void useJar(boolean b)
	{ _useJar = b; }



	public static void main(String args[]) {
		TemplateLoader tl = new TemplateLoader(); 
		tl.useJar(true);
		tl.setJarPath("test/lib/app.jar");

		// Could have done the the same thing with this:
		//TemplateLoader t2 = new TemplateLoader("lib/app.jar");

		System.out.println("Loading templates/temp1.template " +
				"from test/lib/app.jar\n" +
				"== Start Template =============================\n" +
				tl.loadTemplate("templates/temp1.template", false) +
				"== End Template ===============================\n\n");

		tl.useJar(false);
		System.out.println("Loading file test/templates/temp1.template\n" +
				"== Start Template =============================\n" +
				tl.loadTemplate("test/templates/temp1.template", false) +
				"== End Template ===============================\n");

		System.out.println("Getting test/templates/temp1.template from cache\n"+
				"== Start Template =============================\n" +
				tl.getCachedTemplate("test/templates/temp1.template") +
				"== End Template ===============================\n");

		System.out.println("\n\nCaching jar...\n");
		tl.cacheTemplates();   // Use the existing jarPath
		System.out.println("Loading temp2 from cache\n" + 
				"== Start Template =============================\n" +
				tl.getCachedTemplate("templates/temp2.template") +
				"== End Template ===============================\n");
		System.out.println("Loading temp3 from cache\n" + 
				"== Start Template =============================\n" +
				tl.getCachedTemplate("templates/temp3.html") +
				"== End Template ===============================\n");

	}


}
