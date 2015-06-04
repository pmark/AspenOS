package org.aspenos.exception;


/**
 * This is a general paraent class for all registry related exceptions.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class RegistryException extends Exception
{
  /**
   * Constructs an <code>RegistryException</code> with no specified detail message. 
   */
  public RegistryException(){
    super();
  }

  /**
   * Constructs an <code>RegistryException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public RegistryException(String s){
      super(s);
  }
}
