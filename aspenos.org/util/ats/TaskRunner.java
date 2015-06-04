package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.logging.*;
import org.aspenos.util.*;

/**
 * 
 */
public class TaskRunner implements Runnable {

	private TaskDefs _taskDefs = null;
	private LoggerWrapper _lw = null;


	/**
	 *
	 */
	public TaskRunner(TaskDefs taskDefs) {
		_taskDefs = taskDefs;
	}


	/**
	 *
	 */
	public TaskRunner(TaskDefs taskDefs, LoggerWrapper lw) {
		_taskDefs = taskDefs;
		_lw = lw;
	}



	/**
	 *
	 */
	public void run() {
		//System.out.println("TR: handling task set: " + _taskDefs.size());
		TaskDef taskDef = null;

		Object[] array = _taskDefs.toArray();
		Arrays.sort(array, (TaskDef)_taskDefs.get(0));

		try {
			for (int i=0; i<array.length; i++) {
				taskDef = (TaskDef)array[i];
				taskDef.setProperty("reg_bundles", 
					_taskDefs.getRegBundles());
				taskDef.setProperty("template_loaders", 
					_taskDefs.getTemplateLoaders());
				handleTask(taskDef);
			}
		} catch (Exception ex) {
			_lw.logErr("TR: Problem handling task: ", ex);
		}
	}


	/**
	 *
	 */
	private void handleTask(TaskDef taskDef) throws Exception {
		String cname = (String)taskDef.getProperty("class_name");
		Task task = (Task)Class.forName(cname).newInstance();
		task.setLogger(_lw);
		task.setProperties(taskDef.getProperties());
		task.handleTask();
		task.returnDbConnections();
		//_lw.logDebugMsg("TR: Done handling task");
	}

}


