package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.sleepycat.je.DatabaseException;

public class CacheBannerViewDatabase {
	BannerViewDatabase bv_db;
	//private int datacache[];
	//Vector<BannerView> vec;
	IntObjectHashMap map;
	public int lastid;
	
	public CacheBannerViewDatabase(File f, AdBlasterDbInstance inst) throws DatabaseException{
		bv_db = new BannerViewDatabase(f,inst.instanceBinding);
		//vec = new Vector<BannerView>();
		map = new IntObjectHashMap();
/*		BannerView bv = null;
		for (int i = 0; i < bv_db.getBannerViewCount(); i++){
			bv = bv_db.get(i);
			vec.add(bv);
			if (i%1000 == 0){
				System.out.println("Loaded bannerview " + i + ": " + bv);
			}
		}*/
		this.lastid = bv_db.getBannerViewCount();

	}
	
	public void close() throws DatabaseException {
		bv_db.close();
	}

	public BannerView get(int index) {
		if (map.containsKey(index)){
			return (BannerView)map.get(index);
		} else {
			BannerView bv = bv_db.get(index);
			map.put(index, bv);
			return bv;
		}
//		return vec.get(index);
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
