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

	static abstract interface PageValidator{
		public boolean validate(String page);
		public PageValidator clone();
		public void make(String string);
		public boolean validate(BannerView bv);
	}
	
	public static class PageValidator1 implements PageValidator{
		boolean negated;
		Vector <String>pages;
		public PageValidator1(){
			negated = true;
			pages = new Vector<String>();
		}
		public PageValidator clone(){
			PageValidator1 pv = new PageValidator1();
			pv.negated = negated;
			pv.pages = (Vector<String>) this.pages.clone();
			return pv;
		}
		public void add(String string) {
			pages.add(string);
		}
		public boolean validate(String page){
			if (negated) {
				for (int i=1; i<pages.size(); i++) {
					if (pages.get(i).equals(page)) {
						return false;
					}
				}
				return true;
			} else {
				for (int i=1; i<pages.size(); i++) {
					if (pages.get(i).equals(page)) {
						return true;
					}
				}
				return false;
			}
		}
		public void make(String string) {
			if (string.equals("")){
				this.negated = true;
				return;
			}				

			String[] pages = string.split(",");
			for (int i=0; i<pages.length; i++) {
				if (i==0) {
					if (pages[i].equals("0")) {
						negated = true;
					} else {
						negated = false;
						add(pages[i]);
					}
				} else {
					add(pages[i]);
				}
			}
		}
		public boolean validate(BannerView bv) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class PageValidator2 implements PageValidator{
		Vector<Integer> pages;
		PageDatabase pdb;
		PageValidator2(PageDatabase pdb){
			this.pdb = pdb;
		}
		public PageValidator clone(){
			PageValidator2 pv = new PageValidator2(pdb);
			pv.pages = (Vector<Integer>) this.pages.clone();
			return pv;
		}
		public boolean validate(int page){
			if (pages.get(0) == Integer.NEGATE) {
				for (int i=1; i<pages.size(); i++) {
					if (pages.get(i).intValue() == page) {
						return false;
					}
				}
				return true;
			} else {
				for (int i=1; i<pages.size(); i++) {
					if (pages.get(i).intValue() == page) {
						return true;
					}
				}
				return false;
			}
		}
		public boolean validate(String p) {
			int page = pdb.getPage(p);
			return validate(page);
		}
		public void add(String page) {
			if (page == "")
				pages.add(Integer.NEGATE);
			else
				pages.add(new Integer(pdb.getPage(page)));
		}
		public void add(Integer page) {
			pages.add(page);
		}
		public void make(String string) {
			if (string.equals("")){
				add("");
			}				

			String[] pages = string.split(",");
			for (int i=0; i<pages.length; i++) {
				if (i==0) {
					if (pages[i].equals("0")) {
						add(Integer.NEGATE);
					} else {
						add(Integer.IDENTITY);
						int page = pdb.getPage(pages[i]);
						if (page != 0) {
							add(Integer.valueOf(pdb.getPage(pages[i])));
						}
					}
				} else {
					int page = pdb.getPage(pages[i]);
					if (page != 0) {
						add(Integer.valueOf(pdb.getPage(pages[i])));
					}
				}
			}
		}
		public boolean validate(BannerView bv) {
			return validate(bv.getPage());
		}
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
		// TODO Auto-generated method stub
		
	}
}
