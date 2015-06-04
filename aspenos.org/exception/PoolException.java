package org.aspenos.exception;


/**
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class PoolException extends Exception
{
  /**
   * Constructs an <code>PoolException</code> with no specified detail message. 
   */
  public PoolException(){
    super();
  }

  /**
   * Constructs an <code>PoolException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public PoolException(String s){
      super(s);
  }
}
