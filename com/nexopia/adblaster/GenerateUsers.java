/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateUsers {
	static final int NUM_USERS = 100;
	
	public static void main(String[] args) {
		UserDatabase uDb = null;
		try {
			uDb = new UserDatabase();
			
			System.out.println(uDb.getUserCount());
			
			uDb.empty(); //wipe the current contents of the user database
			
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