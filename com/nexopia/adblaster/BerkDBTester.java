/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;

/**
 * @author wolfe
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class BerkDBTester {
	static UserDatabase userDb;
	static HashMap bannerMap;
	
	public static void main(String[] args) {
		//load all banners into memory
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM banners";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println(rs.getFetchSize());
			HashMap banners = new HashMap();
			for (int i=0; rs.next(); i++) {
				banners.put(new Integer(rs.getInt("ID")), new Banner(rs));
			}
			/*//Display the Banners
			for (Iterator it = banners.values().iterator(); it.hasNext(); ){
				Banner b = (Banner)it.next();
				System.out.println(b.getID() + " " + b.getInterests());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Create our UserDB and BannerViewDB
		Environment dbEnv = null;
		BannerViewDatabase db = null;
		//userDb = null;
		//try {
		try {
			db = new BannerViewDatabase(); 
			
			Random r = new Random(1);
			/*
			for (int i=0; i<20000; i++) {
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4";
				User u = new User(userid, age, sex, loc, interests);
				Banner b = new Banner(2);
				BannerView bv = new BannerView(u,b,3);
				db.insert(bv);
			}
			db.close();
			dbEnv.close();
			/*/
			BannerViewCursor c = db.getCursor(0,0,0);
			BannerView bv = c.getCurrent();
			int i=0;
			while (bv != null) {
				System.out.println("Line: " + i + " - BannerID: " + bv.getBanner().getID() + " - TimeStamp: " + bv.getTime());
				i++;
				bv = c.getNext();
			}
			/*
			Cursor c = rawDb.openCursor(null,null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			if (c.getFirst(key, data, null) == OperationStatus.SUCCESS) {
				UserBinding ub = new UserBinding();
				User u = (User)ub.entryToObject(data);
				System.out.println(u);
				while (c.getNext(key,data,null) == OperationStatus.SUCCESS) {
					u = (User)ub.entryToObject(data);
					System.out.println(u);
				}
			}
			
			
			
			/*for (int i=0; i<20000; i++) {
				User u = udb.getUser(i);
				System.out.println(u);
			}
			/**/
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}
}

