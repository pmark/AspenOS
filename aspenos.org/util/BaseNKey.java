package org.aspenos.util;

import java.util.*;
import java.io.*;

/**
 * A six character BaseNKey where N = 36
 * can represent 2.176 billion integer values.
 */
public class BaseNKey {

	private int _base;
	private long _longValue;
	private String _stringValue;



	//// CONSTRUCT ///////////////////////////////
	public BaseNKey(int base) {
		_base = base;
		setLong(0);
		setString("0");
	}

	public BaseNKey(int base, String str) {
		_base = base;
		setString(str);
	}

	public BaseNKey(int base, long l) {
		_base = base;
		setLong(l);
	}

	//// HELPER //////////////////////////////////
	public void increment() {
		_longValue++;
		_stringValue = getNextKey();
	}


	//// SET /////////////////////////////////////
	public void setLong(long l) {
		_longValue = l;
		_stringValue = Long.toString(_longValue, _base);
	}

	public void setString(String str) {
		_stringValue = str.toLowerCase();
		_longValue = Long.parseLong(_stringValue, _base);
	}


	//// GET /////////////////////////////////////
	public long getLong() {
		return _longValue;
	}

	public String getString() {
		return _stringValue;
	}

	public String getNextKey() {
		return Long.toString(_longValue+1, _base);
	}





	public static void main(String[] args) {

		System.out.println("\n\nBase N characters\n\n");

		int base = 36;
		BaseNKey bnk = new BaseNKey(base, 100);

		for (long l=0; l<75; l++) {
			System.out.println(l + ":  '" + bnk.getString() +"', " +
					bnk.getLong());
			bnk.increment();
			//System.out.println(l + " % base = " + l%base);

		}

	}

}
