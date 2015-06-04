package org.aspenos.util;

import java.util.*;

/**
 * Simple encryption scheme.
 */
public class SpunkCrypt {

	public static String decode(String src) {
		char[] buf = new char[src.length()];

		for (int i=0; i < buf.length; i++) {
			buf[i] = src.charAt(i);
			buf[i] -= buf.length / 10;
			buf[i] += i;
			buf[i] /= 2;
		}

		return new String(buf);
	}


	public static String encode(String src) {
		char[] buf =  new char[src.length()];

		for (int i=0; i < buf.length; i++) {
			buf[i] = src.charAt(i);
			buf[i] *= 2;
			buf[i] -= i;
			buf[i] += buf.length / 10;
		}
		
		return new String(buf);
	}

	public static void main(String args[]) {

		String enc, end, start;
		if (args.length == 0) 
			start = "I'd like to give the world a Spunk.";
		else
			start = args[0];
		
		System.out.println("\n Original string: " + start);

		enc = SpunkCrypt.encode(start);
		System.out.println(  "Encrypted string: " + enc);

		end = SpunkCrypt.decode(enc);
		System.out.println(  "  Decoded string: " + end + "\n");
	}

}
