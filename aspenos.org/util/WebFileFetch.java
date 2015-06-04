package org.aspenos.util;

import java.io.*; 
import java.util.*; 
import javax.servlet.http.*;
import javax.servlet.*; 

//import org.aspenos.app.aoscontentserver.util.ServerInit;

public class WebFileFetch extends WebFetch {

	private String _saveDir;
	private String _username;
	private String _password;
	private boolean _doAuthentication;

	/**
	 * Gets and saves a remote file to the set save directory and
	 * uses the file's original name.
	 */
 	public File saveRemoteFile(String url, Map params, String method)
			throws Exception  {

		InputStream is;
		
		if (method == null)
			method = "get";

		if (method.equalsIgnoreCase("post")) {

			if (_doAuthentication)
				is = doAuthPostStream(_username, _password, url, params);
			else
				is = doPostStream(url, params);

		} else {

			if (_doAuthentication)
				is = doAuthGetStream(_username, _password, url, params);
			else
				is = doGetStream(url, params);
		}

		String fileName;
		int pos = url.lastIndexOf("/");
		if (pos == -1)
			fileName = url;
		else
			fileName = url.substring(pos+1);

		return readIntoFile(is, fileName);
	}



	/**
	 *
	 */
 	public File getAsTempFile(String url, Map params, String method, 
			String prefix, String suffix, File tmpDir) 
			throws Exception  {

		InputStream is;
		
		if (method == null)
			method = "get";

		if (method.equalsIgnoreCase("post")) {

			if (_doAuthentication)
				is = doAuthPostStream(_username, _password, url, params);
			else
				is = doPostStream(url, params);

		} else {

			if (_doAuthentication)
				is = doAuthGetStream(_username, _password, url, params);
			else
				is = doGetStream(url, params);
		}

		return readIntoTempFile(is, prefix, suffix, tmpDir);
	}



	/**
	 *
	 */
	public Properties loadProperties(String url, Map params, String method)
			throws Exception  {

		InputStream is;
		
		if (method == null)
			method = "get";

		if (method.equalsIgnoreCase("post")) {

			if (_doAuthentication)
				is = doAuthPostStream(_username, _password, url, params);
			else
				is = doPostStream(url, params);

		} else {
			if (_doAuthentication) {
				is = doAuthGetStream(_username, _password, url, params);
			} else {
				is = doGetStream(url, params);
			}
		}

		Properties props = new Properties();
		props.load(is);

		return props;
	}


	/******************************************************/
	/******************************************************/


	/**
	 *
	 */
	public void setDoAuthentication(boolean b) {
		_doAuthentication = b;
	}

	/**
	 *
	 */
	public boolean getDoAuthentication() {
		return _doAuthentication;
	}

	/**
	 *
	 */
	public void setAuthInfo(String username, String password) {
		_username = username;
		_password = password;
	}


	/**
	 *
	 */
	public void setSaveDir(String saveDir) {
		if (!saveDir.endsWith(File.separator))
			saveDir += File.separator;
		File f = new File(saveDir);
		if (!f.exists())
			f.mkdirs();
		_saveDir = saveDir;
	}


	/**
	 *
	 */
	public String getSaveDir() {
		return _saveDir;
	}


	/**
	 *
	 */
	public File getExistingFile(String filePath) {

		String fileName;
		int pos = filePath.lastIndexOf("/");
		if (pos == -1)
			fileName = filePath;
		else
			fileName = filePath.substring(pos+1);

		File f = null;
		try {
			f = new File(_saveDir + fileName);
			if (!f.exists())
				f = null;
		} catch (Exception ex) {
			// ok to absorb exception
		}

		return f;
	}

	/******************************************************/
	/******************************************************/


	/**
	 * Reads a stream into the given file path.  If a file 
	 * already exists at that path, it is deleted first.
	 */
	private File readIntoFile(InputStream is, String fileName)
				throws Exception {

		File f = new File(_saveDir + fileName);
		if (f.exists()) 
			f.delete();
		f.createNewFile();

		writeToFile(is, f);

		return f;
	}


	/**
	 *
	 */
	private File readIntoTempFile(InputStream is, String prefix,
				String suffix, File tmpDir) 
				throws Exception {

		if (tmpDir == null)
			tmpDir = new File(_saveDir);

		File tmpFile = File.createTempFile(prefix, suffix, tmpDir);
		tmpFile.deleteOnExit();

		writeToFile(is, tmpFile);


		return tmpFile;
	}


	private void writeToFile(InputStream is, File f)
				throws IOException {

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f); 

			/*
			byte[] buff = new byte[64];
			while ((is.read(buff)) > 0) {
				fos.write(buff);
			}
			*/
			int b;
			while ((b=is.read()) != -1) {
				fos.write(b);
			}
			fos.flush();
		} finally {
			if (fos != null)
				fos.close();
			fos = null;
			is.close();
			is = null;
		}

	}

}
