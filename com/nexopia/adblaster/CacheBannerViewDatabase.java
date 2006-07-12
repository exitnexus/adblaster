package com.nexopia.adblaster;

import java.util.Vector;

import com.sleepycat.je.DatabaseException;

public class CacheBannerViewDatabase {
	BannerViewDatabase bv_db;
	//private int datacache[];
	Vector<BannerView> vec;
	public int lastid;
	
	public CacheBannerViewDatabase() throws DatabaseException{
		bv_db = new BannerViewDatabase();
		vec = new Vector<BannerView>();
		//BannerViewCursor cursor;
			//cursor = bv_db.getCursor(0,0,0);
			//int i = 0;
			BannerView bv = null;
			//while((bv = cursor.getNext()) != null){
			for (int i = 0; i < bv_db.getBannerViewCount(); i++){
				//bv.b = pol.getBestBanner(this, bv);
				bv = bv_db.get(i);
				vec.add(bv);
				//i++;
				if (i%1000 == 0){
					System.out.println("Loaded bannerview " + i + ": " + bv);
				}
			}
			//cursor.close();
		this.lastid = bv_db.getBannerViewCount();

	}
	
	public void close() throws DatabaseException {
		bv_db.close();
	}

	public BannerView get(int index) {
		return vec.get(index);
//		return bv_db.get(index);
	}

	public Vector<BannerView> getByUser(int id) {
		Vector<BannerView> vec = new Vector<BannerView>();
		for (int i = 0; i < this.lastid; i++){
			BannerView bv = this.get(i);
			if (bv.getUserID() == id){
				vec.add(bv);
			}
		}
		return vec;
	}
}
