package com.nexopia.adblaster;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerStat;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.FlatFilePageValidator;
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
			PageValidatorFactory pageValidator = new PageValidatorFactory(FlatFilePageValidator.class, validatorArgs);
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
		calculateViewStats();
	}
	
	public void calculateViewStats() {
		for (int i=0; i<FlatFileConfig.FILE_COUNT; i++) {
			System.out.println("Calculating view stats for subsection " + i + ".");
			try {
				bannerViewReader.load(i);
			} catch (IOException e) {
				System.err.println("Failed to load banner view file " + i);
				e.printStackTrace();
			}
			Vector<BannerView> bannerViews = bannerViewReader.getCurrentBannerViews();
			for (BannerView bv: bannerViews) {
				BannerStat stat = bannerViewStats.get(bv.getBannerId());
				if (stat != null) {
					stat.dailyviews++;
				}
			}
		}
		System.out.println("View stat calculation complete.");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NeoAdBlaster adblaster = new NeoAdBlaster(args[0]);
		adblaster.run();
	}

}
