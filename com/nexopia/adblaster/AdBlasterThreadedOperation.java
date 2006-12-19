/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;

public final class AdBlasterThreadedOperation implements Runnable {
	private static final int swap_max = 0; //The maximum depth of swap searches
	
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

	public boolean serveView(AbstractAdBlasterInstance instanc, BannerView bv, Banner b){
			// System.out.println("Trying bannerview " + j);
		if (bv.getBannerId() == 0 || gd.universe.getBannerByID(bv.getBannerId()).getPayrate(instanc) < b.getPayrate(instanc)){
			if (instanc.isValidBannerForView(bv,b)){
				// single swap
				chunk.notifyChange(bv, b);
				bv.setBanner(b);
				return true;
			}
		}
		
		
		if (swap_max > 0){
			// Then try DFS
			boolean doable = false;
			if ((instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT) &&
					(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT)){
		
				if (instanc.isValidBannerForView(bv,b)){
					doable = true;
				}
			}
		
			if (doable){
		
				// System.out.println("Trying bannerview " + j);
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
						return true;
					}
				}
			}
		}
		return false;

	}
	
	public void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector<Banner> unserved = instanc.orderBannersByPayrate(instanc.getUnserved());
		System.out.println("Payrates:");
		for (Banner banner : unserved){
			System.out.println(banner.getPayrate(instanc));
		}
		System.out.println("Improving based on unserved banners.");
		
		for (int j = 0; j < instanc.getViewCount();	j++){
			BannerView bv = instanc.getViews().elementAt(j);
			if (j % 1000 == 0)
				System.out.println(j + " of " + instanc.getViewCount());

			for (int i = 0; i < unserved.size(); i++){
				Banner b = (Banner)unserved.get(i);
				// First try simple search...mo
				if ((instanc.bannerCount(b) < b.getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT) &&
						(instanc.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT)){
					 
					if (serveView(instanc, bv, b))
						break; 	//You already swapped in the best banner possible, 
								//so don't try anymore.
				}
			}
		}
	}

	public synchronized boolean isFinished() {
		return this.finished;
	}

}
