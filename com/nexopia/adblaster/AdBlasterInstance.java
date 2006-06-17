package com.nexopia.adblaster;

import java.util.Vector;


public class AdBlasterInstance {
	Vector views;
	AdBlasterInstance(int num, AdCampaign ac, AdBlasterPolicy pol){
		for (int i = 0; i < num; i++){
			views.add(ac.randomView(pol));
		}
	}

}
