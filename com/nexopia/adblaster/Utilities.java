/*
 * Created on Jun 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.StringTokenizer;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities {
	public static PageDatabase pageDb;
	static {
		try {
			pageDb = new PageDatabase();
		} catch (DatabaseException dbe) {
			System.err.println("Unable to open the page database, terminating.");
			dbe.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static Vector<Integer> stringToVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector<Integer> v = new Vector<Integer>();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		return v;
	}
	
	public static Vector<Integer> stringToNegationVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector<Integer> v = new Vector<Integer>();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		
		if (v.isEmpty()) {
			v.add((Integer.NEGATE));
		} else if (v.get(0).intValue() == 0) {
			v.setElementAt(Integer.NEGATE, 0);
		} else {
			v.insertElementAt(Integer.IDENTITY, 0);
		}
		
		return v;
	}

	public static Vector<Integer> stringToPageNegationVector(String string) {
		if (string.equals("")){
			Vector<Integer> vec = new Vector<Integer>();
			vec.add(Integer.NEGATE);
			return vec;
		}				

		String[] pages = string.split(",");
		Vector<Integer> v = new Vector<Integer>();
		for (int i=0; i<pages.length; i++) {
			if (i==0) {
				if (pages[i].equals("0")) {
					v.add(Integer.NEGATE);
				} else {
					v.add(Integer.IDENTITY);
					int page = Utilities.pageDb.getPage(pages[i]);
					if (page != 0) {
						v.add(Integer.valueOf(Utilities.pageDb.getPage(pages[i])));
					}
				}
			} else {
				int page = Utilities.pageDb.getPage(pages[i]);
				if (page != 0) {
					v.add(Integer.valueOf(Utilities.pageDb.getPage(pages[i])));
				}
			}
		}
		return v;
	}
}
