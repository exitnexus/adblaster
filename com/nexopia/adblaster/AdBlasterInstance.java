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
	
	public AdBlasterInstance(AbstractAdBlasterUniverse ac) {
		super(ac);
	}

	public void fillInstance(AdBlasterPolicy pol){
		for (int i = 0; i < views.size(); i++){
			BannerView bv = (BannerView)views.get(i);
			bv.b = null;
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < views.size(); i++){
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)views.size())*100 + "% complete.");
				time = System.currentTimeMillis();
			}
			BannerView bv = (BannerView)views.get(i);
			Banner b = pol.getBestBanner(this, bv);
			bv.b = b;
		}
	}

	public boolean isValidBannerForUser(User u, Banner b) {
		return u.interests.hasAllIn(b.interests);
	}
	
	public BannerView randomView(AbstractAdBlasterUniverse ac) {
		// TODO Auto-generated method stub
		User randomPick = campaign.getUser((int) (Math.random()*campaign.getUserCount()));
		int time = (int) (Math.random()*60*60*24);
		return new BannerView(randomPick, null, time);
	}

	public static AdBlasterInstance randomInstance(int num, AbstractAdBlasterUniverse ac) {
		AdBlasterInstance instance = new AdBlasterInstance(ac);
		for (int i = 0; i < num; i++){
			BannerView bv = instance.randomView(ac);
			instance.views.add(bv);
		}
		return instance;
	}

	/**
	 * For a particular instance, get a list of all of the banners that were not served
	 * that could have made a profit.
	 * @return A vector of banners.
	 */
	public Vector getUnserved() {
		Vector unserved = new Vector();
		for (int i = 0; i < this.campaign.getBannerCount(); i++){
			Banner b = (Banner)this.campaign.getBanner(i);
			int count = count(b);
			if (count < b.getMaxHits()){
				unserved.add(new Tuple(b, new Integer(b.getMaxHits() - count)));
			}
		}
		return unserved;
	}


	public AbstractAdBlasterInstance copy() {
		AdBlasterInstance instance = new AdBlasterInstance(this.campaign);
		instance.views = new Vector();
		for (int i = 0; i < this.views.size(); i++){
			instance.views.add(((BannerView)this.views.get(i)).copy());
		}
		instance.dbEnv = this.dbEnv;
		instance.db = this.db;
		return instance;

	}
	
	public void makeMeADatabase(){
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
				dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
			UserDatabase userDb = new UserDatabase(dbEnv);
			Random r = new Random(1);
			for (int i=0; i<this.views.size(); i++) {
				db.insert((BannerView)this.views.get(i));
			}
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}

}
