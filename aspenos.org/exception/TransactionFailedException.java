package org.aspenos.exception;


/**
 * This is a specific registry related exception.
 *
 * @see     java.lang.Exception
 * @see     java.lang.Throwable
 */
public class TransactionFailedException extends RegistryException {
  /**
   * Constructs an <code>TransactionFailedException</code> with no specified detail message. 
   */
  public TransactionFailedException(){
    super();
  }

  /**
   * Constructs an <code>TransactionFailedException</code> with the specified detail message. 
   *
   * @param   s   the detail message.
   */
  public TransactionFailedException(String s){
      super(s);
  }
}
