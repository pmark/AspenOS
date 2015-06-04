package org.aspenos.exception;


/**
 * This is a specific registry related exception.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class InvalidSessionException extends RegistryException {
  /**
   * Constructs an <code>InvalidSessionException</code> with no specified detail message. 
   */
  public InvalidSessionException(){
    super();
  }

  /**
   * Constructs an <code>InvalidSessionException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public InvalidSessionException(String s){
      super(s);
  }
}
