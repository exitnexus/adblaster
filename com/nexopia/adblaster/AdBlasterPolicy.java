package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Vector;


public class AdBlasterPolicy {
	HashMap coefficients;
	
	public AdBlasterPolicy(AdCampaign ac) {
		coefficients = new HashMap();
		for (int i = 0; i < ac.b.length; i++){
			coefficients.put(ac.b[i], new Float(1));
		}
	}

	public static AdBlasterPolicy randomPolicy(AdCampaign ac) {
		return new AdBlasterPolicy(ac);
	}

	public void increment(Banner b, double d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() + d));
	}

	public void upgradePolicy(AdBlasterInstance instance) {
		boolean changed = true;
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
		}
	}

}
