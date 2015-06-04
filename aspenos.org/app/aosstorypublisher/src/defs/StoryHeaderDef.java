package org.aspenos.app.aosstorypublisher.defs;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;
import org.aspenos.app.aosstorypublisher.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class StoryHeaderDef extends IdDef 
		implements IStoryPublisherConstants {

	public StoryHeaderDef() {
		super();
		setDefName("StoryHeader");
	}


	public StoryHeaderDef(Map m) {
		super(m, "StoryHeader"); 
	}


	public String getSqlFieldsAndValues() {
		StringBuffer sb = new StringBuffer();

		String story_id = getId();
		if (story_id == null || story_id.equals("null")) {
			story_id = (String)getProperty("story_id");
		}

		String file_name = (String)getProperty("file_name");
		String title = (String)getProperty("title");
		String site = (String)getProperty("site");
		String locale = (String)getProperty("locale");
		String section = (String)getProperty("section");
		String category = (String)getProperty("category");
		String story_date = (String)getProperty("story_date");
		String pub_start_date = (String)getProperty("pub_start_date");
		String pub_end_date = (String)getProperty("pub_end_date");
		String pub_now = (String)getProperty("pub_now");

		StringBuffer fields = new StringBuffer()
			.append("file_name,title,site,locale,section,category,pub_now) VALUES (");

		sb.append("(");


		// check the locale
		if (locale == null || locale.equals(""))
			locale = DEFAULT_LOCALE;

		// check the dates
		if (pub_now == null || !pub_now.toLowerCase().startsWith("t"))
			pub_now = "false";
		else
			pub_now = "true";

		boolean useStoryDate = true;
		boolean useStartDate = true;
		boolean useEndDate = true;
		StringBuffer tmp = null;

		// story date
		if (story_date == null || story_date.equals("")) {
			useStoryDate = false;
		} else {
			tmp = new StringBuffer("story_date,")
				.append(fields);
			fields = tmp;
		}

		// start date
		if (pub_start_date == null || pub_start_date.equals("")) {
			useStartDate = false;
		} else {
			tmp = new StringBuffer("pub_start_date,")
				.append(fields);
			fields = tmp;
		}

		// end date
		if (pub_end_date == null || pub_end_date.equals("")) {
			useEndDate = false;
		} else {
			tmp = new StringBuffer("pub_end_date,")
				.append(fields);
			fields = tmp;
		}


		// Skip the ID if it's null
		if (story_id == null || story_id.equals("null")) {
			sb.append(fields.toString()).append("'");

		} else {
			sb.append("story_id,")
				.append(fields.toString())
				.append(story_id)
				.append(",'");
		}

		if (useEndDate)
			sb.append(pub_end_date).append("','");
		if (useStartDate)
			sb.append(pub_start_date).append("','");
		if (useStoryDate)
			sb.append(story_date).append("','");

		sb.append(file_name)
			.append("','")
			.append(title)
			.append("','")
			.append(site)
			.append("','")
			.append(locale)
			.append("','")
			.append(section)
			.append("','")
			.append(category)
			.append("','")
			.append(pub_now)
			.append("') ");

		return sb.toString();
	}


	public String getSqlUpdateFandV() {
		StringBuffer sb = new StringBuffer();

		String story_id = getId();
		if (story_id == null || story_id.equals("null")) {
			story_id = (String)getProperty("story_id");
		}

		String file_name = (String)getProperty("file_name");
		String title = (String)getProperty("title");
		String site = (String)getProperty("site");
		String locale = (String)getProperty("locale");
		String section = (String)getProperty("section");
		String category = (String)getProperty("category");
		String story_date = (String)getProperty("story_date");
		String pub_start_date = (String)getProperty("pub_start_date");
		String pub_end_date = (String)getProperty("pub_end_date");
		String pub_now = (String)getProperty("pub_now");


		if (pub_now == null || !pub_now.toLowerCase().startsWith("t"))
			pub_now = "false";
		else
			pub_now = "true";


		// Skip the ID if it's null
		if (story_id != null || !story_id.equals("null") ||
					!story_id.equals("")) {
			sb.append("story_id=").append(story_id).append(",");

		} 


		// story date
		if (story_date != null && !story_date.equals(""))
			sb.append("story_date='").append(story_date).append("',");

		// start date
		sb.append("pub_start_date=");
		if (pub_start_date == null || pub_start_date.equals(""))
			sb.append("null,");
		else
			sb.append("'").append(pub_start_date).append("',");

		// end date
		sb.append("pub_end_date=");
		if (pub_end_date == null || pub_end_date.equals("")) 
			sb.append("null,");
		else
			sb.append("'").append(pub_end_date).append("',");


		sb.append("file_name='").append(file_name).append("',")
			.append("title='").append(title).append("',")
			.append("site='").append(site).append("',")
			.append("locale='").append(locale).append("',")
			.append("section='").append(section).append("',")
			.append("category='").append(category).append("',")
			.append("pub_now=").append(pub_now);

		return sb.toString();
	}

}
