package com.nexopia.adblaster.struct;

import java.util.Vector;

import com.nexopia.adblaster.AbstractAdBlasterInstance;

public class PageView {
	Vector<BannerView> bannerViews;
	
	public PageView(Vector<BannerView> bannerViews) {
		this.bannerViews = bannerViews;
	}
	
	public int getPayRate(AbstractAdBlasterInstance instance) {
		int payrate = 0;
		for (BannerView bv: bannerViews) {
			payrate += instance.getBanner(bv.getBannerId()).getPayrate(instance);
		}
		return payrate;
	}
	
	public PageView clone() {
		return new PageView(new Vector<BannerView>(bannerViews));
	}

	public Vector<BannerView> getViews() {
		return bannerViews;
	}

	public void clearViews() {
		for (BannerView bv: bannerViews) {
			bv.setBannerID(0);
		}
	}

	public void update(PageView bestPageView) {
		for (int i=0; i<this.bannerViews.size(); i++) {
			this.bannerViews.get(i).setBannerID(bestPageView.getViews().get(i).getBannerId());
		}
	}

}
