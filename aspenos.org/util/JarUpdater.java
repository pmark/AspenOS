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
public class JarUpdater {

	protected String _jarPath		= "";
	protected LoggerWrapper _lw		= null;

	public JarUpdater() {
		_lw = new LoggerWrapper();
		_lw.setDefaultLogs();
	}

	public JarUpdater(LoggerWrapper lw) {
		_lw = lw;
	}


	public JarUpdater(String jarPath) {
		_jarPath = jarPath;
	}

	public JarUpdater(LoggerWrapper lw, String jarPath) {
		_jarPath = jarPath;
		_lw = lw;
	}



	/**
	 * Replaces an entry with the given string.
	 */
	public void updateEntry(String entryName, String s) 
			throws IOException {

		File tempFile = File.createTempFile("Jar", "UpdaterString");

		// write the string out to the temp file
		BufferedReader in = new BufferedReader(
				new StringReader(s));
		PrintWriter out = new PrintWriter(
				new FileWriter(tempFile));

		String curLine;
		while ((curLine=in.readLine()) != null) {
			out.println(curLine);
		}

		
		in.close();
		out.flush();
		out.close();

		updateEntry(entryName, tempFile);

		tempFile.delete();
	}


	/**
	 * Replaces an entry with the given file.
	 */
	public void updateEntry(String entryName, File newFile) 
			throws IOException {

		FileInputStream fis = null;
		ZipOutputStream zout = null;
		ZipInputStream  stream = null;

		File tempJar = null;

		try {
			if (_jarPath == null || _jarPath.equals("")) {
				_lw.logErr("updateEntry():  need jar path");
				_lw.logDebugMsg("updateEntry():  need jar path");
				return;
			}

			// prepare the input file for reading 
			File origJar = new File(_jarPath);
			stream = new ZipInputStream(
					new FileInputStream(origJar));


			// prepare the temp file for writing a new jar
			tempJar = File.createTempFile("Jar", "Updater");

			zout = new ZipOutputStream(
					new FileOutputStream(tempJar));


			// read and write the jars
			int buffSize = 512;
			byte[] b = new byte[buffSize];
			int len;
			boolean entryUpdated = false;
			ZipEntry zipEntry = null;
			while ((zipEntry=stream.getNextEntry()) != null) {

				String curEntryName = zipEntry.getName();

				zout.putNextEntry(zipEntry);

				if(curEntryName.equals(entryName)) {

					// write the new file here
					// in place of the old one
					fis = new FileInputStream(newFile);
					while ((len = fis.read(b)) != -1) {
						//int len = checkBuff(b);
						zout.write(b, 0, len);
						b = new byte[buffSize];
					}

					entryUpdated = true;

				} else {

					// no match, write the current entry
					while ((len = stream.read(b)) != -1) {
						//int len = checkBuff(b);
						zout.write(b, 0, len);
						b = new byte[buffSize];
					}
				}

				zout.closeEntry();

			}  // end of jar-searching while


			// add the entry as a new entry
			// if it is not replacing an
			// existing one
			if (!entryUpdated) {

				zipEntry = new ZipEntry(entryName);
				zout.putNextEntry(zipEntry);

				fis = new FileInputStream(newFile);
				while ((len = fis.read(b)) != -1) {
					//int len = checkBuff(b);
					zout.write(b, 0, len);
					b = new byte[buffSize];
				}
				zout.closeEntry();
			}


			//////////////////////////////////////////////
			// Now copy the temp jar file to the original
			//////////////////////////////////////////////

			File origJarBackup = File.createTempFile("Jar", "UpdaterBAK");

			// make a backup copy of the original,
			// just in case of failure
			boolean madeBackup = false;
			FileCopy.copy(origJar, origJarBackup);
			madeBackup = true;

			// copy the temp jar file to the original
			boolean overwroteOrig = false;
			FileCopy.copy(tempJar, origJar);
			overwroteOrig = true;

			// erase the temp files
			tempJar.delete();
			origJarBackup.delete();

			// if a backup was made but there was a problem 
			// overwriting the original file, replace the
			// original with the backup.
			if (madeBackup && !overwroteOrig) {
				FileCopy.copy(origJarBackup, origJar);
			}

		} catch(Exception e) {
			_lw.logErr("updateEntry(): problem updating jar", e);
			_lw.logDebugMsg("updateEntry(): problem updating jar", e);

		} finally {

			// clean up
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
			if (stream != null) {
				stream.close();
				//stream.finalize();
				stream = null;
			}
		} // end finally


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


	public static void main(String args[]) {

		try {

			System.out.println("JarUpdater <jar name> " +
					"<entry name> <new file name>");

			File theJar = new File(args[0]);
			JarUpdater ju = new JarUpdater(theJar.getAbsolutePath());

			// do a test with a string
			ju.updateEntry(args[1], "This is text that is\n\n\t" +
					"supposed to replace the contents of " +
					"the original " + args[1]);

			// do a test with a file
			//File newFile = new File(args[2]);
			//ju.updateEntry(args[1], newFile);

			System.out.println("\n\nThe jar has been updated\n\n");

		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}

}


