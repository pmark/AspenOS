package org.aspenos.mail.exception;

public class MailerException extends Exception
{

	/**
	* Constructs a MailerException with no detail message.
	*/
	public MailerException() 
	{
		super();
	}

	/**
	* Constructs a MailerException with the specified detail message.
	* A detail message is a String that describes this particular exception.
	*
	* @param message detailed message.
	*/
	public MailerException(String message) 
	{
		super(message);
	}
}
