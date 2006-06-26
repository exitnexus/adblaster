/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.SecondaryMultiKeyCreator;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class UserBannerDatabase {
	private Database db;
	//private SecondaryDatabase bannerDb;
	private Environment env;
	private Collection banners;
	Vector users;
	int userCount;
	
	public UserBannerDatabase() throws DatabaseException {
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(new File("UserBanner.db"), envConf);
		UserDatabase uDb = new UserDatabase();
		BannerDatabase bDb = new BannerDatabase();
		banners = bDb.getBanners();
		users = uDb.getAllUsers();
		openDatabases();
	}
	
	private void openDatabases() throws DatabaseException {
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		db = env.openDatabase(null, "UserBanner", dbConf);
	}
	
	public void insert(int userid, int bannerid) throws DatabaseException {
		UserBannerBinding ubb = new UserBannerBinding();
		DatabaseEntry key = new DatabaseEntry();
		int[] intArray = {userid, bannerid};
		ubb.objectToEntry(intArray, key);
		DatabaseEntry data = new DatabaseEntry();
		db.put(null, key, data);
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
		try {
			UserBannerDatabase ubDb = new UserBannerDatabase();
			ubDb.generate();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			System.err.println("Unable to truncate userbanner database: " +e);
			e.printStackTrace();
		}
	}
	
	public void generate() throws DatabaseException {
		for (Iterator i=banners.iterator(); i.hasNext();) {
			Banner b = (Banner)i.next();
			for (Iterator j=users.iterator(); j.hasNext();) {
				User u = (User) j.next();
				if (b.validUser(u)) {
					this.insert(b.getID(), u.getID());
				}
			}
		}
	}
}