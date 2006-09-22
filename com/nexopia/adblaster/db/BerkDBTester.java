/*
 * Created on Jun 16, 2006
 */
package com.nexopia.adblaster.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import com.nexopia.adblaster.BannerViewCursor;
import com.nexopia.adblaster.BannerViewDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.Integer;

/**
 * DEPRECATED
 */

public class BerkDBTester {
	static UserDatabase userDb;
	static HashMap bannerMap;
	
	public static void main(String[] args) {
		//load all banners into memory
		System.out.println("1");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM banners";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println(rs.getFetchSize());
			HashMap<Integer, Banner> banners = new HashMap<Integer, Banner>();
			for (int i=0; rs.next(); i++) {
				Banner b = new Banner(rs, null, null);
				System.out.println(b);
				banners.put(new Integer(rs.getInt("ID")), b);
			}
			/*//Display the Banners
			for (Iterator it = banners.values().iterator(); it.hasNext(); ){
				Banner b = (Banner)it.next();
				System.out.println(b.getID() + " " + b.getInterests());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("2");
		//Create our UserDB and BannerViewDB
		//Environment dbEnv = null;
		BannerViewDatabase db = null;
		//userDb = null;
		//try {
		try {
			//db = new BannerViewDatabase("test"); 
			
			//Random r = new Random(1);
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
			c.getNext();
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

