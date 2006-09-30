/**
 * 
 */
package com.nexopia.adblaster.util;

import java.util.Vector;

import com.nexopia.adblaster.db.PageDatabase;
import com.nexopia.adblaster.struct.BannerView;

public class PageValidator2 implements PageValidator{
	Vector<Integer> pages;
	PageDatabase pdb;
	
	public PageValidator2(PageDatabase pdb){
		this.pdb = pdb;
		pages = new Vector<Integer>();
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