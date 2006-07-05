/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.sleepycat.je.CheckpointConfig;
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
	HashMap <Integer, User>cache;
	//int keys[] = null;
	Vector <Integer>keys = null;
	
	public UserDatabase() throws DatabaseException {
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("User.db"), envConf);
		openDatabases();
		cache = new HashMap<Integer, User>();
		keys = new Vector<Integer>();
		this.refreshUserCount();
	}
	
	private void openDatabases() throws DatabaseException {
		//Create primary database, keyed by uid
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		db = env.openDatabase(null, "Users", dbConf);
		
		/*
		//Create index keyed by bannerid, maps bannerid to valid users for the banner
		SecondaryMultiKeyCreator bannerKey = new BannerKeyCreator(banners);
		SecondaryConfig bannerConf = new SecondaryConfig();
		bannerConf.setAllowCreate(true);
		bannerConf.setSortedDuplicates(true);
		bannerConf.setMultiKeyCreator(bannerKey);
		bannerDb = env.openSecondaryDatabase(null, "BannerUsers", db, bannerConf);
		*/
	}
	
	IntegerBinding ib = new IntegerBinding();
	DatabaseEntry key = new DatabaseEntry();
	UserBinding ub = new UserBinding();
	DatabaseEntry data = new DatabaseEntry();

	public void insert(User u) throws DatabaseException {
		ib.intToEntry(u.getID(), key);
		ub.objectToEntry(u, data);
		db.put(null, key, data);
		cache.put(this.userCount++, u);
	}
	
	public User getUser(Integer i){
		User u = (User)cache.get(i);
		if (u != null){
			return u;
		}
		throw new UnsupportedOperationException();
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
					if (value.getData() != null) {
						UserBinding ub = new UserBinding();
						User u = (User)ub.entryToObject(value);
						cache.put(u.id, u);
					}
				}
				this.userCount = i;
			} else {
				this.userCount = 0;
			}
			keys.clear();
			keys.addAll(cache.keySet());
			//keys = cache.getKeyArray();
		} catch (Exception dbe) {
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

	public Vector<User> getAllUsers() {
		Vector<User> users = new Vector<User>();
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
		this.closeDatabases();
		this.env.sync();
		env.cleanLog();
		CheckpointConfig force = new CheckpointConfig();
        force.setForce(true);
        env.checkpoint(force);
		this.env.close();
		this.env = null;
	}
	
	private void closeDatabases() throws DatabaseException {
		this.db.close();
		//this.bannerDb.close();
	}
	
	
	public static void main(String args[]){
		UserDatabase user_db = null;
		//Environment dbEnv = null;
		try {
			//BannerDatabase bdb = new BannerDatabase();
			user_db = new UserDatabase();
			
			System.out.println(user_db.userCount);
			
			/*Random r = new Random ();
			for (int i = 0; i < 100; i++){
				
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
			System.err.println("Unable to truncate user databases: " +e);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void dump() {
		Vector v = getAllUsers();
		for (int i=0; i<v.size(); i++) {
			System.out.println((User)v.get(i));
		}
		System.out.println("Total users: " + v.size());
		
	}

	public User getUserByIndex(int i) {
		return getUser(keys.get(i));
	}
}