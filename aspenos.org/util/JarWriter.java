package org.aspenos.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.logging.*;

/**
 * Given a List of File objects, this class can
 * write them to a jar (or zip) file.
 * 
 * @author P. Mark Anderson
 **/
public class JarWriter {

	protected LoggerWrapper _lw		= null;
	protected List _fileList	= null;
	protected List _nameList	= null;


	public JarWriter() {
		_lw = new LoggerWrapper();
		_lw.setDefaultLogs();
	}

	public JarWriter(LoggerWrapper lw) {
		_lw = lw;
	}

////////////////////////////////////////////////////////////

	/**
	 * Writes all of the File objects in the entry list
	 * to the jar specified by the 'outFilePath' parameter.
	 */
	public void writeEntries(String outFilePath) throws IOException {
		if (_fileList == null)
			return;

		ZipOutputStream zout = null;
		FileInputStream fis = null;

		try {
			File outFile = new File(outFilePath);
			if (outFile.exists())
				outFile.delete();

			zout = new ZipOutputStream(
					new FileOutputStream(outFile));

			if (_nameList == null)
				_nameList = new ArrayList();
			Iterator namesIt = _nameList.iterator();
			Iterator filesIt = _fileList.iterator();
			while (filesIt.hasNext()) {
				File fileEntry = (File)filesIt.next();

				String fileName;
				if (namesIt.hasNext())
					fileName = (String)namesIt.next();
				else
					fileName = fileEntry.getName();


				ZipEntry zipEntry = new ZipEntry(fileName);

				zout.putNextEntry(zipEntry);
				_lw.logDebugMsg("Putting entry: " + fileName);
				if (!fileEntry.isDirectory()) {
					// Now write the contents of the entry
					// to the jar
					//_lw.logDebugMsg("getting file: " + 
					//		fileEntry.getAbsolutePath());
					fis = new FileInputStream(fileEntry);
					int buffSize = 512;
					byte[] b = new byte[buffSize];
					int len;
					while ((len = fis.read(b)) != -1) {
						//int len = checkBuff(b);
						zout.write(b, 0, len);
						b = new byte[buffSize];
					}

					/*
					fis = new FileInputStream(fileEntry);
					int b;
					while ((b=fis.read()) != -1) {
						zout.write(b);
					}
					*/
				}

				zout.closeEntry();
			}

		} finally {

			if (zout != null) {
				zout.close();
				//zout.finalize();
				zout = null;
			}
			if (fis != null) {
				fis.close();
				//fis.finalize();
				fis = null;
			}
		}
	}


	/**
	 * Set the entry list to a List of File objects
	 * that represent files to pack into a jar.
	 */
	public void setEntries(List l) {
		_fileList = l;
	}


	/**
	 * Sets all files and directories in the given 
	 * directory (or file) to the entry list.  Uses
	 * the given path as the root of the jar entries.
	 */
	public void buildEntryList(String getPath, boolean includeDir) 
			throws IOException {
		buildEntryList(getPath, getPath, includeDir);
	}


	/**
	 * Sets all files and directories in the given 
	 * directory (or file) to the entry list.  Uses
	 * the startPath as the root directory from which 
	 * to start jarring.
	 * @param startPath Used to make a relative entry name
	 *     instead of using the absolute path to a file.
	 * @param getPath Path to a file or directory that is
	 *     to be added to the jar.
	 * @param includeDir If true, the last directory in
	 *     in the startPath is included in each entry.
	 */
	public void buildEntryList(String startPath, String getPath, 
			boolean includeDir) throws IOException {

		File startDir = new File(startPath);
		File getFile = new File(getPath);

		if (!startDir.isDirectory() || !startDir.exists()) {
			// This doesn't make sense.  Why would the startPath
			// be a file and not a directory?
			startPath = getPath;
			startDir = new File(startPath);
		}

		// The getFile must exist
		if (!getFile.exists()) {
			throw new FileNotFoundException(getPath);
		}


		// Build the entry root path 
		String entryRoot = "";
		int pos;

		if (startPath.equals(getPath)) {
			entryRoot = "";
		} else {
			// Take everything to the right of the startPath
			// from the getPath.  Deal with the includeDir
			// option later.
			if (getPath.startsWith(startPath)) {
				if (getFile.isDirectory()) {
					pos = startPath.length();

					if (!startPath.endsWith(File.separator))
						pos++;

					//_lw.logDebugMsg("\"" + getPath +
					//		"\".substring(" + pos + ")");
					entryRoot = getPath.substring(pos);

				} else {

					// Since the get path is a file,
					// take all dirs up to the file.
					pos = getPath.lastIndexOf(File.separator)+1;
					if (pos == -1)
						entryRoot = "";
					else
						entryRoot = getPath.substring(
								startPath.length(), pos);
				}
			} else {
				entryRoot = "";
			}
		}

		// Reset the lists
		_fileList = new ArrayList();
		_nameList = new ArrayList();


		// Handle the includeDir option
		if (includeDir) {
			// Strip off the last directory 
			// of the start path

			// take the slash off

			if (startPath.endsWith(File.separator)) 
				startPath = startPath.substring(0, startPath.length()-1);

			pos = startPath.lastIndexOf(File.separator);

			// put the slash back on
			if (!startPath.endsWith(File.separator)) 
				startPath += File.separator;
			 
			if (pos == -1) pos = 0;
			entryRoot = startPath.substring(pos+1) + entryRoot;
		} 


		// If it is just one file, that's easy.
		// Just add it and the entry name.
		if (!getFile.isDirectory()) {
			_nameList.add(entryRoot + getFile.getName());
			_fileList.add(getFile);
			return;
		}


		// Check for trailing slashes
		if (getFile.isDirectory()) {
			if (!getPath.endsWith(File.separator))
				getPath += File.separator;
		}
		if (startDir.isDirectory()) {
			if (!startPath.endsWith(File.separator))
				startPath += File.separator;
		} 
		if (!entryRoot.equals("") && !entryRoot.endsWith(File.separator))
			entryRoot += File.separator;

		// Add the entry root file, now that the 
		// trailing slash is there
		if (includeDir) {
			_nameList.add(entryRoot);
			_fileList.add(getFile);
		}

		// Now that the entry root is set up, we can 
		// start getting all of the files.
		recurseDirectories(getPath, entryRoot);
	
	}

	private void recurseDirectories(String startDirName, String baseDir) {

		File startDir = new File(startDirName);
		String[] dirList = startDir.list();

		// check for an empty directory
		if (dirList.length == 0)
			return;

		//_lw.logDebugMsg("start dir: " + startDirName);
		//_lw.logDebugMsg("base dir: " + baseDir);

		if (!startDirName.equals("") && !startDirName.endsWith(File.separator))
			startDirName += File.separator;

		if (!baseDir.equals("") && !baseDir.endsWith(File.separator))
			baseDir += File.separator;

		// Get each listed file and dir.  If it's a file,
		// add it.  If it's a dir, add it then recurse.
		String fullName;
		String nextFileName;
		File nextFile;
		for (int i=0; i<dirList.length; i++) {

			nextFileName = dirList[i];
			nextFile = new File(startDirName + nextFileName);
			fullName = baseDir + nextFile.getName();

			// put a slash on the dir
			if (nextFile.isDirectory()) {
				if (!fullName.endsWith(File.separator))
					fullName += File.separator;
			}

			// update the lists
			_nameList.add(fullName);
			_fileList.add(nextFile);

//System.out.println("Added file: " + nextFile.getAbsolutePath());
//System.out.println("Added name: " + fullName);

			// go get this directory's files
			if (nextFile.isDirectory()) {
				recurseDirectories(nextFile.getAbsolutePath(), fullName);
			}
		}
	}

////////////////////////////////////////////////////////////

	public static void main(String args[]) {

		try {
			JarWriter w = new JarWriter();

			//// TESTS ////////////////////////////////
			/*
			ArrayList entryList = new ArrayList();
			File entry;
			entry = new File("myfile.txt");
			entryList.add(entry);
			entry = new File("myfile.orig");
			entryList.add(entry);
			w.setEntries(entryList);
			*/

			//w.buildEntryList("/opt/aspenos/apps/AOSSystemConsole", true);
			//w.buildEntryList("/opt/aspenos/apps/AOSContentServer", false);
			w.buildEntryList("/opt/aspenos/apps/", 
				"/opt/aspenos/apps/AOSContentServer/myfile.blah", true);
			//w.buildEntryList(
			//		"/opt/aspenos/apps/", 
			//		"/opt/aspenos/apps/AOSContentServer/", false);

			w.writeEntries("jarwriter_test.jar");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
