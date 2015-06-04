package org.aspenos.logging;


/**
 *
 *
 * Typical use:

 	LoggerWrapper lw = new LoggerWrapper(_logger, "OrderServlet: ");
	lw.
 	
 *
 */
public class LoggerWrapper
{

	private Logger _logger;
	private String _logLabel;


	//===== Constructor ===========================================
	public LoggerWrapper()
	{
		_logger = new Logger();
		_logLabel = "";
	}

	public LoggerWrapper(Logger logger)
	{
		_logger = logger;
		_logLabel = "";
	}

	public LoggerWrapper(Logger logger, String logLabel)
	{
		_logger = logger;
		_logLabel = logLabel;
	}

	


	//===== Primary Methods =======================================
	public void setLogLabel(String newLabel)
	{
		_logLabel = newLabel;
	}


  // Message ================================================================
	public void setMsgLog(String path)
	{ 
		if (path == null)
		{
			logErr(_logLabel, "Message log's path must not be null!");
			logDebugMsg(_logLabel, "Message log's path must not be null!");
		}
		_logger.setMsgLog(path); 
	}

	public void logMsg(String msg)
	{ 
		_logger.logMsg(_logLabel + " " + msg); 
	}

	public void logMsg(String logLabel, String msg)
	{ 
		_logLabel = logLabel;
		_logger.logMsg(_logLabel + " " + msg); 
	}


  // Error ================================================================
	public void setErrLog(String path)
	{ 
		if (path == null)
		{
			logErr(_logLabel, "Error log's path must not be null!");
			logDebugMsg(_logLabel, "Error log's path must not be null!");
		}
		_logger.setErrLog(path); 
	}

	public void logErr(String msg)
	{ 
		_logger.logErr(_logLabel + " " + msg); 
	}

	public void logErr(String msg, Exception ex)
	{ 
		_logger.logErr(_logLabel + " " + msg,  ex); 
	}

	public void logErr(String logLabel, String msg)
	{ 
		_logLabel = logLabel;
		_logger.logErr(_logLabel + " " + msg); 
	}

	public void logErr(String logLabel, String msg, Exception ex)
	{ 
		_logLabel = logLabel;
		_logger.logErr(_logLabel + " " + msg,  ex); 
	}



  // Debug ================================================================
	public void setDebugLog(String path)
	{ 
		if (path == null)
		{
			logErr(_logLabel, "Debug log's path must not be null!");
			logDebugMsg(_logLabel, "Debug log's path must not be null!");
		}
		_logger.setDebugLog(path); 
	}

	public void logDebugMsg(String msg)
	{ 
		_logger.logDebugMsg(_logLabel + " " + msg); 
	}

	public void logDebugMsg(String msg, Exception ex)
	{ 
		_logger.logDebugMsg(_logLabel + " " + msg, ex); 
	}

	public void logDebugMsg(String logLabel, String msg)
	{ 
		_logLabel = logLabel;
		_logger.logDebugMsg(_logLabel + " " + msg); 
	}

	public void logDebugMsg(String logLabel, String msg, Exception ex)
	{ 
		_logLabel = logLabel;
		_logger.logDebugMsg(_logLabel + " " + msg, ex); 
	}

  // Extras ===============================================================
	public Logger getLogger()
	{
		return _logger;
	}


	public void setDefaultLogs() 
	{
		_logger = new Logger();
		_logger.setMsgLog("default.msg");
		_logger.setDebugLog("default.debug");
		_logger.setErrLog("default.err");
		_logger.setDoDebug(true);
		_logLabel = "";
	}
}

