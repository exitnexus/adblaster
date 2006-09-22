package com.nexopia.adblaster;

import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.ProgressIndicator;

public class AdBlasterThreadedInstance extends AbstractAdBlasterInstance {
	private Vector<BannerView> views;
	private GlobalData gd;
	
	public AdBlasterThreadedInstance(GlobalData gd) {
		super(gd.universe);
		this.gd = gd;
		views = new Vector<BannerView>();
		
	}

	public float totalProfit(){
		System.out.println("Calculating profit.");
		float count = 0;
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if (System.currentTimeMillis() - time > 5000){
				ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
			BannerView bv =((BannerView)getView(i)); 
			if (bv.getBanner() != null){
				count += bv.getBanner().getRealPayrate();
			}
		}
		return count;
	}
	
	@Override
	public void fillInstance(I_Policy pol) {
		long time = System.currentTimeMillis();
		System.out.println("Filling instance.");
		ProgressIndicator.setTitle("% Complete Filling");
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if (bv.getBanner() != null){
				throw new UnsupportedOperationException();
			}
			if ((System.currentTimeMillis() - time) > 5000){
				ProgressIndicator.show(i, getViewCount());
				time = System.currentTimeMillis();
			}
			Banner b = pol.getBestBanner(this, bv);
			//Banner b = universe.getRandomBannerMatching(bv, this);
			bv.setBanner(b);
		}
	}

	@Override
	protected BannerView getView(int i) {
		return views.get(i);
	}

	@Override
	public int getViewCount() {
		return views.size();
	}

	@Override
	public int bannerCount(ServablePropertyHolder banner) {
		return gd.instance.bannerCount(banner);
	}

	int campaignCount(Banner banner) {
		return gd.instance.campaignCount(banner);
	}

	@Override
	public Vector<Banner> getUnserved() {
		return gd.getUnserved();
	}

	public void addView(BannerView bv) {
		views.add(bv);
	}

	
}
