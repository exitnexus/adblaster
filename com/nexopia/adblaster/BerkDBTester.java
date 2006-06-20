/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * @author wolfe
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class BerkDBTester {

	public static void main(String[] args) {
		Environment dbEnv = null;
		BannerViewDatabase db = null;
		
		//try {
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
				dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
			Random r = new Random(1);
			for (int i=0; i<20000; i++) {
				BannerView x = new BannerView(new User(0), new Banner(Math.abs(r.nextInt())%100), Math.abs(r.nextInt())%5);
				db.insert(x);
			}
			BannerViewCursor c = db.getCursor(0,0);
			BannerView bv = c.getCurrent();
			System.out.println(bv);
			int i=0;
			while (bv != null) {
				System.out.println("Line: " + i + " - BannerID: " + bv.getBanner().getID() + " - TimeStamp: " + bv.getTime());
				i++;
				bv = c.getNext();
			}
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}
}

