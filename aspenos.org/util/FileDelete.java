package org.aspenos.util;

import java.io.*;

/**
 * Deletes a file or recursively deletes a directory.
 */
final public class FileDelete {


   /**
	*
	*/
	public static void deleteFile(String p_file) { 
		deleteFile(new File(p_file));
	}


   /**
	*
	*/
	public static void deleteFile(File p_file) { 
		// If it is a directory, empty it first 
		if(p_file.isDirectory()) {

			String[] dirList = p_file.list(); 

			for(int i=0; i<dirList.length; i++) {

				File aFile = new File(p_file.getPath() +
						File.separator + dirList[i]); 

				if(aFile.isDirectory()) 
					deleteFile(aFile); 

				aFile.delete(); 
			}
		}
		p_file.delete(); 
	}

}
