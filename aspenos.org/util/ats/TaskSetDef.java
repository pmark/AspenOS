package org.aspenos.util.ats;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * Represents one task set, which is set of
 * of scheduled tasks.  The schedule applies
 * to each task.  The order of tasks is kept
 * intact and the TaskRunner does execute
 * the tasks in the specified order, waiting
 * for each prior task to complete before 
 * running the next task.
 * 
 * @author P. Mark Anderson
 */
public class TaskSetDef extends IdDef {

	private static final String DEFS = "taskdefs";

	public TaskSetDef() {
		super();
		setDefName("TaskSet");
	}


	public TaskSetDef(Map m) {
		super(m, "TaskSet"); 
	}

	public void setTaskDefs(TaskDefs taskDefs) {
		this.setProperty(DEFS, taskDefs);
	}

	public TaskDefs getTaskDefs() {
		return (TaskDefs)this.getProperty(DEFS);
	}


	/**
	 *
	 */
	public boolean isHappeningWithinPeriod(long period) {

		String syear = (String)getProperty("year");		
		String smonth = (String)getProperty("month");		
		String sdate = (String)getProperty("date");		
		String shour = (String)getProperty("hour");		
		String sminute = (String)getProperty("minute");		
		String sactive = (String)getProperty("active");		

		List monthList, dateList, hourList, minuteList;

		boolean multiDate=false;
		int year, month, date, hour, minute, active;

		// check if active now
		if (sactive == null) active = 1;
		else active = Integer.parseInt(sactive);
		if (active == 0) return false;

		// get the year
		if (syear == null || syear.equals("*")) year = -1;
		else year = Integer.parseInt(syear);

		// multiple months may be specified
		if (smonth == null || smonth.equals("*")) 
			monthList = null;
		else if (hasMultiValue(smonth)) 
			monthList = parseMultiValue(smonth);
		else {
			monthList = new ArrayList();
			monthList.add(smonth);
		}


		// multiple days may be specified
		if (sdate == null || sdate.equals("*")) {
			dateList = null;
		} else if (hasMultiValue(sdate)) {
			dateList = parseMultiValue(sdate);
		} else {
			dateList = new ArrayList();
			dateList.add(sdate);
		}


		// multiple hours may be specified
		if (shour == null || shour.equals("*")) 
			hourList = null;
		else if (hasMultiValue(shour)) 
			hourList = parseMultiValue(shour);
		else {
			hourList = new ArrayList();
			hourList.add(shour);
		}


		// multiple minutes may be specified
		if (sminute == null || sminute.equals("*")) 
			minuteList = null;
		else if (hasMultiValue(sminute)) 
			minuteList = parseMultiValue(sminute);
		else {
			minuteList = new ArrayList();
			minuteList.add(sminute);
		}

		return checkPeriod(period, year, monthList, dateList, hourList, minuteList);

	}

	private boolean checkPeriod(long period, int year, List monthList, 
			List dateList, List hourList, List minuteList) {

		// set up the current date range
		GregorianCalendar now = new GregorianCalendar();
		now.set(Calendar.SECOND, 0);  // reset to 00 seconds
		long millisNow = now.getTime().getTime();
		int month, date, hour, minute;
		Date dNow = new Date(millisNow);
		Date dLater = new Date(millisNow+period);

		// Use the current time for any unspecified fields
		if (year 	== -1) year = now.get(Calendar.YEAR);

		if (monthList == null) {
			month = now.get(Calendar.MONTH)+1;
			monthList = new ArrayList();
			monthList.add(Integer.toString(month));
		}

		if (dateList == null) {
			date = now.get(Calendar.DATE);
			dateList = new ArrayList();
			dateList.add(Integer.toString(date));
		}

		if (hourList == null) {
			hour = now.get(Calendar.HOUR_OF_DAY);
			hourList = new ArrayList();
			hourList.add(Integer.toString(hour));
		}

		if (minuteList == null) {
			minute = now.get(Calendar.MINUTE);
			minuteList = new ArrayList();
			minuteList.add(Integer.toString(minute));
		}


		Iterator monit;
		Iterator dit;
		Iterator hit;
		Iterator mit;
		GregorianCalendar gc=null;
		Date dTask;
		long millisTask;
		boolean isInPeriod=false;

		// handle the month list
		monit = monthList.iterator();
		while (monit.hasNext()) {
			month = Integer.parseInt((String)monit.next());
			month--;   // GregorianCal uses zero based months

			// handle the date list
			dit = dateList.iterator();
			while (dit.hasNext()) {
				date = Integer.parseInt((String)dit.next());

				// handle the hour list
				hit = hourList.iterator();
				while (hit.hasNext()) {
					hour = Integer.parseInt((String)hit.next());

					// handle the minute
					mit = minuteList.iterator();
					while (mit.hasNext()) {
						minute = Integer.parseInt((String)mit.next());

						// finally compare this date to the current period
						gc = new GregorianCalendar(year,month,date,hour,minute);
						millisTask = gc.getTime().getTime();
						dTask = new Date(millisTask + 1000);  // add one second
						isInPeriod = (!dTask.before(dNow) && !dTask.after(dLater));
						if (isInPeriod) return true;
					}
				}
			}
		}

		return false;
	}


	private boolean hasMultiValue(String val) {
		return (val.indexOf("-") != -1 || val.indexOf(",") != -1);
	}



	/**
	 * Returns a List of Strings with each value.
	 */
	public List parseMultiValue(String val) {

		ArrayList allList=new ArrayList();
		String dashLeft, rangeItem, commaLeft="";
		int rangeBegin=0, rangeEnd, i;
		boolean firstDash=false;
		StringTokenizer comma;


		StringTokenizer dash = new StringTokenizer(val, "-");
		while (dash.hasMoreTokens()) {

			// next dash token may contain CSV
			dashLeft = dash.nextToken();

			// get the first CSV token in this dash token
			// it is the end of a range, or the beginning of 
			// the whole list if firstDash
			comma = new StringTokenizer(dashLeft, ",");
			if (comma.hasMoreTokens()) {
				commaLeft = comma.nextToken();
				rangeEnd = Integer.parseInt(commaLeft);
			} else {
				rangeEnd=0;
			}

			if (firstDash) {
				allList.add(commaLeft);

			} else {

				for (i=rangeBegin+1; i<=rangeEnd; i++) {
					rangeItem = Integer.toString(i);
					allList.add(rangeItem);
				}
			}

			// add the CSVs, if there are any
			// or just add the range beginning
			while (comma.hasMoreTokens()) {
				commaLeft = comma.nextToken();
				allList.add(commaLeft);
			}

			rangeBegin = Integer.parseInt(commaLeft);
			firstDash = false;

		}

		return allList;
	}


	public static void main(String[] args) {

		TaskSetDef t = new TaskSetDef();
		List l;
		
		l = t.parseMultiValue("1,2,3,4-8,9-10-15,16");
		System.out.println("\n\nFINAL: " + l.toString());
	}

}
