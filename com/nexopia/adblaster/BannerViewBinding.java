/*
 * Created on Jun 16, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.WeakHashMap;

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
	
	AbstractAdBlasterUniverse ac;
	AbstractAdBlasterInstance inst;
	int currentIndex;
	boolean indexFresh = false;
	static WeakHashMap<BannerView, Boolean> weakmap = new WeakHashMap<BannerView, Boolean>();

	/*static {
		Runnable memoryMonitor = new Runnable(){
			public void run() {
				synchronized(this){
					while (true){
						try {
							this.wait(30000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Bannerviews in memory: " + weakmap.keySet().size());
					}
				}
			}
		};
		new Thread(memoryMonitor).start();
	}*/
	
	/* Pass (null, null) to get a "write only" binding */
	BannerViewBinding(AbstractAdBlasterUniverse universe, AbstractAdBlasterInstance i){
		ac = universe;
		inst = i;
	}

	/*Must be called before entryToObject is called.*/
	void setIndex(int i){
		currentIndex = i;
		indexFresh = true;
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	public Object entryToObject(TupleInput ti) {
		if (!indexFresh)
			throw new UnsupportedOperationException("You must call setIndex() first.");
		
		indexFresh = false;
		int bid = ti.readInt();
		int time = ti.readInt();
		int uid = ti.readInt();
		byte size = ti.readByte();
		int page = ti.readInt();
		BannerView bv = new BannerView(inst, currentIndex, uid, bid ,time, size, page);
		weakmap.put(bv, Boolean.TRUE);
		return bv;

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
		to.writeInt(bv.getBanner()==null?-1:bv.getBanner().getID());
		to.writeInt(bv.getTime());
		to.writeInt(bv.getUser().getID());
		to.writeByte(bv.getSize());
		to.writeInt(bv.getPage());

	}

}
