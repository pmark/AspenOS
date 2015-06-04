/**
 * MyExtractorDialog.java
 */
 
package ranab.jar;

import java.io.File;
import java.util.zip.ZipEntry;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import ranab.gui.MyGuiUtil;

/** 
 * @author  Rana Bhattacharyya
 * @version 
 */
public 
class MyExtractorDialog 
       extends JDialog
       implements MyJarObserver {
        
  private JProgressBar mjProgBar;
  private JPanel mjButtonPanel;
  private JButton mjPauseButton;
  private JButton mjCloseButton;
  private JScrollPane mjScrollPane;
  private JTextArea mjTextArea;  
  
  private MyExtractorDialog mSelf;
  private MyJarExtractor mJarExtractor;    
         
         
  /** 
   * Creates new form JDialog 
   */
  public MyExtractorDialog(Frame parent, 
                           File compressedFile,
                           File dir) {
    super (parent, true);
    mSelf = this;
    mJarExtractor = new MyJarExtractor(compressedFile, dir, this);
    initComponents ();
    pack ();
    MyGuiUtil.setLocation(this);
    mJarExtractor.extract();
    setVisible (true);
  }

  
  /** 
   * This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents () {
    mjProgBar = new JProgressBar ();
    mjButtonPanel = new JPanel ();
    mjPauseButton = new JButton ();
    mjCloseButton = new JButton ();
    mjScrollPane = new JScrollPane ();
    mjTextArea = new JTextArea ();
    setResizable (false);
    addWindowListener (
      new WindowAdapter () {
        public void windowClosing (WindowEvent evt) {
          closeDialog (evt);
        }
      }
    );

    mjProgBar.setBorder (new MatteBorder(1, 1, 1, 1, Color.black));
    mjProgBar.setStringPainted (true);

    getContentPane ().add (mjProgBar, BorderLayout.NORTH);

    mjButtonPanel.setBorder (new EtchedBorder());

    mjPauseButton.setText ("Pause");
    mjPauseButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
           handlePause();
        }
      }  
    );
    mjButtonPanel.add (mjPauseButton);

    
    mjCloseButton.setText ("Stop");
    mjCloseButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
           handleStop();
        }
      }
    );
    mjButtonPanel.add (mjCloseButton);

    getContentPane ().add (mjButtonPanel, BorderLayout.SOUTH);

    mjTextArea.setColumns (30);
    mjTextArea.setRows (15);
    mjTextArea.setEditable (false);
    mjScrollPane.setViewportView (mjTextArea);

    getContentPane ().add (mjScrollPane, BorderLayout.CENTER);
  }
  
  
  /*
   * Handle window closing event.
   */
   protected void processWindowEvent(WindowEvent e) {

       int id = e.getID();
       if(id == WindowEvent.WINDOW_CLOSING) {
           if( mJarExtractor.isStopped() || MyGuiUtil.getConfirmation(mSelf, "Do you really want to stop?") ) {
             mJarExtractor.stop();
             closeDialog(e);
           }
       }
       else {
         super.processWindowEvent(e);
       }
    }
    
    
  /**
   * handle pause/resume menu handle
   */
  private void handlePause() {
     if(mJarExtractor.isPaused()) {
        mJarExtractor.resume();
        mjPauseButton.setText("Pause");
      }
      else {
        mJarExtractor.pause();
        mjPauseButton.setText("Resume");
      }
  }
    
  /**
   * handle stop button
   */
  private void handleStop() {
     if( mJarExtractor.isStopped() || MyGuiUtil.getConfirmation(mSelf, "Do you really want to stop?") ) {
        mJarExtractor.stop();
     } 
  }
   
  
  /** 
   * Closes the dialog 
   */
  private void closeDialog(WindowEvent evt) {
    setVisible (false);
    dispose ();
  }
  
  
  ////// MyJarObserver implementation /////
  public void start() {
    mjProgBar.setMinimum(0);
    mjTextArea.setText("");
  }
  
  public void setCount(int count) {
    mjProgBar.setMaximum(count);
  }
  
  public void setNext(ZipEntry ze) {
      mjTextArea.append(ze.toString());
      mjTextArea.append("\n");
      mjProgBar.setValue(mjProgBar.getValue() + 1);
  }
  
  public void setError(String errMsg) {
    MyGuiUtil.showErrorMessage(mSelf, errMsg);
  }
  
  public void end() {
    mjPauseButton.setEnabled(false);
    mjCloseButton.setEnabled(false);
  }
  
}