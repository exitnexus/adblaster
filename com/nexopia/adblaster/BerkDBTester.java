/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster;

import java.io.File;

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
		
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
			BannerViewCursor c = db.getCursor(0,0);
			BannerView bv = c.getCurrent();
			int i=0;
			while (bv != null) {
				System.out.println("Line: " + i + " - BannerID: " + bv.getBanner().getID() + " - TimeStamp: " + bv.getTime());
				i++;
				bv = c.getNext();
			}
		} catch (DatabaseException dbe) {
			System.err.println("Exception: " + dbe);
		} finally {
		}
		
	}
}

