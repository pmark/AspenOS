package ranab.gui;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

/**
 * This is the GUI utility class. 
 */
public
class MyGuiUtil {
	
	private static JFileChooser mDirChoose   = null;    
	private static JFileChooser mFileChoose  = null;
	
	
	/**
	 * create image icon
	 */
	public static ImageIcon createImageIcon(String imgFile) {
		URL imgUrl = ClassLoader.getSystemClassLoader().getResource(imgFile);
		if(imgUrl == null) {
			return null;
		}
		
		return new ImageIcon(imgUrl);
	}
	
	
	/**
	 * create splash window. Returns null if image not found.
	 */
	public static JWindow createSplashWindow(String imgFile) {
		
		ImageIcon icon = createImageIcon(imgFile);
		if(icon == null) {
			return null;
		}
		
		JLabel lab = new JLabel();
		lab.setIcon(icon);
    
		Dimension iDim = new Dimension(icon.getIconWidth(), icon.getIconHeight());

		JWindow splashWin = new JWindow();
		splashWin.getContentPane().add(lab);
		splashWin.setSize(iDim);
    setLocation(splashWin);
		return splashWin;
	}

	
	
  /**
   * Show error message.
   */
	public static void showErrorMessage(Component parent, String str) {
		JOptionPane.showMessageDialog(parent, str, "Error!",
                                      JOptionPane.ERROR_MESSAGE);
	}
	
	
	/**
	 * Show warning message.
	 */
	public static void showWarningMessage(Component parent, String str) {
		JOptionPane.showMessageDialog(parent, str, "Warning!",
                                      JOptionPane.WARNING_MESSAGE);
	}

	/**
   * Show message.
   */
  public static void showInformationMessage(Component parent, String str) {
    JOptionPane.showMessageDialog(parent, str, "Information!",
                                      JOptionPane.INFORMATION_MESSAGE );
  }

  
  
   /**
    * Get user confirmation.
    */
   public static boolean getConfirmation(Component parent, String str) {
		
		int res = JOptionPane.showConfirmDialog(parent, 
									str,
									"Confirmation",
							  	JOptionPane.YES_NO_OPTION, 
							  	JOptionPane.QUESTION_MESSAGE 
						    );
						     	  
		return (res == JOptionPane.YES_OPTION);	
   }

   
   /**
    * get file name
    */
   public static String getFileName(Component parent) {
		
		if(mFileChoose == null) {
			mFileChoose = new JFileChooser();
			mFileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		
		int returnVal = mFileChoose.showOpenDialog(parent);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) {
    		return mFileChoose.getSelectedFile().getAbsolutePath();
		}
		else {
			return null;
    }
   }
	
   /**
    * get directory name
    */	
   public static String getDirName(Component parent) {
		
		if(mDirChoose == null) {
			mDirChoose = new JFileChooser();
			mDirChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		
		int returnVal = mDirChoose.showOpenDialog(parent);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) {
    		return mDirChoose.getSelectedFile().getAbsolutePath();
		}
		else {
			return null;
    }
   }
   
   /**
    * update component UI
    */
   public static void updateLnF() {
       
       if(mDirChoose != null) {
           SwingUtilities.updateComponentTreeUI(mDirChoose);
       }
       if(mFileChoose != null) {
           SwingUtilities.updateComponentTreeUI(mFileChoose);
       }
   }
   
   /**
    * position properly
    */
   public static void setLocation(Component comp) {
     Dimension cDim = comp.getSize();
     Dimension wDim = Toolkit.getDefaultToolkit().getScreenSize();
     comp.setLocation((wDim.width - cDim.width)/2, (wDim.height - cDim.height)/2);
   }
   
}