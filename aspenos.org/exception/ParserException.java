package org.aspenos.exception;


/**
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class ParserException extends Exception
{
  /**
   * Constructs an <code>ParserException</code> with no specified detail message. 
   */
  public ParserException(){
    super();
  }

  /**
   * Constructs an <code>ParserException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public ParserException(String s){
      super(s);
  }
}
