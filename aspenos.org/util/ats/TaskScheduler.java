package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.logging.*;
import org.aspenos.util.*;
import org.aspenos.app.aoscontentserver.util.*;
import org.aspenos.app.aoscontentserver.server.*;

/**
 * AspenOS Task Scheduler (ATS).
 *
 * Reads XML task set files in <AOS ROOT DIR>/tasks which
 * represent tasks to be run on a schedule.
 *
 * Here is a sample config file.  Units are in minutes.
 * <AOS ROOT>/tasks/ats.properties 
		ats.new_period = 1
		ats.batch_period = 60
		ats.handler_period = 1
 *
 * Definitions:
 *    New Period = 
 *       The interval of time between the installation of
 *       new task sets.  Task sets are stored in the
 *       cache by their file name, so installing a file
 *       with the same name as one already in the cache
 *       will replace the existing (old) one.
 *
 *    Batch Period = 
 *       Task sets which occur in the next time period
 *       of this length will be cached and checked
 *       more often.  The purpose of the batch period is 
 *       purely for efficiency.  Consider the caching of
 *       all task sets in the 'tasks' directory, and it
 *       should be apparent that the cache could be very,
 *       very large.  It is only necessary to cache task 
 *       sets which occur soon.
 *
 *    Handler Period = 
 *       Task sets in the cache which occur between the
 *       current time and the next handler period minutes
 *       will be handled by the TaskRunner.  Cached task set
 *       schedules are checked every handler period minutes.
 *
 */
public class TaskScheduler extends EventHandlerServlet {

	///// CLASS CONSTANTS //////////////////
	public static String _aosHome = null;
	static {
		try { _aosHome = ServerInit.getAspenHomeDir();
		} catch (Exception ex) { _aosHome = "/opt/aspenos/"; }
	}
	public static final String TASK_DIR = 
		_aosHome + "tasks" + File.separator;

	public static final String NEW_DIR = 
		TASK_DIR + "new" + File.separator;

	public static final String OLD_DIR = 
		TASK_DIR + "old" + File.separator;

	private static final String PROPS_PATH = 
		TASK_DIR + "ats.properties";

	private static final long DEF_HANDLER_PERIOD = 1;
	private static final long DEF_BATCH_PERIOD = 60;
	private static final long DEF_INSTALL_PERIOD = 3;
	private static final int  PERIOD_SCALAR = 60000;

	private static final int NO_ACTION = 0;
	private static final int REWRITE_FILE = 1;
	private static final int DELETE_TASKSET = 2;

	private static final String DEF_DEBUG_LOG = TASK_DIR +
		"ats.log";
	private static final String DEF_MSG_LOG = TASK_DIR +
		"ats.log";
	private static final String DEF_ERR_LOG = TASK_DIR +
		"ats.log";
	private static final boolean DO_DEBUG = true;

	public static final long MILLIS_PER_MINUTE = 60 * 1000;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
	public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;
	public static final long MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY;
	public static final long MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY;

	///// CLASS VARIABLES //////////////////
	//private static TaskScheduler _instance = new TaskScheduler();
	private static TaskScheduler _instance = null;
	private static Hashtable _curTaskSetFiles = null;

	private static TaskHandlerTimer _handlerTimer = null;
	private static TaskBatchTimer _newBatchTimer = null;
	private static TaskInstallTimer _installTimer = null;
	private static LoggerWrapper _lw = null;
	private static PropLoader _pl = null;
	private long _handlerPeriod;
	private long _batchPeriod;
	private long _installPeriod;

	static {
		//System.out.println("STATIC: getInstance");
		getInstance();
	}


	///// CONSTRUCTION /////////////////////
	/**
	 * Uses the Xerces parser.
	 */
	private TaskScheduler() {

		System.setProperty("sax.parser.class",
				"org.apache.xerces.parsers.SAXParser");
		System.setProperty("sax.parser.validating",
				"false");

		setLogger(null);
		checkDirs();
		loadProps();
		_lw.logDebugMsg("Handler: " + _handlerPeriod);
		_handlerTimer = new TaskHandlerTimer(_handlerPeriod);
		_lw.logDebugMsg("Batch: " + _batchPeriod);
		_newBatchTimer = new TaskBatchTimer(_batchPeriod);
		_lw.logDebugMsg("Installer: " + _installPeriod);
		_installTimer = new TaskInstallTimer(_installPeriod);
		_curTaskSetFiles = new Hashtable();

		getNewBatch();
		installNewTasks();
		processCurrentBatch();
	}


	/**
	 * 
	 */
	public static TaskScheduler getInstance() {
		if (_instance == null) {
			_instance = new TaskScheduler();
		}
		return _instance;
	}



	///// PRIMARIES ////////////////////////////
	/**
	 * 
	 */
	public void setLogger(LoggerWrapper lw) {
		if (_lw == null && lw == null) {
			Logger logger = new Logger();
			logger.setDebugLog(DEF_DEBUG_LOG);
			logger.setMsgLog(DEF_MSG_LOG);
			logger.setErrLog(DEF_ERR_LOG);
			logger.setDoDebug(DO_DEBUG);
			_lw = new LoggerWrapper(logger);
		}
		else {
			_lw = lw;
		}
	}


	/**
	 * Returns a hash with values for year, month, date,
	 * hour, and minute that are offset by the given values.
	 * Caveat:  Since the length of months and years can change,
	 * this method approximates offsets that make use of the month
	 * and/or year.  Months are approximated to be 30 days long
	 * and years are 365 days.  
	 *
	 * An example of the problem is if offMonth = 1, the current
	 * date is 25, and the current month is 31 days long, the 
	 * resultant month's date will be 24 instead of 25 since all 
	 * months are approximated as 30 days long.
	 *
	 * Just think of the month and year offset values as multipliers
	 * of 30 and 365 days added on to the current date.
	 */ 
	public Map getTimeOffset(int offYear, int offMonth, int offDate,
			int offHour, int offMinute) {

		Map hash = new HashMap();
		GregorianCalendar gc = new GregorianCalendar();
		Date d = new Date();
		long curMillis = d.getTime();
		long offMillis = curMillis;

		offMillis += MILLIS_PER_YEAR 	* offYear;
		offMillis += MILLIS_PER_MONTH 	* offMonth;
		offMillis += MILLIS_PER_DAY 	* offDate;
		offMillis += MILLIS_PER_HOUR 	* offHour;
		offMillis += MILLIS_PER_MINUTE	* offMinute;

		d = new Date(offMillis);
		gc.setTime(d);

		int year = gc.get(Calendar.YEAR);
		int month = gc.get(Calendar.MONTH)+1;
		int date = gc.get(Calendar.DAY_OF_MONTH);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int minute = gc.get(Calendar.MINUTE);

		hash.put("year", Integer.toString(year));
		hash.put("month", Integer.toString(month));
		hash.put("date", Integer.toString(date));
		hash.put("hour", Integer.toString(hour));
		hash.put("minute", Integer.toString(minute));

		return hash;
	}


	/**
	 * Creates or overwrites a file with the given name
	 * and contents in the 'new' task directory.  The 
	 * new task sets will be handled at the next check
	 * of the 'new' directory.
	 */
	public void installXML(String fileName, String xml)
			throws IOException {

		if (!fileName.toLowerCase().endsWith(".xml"))
			fileName += ".xml";

		PrintWriter out = new PrintWriter(
				new FileWriter(NEW_DIR + fileName, false), true);
		out.println(xml);
		out.close();
	}


	/**
	 * Removes the task set from the current batch
	 * and optionally deletes the task set file.
	 */
	public void uninstallFile(String fileName, boolean delFile) {
		// take it out of memory
		_curTaskSetFiles.remove(fileName);

		if (delFile) {
			File f = new File(TaskScheduler.TASK_DIR + fileName);
			if (f.exists()) {
				_lw.logDebugMsg("TS: Deleting taskset file: " + fileName);
				f.delete();
			}
		}
	}


	/**
	 *
	 */
	private void checkDirs() {
		File f = new File(TASK_DIR);
		if (!f.exists())
			f.mkdir();
		f = new File(NEW_DIR);
		if (!f.exists())
			f.mkdir();
		f = new File(OLD_DIR);
		if (!f.exists())
			f.mkdir();
	}


	/**
	 *
	 */
	private void loadProps() {
		_pl = new PropLoader();
		try {
			_pl.load(PROPS_PATH);
			_handlerPeriod 	= _pl.getLong("ats.handler_period",DEF_HANDLER_PERIOD);
			_batchPeriod 	= _pl.getLong("ats.batch_period",DEF_BATCH_PERIOD);
			_installPeriod 	= _pl.getLong("ats.new_period",DEF_INSTALL_PERIOD);
		} catch (Exception ex) {
			_lw.logErr("Can't load properties from " + PROPS_PATH);
			_handlerPeriod 	= DEF_HANDLER_PERIOD;
			_batchPeriod 	= DEF_BATCH_PERIOD;
			_installPeriod 	= DEF_INSTALL_PERIOD;
		}

		// The batch period should be something like 60
		// while the handler period could be 1 and it
		// doesn't make sense that the batch is less
		// than the handler.
		if (_batchPeriod < _handlerPeriod)
			_batchPeriod = _handlerPeriod;

		_handlerPeriod 	*= PERIOD_SCALAR;
		_batchPeriod 	*= PERIOD_SCALAR;
		_installPeriod 	*= PERIOD_SCALAR;
	}


	/**
	 * Adds tasks that are scheduled to occur within the next
	 * batch period to the current cache.  Reads every file
	 * in the ATS task dir that ends with the .xml extension.
	 */
	private void getNewBatch() {
		//_lw.logDebugMsg("TS: Caching the next " + getBatchPeriod() + " minute batch");

		Hashtable taskSetFiles = getTaskSetFiles(TASK_DIR);
		TaskSetDef taskSetDef = null;
		int addCount=0;

		// For each file, get the TaskSetDefs
		TaskSetDefs taskSetDefs;
		String fileName;
		Iterator fit = taskSetFiles.keySet().iterator();
		while (fit.hasNext()) {

			try {
				fileName = (String)fit.next();

				taskSetDefs = getTaskSetsFromFile(
						(File)taskSetFiles.get(fileName));


				// If at least one TaskSetDef is in the next 
				// new batch period, add this file to the current
				// batch of task set files.
				if (taskSetDefs.isHappeningWithinPeriod(_batchPeriod)) {
					//_lw.logDebugMsg("TS: Adding " + fileName + " to next batch");
					_curTaskSetFiles.put(fileName, taskSetDefs);
					addCount++;
				}

			} catch (Exception ex) {
				_lw.logErr("TS: problem getting the next batch: ", ex);
			}
		}

		if (addCount > 0)
			_lw.logDebugMsg("TS: added " + addCount + 
					" sets to cache.  TOTAL: " + _curTaskSetFiles.size());
	}


	/**
	 * Moves the file to the task dir, overwriting files
	 * with the same names, and adds the task sets in the
	 * new files to the current batch to be processed
	 * immediately.
	 */
	private void installNewTasks() {
		//_lw.logDebugMsg("TS: Checking for new tasks");
		TaskSetDefs taskSetDefs;
		Hashtable taskSetFiles = getTaskSetFiles(NEW_DIR);

		// Move the new files to the task dir
		String fileName;
		Iterator fit = taskSetFiles.keySet().iterator();
		while (fit.hasNext()) {

			try {
				fileName = (String)fit.next();

				// Add the new task into the current batch of tasks
				// to be processed
				_lw.logDebugMsg("TS: Installing " + fileName);
				taskSetDefs = getTaskSetsFromFile(
						(File)taskSetFiles.get(fileName));
				_curTaskSetFiles.put(fileName, taskSetDefs);

				FileCopy.copy(NEW_DIR+fileName, TASK_DIR+fileName);
				FileDelete.deleteFile(NEW_DIR+fileName);

				//_lw.logDebugMsg("TS: Installed " + taskSetDefs.size() + " new tasks");

			} catch (Exception ex) {
				_lw.logErr("TS: problem getting new batch: ", ex);
			}
		}
	}



	/**
	 * Handles the task sets in the current batch as soon as 
	 * their time has come.
	 */
	private void processCurrentBatch() {
		int action;
		TaskSetDefs taskSetDefs;
		String fileName;
		if (_curTaskSetFiles.size() < 1)
			return;

		//_lw.logDebugMsg("TS: Processing " + _curTaskSetFiles.size() + " file(s)");
		Iterator fit = _curTaskSetFiles.keySet().iterator();
		while (fit.hasNext()) {
			fileName = (String)fit.next();
			taskSetDefs = (TaskSetDefs)_curTaskSetFiles.get(fileName);

			// If at least one TaskSetDef is in the next 
			// new batch period, add this file to the current
			// batch of task set files.
			if (taskSetDefs.isHappeningWithinPeriod(_handlerPeriod)) {

				//_lw.logDebugMsg("TS: Processing " + taskSetDefs.size() + " task sets");
				action = handleTaskSets(taskSetDefs);

				/*
				_lw.logDebugMsg("TS: task set has been handled: " + action);
				try {
					if (action == DELETE_TASKSET) {
							FileCopy.copy(TASK_DIR+fileName, OLD_DIR+fileName);
							FileDelete.deleteFile(TASK_DIR+fileName);
					} else if (action == REWRITE_FILE) {
						rewriteFile(fileName, taskSetDefs);
					}
				} catch (IOException ioe) {
					_lw.logErr("Can't copy/delete/rewrite file: ", ioe);
				}
				*/
				if (taskSetDefs.runOnce()) {
					_lw.logDebugMsg("TS: Removing run_once TaskSetDefs from batch");
					_curTaskSetFiles.remove(fileName);
				}
			}
		}

	}


	/**
	 * Handles any tasks in the given task set scheduled to 
	 * run in the new task handler period.  This method is
	 * used to handle all of the TaskSets that are in an
	 * XML task file.
	 *
	 * return int an action code for future scheduling of
	 *     the give task sets.
	 */
	public void handleTaskSetsNow(TaskSetDefs taskSetDefs) {
		TaskSetDef taskSetDef = null;
		Iterator tit = taskSetDefs.iterator();
		while (tit.hasNext()) {
			taskSetDef = (TaskSetDef)tit.next();
			handleTaskSet(taskSetDef);
		}
	}


	/**
	 * Handles any tasks in the given task set scheduled to 
	 * run in the new task handler period.  This method is
	 * used to handle all of the TaskSets that are in an
	 * XML task file.
	 *
	 * return int an action code for future scheduling of
	 *     the give task sets.
	 */
	private int handleTaskSets(TaskSetDefs taskSetDefs) {
		int action = NO_ACTION;
		int tmpAction; 
		TaskSetDef taskSetDef = null;
		Iterator tit = taskSetDefs.iterator();
		while (tit.hasNext()) {
			taskSetDef = (TaskSetDef)tit.next();

			if (!taskSetDef.isHappeningWithinPeriod(_handlerPeriod))
				continue;

			tmpAction = handleTaskSet(taskSetDef);

			/*
			if (tmpAction == DELETE_TASKSET) {
				taskSetDefs.remove(taskSetDef);
				action = REWRITE_FILE;
				_lw.logDebugMsg("TS: removing a done task set");
			} else if (tmpAction == REWRITE_FILE) {
				action = REWRITE_FILE;
				_lw.logDebugMsg("TS: task set requested rewrite");
			}
			*/
		}

		// If the task sets have changed, then the file
		// will get rewritten but if there are no more
		// task sets then the file should be deleted.
		/*
		if (taskSetDefs.size() == 0) {
			action = DELETE_TASKSET;
			_lw.logDebugMsg("TS: No more task sets");
		}
		*/

		return action;
	}


	/**
	 *
	 */
	private int handleTaskSet(TaskSetDef taskSetDef) {
		int action = NO_ACTION;
		TaskDefs taskDefs = taskSetDef.getTaskDefs();
		if (taskDefs == null) {
			_lw.logDebugMsg("TS: No tasks for this task set!");
			return action;
		}

		taskDefs.setRegBundles(__appRegBundles);
		taskDefs.setTemplateLoaders(__templateLoaders);
		TaskRunner tr = new TaskRunner(taskDefs, _lw);
		Thread tsThread = new Thread(tr);
		tsThread.start();
		//_lw.logDebugMsg("TS: Started task runner for "+taskDefs.size()+" tasks");

		return action;
	}


	/**
	 *
	 */
	/*
	private void rewriteFile(String fileName, TaskSetDefs taskSetDefs) 
			throws IOException {
	}
	*/


	/**
	 * Returns a hash of File objects.
	 */
	private Hashtable getTaskSetFiles(String dirPath) {

		Hashtable taskSetFiles = new Hashtable();
		String name;
		File file;
		File dir = new File(dirPath);
		File files[] = dir.listFiles();
		for (int i=0; i<files.length; i++) {
			file = files[i];
			name = file.getName();
			if (!name.toLowerCase().endsWith(".xml"))
				continue;

			taskSetFiles.put(name, file);
		}
		return taskSetFiles;
	}


	/**
	 *
	 */
	public TaskSetDefs getTaskSetsFromFile(File file) 
			throws Exception {

		SaxTaskSetParser parser = null;
		parser = new SaxTaskSetParser(file);
		TaskSetDefs taskSetDefs = parser.getTaskSetDefs();
		//_lw.logDebugMsg("TS: parsed XML in " + file.getName());
		return taskSetDefs;
	}


	/**
	 *
	 */
	public TaskSetDefs getTaskSetsFromXML(String xml) 
			throws Exception {

		SaxTaskSetParser parser = null;
		parser = new SaxTaskSetParser(xml);
		TaskSetDefs taskSetDefs = parser.getTaskSetDefs();
		_lw.logDebugMsg("TS: parsed XML string");
		return taskSetDefs;
	}




	///// INNER CLASSES ///////////////////////////
	private class TaskHandlerTimer implements TimerListener {
		public TaskHandlerTimer(long interval) {
			Timer t = new Timer(this, interval, false );
			t.start();
		}

		public void timeElapsed(Timer t) {
			processCurrentBatch();
		}
	}


	private class TaskBatchTimer implements TimerListener {
		public TaskBatchTimer(long interval) {
			Timer t = new Timer(this, interval, false );
			t.start();
		}

		public void timeElapsed(Timer t) {
			//System.out.println("TaskHandlerTimer elapsed: " + this.toString());
			getNewBatch();
		}
	}


	private class TaskInstallTimer implements TimerListener {
		public TaskInstallTimer(long interval) {
			Timer t = new Timer(this, interval, false );
			t.start();
		}

		public void timeElapsed(Timer t) {
			installNewTasks();
		}
	}

	/**
	 * Returns number of minutes between new
	 * task set installations.
	 */
	public long getInstallPeriod() {
		return _installPeriod / PERIOD_SCALAR; 
	}

	/**
	 * Returns number of minutes between 
	 * handler periods.  A handler period 
	 * is the span of time between processing
	 * of task sets in the current batch.
	 */
	public long getHandlerPeriod() {
		return _handlerPeriod / PERIOD_SCALAR;
	}

	/**
	 * Returns number of minutes between 
	 * roloads of the main taskset directory.
	 */
	public long getBatchPeriod() {
		return _batchPeriod / PERIOD_SCALAR;
	}



	///// MAIN ///////////////////////////////////
	public static void main(String[] args) {

		System.out.println("MAIN: getInstance");
		TaskScheduler sched = TaskScheduler.getInstance(); 

		String dir = TaskScheduler.TASK_DIR;
		Logger logger = new Logger();
		logger.setMsgLog(dir+"ats.msg");
		logger.setErrLog(dir+"ats.err");
		logger.setDebugLog(dir+"ats.debug");
		logger.setDoDebug(true);
		LoggerWrapper lw = new LoggerWrapper(logger);
		sched.setLogger(lw);
	}

}
