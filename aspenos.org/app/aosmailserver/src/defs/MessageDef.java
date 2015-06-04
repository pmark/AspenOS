package org.aspenos.app.aosmailserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class MessageDef extends IdDef {

	public MessageDef() {
		super();
		setDefName("Message");
	}


	public MessageDef(Map m) {
		super(m, "Message"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer("(mt_id,list_id,status");

		String mt_id = (String)getProperty("subscriber_id");
		String list_id = (String)getProperty("list_id");
		String status = (String)getProperty("status");
		String send_datetime = (String)getProperty("send_datetime");

		boolean useSendDateTime = false;
		
		
		if (send_datetime != null && !send_datetime.equals("")) {
			useSendDateTime = true;
			sb.append(",send_datetime");
		}

		if (mt_id == null || mt_id.equals("null")) 
			mt_id = "''";
		if (list_id == null || list_id.equals("null")) 
			list_id = "''";
		if (status == null || status.equals("null")) 
			status = "";

		sb.append(") VALUES (")
			.append(mt_id)
			.append(",")
			.append(list_id)
			.append(",'")
			.append(status)
			.append("'");

		if (useSendDateTime)
			sb.append(",'")
				.append(send_datetime)
				.append("'");

		sb.append(") ");

		return sb.toString();
	}

}
