package com.nexopia.adblaster;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{
	HashMap swappedViews; //always look for a view here before checking the database
	CacheBannerViewDatabase db;
	
	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
	}
	public void load() {
		try {
			db = new CacheBannerViewDatabase();
			
		} catch (DatabaseException e) {
			System.err.println("Failed to make a BannerViewDatabase object: " + e);
			e.printStackTrace();
		}
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
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//TODO Don't know how this should behave, it needs to be dealt with
	public void fillInstance(I_Policy pol) {
		/* Doesn't really do anything */
		System.out.println("Filling " + getViewCount() + " instances.");
		for (int i = 0; i < getViewCount(); i++){
			getView(i).setBanner(null);
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete.");
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
	
	public static void main(String args[]){
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);

		Environment dbEnv = null;
		try {
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}

		AdBlasterDbUniverse abu = new AdBlasterDbUniverse();
		AdBlasterDbInstance abdbi = new AdBlasterDbInstance(abu);
		I_Policy pol = AdBlasterPolicy.randomPolicy(abu);
		abdbi.fillInstance(pol);
	}

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

	private void updateDB(BannerView view, Banner b) {
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
