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
import java.util.Vector;
import java.util.WeakHashMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
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
	private BannerViewBinding instanceBinding;
	UserBinding userKey;
	BannerTimeKeyCreator bt;
	
	/*public BannerViewDatabase() throws DatabaseException {
		//Create our primary database keyed by a unique ID
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("BannerView.db"), envConf);
		this.openDatabases();
	}*/
	
	public BannerViewDatabase(File f, BannerViewBinding bvb) throws DatabaseException {
		this.instanceBinding = bvb;
		if (!f.exists()){
			f.mkdir();
		}
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(f, envConf);
		this.openDatabases();
		bt = new BannerTimeKeyCreator(this.instanceBinding);
		userKey = new UserBinding();
	}

	public BannerViewDatabase(String string, BannerViewBinding bvb ) throws DatabaseException {
		this(new File("BannerView.db." + string), bvb);
	}

	public void insert(DbBannerView bv) throws DatabaseException  {
		try {
			lastid++;
			//ib.intToEntry(new Integer(lastid), key);
			ib.intToEntry(lastid, key);
			BannerViewBinding bvb = bv.getInstance().instanceBinding;
			bvb.objectToEntry(bv, data);
			db.put(null, key, data);
		} catch (DatabaseException dbe) {
			lastid--;
			throw dbe;
		}
	}
	
	public void insert(int uid, int bid, int time, byte size, int page) throws DatabaseException {
		try {
			lastid++;
			//ib.intToEntry(new Integer(lastid), key);
			ib.intToEntry(lastid, key);
			ib.intBannerViewToEntry(bid,time,uid,size,page, data);
			db.put(null, key, data);
		} catch (DatabaseException dbe) {
			lastid--;
			throw dbe;
		}
	}
	
	DatabaseEntry searchKey = new DatabaseEntry();
	DatabaseEntry searchData = new DatabaseEntry();
	public BannerViewCursor getCursor(int bannerID, int initialTime, int index) throws DatabaseException {
		SecondaryCursor c = bannerTimeDb.openSecondaryCursor(null, null);
		int[] a = { bannerID, initialTime, index };
		bt.objectToEntry(a, searchKey);
		c.getSearchKeyRange(searchKey, searchData, null);
		return new BannerViewCursor(c, this.instanceBinding);
	}

	public BannerViewCursor getCursor() throws DatabaseException {
		Cursor c = db.openCursor(null, null);
		ib.intToEntry(0, searchKey);
		c.getSearchKeyRange(searchKey, searchData, null);
		return new BannerViewCursor(c, this.instanceBinding);
	}

	public int getBannerViewCount() {
		return lastid;
	}
	
	public synchronized void close() throws DatabaseException {
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
	static TwoIntegerBinding tib = new TwoIntegerBinding();
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
		BannerTimeKeyCreator bannerTimeKey = new BannerTimeKeyCreator(this.instanceBinding);
		SecondaryConfig bannerTimeConf = new SecondaryConfig();
		bannerTimeConf.setAllowCreate(true);
		//bannerTimeConf.setSortedDuplicates(true);
		bannerTimeConf.setKeyCreator(bannerTimeKey);
		bannerTimeDb = env.openSecondaryDatabase(null, "BannerTimeViews", db, bannerTimeConf);

		//open a database keyed by userid
		SecondaryKeyCreator userKey = new SecondaryKeyCreator(){

			public boolean createSecondaryKey(SecondaryDatabase userKeyDB, DatabaseEntry key, DatabaseEntry data, DatabaseEntry secondaryKey) throws DatabaseException {
				BannerViewBinding bvb = instanceBinding;
				bvb.setIndex(ib.entryToInt(key));
				BannerView bv = (BannerView) bvb.entryToObject(data);
				int uid = bv.getUserID();
				tib.intsToEntry(uid, bv.getIndex(), secondaryKey);
				return true;
			}
			
		};
		SecondaryConfig userConf = new SecondaryConfig();
		userConf.setAllowCreate(true);
		//userConf.setSortedDuplicates(true);
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

		BannerViewBinding bvb = instanceBinding;
		bvb.setIndex(index);
		BannerView bv = (BannerView)bvb.entryToObject(data);
		return bv;
	}
	
	public Vector <BannerView> getByUser(int uID) {
		Vector <BannerView>vec = new Vector<BannerView>();
		tib.intsToEntry(uID, 0, key);
		Cursor cursor = null;
		try {
			cursor = userDb.openCursor(null, null);
			cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);

			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (data.getData() != null) {
					BannerViewBinding bvb = instanceBinding;
					bvb.setIndex(ib.entryToInt(key));
					BannerView bv = (BannerView)bvb.entryToObject(data);
					//System.out.println(bv);
					if (bv.getUserID() != uID){
						break;
					}
					vec.add(bv);
				}
			}
			cursor.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return vec;
	}
	
	public static class TwoIntegerBinding extends TupleBinding {

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
		/*WARNING: This is incorrect, but its never called*/
		public void objectToEntry(Object o, TupleOutput to) {
			Integer i = (Integer)o;
			to.writeInt(i.intValue());
		}

	    protected TupleOutput getTupleOutput() {
	        int byteSize = getTupleBufferSize();
	        if (byteSize != 0) {
	            return new TupleOutput(new byte[byteSize]);
	        } else {
	            return new TupleOutput();
	        }
	    }

		public void intsToEntry(int i, int j, DatabaseEntry entry) {
	        TupleOutput output = getTupleOutput();
			output.writeInt(i);
			output.writeInt(j);
	        outputToEntry(output, entry);
		}

	}

}


