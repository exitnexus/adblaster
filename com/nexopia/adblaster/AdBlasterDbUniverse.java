package com.nexopia.adblaster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

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
	PageDatabase pageDb;
	public Campaign.CampaignDB campaignDB;

	public Campaign getCampaignByIndex(int i) {
		return campaignDB.getByIndex(i);
	}
	public int getCampaignCount() {
		return campaignDB.getCampaignCount();
	}

	public AdBlasterDbUniverse(String s, PageDatabase pageDb){
		this.pageDb = pageDb;
		Object args[] = {pageDb};
		PageValidatorFactory factory = 
			new PageValidatorFactory(PageValidator2.class,args);
		campaignDB = new Campaign.CampaignDB(factory);
		bannerDB = new BannerDatabase(campaignDB, factory);
	}

	public AdBlasterDbUniverse(File userdb_file, File pagedb_file){
		try {
			PageDatabase pageDb = new PageDatabase(pagedb_file);
			this.pageDb = pageDb;
			Object args[] = {pageDb};
			PageValidatorFactory factory = 
				new PageValidatorFactory(PageValidator2.class,args);
			campaignDB = new Campaign.CampaignDB(factory);
			bannerDB = new BannerDatabase(campaignDB, factory);
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
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getBanners()
	 */
	public Collection getBanners() {
		return bannerDB.getBanners();
	}

	public void saveCoefficients(HashMap<Banner,Float> coefficients) {
		bannerDB.saveCoefficients(coefficients);
	}

	public PageDatabase getPageDatabase() {
		return this.pageDb;
	}

	public Collection<Banner> getBannerList() {
		return bannerDB.getBanners();
	}
}
