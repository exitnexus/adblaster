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
			
			this.init(user_db.users.size(), banner_db.banners.size());
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
	}
}
