/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.Random;

import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateUsers {
	static final int NUM_USERS = 10000;
	
	public static void main(String[] args) {
		UserDatabase uDb = null;
		try {
			BannerDatabase bdb = new BannerDatabase();
			uDb = new UserDatabase(bdb.getBanners()); //we're passing in an empty banners set here
			
			System.out.println(uDb.getUserCount());
			
			uDb.empty(); //wipe the current contents of the user database
			
			Random r = new Random ();
			for (int i = 0; i < NUM_USERS; i++){
				User u =User.generateRandomUser();
				uDb.insert(u);
			}
			uDb.dump();
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
			dbe.printStackTrace();
		} finally {
			try {
				uDb.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}