package org.aspenos.logging;


/**
 *
 *
 * Typical use:

 	// Log debug messages in the standard message file:
	setMsgLog("path/log.msg");
	setDebugLog( getMsgLog() );
	setDoDebug( true );
 *
 *
 */
public class Logger
{
	public final static String DEFAULT_LOG_LOC = 
		"default.log";

	private LogFile _msgLog=null;
	private LogFile _errLog=null;
	private LogFile _debugLog=null;
	private LogFile _defaultLog=null;
	private boolean _doDebug=true;



	//===== Logger Constructor ===========================================
	public Logger()
	{
		_defaultLog = new LogFile(DEFAULT_LOG_LOC);
	}

	

	//===== Log Set Methods ==============================================
  // Message
	public void setMsgLog(String path)
	{ _msgLog = new LogFile(path); }

	public void setMsgLog(LogFile lf)
	{ _msgLog = lf; }

  // Error
	public void setErrLog(String path)
	{ _errLog = new LogFile(path); }

	public void setErrLog(LogFile lf)
	{ _errLog = lf; }

  // Debug
	public void setDebugLog(String path)
	{ _debugLog = new LogFile(path); }

	public void setDebugLog(LogFile lf)
	{ _debugLog = lf; }
	
	public void setDoDebug(boolean b)
	{ _doDebug = b; }



	//===== Log Get Methods ==============================================
	public LogFile getMsgLog()
	{ return _msgLog; }

	public LogFile getErrLog()
	{ return _errLog; }

	public LogFile getDebugLog()
	{ return _debugLog; }
	
	public boolean getDoDebug()
	{ return _doDebug; }



	//===== Public Log Write Methods ======================================
  // Message
	public void logMsg(String msg)
	{ writeToLogFile(_msgLog, msg); }

	public void logMsg(String msg, Exception ex)
	{ writeToLogFile(_msgLog, msg, ex); }


  // Error
	public void logErr(String msg)
	{ writeToLogFile(_errLog, msg); }

	public void logErr(String msg, Exception ex)
	{ writeToLogFile(_errLog, msg, ex); }


  // Debug
	public void logDebugMsg(String msg)
	{ 
		if (_doDebug) 
			writeToLogFile(_debugLog, "DEBUG:  " + msg); 
	}

	public void logDebugMsg(String msg, Exception ex)
	{ 
		if (_doDebug) 
			writeToLogFile(_debugLog,  "DEBUG:  " + msg,  ex); 
	}




	//===== Private Log Write Methods ======================================
	private void writeToLogFile(LogFile lf, String msg)
	{
		if (lf == null)
			lf = _defaultLog;

		lf.log(msg);
	}


	private void writeToLogFile(LogFile lf, String msg, Exception ex)
	{
		if (lf == null)
			lf = _defaultLog;

		lf.log(msg, ex);
	}


}

