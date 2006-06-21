package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class AdBlasterPolicy {
	HashMap coefficients;
	AbstractAdBlasterUniverse campaign;
	
	public AdBlasterPolicy(AbstractAdBlasterUniverse ac) {
		coefficients = new HashMap();
		campaign = ac;
		for (int i = 0; i < ac.getBannerCount(); i++){
			coefficients.put(ac.getBanner(i), new Float(Math.random()));
		}
	}

	public static AdBlasterPolicy randomPolicy(AbstractAdBlasterUniverse ac) {
		return new AdBlasterPolicy(ac);
	}

	public void increment(Banner b, double d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() + d));
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
		for (int i = 0; i < campaign.getBannerCount(); i++){
			int totalAvailable = 1;
			int totalUsed = 0;
			Banner b = campaign.getBanner(i);
			for (Iterator it = instance.views.iterator(); it.hasNext();){
				BannerView bv = (BannerView)it.next();
				if (instance.isValidBannerForUser(bv.u,b)){
					totalAvailable++;
				}
				if (bv.b == b){
					totalUsed++;
				}
			}
			float f = ((float)totalUsed)/(float)Math.min(b.maxHits,totalAvailable);
			System.out.println(totalUsed +","+Math.min(totalAvailable,b.maxHits));
			coefficients.put(b, new Float(f)); 
		}

	}

	Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv) {
		User u = bv.u;
		int t = bv.getTime();
		
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.campaign.getBannerCount(); j++){
			Banner b = instance.campaign.getBanner(j);
			float score = ((Float)coefficients.get(b)).floatValue();
			if (instance.isValidBannerForUser(u, b) && (instance.count(b) < b.getMaxHits())){
				if (score > bestScore){
					bestScore = score;
					bestMatch = j;
				}
			}
		}
		
		Banner banner = instance.campaign.getBanner(bestMatch);
		return banner;
	}

}
