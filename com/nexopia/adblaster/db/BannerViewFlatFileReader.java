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
	private Vector<FileReader> files;
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
		files = new Vector<FileReader>();
		if (!directory.isDirectory()) {
			throw new SecurityException(directory.getName() + " is not a directory.");
		}
		for (int i=0; i<FlatFileConfig.FILE_COUNT;i++) {
			File f = new File(directory, "bannerview."+i+".db");
			files.add(new FileReader(f));
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
		for (FileReader f: files) {
			BufferedReader reader = new BufferedReader(f);
			try {
				while (reader.readLine() != null) {
					bannerViewCount++;
				}
			} catch (IOException e) {
				System.err.println("IO Error while counting banner views.");
				e.printStackTrace();
			}
		}
		return bannerViewCount;
	}

	public void close() throws IOException {
		for (FileReader f: files) {
			f.close();
		}
	}

	public Vector<BannerView> getByUser(int id) {
		return userBannerViewMap.get(id);
	}
	
	public Vector<BannerView> getCurrentBannerViews() {
		return bannerViews;
	}

	
	public void load(int fileNumber) throws IOException {
		FileReader f = files.get(fileNumber);
		BufferedReader reader = new BufferedReader(f);
		String bannerViewString;
		
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>();
		while ((bannerViewString = reader.readLine()) != null) {
			BannerView bv = new BannerView(bannerViewString);
			bannerViews.add(bv);
			if (bv.getUserID() > 0){
				Vector<BannerView> viewMap = userBannerViewMap.get(bv.getUserID());
				if (viewMap == null){
					viewMap = new Vector<BannerView>();
					userBannerViewMap.put(bv.getUserID(), viewMap);
				}
				viewMap.add(bv);
			}
		}
		System.out.println("Loaded " + bannerViews.size() + " views in set " + fileNumber);
	}

}
