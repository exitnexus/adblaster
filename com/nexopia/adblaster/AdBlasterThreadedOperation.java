/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;

public final class AdBlasterThreadedOperation implements Runnable {
	private static final int swap_max = 4; //The maximum depth of swap searches
	
	private final GlobalData gd;
	private final AdBlasterThreadedInstance chunk;
	private boolean finished;
	private float original_profit;
	
	public AdBlasterThreadedOperation(GlobalData globalData, AdBlasterThreadedInstance chunk) {
		super();
		this.gd = globalData;
		this.chunk = chunk;
		
		finished = false;
	}

	public void run() {
		finished = false;
		operateOnChunk(chunk);
	}
	
	public synchronized void operateOnChunk(AdBlasterThreadedInstance chunk) {
		System.out.println("Operating on a chunk...");
		for (Banner b : gd.universe.getBanners()){
			System.out.println(b.getID() + " : " + chunk.bannerCount(b));
		}
		original_profit = chunk.totalProfit();

		for (int k = 0; k < chunk.getViewCount(); k++){
			BannerView bv = chunk.getViews().elementAt(k);
			chunk.notifyChange(bv, null);
			bv.setBanner(null);
		}

		chunk.fillInstance(gd.pol);
		
	
		System.out.println("Upgrading policy.");
		new PolicyLearner(gd.pol, gd.universe).upgradePolicy(chunk, this);
		
		for (BannerView bv : chunk.getViews()){
			chunk.notifyChange(bv, null);
			bv.setBanner(null);
		}

		chunk.fillInstance(gd.pol);
		
		for (Banner b : gd.universe.getBanners()){
			System.out.println(b.getID() + " : " + chunk.bannerCount(b));
		}

		System.out.println(chunk.getViewCount());
		System.out.println("Profits compared");
		System.out.println("original: " + original_profit);
		System.out.println("new: " + chunk.totalProfit());
		this.finished = true;
		this.notify();
	}

	
	public void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector<Banner> unserved = instanc.getUnserved();
		System.out.println("Improving based on unserved banners.");
		for (int i = 0; i < unserved.size(); i++){
			Banner b = (Banner)unserved.get(i);
			
			// First try simple search...mo
			for (int j = 0; j < instanc.getViewCount() && 
					(instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()) &&
					(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay());
					j++){
				// System.out.println("Trying bannerview " + j);
				BannerView bv = instanc.getViews().elementAt(j);
				if (bv.getBannerId() == 0 || gd.universe.getBannerByID(bv.getBannerId()).getPayrate(instanc) < b.getPayrate(instanc)){
					if (instanc.isValidBannerForView(bv,b)){
						// single swap
						chunk.notifyChange(bv, b);
						bv.setBanner(b);
					}
				}
			}
			
			// Then try DFS
			boolean doable = false;
			for (int j = 0; j < instanc.getViewCount() && 
				(instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()) &&
				(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay());
				j++){
				BannerView bv = instanc.getViews().elementAt(j);
				if (instanc.isValidBannerForView(bv,b)){
					doable = true;
				}
			}
			if (doable){
				for (int j = 0; j < instanc.getViewCount() && 
				(instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()) &&
				(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay());
				j++){
					// System.out.println("Trying bannerview " + j);
					BannerView bv = instanc.getViews().elementAt(j);
					if (bv.getBannerId() == 0 || gd.universe.getBannerByID(bv.getBannerId()).getPayrate(instanc) < b.getPayrate(instanc)){
						Vector swaps = null;
						for (int l = 1; l < swap_max; l+=2){
							Vector path = instanc.depthLimitedDFS(bv, b, l);
							if (path != null){
								swaps = path;
								break;
								
							}
						}
						if (swaps != null){
							instanc.doSwap(swaps, b);
							break;
						}
					}
				}
			}
		}
	}

	public synchronized boolean isFinished() {
		return this.finished;
	}

}
