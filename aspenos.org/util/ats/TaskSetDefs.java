package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 **/
public class TaskSetDefs extends IdDefs {

	private boolean _runOnce = false;


	public TaskSetDefs() {
	}

	public TaskSetDefs(List l) throws Exception {
		super(l, "org.aspenos.util.ats.TaskSetDef");
	}


	/**
	 * Examines each TaskSetDef to see if it is scheduled to 
	 * occur within the next given time period.
	 */
	public boolean isHappeningWithinPeriod(long period) {
		TaskSetDef taskSetDef = null;
		Iterator tit = this.iterator();
		while (tit.hasNext()) {
			taskSetDef = (TaskSetDef)tit.next();
			if (taskSetDef.isHappeningWithinPeriod(period))
				return true;
		}

		return false;
	}

	public void setRunOnce(boolean b) {
		_runOnce = b;
	}

	public boolean runOnce() {
		return _runOnce;
	}

}
