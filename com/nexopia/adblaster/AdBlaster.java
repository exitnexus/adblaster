package com.nexopia.adblaster;

import java.util.Vector;

public class AdBlaster {

	
	public static void main(String args[]){
		AdCampaign ac = AdCampaign.generateTestData(100,100);
		
		AdBlasterPolicy pol = new AdBlasterPolicy(ac);
		
		for (int i = 0; i < 10; i++){
			AdBlasterInstance instance = AdBlaster.generateInstance(pol, ac);
			pol = upgradePolicy(instance);
		}
	}

	private static AdBlasterPolicy upgradePolicy(AdBlasterInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	private static AdBlasterPolicy generatePolicy(AdCampaign ac) {
		// TODO Auto-generated method stub
		return null;
	}

	private static AdBlasterInstance generateInstance(AdBlasterPolicy pol, AdCampaign ac) {
		/*
		 * Based on an existing policy, generate what we would have done with it.
		 */
		
		return null;
	}

	
}
