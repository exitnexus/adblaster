/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UserDatabase {
	private Database db;
	private int userCount;
	
	public UserDatabase(Environment dbEnv) throws DatabaseException {
		//Create our primary database keyed by a unique ID
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		dbConf.setDeferredWrite(true);
		this.db = dbEnv.openDatabase(null, "Users", dbConf);
		this.refreshUserCount();
	}
	
	public void insert(User u) throws DatabaseException  {
		IntegerBinding ib = new IntegerBinding();
		DatabaseEntry key = new DatabaseEntry();
		ib.objectToEntry(new Integer(u.getID()), key);
		UserBinding ub = new UserBinding();
		DatabaseEntry data = new DatabaseEntry();
		ub.objectToEntry(u, data);
		db.put(null, key, data);
		DatabaseEntry result = new DatabaseEntry();
		db.get(null, key, result, null);
		this.userCount++;
		db.sync();
	}
	
	public User getUser(int userid) throws DatabaseException {
		IntegerBinding ib = new IntegerBinding();
		DatabaseEntry key = new DatabaseEntry();
		ib.objectToEntry(new Integer(userid), key);
		DatabaseEntry data = new DatabaseEntry();
		db.get(null, key, data, null);
		if (data.getData() != null) {
			UserBinding ub = new UserBinding();
			User u = (User)ub.entryToObject(data);
			return u;
		} else {
			System.out.println("Invalid user.");
			return null;
		}
	}
	
	public int getUserCount(){
		return userCount;
	}
	
	public static void main(String args[]){
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			Environment dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			
			UserDatabase user_db = new UserDatabase(dbEnv);
			
			System.out.println(user_db.userCount);
			
			Random r = new Random ();
			for (int i = 0; i < 100; i++){
				
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4";
				User u;
				user_db.insert(u=new User(userid, age, sex, loc, interests));
				System.out.println("Inserted " + u);
			}
			user_db.refreshUserCount();
			System.out.println(user_db.getUserCount());
			for (int i = 0; i < 100; i++){
				User u = user_db.getUser(i);
				System.out.println(u);
			}
		
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
	}
	
	public void refreshUserCount() {
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			Cursor c = db.openCursor(null, null);
			if (c.getFirst(key, value, null) == OperationStatus.SUCCESS) {
				int i = 1;
				while (c.getNext(key, value, null) == OperationStatus.SUCCESS) {
					i++;
				}
				this.userCount = i;
			} else {
				this.userCount = 0;
			}
		} catch (DatabaseException dbe) {
			this.userCount = 0;
			System.err.println("Database Exception in refreshUserCount(): " + dbe);
			dbe.printStackTrace();
		}
		
	}
	
	
}
