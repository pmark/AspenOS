package org.aspenos.exception;

import java.sql.*;

/**
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class SDbException extends SQLException
{
  /**
   * Constructs an <code>SDbException</code> with no specified detail message. 
   */
  public SDbException(){
    super();
  }

  /**
   * Constructs an <code>SDbException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public SDbException(String s){
      super(s);
  }
}
