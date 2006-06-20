/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryCursor;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BannerViewCursor {
	SecondaryCursor c;
	public BannerViewCursor(SecondaryCursor c) {
		this.c = c;
	}
	
	public BannerView getNext() {
		DatabaseEntry data = new DatabaseEntry();
		DatabaseEntry key = new DatabaseEntry();
		try {
			c.getNext(key, data, null);
		} catch (DatabaseException e) {
			System.out.println("DatabaseException: " + e);
			e.printStackTrace();
			return null;
		}
		if (data.getData() != null) {
			BannerViewBinding bvb = new BannerViewBinding();
			BannerView bv = (BannerView)bvb.entryToObject(data);
			return bv;
		} else {
			return null;
		}
	}
	
	public BannerView getCurrent() {
		DatabaseEntry data = new DatabaseEntry();
		try {
			c.getCurrent(new DatabaseEntry(), data, null);
		} catch (DatabaseException e) {
			System.out.println("Database Exception: " + e);
			e.printStackTrace();
			return null;
		}
		if (data.getData() != null) {
			BannerViewBinding bvb = new BannerViewBinding();
			BannerView bv = (BannerView)bvb.entryToObject(data);
			return bv;
		} else {
			return null;
		}
	}
}
