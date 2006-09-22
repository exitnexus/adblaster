/*
 * Created on Jun 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.util;

import java.util.StringTokenizer;
import java.util.Vector;

import com.nexopia.adblaster.db.PageDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities {
	
	
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

	public static PageValidator stringToPageValidator(String string) {
		if (string.equals("")){
			PageValidator val = new PageValidator1();
			return val;
		}				

		String[] pages = string.split(",");
		PageValidator1 val = new PageValidator1();
		for (int i=0; i<pages.length; i++) {
			if (i==0) {
				if (pages[i].equals("0")) {
					val.negated = true;
				} else {
					val.negated = false;
					val.add(pages[i]);
				}
			} else {
				val.add(pages[i]);
			}
		}
		return val;
	}

	public static PageValidator2 stringToPageNegationVector(String string, PageDatabase pageDb) {
		if (string.equals("")){
			PageValidator2 vec = new PageValidator2(pageDb);
			vec.add("");
			return vec;
		}				

		String[] pages = string.split(",");
		PageValidator2 v = new PageValidator2(pageDb);
		for (int i=0; i<pages.length; i++) {
			if (i==0) {
				if (pages[i].equals("0")) {
					v.add(Integer.NEGATE);
				} else {
					v.add(Integer.IDENTITY);
					int page = pageDb.getPage(pages[i]);
					if (page != 0) {
						v.add(Integer.valueOf(pageDb.getPage(pages[i])));
					}
				}
			} else {
				int page = pageDb.getPage(pages[i]);
				if (page != 0) {
					v.add(Integer.valueOf(pageDb.getPage(pages[i])));
				}
			}
		}
		return v;
	}

	public static Banner priorityChoose(Vector<Banner> valid) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void bannerDebug(String debugLog) {
		System.out.println(debugLog);
		
	}
}
