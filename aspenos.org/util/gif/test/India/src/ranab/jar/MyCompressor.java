package ranab.jar;

import java.io.*;

/**
 * This is tha abstract base class of all the file
 * compressors.
 */
public
abstract class MyCompressor {

  protected File mCompressedFile;
  protected MyJarObserver mObserver;
  
  
  /**
   * constructor
   * @file fl the new compressed file 
   */
  public MyCompressor(File fl, MyJarObserver observer) {
    mCompressedFile = fl;
    mObserver = observer;
  }
  
  /**
   * add a file/directory
   */
  public void addFile(File file,
                      boolean recursion,
                      boolean pathInfo,
                      int level) {
    
    if(file.isDirectory()) { 
      addFile(file, file, recursion, pathInfo, true, level);                    
    } 
    else {
      addFile(file, file.getParentFile(), recursion, pathInfo, true, level);
    }
  }
  
  
  /**
   * actual work horse
   */
  private void addFile(File file,
                       File parent,
                       boolean recursion,
                       boolean pathInfo,
                       boolean firstTime,
                       int level) {                        
                         
    // if not directory - write now 
    if(!file.isDirectory()) {
      
      // get entry name
      String filePath = file.getAbsolutePath(); 
      String dirPath = parent.getAbsolutePath();
      String entryName = file.getName();
      if(pathInfo && filePath.startsWith(dirPath)) {
        entryName = filePath.substring(dirPath.length() + 1);
      }
      setCompressionLevel(level);
      addFile(file, entryName);
    }
    else if(firstTime || recursion) {
      File fileList[] = file.listFiles();
      for(int i=0; i<fileList.length; i++) {  
        addFile(fileList[i], parent, recursion, pathInfo, false, level);
      }
    }
  }

  
  /**
   * open a new compressed file
   */
  abstract public void open() throws Exception; 
   
  /**
   * add a file
   *
   * @param file the file to be added
   * @param name the entry name (usually the filename)
   */
  abstract protected void addFile(File file, String name);
  
  
  /**
   * set the compression level
   */
  abstract protected void setCompressionLevel(int level);
   
   
  /**
   * close the compressed file
   */
  abstract public void close();

  /**
   * get the compressed filename
   */
  public String toString() {
    return mCompressedFile.getAbsolutePath();
  }
  
}