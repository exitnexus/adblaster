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
	private HashMap<Integer, Boolean> checked;
	private static final Random rand = new Random();
	
	Interests(){
	  checked = new HashMap<Integer, Boolean>();
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
				checked.put(Integer.valueOf(Integer.parseInt(token)), Boolean.TRUE);
			} catch (NumberFormatException nfe) {
				System.err.println("Number Format Exception creating Interests: "+ nfe);
				nfe.printStackTrace();
				//do nothing for any value that can't be parsed as an in
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Interests(Interests interests) {
		checked = (HashMap<Integer, Boolean>) interests.checked.clone();
	}

	public void fill(String interests) {
		String[] split = interests.split(",");
		if (checked == null) {
			checked = new HashMap<Integer, Boolean>();
		}
		if (!checked.isEmpty()) {
			for (Integer integer : checked.keySet()) {
				integer.free();
			}
			checked.clear();
		}
		for (int i=0; i<split.length; i++) {
			checked.put(Integer.valueOf(Integer.parseInt(split[i])), Boolean.TRUE);
		}
	}

	
	
	boolean has(Integer k) {
		return checked.containsKey(k);
	}
	
	public void add(Integer i) {
		checked.put(i, Boolean.TRUE);
	}
	
	Set getChecked() {
		return checked.keySet();
	}
	
	int getCount(){
		return checked.size();
	}

	public boolean hasAnyIn(Interests interests) {
		if (interests.getChecked().size() < 1){
			return true;
		}
		
		for (Iterator it = interests.getChecked().iterator(); it.hasNext(); ){
			Integer interest = (Integer)it.next();
			if (has(interest)){
				return true;
			}
		}
		return false;
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