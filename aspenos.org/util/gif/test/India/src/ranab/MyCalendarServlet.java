package ranab;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import ranab.tpl.*;


public class MyCalendarServlet extends MyBaseServlet {
  
  
  private static MyTemplate CAL_TPL = null; 
  
  
  private static final String[] MONTH_NAME =  {
  	"January", "February", "March",     "April",   "May",      "June",
	"July",    "August",   "September", "October", "November", "December"
  };
  
  /**
   * initialize vector from the data file
   */
  // Directory name is hard coded. It is not a very good idea.
  // Better approach is to get the name from servlet initparam
  // or get the real path from the <code>ServletContext</code> object.
  public void init(ServletConfig config) throws ServletException {
      super.init(config);
	  CAL_TPL = new MyTemplate(new File(getDocRoot(config), "template/cal.txt"));
  } 
  
  /**
   * serve request
   */
  public void service(HttpServletRequest request, HttpServletResponse response)
    			throws IOException, ServletException {
  	
	 response.setContentType("text/html");
	 OutputStream out = response.getOutputStream();	 
	 
  	 int month = getParam(request, "month");
     int year = getParam(request, "year");
	 
	 // error checking
	 String errStr = "";
	 if ((month < 0) || (month > 11))  {
	 	errStr += "Not proper month data.<br>";
	 }
	 if (year < 0)  {
	 	errStr += "Not proper year data.<br>";
	 }
	 if (!errStr.equals(""))  {
     	showErrorPage(out, errStr);
     	out.close();
		return;
	 }
	
	 // populate hashtable
	 Hashtable dataHash = new Hashtable();
	 dataHash.put("year", new Integer(year));
	 dataHash.put("month", new Integer(month));
	 dataHash.put("data", getCalendarData(month, year));
	 dataHash.put("monthStr", MONTH_NAME[month]);
	 dataHash.put("nextYear", new Integer(year+1));
	 dataHash.put("prevYear", new Integer(year-1));
	 dataHash.put("prevMonth", new Integer((month+11)%12));
	 dataHash.put("nextMonth", new Integer((month+01)%12));
	 
	 loadFile(CAL_TPL, out, dataHash);
	 out.close();
  }
  
  private Vector getCalendarData(int month, int year)  {
  	 
	 // initialize vector
	 Vector monthData = new Vector(7);
	 for(int row=0; row<6; row++)  {
	 	Vector weekVect = new Vector(6);
		monthData.add(weekVect);
		for(int col=0; col<7; col++)  {
			weekVect.add("&nbsp;");
		}		
	 }
	 
	 // populate actual data
	 GregorianCalendar cal = new GregorianCalendar(year, month, 1);
	 int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	 for(int i=1; i<=totalDays; i++) {
      	cal.set(Calendar.DATE, i);
      	int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
      	int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
      	
		int col = dayOfWeek - 1;
		int row = weekOfMonth - 1;
		
		Vector weekVect = (Vector)monthData.get(row);
		weekVect.set(col, new Integer(i));
     }
	 return monthData;
  }
  
  
  private int getParam(HttpServletRequest req, String key)  {
  	String val = req.getParameter(key);
	
	if ( (val == null) || (val.trim().equals("")) )  {
		return -1;
	}
	
	try  {
		return Integer.parseInt(val);
	}
	catch(NumberFormatException ex)  {
	}
	return -1;
  }
 
 public static void main(String args[])  {
 	MyCalendarServlet ser = new MyCalendarServlet();
	ser.getCalendarData(0, 2000);
 }
  
}