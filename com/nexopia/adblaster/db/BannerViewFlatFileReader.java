package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import com.nexopia.adblaster.BannerViewCursor;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.IntObjectHashMap;

public class BannerViewFlatFileReader {
	private Vector<BannerView> bannerViews;
	private IntObjectHashMap<Vector<BannerView>> userBannerViewMap;
	private Vector<FileReader> files;
	private File directory;
	private int bannerViewCount = 0;
	private BannerDatabase bannerDB;
	private UserFlatFileReader userDB;
	
	public BannerViewFlatFileReader(File directory, BannerDatabase bannerDB, UserFlatFileReader userDB) {
		this.directory = directory;
		this.init(bannerDB, userDB);
	}
	public BannerViewFlatFileReader(String directoryName, BannerDatabase bannerDB, UserFlatFileReader userDB) {
		directory = new File(directoryName);
		this.init(bannerDB, userDB);
	}
	
	private void init(BannerDatabase bannerDB, UserFlatFileReader userDB){
		this.bannerDB = bannerDB;
		this.userDB = userDB;
		files = new Vector<FileReader>();
		if (!directory.isDirectory()) {
			throw new SecurityException(directory.getName() + " is not a directory.");
		}
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>(); 
	}

	public BannerViewCursor getCursor() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("You should now implement this method!");
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
		files.get(fileNumber);
		File f = new File(directory, "bannerview."+fileNumber+".db");
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String bannerViewString;
		
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>();
		
		while ((bannerViewString = reader.readLine()) != null) {
			BannerView bv = new BannerView(bannerViewString, bannerDB, userDB);
			bannerViews.add(bv);
			userBannerViewMap.get(bv.getUserID()).add(bv);
		}
	}

}
