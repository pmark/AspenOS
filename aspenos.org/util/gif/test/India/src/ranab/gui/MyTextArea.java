package ranab.gui;

import java.io.*;
import javax.swing.*;


/*
 * This class encapsulates <code>java.io.Writer</code>.
 */
public
class MyTextArea extends Writer {
	
	private JTextArea mTxtArea;
	private PrintWriter mWr;
	
  
	public MyTextArea(JTextArea txt) {
		mTxtArea = txt;
		mWr = null;
	}
	
  /**
   * get writer object
   */
	public PrintWriter getWriter() {
		if(mWr == null) {
			mWr = new PrintWriter(this);
		}
		return mWr;
	}
	
  /**
   * append the string
   */
	public void write(char[] cbuf, int off, int len) {
		mTxtArea.append(new String(cbuf, off, len));
	}
	
  
	public void flush() {
	}
	
  
	public void close() {
	}
	
}