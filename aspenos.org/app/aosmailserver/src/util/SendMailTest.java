package org.aspenos.app.aosmailserver.util;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.mail.*;
import org.aspenos.logging.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class SendMailTest {

	public static final String ADDRESS = "null@204.131.176.27";

	public SendMailTest() {
	}

	public void go(int numThreads, int numRecips) {

		LoggerWrapper lw = new LoggerWrapper();
		BatchMailing[] bm = new BatchMailing[numThreads];

		// Build the BatchMailing
		int i;
		boolean first=true;
		StringBuffer sb = new StringBuffer();

		for (i=0; i<numRecips; i++) {
			if (first)
				first=false;
			else
				sb.append(", ");

			sb.append(ADDRESS);
		}
		String to = sb.toString();
		String body = "This is the body of the SendMailTest email message." +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message."  +
			"This is the body of the SendMailTest email message.";

		for (i=0; i<numThreads; i++) {
			bm[i] = new BatchMailing(lw);
			bm[i].setRecips( to, null, null );
			bm[i].setMessage( body, "This is the subject", "SendMailTest@ix" );
			bm[i].setUseCommonTo( false );
			bm[i].setRecipsPerBatch( 1, 0, 0 );   
		}


		// send with separate threads
		Thread[] threadPool = new Thread[numThreads];
		for (i=0; i<numThreads; i++) {
			try {
				lw.logDebugMsg("sending batch #" + i);
				threadPool[i] = new Thread(bm[i]);
				threadPool[i].start();
				lw.logDebugMsg("thread #" + i + " has been started.\n");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		SendMailTest smt = new SendMailTest();
		int numThreads = 1;
		int numRecips = 20;

		if (args.length == 2) {
			numThreads = Integer.parseInt(args[0]);
			numRecips = Integer.parseInt(args[1]);
		}

		smt.go(numThreads, numRecips);
	}
}
