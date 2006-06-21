package com.nexopia.adblaster;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {

	UserDatabase user_db;
	BannerDatabase banner_db;
	Environment dbEnv;
	
	public AdBlasterDbUniverse(){
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			
			banner_db = new BannerDatabase();
						
			user_db = new UserDatabase(this.dbEnv);
			
			this.init(user_db.getUserCount(), banner_db.banners.size());
			for (int i = 0; i < banner_db.banners.size(); i++){
				this.setBanner(i, (Banner)banner_db.banners.get(i));
			}
			for (int i = 0; i < user_db.getUserCount(); i++){
				this.setUser(i, user_db.getUser(i));
			}
			
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
	}
}
