/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BannerViewDatabase {
	Database db;
	SecondaryDatabase timeDb;
	int lastid;
	
	public BannerViewDatabase(Environment dbEnv) throws DatabaseException {
		//Create our primary database keyed by a unique ID
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		this.db = dbEnv.openDatabase(null, "PrimaryBannerViews", dbConf);
		Cursor cur = db.openCursor(null, null);
		DatabaseEntry lastKey = new DatabaseEntry();
		cur.getLast(lastKey, new DatabaseEntry(), null);
		
		if (lastKey != null && lastKey.getData() != null) {
			lastid = byteArrayToInt(lastKey.getData());
		} else {
			lastid = 0;
		}
		
		//Create a database keyed by BannerID, timestamp
		BannerTimeKeyCreator bannerTimeKey = new BannerTimeKeyCreator();
		SecondaryConfig bannerTimeConf = new SecondaryConfig();
		bannerTimeConf.setAllowCreate(true);
		bannerTimeConf.setSortedDuplicates(true);
		bannerTimeConf.setKeyCreator(bannerTimeKey);
		SecondaryDatabase bannerTimeDb = dbEnv.openSecondaryDatabase(null, "BannerTimeViews", db, bannerTimeConf);
		
		//Create a database keyed by BannerID
		BannerKeyCreator bannerKey = new BannerKeyCreator();
		SecondaryConfig bannerConf = new SecondaryConfig();
		bannerConf.setAllowCreate(true);
		bannerConf.setSortedDuplicates(true);
		bannerConf.setKeyCreator(bannerTimeKey);
		SecondaryDatabase bannerDb = dbEnv.openSecondaryDatabase(null, "BannerViews", db, bannerConf);
		
	}
	
	public void insert(BannerView bv) throws DatabaseException  {
		try {
			lastid++;
			DatabaseEntry key = new DatabaseEntry(intToByteArray(lastid));
			BannerViewBinding bvb = new BannerViewBinding();
			DatabaseEntry data = new DatabaseEntry();
			bvb.objectToEntry(bv, data);
			db.put(null, key, data);
		} catch (DatabaseException dbe) {
			lastid--;
			throw dbe;
		}
	}
	
	
	
	private static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
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
