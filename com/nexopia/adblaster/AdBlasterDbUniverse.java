package com.nexopia.adblaster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import com.sleepycat.je.DatabaseException;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {
	private BannerDatabase bannerDB;
	UserDatabase userDB;
	
	public AdBlasterDbUniverse(String s){
		try {
			bannerDB = new BannerDatabase();
			userDB = new UserDatabase(s);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public AdBlasterDbUniverse(File userdb_file){
		try {
			bannerDB = new BannerDatabase();
			userDB = new UserDatabase(userdb_file);
		} catch (DatabaseException e) {
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
	public Banner getBannerByIndex(int i) {
		/*This is by index...*/
		return bannerDB.getBannerByIndex(i);
	}

	public Banner getBannerByID(int i) {
		/*This is by ID...*/
		return bannerDB.getBannerByID(i);
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
		//Integer I = Integer.valueOf(i);
		User u = userDB.getUser(i);
		//I.free();
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

	public User getRandomUser() {
		return userDB.getUserByIndex((int)(Math.random()*(userDB.userCount-1)));
	}

	public User getUserByIndex(int randomPick) {
		return userDB.getUserByIndex(randomPick);
	}

	public void addUser(User u) {
		userDB.cache.put(u.id, u);
		
	}

	public void saveCoefficients(HashMap<Banner,Float> coefficients) {
		bannerDB.saveCoefficients(coefficients);
	}
}
