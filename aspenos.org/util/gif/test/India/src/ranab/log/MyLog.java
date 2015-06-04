package ranab.log;

import java.io.*;
import java.util.*;
import java.text.*;


/**
 * Log class to write log data. It uses <code>RandomAccessFile</code>.
 * The extension ".log" will be appended to the file. If the log file
 * line number exceeds the limit, a new log file will be opened.
 *
 * @author Rana Bhattacharyya.
 */
public
class MyLog {

	/**
	 * Log levels.
	 */
	public static final int LOG_DEBUG  = 0;  // lowest priority
	public static final int LOG_NORMAL = 1;
	public static final int LOG_ERROR  = 2;
	public static final int LOG_FATAL  = 3;  // highest priority
  
	private static final String mstLogHeader[] = { "DEB",
						                           "NOR",
						                           "ERR",
						                           "FAT"
	};

	private DateFormat mDateFmt = null;
    private PrintWriter mOfs = null;
	private File mLogFile = null;
	private int miLogLevel = LOG_NORMAL;
	private int miMaxLine = -1; // no line limit
	private int miLineCount = 0;
	

	/**
	 * Constructor. This class will add .log extension
	 */
	public MyLog(File logFile) {
    
    	if(logFile == null) {
      		throw new NullPointerException("Log file is null.");
    	}
    	
		mDateFmt = new SimpleDateFormat("MMM dd, yy HH:mm");
		mLogFile = logFile;
		miLineCount = countLineNumber();
		open();
	}
	
	/**
	 * set log date format
	 */
	public void setDateFormat(DateFormat fmt)  {
		mDateFmt = fmt;
	}


	/**
	 * Open log file.
	 */
	private synchronized void open() {
    
    try {
      if(mLogFile != null) {
        RandomAccessFile raf = new RandomAccessFile(mLogFile, "rw");
        raf.seek(raf.length());
        FileWriter fw = new FileWriter(raf.getFD()); 
        mOfs = new PrintWriter(new BufferedWriter(fw));
      }
 		} catch(Exception e) {
 			System.err.println("Error in opening log file " + e);
 			mOfs = null;
 		}
	}


	/**
	 * Calculate the log level
	 */
	private int calculateLogLevel(int level) {
		int logLevel = level;

		if(level > LOG_FATAL)
			logLevel = LOG_FATAL;
		else if(level < LOG_DEBUG)
			logLevel = LOG_DEBUG;

		return logLevel;
	}


	/**
	 * Set log level. If <code>level</code> is beypnd the
	 * range, it uses <code>LOG_NORMAL</code> to wtite log data.
	 */
	public void setLogLevel(int level) {
		miLogLevel = calculateLogLevel(level);
	}


	/**
	 * Print log data.
	 */
  public synchronized void write(String str) {
    write(MyLog.LOG_NORMAL ,str);
  }
   
 
  public synchronized void write(int level, String str) {

		level = calculateLogLevel(level);

		// write the string
		if(level >= miLogLevel) {
			String logStr = "[" + mDateFmt.format(new Date()) + " (" + mstLogHeader[level] +")] " + str;

			if(mOfs!=null) {

				// save file if needed
				if(miMaxLine != -1 && miLineCount >= miMaxLine)
					save();

				// now write it
 				try {
 					mOfs.println(logStr);
 					miLineCount++;
 				} catch(Exception e) {
 					mOfs = null;
 					System.err.println("Could not write log data " + e);
 				}
 			}
 			else
 				System.out.println(logStr);
		}
	}


	/**
	 * Set the log file and open a new log file. Returns
	 * the name of the saved log file.
	 */
	public synchronized String save() {

		if(mLogFile == null) {
			return null;
    }
    
		try {
			long dt = new Date().getTime();
			File sf = new File(mLogFile.getAbsolutePath() + '.' + dt);
			close();
			mLogFile.renameTo(sf);
			open();
			miLineCount = 0;
			return sf.getAbsolutePath();
		}
		catch(Exception ex) {
			System.err.println("Could not save " + ex);
		}
		return null;
	}


	/**
	 * Close the log file.
 	 */
 	public synchronized void close() {
 		try {
 			if(mOfs!=null)
 				mOfs.close();
 		}
 		catch(Exception e) {
 			System.err.println("Exception while closing log file " + e);
 		}
 		finally {
 			mOfs = null;
 		}
 	}


	/**
	 * Get max line count
	 */
	public int getMaxLineCount() {
		return miMaxLine;
	}


	/**
	 * Set max line count
	 */
	public void setMaxLineCount(int i) {

		// no limit
		if(i <= 0)
			miMaxLine = -1;

		miMaxLine = i;
	}


	/**
	 * Count the number of lines
	 */
	private synchronized int countLineNumber() {

		try {

			// file does not exist
			if(!mLogFile.exists())
				return 0;

			// read the whole file
			LineNumberReader lnr = new LineNumberReader( new BufferedReader(new FileReader(mLogFile)) );
			while(lnr.read() != -1){}

			int line = lnr.getLineNumber();
			lnr.close();
			return line;

		} catch(Exception ex) {
			System.err.println("Cannot get line count " + ex);
		}
		return 0;
	}


	/**
	 * Get the current line number
	 */
	public int getLineCount() {
		return miLineCount;
	}

}