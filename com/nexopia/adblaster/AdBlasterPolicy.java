package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Arrays;

public class AdBlasterPolicy implements I_Policy {
	private HashMap coefficients;
	AbstractAdBlasterUniverse universe;
	Vector banners = null;
	
	public AdBlasterPolicy(AbstractAdBlasterUniverse ac) {
		coefficients = new HashMap();
		universe = ac;
		for (int i = 0; i < ac.getBannerCount(); i++){
			coefficients.put(ac.getBannerByIndex(i), new Float(Math.random()));
		}
	}

	public static I_Policy randomPolicy(AbstractAdBlasterUniverse ac) {
		return new AdBlasterPolicy(ac);
	}

	public void increment(Banner b, double d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() + d));
		banners = null;
	}

	public void upgradePolicy(AbstractAdBlasterInstance instance) {
		/*boolean changed = true;
		while (changed){
			changed = false;
			Vector unserved = instance.getUnserved();
			for (int i = 0; i < unserved.size(); i++){
				Tuple t = (Tuple)unserved.get(i);
				Banner b = (Banner)t.data.get(0); 
				int c = ((Integer)t.data.get(1)).intValue();
				if (b.getPayrate() > 0){
					for (int j = 0; j < instance.views.size() && c > 0; j++){
						BannerView bv = (BannerView) instance.views.get(j);
						if (bv.b.getPayrate() < b.getPayrate() && instance.isValidBannerForUser(bv.u,b)){
							changed = true;
							c--;
							this.increment(b, 0.1);
							this.increment(bv.b, -0.1);
							bv.b = b;
						}
					}
				}
			}
		}*/
		float count = -1;
		while(instance.totalProfit() != count){
			count = instance.totalProfit();
			AdBlaster.iterativeImprove(instance);
		}
		for (int i = 0; i < universe.getBannerCount(); i++){
			int totalAvailable = 1;
			int totalUsed = 0;
			Banner b = universe.getBannerByIndex(i);
			System.out.println("Calculating banner " + i + "/" + universe.getBannerCount());
			for (int j = 0; j < instance.getViewCount(); j++){
				BannerView bv = instance.getView(j);
				if (instance.isValidBannerForView(bv,b)){
					totalAvailable++;
				}
				if (bv.getBanner() == b){
					totalUsed++;
				}
			}
			float f = ((float)totalUsed)/(float)Math.min(b.getMaxHits(),totalAvailable);
			coefficients.put(b, new Float(f)); 
			banners = null;
		}

	}

	public Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv) {
		User u = bv.getUser();
		int t = bv.getTime();
		if (banners == null){
			banners = orderBannersByScore(instance);
		}
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.universe.getBannerCount(); j++){
			Banner b = (Banner) banners.get(j);
			float score = ((Float)coefficients.get(b)).floatValue();
			if (score > bestScore){
				if ( instance.count(b) < b.getMaxHits() ){
					if (instance.isValidBannerForView(bv, b)){
						bestScore = score;
						bestMatch = j;
					}
				}
			}
		}
		
		Banner banner = instance.universe.getBannerByIndex(bestMatch);
		return banner;
	}
	

	private Vector orderBannersByScore(AbstractAdBlasterInstance instance) {
		Vector vec = new Vector();
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.universe.getBannerCount(); j++){
			Banner b = instance.universe.getBannerByIndex(j);
			float score = ((Float)coefficients.get(b)).floatValue();
			int i = 0;
			float score2 = Float.NEGATIVE_INFINITY;
			while (i < vec.size() && score2 > score){
				Banner b2 = (Banner) vec.get(i);
				score2 = ((Float)coefficients.get(b2)).floatValue();
				i++;
			}
			vec.insertElementAt(b, i);
			
		}
		return vec;
	}

	public Float getCoefficient(Banner bannerByIndex) {
		return (Float)coefficients.get(bannerByIndex);
	}

}
