/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.StringTokenizer;
import java.util.Vector;


class Interests{
	private Vector checked;
	
	Interests(){
	  checked = new Vector();
	}
	
	Interests(String interests) {
		StringTokenizer st = new StringTokenizer(interests, ",");
		while (st.hasMoreTokens()) {
			checked.add(new Integer(Integer.parseInt(st.nextToken())));
		}
	}
	
	boolean has(int k) {
		return checked.contains(new Integer(k));
	}
	
	void add(Integer i) {
		checked.add(i);
	}
	
	Vector getChecked() {
		return checked;
	}
	
}