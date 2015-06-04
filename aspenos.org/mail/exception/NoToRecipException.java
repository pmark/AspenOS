package org.aspenos.mail.exception;

public class NoToRecipException extends Exception
{

	/**
	* Constructs a NoToRecipException with no detail message.
	*/
	public NoToRecipException() 
	{
		super();
	}

	/**
	* Constructs a NoToRecipException with the specified detail message.
	* A detail message is a String that describes this particular exception.
	*
	* @param message detailed message.
	*/
	public NoToRecipException(String message) 
	{
		super(message);
	}
}
