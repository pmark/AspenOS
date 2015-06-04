package org.aspenos.util;

import java.sql.*;
import java.util.*;

import org.aspenos.logging.*;


public class ThreadPool {

	private int _initialThreads=0;
	private int _maxThreads=5;
	private Hashtable _threads;
	private LoggerWrapper _lw;


	public ThreadPool(
			LoggerWrapper lw,
			int initialThreads,
			int maxThreads) {

		_lw = lw;

		init(initialThreads, maxThreads);
	}


	public ThreadPool(
			int initialThreads,
			int maxThreads) {
			
		init(initialThreads, maxThreads);
	}

	private void init(
			int initialThreads,
			int maxThreads) {

		_maxThreads = maxThreads;
		_initialThreads = initialThreads;

		_threads = new Hashtable();

		if (_maxThreads < 1)
			_maxThreads = 5;

		for (int i=0; i < _initialThreads; i++) {
			_threads.put(new Thread(), Boolean.FALSE);
		}

		if (_lw != null)
			_lw.logDebugMsg("TP: DONE creating threads");
	}


	public Thread getThread() {
		Thread thread = null;
		Enumeration threadKeys = _threads.keys();

		synchronized (_threads) {
			while (threadKeys.hasMoreElements()) {
				thread = (Thread)threadKeys.nextElement();

				Boolean b = (Boolean)_threads.get(thread);
				if (b == Boolean.FALSE) {
					_threads.put(thread, Boolean.TRUE);
					_lw.logDebugMsg("TP: returning pooled thread");
					return thread;
				}
			}
		}

		// No free threads; make new one
		if (_threads.size() < _maxThreads) {
			_lw.logDebugMsg("TP: creating a new thread");
			thread = new Thread();
			_threads.put(thread, Boolean.FALSE);
		} else {
			_lw.logDebugMsg("TP: no more threads available");
			return null;
		}

		return getThread();
	}


	public void returnThread(Thread returned) {
		Thread thread;
		Enumeration threadKeys = _threads.keys();
		while (threadKeys.hasMoreElements()) {
			thread = (Thread)threadKeys.nextElement();
			if (thread == returned) {
				_threads.put(thread, Boolean.FALSE);
				break;
			}
		}
	}

}
