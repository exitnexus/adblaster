package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance 
		implements I_AdBlasterInstance {

	BannerViewDatabase bannerview_db;
	Environment dbEnv;
	
	public AdBlasterDbInstance(){
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
			bannerview_db = new BannerViewDatabase(this.dbEnv);
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
		
	}

	public void fillInstance(AdBlasterPolicy pol) {
		// TODO Auto-generated method stub

	}

	public boolean isValidBannerForUser(User u, Banner b) {
		// TODO Auto-generated method stub
		return false;
	}

	public BannerView randomView(AdBlasterUniverse campaign) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getUnserved() {
		// TODO Auto-generated method stub
		return null;
	}

	public float totalProfit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public AdBlasterInstance copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
