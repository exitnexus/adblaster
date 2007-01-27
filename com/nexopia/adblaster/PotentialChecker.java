/*
 * Maximum potential views: 9788233
Running time: 651.116s
Calculation time: 612.292s

 */

package com.nexopia.adblaster;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.CampaignDB;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.User;
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
	private int countEstimate = 20000000; //magic guess at how many banners we serve in a day, gets updated from real numbers as soon as one file has been scanned
	private int lastFile = 0;
	
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
	
	public int getSecond(){
		return Calendar.getInstance().get(Calendar.SECOND) +
			(60*	(Calendar.getInstance().get(Calendar.MINUTE) +
				(60*	(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) +
					(24 * Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
				))
			));
			
	}
	public int potentialViews() {
		
		if (banner == null) {
			System.err.println("Potential views called for non-existant banner: " + bannerID);
			return 0;
		}
		int lastSecond = getSecond();
		int viewCount = 0;
		int totalViewCount = 0;
		for (int i=0; i<FlatFileConfig.FILE_COUNT; i++) {
			IntObjectHashMap<int[]> userViewMap = new IntObjectHashMap<int[]>();
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					totalViewCount++;
					if (bv.getPassback() != 0) {
						continue;
					}
					int second = getSecond();
					if (second > lastSecond + 5) {
						lastSecond = second;
						updateProgress(i, totalViewCount, viewCount);
					}
					User u = userReader.getUser(bv.getUserID());
					if (u != null) {
						int[] views = userViewMap.get(u.getID());
						if (views == null) {
							views = new int[banner.getViewsPerUser()];
							userViewMap.put(u.getID(), views);
						}
						if (banner.validUser(u) && banner.getSize() == bv.getSize()) {
							if  (skipFrequency || banner.getViewsPerUser() == 0 || views[0] < bv.getTime()-banner.getLimitByPeriod()) {
								viewCount++;
								if (banner.getViewsPerUser() > 0){
									for (int j=1; j<banner.getViewsPerUser(); j++) {
										views[j-1] = views[j];
									}
									views[banner.getViewsPerUser()-1] = bv.getTime();
								}
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
		insertResult(viewCount);
		return viewCount;
	}
	
	private void updateProgress(int currentFile, int totalViewCount, int viewCount) {
		if (lastFile != currentFile) {
			countEstimate = (int) ( ((double)totalViewCount/(double)currentFile) * ((double)FlatFileConfig.FILE_COUNT) );
			lastFile = currentFile;
		}
		System.out.println(viewCount);
		double completeEstimate = (double)totalViewCount/(double)countEstimate;
		int resultEstimate = (int) (((double)viewCount/(double)totalViewCount) * countEstimate);
		//write the count estimate to the database
		try {
			Statement statement = JDBCConfig.createStatement();
			String sql = "REPLACE INTO `potentialviews` SET " +
					"`bannerid` = " + this.bannerID + ", " +
					"`day` = " + Calendar.DAY_OF_YEAR + ", " +
					"`percentcomplete` = " + completeEstimate*100 + ", " +
					"`potentialviews` = " + resultEstimate + ";";
			System.out.println("Executing: " + sql);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void insertResult(int viewCount) {
		updateProgress(FlatFileConfig.FILE_COUNT, this.countEstimate, viewCount);
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
		
		int bid = Integer.parseInt(args[0]);
		boolean skipFrequencyChecks = false;
		if (args.length > 3) {
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