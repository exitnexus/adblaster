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

}
