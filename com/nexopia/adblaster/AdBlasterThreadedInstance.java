package com.nexopia.adblaster;

import java.io.FileNotFoundException;
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

public class AdBlasterThreadedInstance extends AbstractAdBlasterInstance {
	private Vector<BannerView> views;
	private GlobalData gd;
	
	public AdBlasterThreadedInstance(GlobalData gd, int subset_num) {
		super(gd.universe);
		try {
			gd.fullDay.db.load(subset_num);
			gd.fullDay.userDB.load(subset_num);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gd = gd;
		views = new Vector<BannerView>();
		views.addAll(gd.fullDay.db.getCurrentBannerViews());
		System.out.println("Loaded " + views.size() + " banner views.");
	}

	public float totalProfit(){
		int i = 0;
		System.out.println("Calculating profit.");
		float count = 0;
		long time = System.currentTimeMillis();
		for (BannerView bv : getViews()){ 
			i++;
			if (System.currentTimeMillis() - time > 5000){
				System.out.println("Percent: " + i/getViewCount());
				time = System.currentTimeMillis();
			}
			if ((bv != null) && (bv.getBannerId() != 0)){
				if (universe.getBannerByID(bv.getBannerId()) == null)
					System.out.println("Banner " + bv.getBannerId() + " is bad.");
				else
					count += universe.getBannerByID(bv.getBannerId()).getRealPayrate();
			}
		}
		return count;
	}
	
	@Override
	public void fillInstance(I_Policy pol) {
		int i = 0;
		long time = System.currentTimeMillis();
		System.out.println("Filling instance.");
		for (BannerView bv : getViews()){
			i++;
			if (bv.getBannerId() != 0){
				throw new UnsupportedOperationException();
			}
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("Percent: " + i / getViewCount());
				time = System.currentTimeMillis();
			}
			Banner b = pol.getBestBanner(this, bv);
			//Banner b = universe.getRandomBannerMatching(bv, this);
			bv.setBanner(b);
		}
	}

/*	@Override
	protected BannerView getView(int i) {
		return views.get(i);
	}
*/
	@Override
	public int getViewCount() {
		return views.size();
	}

	public void addView(BannerView bv) {
		views.add(bv);
	}

	public void addAddAllViews(Collection<BannerView> subset) {
		this.views.addAll(subset);
	}

	@Override
	public Vector<BannerView> getViews() {
		return views;
	}

	@Override
	public User getUser(int uid) {
		return gd.fullDay.getUser(uid);
	}

	
}
