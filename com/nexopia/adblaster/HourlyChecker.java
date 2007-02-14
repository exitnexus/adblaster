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
import com.nexopia.adblaster.db.CampaignDB;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.ConfigFile;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.FlatFilePageValidator;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.PageValidatorFactory;

/* Run this file with 2 arguments:
 * arg1: directory where flat file information is stored
 * arg2: id of the banner to check potential for
 */

public class HourlyChecker {
	private static ConfigFile config;
	private int bannerID;
	private UserFlatFileReader userReader;
	private BannerViewFlatFileReader bannerViewReader;
	
	public HourlyChecker(File directory) throws IOException {
		Object args1[] = {new PageFlatFileDatabase(directory, true)};

		PageValidatorFactory factory = 
			new PageValidatorFactory(FlatFilePageValidator.class,args1);
		
		CampaignDB cdb = new CampaignDB(factory);
		
		userReader = new UserFlatFileReader(directory);
		bannerViewReader = new BannerViewFlatFileReader(directory);
	}

	static int state = 1;
	static int hour = 0;
	static int count = 0;
	public static void insertOrUpdate(IntObjectHashMap<int[]> map, int i, int size, int b){
		if ((i%3600) >= 2700){
			if (state == 3){
				state = 4;
			} 
			if (state >= 2){
				i = (i%3600 + ((hour)*3600)) / 60;
			} else {
				i = (i%3600 + ((hour-1)*3600)) / 60;
			}
		} else if ((i%3600) >= 1800) {
			if (state == 2){
				state = 3;
			}
			i = (i%3600 + ((hour)*3600)) / 60;
		} else if ((i%3600) >= 900){
			if (state == 1)
				state = 2;
			i = (i%3600 + ((hour)*3600)) / 60;
		} else if ((i%3600) < 900){
			if (state == 4){
				state = 1;
				hour++;
			}
			i = (i%3600 + ((hour)*3600)) / 60;
		}

		count++;
		//if (count % 1000 == 0)
		//	System.out.println("" + hour + " : " + i%3600);
		int[] views = map.get(i);
		if (views == null) {
			views = new int[10];
			map.put(i, views);
		}
		views[size] = views[size] + 1;
	}
	
	public int potentialViews() throws IOException {
		int counts[] = new int[10];
		int viewCount = 0;
		int totalViewCount = 0;

		IntObjectHashMap<int[]> userViewMap = new IntObjectHashMap();

//		for (int i=19; i<FlatFileConfig.FILE_COUNT; i++) {
		int i = 19;
		{
			System.out.println(i + "%");
			
			
			try {
				bannerViewReader.load(i);
				userReader.load(i);
				hour = 0;
				state = 1;
				for (BannerView bv: bannerViewReader.getCurrentBannerViews()) {
					totalViewCount++;
					if (bv.getPassback() == 0){
						User u = userReader.getUser(bv.getUserID());
						if (u != null){
							//if (bv.getSize() == 1) {
								insertOrUpdate(userViewMap, bv.getTime(), bv.getSize(), bv.getTime());
								//counts[bv.getSize()]++;
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
			//for (IntObjectHashMap<int[]> userViewMap : userViewMaps){
			//	System.out.println(size);
			//	size++;

				for (int j : userViewMap.getKeyArray()){
					if (j != 0){
						//System.out.println("" + j + " : " + userViewMap.get(j)[0]);
					}
				}
				/*for (int j : userViewMap.getKeyArray()){
					if (j != 0){
						insertOrUpdate(impressionRanges, userViewMap.get(j)[0], size);
						//System.out.println(userViewMap.get(j)[0]);
					}
				}
				for (int range : impressionRanges[size].getKeyArray()){
					if (range != 0){
						//System.out.println(range + " : " + impressionRanges[size].get(range)[0]);
					}
				}*/
			//	size++;
			//}
			/*for (int j = 0; j < 10; j++){
				System.out.println("counts " + j + " : " + counts[j]);
			}*/
		}
		FileWriter f = new FileWriter(new File("output.txt"));
		byte size = 0;
		System.out.println("----------" + size + "------------");
		f.write("----------" + size + "------------\n");
		size++;
		for (int j : userViewMap.getKeyArray()){
			if (j != 0){
				f.write("" + j + ",");
				for (int k = 0; k < 10; k++){
					if (k > 0)
						f.write(",");
					System.out.println("" + j + " : " + userViewMap.get(j)[0]);
					f.write(""+userViewMap.get(j)[k]);	
				}
				f.write("\n");
			}
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
			config = new ConfigFile(new File(args[1]));
		} else {
			config = new ConfigFile(new File("banner.config"));
		}
		
		JDBCConfig.initDBConnection(config);
		
		System.out.println("Running a impression checker using the directory '" + directory);
		long startTime = System.currentTimeMillis();
		try {
			HourlyChecker checker = new HourlyChecker(directory);
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
