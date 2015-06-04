package ranab;

public class MyException extends Exception {

  public MyException() {
  	super();
  }
	
  public MyException(String msg)  {
  	super(msg);
  }	
	
  public MyException(Exception ex)  {
  	super(ex.getMessage());
  }	
	
}