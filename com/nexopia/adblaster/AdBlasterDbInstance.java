package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.WeakHashMap;

import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.BannerViewFlatFileWriter;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{
	HashMap swappedViews; //always look for a view here before checking the database
	BannerViewFlatFileReader db;
	UserFlatFileReader userDB;

	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
	}
	public void loadNoCount(File dbf, File u_dbf, File data) {
		try {
			db = new BannerViewFlatFileReader(dbf);
			userDB = new UserFlatFileReader(u_dbf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(data));
			br.readLine();
			String str = "";
			while ((str = br.readLine()) != null){
				String r[] = str.split(", ");
				Banner b = universe.getBannerByID(Integer.parseInt(r[1]));
				this.bannerCountMap.put(b, new Integer(Integer.parseInt(r[2])));
				if (!this.campaignCountMap.containsKey(b.getCampaign())){
					this.campaignCountMap.put(b.getCampaign(), new Integer(0));
				}
				this.campaignCountMap.put(b.getCampaign(), new Integer(this.campaignCount(b) + Integer.parseInt(r[2])));
				System.out.println(r[1] + ":" + r[2]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void load(File f, File u_dbf) {
		try {
			System.out.println("Counting Bannerviews.");
			db = new BannerViewFlatFileReader(f);
			userDB = new UserFlatFileReader(u_dbf);
			/*{
				long time = System.currentTimeMillis();
				for (int i = 0; i < db.getBannerViewCount(); i++){
					ProgressIndicator.show(i, db.getBannerViewCount());
					BannerView bv = db.get(i);
					if (bv.getBanner() != null){
						updateMap(bv);
					}
					weakmap.put(bv, Boolean.TRUE);
				}
				System.out.println(System.currentTimeMillis() - time);
			}*/
			
			long time = System.currentTimeMillis();
			for (int i = 0; i < BannerViewFlatFileWriter.FILE_COUNT; i++){
				db.load(i);
				userDB.load(i);
				for (BannerView bv : db.getCurrentBannerViews()){
					if (bv.getBannerId() != 0){
						updateMap(bv);
					}
				}
			}
			System.out.println(System.currentTimeMillis() - time);

			int i = 0;
			for (Banner b : this.universe.getBanners()){
				if (b != null){
					System.out.print(i++ + ", ");
					System.out.print(b.getID() + ", ");
					System.out.println(bannerCount(b));
				}
			}
			System.out.println("Done.");
		} catch (Exception e) {
			System.err.println("Failed to make a BannerViewDatabase object: " + e);
			e.printStackTrace();
		}
	}
	
	public User getUser(int i) {
		User u = userDB.getUser(i);
		return u;
	}


	public float totalProfit(){
		float count = 0;
		for (Banner b : this.universe.getBanners()){
			float worth = this.bannerCount(b) * b.getRealPayrate();
			count += worth;
		}
		return count;
	}

	/*public void test(){
		BannerViewCursor cursor;
		try {
			cursor = db.getCursor(0,0,0);
			int i = 0;
			BannerView bv = null;
			bv = cursor.getCurrent();
			while(bv  != null && i < 10000){
				i++;
				if (i%100 == 0){
					System.out.println("Loaded bannerview " + i + ": " + bv);
				}
				
				//bv.b = pol.getBestBanner(this, bv);
				bv = cursor.getNext();
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/
	
	public void fillInstance(I_Policy pol) {
		System.out.println("Clearing " + getViewCount() + " instances.");
		long time = System.currentTimeMillis();
		for (BannerView bv : getViews()){
			notifyChange(bv, null);
			bv.setBanner(null);
			if ((System.currentTimeMillis() - time) > 5000){
				//ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
		}
		System.out.println("Filling " + getViewCount() + " instances.");
		for (BannerView bv : getViews()){
			if ((System.currentTimeMillis() - time) > 5000){
				//ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
			Banner b = pol.getBestBanner(this, bv);
			//Banner b = universe.getRandomBannerMatching(bv, this);
			notifyChange(bv, b);
			bv.setBanner(b);
		}
	}

	/*public AbstractAdBlasterInstance copy() {
		AdBlasterDbInstance instance = new AdBlasterDbInstance(this.universe);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			instance.addView(new BannerView(getUserForView(i), getBannerForView(i), getTimeForView(i)));
		}
		return instance;

	}*/
	
//	public int getViewCount() {
	//	return db.lastid;
	//}

	/*XXX: Next three should update the database.*/
	public void notifyChange(BannerView view, Banner b) {
		super.notifyChange(view,b);
		updateDB(view,b);
	}
	
	private void updateDB(BannerView view, ServablePropertyHolder b) {
		/*try {
			IntegerBinding ib = new IntegerBinding();
			DatabaseEntry key = new DatabaseEntry();
			ib.objectToEntry(new Integer(view.getIndex()), key);
			BannerViewBinding bvb = AdBlaster.instanceBinding;
			DatabaseEntry data = new DatabaseEntry();
			bvb.objectToEntry(view, data);
			db.db.delete(null, key);
			view.setBannerWithoutFire(b);
			db.db.put(null, key, data);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}*/
	}
	public void notifyChangeUser(BannerView view) {
		updateDB(view, universe.getBannerByID(view.getBannerId()));
	}
	public void notifyChangeTime(BannerView view) {
		updateDB(view, universe.getBannerByID(view.getBannerId()));
	}
	@Override
	public int getViewCount() {
		return db.getBannerViewCount();
	}
	@Override
	public Vector<BannerView> getViews() {
		throw new UnsupportedOperationException("Don't call this method - no way to" +
				"load all bannerviews into memory at once.");
	}
	@Override
	public int getMinViewsPerInstance(Banner banner) {
		return banner.getCampaign().getMinViewsPerDay();
	}


}
