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
public class FormMailerRegistry extends MailServerRegistry {

	public FormMailerRegistry() {
	}

	public FormMailerRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Get all form mailers by site name.
	 */
	public FormMailerDefs getAllFormMailers(String siteName)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM list 
		//  WHERE  site_name='siteName'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "form_mailer";
		StringBuffer where = new StringBuffer("site_name='")
			.append(siteName).append("'");

		List l = (List)_db.selectAsHash(attribs, from, where.toString());

		return new FormMailerDefs(l);
	}


	/** 
	 * Get form mailer by its ID.
	 */
	public FormMailerDef getFormMailerById(String fmId)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM list 
		//  WHERE  list_id=fmId
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "form_mailer";
		StringBuffer where = new StringBuffer("fm_id=")
			.append(fmId);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		FormMailerDef def = new FormMailerDef(hash);
		Integer i = (Integer)hash.get("fm_id");
		if (i != null)
			def.setId(i.toString());

		return def;
	}

	/** 
	 * Get form mailer by its site_name.
	 */
	public FormMailerDef getFormMailerById(String site_name)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM list 
		//  WHERE  site_name=site_name
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "form_mailer";
		StringBuffer where = new StringBuffer("site_name='")
			.append(fmId).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		FormMailerDef def = new FormMailerDef(hash);
		Integer i = (Integer)hash.get("fm_id");
		if (i != null)
			def.setId(i.toString());

		return def;
	}

}
