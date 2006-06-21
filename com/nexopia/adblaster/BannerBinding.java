/*
 * Created on Jun 16, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.StringTokenizer;
import java.util.Vector;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/*
We shouldn't be using this, we don't want to store a local copy of banners
class BannerBinding extends TupleBinding {

	public Object entryToObject(TupleInput ti) {
		//TODO need to get Banner and User out from inside AdCampaign so this works
		int id = ti.readInt();
		int payrate = ti.readInt();
		int maxHits = ti.readInt();
		Vector locations = buildVector(ti.readString(ti.getStringByteLength()));
		Vector ages = buildVector(ti.readString(ti.getStringByteLength()));
		Vector sexes = buildVector(ti.readString(ti.getStringByteLength()));
		Interests interests = new Interests(ti.readString(ti.getStringByteLength()));
		Banner b = new Banner(id, payrate, maxHits, locations, ages, sexes, interests);
		return b;
	}

	private Vector buildVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector v = new Vector();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		return v;
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		Banner b = (Banner) obj;
		to.writeInt(b.getID());
		to.writeInt(b.getPayrate());
		to.writeInt(b.getMaxHits());
		to.writeString(b.getLocations().toString());
		to.writeString(b.getAges().toString());
		to.writeString(b.getSexes().toString());
		to.writeString(b.getInterests().toString());
	}
}
*/