/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.Iterator;
import java.util.List;

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
	private DatabaseEntry key = new DatabaseEntry();
	private DatabaseEntry data = new DatabaseEntry();
	private IntegerBinding ib = new IntegerBinding();
	
	public BannerViewDatabase() throws DatabaseException {
		//Create our primary database keyed by a unique ID
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("BannerView.db"), envConf);
		this.openDatabases();
	}
	
	public void insert(BannerView bv) throws DatabaseException  {
		try {
			lastid++;
			//ib.intToEntry(new Integer(lastid), key);
			ib.intToEntry(lastid, key);
			BannerViewBinding bvb = AdBlaster.instanceBinding;
			bvb.objectToEntry(bv, data);
			db.put(null, key, data);
		} catch (DatabaseException dbe) {
			lastid--;
			throw dbe;
		}
	}
	
	BannerTimeKeyCreator bt = new BannerTimeKeyCreator();
	DatabaseEntry searchKey = new DatabaseEntry();
	DatabaseEntry searchData = new DatabaseEntry();
	public BannerViewCursor getCursor(int bannerID, int initialTime, int index) throws DatabaseException {
		SecondaryCursor c = bannerTimeDb.openSecondaryCursor(null, null);
		int[] a = { bannerID, initialTime };
		bt.objectToEntry(a, searchKey);
		c.getSearchKeyRange(searchKey, searchData, null);
		return new BannerViewCursor(c);
	}
	
	public int getBannerViewCount() {
		return lastid;
	}
	
	public void close() throws DatabaseException {
		this.closeDatabases();
		env.close();
	}

	/**
	 * 
	 */
	public void empty() {
		try {
			this.closeDatabases();
			List databaseNames = env.getDatabaseNames();
			for (Iterator i=databaseNames.iterator(); i.hasNext(); ) {
				String name = (String)i.next();
				System.out.println("Truncating " +name+ "... ");
				System.out.println(env.truncateDatabase(null, name, true) + " records truncated.");
			}
			this.openDatabases();
		} catch (DatabaseException e) {
			System.err.println("Unable to truncate bannerview databases: " +e);
			e.printStackTrace();
		}
		
	}

	/**
	 * @throws DatabaseException
	 * 
	 */
	private void openDatabases() throws DatabaseException {
		//open the primary database
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
		
		//open a database keyed by BannerID, timestamp
		BannerTimeKeyCreator bannerTimeKey = new BannerTimeKeyCreator();
		SecondaryConfig bannerTimeConf = new SecondaryConfig();
		bannerTimeConf.setAllowCreate(true);
		bannerTimeConf.setSortedDuplicates(true);
		bannerTimeConf.setKeyCreator(bannerTimeKey);
		bannerTimeDb = env.openSecondaryDatabase(null, "BannerTimeViews", db, bannerTimeConf);

		//open a database keyed by userid
		SecondaryKeyCreator userKey = new UserBinding();
		SecondaryConfig userConf = new SecondaryConfig();
		userConf.setAllowCreate(true);
		userConf.setSortedDuplicates(true);
		userConf.setKeyCreator(userKey);
		userDb = env.openSecondaryDatabase(null, "UserViews", db, userConf);
		
	}

	/**
	 * @throws DatabaseException
	 * 
	 */
	private void closeDatabases() throws DatabaseException {
		bannerTimeDb.close();
		userDb.close();
		db.close();
	}

	/**index;
	 * 
	 */
	public void dump() {
		BannerViewCursor c;
		try {
			c = this.getCursor(0,0,0);
			BannerView bv = c.getCurrent();
			while (bv != null) {
				System.out.println(bv);
				bv = c.getNext();
			}
			c.close();
			System.out.println("Total: " + this.getBannerViewCount());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BannerView get(int index) {
		ib.intToEntry(Math.max(index,1), key);
		try {
			db.get(null, key, data, null);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BannerViewBinding bvb = AdBlaster.instanceBinding;
		bvb.setIndex(index);
		BannerView bv = (BannerView)bvb.entryToObject(data);
		return bv;
	}
}


