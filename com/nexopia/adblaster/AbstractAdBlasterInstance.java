package com.nexopia.adblaster;

import java.util.Vector;

import com.sleepycat.je.Environment;

public abstract class AbstractAdBlasterInstance {

	Vector views;
	Environment dbEnv;
	BannerViewDatabase db;
	AbstractAdBlasterUniverse campaign;
	
	public AbstractAdBlasterInstance(AbstractAdBlasterUniverse ac){
		campaign = ac;
		views = new Vector();
	}
	
	public abstract void fillInstance(AdBlasterPolicy pol);
	
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

	public Vector getUnserved() {
		/**
		 * For a particular instance, get a list of all of the banners that were not served
		 * that could have made a profit.
		 * @return A vector of banners.
		 */
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

	int count(Banner banner) {
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
				count += ((BannerView)views.get(i)).b.getPayrate();
			}
		}
		return count;
	}

	abstract public AbstractAdBlasterInstance copy();
	
}
