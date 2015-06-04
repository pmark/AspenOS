package ranab;

import java.io.*;
import java.awt.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import ranab.img.*;


public
class MyClockServlet extends MyBaseServlet  {
	
	// angles
	public final static double PI = 3.1415926; 
	public final static double TICK_ANGLE = 2*PI/12.0;
	
	// clock graphical parameters
	public final static int CLOCK_SIZE = 200;
	public final static int BORD_WIDTH = 2;
	public final static int TICK_SIZE  = 7;
	
	public final static Color BG_COLOR   = Color.white;
	public final static Color BORD_COLOR = Color.cyan;
	public final static Color TICK_COLOR = Color.black;
	public final static Color HOUR_COLOR = Color.blue;
	public final static Color MIN_COLOR  = Color.yellow;
	public final static Color SEC_COLOR  = Color.red;
	
	
	// initialize servlet
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	
	/**
	 * serve request
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
    	throws IOException, ServletException {
    	
		int hour = getInteger(request, "hour", 0)%12;
		int min = getInteger(request, "minute", 0)%60;
		int sec = getInteger(request, "second", 0)%60;
		
		MyGifImage img = getClockImage(hour, min, sec);
        response.setContentType(img.getMimeType());
        OutputStream os = response.getOutputStream();
        img.encode(os);
        os.close();
	}
	
	/**
	 * get integer param 
	 */
	private int getInteger(HttpServletRequest request, String str, int defaultInt)  {
		
		String val = request.getParameter(str);
		if ( (val == null) || val.trim().equals(""))  {
			return defaultInt;
		}
		
		try  {
			return Integer.parseInt(val);
		}
		catch(NumberFormatException ex)  {
			return defaultInt;
		}
	}
	
	/**
	 * get the clock image
	 */ 
	private MyGifImage getClockImage(int hour, int min, int sec)  {
		
		MyGifImage img = new MyGifImage(CLOCK_SIZE, CLOCK_SIZE);
		
		Graphics2D g = img.getGraphics();
		g.translate(CLOCK_SIZE/2, CLOCK_SIZE/2);
		
		drawBackground(g);
		drawBorder(g);
		drawTicks(g);
		drawHour(g, hour, min, sec);
		drawMinute(g, min, sec);
		drawSecond(g, sec);
		return img;
	}
	
	// draw background
	private void drawBackground(Graphics2D g)  {
		g.setColor(BG_COLOR);
		g.fillRect(-CLOCK_SIZE/2, -CLOCK_SIZE/2, CLOCK_SIZE, CLOCK_SIZE);
	}
	
	// draw border
	private void drawBorder(Graphics2D g)  {
		g.setColor(BORD_COLOR);
		g.fillOval(-CLOCK_SIZE/2, -CLOCK_SIZE/2, CLOCK_SIZE, CLOCK_SIZE);
		
		int top = -CLOCK_SIZE/2 + BORD_WIDTH;
		int size = CLOCK_SIZE - 2*BORD_WIDTH;
		g.setColor(BG_COLOR);
		g.fillOval(top, top, size, size);
	}
	
	// draw ticks
	private void drawTicks(Graphics2D g)  {
		g.setColor(TICK_COLOR);
		int finalSize = (CLOCK_SIZE/2) - BORD_WIDTH;
		
		double angle = 0;
		for(int i=0; i<12; i++)  {
			g.drawLine(0, -finalSize, 0, -(finalSize-TICK_SIZE));
			g.rotate(TICK_ANGLE);
			angle += TICK_ANGLE;
		}
		g.rotate(-angle);
	}
	
	// draw second
	private void drawSecond(Graphics2D g, int sec)  {
		double angle = 2*PI*sec/60.0;
		int width = ((CLOCK_SIZE/2) - BORD_WIDTH)*7/8;
		g.setColor(SEC_COLOR);
		g.rotate(angle);
		g.drawLine(0, 0, 0, -width);
		g.rotate(-angle);
	}
	
	// draw minute
	private void drawMinute(Graphics2D g, int min, int sec)  {
		double angle = 2*PI*min/60.0 + TICK_ANGLE*sec/(5*60.0);
		int width = ((CLOCK_SIZE/2) - BORD_WIDTH)*6/7;
		g.setColor(MIN_COLOR);
		g.rotate(angle);
		
		int xpoints[] =  {
			0,
			-width/20,
			0,
			width/20,
			0
		};
		
		int ypoints[] =  {
			0,
			-width*2/3,
			-width,
			-width*2/3,
			0
		};
		
		g.fill(new Polygon(xpoints, ypoints, xpoints.length));
		g.rotate(-angle);
	}
	
	// draw hour
	private void drawHour(Graphics2D g, int hour, int min, int sec)  {
		double angle = 2*PI*hour/12.0 + TICK_ANGLE*min/60.0 + TICK_ANGLE*sec/(60.0*60.0);
		int width = ((CLOCK_SIZE/2) - BORD_WIDTH)*5/8;
		g.setColor(HOUR_COLOR);
		g.rotate(angle);
		
		int xpoints[] =  {
			0,
			-width/10,
			0,
			width/10,
			0
		};
		
		int ypoints[] =  {
			0,
			-width*2/3,
			-width,
			-width*2/3,
			0		
		};
		g.fill(new Polygon(xpoints, ypoints, xpoints.length));
		g.rotate(-angle);
	}

}