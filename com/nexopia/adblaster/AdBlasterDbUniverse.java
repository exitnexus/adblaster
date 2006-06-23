package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {

	public AdBlasterDbUniverse(){
					
		try {
			UserDatabase user_db;
			BannerDatabase banner_db;
			banner_db = new BannerDatabase();
			user_db = new UserDatabase();
			
			this.init(user_db.getUserCount(), banner_db.banners.size());
			for (int i = 0; i < banner_db.banners.size(); i++){
				this.setBanner(i, (Banner)banner_db.banners.get(i));
			}
			user_db.refreshUserCount();
			System.out.println(user_db.getUserCount());
			Vector v = user_db.getAllUsers();
			for (int i = 0; i < v.size(); i++){
				this.setUser(i, (User)v.get(i));
			}
			user_db.close();
			//banner_db.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
