package com.nexopia.adblaster;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.ProgressIndicator;

public class AdBlasterThreadedInstance extends AbstractAdBlasterInstance {
	private Vector<BannerView> views;
	private UserFlatFileReader userDB;
	private BannerViewFlatFileReader bannerDB;
	private GlobalData gd;
	
	public AdBlasterThreadedInstance(GlobalData gd, int subset_num) {
		super(gd.universe);
		bannerDB = new BannerViewFlatFileReader(gd.bannerViewDirectory);
		try {
			bannerDB.load(subset_num);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void addView(BannerView bv) {
		views.add(bv);
	}

	public User getUser(int i) {
		//Integer I = Integer.valueOf(i);
		User u = userDB.getUser(i);
		//I.free();
		return u;
	}

	public void addAddAllViews(Collection<BannerView> subset) {
		this.views.addAll(subset);
	}

	
}
