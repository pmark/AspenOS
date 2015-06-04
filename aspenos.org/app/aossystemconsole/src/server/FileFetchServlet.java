package org.aspenos.app.aossystemconsole.server;

import java.io.*; 
import java.util.*; 
import javax.servlet.http.*;
import javax.servlet.*; 

import org.aspenos.util.*;

public class FileFetchServlet extends HttpServlet {

  public void doGet (HttpServletRequest req,
		 HttpServletResponse res) 
	 	 throws ServletException, IOException {

    res.setContentType("text/html");
    PrintWriter pw = res.getWriter();

	String url = "http://64.77.39.2/aos/aspenos.properties";
	String logPath = "default.log";

    pw.println("Fetching properties file:<br><font color='#00FF00'>" + 
			url + "</font>");

	Properties props = new Properties();

	try {
		WebFileFetch fetcher = new WebFileFetch();
		
		//props.load(fetcher.doGetStream(url, null));
		props = fetcher.loadProperties(url, null, "get");

		StringBuffer tmp = new StringBuffer();
		String strProps = props.toString();

		strProps = strProps.substring(1,strProps.length()-1);
		StringTokenizer st = new StringTokenizer(strProps, ",=");

		tmp.append("<br><br><b>PROPERTIES:</b>\n<blockquote>");
		while (st.hasMoreTokens()) {
			tmp.append("\n")
				.append(st.nextToken())
				.append(" = <font color=\"#0000FF\">")
				.append(st.nextToken())
				.append("</font><br>");
		}
		tmp.append("</blockquote>");
		pw.println(tmp.toString());
	} catch (Exception ex) {
		pw.println("<br><font color='#FF0000'>Unable to get properties</font>");
	}

	pw.println("<br><br><br>");
	pw.println("<form action=\"\" method=\"GET\">\n");
	pw.println("<input type=\"submit\" name=\"delete\" value=\"delete log\">\n");
	pw.println("</form>\n");
	pw.println("<br>");

	// Delete the log file if the button was clicked
	if (req.getParameter("delete") != null) {
		try {
			File f = new File(logPath);
			if (f.exists()) {
				f.delete();
				if (f.exists())  {
					pw.println("<br><b>default.log was deleted but still " +
							"exists.</b><br><br>");
				} else
					pw.println("<br><b>default.log was deleted.</b><br><br>");
			} else {
				pw.println("<br><b>default.log did not exist.</b><br><br>");
			}

		} catch (Exception fex) {
			pw.println("<font color='#FF0000'>Unable to delete default.log</font>");
		}
	} 


	//// try to get the log file
	try {
		File f = new File(logPath);
		if (f.exists()) {
			BufferedReader bs = new BufferedReader(
					new FileReader(f));
			StringBuffer log = new StringBuffer();
			String line = null;
			while ((line=bs.readLine()) != null) {
				log.append(line);
				log.append("<br>");
			}

			pw.println("<b>default.log</b>:<br><br>");
			pw.println(log.toString());
		} else {
			pw.println("<b>ServerInit log does not exist.</b>");
		}
	} catch (Exception fex) {
		pw.println("<font color='#FF0000'>Unable to get default.log</font>");
	}

    pw.close();
  }

}
