/*
 * Created on Jun 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.StringTokenizer;
import java.util.Vector;

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
}
