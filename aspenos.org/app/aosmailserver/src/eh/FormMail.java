package org.aspenos.app.aosmailserver.eh;

import java.io.*;
import java.util.*;

import org.aspenos.app.aosmailserver.util.*;
import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.mail.*;


/**
 * Loads a mail template, swaps in form data, then sends 
 * the mail message.
 *
 * Mail templates are stored in the AOSMailServer data dir:
 * <AspenOS root>/apps/AOSMailServer/data/mail_templates/
 * 
 * Templates are organized by group and locale:
 * <group>/<locale>/
 *
 * HTML parameters: 
 *    tgroup - mail template group
 *    tlocale - the default locale is en_us
 *    tname - name of the mail template
 *    redir - URL for redirect if mail was sent successfully
 *    error - URL for redirect if there was an error 
 *    textbody - if used, this field is formatted before sending
 * 
 */
public class FormMail extends MailEHParent 
		implements MailConstants {

	public HashMap handleEvent() {

		HashMap tagTable = getClonedTagTable();
		HashMap params = (HashMap)_wer.getProperty("req_params");

		String tgroup = (String)params.get("tgroup");
		String tlocale = (String)params.get("tlocale");
		String tname = (String)params.get("tname");
		String redir = (String)params.get("redir");
		String error = (String)params.get("error");
		String textbody = (String)params.get("textbody");
		String s = File.separator;
		StringBuffer mtPath = new StringBuffer(128);
		boolean bNoSuccessRedir=false, bNoErrorRedir=false;


		if (tgroup == null)
			tgroup = "default";
		if (tlocale == null)
			tlocale = DEF_LOCALE;
		if (tname == null)
			tname = (String)params.get("mail_template");
		if (tname == null)
			tname = "default";
		if (redir == null || redir.equals("")) 
			if (DEF_REDIR == null) 
				bNoSuccessRedir = true;
			else
				redir = DEF_REDIR;
		if (error == null || error.equals(""))
			if (DEF_ERROR_REDIR == null) 
				bNoErrorRedir = true;
			else
				error = DEF_ERROR_REDIR;
			
		if (textbody != null) {
			textbody = formatText(textbody);
			params.put("textbody", textbody);
		}

		if (!tname.endsWith(".template"))
			tname += ".template";

		// build the mail template path
		mtPath.append(AOSMAIL_TEMPLATE_PATH)
			.append(tgroup).append(s)
			.append(tlocale).append(s)
			.append(tname);


		try {
			MailTemplate mt = new MailTemplate(_lw);
			mt.setTagTable(params);
			mt.loadFile(mtPath.toString());
			mt.send();


			if (bNoSuccessRedir) { 
				// load the success template
				String msg = "Thank you.  Your message has been sent.";
				HashMap tt = new HashMap();
				tt.put("success_msg", msg);
				_lw.logDebugMsg("Getting system template: success");
				tagTable.put("content", getSystemTemplate("success", tt));
			} else {
				tagTable.put("REDIR_URL", redir);
			}

		} catch (Exception ex) {
			_lw.logDebugMsg("FormMail problem: ", ex);
			_lw.logErr("FormMail problem: ", ex);

			if (bNoErrorRedir) { 
				// load the error template
				String msg = "Sorry, please try again later.<BR>" +
					ex.toString();
				HashMap tt = new HashMap();
				tt.put("error_msg", msg);
				_lw.logDebugMsg("Getting system template: error");
				tagTable.put("content", getSystemTemplate("error", tt));
			} else {
				tagTable.put("REDIR_URL", error);
			}
		}

		return tagTable; 
	}


	private String formatText(String text) {
		try {
			if (text == null) return null;
			else text = TextUtils.formatBody(text,false);
		} catch (Exception ioe) {
			_lw.logDebugMsg("couldn't format body: ", ioe);
			_lw.logErr("couldn't format body: ", ioe);
		}
		return text;
	}

}

