package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.util.*;
import org.aspenos.db.*;

/**
 *
 */
public class WebEventRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public WebEventRegistry() {
	}

	public WebEventRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =======================================================
	/** 
	 * Returns all info about a particular WebEvent. 
	 *
	 * @param name the WebEvent's name
	 * @return the WebEvent with this name
	 */
	public WebEventDef getEventByName(String name) 
			throws SQLException {
		return getEventByName(null,name);
	}

	public WebEventDef getEventByName(String rgKey, String name) 
			throws SQLException {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		attribs		= " * ";
		from 		= new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey);
		where 		= new StringBuffer("name='")
			.append(name).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());
		
		if (hash == null || hash.size() == 0)
			return null;

		WebEventDef event = new WebEventDef(hash);
		Integer id = (Integer)hash.get("webevent_id");
		if (id != null)
			event.setId(id.toString());

		return event;
	}


	/** 
	 * Returns all info about a particular WebEvent.
	 *
	 * @param weid the WebEvent's ID
	 * @return the WebEvent with this ID
	 */
	public WebEventDef getEventById(String weid) 
			throws SQLException {
		return getEventById(null, weid);
	}

	public WebEventDef getEventById(String rgKey, String weid) 
			throws SQLException {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		attribs		= " * ";
		from 		= new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey);

		where 		= new StringBuffer(" webevent_id=")
			.append(weid);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		WebEventDef event = new WebEventDef(hash);
		Integer id = (Integer)hash.get("webevent_id");
		if (id != null)
			event.setId(id.toString());

		return event;
	}


	/** 
	 * Returns a set IDs for resources associated with a given WebEvent.
	 *
	 * @param weid the WebEvent's ID
	 * @return the IDs of all of the given WebEvent's resources
	 * or null if there are none.
	 */
	public IdDefs getAllResourceIds(String weid) 
			throws SQLException {
		return getAllResourceIds(null, weid);
	}

	public IdDefs getAllResourceIds(String rgKey, String weid) 
			throws SQLException {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		attribs		= " resource_id,ordinal ";
		from 		= new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey);
		where 		= new StringBuffer(" webevent_id=")
			.append(weid).append(" ORDER BY ordinal");

		List resourceIds = _db.select(attribs, from.toString(), where.toString());

		return new IdDefs(resourceIds);
	}


	/** 
	 * Checks if a role can access a given event.
	 * A role has access to an event if and only if
	 * there is at least one template defined for at
	 * least one of the event's resources.  
	 *
	 * A template defined for role #0 (the default role)
	 * DOES count as giving the role access to the resource.
	 *
	 * @param weid the WebEvent's ID
	 * @return true if so
	 */
	public boolean roleHasAccess(String weid, String roleid) 
			throws SQLException {
		return roleHasAccess(null,weid,roleid);
	}

	public boolean roleHasAccess(String rgKey, String weid, String roleid) 
			throws SQLException {

		StringBuffer from;
		StringBuffer templateWhere;
		StringBuffer unionSelect;
		StringBuffer bothUnions;
		StringBuffer where;

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE wer.resource_id=rt.resource_id 
		//  AND wer.webevent_id=101
		//  AND rt.role_id=2
		//  UNION
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE wer.resource_id=rt.resource_id 
		//  AND wer.webevent_id=101
		//  AND rt.role_id=0
		//  UNION
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE wer.resource_id=rt.resource_id 
		//  AND wer.webevent_id=101
		//  AND rt.role_id=-1
		/////////////////////////////////////////////////////////////

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		from = new StringBuffer("resourcetemplates")
			.append(_regGroupSeparator).append(rgKey)
			.append(" rt,webeventresources")
			.append(_regGroupSeparator).append(rgKey)
			.append(" wer");
		

		templateWhere = new StringBuffer(
			"wer.resource_id=rt.resource_id AND wer.webevent_id=")
			.append(weid)
			.append(" AND rt.role_id=");

		unionSelect = new StringBuffer(" UNION SELECT * FROM ")
			.append(from.toString())
			.append(" WHERE ")
			.append(templateWhere.toString());

		bothUnions = new StringBuffer(unionSelect.toString())
			.append("0 ")
			.append(unionSelect.toString())
			.append("-1 ");

		where = new StringBuffer(templateWhere.toString())
			.append(roleid).append(" ")
			.append(bothUnions.toString());

		return _db.canFind(from.toString(), where.toString());
	}


	/**
	 * Updates one web event.
	 */
	public void updateWebEventDef(IdDef origId, WebEventDef def)
			throws SQLException {
		updateWebEventDef(null, origId, def);
	}

	public void updateWebEventDef(String rgKey, IdDef origId, WebEventDef def)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;
 
		StringBuffer update = new StringBuffer("UPDATE webevent_")
				.append(rgKey)
				.append(" SET ");
		StringBuffer where = new StringBuffer(" WHERE webevent_id=")
			.append(origId.getId());

		StringBuffer tmp = new StringBuffer(update.toString())
			.append(def.getUpdateFieldsAndValues())
			.append(where.toString());

		//debug("WER.updateWED: " + tmp.toString());
		_db.update(tmp.toString());
	}


	/**
	 * Stores one web event.
	 */
	public void storeWebEventDef(WebEventDef def)
			throws SQLException {
		storeWebEventDef(null, def);
	}

	public void storeWebEventDef(String rgKey, WebEventDef def)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		StringBuffer insert = new StringBuffer("INSERT INTO webevent_")
				.append(rgKey)
				.append(" ");

		String tmp = insert.toString() + 
			def.getSqlFieldsAndValues();

		//debug("WER.storeWED: " + tmp);
		_db.insert(tmp);
	}


	/**
	 * Stores a set of web events.
	 */
	public void storeWebEventDefs(WebEventDefs defs)
			throws SQLException {
		storeWebEventDefs(null, defs);
	}

	public void storeWebEventDefs(String rgKey, WebEventDefs defs)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		StringBuffer insert = new StringBuffer("INSERT INTO webevent_")
				.append(rgKey)
				.append(" ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			WebEventDef def = (WebEventDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			//debug("storeWebEventDefs:\n" + tmp);

			_db.insert(tmp);
		}
	}


	/**
	 * Stores a set of ERTs (Event/Resource/Template combos).
	 * If the 'registry_group' attribute of an ERTDef is set,
	 * then that group is used for the insert.  Otherwise
	 * rgKey, or _regGroup the one set at registry creation time,
	 * is used as the default.
	 * 
	 * Multiple insertions into two separate tables can occur
	 * for each ERTDef.  
	 */
	public void storeERTDefs(ERTDefs defs)
			throws SQLException {
		storeERTDefs(null, defs);
	}

	public void storeERTDefs(String rgKey, ERTDefs defs)
			throws SQLException {

		for (Iterator it=defs.iterator(); it.hasNext();) {
			ERTDef def = (ERTDef)it.next();

			String regGroup = null;

			//regGroup = def.getRegGroupName();

			if (regGroup == null) {
				if (rgKey == null)
					regGroup = _regGroup;
				else
					regGroup = rgKey;
			}

			fillInERTFields(regGroup, def);

			StringBuffer werInsert = new StringBuffer(
					"INSERT INTO webeventresources_")
					.append(regGroup)
					.append(" ");

			StringBuffer rtInsert = new StringBuffer(
					"INSERT INTO resourcetemplates_")
					.append(regGroup)
					.append(" ");

			String fandv;
			StringBuffer sql;
			Iterator lit;

			List list = def.getSqlWERFieldsAndValues();
			for (lit=list.iterator(); lit.hasNext();) {
				fandv = (String)lit.next();
				sql = new StringBuffer(werInsert.toString())
					.append(fandv);
				_db.insert(sql.toString());
			}

			list = def.getSqlRTFieldsAndValues();
			for (lit=list.iterator(); lit.hasNext();) {
				fandv = (String)lit.next();
				sql = new StringBuffer(rtInsert.toString())
					.append(fandv);
				_db.insert(sql.toString());
			}
		}
	}


	/** 
	 * Returns all web events for a given registry group key.
	 *
	 * @param rgKey the registry group key
	 */
	public WebEventDefs getAllEventsForRGKey(String rgKey) 
			throws SQLException, Exception {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= "*";
		from 		= new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ORDER BY name ASC ");

		List weds = _db.selectAsHash(attribs, from.toString(), 
				null);

		return new WebEventDefs(weds);
	}


	/**
	 * Gets any Event, Resource, Template and Role
	 * data that is not already present.
	 */
	private void fillInERTFields(String rgKey, ERTDef ert) throws SQLException {

			// if no id
			//// get name
			//// if no name
			/////// ERROR
			//// else found name
			////// def = getByName(name);
			// else found id
			//// def = getById(id);
			// newDefs.add(def);

		
		String id, name;

		WebEventDefs newWEDefs = new WebEventDefs();
		ResourceDefs newRDefs;
		TemplateDefs newTDefs;
		RoleDefs newRoleDefs;

		ResourceRegistry resReg = new ResourceRegistry(_db);
		TemplateRegistry tReg = new TemplateRegistry(_db);
		RoleRegistry roleReg = new RoleRegistry(_db);

		WebEventDefs wedefs = (WebEventDefs)ert.getProperty("webevent_defs");
		Iterator weit = wedefs.iterator();
		while (weit.hasNext()) {

			WebEventDef wed = (WebEventDef)weit.next();
			ResourceDefs rdefs = (ResourceDefs)wed.getProperty("resource_defs");

			// get the ID
			id = (String)wed.getId();
			if (id == null)
				id = (String)wed.getProperty("webevent_id");

			// check the ID
			if (id == null || id.equals("null")) {
				name = (String)wed.getProperty("name");

				if (name == null || name.equals("null")) {
					// ERROR!!
					debug("WER ERROR:  both the event name and ID null!");
				} else {
					wed = getEventByName(rgKey, name);
				}
			} else  {
				wed = getEventById(rgKey, id);
			}

			if (wed == null)
				continue;

			newWEDefs.add(wed);
			newRDefs = new ResourceDefs();

			Iterator rit = rdefs.iterator();
			while (rit.hasNext()) {
				ResourceDef rd = (ResourceDef)rit.next();
				TemplateDefs tdefs = (TemplateDefs)rd.getProperty("template_defs");

				// get the ID
				id = (String)rd.getId();
				if (id == null)
					id = (String)rd.getProperty("resource_id");

				// check the ID
				if (id == null || id.equals("null")) {
					name = (String)rd.getProperty("name");

					if (name == null || name.equals("null")) {
						// ERROR!!
						debug("WER ERROR:  both the resource name and ID null!");
					} else {
						rd = resReg.getResourceByName(rgKey, name);
					}
				} else  {
					rd = resReg.getResourceById(rgKey, id);
				}

				if (rd == null)
					continue;

				newRDefs.add(rd);
				newTDefs = new TemplateDefs();

				Iterator tit = tdefs.iterator();
				while (tit.hasNext()) {
					TemplateDef td = (TemplateDef)tit.next();
					RoleDefs roledefs = (RoleDefs)td.getProperty("role_defs");

					// get the ID
					id = (String)td.getId();
					if (id == null)
						id = (String)td.getProperty("template_id");

					// check the ID
					if (id == null || id.equals("null")) {
						name = (String)td.getProperty("name");

						if (name == null || name.equals("null")) {
							// ERROR!!
							debug("WER ERROR:  both the template " +
									"name and ID null!");
						} else {
							td = tReg.getTemplateByName(rgKey, name);
						}
					} else  {
						td = tReg.getTemplateById(rgKey, id);
					}

					if (td == null)
						continue;

					newTDefs.add(td);
					newRoleDefs = new RoleDefs();

					Iterator roleit = roledefs.iterator();
					while (roleit.hasNext()) {
						RoleDef role = (RoleDef)roleit.next();

						// get the ID
						id = (String)role.getId();
						if (id == null)
							id = (String)role.getProperty("role_id");

						// check the ID
						if (id == null || id.equals("null")) {
							name = (String)role.getProperty("name");

							if (name == null || name.equals("null")) {
								// ERROR!!
								debug("WER ERROR:  both the role " +
										"name and ID null!");
							} else {
								role = roleReg.getRoleByName(name);
							}
						} else  {
							role = roleReg.getRoleById(id);
						}

						if (role == null)
							continue;

						newRoleDefs.add(role);
					}
					td.setProperty("role_defs", newRoleDefs);
				}
				rd.setProperty("template_defs", newTDefs);
			}
			wed.setProperty("resource_defs", newRDefs);
		}
		ert.setProperty("webevent_defs", newWEDefs);
	}



	/**
	 * Retrieves a set of roles associated with an event's
	 * resource-templates.
	 *
	 * @return All roles defined for the given event
	 */
	public List getEventRoles(String weid)
			throws SQLException {
		return getEventRoles(null,weid);
	}
	public List getEventRoles(String rgKey, String weid)
			throws SQLException {

		StringBuffer where = new StringBuffer(20)
			.append("webevent_id=").append(weid);

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;


		// get the resource IDs for this event
		List resIds = _db.selectAttrib("resource_id", 
				"webeventresources_"+rgKey, 
				"webevent_id="+weid);

		if (resIds == null || resIds.size() == 0)
			return null;

		String resIdWhere = DbTranslator.buildWhereStringList(
				resIds, "resource_id", "OR", false);

		// get the resource IDs for this event
		List roleIds = _db.selectAttrib("role_id", 
				"resourcetemplates_"+rgKey, resIdWhere);


		// convert the Integer role IDs to Strings
		ArrayList allIds = new ArrayList();
		String rid;
		Iterator lit = roleIds.iterator();
		while (lit.hasNext()) {
			rid = ((Integer)lit.next()).toString();
			allIds.add(rid);
		}

		return allIds;
	}

}


