/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.io.File;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BannerViewDatabase {
	Database db;
	SecondaryDatabase bannerTimeDb;
	SecondaryDatabase userDb; //maps users to bannerviews
	Environment env;
	int lastid;
	int bannerviewcount;
	
	public BannerViewDatabase() throws DatabaseException {
		//Create our primary database keyed by a unique ID
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("BannerView.db"), envConf);
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		this.db = env.openDatabase(null, "PrimaryBannerViews", dbConf);
		Cursor cur = db.openCursor(null, null);
		DatabaseEntry lastKey = new DatabaseEntry();
		cur.getLast(lastKey, new DatabaseEntry(), null);
		cur.close();
		if (lastKey != null && lastKey.getData() != null) {
			IntegerBinding ib = new IntegerBinding();
			Integer i = (Integer)ib.entryToObject(lastKey); 
			lastid = i.intValue();
		} else {
			lastid = 0;
		}
		
		//Create a database keyed by BannerID, timestamp
		BannerTimeKeyCreator bannerTimeKey = new BannerTimeKeyCreator();
		SecondaryConfig bannerTimeConf = new SecondaryConfig();
		bannerTimeConf.setAllowCreate(true);
		bannerTimeConf.setSortedDuplicates(true);
		bannerTimeConf.setKeyCreator(bannerTimeKey);
		bannerTimeDb = env.openSecondaryDatabase(null, "BannerTimeViews", db, bannerTimeConf);

		//Create a database keyed by userid
		SecondaryKeyCreator userKey = new UserBinding();
		SecondaryConfig userConf = new SecondaryConfig();
		userConf.setAllowCreate(true);
		userConf.setSortedDuplicates(true);
		userConf.setKeyCreator(userKey);
		userDb = env.openSecondaryDatabase(null, "UserViews", db, userConf);
	}
	
	public void insert(BannerView bv) throws DatabaseException  {
		try {
			lastid++;
			IntegerBinding ib = new IntegerBinding();
			DatabaseEntry key = new DatabaseEntry();
			ib.objectToEntry(new Integer(lastid), key);
			BannerViewBinding bvb = new BannerViewBinding();
			DatabaseEntry data = new DatabaseEntry();
			bvb.objectToEntry(bv, data);
			db.put(null, key, data);
		} catch (DatabaseException dbe) {
			lastid--;
			throw dbe;
		}
	}
	
	public BannerViewCursor getCursor(int bannerID, int initialTime) throws DatabaseException {
		SecondaryCursor c = bannerTimeDb.openSecondaryCursor(null, null);
		BannerTimeKeyCreator bt = new BannerTimeKeyCreator();
		DatabaseEntry searchKey = new DatabaseEntry();
		int[] a = { bannerID, initialTime };
		bt.objectToEntry(a, searchKey);
		c.getSearchKeyRange(searchKey, new DatabaseEntry(), null);
		return new BannerViewCursor(c);
	}
	
	public int getBannerViewCount() {
		return lastid;
	}
	
	public void close() throws DatabaseException {
		bannerTimeDb.close();
		db.close();
		env.close();
	}
}
