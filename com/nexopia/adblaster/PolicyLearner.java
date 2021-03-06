package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.ServablePropertyHolder;

public class PolicyLearner {
	AbstractAdBlasterUniverse universe;
	AdBlasterPolicy pol;
	Vector<Banner> banners = null;
	
	PolicyLearner(AdBlasterPolicy pol, AbstractAdBlasterUniverse ac){
		banners = new Vector<Banner>();
		universe = ac;
		this.pol = pol;
	}
	
	public void upgradePolicy(AbstractAdBlasterInstance chunk, AdBlasterThreadedOperation op) {
		System.out.println("Upgrading.");
		int sbefore[] = new int[universe.getBannerCount()];
		int i = 0;
		for (ServablePropertyHolder b : universe.getBanners()){
			sbefore[i++] = chunk.bannerCount(b);
		}
		

		float count = -1;
		float newcount = 0;
		int iterations = 0;
		while((newcount = chunk.totalProfit()) != count && iterations < 2){
			System.out.println("An iteration...");
			iterations++;
			count = newcount;
			op.iterativeImprove(chunk);
		}
		System.out.println("Calculating Banner Coefficients...");
		i=0;
		for (Banner b : universe.getBanners()){
			int after = chunk.bannerCount(b);
			int before = sbefore[i];
			float f = ((float)((1.0f + after) / (1.0f + before)));
			pol.incrementMultiply(b, (float)Math.pow(f, 1.0f)); 
		}
		
		synchronized(banners) {
			banners = pol.orderBannersByScore(chunk);
		}

	}

}
