package org.aspenos.app.aosmailserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.db.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class SubscriberDef extends IdDef {

	public SubscriberDef() {
		super();
		setDefName("Subscriber");
	}


	public SubscriberDef(Map m) {
		super(m, "Subscriber"); 
	}


	public String getInsertSqlFandV() {
		return getSqlFieldsAndValues();
	}

	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String subscriber_id = getId();
		if (subscriber_id == null || subscriber_id.equals("null")) {
			subscriber_id = (String)getProperty("subscriber_id");
		}
		String name = (String)getProperty("name");
		String email = (String)getProperty("email");

		String fields = 
			" name,email) VALUES (";

		sb.append(" (");

		// Skip the ID if it's null
		if (subscriber_id == null || subscriber_id.equals("null")) {
			sb.append(fields).append("'");

		} else {
			sb.append("subscriber_id,")
				.append(fields)
				.append(subscriber_id)
				.append(",'");
		}

		sb.append(DbTranslator.makeSQLSafe(name))
			.append("','")
			.append(DbTranslator.makeSQLSafe(email))
			.append("') ");

		return sb.toString();
	}


	public String getUpdateSqlFandV() {

		String name = (String)getProperty("name");
		String email = (String)getProperty("email");

		// append the fields
		StringBuffer fandv = new StringBuffer()
			.append("name='").append(DbTranslator.makeSQLSafe(name))
			.append("',email='").append(DbTranslator.makeSQLSafe(email))
			.append("'");

		return fandv.toString();
	}
}
