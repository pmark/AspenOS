package ranab;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import ranab.tpl.*;

public
class MyBaseServlet extends HttpServlet  {
	
  // Here the document root is hard coded.
  // It is not a very good idea. The better approach
  // is to use servlet init params or get it from the 
  // servlet context.
  private static File DOC_ROOT = new File("D:/jakarta/jakarta-tomcat/webapps/ROOT/~ranab");
  
  
  private static MyTemplate PROBLEM_TPL = null;
  private static MyTemplate ERROR_TPL = null;
  	
	
  public void init(ServletConfig config) throws ServletException {
  	  super.init(config);
	  File docRoot = getDocRoot();
	  PROBLEM_TPL = new MyTemplate(new File(docRoot, "template/problem.txt"));
  	  ERROR_TPL = new MyTemplate(new File(docRoot, "template/error.txt"));
  }

  // dummay methods
  protected File getDocRoot()  {
  	return DOC_ROOT;
  }
  
  protected File getDocRoot(ServletConfig cfg)  {
  	return getDocRoot();
  }
  
  protected File getDocRoot(ServletContext ctx)  {
  	return getDocRoot();
  }
   
  
  // load template file with error correction
  protected void loadFile(MyTemplate tpl, OutputStream out, Map hash)  {
  	try  {
		tpl.loadFile(out, hash);
  	}
	catch(Exception ex)  {
		showProblemPage(out, ex);
	}
  }
  
  // show error page
  protected void showErrorPage(OutputStream out, String msg)  {
  	try  {	
		Hashtable hash = new Hashtable();
		hash.put("message", msg);
		ERROR_TPL.loadFile(out, hash);
  	}
	catch(Exception ex)  {
	}
  }	
 
  // display problem page
  protected void showProblemPage(OutputStream out, String msg)  {
  	try  {
  		Hashtable hash = new Hashtable();
		hash.put("message", msg);
		PROBLEM_TPL.loadFile(out, hash);
	}
	catch(Exception ex)  {
	}
  }
 
  // display problem page with exception stack trace
  protected void showProblemPage(OutputStream out, Exception ex)  {
  	try  {
  		Hashtable hash = new Hashtable();
		hash.put("message", getStackTrace(ex));
		PROBLEM_TPL.loadFile(out, hash);
	}
	catch(Exception e)  {
	}
  }
 
  // get exception stack trace	
  public static String getStackTrace(Exception ex)  {
  	String result = "";
	try  {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.close();
		sw.close();
		result = sw.toString();
	}
	catch(Exception e)  {
	}
	return result;
  } 	
  
}
