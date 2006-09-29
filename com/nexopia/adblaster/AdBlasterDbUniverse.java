package com.nexopia.adblaster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.PageDatabase;
import com.nexopia.adblaster.db.UserDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.sleepycat.je.DatabaseException;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {
	private BannerDatabase bannerDB;
	private UserDatabase userDB;
	private PageDatabase pageDb;
	public Campaign.CampaignDB campaignDB;

	public Campaign getCampaignByIndex(int i) {
		return campaignDB.getByIndex(i);
	}
	public int getCampaignCount() {
		return campaignDB.getCampaignCount();
	}

	public AdBlasterDbUniverse(String s, PageDatabase pageDb){
		try {
			this.pageDb = pageDb;
			campaignDB = new Campaign.CampaignDB();
			Object args[] = {pageDb};
			bannerDB = new BannerDatabase(campaignDB, new PageValidatorFactory(PageValidator2.class,args));
			userDB = new UserDatabase(s);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public AdBlasterDbUniverse(File userdb_file, File pagedb_file){
		try {
			PageDatabase pageDb = new PageDatabase(pagedb_file);
			this.pageDb = pageDb;
			campaignDB = new Campaign.CampaignDB();
			Object args[] = {pageDb};
			bannerDB = new BannerDatabase(campaignDB, new PageValidatorFactory(PageValidator2.class,args));
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
		return userDB.getUserByIndex((int)(Math.random()*(userDB.getUserCount()-1)));
	}

	public User getUserByIndex(int randomPick) {
		return userDB.getUserByIndex(randomPick);
	}

	public void addUser(User u) {
		userDB.cache.put(u.getID(), u);
		
	}

	public void saveCoefficients(HashMap<Banner,Float> coefficients) {
		bannerDB.saveCoefficients(coefficients);
	}

	public PageDatabase getPageDatabase() {
		return this.pageDb;
	}
}
