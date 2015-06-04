/*
 * MyJarUtil.java
 *
 */
 
package ranab.jar;

import java.util.zip.ZipEntry;
import java.util.jar.JarFile;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import ranab.gui.MyGuiUtil;



/** 
 * @author  Rana Bhattacharyya
 * @version 
 */
public 
class MyJarUtil extends JFrame {
  
  private final static String SPLASH_IMG = "images/jar.gif";
  private final static String APP_NAME = "JarUtil";
  
  private JMenuBar mjMenuBar;
  
  private JMenu mjFileMenu;
  private JMenu mjNewMenu;
  private JMenuItem mjNewJarMenu;
  private JMenuItem mjNewZipMenu;
  private JMenuItem mjOpenMenu;
  private JMenuItem mjAddMenu;
  private JMenuItem mjCloseMenu;
  private JMenu mjExtractMenu;
  private JMenuItem mjExtractAllMenu;
  private JMenuItem mjExtractSelectMenu;
  private JSeparator mjExitSeparator;
  private JMenuItem mjExitMenu;
  
  private JMenu mjLookFeelMenu;
  private JCheckBoxMenuItem mjMetalMenu;
  private JCheckBoxMenuItem mjWindowsMenu;
  private JCheckBoxMenuItem mjMotifMenu;
  
  private JLabel mjHeader;
  private JPanel mjTablePane; 
  
  private MyJarUtil mSelf;
  private MyJarTable mJarTable;
  
  private File mCurrentOpenFile;
  private MyCompressor mCompressor;
  private MyAddFileChooser mFileChooser;
  
  
  /** 
   * Creates new form JFrame 
   */
  public MyJarUtil() {
      
      // show splash window
      JWindow splashWin = MyGuiUtil.createSplashWindow(SPLASH_IMG);
      if(splashWin != null) {
        splashWin.setVisible(true);
      }

      initComponents ();
      pack ();
      setSize(new Dimension(600, 500));
      MyGuiUtil.setLocation(this);
      
      mCurrentOpenFile = null;
      mCompressor = null;
      mSelf = this;
      setAppStatus();
      
      // hide splash wwindow
      if(splashWin != null) {
        splashWin.setVisible(false);
      }
      
      setVisible(true);
  }

  
  /** 
   * This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents () {
    mjMenuBar = new JMenuBar ();
    mjFileMenu = new JMenu ();
    mjNewMenu = new JMenu ();
    mjNewJarMenu = new JMenuItem ();
    mjNewZipMenu = new JMenuItem ();
    mjOpenMenu = new JMenuItem ();
    mjAddMenu = new JMenuItem ();
    mjCloseMenu = new JMenuItem ();
    mjExtractMenu = new JMenu ();
    mjExtractAllMenu = new JMenuItem ();
    mjExtractSelectMenu = new JMenuItem ();
    mjExitSeparator = new JSeparator ();
    mjExitMenu = new JMenuItem ();
    mjLookFeelMenu = new JMenu ();
    mjMetalMenu = new JCheckBoxMenuItem ();
    mjWindowsMenu = new JCheckBoxMenuItem ();
    mjMotifMenu = new JCheckBoxMenuItem ();
    mjHeader = new JLabel ();
    
    mFileChooser = new MyAddFileChooser();
    
    mjFileMenu.setText("File");
    
    mjNewMenu.setText ("New");

    mjNewJarMenu.setText ("Jar");
    mjNewJarMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjNewJarMenuActionPerformed (evt);
      }
    }
    );
    mjNewMenu.add (mjNewJarMenu);
    
    mjNewZipMenu.setText ("Zip");
    mjNewZipMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjNewZipMenuActionPerformed (evt);
      }
    }
    );
    mjNewMenu.add (mjNewZipMenu);
    mjFileMenu.add (mjNewMenu);
    
    mjOpenMenu.setText ("Open");
    mjOpenMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjOpenMenuActionPerformed (evt);
      }
    }
    );
    mjFileMenu.add (mjOpenMenu);
    
    mjAddMenu.setText ("Add File");
    mjAddMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjAddMenuActionPerformed (evt);
      }
    }
    );
    mjFileMenu.add (mjAddMenu);
    
    mjCloseMenu.setText ("Close");
    mjCloseMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjCloseMenuActionPerformed (evt);
      }
    }
    );
    mjFileMenu.add (mjCloseMenu);
    
    mjExtractMenu.setText ("Extract");
   
    mjExtractAllMenu.setText("All");
    mjExtractAllMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjExtractAllMenuActionPerformed (evt);
      }
    }
    );
    mjExtractMenu.add(mjExtractAllMenu);
    
    mjExtractSelectMenu.setText("Selection");
    mjExtractSelectMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjExtractSelectMenuActionPerformed (evt);
      }
    }
    );
    mjExtractMenu.add(mjExtractSelectMenu);
    
    mjFileMenu.add (mjExtractMenu);

    mjFileMenu.add (mjExitSeparator);
    mjExitMenu.setText ("Exit");
    mjExitMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjExitMenuActionPerformed (evt);
      }
    }
    );
    mjFileMenu.add (mjExitMenu);
    
    mjMenuBar.add (mjFileMenu);
    mjLookFeelMenu.setText ("Look & Feel");

    mjMetalMenu.setText ("Metal");
    mjMetalMenu.setState(true);
    mjMetalMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjMetalMenuActionPerformed (evt);
      }
    }
    );
    mjLookFeelMenu.add (mjMetalMenu);
    
    mjWindowsMenu.setText ("Windows");
    mjWindowsMenu.setState(false);
    mjWindowsMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjWindowsMenuActionPerformed (evt);
      }
    }
    );
    mjLookFeelMenu.add (mjWindowsMenu);
    
    mjMotifMenu.setText ("Motif");
    mjMotifMenu.setState(false);
    mjMotifMenu.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent evt) {
        mjMotifMenuActionPerformed (evt);
      }
    }
    );
    mjLookFeelMenu.add (mjMotifMenu);
    mjMenuBar.add (mjLookFeelMenu);
    
    mjHeader.setBackground (new Color (171, 213, 255));
    mjHeader.setBorder (new EtchedBorder());
    mjHeader.setText ("Jar Utility");
    mjHeader.setForeground (Color.black);
    mjHeader.setHorizontalAlignment (SwingConstants.CENTER);
    mjHeader.setFont (new Font ("Serif", 1, 14));


    getContentPane ().add (mjHeader, BorderLayout.NORTH);

    mjTablePane = new JPanel();
    mJarTable = new MyJarTable(mjTablePane);
    mjTablePane.setLayout(new BorderLayout());
    mjTablePane.setBackground(Color.white);
    mjTablePane.add(mJarTable.getTable(), BorderLayout.CENTER);
    getContentPane ().add (mjTablePane, BorderLayout.CENTER);

    setJMenuBar (mjMenuBar);
    
  }
  
  ///// all menu handlers /////
  // new zip menu handle
  private void mjNewZipMenuActionPerformed (ActionEvent evt) {
    
    // get filename
    String newZip = MyGuiUtil.getFileName(mSelf);
    if(newZip == null) {
      return;
    }
    
    // if exists get confirmation
    File zipFile = new File(newZip);
    if(zipFile.exists()) {
      boolean yes = MyGuiUtil.getConfirmation(mSelf, "Do you want to overwrite " + zipFile + "?");
      if(!yes) {
        return;
      }
    }
    
    // open compressor
    try {
      mCompressor = new MyZipCompressor(zipFile, mJarTable);
      mCompressor.open();
      mCurrentOpenFile = null;
    }
    catch(Exception ex) {
      mCompressor = null;
    }
  
    setAppStatus();
  }
  
  // new jar menu handle
  private void mjNewJarMenuActionPerformed (ActionEvent evt) {
    
    // get filename
    String newJar = MyGuiUtil.getFileName(mSelf);
    if(newJar == null) {
      return;
    }
    
    // if exists get confirmation
    File jarFile = new File(newJar);
    if(jarFile.exists()) {
      boolean yes = MyGuiUtil.getConfirmation(mSelf, "Do you want to overwrite " + jarFile + "?");
      if(!yes) {
        return;
      }
    }

    // open compresor
    try {
      mCompressor = new MyJarCompressor(new File(newJar), mJarTable);
      mCompressor.open();
      mCurrentOpenFile = null;
    }
    catch(Exception ex) {
      mCompressor = null;
    }
    
    setAppStatus();
  }
  
  // open menu handler
  private void mjOpenMenuActionPerformed (ActionEvent evt) {
    String zipFile = MyGuiUtil.getFileName(mSelf);
    if(zipFile == null) {
      return;
    }
    
    mCurrentOpenFile = new File(zipFile);
    mCompressor = null;
    
    MyJarViewer tableView = new MyJarViewer(mCurrentOpenFile, mJarTable);
    tableView.view();
    setAppStatus();
  }
  
  // add menu handler
  private void mjAddMenuActionPerformed (ActionEvent evt) {
    
    File selectedFile = mFileChooser.getFileName(mSelf);
    if(selectedFile == null) {
      return;
    }
    
    boolean recursive = mFileChooser.isRecursive();
    boolean pathInfo = mFileChooser.isPathInfo();
    int level = mFileChooser.getCompressionLevel();
    mCompressor.addFile(selectedFile, recursive, pathInfo, level);
  }
  
  // close menu handle
  private void mjCloseMenuActionPerformed (ActionEvent evt) {
    
    mCompressor.close();
    mCurrentOpenFile = new File(mCompressor.toString());
    mCompressor = null;
    setAppStatus();
  }
  
  // extract all menu handler
  private void mjExtractAllMenuActionPerformed (ActionEvent evt) {
    
    String dir = MyGuiUtil.getDirName(mSelf);
    if(dir == null) {
      return;
    }
    
    File zipDir = new File(dir);
    new MyExtractorDialog(mSelf, mCurrentOpenFile, zipDir);
  }
  
  // extract selection menu handler
  private void mjExtractSelectMenuActionPerformed (ActionEvent evt) {
    
    ZipEntry ze[] = mJarTable.getSelectedEntries();
    if(ze == null) {
      return;
    }
    
    String dirName = MyGuiUtil.getDirName(mSelf);
    if(dirName == null) {
      return;
    }
    
    File dirFile = new File(dirName);
    MyJarExtractor ext = new MyJarExtractor(mCurrentOpenFile, dirFile, null);
    
    try {
      for(int i=0; i<ze.length; i++) {
        ext.extract(new JarFile(mCurrentOpenFile), ze[i]);
      }
    }
    catch(Exception ex) {
      MyGuiUtil.showErrorMessage(mSelf, ex.getMessage());
    }
  }
  
  // exit menu handles
  private void mjExitMenuActionPerformed (ActionEvent evt) {
      if(MyGuiUtil.getConfirmation(mSelf, "Do you really want to exit?")) {
        mSelf.setVisible(false);
        mSelf.dispose();
        System.exit(0);
      }
  }
  
  // windows L&F menu handle
  private void mjWindowsMenuActionPerformed (ActionEvent evt) {
    mjMetalMenu.setState(false);
    mjWindowsMenu.setState(true);
    mjMotifMenu.setState(false);
    menuLfHandle("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");    
  }
  
  // metal L&F menu handle
  private void mjMetalMenuActionPerformed (ActionEvent evt) {
    mjMetalMenu.setState(true);
    mjWindowsMenu.setState(false);
    mjMotifMenu.setState(false);
    menuLfHandle("Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
  }
  
  // motif L&F menu handle
  private void mjMotifMenuActionPerformed (ActionEvent evt) {
    mjMetalMenu.setState(false);
    mjWindowsMenu.setState(false);
    mjMotifMenu.setState(true);
    menuLfHandle("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
  }

  
  /**
   * set look and feel
   */
   private void menuLfHandle(String name, String lnfName) {
       
       try {
           UIManager.setLookAndFeel(lnfName);
           SwingUtilities.updateComponentTreeUI(this);
           MyGuiUtil.updateLnF();  
       }
       catch(ClassNotFoundException ex) {
           MyGuiUtil.showErrorMessage(this, "Class " + lnfName + " not found.");
       }
       catch(InstantiationException ex) {
           MyGuiUtil.showErrorMessage(this, "Cannot instantiate " + lnfName);
       }
       catch(UnsupportedLookAndFeelException ex) {
           MyGuiUtil.showErrorMessage(this, "Unsupported Look & Feel " + name);
       }
       catch(Exception ex) {
           MyGuiUtil.showErrorMessage(this, ex.getLocalizedMessage());
       }
   }

  
  /*
   * Handle window closing event.
   */
   protected void processWindowEvent(WindowEvent e) {

       int id = e.getID();
       if(id == WindowEvent.WINDOW_CLOSING) {
           if( !MyGuiUtil.getConfirmation(mSelf, "Do you really want to exit?") ) {
           return;
         }

         super.processWindowEvent(e);
         mSelf.setVisible(false);
         mSelf.dispose();
         System.exit(0);
       }
       else {
         super.processWindowEvent(e);
       }
    }
     
    /**
     * set menu status
     */
    private void setAppStatus() {
      
      // set window title
      if(mCurrentOpenFile != null) {
        mSelf.setTitle(APP_NAME + " - " + mCurrentOpenFile.getAbsolutePath());
      }
      else if(mCompressor != null) {
        mSelf.setTitle(APP_NAME + " - " + mCompressor.toString());
      }
      else {
        mSelf.setTitle(APP_NAME);
      }
      
      // set menu status
      if(mCurrentOpenFile != null) {
        mjNewJarMenu.setEnabled(true);
        mjNewZipMenu.setEnabled(true);
        mjOpenMenu.setEnabled(true);
        mjAddMenu.setEnabled(false);
        mjCloseMenu.setEnabled(false);
        mjExtractAllMenu.setEnabled(true);
        mjExtractSelectMenu.setEnabled(true);
      }
      else if(mCompressor != null) {
        mjNewJarMenu.setEnabled(false);
        mjNewZipMenu.setEnabled(false);
        mjOpenMenu.setEnabled(false);
        mjAddMenu.setEnabled(true);
        mjCloseMenu.setEnabled(true);
        mjExtractAllMenu.setEnabled(false);
        mjExtractSelectMenu.setEnabled(false);
      }
      else {
        mjNewJarMenu.setEnabled(true);
        mjNewZipMenu.setEnabled(true);
        mjOpenMenu.setEnabled(true);
        mjAddMenu.setEnabled(false);
        mjCloseMenu.setEnabled(false);
        mjExtractAllMenu.setEnabled(false);
        mjExtractSelectMenu.setEnabled(false);
      }
      
    }
     
    
    // program starting point
    public static void main(String args[]) {
      MyJarUtil mj = new MyJarUtil();
    }
    
}