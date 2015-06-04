package ranab.jar;

import java.io.File;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;


/**
 * custom file chooser dialog box. It is used to add file
 */
public
class MyAddFileChooser {
  
  private JFileChooser mjFileChooser;
  private JCheckBox mjRecursive;
  private JCheckBox mjPathInfo;
  private JComboBox mjLevelCombo;
  
  private boolean mbRecursive;
  private boolean mbPathInfo;
  private int miCompressionLevel;
  
  /**
   * constructor
   */
  public MyAddFileChooser() {
    mbRecursive = false;
    mbPathInfo = false;
    miCompressionLevel = 0;
    
    mjFileChooser = new JFileChooser();
    mjFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    mjFileChooser.setAccessory(getPanel());
  }
  
  /**
   * get accessory panel
   */
  private JPanel getPanel()  {
    
    JPanel pane = new JPanel();
    pane.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    c.anchor = GridBagConstraints.WEST;
    c.gridwidth = 2;
    c.gridx = 0;
    c.gridy = 0;
    mjRecursive = new JCheckBox("Recursive");
    pane.add(mjRecursive, c);
    
    c.gridy = 1;
    mjPathInfo = new JCheckBox("PathInfo");
    pane.add(mjPathInfo, c);
    
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 2;
    mjLevelCombo = new JComboBox();
    for(int i=0; i<10; i++) {
      mjLevelCombo.addItem(new Integer(i));
    }
    pane.add(mjLevelCombo, c);
    
    c.anchor = GridBagConstraints.EAST;
    c.gridx = 1;
    JLabel compLev = new JLabel("Level");
    compLev.setForeground(Color.black);
    pane.add(compLev, c);
    
    return pane;
  }
  
  /**
   * is recursive?
   */
  public boolean isRecursive() {
    return mbRecursive;
  }
  
  /**
   * is full path?
   */
  public boolean isPathInfo() {
    return mbPathInfo;
  }
  
  /**
   * get selected compression level
   */
  public int getCompressionLevel() {
    return miCompressionLevel;
  }
  
  /**
   * get selected file name. If not selected returns null.
   */
  public File getFileName(Component parent) {
    
    File selectedFile = null;
    setPanelData();
    int returnVal = mjFileChooser.showOpenDialog(parent);
    
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        selectedFile = mjFileChooser.getSelectedFile();
        getPanelData();
    }
    
    return selectedFile;
  }
  
  /**
   * get GUI widget status
   */
  private void getPanelData() {
    mbRecursive = mjRecursive.isSelected();
    mbPathInfo = mjPathInfo.isSelected();
    miCompressionLevel = mjLevelCombo.getSelectedIndex();
  }
  
  /**
   * set GUI widget status
   */
  private void setPanelData() {
    mjRecursive.setSelected(mbRecursive);
    mjPathInfo.setSelected(mbPathInfo);
    mjLevelCombo.setSelectedIndex(miCompressionLevel);
  }
  
}