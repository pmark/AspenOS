package org.aspenos.util;

import java.util.*;
import java.io.*;

public class SysAdminTool
{

	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	public static final String SENDMAIL_CMD = "/usr/local/data/sendmail_cmd";
	public static final long DELAY1 = 70000;


	public static String restartSendmail()
	{
		try
		{
			File f = new File(SENDMAIL_CMD);

			if (!f.exists())
				f.createNewFile();

			// Delay
			Thread.sleep(DELAY1);

			// if file exists after waiting for the
			// server to restart...
			if (f.exists())
				return FAIL; 	// system didn't restart
			else
				return SUCCESS;
		}
		catch (IOException ioe)
		{ 
			System.err.println("SAT: ioe: " + ioe);
			return FAIL; 
		}
		catch (InterruptedException ie)
		{ 
			System.err.println("SAT: Thread interrupted: " + ie);
			return FAIL; 
		}
	}



}
