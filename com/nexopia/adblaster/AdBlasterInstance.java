package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.AdCampaign.Banner;
import com.nexopia.adblaster.AdCampaign.User;

/*
 * A representation of all data we store for one day, along with some functions to
 * fill with learning data.
 */
public class AdBlasterInstance {
	Vector views;
	AdCampaign campaign;
	
	AdBlasterInstance(int num, AdCampaign ac){
		campaign = ac;
		for (int i = 0; i < num; i++){
			views.add(randomView(ac));
		}
	}
	
	public void fillInstance(AdBlasterPolicy pol){
		for (int i = 0; i < views.size(); i++){
			Banner banner = campaign.b[(int) (Math.random()*campaign.b.length)];
			((BannerView)views.get(i)).b = banner;
		}
	}
	
	public BannerView randomView(AdCampaign campaign) {
		// TODO Auto-generated method stub
		User randomPick = campaign.u[(int) (Math.random()*campaign.u.length)];
		int time = (int) (Math.random()*60*60*24);
		return new BannerView(randomPick, null, time);
	}

}
