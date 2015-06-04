/******************************************************
	FILE: DbConnTest.java
	AUTHOR: P. Mark Anderson
*******************************************************/
package org.aspenos.db;

import java.util.*;


public class DbConnTest {
	public static final String DRIVER = "postgresql.Driver";

    private DbTranslator db = new DbTranslator();
    private String dsn = new String();
    private String user  = new String();
    private String passwd = new String();
    private String table  = new String();

    public static void main(String args[]) throws Exception {    
		System.out.println("\n\n");
		try { 
			if (DbConnTest.DRIVER != "") {
				System.out.println("Registering driver: " + 
				DbConnTest.DRIVER + "\n\n");
				Class.forName (DbConnTest.DRIVER); 

				Properties sysProps = System.getProperties();
				sysProps.put("jdbc.drivers", DbConnTest.DRIVER);
			}
		} catch(Exception e) {
			System.out.println("Error loading Postgres driver: " + 
					e.getMessage());
		}

		System.out.println("DbConnTest -- OPTIONS:\n" +
			"\n\tby itself:  DbConnTest" +
			"\n\twith a host:  DbConnTest <name of host>" +
			"\n\tall options:  DbConnTest <DSN> <user> <pwd> <table>"+
			"\n\n\tUnspecified host goes to \"localhost\"" +
			"\n\tUnspecified DB goes to \"testdb\"" +
			"\n\tUnspecified table goes to \"table1\"\n\n");

		DbConnTest app;
		if (args.length == 1)
			app = new DbConnTest(args[0]);
		else if (args.length > 1)
			app = new DbConnTest(args[0], args[1], args[2], args[3]);
		else if (args.length == 0)
			app = new DbConnTest("localhost");
    }

    public DbConnTest (String host) {
		dsn = "jdbc:postgresql://" + host + "/testdb";
		user = "postgres";
		passwd = "postgres";
		table = "table1";

		System.out.println("Running test..." +
				"\n\tDSN    = " + dsn +
				"\n\tUSER   = " + user +
				"\n\tPWD    = " + passwd +
				"\n\tTABLE  = " + table);

		go();
    }

    public DbConnTest (String dsn, String user, String passwd, 
			String table) {
		this.dsn = dsn; 
		this.user = user; 
		this.passwd = passwd; 
		this.table = table; 

		System.out.println("Running test..." +
				"\n\tDSN    = " + dsn +
				"\n\tUSER   = " + user +
				"\n\tPWD    = " + passwd +
				"\n\tTABLE  = " + table);

		go();
    }



    public void go() {	
		try {
			ArrayList rs = new ArrayList();
					
			// Open the datasource
			db.open(dsn, user, passwd);
			if (db.isClosed())
				return;

			rs = (ArrayList)db.selectAll(table);		

			System.out.println("--- QUERY IS DONE ---");
				
			for (int i=0; i < rs.size(); i++) {
				ArrayList record = (ArrayList)rs.get(i);

				System.out.println("\nRecord #" + i);
				for (int j=0; j < record.size(); j++) {
					Object value = record.get(j);
					System.out.println("*** " + value.getClass() +
							"\t*** " + value.toString());
				}
			}
		} catch (Exception e) {
			System.out.println("\n\nWeird exception: " + e.getMessage());
		}
    }

}

