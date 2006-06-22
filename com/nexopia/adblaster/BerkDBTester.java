/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;

/**
 * @author wolfe
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class BerkDBTester {
	static UserDatabase userDb;
	
	public static void main(String[] args) {
		Environment dbEnv = null;
		BannerViewDatabase db = null;
		userDb = null;
		//try {
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			System.out.println("No Write Sync: " + envConf.getTxnWriteNoSync() );
			System.out.println("No Sync: " + envConf.getTxnNoSync() );
			System.out.println("envConf: " + envConf);
			System.out.println("Locking: " + envConf.getLocking());
			System.out.println("Transactional: " + envConf.getTransactional());
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			UserDatabase udb = new UserDatabase(dbEnv);
			Random r = new Random(1);
			for (int i=0; i<20000; i++) {
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4";
				User u = new User(userid, age, sex, loc, interests);
				udb.insert(u);
			}
			userDb.close();
			dbEnv.close();
			/*/
			Cursor c = rawDb.openCursor(null,null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			if (c.getFirst(key, data, null) == OperationStatus.SUCCESS) {
				UserBinding ub = new UserBinding();
				User u = (User)ub.entryToObject(data);
				System.out.println(u);
				while (c.getNext(key,data,null) == OperationStatus.SUCCESS) {
					u = (User)ub.entryToObject(data);
					System.out.println(u);
				}
			}
			
			
			
			/*for (int i=0; i<20000; i++) {
				User u = udb.getUser(i);
				System.out.println(u);
			}
			/*BannerViewCursor c = db.getCursor(0,0);
			BannerView bv = c.getCurrent();
			int i=0;
			while (bv != null) {
				System.out.println("Line: " + i + " - BannerID: " + bv.getBanner().getID() + " - TimeStamp: " + bv.getTime());
				i++;
				bv = c.getNext();
			}*/
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}
}

