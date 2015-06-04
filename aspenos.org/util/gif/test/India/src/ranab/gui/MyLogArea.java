package ranab.gui;

import java.io.*;
import javax.swing.*;

/*
 * This class encapsulates <code>java.io.Writer</code>
 */
public
class MyLogArea extends Writer {
	
	private JTextArea mTxtArea;
	private PrintWriter mWr;
	
	public MyLogArea(JTextArea txt) {
		mTxtArea = txt;
		mWr = null;
	}
	
	public PrintWriter getWriter() {
		if(mWr == null) {
			mWr = new PrintWriter(this);
			mTxtArea.setText("");
		}
		return mWr;
	}
	
	public void write(char[] cbuf, int off, int len) {
		mTxtArea.append(new String(cbuf, off, len));
	}
	
	public void flush() {
	}
	
	public void close() {
	}
	
}