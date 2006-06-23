package com.nexopia.adblaster;

import java.io.File;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {
	private BannerDatabase bannerDB;
	private UserDatabase userDB;
	
	public AdBlasterDbUniverse(){
		try {
			bannerDB = new BannerDatabase();
			userDB = new UserDatabase();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#init(int, int)
	 */
	protected void init(int u_num, int b_num) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getBanner(int)
	 */
	public Banner getBanner(int i) {
		return bannerDB.getBanner(i);
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getBannerCount()
	 */
	public int getBannerCount() {
		return bannerDB.getBannerCount();
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getUser(int)
	 */
	public User getUser(int i) {
		User u;
		try {
			u = userDB.getUser(i);
		} catch (DatabaseException dbe) {
			System.err.println("Unable to retrieve user " + i + ": " + dbe);
			dbe.printStackTrace();
			return null;
		}
		return u;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getUserCount()
	 */
	public int getUserCount() {
		return userDB.getUserCount();
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getBanners()
	 */
	public Collection getBanners() {
		return bannerDB.getBanners();
	}
}
