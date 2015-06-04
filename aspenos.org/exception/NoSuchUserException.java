package org.aspenos.exception;


/**
 * This is a specific registry related exception.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class NoSuchUserException extends RegistryException {
  /**
   * Constructs an <code>NoSuchUserException</code> with no specified detail message. 
   */
  public NoSuchUserException(){
    super();
  }

  /**
   * Constructs an <code>NoSuchUserException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public NoSuchUserException(String s){
      super(s);
  }
}
