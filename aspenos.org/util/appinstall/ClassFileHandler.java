package org.aspenos.util.appinstall;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.aspenos.xml.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.exception.*;

/**
 * 
 * @author P. Mark Anderson
 */

public class ClassFileHandler {

	private HashMap _files;
	private LoggerWrapper _lw;
	private String _aspenClassDir;

	/**
	 * 
	 */
	public ClassFileHandler(HashMap files,
			String aspenClassDir, LoggerWrapper lw) {

		if (!aspenClassDir.endsWith(File.separator))
			aspenClassDir+= File.separator;
		_aspenClassDir = aspenClassDir;

		_lw = lw;
		_files = files;
	}


	/**
	 * Simply copy the each class file in _files to
	 * the _aspenClassDir.
	 */
	public void install() throws IOException {

		Iterator keys = _files.keySet().iterator();
		while (keys.hasNext()) {
			String fileName = (String)keys.next();
			String contents = (String)_files.get(fileName);

			_lw.logMsg("CFH", "- " + fileName);

			// Create the directories up to the file
			String filePath = _aspenClassDir + fileName;

			AspenUtils.makeFileDirs(filePath);

			//////// ONLY FOR CHARACTER FILES: /////////////
			// Write the file out
			//FileWriter writer = new FileWriter(filePath);
			//writer.write(contents);
			//writer.flush();
			//writer.close();

			FileOutputStream out = null;
			ByteArrayInputStream bis = null; 
			try {
				out = new FileOutputStream(filePath);

				// Now write the contents of the string
				bis = new ByteArrayInputStream(contents.getBytes());
				int buffSize = 512;
				byte[] b = new byte[buffSize];
				int len;
				while ((len = bis.read(b)) != -1) {
					//int len = checkBuff(b);
					out.write(b, 0, len);
					b = new byte[buffSize];
				}
			} finally {

				if (out != null) {
					out.close();
					//out.finalize();
					out = null;
				}
				if (bis != null) {
					bis.close();
					//fis.finalize();
					bis = null;
				}
			}
		}
	}

}
