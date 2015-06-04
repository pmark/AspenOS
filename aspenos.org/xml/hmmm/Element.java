package org.aspenos.xml;

import java.util.*;

public class Element {

    private String name;
    private Hashtable attributes;
    private Vector children;
  

    public Element(String name) {
		this(name, null);
    }

    public Element(String name, String model) {
		this.name = name;
		attributes = new Hashtable();
		children = new Vector();
		parseModel(model);
    }

    public void setAttribute(String name, 
			     String type,
			     String[] options,
			     String defaultValue,
			     boolean isFixed,
			     boolean isRequired) {
	
		attributes.put(name, new Attribute(name,
					   type,
					   options,
					   defaultValue,
					   isFixed,
					   isRequired));
    }

    public Attribute getAttribute(String name) {
		return (Attribute)attributes.get(name);
    }

    public Enumeration getAttributesNames() {
		return attributes.keys();
    }

    public Enumeration getAttributes() {
		return attributes.elements();
    }

    public String getName() {
		return name;
    }

    public void setChild(String name) {
		children.add(name);
    }

    public Enumeration getChildren() {
		return children.elements();
    }
	
    private void parseModel(String model) {

		model = model.replace('(', ' ');
		model = model.replace(')', ' ');
		model =	model.replace(',', ' ');
		model = model.replace('|', ' ');
		model = model.replace('?', ' ');
		model = model.replace('*', ' ');
		model = model.replace('+', ' ');

		StringTokenizer strtok = new StringTokenizer(model, " ");
		while(strtok.hasMoreElements()) {
			setChild(strtok.nextToken());
		}
    }


    public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Element: ").append(getName()).append("\n");

		for(Enumeration e = getChildren(); e.hasMoreElements(); ) 
			buff.append("Child: ").append((String)e.nextElement()).append("\n");

		for(Enumeration e = getAttributes(); e.hasMoreElements();) 
			buff.append(e.nextElement().toString());
		
		return buff.toString();
    }

}


