package org.aspenos.util;

import java.util.*;
import java.io.*;
//import com.iplanet.portalserver.util.*;

import org.aspenos.logging.*;


/**
 *
 * @author P. Mark Anderson
 *
 */
public class StringTree {

/* ===== private fields ==================================*/
	private static HashMap _treeMap = null;
	private static HashMap _firstLevelChildren = null;
	private static ArrayList _treeList = null;
	private static String _separator = null;
	private LoggerWrapper _lw = null;

	/**
	 *
	 */
	public StringTree(Map m) {
		_separator = " > ";

		if (m == null)
			_treeMap = new HashMap();
		else
			_treeMap = (HashMap)m;

		loadFirstLevelChildren();
	}


	/**
	 *
	 */
	public StringTree(LoggerWrapper lw, Map m) {
		_lw = lw;
		_separator = " > ";

		if (m == null)
			_treeMap = new HashMap();
		else
			_treeMap = (HashMap)m;

		loadFirstLevelChildren();
	}


	/**
	 * Gets a list of each of a parent's children.  The
	 * root (parent) node will be included in the list.
	 * @param root The parent node at which to start
	 * @return List of the given parent's children,
	 *    each represented as a String with separators. 
	 *    The child shows the entire list of its parents.
	 *    Example:  parent > child1 > child2
	 */
	public List getChildren(String root) {
		return getChildren(root, true);
	}

	/**
	 * Gets a list of each of a parent's children.
	 *
	 * @param root The parent node at which to start
	 * @param includeRoot true if the root should be 
	 *                    included in the list
	 * @return List of the given parent's children,
	 *    each represented as a String with separators. 
	 *    The child shows the entire list of its parents.
	 *    Example:  parent > child1 > child2
	 */
	public List getChildren(String root, boolean includeRoot) {
		_treeList = new ArrayList();

		getChildrenRecursive( root, root, includeRoot );
		reverseTreeList();

		return (List)_treeList;
	}


	/**
	 *
	 */
	public void setSeparator(String s) {
		_separator = s;
	}

	/**
	 *
	 */
	public String getSeparator() {
		return _separator;
	}



/* ===== private methods =================================*/
	/**
	 * Recursively creates a string representing the
	 * path to a node for each of node's children.
	 * @param root The node at which to start
	 * @param includeRoot true if the root should be 
	 *                    included in the list
	 */
	private void getChildrenRecursive(String root, String fullName,
			boolean includeRoot) {

		ArrayList firstLevel = getFirstLevelChildren( root );

		if ( firstLevel == null ) {
			// This node is a leaf, so just add it to the list.
			if (includeRoot)
				_treeList.add( fullName );

		} else {

			// Get children of all first level nodes
			Iterator it = firstLevel.iterator();
			while ( it.hasNext() ) {
				String child = (String)it.next();
				StringBuffer fullChildName = 
					new StringBuffer();

				if (includeRoot) {
					fullChildName.append(fullName)
						.append(_separator);
				}

				fullChildName.append(child);

				getChildrenRecursive( child, fullChildName.toString(), true );
			}

			if (includeRoot)
				_treeList.add( fullName );
		}
	}
				

	/**
	 * Gets a list of only the first level of children
	 * for a given node.
	 * @param root The node at which to start
	 * @return a list of children, or null if there are none
	 */
	private ArrayList getFirstLevelChildren(String root) {
		return (ArrayList)_firstLevelChildren.get(root);
	}


	/**
	 * Loads a list of only the first level of children
	 * for a given node.  This is used as a cache.
	 */
	private void loadFirstLevelChildren() {

		ArrayList children = new ArrayList();
		_firstLevelChildren = new HashMap();

		// Iterate through each and every mapping in
		// order to find value matches.  For each 
		// match, store the key in a list.
		Set keys = _treeMap.keySet();

		Iterator mainIt = keys.iterator();
		while (mainIt.hasNext()) {
			String child  = (String)mainIt.next();
			String parent = (String)_treeMap.get( child );

			// Find all matching keys (children) for
			// this value (parent).
			if (!_firstLevelChildren.containsKey( parent ))
			{
				ArrayList tmpChildList = new ArrayList();
				Iterator tmpIt = keys.iterator();

				// Look for children that have a parent that
				// matches the current 'parent'
				while (tmpIt.hasNext()) {
					String tmpChild  = (String)tmpIt.next();
					String tmpParent = (String)_treeMap.get( tmpChild );

					// Add children with matching parents to a list
					if (tmpParent.equals( parent ))
						tmpChildList.add( tmpChild );
				}

				_firstLevelChildren.put(parent, tmpChildList);
			} // end if
		} // end while
	}


	/**
	 * Reverses the order of elements in _treeList.
	 */
	private void reverseTreeList()
	{
		if (_treeList == null)
			return;

		ArrayList tmp = new ArrayList();
		int i, size = _treeList.size();

		for (i=size-1; i >= 0; i--) {
			tmp.add( (String)_treeList.get( i ) );
		}

		_treeList = tmp;
	}




	public static void main(String[] args)
	{
		HashMap hash = new HashMap();
		
		// map child (key) to parent (value)
		hash.put("B", "A");
		hash.put("C", "A");
		hash.put("D", "B");
		hash.put("E", "B");
		hash.put("F", "C");
		hash.put("G", "C");
		hash.put("H", "D");
		hash.put("I", "D");
		hash.put("J", "E");
		hash.put("K", "E");
		hash.put("L", "F");
		hash.put("M", "F");
		
		System.out.println("\n\nCreating StringTree...");
		StringTree st = new StringTree(null, hash);

		st.setSeparator(" - ");
		System.out.println("Separator is:  " + st.getSeparator());


		String startNode = "A";
		System.out.println("Getting children starting at node '" +
				startNode + "'\n");
		List l = st.getChildren(startNode);
		Iterator it = l.iterator();
		while (it.hasNext()) {
			System.out.println((String)it.next());
		}


		System.out.println("\n\nGetting children starting at node '" +
				startNode + "',\nwithout the root node included.\n");
		l = st.getChildren(startNode, false);
		it = l.iterator();
		while (it.hasNext()) {
			System.out.println((String)it.next());
		}
	}

}



