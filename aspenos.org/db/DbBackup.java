/********************************************************
	FILE: DbBackup.java
	AUTHOR: P. Mark Anderson
	DATE: 4/2000
********************************************************/

package org.aspenos.db;

import java.sql.*;
import java.util.*;


public class DbBackup {


	private DbPersistence _db = null;
	private boolean _useSemi = true;

	/**
	 *
	 */
	public DbBackup(DbPersistence db) {
		_db = db;
	}

	/**
	 *
	 */
	public String backupTable(String table, String fields)
			throws SQLException {
		return backupTable(table, fields, null, null);
	}
	
	/**
	 *
	 */
	public String backupTable(String table, String fields, 
			String ignore, String where) 
			throws SQLException {
		StringBuffer bak = new StringBuffer();
		StringBuffer tmp;
		StringBuffer line;



		// build the SQL selection query
		line = new StringBuffer("SELECT "); 
		line.append(fields);
		line.append(" FROM ");
		line.append(table);
		if (where != null) {
			if (where.length() > 0) {
				line.append(" WHERE ");
				line.append(where);
			}
		}


		// make sure 'fields' is formed correctly
		if (fields.equals("*")) {
			List vec = _db.getColumnNames( table );
			Iterator attribs = vec.iterator(); 

			tmp = new StringBuffer();
			while (attribs.hasNext()) {
				if (tmp.length() > 0)
					tmp.append(", ");
				tmp.append("\"");
				tmp.append( (String)attribs.next() );
				tmp.append("\"");
			}

			fields = tmp.toString();
		}  // Done getting all field names from DB


		// remove any 'ignore' fields
		if (ignore != null && ignore.length() > 0) {
			Vector allFields = new Vector();
			Vector ignoreFields = new Vector();
			StringTokenizer stFields, stIgnore;
			String next;
			
			stIgnore = new StringTokenizer( ignore, "," );
			while (stIgnore.hasMoreTokens()) {
				tmp = new StringBuffer("\"");
				tmp.append((String)stIgnore.nextToken());
				tmp.append("\"");
				ignoreFields.addElement ( tmp );
			}

			stFields = new StringTokenizer( fields, "," );
			fields = new String();
			while (stFields.hasMoreTokens()) {
				next = (String)stFields.nextToken();
				if (!ignoreFields.contains( next )) {
					if (fields.length() > 0)
						fields += ", ";
					fields += next;
					//allFields.addElement ( next );  // Old style!
				}
			}
		}  // Done removing ignore fields

		if (fields.length() < 1)
			return "";

		// get the data 
		List values = _db.select(fields, table, where);


		// build the base SQL string
		line = new StringBuffer("INSERT INTO ");
		line.append(table);
		line.append(" (");
		line.append(fields);
		line.append(") VALUES ("); 

		Iterator e = values.iterator();
		while (e.hasNext()) {
			tmp = new StringBuffer(line.toString());
			Iterator e2 = ((List)e.next()).iterator();
			while (e2.hasNext()) {
				if (tmp.length() > line.length())
					tmp.append(", ");
				tmp.append("\"");
				tmp.append( e2.next().toString() );
				tmp.append("\"");
			}
			tmp.append(")");
			if (_useSemi)
				tmp.append(";");
			tmp.append("\n");

			bak.append( tmp.toString() );
		}

		return bak.toString();
	}
	

	/**
	 *
	 */
	public String createSQLBackupLog(String[] tableNames) 
			throws SQLException {
		StringBuffer bak = new StringBuffer();

		for (int i=0; i < tableNames.length; i++) {
			if (tableNames[i] != null) {
				bak.append( backupTable( tableNames[i], "*", null, null ));
				bak.append("\n");
			}
		}

		return bak.toString();
	}


	/**
	 *
	 */
	public String createSQLBackupLog(DataSelection[] ds) 
			throws SQLException {
		StringBuffer bak = new StringBuffer();

		for (int i=0; i < ds.length; i++) {
			if (ds[i] != null) {
				bak.append( backupTable( ds[i].getTable(), ds[i].getFields(), 
						ds[i].getIgnore(), ds[i].getWhere() ));
				bak.append("\n");
			}
		}

		return bak.toString();
	}


	/**
	 *
	 */
/*
	public String createSQLBackupLog() 
		throws SQLException {
		String tableNames[];

		// Get all tables in _db

		createSQLBackupLog(tableNames, "*");
	}
*/

	/**
	 *
	 */
	public void setUseSemi(boolean b) {
		_useSemi = b;
	}



	public static void main(String[] args) {
		try {
			Class.forName("postgresql.Driver");	
			Properties p = System.getProperties();
			p.put("jdbc.drivers", "postgresql.Driver");
		}
		catch (Exception e) {
			System.err.println("Problem registering DB driver");
			e.printStackTrace();
		}


		DbTranslator db = null;
		try { 
			db = new DbTranslator();
			String dsn = "jdbc:postgresql://127.0.0.1/returnalert";
			String user = "postgres";
			String pwd = "postgres";
		
			db.open(dsn, user, pwd);
		}
		catch (Exception e) {
			System.err.println("Problem opening DB");
			e.printStackTrace();
		}


		try {
			DbBackup dbb = new DbBackup( db );
			String log;
			String tables[] = new String[10];
			tables[0] = "recent";
			tables[1] = "today";
			tables[2] = "settings";
			tables[3] = "inbox";
			//tables[4] = "ccalertdata";
			log = dbb.createSQLBackupLog( tables );


			//DataSelection ds[] = new DataSelection[2];
			//ds[0] = new DataSelection("ccalertdata", 
			//	"data_id,company,senddate", "senddate", "data_id > 899");
			//log = dbb.createSQLBackupLog( ds );


			//String log = dbb.backupTable( "recent", "recent_id", 
			//		"data_id", null );


			System.out.println("\n\n" + log + "\n\n");
		}
		catch (Exception e) {
			System.err.println("Problem creating SQL log");
			e.printStackTrace();
		}



	}

}



