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
	public static Vector stringToVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector v = new Vector();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		return v;
	}
}
