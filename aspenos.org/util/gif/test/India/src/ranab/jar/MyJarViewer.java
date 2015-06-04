package ranab.jar;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * view a jar file - display all the entries.
 */
public
class MyJarViewer implements Runnable {
  
  private File mJarFile;
  private MyJarObserver mObserver;

  
  public MyJarViewer(File jarFile, 
                     MyJarObserver observer) {                   
    mJarFile = jarFile;
    mObserver = observer;
  }
  
  /**
   * invoke a new thread
   */
  public void view() {
     Thread th = new Thread(this);
     th.start();
  }
  
  /**
   * thread starting point - open the jar file
   */
  public void run() {
    mObserver.start();
    try {
      JarFile jf = new JarFile(mJarFile);
      mObserver.setCount(jf.size());
      Enumeration en = jf.entries();
      while(en.hasMoreElements()) {
        JarEntry je = (JarEntry)en.nextElement();
        mObserver.setNext(je);
      }
    }
    catch(Exception ex) {
      mObserver.setError(ex.getMessage());
    }
    finally {
      mObserver.end();
    }
  }
  
  /**
   * get the jar filename
   */
  public String toString() {
    return mJarFile.getAbsolutePath();
  }
  
}