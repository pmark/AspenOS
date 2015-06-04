package org.aspenos.util;

import java.io.*;
import java.util.*;
import org.aspenos.db.*;

/**
 * Represents a ubiquitous identifier.
 * 
 * @author P. Mark Anderson
 **/
public class IdDef implements Cloneable, Serializable {

	protected String _defName = null;
	protected Map _properties = null;
	protected String _id = null;


	//// CONSTRUCTORS /////////////////////////////
	public IdDef() {
		_properties = new HashMap();
		setDefName( "Id" );
	}

	public IdDef(String id) {
		_properties = new HashMap();
		setId( id );
		setDefName( "Id" );
	}

	public IdDef(String id, String defName) {
		_properties = new HashMap();
		setId( id );
		setDefName( defName );
	}

	public IdDef(Map props) {
		setProperties( props );
		setDefName( "Id" );
	}

	public IdDef(Map props, String defName) {
		setProperties( props );
		setDefName( defName );
	}



	//// ACCESSORS ////////////////////////////////
	public String getId() {
		return _id;
	}

	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Null Ids are not allowed.");
		_id = id;
	}

	public String getDefName() {
		return _defName;
	}

	public void setDefName(String defName) {
		_defName = defName;
	}

	public Map getProperties() {
		return _properties;
	}

	public void setProperties(Map props) {
		_properties = props;
	}

	public void setProperty(String key, Object value) {
		_properties.put(key, value);
	}

	public Object getProperty(String key) {
		return _properties.get(key);
	}

	public String getString(String key) {
		Object o = _properties.get(key);
		if (o == null) return null;
		return o.toString();
	}

	public Integer getInteger(String key) {
		Object o = _properties.get(key);
		if (o == null) return null;
		return new Integer(o.toString());
	}

	public Float getFloat(String key) {
		Object o = _properties.get(key);
		if (o == null) return null;
		return new Float(o.toString());
	}

	/**
	 * Removes the first char, usually it's a $.
	 */
	public Float getMoneyFloat(String key) {
		Object o = _properties.get(key);
		if (o == null) return null;
		return new Float(o.toString().substring(1));
	}

	/**
	 * Removes the first char, usually it's a $.
	 */
	public String getMoneyString(String key) {
		Object o = _properties.get(key);
		if (o == null) return null;
		return o.toString().substring(1);
	}

	public java.util.Date getDate(String key) {
		return (java.util.Date)_properties.get(key);
	}



	//// UTILITY ///////////////////////////////////
	/**
	 * Merges any properties that do not already exist
	 * in this IdDef.  No existing values in this
	 * IdDef will be overwritten.  The only possible
	 * outcomes of a merge are that this IdDef's properties
	 * will have more key-value pairs OR it will be 
	 * unchanged.
	 */
	public void mergeWith(IdDef newDef) {

		Map newProps = newDef.getProperties();
		Iterator newKeys = newProps.keySet().iterator();

		while (newKeys.hasNext()) {

			String key = (String)newKeys.next();

			// add the property if not exist in this
			if (!this._properties.containsKey(key)) {
				this.setProperty(key, (String)newProps.get(key));
			}
		}

	}

	/**
	 * Produce an XML representation of this def.
	 * An XML representation of the property mappings
	 * can be produced with propsToXML(), which is used
	 * by this method.  
	 *
	 * example:  
	 *	<Id id="abc123">
	 *		<Attrib>
	 *			<Key>some-hash-key</Key>
	 *			<Value>some-hash-value</Value>
	 *		</Attrib>
	 *		...
	 *	</Id>
	*/
	public String toXML() {

		StringBuffer xml = new StringBuffer()
			.append("<")
			.append(_defName)
			.append(" id=\"")
			.append(_id )
			.append("\">\n")
			.append( propsToXML() )
			.append("</")
			.append(_defName)
			.append(">\n");

		return xml.toString();
	}


	/**
	 *
	 */
	protected String propsToXML() {
		StringBuffer xml = new StringBuffer();

		for( Iterator it = _properties.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			Object value = _properties.get(key);
			if (value == null)
				value = "null";

			xml.append("<Attrib>\n")
				.append("<Key>").append( key ).append("</Key>")
				.append("<Value>").append( value.toString() )
				.append("</Value>\n")
				.append("</Attrib>\n");
		}

		return xml.toString();
	}


	public Object clone() {
		IdDef newId = new IdDef(new String(_id));

		for (Iterator it = _properties.keySet().iterator();it.hasNext();) {
			String key = new String((String)it.next());
			Object value = _properties.get(key);
			newId.setProperty(key,value);
			newId.setDefName(_defName);
		}
		return newId;
	}


	public String toString() {
		return "IdDef.toString()=toXML():\n" + toXML();
	}


	public void makeSQLUnsafe() {
		for (Iterator it=_properties.keySet().iterator();it.hasNext();) {
			String key = new String((String)it.next());
			Object value = _properties.get(key);
			if (value != null)
				setProperty(key,DbTranslator.makeSQLUnsafe(value.toString()));
		}
	}


	public void makeSQLSafe() {
		for (Iterator it=_properties.keySet().iterator();it.hasNext();) {
			String key = new String((String)it.next());
			Object value = _properties.get(key);
			if (value != null)
				setProperty(key,DbTranslator.makeSQLSafe(value.toString(), false, false));
		}
	}


}
