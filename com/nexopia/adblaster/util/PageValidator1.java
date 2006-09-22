/**
 * 
 */
package com.nexopia.adblaster.util;

import java.util.Vector;

import com.nexopia.adblaster.struct.BannerView;

public class PageValidator1 implements PageValidator{
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