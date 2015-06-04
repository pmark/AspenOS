package org.aspenos.exception;


/**
 * This is a specific registry related exception.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class WrongPasswordException extends RegistryException {
  /**
   * Constructs an <code>WrongPasswordException</code> with no specified detail message. 
   */
  public WrongPasswordException(){
    super();
  }

  /**
   * Constructs an <code>WrongPasswordException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public WrongPasswordException(String s){
      super(s);
  }
}
