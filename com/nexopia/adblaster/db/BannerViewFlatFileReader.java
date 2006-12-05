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
	
	public BannerViewFlatFileReader(String directoryName) {
		files = new Vector<FileReader>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			throw new SecurityException(directoryName + " is not a directory.");
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

	public BannerView get(int index) {
		// TODO Auto-generated method stub
		//It is not obvious exactly how this should behave, when implementing it be careful to
		//check how it is used and implement it sanely.
		throw new UnsupportedOperationException("You should now implement this method!");
	}

	public Vector<BannerView> getByUser(int id) {
		return userBannerViewMap.get(id);
	}

	
	public void load(int fileNumber) throws IOException {
		files.get(fileNumber);
		File f = new File(directory, "bannerview."+fileNumber+".db");
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String bannerViewString;
		
		bannerViews = new Vector<BannerView>();
		userBannerViewMap = new IntObjectHashMap<Vector<BannerView>>();
		
		while ((bannerViewString = reader.readLine()) != null) {
			BannerView bv = new BannerView(bannerViewString);
			bannerViews.add(bv);
			userBannerViewMap.get(bv.getUserID()).add(bv);
		}
	}

}
