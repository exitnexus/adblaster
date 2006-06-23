/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


class Interests{
	HashMap checked;
	private static final Random rand = new Random();
	Interests(){
	  checked = new HashMap();
	}
	
	Interests(String interests) {
		this();
		StringTokenizer st = new StringTokenizer(interests, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.length() == 0) {
				continue;
			}
			try {
				checked.put(new Integer(Integer.parseInt(token)), Boolean.TRUE);
			} catch (NumberFormatException nfe) {
				System.err.println("Number Format Exception creating Interests: "+ nfe);
				nfe.printStackTrace();
				//do nothing for any value that can't be parsed as an in
			}
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

	public void putAll(Vector vector) {
		// TODO Auto-generated method stub
		
	}
	
	public String toString() {
		String interests = "";
		boolean first = true;
		for (Iterator it = this.getChecked().iterator(); it.hasNext(); ){
			if (first) {
				first = false;
				interests += (Integer)it.next();
			} else {
				interests += "," + (Integer)it.next();
			}
		}
		return interests;
	}

	/**
	 * @param userInterests
	 * @return
	 */
	public boolean containsAny(Interests userInterests) {
		for (Iterator it = userInterests.getChecked().iterator(); it.hasNext(); ){
			Integer interest = (Integer)it.next();
			if (checked.containsKey(interest)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public static Interests generateRandomInterests() {
		Interests interests = new Interests();
		int numberOfInterests = rand.nextInt(20);
		for (int i=0;i<numberOfInterests;i++) {
			interests.add(new Integer(rand.nextInt(200)));
		}
		return interests;
	}
	
}