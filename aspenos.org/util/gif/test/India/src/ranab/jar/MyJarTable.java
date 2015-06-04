package ranab.jar;

import java.util.Vector;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import ranab.gui.MyGuiUtil;


/**
 * This is the table model to display Jar file.
 */
public 
class MyJarTable 
    extends AbstractTableModel
    implements MyJarObserver {
    
  private final static SimpleDateFormat mFormatter = new SimpleDateFormat ("dd,MMM,yy hh:mm a");
  private final static String[] mstHeader = {
    "Name", "Size", "Compressed Size", "Modification Time"
  };
  
  private Vector mTabelData = null;
  
  private Component mParent;
  private JTable mjTable;
  
  /**
   * initialize the table model
   */
  public MyJarTable(Component parent) {
    mTabelData = new Vector();
    mParent = parent;
    mjTable = new JTable(this);
    mjTable.setColumnSelectionAllowed(false);
    mjTable.setBackground(Color.white);
  }
  
  
  // TableModel implementation
  /**
   * get the number of columns
   */
  public int getColumnCount() {
    return mstHeader.length;
  }
  
  /**
   * get column name
   */
  public String getColumnName(int index) {
    return mstHeader[index];
  }
 
  /**
   * get column class
   */
  public Class getColumnClass(int columnIndex) {
    switch(columnIndex) {
      case 0:
        return String.class;
      
      case 1:
        return Long.class;
      
      case 2:
        return Long.class;
      
      case 3:
        return String.class;
      
      default:
        return String.class;
    }
  }
  
  /**
   * cell is not editable in this table
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
  }
  
  /**
   * get the number of rows
   */
  public int getRowCount() {
    return mTabelData.size();
  }
  
  /**
   * get cell value
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    ZipEntry ze = (ZipEntry)mTabelData.elementAt(rowIndex);
    switch(columnIndex) {
      case 0: 
        return ze.getName();
      case 1:
        return new Long(ze.getSize());
      case 2:
        return new Long(ze.getCompressedSize());
      case 3:
        return mFormatter.format(new Date(ze.getTime()));
      default:
        return null;
    }
  }
  
  
  // MyJarObserver implementation
  /**
   * start viewing - clear the old data
   */
  public void start() {
    int sz = mTabelData.size();
    if(sz != 0) {
      mTabelData.clear();
      fireTableRowsDeleted(0, sz-1);
    }
  }
  
  /**
   * set count - ignore
   */
  public void setCount(int count) {
  }
  
  /**
   * next entry found
   */
  public void setNext(ZipEntry je) {
    int row = mTabelData.size();
    mTabelData.add(je);
    fireTableRowsInserted(row, row);
  }
  
  /**
   * error messgae
   */
  public void setError(String errMsg) {
    MyGuiUtil.showErrorMessage(mParent, errMsg);
  }  
  
  /**
   * end viewing - ignore
   */
  public void end() {
  }
  
  
  /**
   * get the scroll pane that contains the table
   */
  public JScrollPane getTable() {
    return new JScrollPane(mjTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }
  
  
  /**
   * returns the selected zip entries
   */
  public ZipEntry[] getSelectedEntries() {
    int indices[] = mjTable.getSelectedRows();
    if(indices.length == 0) {
      return null;
    }
    
    ZipEntry entries[] = new ZipEntry[indices.length];
    for(int i=0; i<entries.length; i++) {
      entries[i] = (ZipEntry)mTabelData.elementAt(indices[i]);
    }
    return entries;
  }
  
} 