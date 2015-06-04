package org.aspenos.exception;


/**
 * This is a specific registry related exception.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class UserAlreadyExistsException extends RegistryException {
  /**
   * Constructs an <code>UserAlreadyExistsException</code> with no specified detail message. 
   */
  public UserAlreadyExistsException(){
    super();
  }

  /**
   * Constructs an <code>UserAlreadyExistsException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public UserAlreadyExistsException(String s){
      super(s);
  }
}
