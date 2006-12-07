package com.nexopia.adblaster;

import java.util.Collection;
import java.util.HashMap;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.util.PageValidatorFactory;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {
	private BannerDatabase bannerDB;

	public Campaign.CampaignDB campaignDB;

	public Campaign getCampaignByIndex(int i) {
		return campaignDB.getByIndex(i);
	}
	public int getCampaignCount() {
		return campaignDB.getCampaignCount();
	}

	public AdBlasterDbUniverse(PageValidatorFactory factory){
		campaignDB = new Campaign.CampaignDB(factory);
		bannerDB = new BannerDatabase(campaignDB, factory);
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

	public Collection<Banner> getBannerList() {
		return bannerDB.getBanners();
	}
}
