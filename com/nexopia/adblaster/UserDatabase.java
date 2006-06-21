/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UserDatabase {
	Database db;
	private int userCount;
	
	public UserDatabase(Environment dbEnv) throws DatabaseException {
		//Create our primary database keyed by a unique ID
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
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
			return null;
		}
	}
	
	public int getUserCount(){
		return userCount;
	}
	
	public void refreshUserCount() {
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			Cursor c = db.openCursor(null, null);
			if (value.getData() != null) {
				int i = 1;
				while (c.getNext(key, value, null) != null) {
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
