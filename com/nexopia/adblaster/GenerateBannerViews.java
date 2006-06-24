/*
 * Created on Jun 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateBannerViews {
	static final int NUM_BANNERVIEWS = 10000;
	
	public static void main(String[] args) {
		UserDatabase uDb = null;
		BannerViewDatabase bvDb = null;
		try {
			BannerDatabase bdb = new BannerDatabase();
			uDb = new UserDatabase(bdb.getBanners());
			Vector users = uDb.getAllUsers();
			Object[] banners = bdb.getBanners().toArray();
			
			bvDb = new BannerViewDatabase();
			
			System.out.println(bvDb.getBannerViewCount());
			bvDb.empty();

			Random r = new Random();
			for (int i = 0; i < NUM_BANNERVIEWS; i++){
				User u = (User)users.get(r.nextInt(users.size()));
				Banner b = (Banner)banners[r.nextInt(banners.length)];
				int t = r.nextInt(86400); //seconds in the day
				bvDb.insert(new BannerView(null,i,u,b,t));
			}
			bvDb.dump();
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
			dbe.printStackTrace();
		} finally {
			try {
				uDb.close();
				bvDb.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}