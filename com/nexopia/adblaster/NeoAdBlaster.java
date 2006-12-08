package com.nexopia.adblaster;

import java.io.IOException;
import java.util.Collection;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerStat;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;

public class NeoAdBlaster {
	IntObjectHashMap<BannerStat> bannerViewStats;
	BannerDatabase bannerDB;
	CampaignDB campaignDB;
	BannerViewFlatFileReader bannerViewReader;
	UserFlatFileReader userReader;
	PageFlatFileDatabase pageDB;
	
	public NeoAdBlaster(String directoryName) {
		try {
			pageDB = new PageFlatFileDatabase(directoryName, true);
			Object validatorArgs[] = {pageDB};
			PageValidatorFactory pageValidator = new PageValidatorFactory(PageValidator2.class, validatorArgs);
			campaignDB = new CampaignDB(pageValidator);
			bannerDB = new BannerDatabase(campaignDB, pageValidator);
			bannerViewReader = new BannerViewFlatFileReader(directoryName);
			userReader = new UserFlatFileReader(directoryName);
		} catch (IOException e) {
			System.err.println("Unable to open databases in directory: "+directoryName);
			e.printStackTrace();
			System.exit(-1);
		}
		bannerViewStats = new IntObjectHashMap<BannerStat>();
		Collection<Banner> banners = bannerDB.getBanners();
		for (Banner b: banners) {
			bannerViewStats.put(b.getID(), new BannerStat());
		}
		
	}
	
	public void run() {
		//do something
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NeoAdBlaster adblaster = new NeoAdBlaster(args[0]);
		adblaster.run();
	}

}
