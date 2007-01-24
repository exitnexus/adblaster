/*
 * Maximum potential views: 9788233
Running time: 651.116s
Calculation time: 612.292s

 */

package com.nexopia.adblaster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.ConfigFile;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.FlatFilePageValidator;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.PageValidatorFactory;

/* Run this file with 2 arguments:
 * arg1: directory where flat file information is stored
 * arg2: id of the banner to check potential for
 */

public class ImpressionChecker {
	private static ConfigFile config;
	private int bannerID;
	private UserFlatFileReader userReader;
	private BannerViewFlatFileReader bannerViewReader;
	
	public ImpressionChecker(File directory) throws IOException {
		Object args1[] = {new PageFlatFileDatabase(directory, true)};

		PageValidatorFactory factory = 
			new PageValidatorFactory(FlatFilePageValidator.class,args1);
		
		CampaignDB cdb = new CampaignDB(factory);
		
		userReader = new UserFlatFileReader(directory);
		bannerViewReader = new BannerViewFlatFileReader(directory);
	}
	
	public static void insertOrUpdate(IntObjectHashMap<int[]>[] map, int i, byte size){
		int[] views = map[size].get(i);
		if (views == null) {
			views = new int[1];
			map[size].put(i, views);
		}
		views[0] = views[0] + 1;
	}
	
	public int potentialViews() throws IOException {
		IntObjectHashMap<int[]>[] impressionRanges = new IntObjectHashMap[10];
		for (int j = 0; j < impressionRanges.length; j++){
			impressionRanges[j] = new IntObjectHashMap<int[]>();
		}
		int counts[] = new int[10];
		int viewCount = 0;
		int totalViewCount = 0;
		for (int i=0; i<FlatFileConfig.FILE_COUNT; i++) {
			System.out.println(i + "%");
			
			IntObjectHashMap<int[]>[] userViewMaps = new IntObjectHashMap[10];
			for (int j = 0; j < userViewMaps.length; j++){
				userViewMaps[j] = new IntObjectHashMap<int[]>();
			}
			
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					totalViewCount++;
					if (bv.getPassback() == 0){
						User u = userReader.getUser(bv.getUserID());
						if (u != null){
							//if (u.getLocation() == 21 || u.getLocation() == 389 || u.getLocation() == 386) {
								//insertOrUpdate(userViewMaps, 1, bv.getSize());
								counts[bv.getSize()]++;
							//}
						} else {
							System.err.println("BannerView for non-existant user: " + bv.getUserID());
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Error loading file #" + i);
				e.printStackTrace();
			}
			byte size = 0;
			/*for (IntObjectHashMap<int[]> userViewMap : userViewMaps){
				for (int j : userViewMap.getKeyArray()){
					if (j != 0){
						insertOrUpdate(impressionRanges, userViewMap.get(j)[0], size);
						//System.out.println(userViewMap.get(j)[0]);
					}
				}
				for (int range : impressionRanges[size].getKeyArray()){
					if (range != 0){
						//System.out.println(range + " : " + impressionRanges[size].get(range)[0]);
					}
				}
				size++;
			}*/
			for (int j = 0; j < 10; j++){
				System.out.println("counts " + j + " : " + counts[j]);
			}
		}
		FileWriter f = new FileWriter(new File("c:/output.txt"));
		byte size = 0;
		for (IntObjectHashMap<int[]> impressionRange : impressionRanges){
			System.out.println("----------" + size + "------------");
			f.write("----------" + size + "------------");
			for (int range : impressionRange.getKeyArray()){
				if (range != 0){
					System.out.println(range + " : " + impressionRange.get(range)[0]);
					f.write(range + " : " + impressionRange.get(range)[0] + "\r\n");
				}
			}
			size++;
		}
		f.flush();
		f.close();
		return viewCount;
	}
	
	
	public static void main(String args[]) {
		File directory;
		if (args.length > 0) {
			directory = new File(args[0]);
		} else {
			directory = FlatFileConfig.getDefaultDirectory();
		}
		
		if (args.length > 1){
			config = new ConfigFile(new File(args[0]));
		} else {
			config = new ConfigFile(new File("banner.config"));
		}
		
		JDBCConfig.initDBConnection(config);
		
		System.out.println("Running a impression checker using the directory '" + directory);
		long startTime = System.currentTimeMillis();
		try {
			ImpressionChecker checker = new ImpressionChecker(directory);
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