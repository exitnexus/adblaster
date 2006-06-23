/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.Random;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

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
			uDb.dump(); //print contents of current database
			
			uDb.empty(); //wipe the current contents of the user database
			
			Random r = new Random ();
			for (int i = 0; i < 100; i++){
				User u =User.generateRandomUser();
//				
				uDb.insert(u);
				uDb.insert(u);
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4,6,7,8,9";
				
				//uDb.insert(u=new User(userid, age, sex, loc, interests));
				System.out.println("Inserted " + u);
			}

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
	/*
	BannerDatabase bDb = new BannerDatabase();
		UserDatabase uDb = null;
		
		try {
			uDb = new UserDatabase(bDb.getBanners());
		} catch (DatabaseException e) {
			System.err.println("Unable to open user database: "+e);
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		uDb.dump(); //print contents of current database
		
		uDb.empty(); //wipe the current contents of the user database
		
		try {
			for (int i=0;i<NUM_USERS;i++) {
				uDb.insert(User.generateRandomUser());
			}
		} catch (DatabaseException e1) {
			System.err.println("Error while inserting random users: "+ e1);
			e1.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try {
			uDb.close();
		} catch (DatabaseException e2) {
			System.err.println("Error while closing user database: "+ e2);
			e2.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		System.gc();
	}*/
}
