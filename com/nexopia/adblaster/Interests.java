/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


class Interests{
	BitSet checked;
	private static final Random rand = new Random();
	boolean negate;
	
	//Interests(){
	//  checked = new BitSet();
	//}
	
	/*Interests(String interests) {
		this(interests, false);
		
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
	}*/
	
	public Interests(Interests interests) {
		checked = (BitSet) interests.checked.clone();
	}

	//if this constructor is used and true is passed in, then an empty
	//string will mean all interests are true, should be true for banners/campaigns false for users
	public Interests(String interests, boolean isBanner) {
		checked = new BitSet();
		if (isBanner){
			if (interests.length() == 0) {
				negate = true;
			} else {
				negate = false;
				String[] splitInterests = interests.split(",");
				for (String interest : splitInterests) {
					if (interest.equals("0")) {
						negate = true;
						continue;
					} else if (interest.equals("")){
						continue;
					} else {
						try {
							checked.set(Integer.parseInt(interest));
						} catch (NumberFormatException e) {
							System.err.println("Error parsing " + interest + " as an interest id.");
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			negate = false;
			String[] splitInterests = interests.split(",");
			for (String interest : splitInterests) {
				if (interest.equals("0") || interest.equals("")) {
					//intentionally skip. Anonymous user.
				} else {
					try {
						checked.set(Integer.parseInt(interest));
					} catch (NumberFormatException e) {
						System.err.println("Error parsing " + interest + " as an interest id.");
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void fill(String interests, boolean isUser) {
		if (checked == null) {
			checked = new BitSet();
		}
		checked.clear();
		negate = false;
		String[] splitInterests = interests.split(",");
		for (String interest : splitInterests) {
			if (interest.equals("0")) {
				if (!isUser){
					negate = true;
				}
				continue;
			} else if (interest.equals("")){
				continue;
			} else {
				try {
					checked.set(Integer.parseInt(interest));
				} catch (NumberFormatException e) {
					System.err.println("Error parsing " + interest + " as an interest id.");
					e.printStackTrace();
				}
			}
		}
	}

	
	
	boolean has(int k) {
		return checked.get(k);
	}
	
	public void add(int i) {
		checked.set(i);
	}
	
	/*Set getChecked() {
		return checked.keySet();
	}*/
	
	int getCount(){
		return checked.size();
	}

	public boolean matches(Interests interests) {
		boolean matches = checked.intersects(interests.checked);
		if (negate ^ interests.negate) {
			return !matches;
		} else {
			return matches;
		}
		/*
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
		*/
	}

	
	public String toString() {
		String interests = "";
		if (negate) {
			interests = "0,";
		}
		boolean first = true;
		for(int i=checked.nextSetBit(0); i>=0; i=checked.nextSetBit(i+1)) { 
			if (first) {
				first = false;
				interests += ""+i;
			} else {
				interests += "," + i;
			}
		}
		return interests;
	}
	
	public void clear() {
		checked.clear();
	}

	
	/**
	 * @return
	 */
	public static Interests generateRandomInterests() {
		Interests interests = new Interests("", false);
		int numberOfInterests = rand.nextInt(20);
		for (int i=0;i<numberOfInterests;i++) {
			interests.checked.set(rand.nextInt(200));
		}
		return interests;
	}

	
	
}