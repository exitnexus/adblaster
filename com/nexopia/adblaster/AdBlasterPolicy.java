package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class AdBlasterPolicy {
	HashMap coefficients;
	AdBlasterUniverse campaign;
	
	public AdBlasterPolicy(AdBlasterUniverse ac) {
		coefficients = new HashMap();
		campaign = ac;
		for (int i = 0; i < ac.b.length; i++){
			coefficients.put(ac.b[i], new Float(Math.random()));
		}
	}

	public static AdBlasterPolicy randomPolicy(AdBlasterUniverse ac) {
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
		for (int i = 0; i < campaign.b.length; i++){
			int totalAvailable = 1;
			int totalUsed = 0;
			Banner b = campaign.b[i];
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

}
