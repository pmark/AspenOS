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
public class EmailListRegistry extends MailServerRegistry {

	public EmailListRegistry() {
	}

	public EmailListRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Get all email lists by site name.
	 */
	public EmailListDefs getEmailLists(String siteName)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM list 
		//  WHERE  site_name='siteName'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "list";
		StringBuffer where = new StringBuffer("site_name='")
			.append(siteName).append("'");

		List l = (List)_db.selectAsHash(attribs, from, where.toString());

		return new EmailListDefs(l);
	}


	/** 
	 * Get an email list by its ID.
	 */
	public EmailListDef getEmailListById(String elId)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM list 
		//  WHERE  list_id=elId
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "list";
		StringBuffer where = new StringBuffer("list_id=")
			.append(elId);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		EmailListDef def = new EmailListDef(hash);
		Integer i = (Integer)hash.get("list_id");
		if (i != null)
			def.setId(i.toString());

		return def;
	}


	/** 
	 * Creates email list.
	 */
	public void createEmailList(EmailListDef el)
			throws SQLException {

		StringBuffer ins = new StringBuffer(30)
			.append("INSERT INTO list ")
			.append(el.getInsertSqlFandV());

		_db.insert(ins.toString());
	}


	/** 
	 * Get all list subscribers by list ID.
	 */
	public SubscriberDefs getAllSubscribers(String listId)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT s.* FROM list l,list_subscribers ls, subscribers s
		//  WHERE  l.list_id=ls.list_id 
		//  AND s.subscriber_id=ls.subscriber_id
		//  AND l.list_id=listId
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "list l,list_subscribers ls, subscriber s";
		StringBuffer where = new StringBuffer("l.list_id=ls.list_id AND ")
			.append("s.subscriber_id=ls.subscriber_id AND l.list_id=")
			.append(listId);

		List l = (List)_db.selectAsHash(attribs, from, where.toString());

		return new SubscriberDefs(l);
	}


	/** 
	 * Subscribes a user to a list.
	 * @return the new subscriber's AOS Mail Server ID
	 */
	public int subscribeToList(String list_sys_name, String site_name, 
			SubscriberDef sd)
			throws Exception {

		// Get the new subscriber's name and email address
		String name = (String)sd.getProperty("name");
		if (name == null || name.equals("")) {
			StringBuffer tmp = new StringBuffer()
				.append((String)sd.getProperty("first_name"))
				.append(" ")
				.append((String)sd.getProperty("last_name"));
			name = tmp.toString();
		}

		String email = (String)sd.getProperty("email");

		StringBuffer insert1 = new StringBuffer("INSERT INTO subscriber ")
			.append("(name,email) VALUES ('")
			.append(name).append("','")
			.append(email).append("')");

		StringBuffer sub_where = new StringBuffer()
			.append("email='")
			.append(email).append("'");

		StringBuffer list_where = new StringBuffer()
			.append("list_sys_name='")
			.append(list_sys_name).append("'");



		boolean transactionFailed = true;
		Integer subscriber_id = new Integer(-1);
		try {
			setAutoCommit(false);

			// check if this email address is already there
			boolean alreadyExists = 
				_db.canFind("subscriber", sub_where.toString());

			if (!alreadyExists) {
				// add a record to subscriber
				_db.insert(insert1.toString());
			}

			// get the new subscriber's ID
			subscriber_id = (Integer)
				_db.selectFirstAttrib("subscriber_id", "subscriber", 
						sub_where.toString());

			// get the list ID
			Integer list_id = (Integer)
				_db.selectFirstAttrib("list_id", "list", list_where.toString());
			if (list_id == null)
				throw new Exception("Cannot find email list named '" +
						list_sys_name + "'");


			StringBuffer ls_where = new StringBuffer()
				.append("list_id=")
				.append(list_id)
				.append(" AND subscriber_id=")
				.append(subscriber_id);

			alreadyExists = 
				_db.canFind("list_subscribers", ls_where.toString());

			if (!alreadyExists) {
				StringBuffer insert2 = new StringBuffer("INSERT INTO list_subscribers ")
					.append("(list_id,subscriber_id) VALUES (")
					.append(list_id.toString()).append(",")
					.append(subscriber_id.toString()).append(")");

				// add a record to list_subscribers
				_db.insert(insert2.toString());
			}

			commit();
			resetAutoCommit();
			transactionFailed = false;

		} finally {
			if (transactionFailed) {
				rollback();
				subscriber_id = new Integer(-1);
			}
		}

		return subscriber_id.intValue();
	}


	/** 
	 * Gets a Subscriber's subscribed list IDs as Strings.
	 */
	public List getSubscribedLists(String subscriber_id)
			throws SQLException {
		SubscriberDef sub = null;

		List l = _db.selectAttrib("list_id", "list_subscribers",
				"subscriber_id=" + subscriber_id);

		List rl = new ArrayList();

		Iterator lit = l.iterator();
		while (lit.hasNext()) {
			Integer i = (Integer)lit.next();
			rl.add(i.toString());
		}

		return rl;
	}


	/** 
	 * Updates a Subscriber.
	 */
	public void updateSubscriber(String subscriber_id, SubscriberDef sd)
			throws SQLException {

		StringBuffer update = new StringBuffer(256)
			.append("UPDATE subscriber SET ")
			.append(sd.getUpdateSqlFandV())
			.append(" WHERE subscriber_id=")
			.append(subscriber_id);

		_db.update(update.toString());
	}


	/** 
	 * Unsubscribes from all lists.  Optionally removes 
	 * the subscriber info too.
	 */
	public void unsubscribeAll(String subscriber_id, 
			boolean deleteSub) throws SQLException {

		StringBuffer un = new StringBuffer(256)
			.append("DELETE FROM list_subscribers WHERE subscriber_id=")
			.append(subscriber_id);
		_db.update(un.toString());

		if (deleteSub) {
			StringBuffer del = new StringBuffer(256)
				.append("DELETE FROM subscriber WHERE subscriber_id=")
				.append(subscriber_id);
			_db.update(del.toString());
		}
	}


	/** 
	 * Deletes a list given its list_id.
	 * Optionally deletes all subscriber info
	 * for subscribers who are subscribed
	 * to just this list. 
	 *
	 * This method uses a view called 'sub_count'
	 *
	 * @return List of subscriber IDs subscribed only
	 *   to the given list if deleteSub is true, else
	 *   null.
	 */
	public List deleteListById(String list_id, 
			boolean deleteSub) throws SQLException {

		StringBuffer del = null;
		List l = null;

		// optionally delete from subscriber
		try {
			setAutoCommit(false);

			if (deleteSub) {

				// get all subscribers subscribed to only this list
				String attrib = "ls1.subscriber_id";
				String from = "list_subscribers ls1, sub_count";
				String where = "ls1.subscriber_id=sub_count.subscriber_id " +
					"AND (2 > sub_count.count) AND ls1.list_id=" +
					list_id;

				l = _db.selectAttrib(attrib, from, where);

				if (l.size() > 0) {
					String subIdOrs = DbTranslator.buildWhereStringList(
							l, "subscriber_id", "OR", false);

					del = new StringBuffer(50)
						.append("DELETE FROM subscriber WHERE ")
						.append(subIdOrs);

					_db.update(del.toString());
				}
			}

			// delete from list
			StringBuffer un = new StringBuffer(50)
				.append("DELETE FROM list_subscribers WHERE list_id=")
				.append(list_id);
			_db.update(un.toString());

			// delete from list_subscribers
			del = new StringBuffer(50)
				.append("DELETE FROM list WHERE list_id=")
				.append(list_id);
			_db.update(del.toString());
		} finally {
			resetAutoCommit();
		}

		return l;
	}


	/** 
	 * An efficient query that counts the number of
	 * subscribers for each list.
	 *
	 * @return List of HashMaps that each contain two
	 *   keys: list_id, and count.
	 */
	public List countSubscribers() throws SQLException {
		return _db.selectAsHash("list_id,count(subscriber_id)",
				"list_subscribers GROUP BY list_id", null);
	}

}
