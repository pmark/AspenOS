package org.aspenos.app.aossystemconsole.server;

import java.io.*; 
import java.sql.*; 
import java.util.*; 
import javax.servlet.http.*;
import javax.servlet.*; 

import org.aspenos.util.*;
import org.aspenos.db.*;
import org.aspenos.app.aoscontentserver.util.*;

public class DbTestServlet extends HttpServlet {

	protected boolean _initDone = false;

	DbConnectionPool _dbPoolCS = null;
	DbConnectionPool _dbPoolConsole = null;

	protected String _dbDriver = "postgresql.Driver";
	protected String _csDSN = "jdbc:postgresql://localhost/aoscs";
	protected String _consoleDSN = "jdbc:postgresql://localhost/aosconsole";
	protected String _user = "postgres";
	protected String _pwd = "vitagreen5";

	public void init(ServletConfig sc) throws ServletException {
		super.init(sc);

		try {
			ServerInit.log("DBTS: registering DB driver: " +
					_dbDriver);

			ServerInit.log("DBTS: creating pool for: " +
					_csDSN);
			_dbPoolCS = new DbConnectionPool(null, _csDSN, 
					_user, _pwd, 
					_dbDriver,1,3);

			ServerInit.log("DBTS: creating pool for: " +
					_consoleDSN);
			_dbPoolConsole =
				new DbConnectionPool(null, _consoleDSN, 
						_user, _pwd, 
						_dbDriver,1,3);

			ServerInit.log("DBTS: DONE setting up DB pool");
		} catch (Exception ex) {
			ServerInit.log(
					"DBTS: unable to create db connection pool: " +
					ex.toString());
			ServletException se = (ServletException)ex
				.fillInStackTrace();
			throw se;
		}

		_initDone = true;
	}


	public void doGet (HttpServletRequest req,
			HttpServletResponse res) 
			throws ServletException, IOException {

		res.setContentType("text/html");
		PrintWriter pw = res.getWriter();

		ServerInit.log("DBTS: doGet starting");

		try {

			String csData = runCSTest();
			pw.println("CS DB data: " + csData);

			String consoleData = runConsoleTest();
			pw.println("Console DB data: " + consoleData);



		} catch (Exception ex) {
			ServerInit.log(
					"DBTS: problem in doGet: " +
					ex.toString());
			ServletException se = (ServletException)ex
				.fillInStackTrace();
			throw se;
		}

		pw.println("<br><br>Looks like it worked");
		pw.close();
	}

	protected String runCSTest() 
			throws SQLException {
		DbPersistence conn = _dbPoolCS.getConnection();

		String str = "no change";

		ServerInit.log("DBTS: Testing CS");
		ServerInit.log("DBTS: getting column names");
		List columns = conn.getColumnNames("principal");
		str = columns.toString();
		ServerInit.log("DBTS: column names: " + str);
		str += "<br><br><font color='#00FF00'>";

		ServerInit.log("DBTS: talking to DB");
		str += (String)conn.selectFirstAttrib("username", "principal","");
		ServerInit.log("DBTS: ***** DB access successful!");
		str += "</font><br><br>";
			
		_dbPoolCS.returnConnection(conn);
		return str;
	}

	protected String runConsoleTest() 
			throws SQLException {
		DbPersistence conn = _dbPoolConsole.getConnection();

		String str = "no change";

		ServerInit.log("DBTS: Testing CONSOLE");
		ServerInit.log("DBTS: getting column names");
		List columns = conn.getColumnNames("app");
		str = columns.toString();
		ServerInit.log("DBTS: column names: " + str);
		str += "<br><br><font color='#00FF00'>";

		ServerInit.log("DBTS: talking to DB");
		str += (String)conn.selectFirstAttrib("display_name", "vendor","");
		ServerInit.log("DBTS: ***** DB access successful!");
		str += "</font><br><br>";

		_dbPoolConsole.returnConnection(conn);
		return str;
	}
}
