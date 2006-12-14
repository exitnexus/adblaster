package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.IntObjectHashMap;

public class BannerViewFlatFileReader {
	private Vector<BannerView> bannerViews;
	private IntObjectHashMap<Vector<BannerView>> userBannerViewMap;
	private File directory;
	private int bannerViewCount = 0;
	
	public BannerViewFlatFileReader(File directory) throws FileNotFoundException {
		this.directory = directory;
		this.init();
	}
	public BannerViewFlatFileReader(String directoryName) throws FileNotFoundException {
		directory = new File(directoryName);
		this.init();
	}
	
	private void init() throws FileNotFoundException{
		if (!directory.isDirectory()) {
			throw new SecurityException(directory.getName() + " is not a directory.");
		}
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>(); 
	}

	//If you want this value recalculated call refreshBannerViewCount
	public int getBannerViewCount() {
		if (bannerViewCount == 0) {
			refreshBannerViewCount();
		}
		return bannerViewCount;
	}
	
	public int refreshBannerViewCount() {
		bannerViewCount = 0;
		for (int i=0; i<100; i++) {
			try {
				BufferedReader reader = getFile(i);
				while (reader.readLine() != null) {
					bannerViewCount++;
				}
				reader.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				System.err.println("IO Error while counting banner views.");
				e.printStackTrace();
			}
		}
		return bannerViewCount;
	}

	public Vector<BannerView> getByUser(int id) {
		return userBannerViewMap.get(id);
	}
	
	public Vector<BannerView> getCurrentBannerViews() {
		return bannerViews;
	}

	
	public void load(int fileNumber) throws IOException {
		BufferedReader reader = getFile(fileNumber);
		String bannerViewString;
		
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>();
		while ((bannerViewString = reader.readLine()) != null) {
			BannerView bv = new BannerView(bannerViewString);
			if (bv.getPassback() != 0) { //ignore reserves from passbacks
				bannerViews.add(bv);
				if (bv.getUserID() > 0){ //only create user hashmaps for non anonymous users
					Vector<BannerView> viewMap = userBannerViewMap.get(bv.getUserID());
					if (viewMap == null){
						viewMap = new Vector<BannerView>();
						userBannerViewMap.put(bv.getUserID(), viewMap);
					}
					viewMap.add(bv);
				}
			}
		}
		reader.close();
		System.out.println("Loaded " + bannerViews.size() + " views in set " + fileNumber);
	}
	private BufferedReader getFile(int fileNumber) throws FileNotFoundException {
		File f = new File(directory, "bannerview."+fileNumber+".db");
		FileReader fr = new FileReader(f);
		BufferedReader reader = new BufferedReader(fr);
		return reader;
	}

}
