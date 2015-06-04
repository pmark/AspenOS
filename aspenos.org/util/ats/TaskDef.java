package org.aspenos.util.ats;

import java.io.*;
import java.util.*;

import org.aspenos.util.*;

/**
 * 
 * @author P. Mark Anderson
 */
public class TaskDef extends IdDef implements Comparator {


	public TaskDef() {
		super();
		setDefName("Task");
	}


	public TaskDef(Map m) {
		super(m, "Task"); 
	}

	public int getOrdinal() {
		return Integer.parseInt((String)getProperty("ordinal"));
	}

	public int compare(Object o1, Object o2) {
		int ord1 = ((TaskDef)o1).getOrdinal();
		int ord2 = ((TaskDef)o2).getOrdinal();

		if (ord1 < ord2)
			return -1;
		if (ord1 == ord2)
			return 0;

		return 1;
	}

	/**
	 * Note: this comparator imposes orderings that are 
	 * inconsistent with equals.  Instead, it uses Object's
	 * equals() method.
	 */
	public boolean equals(Object obj) {
		return this.equals(obj);
	}
}
