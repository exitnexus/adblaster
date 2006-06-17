/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
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
		Database db = null;
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			DatabaseConfig dbConf = new DatabaseConfig();
			dbConf.setAllowCreate(true);
			db = dbEnv.openDatabase(null, "BerkDBTester1", dbConf);
			DatabaseEntry key = new DatabaseEntry(intToByteArray(1));
			DatabaseEntry value = new DatabaseEntry(intToByteArray(10000));
			db.put(null, key, value);
			DatabaseEntry result = new DatabaseEntry();
			db.get(null, key, result, null);
			System.out.println("result:" + result);
		} catch (DatabaseException dbe) {
			System.err.println("Exception: " + dbe);
		} finally {
		}
		System.out.println("Hello World!");
	}
	
	public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}
