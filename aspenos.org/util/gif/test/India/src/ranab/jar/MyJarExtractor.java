package ranab.jar;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.jar.JarFile;


/**
 * This is JAR file extractor class. It extracts the JAR file
 * in a separate thread. Where we are passing a MyJarObserver
 * object to track the current status of this decompression.
 */
public
class MyJarExtractor implements Runnable {
  
  private File mDir;
  private File mJarFile;
  private MyJarObserver mObserver;
  private boolean mbIsPauseRequest;
  private boolean mbIsStopRequest;
  
  
  public MyJarExtractor(File jarFile, 
                        File dir,
                        MyJarObserver observer) {
                          
    mJarFile = jarFile;
    mDir = dir;
    mObserver = observer;
  }
  
  
  /**
   * invoke a new thread and start decompression
   */
  public void extract() {
     Thread th = new Thread(this);
     th.start();
  }
  
  public void run() {
    
    mbIsPauseRequest = false;
    mbIsStopRequest = false;
    
    mObserver.start();
    try {
      ZipFile jf = new JarFile(mJarFile);
      mObserver.setCount(jf.size());
      Enumeration en = jf.entries();
      
      while(en.hasMoreElements()) {
        
        // check request
        while(mbIsPauseRequest && (!mbIsStopRequest) ) {
          Thread.sleep(100);
        }
        
        if(mbIsStopRequest) {
          return;
        }
        
        ZipEntry je = (ZipEntry)en.nextElement();
        extract(jf, je);
        mObserver.setNext(je);
      }
    }
    catch(Exception ex) {
      mObserver.setError(ex.getMessage());
    }
    finally {
      mbIsStopRequest = true;
      mObserver.end();
    }
  }
  
  
  /**
   * Extract an entry from the zip file.
   */
  public void extract(ZipFile jf, ZipEntry ze) throws Exception {
    File fl = new File(mDir, ze.toString());

    // directory create it
    if(ze.isDirectory()) {
        fl.mkdirs();
        return;
    }

    File par = new File(fl.getParent());
          if(!par.exists()) {
            par.mkdirs();
    }

    // file decompres it
	FileOutputStream fos = new FileOutputStream(fl);
	extract(jf, ze, fos);
	fos.close();
  }
  
  
  /**
   * write ZipEntry into OutputStream
   */
  public static void extract (ZipFile jf, ZipEntry ze, OutputStream out) 
  			throws Exception {
  	
	InputStream is = jf.getInputStream(ze);
    byte buff[] = new byte[1024];
    int cnt = -1;
    while((cnt = is.read(buff)) != -1) {
      out.write(buff, 0, cnt);
    }
	is.close();
  }
  
  
  
  /**
   * stop decompression
   */
  public void stop() {
    mbIsStopRequest = true;
  }
  
  public boolean isStopped() {
    return mbIsStopRequest;
  }
  
  /**
   * pause decompression
   */
  public void pause() {
    mbIsPauseRequest = true;
  }
  
  public boolean isPaused() {
    return mbIsPauseRequest;
  }
  
  /**
   * resume decompression
   */
  public void resume() {
    mbIsPauseRequest = false;
  }
   
   
  /**
   * get jar file name
   */
  public String toString() {
    return mJarFile.getAbsolutePath();
  }
  
}