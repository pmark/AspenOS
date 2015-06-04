package org.aspenos.util;

import java.util.*;

public class DateTool
{
	public static String _divider = "/";


	/**
	 * Return today's date.
	 * @param iso9000 yyyy/mm/dd if true
	 **/
	public static String getDateToday(boolean iso9000)
	{
		GregorianCalendar gc = new GregorianCalendar();

		int year = gc.get(Calendar.YEAR);
		int month = (gc.get(Calendar.MONTH)+1);
		int date = gc.get(Calendar.DATE);

		String strYear = Integer.toString(year);
		String strMonth = Integer.toString(month);
		String strDate = Integer.toString(date);

		if (month < 10)
			strMonth = "0" + month;
		if (date < 10)
			strDate = "0" + date;


		StringBuffer theDate = new StringBuffer();
		if (iso9000) {
			theDate.append(strYear)
				.append(_divider)
				.append(strMonth)
				.append(_divider)
				.append(strDate);
		} else {
			theDate.append(strMonth)
				.append(_divider)
				.append(strDate)
				.append(_divider)
				.append(strYear);
		}

		return theDate.toString();
	}

	/**
	 * Return today's date as a yyyy/mm/dd formatted string.
	 **/
	public static String getDateToday()
	{
		return getDateToday(false);
	}

	/**
	 * Return today's date and time as a 
	 * yyyy/mm/dd hh:mm:ss  formatted String
	 **/
	public static String getDateTime()
	{
		GregorianCalendar gc = new GregorianCalendar();
		StringBuffer time = new StringBuffer();
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int min = gc.get(Calendar.MINUTE);
		int sec = gc.get(Calendar.SECOND);

		if (hour < 10)
			time.append("0");
		time.append(hour);
		time.append(":");

		if (min < 10)
			time.append("0");
		time.append(min);
		time.append(":");

		if (sec < 10)
			time.append("0");
		time.append(sec);

		StringBuffer theDate = new StringBuffer()
			.append(gc.get(Calendar.YEAR))
			.append(_divider)
			.append((gc.get(Calendar.MONTH)+1))
			.append(_divider)
			.append(gc.get(Calendar.DATE))
			.append(" ")
			.append(time.toString());

		return theDate.toString();
	}

	/**
	 * Return time as a hh:mm:ss  formatted String
	 **/
	public static String getCurrentTime()
	{
		GregorianCalendar gc = new GregorianCalendar();
		StringBuffer time = new StringBuffer();
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int min = gc.get(Calendar.MINUTE);
		int sec = gc.get(Calendar.SECOND);

		if (hour < 10)
			time.append("0");
		time.append(hour);
		time.append(":");

		if (min < 10)
			time.append("0");
		time.append(min);
		time.append(":");

		if (sec < 10)
			time.append("0");
		time.append(sec);

		return time.toString();
	}

	/**
	 * @return A vector with 0) Correctly formatted string
	 *				1) GregorianCalendar set to correct time
	 **/
	public static Vector parseDate(String d)
	{
		GregorianCalendar gc;
		StringTokenizer str;
		String strDate;
		int y, mon, day;
		Vector v = new Vector();

		if (d != null)
		{
			str = new StringTokenizer(d, "-");
			y = Integer.parseInt(str.nextToken());  // Y
			mon = Integer.parseInt(str.nextToken());  // M
			day = Integer.parseInt(str.nextToken());  // D
			strDate = y + "-" + mon + "-" + day;
			gc = new GregorianCalendar(y, mon-1, day);
		}
		else
		{
			// Use default 'not used'  date
			gc = new GregorianCalendar(1,1,1);
			strDate = "1-1-1";
		}

		v.addElement(strDate);
		v.addElement(gc);

		return v;
	}

	public static void setDivider(String newdiv)
	{
		_divider = newdiv;
	}
}
