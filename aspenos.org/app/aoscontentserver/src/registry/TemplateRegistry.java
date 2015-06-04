package org.aspenos.app.aoscontentserver.registry;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.aspenos.exception.*;
import org.aspenos.app.aoscontentserver.defs.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.xml.*;
import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.xml.*;

/**
 *
 */
public class TemplateRegistry extends CSRegistry implements IRegistry {

	// Init Methods ==========================================================
	public TemplateRegistry() {
	}

	public TemplateRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =======================================================

	/** 
	 * Retrieves all templates and maps their IDs to 
	 * their names.
	 *
	 * @return A List of HashMap objects that contain the
	 *    template_id mapped to the template name.
	 */
	public Map getIDToNameMappings(String rgKey) 
			throws Exception {

		//// QUERY //////////////////////////////////////////
		// SELECT template_id,name
		// FROM template
		/////////////////////////////////////////////////////

		HashMap tMap = new HashMap();

		String attribs = "template_id,name";

		StringBuffer from = new StringBuffer();

		from.append("template")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ORDER BY name ASC ");

		List templateList = _db.selectAsHash(
				attribs, from.toString(), "");

		Iterator tit = templateList.iterator();
		while (tit.hasNext()) {
			HashMap hash = (HashMap)tit.next();
			String tid = ((Integer)hash.get("template_id")).toString();
			String name = (String)hash.get("name");

			tMap.put(tid,name);
		}

	//debug("TR: returning tMap: " + tMap.toString());
		return tMap;
	}


	/** 
	 * Retrieves all template IDs for a resource.
	 *
	 * @param resName the name of a resource
	 * @return All of the templates for a resource.
	 */
	public List getRTsByResourceName(String rgKey, String resName) 
			throws Exception {

		//// QUERY #1 ////////////////////////////// 
		// SELECT *
		// FROM resourcetemplates rt, resource r
		// WHERE rt.resource_id=r.resource_id
		// AND r.name='resName'
		////////////////////////////////////////////
		String attribs;
		StringBuffer from = new StringBuffer();
		StringBuffer where = new StringBuffer();

		attribs = "*";

		from.append("resourcetemplates")
			.append(_regGroupSeparator).append(rgKey)
			.append(" rt,resource")
			.append(_regGroupSeparator).append(rgKey)
			.append(" r");

		where.append("rt.resource_id=r.resource_id AND r.name='")
			.append(resName)
			.append("'");

		List tList = _db.selectAsHash(attribs, 
				from.toString(), where.toString());

		if (tList == null || tList.size() == 0)
			return null;

		return tList;
	}


	/** 
	 * Retrieves any templates associated with a template ID and
	 * a role ID.
	 * @param weid the web event's ID 
	 * @param roleid the role's ID
	 * @return All of the templates for all of the resources for 
	 * this event/role combination.
	 */
	public TemplateDefs getTemplatesByEvent(String weid, String roleid) 
			throws Exception {
		return getTemplatesByEvent(null, weid, roleid);
	}

	public TemplateDefs getTemplatesByEvent(String rgKey, 
			String weid, String roleid) 
			throws Exception {

 		String attribs;
		StringBuffer from, unionSelect, templateWhere, where;

		// I think this has to take three separate queries.
		// 1)  The first gets all matching template IDs for the role and 
		// webevent.
		// 2)  The second gets all templates for this resource with 
		// role ID 0, the default role.  That only happens if nothing
		// is returned from the first query.
		// 3)  The third gets the actual data about each template
		// by using the list of template IDs.


		///// Query #1 -- get the template IDs //////////////////////
		// SELECT rt.template_id,wer.ordinal 
		// FROM resourcetemplates rt,webeventresources wer 
		// WHERE wer.resource_id=rt.resource_id  
		// AND wer.webevent_id=<given web event ID> 
		// AND rt.role_id=<given role ID>'

		//  -- The following portion of the query appends all templates
		//  -- for this resource that have role ID -1.
		// UNION  
		// SELECT rt.template_id,wer.ordinal 
		// FROM resourcetemplates rt,webeventresources wer 
		// WHERE wer.resource_id=rt.resource_id  
		// AND wer.webevent_id=<given web event ID>
		// AND rt.role_id=-1 
		// ORDER BY ordinal ASC 
		/////////////////////////////////////////////////////////////

		if (rgKey == null || rgKey.equals("")) {
			rgKey = _regGroup;
		}

		attribs = "rt.template_id,wer.ordinal";

		from = new StringBuffer("resourcetemplates")
			.append(_regGroupSeparator).append(rgKey)
			.append(" rt,webeventresources")
			.append(_regGroupSeparator).append(rgKey)
			.append(" wer");
		
		templateWhere = new StringBuffer(
				"wer.resource_id=rt.resource_id AND wer.webevent_id=")
			.append(weid)
			.append(" AND rt.role_id=");

		unionSelect = new StringBuffer(" UNION (SELECT ")
			.append(attribs)
			.append(" FROM ")
			.append(from.toString())
			.append(" WHERE ")
			.append(templateWhere.toString())
			.append("-1) ORDER BY ordinal");

		where = new StringBuffer(templateWhere.toString())
			.append(roleid)
			.append(unionSelect.toString());

//System.out.println("TemplateRegistry: " + unionSelect.toString());
//System.err.println("TemplateRegistry: " + unionSelect.toString());
//debug("TemplateRegistry: where: " + where.toString());


		List list = _db.selectAttrib(attribs, 
				from.toString(), where.toString());

		if (list == null || list.size() == 0)
			return null;


		///// Query #2 -- get the default template (ID = 0)
		// SELECT rt.template_id 
		// FROM resourcetemplates rt,webeventresources wer 
		// WHERE wer.resource_id=rt.resource_id  
		// AND wer.webevent_id=<given web event ID> 
		// AND rt.role_id=0 
		if (list.size() == 0) {
			where = new StringBuffer(templateWhere.toString())
				.append("0' ");

			list = _db.selectAttrib(attribs, 
					from.toString(), where.toString());
		}



		// Create a string of all the template IDs
		String tids = DbTranslator.buildWhereStringList(
				list, "template_id", "OR", false);


		///// Query #3 -- get all template details
		// SELECT *
		// FROM template 
		// WHERE template_id=x OR template_id=y ... 
		attribs	= " * ";
		from	= new StringBuffer("template")
			.append(_regGroupSeparator).append(rgKey);

		list = _db.selectAsHash(attribs, from.toString(), tids);

		if (list == null || list.size() == 0)
			return null;

		return new TemplateDefs(list);
	}


	/** 
	 * Retrieves a template by its name.
	 * @param name of the template to get
	 * @return The template
	 */
	public TemplateDef getTemplateByName(String rgKey, String name) 
			throws SQLException {

 		String attribs;
		StringBuffer from, where;

		if (rgKey == null || rgKey.equals("")) {
			rgKey = _regGroup;
		}

		attribs	= " * ";
		from	= new StringBuffer("template")
			.append(_regGroupSeparator).append(rgKey);
		where 	= new StringBuffer(" name='").append(name).append("' ");

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		return new TemplateDef(hash);
	}


	/** 
	 * Retrieves a template by its ID.
	 * @param tid of the template to get
	 * @return The template
	 */
	public TemplateDef getTemplateById(String rgKey, String tid) 
			throws SQLException {

 		String attribs;
		StringBuffer from, where;

		if (rgKey == null || rgKey.equals("")) {
			rgKey = _regGroup;
		}

		attribs	= " * ";
		from	= new StringBuffer("template")
			.append(_regGroupSeparator).append(rgKey);
		where 	= new StringBuffer(" template_id=")
			.append(tid);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from.toString(), where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		return new TemplateDef(hash);
	}


	/**
	 * Stores a template.
	 */
	public void storeTemplateDef(TemplateDef def)
			throws SQLException {
		storeTemplateDef(null, def);
	}

	public void storeTemplateDef(String rgKey, TemplateDef def)
			throws SQLException {

		if (rgKey == null || rgKey.equals("")) {
			rgKey = _regGroup;
		}

		StringBuffer insert = new StringBuffer("INSERT INTO template")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ");

		String tmp = insert.toString() + 
			def.getSqlFieldsAndValues();

		_db.insert(tmp);
	}


	/**
	 * Update a template.
	 */
	public void updateTemplateDef(String rgKey, IdDef origId, TemplateDef def)
			throws SQLException {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;
 
		StringBuffer update = new StringBuffer("UPDATE template")
				.append(_regGroupSeparator).append(rgKey)
				.append(" SET ");
		StringBuffer where = new StringBuffer(" WHERE template_id=")
			.append(origId.getId());
		StringBuffer tmp = new StringBuffer(update.toString())
			.append(def.getUpdateFieldsAndValues())
			.append(where.toString());

		debug("TR.updateTD: " + tmp.toString());
		_db.update(tmp.toString());
	}


	/**
	 * Stores a set of resources.
	 */
	public void storeTemplateDefs(TemplateDefs defs)
			throws SQLException {
		storeTemplateDefs(null, defs);
	}

	public void storeTemplateDefs(String rgKey, TemplateDefs defs)
			throws SQLException {

		if (rgKey == null || rgKey.equals("")) {
			rgKey = _regGroup;
		}


		StringBuffer insert = new StringBuffer("INSERT INTO template")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ");

		for (Iterator it=defs.iterator(); it.hasNext();) {
			TemplateDef def = (TemplateDef)it.next();
			String tmp = insert.toString() + 
				def.getSqlFieldsAndValues();

			_db.insert(tmp);
		}
	}


	/** 
	 * Returns all templates for a given registry group key.
	 *
	 */
	public TemplateDefs getAllTemplatesForRGKey(String rgKey) 
			throws SQLException, Exception {

		String attribs;
		StringBuffer from;
		StringBuffer where;

		attribs		= "*";
		from 		= new StringBuffer("template")
			.append(_regGroupSeparator).append(rgKey)
			.append(" ORDER BY name ASC ");

		List tdefs = _db.selectAsHash(attribs, from.toString(), null);

		return new TemplateDefs(tdefs);
	}


	/**
	 * Stores new resourcetemplates records given a list
	 * of role IDs to match with resource/template pairs.  
	 * This method will remove existing role IDs from
	 * the list if they would produce duplicate records.
	 *
	 * @param resName The name of the resource to use
	 * @param tmplId The ID of the template to use
	 * @param roleIds A list of role IDs to assign to 
	 *		the resource/template pair.
	 */
	public void assignTemplateRoles(String rgKey, String resName, 
			IdDef tmplId, List roleIds)
			throws Exception {

		if (rgKey == null || rgKey.equals(""))
			rgKey = _regGroup;

		//// QUERY /////////////////////////////
		// SELECT resource_id 
		// FROM resource
		// WHERE name='resName'
		////////////////////////////////////////
		StringBuffer from  = new StringBuffer("resource")
			.append(_regGroupSeparator).append(rgKey);
		StringBuffer where  = new StringBuffer("name='")
			.append(resName).append("'");

		Object o = _db.selectFirstAttrib("resource_id",
				from.toString(), where.toString());
		if (o == null) {
			throw new Exception("no resource_id for resource " +
					resName);
		}

		String resid = ((Integer)o).toString();
		String templateIdStr = tmplId.getId();


		// Remove would-be duplicates from the roleIds list
		// Get all existing role IDs for this r/t pair


		/////////////////////////////////////////////// 
		/////////////////////////////////////////////// 
		// Build the base delete string
		StringBuffer deleteStr = 
				new StringBuffer("DELETE FROM resourcetemplates")
				.append(_regGroupSeparator).append(rgKey)
				.append(" WHERE template_id=")
				.append(templateIdStr)
				.append(" AND resource_id=")
				.append(resid);

		// clear all existing records
	//debug("TR: deleting: " + deleteStr.toString());
			_db.update(deleteStr.toString());

		/////////////////////////////////////////////// 
		/////////////////////////////////////////////// 
		// Build the base insert string
		StringBuffer insertStr = 
				new StringBuffer("INSERT INTO resourcetemplates")
				.append(_regGroupSeparator).append(rgKey)
				.append(" (template_id,resource_id,role_id) VALUES (")
				.append(templateIdStr)
				.append(",")
				.append(resid)
				.append(",");

		/////////////////////////////////////////////// 
		/////////////////////////////////////////////// 
		// Now insert the records
		Iterator insertIt = roleIds.iterator();
		while (insertIt.hasNext()) {
			String roleId = (String)insertIt.next();
			StringBuffer tmpInsert = 
				new StringBuffer(insertStr.toString())
				.append(roleId)
				.append(")");

	//debug("TR: inserting: " + tmpInsert.toString());
			_db.insert(tmpInsert.toString());
		}
	}

	public void importXML(String xml, int operation) 
			throws IOException, SAXException, SQLException {
		if (_lw != null)
			_lw.logDebugMsg("TemplateRegistry.importXML()");

		SaxTemplateParser parser = new SaxTemplateParser(xml);
		TemplateDefs tdefs = parser.getTemplateDefs();

		if (operation == IRegistry.XML_ADD)
			storeTemplateDefs(tdefs);
		else {
			if (_lw != null) _lw.logDebugMsg(
				"TemplateRegistry.importXML(): " +
				"operation not implemented: " + operation);
		}
	}


}


