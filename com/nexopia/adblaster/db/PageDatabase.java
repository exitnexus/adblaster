/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.db;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;

import com.nexopia.adblaster.IntegerBinding;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.sleepycat.bind.tuple.StringBinding;
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

public class PageDatabase {
	
	private Database db;
	private Environment env;
	int userCount;
	IntObjectHashMap cache;
	int keys[] = null;
	int lastid;
	int count;
	//Vector <Integer>keys = null;
	
	public PageDatabase(File f) throws DatabaseException{
		if (!f.exists()){
			f.mkdir();
		}
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);
		env = new Environment(f, envConf);
		cache = new IntObjectHashMap();
		openDatabases();
	}


	public PageDatabase(String string) throws DatabaseException {
		this(new File("Page.db." + string));
	}


	private void openDatabases() throws DatabaseException {
		//Create primary database, keyed by page string
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		this.db = this.env.openDatabase(null, "Pages", dbConf);
		this.refreshPageCount();
	}
	
	IntegerBinding ib = new IntegerBinding();
	StringBinding sb = new StringBinding();
	DatabaseEntry key = new DatabaseEntry();
	DatabaseEntry data = new DatabaseEntry();

	public synchronized int insert(String page) throws DatabaseException {
		lastid++;
		ib.intToEntry(lastid, data);
		sb.objectToEntry(page, key);
		if (db.putNoOverwrite(null, key, data) != OperationStatus.KEYEXIST) {
			cache.put(lastid, page);
			System.out.println("Added page \"" + page + "\" with index: " + lastid);
			return lastid;
		} else {
			lastid--;
			sb.objectToEntry(page, key);
			db.get(null, key, data, null);
			return ib.entryToInt(data);
		}
		
	}
	
	public String getPage(int i){
		return (String)cache.get(i);
	}

	public int getPage(String page) {
		sb.objectToEntry(page, key);
		try {
			if (db.get(null, key, data, null) == OperationStatus.SUCCESS) {
				return ib.entryToInt(data);
			} else {
				return 0;
			}
		} catch (DatabaseException dbe) {
			System.err.println("Database error loading page from database, returning 0 index.");
			return 0;
		}
	}
	
	public void refreshPageCount() throws DatabaseException {
		Cursor c = this.db.openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		if (c.getFirst(key, value, null) == OperationStatus.SUCCESS) {
			this.count = 1;
			this.lastid = this.ib.entryToInt(value);
			cache.put(lastid, sb.entryToObject(key));
			while (c.getNext(key, value, null) == OperationStatus.SUCCESS) {
				count++;
				int curid = this.ib.entryToInt(value);
				cache.put(curid, sb.entryToObject(key));
				if (curid > this.lastid) {
					this.lastid = curid;
				}
			}
		} else {
			this.lastid = 0;
			this.count = 0;
		}
		c.close();
	}

	
	
	public synchronized void close() throws DatabaseException {
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

	public void dump() {
		try {
			Cursor c = this.db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			if (c.getFirst(key, value, null) == OperationStatus.SUCCESS) {
				System.out.println(sb.entryToObject(key) + ":" + this.ib.entryToInt(value));
				while (c.getNext(key, value, null) == OperationStatus.SUCCESS) {
					System.out.println(sb.entryToObject(key) + ":" + this.ib.entryToInt(value));
				}
			}
			c.close();
		} catch (DatabaseException dbe) {
			System.err.println("error dumping page database");
			dbe.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PageDatabase pdb = null;
		try {
			pdb = new PageDatabase("test");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Starting dump...");
		pdb.dump();
		System.out.println("...dump complete.");
	}


	
}