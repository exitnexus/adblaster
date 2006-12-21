package com.nexopia.adblaster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

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
	
	public PotentialChecker(String directory, int bid) throws IOException {
		bannerID = bid;
		Object args1[] = {new PageFlatFileDatabase(directory, true)};

		PageValidatorFactory factory = 
			new PageValidatorFactory(FlatFilePageValidator.class,args1);
		
		CampaignDB cdb = new CampaignDB(factory);
		bannerDB = new BannerDatabase(cdb, factory);
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
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					User u = userReader.getUser(bv.getUserID());
					if (u != null) {
						if (banner.validUser(u)) {
							viewCount++;
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
		if (args.length < 2) {
			System.err.println("Run this file with 2 arguments:");
			System.err.println("\targ1: The directory where flat file information is stored.");
			System.err.println("\targ2: The id of the banner to check potential for.");
			System.exit(-1);
		}
		String directory = args[0];
		int bid = Integer.parseInt(args[1]);
		
		System.out.println("Running a potential check using the directory '" + directory + "' and banner '" + bid + "'");
		long startTime = System.currentTimeMillis();
		try {
			PotentialChecker checker = new PotentialChecker(directory, bid);
			long startCalculation = System.currentTimeMillis();
			System.out.println("Maximum potential views: " + checker.potentialViews());
			System.out.println("Running time: " + (double)(startTime - System.currentTimeMillis())/1000 + "s");
			System.out.println("Calculation time: " + (double)(startCalculation - System.currentTimeMillis())/1000 + "s");
		} catch (IOException e) {
			System.err.println("Invalid directory specified: " + directory);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}