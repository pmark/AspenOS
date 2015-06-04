package org.aspenos.mail;

import java.util.*;
import java.io.*;


/**
 * Tests the Mailer's sendRich routine.
 **/
public class RichMailTest
{

	/**
	 * Runs the test.
	 *
	 **/
	public static void main(String[] args)
	{
		try
		{
			Mailer m = new Mailer();

    		String message;
			String subject="rich test";
			String to="pmarkanderson@yahoo.com";
			String cc=null;
			String bcc=null;
			String from="RichMailTest@AspenOS.net";

			StringBuffer sb = new StringBuffer(100);
			sb.append("<HTML>\n");
			sb.append("<HEAD>\n");
			sb.append("<TITLE>\n");
			sb.append("The Title\n");
			sb.append("</TITLE>\n");
			sb.append("</HEAD>\n");
			sb.append("<BODY>\n");
			sb.append("<H1>");
			sb.append("This mail proves that HTML mails can be automated!");
			sb.append("</H1>\n");
			sb.append("</BODY>\n");
			sb.append("</HTML>\n");

			message = sb.toString();

			if (args.length == 1)
				to = args[0];

    		m.sendRich(message,subject,to,cc,bcc,from);

			System.out.println("rich mail sent to " + to);
		}
		catch (Exception e)
		{
			System.out.println("Can't send: ");
			e.printStackTrace();
		}
	}
}


