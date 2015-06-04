package org.aspenos.util;

import java.io.*;
import java.util.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class GeneralDef extends IdDef {

	protected Map _insDefaults  = new HashMap();
	protected List _dbFields = new ArrayList();
	protected List _excludes = new ArrayList();

	public GeneralDef(Map props) {
		super(props);
	}

	public GeneralDef() {
		super();
	}



	/**
	 * Builds the fields and values part of a SQL insert statement.  
	 * The DB fields and at least one value must be set.
	 * Null values are treated as follows:
	 *    If the field is an excluded field, it is skipped.
	 *    Else if there is a default value, that value is used.
	 *    If field is not excluded and the default value is not 
	 *    present or is null, the DB value for null is inserted 
	 *    (not the string 'null').
	 */
	public String getInsertSQLFandV() {
		if (_properties == null || _dbFields == null)
			return null;

		StringBuffer insert = new StringBuffer(200);
		StringBuffer sqlFields = new StringBuffer(100);
		StringBuffer sqlValues = new StringBuffer(100);
		String field, value;
		boolean first = true;

		// for each DB field
		Iterator fit = _dbFields.iterator();
		while (fit.hasNext()) {

			// get the field name and its value
			field = (String)fit.next();
			value = (String)_properties.get(field);

			// if the value is NOT null OR
			// if this field is NOT excluded if null
			// then add it to the fields SQL
			if (value != null || !_excludes.contains(field)) {
				if (first) {
					first = false;
				} else {
					sqlFields.append(",");
					sqlValues.append(",");
				}

				sqlFields.append(field);

				// get a default value if null
				if (value == null && _insDefaults != null) {
					value = (String)_insDefaults.get(field);
				}

				// if the value is still null, insert the DB's null value
				if (value == null)
					sqlValues.append("null");
				else
					sqlValues.append("'").append(value).append("'");
			} 
		}

		insert.append("(")
			.append(sqlFields.toString())
			.append(") VALUES (")
			.append(sqlValues.toString())
			.append(")");

		return insert.toString();
	}


	/**
	 * A convenience method that builds a complete
	 * SQL INSERT string.  
	 *
	 * @param table table to insert with values
	 */
	public String getInsertSQL(String table) {
		StringBuffer insert = new StringBuffer(200);
		insert.append("INSERT INTO ")
			.append(table)
			.append(" ")
			.append(getInsertSQLFandV());
		return insert.toString();
	}


	/**
	 * Builds the comma separated field = value pairs part 
	 * of a SQL update statement.  
	 * The DB fields and at least one value must be set.
	 * Null values are treated as follows:
	 *    If the field is an excluded field, it is skipped.
	 *    DEFAULT VALUES ARE NEVER USED IN UPDATES.
	 *    If field is not excluded and the value is null, 
	 *    the DB value for null is inserted 
	 *    (not the string 'null').
	 */
	public String getUpdateSQLFandV() {
		if (_properties == null || _dbFields == null)
			return null;

		StringBuffer update = new StringBuffer(200);
		StringBuffer sqlPairs = new StringBuffer(100);
		Iterator fit = _dbFields.iterator();
		String field, value;
		boolean first = true;

		// for each DB field
		while (fit.hasNext()) {

			// get the field name and its value
			field = (String)fit.next();
			value = (String)_properties.get(field);

			// if the value is NOT null OR
			// if this field is NOT excluded if null
			// then add it to the fields SQL
			if (value != null || !_excludes.contains(field)) {
				if (first) {
					first = false;
				} else {
					sqlPairs.append(",");
				}

				sqlPairs.append(field).append("=");

				if (value == null)
					sqlPairs.append("null");
				else
					sqlPairs.append("'").append(value).append("'");
			} 
		}

		update.append(sqlPairs.toString());
		return update.toString();
	}


	/**
	 * A convenience method that builds a complete
	 * SQL UPDATE string.  Remember to add a WHERE
	 * clause!
	 *
	 * @param table table to update with values
	 */
	public String getUpdateSQL(String table) {
		StringBuffer update = new StringBuffer(200);
		update.append("UPDATE ")
			.append(table)
			.append(" SET ")
			.append(getUpdateSQLFandV());
		return update.toString();
	}


	/**
	 *
	 */
	public void setDbFields(List dbFields) {
		_dbFields = dbFields;
	}


	/**
	 * Sets default values to be used for building INSERT.
	 */
	public void setInsertDefaults(Map insDefaults) {
		_insDefaults = insDefaults;
	}


	/**
	 * If there is no value for the exclude fields
	 * they are not included in the field list.
	 * This is good for IDs.  DB fields with
	 * default values (like timestamps) should
	 * NOT be excluded.
	 */
	public void setExcludeFields(List excludes) {
		_excludes = excludes;
	}



	public static void main(String args[]) {

		HashMap props = new HashMap();
		props.put("first", "f");
		props.put("last", "l");
		//props.put("email", "e");

		HashMap insDefaults = new HashMap();
		//insDefaults.put("email", "defEmail");

		ArrayList dbFields = new ArrayList();
		dbFields.add("subscriber_id");
		dbFields.add("first");
		dbFields.add("last");
		dbFields.add("email");

		ArrayList excludes = new ArrayList();
		excludes.add("subscriber_id");

		GeneralDef def = new GeneralDef(props);
		def.setDbFields(dbFields);
		def.setInsertDefaults(insDefaults);
		def.setExcludeFields(excludes);
		
		System.out.println(def.getInsertSQL("subscriber"));
		System.out.println(def.getUpdateSQL("subscriber"));
	}

}
