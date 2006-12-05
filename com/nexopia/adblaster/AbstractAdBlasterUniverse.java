package com.nexopia.adblaster;

import java.util.Collection;
import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.User;


public abstract class AbstractAdBlasterUniverse {
	
	abstract public Banner getBannerByIndex(int i);
	abstract public Banner getBannerByID(int i);

	abstract public int getBannerCount();

	abstract public Collection getBanners();
	abstract public Campaign getCampaignByIndex(int i);
	abstract public int getCampaignCount();

	abstract public Collection<Banner>getBannerList();
	
	public Banner getRandomBannerMatching(BannerView bv, AbstractAdBlasterInstance instance) {
		Banner match = null;
		while (match == null){
			int index = (int)(Math.random()*getBannerCount());
			Banner b = getBannerByIndex(index);
			if (b == null){
				System.err.println("There is an error here (null banner in the database at " + index);
			}
			if (b != null && instance.isValidBannerForView(bv, b) && 
					(instance.bannerCount(b) < b.getIntegerMaxViewsPerDay()) && 
					(instance.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay())){
				match = b;
			}
		}
		return match;	
	}
	
}
