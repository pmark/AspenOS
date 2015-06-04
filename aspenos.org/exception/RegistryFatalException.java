package org.aspenos.exception;


/**
 * This is a general parent class for all FATAL registry exceptions.
 *
 * @see     java.lang.RuntimeException
 * @see     java.lang.Throwable
 */
public class RegistryFatalException extends RuntimeException
{
  /**
   * Constructs an <code>RegistryFatalException</code> with no specified detail message. 
   */
  public RegistryFatalException(){
    super();
  }

  /**
   * Constructs an <code>RegistryFatalException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public RegistryFatalException(String s){
      super(s);
  }
}
