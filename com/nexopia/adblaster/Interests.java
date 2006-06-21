/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


class Interests{
	private HashMap checked;
	
	Interests(){
	  checked = new HashMap();
	}
	
	Interests(String interests) {
		StringTokenizer st = new StringTokenizer(interests, ",");
		while (st.hasMoreTokens()) {
			checked.put(new Integer(Integer.parseInt(st.nextToken())), Boolean.TRUE);
		}
	}
	
	boolean has(Integer k) {
		return checked.containsKey(k);
	}
	
	void add(Integer i) {
		checked.put(i, Boolean.TRUE);
	}
	
	Set getChecked() {
		return checked.keySet();
	}

	public boolean hasAllIn(Interests interests) {
		for (Iterator it = interests.getChecked().iterator(); it.hasNext(); ){
			Integer interest = (Integer)it.next();
			if (!has(interest)){
				return false;
			}
		}
		return true;
	}
	
}