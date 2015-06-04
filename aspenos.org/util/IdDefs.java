package org.aspenos.util;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Represents a set of ubiquitous identifiers.
 * 
 * @author P. Mark Anderson
 **/
public class IdDefs extends ArrayList
		implements Serializable, Cloneable {

	/**
	 * 
	 */
	public IdDefs() {
	}


	/**
	 * Constructs a bunch of IdDef objects 
	 * with the Map objects contained in the mapList
	 * and inserts them into this list.
	 */
	public IdDefs(List mapList) {
		Iterator it = mapList.iterator();
		while (it.hasNext()) {
			Map hash = (Map)it.next();
			this.add( new IdDef( hash ) );
		}
	}


	/**
	 * Constructs a bunch of IdDef objects 
	 * (usually sublcasses of IdDef) with
	 * the Map objects contained in the given List.
	 * The given List must contain Maps, which will
	 * be used to construct the individual IdDef
	 * objects. The defClass is the full name of the
	 * class that is to be loaded and inserted
	 * into this list.  The maps are used as the
	 * properties for the new def class.
	 *
	 * @param mapList - a list of def properties
	 * @param defClass - the full name of the
	 *    specific def class to load.
	 */
	public IdDefs(List mapList, String defClass) 
			throws Exception {
		Iterator it = mapList.iterator();
		while (it.hasNext()) {
			Map hash = (Map)it.next();
			
			Class[] paramTypes = new Class[1];
			paramTypes[0] = Class.forName("java.util.Map");

			Object[] params = new Object[1];
			params[0] = hash;
			
			Class newClass = Class.forName(defClass);
			Constructor cons = newClass.getConstructor(paramTypes);
			//Object o = (IdDef)cons.newInstance(params);
			Object o = cons.newInstance(params);

			this.add( o );
		}
	}


	public void mergeWith(IdDefs ids) {

		// For each entity, check to see if an 
		// identical key already exists in this 
		Iterator i = ids.iterator();
		while (i.hasNext()) {
			IdDef def = (IdDef)i.next();
			String id = def.getId();

			//Check to see if it already exists based on ID
			IdDef existingDef = null;
			Iterator thisDefs = this.iterator();
			while (thisDefs.hasNext()) {

				IdDef tmpDef = (IdDef)thisDefs.next();
				String tmpDefId = tmpDef.getId();
				if (id.equals(tmpDefId)) {
					existingDef = tmpDef;
					break;  //There shouldn't be more than one existing def
				}
				//Else we leave existingDef=null;
			}

			if (existingDef != null) {
				// Def Already defined.
				// Let's make sure we have all of the associated properties 
				// in the existing def
				existingDef.mergeWith( def );

			} else {
				// Add the new def
				this.add(def);
			}
		}
	}

	public Object clone() {

		IdDefs retval = new IdDefs();
		for (Iterator i = this.iterator(); i.hasNext();) {
			IdDef def = (IdDef)i.next();
			retval.add((IdDef)def.clone());
		}
		return retval;
	}

	public String toXML() {

		StringBuffer sb = new StringBuffer();
		Iterator it = this.iterator();
		while (it.hasNext()) {
			IdDef next = (IdDef)it.next();
			sb.append(next.toXML());
		}
		return sb.toString();
	}

}
