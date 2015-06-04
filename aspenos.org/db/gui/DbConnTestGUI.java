/******************************************************
	FILE: DbConnTestGUI.java
	AUTHOR: P. Mark Anderson
	DATE: 12/98

	Displays a GUI for inputing SELECT statements and 
	viewing the results.  
*******************************************************/
package org.aspenos.db;

import java.awt.Event;
import java.awt.Frame;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;


public class DbConnTestGUI extends Frame 
{
	public static String DRIVER = "postgresql.Driver";
	//public static String DRIVER = "rst.sql.Driver";
	//public static String DRIVER = null;

    private DbTranslator db    = new DbTranslator();
    private List lstResults  = new List(5, false);
    private TextField dsn = new TextField(256);
    private TextField user  = new TextField(64);
    private TextField passwd = new TextField(64);
    private TextField table  = new TextField(64);
    private TextField attrib  = new TextField(64);
    private TextField where  = new TextField(64);

    public static void main(String args[]) throws Exception
    {    
		// Make sure to register the driver
		try 
		{ 
			if (args.length != 0)
				DRIVER = args[0];

			if (DRIVER != null)
			{
				System.out.println("Registering driver: " + DRIVER);
				Class.forName (DRIVER); 
			}
		}
		catch(Exception e) 
		{
			System.out.println("Error loading Postgres driver: " + 
					e.getMessage());
		}

		DbConnTestGUI app = new DbConnTestGUI("DbConnTestGUI Interface");
    }

    public DbConnTestGUI (String frameTitle) 
    {
		super(frameTitle);
		setBounds(200,100,420,500);
		setVisible(true);

		Panel north = new Panel();
		
		// Create the user interface
		north.setLayout(new GridLayout(0,1,0,0));

		if (DRIVER.toLowerCase().indexOf("odbc") != -1)
			dsn.setText("jdbc:odbc:");
		else {
			int pos = DRIVER.toLowerCase().indexOf(".");
			if (pos == -1)
				pos = DRIVER.length();
			String dbType = DRIVER.toLowerCase().substring(0,pos);
			dsn.setText("jdbc:" + dbType + "://");
		}
		user.setText("");
		passwd.setText("");
		attrib.setText("*");
		table.setText("");

		north.add(new Label("DSN", Label.LEFT));
		north.add(dsn);

		north.add(new Label("Username", Label.LEFT));
		north.add(user);

		north.add(new Label("Password", Label.LEFT));
		north.add(passwd);

		north.add(new Label("Select", Label.LEFT));
		north.add(attrib);

		north.add(new Label("From", Label.LEFT));
		north.add(table);

		north.add(new Label("Where", Label.LEFT));
		north.add(where);

		// Add the 3 main panels
		add(north, BorderLayout.NORTH);
		add(lstResults, BorderLayout.CENTER);
		add(new Button("Go!"), BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent event)
			{
				dispose();
				System.exit(0);
			}
		});	
    }

	// This method handles the button click
    public boolean action(Event evt, Object arg)
    {	
		if (evt.target instanceof Button)
		{
			// When the "Go!" button is pressed, the text fields
			// on the GUI are passed to the translator, which forms
			// a SELECT statement and runs it against the DB.
			try
			{
				java.util.List rs;
				java.util.List row;
				java.util.List columns;
						
				// Clear out the scrolling text list
				lstResults.removeAll();
				
				// Open the datasource
				db.open(dsn.getText(), user.getText(), passwd.getText());
				if (db.isClosed())
					return true;

				// Now add the attribute names to the top row
				columns = db.getColumnNames(table.getText());
				lstResults.addItem(columns.toString());
				System.out.println("***" + columns.toString());
				
				// Find the matching tuples
				rs = db.select(attrib.getText(), 
					table.getText(), where.getText());		
					
				// Update the scrolling text list
				for (int i=0; i < rs.size(); i++)
				{
					row = (java.util.List)rs.get(i);
					lstResults.addItem(row.toString());
				}
				System.out.println("done\n");
			}
			catch (Exception e)
			{
				System.out.println("Weird exception: ");
				e.printStackTrace();
			}
		}
		return true;
    }

    public Insets getInsets()
    {
		// This just makes the window look pretty
		return new Insets(35,15,15,15);
    }
}

