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
	private Environment env;
	int userCount;
	
	public UserDatabase() throws DatabaseException {
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("User.db"), envConf);
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		db = env.openDatabase(null, "PrimaryBannerViews", dbConf);
		this.refreshUserCount();
	}
	
	public void insert(User u) throws DatabaseException {
		IntegerBinding ib = new IntegerBinding();
		DatabaseEntry key = new DatabaseEntry();
		ib.objectToEntry(new Integer(u.getID()), key);
		UserBinding ub = new UserBinding();
		DatabaseEntry data = new DatabaseEntry();
		ub.objectToEntry(u, data);
		db.put(null, key, data);
		this.userCount++;
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
	
	public void refreshUserCount() {
		Cursor c = null;
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			c = db.openCursor(null, null);
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
		} finally {
			try {
				c.close();
			} catch (DatabaseException e) {
				System.err.println("Failed to close cursor after refreshUserCount: "+e);
				e.printStackTrace();
			}
		}
	}

	public Vector getAllUsers() {
		Vector users = new Vector();
		Cursor c = null;
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			c = db.openCursor(null, null);
			if (c.getFirst(key, value, null) == OperationStatus.SUCCESS) {
				if (value.getData() != null) {
					UserBinding ub = new UserBinding();
					User u = (User)ub.entryToObject(value);
					users.add(u);
				} else {
					System.out.println("Invalid user.");
					return null;
				}
				while (c.getNext(key, value, null) == OperationStatus.SUCCESS) {
					if (value.getData() != null) {
						UserBinding ub = new UserBinding();
						User u = (User)ub.entryToObject(value);
						users.add(u);
					} else {
						System.out.println("Invalid user.");
						return null;
					}
				}
			}
		} catch (DatabaseException dbe) {
			System.err.println("Database Exception in getAllUsers(): " + dbe);
			dbe.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			try {
				c.close();
			} catch (DatabaseException e) {
				System.err.println("Failed to close cursor after getAllUsers: "+e);
				e.printStackTrace();
			}
		}
		return users;
 	}

	public int getUserCount() {
		return this.userCount;
	}
	
	public void close() throws DatabaseException {
		this.db.close();
		this.env.close();
	}
	
	public static void main(String args[]){
		UserDatabase user_db = null;
		Environment dbEnv = null;
		try {
			user_db = new UserDatabase();
			
			System.out.println(user_db.userCount);
			
			Random r = new Random ();
			/*for (int i = 0; i < 100; i++){
				
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4,6,7,8,9";
				User u;
				user_db.insert(u=new User(userid, age, sex, loc, interests));
				System.out.println("Inserted " + u);
			}*/
			user_db.refreshUserCount();
			System.out.println(user_db.getUserCount());
			for (int i = 0; i < 100; i++){
				User u = user_db.getUser(i);
				System.out.println(u);
			}
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
			dbe.printStackTrace();
		} finally {
			try {
				user_db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}