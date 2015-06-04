package org.aspenos.mail;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;
import org.aspenos.logging.*;
import org.aspenos.mail.exception.*;

/**
 * MailTemplate loads tag-swappable mail templates
 * using the same mechanism as the HTML templates, 
 * so locales and caching are supported.
 *
 * To load templates, use the generic load()
 * method to load a template by name.
 * load( "nameOfYourTemplate.template" );
 * 
 * @author P. Mark Anderson
 *
 */
public class MailTemplate implements Runnable {
	protected String _currentTemplate;
	protected String _type;
	protected String _locale;
	protected String _subject, _to, _cc, _bcc, _from;
	protected boolean _doFormat = false;
	protected Map _tagTable;
	protected TemplateLoader _tl;
	protected LoggerWrapper _lw;
	protected boolean _richText;



  // ======= CONSTRUCTORS ============================================
	/**
	 * 
	 **/
	public MailTemplate() {
		_lw = new LoggerWrapper(); // use default
		_tagTable = null;

		setAllProperties();
	}


	/**
	 *
	 **/
	public MailTemplate(LoggerWrapper lw) {
		_lw = lw;
		_tagTable = null;

		setAllProperties();
	}


	/**
	 * 
	 **/
	public MailTemplate(TemplateLoader tl) {
		_tl = tl;
		_lw = new LoggerWrapper(); // use default
		_tagTable = null;

		setAllProperties();
	}


	/**
	 *
	 **/
	public MailTemplate(TemplateLoader tl, LoggerWrapper lw) {
		_tl = tl;
		_lw = lw;
		_tagTable = null;

		setAllProperties();
	}


  // ======= WORKHORSE METHOD =========================================
	/**
	 * This is the method that actually gets the template.  
	 * The other methods just call this in various ways.
	 * The template is loaded into the MailTemplate instance
	 * and is returned to the caller.
	 *
	 * @param file the name of the file to load
	 * @return the template in that file
	 **/
	public String load(String file) { //throws TemplateException {
		_currentTemplate = _tl.loadTemplate(_type, _locale,  file);
		return _currentTemplate;
	}


	public String loadDirect(String file) { //throws TemplateException {
		_currentTemplate = _tl.loadTemplate(file);
		return _currentTemplate;
	}


	public String loadFile(String path) throws IOException {
		File f = new File(path);
		BufferedReader in = new BufferedReader(
			new FileReader(f));
		StringBuffer sb = new StringBuffer((int)f.length());
		String line;
		while ((line=in.readLine()) != null)
			sb.append(line).append("\n");

		_currentTemplate = sb.toString();
		return _currentTemplate;
	}


  // ======= WHATEVER METHODS =========================================
	/**
	 * Use to set the mail to rich text (HTML) mode and send at 
	 * at the same time.
	 * @param sendAsHTML true or false
	 **/
	public void run() {
		try {
			//_lw.logDebugMsg("MT:templ", _currentTemplate);
			//_lw.logDebugMsg("MT:tags", _tagTable.toString());
			send();

		} catch (MailerException mex) {
			String msg = "MailTemplate.run: Send problem: "+mex.toString();
			if (_lw == null) {
				_lw.logDebugMsg(msg);
				_lw.logErr(msg);
			} else { System.err.println(msg); }
		}
	}

	/**
	 * Use to set the mail to rich text (HTML) mode and send at 
	 * at the same time.
	 * @param sendAsHTML true or false
	 **/
	public void send(boolean sendAsHTML) throws MailerException {
		_richText = sendAsHTML;
		send();
	}

	/**
	 * Swaps all tags into the loaded template before
	 * sending the message to the already specified recipients.
	 **/
	public void send() throws MailerException {

		//_lw.logDebugMsg("MT.send:  starting");

		Mailer m = new Mailer();
		//_lw.logDebugMsg("MT.send:  got mailer");

		// Make sure the message body is valid
		if (_currentTemplate == null || _currentTemplate.length() == 0) {
			_lw.logDebugMsg("MT.send:  no body!");
			throw new MailerException("Empty message body.");
		}

		// Swap out the tags
		if (_tagTable != null) {
			FieldExchanger fe = new FieldExchanger(_lw);
			_currentTemplate = fe.doExchange(_currentTemplate, 
					(HashMap)_tagTable);
		}


		//_lw.logDebugMsg("MT.send:  extracting fields from template");

		// Get the header fields, if they exist, and remove them
		// from the current template (in _currentTemplate)
		String body = extractFieldsFromTemplate();

		//_lw.logDebugMsg("MT.send:  setting up mail header");


		// Make sure that there is at least one recipient
		if (_to == null && _cc == null && _bcc == null)
			throw new MailerException("No recipent specified.");

		if (_to == null) _to = "";
		if (_cc == null) _cc = "";
		if (_bcc == null) _bcc = "";
		if (_from == null) _from = "";
		if (_subject == null) _subject = "";


		try {
		if (_doFormat)
			body = TextUtils.formatBody(body, false);
		} catch (IOException ioe) {
			_lw.logDebugMsg("MailTemplate: Cannot format body: " + ioe);
			_lw.logErr("MailTemplate: Cannot format body: " + ioe);
		}

		//_lw.logDebugMsg("MT.send:  sending to " + _to);
		if (_richText)
			m.sendRich( body, _subject, _to, _cc, _bcc, _from);
		else
			m.sendPlain( body, _subject, _to, _cc, _bcc, _from);

		_lw.logDebugMsg("MT.send:  done");
	}



  // ======= RETRIEVAL METHODS ======================================
	/**
	 * Returns labels and values for the current template's subject,
	 * to, cc, bcc, and from fields.
	 * @return a string of all header fields.
	 */
	public String getHeader() { 
		return new StringBuffer("Subject: ").append(getSubject())
			.append("  To: ").append(getTo())
			.append("  CC: ").append(getCc())
			.append("  BCC: ").append(getBcc())
			.append("  From: ").append(getFrom())
			.toString();
	}

	/**
	 * Returns labels and values for the current template's 
	 * to, cc, and bcc fields.
	 * @return a string of all header fields.
	 */
	public String getRecips() { 
		return new StringBuffer("To: ").append(getTo())
			.append("  CC: ").append(getCc())
			.append("  BCC: ").append(getBcc())
			.toString();
	}

	public String getType()
	{ return _type; }

	public String getLocale()
	{ return _locale; }

	public String getTo()
	{ return _to; }

	public String getCc()
	{ return _cc; }

	public String getBcc()
	{ return _bcc; }

	public String getFrom()
	{ return _from; }

	public String getSubject()
	{ return _subject; }

	public String getBody()
	{ return _currentTemplate; }

	public boolean getDoFormat()
	{ return _doFormat; }


  // ======= SETUP METHODS ===========================================
	public void setType(String type)
	{ _type = type; }


	public void setLocale(String locale)
	{ _locale = locale; }


	public void setTagTable(Map tagTable)
	{ _tagTable = tagTable; }


	public void setHeader(String subject, String to, 
			String cc, String bcc, String from) { 
		_subject = subject; 	
		_to = to;
		_cc = cc;	
		_bcc = bcc;	
		_from = from;
	}

	public void setRecips(String to, String cc, String bcc) { 
		_to = to;
		_cc = cc;	
		_bcc = bcc;
	}

	public void setTo(String to)
	{ _to = to; }

	public void setCc(String cc)
	{ _cc = cc; }

	public void setBcc(String bcc)
	{ _bcc = bcc; }

	public void setFrom(String from)
	{ _from = from; }

	public void setSubject(String subject)
	{ _subject = subject; }

	public void setBody(String body)
	{ _currentTemplate = body; }

	public void setDoFormat(boolean doFormat)
	{ _doFormat = doFormat; }



	/**
	 *
	 **/
	private void setAllProperties() {
		_type = "mail_templates";
		_locale = "en_us";
	}


	/**
	 * Extract and removes lines with "from:", "to:", "cc:",
	 * and "bcc:" directives from _currentTemplate.
	 *
	 * @return The template without the header directives
	 **/
	private String extractFieldsFromTemplate() {
		if (_currentTemplate == null)
			return new String();

		String tmp, tmp2, line;
		StringTokenizer st;
		StringBuffer sb = new StringBuffer();

		tmp = _currentTemplate.toString();
		BufferedReader br = new BufferedReader( new StringReader(tmp) );

		try {

			while ((line=br.readLine()) != null) {

				st = new StringTokenizer(line, ":");

				if (st.hasMoreTokens()) {
					tmp2 = st.nextToken();

					tmp2 = tmp2.trim();

					if (tmp2.equalsIgnoreCase("_subject"))
						_subject = getRemainingTokens(st);
					else if (tmp2.equalsIgnoreCase("_to"))
						_to = getRemainingTokens(st);
					else if (tmp2.equalsIgnoreCase("_from"))
						_from = getRemainingTokens(st);
					else if (tmp2.equalsIgnoreCase("_cc"))
						_cc = getRemainingTokens(st);
					else if (tmp2.equalsIgnoreCase("_bcc"))
						_bcc = getRemainingTokens(st);
					else {
						sb.append(line);
						if (!line.trim().equals("\n"))
							sb.append("\n");
					}
				} else {
					sb.append(line);
					if (!line.trim().equals("\n"))
						sb.append("\n");
				}
			}
		} catch (IOException ioe) {
			_lw.logDebugMsg("MailTemplate.extract: Caught IOException: ",
					ioe);
			_lw.logErr("MailTemplate.extract: Caught IOException: " + 
					ioe);
		}

		return sb.toString();
	}

	private String getRemainingTokens(StringTokenizer st) {
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreElements()) {
			String str = st.nextToken();
			sb.append(str);
			if (str.toLowerCase().endsWith("tag"))
				sb.append(":");
		}
		return sb.toString();
	}
}
