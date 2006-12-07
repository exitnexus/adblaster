/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;

public final class AdBlasterThreadedOperation implements Runnable {
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
		original_profit = chunk.totalProfit();

		chunk.fillInstance(gd.pol);
		
	
		System.out.println("Upgrading policy.");
		new PolicyLearner(gd.pol, gd.universe).upgradePolicy(chunk, this);
		
		for (int i = 0; i < chunk.getViewCount(); i++){
			chunk.getViews().elementAt(i).setBanner(null);
		}

		chunk.fillInstance(gd.pol);
		
		this.finished = true;
		this.notify();
	}

	
	public void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector<Banner> unserved = gd.fullDay.getUnserved();
		System.out.println("Improving based on unserved banners.");
		for (int i = 0; i < unserved.size(); i++){
			Banner b = (Banner)unserved.get(i);
			
			// First try simple search
			for (int j = 0; j < instanc.getViewCount() && 
					(instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()) &&
					(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay());
					j++){
				// System.out.println("Trying bannerview " + j);
				BannerView bv = instanc.getViews().elementAt(j);
				if (bv.getBannerId() == 0 || gd.universe.getBannerByID(bv.getBannerId()).getPayrate(instanc) < b.getPayrate(instanc)){
					if (instanc.isValidBannerForView(bv,b)){
						// single swap
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
						int swap_max = 0;
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