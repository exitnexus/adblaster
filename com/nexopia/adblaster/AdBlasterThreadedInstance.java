package com.nexopia.adblaster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.FlatFileConfig;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;

public class AdBlasterThreadedInstance extends AbstractAdBlasterInstance {
	private Vector<BannerView> views;
	private GlobalData gd;
	private BannerViewFlatFileReader db;
	private UserFlatFileReader userDB;
	
	public AdBlasterThreadedInstance(GlobalData gd, int subset_num) {
		super(gd.universe);
		try {
			db = new BannerViewFlatFileReader(gd.bannerViewDirectory);
			db.load(subset_num);
			userDB = new UserFlatFileReader(gd.bannerViewDirectory);
			userDB.load(subset_num);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gd = gd;
		views = new Vector<BannerView>();
		for (BannerView bv : db.getCurrentBannerViews()){
			views.add(bv);
			Banner b = universe.getBannerByID(bv.getBannerId());
			if (!this.bannerCountMap.containsKey(b))
				this.bannerCountMap.put(b, Integer.valueOf(0));
			this.updateMap(bv);
		}
		System.out.println("Loaded " + views.size() + " banner views.");
	}

	public float totalProfit(){
		int i = 0;
		System.out.println("Calculating profit on " + getViews().size() + " views.");
		float count = 0;
		long time = System.currentTimeMillis();
		for (BannerView bv : getViews()){ 
			i++;
			if (System.currentTimeMillis() - time > 5000){
				System.out.println("Percent: " + i/getViewCount());
				time = System.currentTimeMillis();
			}
			if (bv == null){
				throw new UnsupportedOperationException("Null bannerview in the list!");
			}
			if (bv.getBannerId() == 0){
				continue;
			}
			count += universe.getBannerByID(bv.getBannerId()).getRealPayrate();
		}
		System.out.println("profit:" + count);
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
				System.out.println("Percent: " + (float)i / (float)getViewCount());
				time = System.currentTimeMillis();
			}
			Banner b = pol.getBestBanner(this, bv);
			//Banner b = universe.getRandomBannerMatching(bv, this);
			notifyChange(bv, b);
			bv.setBanner(b);
		}
	}

	public Vector<Banner> getUnserved() {
		/**Loaded bannerview
		 * For a particular instance, get a list of all of the banners that were not served
		 * that could have made a profit.
		 * @return A vector of banners.
		 */
		Vector<Banner> unserved = new Vector<Banner>();
		Collection banners = this.universe.getBanners();
		Banner b = null;
		for (Iterator i = banners.iterator(); i.hasNext(); ){
			b = (Banner)i.next();
			if (b == null){
				System.err.println("Error here: null banners in the list?");
			}  else {
				if (bannerCount(b) < (b.getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT) && campaignCount(b) < (b.getCampaign().getIntegerMaxViewsPerDay()/FlatFileConfig.FILE_COUNT)){
					unserved.add(b);
				}
			}
		}
		return unserved;
	}

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
		return userDB.getUser(uid);
	}

	
}
