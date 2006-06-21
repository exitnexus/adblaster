package com.nexopia.adblaster;

import java.util.Vector;

import com.sleepycat.je.DatabaseException;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance 
		implements I_AdBlasterInstance {

	UserDatabase user_db;
	
	public AdBlasterDbInstance(){
		try {
			user_db = new UserDatabase(this.dbEnv);
		} catch (DatabaseException e) {
			e.printStackTrace();
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
