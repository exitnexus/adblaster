package com.nexopia.adblaster;

import java.util.Vector;

public class AdBlasterThreadedInstance extends AbstractAdBlasterInstance {
	private Vector<BannerView> views;
	private GlobalData gd;
	
	public AdBlasterThreadedInstance(GlobalData gd) {
		super(gd.universe);
		this.gd = gd;
		views = new Vector<BannerView>();
		
	}

	@Override
	public void fillInstance(I_Policy pol) {
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete clearing.");
				time = System.currentTimeMillis();
			}
			getView(i).setBanner(null);
		}
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete filling.");
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
	int count(Banner banner) {
		return gd.instance.count(banner);
	}

	@Override
	public Vector<Tuple<Banner,Integer>> getUnserved() {
		return gd.getUnserved();
	}

	public void addView(BannerView bv) {
		views.add(bv);
	}

	
}
