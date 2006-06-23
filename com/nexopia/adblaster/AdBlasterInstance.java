package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;


/*
 * A representation of all data we store for one day, along with some functions to
 * fill with learning data.
 */
public class AdBlasterInstance extends AbstractAdBlasterInstance{
	private Vector views;
	
	public AdBlasterInstance(AbstractAdBlasterUniverse ac) {
		super(ac);
		views = new Vector();
		
	}

	public void fillInstance(AdBlasterPolicy pol){
		for (int i = 0; i < getViewCount(); i++){
			setBannerView(i, null);
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete.");
				time = System.currentTimeMillis();
			}
			//Banner b = pol.getBestBanner(this, bv);
			Banner b = universe.getRandomBannerMatching(getBannerForView(i), getUserForView(i), getTimeForView(i), this);
			setBannerView(i, b);
		}
	}

	public void addView(BannerView bv) {
		this.views.add(bv);
		if (bv.b != null){
			this.bannerCountMap.put(bv.b, new Integer(((Integer)bannerCountMap.get(bv.b)).intValue()+1));
		}
	}
	
	public AbstractAdBlasterInstance copy() {
		AdBlasterInstance instance = new AdBlasterInstance(this.universe);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			instance.addView(new BannerView(getUserForView(i), getBannerForView(i), getTimeForView(i)));
		}
		return instance;

	}

	public Banner getBannerForView(int i){
		return ((BannerView)this.views.get(i)).b;
	}
	public User getUserForView(int i){
		return ((BannerView)this.views.get(i)).u;
	}
	public int getTimeForView(int i){
		return ((BannerView)this.views.get(i)).time;
	}
	
	public static AdBlasterInstance randomInstance(int num, AbstractAdBlasterUniverse ac) {
		AdBlasterInstance instance = new AdBlasterInstance(ac);
		for (int i = 0; i < num; i++){
			BannerView bv = instance.randomView(ac);
			instance.addView(bv);
		}
		return instance;
	}

	public float totalProfit() {
		float count = 0;
		for (int i = 0; i < views.size(); i++){
			if (((BannerView)views.get(i)).b != null){
				count += ((BannerView)views.get(i)).b.getPayrate();
			}
		}
		return count;
	}
	
	public int getViewCount() {
		return this.views.size();
	}

	public void makeMeADatabase(Environment dbEnv){
		try {			

			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			
			BannerViewDatabase db = new BannerViewDatabase();

			Random r = new Random(1);
			System.out.println("Should be inserting " + this.getViewCount() + " BannerViews.");
			for (int i=0; i<this.getViewCount(); i++) {
				db.insert((BannerView)this.views.get(i));
			}
			
			db.close();
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}
	
	public void setBannerView(int j, Banner b) {
		BannerView bv = (BannerView)views.get(j);
		if (bv.b != null){
			this.bannerCountMap.put(bv.b, new Integer(((Integer)bannerCountMap.get(bv.b)).intValue()-1));
		}
		if (b != null){
			this.bannerCountMap.put(b, new Integer(((Integer)bannerCountMap.get(b)).intValue()+1));
		}
		
		bv.b = b;
	}

}
