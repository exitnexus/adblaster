/*
 * Created on Jun 19, 2006
 */
package com.nexopia.adblaster.db;

import com.nexopia.adblaster.IntegerBinding;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.Integer;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class BannerTimeKeyCreator extends TupleBinding implements SecondaryKeyCreator {
	BannerViewBinding instanceBinding;
	public BannerTimeKeyCreator(BannerViewBinding bvb) {
		this.instanceBinding = bvb;
	}
	
	IntegerBinding IBinstance = new IntegerBinding();
	
	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryKeyCreator#createSecondaryKey(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry)
	 */
	public boolean createSecondaryKey(SecondaryDatabase db,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry secondaryKey)
			throws DatabaseException {
		BannerViewBinding bvb = instanceBinding;
		Integer i = (Integer)(IBinstance.entryToObject(key));
		bvb.setIndex(i.intValue());
		BannerView bv = (BannerView)bvb.entryToObject(data);
		int[] a = { bv.getBannerId(), bv.getTime(), bv.getIndex() };
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
		obj[2] = ti.readInt(); //index
		return obj;
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	public void objectToEntry(Object obj, TupleOutput to) {
		int[] a = (int[])obj;
		to.writeInt(a[0]); //bannerid
		to.writeInt(a[1]); //time
		to.writeInt(a[2]); //index
	}
}
