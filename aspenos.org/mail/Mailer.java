/*
 * Mailer.java	
 * This is a cool class.
 * I like it.
 *
 */
package org.aspenos.mail;

import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;

import org.aspenos.util.*;
import org.aspenos.mail.exception.*;
import org.aspenos.logging.*;

/**
 *
 * @author P. Mark Anderson
 */
public class Mailer
{
	private Vector _alerts;
	private Session _session;
	private boolean _debug;
	private boolean _attemptedRestart = false;
	private LoggerWrapper _lw = null;

//////////////////////////////////////////////////////////
	public static void main(String args[])
	{
		Mailer m = new Mailer();
		//ram.sendRich();

		GregorianCalendar calendar = new GregorianCalendar();
		String d = new String();
		String t = new String();
		String zone = new String();

		int nDate = calendar.get(Calendar.DATE); 
		int nMin = calendar.get(Calendar.MINUTE);
		int nHour = calendar.get(Calendar.HOUR_OF_DAY);
		int nAmpm;

		nAmpm = (nHour < 12) ? 0 : 1;

		if (nHour > 12)
			nHour -= 12;

		d = (calendar.get(Calendar.MONTH) + 1) + "/" + 
			((nDate < 10) ? "0" : "") + nDate + "/" + 
			calendar.get(Calendar.YEAR);

		t = nHour + ":" + 
			((nMin < 10) ? "0" : "") + nMin + " " +
			((nAmpm == 1) ? "PM" : "AM");


		//System.err.println("Date: " + t + " || " + d);
		int argc = args.length;

		try
		{
			if (argc == 0)
			{
				System.out.print("Sending mail to mark@hldesign.com...");

				// Send it
				m.sendPlain("Test mail.\nSystem date/time is: " + d + 
					"  " + t,
					(new Date()).toString(), 
					"mark@hldesign.com", // To
					"", // CC
					"", // BCC
					"TestMail@hldesign.com");  // From

				System.out.println("MAIL SENT!");
				System.out.println("\nUsage:  Mailer" +
					"\t\t-- or --\t" +
					"Mailer <to> <cc> <bcc> <from> <subject> <body>\n");

			}
			else if (argc == 6)
			{
				String body = "System date/time is: " + d + 
					"  " + t + "\n\n" + args[5];

				System.out.print("Sending mail...");
				m.sendPlain(body,	// Body 
						args[4],	// Subject 
						args[0],    // To
						args[1], 		// CC
						args[2], 		// BCC
						args[3]);   // From

				System.out.println("MAIL SENT!");
			}
			else
				System.out.println("\nUsage:  Mailer" +
					"\t\t-- or --\t" +
					"Mailer <to> <cc> <bcc> <from> <subject> <body>\n");
		} 
		catch (Exception ex)
		{
			System.err.println("\n\nUNABLE TO SEND MAIL:  " + ex + "\n");
			ex.printStackTrace();
		}
	}
//////////////////////////////////////////////////////////


	public Mailer()
	{
		Logger l = new Logger();
		_lw = new LoggerWrapper(l);
		init();
	}

	public Mailer(LoggerWrapper lw)
	{
		_lw = lw;
		init();
	}

	private void init() {
		_session = Session.getDefaultInstance(System.getProperties(), null);
	}

    public void sendPlain(String message, String subject,              
			String to, String cc, String bcc, String from)
			throws MailerException
	{
		String mailer = "AspenOS Mail Service";
//_lw.logDebugMsg("Mailer.sendPlain() starting");
		try 
		{
			// construct the message
			Message msg = new MimeMessage(_session);
			msg.setFrom(new InternetAddress(from));

			// Set the recipients
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(cc, false));
			if (bcc != null)
				msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(bcc, false));

			msg.setSubject(subject);
			//StringBuffer sb = new StringBuffer(message);

			msg.setDataHandler(new DataHandler(
				new ByteArrayDataSource(_lw, message, "text/plain")));

			msg.setHeader("X-Mailer", mailer);
			msg.setSentDate(new Date());

			// Use the local send method
			send(msg); 
		}
		catch (Exception e) 
		{
			String exMsg = "**Mailer.sendPlain(): ";
			if (_attemptedRestart)
				exMsg += "ATTEMPTED RESTART.   ";
			_attemptedRestart = false;
			exMsg += e.getMessage();

			MailerException e2=new MailerException(exMsg);
			e2 = (MailerException)e2.fillInStackTrace();
			throw e2;
		}
	}

    public void sendRich(String message, String subject,              
			String to, String cc, String bcc, String from)
			throws MailerException
	{
		/*
		String subject = "JavaMail Tester";
		String to = "andersop@central.sun.com";
		String cc = "pmark.anderson@sun.com";
		String bcc = "andersop@colorado.edu";
		*/
		String mailer = "AspenOS Mail Service";

		try 
		{
			// construct the message
			Message msg = new MimeMessage(_session);
			msg.setFrom(new InternetAddress(from));

			msg.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(to, false));
			
			if (cc != null)
				msg.setRecipients(Message.RecipientType.CC,
							InternetAddress.parse(cc, false));
			if (bcc != null)
				msg.setRecipients(Message.RecipientType.BCC,
							InternetAddress.parse(bcc, false));

			msg.setSubject(subject);
			msg.setDataHandler(new DataHandler(
				new ByteArrayDataSource(_lw, message, "text/html")));

			msg.setHeader("X-Mailer", mailer);
			msg.setSentDate(new Date());

			// Use the local send method
			send(msg);
		}
		catch (Exception e) 
		{
			String exMsg = "Mailer.sendRich(): ";
			if (_attemptedRestart)
				exMsg += "ATTEMPTED RESTART.   ";
			_attemptedRestart = false;
			exMsg += e.getMessage();

			MailerException e2=new MailerException(exMsg);
			e2 = (MailerException)e2.fillInStackTrace();
			throw e2;
		}
	}

	public void createHtmlBody(Message msg) throws Exception
	{
		/*
		String line;
		StringBuffer sb = new StringBuffer();

		sb.append("<HTML>\n");
		sb.append("<HEAD>\n");
		sb.append("<TITLE>\n");
		sb.append(msg.getSubject() + "\n");
		sb.append("</TITLE>\n");
		sb.append("</HEAD>\n");

		sb.append("<BODY>\n");
		sb.append("<H1>" + 
				"This mail proves that HTML mails can be automated!" +
				"</H1>" + "\n");

		// Import the header
		//FileInputStream in = new FileInputStream( "/usr/local/data/" + 
				//"RAHeader.html" );

		while ((line = in.readLine()) != null) 
		{
			sb.append(line);
			sb.append("\n");
		} 

		// Import the the data (body)
		// Import the footer

		sb.append("</BODY>\n");
		sb.append("</HTML>\n");

		msg.setDataHandler(new DataHandler(
			new ByteArrayDataSource(_lw, sb.toString(), "text/html")));
		*/
	}

	public Message createMultipartMessage() throws MessagingException
	{
		MimeMessage m = new MimeMessage(_session);

		MimeBodyPart bp1 = new MimeBodyPart();
		bp1.setText("Bodypart 1");
		MimeBodyPart bp2 = new MimeBodyPart();
		bp2.setText("Bodypart 2");
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(bp1);
		mp.addBodyPart(bp2);
		m.setContent(mp);

		return m;
	}

	public void send(Message message) throws MessagingException
	{
		Transport.send(message);
/*
		try
		{ 
			Transport.send(message);
		}
		catch (Exception ex)
		{ // Attempt to restart the transport server and try again
			
			_lw.logErr("[" + DateTool.getDateTime() + "]  " +
				"SEND FAILED!!  Attempting to restart Sendmail...");

			// Let the sys admin tool try to restart Sendmail
			_attemptedRestart = true;
			String sysResponse = SysAdminTool.restartSendmail();

			_lw.logErr("[" + DateTool.getDateTime() + "]  " +
				"System response to Sendmail restart:  " + sysResponse);
				
			if (!sysResponse.equals(SysAdminTool.SUCCESS))
				_lw.logErr("[" + DateTool.getDateTime() + "]  " +
					"Attempting to resend anyway...");
			else
				_lw.logErr("[" + DateTool.getDateTime() + "]  " +
					"Attempting to resend again...");

			Transport.send(message);
		}
*/
	}
}


/* This class implements a typed DataSource from :
 * 	an InputStream
 *	a byte array
 * 	a String
 */
class ByteArrayDataSource implements DataSource 
{
    private byte[] data; // data
    private String type; // content-type

    /* Create a datasource from an input stream */
    ByteArrayDataSource(LoggerWrapper lw, InputStream is, String type) throws IOException
	{
        this.type = type;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int ch;

		while ((ch = is.read()) != -1)

		// XXX : must be made more efficient by
		// doing buffered reads, rather than one byte reads
		os.write(ch);
		data = os.toByteArray();
    }

    /* Create a datasource from a byte array */
    ByteArrayDataSource(LoggerWrapper lw, byte[] data, String type) 
	{
        this.data = data;
		this.type = type;
    }

    /* Create a datasource from a String */
    ByteArrayDataSource(LoggerWrapper lw, String data, String type) 
	{
		try
		{
			// Assumption that the string contains only ascii
			// characters ! Else just pass in a charset into this
			// constructor and use it in getBytes()
			this.data = data.getBytes("iso-8859-1");
		}
		catch (UnsupportedEncodingException uex) 
		{ 
			String errMsg = "Mailer: UnsupportedEncodingException: " +
					uex + "\n\n";
			lw.logErr(errMsg);
			lw.logDebugMsg(errMsg);
		}

		this.type = type;
    }

    public InputStream getInputStream() throws IOException 
	{
		if (data == null)
			throw new IOException("no data");
		return new ByteArrayInputStream(data);
    }

    public OutputStream getOutputStream() throws IOException 
	{
		throw new IOException("cannot do this");
    }

    public String getContentType() 
	{
        return type;
    }

    public String getName() 
	{
        return "dummy";
    }


}

