/*
 * Created on Jun 19, 2006
 */
package com.nexopia.adblaster;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class TimeKeyCreator implements SecondaryKeyCreator {
	public TimeKeyCreator() {
	}
	
	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryKeyCreator#createSecondaryKey(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry)
	 */
	public boolean createSecondaryKey(SecondaryDatabase db,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry secondaryKey)
			throws DatabaseException {
		BannerViewBinding bvb = new BannerViewBinding();
		BannerView bv = (BannerView)bvb.entryToObject(data);
		secondaryKey.setData(intToByteArray(bv.getTime()));
		return true;
	}

	private static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}
