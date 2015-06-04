/*
 * BatchMailing.java	
 *
 */
package org.aspenos.mail;

import java.io.*;
import java.util.*;

import org.aspenos.mail.exception.*;
import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.app.aosmailserver.registry.*;
import org.aspenos.app.aosmailserver.defs.*;



/**
 * Disperses a message to a large recipient list among
 * several smaller lists for efficiency. 
 *
 * Features:
 * 	o  Set the max number of recips per message.
 *	o  For BCC lists, set common TO list for each send.
 *
 * Example:
 	BatchMailing bm = new BatchMailing();
	bm.setRecips( to, cc, bcc );
	bm.setMessage( body, subject, from );

	// To send a message to three recips at a time:
	bm.setRecipsPerBatch( 1, 1, 1);   

	// To send one message to all recipients at the
	// same time.  This is inefficient!
	bm.setRecipsPerBatch( -1, -1, -1);   

	// To send a message to eight recips in the "bcc"
	// field at a time, while mailing the same message
	// to the recips in the "to" field each time.
	// This is how a business can send messages to 
	// multiple clients without the clients' email
	// addresses being visible while the same recip
	// in the "to" field is visible.  
	bm.setRecips( "members@MyBusiness.net", "", bcc);
	bm.setUseCommonTo( true );
	bm.setRecipsPerBatch( -1, 0, 8);   

	// To send a message to one recipient in the "to"
	// field, three in the "cc" field, and all recips
	// in the "bcc" field.
	bm.setRecipsPerBatch( 1, 3, -1);   

	// To send to only the recips in the "to" field, 
	// fifteen at a time (NOTE that the recips in 
	// the "cc" and "bcc" fields will not be sent 
	// the message):
	bm.setRecipsPerBatch( 15, 0, 0);   

	// You can make a new send thread easily
	Thread sendThread = new Thread(bm);
	sendThread.start();
	
	// Or just send it without using a new thread
	bm.send();
 *
 *
 */
public class BatchMailing implements Runnable
{
	private String _to;
	private String _cc;
	private String _bcc;
	private String _from;
	private String _subject;
	private String _body;
	private String _mediaType;
	private int _toRecipsPerBatch;
	private int _ccRecipsPerBatch;
	private int _bccRecipsPerBatch;
	private boolean _useCommonTo;
	private boolean _useCommonCc;
	private boolean _useCommonBcc;
	private boolean _useSendRegistry;
	private boolean _doFormatBody;
	private LoggerWrapper _lw;
	private SendRegistry _sendReg;
	private MessageDef _msgDef;

	private static final String PLAIN = "plain";
	private static final String RICH = "rich";
	public static final int MAX_CHARS_PER_LINE = 73;



	public BatchMailing()
	{
		Logger logger = new Logger();
		_lw = new LoggerWrapper(logger);
		init();

	}

	public BatchMailing(LoggerWrapper lw)
	{
		_lw = lw;
		init();
	}

	private void init() {
		_toRecipsPerBatch = 10;
		_ccRecipsPerBatch = 10;
		_bccRecipsPerBatch = 10;

		_useCommonTo = false;
		_useCommonCc = false;
		_useCommonBcc = false;

		_useSendRegistry = false;

		_mediaType = PLAIN;
	}

	public void run() {

		StringBuffer statusMsg = new StringBuffer();
		String sendMsg;

		try {

			sendMsg = this.send();
			statusMsg.append("SUCCESS.  ")
				.append(sendMsg);

		} catch (Exception ex) {

			_lw.logErr("BatchMailing: problem in send thread: ", ex);
			_lw.logDebugMsg("BatchMailing: problem in send thread: ", ex);

			statusMsg.append("FAILED.  ")
				.append(ex.toString());
		}

		// if the mail registry is being used,
		// the sent message archive needs to be
		// updated along with the message's send
		// status.
		if (_useSendRegistry) {
			// a MessageDef contains the MT ID, list ID, a status,
			// and optionally a send date
			try {
				_msgDef.setProperty("status", statusMsg.toString());
				_sendReg.storeSentMessage(_msgDef);
			} catch (Exception sqlex) {
				String msg = "BatchMailing: unable to store sent message: ";
				_lw.logErr(msg, sqlex);
				_lw.logDebugMsg(msg, sqlex);
			}
		} 
	}

	public void setRichText(boolean b)
	{
		if (b)
			_mediaType = RICH;
		else
			_mediaType = PLAIN;
	}

	public void setRecips(String to, String cc, String bcc)
	{
		_to = to;
		_cc = cc;
		_bcc = bcc;
	}

	public void setMessage(String body, String subject, String from)
	{
		_body = body;
		_subject = subject;
		_from = from;
	}

	public String send() throws NoToRecipException, Exception
	{
		Mailer m = new Mailer();
		if (_lw == null)
			m = new Mailer();
		else
			m = new Mailer(_lw);
		_lw.logDebugMsg("BM: created Mailer");

		Vector toList = new Vector();
		Vector ccList = new Vector();
		Vector bccList = new Vector();
		StringBuffer errMsg = new StringBuffer();

		// format the body
		try {
			if (_doFormatBody)
				_body = formatBody(_body);
		} catch (Exception fex) {
			String msg = "Unable to format message body";
			_lw.logDebugMsg(msg, fex);
			_lw.logErr(msg, fex);
		}

		// Must have at least a TO recipient
		if (_to == null)
			_to = "";
		if (_to.length() == 0)
			throw new NoToRecipException();


		// Create the list arrays
		// ========================== TO ===========
		if (_useCommonTo)
			toList.addElement( _to );
		else
			toList = divideList( _to, _toRecipsPerBatch );

		// ========================== CC ===========
		if (_cc != null && _cc.length() > 0)
		{
			if (_useCommonCc)
				ccList.addElement( _cc );
			else
				ccList = divideList( _cc, _ccRecipsPerBatch );
		}

		// ========================== BCC ==========
		if (_bcc != null && _bcc.length() > 0)
		{
			if (_useCommonBcc)
				bccList.addElement( _bcc );
			else
				bccList = divideList( _bcc, _bccRecipsPerBatch );
		}


		int i=0, j=0, k=0;
		int toListSize = toList.size();
		int ccListSize = ccList.size();
		int bccListSize = bccList.size();
		int batchCount = 0;
		String toTmp, ccTmp, bccTmp;

		_lw.logDebugMsg("BM: Building mail and sending batch:");
		// Build the mail
		while (true)
		{
			// ========================== TO ===========
			if (_useCommonTo)
			{
				toTmp = (String)toList.elementAt( 0 );
				
			}
			else
			{
				if (toListSize > i)
					toTmp = (String)toList.elementAt( i++ );
				else
					toTmp = "";
					
			}

			// ========================== CC ===========
			if (_useCommonCc)
				ccTmp = (String)ccList.elementAt( 0 );
			else
			{
				if (ccListSize > j)
					ccTmp = (String)ccList.elementAt( j++ );
				else
					ccTmp = "";
			}

			// ========================== BCC ==========
			if (_useCommonBcc)
				bccTmp = (String)bccList.elementAt( 0 );
			else
			{
				if (bccListSize > k)
					bccTmp = (String)bccList.elementAt( k++ );
				else
					bccTmp = "";
			}

			if ((_useCommonTo  || toTmp.length() == 0) && 
				(_useCommonCc  || ccTmp.length() == 0) && 
				(_useCommonBcc || bccTmp.length() == 0))
			{
				// No more mail to send
				break;
			}
			else
			{
				try {
					if (_mediaType.equals(PLAIN))
						m.sendPlain(_body, _subject, toTmp, ccTmp, bccTmp, _from);
					else  // for everything else, there's RICH
						m.sendRich(_body, _subject, toTmp, ccTmp, bccTmp, _from);

					batchCount++;
				} catch (Exception ex) {
					errMsg.append(ex.toString())
						.append("\n");
				}
			}
		}
		_lw.logDebugMsg("BM: Sent " + batchCount + 
				" " + _mediaType + 
				" message(s) in this batch mailing.");
		if (errMsg.length() != 0)
			_lw.logDebugMsg("BM: ENCOUNTERED ERRORS: " + 
					errMsg.toString());

		return errMsg.toString();
	}


	public Vector divideList( String strRecips, int recipsPerBatch )
	{
		Vector list = null;
		StringBuffer line = null;
		int numRecips = 0;
		int i = 0;
		
		// Delimit recips with spaces, not semi or comma
		strRecips = strRecips.replace(';', ',');
		strRecips = strRecips.replace(',', ' ');

		list = new Vector();
		if (recipsPerBatch == -1)
		{
			list.addElement( strRecips );
			return list;
		}
		else if (recipsPerBatch == 0)
		{
			list.addElement( "" );
			return list;
		}

		StringTokenizer st = new StringTokenizer(strRecips, " ");
		int totalRecips = st.countTokens();
		while (st.hasMoreTokens())
		{
			if (line == null) line = new StringBuffer();
			else line.append(" ");
				
			line.append( st.nextElement() );
			numRecips++;
			if ((numRecips % recipsPerBatch == 0) || numRecips ==
				totalRecips)
			{
				list.addElement( line.toString() );
				line = null;
			}
		}

		return list;
	}


	public void setRecipsPerBatch(int to, int cc, int bcc)
	{ 
		_toRecipsPerBatch = to; 
		_ccRecipsPerBatch = cc; 
		_bccRecipsPerBatch = bcc; 
	}	
	
	public int getToRecipsPerBatch()
	{ return _toRecipsPerBatch; }	

	public int getCcRecipsPerBatch()
	{ return _ccRecipsPerBatch; }	

	public int getBccRecipsPerBatch()
	{ return _bccRecipsPerBatch; }	
	

	public void setUseCommonTo(boolean b)
	{ _useCommonTo = b; }

	public void setUseCommonCc(boolean b)
	{ _useCommonCc = b; }

	public void setUseCommonBcc(boolean b)
	{ _useCommonBcc = b; }

	public void setDoFormatBody(boolean b)
	{ _doFormatBody = b; }


	public void setUseSendRegistry(boolean b)
	{ _useSendRegistry = b; }


	public void setSendRegistry(SendRegistry sendReg)
	{ _sendReg = sendReg; }

	public void setMessageDef(MessageDef msgDef)
	{ _msgDef = msgDef; }


	public static void main(String args[])
	{
		BatchMailing bm = new BatchMailing();

		String to = "mark@ix";
		String cc = null;
		String bcc = 
			"mark@hldesign.com, dev@hldesign.com, services@hldesign.com";
		String body = "Yo Wassup?!?!?";
		String subject = "Testing BatchMailing";
		String from = "jojo@theApe.com";



		// Build the BatchMailing
		bm.setRecips( to, cc, bcc );
		bm.setMessage( body, subject, from );
		//bm.setUseCommonTo( true );
		//bm.setRecipsPerBatch( -1, 0, 2);   

		// If sending to only one recipient in the TO
		// field, useCommonTo must be false.
		bm.setUseCommonTo( false );
		bm.setRecipsPerBatch( 1, 0, 0);   

		try
		{
			bm.send();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

			
	}


	private String formatBody(String body)
			throws IOException {

		StringBuffer formatted = new StringBuffer();

		BufferedReader br = new BufferedReader(
				new StringReader(body));

		String line, lhs, rhs, newLine;
		int pos;
		while ((line=br.readLine()) != null) {

			rhs = line;
			while (rhs.length() > MAX_CHARS_PER_LINE) {

				// find the new end of the line
				pos = rhs.lastIndexOf(" ", MAX_CHARS_PER_LINE);

				// get the new LHS
				lhs = rhs.substring(0,pos);

				// get the new RHS
				if (rhs.length() > pos) {
					rhs = rhs.substring(pos+1);
				} 

				// write the current line
				formatted.append(lhs);
				formatted.append("\n");
			} 

			// write the current line
			formatted.append(rhs);
			formatted.append("\n");
		}

		return formatted.toString().trim();
	}

}
