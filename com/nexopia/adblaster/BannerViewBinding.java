/*
 * Created on Jun 16, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class BannerViewBinding extends TupleBinding {

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	public Object entryToObject(TupleInput ti) {
		//TODO need to get Banner and User out from inside AdCampaign so this works
		Banner b = new Banner(ti.readInt());//, BerkDBTester.bannerMap);
		int time = ti.readInt();
		User u = new User(ti.readInt(), BerkDBTester.userDb);
		return new BannerView(u,b,time);
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	public void objectToEntry(Object obj, TupleOutput to) {
		/*User u = (User) obj;
		to.writeInt(u.getID());
		to.writeByte(u.getAge());
		to.writeByte(u.getSex());
		to.writeShort(u.getLocation());
		to.writeString(u.getInterests().toString());
		*/
		BannerView bv = (BannerView)obj;
		to.writeInt(bv.getBanner().getID());
		to.writeInt(bv.getTime());
		to.writeInt(bv.getUser().getID());

	}
}
