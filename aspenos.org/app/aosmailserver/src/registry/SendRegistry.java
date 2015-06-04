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
public class SendRegistry extends MailServerRegistry {

	public SendRegistry() {
	}

	public SendRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Gets all messages that were sent between the 
	 * two given date/times.
	 */
	public MessageDefs getSentMessages(Calendar after, Calendar before)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM sent_message
		//  WHERE  send_datetime > after
		//  AND  send_datetime < before
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "sent_message";
		StringBuffer where = new StringBuffer("");

		if (after != null)
			where.append("send_datetime > '")
				.append(after.toString()).append("'");

		if (before != null)
			where.append("send_datetime < '")
				.append(before.toString()).append("'");

		List l = _db.selectAsHash(attribs, from, where.toString());
		MessageDefs defs = new MessageDefs(l);

		return defs;
	}


	/** 
	 * Store a sent message.
	 */
	public synchronized void storeSentMessage(MessageDef msgDef) 
			throws SQLException {

		String insert = "INSERT INTO sent_message ";
		String fandv = msgDef.getSqlFieldsAndValues();
		String tmp = insert + fandv;
		_db.insert(tmp);
	}


}
