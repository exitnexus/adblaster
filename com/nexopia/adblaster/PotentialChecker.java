/*
 * Maximum potential views: 9788233
Running time: 651.116s
Calculation time: 612.292s

 */

package com.nexopia.adblaster;

import java.io.File;
import java.io.IOException;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.FlatFilePageValidator;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.PageValidatorFactory;

/* Run this file with 2 arguments:
 * arg1: directory where flat file information is stored
 * arg2: id of the banner to check potential for
 */

public class PotentialChecker {
	private int bannerID;
	private Banner banner;
	private UserFlatFileReader userReader;
	private BannerDatabase bannerDB;
	private BannerViewFlatFileReader bannerViewReader;
	private boolean skipFrequency;
	
	public PotentialChecker(File directory, int bid, boolean skipFrequencyCheck) throws IOException {
		bannerID = bid;
		skipFrequency = skipFrequencyCheck;
		Object args1[] = {new PageFlatFileDatabase(directory, true)};

		PageValidatorFactory factory = 
			new PageValidatorFactory(FlatFilePageValidator.class,args1);
		
		CampaignDB cdb = new CampaignDB(factory);
		bannerDB = new BannerDatabase(cdb, factory, false);
		banner = bannerDB.getBannerByID(bid);
		
		userReader = new UserFlatFileReader(directory);
		bannerViewReader = new BannerViewFlatFileReader(directory);
	}
	
	public int potentialViews() {
		
		if (banner == null) {
			System.err.println("Potential views called for non-existant banner: " + bannerID);
			return 0;
		}
		
		int viewCount = 0;
		for (int i=0; i<FlatFileConfig.FILE_COUNT; i++) {
			IntObjectHashMap<int[]> userViewMap = new IntObjectHashMap<int[]>();
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					User u = userReader.getUser(bv.getUserID());
					if (u != null) {
						int[] views = userViewMap.get(u.getID());
						if (views == null) {
							views = new int[banner.getViewsPerUser()];
							userViewMap.put(u.getID(), views);
						}
						if (banner.validUser(u)) {
							if  (skipFrequency || views[0] < bv.getTime()-banner.getLimitByPeriod()) {
								viewCount++;
								for (int j=1; j<banner.getViewsPerUser(); j++) {
									views[j-1] = views[j];
								}
								views[banner.getViewsPerUser()-1] = bv.getTime();
							}
						}
					} else {
						System.err.println("BannerView for non-existant user: " + bv.getUserID());
					}
				}
			} catch (IOException e) {
				System.err.println("Error loading file #" + i);
				e.printStackTrace();
			}
		}
		return viewCount;
	}
	
	
	
	public static void main(String args[]) {
		if (args.length < 1) {
			System.err.println("Run this file with 2 arguments:");
			System.err.println("\targ1: The id of the banner to check potential for.");
			System.err.println("\targ2 (Optional): The directory where flat file information is stored.");
			System.exit(-1);
		}
		File directory;
		if (args.length > 1) {
			directory = new File(args[1]);
		} else {
			directory = FlatFileConfig.getDefaultDirectory();
		}
		
		int bid = Integer.parseInt(args[1]);
		boolean skipFrequencyChecks = false;
		if (args.length > 2) {
			skipFrequencyChecks = Boolean.parseBoolean(args[2]);
			if (skipFrequencyChecks) {
				System.out.println("Skipping frequency checks.");
			} else {
				System.out.println("Performing frequency checks.");
			}
		}
		
		System.out.println("Running a potential check using the directory '" + directory + "' and banner '" + bid + "'");
		long startTime = System.currentTimeMillis();
		try {
			PotentialChecker checker = new PotentialChecker(directory, bid, skipFrequencyChecks);
			long startCalculation = System.currentTimeMillis();
			System.out.println("Maximum potential views: " + checker.potentialViews());
			System.out.println("Running time: " + (double)(System.currentTimeMillis() - startTime)/1000 + "s");
			System.out.println("Calculation time: " + (double)(System.currentTimeMillis() - startCalculation)/1000 + "s");
		} catch (IOException e) {
			System.err.println("Invalid directory specified: " + directory);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}