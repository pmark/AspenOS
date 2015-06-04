package org.aspenos.app.aosstorypublisher.registry;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.sql.*;

import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aosstorypublisher.defs.*;
import org.aspenos.app.aosstorypublisher.util.*;

/**
 *
 */
public class StoryRegistry extends StoryPublisherRegistry 
		implements IStoryPublisherConstants {


	public StoryRegistry() {
	}

	public StoryRegistry(DbPersistence db) {
		_db = db;
	}


	// Primary Methods =============================================
	/** 
	 * Gets all stories that were created between the 
	 * two given date/times.
	 */
	public StoryHeaderDefs getStoryList(String site, String section,
			Calendar after, Calendar before)
			throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM story
		//  WHERE  story_date > after
		//  AND  story_date < before
		//  AND  site = 'site'
		//  AND  section = 'section'
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "story";
		StringBuffer where = new StringBuffer("site='")
			.append(site).append("' AND section='")
			.append(section).append("' ");

		if (after != null)
			where.append("AND story_date > '")
				.append(after.toString()).append("'");

		if (before != null)
			where.append("AND story_date < '")
				.append(before.toString()).append("'");

		where.append(" ORDER BY story_date DESC");

		List l = _db.selectAsHash(attribs, from, where.toString());
		StoryHeaderDefs defs = new StoryHeaderDefs(l);

		return defs;
	}


	/** 
	 * Gets a story headed given its ID.
	 */
	public StoryHeaderDef getStoryHeader(String story_id)
			throws SQLException {

		//////// Sample query ///////////////////////////////////////
		//  SELECT * FROM story
		//  WHERE  story_id=given ID
		/////////////////////////////////////////////////////////////

		String attribs = "*";
		String from = "story";
		StringBuffer where = new StringBuffer("story_id=")
			.append(story_id);

		HashMap hash = (HashMap)
			_db.selectFirstAsHash(attribs, from, where.toString());

		if (hash == null || hash.size() == 0)
			return null;

		StoryHeaderDef def = new StoryHeaderDef(hash);
		String id = ((Integer)def.getProperty("story_id")).toString();
		def.setId(id);

		return def;
	}


	/** 
	 * Gets a story given its ID.
	 */
	public StoryDef getStory(String story_id)
			throws SQLException, IOException {

		StoryHeaderDef header = getStoryHeader(story_id);

		// read the body from the file
		File storyFile = getBodyFile(header);
		String body = readBodyFile(storyFile);

		StoryDef story = new StoryDef();
		story.setHeader(header);
		story.setProperty("body", body);
		return story;
	}


	/** 
	 * Chages a story's section.
	 */
	public synchronized void changeSection(
			String story_id, String section) 
			throws SQLException {

		// update the database
		StringBuffer update = new StringBuffer()
			.append("UPDATE story SET section='")
			.append(section).append("' WHERE story_id=")
			.append(story_id);
		_db.insert(update.toString());
	}


	/**
	 *
	 */
	public long getNextStoryId() throws SQLException {
		Number n = (Number)
			_db.selectFirstAttrib("(max(story_id)+1)", "story", "");
		return n.longValue();
	}


	/** 
	 * Store a story.
	 * The synchronized portion is synchronized
	 * because the story ID mustn't 
	 * be incremented after it is used to generate the 
	 * new file name.
	 */
	public void storeStory(StoryDef story) 
			throws SQLException, IOException {

		String insert = "INSERT INTO story ";

		synchronized (this) {
			long nextStoryId = getNextStoryId();

			// create a new file name
			String file_name = Long.toString(nextStoryId, RADIX);
			story.getHeader().setProperty("file_name", file_name);

			//debug("New story: " + nextStoryId + ":  " + file_name);

			// insert the story header into the DB
			String fandv = story.getSqlFieldsAndValues();
			String tmp = insert + fandv;
			//debug("\n\nSR.insert: " + tmp);
			_db.insert(tmp);
		}

		// write the body to a file
		String body = (String)story.getProperty("body");
		File storyFile = getBodyFile(story.getHeader());
		writeBodyFile(storyFile, body);

	}


	/** 
	 * Delete a story.
	 */
	public synchronized void deleteStory(String story_id) 
			throws SQLException, IOException {

		StoryHeaderDef header = getStoryHeader(story_id);
		File storyFile = getBodyFile(header);

		// delete the file
		if (storyFile.exists())
			storyFile.delete();

		// delete the story from the DB
		String del = "DELETE FROM story WHERE story_id=" +
			story_id;
		_db.update(del);

	}


	/** 
	 * Update a story.
	 */
	public synchronized void updateStory(String orig_id, StoryDef story) 
			throws SQLException, IOException {

		// write the body to a file
		String body = (String)story.getProperty("body");
		File storyFile = getBodyFile(story.getHeader());
		writeBodyFile(storyFile, body);

		// update the story header in the DB
		String update = "UPDATE story SET ";
		String fandv = story.getSqlUpdateFandV();
		StringBuffer sb = new StringBuffer(update)
			.append(fandv);
		if (orig_id != null && !orig_id.equals("")) {
			sb.append(" WHERE story_id=").append(orig_id);
		}

		String tmp = sb.toString();
		//debug("\n\nSR.update: " + tmp);
		_db.update(tmp);
	}




	/// TEMP ///////////////////////////////////
	/** 
	 * Utility method that gets the 'file_name' (was body) field of each
	 * record and stores it in its own file in a directory
	 * that is the same name as the section.
	 */
	public synchronized void dumpBodiesToFiles() throws Exception {

		//////// Sample query ///////////////////////////////////////
		//  SELECT story_id,file_name FROM story
		/////////////////////////////////////////////////////////////

		String attribs = "story_id,file_name,section";

		String from = "story";

		List l = _db.selectAsHash(attribs, from, "");

		String baseDir = "/tmp/telski.com/en_us/";
		String section, file_name, body;

		File f = new File(baseDir);
		// don't let anybody overwrite the data, that would
		// REALLY suck.
		if (f.exists())
			return;

		Iterator it = l.iterator();
		while (it.hasNext()) {
			HashMap hash = (HashMap)it.next();

			// get the story data from the DB rec
			Integer story_id = (Integer)hash.get("story_id");
			section = (String)hash.get("section");
			body = (String)hash.get("file_name");

			// make the filename
			file_name = Long.toString(story_id.longValue(), RADIX);

			String path = baseDir;

			File dir = new File(path);
			if (!dir.exists())
				dir.mkdirs();
			
			// add the filename
			path += file_name;

			File storyFile = new File(path);
			writeBodyFile(storyFile, body);

			StringBuffer update = new StringBuffer()
				.append("UPDATE ")
				.append(from)
				.append(" SET file_name='")
				.append(file_name)
				.append("' WHERE story_id=")
				.append(story_id);

			_db.update(update.toString());
		}

	}


	/////////////////////////////////////////////////////////

	/**
	 * 
	 */
	private String readBodyFile(File storyFile) 
			throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(
			new FileReader(storyFile));

		String line;
		while ((line=in.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}


	/**
	 * 
	 */
	private void writeBodyFile(File storyFile, String body) 
			throws IOException {
		PrintWriter out = new PrintWriter(
				new BufferedWriter(
					new FileWriter(storyFile)));

		out.print(body);
		out.flush();
		out.close();
	}


	/////////////////////////////////////////////////////////
	public static File getBodyFile(StoryHeaderDef header) {
		return new File(getBodyPath(header));
	}


	public static String getBodyPath(StoryHeaderDef header) {
		String site = (String)header.getProperty("site");
		String locale = (String)header.getProperty("locale");
		String file_name = (String)header.getProperty("file_name");

		if (locale == null || locale.equals("")) {
			locale = DEFAULT_LOCALE;
			header.setProperty("locale", locale);
		}

		StringBuffer file_path = new StringBuffer(AOS_STORY_DIR)
			.append(site)
			.append(File.separator)
			.append(locale)
			.append(File.separator)
			.append(file_name);
		return file_path.toString();
	}


}
