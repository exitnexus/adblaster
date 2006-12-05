package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.WeakHashMap;

import com.nexopia.adblaster.db.BannerViewBinding;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.UserDatabase;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.ProgressIndicator;
import com.sleepycat.je.DatabaseException;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{
	HashMap swappedViews; //always look for a view here before checking the database
	BannerViewFlatFileReader db;
	private UserFlatFileReader userDB;
	public BannerViewBinding instanceBinding;

	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
		instanceBinding = new BannerViewBinding(this.universe, this);
	}
	public void loadNoCount(File dbf, File u_dbf, File data) {
		try {
			db = new BannerViewFlatFileReader(dbf, instanceBinding);
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
			db = new BannerViewFlatFileReader(f, instanceBinding);
			ProgressIndicator.setTitle("Counting bannerviews...");
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
			
			int i=0;
			long time = System.currentTimeMillis();
			BannerViewCursor c = db.getCursor();
			BannerView bv;
			while ((bv = c.getNext()) != null){
				i++;
				if (i > db.getBannerViewCount()){
					System.out.println(bv.getIndex() + ":" + bv.getUserID());
				}
				ProgressIndicator.show(i, db.getBannerViewCount());
				if (bv.getBanner() != null){
					updateMap(bv);
				}
			}
			System.out.println(System.currentTimeMillis() - time);

			for (i = 0; i < this.universe.getBannerCount(); i++){
				Banner b = this.universe.getBannerByIndex(i);
				if (b != null){
					System.out.print(i + ", ");
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
		//Integer I = Integer.valueOf(i);
		User u = userDB.getUser(i);
		//I.free();
		return u;
	}


	public float totalProfit(){
		float count = 0;
		for (int i = 0; i < this.universe.getBannerCount(); i++){
			Banner b = this.universe.getBannerByIndex(i);
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
	
	public void close(){
		try {
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fillInstance(I_Policy pol) {
		System.out.println("Clearing " + getViewCount() + " instances.");
		ProgressIndicator.setTitle("% complete clearing");
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			getView(i).setBanner(null);
			if ((System.currentTimeMillis() - time) > 5000){
				ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
		}
		System.out.println("Filling " + getViewCount() + " instances.");
		ProgressIndicator.setTitle("% complete filling");
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if ((System.currentTimeMillis() - time) > 5000){
				ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
			Banner b = pol.getBestBanner(this, bv);
			//Banner b = universe.getRandomBannerMatching(bv, this);
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
	
	protected BannerView getView(int index) {
		BannerView bv = db.get(index);
		return bv;
	}

	public int getViewCount() {
		return db.lastid;
	}

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
		updateDB(view, view.getBanner());
	}
	public void notifyChangeTime(BannerView view) {
		updateDB(view, view.getBanner());
	}


}
