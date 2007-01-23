package com.nexopia.adblaster.struct;

import java.util.Vector;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.AdBlasterThreadedInstance;

public class PageView implements Cloneable {
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
			Banner b = instance.getBanner(bv.getBannerId());
			if (b != null) {
				payrate += b.getPayrate(instance);
			}
		}
		return payrate;
	}
	
	public PageView clone() {
		Vector<BannerView> new_vector = new Vector<BannerView>(bannerViews);
		for (int i=0; i< new_vector.size(); i++) {
			new_vector.set(i, new_vector.get(i).clone());
		}
		return new PageView(this.id, new_vector);
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
			int newBannerID = bestPageView.getViews().get(i).getBannerId();
			Banner b = chunk.getBanner(newBannerID);
			chunk.notifyChange(bv, b);
			bv.setBannerID(newBannerID);
		}
	}

	public void addBannerView(BannerView bv) {
		bannerViews.add(bv);
	}
	
	public int getID() {
		return this.id;
	}

}
