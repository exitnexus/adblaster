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
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class UserBinding extends TupleBinding implements SecondaryKeyCreator {
	IntegerBinding ib = new IntegerBinding();

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	public Object entryToObject(TupleInput ti) {
		//TODO need to get Banner and User out from inside AdCampaign so this works
		int id = ti.readInt();
		byte age = ti.readByte();
		byte sex = ti.readByte();
		short loc = ti.readShort();
		int len = ti.getStringByteLength()-1;
		String s = "";
		if (len > 0){
			try {
				s = ti.readString(len);
			} catch (IllegalArgumentException e){
				System.err.println("Warning... did not load interests.");
				e.printStackTrace();
				s = "";   
			}
		}
		//System.out.println("Creating user " + id);
		User u = new User(id, age, sex, loc, s);
		return u;
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	public void objectToEntry(Object obj, TupleOutput to) {
		User u = (User) obj;
		to.writeInt(u.getID());
		to.writeByte(u.getAge());
		to.writeByte(u.getSex());
		to.writeShort(u.getLocation());
		to.writeString(u.getInterests().toString());
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryKeyCreator#createSecondaryKey(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry)
	 */
	TwoIntegerBinding tib = new TwoIntegerBinding();
	public boolean createSecondaryKey(SecondaryDatabase userKeyDB, DatabaseEntry key, DatabaseEntry data, DatabaseEntry secondaryKey) throws DatabaseException {
		BannerViewBinding bvb = AdBlaster.instanceBinding;
		bvb.setIndex(ib.entryToInt(key));
		BannerView bv = (BannerView) bvb.entryToObject(data);
		User u = bv.getUser();
		tib.intsToEntry(u.getID(), bv.getIndex(), secondaryKey);
		return true;
	}

	private class TwoIntegerBinding extends TupleBinding {

		/* (non-Javadoc)
		 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
		 */
		public Object entryToObject(TupleInput ti) {
			return new Integer(ti.readInt());
		}

		public int entryToInt(DatabaseEntry entry) {
	        return entryToInput(entry).readInt();
		}

		/* (non-Javadoc)
		 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
		 */
		public void objectToEntry(Object o, TupleOutput to) {
			Integer i = (Integer)o;
			to.writeInt(i.intValue());
		}

	    protected TupleOutput getTupleOutput(int i) {
	        int byteSize = getTupleBufferSize();
	        if (byteSize != 0) {
	            return new TupleOutput(new byte[byteSize]);
	        } else {
	            return new TupleOutput();
	        }
	    }

		public void intsToEntry(int i, int j, DatabaseEntry entry) {
	        TupleOutput output = getTupleOutput(i);
			output.writeInt(i);
			output.writeInt(j);
	        outputToEntry(output, entry);
		}

	}

}


