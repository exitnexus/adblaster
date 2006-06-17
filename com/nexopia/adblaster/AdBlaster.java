package com.nexopia.adblaster;

import java.util.Vector;

public class AdBlaster {

	
	public static void main(String args[]){
		AdCampaign ac = AdCampaign.generateTestData(100,100);
		
		AdBlasterPolicy pol = new AdBlasterPolicy(ac);
		
		AdBlasterInstance instance = new AdBlasterInstance(1000, ac);
		
		for (int i = 0; i < 10; i++){
			instance.fillInstance(pol);
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

	
}
