package ranab.jar;

import java.util.zip.ZipEntry;

public
interface MyJarObserver {
  
  public void start();
  public void setCount(int count); 
  public void setNext(ZipEntry je);
  public void setError(String errMsg);
  public void end();
}