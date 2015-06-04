package org.aspenos.util;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.aspenos.logging.*;

/**
 *
 */
public class RegistryFactory {

	// Init Methods ==========================================================
	public RegistryFactory() {
	}


	// Primary Methods =======================================================
	public IRegistry createRegistry(String regClass) 
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		return (IRegistry)Class.forName(regClass).newInstance();
	}

}


