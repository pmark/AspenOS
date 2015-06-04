package org.aspenos.app.aoscontentserver.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.registry.*;
import org.aspenos.app.aoscontentserver.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class ERTDef extends IdDef {

	public ERTDef() {
		super();
		setDefName("ERT");
	}


	public ERTDef(Map m) {
		super(m, "ERT"); 
	}

	public String toXML() {

		StringBuffer sb = new StringBuffer();

		String regGroup = (String)getProperty("registry_group");
		sb.append("<ERTDef registry_group=\"")
			.append(regGroup)
			.append("\">\n");

		WebEventDefs wedefs = (WebEventDefs)getProperty("webevent_defs");
		if (wedefs == null)
			wedefs = new WebEventDefs();
		Iterator weit = wedefs.iterator();
		while (weit.hasNext()) {

			WebEventDef wed = (WebEventDef)weit.next();
			sb.append("  <WebEventDef id=\"")
				.append(wed.getId())
				.append("\" name=\"")
				.append((String)wed.getProperty("name"))
				.append("\">\n");

			ResourceDefs rdefs = (ResourceDefs)wed.getProperty("resource_defs");
			if (rdefs == null)
				rdefs = new ResourceDefs();
			Iterator rit = rdefs.iterator();
			while (rit.hasNext()) {
				ResourceDef rd = (ResourceDef)rit.next();
				sb.append("    <ResourceDef id=\"")
					.append(rd.getId())
					.append("\" name=\"")
					.append((String)rd.getProperty("name"))
					.append("\">\n");

				TemplateDefs tdefs = (TemplateDefs)rd.getProperty("template_defs");
				if (tdefs == null)
					tdefs = new TemplateDefs();
				Iterator tit = tdefs.iterator();
				while (tit.hasNext()) {
					TemplateDef td = (TemplateDef)tit.next();
					sb.append("      <TemplateDef id=\"")
						.append(td.getId())
						.append("\" name=\"")
						.append((String)td.getProperty("name"))
						.append("\">\n");

					RoleDefs roledefs = (RoleDefs)td.getProperty("role_defs");
					if (roledefs == null)
						roledefs = new RoleDefs();
					Iterator roleit = roledefs.iterator();
					while (roleit.hasNext()) {
						RoleDef role = (RoleDef)roleit.next();
						sb.append("        <RoleDef id=\"")
							.append((String)role.getProperty("id"))
							.append("\" name=\"")
							.append((String)role.getProperty("name"))
							.append("\"/>\n");
					}
					sb.append("      </TemplateDef>\n");
				}
				sb.append("    </ResourceDef>\n");
			}
			sb.append("  </WebEventDef>\n");
		}
		sb.append("</ERTDef>\n");

		return sb.toString();
	}


	public String getRegGroupName() {
		return (String)getProperty("registry_group");
	}


	/**
	 * webeventresources
	 */
	public List getSqlWERFieldsAndValues() {
		// get the web event defs
		// for each wedef,
		// get webevent id
		// get resource defs
		// for each rdef,
		// get resource id
		// get ordinal
		// create SQL with weid, rid, ordinal
		// add SQL to list
		
		List list = new ArrayList();
		String fields = " (webevent_id,resource_id,ordinal) VALUES ('";

		WebEventDefs wedefs = (WebEventDefs)getProperty("webevent_defs");
		Iterator weit = wedefs.iterator();
		while (weit.hasNext()) {

			WebEventDef wed = (WebEventDef)weit.next();
			String weid = wed.getId();

			ResourceDefs rdefs = (ResourceDefs)wed.getProperty("resource_defs");
			Iterator rit = rdefs.iterator();
			while (rit.hasNext()) {
				ResourceDef rd = (ResourceDef)rit.next();
				String resid = rd.getId();
				String ordinal = (String)rd.getProperty("ordinal");
				if (ordinal == null)
					ordinal = "";

				// Build the SQL
				StringBuffer values = new StringBuffer(weid)
					.append("','")
					.append(resid)
					.append("','")
					.append(ordinal)
					.append("') ");

				StringBuffer sql = new StringBuffer(fields)
					.append(values.toString());

				list.add(sql.toString());
			}
		}

		return list;
	}


	/**
	 * resourcetemplates
	 */
	public List getSqlRTFieldsAndValues() {
		// get the web event defs
		// for each wedef,
		// get resource defs
		// for each rdef,
		// get resource id
		// get template defs
		// for each tdef,
		// get template id
		// get role id
		// create SQL with tid, resid, roleid
		// add SQL to list
		
		List list = new ArrayList();
		String fields = " (template_id,resource_id,role_id) VALUES (";

		WebEventDefs wedefs = (WebEventDefs)getProperty("webevent_defs");
		if (wedefs == null)
			wedefs = new WebEventDefs();
		Iterator weit = wedefs.iterator();
		while (weit.hasNext()) {

			WebEventDef wed = (WebEventDef)weit.next();

			ResourceDefs rdefs = (ResourceDefs)wed.getProperty("resource_defs");
			if (rdefs == null)
				rdefs = new ResourceDefs();
			Iterator rit = rdefs.iterator();
			while (rit.hasNext()) {
				ResourceDef rd = (ResourceDef)rit.next();
				String resid = rd.getId();
				if (resid == null || resid.equals("null"))
					resid = ((Integer)rd.getProperty("resource_id")).toString();

				TemplateDefs tdefs = (TemplateDefs)rd.getProperty("template_defs");
				if (tdefs == null)
					tdefs = new TemplateDefs();
				Iterator tit = tdefs.iterator();
				while (tit.hasNext()) {
					TemplateDef td = (TemplateDef)tit.next();
					String tid = td.getId();
					if (tid == null || tid.equals("null"))
						tid = ((Integer)td.getProperty("template_id")).toString();

					RoleDefs roledefs = (RoleDefs)td.getProperty("role_defs");
					if (roledefs == null)
						roledefs = new RoleDefs();
					Iterator roleit = roledefs.iterator();
					while (roleit.hasNext()) {
						RoleDef role = (RoleDef)roleit.next();

						String roleid = role.getId();
						if (roleid == null || roleid.equals("null"))
							roleid = ((Integer)
									role.getProperty("role_id")).toString();

						// Build the SQL
						StringBuffer values = new StringBuffer(tid)
							.append(",")
							.append(resid)
							.append(",")
							.append(roleid)
							.append(") ");

						StringBuffer sql = new StringBuffer(fields)
							.append(values.toString());

						list.add(sql.toString());
					}
				}
			}
		}

		return list;
	}


}


