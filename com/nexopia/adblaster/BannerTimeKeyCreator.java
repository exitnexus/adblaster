/*
 * Created on Jun 19, 2006
 */
package com.nexopia.adblaster;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class BannerTimeKeyCreator extends TupleBinding implements SecondaryKeyCreator {
	public BannerTimeKeyCreator() {
	}
	
	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryKeyCreator#createSecondaryKey(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry)
	 */
	public boolean createSecondaryKey(SecondaryDatabase db,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry secondaryKey)
			throws DatabaseException {
		BannerViewBinding bvb = new BannerViewBinding();
		BannerView bv = (BannerView)bvb.entryToObject(data);
		int[] a = { bv.getBanner().getID(), bv.getTime() };
		objectToEntry(a, secondaryKey);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	public Object entryToObject(TupleInput ti) {
		int[] obj = new int[2];
		obj[0] = ti.readInt(); //bannerID
		obj[1] = ti.readInt(); //time
		return obj;
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	public void objectToEntry(Object obj, TupleOutput to) {
		int[] a = (int[])obj;
		to.writeInt(a[0]); //bannerid
		to.writeInt(a[1]); //time
	}
}
