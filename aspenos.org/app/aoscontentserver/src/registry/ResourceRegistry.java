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
public class ResourceRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public ResourceRegistry() {
	}

	public ResourceRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Checks if a role is contained by a resource.
	 * Returns true if there is a template defined for
	 * the given role ID.
	 *
	 * Also returns true if the resource has a default role
	 * template defined (where role ID is 0).
	 *
	 * @param resourceid in which to check
	 * @param roleid of which to check
	 * @return true if the resource contains the role
	 */
	public boolean roleHasAccess(String resourceid, String roleid) 
			throws SQLException {

		StringBuffer templateWhere;
		StringBuffer where;
		StringBuffer unionSelect;
		StringBuffer bothUnions;

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM resourcetemplates
		//  WHERE rt.resource_id=4
		//  AND rt.role_id=2
		//  UNION
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE rt.resource_id=4
		//  AND rt.role_id=0
		//  UNION
		//  SELECT * FROM resourcetemplates rt,webeventresources wer
		//  WHERE rt.resource_id=4
		//  AND rt.role_id=-1
		/////////////////////////////////////////////////////////////

		StringBuffer from  = new StringBuffer("resourcetemplates")
			.append(_regGroupSeparator).append(_regGroup);

		templateWhere = new StringBuffer("resource_id=")
			.append(resourceid)
			.append(" AND role_id=");

		unionSelect = new StringBuffer(" UNION SELECT * ")
			.append(from.toString())
			.append(" WHERE ")
			.append(templateWhere.toString());

		bothUnions = new StringBuffer(unionSelect.toString())
			.append("0 ")
			.append(unionSelect.toString())
			.append("-1' ");

		where = new StringBuffer(templateWhere.toString())
			.append(roleid).append("' ")
			.append(bothUnions.toString());

		return _db.canFind(from.toString(), where.toString());
	}


	/**
	 * Stores a new webeventresource record.
	 */
	public void storeEventResource(ResourceDef rdef, 
			String eventName, String ordinal)
			throws SQLException {
		storeEventResource(null, rdef, eventName, ordinal);
	}

	public void storeEventResource(String rgKey, 
			ResourceDef rdef, String eventName,
			String ordinal)
			throws SQLException {

		//// QUERY /////////////////////////////
		// SELECT webevent_id 
		// FROM webevent
		// WHERE name='eventName'
		////////////////////////////////////////
		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		StringBuffer from  = new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey);
		StringBuffer where  = new StringBuffer("name='")
			.append(eventName).append("'");

		String weid = ((Integer)_db.selectFirstAttrib("webevent_id",
				from.toString(), where.toString())).toString();

		if (weid == null || weid.equals(""))
			throw new SQLException("The webevent ID was undefined");
		
		String resid = rdef.getId();
		if (resid == null || resid.equals(""))
			resid = (String)rdef.getProperty("resource_id");
		if (resid == null || resid.equals(""))
			throw new SQLException("The resource ID was undefined");

		if (ordinal == null || ordinal.equals(""))
			ordinal = "''";

		// Now insert the record
		StringBuffer insert = 
				new StringBuffer("INSERT INTO webeventresources")
				.append(_regGroupSeparator).append(rgKey)
				.append(" (webevent_id,resource_id,ordinal) VALUES (")
				.append(weid)
				.append(",")
				.append(resid)
				.append(",")
				.append(ordinal)
				.append(")");

		String tmp = insert.toString();

		debug("RR: " + tmp);

		_db.insert(tmp);
	}


	/**
	 * Updates a webeventresource record.
	 */
	public void updateEventResource(String rgKey, 
			IdDef origId,
			ResourceDef rdef, String eventName,
			String ordinal)
			throws SQLException {

		//// QUERY /////////////////////////////
		// SELECT webevent_id 
		// FROM webevent
		// WHERE name='eventName'
		////////////////////////////////////////
		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		// get the event ID for the event name
		StringBuffer from  = new StringBuffer("webevent")
			.append(_regGroupSeparator).append(rgKey);
		StringBuffer where  = new StringBuffer("name='")
			.append(eventName).append("'");


		Object o = _db.selectFirstAttrib("webevent_id",
				from.toString(), where.toString());

		String weid = ((Integer)o).toString();
		
		String resid = rdef.getId();
		if (resid == null || resid.equals(""))
			resid = (String)rdef.getProperty("resource_id");

		if (ordinal == null || ordinal.equals(""))
			ordinal = "''";

		// Now update the record
		StringBuffer update = 
				new StringBuffer("UPDATE webeventresources")
				.append(_regGroupSeparator).append(rgKey)
				.append(" SET webevent_id=")
				.append(weid)
				.append(",resource_id=")
				.append(resid)
				.append(", ordinal=")
				.append(ordinal)
				.append(" WHERE resource_id=")
				.append(origId.getId());

		debug("RR.updateER: " + update.toString());
		_db.update(update.toString());
	}


	/**
	 * Stores a resource.
	 */
	public void storeResourceDef(String rgKey, ResourceDef rdef)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		StringBuffer insert = new StringBuffer("INSERT INTO resource")
				.append(_regGroupSeparator).append(rgKey)
				.append(" ");

		String tmp = insert.toString() + 
			rdef.getSqlFieldsAndValues();

		_db.insert(tmp);
	}


	/**
	 * Updates a resource.
	 */
	public void updateResourceDef(String rgKey, 
			IdDef origId, ResourceDef rdef)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;
 
		StringBuffer update = new StringBuffer("UPDATE resource")
				.append(_regGroupSeparator).append(rgKey)
				.append(" SET ");
		StringBuffer where = new StringBuffer(" WHERE resource_id=")
			.append(origId.getId());
		StringBuffer tmp = new StringBuffer(update.toString())
			.append(rdef.getUpdateFieldsAndValues())
			.append(where.toString());

		debug("RR.updateRD: " + tmp.toString());
		_db.update(tmp.toString());
	}


	/**
	 * Stores a set of resources.
	 */
	public void storeResourceDefs(ResourceDefs rdefs)
			throws SQLException {
		storeResourceDefs(null, rdefs);
	}

	public void storeResourceDefs(String rgKey, ResourceDefs rdefs)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		StringBuffer insert = new StringBuffer("INSERT INTO resource")
				.append(_regGroupSeparator).append(rgKey)
				.append(" ");

		for (Iterator it=rdefs.iterator(); it.hasNext();) {
			ResourceDef rdef = (ResourceDef)it.next();
			String tmp = insert.toString() + 
				rdef.getSqlFieldsAndValues();

			_db.insert(tmp);
		}
	}



	/** 
	 * Returns all info about a particular Resource.
	 *
	 * @param name the Resource's name 
	 * @return the Resource with this name 
	 */
	public ResourceDef getResourceByName(String rgKey, String name) 
			throws SQLException {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= " * ";
		from 		= new StringBuffer("resource")
			.append(_regGroupSeparator).append(rgKey);
		where 		= new StringBuffer(" name='")
			.append(name).append("'");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		ResourceDef res = new ResourceDef(hash);
		Integer id = (Integer)hash.get("resource_id");
		if (id != null)
			res.setId(id.toString());

		return res;
	}


	/** 
	 * Returns all info about a particular Resource.
	 *
	 * @param resid the Resource's ID
	 * @return the Resource with this ID
	 */
	public ResourceDef getResourceById(String rgKey, String resid) 
			throws SQLException {
		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= " * ";
		from 		= new StringBuffer("resource")
			.append(_regGroupSeparator).append(rgKey);
		where 		= new StringBuffer(" resource_id=")
			.append(resid);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		ResourceDef res = new ResourceDef(hash);
		Integer id = (Integer)hash.get("resource_id");
		if (id != null)
			res.setId(id.toString());

		return res;
	}

	/** 
	 * Returns all resources for a given registry group key.
	 *
	 * @param rgKey the registry group key
	 */
	public ResourceDefs getAllResourcesForRGKey(String rgKey) 
			throws SQLException, Exception {

		String attribs;
		StringBuffer from;

		attribs		= "*";
		from 		= new StringBuffer("resource")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ORDER BY name ASC ");

		List rdefs = _db.selectAsHash(attribs, from.toString(), null);

		return new ResourceDefs(rdefs);
	}


	/** 
	 * Returns all resources for a given event ID.
	 *
	 * @param rgKey the registry group key
	 */
	public ResourceDefs getEventResources(IdDef weid) 
			throws SQLException, Exception {
		return getEventResources(null, weid);
	}

	/** 
	 * Returns all resources for a given event ID.
	 *
	 * @param rgKey the registry group key
	 */
	public ResourceDefs getEventResources(String rgKey, IdDef weid) 
			throws SQLException, Exception {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		// Select all resource IDs
		attribs		= "resource_id,ordinal";
		from 		= new StringBuffer("webeventresources")
			.append(_regGroupSeparator).append(rgKey);
		where 		= new StringBuffer("webevent_id=")
			.append(weid.getId())
			.append(" ORDER BY ordinal"); 

		List rids = _db.selectAttrib(attribs, from.toString(), 
				where.toString());


		// Select the actual resources
		List rdefs = new ArrayList();
		if (rids.size() > 0) {
			from = new StringBuffer("resource")
				.append(_regGroupSeparator).append(rgKey);
			String whereRid = DbTranslator.buildWhereStringList(
					rids, "resource_id", "OR", false);

	//debug("\nRR: getting resources for event #" + weid.getId() + 
	//		": SELECT * FROM " + from.toString() + " WHERE " + whereRid);

			rdefs = _db.selectAsHash("*", from.toString(), whereRid);
		}

		return new ResourceDefs(rdefs);
	}


}
