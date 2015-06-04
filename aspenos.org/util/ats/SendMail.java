package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;
import org.aspenos.mail.*;
import org.aspenos.mail.exception.*;
import org.aspenos.logging.*;


/**
 * 
 */
public class SendMail extends Task {

	public void handleTask() {

		logDebugMsg("(Task)SendMail: starting");

		String mt_path = getString("mt_path");
		String jar_path = getString("jar_path");
		String rich_text = getString("rich_text");

		TemplateLoader tl = null;
		MailTemplate mt = null;

		if (jar_path == null) {
			tl = new TemplateLoader();
			tl.useJar(false);
		} else {
			tl = new TemplateLoader(jar_path);
			tl.useJar(true);
		}


		if (mt_path == null) {
			logErr("(Task)SendMail: mt_path not specified, can't send!");
			return;
		}

		LoggerWrapper lw = getLogger();
		if (lw == null)
			mt = new MailTemplate(tl);
		else
			mt = new MailTemplate(tl, lw);

		logDebugMsg("(Task)SendMail: loading template from: " + mt_path);
		mt.loadDirect(mt_path);

		mt.setTagTable(getProperties());

		// send the mail template
		try {
			logDebugMsg("(Task)SendMail: sending message");
			if (rich_text != null && rich_text.equals("true"))
				mt.send(true);
			else
				mt.send(false);
		} catch (MailerException mex) {
			logErr("(Task)SendMail: Cannot send: ", mex);
		}
	}
}
