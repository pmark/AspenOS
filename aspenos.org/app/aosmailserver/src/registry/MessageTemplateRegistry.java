package org.aspenos.app.aosmailserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aosmailserver.defs.*;

/**
 *
 */
public class MessageTemplateRegistry extends MailServerRegistry {

	public MessageTemplateRegistry() {
	}

	public MessageTemplateRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * 
	 */
	public MessageTemplateDef getMessageTemplateById(String mtId)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM message_template 
		//  WHERE  mt_id=mtId
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "message_template";
		StringBuffer where = new StringBuffer("mt_id=")
			.append(mtId).append("");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		MessageTemplateDef mt = new MessageTemplateDef(hash);

		Integer i = (Integer)hash.get("mt_id");
		if (i != null)
			mt.setId(i.toString());

		return mt;
	}


	/** 
	 * Store a message template.
	 */
	public synchronized int storeMessageTemplate(MessageTemplateDef mt) 
			throws SQLException {

		String insert = "INSERT INTO message_template ";
		String fandv = mt.getSqlFieldsAndValues();
		String tmp = insert + fandv;
		_db.insert(tmp);

		Integer id = (Integer)_db.selectFirstAttrib("MAX(mt_id)", 
				"message_template","");

		int rv = id.intValue();

		return rv;
	}




}
