/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BannerViewCursor {
	Cursor c;
	BannerViewBinding instanceBinding;
	public BannerViewCursor(Cursor c, BannerViewBinding bvb) {
		this.c = c;
		this.instanceBinding = bvb;
	}
	
	DatabaseEntry data = new DatabaseEntry();
	DatabaseEntry key = new DatabaseEntry();
	IntegerBinding ib = new IntegerBinding();
	public BannerView getNext() {
		try {
			if (c.getNextNoDup(key, data, null) == OperationStatus.NOTFOUND)
				return null;
		} catch (DatabaseException e) {
			System.out.println("DatabaseException: " + e);
			e.printStackTrace();
			return null;
		}
		if (data.getData() != null) {
			BannerViewBinding bvb = instanceBinding;
			bvb.setIndex(ib.entryToInt(key));
			BannerView bv = (BannerView)bvb.entryToObject(data);
			return bv;
		} else {
			return null;
		}
	}
	
	public BannerView getCurrent() {
		try {
			c.getCurrent(key, data, null);
		} catch (DatabaseException e) {
			System.out.println("Database Exception: " + e);
			e.printStackTrace();
			return null;
		}
		if (data.getData() != null) {
			BannerViewBinding bvb = instanceBinding;
			bvb.setIndex(ib.entryToInt(key));
			BannerView bv = (BannerView)bvb.entryToObject(data);
			return bv;
		} else {
			return null;
		}
	}
	
	public void close() throws DatabaseException {
		c.close();
	}
}
