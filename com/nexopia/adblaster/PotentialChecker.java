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
		int viewCount = 0;
		for (int i=0; i<FlatFileConfig.FILE_COUNT; i++) {
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					if (banner.validUser(userReader.getUser(bv.getUserID()))) {
						viewCount++;
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
		try {
			PotentialChecker checker = new PotentialChecker(directory, bid);
			System.out.println("Maximum potential views: " + checker.potentialViews());
		} catch (IOException e) {
			System.err.println("Invalid directory specified: " + directory);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}