package org.aspenos.xml;

/**
 *
 */
public class Attribute {

    public String name;
    public String type;
    public String[] options;
    public String defaultValue;
    public boolean isFixed;
    public boolean isRequired;
    

	/**
	 *
	 */
    public Attribute(
			String name, 
			String type,
			String[] options,
			String defaultValue,
			boolean isFixed,
			boolean isRequired) {
	
		this.name = name;
		this.type = type;
		this.options = options;
		this.defaultValue = defaultValue;
		this.isFixed = isFixed;
		this.isRequired = isRequired;
    }


	/**
	 *
	 */
    public String toString() {

		StringBuffer buff = new StringBuffer();
		buff.append("Attribute: name=").append(name).append("\n");
		buff.append("Attribute: type=").append(type).append("\n");

		if (options != null) {

			for(int i=0; i < options.length; i++) {

				buff.append("Attribute: option=")
					.append(options[i]).append("\n")
					.append("Attribute: defaultValue=")
					.append(defaultValue).append("\n")
					.append("Attribute: isFixed=")
					.append(isFixed).append("\n")
					.append("Attribute: isRequired=")
					.append(isRequired).append("\n");
			}
		}

		return buff.toString();
    }


	    

}
