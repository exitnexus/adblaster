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
	
	public void fillInstance(AdBlasterPolicy pol) {
	}

	public boolean isValidBannerForUser(User u, Banner b) {
		return false;
	}

	public BannerView randomView(AdBlasterUniverse campaign) {
		return null;
	}

	public Vector getUnserved() {
		return null;
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
