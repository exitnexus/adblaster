package com.nexopia.adblaster.struct;

import java.util.Vector;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.AdBlasterThreadedInstance;

public class PageView {
	Vector<BannerView> bannerViews;
	private int id;
	
	public PageView(int pageID, Vector<BannerView> bannerViews) {
		this.id = pageID;
		this.bannerViews = bannerViews;
	}
	
	public PageView(int pageID) {
		this.id = pageID;
		this.bannerViews = new Vector<BannerView>();
	}

	public int getPayRate(AbstractAdBlasterInstance instance) {
		int payrate = 0;
		for (BannerView bv: bannerViews) {
			payrate += instance.getBanner(bv.getBannerId()).getPayrate(instance);
		}
		return payrate;
	}
	
	public PageView clone() {
		return new PageView(this.id, new Vector<BannerView>(bannerViews));
	}

	public Vector<BannerView> getViews() {
		return bannerViews;
	}

	public void clearViews() {
		for (BannerView bv: bannerViews) {
			bv.setBannerID(0);
		}
	}

	public void update(PageView bestPageView, AdBlasterThreadedInstance chunk) {
		for (int i=0; i<this.bannerViews.size(); i++) {
			BannerView bv = this.bannerViews.get(i);
			Banner b = chunk.getBanner(bv.getBannerId());
			chunk.notifyChange(bv, b);
			bv.setBannerID(bestPageView.getViews().get(i).getBannerId());
		}
	}

	public void addBannerView(BannerView bv) {
		bannerViews.add(bv);
	}
	
	public int getID() {
		return this.id;
	}

}
