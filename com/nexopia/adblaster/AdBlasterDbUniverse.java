package com.nexopia.adblaster;

import java.util.Collection;

import com.sleepycat.je.DatabaseException;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {
	private BannerDatabase bannerDB;
	private UserDatabase userDB;
	
	public AdBlasterDbUniverse(){
		try {
			bannerDB = new BannerDatabase();
			userDB = new UserDatabase();
			Campaign.init();
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
		Integer I = Integer.valueOf(i);
		User u = userDB.getUser(I);
		I.free();
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
}
