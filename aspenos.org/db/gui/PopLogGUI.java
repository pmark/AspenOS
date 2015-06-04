/******************************************************
	FILE: PopLogGUI.java
	AUTHOR: P Mark Anderson
	DATE: 1/2000

	Creates SQL log files that have a bunch of INSERT
	statements that reflect the population of DB tables.
*******************************************************/
package org.aspenos.db;

import java.awt.Event;
import java.awt.Frame;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;


public class PopLogGUI extends Frame 
{
	//public static final String DRIVER = "postgresql.Driver";
	//public static final String DRIVER = "rst.sql.Driver";
	public static final String DRIVER = "";

    private DbTranslator db    = new DbTranslator();
    private List lstResults  = new List(5, false);
    private TextField dsn = new TextField(256);
    private TextField user  = new TextField(64);
    private TextField passwd = new TextField(64);
    private TextField table  = new TextField(64);
    private TextField sqlLog  = new TextField(64);
    private TextField blankDate  = new TextField(64);

    public static void main(String args[]) throws Exception
    {    
		// Make sure to register the driver
		try 
		{ 
			if (DRIVER != "")
			{
				System.out.println("Registering driver: " + DRIVER);
				Class.forName (DRIVER); 
			}
		}
		catch(Exception e) 
		{
			System.err.println("Error loading Postgres driver: " + e.getMessage());
		}

		PopLogGUI app = new PopLogGUI("PopLogGUI Interface");
    }

    public PopLogGUI (String frameTitle) 
    {
		super(frameTitle);
		setBounds(200,100,420,500);
		setVisible(true);

		Panel north = new Panel();
		
		// Create the user interface
		north.setLayout(new GridLayout(0,1,0,0));

		dsn.setText("jdbc:postgresql://localhost/irdb");
		user.setText("postgres");
		passwd.setText("");
		sqlLog.setText("/tmp/poplog.sql");
		table.setText("users");
		blankDate.setText("01-01-0001");

		north.add(new Label("DSN", Label.LEFT));
		north.add(dsn);

		north.add(new Label("Username", Label.LEFT));
		north.add(user);

		north.add(new Label("Password", Label.LEFT));
		north.add(passwd);

		north.add(new Label("SQL log", Label.LEFT));
		north.add(sqlLog);

		north.add(new Label("From table", Label.LEFT));
		north.add(table);

		north.add(new Label("Blank dates will be", Label.LEFT));
		north.add(blankDate);

		// Add the 3 main panels
		add(north, BorderLayout.NORTH);
		add(lstResults, BorderLayout.CENTER);
		add(new Button("Create SQL Log"), BorderLayout.SOUTH);

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
				String attribs, values;
				Vector rs = new Vector();
				Vector row = new Vector();
						
				// Clear out the scrolling text list
				lstResults.removeAll();
				
				// Open the datasource
				db.open(dsn.getText(), user.getText(), passwd.getText());
				if (!db.isOpen())
				{
					System.err.println("\nUnable to open DB!\n");
					return true;
				}

				// Now add the attribute names to the top row
				row = db.getAttribs(table.getText());
				attribs = row.toString();
				lstResults.addItem(attribs);
				attribs = attribs.substring(1,attribs.length()-1);
				
				// Find the matching tuples
				rs = db.find("*", table.getText(), "");		
					
				// Update the scrolling text list
				if (rs != null)
				{
					for (int i=0; i < rs.size(); i++)
					{
						row = (Vector)rs.elementAt(i);
						if (row != null)
						{
							values = cleanUp(row.toString());

							System.out.println("INSERT INTO " + table.getText() +
									" VALUES (" + values + ");");
							lstResults.addItem(values);
						}
						else
							lstResults.addItem("Row #" + i + " was NULL.");
					}
				}
				else if (rs.size() == 0)
					lstResults.addItem("No records were found.");
				else
					lstResults.addItem("db.find() returned NULL.");

			}
			catch (Exception e)
			{
				System.err.println("Wierd exception: " + e.getMessage());
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

	private String cleanUp(String str)
	{
		char ch;
		String rv = "";
		String val = "";

		str = "'" + str.substring(1, str.length()-1);

		val = "" + str.charAt(0);
		for (int i=1; i<str.length(); i++)
		{
			ch = str.charAt(i);
			if (ch == ',')
			{
				if (val.equals("null"))
				{
					rv = rv.substring(0, rv.length()-1);
					rv += val;
				}
				else
					rv += val + "'";

				if (i < str.length()-1)
					rv += ", '";
				val = "";
			}
			else if (!str.substring(i-1, i+1).equals(", "))
				val += ch;
		}

		return rv + "'";
	}
}

