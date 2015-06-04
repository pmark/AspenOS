package org.aspenos.util.ats;

import java.util.*;
import java.io.*;

import org.aspenos.util.*;

/**
 * 
 */
public class DeleteTaskSetFile extends Task {


	public void handleTask() {
		String fileName = getString("file_name");
		TaskScheduler.getInstance().uninstallFile(fileName, true);
	}

}
