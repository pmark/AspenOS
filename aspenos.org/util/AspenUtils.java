package org.aspenos.util;

import java.io.*;
import java.util.*;


public class AspenUtils {

	/**
	 * If the path to the specified FILE does not yet exist,
	 * make all the directories up to the file.
	 *
	 * @param fileName path to and name of a file
	 */
	public static void makeFileDirs(String fileName) {
		int pos = fileName.lastIndexOf(File.separator);
		if (pos != -1) {
			String dir = fileName.substring(0,pos);
			makeDirDirs(dir);
		}
	}


	/**
	 * If the path to the specified DIRECTORY does not yet exist,
	 * make all the directories up to it.  This is the same
	 * as makeFileDirs(), except that you give this one a 
	 * directory name instead of a file name.
	 */
	public static void makeDirDirs(String dir) {
		File f = new File(dir);
		if (!f.exists())
			f.mkdirs();
	}
}




