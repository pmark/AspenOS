package org.aspenos.util;

import java.io.*;

/**
 * This class can be used for copying files and/or directories in Java.
 * I wrote it because, for some reason, there is no java.io.File.copy()
 * method. It can be used to copy file->file, file->directory, or
 * directory->directory. The copy method is static, and thus this class
 * should not be instantiated.<p>
 *
 * @version 1.1, January 1, 1996
 * @author  Jamie Hall
 */

final public class FileCopy {


   /**
	* This class shouldn't be instantiated.
	*/
	private FileCopy() {
	}


   /**
	* Copy files and/or directories.
	*
	* @param src source file or directory
	* @param dest destination file or directory
   	* @exception IOException if operation fails
	*/
	public static void copy(String src, String dest) throws IOException {
		copy(new File(src), new File(dest));
	}

   /**
	* Copy files and/or directories.
	*
	* @param src source file or directory
	* @param dest destination file or directory
   	* @exception IOException if operation fails
	*/
	public static void copy(File src, File dest) throws IOException {

		FileInputStream source = null;
		FileOutputStream destination = null;
		byte[] buffer;
		int bytes_read;

		// Make sure the specified source exists and is readable.
		if (!src.exists())
			throw new IOException("source not found: " + src);
		if (!src.canRead())
			throw new IOException("source is unreadable: " + src);

		if (src.isFile()) {
			if (!dest.exists()) {
                File parentdir = parent(dest);
                if (!parentdir.exists())
					parentdir.mkdir();
			}
			else if (dest.isDirectory()) {
				dest = new File(dest + File.separator + src);
			}
		}
		else if (src.isDirectory()) {
			if (dest.isFile())
				throw new IOException("cannot copy directory " + src + " to file " + dest);

			if (!dest.exists())
				dest.mkdir();
		}
		
		// The following line requires that the file already
		// exists!!  Thanks to Scott Downey (downey@telestream.com)
		// for pointing this out.  Someday, maybe I'll find out
		// why java.io.File.canWrite() behaves like this.  Is it
		// intentional for some odd reason?
		//if (!dest.canWrite())
			//throw new IOException("destination is unwriteable: " + dest);

		// If we've gotten this far everything is OK and we can copy.
		if (src.isFile()) {
			try {
	            source = new FileInputStream(src);
		        destination = new FileOutputStream(dest);
			    buffer = new byte[1024];
				while(true) {
	                bytes_read = source.read(buffer);
		            if (bytes_read == -1) break;
			        destination.write(buffer, 0, bytes_read);
				}
			}
	        finally {
		        if (source != null) 
			        try { source.close(); } catch (IOException e) { ; }
				if (destination != null) 
	                try { destination.close(); } catch (IOException e) { ; }
		    }
		}
		else if (src.isDirectory()) {
			String targetfile, target, targetdest;
			String[] files = src.list();

			for (int i = 0; i < files.length; i++) {
				targetfile = files[i];
				target = src + File.separator + targetfile;
				targetdest = dest + File.separator + targetfile;


				if ((new File(target)).isDirectory()) {
		 			copy(new File(target), new File(targetdest));
				}
				else {

					try {
						source = new FileInputStream(target);
						destination = new FileOutputStream(targetdest);
						buffer = new byte[1024];
					 
						while(true) {
							bytes_read = source.read(buffer);
							if (bytes_read == -1) break;
							destination.write(buffer, 0, bytes_read);
						}
					}
					finally {
						if (source != null) 
							try { source.close(); } catch (IOException e) { ; }
						if (destination != null) 
							try { destination.close(); } catch (IOException e) { ; }
					}
				}
			}
		}
	}


   /**
	* File.getParent() can return null when the file is specified without
	* a directory or is in the root directory. This method handles those cases.
	*
	* @param f the target File to analyze
	* @return the parent directory as a File
	*/
	private static File parent(File f) {
		String dirname = f.getParent();
		if (dirname == null) {
			if (f.isAbsolute()) return new File(File.separator);
			else return new File(System.getProperty("user.dir"));
		}
		return new File(dirname);
	}

}
