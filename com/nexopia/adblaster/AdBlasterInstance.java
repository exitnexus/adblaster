package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;


/*
 * A representation of all data we store for one day, along with some functions to
 * fill with learning data.
 */
public class AdBlasterInstance {
	Vector views;
	AdCampaign campaign;
	Environment dbEnv;
	BannerViewDatabase db;
	
	
	AdBlasterInstance(AdCampaign ac){
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
		
		campaign = ac;
		views = new Vector();
	}
	
	public void fillInstance(AdBlasterPolicy pol){
		for (int i = 0; i < views.size(); i++){
			BannerView bv = (BannerView)views.get(i);
			bv.b = null;
		}
		for (int i = 0; i < views.size(); i++){
			BannerView bv = (BannerView)views.get(i);
			Banner b = getBestBanner(pol, bv);
			bv.b = b;
			try {
				db.insert(bv);
			} catch (DatabaseException dbe) {
				System.err.println("DatabaseException: " + dbe);
			}
		}
	}

	private Banner getBestBanner(AdBlasterPolicy pol, BannerView bv) {
		User u = bv.u;
		int t = bv.getTime();
		
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < campaign.b.length; j++){
			Banner b = campaign.b[j];
			float score = ((Float)pol.coefficients.get(b)).floatValue();
			if (!isValidBannerForUser(u, b) || (this.count(b) >= b.max_hits)){
				score = -1;
			}
			if (score > bestScore){
				bestScore = score;
				bestMatch = j;
			}
		}
		
		Banner banner = campaign.b[bestMatch];
		return banner;
	}

	public boolean isValidBannerForUser(User u, Banner b) {
		for (int k = 0; k < b.interests.checked.size(); k++){
			Integer interest = (Integer)b.interests.checked.get(k);
			if (!u.interests.has(interest.intValue()))
				return false;
		}
		return true;
	}
	
	public BannerView randomView(AdCampaign campaign) {
		// TODO Auto-generated method stub
		User randomPick = campaign.u[(int) (Math.random()*campaign.u.length)];
		int time = (int) (Math.random()*60*60*24);
		return new BannerView(randomPick, null, time);
	}

	public static AdBlasterInstance randomInstance(int num, AdCampaign ac) {
		AdBlasterInstance instance = new AdBlasterInstance(ac);
		for (int i = 0; i < num; i++){
			instance.views.add(instance.randomView(ac));
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
		for (int i = 0; i < this.campaign.b.length; i++){
			Banner b = (Banner)this.campaign.b[i];
			int count = count(b);
			if (count < b.max_hits){
				unserved.add(new Tuple(b, new Integer(count)));
			}
		}
		return unserved;
	}

	private int count(Banner banner) {
		int count = 0;
		for (int i = 0; i < views.size(); i++){
			if (((BannerView)views.get(i)).b == banner){
				count ++;
			}
		}
		return count;
	}

	public float totalProfit() {
		float count = 0;
		for (int i = 0; i < views.size(); i++){
			if (((BannerView)views.get(i)).b != null){
				count += ((BannerView)views.get(i)).b.profit;
			}
		}
		return count;
	}

}
